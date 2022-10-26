package cn.ffcs.is.mss.analyzer.flink.alert

import java.util.Properties

import cn.ffcs.is.mss.analyzer.bean.{BbasExtremelyActiveSystemEntity, DdosWarnEntity}
import cn.ffcs.is.mss.analyzer.druid.model.scala.AlertModel
import cn.ffcs.is.mss.analyzer.utils.{Constants, IniProperties, JsonUtil}
import org.apache.flink.api.common.functions.RichMapFunction
import org.apache.flink.api.scala._
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaConsumer, FlinkKafkaProducer}
import org.apache.flink.streaming.util.serialization.SimpleStringSchema

object AbnormalSystemAlert {
  def main(args: Array[String]): Unit = {
    //根据传入的参数解析配置文件
    val confProperties = new IniProperties(args(0))

    //该任务的名字
    val jobName = confProperties.getValue(Constants.FLINK_ALERT_STATISTICS_ABNORMAL_SYSTEM_CONFIG,
      Constants.ALERT_STATISTICS_ABNORMAL_SYSTEM_JOB_NAME)
    //kafka Source的名字
    val kafkaSourceName = confProperties.getValue(Constants
      .FLINK_ALERT_STATISTICS_ABNORMAL_SYSTEM_CONFIG, Constants
      .ALERT_STATISTICS_ABNORMAL_SYSTEM_KAFKA_SOURCE_NAME)
    //kafka sink的名字
    val kafkaSinkName = confProperties.getValue(Constants.FLINK_ALERT_STATISTICS_ABNORMAL_SYSTEM_CONFIG,
      Constants.ALERT_STATISTICS_ABNORMAL_SYSTEM_KAFKA_SINK_NAME)

    //kafka Source的并行度
    val kafkaSourceParallelism = confProperties.getIntValue(Constants
      .FLINK_ALERT_STATISTICS_ABNORMAL_SYSTEM_CONFIG, Constants
      .ALERT_STATISTICS_ABNORMAL_SYSTEM_KAFKA_SOURCE_PARALLELISM)
    //对数据处理的并行度
    val dealParallelism = confProperties.getIntValue(Constants
      .FLINK_ALERT_STATISTICS_ABNORMAL_SYSTEM_CONFIG,
      Constants.ALERT_STATISTICS_ABNORMAL_SYSTEM_DEAL_PARALLELISM)
    //写入kafka的并行度
    val kafkaSinkParallelism = confProperties.getIntValue(Constants
      .FLINK_ALERT_STATISTICS_ABNORMAL_SYSTEM_CONFIG, Constants
      .ALERT_STATISTICS_ABNORMAL_SYSTEM_KAFKA_SINK_PARALLELISM)

    //kafka的服务地址
    val brokerList = confProperties.getValue(Constants.FLINK_COMMON_CONFIG, Constants
      .KAFKA_BOOTSTRAP_SERVERS)
    //flink消费的group.id
    val groupId = confProperties.getValue(Constants.FLINK_ALERT_STATISTICS_ABNORMAL_SYSTEM_CONFIG,
      Constants.ALERT_STATISTICS_ABNORMAL_SYSTEM_GROUP_ID)
    //kafka source 的topic
    val sourceTopic = confProperties.getValue(Constants.FLINK_ALERT_STATISTICS_ABNORMAL_SYSTEM_CONFIG, Constants
      .ALERT_STATISTICS_ABNORMAL_SYSTEM_KAFKA_SOURCE_TOPIC)
    //kafka sink 的topic
    val sinkTopic = confProperties.getValue(Constants.FLINK_ALERT_STATISTICS_CONFIG, Constants
      .ALERT_STATISTICS_KAFKA_TOPIC)


    //flink全局变量
    val parameters: Configuration = new Configuration()
    parameters.setString(Constants.ALERT_STATISTICS_ABNORMAL_SYSTEM_ALERT_NAME, confProperties.getValue
    (Constants.FLINK_ALERT_STATISTICS_ABNORMAL_SYSTEM_CONFIG, Constants
      .ALERT_STATISTICS_ABNORMAL_SYSTEM_ALERT_NAME))
    parameters.setString(Constants.ALERT_STATISTICS_ABNORMAL_SYSTEM_ALERT_TYPE, confProperties.getValue
    (Constants.FLINK_ALERT_STATISTICS_ABNORMAL_SYSTEM_CONFIG, Constants
      .ALERT_STATISTICS_ABNORMAL_SYSTEM_ALERT_TYPE))
    parameters.setString(Constants.ALERT_STATISTICS_ABNORMAL_SYSTEM_ALERT_REGION, confProperties
      .getValue(Constants.FLINK_ALERT_STATISTICS_ABNORMAL_SYSTEM_CONFIG, Constants
        .ALERT_STATISTICS_ABNORMAL_SYSTEM_ALERT_REGION))
    parameters.setString(Constants.ALERT_STATISTICS_ABNORMAL_SYSTEM_ALERT_BUSINESS, confProperties
      .getValue(Constants.FLINK_ALERT_STATISTICS_ABNORMAL_SYSTEM_CONFIG, Constants
        .ALERT_STATISTICS_ABNORMAL_SYSTEM_ALERT_BUSINESS))
    parameters.setString(Constants.ALERT_STATISTICS_ABNORMAL_SYSTEM_ALERT_DOMAIN, confProperties
      .getValue(Constants.FLINK_ALERT_STATISTICS_ABNORMAL_SYSTEM_CONFIG, Constants
        .ALERT_STATISTICS_ABNORMAL_SYSTEM_ALERT_DOMAIN))
    parameters.setString(Constants.ALERT_STATISTICS_ABNORMAL_SYSTEM_ALERT_IP, confProperties.getValue
    (Constants.FLINK_ALERT_STATISTICS_ABNORMAL_SYSTEM_CONFIG, Constants
      .ALERT_STATISTICS_ABNORMAL_SYSTEM_ALERT_IP))
    parameters.setString(Constants.ALERT_STATISTICS_ABNORMAL_SYSTEM_ALERT_DEVICE, confProperties
      .getValue(Constants.FLINK_ALERT_STATISTICS_ABNORMAL_SYSTEM_CONFIG, Constants
        .ALERT_STATISTICS_ABNORMAL_SYSTEM_ALERT_DEVICE))
    parameters.setString(Constants.ALERT_STATISTICS_ABNORMAL_SYSTEM_ALERT_RULE_ID, confProperties
      .getValue(Constants.FLINK_ALERT_STATISTICS_ABNORMAL_SYSTEM_CONFIG, Constants
        .ALERT_STATISTICS_ABNORMAL_SYSTEM_ALERT_RULE_ID))
    parameters.setString(Constants.ALERT_STATISTICS_ABNORMAL_SYSTEM_ALERT_TIMESTAMP_FORMAT,
      confProperties.getValue(Constants.FLINK_ALERT_STATISTICS_ABNORMAL_SYSTEM_CONFIG, Constants
        .ALERT_STATISTICS_ABNORMAL_SYSTEM_ALERT_TIMESTAMP_FORMAT))
    parameters.setInteger(Constants.ALERT_STATISTICS_ABNORMAL_SYSTEM_ALERT_ASSEMBLY, confProperties
      .getIntValue(Constants.FLINK_ALERT_STATISTICS_ABNORMAL_SYSTEM_CONFIG, Constants
        .ALERT_STATISTICS_ABNORMAL_SYSTEM_ALERT_ASSEMBLY))

    //check pointing的间隔
    val checkpointInterval = confProperties.getLongValue(Constants
      .FLINK_ALERT_STATISTICS_ABNORMAL_SYSTEM_CONFIG, Constants
      .ALERT_STATISTICS_ABNORMAL_SYSTEM_CHECKPOINT_INTERVAL)

    //获取ExecutionEnvironment
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    //设置check pointing的间隔
    env.enableCheckpointing(checkpointInterval)
    //设置流的时间为EventTime
    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)
    //设置flink全局变量
    env.getConfig.setGlobalJobParameters(parameters)

