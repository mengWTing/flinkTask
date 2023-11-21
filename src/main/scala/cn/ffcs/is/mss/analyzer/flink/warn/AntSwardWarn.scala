package cn.ffcs.is.mss.analyzer.flink.warn

import java.net.URLDecoder
import java.sql.Timestamp
import java.util.Properties

import cn.ffcs.is.mss.analyzer.bean.AntSwordWarnEntity
import cn.ffcs.is.mss.analyzer.druid.model.scala.OperationModel
import cn.ffcs.is.mss.analyzer.flink.sink.MySQLSink
import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaConsumer, FlinkKafkaProducer}
import org.apache.flink.streaming.util.serialization.SimpleStringSchema
import cn.ffcs.is.mss.analyzer.utils.{Constants, IniProperties, JsonUtil}
import org.apache.flink.api.common.functions.RichMapFunction
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.ProcessFunction
import org.apache.flink.api.scala._
import org.apache.flink.util.Collector

/**
 * @Auther kimchie
 * @Description 蚁剑流量检测
 * @Date: Created in 2023/2/07 16:34
 * @Modified By kimchie
 */
object AntSwardWarn {

  def main(args: Array[String]): Unit = {
    //根据传入的参数解析配置文件
//    val args0 = "/Users/chenwei/IdeaProjects/mss/src/main/resources/flink.ini"
//    val confProperties = new IniProperties(args0)
    val confProperties = new IniProperties(args(0))
    //该任务的名字
    val jobName = confProperties.getValue(Constants.FLINK_ANT_SWORD_CONFIG, Constants
      .FLINK_ANT_SWORD_CONFIG_JOB_NAME)

    //kafka Source的名字
    val kafkaSourceName = confProperties.getValue(Constants.FLINK_ANT_SWORD_CONFIG, Constants
      .FLINK_ANT_SWORD_CONFIG_KAFKA_SOURCE_NAME)
    //kafka sink的名字
    val kafkaSinkName = confProperties.getValue(Constants.FLINK_ANT_SWORD_CONFIG, Constants
      .FLINK_ANT_SWORD_CONFIG_KAFKA_SINK_NAME)

    //kafka Source的并行度
    val kafkaSourceParallelism = confProperties.getIntValue(Constants.FLINK_ANT_SWORD_CONFIG,
      Constants.FLINK_ANT_SWORD_CONFIG_SOURCE_PARALLELISM)
    //对数据处理的并行度
    val dealParallelism = confProperties.getIntValue(Constants.FLINK_ANT_SWORD_CONFIG,
      Constants.FLINK_ANT_SWORD_CONFIG_DEAL_PARALLELISM)
    //写入kafka的并行度
    val kafkaSinkParallelism = confProperties.getIntValue(Constants.FLINK_ANT_SWORD_CONFIG,
      Constants.FLINK_ANT_SWORD_CONFIG_SINK_PARALLELISM)

    //kafka的服务地址
    val brokerList = confProperties.getValue(Constants.FLINK_COMMON_CONFIG, Constants
      .KAFKA_BOOTSTRAP_SERVERS)
    //flink消费的group.id
    val groupId = confProperties.getValue(Constants.FLINK_ANT_SWORD_CONFIG, Constants
      .FLINK_ANT_SWORD_CONFIG_GROUP_ID)

    //kafka source 的topic
    val kafkaSourceTopic = confProperties.getValue(Constants.OPERATION_FLINK_TO_DRUID_CONFIG,
      Constants.OPERATION_TOPIC)
    //kafka sink 的topic
    val kafkaSinkTopic = confProperties.getValue(Constants.FLINK_ANT_SWORD_CONFIG, Constants
      .FLINK_ANT_SWORD_CONFIG_KAFKA_SINK_TOPIC)
    val warningSinkTopic = confProperties.getValue(Constants.WARNING_FLINK_TO_DRUID_CONFIG, Constants
      .WARNING_TOPIC)

    //flink全局变量
    val parameters: Configuration = new Configuration()
    //配置文件系统类型
    parameters.setString(Constants.FILE_SYSTEM_TYPE, confProperties.getValue(Constants
      .FLINK_COMMON_CONFIG, Constants.FILE_SYSTEM_TYPE))
    //c3p0连接池配置文件路径
    parameters.setString(Constants.c3p0_CONFIG_PATH, confProperties.getValue(Constants
      .FLINK_COMMON_CONFIG, Constants.c3p0_CONFIG_PATH))

    //check pointing的间隔
    val checkpointInterval = confProperties.getLongValue(Constants.FLINK_ANT_SWORD_CONFIG,
      Constants.FLINK_ANT_SWORD_CONFIG_CHECKPOINT_INTERVAL)

    //获取ExecutionEnvironment
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    //设置check pointing的间隔
    //env.enableCheckpointing(checkpointInterval)
    //设置flink全局变量
    env.getConfig.setGlobalJobParameters(parameters)

    //设置kafka消费者相关配置
    val props = new Properties()
    //设置kafka集群地址
    props.setProperty("bootstrap.servers", brokerList)
    //设置flink消费的group.id
    props.setProperty("group.id", groupId)

    //获取kafka消费者
    val consumer = new FlinkKafkaConsumer[String](kafkaSourceTopic, new SimpleStringSchema,
      props).setStartFromGroupOffsets()
    //获取kafka生产者
    val producer = new FlinkKafkaProducer[String](brokerList, kafkaSinkTopic, new
        SimpleStringSchema())
    val warningProducer = new FlinkKafkaProducer[String](brokerList, warningSinkTopic, new
        SimpleStringSchema())
    //获取kafka数据
    val dStream = env.addSource(consumer).setParallelism(kafkaSourceParallelism)
                  .uid(kafkaSourceName).name(kafkaSourceName)
//    val dStream = env.socketTextStream("192.168.1.24", 8888).setParallelism(1)

    val antSwardWarnStream = dStream
      .map(new RichMapFunction[String, (Option[OperationModel], String)] {
        override def map(value: String): (Option[OperationModel], String) = (OperationModel.getOperationModel(value), value)
      }).setParallelism(1)
      .filter(_._1.isDefined).setParallelism(dealParallelism)
      .map(t => (t._1.head, t._2)).setParallelism(dealParallelism)
      .process(new antSwardProcessFunction)

    val value: DataStream[(Object, Boolean)] = antSwardWarnStream.map(_._1)
    val alertKafkaValue = antSwardWarnStream.map(_._2)

    value.addSink(new MySQLSink).uid("ant_sword").name("ant_sword")
        .setParallelism(kafkaSinkParallelism)

        antSwardWarnStream
          .map(o => {
            JsonUtil.toJson(o._1.asInstanceOf[AntSwordWarnEntity])
          })
          .addSink(producer)
          .uid(kafkaSinkName)
          .name(kafkaSinkName)
          .setParallelism(kafkaSinkParallelism)

    alertKafkaValue.addSink(warningProducer).setParallelism(kafkaSinkParallelism)

    env.execute(jobName)

  }


