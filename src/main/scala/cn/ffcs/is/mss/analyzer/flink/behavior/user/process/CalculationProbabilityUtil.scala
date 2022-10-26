/*
 * @project mss
 * @company Fujian Fujitsu Communication Software Co., Ltd.
 * @author chenwei
 * @date 2019-12-06 11:00:03
 * @version v1.0
 * @update [no] [date YYYY-MM-DD] [name] [description]
 */
package cn.ffcs.is.mss.analyzer.flink.behavior.user.process

import java.net.URLDecoder

import cn.ffcs.is.mss.analyzer.druid.model.scala.OperationModel
import cn.ffcs.is.mss.analyzer.ml.kde.{Kernel, KernelDensity}
import cn.ffcs.is.mss.analyzer.ml.tree.server.{DefaultFuture, Request}
import cn.ffcs.is.mss.analyzer.utils.{JsonUtil, TimeUtil}
import cn.ffcs.is.mss.analyzer.utils2.IpUtil
import eu.bitwalker.useragentutils.UserAgent
import io.netty.buffer.Unpooled
import io.netty.channel.Channel

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

/**
 *
 * @author chenwei
 * @date 2019-12-06 11:00:03
 * @title CalculationProbabilityUtil
 * @update [no] [date YYYY-MM-DD] [name] [description]
 */
object CalculationProbabilityUtil {

  /**
   * 获取http参数类型
   *
   * @param param
   * @param channel
   * @return
   */
  def getHttpParamType(param: String, channel: Channel, timeOut: Long): String = {

    val request = new Request(Request.getNewUUID(), System.currentTimeMillis(), param)

    val defaultFuture = new DefaultFuture(channel, timeOut, request)

    DefaultFuture.FUTURES.put(request.id, defaultFuture)
    DefaultFuture.CHANNELS.put(request.id, channel)

    val requestJson = JsonUtil.toJson(request) + "\n"
    val messageByteBuf = Unpooled.buffer(requestJson.length)
    messageByteBuf.writeBytes(requestJson.getBytes)
    channel.writeAndFlush(messageByteBuf)



    try defaultFuture.get().responseStr
    catch {
      case e:Exception => "-1"
    }

  }

  /**
   * 将url进行解码，之后获取url?之前的部分
   *
   * @param URL
   * @return
   */
  def getPreUrl(URL: String): String = {

    try {
      URLDecoder.decode(URL, "utf-8").toLowerCase.split("\\?", 2)(0)
    } catch {
      case e: Exception => {
        ""
      }
    }

  }

  /**
   * 将url解析成[k,v]的形式
   *
   * @param URL
   * @return
   */
  def getUrlParamMap(URL: String): Map[String, String] = {

    val urlParamMap = mutable.Map[String, String]()

    val url = try {
      URLDecoder.decode(URL, "utf-8").toLowerCase
    } catch {
      case e: Exception => URL.toLowerCase()
    }


    if (url.contains("?")) {
      val params = url.split("\\?", 2)(1)

      params.split("&", -1).filter(t => t.contains("=")).foreach(t => {
        val (k, v) = t.split("=", 2) match {
          case Array(k, v) => (k, v)
        }
        urlParamMap.put(k, v)
      })
    }

    urlParamMap.toMap
  }

  /**
   * 计算url参数概率
   *
   * @param paramTypeMap
   * @param paramResultMap
   * @return
   */
  def getUrlParamProbability(paramTypeMap: mutable.Map[String, (String, String)],
                             paramResultMap: mutable.Map[String, mutable.Map[(String, String),
                               Long]]): Double = {

    var sum = 0.0
    var count = 0

    if (paramTypeMap != null && paramTypeMap.nonEmpty) {

      for ((k, (v, paramType)) <- paramTypeMap) {
        val paramMap = paramResultMap.getOrElse(k, mutable.Map[(String, String), Long]())


        var t = 0.0

        if (paramType.equals("0") || paramType.equals("1") || paramType.equals("2")
          || paramType.equals("4") || paramType.equals("6") || paramType.equals("7")
          || paramType.equals("8") || paramType.equals("9") || paramType.equals("14")
          || paramType.equals("16") || paramType.equals("17") || paramType.equals("21")
          || paramType.equals("22") || paramType.equals("23")) {
          t = getTypeFrequency(paramType, paramMap)
        } else if (paramType.equals("5")) {
          t = getFrequency(v, paramMap)
        } else if (paramType.equals("12")) {
          t = getFileProbability(v, paramMap)
        } else if (paramType.equals("19")) {
          t = getSimilarity(v, paramMap)
        } else {
          count = count - 1
        }

        sum = sum + t
        count = count + 1

      }

    }

    if (count > 0) {
      sum / count
    } else {
      1.0
    }

  }