    //设置kafka消费者相关配置
    val props = new Properties()
    //设置kafka集群地址
    props.setProperty("bootstrap.servers", brokerList)
    //设置flink消费的group.id
    props.setProperty("group.id", groupId)

    //获取kafka消费者
    val consumer = new FlinkKafkaConsumer[String](sourceTopic, new SimpleStringSchema, props)
      .setStartFromGroupOffsets()
    //获取kafka生产者
    val producer = new FlinkKafkaProducer[String](brokerList, sinkTopic, new SimpleStringSchema())
    //获取kafka数据
    val dStream = env.addSource(consumer).setParallelism(kafkaSourceParallelism)
      .uid(kafkaSourceName).name(kafkaSourceName)

    val directoryTraversalWarnAlertStream = dStream
      .map(tuple => {
        (JsonUtil.fromJson[BbasExtremelyActiveSystemEntity](tuple), 1L)
      }).setParallelism(dealParallelism)
      .map(new AbnormalSyatemWarnToAlertModel).setParallelism(dealParallelism)
      .map(JsonUtil.toJson(_)).setParallelism(dealParallelism)

    directoryTraversalWarnAlertStream.addSink(producer)
      .uid(kafkaSinkName)
      .name(kafkaSinkName)
      .setParallelism(kafkaSinkParallelism)

