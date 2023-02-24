package cn.ffcs.is.mss.analyzer.flink.warn

import java.io.{BufferedReader, InputStreamReader}
import java.net.URI
import java.sql.Timestamp
import java.util.Properties

import cn.ffcs.is.mss.analyzer.bean.ActiveOutreachWarnEntity
import cn.ffcs.is.mss.analyzer.druid.model.scala.QuintetModel
import cn.ffcs.is.mss.analyzer.flink.sink.MySQLSink
import cn.ffcs.is.mss.analyzer.utils.{Constants, IniProperties, JsonUtil}
import org.apache.flink.api.common.accumulators.LongCounter
import org.apache.flink.api.common.functions.RichFilterFunction
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.ProcessFunction
import org.apache.flink.streaming.api.scala.{StreamExecutionEnvironment, _}
import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaConsumer, FlinkKafkaProducer}
import org.apache.flink.streaming.util.serialization.SimpleStringSchema
import org.apache.flink.util.Collector
import org.apache.hadoop.fs.{FileSystem, Path}

import scala.collection.mutable

/**
 * @title ActiveOutreachWarn
 * @author hanyu
 * @date 2020-09-08 19:12
 * @description
 * @update [no][date YYYY-MM-DD][name][description]
 */
object ActiveOutreachWarn {
  def main(args: Array[String]): Unit = {
    val confProperties = new IniProperties(args(0))
    //任务的名字
    val jobName = confProperties.getValue(Constants.ACTIVE_OUTREACH_ANALYZE_CONFIG, Constants
      .ACTIVE_OUTREACH_ANALYZE_JOB_NAME)


    //并行度
    val sourceParallelism = confProperties.getIntValue(Constants.ACTIVE_OUTREACH_ANALYZE_CONFIG,
      Constants.ACTIVE_OUTREACH_ANALYZE_SOURCE_PARALLELISM)
    val sinkParallelism = confProperties.getIntValue(Constants.ACTIVE_OUTREACH_ANALYZE_CONFIG,
      Constants.ACTIVE_OUTREACH_ANALYZE_SINK_PARALLELISM)
    val dealParallelism = confProperties.getIntValue(Constants.ACTIVE_OUTREACH_ANALYZE_CONFIG,
      Constants.ACTIVE_OUTREACH_ANALYZE_DEAL_PARALLELISM)


    //check pointing的间隔
    val checkpointInterval = confProperties.getLongValue(Constants.ACTIVE_OUTREACH_ANALYZE_CONFIG,
      Constants.ACTIVE_OUTREACH_ANALYZE_CHECKPOINT_INTERVAL)


    //kafka的服务地址
    val brokerList = confProperties.getValue(Constants.FLINK_COMMON_CONFIG, Constants
      .KAFKA_BOOTSTRAP_SERVERS)
    //flink消费的group.id
    val groupId = confProperties.getValue(Constants.ACTIVE_OUTREACH_ANALYZE_CONFIG, Constants
      .ACTIVE_OUTREACH_ANALYZE_GROUP_ID)
    //kafka source 的topic
    val topic = confProperties.getValue(Constants.ACTIVE_OUTREACH_ANALYZE_CONFIG, Constants
      .ACTIVE_OUTREACH_ANALYZE_KAFKA_SOURCE_TOPIC)
    //kafka sink topic
    val kafkaSinkTopic = confProperties.getValue(Constants.ACTIVE_OUTREACH_ANALYZE_CONFIG,
      Constants.ACTIVE_OUTREACH_ANALYZE_KAFKA_SINK_TOPIC)
    //写入kafka的并行度
    val kafkaSinkParallelism = confProperties.getIntValue(Constants.ACTIVE_OUTREACH_ANALYZE_CONFIG,
      Constants.ACTIVE_OUTREACH_ANALYZE_KAFKA_SINK_PARALLELISM)

    val warningSinkTopic = confProperties.getValue(Constants.WARNING_FLINK_TO_DRUID_CONFIG, Constants
      .WARNING_TOPIC)

    //安全流量阈值
    val flowMinValue = confProperties.getIntValue(Constants.ACTIVE_OUTREACH_ANALYZE_CONFIG,
      Constants.ACTIVE_OUTREACH_ANALYZE_FLOW_MIN_VALUE)

    //flink全局变量
    val parameters: Configuration = new Configuration()
    //配置文件系统类型
    parameters.setString(Constants.FILE_SYSTEM_TYPE, confProperties.getValue(Constants
      .FLINK_COMMON_CONFIG, Constants.FILE_SYSTEM_TYPE))
    //c3p0连接池配置文件路径
    parameters.setString(Constants.c3p0_CONFIG_PATH, confProperties.getValue(Constants
      .FLINK_COMMON_CONFIG, Constants.c3p0_CONFIG_PATH))
    //内网ip文件路径
    parameters.setString(Constants.ACTIVE_OUTREACH_ANALYZE_IP_WHITE_LIST, confProperties.getValue(Constants.
      ACTIVE_OUTREACH_ANALYZE_CONFIG, Constants.ACTIVE_OUTREACH_ANALYZE_IP_WHITE_LIST))
    //办公网ip文件路径
    parameters.setString(Constants.ACTIVE_OUTREACH_ANALYZE_OFFICE_IP, confProperties.getValue(Constants.
      ACTIVE_OUTREACH_ANALYZE_CONFIG, Constants.ACTIVE_OUTREACH_ANALYZE_OFFICE_IP))
    //端口白名单
    parameters.setString(Constants.ACTIVE_OUTREACH_ANALYZE_PORT_WHITE_LIST, confProperties.getValue(Constants.
      ACTIVE_OUTREACH_ANALYZE_CONFIG, Constants.ACTIVE_OUTREACH_ANALYZE_PORT_WHITE_LIST))
    //特殊ip名单
    parameters.setString(Constants.ACTIVE_OUTREACH_ANALYZE_SPECIAL_IP, confProperties.getValue(Constants.
      ACTIVE_OUTREACH_ANALYZE_CONFIG, Constants.ACTIVE_OUTREACH_ANALYZE_SPECIAL_IP))


    //设置kafka消费者相关配置
    //设置kafka消费者相关配置
    val props = new Properties()
    //设置kafka集群地址
    props.setProperty("bootstrap.servers", brokerList)
    //设置flink消费的group.id
    props.setProperty("group.id", groupId + "test")
    //获取kafka消费者
    val consumer = new FlinkKafkaConsumer[String](topic, new SimpleStringSchema, props)
      .setStartFromLatest()
    //获取kafka 生产者
    val producer = new FlinkKafkaProducer[String](brokerList, kafkaSinkTopic, new SimpleStringSchema())

    //获取ExecutionEnvironment
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    //设置check pointing的间隔
    env.enableCheckpointing(checkpointInterval)
    //设置flink全局变量
    env.getConfig.setGlobalJobParameters(parameters)
    val sinkData = env.addSource(consumer).setParallelism(sourceParallelism)
      .map(JsonUtil.fromJson[QuintetModel] _).setParallelism(dealParallelism)
      .filter(new RichFilterFunction[QuintetModel] {
        override def filter(value: QuintetModel): Boolean = {
          value.isSucceed.equals("1") || value.isSucceed.equals("连接已建立")
        }
      }).setParallelism(dealParallelism)
      .filter(_.outputOctets > flowMinValue).setParallelism(dealParallelism)
      .filter(_.destinationIp.contains(".")).setParallelism(dealParallelism)
      .process(new ActiveOutreachProcessFunction).setParallelism(dealParallelism)

    sinkData
      .addSink(new MySQLSink).setParallelism(sinkParallelism)

    sinkData
      .map(o => {
        JsonUtil.toJson(o._1.asInstanceOf[ActiveOutreachWarnEntity])
      })
      .addSink(producer)
      .setParallelism(kafkaSinkParallelism)

    //将告警数据写入告警库topic
    val warningProducer = new FlinkKafkaProducer[String](brokerList, warningSinkTopic, new
        SimpleStringSchema())

    sinkData.map(m => {
      var inPutKafkaValue = ""
      try {
        val entity = m._1.asInstanceOf[ActiveOutreachWarnEntity]
        inPutKafkaValue = "未知用户" + "|" + "主动外联" + "|" + entity.getAlertTime.getTime + "|" +
          "" + "|" + "" + "|" + "" + "|" +
          "" + "|" + entity.getSourceIp + "|" + entity.getSourcePort + "|" +
          entity.getDestinationIp + "|" + entity.getDestinationPort + "|" + "" + "|" +
          "" + "|" + "" + "|" + ""
      } catch {
        case e: Exception => {
        }
      }
      inPutKafkaValue
    }).addSink(warningProducer).setParallelism(sinkParallelism)

    env.execute(jobName)

  }

