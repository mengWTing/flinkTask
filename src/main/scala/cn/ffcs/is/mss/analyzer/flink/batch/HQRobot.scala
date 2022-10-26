package cn.ffcs.is.mss.analyzer.flink.batch

import java.text.SimpleDateFormat
import java.util
import java.util.Date

import org.apache.commons.lang.StringUtils
import org.apache.flink.api.common.functions.RichFilterFunction
import org.apache.flink.api.common.io.FilePathFilter
import org.apache.flink.api.common.operators.Order
import org.apache.flink.api.java.io.TextInputFormat
import org.apache.flink.api.scala.ExecutionEnvironment
import org.apache.flink.core.fs.{FileSystem, Path}
import org.apache.flink.api.scala._
import org.apache.flink.configuration.Configuration

import scala.collection.mutable

object HQRobot {
  def main(args: Array[String]): Unit = {
    val filePath = "hdfs://10.142.82.181:8020/user/hive/warehouse/mssflow"
    val resultPath = "hdfs://10.142.82.181:8020/containUrl"

    val env = ExecutionEnvironment.getExecutionEnvironment

    val fileInputFormat = new TextInputFormat(new Path(filePath))
    fileInputFormat.setNestedFileEnumeration(true)

    fileInputFormat.setFilesFilter(new FilePathFilter {
      override def filterPath(filePath: Path): Boolean = {
        //如果是目录不过滤
        if ("^time=.*".r.findAllIn(filePath.getName).nonEmpty) {
          val time = filePath.getName.substring(5, 13).toLong
          // && time <= 20190910
          //          if (!(time >= 20191018 && time <= 20191108)) {
          //            println(filePath)
          //            true
          //          } else {
          //            false
          //          }
          //                    time != 20191028
          !(time >= 20191018 && time <= 20191108)
        } else {
          //如果是.txt文件
          //          ".txt$".r.findAllIn(filePath.getName).isEmpty
          if (".txt$".r.findAllIn(filePath.getName).nonEmpty) {
            false
          } else {
            true
          }
        }
      }
    })


    val hdfsLines = env.readFile(fileInputFormat, filePath).setParallelism(4)
      .filter(new hqFilterFunc).setParallelism(3)
      //      .map(t => {
      //        val splits = t.split("\\|", -1)
      //        //        val userName = splits(0)
      //        val url = splits(6)
      //        val username = splits(0)
      //        val srcIp = splits(3)
      //        val time = splits(10).toLong
      //        val date = new Date(time)
      //        //        val time = splits(10)
      //        //        val host = splits(5)
      //        val sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
      //        username + "," + srcIp + "," + sdf.format(date) + "," + url
      //      }).setParallelism(3)
      //      .groupBy(0)
      //      .sortGroup(1, Order.ASCENDING)
      //      .reduceGroup(it => {
      //        var userName = ""
      //        var before = 0L
      //        val map = new mutable.HashMap[Long, util.ArrayList[String]]()
      //        while (it.hasNext) {
      //          val tuple = it.next()
      //          userName = tuple._1.split("\\|", -1)(0)
      //          val url = tuple._3
      //          val time = tuple._2.toLong / 1000 * 1000
      //          val width = time - before
      //          if (width != 0L && width < 1567958400000L) {
      //            val list = map.getOrElse(width, new util.ArrayList[String]())
      //            list.add(url)
      //            map.update(width, list)
      //          }
      //          before = time
      //
      //        }
      //        var result = userName + ","
      //        for ((k, v) <- map) {
      //          result += k + "," + StringUtils.join(v, "|") + "\n"
      //        }
      //        result
      //      })
      .writeAsText(resultPath, FileSystem.WriteMode.OVERWRITE).setParallelism(1)

    env.execute("keyi")

  }

  class hqFilterFunc extends RichFilterFunction[String] {
    val maliciousIp = new mutable.HashSet[String]()
    val keySet = new mutable.HashSet[String]()

    override def open(parameters: Configuration): Unit = {
      val filterKeys = "css|png|js|jepg|icon|jpg|gif|ico"
      val keys = filterKeys.split("\\|", -1)
      keys.foreach(i => keySet.add(i))
    }

    override def filter(value: String): Boolean = {
      val splits = value.split("\\|", -1)
      try {
        //        val words = splits(6).reverse.split("\\.", 2)
        //        val suffix = words(0).reverse
        //        val timestamp = splits(10).toLong
        //        val time = new Date(timestamp)

        val username = splits(0)
        val srcIp = splits(3)
        val url = splits(6)
        //        splits(0).contains("@HQ") && !keySet.contains(suffix) //&& (time.getHours <= 7 || time.getHours >= 15)
        //        (username.equals("23010765@HL") || username.equals("35002819@FJ") || username.equals
        //        ("71101755@SH") || username.equals("32036068@JS") || srcIp.equals("132.224.19.156") || srcIp.equals
        //        ("133.69.128.90"))
        url.contains("http://ceo.mss.ctc.com:8086/WebSer/rest/")

      } catch {
        case e: Exception => false

      }
    }
  }

}
