package cn.ffcs.is.mss.analyzer.flink.warn

import java.io.{BufferedReader, InputStreamReader}
import java.net.{URI, URLDecoder}
import java.sql.Timestamp
import java.util.Properties

import cn.ffcs.is.mss.analyzer.bean.BbasXssInjectionWarnValidityEntity
import cn.ffcs.is.mss.analyzer.druid.model.scala.OperationModel
import cn.ffcs.is.mss.analyzer.flink.sink.{MySQLSink, Sink}
import cn.ffcs.is.mss.analyzer.flink.source.Source
import cn.ffcs.is.mss.analyzer.utils.GetInputKafkaValue.getInputKafkaValue
import cn.ffcs.is.mss.analyzer.utils.libInjection.xss.XSSInjectionUtil
import cn.ffcs.is.mss.analyzer.utils.{Constants, IniProperties, JedisUtil, JsonUtil, TimeUtil}
import org.apache.flink.api.common.eventtime.WatermarkStrategy
import org.apache.flink.configuration.ConfigOptions
import org.apache.flink.api.common.functions.RichMapFunction
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.ProcessFunction
import org.apache.flink.streaming.api.scala._
import org.apache.flink.util.Collector
import org.apache.hadoop.fs.{FileSystem, Path}
import redis.clients.jedis.{Jedis, JedisPool}


/**
 * @title XssInjectionPackageAnalyze
 * @author hanyu
 * @date 2021-03-11 09:13
 * @description
 * @update [no][date YYYY-MM-DD][name][description]
 */