  /**
   *
   *
   * @return 判断ipStr是否为内网 内网返回True 外网返回False
   * @author hanyu
   * @date 2020/9/9 18:13
   * @description ipStr：需要判断的ip
   *              innerIp：内网ip字典
   *              specialIp：特殊ip字典
   *              端口白名单
   * @update [no][date YYYY-MM-DD][name][description]
   */
  def innerIpVerdict(ipStr: String, innerIp: mutable.HashSet[String], specialIp: String): Boolean = {
    val index1 = ipStr.lastIndexOf(".")
    if (!(-1).equals(index1)) {
      val destIpStr1 = ipStr.substring(0, index1)
      val index2 = destIpStr1.lastIndexOf(".")
      if (!(-1).equals(index2)) {
        val destipStr2 = ipStr.substring(0, index2)
        innerIp.contains(ipStr) || innerIp.contains(destIpStr1) || innerIp.contains(destipStr2) || specialIp.contains(destipStr2)
      } else {
        false
      }
    } else {
      false
    }
  }


  class ActiveOutreachProcessFunction extends ProcessFunction[QuintetModel, (Object, Boolean)] {
    var portWhiteList = ""
    var ipWhiteListPlath = ""
    var officePlaceListPlath = ""
    var specialIp = ""
    var innerNetIp = new mutable.HashSet[String]()
    var officePlaceIp = new mutable.HashSet[String]()

