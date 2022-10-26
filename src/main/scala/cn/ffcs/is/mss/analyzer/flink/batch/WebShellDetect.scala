package cn.ffcs.is.mss.analyzer.flink.batch

import java.text.SimpleDateFormat
import java.util.{Calendar}

import cn.ffcs.is.mss.analyzer.utils.IniProperties
import org.apache.flink.api.common.functions.RichFilterFunction
import org.apache.flink.api.common.io.FilePathFilter
import org.apache.flink.api.java.io.TextInputFormat
import org.apache.flink.api.scala.ExecutionEnvironment
import org.apache.flink.configuration.Configuration
import org.apache.flink.core.fs.{FileSystem, Path}
import org.apache.flink.api.scala._

import scala.collection.mutable

object WebShellDetect {
  def main(args: Array[String]): Unit = {
    val confProperties = new IniProperties(args(0))
    val filePath = "hdfs://10.142.82.181:8020/user/hive/warehouse/mssflow"
    val resultPath = "hdfs://10.142.82.181:8020/403mss-4"
    val env = ExecutionEnvironment.getExecutionEnvironment

    val fileInputFormat = new TextInputFormat(new Path(filePath))
    fileInputFormat.setNestedFileEnumeration(true)

    fileInputFormat.setFilesFilter(new FilePathFilter {
      override def filterPath(filePath: Path): Boolean = {
        //如果是目录不过滤
        if ("^time=.*".r.findAllIn(filePath.getName).nonEmpty) {
          val time = filePath.getName.substring(5, 13).toLong
          // && time <= 20190910
          if (!(time >= 20190401 && time <= 20190430)) {
            println(filePath)
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

    val hdfsLines = env.readFile(fileInputFormat, filePath).setParallelism(7)
      .filter(new WebShellFilter).setParallelism(9)
//      .map(t => {
//        val splits = t.split("\\|", -1)
//        val url = splits(6)
//        val topUrl = url.split("\\?")(0)
//        (topUrl, url, splits(0), splits(13))
//      })
//      .groupBy(0)
//      .reduceGroup(it => {
//        var topUrl = ""
//        var count = 0L
//        val userMap = new mutable.HashMap[String, Int]()
//        while (it.hasNext) {
//          val tup = it.next()
//          topUrl = tup._1
//          count += 1
//          val userName = tup._3
//
//          var userCount = userMap.getOrElse(userName, 0)
//          userCount += 1
//          userMap.update(userName, userCount)
//
//        }
//        var lastField = ""
//        for ((k, v) <- userMap) {
//          lastField += k + "=" + v + "|"
//        }
//        (topUrl, count, lastField)
//      }).filter(t => {
//      val splits = t._3.split("\\|")
//      splits.size == 1 && splits(0).split("=")(1).toInt > 10
//    })

      .writeAsText(resultPath, FileSystem.WriteMode.OVERWRITE).setParallelism(1)

    env.execute("webshell4")

  }

  class WebShellFilter extends RichFilterFunction[String] {
    val keySet = new mutable.HashSet[String]()

    override def open(parameters: Configuration): Unit = {
      val filterKeys = "css|png|js|jepg|icon|jpg|gif|ico|do|xlsx|properties|dwr|action|xls"
      val keys = filterKeys.split("\\|", -1)
      keys.foreach(i => keySet.add(i))
    }


    override def filter(value: String): Boolean = {
      val splits = value.split("\\|", -1)
      try {
        val topUrl = splits(6).split("\\?", -1)(0)
        val words = topUrl.reverse.split("\\.", 2)
        val suffix = words(0).reverse
        //        !keySet.contains(suffix) && splits(8).length == 0 && splits(6).length > 0 && splits(0).length > 0 && splits(0).contains("@")
        splits(13).toInt == 403
        //        splits(0).length>0 && !splits(0).contains("@")

      } catch {
        case e: Exception => false
      }
    }
  }

  /**
    * 获得n天前的日期
    * 返回 类型 20190823
    *
    * @param day
    */
  def getNDays(calendar: Calendar, day: Int): Int = {

    calendar.add(Calendar.DATE, day)
    val date = calendar.getTime
    val df = new SimpleDateFormat("yyyyMMdd")
    df.format(date).toInt
  }

}
