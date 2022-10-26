/*
 * @project mss
 * @company Fujian Fujitsu Communication Software Co., Ltd.
 * @author chenwei
 * @date 2019-12-14 22:44:38
 * @version v1.0
 * @update [no] [date YYYY-MM-DD] [name] [description]
 */
package cn.ffcs.is.mss.analyzer.flink.behavior.user
import java.io.{BufferedReader, InputStreamReader}
import java.net.URI
import java.text.SimpleDateFormat
import java.util.Properties
import cn.ffcs.is.mss.analyzer.druid.model.scala.OperationModel
import cn.ffcs.is.mss.analyzer.flink.behavior.user.filter.UserBehaviorAnalyzerFilter
import cn.ffcs.is.mss.analyzer.flink.behavior.user.process.CalculationProbabilityUtil
import cn.ffcs.is.mss.analyzer.ml.kde.{Kernel, KernelDensity}
import cn.ffcs.is.mss.analyzer.ml.tree.server.NettyChannelPool
import cn.ffcs.is.mss.analyzer.utils.{Constants, IniProperties, JedisUtil, TimeUtil}
import org.apache.flink.api.common.functions.RichMapFunction
import org.apache.flink.api.common.io.FilePathFilter
import org.apache.flink.api.java.io.TextInputFormat
import org.apache.flink.api.scala._
import org.apache.flink.configuration.Configuration
import org.apache.flink.core.fs.{FileSystem, Path}
import redis.clients.jedis.{Jedis, JedisPool, Pipeline}

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

/**
 *
 * @author chenwei
 * @date 2019-12-14 22:44:38
 * @title UserBehaviorAnalyzerBatch  批任务建立用户画像
 * @update [no] [date YYYY-MM-DD] [name] [description]
 */
object UserBehaviorAnalyzerBatch {

