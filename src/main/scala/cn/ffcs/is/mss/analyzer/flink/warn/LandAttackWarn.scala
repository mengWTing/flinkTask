package cn.ffcs.is.mss.analyzer.flink.warn

import java.sql.Timestamp
import java.util.Properties

import cn.ffcs.is.mss.analyzer.bean.DdosWarnEntity
import cn.ffcs.is.mss.analyzer.druid.model.scala.QuintetModel
import cn.ffcs.is.mss.analyzer.flink.sink.{MySQLSink, Sink}
import cn.ffcs.is.mss.analyzer.flink.source.Source
import cn.ffcs.is.mss.analyzer.utils.{Constants, IniProperties, JsonUtil}
import org.apache.flink.api.common.eventtime.WatermarkStrategy
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.ProcessFunction
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.util.Collector
import org.apache.flink.streaming.api.scala._

/**
  * 具有相同源地址和目标地址
  * TCP协议
  */
object LandAttackWarn {
  def main(args: Array[String]): Unit = {
    //根据传入的参数解析配置文件
    //val args0 = "E:\ffcs\mss\src\main\resources\flink.ini"
    //val confProperties = new IniProperties(args0)
    val confProperties = new IniProperties(args(0))


    //该任务的名字
    val jobName = confProperties.getValue(Constants.FLINK_DDOS_LAND_DETECT_CONFIG, Constants
      .DDOS_LAND_DETECT_JOB_NAME)
    //kafka Source的名字
    val kafkaSourceName = confProperties.getValue(Constants.FLINK_DDOS_LAND_DETECT_CONFIG, Constants
      .DDOS_LAND_DETECT_KAFKA_SOURCE_NAME)
    //mysql sink的名字
    val sqlSinkName = confProperties.getValue(Constants.FLINK_DDOS_LAND_DETECT_CONFIG, Constants
      .DDOS_LAND_DETECT_SQL_SINK_NAME)
    //kafka sink的名字
    val kafkaSinkName = confProperties.getValue(Constants.FLINK_DDOS_LAND_DETECT_CONFIG, Constants
      .DDOS_LAND_DETECT_KAFKA_SINK_NAME)


    //kafka Source的并行度
    val kafkaSourceParallelism = confProperties.getIntValue(Constants.FLINK_DDOS_LAND_DETECT_CONFIG,
      Constants.DDOS_LAND_DETECT_KAFKA_SOURCE_PARALLELISM)
    //对数据处理的并行度
    val dealParallelism = confProperties.getIntValue(Constants.FLINK_DDOS_LAND_DETECT_CONFIG,
      Constants.DDOS_LAND_DETECT_DEAL_PARALLELISM)
    //写入mysql的并行度
    val sqlSinkParallelism = confProperties.getIntValue(Constants.FLINK_DDOS_LAND_DETECT_CONFIG,
      Constants.DDOS_LAND_DETECT_SQL_SINK_PARALLELISM)
    //写入kafka的并行度
    val kafkaSinkParallelism = confProperties.getIntValue(Constants.FLINK_DDOS_LAND_DETECT_CONFIG,
      Constants.DDOS_LAND_DETECT_KAFKA_SINK_PARALLELISM)
    //check pointing的间隔
    val checkpointInterval = confProperties.getLongValue(Constants.FLINK_DDOS_LAND_DETECT_CONFIG,
      Constants.DDOS_LAND_DETECT_CHECKPOINT_INTERVAL)

    //kafka的服务地址
    val brokerList = confProperties.getValue(Constants.FLINK_COMMON_CONFIG, Constants
      .KAFKA_BOOTSTRAP_SERVERS)
    //flink消费的group.id
    val groupId = confProperties.getValue(Constants.FLINK_DDOS_LAND_DETECT_CONFIG, Constants
      .DDOS_LAND_DETECT_GROUP_ID)
    //kafka source 的topic
    val kafkaSourceTopic = confProperties.getValue(Constants.QUINTET_FLINK_TO_DRUID_CONFIG,
      Constants.QUINTET_TO_KAFKA_TOPIC)
    //kafka sink 的topic
    val kafkaSinkTopic = confProperties.getValue(Constants.FLINK_DDOS_LAND_DETECT_CONFIG, Constants
      .DDOS_LAND_DETECT_KAFKA_SINK_TOPIC)

    val warningSinkTopic = confProperties.getValue(Constants.WARNING_FLINK_TO_DRUID_CONFIG, Constants
      .WARNING_TOPIC)

    val parameters: Configuration = new Configuration()
    //c3p0连接池配置文件路径
    parameters.setString(Constants.c3p0_CONFIG_PATH, confProperties.getValue(Constants.FLINK_COMMON_CONFIG, Constants
      .c3p0_CONFIG_PATH))

    val env = StreamExecutionEnvironment.getExecutionEnvironment
//    env.enableCheckpointing(checkpointInterval)
    env.getConfig.setGlobalJobParameters(parameters)

    //获取kafka消费者
    val consumer = Source.kafkaSource(kafkaSourceTopic, groupId, brokerList)

    val alertData = env.fromSource(consumer, WatermarkStrategy.noWatermarks(), kafkaSourceName).setParallelism(kafkaSourceParallelism)
      .uid(kafkaSourceName).name(kafkaSourceName)
      .map(JsonUtil.fromJson[QuintetModel] _).setParallelism(dealParallelism)
      .filter(model => {
        "1".equals(model.protocol) && (model.destinationIp != null && model.destinationIp.length > 0 && model
          .destinationIp.equals(model.sourceIp))
      }).setParallelism(dealParallelism)
      .process(new LandProcessFunction)

    val value = alertData.map(_._1)
    val alertKafkaValue = alertData.map(_._2)
    value.addSink(new MySQLSink)
      .uid(sqlSinkName).name(sqlSinkName)
      .setParallelism(sqlSinkParallelism)

    //获取kafka生产者
    val producer = Sink.kafkaSink(brokerList, kafkaSinkTopic)
    alertData.map(m => {
      JsonUtil.toJson(m._1._1.asInstanceOf[DdosWarnEntity])
    })
      .sinkTo(producer)
      .uid(kafkaSinkName)
      .name(kafkaSinkName)
      .setParallelism(kafkaSinkParallelism)

    //将告警数据写入告警库topic
    val warningProducer = Sink.kafkaSink(brokerList, warningSinkTopic)
    alertKafkaValue.sinkTo(warningProducer).setParallelism(kafkaSinkParallelism)


    env.execute(jobName)

  }

  class LandProcessFunction extends ProcessFunction[QuintetModel, ((Object, Boolean), String)] {
    val inputKafkaValue = ""
    override def processElement(value: QuintetModel, ctx: ProcessFunction[QuintetModel, ((Object, Boolean), String)]#Context,
                                out: Collector[((Object, Boolean), String)]): Unit = {
      val landWarnEntity = new DdosWarnEntity

      landWarnEntity.setSourceIp(value.sourceIp)
      //      landWarnEntity.setStatus(value.isSucceed.toInt)
      landWarnEntity.setDestIp(value.destinationIp)
      landWarnEntity.setWarnTime(new Timestamp(value.timeStamp))
      landWarnEntity.setOccurCount(1)
      landWarnEntity.setWarnType(0)
      val inPutKafkaValue = "未知用户" + "|" + "DDOS攻击" + "|" + value.timeStamp + "|" +
        "" + "|" + "" + "|" + "" + "|" +
        "" + "|" + value.sourceIp + "|" + "" + "|" +
        value.destinationIp + "|" + "" + "|" + "" + "|" +
        "" + "|" + "" + "|" + ""
      out.collect((landWarnEntity, true), inPutKafkaValue)
    }
  }

}