    override def open(parameters: Configuration): Unit = {
      val globConf = getRuntimeContext.getExecutionConfig.getGlobalJobParameters.asInstanceOf[Configuration]
      portWhiteList = globConf.getString(Constants.ACTIVE_OUTREACH_ANALYZE_PORT_WHITE_LIST, "")
      ipWhiteListPlath = globConf.getString(Constants.ACTIVE_OUTREACH_ANALYZE_IP_WHITE_LIST, "")
      officePlaceListPlath = globConf.getString(Constants.ACTIVE_OUTREACH_ANALYZE_OFFICE_IP, "")
      specialIp = globConf.getString(Constants.ACTIVE_OUTREACH_ANALYZE_SPECIAL_IP, "")

      val systemType = globConf.getString(Constants.FILE_SYSTEM_TYPE, "")
      val fs = FileSystem.get(URI.create(systemType), new org.apache.hadoop.conf.Configuration())

      val fsDataInputStream = fs.open(new Path(ipWhiteListPlath))
      val bufferedReader = new BufferedReader(new InputStreamReader(fsDataInputStream))
      //本地测试
      //      val stream = new FileReader(new File(""))
      //      val bufferedReader = new BufferedReader(stream)
      var line: String = bufferedReader.readLine()
      while (line != null) {
        val splits = line.split("\\|", -1)
        if (splits.length > 1) {
          innerNetIp += splits(0)
        }
        line = bufferedReader.readLine()
      }

      val officePlaceDataInputStream = fs.open(new Path(officePlaceListPlath))
      val officePlaceBR = new BufferedReader(new InputStreamReader(officePlaceDataInputStream))

      var officeLine: String = officePlaceBR.readLine()
      while (officeLine != null) {
        val splits = officeLine.split("\\|", -1)
        if (splits.length > 1) {
          officePlaceIp += splits(0)
        }
        officeLine = officePlaceBR.readLine()
      }


    }

    override def processElement(value: QuintetModel, ctx: ProcessFunction[QuintetModel, (Object, Boolean)]#Context,
                                out: Collector[(Object, Boolean)]): Unit = {
      val sourceIp = value.sourceIp
      val sourcePort = value.sourcePort
      val destIp = value.destinationIp
      val destPort = value.destinationPort
      val innerSourceBool = innerIpVerdict(sourceIp, innerNetIp, specialIp)
      val innerDestBool = innerIpVerdict(destIp, innerNetIp, specialIp)
      val officeSourceBool = innerIpVerdict(sourceIp, officePlaceIp, specialIp)
      val officeDestBool = innerIpVerdict(destIp, officePlaceIp, specialIp)
      val sourcePortBool = portWhiteList.contains(sourcePort)
      val destPortBool = portWhiteList.contains(destPort)
      val entity = new ActiveOutreachWarnEntity
      entity.setAlertTime(new Timestamp(value.timeStamp))
      entity.setDestinationIp(value.destinationIp)
      entity.setDestinationPort(value.destinationPort)
      entity.setSourceIp(value.sourceIp)
      entity.setSourcePort(value.sourcePort)
      entity.setOutPutOctets(value.outputOctets)
      if (innerSourceBool && !sourcePortBool && !destPortBool && !officeDestBool && !innerDestBool) {
        entity.setIsOffice(0)

        out.collect(entity, true)
      }
      if (officeSourceBool && !sourcePortBool && !destPortBool && !officeDestBool && !innerDestBool) {
        entity.setIsOffice(1)
        out.collect(entity, true)

      }
    }
  }

}