  /**
   * 计算频率
   *
   * @param string
   * @param paramMap
   * @return
   */
  def getFrequency(string: String, paramMap: mutable.Map[(String, String), Long]): Double = {


    val sum = paramMap.values.sum.toDouble
    val v = paramMap.filter(t => string.equals(t._1._1)).values
    val frequency = if (v.isEmpty) 0.0 else v.head
    val max = if (paramMap.values.isEmpty) 0 else paramMap.values.max.toDouble

    if (sum == 0) {
      1.0
    } else if (max < 5) {
      1.0
    } else {
      frequency / max
    }

  }

  /**
   * 计算相似度
   *
   * @param string
   * @param paramMap
   * @return
   */
  def getSimilarity(string: String, paramMap: mutable.Map[(String, String), Long]): Double = {

    var sum = 0.0
    var count = 0L

    for ((param, number) <- paramMap) {

      sum = sum + getJaccard(string, param._1) * number
      count = count + number

    }

    if (count == 0) {
      1.0
    } else {
      sum / count.toDouble
    }

  }

  /**
   * 获取jaccard相似度
   * |A∩B|/|A∪B|
   *
   * @param string1
   * @param string2
   * @return
   */
  def getJaccard(string1: String, string2: String): Double = {

    val chars1 = string1.toCharArray.toSet
    val chars2 = string2.toCharArray.toSet

    (chars1 & chars2).size.toDouble / (chars1 ++ chars2).size.toDouble
  }

  //@todo
  //优化
  def getTypeFrequency(`type`: String, paramMap: mutable.Map[(String, String), Long]): Double = {

    val typeMap = mutable.Map[String, Long]()

    for ((param, num) <- paramMap) {
      typeMap.put(param._2, typeMap.getOrElse(param._2, 0L) + num)
    }


    val sum = typeMap.values.toSet.sum.toDouble
    if (sum == 0) {
      return 1.0
    }
    val min = typeMap.values.toSet.min.toDouble
    val max = typeMap.values.toSet.max.toDouble
    val n = typeMap.getOrElse(`type`, 0L)
    if (min == max) {
      return 1.0
    }

    if (n < min) {
      return 0.0
    }

    if (n == min) {
      return n / sum
    }

    (n - min) / (max - min)
  }

  //@todo
  //优化
  /**
   * 计算文件名类型的概率
   *
   * @param string
   * @param paramMap
   * @return
   */
  def getFileProbability(string: String, paramMap: mutable.Map[(String, String), Long]): Double = {
    val typeMap = mutable.Map[String, Long]()


    for ((param, num) <- paramMap) {
      typeMap.put(param._1.reverse.split("\\.", 2)(0), typeMap.getOrElse(param._1.reverse.split
      ("\\.", 2)(0), 0L) + num)
    }

    val sum = typeMap.values.toSet.sum.toDouble
    if (sum == 0) {
      return 1.0
    }
    val min = typeMap.values.toSet.min.toDouble
    val max = typeMap.values.toSet.max.toDouble
    val n = typeMap.getOrElse(string.reverse.split("\\.", 2)(0), 0L)
    if (min == max) {
      return 1.0
    }

    if (n < min) {
      return 0.0
    }

    if (n == min) {
      return n / sum
    }


    ((n - min) / (max - min) + getSimilarity(string, paramMap)) / 2


  }

  //@todo
  //优化

  /**
   * 计算数字类型的概率
   *
   * @param string
   * @param paramMap
   * @return
   */
  def getNumberProbability(string: String, paramMap: mutable.Map[(String, String),
    ArrayBuffer[Double]]): Double = {
    val number = string.toDouble

    if (paramMap.isEmpty) {
      return 1.0
    }

    val arrayBuffer = mutable.ArrayBuffer[Double]()
    for (elem <- paramMap) {
      for (i <- 0 until elem._1._1.toInt) {
        arrayBuffer.append(elem._1._1.toDouble)
      }
    }

    val resultArrayBuffer = mutable.ArrayBuffer[Double]()


    val start = arrayBuffer.min.toInt.min(number.toInt)
    val end = arrayBuffer.max.toInt.max(number.toInt)
    var step = 1
    if (end - start > 1000) {
      step = (end - start) / 100
    }
    for (i <- start - step until end + step by step) {

      resultArrayBuffer.append(KernelDensity.scoreSamplesCircle(kernel = Kernel.Gaussian,
        arrayBuffer.toArray, i, step * 15, start - step * 2, end + step * 2))

    }


    val max = if (resultArrayBuffer.isEmpty) 0 else resultArrayBuffer.max
    val min = 0
    val result = KernelDensity.scoreSamplesCircle(kernel = Kernel.Gaussian,
      arrayBuffer.toArray, number, step * 15, start - step * 2, end + step * 2)

    if (max == min) {
      return 1.0
    }

    if (result == min) {
      return result / max
    }

    (result - min) / (max - min)

  }