object XssInjectionValidity {
  def main(args: Array[String]): Unit = {

    //根据传入的参数解析配置文件
    //val args0 = "/Users/chenwei/IdeaProjects/mss/src/main/resources/flink.ini"
    //val confProperties = new IniProperties(args0)
    val confProperties = new IniProperties(args(0))
    //该任务的名字
    val jobName = confProperties.getValue(Constants.XSS_INJECTION_VALIDITY_CONFIG, Constants
      .XSS_INJECTION_VALIDITY_JOB_NAME)
    //kafka Source的名字
    val kafkaSourceName = confProperties.getValue(Constants.XSS_INJECTION_VALIDITY_CONFIG, Constants
      .XSS_INJECTION_VALIDITY_KAFKA_SOURCE_NAME)
    //mysql sink的名字
    val sqlSinkName = confProperties.getValue(Constants.XSS_INJECTION_VALIDITY_CONFIG, Constants
      .XSS_INJECTION_VALIDITY_SQL_SINK_NAME)
    //kafka sink的名字
    val kafkaSinkName = confProperties.getValue(Constants.XSS_INJECTION_VALIDITY_CONFIG, Constants
      .XSS_INJECTION_VALIDITY_KAFKA_SINK_NAME)

    //kafka Source的并行度
    val kafkaSourceParallelism = confProperties.getIntValue(Constants.XSS_INJECTION_VALIDITY_CONFIG,
      Constants.XSS_INJECTION_VALIDITY_KAFKA_SOURCE_PARALLELISM)
    //对数据处理的并行度
    val dealParallelism = confProperties.getIntValue(Constants.XSS_INJECTION_VALIDITY_CONFIG,
      Constants.XSS_INJECTION_VALIDITY_DEAL_PARALLELISM)
    //写入mysql的并行度
    val sqlSinkParallelism = confProperties.getIntValue(Constants.XSS_INJECTION_VALIDITY_CONFIG,
      Constants.XSS_INJECTION_VALIDITY_SQL_SINK_PARALLELISM)
    //写入kafka的并行度
    val kafkaSinkParallelism = confProperties.getIntValue(Constants.XSS_INJECTION_VALIDITY_CONFIG,
      Constants.XSS_INJECTION_VALIDITY_KAFKA_SINK_PARALLELISM)

    //kafka的服务地址
    val brokerList = confProperties.getValue(Constants.FLINK_COMMON_CONFIG, Constants
      .KAFKA_BOOTSTRAP_SERVERS)
    //flink消费的group.id
    val groupId = confProperties.getValue(Constants.XSS_INJECTION_VALIDITY_CONFIG, Constants
      .XSS_INJECTION_VALIDITY_GROUP_ID)
    //kafka source 的topic
    val kafkaSourceTopic = confProperties.getValue(Constants.OPERATION_FLINK_TO_DRUID_CONFIG,
      Constants.OPERATION_TOPIC)
    //kafka sink 的topic
    val kafkaSinkTopic = confProperties.getValue(Constants.XSS_INJECTION_VALIDITY_CONFIG, Constants
      .XSS_INJECTION_VALIDITY_KAFKA_SINK_TOPIC)
    val warningSinkTopic = confProperties.getValue(Constants.WARNING_FLINK_TO_DRUID_CONFIG, Constants
      .WARNING_TOPIC)
    //flink全局变量
    val parameters: Configuration = new Configuration()
    //c3p0连接池配置文件路径
    parameters.setString(Constants.c3p0_CONFIG_PATH, confProperties.getValue(Constants
      .FLINK_COMMON_CONFIG, Constants.c3p0_CONFIG_PATH))
    //文件系统类型
    parameters.setString(Constants.FILE_SYSTEM_TYPE, confProperties.getValue(Constants
      .FLINK_COMMON_CONFIG, Constants.FILE_SYSTEM_TYPE))
    //sql注入规则路径
    parameters.setString(Constants.XSS_INJECTION_VALIDITY_RULE_PATH, confProperties.getValue(Constants
      .XSS_INJECTION_VALIDITY_CONFIG, Constants.XSS_INJECTION_VALIDITY_RULE_PATH))
    parameters.setString(Constants.XSS_INJECTION_VALIDITY_HDFS_RULE_PATH, confProperties.getValue
    (Constants.XSS_INJECTION_VALIDITY_CONFIG, Constants.XSS_INJECTION_VALIDITY_HDFS_RULE_PATH))

    parameters.setInteger(Constants.XSS_INJECTION_VALIDITY_GROUP_SPLIT, confProperties.getIntValue(Constants.
      XSS_INJECTION_VALIDITY_CONFIG, Constants.XSS_INJECTION_VALIDITY_GROUP_SPLIT))
    parameters.setInteger(Constants.XSS_INJECTION_VALIDITY_KV_SPLIT, confProperties.getIntValue(Constants.
      XSS_INJECTION_VALIDITY_CONFIG, Constants.XSS_INJECTION_VALIDITY_KV_SPLIT))

    //文件系统类型
    parameters.setString(Constants.FILE_SYSTEM_TYPE, confProperties.getValue(Constants.FLINK_COMMON_CONFIG, Constants.FILE_SYSTEM_TYPE))
    //redis todo 测试阶段使用解回包写入redis的配置信息
    parameters.setString(Constants.REDIS_PACKAGE_PROPERTIES, confProperties.getValue(Constants
      .FLINK_COMMON_CONFIG, Constants.REDIS_PACKAGE_PROPERTIES))

    //check pointing的间隔
    val checkpointInterval = confProperties.getLongValue(Constants.XSS_INJECTION_VALIDITY_CONFIG,
      Constants.XSS_INJECTION_VALIDITY_CHECKPOINT_INTERVAL)

    //获取ExecutionEnvironment
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    //设置check pointing的间隔
    //env.enableCheckpointing(checkpointInterval)
    //设置flink全局变量
    env.getConfig.setGlobalJobParameters(parameters)
    env.getConfig.setAutoWatermarkInterval(0)

    //获取kafka消费者
    val consumer = Source.kafkaSource(kafkaSourceTopic, groupId, brokerList)
    //获取kafka生产者
    val producer = Sink.kafkaSink(brokerList, kafkaSinkTopic)
    //获取写入告警数据的kafka生产者
    val warningProducer = Sink.kafkaSink(brokerList, warningSinkTopic)
    // 获取kafka数据
    val dStream = env.fromSource(consumer, WatermarkStrategy.noWatermarks(), kafkaSourceName).setParallelism(kafkaSourceParallelism)
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
    val placePath = confProperties.getValue(Constants.OPERATION_FLINK_TO_DRUID_CONFIG, Constants
      .OPERATION_PLACE_PATH)
    //host-系统名关联文件路径
    val systemPath = confProperties.getValue(Constants.OPERATION_FLINK_TO_DRUID_CONFIG, Constants
      .OPERATION_SYSTEM_PATH)
    //用户名-常用登录地关联文件路径
    val usedPlacePath = confProperties.getValue(Constants.OPERATION_FLINK_TO_DRUID_CONFIG,
      Constants.OPERATION_USEDPLACE_PATH)
    val xssInjectionWarnStream: DataStream[((Object, Boolean), String)] = dStream
      .map(new RichMapFunction[String, (Option[OperationModel], String)] {
        override def open(parameters: Configuration): Unit = {
          OperationModel.setPlaceMap(placePath)
          OperationModel.setSystemMap(systemPath)
          OperationModel.setMajorMap(systemPath)
          OperationModel.setUsedPlacesMap(usedPlacePath)
        }

        override def map(value: String): (Option[OperationModel], String) = (OperationModel
          .getOperationModel(value), value)
      }).setParallelism(1)
      .filter(_._1.isDefined).setParallelism(dealParallelism)
      .map(t => (t._1.head, t._2)).setParallelism(dealParallelism)
      .process(new XssInjectionProcessFunction)
    //DataStream[(Object, Boolean)]
    val value: DataStream[(Object, Boolean)] = xssInjectionWarnStream.map(_._1)
    val alertKafkaValue = xssInjectionWarnStream.map(_._2)
    value.addSink(new MySQLSink).uid(sqlSinkName).name(sqlSinkName)
      .setParallelism(sqlSinkParallelism)

    value
      .filter(tup => {
        tup._1.asInstanceOf[BbasXssInjectionWarnValidityEntity].getValidity == 1
      })
      .map(o => {
        JsonUtil.toJson(o._1.asInstanceOf[BbasXssInjectionWarnValidityEntity])
      })

      .sinkTo(producer)
      .uid(kafkaSinkName)
      .name(kafkaSinkName)
      .setParallelism(kafkaSinkParallelism)

    alertKafkaValue.sinkTo(warningProducer).setParallelism(kafkaSinkParallelism)

    env.execute(jobName)

  }


