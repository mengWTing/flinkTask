package cn.ffcs.is.mss.analyzer.flink.warn

import java.sql.Timestamp
import java.util.Properties

import cn.ffcs.is.mss.analyzer.bean.BbasUaAttackAttemptEntity
import cn.ffcs.is.mss.analyzer.druid.model.scala.OperationModel
import cn.ffcs.is.mss.analyzer.flink.sink.{MySQLSink, Sink}
import cn.ffcs.is.mss.analyzer.utils.GetInputKafkaValue.getInputKafkaValue
import cn.ffcs.is.mss.analyzer.utils.{Constants, IniProperties, JedisUtil, JsonUtil}
import org.apache.flink.configuration.{ConfigOptions, Configuration}
import org.apache.flink.streaming.api.functions.ProcessFunction
import org.apache.flink.streaming.api.scala._
import org.apache.flink.util.Collector
import org.apache.hadoop.fs.{FileSystem, Path}
import redis.clients.jedis.{Jedis, JedisPool}
import java.io.{BufferedReader, InputStreamReader}
import java.net.{URI, URLDecoder}

import cn.ffcs.is.mss.analyzer.flink.source.Source
import org.apache.flink.api.common.eventtime.WatermarkStrategy

import scala.collection.mutable.ArrayBuffer

/**
 * @title AttackAttemptWarn
 * @author hanyu
 * @date 2021-03-23 18:09
 * @description UA攻击尝试告警
 * @update [no][date YYYY-MM-DD][name][description]
 */
object AttackAttemptWarn {
  def main(args: Array[String]): Unit = {

    //val args0 = "./src/main/resources/flink.ini"
    //根据传入的参数解析配置文件

    //val confProperties = new IniProperties(args0)
    val confProperties = new IniProperties(args(0))

    //该任务的名字
    val jobName = confProperties.getValue(Constants.ATTACK_ATTEMPT_CONFIG,
      Constants.ATTACK_ATTEMPT_JOB_NAME)

    //source并行度
    val sourceParallelism = confProperties.getIntValue(Constants
      .ATTACK_ATTEMPT_CONFIG, Constants.ATTACK_ATTEMPT_SOURCE_PARALLELISM)
    //deal并行度
    val dealParallelism = confProperties.getIntValue(Constants
      .ATTACK_ATTEMPT_CONFIG, Constants.ATTACK_ATTEMPT_DEAL_PARALLELISM)
    //sink的并行度
    val sinkParallelism = confProperties.getIntValue(Constants.ATTACK_ATTEMPT_CONFIG,
      Constants.ATTACK_ATTEMPT_SINK_PARALLELISM)

    //kafka的服务地址
    val brokerList = confProperties.getValue(Constants.FLINK_COMMON_CONFIG, Constants
      .KAFKA_BOOTSTRAP_SERVERS)
    //flink消费的group.id
    val groupId = confProperties.getValue(Constants.ATTACK_ATTEMPT_CONFIG,
      Constants.ATTACK_ATTEMPT_GROUP_ID)
    //kafka source的topic
    val sourceTopic = confProperties.getValue(Constants.ATTACK_ATTEMPT_CONFIG, Constants
      .ATTACK_ATTEMPT_KAFKA_SOURCE_TOPIC)
    //kafka sink的topic
    val sinkTopic = confProperties.getValue(Constants.ATTACK_ATTEMPT_CONFIG, Constants
      .ATTACK_ATTEMPT_ANALYZE_KAFKA_SINK_TOPIC)
    val warningSinkTopic = confProperties.getValue(Constants.WARNING_FLINK_TO_DRUID_CONFIG, Constants
      .WARNING_TOPIC)


    //flink全局变量
    val parameters: Configuration = new Configuration()
    //c3p0连接池配置文件路径
    parameters.setString(Constants.c3p0_CONFIG_PATH, confProperties.getValue(Constants
      .FLINK_COMMON_CONFIG, Constants.c3p0_CONFIG_PATH))

    parameters.setString(Constants.FILE_SYSTEM_TYPE, confProperties.getValue(Constants
      .FLINK_COMMON_CONFIG, Constants.FILE_SYSTEM_TYPE))
    //UA攻击检测规则
    parameters.setString(Constants.ATTACK_ATTEMPT_KEY_STRING, confProperties.getValue(Constants.
      ATTACK_ATTEMPT_CONFIG, Constants.ATTACK_ATTEMPT_KEY_STRING))
    //UA python 白名单
    parameters.setString(Constants.ATTACK_ATTEMPT_PYTHON_WHITE_LIST, confProperties.getValue(Constants.
      ATTACK_ATTEMPT_CONFIG, Constants.ATTACK_ATTEMPT_PYTHON_WHITE_LIST))
    //    parameters.setString(Constants.FILE_SYSTEM_TYPE, confProperties.getValue(Constants
    //      .FLINK_COMMON_CONFIG, Constants.FILE_SYSTEM_TYPE))
    //redis todo 测试阶段使用解回包写入redis的配置信息
    parameters.setString(Constants.REDIS_PACKAGE_PROPERTIES, confProperties.getValue(Constants
      .FLINK_COMMON_CONFIG, Constants.REDIS_PACKAGE_PROPERTIES))

    //check pointing的间隔
    val checkpointInterval = confProperties.getLongValue(Constants
      .ATTACK_ATTEMPT_CONFIG, Constants.ATTACK_ATTEMPT_CHECKPOINT_INTERVAL)

    //获取kafka消费者
    val consumer = Source.kafkaSource(sourceTopic, groupId, brokerList)

    //获取kafka 生产者
    val producer = Sink.kafkaSink(brokerList, sinkTopic)
    val warningProducer = Sink.kafkaSink(brokerList, warningSinkTopic)

    //获取ExecutionEnvironment
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    //设置checkpoint
//    env.enableCheckpointing(checkpointInterval)
    env.getConfig.setGlobalJobParameters(parameters)
    env.getConfig.setAutoWatermarkInterval(0)

    //获取Kafka数据流
    val attackAttemptWarnStream = env.fromSource(consumer, WatermarkStrategy.noWatermarks(), "kafkaSource").setParallelism(sourceParallelism)
        .uid("kafkaSource").name("kafkaSource")
      .process(new AttackAttemptProcessFunction).setParallelism(dealParallelism)

    val value: DataStream[(Object, Boolean)] = attackAttemptWarnStream.map(_._1)
    val alertKafkaValue = attackAttemptWarnStream.map(_._2)

    value.addSink(new MySQLSink)

    value
      .map(o => {
        JsonUtil.toJson(o._1.asInstanceOf[BbasUaAttackAttemptEntity])
      })
      .sinkTo(producer)
      .setParallelism(sinkParallelism)

    alertKafkaValue.sinkTo(warningProducer).setParallelism(sinkParallelism)

    env.execute(jobName)

  }