  /**
   * 计算单位时间内访问系统个数的概率
   *
   * @param count
   * @param systemCountMap
   * @return
   */
  def getSystemCountProbability(count: Int, systemCountMap: mutable.Map[String, String]): Double = {

    if (systemCountMap.isEmpty) {
      return 0.0
    }

    var sum = 0.0
    var num = 0.0

    systemCountMap.foreach(t => {

      sum += t._2.toInt
      if (count - 3 <= t._1.toInt) {
        num += t._2.toInt
      }
    })

    num / sum

  }

  /**
   * 计算http状态码概率
   *
   * @param httpStatusMap
   * @return
   */
  def getHttpStatusProbability(httpStatusMap: mutable.Map[String, mutable.ArrayBuffer[Long]])
  : Double = {
    var count = 0L
    var sum = 0L
    for (status <- httpStatusMap.keySet) {
      if (status.length > 0 && (status.charAt(0) == '2' || status.charAt(0) == '3')) {
        count += httpStatusMap(status).length
      }
      sum += httpStatusMap(status).length
    }

    count.toDouble / sum.toDouble
  }

  /**
   * 获取归一化的结果
   *
   * @param value
   * @param max
   * @param min
   * @return
   */
  def getNormalization(value: Double, max: Double, min: Double): Double = {
    if (min == max || max <= value) {
      1.0
    } else {
      (value - min) / (max - min)
    }
  }

  /**
   * 将Long类型数组转换成Double类型数组
   *
   * @param arrayBuffer
   * @return
   */
  def longArrayToDoubleArray(arrayBuffer: Array[Long]): Array[Double] = {

    val array = new Array[Double](arrayBuffer.length)
    for (i <- array.indices) {
      array(i) = arrayBuffer(i)
    }
    array
  }

  def getTransformTimestampArrayBuffer(timestampArrayBuffer: Array[Long]): Array[Long] = {
    val transformArrayBuffer = mutable.ArrayBuffer[Long]()

    timestampArrayBuffer.foreach(t => transformArrayBuffer.append(timeStampTransform(t)))
    transformArrayBuffer.toArray
  }

  /**
   * 将IPV4转换成long类型
   *
   * @param sourceIp
   * @return
   */
  def sourceIpTransform(sourceIp: String): Long = {
    IpUtil.dottedDecimalNotationIPV4ToLong(sourceIp)
  }

  /**
   * 获取该时间戳是北京时间中每天的第几分钟
   *
   * @param timeStamp
   * @return
   */
  def timeStampTransform(timeStamp: Long): Long = {
    ((timeStamp + 8 * TimeUtil.HOUR_MILLISECOND) / TimeUtil.MINUTE_MILLISECOND) % (TimeUtil
      .DAY_MILLISECOND / TimeUtil.MINUTE_MILLISECOND)
  }

  /**
   * 获取当前时间的是北京时间第几小时
   *
   * @param timestamp
   * @param offsetHour
   * @return
   */
  def getHourTimestamp(timestamp: Long, offsetHour: Int): Long = {
    (timestamp / 1000 / 60 / 60 + 8 + offsetHour) % 24
  }

  /**
   * 获取用户名
   *
   * @param line
   * @param operationModel
   * @return
   */
  def getUsername(line: String, operationModel: OperationModel): String = {
    operationModel.userName
  }

  /**
   * 获取登录的时间点
   *
   * @param line
   * @param operationModel
   * @return
   */
  def getTimeStamp(line: String, operationModel: OperationModel): Long = {
    operationModel.timeStamp
  }

  /**
   * 获取登录的源ip
   *
   * @param line
   * @param operationModel
   * @return
   */
  def getSourceIp(line: String, operationModel: OperationModel): String = {
    operationModel.sourceIp
  }

  /**
   * 获取登录地
   *
   * @param line
   * @param operationModel
   * @return
   */
  def getPlace(line: String, operationModel: OperationModel): String = {
    operationModel.loginPlace
  }

  /**
   * 获取源IP
   *
   * @param line
   * @param operationModel
   * @return
   */
  def getSourcePort(line: String, operationModel: OperationModel): Int = {
    try operationModel.sourcePort.toInt
    catch {
      case e: Exception => 0
    }
  }

  /**
   * 获取目的IP
   *
   * @param line
   * @param operationModel
   * @return
   */
  def getDestinationIp(line: String, operationModel: OperationModel): String = {
    operationModel.destinationIp
  }

  /**
   * 获取目的port
   *
   * @param line
   * @param operationModel
   * @return
   */
  def getDestinationPort(line: String, operationModel: OperationModel): Int = {
    try operationModel.destinationPort.toInt
    catch {
      case e: Exception => 0
    }
  }

