/*
 * @project mss
 * @company Fujian Fujitsu Communication Software Co., Ltd.
 * @author chenwei
 * @date 2019-12-02 18:15:27
 * @version v1.0
 * @update [no] [date YYYY-MM-DD] [name] [description]
 */
package cn.ffcs.is.mss.analyzer.flink.behavior.user

import cn.ffcs.is.mss.analyzer.flink.behavior.user.filter.UserBehaviorAnalyzerFilter
import cn.ffcs.is.mss.analyzer.flink.behavior.user.sink.{UserBehaviorAnalyzerPhoenixSink,
  UserBehaviorAnalyzerTotalScore}
import cn.ffcs.is.mss.analyzer.utils.{Constants, IniProperties}
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer
import java.util.Properties

import cn.ffcs.is.mss.analyzer.flink.behavior.user.process.UserBehaviorAnalyzerProcess
import cn.ffcs.is.mss.analyzer.flink.sink.MySQLSink

import scala.io.Source


/**
  *
  * @author chenwei
  * @date 2019-12-02 18:15:27
  * @title UserBehaviorAnalyzer3  流任务是进行和历史画像数据对比 判断是否出相应的告警
  * @update [no] [date YYYY-MM-DD] [name] [description]
  */
object UserBehaviorAnalyzerStream {
  def main(args: Array[String]): Unit = {

    //val args0 = "./src/main/resources/flink.ini"
    //val args0 = "/project/flink/conf/flink.ini"
    //根据传入的参数解析配置文件
    //val confProperties = new IniProperties(args0)
    val confProperties = new IniProperties(args(0))

    //该任务的名字
    val jobName = confProperties.getValue(Constants.FLINK_USER_BEHAVIOR_ANALYZER_CONFIG,
      Constants.USER_BEHAVIOR_ANALYZER_STREAM_JOB_NAME)
    //kafka Source的名字
    val kafkaSourceName = confProperties.getValue(Constants.FLINK_USER_BEHAVIOR_ANALYZER_CONFIG,
      Constants.USER_BEHAVIOR_ANALYZER_KAFKA_SOURCE_NAME)
    //mysql sink的名字
    val sqlSinkName = confProperties.getValue(Constants.FLINK_USER_BEHAVIOR_ANALYZER_CONFIG,
      Constants.USER_BEHAVIOR_ANALYZER_SQL_SINK_NAME)
    //用户行为分析phoenix sink名
    val phoenixSinkName = confProperties.getValue(Constants.FLINK_USER_BEHAVIOR_ANALYZER_CONFIG,
      Constants.USER_BEHAVIOR_ANALYZER_PHOENIX_SINK_NAME)
    //用户行为分析更新总分sink名
    val totalScoreSinkName = confProperties.getValue(Constants.FLINK_USER_BEHAVIOR_ANALYZER_CONFIG,
      Constants.USER_BEHAVIOR_ANALYZER_TOTAL_SCORE_SINK_NAME)

    //kafka Source的并行度
    val kafkaSourceParallelism = confProperties.getIntValue(Constants
      .FLINK_USER_BEHAVIOR_ANALYZER_CONFIG, Constants
      .USER_BEHAVIOR_ANALYZER_KAFKA_SOURCE_PARALLELISM)
    //对数据处理的并行度
    val dealParallelism = confProperties.getIntValue(Constants
      .FLINK_USER_BEHAVIOR_ANALYZER_CONFIG, Constants.USER_BEHAVIOR_ANALYZER_DEAL_PARALLELISM)
    //写入mysql的并行度
    val sqlSinkParallelism = confProperties.getIntValue(Constants
      .FLINK_USER_BEHAVIOR_ANALYZER_CONFIG, Constants.USER_BEHAVIOR_ANALYZER_SQL_SINK_PARALLELISM)
    //用户行为分析 更新总分并行度
    val totalScoreParallelism = confProperties.getIntValue(Constants.FLINK_USER_BEHAVIOR_ANALYZER_CONFIG,
      Constants.USER_BEHAVIOR_ANALYZER_TOTAL_SCORE_SINK_PARALLELISM)
    //用户行为分析 phoenix sink 并行度
    val phoenixSinkParallelism = confProperties.getIntValue(Constants.FLINK_USER_BEHAVIOR_ANALYZER_CONFIG,
      Constants.USER_BEHAVIOR_ANALYZER_PHOENIX_SINK_PARALLELISM)

    //kafka的服务地址
    val brokerList = confProperties.getValue(Constants.FLINK_COMMON_CONFIG, Constants
      .KAFKA_BOOTSTRAP_SERVERS)
    //flink消费的group.id
    val groupId = confProperties.getValue(Constants.FLINK_USER_BEHAVIOR_ANALYZER_CONFIG,
      Constants.USER_BEHAVIOR_ANALYZER_GROUP_ID)
    //kafka的topic
    val topic = confProperties.getValue(Constants.OPERATION_FLINK_TO_DRUID_CONFIG, Constants
      .OPERATION_TOPIC)


    //flink全局变量
    val parameters: Configuration = new Configuration()
    //文件类型配置
    parameters.setString(Constants.FILE_SYSTEM_TYPE, confProperties.getValue(Constants
      .FLINK_COMMON_CONFIG, Constants.FILE_SYSTEM_TYPE))
    //日期配置文件的路径
    parameters.setString(Constants.DATE_CONFIG_PATH, confProperties.getValue(Constants
      .FLINK_COMMON_CONFIG, Constants.DATE_CONFIG_PATH))
    //c3p0连接池配置文件路径
    parameters.setString(Constants.c3p0_CONFIG_PATH, confProperties.getValue(Constants
      .FLINK_COMMON_CONFIG, Constants.c3p0_CONFIG_PATH))
    //redis连接池配置文件路径
    parameters.setString(Constants.REDIS_CONFIG_PATH, confProperties.getValue(Constants.
      FLINK_COMMON_CONFIG, Constants.REDIS_CONFIG_PATH))

    //check pointing的间隔
    val checkpointInterval = confProperties.getLongValue(Constants
      .FLINK_USER_BEHAVIOR_ANALYZER_CONFIG, Constants.USER_BEHAVIOR_ANALYZER_CHECKPOINT_INTERVAL)

    //ip-地点关联文件路径
    parameters.setString(Constants
      .OPERATION_PLACE_PATH, confProperties.getValue(Constants.OPERATION_FLINK_TO_DRUID_CONFIG,
      Constants
        .OPERATION_PLACE_PATH))
    //host-系统名关联文件路径
    parameters.setString(Constants
      .OPERATION_SYSTEM_PATH, confProperties.getValue(Constants.OPERATION_FLINK_TO_DRUID_CONFIG,
      Constants
        .OPERATION_SYSTEM_PATH))
    //用户名-常用登录地关联文件路径
    parameters.setString(Constants
      .OPERATION_USEDPLACE_PATH, confProperties.getValue(Constants.OPERATION_FLINK_TO_DRUID_CONFIG,
      Constants.OPERATION_USEDPLACE_PATH))

    //用户行为分析判断是否长时间未访问的时间阈值
    parameters.setLong(Constants.
      USER_BEHAVIOR_ANALYZER_NOT_LOGIN_FOR_A_LONG_TIME_WARN_TIME_THRESHOLD,
      confProperties.getLongValue(Constants.FLINK_USER_BEHAVIOR_ANALYZER_CONFIG,
        Constants.USER_BEHAVIOR_ANALYZER_NOT_LOGIN_FOR_A_LONG_TIME_WARN_TIME_THRESHOLD))
    //用户行为分析判断是否在非常用时间访问的时间概率阈值
    parameters.setDouble(Constants.
      USER_BEHAVIOR_ANALYZER_NOT_USED_TIME_LOGIN_WARN_PROBABILITY_THRESHOLD,
      confProperties.getFloatValue(Constants.FLINK_USER_BEHAVIOR_ANALYZER_CONFIG,
        Constants.USER_BEHAVIOR_ANALYZER_NOT_USED_TIME_LOGIN_WARN_PROBABILITY_THRESHOLD))
    //用户行为分析判断是否是机器人访问的源ip文件路径
    parameters.setString(Constants.USER_BEHAVIOR_ANALYZER_ROBOT_WARN_SOURCE_IP_PATH,
      confProperties.getValue(Constants.FLINK_USER_BEHAVIOR_ANALYZER_CONFIG,
        Constants.USER_BEHAVIOR_ANALYZER_ROBOT_WARN_SOURCE_IP_PATH))
    //用户行为分析判断是否是异地登录时，人力编码和登录地的对应关系
    parameters.setString(Constants.USER_BEHAVIOR_ANALYZER_REMOTE_LOGIN_WARN_PLACE_PATH,
      confProperties.getValue(Constants.FLINK_USER_BEHAVIOR_ANALYZER_CONFIG,
        Constants.USER_BEHAVIOR_ANALYZER_REMOTE_LOGIN_WARN_PLACE_PATH))
    //用户行为分析判断是否频繁切换登录地时登录地阈值
    parameters.setInteger(Constants.
      USER_BEHAVIOR_ANALYZER_FREQUENTLY_SWITCH_LOGIN_LOCATIONS_WARN_PLACE_THRESHOLD,
      confProperties.getIntValue(Constants.FLINK_USER_BEHAVIOR_ANALYZER_CONFIG,
        Constants.USER_BEHAVIOR_ANALYZER_FREQUENTLY_SWITCH_LOGIN_LOCATIONS_WARN_PLACE_THRESHOLD))
    //用户行为分析判断是否频繁切换登录地时保存登录地的时间范围
    parameters.setLong(Constants.
      USER_BEHAVIOR_ANALYZER_FREQUENTLY_SWITCH_LOGIN_LOCATIONS_WARN_TIME_RANGE,
      confProperties.getLongValue(Constants.FLINK_USER_BEHAVIOR_ANALYZER_CONFIG,
        Constants.USER_BEHAVIOR_ANALYZER_FREQUENTLY_SWITCH_LOGIN_LOCATIONS_WARN_TIME_RANGE))
    //用户行为分析计算http状态码时参考的数据范围
    parameters.setLong(Constants.USER_BEHAVIOR_ANALYZER_HTTP_STATUS_REFERENCE_TIME_RANGE,
      confProperties.getLongValue(Constants.FLINK_USER_BEHAVIOR_ANALYZER_CONFIG,
        Constants.USER_BEHAVIOR_ANALYZER_HTTP_STATUS_REFERENCE_TIME_RANGE))
    //用户行为分析session时间
    parameters.setLong(Constants.USER_BEHAVIOR_ANALYZER_SESSION_TIME,
      confProperties.getLongValue(Constants.FLINK_USER_BEHAVIOR_ANALYZER_CONFIG,
        Constants.USER_BEHAVIOR_ANALYZER_SESSION_TIME))
    //用户行为分析phoenix driver class
    parameters.setString(Constants.USER_BEHAVIOR_ANALYZER_PHOENIX_DRIVER_CLASS,
      confProperties.getValue(Constants.FLINK_USER_BEHAVIOR_ANALYZER_CONFIG,
        Constants.USER_BEHAVIOR_ANALYZER_PHOENIX_DRIVER_CLASS))
    //用户行为分析插入hbase sql
    parameters.setString(Constants.USER_BEHAVIOR_ANALYZER_UPSERT_HBASE_SQL,
      confProperties.getValue(Constants.FLINK_USER_BEHAVIOR_ANALYZER_CONFIG,
        Constants.USER_BEHAVIOR_ANALYZER_UPSERT_HBASE_SQL))
    //用户行为分析phoenix url
    parameters.setString(Constants.USER_BEHAVIOR_ANALYZER_PHOENIX_URL,
      confProperties.getValue(Constants.FLINK_USER_BEHAVIOR_ANALYZER_CONFIG,
        Constants.USER_BEHAVIOR_ANALYZER_PHOENIX_URL))
    //用户行为分析参数分类服务host
    parameters.setString(Constants.USER_BEHAVIOR_ANALYZER_PARAM_CLASSIFICATION_SERVER_HOST,
      confProperties.getValue(Constants.FLINK_USER_BEHAVIOR_ANALYZER_CONFIG,
        Constants.USER_BEHAVIOR_ANALYZER_PARAM_CLASSIFICATION_SERVER_HOST))
    //用户行为分析参数分类服务port
    parameters.setInteger(Constants.USER_BEHAVIOR_ANALYZER_PARAM_CLASSIFICATION_SERVER_PORT,
      confProperties.getIntValue(Constants.FLINK_USER_BEHAVIOR_ANALYZER_CONFIG,
        Constants.USER_BEHAVIOR_ANALYZER_PARAM_CLASSIFICATION_SERVER_PORT))
    //用户行为分析参数分类服务netty连接池大小
    parameters.setInteger(Constants.USER_BEHAVIOR_ANALYZER_PARAM_CLASSIFICATION_NETTY_POOL_SIZE,
      confProperties.getIntValue(Constants.FLINK_USER_BEHAVIOR_ANALYZER_CONFIG,
        Constants.USER_BEHAVIOR_ANALYZER_PARAM_CLASSIFICATION_NETTY_POOL_SIZE))
    //用户行为分析参数分类服务netty连接timeout
    parameters.setString(Constants.USER_BEHAVIOR_ANALYZER_PARAM_CLASSIFICATION_NETTY_POOL_TIMEOUT,
      confProperties.getValue(Constants.FLINK_USER_BEHAVIOR_ANALYZER_CONFIG,
        Constants.USER_BEHAVIOR_ANALYZER_PARAM_CLASSIFICATION_NETTY_POOL_TIMEOUT))
    //用户行为分析忽略的后缀名
    parameters.setString(Constants.USER_BEHAVIOR_ANALYZER_IGNORE_SUFFIX,
      confProperties.getValue(Constants.FLINK_USER_BEHAVIOR_ANALYZER_CONFIG,
        Constants.USER_BEHAVIOR_ANALYZER_IGNORE_SUFFIX))
    //用户行为分析忽略的源IP
    parameters.setString(Constants.USER_BEHAVIOR_ANALYZER_IGNORE_SOURCE_IP,
      confProperties.getValue(Constants.FLINK_USER_BEHAVIOR_ANALYZER_CONFIG,
        Constants.USER_BEHAVIOR_ANALYZER_IGNORE_SOURCE_IP))
    //用户行为分析关注的用户列表文件路径
    parameters.setString(Constants.USER_BEHAVIOR_ANALYZER_FOLLOW_USERNAME_PATH,
      confProperties.getValue(Constants.FLINK_USER_BEHAVIOR_ANALYZER_CONFIG,
        Constants.USER_BEHAVIOR_ANALYZER_FOLLOW_USERNAME_PATH))
    //用户行为分析总分在redis的key
    parameters.setString(Constants.USER_BEHAVIOR_ANALYZER_TOTAL_SCORE_REDIS_KEY_FORMAT,
      confProperties.getValue(Constants.FLINK_USER_BEHAVIOR_ANALYZER_CONFIG,
        Constants.USER_BEHAVIOR_ANALYZER_TOTAL_SCORE_REDIS_KEY_FORMAT))
    //用户行为分析总次数在redis的key
    parameters.setString(Constants.USER_BEHAVIOR_ANALYZER_TOTAL_COUNT_REDIS_KEY_FORMAT,
      confProperties.getValue(Constants.FLINK_USER_BEHAVIOR_ANALYZER_CONFIG,
        Constants.USER_BEHAVIOR_ANALYZER_TOTAL_COUNT_REDIS_KEY_FORMAT))
    //用户行为分析时间概率在redis的key
    parameters.setString(Constants.USER_BEHAVIOR_ANALYZER_TIME_PROBABILITY_REDIS_KEY_FORMAT,
      confProperties.getValue(Constants.FLINK_USER_BEHAVIOR_ANALYZER_CONFIG,
        Constants.USER_BEHAVIOR_ANALYZER_TIME_PROBABILITY_REDIS_KEY_FORMAT))
    //用户行为分析系统时间概率在redis的key
    parameters.setString(Constants.USER_BEHAVIOR_ANALYZER_SYSTEM_TIME_PROBABILITY_REDIS_KEY_FORMAT,
      confProperties.getValue(Constants.FLINK_USER_BEHAVIOR_ANALYZER_CONFIG,
        Constants.USER_BEHAVIOR_ANALYZER_SYSTEM_TIME_PROBABILITY_REDIS_KEY_FORMAT))
    //用户行为分析操作系统时间概率在redis的key
    parameters.setString(Constants.
      USER_BEHAVIOR_ANALYZER_OPERATION_SYSTEM_TIME_PROBABILITY_REDIS_KEY_FORMAT,
      confProperties.getValue(Constants.FLINK_USER_BEHAVIOR_ANALYZER_CONFIG,
        Constants.USER_BEHAVIOR_ANALYZER_OPERATION_SYSTEM_TIME_PROBABILITY_REDIS_KEY_FORMAT))
    //用户行为分析浏览器时间概率在redis的key
    parameters.setString(Constants.USER_BEHAVIOR_ANALYZER_BROWSER_TIME_PROBABILITY_REDIS_KEY_FORMAT,
      confProperties.getValue(Constants.FLINK_USER_BEHAVIOR_ANALYZER_CONFIG,
        Constants.USER_BEHAVIOR_ANALYZER_BROWSER_TIME_PROBABILITY_REDIS_KEY_FORMAT))
    //用户行为分析源IP时间概率在redis的key
    parameters.setString(Constants.
      USER_BEHAVIOR_ANALYZER_SOURCE_IP_TIME_PROBABILITY_REDIS_KEY_FORMAT,
      confProperties.getValue(Constants.FLINK_USER_BEHAVIOR_ANALYZER_CONFIG,
        Constants.USER_BEHAVIOR_ANALYZER_SOURCE_IP_TIME_PROBABILITY_REDIS_KEY_FORMAT))
    //用户行为分析源IP概率在redis的key
    parameters.setString(Constants.USER_BEHAVIOR_ANALYZER_SOURCE_IP_PROBABILITY_REDIS_KEY_FORMAT,
      confProperties.getValue(Constants.FLINK_USER_BEHAVIOR_ANALYZER_CONFIG,
        Constants.USER_BEHAVIOR_ANALYZER_SOURCE_IP_PROBABILITY_REDIS_KEY_FORMAT))
    //用户行为分析单位时间内访问的系统个数在redis的key
    parameters.setString(Constants.USER_BEHAVIOR_ANALYZER_SYSTEM_COUNT_REDIS_KEY_FORMAT,
      confProperties.getValue(Constants.FLINK_USER_BEHAVIOR_ANALYZER_CONFIG,
        Constants.USER_BEHAVIOR_ANALYZER_SYSTEM_COUNT_REDIS_KEY_FORMAT))
    //用户行为分析最近一段时间内的HTTP 状态码在redis的key
    parameters.setString(Constants.USER_BEHAVIOR_ANALYZER_HTTP_STATUS_REDIS_KEY_FORMAT,
      confProperties.getValue(Constants.FLINK_USER_BEHAVIOR_ANALYZER_CONFIG,
        Constants.USER_BEHAVIOR_ANALYZER_HTTP_STATUS_REDIS_KEY_FORMAT))
    //用户行为分析URL参数在redis的key
    parameters.setString(Constants.USER_BEHAVIOR_ANALYZER_URL_PARAM_REDIS_KEY_FORMAT,
      confProperties.getValue(Constants.FLINK_USER_BEHAVIOR_ANALYZER_CONFIG,
        Constants.USER_BEHAVIOR_ANALYZER_URL_PARAM_REDIS_KEY_FORMAT))
    //用户行为分析最后一次访问信息在redis的key
    parameters.setString(Constants.USER_BEHAVIOR_ANALYZER_LAST_VISIT_INFORMATION_REDIS_KEY_FORMAT,
      confProperties.getValue(Constants.FLINK_USER_BEHAVIOR_ANALYZER_CONFIG,
        Constants.USER_BEHAVIOR_ANALYZER_LAST_VISIT_INFORMATION_REDIS_KEY_FORMAT))
    //用户行为分析最后一次访问信息在redis的key 最后一次访问时间的field
    parameters.setString(Constants.
      USER_BEHAVIOR_ANALYZER_LAST_VISIT_INFORMATION_REDIS_FIELD_FORMAT_LAST_VISIT_TIMESTAMP,
      confProperties.getValue(Constants.FLINK_USER_BEHAVIOR_ANALYZER_CONFIG,
        Constants.USER_BEHAVIOR_ANALYZER_LAST_VISIT_INFORMATION_REDIS_FIELD_FORMAT_LAST_VISIT_TIMESTAMP))
    //用户行为分析最后一次访问信息在redis的key 最后一次访问地点的field
    parameters.setString(Constants.
      USER_BEHAVIOR_ANALYZER_LAST_VISIT_INFORMATION_REDIS_FIELD_FORMAT_LAST_VISIT_PLACE,
      confProperties.getValue(Constants.FLINK_USER_BEHAVIOR_ANALYZER_CONFIG,
        Constants.USER_BEHAVIOR_ANALYZER_LAST_VISIT_INFORMATION_REDIS_FIELD_FORMAT_LAST_VISIT_PLACE))
    //用户行为分析最后一次访问信息在redis的key 最后一次访问系统的field
    parameters.setString(Constants.
      USER_BEHAVIOR_ANALYZER_LAST_VISIT_INFORMATION_REDIS_FIELD_FORMAT_LAST_VISIT_SYSTEM,
      confProperties.getValue(Constants.FLINK_USER_BEHAVIOR_ANALYZER_CONFIG,
        Constants.USER_BEHAVIOR_ANALYZER_LAST_VISIT_INFORMATION_REDIS_FIELD_FORMAT_LAST_VISIT_SYSTEM))
    //用户行为分析最后一次访问信息在redis的key 评分的field
    parameters.setString(Constants.
      USER_BEHAVIOR_ANALYZER_LAST_VISIT_INFORMATION_REDIS_FIELD_FORMAT_SCORE,
      confProperties.getValue(Constants.FLINK_USER_BEHAVIOR_ANALYZER_CONFIG,
        Constants.USER_BEHAVIOR_ANALYZER_LAST_VISIT_INFORMATION_REDIS_FIELD_FORMAT_SCORE))
    //用户行为分析最后一次访问信息在redis的key 访问次数的field
    parameters.setString(Constants.
      USER_BEHAVIOR_ANALYZER_LAST_VISIT_INFORMATION_REDIS_FIELD_FORMAT_COUNT,
      confProperties.getValue(Constants.FLINK_USER_BEHAVIOR_ANALYZER_CONFIG,
        Constants.USER_BEHAVIOR_ANALYZER_LAST_VISIT_INFORMATION_REDIS_FIELD_FORMAT_COUNT))
    //用户行为分析最后一次访问信息在redis的key 地点的field
    parameters.setString(Constants.
      USER_BEHAVIOR_ANALYZER_LAST_VISIT_INFORMATION_REDIS_FIELD_FORMAT_PLACE,
      confProperties.getValue(Constants.FLINK_USER_BEHAVIOR_ANALYZER_CONFIG,
        Constants.USER_BEHAVIOR_ANALYZER_LAST_VISIT_INFORMATION_REDIS_FIELD_FORMAT_PLACE))
    //用户行为分析最后一次访问信息在redis的key 用户状态的field
    parameters.setString(Constants.
      USER_BEHAVIOR_ANALYZER_LAST_VISIT_INFORMATION_REDIS_FIELD_FORMAT_STATUS,
      confProperties.getValue(Constants.FLINK_USER_BEHAVIOR_ANALYZER_CONFIG,
        Constants.USER_BEHAVIOR_ANALYZER_LAST_VISIT_INFORMATION_REDIS_FIELD_FORMAT_STATUS))
    //用户行为分析最后一次访问信息在redis的key 用户描述的field
    parameters.setString(Constants.
      USER_BEHAVIOR_ANALYZER_LAST_VISIT_INFORMATION_REDIS_FIELD_FORMAT_PERSONAS,
      confProperties.getValue(Constants.FLINK_USER_BEHAVIOR_ANALYZER_CONFIG,
        Constants.USER_BEHAVIOR_ANALYZER_LAST_VISIT_INFORMATION_REDIS_FIELD_FORMAT_PERSONAS))

    //用户行为分析double保留位数
    parameters.setInteger(Constants.USER_BEHAVIOR_ANALYZER_DOUBLE_AFTER_POINT_LENGTH,
      confProperties.getIntValue(Constants.FLINK_USER_BEHAVIOR_ANALYZER_CONFIG,
        Constants.USER_BEHAVIOR_ANALYZER_DOUBLE_AFTER_POINT_LENGTH))

    //xss检测库，在hdfs的路径
    parameters.setString(Constants.
      XSS_INJECTION_HDFS_RULE_PATH, confProperties.getValue(Constants.FLINK_XSS_INJECTION_CONFIG,
      Constants.XSS_INJECTION_HDFS_RULE_PATH))
    //xss检测库，下载到本地的路径
    parameters.setString(Constants.
      XSS_INJECTION_RULE_PATH, confProperties.getValue(Constants.FLINK_XSS_INJECTION_CONFIG,
      Constants.XSS_INJECTION_RULE_PATH))
    //sql注入检测规则的路径
    parameters.setString(Constants.
      SQL_INJECTION_RULE_PATH, confProperties.getValue(Constants.FLINK_SQL_INJECTION_CONFIG,
      Constants.SQL_INJECTION_RULE_PATH))

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
    val consumer = new FlinkKafkaConsumer[String](topic, new SimpleStringSchema, props)
      .setStartFromGroupOffsets()
    // 获取kafka数据
    val dStream = env.addSource(consumer).setParallelism(kafkaSourceParallelism)
      .uid(kafkaSourceName).name(kafkaSourceName)
    //val path = "/Users/chenwei/Downloads/NeedHQ-NEW.txt"
    //val path = "file:///project/flink/data/NeedHQ-NEW.txt"
    //val dStream = env.readTextFile(path, "utf-8").setParallelism(1)

    //val lines = Source.fromFile(path, "utf-8").getLines().take(1000)
    //val dStream = env.fromCollection(lines.toList)


    val userBehaviorAnalyzerStream = dStream
      .filter(new UserBehaviorAnalyzerFilter).setParallelism(dealParallelism)
      .keyBy(_.split("\\|", -1)(0))
      .process(new UserBehaviorAnalyzerProcess()).setParallelism(dealParallelism)
    //.process(new UserBehaviorAnalyzerProcess2()).setParallelism(1)

    userBehaviorAnalyzerStream
      .filter(_._1.isDefined).setParallelism(phoenixSinkParallelism)
      .map(_._1.head).setParallelism(phoenixSinkParallelism)
      .addSink(new UserBehaviorAnalyzerPhoenixSink).setParallelism(phoenixSinkParallelism)
      .uid(phoenixSinkName).name(phoenixSinkName)

    userBehaviorAnalyzerStream
      .filter(_._2.isDefined).setParallelism(sqlSinkParallelism)
      .map(_._2.head).setParallelism(sqlSinkParallelism)
      .addSink(new MySQLSink).setParallelism(sqlSinkParallelism).uid(sqlSinkName).name(sqlSinkName)

    userBehaviorAnalyzerStream
      .filter(_._3.isDefined).setParallelism(totalScoreParallelism)
      .map(_._3.head).setParallelism(totalScoreParallelism)
      .addSink(new UserBehaviorAnalyzerTotalScore).setParallelism(totalScoreParallelism)
      .uid(totalScoreSinkName).name(totalScoreSinkName)


    env.execute(jobName)


  }

}