package cn.ffcs.is.mss.analyzer.flink.unknowRisk.funcation


import cn.ffcs.is.mss.analyzer.druid.model.scala.OperationModel
import cn.ffcs.is.mss.analyzer.druid.model.scala.OperationModel.REGEX
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}

import java.io.{BufferedReader, InputStreamReader}
import java.net.{URI, URLDecoder}
import java.util
import java.util.regex.Pattern
import scala.collection.mutable

/**
 * @ClassName UrlParameterUtil
 * @author hanyu
 * @date 2021/10/25 10:36
 * @description
 * @update [no][date YYYY-MM-DD][name][description]
 * */
object UnknownRiskUtil {

  /**
   * @return (url参数key集合，url参数值集合)
   * @author hanyu
   * @date 2021/10/25 11:26
   * @description 返回url中的参数
   * @update [no][date YYYY-MM-DD][name][description]
   */
  def getUrlParameterTup(url: String): (String, String) = {
    val parameterMap = new mutable.HashMap[String, String]()
    if (url != null && url.nonEmpty) {
      val strUrlParam = truncateUrlPage(url)
      val arrSplit = strUrlParam.split("[&]", -1)
      if (arrSplit.length > 1) {
        for (strSplit <- arrSplit) {
          val arrSplitEqual = strSplit.split("[=]", -1)
          //解析出键值
          if (arrSplitEqual.length > 1) {
            parameterMap.put(arrSplitEqual(0), arrSplitEqual(1))
          }
          else if (arrSplitEqual(0) ne "")
            parameterMap.put(arrSplitEqual(0), "")

        }
        (parameterMap.keySet.toString(), parameterMap.values.toString())
      } else {
        ("null", "null")
      }
    } else {
      ("null", "null")
    }

  }
  def getUrlParameterMap(url: String): mutable.HashMap[String, String] = {
    val parameterMap = new mutable.HashMap[String, String]()
    if (url != null && url.nonEmpty) {
      val strUrlParam = truncateUrlPage(url)
      val arrSplit = strUrlParam.split("[&]", -1)
      if (arrSplit.length > 1) {
        for (strSplit <- arrSplit) {
          val arrSplitEqual = strSplit.split("[=]", -1)
          //解析出键值
          if (arrSplitEqual.length > 1) {
            parameterMap.put(arrSplitEqual(0), arrSplitEqual(1))
          }
          else if (arrSplitEqual(0) ne "")
            parameterMap.put(arrSplitEqual(0), "")

        }
        parameterMap
      } else {
       null
      }
    } else {
      null
    }

  }

  /**
   *
   *
   * @return String
   * @author hanyu
   * @date 2021/10/25 11:27
   * @description 去掉url中的路径，留下请求参数部分
   * @update [no][date YYYY-MM-DD][name][description]
   */
  def truncateUrlPage(url: String): String = {
    var strAllParam = ""
    val urlString = getUrl(url)
    if (urlString.contains("?") && urlString.length > 1) {
      val urlSplit = urlString.split("[?]", -1)
      if (urlSplit.nonEmpty) {
        for (i <- urlSplit) {
          strAllParam = i
        }
      }
    }
    strAllParam
  }

  def getUrl(url: String): String = {

    if (url != null && url.nonEmpty) {
      try {
        return URLDecoder.decode(url, "utf-8").trim.toLowerCase
      } catch {
        case e: Exception =>
      }
      url.trim
    } else {
      ""
    }
  }

  /**
   *
   *
   * @return String
   * @author hanyu
   * @date 2021/11/15 10:23
   * @description 获取写入druid的前置数据（入kafka的数据） 写入过滤空
   *              operation - url
   *              value - hostValue
   * @update [no][date YYYY-MM-DD][name][description]
   */
  def getInputKafkavalue(operation: OperationModel, url: String, alertName: String, packageValue: String): String = {
    var inPutKafkaValue = ""
    var returnValue = ""
    if (packageValue != null && packageValue.length > 200) {
      returnValue = packageValue.substring(0, 200)
    } else {
      returnValue = packageValue
    }

    if (operation != null) {
      if (operation.usedPlace.split("\\|", -1).length > 1) {
        inPutKafkaValue = operation.userName + "|" + alertName + "|" + operation.timeStamp + "|" +
          operation.loginMajor + "|" + operation.loginSystem + "|" + operation.usedPlace.replaceAll("\\|", "^") + "|" +
          operation.isRemote + "|" + operation.sourceIp + "|" + operation.sourcePort + "|" +
          operation.destinationIp + "|" + operation.destinationPort + "|" + url + "|" +
          operation.httpStatus + "|" + operation.packageType + "|" + returnValue
      } else {
        inPutKafkaValue = operation.userName + "|" + alertName + "|" + operation.timeStamp + "|" +
          operation.loginMajor + "|" + operation.loginSystem + "|" + operation.usedPlace + "|" +
          operation.isRemote + "|" + operation.sourceIp + "|" + operation.sourcePort + "|" +
          operation.destinationIp + "|" + operation.destinationPort + "|" + url + "|" +
          operation.httpStatus + "|" + operation.packageType + "|" + returnValue
      }
      inPutKafkaValue
    } else {
      ""
    }

  }

}