  class XssInjectionProcessFunction extends ProcessFunction[(OperationModel, String), ((Object, Boolean), String)] {
    var xSSInjectionUtil: XSSInjectionUtil = null
    var groupSplit: Char = _
    var kvSplit: Char = _
    //redis
    var jedisPool: JedisPool = _
    var jedis: Jedis = _

    override def open(parameters: Configuration): Unit = {
      val globConf = getRuntimeContext.getExecutionConfig.getGlobalJobParameters
        .asInstanceOf[Configuration]
      val rulePath = globConf.getString(ConfigOptions.key(Constants.XSS_INJECTION_VALIDITY_RULE_PATH).stringType().defaultValue("/"))
      val fileSystemType = globConf.getString(ConfigOptions.key(Constants.FILE_SYSTEM_TYPE).stringType().defaultValue("/"))
      val RuleHDFSPath = globConf.getString(ConfigOptions.key(Constants.XSS_INJECTION_VALIDITY_HDFS_RULE_PATH).stringType().defaultValue("/"))
      val jedisConfigPath = globConf.getString(ConfigOptions.key(Constants.REDIS_PACKAGE_PROPERTIES).stringType().defaultValue(""))

      //      System.load(rulePath)
      xSSInjectionUtil = new XSSInjectionUtil(fileSystemType, RuleHDFSPath, rulePath)

      groupSplit = globConf.getInteger(ConfigOptions.key(Constants.XSS_INJECTION_VALIDITY_GROUP_SPLIT).intType().defaultValue(0)).asInstanceOf[Char]
      kvSplit = globConf.getInteger(ConfigOptions.key(Constants.XSS_INJECTION_VALIDITY_KV_SPLIT).intType().defaultValue(0)).asInstanceOf[Char]

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

    }

