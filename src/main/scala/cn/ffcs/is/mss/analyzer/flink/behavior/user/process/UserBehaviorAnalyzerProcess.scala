/*
 * @project mss
 * @company Fujian Fujitsu Communication Software Co., Ltd.
 * @author chenwei
 * @date 2019-12-04 16:04:53
 * @version v1.0
 * @update [no] [date YYYY-MM-DD] [name] [description]
 */
package cn.ffcs.is.mss.analyzer.flink.behavior.user.process

import java.io.{BufferedReader, InputStreamReader}
import java.net.URI
import java.sql.Timestamp
import java.util.Properties
import java.util.concurrent.Executors

import cn.ffcs.is.mss.analyzer.bean.BbasUserBehaviorAnalyzerWarnEntity
import cn.ffcs.is.mss.analyzer.druid.model.scala.OperationModel
import cn.ffcs.is.mss.analyzer.flink.behavior.user.UserBehaviorPhoenixModel
import cn.ffcs.is.mss.analyzer.ml.tree.server.NettyChannelPool
import cn.ffcs.is.mss.analyzer.utils.libInjection.sql.Libinjection
import cn.ffcs.is.mss.analyzer.utils.libInjection.xss.XSSInjectionUtil
import cn.ffcs.is.mss.analyzer.utils.{Constants, JedisUtil, TimeUtil}
import org.apache.flink.api.common.state.{ValueState, ValueStateDescriptor}
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.ProcessFunction
import org.apache.flink.util.Collector
import redis.clients.jedis.{Jedis, JedisPool, Pipeline}

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.collection.JavaConversions._
import scala.concurrent.{ExecutionContext, Future}

/**
 *
 * @author chenwei
 * @date 2019-12-04 16:04:53
 * @title UserBehaviorAnalyzerProcess
 * @update [no] [date YYYY-MM-DD] [name] [description]
 */
class UserBehaviorAnalyzerProcess extends ProcessFunction[String, (Option[UserBehaviorPhoenixModel], Option[(Object, Boolean)], Option[Double])] {


  //所有访问的http响应码，及其对应的时间戳
  lazy val allHttpStatusTimestampValueState: ValueState[mutable.Map[String, mutable
  .ArrayBuffer[Long]]] = getRuntimeContext
    .getState(new ValueStateDescriptor[mutable.Map[String, mutable.ArrayBuffer[Long]]]
    ("allHttpStatusTimestampValueState", classOf[mutable.Map[String, mutable.ArrayBuffer[Long]]]))

  //所有访问过的系统，及其对应的时间戳
  lazy val allSystemTimestampValueState: ValueState[mutable.Map[String, mutable
  .ArrayBuffer[Long]]] = getRuntimeContext
    .getState(new ValueStateDescriptor[mutable.Map[String, mutable.ArrayBuffer[Long]]]
    ("allSystemTimestampValueState", classOf[mutable.Map[String, mutable.ArrayBuffer[Long]]]))

  var nettyChannelPool: NettyChannelPool = _

  var JEDIS_POOL: JedisPool = _
  var JEDIS: Jedis = _
  var PIPELINED: Pipeline = _
  var libinjection: Libinjection = _
  var xSSInjectionUtil: XSSInjectionUtil = _

  var TIME_THRESHOLD = 0L
  var PROBABILITY_THRESHOLD = 0.0
  var ROBOT_SOURCE_IP_SET: Set[String] = _
  var ID_PLACE_MAP: Map[String, Set[String]] = _
  var PLACE_THRESHOLD = 3
  var TIME_RANGE = 86400000L

