package cn.ffcs.is.mss.analyzer.flink.warn

import java.io.{BufferedReader, InputStreamReader}
import java.net.{URI, URLDecoder}
import java.sql.Timestamp
import java.util.Properties
import cn.ffcs.is.mss.analyzer.bean.BbasSqlInjectionWarnValidityEntity
import cn.ffcs.is.mss.analyzer.druid.model.scala.OperationModel
import cn.ffcs.is.mss.analyzer.flink.sink.MySQLSink
import cn.ffcs.is.mss.analyzer.utils.GetInputKafkaValue.getInputKafkaValue
import cn.ffcs.is.mss.analyzer.utils.libInjection.sql.Libinjection
import cn.ffcs.is.mss.analyzer.utils.{Constants, IniProperties, JedisUtil, JsonUtil}
import org.apache.flink.api.common.accumulators.LongCounter
import org.apache.flink.api.common.functions.RichMapFunction
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.functions.ProcessFunction
import org.apache.flink.streaming.api.scala.{StreamExecutionEnvironment, _}
import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaConsumer, FlinkKafkaProducer}
import org.apache.flink.streaming.util.serialization.SimpleStringSchema
import org.apache.flink.util.Collector
import org.apache.hadoop.fs.{FileSystem, Path}
import redis.clients.jedis.{Jedis, JedisPool}

/**
 * @title sqlInjectionPackageAnalyze
 * @author hanyu
 * @date 2021-03-10 16:49
 * @description
 * @update [no][date YYYY-MM-DD][name][description]
 */
object sqlInjectionValidity {

