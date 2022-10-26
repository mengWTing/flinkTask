/*
 * @project mss
 * @company Fujian Fujitsu Communication Software Co., Ltd.
 * @author chenwei
 * @date 2019-12-05 14:17:34
 * @version v1.0
 * @update [no] [date YYYY-MM-DD] [name] [description]
 */
package cn.ffcs.is.mss.analyzer.flink.behavior.user.process

import cn.ffcs.is.mss.analyzer.utils.libInjection.sql.Libinjection
import cn.ffcs.is.mss.analyzer.utils.libInjection.xss.XSSInjectionUtil

import scala.util.Random

/**
 *
 * @author chenwei
 * @date 2019-12-05 14:17:34
 * @title CalculationWarnTypeUtil
 * @update [no] [date YYYY-MM-DD] [name] [description]
 */
object CalculationWarnTypeUtil {

  def getWarnType(username: String, timestamp: Long, lastTimeStamp: Long, timeThreshold: Long,
                  timeProbability: Double, probabilityThreshold: Double, url: String,
                  sourceIp: String, place: String, system: String, status: Int,
                  libinjection: Libinjection, xSSInjectionUtil: XSSInjectionUtil,
                  robotIpSet: Set[String], idPLaceMap: Map[String, Set[String]],
                  placeSet: Set[String], placeThreshold: Int
                 ): Long = {


    var result = 0L
    result += isFrequentlySwitchLoginLocations(placeSet, placeThreshold, 0)
    result += isRemoteLogin(username, place, idPLaceMap, 1)
    result += isNotUsedTimeLogin(timeProbability, probabilityThreshold, 2)
    result += isFirstLogin(lastTimeStamp, 3)
    result += isNotLoginForALongTime(timestamp, lastTimeStamp, timeThreshold, 4)
    result += isLeavingUser(status, 5)
    result += isSqlInjection(url, libinjection, 6)
    result += isXssAttack(url, xSSInjectionUtil, 7)
    result += isWebShell(url, 8)
    result += isDirectoryTraversal(url, 9)
    //result += isRobot(sourceIp, robotIpSet, 10)
    result += isUnknownSystem(system, 11)

    result
  }

  /**
   * 判断是否是频繁切换登录地
   * 如果指定时间内的登录地大于阈值，则判断是频繁切换登录地
   * 返回事件类型编码3
   *
   * @param placeSet
   * @param placeThreshold
   * @return
   */
  def isFrequentlySwitchLoginLocations(placeSet: Set[String], placeThreshold: Int, index: Int)
  : Long = {
    if (placeSet.size > placeThreshold) {
      1L << index
    } else {
      0L
    }
  }

  /**
   * 判断是否在异地登录
   * 如果登录地不在指定的登录地中
   * 返回事件类型编码5
   *
   * @param username
   * @param place
   * @param idPLaceMap
   * @return
   */
  def isRemoteLogin(username: String, place: String, idPLaceMap: Map[String, Set[String]],
                    index: Int): Long = {


    if (idPLaceMap == null) {
      0L
    } else {
      if (!idPLaceMap.getOrElse(username, Set[String]()).contains(place)) {
        1L << index
      } else {
        0L
      }
    }
  }

  /**
   * 判断是否是不在常用时间访问
   * 如果时间概率小于阈值
   * 返回事件类型编码7
   *
   * @param timeProbability
   * @param probabilityThreshold
   * @return
   */
  def isNotUsedTimeLogin(timeProbability: Double, probabilityThreshold: Double, index: Int): Long = {
    if (timeProbability < probabilityThreshold) {
      1L << index
    } else {
      0L
    }
  }

  /**
   * 判断是否是第一次登录
   *
   * @param lastTimeStamp
   * @return
   */
  def isFirstLogin(lastTimeStamp: Long, index: Int): Long = {

    if (lastTimeStamp == 0L) {
      1L << index
    } else {
      0L
    }

  }

  /**
   * 判断是否长时间未访问
   *
   * @param timestamp
   * @param lastTimeStamp
   * @param timeThreshold
   * @return
   */
  def isNotLoginForALongTime(timestamp: Long, lastTimeStamp: Long, timeThreshold: Long,
                             index: Int): Long = {
    if (lastTimeStamp != 0L && timestamp - lastTimeStamp > timeThreshold) {
      1L << index
    } else {
      0L
    }
  }

  /**
   * 判断是否为离职用户
   *
   * @return
   */
  def isLeavingUser(status: Int, index: Int): Long = {
    if (status != 1) {
      1L << index
    } else {
      0L
    }
  }

  /**
   * 判断是否为sql注入
   *
   * @param url
   * @return
   */
  def isSqlInjection(url: String, libinjection: Libinjection, index: Int): Long = {
    if (libinjection.libinjection_sqli(url)) {
      1L << index
    } else {
      0L
    }
  }

  /**
   * 判断是否为xss攻击
   *
   * @param url
   * @return
   */
  def isXssAttack(url: String, xSSInjectionUtil: XSSInjectionUtil, index: Int): Long = {

    //if (xSSInjectionUtil.checkXss(url) == 1) {
    //  1
    //} else {
    //  0
    //} << index
    0L
  }

  // TODO:
  /**
   * 判断是否为Webshell
   *
   * @param url
   * @return
   */
  def isWebShell(url: String, index: Int): Long = {

    if (false) {
      1L << index
    } else {
      0L
    }
  }

  /**
   * 判断是否为目录遍历攻击
   *
   * @param url
   * @return
   */
  def isDirectoryTraversal(url: String, index: Int): Long = {

    if (".*../../../../.*".r.findFirstIn(url).isDefined) {
      1L << index
    } else {
      0L
    }
  }

  /**
   * 判断是否为机器人访问
   *
   * @param sourceIp
   * @param robotIpSet
   * @return
   */
  def isRobot(sourceIp: String, robotIpSet: Set[String], index: Int): Long = {
    if (robotIpSet != null && robotIpSet.contains(sourceIp)) {
      1L << index
    } else {
      0L
    }
  }


  /**
   * 判断是否为未知系统
   *
   * @param system
   * @return
   */
  def isUnknownSystem(system: String, index: Int): Long = {
    if ("未知系统".equals(system)) {
      1L << index
    } else {
      0L
    }
  }

}