  /**
   * 获取host
   *
   * @param line
   * @param operationModel
   * @return
   */
  def getHost(line: String, operationModel: OperationModel): String = {
    val values = line.split("\\|", -1)
    if (values.length > 5) values(5) else ""
  }

  /**
   * 获取前一跳连接
   *
   * @param line
   * @param operationModel
   * @return
   */
  def getReference(line: String, operationModel: OperationModel): String = {
    val values = line.split("\\|", -1)
    if (values.length > 8) values(8) else ""
  }

  /**
   * 获取时候下载
   *
   * @param line
   * @param operationModel
   * @return
   */
  def getIsDownload(line: String, operationModel: OperationModel): String = {
    operationModel.isDownload
  }

  /**
   * 获取是否下载成功
   *
   * @param line
   * @param operationModel
   * @return
   */
  def getIsDownSuccess(line: String, operationModel: OperationModel): String = {
    operationModel.isDownSuccess
  }

  /**
   * 获取下载文件大小
   *
   * @param line
   * @param operationModel
   * @return
   */
  def getDownFileSize(line: String, operationModel: OperationModel): Long = {
    operationModel.downFileSize
  }

  /**
   * 获取下载文件名
   *
   * @param line
   * @param operationModel
   * @return
   */
  def getDownFileName(line: String, operationModel: OperationModel): String = {
    operationModel.downFileName
  }

  /**
   * 获取post表单值
   *
   * @param line
   * @param operationModel
   * @return
   */
  def getFormValue(line: String, operationModel: OperationModel): String = {
    val values = line.split("\\|", -1)
    if (values.length > 30) values(30) else ""
  }

  /**
   * 获取上行流量
   *
   * @param line
   * @param operationModel
   * @return
   */
  def getInputOctets(line: String, operationModel: OperationModel): Long = {
    operationModel.inputOctets
  }

  /**
   * 获取下行流量
   *
   * @param line
   * @param operationModel
   * @return
   */
  def getOutputOctets(line: String, operationModel: OperationModel): Long = {
    operationModel.outputOctets
  }

  /**
   * 获取使用的操作系统
   *
   * @param line
   * @param operationModel
   * @return
   */
  def getOperationSystem(line: String, operationModel: OperationModel): String = {
    val userAgentString = getUserAgent(line, operationModel)
    val userAgent = UserAgent.parseUserAgentString(userAgentString)

    userAgent.getOperatingSystem.getName
    userAgent.getBrowser.getName

    val operatingSystemName = try {
      userAgent.getOperatingSystem.getName
    } catch {
      case e: Exception => ""
    }
    operatingSystemName
  }

  /**
   * 获取使用的浏览器
   *
   * @param line
   * @param operationModel
   * @return
   */
  def getBrowser(line: String, operationModel: OperationModel): String = {
    val userAgentString = getUserAgent(line, operationModel)

    val userAgent = UserAgent.parseUserAgentString(userAgentString)

    val browserName = try {
      userAgent.getBrowser.getName
    } catch {
      case e: Exception => ""
    }
    browserName
  }

  /**
   * 获取访问的url
   *
   * @param line
   * @param operationModel
   * @return
   */
  def getUrl(line: String, operationModel: OperationModel): String = {
    val values = line.split("\\|", -1)
    if (values.length > 6) values(6) else ""
  }

  /**
   * 获取访问的业务系统
   *
   * @param line
   * @param operationModel
   * @return
   */
  def getSystem(line: String, operationModel: OperationModel): String = {
    operationModel.loginSystem
  }

  /**
   * 获取http 状态码
   *
   * @param line
   * @param operationModel
   * @return
   */
  def getHttpStatus(line: String, operationModel: OperationModel): String = {
    val values = line.split("\\|", -1)
    if (values.length > 13) values(13) else ""
  }

  /**
   * 获取user agent
   *
   * @param line
   * @param operationModel
   * @return
   */
  def getUserAgent(line: String, operationModel: OperationModel): String = {
    val values = line.split("\\|", -1)
    if (values.length > 9) values(9) else ""
  }

  /**
   * 获取在redis中的key
   *
   * @param keyFormat
   * @param value
   * @return
   */
  def getRedisKey(keyFormat: String, value: Any*): String = {
    keyFormat.format(value: _*)
  }

  /**
   * 获取在redis中的field
   *
   * @param fieldFormat
   * @param value
   * @return
   */
  def getRedisField(fieldFormat: String, value: Any*): String = {
    fieldFormat.format(value: _*)
  }

  /**
   * 保留指定位数的double类型的值(数据过大,保留位数过长会导致数据上溢)
   *
   * @param double
   * @param length
   * @return
   */
  def formatDouble(double: Double, length: Int): Double = {
    val t = Math.pow(10, length)
    (double * t).toInt / t
  }
}
