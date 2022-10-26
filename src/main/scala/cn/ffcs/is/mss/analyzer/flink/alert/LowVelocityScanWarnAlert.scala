package cn.ffcs.is.mss.analyzer.flink.alert

import java.util.Properties

import cn.ffcs.is.mss.analyzer.bean.LowVelocityScanEntity
import cn.ffcs.is.mss.analyzer.druid.model.scala.AlertModel
import cn.ffcs.is.mss.analyzer.utils.{Constants, IniProperties, JsonUtil}
import org.apache.flink.api.common.accumulators.LongCounter
import org.apache.flink.api.common.functions.RichMapFunction
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaConsumer, FlinkKafkaProducer}
import org.apache.flink.streaming.util.serialization.SimpleStringSchema

/**
 * @title 慢速扫描告警写入风控告警
 * @author hanyu
 * @date 2020-09-10 10:00
 * @description
 * @update [no][date YYYY-MM-DD][name][description]
 */
object LowVelocityScanWarnAlert {
  def main(args: Array[String]): Unit = {
    val confProperties = new IniProperties(args(0))
    //该任务的名字
    val jobName = confProperties.getValue(Constants.ALERT_LOW_VELOCITY_SCAN_CONFIG,
      Constants.ALERT_LOW_VELOCITY_SCAN_JOB_NAME)
    //check pointing的间隔
    val checkpointInterval = confProperties.getLongValue(Constants.ALERT_LOW_VELOCITY_SCAN_CONFIG,
      Constants.ALERT_LOW_VELOCITY_SCAN_CHECKPOINT_INTERVAL)

    //kafka Source的名字
    val kafkaSourceName = confProperties.getValue(Constants.ALERT_LOW_VELOCITY_SCAN_CONFIG, Constants
      .ALERT_LOW_VELOCITY_SCAN_KAFKA_SOURCE_NAME)
    //kafka sink的名字
    val kafkaSinkName = confProperties.getValue(Constants.ALERT_LOW_VELOCITY_SCAN_CONFIG, Constants
      .ALERT_LOW_VELOCITY_SCAN_KAFKA_SINK_NAME)

    //kafka Source的并行度
    val kafkaSourceParallelism = confProperties.getIntValue(Constants.ALERT_LOW_VELOCITY_SCAN_CONFIG, Constants
      .ALERT_LOW_VELOCITY_SCAN_KAFKA_SOURCE_PARALLELISM)
    //对数据处理的并行度
    val dealParallelism = confProperties.getIntValue(Constants.ALERT_LOW_VELOCITY_SCAN_CONFIG, Constants
      .ALERT_LOW_VELOCITY_SCAN_DEAL_PARALLELISM)
    //写入kafka的并行度
    val kafkaSinkParallelism = confProperties.getIntValue(Constants.ALERT_LOW_VELOCITY_SCAN_CONFIG, Constants
      .ALERT_LOW_VELOCITY_SCAN_KAFKA_SINK_PARALLELISM)

    //kafka的服务地址
    val brokerList = confProperties.getValue(Constants.FLINK_COMMON_CONFIG, Constants.KAFKA_BOOTSTRAP_SERVERS)
    //flink消费的group.id
    val groupId = confProperties.getValue(Constants.ALERT_LOW_VELOCITY_SCAN_CONFIG,
      Constants.ALERT_LOW_VELOCITY_SCAN_GROUP_ID)
    //kafka source 的topic
    val sourceTopic = confProperties.getValue(Constants.LOW_VELOCITY_SCAN_CONFIG, Constants
      .LOW_VELOCITY_SCAN_KAFKA_SINK_TOPIC)
    //kafka sink 的topic
    //    val sinkTopic = confProperties.getValue(Constants.ALERT_LOW_VELOCITY_SCAN_CONFIG, Constants
    //      .ALERT_LOW_VELOCITY_SCAN_KAFKA_SINK_TOPIC)
    //kafka sink 的topic
    val sinkTopic = confProperties.getValue(Constants.FLINK_ALERT_STATISTICS_CONFIG, Constants
      .ALERT_STATISTICS_KAFKA_TOPIC)


    //flink全局变量
    val parameters: Configuration = new Configuration()
    parameters.setString(Constants.ALERT_LOW_VELOCITY_SCAN_ALERT_NAME, confProperties
      .getValue(Constants.ALERT_LOW_VELOCITY_SCAN_CONFIG, Constants
        .ALERT_LOW_VELOCITY_SCAN_ALERT_NAME))
    parameters.setString(Constants.ALERT_LOW_VELOCITY_SCAN_ALERT_TYPE, confProperties
      .getValue(Constants.ALERT_LOW_VELOCITY_SCAN_CONFIG, Constants
        .ALERT_LOW_VELOCITY_SCAN_ALERT_TYPE))
    parameters.setString(Constants.ALERT_LOW_VELOCITY_SCAN_REGION, confProperties
      .getValue(Constants.ALERT_LOW_VELOCITY_SCAN_CONFIG, Constants
        .ALERT_LOW_VELOCITY_SCAN_REGION))
    parameters.setString(Constants.ALERT_LOW_VELOCITY_SCAN_BUSINESS, confProperties
      .getValue(Constants.ALERT_LOW_VELOCITY_SCAN_CONFIG, Constants
        .ALERT_LOW_VELOCITY_SCAN_BUSINESS))
    parameters.setString(Constants.ALERT_LOW_VELOCITY_SCAN_DOMAIN, confProperties
      .getValue(Constants.ALERT_LOW_VELOCITY_SCAN_CONFIG, Constants
        .ALERT_LOW_VELOCITY_SCAN_DOMAIN))
    parameters.setString(Constants.ALERT_LOW_VELOCITY_SCAN_IP, confProperties
      .getValue(Constants.ALERT_LOW_VELOCITY_SCAN_CONFIG, Constants
        .ALERT_LOW_VELOCITY_SCAN_IP))
    parameters.setString(Constants.ALERT_LOW_VELOCITY_SCAN_DEVICE, confProperties
      .getValue(Constants.ALERT_LOW_VELOCITY_SCAN_CONFIG, Constants
        .ALERT_LOW_VELOCITY_SCAN_DEVICE))
    parameters.setString(Constants.ALERT_LOW_VELOCITY_SCAN_RULE_ID, confProperties
      .getValue(Constants.ALERT_LOW_VELOCITY_SCAN_CONFIG, Constants
        .ALERT_LOW_VELOCITY_SCAN_RULE_ID))
    parameters.setString(Constants.ALERT_LOW_VELOCITY_SCAN_ALERT_TIMESTAMP_FORMAT,
      confProperties.getValue(Constants.ALERT_LOW_VELOCITY_SCAN_CONFIG, Constants
        .ALERT_LOW_VELOCITY_SCAN_ALERT_TIMESTAMP_FORMAT))
    parameters.setInteger(Constants.ALERT_LOW_VELOCITY_SCAN_ASSEMBLY, confProperties
      .getIntValue(Constants.ALERT_LOW_VELOCITY_SCAN_CONFIG, Constants
        .ALERT_LOW_VELOCITY_SCAN_ASSEMBLY))

    //获取ExecutionEnvironment
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    //设置check pointing的间隔
    //env.enableCheckpointing(checkpointInterval)
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
    val producer = new FlinkKafkaProducer[String](brokerList, sinkTopic, new SimpleStringSchema)
    //获取kafka数据
    val dStream = env.addSource(consumer).setParallelism(kafkaSourceParallelism)
      .uid(kafkaSourceName).name(kafkaSourceName)

    val WarnAlertStream = dStream
      .map(tuple => {
        (JsonUtil.fromJson[LowVelocityScanEntity](tuple), 1L)
      }).setParallelism(dealParallelism)
      .map(new LowVelocityScanWarnToAlertModel).setParallelism(dealParallelism)
      .map(JsonUtil.toJson(_)).setParallelism(dealParallelism)

    WarnAlertStream.addSink(producer)
      .uid(kafkaSinkName)
      .name(kafkaSinkName)
      .setParallelism(kafkaSinkParallelism)

    env.execute(jobName)


  }

