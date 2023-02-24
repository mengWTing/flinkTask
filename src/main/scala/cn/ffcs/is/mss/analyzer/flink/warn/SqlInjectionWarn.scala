package cn.ffcs.is.mss.analyzer.flink.warn

import java.net.URLDecoder
import java.sql.Timestamp
import java.util.Properties

import cn.ffcs.is.mss.analyzer.bean.BbasSqlInjectionWarnEntity
import cn.ffcs.is.mss.analyzer.druid.model.scala.OperationModel
import cn.ffcs.is.mss.analyzer.flink.sink.MySQLSink
import cn.ffcs.is.mss.analyzer.utils.GetInputKafkaValue.getInputKafkaValue
import cn.ffcs.is.mss.analyzer.utils.libInjection.sql.Libinjection
import cn.ffcs.is.mss.analyzer.utils.{Constants, IniProperties, JsonUtil}
import org.apache.flink.api.common.functions.RichMapFunction
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.ProcessFunction
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaConsumer, FlinkKafkaProducer}
import org.apache.flink.streaming.util.serialization.SimpleStringSchema
import org.apache.flink.util.Collector

/**
  * @Auther chenwei
  * @Description sql注入告警
  * @Date: Created in 2018/12/14 16:33
  * @Modified By
  */
object SqlInjectionWarn {
  def main(args: Array[String]): Unit = {

    //根据传入的参数解析配置文件
    //val args0 = "/Users/chenwei/IdeaProjects/mss/src/main/resources/flink.ini"
    //val confProperties = new IniProperties(args0)
    val confProperties = new IniProperties(args(0))
    //该任务的名字
    val jobName = confProperties.getValue(Constants.FLINK_SQL_INJECTION_CONFIG, Constants.SQL_INJECTION_JOB_NAME)
    //kafka Source的名字
    val kafkaSourceName = confProperties.getValue(Constants.FLINK_SQL_INJECTION_CONFIG, Constants
      .SQL_INJECTION_KAFKA_SOURCE_NAME)
    //mysql sink的名字
    val sqlSinkName = confProperties.getValue(Constants.FLINK_SQL_INJECTION_CONFIG, Constants
      .SQL_INJECTION_SQL_SINK_NAME)
    //kafka sink的名字
    val kafkaSinkName = confProperties.getValue(Constants.FLINK_SQL_INJECTION_CONFIG, Constants
      .SQL_INJECTION_KAFKA_SINK_NAME)


    //kafka Source的并行度
    val kafkaSourceParallelism = confProperties.getIntValue(Constants.FLINK_SQL_INJECTION_CONFIG, Constants
      .SQL_INJECTION_KAFKA_SOURCE_PARALLELISM)
    //对数据处理的并行度
    val dealParallelism = confProperties.getIntValue(Constants.FLINK_SQL_INJECTION_CONFIG, Constants
      .SQL_INJECTION_DEAL_PARALLELISM)
    //写入mysql的并行度
    val sqlSinkParallelism = confProperties.getIntValue(Constants.FLINK_SQL_INJECTION_CONFIG, Constants
      .SQL_INJECTION_SQL_SINK_PARALLELISM)
    //写入kafka的并行度
    val kafkaSinkParallelism = confProperties.getIntValue(Constants.FLINK_SQL_INJECTION_CONFIG, Constants
      .SQL_INJECTION_KAFKA_SINK_PARALLELISM)

    val warningSinkTopic = confProperties.getValue(Constants.WARNING_FLINK_TO_DRUID_CONFIG, Constants
      .WARNING_TOPIC)

    //kafka的服务地址
    val brokerList = confProperties.getValue(Constants.FLINK_COMMON_CONFIG, Constants.KAFKA_BOOTSTRAP_SERVERS)
    //flink消费的group.id
    val groupId = confProperties.getValue(Constants.FLINK_SQL_INJECTION_CONFIG, Constants.SQL_INJECTION_GROUP_ID)
    //kafka source 的topic
    val kafkaSourceTopic = confProperties.getValue(Constants.OPERATION_FLINK_TO_DRUID_CONFIG, Constants.OPERATION_TOPIC)
    //kafka sink 的topic
    val kafkaSinkTopic = confProperties.getValue(Constants.FLINK_SQL_INJECTION_CONFIG, Constants
      .SQL_INJECTION_KAFKA_SINK_TOPIC)

    //flink全局变量
    val parameters: Configuration = new Configuration()
    //c3p0连接池配置文件路径
    parameters.setString(Constants.c3p0_CONFIG_PATH, confProperties.getValue(Constants.FLINK_COMMON_CONFIG, Constants
      .c3p0_CONFIG_PATH))
    //文件系统类型
    parameters.setString(Constants.FILE_SYSTEM_TYPE, confProperties.getValue(Constants.FLINK_COMMON_CONFIG, Constants
      .FILE_SYSTEM_TYPE))
    //sql注入规则路径
    parameters.setString(Constants.SQL_INJECTION_RULE_PATH, confProperties.getValue(Constants
      .FLINK_SQL_INJECTION_CONFIG, Constants.SQL_INJECTION_RULE_PATH))

    //check pointing的间隔
    val checkpointInterval = confProperties.getLongValue(Constants.FLINK_SQL_INJECTION_CONFIG, Constants
      .SQL_INJECTION_CHECKPOINT_INTERVAL)

    //获取ExecutionEnvironment
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    //设置check pointing的间隔
    env.enableCheckpointing(checkpointInterval)
    //设置flink全局变量
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
    //获取kafka生产者
    val producer = new FlinkKafkaProducer[String](brokerList, kafkaSinkTopic, new SimpleStringSchema())

    // 获取kafka数据
    val dStream = env.addSource(consumer).setParallelism(kafkaSourceParallelism)
      .uid(kafkaSourceName).name(kafkaSourceName)

    //val dStream = env.readTextFile("/Users/chenwei/Downloads/测试数据/sql注入样例数据(刘东提供)2.txt")
    //  .map(tuple => {
    //    val values = tuple.split("\\|",-1)
    //    val stringBuilder = new StringBuilder
    //    for (i <- 0 until 30){
    //      stringBuilder.append(values(i))
    //      stringBuilder.append("|")
    //    }
    //    stringBuilder.append(values(30))
    //    stringBuilder.toString()
    //  })

    //ip-地点关联文件路径
    val placePath = confProperties.getValue(Constants.OPERATION_FLINK_TO_DRUID_CONFIG, Constants.OPERATION_PLACE_PATH)
    //host-系统名关联文件路径
    val systemPath = confProperties.getValue(Constants.OPERATION_FLINK_TO_DRUID_CONFIG, Constants.OPERATION_SYSTEM_PATH)
    //用户名-常用登录地关联文件路径
    val usedPlacePath = confProperties.getValue(Constants.OPERATION_FLINK_TO_DRUID_CONFIG, Constants
      .OPERATION_USEDPLACE_PATH)


    val sqlInjectionWarnStream = dStream
      .map(new RichMapFunction[String, (Option[OperationModel], String)] {
        override def open(parameters: Configuration): Unit = {
          OperationModel.setPlaceMap(placePath)
          OperationModel.setSystemMap(systemPath)
          OperationModel.setMajorMap(systemPath)
          OperationModel.setUsedPlacesMap(usedPlacePath)
        }

        override def map(value: String): (Option[OperationModel], String) = (OperationModel.getOperationModel(value),
          value)
      }).setParallelism(1)
      .filter(_._1.isDefined).setParallelism(dealParallelism)
      .map(t => (t._1.head, t._2)).setParallelism(dealParallelism)
      .process(new SqlInjectionProcessFunction)

    val value = sqlInjectionWarnStream.map(_._1)
    val alertKafkaValue = sqlInjectionWarnStream.map(_._2)
    value.addSink(new MySQLSink).uid(sqlSinkName).name(sqlSinkName)
      .setParallelism(sqlSinkParallelism)

    sqlInjectionWarnStream
      .map(o => {
        JsonUtil.toJson(o._1._1.asInstanceOf[BbasSqlInjectionWarnEntity])
      })
      .addSink(producer)
      .uid(kafkaSinkName)
      .name(kafkaSinkName)
      .setParallelism(kafkaSinkParallelism)

        //将告警数据写入告警数据库topic
        val warningProducer = new FlinkKafkaProducer[String](brokerList, warningSinkTopic, new
            SimpleStringSchema())
    alertKafkaValue.addSink(warningProducer).setParallelism(kafkaSinkParallelism)

    env.execute(jobName)

  }


