/*
 * @project mss
 * @company Fujian Fujitsu Communication Software Co., Ltd.
 * @author chenwei
 * @date 2019-04-03 14:15:39
 * @version v1.0
 * @update [no] [date YYYY-MM-DD] [name] [description]
 */
package cn.ffcs.is.mss.analyzer.flink.warn

import java.io.{BufferedReader, InputStreamReader}
import java.net.URI
import java.sql.Timestamp
import java.util.Properties
import java.util.concurrent.{ExecutorService, Executors, ThreadPoolExecutor}

import cn.ffcs.is.mss.analyzer.bean.{BbasAbnormalStatusUserWarnEntity, CtPostInfoEntity}
import cn.ffcs.is.mss.analyzer.druid.model.scala.OperationModel
import cn.ffcs.is.mss.analyzer.flink.sink.MySQLSink
import cn.ffcs.is.mss.analyzer.utils._
import org.apache.flink.api.common.functions.RichFilterFunction
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.ProcessFunction
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaConsumer, FlinkKafkaProducer}
import org.apache.flink.streaming.util.serialization.SimpleStringSchema
import org.apache.flink.util.Collector
import org.apache.hadoop.fs.{FileSystem, Path}
import redis.clients.jedis.{Jedis, JedisPool, JedisPubSub}

import scala.collection.mutable
import scala.collection.JavaConversions._

/**
  *
  * @author chenwei
  * @date 2019-04-03 14:15:39
  * @title AbnormalStatusUser 异常状态的用户（离职用户在线）
  * @update [no] [date YYYY-MM-DD] [name] [description]
  */