  class LowVelocityScanWarnToAlertModel extends RichMapFunction[(LowVelocityScanEntity, Long
    ), AlertModel] {
    //定义累加器
    private val messagesReceived = new LongCounter()
    private val messagesSend = new LongCounter()
    var alertName = ""
    var alertType = ""
    var alertLevel = 2
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
      //初始化累加器
      getRuntimeContext.addAccumulator("DetectCrawlerFunction: Messages received", messagesReceived)
      getRuntimeContext.addAccumulator("DetectCrawlerFunction: Messages send", messagesSend)

      //获取全局变量
      val globConf = getRuntimeContext.getExecutionConfig.getGlobalJobParameters
        .asInstanceOf[Configuration]
      alertName = globConf.getString(Constants.ALERT_LOW_VELOCITY_SCAN_ALERT_NAME, "")
      alertType = globConf.getString(Constants.ALERT_LOW_VELOCITY_SCAN_ALERT_TYPE, "")
      alertRegion = globConf.getString(Constants.ALERT_LOW_VELOCITY_SCAN_REGION, "")
      alertBusiness = globConf.getString(Constants.ALERT_LOW_VELOCITY_SCAN_BUSINESS,
        "")
      alertDomain = globConf.getString(Constants.ALERT_LOW_VELOCITY_SCAN_DOMAIN, "")
      alertIp = globConf.getString(Constants.ALERT_LOW_VELOCITY_SCAN_IP, "")
      alertDevice = globConf.getString(Constants.ALERT_LOW_VELOCITY_SCAN_DEVICE, "")
      alertRuleId = globConf.getString(Constants.ALERT_LOW_VELOCITY_SCAN_RULE_ID, "")
      alertTimeStampFormat = globConf.getString(Constants
        .ALERT_LOW_VELOCITY_SCAN_ALERT_TIMESTAMP_FORMAT, "")
      alertAssembly = globConf.getInteger(Constants
        .ALERT_LOW_VELOCITY_SCAN_ASSEMBLY, 19)


    }

