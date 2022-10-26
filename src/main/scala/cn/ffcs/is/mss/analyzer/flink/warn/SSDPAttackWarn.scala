package cn.ffcs.is.mss.analyzer.flink.warn

import java.sql.Timestamp
import java.util.Properties

import cn.ffcs.is.mss.analyzer.bean.DdosWarnEntity
import cn.ffcs.is.mss.analyzer.druid.model.scala.QuintetModel
import cn.ffcs.is.mss.analyzer.flink.sink.MySQLSink
import cn.ffcs.is.mss.analyzer.utils.{Constants, IniProperties, JsonUtil}
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.functions.{AssignerWithPunctuatedWatermarks, ProcessFunction}
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.watermark.Watermark
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaConsumer, FlinkKafkaProducer}
import org.apache.flink.streaming.util.serialization.SimpleStringSchema
import org.apache.flink.util.Collector

import scala.collection.mutable.ArrayBuffer

object SSDPAttackWarn {
  def main(args: Array[String]): Unit = {
    //val args0 = "E:\ffcs\mss\src\main\resources\flink.ini"
    //val confProperties = new IniProperties(args0)
    val confProperties = new IniProperties(args(0))


    //该任务的名字
    val jobName = confProperties.getValue(Constants.FLINK_DDOS_SSDP_DETECT_CONFIG, Constants
      .DDOS_SSDP_DETECT_JOB_NAME)
    //kafka Source的名字
    val kafkaSourceName = confProperties.getValue(Constants.FLINK_DDOS_SSDP_DETECT_CONFIG, Constants
      .DDOS_SSDP_DETECT_KAFKA_SOURCE_NAME)
    //mysql sink的名字
    val sqlSinkName = confProperties.getValue(Constants.FLINK_DDOS_SSDP_DETECT_CONFIG, Constants
      .DDOS_SSDP_DETECT_SQL_SINK_NAME)
    //kafka sink的名字
    val kafkaSinkName = confProperties.getValue(Constants.FLINK_DDOS_SSDP_DETECT_CONFIG, Constants
      .DDOS_SSDP_DETECT_KAFKA_SINK_NAME)


    //kafka Source的并行度
    val kafkaSourceParallelism = confProperties.getIntValue(Constants.FLINK_DDOS_SSDP_DETECT_CONFIG,
      Constants.DDOS_SSDP_DETECT_KAFKA_SOURCE_PARALLELISM)
    //对数据处理的并行度
    val dealParallelism = confProperties.getIntValue(Constants.FLINK_DDOS_SSDP_DETECT_CONFIG,
      Constants.DDOS_SSDP_DETECT_DEAL_PARALLELISM)
    //写入mysql的并行度
    val sqlSinkParallelism = confProperties.getIntValue(Constants.FLINK_DDOS_SSDP_DETECT_CONFIG,
      Constants.DDOS_SSDP_DETECT_SQL_SINK_PARALLELISM)
    //写入kafka的并行度
    val kafkaSinkParallelism = confProperties.getIntValue(Constants.FLINK_DDOS_SSDP_DETECT_CONFIG,
      Constants.DDOS_SSDP_DETECT_KAFKA_SINK_PARALLELISM)
    //check pointing的间隔
    val checkpointInterval = confProperties.getLongValue(Constants.FLINK_DDOS_SSDP_DETECT_CONFIG,
      Constants.DDOS_SSDP_DETECT_CHECKPOINT_INTERVAL)

    //kafka的服务地址
    val brokerList = confProperties.getValue(Constants.FLINK_COMMON_CONFIG, Constants
      .KAFKA_BOOTSTRAP_SERVERS)
    //flink消费的group.id
    val groupId = confProperties.getValue(Constants.FLINK_DDOS_SSDP_DETECT_CONFIG, Constants
      .DDOS_SSDP_DETECT_GROUP_ID)
    //kafka source 的topic
    val kafkaSourceTopic = confProperties.getValue(Constants.QUINTET_FLINK_TO_DRUID_CONFIG,
      Constants.QUINTET_TO_KAFKA_TOPIC)
    //kafka sink 的topic
    val kafkaSinkTopic = confProperties.getValue(Constants.FLINK_DDOS_SSDP_DETECT_CONFIG, Constants
      .DDOS_SSDP_DETECT_KAFKA_SINK_TOPIC)

    val warningSinkTopic = confProperties.getValue(Constants.WARNING_FLINK_TO_DRUID_CONFIG, Constants
      .WARNING_TOPIC)

    val parameters: Configuration = new Configuration()

    parameters.setInteger(Constants.DDOS_SSDP_DETECT_THRESHOLD, confProperties.getIntValue(Constants
      .FLINK_DDOS_SSDP_DETECT_CONFIG, Constants.DDOS_SSDP_DETECT_THRESHOLD))
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
      .map(JsonUtil.fromJson[QuintetModel] _).setParallelism(dealParallelism)
      .filter(model => {
        "1900".equals(model.destinationPort) && "0".equals(model.protocol)
      }).setParallelism(dealParallelism)
      .assignTimestampsAndWatermarks(new AssignerWithPunctuatedWatermarks[QuintetModel] {
        override def
        checkAndGetNextWatermark(lastElement: QuintetModel, extractedTimestamp: Long): Watermark = {
          new Watermark(extractedTimestamp - 10000)
        }

        override def extractTimestamp(element: QuintetModel, previousElementTimestamp: Long): Long = {
          element.timeStamp
        }
      })
      .map(model => {
        (model.timeStamp / 1000 / 60 * 1000 * 60 + "-" + model.destinationIp, ArrayBuffer[QuintetModel](model))
      }).setParallelism(dealParallelism)
      .keyBy(0)
      .timeWindow(Time.minutes(1L), Time.minutes(1L))
      .reduce((o1, o2) => {
        (o1._1, o1._2.++:(o2._2)
        )
      })
      .process(new SSDPProcessFunction).setParallelism(dealParallelism)


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

  class SSDPProcessFunction extends ProcessFunction[(String, ArrayBuffer[QuintetModel]), (Object, Boolean)] {
    var threshold: Int = _

    override def open(parameters: Configuration): Unit = {
      val globConf = getRuntimeContext.getExecutionConfig.getGlobalJobParameters.asInstanceOf[Configuration]

      threshold = globConf.getInteger(Constants.DDOS_SSDP_DETECT_THRESHOLD, 0)
    }

    override def processElement(value: (String, ArrayBuffer[QuintetModel]), ctx: ProcessFunction[(String,
      ArrayBuffer[QuintetModel]), (Object, Boolean)]#Context, out: Collector[(Object, Boolean)]): Unit = {

      if (value._2.size >= threshold) {
        val splits = value._1.split("-")
        val timestamp = splits(0).toLong
        val destIp = splits(1)
        val ssdpWarndEntity = new DdosWarnEntity
        ssdpWarndEntity.setDestIp(destIp)
        ssdpWarndEntity.setSourceIp(value._2.last.sourceIp)
        ssdpWarndEntity.setWarnTime(new Timestamp(timestamp))
        ssdpWarndEntity.setOccurCount(value._2.size)
        ssdpWarndEntity.setWarnType(1)
        out.collect((ssdpWarndEntity, true))
      }
    }
  }

}
