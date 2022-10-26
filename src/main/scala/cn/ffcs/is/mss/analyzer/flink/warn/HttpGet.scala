package cn.ffcs.is.mss.analyzer.flink.warn

import java.sql.Timestamp
import java.util.Properties

import cn.ffcs.is.mss.analyzer.bean.DdosWarnEntity
import cn.ffcs.is.mss.analyzer.druid.model.scala.OperationModel
import cn.ffcs.is.mss.analyzer.flink.sink.MySQLSink
import cn.ffcs.is.mss.analyzer.utils.{Constants, IniProperties, JsonUtil}
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.functions.{AssignerWithPunctuatedWatermarks, ProcessFunction}
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaConsumer, FlinkKafkaProducer}
import org.apache.flink.streaming.util.serialization.SimpleStringSchema
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.watermark.Watermark
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.util.Collector

/**
  * 读取业务话单
  * 增加一个时间字段，对时间取整
  * 按照取整后的时间字段和目的ip对数据进行group by
  * 对单位时间内单个目的IP的所有数据进行处理
  * 判断ua是否可以正常解析到操作系统
  * 判断ua是否可以正常解析到浏览器
  * refer是否为空
  */
object HttpGet {
  def main(args: Array[String]): Unit = {
    //val args0 = "E:\ffcs\mss\src\main\resources\flink.ini"
    //val confProperties = new IniProperties(args0)
    val confProperties = new IniProperties(args(0))


    //该任务的名字
    val jobName = confProperties.getValue(Constants.FLINK_DDOS_HTTP_GET_DETECT_CONFIG, Constants
      .DDOS_HTTP_GET_DETECT_JOB_NAME)
    //kafka Source的名字
    val kafkaSourceName = confProperties.getValue(Constants.FLINK_DDOS_HTTP_GET_DETECT_CONFIG, Constants
      .DDOS_HTTP_GET_DETECT_KAFKA_SOURCE_NAME)
    //mysql sink的名字
    val sqlSinkName = confProperties.getValue(Constants.FLINK_DDOS_HTTP_GET_DETECT_CONFIG, Constants
      .DDOS_HTTP_GET_DETECT_SQL_SINK_NAME)
    //kafka sink的名字
    val kafkaSinkName = confProperties.getValue(Constants.FLINK_DDOS_HTTP_GET_DETECT_CONFIG, Constants
      .DDOS_HTTP_GET_DETECT_KAFKA_SINK_NAME)


    //kafka Source的并行度
    val kafkaSourceParallelism = confProperties.getIntValue(Constants.FLINK_DDOS_HTTP_GET_DETECT_CONFIG,
      Constants.DDOS_HTTP_GET_DETECT_KAFKA_SOURCE_PARALLELISM)
    //对数据处理的并行度
    val dealParallelism = confProperties.getIntValue(Constants.FLINK_DDOS_HTTP_GET_DETECT_CONFIG,
      Constants.DDOS_HTTP_GET_DETECT_DEAL_PARALLELISM)
    //写入mysql的并行度
    val sqlSinkParallelism = confProperties.getIntValue(Constants.FLINK_DDOS_HTTP_GET_DETECT_CONFIG,
      Constants.DDOS_HTTP_GET_DETECT_SQL_SINK_PARALLELISM)
    //写入kafka的并行度
    val kafkaSinkParallelism = confProperties.getIntValue(Constants.FLINK_DDOS_HTTP_GET_DETECT_CONFIG,
      Constants.DDOS_HTTP_GET_DETECT_KAFKA_SINK_PARALLELISM)
    //check pointing的间隔
    val checkpointInterval = confProperties.getLongValue(Constants.FLINK_DDOS_HTTP_GET_DETECT_CONFIG,
      Constants.DDOS_HTTP_GET_DETECT_CHECKPOINT_INTERVAL)

    //kafka的服务地址
    val brokerList = confProperties.getValue(Constants.FLINK_COMMON_CONFIG, Constants
      .KAFKA_BOOTSTRAP_SERVERS)
    //flink消费的group.id
    val groupId = confProperties.getValue(Constants.FLINK_DDOS_HTTP_GET_DETECT_CONFIG, Constants
      .DDOS_HTTP_GET_DETECT_GROUP_ID)
    //kafka source 的topic
    val kafkaSourceTopic = confProperties.getValue(Constants.OPERATION_FLINK_TO_DRUID_CONFIG,
      Constants.OPERATION_TOPIC)
    //kafka sink 的topic
    val kafkaSinkTopic = confProperties.getValue(Constants.FLINK_DDOS_HTTP_GET_DETECT_CONFIG, Constants
      .DDOS_HTTP_GET_DETECT_KAFKA_SINK_TOPIC)

    val warningSinkTopic = confProperties.getValue(Constants.WARNING_FLINK_TO_DRUID_CONFIG, Constants
      .WARNING_TOPIC)

    val parameters: Configuration = new Configuration()

    parameters.setInteger(Constants.DDOS_HTTP_GET_DETECT_THRESHOLD, confProperties.getIntValue(Constants
      .FLINK_DDOS_HTTP_GET_DETECT_CONFIG, Constants.DDOS_HTTP_GET_DETECT_THRESHOLD))
    //c3p0连接池配置文件路径
    parameters.setString(Constants.c3p0_CONFIG_PATH, confProperties.getValue(Constants.FLINK_COMMON_CONFIG, Constants
      .c3p0_CONFIG_PATH))

    val env = StreamExecutionEnvironment.getExecutionEnvironment

    env.enableCheckpointing(checkpointInterval)
    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)