  def main(args: Array[String]): Unit = {

    //根据传入的参数解析配置文件
    //val args0 = "/Users/chenwei/IdeaProjects/mss/src/main/resources/flink.ini"
    //val confProperties = new IniProperties(args0)
    val confProperties = new IniProperties(args(0))
    //该任务的名字
    val jobName = confProperties.getValue(Constants.SQL_INJECTION_VALIDITY_CONFIG, Constants.SQL_INJECTION_VALIDITY_JOB_NAME)
    //kafka Source的名字
    val kafkaSourceName = confProperties.getValue(Constants.SQL_INJECTION_VALIDITY_CONFIG, Constants
      .SQL_INJECTION_VALIDITY_KAFKA_SOURCE_NAME)
    //mysql sink的名字
    val sqlSinkName = confProperties.getValue(Constants.SQL_INJECTION_VALIDITY_CONFIG, Constants
      .SQL_INJECTION_VALIDITY_SQL_SINK_NAME)
    //kafka sink的名字

    val kafkaSinkName = confProperties.getValue(Constants.SQL_INJECTION_VALIDITY_CONFIG, Constants
      .SQL_INJECTION_VALIDITY_KAFKA_SINK_NAME)


    //kafka Source的并行度
    val kafkaSourceParallelism = confProperties.getIntValue(Constants.SQL_INJECTION_VALIDITY_CONFIG, Constants
      .SQL_INJECTION_VALIDITY_KAFKA_SOURCE_PARALLELISM)
    //对数据处理的并行度
    val dealParallelism = confProperties.getIntValue(Constants.SQL_INJECTION_VALIDITY_CONFIG, Constants
      .SQL_INJECTION_VALIDITY_DEAL_PARALLELISM)
    //写入mysql的并行度
    val sqlSinkParallelism = confProperties.getIntValue(Constants.SQL_INJECTION_VALIDITY_CONFIG, Constants
      .SQL_INJECTION_VALIDITY_SQL_SINK_PARALLELISM)
    //写入kafka的并行度
    val kafkaSinkParallelism = confProperties.getIntValue(Constants.SQL_INJECTION_VALIDITY_CONFIG, Constants
      .SQL_INJECTION_VALIDITY_KAFKA_SINK_PARALLELISM)


    //kafka的服务地址
    val brokerList = confProperties.getValue(Constants.FLINK_COMMON_CONFIG, Constants.KAFKA_BOOTSTRAP_SERVERS)
    //flink消费的group.id
    val groupId = confProperties.getValue(Constants.SQL_INJECTION_VALIDITY_CONFIG, Constants.SQL_INJECTION_VALIDITY_GROUP_ID)
    //kafka source 的topic
    val kafkaSourceTopic = confProperties.getValue(Constants.OPERATION_FLINK_TO_DRUID_CONFIG, Constants.OPERATION_TOPIC)
    //kafka sink 的topic
    val kafkaSinkTopic = confProperties.getValue(Constants.SQL_INJECTION_VALIDITY_CONFIG, Constants
      .SQL_INJECTION_VALIDITY_KAFKA_SINK_TOPIC)
    val warningSinkTopic = confProperties.getValue(Constants.WARNING_FLINK_TO_DRUID_CONFIG, Constants
      .WARNING_TOPIC)
    //flink全局变量
    val parameters: Configuration = new Configuration()
    //c3p0连接池配置文件路径
    parameters.setString(Constants.c3p0_CONFIG_PATH, confProperties.getValue(Constants.FLINK_COMMON_CONFIG, Constants
      .c3p0_CONFIG_PATH))
    //文件系统类型
    parameters.setString(Constants.FILE_SYSTEM_TYPE, confProperties.getValue(Constants.FLINK_COMMON_CONFIG, Constants
      .FILE_SYSTEM_TYPE))
    //sql注入规则路径
    parameters.setString(Constants.SQL_INJECTION_VALIDITY_RULE_PATH, confProperties.getValue(Constants
      .SQL_INJECTION_VALIDITY_CONFIG, Constants.SQL_INJECTION_VALIDITY_RULE_PATH))
    //redis todo 测试阶段使用解回包写入redis的配置信息
    parameters.setString(Constants.REDIS_PACKAGE_PROPERTIES, confProperties.getValue(Constants
      .FLINK_COMMON_CONFIG, Constants.REDIS_PACKAGE_PROPERTIES))

    parameters.setInteger(Constants.SQL_INJECTION_VALIDITY_GROUP_SPLIT, confProperties.getIntValue(Constants.
      SQL_INJECTION_VALIDITY_CONFIG, Constants.SQL_INJECTION_VALIDITY_GROUP_SPLIT))

    parameters.setInteger(Constants.SQL_INJECTION_VALIDITY_KV_SPLIT, confProperties.getIntValue(Constants.
      SQL_INJECTION_VALIDITY_CONFIG, Constants.SQL_INJECTION_VALIDITY_KV_SPLIT))

    //check pointing的间隔
    val checkpointInterval = confProperties.getLongValue(Constants.SQL_INJECTION_VALIDITY_CONFIG, Constants
      .SQL_INJECTION_VALIDITY_CHECKPOINT_INTERVAL)

    //获取ExecutionEnvironment
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    //设置check pointing的间隔
    //env.enableCheckpointing(checkpointInterval)
    //设置flink全局变量
    env.getConfig.setGlobalJobParameters(parameters)
    env.setStreamTimeCharacteristic(TimeCharacteristic.ProcessingTime)


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
    val warningProducer = new FlinkKafkaProducer[String](brokerList, warningSinkTopic, new
        SimpleStringSchema())

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
    val value: DataStream[(Object, Boolean)] = sqlInjectionWarnStream.map(_._1)
    val alertKafkaValue = sqlInjectionWarnStream.map(_._2)
    value.addSink(new MySQLSink).uid(sqlSinkName).name(sqlSinkName)
      .setParallelism(1)

    value
      .filter(tup => {
        tup._1.asInstanceOf[BbasSqlInjectionWarnValidityEntity].getValidity == 1
      })
      .map(o => {
        JsonUtil.toJson(o._1.asInstanceOf[BbasSqlInjectionWarnValidityEntity])
      })
      .addSink(producer)
      .uid(kafkaSinkName)
      .name(kafkaSinkName)
      .setParallelism(kafkaSinkParallelism)

    alertKafkaValue.addSink(warningProducer).setParallelism(kafkaSinkParallelism)

    env.execute(jobName)

  }


  class SqlInjectionProcessFunction extends ProcessFunction[(OperationModel, String), ((Object, Boolean), String)] {

    var libinjection: Libinjection = null

    var groupSplit: Char = _
    var kvSplit: Char = _

    //redis
    var jedisPool: JedisPool = _
    var jedis: Jedis = _
    val urlValueMessage: LongCounter = new LongCounter()
    val formValuesMessage: LongCounter = new LongCounter()
    val isTrueMessage: LongCounter = new LongCounter()

