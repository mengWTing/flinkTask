package cn.ffcs.is.mss.analyzer.flink.alert

import java.util
import java.util.{Date, Properties}
import cn.ffcs.is.mss.analyzer.druid.model.scala.AlertModel
import cn.ffcs.is.mss.analyzer.utils._
import com.ibm.icu.text.SimpleDateFormat
import org.apache.flink.api.common.functions.RichMapFunction
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaConsumer, FlinkKafkaProducer}
import org.apache.flink.streaming.util.serialization.SimpleStringSchema
import org.json.{JSONArray, JSONObject}

/**
 * @Auther chenwei
 * @Description 获取所有的告警发送到安管平台
 * @Date: Created in 2018/12/7 11:01
 * @Modified By
 */
object AlertStatistics {

  def main(args: Array[String]): Unit = {


    //根据传入的参数解析配置文件
    //        val args0 = "E:\\ffcs\\mss\\src\\main\\resources\\flink.ini"
    //        val confProperties = new IniProperties(args0)
    val confProperties = new IniProperties(args(0))


    //该任务的名字
    val jobName = confProperties.getValue(Constants.FLINK_ALERT_STATISTICS_CONFIG, Constants.ALERT_STATISTICS_JOB_NAME)
    //kafka Source的名字
    val kafkaSourceName = confProperties.getValue(Constants.FLINK_ALERT_STATISTICS_CONFIG, Constants
      .ALERT_STATISTICS_KAFKA_SOURCE_NAME)
    //告警 sink的名字
    val sqlSinkName = confProperties.getValue(Constants.FLINK_ALERT_STATISTICS_CONFIG, Constants
      .ALERT_STATISTICS_ALERT_SINK_NAME)

    //kafka Source的并行度
    val kafkaSourceParallelism = confProperties.getIntValue(Constants.FLINK_ALERT_STATISTICS_CONFIG, Constants
      .ALERT_STATISTICS_KAFKA_SOURCE_PARALLELISM)
    //发送告警的并行度
    val sqlSinkParallelism = confProperties.getIntValue(Constants.FLINK_ALERT_STATISTICS_CONFIG, Constants
      .ALERT_STATISTICS_ALERT_SINK_PARALLELISM)

    //----------------------------流安全
    //kafka的服务地址
    val brokerList = confProperties.getValue(Constants.FLINK_COMMON_CONFIG, Constants.KAFKA_BOOTSTRAP_SERVERS)
    //flink消费的group.id
    val groupId = confProperties.getValue(Constants.FLINK_ALERT_STATISTICS_CONFIG, Constants.ALERT_STATISTICS_GROUP_ID)
    //kafka的topic
    val topic = confProperties.getValue(Constants.FLINK_ALERT_STATISTICS_CONFIG, Constants.ALERT_STATISTICS_KAFKA_TOPIC)

    //----------------------------端到端系统

    //端到端系统的topic
    val end2endTopic = confProperties.getValue(Constants.FLINK_ALERT_STATISTICS_CONFIG, Constants
      .ALERT_STATISTICS_TO_END_TO_END_KAFKA_TOPIC)
    val end2endBrokerList = confProperties.getValue(Constants.FLINK_ALERT_STATISTICS_CONFIG, Constants
      .ALERT_STATISTICS_TO_END_TO_END_KAFKA_BROKER_LIST)

    //----------------------------风控
    val resikTopic = confProperties.getValue(Constants.FLINK_ALERT_STATISTICS_CONFIG, Constants
      .ALERT_STATISTICS_TO_RISK_CONTROL_KAFKA_TOPIC)
    val resikBrokerList = confProperties.getValue(Constants.FLINK_ALERT_STATISTICS_CONFIG, Constants
      .ALERT_STATISTICS_TO_RISK_CONTROL_KAFKA_BROKER_LIST)


    //check pointing的间隔
    val checkpointInterval = confProperties.getLongValue(Constants.FLINK_ALERT_STATISTICS_CONFIG, Constants
      .ALERT_STATISTICS_CHECKPOINT_INTERVAL)
    //端到端producer配置

    val eteProducer = new FlinkKafkaProducer[String](end2endBrokerList, end2endTopic, new SimpleStringSchema())


    //风控producer配置
    val riskProducer = new FlinkKafkaProducer[String](resikBrokerList, resikTopic, new SimpleStringSchema())


    //设置kafka消费者相关配置
    val props = new Properties()
    //设置kafka集群地址
    props.setProperty("bootstrap.servers", brokerList)
    //设置flink消费的group.id
    props.setProperty("group.id", groupId)

    //获取kafka消费者
    val consumer = new FlinkKafkaConsumer[String](topic, new SimpleStringSchema, props).setStartFromGroupOffsets()

    //获取ExecutionEnvironment
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    //设置check pointing的间隔
    //env.enableCheckpointing(checkpointInterval)
    //设置流的时间为EventTime
    //    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)
    //设置flink全局变量
    //    env.getConfig.setGlobalJobParameters(parameters)


    //获取kafka数据
    val dStream = env.addSource(consumer).setParallelism(kafkaSourceParallelism)
      .uid(kafkaSourceName).name(kafkaSourceName)
    val alertStream = dStream
      .map(line => {
        try {
          Some(JsonUtil.fromJson[AlertModel](line))
        } catch {
          case e: Exception => None
        }
      })
      .filter(_.isDefined)
      .map(_.head)

    val riskControl = alertStream
      .map(alertModel => Array[AlertModel](alertModel))
      .map(array => {
        var timeStamp = 0L
        val jsonArray = new JSONArray()
        array.foreach(alertModel => {
          timeStamp = alertModel.eventTimeStamp
          val jsonObject = new JSONObject(alertModel.toJson)
          jsonArray.put(jsonObject)
        })
        (jsonArray.toString, timeStamp)
      })


    val value = alertStream.map(new AlertCombineMapFunction)
    value.addSink(eteProducer)
      .setParallelism(sqlSinkParallelism)

    value.addSink(riskProducer)
      .setParallelism(sqlSinkParallelism)
    //写入进行告警归并的kafka
    //    alertStream.map(new AlertCombineMapFunction)
    //      .print()
    ////     .addSink(new AlertCombinSinkFunction(combineTopic))


    //发送告警数据到端到端系统(由于暂时webShell误告警过多,先不发送这部分告警)
    //    alertStream
    //      .filter(!_.alertName.equals("webshell告警"))
    //      .map(_.toJson)
    //      .addSink(new EndToEndSinkFunction(end2endTopic)).setParallelism(sqlSinkParallelism)


    env.execute(jobName)

  }


