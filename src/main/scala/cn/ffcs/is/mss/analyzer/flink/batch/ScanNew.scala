/*
 * @project mss
 * @company Fujian Fujitsu Communication Software Co., Ltd.
 * @author chenwei
 * @date 2020-04-03 14:28:50
 * @version v1.0
 * @update [no] [date YYYY-MM-DD] [name] [description]
 */
package cn.ffcs.is.mss.analyzer.flink.batch

import java.io.{BufferedReader, InputStreamReader}
import java.{lang, util}
import java.net.URI
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.regex.Pattern
import java.util.{Calendar, Date, GregorianCalendar, Properties}

import cn.ffcs.is.mss.analyzer.bean.{ErrorWebLengthEntity, WebShellScanAlarmFailEntity, WebShellScanAlarmSucceedEntity}
import cn.ffcs.is.mss.analyzer.flink.sink.DataSetMySQLSink
import cn.ffcs.is.mss.analyzer.utils.{C3P0Util, Constants, IniProperties, SQLHelper}
import org.apache.flink.api.common.functions._
import org.apache.flink.api.common.io.FilePathFilter
import org.apache.flink.api.common.operators.Order
import org.apache.flink.api.java.io.TextInputFormat
import org.apache.flink.api.scala._
import org.apache.flink.configuration.Configuration
import org.apache.flink.core.fs.{FileSystem, Path}
import org.apache.flink.api.scala._
import org.apache.flink.util.Collector

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.util.Random
import scala.util.control.Breaks._

/**
 *
 * @author chenwei
 * @date 2020-04-03 14:28:50
 * @title ScanNew
 * @update [no] [date YYYY-MM-DD] [name] [description]
 */
object ScanNew {

  def main(args: Array[String]): Unit = {

    val whiteUrlPath = "hdfs://10.142.82.181:8020/chenw/webshell/whiteUrl.txt"
    val todayAllDataPath = "hdfs://10.142.82.181:8020/user/hive/warehouse/mssflow"
    val historyWebShellUrlPath = ""

    val left = 20200406
    val right = 20200408

    val fileInputFormatAllData = new TextInputFormat(new Path(todayAllDataPath))
    fileInputFormatAllData.setNestedFileEnumeration(true)
    fileInputFormatAllData.setFilesFilter(new FilePathFilter {
      override def filterPath(filePath: Path): Boolean = {
        //如果是目录不过滤
        if ("^time=.*".r.findAllIn(filePath.getName).nonEmpty) {
          val time = filePath.getName.substring(5, 13).toLong
          // && time <= 20190910
          if (!(time >= left && time < right)) {
            //time >= left && time < right
            true
          } else {
            false
          }
        } else {
          //如果是.txt文件
          if (".txt$".r.findAllIn(filePath.getName).nonEmpty) {
            false
          } else {
            true
          }
        }
      }
    })

    val env = ExecutionEnvironment.getExecutionEnvironment

    //今日数据
    val todayAllDataSet = env.readFile(fileInputFormatAllData, todayAllDataPath).setParallelism(1)
      .filter(line => {
        line.split("\\|", -1).length == 31
      }).setParallelism(1)

    //历史白名单数据集
    val whiteUrlDataSet = env.readTextFile(whiteUrlPath, "UTF-8").setParallelism(1)
    //历史webShell url的路径
    //val historyWebShellUrDataSet = env.readTextFile(whiteUrlPath, "UTF-8")

    //今日白名单数据
    val todayWhiteUrlDataSet = todayAllDataSet.filter(line => {
      val values = line.split("\\|", -1)
      val refer = values(8)
      val url = values(6)
      //保留refer不为空，并且refer与url不相同的数据为今天新增的白名单
      refer != null && refer.length > 0 && !getPreUrl(url).equals(getPreUrl(refer))
    }).setParallelism(1)
      .map(line => {
      getPreUrl(line.split("\\|", -1)(6))
    }).setParallelism(1)


    val todayAllDataReduceGroup = todayAllDataSet.filter(line => {
      val values = line.split("\\|", -1)
      val refer = values(8)
      val url = values(6)
      //保留refer为空或者refer与url相同的数据
      refer == null || refer.length == 0 || getPreUrl(url).equals(getPreUrl(refer))
    }).setParallelism(1)
      .map(line => {
      val values = line.split("\\|", -1)
      //用户名
      val userName = values(0)
      //src ip
      val srcIp = values(3)
      //url
      val url = values(6)
      //userAgent
      val userAgent = values(9)
      //时间戳
      val timeStamp = values(10).trim.toLong
      //url前缀
      val preUrl = getPreUrl(url)
      //页面长度
      val length = values(16)
      //http status
      val httpStatus = values(13)
      //访问类型
      val visitType = values(29)
      //访问类型
      val formValue = values(30)

      (preUrl, Set[(String, String)]((userName, srcIp)), Array[(Long, String, String, String,
        String, String, String, String, String)]((timeStamp, userName, srcIp, url, userAgent,
        length,

        httpStatus, visitType, formValue)))
    }).setParallelism(1)
      .groupBy(0)
      .reduceGroup(new ReduceTodayOriginalData)
      .map(tuple => (tuple._1, tuple._3, 0)).setParallelism(1)


    val todayAllWhiteUrlDataSet = todayAllDataReduceGroup.filter(tuple => tuple._2.isEmpty)
      .setParallelism(1)
      .map(_._1).setParallelism(1)
      .union(todayWhiteUrlDataSet).setParallelism(1)
      .map(t => (t, 0)).setParallelism(1)
      .distinct(0)
      .map(_._1).setParallelism(1)

    val allWhiteUrlDataSet = whiteUrlDataSet.union(todayAllWhiteUrlDataSet).setParallelism(1)

    val detectWebShell = allWhiteUrlDataSet.map(tuple => (tuple, Array[(Long, String, String,
      String, String, String, String, String, String)](), 1))
      .union(todayAllDataReduceGroup.filter(tuple => !tuple._2.isEmpty)).setParallelism(1)
      .groupBy(0)
      .reduceGroup(new ReduceTodayWhiteData)


    detectWebShell.filter(_._3 != 1).setParallelism(1)
      .map(_._2).setParallelism(1)
      .flatMap(t => t).setParallelism(1)
      .writeAsText("hdfs://10.142.82.181:8020/chenw/webshell/webshell.txt", FileSystem.WriteMode.OVERWRITE).setParallelism(1)

    allWhiteUrlDataSet.union(detectWebShell.filter(_._3 == 1).map(_._1))
      .writeAsText("hdfs://10.142.82.181:8020/chenw/webshell/whiteUrlNew.txt", FileSystem.WriteMode.OVERWRITE).setParallelism(1)

    todayAllDataReduceGroup.filter(tuple => !tuple._2.isEmpty).setParallelism(1)
      .flatMap(t => t._2).setParallelism(1)
      .writeAsText("hdfs://10.142.82.181:8020/chenw/webshell/webshell-test.txt", FileSystem.WriteMode.OVERWRITE).setParallelism(1)


    env.execute()


  }