    override def open(parameters: Configuration): Unit = {


      val globConf = getRuntimeContext.getExecutionConfig.getGlobalJobParameters.asInstanceOf[Configuration]
      val rulePath = globConf.getString(Constants.SQL_INJECTION_RULE_PATH, "/")
      val fileSystemType = globConf.getString(Constants.FILE_SYSTEM_TYPE, "/")
      val jedisConfigPath = globConf.getString(Constants.REDIS_PACKAGE_PROPERTIES, "")

      libinjection = new Libinjection(rulePath, fileSystemType)

      groupSplit = globConf.getInteger(Constants.SQL_INJECTION_VALIDITY_GROUP_SPLIT, 0).asInstanceOf[Char]

      kvSplit = globConf.getInteger(Constants.SQL_INJECTION_VALIDITY_KV_SPLIT, 0).asInstanceOf[Char]
      //online 配置
      //根据redis配置文件,初始化redis连接池
      val redisProperties = new Properties()
      val fs = FileSystem.get(URI.create(fileSystemType), new org.apache.hadoop.conf
      .Configuration())
      val fsDataInputStream = fs.open(new Path(jedisConfigPath))
      val bufferedReader = new BufferedReader(new InputStreamReader(fsDataInputStream))
      redisProperties.load(bufferedReader)
      jedisPool = JedisUtil.getJedisPool(redisProperties)
      jedis = jedisPool.getResource

      getRuntimeContext.addAccumulator("url value", urlValueMessage)
      getRuntimeContext.addAccumulator("form values", formValuesMessage)
      getRuntimeContext.addAccumulator("is true", isTrueMessage)


    }

    override def processElement(value: (OperationModel, String), ctx: ProcessFunction[(OperationModel, String),
      ((Object, Boolean), String)]#Context, out: Collector[((Object, Boolean), String)]): Unit = {

      val values = value._2.split("\\|", -1)


      if (values.length >= 30) {


        var isSqlLi = false
//        var httpStatus = values(13).toInt
        var httpStatus = value._1.httpStatus
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
                  urlValueMessage.add(1)
                  injectionValueBuffer.append(parameter(1))
                  injectionValueBuffer.append("|")
                  isSqlLi = true
                }
              }
            }
          }

          if (formValues != null && formValues.length > 0) {
            formValuesMessage.add(1)
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
            isTrueMessage.add(1)
            if (injectionValueBuffer.length() > 0) {
              injectionValueBuffer.deleteCharAt(injectionValueBuffer.length() - 1)
            }

            val bbasSqlInjectionWarnValidityEntity = new BbasSqlInjectionWarnValidityEntity
            bbasSqlInjectionWarnValidityEntity.setWarnDatetime(new Timestamp(value._1.timeStamp))
            bbasSqlInjectionWarnValidityEntity.setUsername(value._1.userName)
            bbasSqlInjectionWarnValidityEntity.setLoginSystem(value._1.loginSystem)
            bbasSqlInjectionWarnValidityEntity.setDestinationIp(value._1.destinationIp)
            bbasSqlInjectionWarnValidityEntity.setLoginPlace(value._1.loginPlace)
            bbasSqlInjectionWarnValidityEntity.setSourceIp(value._1.sourceIp)
            bbasSqlInjectionWarnValidityEntity.setUrl(url)
            bbasSqlInjectionWarnValidityEntity.setHttpStatus(httpStatus)
            //
            bbasSqlInjectionWarnValidityEntity.setPackageName(value._1.packagePath)
            if (formValues.length > 1000) {
              bbasSqlInjectionWarnValidityEntity.setFormValue(formValues.substring(0, 1000))
            } else {
              bbasSqlInjectionWarnValidityEntity.setFormValue(formValues)
            }

            if (injectionValueBuffer.length() > 1000) {
              bbasSqlInjectionWarnValidityEntity.setInjectionValue(injectionValueBuffer.substring(0, 1000))
            } else {
              bbasSqlInjectionWarnValidityEntity.setInjectionValue(injectionValueBuffer.toString)
            }

            Thread.sleep(60000)
            val packageValue = jedis.get(value._1.packagePath)
            if (packageValue != null && packageValue.nonEmpty) {
              bbasSqlInjectionWarnValidityEntity.setPackageTxt(packageValue)
            }
            if (value._1.httpStatus.toString.startsWith("2") || (value._1.packagePath != null && value._1.packagePath.nonEmpty)) {
              bbasSqlInjectionWarnValidityEntity.setValidity(1)
            } else {
              bbasSqlInjectionWarnValidityEntity.setValidity(0)
            }


            val outValue = getInputKafkaValue(value._1, url, "有效SQL注入攻击", packageValue)
            out.collect((bbasSqlInjectionWarnValidityEntity.asInstanceOf[Object], false), outValue)

          }

        }
      }
    }

  }

}