  class antSwardProcessFunction extends ProcessFunction[(OperationModel, String), ((Object, Boolean), String)] {
    var groupSplit: Char = _
    var kvSplit: Char = _
    var ruleList: List[String] = _

    override def open(parameters: Configuration): Unit = {

      val globalConf = getRuntimeContext.getExecutionConfig.getGlobalJobParameters.asInstanceOf[Configuration]
      groupSplit = globalConf.getInteger(Constants.XSS_INJECTION_GROUP_SPLIT, 0).asInstanceOf[Char]
      kvSplit = globalConf.getInteger(Constants.XSS_INJECTION_KV_SPLIT, 0).asInstanceOf[Char]
      ruleList = initCheckAntSwardDataRuleMap
    }

    override def processElement(value: (OperationModel, String), ctx: ProcessFunction[(OperationModel, String), ((Object, Boolean), String)]#Context,
                                out: Collector[((Object, Boolean), String)]): Unit = {
      var isAntSward = false
      val values = value._2.split("\\|", -1)
      val userAgent = values(9)
      val formValues = values(30) //请求内容
      val url = values(6)
      //判断userAgent是否是蚁剑
      if (userAgent.indexOf("antSword") > -1) {
        isAntSward = true
      }

      if (values.length >= 31) {
        if (formValues != null && formValues.length > 0) {
          for (formValue <- formValues.split(groupSplit)) {
            val kvValues = formValue.split(kvSplit)
            if (kvValues != null && kvValues.length == 2) {
              var urlValue = ""
              try {
                urlValue = URLDecoder.decode(kvValues(1).replaceAll("%(?![0-9a-fA-F]{2})", "%25")
                  , "utf-8")
              } catch {
                case e: Exception => {
                }
              }
              for (rule <- ruleList) {
                if (plusPercent(urlValue).indexOf(rule) > -1) {
                  isAntSward = true
                }
              }
            }
          }
        }
      }

      def plusPercent(str: String): String = {
        val stringBuffer = new StringBuffer()
        for (char <- str.toCharArray) {

          stringBuffer.append(char)
          if (char.equals('%')) {
            stringBuffer.append("%")
          }
        }
        stringBuffer.toString
      }

      if (isAntSward) {
        val antSwordWarnEntity = new AntSwordWarnEntity
        antSwordWarnEntity.setAlertTime(new Timestamp(value._1.timeStamp))
        antSwordWarnEntity.setUserName(value._1.userName)
        antSwordWarnEntity.setLoginSystem(value._1.loginSystem)
        antSwordWarnEntity.setDestinationIp(value._1.destinationIp)
        antSwordWarnEntity.setLoginPlace(value._1.loginPlace)
        antSwordWarnEntity.setSourceIp(value._1.sourceIp)
        antSwordWarnEntity.setHttpStatus(value._1.httpStatus)
        if (formValues.length > 1000) {
          antSwordWarnEntity.setFormValue(formValues.substring(0, 1000))
        } else {
          antSwordWarnEntity.setFormValue(formValues)
        }

        val inputKafkaValue = value._1.userName + "|" + "蚁剑工具异常使用行为检测: " + "|" + value._1.timeStamp + "|" +
          "" + "|" + value._1.loginSystem + "|" + "" + "|" +
          "" + "|" + value._1.sourceIp + "|" + "" + "|" +
          value._1.destinationIp + "|" + "" + "|" + url + "|" +
          value._1.httpStatus + "|" + "" + "|" + ""

        out.collect((antSwordWarnEntity, false), inputKafkaValue)
      }
    }

    /**
     * @title 初始化验证蚁剑规则的数据
     * @description
     * @author kimchie
     * @updateTime
     * @throws
     */
    def initCheckAntSwardDataRuleMap: List[String] = {
      var datas: List[String] = List("@ini_s")
      //base64
      datas = datas :+ "QGluaV9z"
      //chr
      datas = datas :+ "cHr(64).ChR(105).ChR(1 10).ChR(105).ChR(95).ChR(115)"
      //chr16
      datas = datas :+ "cHr(0x40).ChR(0x69).ChR(0x6e).ChR(0x69).ChR(0x5f).ChR(0x73)"
      //rot13
      datas = datas :+ "@vav_f"
      datas
    }
  }
}