  class ReduceTodayOriginalData extends RichGroupReduceFunction[(String, Set[(String, String)],
    Array[(Long, String, String, String, String, String, String, String, String)]), (String, Set[
    (String, String)], Array[(Long,
    String, String, String, String, String, String, String, String)])] {


    val userSize = 3

    override def open(parameters: Configuration): Unit = {

    }

    override def reduce(values: lang.Iterable[(String, Set[(String, String)], Array[(Long,
      String, String, String, String, String, String, String, String)])], out: Collector[(String,
      Set[(String, String)], Array[
      (Long, String, String, String, String, String, String, String, String)])]): Unit = {


      var preUrl = ""
      val userSet = mutable.Set[(String, String)]()
      val allData = mutable.ArrayBuffer[(Long, String, String, String, String, String, String,
        String, String)]()
      for (value <- values) {
        if (preUrl.length == 0) {
          preUrl = value._1
        }

        if (value._2.isEmpty) {
          out.collect(preUrl, Set[(String, String)](), Array[(Long, String, String, String,
            String, String, String, String, String)]())
          return
        }

        for (tuple <- value._2) {
          userSet.add(tuple)

          if (userSet.size > userSize) {
            out.collect(preUrl, Set[(String, String)](), Array[(Long, String, String, String,
              String, String, String, String, String)]())
            return
          }
        }

        for (tuple <- value._3) {
          allData.append(tuple)
        }
      }

      out.collect(preUrl, userSet.toSet, allData.toArray)

    }
  }


  class ReduceTodayWhiteData extends RichGroupReduceFunction[(String, Array[(Long, String, String,
    String, String, String, String, String, String)], Int), (String, Array[(Long, String, String,
    String, String, String, String, String, String)], Int)] {
    override def reduce(values: lang.Iterable[(String, Array[(Long, String, String, String, String, String, String, String, String)], Int)], out: Collector[(String, Array[(Long, String, String, String, String, String, String, String, String)], Int)]): Unit = {

      val url = ""
      val arrayBuffer = mutable.ArrayBuffer[(Long, String, String, String, String, String, String, String, String)]()

      val userSet = mutable.Set[String]()

      for (value <- values) {
        if (value._3 == 1 || userSet.size > 3) {
          out.collect((url, Array[(Long, String, String, String, String, String, String, String,
            String)](), 1))
          return
        }

        for (tuple <- value._2) {
          if (tuple._2.length == 0) {
            userSet.add(tuple._3)
          } else {
            userSet.add(tuple._2)
          }
          arrayBuffer.append(tuple)
        }
      }

      (url, arrayBuffer.toArray, 0)

    }
  }


  /**
   * 获取url前缀
   *
   * @param url
   * @return
   */
  def getPreUrl(url: String): String = {

    url.split("\\?", -1)(0).split(";", -1)(0)

  }
}