object AbnormalStatusUser {
  def main(args: Array[String]): Unit = {

    //根据传入的参数解析配置文件
    //val args0 = "/Users/chenwei/IdeaProjects/mss/src/main/resources/flink.ini"
    //val confProperties = new IniProperties(args0)
    val confProperties = new IniProperties(args(0))

    //该任务的名字
    val jobName = confProperties.getValue(Constants.FLINK_ABNORMAL_STATUS_USER_CONFIG, Constants.ABNORMAL_STATUS_USER_JOB_NAME)
    //kafka Source的名字
    val kafkaSourceName = confProperties.getValue(Constants.FLINK_ABNORMAL_STATUS_USER_CONFIG, Constants.ABNORMAL_STATUS_USER_KAFKA_SOURCE_NAME)
    //mysql sink的名字
    val sqlSinkName = confProperties.getValue(Constants.FLINK_ABNORMAL_STATUS_USER_CONFIG, Constants.ABNORMAL_STATUS_USER_SQL_SINK_NAME)
    //kafka sink的名字
    val kafkaSinkName = confProperties.getValue(Constants.FLINK_ABNORMAL_STATUS_USER_CONFIG, Constants.ABNORMAL_STATUS_USER_KAFKA_SINK_NAME)

    //kafka Source的并行度
    val kafkaSourceParallelism = confProperties.getIntValue(Constants.FLINK_ABNORMAL_STATUS_USER_CONFIG, Constants.ABNORMAL_STATUS_USER_KAFKA_SOURCE_PARALLELISM)
    //对数据处理的并行度
    val dealParallelism = confProperties.getIntValue(Constants.FLINK_ABNORMAL_STATUS_USER_CONFIG, Constants.ABNORMAL_STATUS_USER_DEAL_PARALLELISM)
    //写入mysql的并行度
    val sqlSinkParallelism = confProperties.getIntValue(Constants.FLINK_ABNORMAL_STATUS_USER_CONFIG, Constants.ABNORMAL_STATUS_USER_SQL_SINK_PARALLELISM)
    //写入kafka的并行度
    val kafkaSinkParallelism = confProperties.getIntValue(Constants.FLINK_ABNORMAL_STATUS_USER_CONFIG, Constants.ABNORMAL_STATUS_USER_KAFKA_SINK_PARALLELISM)

    val warningSinkTopic = confProperties.getValue(Constants.WARNING_FLINK_TO_DRUID_CONFIG, Constants.
      WARNING_TOPIC)
    //kafka的服务地址
    val brokerList = confProperties.getValue(Constants.FLINK_COMMON_CONFIG, Constants.KAFKA_BOOTSTRAP_SERVERS)
    //flink消费的group.id
    val groupId = confProperties.getValue(Constants.FLINK_ABNORMAL_STATUS_USER_CONFIG, Constants.ABNORMAL_STATUS_USER_GROUP_ID)
    //kafka的topic
    val kafkaSourceTopic = confProperties.getValue(Constants.OPERATION_FLINK_TO_DRUID_CONFIG, Constants.OPERATION_TO_KAFKA_TOPIC)
    //kafka sink 的topic
    val kafkaSinkTopic = confProperties.getValue(Constants.FLINK_ABNORMAL_STATUS_USER_CONFIG, Constants.ABNORMAL_STATUS_USER_ALERT_KAFKA_SINK_TOPIC)

    //flink全局变量
    val parameters: Configuration = new Configuration()
    //文件系统类型
    parameters.setString(Constants.FILE_SYSTEM_TYPE, confProperties.getValue(Constants.FLINK_COMMON_CONFIG, Constants.FILE_SYSTEM_TYPE))
    //c3p0连接池配置文件路径
    parameters.setString(Constants.c3p0_CONFIG_PATH, confProperties.getValue(Constants.FLINK_COMMON_CONFIG, Constants.c3p0_CONFIG_PATH))
    //redis连接池配置文件路径
    parameters.setString(Constants.REDIS_CONFIG_PATH, confProperties.getValue(Constants.FLINK_COMMON_CONFIG, Constants.REDIS_CONFIG_PATH))
    //redis 用户状态hash的key
    parameters.setString(Constants.ABNORMAL_STATUS_USER_USER_STATUS_REDIS_KEY, confProperties.getValue(Constants.FLINK_ABNORMAL_STATUS_USER_CONFIG, Constants.ABNORMAL_STATUS_USER_USER_STATUS_REDIS_KEY))
    //监听redis的频道名
    parameters.setString(Constants.ABNORMAL_STATUS_USER_CHANNEL_NAME, confProperties.getValue(Constants.FLINK_ABNORMAL_STATUS_USER_CONFIG, Constants.ABNORMAL_STATUS_USER_CHANNEL_NAME))


    //check pointing的间隔
    val checkpointInterval = confProperties.getLongValue(Constants.FLINK_ABNORMAL_STATUS_USER_CONFIG, Constants.ABNORMAL_STATUS_USER_CHECKPOINT_INTERVAL)


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
    val consumer = new FlinkKafkaConsumer[String](kafkaSourceTopic, new SimpleStringSchema, props).setStartFromGroupOffsets()
    //获取kafka生产者
    val producer = new FlinkKafkaProducer[String](brokerList, kafkaSinkTopic, new SimpleStringSchema())

    // 获取kafka数据
    val dStream = env.addSource(consumer).setParallelism(kafkaSourceParallelism)
      .uid(kafkaSourceName).name(kafkaSourceName)


    val abnormalStatusUserStream = dStream
      //            .map(OperationModel.getOperationModel _)
      //            .filter(_.isDefined)
      //            .map(_.head)
      //            .map(JsonUtil.toJson(_))
      .map(JsonUtil.fromJson[OperationModel] _)
      .filter(new isCorrectUsername)
      .process(new AbnormalStatusUserProcess).setParallelism(dealParallelism)

    val value: DataStream[(Object, Boolean)] = abnormalStatusUserStream.map(_._1)
    val alertKafkaValue = abnormalStatusUserStream.map(_._2)
    value.addSink(new MySQLSink).uid(sqlSinkName).name(sqlSinkName)
      .setParallelism(sqlSinkParallelism)

    abnormalStatusUserStream
      .map(o => {JsonUtil.toJson(o._1._1.asInstanceOf[BbasAbnormalStatusUserWarnEntity])})
      .addSink(producer)
      .uid(kafkaSinkName)
      .name(kafkaSinkName)
      .setParallelism(kafkaSinkParallelism)

    //将告警数据写入告警库topic
    val warningProducer = new FlinkKafkaProducer[String](brokerList, warningSinkTopic, new
        SimpleStringSchema())
    alertKafkaValue.addSink(warningProducer).setParallelism(kafkaSinkParallelism)
    env.execute(jobName)

  }

  class isCorrectUsername extends RichFilterFunction[OperationModel] {
    override def filter(value: OperationModel): Boolean = {
      val username = value.userName
      !(username == null || username.length != 11 || username.charAt(8) != '@' || "匿名用户".equals(username))
    }
  }