    override def processElement(value: (OperationModel, String), ctx: ProcessFunction[(OperationModel, String),
      ((Object, Boolean), String)]#Context, out: Collector[((Object, Boolean), String)]): Unit = {
      val values = value._2.split("\\|", -1)
      if (values.length >= 30) {
        var isXssLi = false
        val injectionValueBuffer = new StringBuffer
        val url = values(6)
        val formValues = values(30)
        if (url.contains("?")) {
          for (parameters <- url.split("\\?", -1)(1).split("\\&", -1)) {
            val parameter = parameters.split("=", 2)
            if (parameter.length == 2) {
              var urlValue = ""
              try {
                urlValue = URLDecoder.decode(parameter(1).replaceAll("%(?![0-9a-fA-F]{2})",
                  "%25"), "utf-8")
              } catch {
                case e: Exception => {
                }
              }
              if (urlValue.nonEmpty && xSSInjectionUtil.checkXss(plusPercent(urlValue)) == 1) {
                injectionValueBuffer.append(parameter(1))
                injectionValueBuffer.append("|")
                isXssLi = true
              }
            }
          }

        }

        if (formValues != null && formValues.nonEmpty) {
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
              if (xSSInjectionUtil.checkXss(plusPercent(urlValue)) == 1) {
                injectionValueBuffer.append(kvValues(1))
                injectionValueBuffer.append("|")
                isXssLi = true
              }
            }

          }

        }

        if (isXssLi) {
          if (injectionValueBuffer.length() > 0) {
            injectionValueBuffer.deleteCharAt(injectionValueBuffer.length() - 1)
          }
          val bbasXssInjectionWarnValidityEntity = new BbasXssInjectionWarnValidityEntity
          bbasXssInjectionWarnValidityEntity.setWarnDatetime(new Timestamp(value._1.timeStamp))
          bbasXssInjectionWarnValidityEntity.setUsername(value._1.userName)
          bbasXssInjectionWarnValidityEntity.setLoginSystem(value._1.loginSystem)
          bbasXssInjectionWarnValidityEntity.setDestinationIp(value._1.destinationIp)
          bbasXssInjectionWarnValidityEntity.setLoginPlace(value._1.loginPlace)
          bbasXssInjectionWarnValidityEntity.setSourceIp(value._1.sourceIp)
          bbasXssInjectionWarnValidityEntity.setUrl(url)
          bbasXssInjectionWarnValidityEntity.setHttpStatus(value._1.httpStatus)
          //
          bbasXssInjectionWarnValidityEntity.setPackageName(value._1.packagePath)
          if (formValues.length > 1000) {
            bbasXssInjectionWarnValidityEntity.setFormValue(formValues.substring(0, 1000))
          } else {
            bbasXssInjectionWarnValidityEntity.setFormValue(formValues)
          }

          if (injectionValueBuffer.length() > 1000) {
            bbasXssInjectionWarnValidityEntity.setInjectionValue(injectionValueBuffer.substring(0, 1000))
          } else {
            bbasXssInjectionWarnValidityEntity.setInjectionValue(injectionValueBuffer.toString)
          }

          Thread.sleep(60000)
          val packageValue = jedis.get(value._1.packagePath)
          //          if (jedis.get(value._1.packagePath) != null && jedis.get(value._1.packagePath).length > 0) {
          if (packageValue != null && packageValue.nonEmpty) {
            bbasXssInjectionWarnValidityEntity.setPackageTxt(packageValue)
          }

          if (value._1.httpStatus.toString.startsWith("2") || (value._1.packagePath != null && value._1.packagePath.nonEmpty)) {
            bbasXssInjectionWarnValidityEntity.setValidity(1)
          } else {
            bbasXssInjectionWarnValidityEntity.setValidity(0)

          }

          val outValue = getInputKafkaValue(value._1, url, "有效XSS注入攻击", packageValue)
          out.collect((bbasXssInjectionWarnValidityEntity.asInstanceOf[Object], false), outValue)

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

  }


}