  class AttackAttemptProcessFunction extends ProcessFunction[String, ((Object, Boolean), String)] {
    var matchAb = new ArrayBuffer[String]()
    var pythonWhiteListAb = new ArrayBuffer[String]()
    var attackKeyStr = ""
    var pythonWhiteList = ""
    var outValue = ""
    var url = ""
    var jedisPool: JedisPool = _
    var jedis: Jedis = _
    var packageValue = ""
    override def open(parameters: Configuration): Unit = {
      val globConf = getRuntimeContext.getExecutionConfig.getGlobalJobParameters.asInstanceOf[Configuration]
      attackKeyStr = globConf.getString(ConfigOptions.key(Constants.ATTACK_ATTEMPT_KEY_STRING).stringType().defaultValue(""))
      pythonWhiteList = globConf.getString(ConfigOptions.key(Constants.ATTACK_ATTEMPT_PYTHON_WHITE_LIST).stringType().defaultValue(""))
      val fileSystemType = globConf.getString(ConfigOptions.key(Constants.FILE_SYSTEM_TYPE).stringType().defaultValue("/"))

      val jedisConfigPath = globConf.getString(ConfigOptions.key(Constants.REDIS_PACKAGE_PROPERTIES).stringType().defaultValue(""))

      val matchArr = attackKeyStr.split("\\|", -1)
      val pythonWhiteListArr = pythonWhiteList.split("\\|", -1)
      for (i <- matchArr) {
        matchAb.append(i)
      }
      for (j <- pythonWhiteListArr) {
        pythonWhiteListAb.append(j)
      }
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

    override def processElement(value: String, ctx: ProcessFunction[String, ((Object, Boolean), String)]#Context,
                                out: Collector[((Object, Boolean), String)]): Unit = {
      val operationArr = value.split("\\|", -1)
      url = operationArr(6).toLowerCase()
      //      if ((operationArr.length >= 10 && scanStringMatch(operationArr(9), operationArr(5))._1)||(operationArr.length >= 6 &&url.contains("jndi:"))) {
      try {
        if (operationArr.length >= 10 && scanStringMatch(operationArr(9), url)._1) {
          val attackAttemptEntity = new BbasUaAttackAttemptEntity
          attackAttemptEntity.setWarnDatetime(new Timestamp(operationArr(10).toLong))
          attackAttemptEntity.setSourceIp(operationArr(3))
          attackAttemptEntity.setDestinationIp(operationArr(1))
          attackAttemptEntity.setUsername(operationArr(0))
          attackAttemptEntity.setUa(operationArr(9))
          attackAttemptEntity.setUaAttackTypr(scanStringMatch(operationArr(9), url)._2)
          attackAttemptEntity.setUrl(operationArr(6))
          attackAttemptEntity.setLoginSystem(operationArr(5))
          if (OperationModel.getOperationModel(value).isDefined) {
            val head: OperationModel = OperationModel.getOperationModel(value).head
            packageValue = jedis.get(head.packagePath)
            outValue = getInputKafkaValue(head, operationArr(6), "UA攻击", packageValue)
          } else {
            outValue = ""
          }
          out.collect((attackAttemptEntity.asInstanceOf[Object], false), outValue)
        }

      } catch {
        case e: Exception => {
        }
      }

    }

    //    def scanStringMatch(value: String, logInSystem: String): (Boolean, String) = {
    //      val lowerCase = value.toLowerCase
    //      for (i <- matchAb) {
    //        if (lowerCase.contains(i)) {
    //          if (i.equals("python") && !pythonWhiteListAb.contains(logInSystem)) {
    //            return (true, i)
    //          }
    //          return (true, i)
    //        }
    //      }
    //      (false, "")
    //    }
    def scanStringMatch(uaValue: String, urlValue: String): (Boolean, String) = {
      val lowerCase = uaValue.toLowerCase
      for (i <- matchAb) {
        if (lowerCase.contains(i) && !lowerCase.contains("python-urllib") && !lowerCase.contains("python-requests")) {
          return (true, i)
        }
      }
      try {
        val urlValueDec = URLDecoder.decode(urlValue.replaceAll("%(?![0-9a-fA-F]{2})", "%25"), "utf-8")
        if (urlValueDec.contains("jndi ")||urlValueDec.contains("jndi=")||urlValueDec.contains("jndi:")) {
          return (true, "jndi")
        }

      } catch {
        case e: Exception => {
        }
      }
      (false, "")
    }
  }

}