    env.getConfig.setGlobalJobParameters(parameters)


    //设置kafka消费者相关配置
    val props = new Properties()
    //设置kafka集群地址
    props.setProperty("bootstrap.servers", brokerList)
    //设置flink消费的group.id
    props.setProperty("group.id", groupId)

    //获取kafka消费者
    val consumer = new FlinkKafkaConsumer[String](kafkaSourceTopic, new SimpleStringSchema, props)
      .setStartFromGroupOffsets()

    val alertData = env.addSource(consumer).setParallelism(kafkaSourceParallelism)
      .uid(kafkaSourceName).name(kafkaSourceName)
      .filter(line => {
        val splits = line.split("\\|", -1)
        //refer为空
        splits(8).length == 0 && splits.length > 10
      }).setParallelism(dealParallelism)
      .assignTimestampsAndWatermarks(new AssignerWithPunctuatedWatermarks[String] {
        override def
        checkAndGetNextWatermark(lastElement: String, extractedTimestamp: Long): Watermark = {
          new Watermark(extractedTimestamp - 10000)
        }

        override def extractTimestamp(element: String, previousElementTimestamp: Long): Long = {
          element.split("\\|", -1)(10).trim.toLong
        }
      })
      .map(line => {
        val splits = line.split("\\|", -1)
        (splits(10).trim.toLong / 1000 / 60 * 1000 * 60 + "|" + splits(1), splits(3), 1)
      })
      .keyBy(0)
      .timeWindow(Time.minutes(1), Time.minutes(1))
      .reduce((o1, o2) => {
        (o1._1, o2._2, o1._3 + o2._3)
      })
      .process(new HttpGetProcessFunction)

    alertData.addSink(new MySQLSink)
      .uid(sqlSinkName)
      .name(sqlSinkName)
      .setParallelism(sqlSinkParallelism)

    //获取kafka生产者
    val producer = new FlinkKafkaProducer[String](brokerList, kafkaSinkTopic, new SimpleStringSchema())
    alertData.map(m => {
      JsonUtil.toJson(m._1.asInstanceOf[DdosWarnEntity])
    })
      .addSink(producer)
      .uid(kafkaSinkName)
      .name(kafkaSinkName)
      .setParallelism(kafkaSinkParallelism)


    //将告警数据写入告警库topic
    val warningProducer = new FlinkKafkaProducer[String](brokerList, warningSinkTopic, new
        SimpleStringSchema())
    alertData.map(m => {
      var inPutKafkaValue = ""
      try {
        val entity = m._1.asInstanceOf[DdosWarnEntity]
        inPutKafkaValue = "未知用户" + "|" + "DDOS攻击" + "|" + entity.getWarnTime.getTime + "|" +
          "" + "|" + "" + "|" + "" + "|" +
          "" + "|" + entity.getSourceIp + "|" + "" + "|" +
          entity.getDestIp + "|" + "" + "|" + "" + "|" +
          "" + "|" + "" + "|" + ""
      } catch {
        case e: Exception => {
        }
      }
      inPutKafkaValue
    }).addSink(warningProducer).setParallelism(kafkaSinkParallelism)
    env.execute(jobName)


  }

  class HttpGetProcessFunction extends ProcessFunction[(String, String, Int), (Object, Boolean)] {
    var threshold: Int = _


    override def open(parameters: Configuration): Unit = {
      val globConf = getRuntimeContext.getExecutionConfig.getGlobalJobParameters.asInstanceOf[Configuration]
      threshold = globConf.getInteger(Constants.DDOS_HTTP_GET_DETECT_THRESHOLD, 0)
    }


    override def processElement(value: (String, String, Int), ctx: ProcessFunction[(String, String, Int), (Object,
      Boolean)]#Context, out: Collector[(Object, Boolean)]): Unit = {
      if (value._3 >= threshold) {
        val splits = value._1.split("\\|", -1)
        val timestamp = splits(0).toLong
        val destIp = splits(1)

        val httpGetWarn = new DdosWarnEntity
        httpGetWarn.setSourceIp(value._2)
        httpGetWarn.setDestIp(destIp)
        httpGetWarn.setWarnTime(new Timestamp(timestamp))
        httpGetWarn.setOccurCount(value._3)
        httpGetWarn.setWarnType(2)

        out.collect((httpGetWarn, true))
      }
    }
  }

}
