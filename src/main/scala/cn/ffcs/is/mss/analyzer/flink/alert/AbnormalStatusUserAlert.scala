package cn.ffcs.is.mss.analyzer.flink.alert

import java.util.Properties

import cn.ffcs.is.mss.analyzer.bean.BbasAbnormalStatusUserWarnEntity
import cn.ffcs.is.mss.analyzer.druid.model.scala.AlertModel
import cn.ffcs.is.mss.analyzer.utils.{Constants, IniProperties, JsonUtil}
import org.apache.flink.api.common.accumulators.LongCounter
import org.apache.flink.api.common.functions.RichMapFunction
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.streaming.util.serialization.SimpleStringSchema
import org.apache.flink.api.scala._
import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaConsumer, FlinkKafkaProducer}

import scala.collection.mutable

/**
 * @author ZF
 * @Date 2020/6/15 11:49
 * @Classname AbnormalStatusUserAlert
 * @Description 将异常状态的用户告警 写入风控
 * @update [no][date YYYY-MM-DD][name][description]
 */
object AbnormalStatusUserAlert {
  def main(args: Array[String]): Unit = {
    //    val args0 = "E:\\ffcs\\mss\\src\\main\\resources\\flink.ini"
    //    val confProperties = new IniProperties(args0)
    val confProperties = new IniProperties(args(0))


    //该任务的名字
    val jobName = confProperties.getValue(Constants.ALERT_ABNORMAL_STATUS_USER_CONFIG,
      Constants.ALERT_ABNORMAL_STATUS_USER_JOB_NAME)

    //kafka Source的名字
    val kafkaSourceName = confProperties.getValue(Constants
      .ALERT_ABNORMAL_STATUS_USER_CONFIG, Constants
      .ALERT_ABNORMAL_STATUS_USER_KAFKA_SOURCE_NAME)
    //kafka sink的名字
    val kafkaSinkName = confProperties.getValue(Constants
      .ALERT_ABNORMAL_STATUS_USER_CONFIG, Constants
      .ALERT_ABNORMAL_STATUS_USER_KAFKA_SINK_NAME)

    //kafka Source的并行度
    val kafkaSourceParallelism = confProperties.getIntValue(Constants
      .ALERT_ABNORMAL_STATUS_USER_CONFIG, Constants
      .ALERT_ABNORMAL_STATUS_USER_KAFKA_SOURCE_PARALLELISM)
    //对数据处理的并行度
    val dealParallelism = confProperties.getIntValue(Constants
      .ALERT_ABNORMAL_STATUS_USER_CONFIG, Constants
      .ALERT_ABNORMAL_STATUS_USER_DEAL_PARALLELISM)
    //写入kafka的并行度
    val kafkaSinkParallelism = confProperties.getIntValue(Constants
      .ALERT_ABNORMAL_STATUS_USER_CONFIG, Constants
      .ALERT_ABNORMAL_STATUS_USER_KAFKA_SINK_PARALLELISM)

    //kafka的服务地址
    val brokerList = confProperties.getValue(Constants.FLINK_COMMON_CONFIG, Constants
      .KAFKA_BOOTSTRAP_SERVERS)
    //flink消费的group.id
    val groupId = confProperties.getValue(Constants.ALERT_ABNORMAL_STATUS_USER_CONFIG,
      Constants.ALERT_ABNORMAL_STATUS_USER_GROUP_ID)
    //kafka source 的topic
    val sourceTopic = confProperties.getValue(Constants.ALERT_ABNORMAL_STATUS_USER_CONFIG, Constants
      .ALERT_ABNORMAL_STATUS_USER_KAFKA_SOURCE_TOPIC)
    //kafka sink 的topic
    val sinkTopic = confProperties.getValue(Constants.FLINK_ALERT_STATISTICS_CONFIG, Constants
      .ALERT_STATISTICS_KAFKA_TOPIC)


    //flink全局变量
    val parameters: Configuration = new Configuration()
    parameters.setString(Constants.ALERT_ABNORMAL_STATUS_USER_ALERT_NAME, confProperties
      .getValue(Constants.ALERT_ABNORMAL_STATUS_USER_CONFIG, Constants
        .ALERT_ABNORMAL_STATUS_USER_ALERT_NAME))
    parameters.setString(Constants.ALERT_ABNORMAL_STATUS_USER_ALERT_TYPE, confProperties
      .getValue(Constants.ALERT_ABNORMAL_STATUS_USER_CONFIG, Constants
        .ALERT_ABNORMAL_STATUS_USER_ALERT_TYPE))
    parameters.setString(Constants.ALERT_ABNORMAL_STATUS_USER_REGION, confProperties
      .getValue(Constants.ALERT_ABNORMAL_STATUS_USER_CONFIG, Constants
        .ALERT_ABNORMAL_STATUS_USER_REGION))
    parameters.setString(Constants.ALERT_ABNORMAL_STATUS_USER_BUSINESS, confProperties
      .getValue(Constants.ALERT_ABNORMAL_STATUS_USER_CONFIG, Constants
        .ALERT_ABNORMAL_STATUS_USER_BUSINESS))
    parameters.setString(Constants.ALERT_ABNORMAL_STATUS_USER_DOMAIN, confProperties
      .getValue(Constants.ALERT_ABNORMAL_STATUS_USER_CONFIG, Constants
        .ALERT_ABNORMAL_STATUS_USER_DOMAIN))
    parameters.setString(Constants.ALERT_ABNORMAL_STATUS_USER_IP, confProperties
      .getValue(Constants.ALERT_ABNORMAL_STATUS_USER_CONFIG, Constants
        .ALERT_ABNORMAL_STATUS_USER_IP))
    parameters.setString(Constants.ALERT_ABNORMAL_STATUS_USER_DEVICE, confProperties
      .getValue(Constants.ALERT_ABNORMAL_STATUS_USER_CONFIG, Constants
        .ALERT_ABNORMAL_STATUS_USER_DEVICE))
    parameters.setString(Constants.ALERT_ABNORMAL_STATUS_USER_RULE_ID, confProperties
      .getValue(Constants.ALERT_ABNORMAL_STATUS_USER_CONFIG, Constants
        .ALERT_ABNORMAL_STATUS_USER_RULE_ID))
    parameters.setString(Constants.ALERT_ABNORMAL_STATUS_USER_ALERT_TIMESTAMP_FORMAT,
      confProperties.getValue(Constants.ALERT_ABNORMAL_STATUS_USER_CONFIG, Constants
        .ALERT_ABNORMAL_STATUS_USER_ALERT_TIMESTAMP_FORMAT))
    parameters.setInteger(Constants.ALERT_ABNORMAL_STATUS_USER_ASSEMBLY, confProperties
      .getIntValue(Constants.ALERT_ABNORMAL_STATUS_USER_CONFIG, Constants
        .ALERT_ABNORMAL_STATUS_USER_ASSEMBLY))


    //check pointing的间隔
    val checkpointInterval = confProperties.getLongValue(Constants
      .ALERT_ABNORMAL_STATUS_USER_CONFIG, Constants
      .ALERT_ABNORMAL_STATUS_USER_CHECKPOINT_INTERVAL)

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
        (JsonUtil.fromJson[BbasAbnormalStatusUserWarnEntity](tuple), 1L)
      }).setParallelism(dealParallelism)
      .map(new CrawlerWarningToAlertModel).setParallelism(dealParallelism)
      .map(JsonUtil.toJson(_)).setParallelism(dealParallelism)

    directoryTraversalWarnAlertStream.addSink(producer)
      .uid(kafkaSinkName)
      .name(kafkaSinkName)
      .setParallelism(kafkaSinkParallelism)

    env.execute(jobName)
  }

  class CrawlerWarningToAlertModel extends RichMapFunction[(BbasAbnormalStatusUserWarnEntity, Long),
    AlertModel] {

    //定义累加器
    private val messagesReceived = new LongCounter()
    private val messagesSend = new LongCounter()

    var alertNameMap: mutable.HashMap[Int, String] = _
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
    var alertName = ""

    override def open(parameters: Configuration): Unit = {

      //初始化累加器
      getRuntimeContext.addAccumulator("DetectCrawlerFunction: Messages received", messagesReceived)
      getRuntimeContext.addAccumulator("DetectCrawlerFunction: Messages send", messagesSend)

      //获取全局变量
      val globConf = getRuntimeContext.getExecutionConfig.getGlobalJobParameters
        .asInstanceOf[Configuration]
      alertName = globConf.getString(Constants.ALERT_ABNORMAL_STATUS_USER_ALERT_NAME, "")
      alertType = globConf.getString(Constants.ALERT_ABNORMAL_STATUS_USER_ALERT_TYPE, "")
      alertRegion = globConf.getString(Constants.ALERT_ABNORMAL_STATUS_USER_REGION, "")
      alertBusiness = globConf.getString(Constants.ALERT_ABNORMAL_STATUS_USER_BUSINESS,
        "")
      alertDomain = globConf.getString(Constants.ALERT_ABNORMAL_STATUS_USER_DOMAIN, "")
      alertIp = globConf.getString(Constants.ALERT_ABNORMAL_STATUS_USER_IP, "")
      alertDevice = globConf.getString(Constants.ALERT_ABNORMAL_STATUS_USER_DEVICE, "")
      alertRuleId = globConf.getString(Constants.ALERT_ABNORMAL_STATUS_USER_RULE_ID, "")
      alertTimeStampFormat = globConf.getString(Constants
        .ALERT_ABNORMAL_STATUS_USER_ALERT_TIMESTAMP_FORMAT, "")
      alertAssembly = globConf.getInteger(Constants
        .ALERT_ABNORMAL_STATUS_USER_ASSEMBLY, 19)


    }

    override def map(value: (BbasAbnormalStatusUserWarnEntity, Long)): AlertModel = {

      messagesReceived.add(1)
      val alertModelBuilder = AlertModel.getBuilder()
      val model = alertModelBuilder.alertName(AlertModel.getValue(alertName))
        .alertTimestamp(AlertModel.getAlertTimestamp(alertTimeStampFormat, value
          ._1.getWarnDatetime.getTime))
        .alertType(AlertModel.getValue(alertType))
        .alertLevel(alertLevel)
        .alertRegion(AlertModel.getValue(alertRegion))
        .alertBusiness(AlertModel.getValue(alertBusiness))
        .alertDomain(AlertModel.getValue(alertDomain))
        .alertSrcIp(AlertModel.getValue(value._1.getSourceIp))
        .alertSrcPort(AlertModel.getValue(""))
        .alertDestIp(AlertModel.getValue(value._1.getDestinationIp))
        .alertDestPort(AlertModel.getValue(""))
        .alertTimes(value._2.toInt)
        .alertIp(AlertModel.getValue(alertIp))
        .alertDevice(AlertModel.getValue(alertDevice))
        .alertDescription(AlertModel.getAlertDescription(value._1))
        .alertId(AlertModel.getAlertId(alertId))
        .alertRuleId(AlertModel.getValue(alertRuleId))
        .alertStatus(alertStatus)
        .alertUsername(value._1.getUsername)
        .alertAssembly(alertAssembly)
        .eventTimeStamp(value._1.getWarnDatetime.getTime)
        .build()
      messagesSend.add(1)
      model
    }
  }

}