    env.execute(jobName)
  }

  class AbnormalSyatemWarnToAlertModel extends RichMapFunction[(BbasExtremelyActiveSystemEntity, Long),
    AlertModel] {

    var alertName = ""
    var alertType = ""
    var alertLevel = 3
    var alertRegion = ""
    var alertBusiness = ""
    var alertDomain = ""
    var alertIp = ""
    var alertDevice = ""
    var alertId = ""
    var alertRuleId = ""
    var alertStatus = 0
    var alertUsername = ""
    var alertTimeStampFormat = ""
    var alertAssembly = 19

    override def open(parameters: Configuration): Unit = {

      //获取全局变量
      val globConf = getRuntimeContext.getExecutionConfig.getGlobalJobParameters
        .asInstanceOf[Configuration]
      alertName = globConf.getString(Constants.ALERT_STATISTICS_ABNORMAL_SYSTEM_ALERT_NAME, "")
      alertType = globConf.getString(Constants.ALERT_STATISTICS_ABNORMAL_SYSTEM_ALERT_TYPE, "")
      alertRegion = globConf.getString(Constants.ALERT_STATISTICS_ABNORMAL_SYSTEM_ALERT_REGION, "")
      alertBusiness = globConf.getString(Constants.ALERT_STATISTICS_ABNORMAL_SYSTEM_ALERT_BUSINESS,
        "")
      alertDomain = globConf.getString(Constants.ALERT_STATISTICS_ABNORMAL_SYSTEM_ALERT_DOMAIN, "")
      alertIp = globConf.getString(Constants.ALERT_STATISTICS_ABNORMAL_SYSTEM_ALERT_IP, "")
      alertDevice = globConf.getString(Constants.ALERT_STATISTICS_ABNORMAL_SYSTEM_ALERT_DEVICE, "")
      alertRuleId = globConf.getString(Constants.ALERT_STATISTICS_ABNORMAL_SYSTEM_ALERT_RULE_ID, "")
      alertTimeStampFormat = globConf.getString(Constants
        .ALERT_STATISTICS_ABNORMAL_SYSTEM_ALERT_TIMESTAMP_FORMAT, "")
      alertAssembly = globConf.getInteger(Constants
        .ALERT_STATISTICS_ABNORMAL_SYSTEM_ALERT_ASSEMBLY, 19)

    }

    override def map(value: (BbasExtremelyActiveSystemEntity, Long)): AlertModel = {

      val alertModelBuilder = AlertModel.getBuilder()
      alertModelBuilder.alertName(AlertModel.getValue(alertName))
        .alertTimestamp(AlertModel.getAlertTimestamp(alertTimeStampFormat, value
          ._1.getActiveDatetime.getTime))
        .alertType(AlertModel.getValue(alertType))
        .alertLevel(alertLevel)
        .alertRegion(AlertModel.getValue(alertRegion))
        .alertBusiness(AlertModel.getValue(alertBusiness))
        .alertDomain(AlertModel.getValue(alertDomain))
        .alertSrcIp(AlertModel.getValue(value._1.getSourceIp))
        .alertSrcPort(AlertModel.getValue(""))
        .alertDestIp(AlertModel.getValue(value._1.getDestIp))
        .alertDestPort(AlertModel.getValue(""))
        .alertTimes(value._2.toInt)
        .alertIp(AlertModel.getValue(alertIp))
        .alertDevice(AlertModel.getValue(alertDevice))
        .alertDescription(AlertModel.getAlertDescription(value._1))
        .alertId(AlertModel.getAlertId(alertId))
        .alertRuleId(AlertModel.getValue(alertRuleId))
        .alertStatus(alertStatus)
        .alertUsername(AlertModel.getValue(alertUsername))
        .alertAssembly(alertAssembly)
        .eventTimeStamp(value._1.getActiveDatetime.getTime)
        .build()
    }
  }


}