  class SqlInjectionProcessFunction extends ProcessFunction[(OperationModel, String), ((Object, Boolean), String)] {


    var libinjection: Libinjection = null

    var groupSplit: Char = _
    var kvSplit: Char = _
    var inputKafkaValue = ""

    override def open(parameters: Configuration): Unit = {

      val globConf = getRuntimeContext.getExecutionConfig.getGlobalJobParameters.asInstanceOf[Configuration]
      val rulePath = globConf.getString(Constants.SQL_INJECTION_RULE_PATH, "/")
      val fileSystemType = globConf.getString(Constants.FILE_SYSTEM_TYPE, "/")

      libinjection = new Libinjection(rulePath, fileSystemType)

      groupSplit = globConf.getInteger(Constants.SQL_INJECTION_GROUP_SPLIT, 0).asInstanceOf[Char]

      kvSplit = globConf.getInteger(Constants.SQL_INJECTION_KV_SPLIT, 0).asInstanceOf[Char]
    }

    override def processElement(value: (OperationModel, String), ctx: ProcessFunction[(OperationModel, String),
      ((Object, Boolean), String)]#Context, out: Collector[((Object, Boolean), String)]): Unit = {

      val values = value._2.split("\\|", -1)

      if (values.length >= 30) {

        var isSqlLi = false
        var httpStatus = values(13).toInt
        val injectionValueBuffer = new StringBuffer

        val url = values(6)
        if (!url.toLowerCase.contains("http://mssportal.mss.ctc.com/webdynpro/dispatcher/sap.com/pb/pagebuilder;")) {

          val formValues = values(30)
          if (url.contains("?")) {

            for (parameters <- url.split("\\?", -1)(1).split("\\&", -1)) {
              val parameter = parameters.split("=", 2)
              if (parameter.length == 2) {

                var urlValue = ""

                try {
                  urlValue = URLDecoder.decode(parameter(1).replaceAll("%(?![0-9a-fA-F]{2})", "%25"), "utf-8")
                } catch {
                  case e: Exception => {

                  }
                }

                if (urlValue.length > 0 && libinjection.libinjection_sqli(urlValue)) {
                  injectionValueBuffer.append(parameter(1))
                  injectionValueBuffer.append("|")
                  isSqlLi = true
                }
              }
            }
          }

          if (formValues != null && formValues.length > 0) {
            for (formValue <- formValues.split(groupSplit)) {

              val kvValues = formValue.split(kvSplit)
              if (kvValues != null && kvValues.length == 2) {

                var urlValue = ""

                try {
                  urlValue = URLDecoder.decode(kvValues(1).replaceAll("%(?![0-9a-fA-F]{2})", "%25"), "utf-8")
                } catch {
                  case e: Exception => {

                  }
                }

                if (libinjection.libinjection_sqli(urlValue)) {
                  injectionValueBuffer.append(kvValues(1))
                  injectionValueBuffer.append("|")
                  isSqlLi = true
                }
              }

            }

          }

          if (isSqlLi) {
            if (injectionValueBuffer.length() > 0) {
              injectionValueBuffer.deleteCharAt(injectionValueBuffer.length() - 1)
            }
            val bbasSqlInjectionWarnEntity = new BbasSqlInjectionWarnEntity
            bbasSqlInjectionWarnEntity.setWarnDatetime(new Timestamp(value._1.timeStamp))
            bbasSqlInjectionWarnEntity.setUsername(value._1.userName)
            bbasSqlInjectionWarnEntity.setLoginSystem(value._1.loginSystem)
            bbasSqlInjectionWarnEntity.setDestinationIp(value._1.destinationIp)
            bbasSqlInjectionWarnEntity.setLoginPlace(value._1.loginPlace)
            bbasSqlInjectionWarnEntity.setSourceIp(value._1.sourceIp)
            bbasSqlInjectionWarnEntity.setUrl(url)
            bbasSqlInjectionWarnEntity.setHttpStatus(httpStatus)
            if (formValues.length > 1000) {
              bbasSqlInjectionWarnEntity.setFormValue(formValues.substring(0, 1000))
            } else {
              bbasSqlInjectionWarnEntity.setFormValue(formValues)
            }

            if (injectionValueBuffer.length() > 1000) {
              bbasSqlInjectionWarnEntity.setInjectionValue(injectionValueBuffer.substring(0, 1000))
            } else {
              bbasSqlInjectionWarnEntity.setInjectionValue(injectionValueBuffer.toString)
            }

            val outValue = getInputKafkaValue(value._1, url, "SQL注入告警", "")
            out.collect((bbasSqlInjectionWarnEntity.asInstanceOf[Object], false), outValue)

          }

        }
      }
    }
  }

}