  /**
   *
   * @param
   * @return
   * @author liangzhaosuo
   * @date 2020/09/01 9:49
   * @description 将AlertModel转换成可以进行归并的告警对象
   * @update [no][date YYYY-MM-DD][name][description]
   */
  class AlertCombineMapFunction extends RichMapFunction[AlertModel, String] {
    override def map(value: AlertModel): String = {
      val alertMap = new util.HashMap[String, Any]()
      //规则id,给告警规则表中的主键id
      alertMap.put("ruleId", value.alertRuleId)
      //告警类型 0-是关联告警
      alertMap.put("alertType", 0)

      try {
        alertMap.put("timestamp", AlertModel.getAlertTime("yyyy-MM-dd HH:mm:ss", value.alertTimestamp))
      } catch {
        case e: Exception =>
          alertMap.put("timestamp", AlertModel.getAlertTime("yyyy-MM-dd HH:mm:ss", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())
          ))
      }

      alertMap.put("alertName", value.alertName)
      alertMap.put("alertLevel", value.alertLevel)
      //风险类型-该字段需要在flink.ini中进行配置(ALERT_STATISTICS_DDOS_ALERT_TYPE),查找dis_correlation_rule表的typeid(type_alert表)
      alertMap.put("ruleType", value.alertType)
      alertMap.put("assetId", 81328)
      alertMap.put("alertCount", 1)
      alertMap.put("srcIp", value.alertSrcIp)
      alertMap.put("destIp", value.alertDestIp)
      alertMap.put("userName", value.alertUsername)
      alertMap.put("alertStatus", 0)
      alertMap.put("logInfo", value.alertDescription)
      alertMap.put("deleted", 0)
      alertMap.put("srcPort", 0)
      alertMap.put("destPort", 0)
      alertMap.put("hide", 0)
      alertMap.put("deviceIp", "10.142.82.178")
      //alertIp用来存储告警描述
      alertMap.put("alertInfo", value.alertIp)
      //alertDevice用来存储解决方案
      alertMap.put("solution", value.alertDevice)
      alertMap.put("logType", 23L)

