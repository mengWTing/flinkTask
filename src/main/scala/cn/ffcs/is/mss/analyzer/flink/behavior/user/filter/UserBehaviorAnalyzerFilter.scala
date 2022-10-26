/*
 * @project mss
 * @company Fujian Fujitsu Communication Software Co., Ltd.
 * @author chenwei
 * @date 2019-12-04 15:38:11
 * @version v1.0
 * @update [no] [date YYYY-MM-DD] [name] [description]
 */
package cn.ffcs.is.mss.analyzer.flink.behavior.user.filter

import java.io.{BufferedReader, InputStreamReader}
import java.net.URI

import cn.ffcs.is.mss.analyzer.utils.Constants
import org.apache.flink.api.common.functions.RichFilterFunction
import org.apache.flink.configuration.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import scala.collection.mutable

/**
 *
 * @author chenwei
 * @date 2019-12-04 15:38:11
 * @title UserBehaviorAnalyzerFilter
 * @update [no] [date YYYY-MM-DD] [name] [description]
 */
class UserBehaviorAnalyzerFilter extends RichFilterFunction[String] {

  var SUFFIX_SET: Set[String] = _
  var USERNAME_SET: Set[String] = _
  var SOURCE_IP_SET: Set[String] = _

  @transient
  val logger = LoggerFactory.getLogger(classOf[UserBehaviorAnalyzerFilter])

  override def open(parameters: Configuration): Unit = {

    //获取全局配置
    val globConf = getRuntimeContext.getExecutionConfig.getGlobalJobParameters
      .asInstanceOf[Configuration]
    val ignoreSourceIp = globConf.getString(Constants.USER_BEHAVIOR_ANALYZER_IGNORE_SOURCE_IP, "")
    val ignoreSuffix = globConf.getString(Constants.USER_BEHAVIOR_ANALYZER_IGNORE_SUFFIX, "")
    val followUsernamePath = globConf.getString(Constants
      .USER_BEHAVIOR_ANALYZER_FOLLOW_USERNAME_PATH, "")
    //    val fileSystemType = globConf.getString(Constants.FILE_SYSTEM_TYPE, "file:///")
    val fileSystemType = globConf.getString(Constants.FILE_SYSTEM_TYPE, "")

    USERNAME_SET = getFollowUsernameSet(fileSystemType, followUsernamePath)
    SUFFIX_SET = getIgnoreSuffixSet(ignoreSuffix)
    SOURCE_IP_SET = getIgnoreSourceIpSet(ignoreSourceIp)
  }

  override def filter(value: String): Boolean = {

    val values = value.split("\\|", -1)
    if (values.length == 29 || values.length == 31) {

      val username = values(0)
      val suffix = values(6).split("\\?")(0).reverse.split("\\.", 2)(0).reverse

      USERNAME_SET.contains(username) &&
        !SUFFIX_SET.contains(suffix) &&
        !SOURCE_IP_SET.contains(values(3))
    } else {
      false
    }
  }

  def getIgnoreSourceIpSet(ignoreSourceIpStr: String): Set[String] = {
    ignoreSourceIpStr.split("\\|", -1).toSet
  }

  /**
   * 获取需要关注的用户名
   *
   * @return
   */
  def getFollowUsernameSet(fileSystemType: String, followUsernamePath: String): Set[String] = {

    val followUsernameSet = mutable.Set[String]()
    val fs = FileSystem.get(URI.create(fileSystemType), new org.apache.hadoop.conf.Configuration())
    val fsDataInputStream = fs.open(new Path(followUsernamePath))
    val bufferedReader = new BufferedReader(new InputStreamReader(fsDataInputStream))

    bufferedReader.lines()
    var line = bufferedReader.readLine()
    while (line != null) {
      followUsernameSet.add(line)
      line = bufferedReader.readLine()
    }

    try {
      bufferedReader.close()
    } catch {
      case exception: Exception => {
        logger.error("A Exception occurred", exception);
      }
    }

    try {
      fsDataInputStream.close()
    } catch {
      case exception: Exception => {
        logger.error("A Exception occurred", exception);
      }
    }

    try {
      fs.close()
    } catch {
      case exception: Exception => {
        logger.error("A Exception occurred", exception);
      }
    }

    followUsernameSet.toSet

  }


  /**
   * 获取后缀set
   *
   * @return
   */
  def getIgnoreSuffixSet(ignoreSuffixStr: String): Set[String] = {
    ignoreSuffixStr.split("\\|", -1).toSet
  }
}