  class AbnormalStatusUserProcess extends ProcessFunction[OperationModel, ((Object,
    Boolean), String)] {

    var jedisPool: JedisPool = _
    var jedis: Jedis = _
    var USER_STATUS_KEY: String = _
    var USER_STATUS_MAP: java.util.Map[String, String] = _
    var CHANNEL_NAME: String = _
    var sqlHelper: SQLHelper = _
    var threadPool : ExecutorService = _
    val inputKafkaValue: String = ""

    override def open(parameters: Configuration): Unit = {

      val globConf = getRuntimeContext.getExecutionConfig.getGlobalJobParameters
        .asInstanceOf[Configuration]

      //根据redis配置文件,初始化redis连接池
      val jedisProperties = new Properties()
      val jedisConfigPath = globConf.getString(Constants.REDIS_CONFIG_PATH, "")
      val fileSystemType = globConf.getString(Constants.FILE_SYSTEM_TYPE, "/")
      val fs = FileSystem.get(URI.create(fileSystemType), new org.apache.hadoop.conf
      .Configuration())
      val fsDataInputStream = fs.open(new Path(jedisConfigPath))
      val bufferedReader = new BufferedReader(new InputStreamReader(fsDataInputStream))
      jedisProperties.load(bufferedReader)

      jedisPool = JedisUtil.getJedisPool(jedisProperties)
      jedis = jedisPool.getResource

      USER_STATUS_KEY = globConf.getString(Constants.ABNORMAL_STATUS_USER_USER_STATUS_REDIS_KEY, "")
      CHANNEL_NAME = globConf.getString(Constants.ABNORMAL_STATUS_USER_CHANNEL_NAME, "")
      USER_STATUS_MAP = getUserStatusMap(jedis, USER_STATUS_KEY)

      //根据c3p0配置文件,初始化c3p0连接池
      val c3p0Properties = new Properties()
      val c3p0ConfigPath = globConf.getString(Constants.c3p0_CONFIG_PATH, "")
      c3p0Properties.load(new BufferedReader(new InputStreamReader(fs.open(new Path(c3p0ConfigPath)))))
      C3P0Util.ini(c3p0Properties)

      //操作数据库的类
      sqlHelper = new SQLHelper()

      threadPool = Executors.newFixedThreadPool(2)
      threadPool.execute(new RedisListener(jedisPool.getResource, CHANNEL_NAME, USER_STATUS_KEY))
    }

    override def processElement(value: OperationModel, ctx: ProcessFunction[OperationModel, ((Object, Boolean), String)]#Context,
                                out: Collector[((Object, Boolean), String)]): Unit = {

      val status = getUserStatus(jedis, sqlHelper, USER_STATUS_MAP, value.userName, USER_STATUS_KEY)
      if (status == null || isAbnormalStatus(status)) {
        val bbasAbnormalStatusUserWarnEntity = new BbasAbnormalStatusUserWarnEntity()
        bbasAbnormalStatusUserWarnEntity.setDestinationIp(value.destinationIp)
        bbasAbnormalStatusUserWarnEntity.setLoginPlace(value.loginPlace)
        bbasAbnormalStatusUserWarnEntity.setLoginSystem(value.loginSystem)
        bbasAbnormalStatusUserWarnEntity.setSourceIp(value.sourceIp)
        bbasAbnormalStatusUserWarnEntity.setStatus(status)
        bbasAbnormalStatusUserWarnEntity.setUsername(value.userName)
        bbasAbnormalStatusUserWarnEntity.setWarnDatetime(new Timestamp(value.timeStamp))
        val inputKafkaValue = value.userName + "|" + "状态异常用户" + "|" + value.timeStamp + "|" +
          "" + "|" + value.loginSystem + "|" + "" + "|" +
          "" + "|" + value.sourceIp + "|" + value.sourcePort + "|" +
          value.destinationIp + "|" + value.destinationPort + "|" + "" + "|" +
          status + "|" + "" + "|" + ""

        out.collect((bbasAbnormalStatusUserWarnEntity.asInstanceOf[Object], true), inputKafkaValue)
      }


    }

    override def close(): Unit = {

      threadPool.shutdownNow()

    }

    /**
      * 获取用户状态
      *
      * @param jedis
      * @param key
      * @param userName
      * @return
      */
    def getUserStatus(jedis: Jedis, sqlHelper: SQLHelper, map: java.util.Map[String, String], userName: String, key: String): String = {
      if (map.containsKey(userName)) {
        return map(userName)
      } else {

        var userStatus = "2"

        // 慢查询报错，只能查询单个字段
        val list = sqlHelper.query("SELECT USER_STATUS FROM  `SDFS`.`ct_post_info`  WHERE LOGIN_NAME = \"" + userName + "\"")
        if (list != null && list.size() > 0) {
          try {
            userStatus = list.get(0).get("USER_STATUS").toString
          } catch {
            case e: Exception => {}
          }
        }

        map.put(userName, userStatus)
        jedis.hset(key, userName, userStatus)
        return userStatus
      }
    }

    /**
      * 获取用户状态Map
      *
      * @param jedis
      * @param key
      * @param
      * @return
      */
    def getUserStatusMap(jedis: Jedis, key: String): java.util.Map[String, String] = {
      jedis.hgetAll(key)
    }

    /**
      * 是否是异常状态
      *
      * @param
      * @param
      * @param
      * @return
      */
    def isAbnormalStatus(status: String): Boolean = {
      !"1".equals(status)
    }


    /**
      * 监听redis改动的线程
      *
      * @param
      * @param channelName
      * @param key
      */
    class RedisListener(jedis: Jedis, channelName: String, key: String) extends Runnable {

      override def run(): Unit = {
        val jedisPubSub = new JedisPubSub() {

          override def onPMessage(pattern: String, channel: String, message: String): Unit = {
            USER_STATUS_MAP = getUserStatusMap(jedis, key)
          }

        }

        jedis.psubscribe(jedisPubSub, channelName)
      }

    }

  }

}