      JsonUtil.toJson(alertMap)
    }
  }

}

/**
 *
 *
 * @return
 * @author PlatinaBoy
 * @date 2021/7/2 17:14
 * @description 旧代码留存
 * @update [no][date YYYY-MM-DD][name][description]
 */
//package cn.ffcs.is.mss.analyzer.flink.alert
//
//import java.io.{BufferedReader, InputStreamReader}
//import java.net.URI
//import java.text.SimpleDateFormat
//import java.util
//import java.util.{Date, Properties}
//
//import cn.ffcs.is.mss.analyzer.druid.model.scala.AlertModel
//import cn.ffcs.is.mss.analyzer.flink.sink.AlertSink
//import cn.ffcs.is.mss.analyzer.utils._
//import org.apache.flink.api.common.accumulators.LongCounter
//import org.apache.flink.api.common.functions.RichMapFunction
//import org.apache.flink.configuration.Configuration
//import org.apache.flink.streaming.api.TimeCharacteristic
//import org.apache.flink.streaming.api.functions.sink.{RichSinkFunction, SinkFunction}
//import org.apache.flink.streaming.api.functions.{AssignerWithPunctuatedWatermarks, ProcessFunction}
//import org.apache.flink.streaming.api.scala._
//import org.apache.flink.streaming.api.watermark.Watermark
//import org.apache.flink.streaming.api.windowing.time.Time
//import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaConsumer, FlinkKafkaProducer}
//import org.apache.flink.streaming.util.serialization.SimpleStringSchema
//import org.apache.flink.util.Collector
//import org.apache.hadoop.fs.{FileSystem, Path}
//import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
//import org.json.{JSONArray, JSONObject}
//
///**
// * @Auther chenwei
// * @Description 获取所有的告警发送到安管平台
// * @Date: Created in 2018/12/7 11:01
// * @Modified By
// */
//object AlertStatistics {
//
//  def main(args: Array[String]): Unit = {
//
//
//    //根据传入的参数解析配置文件
//    //        val args0 = "E:\\ffcs\\mss\\src\\main\\resources\\flink.ini"
//    //        val confProperties = new IniProperties(args0)
//    val confProperties = new IniProperties(args(0))
//
//
//    //该任务的名字
//    val jobName = confProperties.getValue(Constants.FLINK_ALERT_STATISTICS_CONFIG, Constants.ALERT_STATISTICS_JOB_NAME)
//    //kafka Source的名字
//    val kafkaSourceName = confProperties.getValue(Constants.FLINK_ALERT_STATISTICS_CONFIG, Constants
//      .ALERT_STATISTICS_KAFKA_SOURCE_NAME)
//    //告警 sink的名字
//    val sqlSinkName = confProperties.getValue(Constants.FLINK_ALERT_STATISTICS_CONFIG, Constants
//      .ALERT_STATISTICS_ALERT_SINK_NAME)
//
//    //kafka Source的并行度
//    val kafkaSourceParallelism = confProperties.getIntValue(Constants.FLINK_ALERT_STATISTICS_CONFIG, Constants
//      .ALERT_STATISTICS_KAFKA_SOURCE_PARALLELISM)
//    //发送告警的并行度
//    val sqlSinkParallelism = confProperties.getIntValue(Constants.FLINK_ALERT_STATISTICS_CONFIG, Constants
//      .ALERT_STATISTICS_ALERT_SINK_PARALLELISM)
//
//    //----------------------------流安全
//    //kafka的服务地址
//    val brokerList = confProperties.getValue(Constants.FLINK_COMMON_CONFIG, Constants.KAFKA_BOOTSTRAP_SERVERS)
//    //flink消费的group.id
//    val groupId = confProperties.getValue(Constants.FLINK_ALERT_STATISTICS_CONFIG, Constants.ALERT_STATISTICS_GROUP_ID)
//    //kafka的topic
//    val topic = confProperties.getValue(Constants.FLINK_ALERT_STATISTICS_CONFIG, Constants.ALERT_STATISTICS_KAFKA_TOPIC)
//
//    //----------------------------端到端系统
//
//    //端到端系统的topic
//    val end2endTopic = confProperties.getValue(Constants.FLINK_ALERT_STATISTICS_CONFIG, Constants
//      .ALERT_STATISTICS_TO_END_TO_END_KAFKA_TOPIC)
//
//    //----------------------------风控
//    val combineTopic = confProperties.getValue(Constants.FLINK_ALERT_STATISTICS_CONFIG, Constants
//      .ALERT_STATISTICS_TO_RISK_CONTROL_KAFKA_TOPIC)
//
//
//    //flink全局变量
//    val parameters: Configuration = new Configuration()
//    //ALERT_STATISTICS_ALERT_URL
//    parameters.setString(Constants.ALERT_STATISTICS_ALERT_URL, confProperties.getValue(Constants
//      .FLINK_ALERT_STATISTICS_CONFIG, Constants.ALERT_STATISTICS_ALERT_URL))
//    //ALERT_STATISTICS_NAMESPACE_URI
//    parameters.setString(Constants.ALERT_STATISTICS_NAMESPACE_URI, confProperties.getValue(Constants
//      .FLINK_ALERT_STATISTICS_CONFIG, Constants.ALERT_STATISTICS_NAMESPACE_URI))
//    //ALERT_STATISTICS_LOCAL_PART
//    parameters.setString(Constants.ALERT_STATISTICS_LOCAL_PART, confProperties.getValue(Constants
//      .FLINK_ALERT_STATISTICS_CONFIG, Constants.ALERT_STATISTICS_LOCAL_PART))
//
//    parameters.setLong(Constants.ALERT_STATISTICS_SUBSOC_ID, confProperties.getLongValue(Constants
//      .FLINK_ALERT_STATISTICS_CONFIG, Constants.ALERT_STATISTICS_SUBSOC_ID))
//    parameters.setString(Constants.ALERT_STATISTICS_KEY, confProperties.getValue(Constants
//      .FLINK_ALERT_STATISTICS_CONFIG, Constants.ALERT_STATISTICS_KEY))
//    parameters.setLong(Constants.ALERT_STATISTICS_INTERFACE_TYPE, confProperties.getLongValue(Constants
//      .FLINK_ALERT_STATISTICS_CONFIG, Constants.ALERT_STATISTICS_INTERFACE_TYPE))
//    parameters.setString(Constants.ALERT_STATISTICS_TIMESTAMP_FORMAT, confProperties.getValue(Constants
//      .FLINK_ALERT_STATISTICS_CONFIG, Constants.ALERT_STATISTICS_TIMESTAMP_FORMAT))
//
//    //端到端系统的kafka服务地址
//    parameters.setString(Constants.ALERT_STATISTICS_TO_END_TO_END_KAFKA_BROKER_LIST, confProperties.getValue
//    (Constants.FLINK_ALERT_STATISTICS_CONFIG, Constants.ALERT_STATISTICS_TO_END_TO_END_KAFKA_BROKER_LIST))
//
//    //到风控平台的kafka集群地址
//    parameters.setString(Constants.ALERT_STATISTICS_TO_RISK_CONTROL_KAFKA_BROKER_LIST, confProperties.getValue
//    (Constants.FLINK_ALERT_STATISTICS_CONFIG, Constants.ALERT_STATISTICS_TO_RISK_CONTROL_KAFKA_BROKER_LIST))
//
//
//    //check pointing的间隔
//    val checkpointInterval = confProperties.getLongValue(Constants.FLINK_ALERT_STATISTICS_CONFIG, Constants
//      .ALERT_STATISTICS_CHECKPOINT_INTERVAL)
//    //端到端producer配置
//
//    val eteProducer = new FlinkKafkaProducer[String](brokerList, sinkTopic, new SimpleStringSchema())
//
//
//    //风控producer配置
//    val riskProducer = new FlinkKafkaProducer[String](brokerList, sinkTopic, new SimpleStringSchema())
//
//
//
//    val watermark = confProperties.getLongValue(Constants.FLINK_ALERT_STATISTICS_CONFIG, Constants
//      .ALERT_STATISTICS_WATERMARK)
//    val windowSize = confProperties.getLongValue(Constants.FLINK_ALERT_STATISTICS_CONFIG, Constants
//      .ALERT_STATISTICS_WINDOW_SIZE)
//    val slideSize = confProperties.getLongValue(Constants.FLINK_ALERT_STATISTICS_CONFIG, Constants
//      .ALERT_STATISTICS_SLIDE_SIZE)
//
//    //设置kafka消费者相关配置
//    val props = new Properties()
//    //设置kafka集群地址
//    props.setProperty("bootstrap.servers", brokerList)
//    //设置flink消费的group.id
//    props.setProperty("group.id", groupId)
//
//    //获取kafka消费者
//    val consumer = new FlinkKafkaConsumer[String](topic, new SimpleStringSchema, props).setStartFromGroupOffsets()
//
//
//    //发送到端到端系统的producer
//    //    val endToEndProducer = new FlinkKafkaProducer09[String](end2endBrokerList, end2endTopic, new
//    // SimpleStringSchema())
//
//    //获取ExecutionEnvironment
//    val env = StreamExecutionEnvironment.getExecutionEnvironment
//    //设置check pointing的间隔
//    env.enableCheckpointing(checkpointInterval)
//    //设置流的时间为EventTime
//    //    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)
//    //设置flink全局变量
//    env.getConfig.setGlobalJobParameters(parameters)
//
//
//    //获取kafka数据
//    val dStream = env.addSource(consumer).setParallelism(kafkaSourceParallelism)
//      .uid(kafkaSourceName).name(kafkaSourceName)
//
//
//    //val dStream = env.readTextFile("/Users/chenwei/IdeaProjects/mss/src/main/resources/Test2.txt")
//    val alertStream = dStream
//      .map(line => {
//        try {
//          Some(JsonUtil.fromJson[AlertModel](line))
//        } catch {
//          case e: Exception => None
//        }
//      })
//      .filter(_.isDefined)
//      .map(_.head)
//
//    val riskControl = alertStream
//      .map(alertModel => Array[AlertModel](alertModel))
//      //      .assignTimestampsAndWatermarks(new AssignerWithPunctuatedWatermarks[Array[AlertModel]] {
//      //        override def checkAndGetNextWatermark(lastElement: Array[AlertModel], extractedTimestamp: Long)
//      // : Watermark =
//      //
//      //          new Watermark(extractedTimestamp - 1000)
//      //
//      //        override def extractTimestamp(element: Array[AlertModel], previousElementTimestamp: Long): Long =
//      //          element.head.eventTimeStamp
//      //      })
//      //      .timeWindowAll(Time.milliseconds(5000), Time.milliseconds(5000))
//      //      .reduce((array1, array2) => {
//      //        array1 ++ array2
//      //      })
//      .map(array => {
//        var timeStamp = 0L
//        val jsonArray = new JSONArray()
//        array.foreach(alertModel => {
//          timeStamp = alertModel.eventTimeStamp
//          val jsonObject = new JSONObject(alertModel.toJson)
//          jsonArray.put(jsonObject)
//        })
//        (jsonArray.toString, timeStamp)
//      })
//
//    //    riskControl.addSink(new AlertSink()).setParallelism(sqlSinkParallelism)
//    //      .uid(sqlSinkName).name(sqlSinkName)
//
//    val value = alertStream.map(new AlertCombineMapFunction)
//
//
//    //写入进行告警归并的kafka
//    //    alertStream.map(new AlertCombineMapFunction)
//    //      .print()
//    ////     .addSink(new AlertCombinSinkFunction(combineTopic))
//
//
//
//
//    //发送告警数据到端到端系统(由于暂时webShell误告警过多,先不发送这部分告警)
//    //    alertStream
//    //      .filter(!_.alertName.equals("webshell告警"))
//    //      .map(_.toJson)
//    //      .addSink(new EndToEndSinkFunction(end2endTopic)).setParallelism(sqlSinkParallelism)
//
//
//    env.execute(jobName)
//
//  }
//
//
//  //  /**
//  //    *
//  //    * @param null
//  //    * @return
//  //    * @author liangzhaosuo
//  //    * @date 2020/09/01 17:42
//  //    * @description 发送到风控平台kafka集群
//  //    * @update [no][date YYYY-MM-DD][name][description]
//  //    */
//  //  class AlertCombinSinkFunction(combineTopic: String) extends RichSinkFunction[String] {
//  //    var producer: KafkaProducer[String, String] = _
//  //
//  //    override def open(parameters: Configuration): Unit = {
//  //      //获取全局变量
//  //      val globConf = getRuntimeContext.getExecutionConfig.getGlobalJobParameters
//  //        .asInstanceOf[Configuration]
//  //      val brokerList = globConf.getString(Constants.ALERT_STATISTICS_TO_RISK_CONTROL_KAFKA_BROKER_LIST, "")
//  //      val prop = new Properties()
//  //      prop.setProperty("bootstrap.servers", brokerList)
//  //      prop.setProperty("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
//  //      prop.setProperty("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
//  //
//  //      producer = new KafkaProducer[String, String](prop)
//  //    }
//  //
//  //    override def invoke(value: String): Unit = {
//  //      producer.send(new ProducerRecord[String, String](combineTopic, System.currentTimeMillis().toString, value))
//  //      //因为没有设置props中的一些设置 ，必须手动刷新过去 ，否则数据一直不发送 ，估计是默认的刷新大小没达到
//  //      producer.flush()
//  //    }
//  //  }
//
//
//  /**
//   *
//   * @param
//   * @return
//   * @author liangzhaosuo
//   * @date 2020/09/01 9:49
//   * @description 将AlertModel转换成可以进行归并的告警对象
//   * @update [no][date YYYY-MM-DD][name][description]
//   */
//  class AlertCombineMapFunction extends RichMapFunction[AlertModel, String] {
//
//
//    override def map(value: AlertModel): String = {
//      val alertMap = new util.HashMap[String, Any]()
//      //规则id,给告警规则表中的主键id
//      alertMap.put("ruleId", value.alertRuleId)
//      //告警类型 0-是关联告警
//      alertMap.put("alertType", 0)
//      alertMap.put("timestamp", AlertModel.getAlertTime("yyyy-MM-dd HH:mm:ss", value.alertTimestamp))
//      alertMap.put("alertName", value.alertName)
//      alertMap.put("alertLevel", value.alertLevel)
//      //风险类型-该字段需要在flink.ini中进行配置(ALERT_STATISTICS_DDOS_ALERT_TYPE),查找dis_correlation_rule表的typeid(type_alert表)
//      alertMap.put("ruleType", value.alertType)
//      alertMap.put("assetId", 81328)
//      alertMap.put("alertCount", 1)
//      alertMap.put("srcIp", value.alertSrcIp)
//      alertMap.put("destIp", value.alertDestIp)
//      alertMap.put("userName", value.alertUsername)
//      alertMap.put("alertStatus", 0)
//      alertMap.put("logInfo", value.alertDescription)
//      alertMap.put("deleted", 0)
//      alertMap.put("srcPort", 0)
//      alertMap.put("destPort", 0)
//      alertMap.put("hide", 0)
//      alertMap.put("deviceIp", "10.142.82.178")
//      //alertIp用来存储告警描述
//      alertMap.put("alertInfo", value.alertIp)
//      //alertDevice用来存储解决方案
//      alertMap.put("solution", value.alertDevice)
//      alertMap.put("logType", 23L)
//
//      JsonUtil.toJson(alertMap)
//    }
//  }
//
//
//  //  class EndToEndSinkFunction(sinkTopic: String) extends RichSinkFunction[String] {
//  //    var producer: KafkaProducer[String, String] = _
//  //
//  //    override def open(parameters: Configuration): Unit = {
//  //      //获取全局变量
//  //      val globConf = getRuntimeContext.getExecutionConfig.getGlobalJobParameters
//  //        .asInstanceOf[Configuration]
//  //      val brokerList = globConf.getString(Constants.ALERT_STATISTICS_TO_END_TO_END_KAFKA_BROKER_LIST, "")
//  //      val prop = new Properties()
//  //      prop.setProperty("bootstrap.servers", brokerList)
//  //      prop.setProperty("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
//  //      prop.setProperty("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
//  //
//  //      producer = new KafkaProducer[String, String](prop)
//  //    }
//  //
//  //    override def invoke(value: String): Unit = {
//  //      producer.send(new ProducerRecord[String, String](sinkTopic, System.currentTimeMillis().toString, value))
//  //      //因为没有设置props中的一些设置 ，必须手动刷新过去 ，否则数据一直不发送 ，估计是默认的刷新大小没达到
//  //      producer.flush()
//  //    }
//  //  }
//
//}