  //计算http状态码时参考的数据范围
  var HTTP_STATUS_REFERENCE_TIME_RANGE = TimeUtil.MINUTE_MILLISECOND * 10
  //用户行为分析session时间(1000L * 60 * 5)
  var SESSION_TIME = 300000L
  //参数分类服务netty连接timeout
  var NETTY_TIMEOUT = 30000

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
  //用户行为分析最后一次访问信息在redis的key
  var LAST_VISIT_INFORMATION_REDIS_KEY_FORMAT = "%s:LAST_VISIT_INFORMATION"
  //用户行为分析最后一次访问信息在redis的key 最后一次访问时间的field
  var LAST_VISIT_TIMESTAMP_REDIS_FIELD_FORMAT = "USER_LAST_VISIT_TIMESTAMP"
  //用户行为分析最后一次访问信息在redis的key 最后一次访问地点的field
  var LAST_VISIT_PLACE_REDIS_FIELD_FORMAT = "USER_LAST_VISIT_PLACE"
  //用户行为分析最后一次访问信息在redis的key 最后一次访问系统的field
  var LAST_VISIT_SYSTEM_REDIS_FIELD_FORMAT = "USER_LAST_VISIT_SYSTEM"
  //用户行为分析最后一次访问信息在redis的key 评分的field
  var SCORE_REDIS_FIELD_FORMAT = "USER_SCORE"
  //用户行为分析最后一次访问信息在redis的key 访问次数的field
  var COUNT_REDIS_FIELD_FORMAT = "COUNT"
  //用户行为分析最后一次访问信息在redis的key 地点的field
  var PLACE_REDIS_FIELD_FORMAT = "PLACE:%s"
  //用户行为分析最后一次访问信息在redis的key 用户状态的field
  var STATUS_REDIS_FIELD_FORMAT = "USER_STATUS"
  //用户行为分析最后一次访问信息在redis的key 用户描述的field
  var PERSONAS_REDIS_FIELD_FORMAT = "USER_PERSONAS"
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
      globConf.getInteger(Constants.USER_BEHAVIOR_ANALYZER_PARAM_CLASSIFICATION_SERVER_PORT, 10000))
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

    ////加载sql注入检测工具
    val rulePath = globConf.getString(Constants.SQL_INJECTION_RULE_PATH, "/")
    libinjection = new Libinjection(rulePath, fileSystemType)

    ////加载xss工具
    //val soPath = globConf.getString(Constants.XSS_INJECTION_RULE_PATH, "/")
    //val RuleHDFSPath = globConf.getString(Constants.XSS_INJECTION_HDFS_RULE_PATH, "/")
    //xSSInjectionUtil = new XSSInjectionUtil(fileSystemType, RuleHDFSPath, soPath)

    //ip-地点关联文件路径
    OperationModel.setPlaceMap(globConf.getString(Constants.OPERATION_PLACE_PATH, ""))
    //host-系统名关联文件路径
    OperationModel.setSystemMap(globConf.getString(Constants.OPERATION_SYSTEM_PATH, ""))
    OperationModel.setMajorMap(globConf.getString(Constants.OPERATION_SYSTEM_PATH, ""))
    //用户名-常用登录地关联文件路径
    OperationModel.setUsedPlacesMap(globConf.getString(Constants.OPERATION_USEDPLACE_PATH, ""))

    HTTP_STATUS_REFERENCE_TIME_RANGE = globConf.getLong(Constants
      .USER_BEHAVIOR_ANALYZER_HTTP_STATUS_REFERENCE_TIME_RANGE, 600000L)
    SESSION_TIME = globConf.getLong(Constants.USER_BEHAVIOR_ANALYZER_SESSION_TIME, 300000L)
    TIME_THRESHOLD = globConf.getLong(Constants
      .USER_BEHAVIOR_ANALYZER_NOT_LOGIN_FOR_A_LONG_TIME_WARN_TIME_THRESHOLD, 2592000000L)
    PROBABILITY_THRESHOLD = globConf.getDouble(Constants
      .USER_BEHAVIOR_ANALYZER_NOT_USED_TIME_LOGIN_WARN_PROBABILITY_THRESHOLD, 0.05)
    ROBOT_SOURCE_IP_SET = getRobotIpSet(fileSystemType, globConf.getString(Constants
      .USER_BEHAVIOR_ANALYZER_ROBOT_WARN_SOURCE_IP_PATH, ""))
    ID_PLACE_MAP = getIdPLaceMap(fileSystemType, globConf.getString(Constants
      .USER_BEHAVIOR_ANALYZER_REMOTE_LOGIN_WARN_PLACE_PATH, ""))
    PLACE_THRESHOLD = globConf.getInteger(Constants
      .USER_BEHAVIOR_ANALYZER_FREQUENTLY_SWITCH_LOGIN_LOCATIONS_WARN_PLACE_THRESHOLD, 3)
    TIME_RANGE = globConf.getLong(Constants
      .USER_BEHAVIOR_ANALYZER_FREQUENTLY_SWITCH_LOGIN_LOCATIONS_WARN_TIME_RANGE, 86400000L)

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
      .USER_BEHAVIOR_ANALYZER_SOURCE_IP_TIME_PROBABILITY_REDIS_KEY_FORMAT, "% s:TIME_PROBABILITY:%s")
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
    //用户行为分析最后一次访问信息在redis的key
    LAST_VISIT_INFORMATION_REDIS_KEY_FORMAT = globConf.getString(Constants
      .USER_BEHAVIOR_ANALYZER_LAST_VISIT_INFORMATION_REDIS_KEY_FORMAT, "%s:LAST_VISIT_INFORMATION")
    //用户行为分析最后一次访问信息在redis的key 最后一次访问时间的field
    LAST_VISIT_TIMESTAMP_REDIS_FIELD_FORMAT = globConf.getString(Constants
      .USER_BEHAVIOR_ANALYZER_LAST_VISIT_INFORMATION_REDIS_FIELD_FORMAT_LAST_VISIT_TIMESTAMP,
      "USER_LAST_VISIT_TIMESTAMP")
    //用户行为分析最后一次访问信息在redis的key 最后一次访问地点的field
    LAST_VISIT_PLACE_REDIS_FIELD_FORMAT = globConf.getString(Constants
      .USER_BEHAVIOR_ANALYZER_LAST_VISIT_INFORMATION_REDIS_FIELD_FORMAT_LAST_VISIT_PLACE,
      "USER_LAST_VISIT_PLACE")
    //用户行为分析最后一次访问信息在redis的key 最后一次访问系统的field
    LAST_VISIT_SYSTEM_REDIS_FIELD_FORMAT = globConf.getString(Constants
      .USER_BEHAVIOR_ANALYZER_LAST_VISIT_INFORMATION_REDIS_FIELD_FORMAT_LAST_VISIT_SYSTEM,
      "USER_LAST_VISIT_SYSTEM")
    //用户行为分析最后一次访问信息在redis的key 评分的field
    SCORE_REDIS_FIELD_FORMAT = globConf.getString(Constants
      .USER_BEHAVIOR_ANALYZER_LAST_VISIT_INFORMATION_REDIS_FIELD_FORMAT_SCORE, "USER_SCORE")
    //用户行为分析最后一次访问信息在redis的key 访问次数的field
    COUNT_REDIS_FIELD_FORMAT = globConf.getString(Constants
      .USER_BEHAVIOR_ANALYZER_LAST_VISIT_INFORMATION_REDIS_FIELD_FORMAT_COUNT, "COUNT")
    //用户行为分析最后一次访问信息在redis的key 地点的field
    PLACE_REDIS_FIELD_FORMAT = globConf.getString(Constants
      .USER_BEHAVIOR_ANALYZER_LAST_VISIT_INFORMATION_REDIS_FIELD_FORMAT_PLACE, "PLACE:%s")
    //用户行为分析最后一次访问信息在redis的key 用户状态的field
    STATUS_REDIS_FIELD_FORMAT = globConf.getString(Constants
      .USER_BEHAVIOR_ANALYZER_LAST_VISIT_INFORMATION_REDIS_FIELD_FORMAT_STATUS, "USER_STATUS")
    //用户行为分析最后一次访问信息在redis的key 用户描述的field
    PERSONAS_REDIS_FIELD_FORMAT = globConf.getString(Constants
      .USER_BEHAVIOR_ANALYZER_LAST_VISIT_INFORMATION_REDIS_FIELD_FORMAT_PERSONAS, "USER_PERSONAS")

    //用户行为分析double保留位数
    DOUBLE_AFTER_POINT_LENGTH = globConf.getInteger(Constants.USER_BEHAVIOR_ANALYZER_DOUBLE_AFTER_POINT_LENGTH, 4)
  }

  override def processElement(value: String, ctx: ProcessFunction[String,
    (Option[UserBehaviorPhoenixModel], Option[(Object, Boolean)], Option[Double])]#Context,
                              out: Collector[(Option[UserBehaviorPhoenixModel], Option[(Object,
                                Boolean)
                              ], Option[Double])]): Unit = {


    val operationModelOption = OperationModel.getOperationModel(value)
    if (operationModelOption.isDefined) {
      val operationModel = operationModelOption.head
      val timeStamp = CalculationProbabilityUtil.getTimeStamp(value, operationModel)
      val username = CalculationProbabilityUtil.getUsername(value, operationModel)
      val sourceIp = CalculationProbabilityUtil.getSourceIp(value, operationModel)
      val userAgent = CalculationProbabilityUtil.getUserAgent(value, operationModel)
      val browser = CalculationProbabilityUtil.getBrowser(value, operationModel)
      val operationSystem = CalculationProbabilityUtil.getOperationSystem(value, operationModel)
      val system = CalculationProbabilityUtil.getSystem(value, operationModel)
      val httpStatus = CalculationProbabilityUtil.getHttpStatus(value, operationModel)
      val url = CalculationProbabilityUtil.getUrl(value, operationModel)
      val place = CalculationProbabilityUtil.getPlace(value, operationModel)
      val sourcePort = CalculationProbabilityUtil.getSourcePort(value, operationModel)
      val destinationIp = CalculationProbabilityUtil.getDestinationIp(value, operationModel)
      val destinationPort = CalculationProbabilityUtil.getDestinationPort(value, operationModel)
      val host = CalculationProbabilityUtil.getHost(value, operationModel)
      val reference = CalculationProbabilityUtil.getReference(value, operationModel)
      val isDownload = CalculationProbabilityUtil.getIsDownload(value, operationModel)
      val isDownSuccess = CalculationProbabilityUtil.getIsDownSuccess(value, operationModel)
      val downFileSize = CalculationProbabilityUtil.getDownFileSize(value, operationModel)
      val downFileName = CalculationProbabilityUtil.getDownFileName(value, operationModel)
      val formValue = CalculationProbabilityUtil.getFormValue(value, operationModel)
      val inputOctets = CalculationProbabilityUtil.getInputOctets(value, operationModel)
      val outputOctets = CalculationProbabilityUtil.getOutputOctets(value, operationModel)


      //更新state中的http status信息。
      val allHttpStatusTimestampValue = updateHttpStatusState(httpStatus, timeStamp,
        HTTP_STATUS_REFERENCE_TIME_RANGE, allHttpStatusTimestampValueState.value())
      allHttpStatusTimestampValueState.update(allHttpStatusTimestampValue)
      //更新redis中的http status信息
      updateRedisHttpStatus(JEDIS, username, HTTP_STATUS_REDIS_KEY_FORMAT,
        allHttpStatusTimestampValue)
      //计算http status 概率
      val httpStatusProbability = CalculationProbabilityUtil.formatDouble(CalculationProbabilityUtil
        .getHttpStatusProbability(allHttpStatusTimestampValue), DOUBLE_AFTER_POINT_LENGTH)


      //更新state中的访问系统信息
      val allSystemTimestampValue = updateSystemState(timeStamp, system, SESSION_TIME,
        allSystemTimestampValueState.value())
      allSystemTimestampValueState.update(allSystemTimestampValue)
      //计算系统个数概率
      val systemCountProbability = CalculationProbabilityUtil.formatDouble(CalculationProbabilityUtil
        .getSystemCountProbability(allSystemTimestampValue.keySet.size,
          JEDIS.hgetAll(CalculationProbabilityUtil.getRedisKey(SYSTEM_COUNT_REDIS_KEY_FORMAT,
            username, CalculationProbabilityUtil.getHourTimestamp(timeStamp, 0)))
        ), DOUBLE_AFTER_POINT_LENGTH)

      //分钟索引值
      val timeIndex = CalculationProbabilityUtil.timeStampTransform(timeStamp)

      //计算时间和源IP概率值
      val timeProbability = getRedisKdeProbability(JEDIS, CalculationProbabilityUtil
        .getRedisKey(TIME_PROBABILITY_REDIS_KEY_FORMAT, username), timeIndex)
      val systemTimeProbability = getRedisKdeProbability(JEDIS, CalculationProbabilityUtil
        .getRedisKey(SYSTEM_TIME_PROBABILITY_REDIS_KEY_FORMAT, username, system), timeIndex)
      val operationSystemTimeProbability = getRedisKdeProbability(JEDIS, CalculationProbabilityUtil
        .getRedisKey(OPERATION_SYSTEM_TIME_PROBABILITY_REDIS_KEY_FORMAT, username,
          operationSystem), timeIndex)
      val browserTimeProbability = getRedisKdeProbability(JEDIS, CalculationProbabilityUtil
        .getRedisKey(BROWSER_TIME_PROBABILITY_REDIS_KEY_FORMAT, username, browser), timeIndex)
      val sourceIpTimeProbability = getRedisKdeProbability(JEDIS, CalculationProbabilityUtil
        .getRedisKey(SOURCE_IP_TIME_PROBABILITY_REDIS_KEY_FORMAT, username, sourceIp), timeIndex)
      val sourceIpProbability = getRedisKdeProbability(JEDIS, CalculationProbabilityUtil
        .getRedisKey(SOURCE_IP_PROBABILITY_REDIS_KEY_FORMAT, username), CalculationProbabilityUtil
        .sourceIpTransform(sourceIp))


      val urlParamProbability = CalculationProbabilityUtil.formatDouble(getUrlParamProbability(JEDIS, url, username,
        URL_PARAM_REDIS_KEY_FORMAT, nettyChannelPool, NETTY_TIMEOUT), DOUBLE_AFTER_POINT_LENGTH)

      //计算总的概率值
      val probability = CalculationProbabilityUtil.formatDouble(CalculationProbabilityUtil.getNormalization(timeProbability + systemTimeProbability + operationSystemTimeProbability +
        browserTimeProbability + sourceIpProbability + sourceIpTimeProbability +
        httpStatusProbability + urlParamProbability + systemCountProbability, 9.0, 0.0), DOUBLE_AFTER_POINT_LENGTH)

      val lastTimeStamp = getLastVisitTimestamp(JEDIS, username,
        LAST_VISIT_INFORMATION_REDIS_KEY_FORMAT, LAST_VISIT_TIMESTAMP_REDIS_FIELD_FORMAT)
      val status = getUserStatus(JEDIS, username, LAST_VISIT_INFORMATION_REDIS_KEY_FORMAT,
        STATUS_REDIS_FIELD_FORMAT)
      val placeSet = getPlaceSet(JEDIS, username, timeStamp, place, TIME_RANGE,
        LAST_VISIT_INFORMATION_REDIS_KEY_FORMAT, PLACE_REDIS_FIELD_FORMAT)

      val warnType = CalculationWarnTypeUtil.getWarnType(username, timeStamp, lastTimeStamp,
        TIME_THRESHOLD, timeProbability, PROBABILITY_THRESHOLD, url, sourceIp, place, system,
        status, libinjection, xSSInjectionUtil, ROBOT_SOURCE_IP_SET, ID_PLACE_MAP, placeSet,
        PLACE_THRESHOLD)

      val userBehaviorModel = UserBehaviorPhoenixModel(username, timeStamp, sourceIp, sourcePort,
        place, destinationIp, destinationPort, host, system, url, httpStatus, reference, userAgent,
        browser, operationSystem, isDownload, isDownSuccess, downFileSize, downFileName, formValue,
        inputOctets, outputOctets, timeProbability, systemTimeProbability, operationSystemTimeProbability,
        browserTimeProbability, sourceIpTimeProbability, sourceIpProbability, httpStatusProbability,
        urlParamProbability, systemCountProbability, probability, warnType.toBinaryString);


      if (warnType > 0) {

        val bbasUserBehaviorAnalyzerWarnEntity = new BbasUserBehaviorAnalyzerWarnEntity()
        bbasUserBehaviorAnalyzerWarnEntity.setUserName(username)
        bbasUserBehaviorAnalyzerWarnEntity.setWarnDatetime(new Timestamp(timeStamp))
        bbasUserBehaviorAnalyzerWarnEntity.setSourceIp(sourceIp)
        bbasUserBehaviorAnalyzerWarnEntity.setPlace(place)
        bbasUserBehaviorAnalyzerWarnEntity.setDestinationIp(destinationIp)
        bbasUserBehaviorAnalyzerWarnEntity.setDestinationPort(destinationPort + "")
        bbasUserBehaviorAnalyzerWarnEntity.setHost(host)
        bbasUserBehaviorAnalyzerWarnEntity.setSystem(system)
        bbasUserBehaviorAnalyzerWarnEntity.setUrl(url)
        bbasUserBehaviorAnalyzerWarnEntity.setHttpStatus(httpStatus)
        bbasUserBehaviorAnalyzerWarnEntity.setUserAgent(userAgent)
        bbasUserBehaviorAnalyzerWarnEntity.setOperationSystem(operationSystem)
        bbasUserBehaviorAnalyzerWarnEntity.setBrowser(browser)
        bbasUserBehaviorAnalyzerWarnEntity.setCookie("")
        bbasUserBehaviorAnalyzerWarnEntity.setWarnType(warnType)

        out.collect(Some(userBehaviorModel), Some((bbasUserBehaviorAnalyzerWarnEntity, true)),
          Some(probability))

      } else {
        out.collect((Some(userBehaviorModel), None, Some(probability)))

      }

      //更新并最后一次访问的基本信息
      updateLastVisitInformation(JEDIS, username, LAST_VISIT_INFORMATION_REDIS_KEY_FORMAT,
        timeStamp, place, system, probability, DOUBLE_AFTER_POINT_LENGTH,
        LAST_VISIT_TIMESTAMP_REDIS_FIELD_FORMAT, LAST_VISIT_SYSTEM_REDIS_FIELD_FORMAT,
        LAST_VISIT_PLACE_REDIS_FIELD_FORMAT, SCORE_REDIS_FIELD_FORMAT, COUNT_REDIS_FIELD_FORMAT,
        PLACE_REDIS_FIELD_FORMAT)

    }


  }


  override def close(): Unit = {
    if (nettyChannelPool != null) {
      nettyChannelPool.close()
    }
  }

  /**
   * 计算url参数概率
   *
   * @param jedis
   * @param url
   * @param username
   * @param urlParamRedisKeyFormat
   * @param nettyChannelPool
   * @param timeout
   * @return
   */
  def getUrlParamProbability(jedis: Jedis, url: String, username: String,
                             urlParamRedisKeyFormat: String,
                             nettyChannelPool: NettyChannelPool, timeout: Long): Double = {

    val urlParamMap = CalculationProbabilityUtil.getUrlParamMap(url)
    val preUrl = CalculationProbabilityUtil.getPreUrl(url)

    var urlParamProbability = 1.0

    if (urlParamMap != null && urlParamMap.nonEmpty) {


      val paramTypeMap = mutable.Map[String, (String, String)]()
      val paramResultMap = mutable.Map[String, mutable.Map[(String, String), Long]]()

      for ((k, v) <- urlParamMap) {

        val paramSet = jedis.zrevrangeWithScores(CalculationProbabilityUtil.getRedisKey
        (urlParamRedisKeyFormat, username, preUrl, k), 0, -1)

        val singleKMap = paramResultMap.getOrElse(k, mutable.Map[(String, String), Long]())

        for (p <- paramSet) {
          singleKMap.put((p.getElement.split("\\|", 2)(0), p.getElement.split("\\|", 2)(1)), p
            .getScore.toLong)
        }

        paramResultMap.put(k, singleKMap)

        paramTypeMap.put(k, (v, CalculationProbabilityUtil.getHttpParamType(v, nettyChannelPool
          .getChannel(), timeout)))
      }

      urlParamProbability = CalculationProbabilityUtil.getUrlParamProbability(paramTypeMap,
        paramResultMap)
    }

    urlParamProbability
  }

  /**
   * 获取redis中保存的概率
   *
   * @param jedis
   * @param redisKey
   * @param index
   * @return
   */
  def getRedisKdeProbability(jedis: Jedis, redisKey: String, index: Long)
  : Double = {
    try jedis.hget(redisKey, index + "")
      .toDouble
    catch {
      case e: Exception => {
        0.0
      }
    }

  }

  /**
   * 获取redis中保存的概率最大值
   *
   * @param jedis
   * @param redisKey
   * @param value
   * @return
   */
  def getRedisKdeProbabilityMax(jedis: Jedis, redisKey: String, value: Any*): Double = {
    try jedis.get(CalculationProbabilityUtil.getRedisKey(redisKey, value))
      .toDouble
    catch {
      case e: Exception => {
        0.0
      }
    }
  }

  /**
   * 根据机器人ip配置文件，获取机器人ip列表
   *
   * @param fileSystemType
   * @param robotIpPath
   * @return
   */
  def getRobotIpSet(fileSystemType: String, robotIpPath: String): Set[String] = {
    val fs = org.apache.hadoop.fs.FileSystem.get(URI.create(fileSystemType), new org.apache
    .hadoop.conf.Configuration())
    val fsDataInputStream = fs.open(new org.apache.hadoop.fs.Path(robotIpPath))
    val bufferedReader = new BufferedReader(new InputStreamReader(fsDataInputStream))

    val robotIpSet = mutable.Set[String]()

    var line = bufferedReader.readLine()
    while (line != null) {

      robotIpSet.add(line)
      line = bufferedReader.readLine()
    }

    robotIpSet.toSet

  }

  /**
   * 获取人力编码允许的登录地
   *
   * @param fileSystemType
   * @param idPLacePath
   * @return
   */
  def getIdPLaceMap(fileSystemType: String, idPLacePath: String): Map[String, Set[String]] = {
    val fs = org.apache.hadoop.fs.FileSystem.get(URI.create(fileSystemType), new org.apache
    .hadoop.conf.Configuration())
    val fsDataInputStream = fs.open(new org.apache.hadoop.fs.Path(idPLacePath))
    val bufferedReader = new BufferedReader(new InputStreamReader(fsDataInputStream))

    val idPLaceMap = mutable.Map[String, Set[String]]()

    var line = bufferedReader.readLine()
    while (line != null) {

      val values = line.split("\\|", -1)
      if (values.length > 1) {
        val set = mutable.Set[String]()
        for (i <- 1 until values.length) {
          set.add(values(i))
        }

        idPLaceMap.put(values(0), set.toSet)
      }
      line = bufferedReader.readLine()
    }

    idPLaceMap.toMap
  }


  /**
   * 更新state中保存的system信息
   *
   * @param timeStamp
   * @param system
   * @param sessionTime
   * @param allSystemTimestampValue
   * @return
   */
  def updateSystemState(timeStamp: Long, system: String, sessionTime: Long,
                        allSystemTimestampValue: mutable.Map[String, mutable.ArrayBuffer[Long]])
  : mutable.Map[String, mutable.ArrayBuffer[Long]] = {

    //如果历史记录不为空
    if (allSystemTimestampValue != null) {
      //先过滤范围外的数据
      for (t <- allSystemTimestampValue.keySet) {
        val array = allSystemTimestampValue(t).filter(timeStamp - _ < sessionTime)
        if (array.isEmpty) {
          allSystemTimestampValue.remove(t)
        } else {
          allSystemTimestampValue.put(t, array)
        }
      }

      //如果不是未知系统，增加本次信息
      if (!"未知系统".equals(system)) {
        val array = allSystemTimestampValue.getOrElse(system, new ArrayBuffer[Long]())
        array.append(timeStamp)
        allSystemTimestampValue.put(system, array)
      }
      allSystemTimestampValue
    } else {

      //返回本次信息
      val array = new ArrayBuffer[Long]()
      array.append(timeStamp)
      mutable.Map[String, mutable.ArrayBuffer[Long]]((system, array))

    }
  }

  /**
   * 更新state中保存的http status
   *
   * @param httpStatus
   * @param timeStamp
   * @param httpStatusReferenceTimeRange
   * @param allHttpStatusTimestampValue
   * @return
   */
  def updateHttpStatusState(httpStatus: String, timeStamp: Long, httpStatusReferenceTimeRange: Long,
                            allHttpStatusTimestampValue: mutable.Map[String, mutable
                            .ArrayBuffer[Long]]): mutable.Map[String, mutable.ArrayBuffer[Long]] = {

    //如果历史记录不为空
    if (allHttpStatusTimestampValue != null) {
      //先过滤范围外的数据
      for (t <- allHttpStatusTimestampValue.keySet) {
        val array = allHttpStatusTimestampValue(t).filter(timeStamp - _ <
          httpStatusReferenceTimeRange)
        if (array.isEmpty) {
          allHttpStatusTimestampValue.remove(t)
        } else {
          allHttpStatusTimestampValue.put(t, array)
        }
      }

      //增加本次信息
      val array = allHttpStatusTimestampValue.getOrElse(httpStatus, new ArrayBuffer[Long]())
      array.append(timeStamp)
      allHttpStatusTimestampValue.put(httpStatus, array)
      allHttpStatusTimestampValue
    } else {

      //返回本次信息
      val array = new ArrayBuffer[Long]()
      array.append(timeStamp)
      mutable.Map[String, mutable.ArrayBuffer[Long]]((httpStatus, array))

    }

  }

  /**
   * 更新redis中的http status信息
   *
   * @param jedis
   * @param userName
   * @param httpStatusRedisKeyFormat
   * @param allHttpStatusTimestampValue
   */
  def updateRedisHttpStatus(jedis: Jedis, userName: String, httpStatusRedisKeyFormat: String,
                            allHttpStatusTimestampValue: mutable.Map[String, mutable
                            .ArrayBuffer[Long]]): Unit = {
    for ((status, arrayBuffer) <- allHttpStatusTimestampValue) {
      jedis.hset(CalculationProbabilityUtil.getRedisKey(httpStatusRedisKeyFormat, userName),
        status, arrayBuffer.length + "")
    }
  }

  /**
   * 获取上次访问的时间
   *
   * @param jedis
   * @param userName
   * @param lastVisitInformationRedisKeyFormat
   * @param lastVisitTimestampRedisFieldFormat
   * @return
   */
  def getLastVisitTimestamp(jedis: Jedis, userName: String,
                            lastVisitInformationRedisKeyFormat: String,
                            lastVisitTimestampRedisFieldFormat: String): Long = {

    try jedis.hget(CalculationProbabilityUtil.getRedisKey(lastVisitInformationRedisKeyFormat,
      userName), CalculationProbabilityUtil.getRedisField(lastVisitTimestampRedisFieldFormat))
      .toLong
    catch {
      case e: Exception => 0L
    }

  }

  /**
   * 获取用户状态
   *
   * @param jedis
   * @param userName
   * @param lastVisitInformationRedisKeyFormat
   * @param statusRedisFieldFormat
   * @return
   */
  def getUserStatus(jedis: Jedis, userName: String, lastVisitInformationRedisKeyFormat: String,
                    statusRedisFieldFormat: String): Int = {
    try jedis.hget(CalculationProbabilityUtil.getRedisKey(lastVisitInformationRedisKeyFormat,
      userName), CalculationProbabilityUtil.getRedisField(statusRedisFieldFormat)).toInt
    catch {
      case e: Exception => 1
    }
  }

  /**
   * 获取最近一天内所有的登录地,并且删除多余的部分
   *
   * @param jedis
   * @param userName
   * @param timestamp
   * @param place
   * @param timeRange
   * @param lastVisitInformationRedisKeyFormat
   * @param placeRedisFieldFormat
   * @return
   */
  def getPlaceSet(jedis: Jedis, userName: String, timestamp: Long, place: String, timeRange: Long,
                  lastVisitInformationRedisKeyFormat: String, placeRedisFieldFormat: String)
  : Set[String] = {

    val lastVisitInformationKey = CalculationProbabilityUtil
      .getRedisKey(lastVisitInformationRedisKeyFormat, userName)

    val lastVisitInformationMap = jedis.hgetAll(lastVisitInformationKey)

    val placeSet = mutable.Set[String]()

    val formatArray = placeRedisFieldFormat.split(":", -1)

    lastVisitInformationMap.filter(key => {
      val keyArray = key._1.split(":", -1)
      if (formatArray.length == keyArray.length) {
        var equal = true
        for (i <- 0 until keyArray.length) {
          if (equal) {
            if (formatArray(i) != null && formatArray(i).length > 0) {
              equal = !(formatArray(i)(0) != '%' && !formatArray(i).equals(keyArray(i)))
            } else {
              equal = !formatArray(i).equals(keyArray(i))
            }
          }

        }
        equal
      } else {
        false
      }
    })
      .foreach(t => {
        val placeTimestamp = try t._2.toLong catch {
          case e: Exception => 0L
        }

        if (timestamp - placeTimestamp <= timeRange) {
          placeSet.add(t._1)

        } else {
          jedis.hdel(lastVisitInformationKey, t._1)
        }
      })

    placeSet.add(place)
    placeSet.toSet
  }

  /**
   * 更新最后一次访问的信息
   *
   * @param jedis
   * @param userName
   * @param lastVisitInformationRedisKeyFormat
   * @param timeStamp
   * @param place
   * @param system
   * @param probability
   * @param lastVisitTimestampRedisFieldFormat
   * @param lastVisitSystemRedisFieldFormat
   * @param lastVisitPlaceRedisFieldFormat
   * @param scoreRedisFieldFormat
   * @param countRedisFieldFormat
   * @param placeRedisFieldFormat
   */
  def updateLastVisitInformation(jedis: Jedis, userName: String,
                                 lastVisitInformationRedisKeyFormat: String,
                                 timeStamp: Long, place: String, system: String,
                                 probability: Double, doubleAfterPointLength: Int,
                                 lastVisitTimestampRedisFieldFormat: String,
                                 lastVisitSystemRedisFieldFormat: String,
                                 lastVisitPlaceRedisFieldFormat: String,
                                 scoreRedisFieldFormat: String, countRedisFieldFormat: String,
                                 placeRedisFieldFormat: String): Unit = {

    val lastVisitInformationKey = CalculationProbabilityUtil.
      getRedisKey(lastVisitInformationRedisKeyFormat, userName)
    val lastTimeStampKey = CalculationProbabilityUtil.
      getRedisField(lastVisitTimestampRedisFieldFormat)
    val lastSystemKey = CalculationProbabilityUtil.getRedisField(lastVisitSystemRedisFieldFormat)
    val lastPlaceKey = CalculationProbabilityUtil.getRedisField(lastVisitPlaceRedisFieldFormat)
    val scoreKey = CalculationProbabilityUtil.getRedisField(scoreRedisFieldFormat)
    val countKey = CalculationProbabilityUtil.getRedisField(countRedisFieldFormat)
    val placeKey = CalculationProbabilityUtil.getRedisField(placeRedisFieldFormat, place)

    val lastVisitInformationMap = jedis.hgetAll(lastVisitInformationKey)
    if (lastVisitInformationMap != null && lastVisitInformationMap.size() > 0) {

      val lastTimeStamp = try lastVisitInformationMap.get(lastTimeStampKey).toLong
      catch {
        case e:Exception => 0L
      }
      if (lastTimeStamp < timeStamp) {
        jedis.hset(lastVisitInformationKey, lastTimeStampKey, timeStamp.toString)
        jedis.hset(lastVisitInformationKey, lastSystemKey, system)
        jedis.hset(lastVisitInformationKey, lastPlaceKey, place)
      }


      if (lastVisitInformationMap.containsKey(placeKey)) {
        val lastPlaceTimestamp = try lastVisitInformationMap.get(placeKey).toLong catch {
          case e: Exception => 0L
        }
        if (lastPlaceTimestamp < timeStamp) {
          jedis.hset(lastVisitInformationKey, placeKey, timeStamp.toString)
        }
      } else {
        jedis.hset(lastVisitInformationKey, placeKey, timeStamp.toString)
      }


      val count = {
        try lastVisitInformationMap.getOrDefault(countKey, "0").toLong catch {
          case e: Exception => 0L
        }
      } + 1L
      var score = {
        try lastVisitInformationMap.getOrDefault(scoreKey, "0.0").toDouble catch {
          case e: Exception => 0.0
        }
      }

      score = (count.toDouble - 1) / count.toDouble * score + probability * 100 / count.toDouble
      score = CalculationProbabilityUtil.formatDouble(score, doubleAfterPointLength)
      jedis.hset(lastVisitInformationKey, countKey, count.toString)
      jedis.hset(lastVisitInformationKey, scoreKey, score.toString)

    } else {
      jedis.hset(lastVisitInformationKey, lastTimeStampKey, timeStamp.toString)
      jedis.hset(lastVisitInformationKey, lastSystemKey, system)
      jedis.hset(lastVisitInformationKey, lastPlaceKey, place)
      jedis.hset(lastVisitInformationKey, placeKey, timeStamp.toString)
      val score = CalculationProbabilityUtil.formatDouble(probability * 100, doubleAfterPointLength)
      jedis.hset(lastVisitInformationKey, scoreKey, score.toString)
      jedis.hset(lastVisitInformationKey, countKey, "1")

    }

  }

}