  def main(args: Array[String]): Unit = {

    //val args0 = "./src/main/resources/flink.ini"
    //val args0 = "/project/flink/conf/flink.ini"
    //根据传入的参数解析配置文件
    //val confProperties = new IniProperties(args0)
    val confProperties = new IniProperties(args(0))

    val todayStartTimeStamp = TimeUtil.getDayStartTime(System.currentTimeMillis())
    val lastDealTimestamp = 0L

    val fileSystem = confProperties.getValue(Constants.FLINK_COMMON_CONFIG, Constants
      .FILE_SYSTEM_TYPE)
    //原始数据的路径
    val inputPath = confProperties.getValue(Constants
      .FLINK_USER_BEHAVIOR_ANALYZER_CONFIG,
      Constants.USER_BEHAVIOR_ANALYZER_ALL_DATA_PATH)

    //中间结果的路径
    val outputPath = confProperties.getValue(Constants
      .FLINK_USER_BEHAVIOR_ANALYZER_CONFIG,
      Constants.USER_BEHAVIOR_ANALYZER_TMP_DATA_PATH)
    //该任务的名字
    val jobName = confProperties.getValue(Constants.FLINK_USER_BEHAVIOR_ANALYZER_CONFIG,
      Constants.USER_BEHAVIOR_ANALYZER_BATCH_JOB_NAME)


    //用户行为分析批处理读取原始文件并行度
    val readRawParallelism = confProperties.getIntValue(Constants
      .FLINK_USER_BEHAVIOR_ANALYZER_CONFIG,
      Constants.USER_BEHAVIOR_ANALYZER_BATCH_READ_RAW_PARALLELISM)
    //用户行为分析批处理读取临时文件并行度
    val readTmpParallelism = confProperties.getIntValue(Constants
      .FLINK_USER_BEHAVIOR_ANALYZER_CONFIG,
      Constants.USER_BEHAVIOR_ANALYZER_BATCH_READ_TMP_PARALLELISM)
    //用户行为分析批处理写入临时文件并行度
    val writeTmpParallelism = confProperties.getIntValue(Constants
      .FLINK_USER_BEHAVIOR_ANALYZER_CONFIG,
      Constants.USER_BEHAVIOR_ANALYZER_BATCH_WRITE_TMP_PARALLELISM)
    //用户行为分析批处理处理并行度
    val dealParallelism = confProperties.getIntValue(Constants.FLINK_USER_BEHAVIOR_ANALYZER_CONFIG,
      Constants.USER_BEHAVIOR_ANALYZER_BATCH_DEAL_PARALLELISM)


    //数据范围
    val timeRange = confProperties.getLongValue(Constants.FLINK_USER_BEHAVIOR_ANALYZER_CONFIG,
      Constants.USER_BEHAVIOR_ANALYZER_DATA_TIME_RANGE)

    val dateFormat = confProperties.getValue(Constants.FLINK_USER_BEHAVIOR_ANALYZER_CONFIG,
      Constants.USER_BEHAVIOR_ANALYZER_ALL_DATA_DATE_FORMAT)
    val env = ExecutionEnvironment.getExecutionEnvironment


    val parameters: Configuration = new Configuration()
    //文件类型配置
    parameters.setString(Constants.FILE_SYSTEM_TYPE, confProperties.getValue(Constants
      .FLINK_COMMON_CONFIG, Constants.FILE_SYSTEM_TYPE))
    //redis连接池配置文件路径
    parameters.setString(Constants.REDIS_CONFIG_PATH, confProperties.getValue(Constants.
      FLINK_COMMON_CONFIG, Constants.REDIS_CONFIG_PATH))
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
    parameters.setString(Constants.
      USER_BEHAVIOR_ANALYZER_NOT_LOGIN_FOR_A_LONG_TIME_WARN_TIME_THRESHOLD,
      confProperties.getValue(Constants.FLINK_USER_BEHAVIOR_ANALYZER_CONFIG,
        Constants.USER_BEHAVIOR_ANALYZER_NOT_LOGIN_FOR_A_LONG_TIME_WARN_TIME_THRESHOLD))
    //用户行为分析判断是否在非常用时间访问的时间概率阈值
    parameters.setString(Constants.
      USER_BEHAVIOR_ANALYZER_NOT_USED_TIME_LOGIN_WARN_PROBABILITY_THRESHOLD,
      confProperties.getValue(Constants.FLINK_USER_BEHAVIOR_ANALYZER_CONFIG,
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
    parameters.setString(Constants.
      USER_BEHAVIOR_ANALYZER_FREQUENTLY_SWITCH_LOGIN_LOCATIONS_WARN_PLACE_THRESHOLD,
      confProperties.getValue(Constants.FLINK_USER_BEHAVIOR_ANALYZER_CONFIG,
        Constants.USER_BEHAVIOR_ANALYZER_FREQUENTLY_SWITCH_LOGIN_LOCATIONS_WARN_PLACE_THRESHOLD))
    //用户行为分析判断是否频繁切换登录地时保存登录地的时间范围
    parameters.setString(Constants.
      USER_BEHAVIOR_ANALYZER_FREQUENTLY_SWITCH_LOGIN_LOCATIONS_WARN_TIME_RANGE,
      confProperties.getValue(Constants.FLINK_USER_BEHAVIOR_ANALYZER_CONFIG,
        Constants.USER_BEHAVIOR_ANALYZER_FREQUENTLY_SWITCH_LOGIN_LOCATIONS_WARN_TIME_RANGE))
    //用户行为分析计算http状态码时参考的数据范围
    parameters.setString(Constants.USER_BEHAVIOR_ANALYZER_HTTP_STATUS_REFERENCE_TIME_RANGE,
      confProperties.getValue(Constants.FLINK_USER_BEHAVIOR_ANALYZER_CONFIG,
        Constants.USER_BEHAVIOR_ANALYZER_HTTP_STATUS_REFERENCE_TIME_RANGE))
    //用户行为分析session时间
    parameters.setString(Constants.USER_BEHAVIOR_ANALYZER_SESSION_TIME,
      confProperties.getValue(Constants.FLINK_USER_BEHAVIOR_ANALYZER_CONFIG,
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
    parameters.setString(Constants.USER_BEHAVIOR_ANALYZER_PARAM_CLASSIFICATION_SERVER_PORT,
      confProperties.getValue(Constants.FLINK_USER_BEHAVIOR_ANALYZER_CONFIG,
        Constants.USER_BEHAVIOR_ANALYZER_PARAM_CLASSIFICATION_SERVER_PORT))
    //用户行为分析参数分类服务netty连接池大小
    parameters.setString(Constants.USER_BEHAVIOR_ANALYZER_PARAM_CLASSIFICATION_NETTY_POOL_SIZE,
      confProperties.getValue(Constants.FLINK_USER_BEHAVIOR_ANALYZER_CONFIG,
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

    //用户行为分析源ip使用次数在redis中的key
    parameters.setString(Constants.USER_BEHAVIOR_ANALYZER_SOURCE_IP_REDIS_KEY_FORMAT,
      confProperties.getValue(Constants.FLINK_USER_BEHAVIOR_ANALYZER_CONFIG,
        Constants.USER_BEHAVIOR_ANALYZER_SOURCE_IP_REDIS_KEY_FORMAT));
    //用户行为分析访问系统次数在redis中的key
    parameters.setString(Constants.USER_BEHAVIOR_ANALYZER_SYSTEM_REDIS_KEY_FORMAT,
      confProperties.getValue(Constants.FLINK_USER_BEHAVIOR_ANALYZER_CONFIG,
        Constants.USER_BEHAVIOR_ANALYZER_SYSTEM_REDIS_KEY_FORMAT));
    //用户行为分析源操作系统使用次数在redis中的key
    parameters.setString(Constants.USER_BEHAVIOR_ANALYZER_OPERATION_SYSTEM_REDIS_KEY_FORMAT,
      confProperties.getValue(Constants.FLINK_USER_BEHAVIOR_ANALYZER_CONFIG,
        Constants.USER_BEHAVIOR_ANALYZER_OPERATION_SYSTEM_REDIS_KEY_FORMAT));
    //用户行为分析浏览器使用次数在redis中的key
    parameters.setString(Constants.USER_BEHAVIOR_ANALYZER_BROWSER_REDIS_KEY_FORMAT,
      confProperties.getValue(Constants.FLINK_USER_BEHAVIOR_ANALYZER_CONFIG,
        Constants.USER_BEHAVIOR_ANALYZER_BROWSER_REDIS_KEY_FORMAT));

    //用户行为分析double保留位数
    parameters.setInteger(Constants.USER_BEHAVIOR_ANALYZER_DOUBLE_AFTER_POINT_LENGTH,
      confProperties.getIntValue(Constants.FLINK_USER_BEHAVIOR_ANALYZER_CONFIG,
        Constants.USER_BEHAVIOR_ANALYZER_DOUBLE_AFTER_POINT_LENGTH))

    //用户行为分析计算时间kde时的bandwidth
    parameters.setString(Constants.USER_BEHAVIOR_ANALYZER_TIME_KDE_BANDWIDTH,
      confProperties.getValue(Constants.FLINK_USER_BEHAVIOR_ANALYZER_CONFIG,
        Constants.USER_BEHAVIOR_ANALYZER_TIME_KDE_BANDWIDTH))
    //用户行为分析计算系统-时间kde时的bandwidth
    parameters.setString(Constants.USER_BEHAVIOR_ANALYZER_SYSTEM_TIME_KDE_BANDWIDTH,
      confProperties.getValue(Constants.FLINK_USER_BEHAVIOR_ANALYZER_CONFIG,
        Constants.USER_BEHAVIOR_ANALYZER_SYSTEM_TIME_KDE_BANDWIDTH))
    //用户行为分析计算操作系统-时间kde时的bandwidth
    parameters.setString(Constants.USER_BEHAVIOR_ANALYZER_OPERATION_SYSTEM_TIME_KDE_BANDWIDTH,
      confProperties.getValue(Constants.FLINK_USER_BEHAVIOR_ANALYZER_CONFIG,
        Constants.USER_BEHAVIOR_ANALYZER_OPERATION_SYSTEM_TIME_KDE_BANDWIDTH))
    //用户行为分析计算浏览器-时间kde时的bandwidth
    parameters.setString(Constants.USER_BEHAVIOR_ANALYZER_BROWSER_TIME_KDE_BANDWIDTH,
      confProperties.getValue(Constants.FLINK_USER_BEHAVIOR_ANALYZER_CONFIG,
        Constants.USER_BEHAVIOR_ANALYZER_BROWSER_TIME_KDE_BANDWIDTH))
    //用户行为分析计算源IP-时间kde时的bandwidth
    parameters.setString(Constants.USER_BEHAVIOR_ANALYZER_SOURCE_IP_TIME_KDE_BANDWIDTH,
      confProperties.getValue(Constants.FLINK_USER_BEHAVIOR_ANALYZER_CONFIG,
        Constants.USER_BEHAVIOR_ANALYZER_SOURCE_IP_TIME_KDE_BANDWIDTH))
    //用户行为分析计算源IP kde时的bandwidth
    parameters.setString(Constants.USER_BEHAVIOR_ANALYZER_SOURCE_IP_KDE_BANDWIDTH,
      confProperties.getValue(Constants.FLINK_USER_BEHAVIOR_ANALYZER_CONFIG,
        Constants.USER_BEHAVIOR_ANALYZER_SOURCE_IP_KDE_BANDWIDTH))

    env.getConfig.setGlobalJobParameters(parameters)

    //读取历史数据
    val hdfsRawDataSet = env.readFile(getRawFilePathFilter(inputPath, todayStartTimeStamp,
      lastDealTimestamp, timeRange, dateFormat), inputPath).setParallelism(readRawParallelism)
      .filter(new UserBehaviorAnalyzerFilter).setParallelism(readRawParallelism)
      .map(new RawDataToUserBehaviorModelMap).setParallelism(readRawParallelism)
      .filter(_.isDefined).setParallelism(readRawParallelism)
      .map(_.head).setParallelism(readRawParallelism)

    //读取处理过的中间数据
    val hdfsTmpDataSet: DataSet[(String, Array[UserBehaviorModel])] = env.readFile(getTmpFilePathFilter(inputPath), inputPath)
      .setParallelism(readTmpParallelism)
      .map(UserBehaviorModel.tmpDataToUserBehaviorModel _).setParallelism(readTmpParallelism)
      .filter(_.isDefined).setParallelism(readTmpParallelism)
      .filter(todayStartTimeStamp - _.head.timeStamp <= timeRange)
      .setParallelism(readTmpParallelism)
      .map(t => {
        val userBehaviorModel = t.head
        (userBehaviorModel.username, Array(userBehaviorModel))
      }).setParallelism(readTmpParallelism)

    hdfsRawDataSet.union(hdfsTmpDataSet)
      .groupBy(_._1)
      .reduce((l1, l2) => (l1._1, l1._2 ++ l2._2)).setParallelism(dealParallelism)
      .map(new CalculationMap).setParallelism(dealParallelism)
      .flatMap(_.map(_.toString)).setParallelism(dealParallelism)
      .writeAsText(outputPath, FileSystem.WriteMode.OVERWRITE)
      .setParallelism(writeTmpParallelism)

    env.execute(jobName)


  }

  /**
   * 将原始数据加工为UserBehaviorModel
   */
  class RawDataToUserBehaviorModelMap extends RichMapFunction[String, Option[(String,
    Array[UserBehaviorModel])]] {
    override def open(parameters: Configuration): Unit = {
      val globConf = getRuntimeContext.getExecutionConfig.getGlobalJobParameters
        .asInstanceOf[Configuration]
      //ip-地点关联文件路径
      OperationModel.setPlaceMap(globConf.getString(Constants.OPERATION_PLACE_PATH, ""))
      //host-系统名关联文件路径
      OperationModel.setSystemMap(globConf.getString(Constants.OPERATION_SYSTEM_PATH, ""))
      OperationModel.setMajorMap(globConf.getString(Constants.OPERATION_SYSTEM_PATH, ""))
      //用户名-常用登录地关联文件路径
      OperationModel.setUsedPlacesMap(globConf.getString(Constants.OPERATION_USEDPLACE_PATH, ""))
    }

    override def map(value: String): Option[(String, Array[UserBehaviorModel])] = {
      val userBehaviorModelOption = UserBehaviorModel.rawDataToUserBehaviorModel(value)
      if (userBehaviorModelOption.isDefined) {
        val userBehaviorModel = userBehaviorModelOption.head

        Some(userBehaviorModel.username, Array(userBehaviorModel))
      } else {
        None
      }
    }
  }


  class CalculationMap extends RichMapFunction[(String, Array[UserBehaviorModel]),
    Array[UserBehaviorModel]] {

    //计算time KDE时bandwidth
    var TIME_BANDWIDTH = 90L
    //计算operation KDE时bandwidth
    var OPERATION_SYSTEM_TIME_BANDWIDTH = 90L
    //计算browser KDE时bandwidth
    var BROWSER_TIME_BANDWIDTH = 90L
    //计算system KDE时bandwidth
    var SYSTEM_TIME_BANDWIDTH = 90L
    //计算source ip 时间KDE时bandwidth
    var SOURCE_IP_TIME_BANDWIDTH = 90L
    //计算source ip KDE时bandwidth
    var SOURCE_IP_BANDWIDTH = 16L

    //用户行为分析session时间(1000*60*5)
    var SESSION_TIME = 300000
    //参数分类服务netty连接timeout
    var NETTY_TIMEOUT = 30000

    var nettyChannelPool: NettyChannelPool = _
    //参数分类服务netty连接timeout
    var JEDIS_POOL: JedisPool = _
    var JEDIS: Jedis = _
    var PIPELINED: Pipeline = _

    //用户行为分析时间概率在redis的key
    var TIME_PROBABILITY_REDIS_KEY_FORMAT = "%s:TIME_PROBABILITY"
    //用户行为分析系统时间概率在redis的key
    var SYSTEM_TIME_PROBABILITY_REDIS_KEY_FORMAT = "%s:TIME_PROBABILITY:%s"
    //用户行为分析操作系统时间概率在redis的key
    var OPERATION_SYSTEM_TIME_PROBABILITY_REDIS_KEY_FORMAT = "%s:TIME_PROBABILITY:%s"
    //用户行为分析浏览器时间概率在redis的key
    var BROWSER_TIME_PROBABILITY_REDIS_KEY_FORMAT = "%s:TIME_PROBABILITY:%s"
    //用户行为分析源IP时间概率在redis的key
    var SOURCE_IP_TIME_PROBABILITY_REDIS_KEY_FORMAT = "%s:TIME_PROBABILITY:%s"
    //用户行为分析源IP概率在redis的key
    var SOURCE_IP_PROBABILITY_REDIS_KEY_FORMAT = "%s:PROBABILITY"
    //用户行为分析单位时间内访问的系统个数在redis的key
    var SYSTEM_COUNT_REDIS_KEY_FORMAT = "%s:SYSTEM_COUNT:%s"
    //用户行为分析最近一段时间内的HTTP 状态码在redis的key
    var HTTP_STATUS_REDIS_KEY_FORMAT = "%s:HTTP_STATUS"
    //用户行为分析URL参数在redis的key
    var URL_PARAM_REDIS_KEY_FORMAT = "%s:URL_PARAM:%s:%s"
    //用户行为分析源ip使用次数在redis中的key
    var SOURCE_IP_REDIS_KEY_FORMAT = "%s:SOURCE_IP"
    //用户行为分析访问系统次数在redis中的key
    var SYSTEM_REDIS_KEY_FORMAT = "%s:SYSTEM"
    //用户行为分析源操作系统使用次数在redis中的key
    var OPERATION_SYSTEM_REDIS_KEY_FORMAT = "%s:OPERATION_SYSTEM"
    //用户行为分析浏览器使用次数在redis中的key
    var BROWSER_REDIS_KEY_FORMAT = "%s:BROWSER"
    //用户行为分析double保留位数
    var DOUBLE_AFTER_POINT_LENGTH = 4

    override def open(parameters: Configuration): Unit = {
      val globConf = getRuntimeContext.getExecutionConfig.getGlobalJobParameters
        .asInstanceOf[Configuration]
      //配置netty 连接池大小
      nettyChannelPool = new NettyChannelPool(globConf.getInteger(Constants
        .USER_BEHAVIOR_ANALYZER_PARAM_CLASSIFICATION_NETTY_POOL_SIZE, 20))
      //获取netty链接
      nettyChannelPool.connect(globConf.getString(Constants
        .USER_BEHAVIOR_ANALYZER_PARAM_CLASSIFICATION_SERVER_HOST, "localhost"),
        globConf.getInteger(Constants.USER_BEHAVIOR_ANALYZER_PARAM_CLASSIFICATION_SERVER_PORT,
          10000))
      NETTY_TIMEOUT = globConf.getInteger(Constants
        .USER_BEHAVIOR_ANALYZER_PARAM_CLASSIFICATION_NETTY_POOL_TIMEOUT, 30000)

      //文件系统类型
      val fileSystemType = globConf.getString(Constants.FILE_SYSTEM_TYPE, "file:///")
      //获取redis配置，并建立连接
      val redisConfigPath = globConf.getString(Constants.REDIS_CONFIG_PATH, "")
      val fs = org.apache.hadoop.fs.FileSystem.get(URI.create(fileSystemType), new org.apache
      .hadoop.conf.Configuration())
      val fsDataInputStream = fs.open(new org.apache.hadoop.fs.Path(redisConfigPath))
      val bufferedReader = new BufferedReader(new InputStreamReader(fsDataInputStream))
      val jedisProperties = new Properties()
      jedisProperties.load(bufferedReader)
      JEDIS_POOL = JedisUtil.getJedisPool(jedisProperties)
      JEDIS = JEDIS_POOL.getResource



      //用户行为分析时间概率在redis的key
      TIME_PROBABILITY_REDIS_KEY_FORMAT = globConf.getString(Constants
        .USER_BEHAVIOR_ANALYZER_TIME_PROBABILITY_REDIS_KEY_FORMAT, "%s:TIME_PROBABILITY")
      //用户行为分析系统时间概率在redis的key
      SYSTEM_TIME_PROBABILITY_REDIS_KEY_FORMAT = globConf.getString(Constants
        .USER_BEHAVIOR_ANALYZER_SYSTEM_TIME_PROBABILITY_REDIS_KEY_FORMAT, "%s:TIME_PROBABILITY:%s")
      //用户行为分析操作系统时间概率在redis的key
      OPERATION_SYSTEM_TIME_PROBABILITY_REDIS_KEY_FORMAT = globConf.getString(Constants
        .USER_BEHAVIOR_ANALYZER_OPERATION_SYSTEM_TIME_PROBABILITY_REDIS_KEY_FORMAT,
        "%s:TIME_PROBABILITY:%s")
      //用户行为分析浏览器时间概率在redis的key
      BROWSER_TIME_PROBABILITY_REDIS_KEY_FORMAT = globConf.getString(Constants
        .USER_BEHAVIOR_ANALYZER_BROWSER_TIME_PROBABILITY_REDIS_KEY_FORMAT, "%s:TIME_PROBABILITY:%s")
      //用户行为分析源IP时间概率在redis的key
      SOURCE_IP_TIME_PROBABILITY_REDIS_KEY_FORMAT = globConf.getString(Constants
        .USER_BEHAVIOR_ANALYZER_SOURCE_IP_TIME_PROBABILITY_REDIS_KEY_FORMAT,
        "%s:TIME_PROBABILITY:%s")
      //用户行为分析源IP概率在redis的key
      SOURCE_IP_PROBABILITY_REDIS_KEY_FORMAT = globConf.getString(Constants
        .USER_BEHAVIOR_ANALYZER_SOURCE_IP_PROBABILITY_REDIS_KEY_FORMAT, "%s:SOURCE_IP_PROBABILITY")
      //用户行为分析单位时间内访问的系统个数在redis的key
      SYSTEM_COUNT_REDIS_KEY_FORMAT = globConf.getString(Constants
        .USER_BEHAVIOR_ANALYZER_SYSTEM_COUNT_REDIS_KEY_FORMAT, "%s:SYSTEM_COUNT:%s")
      //用户行为分析最近一段时间内的HTTP 状态码在redis的key
      HTTP_STATUS_REDIS_KEY_FORMAT = globConf.getString(Constants
        .USER_BEHAVIOR_ANALYZER_HTTP_STATUS_REDIS_KEY_FORMAT, "%s:HTTP_STATUS")
      //用户行为分析URL参数在redis的key
      URL_PARAM_REDIS_KEY_FORMAT = globConf.getString(Constants
        .USER_BEHAVIOR_ANALYZER_URL_PARAM_REDIS_KEY_FORMAT, "%s:URL_PARAM:%s:%s")

      //用户行为分析源ip使用次数在redis中的key
      SOURCE_IP_REDIS_KEY_FORMAT = globConf.getString(Constants
        .USER_BEHAVIOR_ANALYZER_SOURCE_IP_REDIS_KEY_FORMAT, "%s:SOURCE_IP")
      //用户行为分析访问系统次数在redis中的key
      SYSTEM_REDIS_KEY_FORMAT = globConf.getString(Constants
        .USER_BEHAVIOR_ANALYZER_SYSTEM_REDIS_KEY_FORMAT, "%s:SYSTEM")
      //用户行为分析源操作系统使用次数在redis中的key
      OPERATION_SYSTEM_REDIS_KEY_FORMAT = globConf.getString(Constants
        .USER_BEHAVIOR_ANALYZER_OPERATION_SYSTEM_REDIS_KEY_FORMAT, "%s:OPERATION_SYSTEM")
      //用户行为分析浏览器使用次数在redis中的key
      BROWSER_REDIS_KEY_FORMAT = globConf.getString(Constants
        .USER_BEHAVIOR_ANALYZER_BROWSER_REDIS_KEY_FORMAT, "%s:BROWSER")

      //用户行为分析double保留位数
      DOUBLE_AFTER_POINT_LENGTH = globConf.getInteger(Constants.USER_BEHAVIOR_ANALYZER_DOUBLE_AFTER_POINT_LENGTH, 4)

      //计算time KDE时bandwidth
      TIME_BANDWIDTH = globConf.getLong(Constants.USER_BEHAVIOR_ANALYZER_TIME_KDE_BANDWIDTH, 90L)
      //计算operation KDE时bandwidth
      OPERATION_SYSTEM_TIME_BANDWIDTH = globConf.getLong(Constants
        .USER_BEHAVIOR_ANALYZER_OPERATION_SYSTEM_TIME_KDE_BANDWIDTH, 90L)
      //计算browser KDE时bandwidth
      BROWSER_TIME_BANDWIDTH = globConf.getLong(Constants
        .USER_BEHAVIOR_ANALYZER_BROWSER_TIME_KDE_BANDWIDTH, 90L)
      //计算system KDE时bandwidth
      SYSTEM_TIME_BANDWIDTH = globConf.getLong(Constants
        .USER_BEHAVIOR_ANALYZER_SYSTEM_TIME_KDE_BANDWIDTH, 90L)
      //计算source ip 时间KDE时bandwidth
      SOURCE_IP_TIME_BANDWIDTH = globConf.getLong(Constants
        .USER_BEHAVIOR_ANALYZER_SOURCE_IP_TIME_KDE_BANDWIDTH, 90L)
      //计算source ip KDE时bandwidth
      SOURCE_IP_BANDWIDTH = globConf.getLong(Constants
        .USER_BEHAVIOR_ANALYZER_SOURCE_IP_KDE_BANDWIDTH, 16L)

    }

    override def map(in: (String, Array[UserBehaviorModel])): Array[UserBehaviorModel] = {


      //用户名
      val username = in._1
      //所有出现过的时间戳
      val timeStampArray = mutable.ArrayBuffer[Long]()
      //<system, 所有出现过的时间戳>
      val systemTimeStampMap = mutable.Map[String, mutable.ArrayBuffer[Long]]()
      //<operationSystem, 所有出现过的时间戳>
      val operationSystemTimeStampMap = mutable.Map[String, mutable.ArrayBuffer[Long]]()
      //<browser, 所有出现过的时间戳>
      val browserTimeStampMap = mutable.Map[String, mutable.ArrayBuffer[Long]]()
      //<sourceIp, 所有出现过的时间戳>
      val sourceIpTimeStampMap = mutable.Map[String, mutable.ArrayBuffer[Long]]()
      //<sourceIp, 所有出现过的次数>
      val sourceIpMap = mutable.Map[String, Long]()
      //所有paramkey
      val paramKeySet = mutable.Set[String]()

      //<url,<k,<v,count>>>
      val allUrlParamMap = mutable.Map[String, mutable.Map[String, mutable.Map[String, Long]]]()

      var lastTimeStamp = 0L
      val allSystemSet = mutable.Map[Long, mutable.Set[String]]()

      val allSystemCountMap = mutable.Map[Long, mutable.Map[Int, Long]]()


      for (value <- in._2.sortWith((u1, u2) => u1.timeStamp - u2.timeStamp < 0)) {

        val timeStamp = value.timeStamp
        val sourceIp = value.sourceIp
        val browser = value.browser
        val operationSystem = value.operationSystem
        val system = value.system
        val url = value.url


        val systemTimeStampArray = systemTimeStampMap.getOrElse(system, mutable.ArrayBuffer[Long]())
        val operationSystemTimeStampArray = operationSystemTimeStampMap
          .getOrElse(operationSystem, mutable.ArrayBuffer[Long]())


        val browserTimeStampArray = browserTimeStampMap.getOrElse(browser, mutable
          .ArrayBuffer[Long]())
        val sourceIpTimeStampArray = sourceIpTimeStampMap.getOrElse(sourceIp, mutable
          .ArrayBuffer[Long]())

        timeStampArray.append(timeStamp)
        systemTimeStampArray.append(timeStamp)
        operationSystemTimeStampArray.append(timeStamp)
        browserTimeStampArray.append(timeStamp)
        sourceIpTimeStampArray.append(timeStamp)

        systemTimeStampMap.put(system, systemTimeStampArray)
        operationSystemTimeStampMap.put(operationSystem, operationSystemTimeStampArray)
        browserTimeStampMap.put(browser, browserTimeStampArray)
        sourceIpTimeStampMap.put(sourceIp, sourceIpTimeStampArray)
        sourceIpMap.put(sourceIp, sourceIpMap.getOrElse(sourceIp, 0L) + 1)


        val urlParamMap = CalculationProbabilityUtil.getUrlParamMap(url)
        if (urlParamMap != null && urlParamMap.nonEmpty) {
          val preUrl = CalculationProbabilityUtil.getPreUrl(url)

          val singleUrlParamMap = allUrlParamMap.getOrElse(preUrl, mutable.Map[String, mutable
          .Map[String, Long]]())

          for ((paramKey, paramValue) <- urlParamMap) {
            val paramMap = singleUrlParamMap.getOrElse(paramKey, mutable.Map[String, Long]())
            paramMap.put(paramValue, paramMap.getOrElse(paramValue, 0L) + 1)
            paramKeySet.add(paramValue)
            singleUrlParamMap.put(paramKey, paramMap)
          }

          allUrlParamMap.put(preUrl, singleUrlParamMap)
        }

        val preHour = CalculationProbabilityUtil.getHourTimestamp(timeStamp, -1)
        val hour = CalculationProbabilityUtil.getHourTimestamp(timeStamp, 0)
        val sufHour = CalculationProbabilityUtil.getHourTimestamp(timeStamp, +1)

        if (timeStamp - lastTimeStamp > SESSION_TIME) {

          val preSystemSet = allSystemSet.getOrElse(preHour, mutable.Set[String]())
          val preCount = preSystemSet.size
          val preSystemCountMap = allSystemCountMap.getOrElse(preHour, mutable.Map[Int, Long]())
          preSystemCountMap.put(preCount, preSystemCountMap.getOrElse(preCount, 0L) + 1)
          allSystemCountMap.put(preHour, preSystemCountMap)
          preSystemSet.clear()
          allSystemSet.put(preHour, preSystemSet)


          val systemSet = allSystemSet.getOrElse(hour, mutable.Set[String]())
          val count = systemSet.size
          val systemCountMap = allSystemCountMap.getOrElse(hour, mutable.Map[Int, Long]())
          systemCountMap.put(count, systemCountMap.getOrElse(count, 0L) + 1)
          allSystemCountMap.put(hour, systemCountMap)
          systemSet.clear()
          allSystemSet.put(hour, systemSet)


          val sufSystemSet = allSystemSet.getOrElse(sufHour, mutable.Set[String]())
          val sufCount = sufSystemSet.size
          val sufSystemCountMap = allSystemCountMap.getOrElse(preHour, mutable.Map[Int, Long]())
          sufSystemCountMap.put(sufCount, sufSystemCountMap.getOrElse(sufCount, 0L) + 1)
          allSystemCountMap.put(preHour, sufSystemCountMap)
          sufSystemSet.clear()
          allSystemSet.put(sufHour, sufSystemSet)

        }

        lastTimeStamp = timeStamp
        val preSystemSet = allSystemSet.getOrElse(preHour, mutable.Set[String]())
        val systemSet = allSystemSet.getOrElse(hour, mutable.Set[String]())
        val sufSystemSet = allSystemSet.getOrElse(sufHour, mutable.Set[String]())
        preSystemSet.add(system)
        systemSet.add(system)
        sufSystemSet.add(system)
        allSystemSet.put(preHour, preSystemSet)
        allSystemSet.put(hour, systemSet)
        allSystemSet.put(sufHour, sufSystemSet)


      }

      PIPELINED = JEDIS.pipelined()

      updateTimeKdeProbabilityRedis(PIPELINED, timeStampArray.toArray, TIME_BANDWIDTH,
        DOUBLE_AFTER_POINT_LENGTH, TIME_PROBABILITY_REDIS_KEY_FORMAT, username)
      updateTimeKdeProbabilityRedis(PIPELINED, systemTimeStampMap.toMap, SYSTEM_TIME_BANDWIDTH,
        DOUBLE_AFTER_POINT_LENGTH, SYSTEM_TIME_PROBABILITY_REDIS_KEY_FORMAT,
        username)
      updateTimeKdeProbabilityRedis(PIPELINED, operationSystemTimeStampMap.toMap,
        OPERATION_SYSTEM_TIME_BANDWIDTH, DOUBLE_AFTER_POINT_LENGTH,
        OPERATION_SYSTEM_TIME_PROBABILITY_REDIS_KEY_FORMAT, username)
      updateTimeKdeProbabilityRedis(PIPELINED, browserTimeStampMap.toMap, BROWSER_TIME_BANDWIDTH,
        DOUBLE_AFTER_POINT_LENGTH, BROWSER_TIME_PROBABILITY_REDIS_KEY_FORMAT,
        username)
      updateTimeKdeProbabilityRedis(PIPELINED, sourceIpTimeStampMap.toMap,
        SOURCE_IP_TIME_BANDWIDTH, DOUBLE_AFTER_POINT_LENGTH,
        SOURCE_IP_TIME_PROBABILITY_REDIS_KEY_FORMAT, username)
      updateSourceIpKdeProbabilityRedis(PIPELINED, sourceIpMap.toMap, SOURCE_IP_BANDWIDTH,
        DOUBLE_AFTER_POINT_LENGTH, SOURCE_IP_PROBABILITY_REDIS_KEY_FORMAT, username)

      updateUrlParamRedis(PIPELINED, nettyChannelPool, NETTY_TIMEOUT, paramKeySet.toSet,
        allUrlParamMap.toMap, URL_PARAM_REDIS_KEY_FORMAT, username)
      updateSystemCountRedis(PIPELINED, allSystemCountMap.toMap, SYSTEM_COUNT_REDIS_KEY_FORMAT,
        username)

      updateRadioRedis(PIPELINED, systemTimeStampMap.toMap, SYSTEM_REDIS_KEY_FORMAT, username)
      updateRadioRedis(PIPELINED, operationSystemTimeStampMap.toMap,
        OPERATION_SYSTEM_REDIS_KEY_FORMAT, username)
      updateRadioRedis(PIPELINED, browserTimeStampMap.toMap, BROWSER_REDIS_KEY_FORMAT, username)
      updateRadioRedis(PIPELINED, sourceIpTimeStampMap.toMap, SOURCE_IP_REDIS_KEY_FORMAT, username)

      PIPELINED.sync()

      in._2

    }

    override def close(): Unit = {
      nettyChannelPool.close()
      JEDIS.close()
      JEDIS_POOL.destroy()
    }

    /**
     * 将不同小时系统访问次数写入redis
     *
     * @param pipeline
     * @param allSystemCountMap
     * @param keyFormat
     * @param username
     */
    def updateSystemCountRedis(pipeline: Pipeline, allSystemCountMap: Map[Long, mutable.Map[Int,
      Long]], keyFormat: String, username: String): Unit = {
      for ((hour, systemCountMap) <- allSystemCountMap) {
        val key = CalculationProbabilityUtil.getRedisKey(keyFormat, username, hour)
        for ((count, num) <- systemCountMap) {
          if (count != 0) {
            PIPELINED.hset(key, count.toString, num.toString)
          }
        }
      }
    }

    /**
     * 更新redis中的占比
     *
     * @param pipeline
     * @param map
     * @param keyFormat
     * @param username
     */
    def updateRadioRedis(pipeline: Pipeline, map: Map[String, ArrayBuffer[Long]],
                         keyFormat: String, username: String): Unit = {

      val key = CalculationProbabilityUtil.getRedisKey(keyFormat, username)
      for ((member, array) <- map) {
        pipeline.zadd(key, array.size, member)
      }

    }

    /**
     * 将kde概率和概率最大值写入redis
     *
     * @param pipeline
     * @param map
     * @param bandwidth
     * @param doubleAfterPointLength
     * @param keyFormat
     * @param username
     */
    def updateTimeKdeProbabilityRedis(pipeline: Pipeline,
                                      map: Map[String, ArrayBuffer[Long]],
                                      bandwidth: Long,
                                      doubleAfterPointLength: Int,
                                      keyFormat: String,
                                      username: String): Unit = {
      for ((key, array) <- map) {
        updateTimeKdeProbabilityRedis(pipeline, array.toArray, bandwidth, doubleAfterPointLength,
          keyFormat, username, key)
      }
    }

    /**
     * 将kde概率和概率最大值写入redis
     *
     * @param pipeline
     * @param array
     * @param doubleAfterPointLength
     * @param bandwidth
     * @param keyFormat
     * @param value
     */
    def updateTimeKdeProbabilityRedis(pipeline: Pipeline,
                                      array: Array[Long],
                                      bandwidth: Long,
                                      doubleAfterPointLength: Int,
                                      keyFormat: String,
                                      value: Any*): Unit
    = {

      val key = CalculationProbabilityUtil.getRedisKey(keyFormat, value: _*)
      val timeMap = getTimeKdeProbabilityMap(array, bandwidth)
      val maxProbability = try timeMap.values.max catch {
        case exception: Exception => 0.0
      }

      for ((index, probability) <- timeMap) {
        pipeline.hset(key, index.toString, CalculationProbabilityUtil.formatDouble(CalculationProbabilityUtil
          .getNormalization(probability, maxProbability, 0.0), doubleAfterPointLength).toString)
      }
    }

    /**
     * 将sourceIp概率和概率最大值写入redis
     *
     * @param pipeline
     * @param map
     * @param bandwidth
     * @param doubleAfterPointLength
     * @param keyFormat
     * @param username
     */
    def updateSourceIpKdeProbabilityRedis(pipeline: Pipeline,
                                          map: Map[String, Long],
                                          bandwidth: Long,
                                          doubleAfterPointLength: Int,
                                          keyFormat: String,
                                          username: String): Unit = {


      val key = CalculationProbabilityUtil.getRedisKey(keyFormat, username)

      val timeMap = getSourceIpKdeProbabilityMap(map, bandwidth)
      val maxProbability = try timeMap.values.max catch {
        case e: Exception => 0.0
      }

      for ((index, probability) <- timeMap) {
        pipeline.hset(key, index.toString, CalculationProbabilityUtil
          .formatDouble(CalculationProbabilityUtil
            .getNormalization(probability, maxProbability, 0.0), doubleAfterPointLength).toString)
      }
    }

    /**
     * 将历史上使用过的参数写入redis
     *
     * @param pipeline
     * @param nettyChannelPool
     * @param timeout
     * @param paramSet
     * @param allUrlParamMap
     * @param keyFormat
     * @param username
     */
    def updateUrlParamRedis(pipeline: Pipeline,
                            nettyChannelPool: NettyChannelPool,
                            timeout: Long,
                            paramSet: Set[String],
                            allUrlParamMap: Map[String, mutable.Map[String, mutable.Map[String, Long]]],
                            keyFormat: String,
                            username: String): Unit = {


      //查询参数类型
      val paramTypeMap = mutable.Map[String, String]()
      for (param <- paramSet) {
        paramTypeMap.put(param, CalculationProbabilityUtil.getHttpParamType(param,
          nettyChannelPool.getChannel(), timeout))
      }

      //将所有的参数及其类型写入redis
      for ((preUrl, singleUrlParamMap) <- allUrlParamMap) {

        for ((paramKey, paramValueMap) <- singleUrlParamMap) {

          val key = CalculationProbabilityUtil.getRedisKey(keyFormat, username, preUrl, paramKey)

          for ((paramValue, count) <- paramValueMap) {

            pipeline.zadd(key, count.toDouble, paramValue + "|" + paramTypeMap(paramValue))

          }

        }

      }
    }

    /**
     * 使用kde计算概率
     *
     * @param arrayBuffer
     * @param bandWidth
     * @return
     */
    def getTimeKdeProbabilityMap(arrayBuffer: Array[Long], bandWidth: Long): Map[Long, Double] = {

      val map = mutable.Map[Long, Double]()

      if (arrayBuffer.nonEmpty) {

        val indexArray = CalculationProbabilityUtil.getTransformTimestampArrayBuffer(arrayBuffer)
        val indexSet = indexArray.toSet
        val doubleArray = CalculationProbabilityUtil.
          longArrayToDoubleArray(CalculationProbabilityUtil.getTransformTimestampArrayBuffer(arrayBuffer))


        for (index <- indexSet) {


          var i = 0
          var p = 1.0
          while (i + index >= 0 && p != 0.0 && !map.contains(i + index)) {
            p = KernelDensity.scoreSamplesCircle(kernel = Kernel.Gaussian,
              doubleArray, index + i, bandWidth, 0,
              TimeUtil.DAY_MILLISECOND / TimeUtil.MINUTE_MILLISECOND)
            map.put(i + index, p)
            i = i - 1
          }

          i = 1
          p = 1.0
          while (i + index < 1440 && p != 0.0 && !map.contains(i + index)) {
            p = KernelDensity.scoreSamplesCircle(kernel = Kernel.Gaussian,
              doubleArray, index + i, bandWidth, 0,
              TimeUtil.DAY_MILLISECOND / TimeUtil.MINUTE_MILLISECOND)
            map.put(i + index, p)
            i = i + 1
          }

        }
      }

      map.toMap
    }

    /**
     * 计算源IPkde概率
     *
     * @param sourceIpMap
     * @param bandWidth
     * @return
     */
    def getSourceIpKdeProbabilityMap(sourceIpMap: Map[String, Long],
                                     bandWidth: Long): mutable.Map[Long, Double] = {

      val map = mutable.Map[Long, Double]()

      if (sourceIpMap.nonEmpty) {

        val arrayBuffer = new ArrayBuffer[Double]()
        for ((sourceIp, count) <- sourceIpMap) {
          val sourceIpLong = CalculationProbabilityUtil.sourceIpTransform(sourceIp).toDouble
          for (i <- 0L until count) {
            arrayBuffer.append(sourceIpLong)
          }
        }


        for (sourceIp <- sourceIpMap.keys) {

          val sourceIpLong = CalculationProbabilityUtil.sourceIpTransform(sourceIp)
          var i = 0
          var p = 1.0
          while (p != 0.0 && !map.contains(i + sourceIpLong)) {
            p = KernelDensity.scoreSamplesCircle(kernel = Kernel.Gaussian,
              arrayBuffer.toArray, sourceIpLong + i, bandWidth, 0,
              TimeUtil.DAY_MILLISECOND / TimeUtil.MINUTE_MILLISECOND)
            map.put(i + sourceIpLong, p)
            i = i - 1
          }

          i = 1
          p = 1.0
          while (p != 0.0 && !map.contains(i + sourceIpLong)) {
            p = KernelDensity.scoreSamplesCircle(kernel = Kernel.Gaussian,
              arrayBuffer.toArray, sourceIpLong + i, bandWidth, 0,
              TimeUtil.DAY_MILLISECOND / TimeUtil.MINUTE_MILLISECOND)
            map.put(i + sourceIpLong, p)
            i = i + 1
          }

        }
      }

      map
    }

  }


  /**
   * 获取原始数据文件过滤器
   *
   * @param inputPath
   * @param timeStamp
   * @param dateFormat
   * @return
   */
  def getRawFilePathFilter(inputPath: String,
                           timeStamp: Long,
                           lastDealTimeStamp: Long,
                           timeRange: Long,
                           dateFormat: String): TextInputFormat = {


    val sampleDateFormat = new SimpleDateFormat(dateFormat)

    val fileInputFormat = new TextInputFormat(new Path(inputPath))
    fileInputFormat.setNestedFileEnumeration(true)
    fileInputFormat.setFilesFilter(
      new FilePathFilter {
        override def filterPath(filePath: Path): Boolean = {
          //如果是目录过滤指定日期之前的数据
          if ("^time=.*".r.findAllIn(filePath.getName).nonEmpty) {

            val fileTimeStamp = sampleDateFormat.parse(filePath.getName.substring(5, 13)).getTime
            if (lastDealTimeStamp == 0L) {
              timeStamp <= fileTimeStamp || timeStamp - fileTimeStamp > timeRange
            } else {
              timeStamp == fileTimeStamp || fileTimeStamp < lastDealTimeStamp
            }

          } else {
            //如果不是目录则只保留.txt文件
            ".*txt$".r.findAllIn(filePath.getName).isEmpty
          }
        }
      })

    fileInputFormat
  }

  /**
   * 获取临时文件过滤器
   *
   * @param inputPath
   * @return
   */
  def getTmpFilePathFilter(inputPath: String): TextInputFormat = {

    val fileInputFormat = new TextInputFormat(new Path(inputPath))
    fileInputFormat.setNestedFileEnumeration(true)
    fileInputFormat.setFilesFilter(
      new FilePathFilter {
        override def filterPath(filePath: Path): Boolean = {
          //如果不是目录则只保留.txt文件
          ".*txt$".r.findAllIn(filePath.getName).isEmpty
        }
      })

    fileInputFormat
  }
}