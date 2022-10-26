/*
 * @project mss
 * @company Fujian Fujitsu Communication Software Co., Ltd.
 * @author chenwei
 * @date 2019-12-17 10:59:31
 * @version v1.0
 * @update [no] [date YYYY-MM-DD] [name] [description]
 */
package cn.ffcs.is.mss.analyzer.flink.behavior.user.sink

import java.io.{BufferedReader, InputStreamReader}
import java.net.URI
import java.util.Properties

import cn.ffcs.is.mss.analyzer.flink.behavior.user.process.CalculationProbabilityUtil
import cn.ffcs.is.mss.analyzer.utils.{Constants, JedisUtil}
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction
import redis.clients.jedis.{Jedis, JedisPool, Pipeline}

/**
 *
 * @author chenwei
 * @date 2019-12-17 10:59:31
 * @title UserBehaviorAnalyzerTotalScore
 * @update [no] [date YYYY-MM-DD] [name] [description]
 */
class UserBehaviorAnalyzerTotalScore extends RichSinkFunction[Double] {

  var JEDIS_POOL: JedisPool = _
  var JEDIS: Jedis = _
  var PIPELINED: Pipeline = _

  var TOTAL_SCORE = "TOTAL_SCORE"
  var TOTAL_COUNT = "TOTAL_COUNT"
  //用户行为分析double保留位数
  var DOUBLE_AFTER_POINT_LENGTH = 4

  override def open(parameters: Configuration): Unit = {

    val globConf = getRuntimeContext.getExecutionConfig.getGlobalJobParameters
      .asInstanceOf[Configuration]
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

    TOTAL_SCORE = globConf.getString(Constants
      .USER_BEHAVIOR_ANALYZER_TOTAL_SCORE_REDIS_KEY_FORMAT, "TOTAL_SCORE")
    TOTAL_COUNT = globConf.getString(Constants
      .USER_BEHAVIOR_ANALYZER_TOTAL_COUNT_REDIS_KEY_FORMAT, "TOTAL_COUNT")

  }

  override def invoke(value: Double): Unit = {
    val count = {
      try JEDIS.get(TOTAL_COUNT).toLong catch {
        case e: Exception => 0L
      }
    } + 1L

    var score = try JEDIS.get(TOTAL_SCORE).toDouble catch {
      case e: Exception => 0.0
    }
    score = (count.toDouble - 1) / count.toDouble * score + value * 100.0 / count.toDouble

    CalculationProbabilityUtil.formatDouble(score, DOUBLE_AFTER_POINT_LENGTH)
    JEDIS.set(TOTAL_SCORE, score.toString)
    JEDIS.set(TOTAL_COUNT, count.toString)

  }
}