    override def map(value: (LowVelocityScanEntity, Long)): AlertModel = {
      messagesReceived.add(1)
      val alertModelBuilder = AlertModel.getBuilder()
        .alertName(AlertModel.getValue(alertName))
        .alertTimestamp(AlertModel.getAlertTimestamp(alertTimeStampFormat, value._1.getAlertTime.getTime))
        .alertType(AlertModel.getValue(alertType))
        .alertLevel(alertLevel)
        .alertRegion(AlertModel.getValue(alertRegion))
        .alertBusiness(AlertModel.getValue(alertBusiness))
        .alertDomain(AlertModel.getValue(alertDomain))
        .alertSrcIp(AlertModel.getValue(value._1.getSourceIp))
        .alertSrcPort(AlertModel.getValue(""))
        .alertDestIp(AlertModel.getValue(value._1.getDestinationIp.split("\\|", -1)(0)))
        .alertDestPort(AlertModel.getValue(value._1.getDestinationPort.split("\\|", -1)(0)))
        .alertTimes(value._2.toInt)
        .alertIp(AlertModel.getValue(alertIp))
        .alertDevice(AlertModel.getValue(alertDevice))
        .alertDescription(AlertModel.getAlertDescription(value._1))
        .alertId(AlertModel.getAlertId(alertId))
        .alertRuleId(AlertModel.getValue(alertRuleId))
        .alertStatus(alertStatus)
        .alertUsername(AlertModel.getValue(alertUsername))
        .alertAssembly(alertAssembly)
        .eventTimeStamp(value._1.getAlertTime.getTime)
        .build()
      messagesSend.add(1)

      alertModelBuilder
    }

  }

}
