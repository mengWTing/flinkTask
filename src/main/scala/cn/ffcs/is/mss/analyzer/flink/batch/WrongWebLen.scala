package cn.ffcs.is.mss.analyzer.flink.batch

import cn.ffcs.is.mss.analyzer.utils.IniProperties
import org.apache.flink.api.common.functions.RichFilterFunction
import org.apache.flink.api.common.io.FilePathFilter
import org.apache.flink.api.java.io.TextInputFormat
import org.apache.flink.api.scala.ExecutionEnvironment
import org.apache.flink.configuration.Configuration
import org.apache.flink.core.fs.{FileSystem, Path}
import org.apache.flink.api.scala._
import scala.collection.mutable

object WrongWebLen {
  def main(args: Array[String]): Unit = {
    val confProperties = new IniProperties(args(0))
    val filePath = "hdfs://10.142.82.181:8020/user/hive/warehouse/mssflow"
    val resultPath = "hdfs://10.142.82.181:8020/error-len"
    val env = ExecutionEnvironment.getExecutionEnvironment

    val fileInputFormat = new TextInputFormat(new Path(filePath))
    fileInputFormat.setNestedFileEnumeration(true)

    fileInputFormat.setFilesFilter(new FilePathFilter {
      override def filterPath(filePath: Path): Boolean = {
        //如果是目录不过滤
        if ("^time=.*".r.findAllIn(filePath.getName).nonEmpty) {
          val time = filePath.getName.substring(5, 13).toLong
          // && time <= 20190910
          if (!(time >= 20191021 && time < 20191022)) {
            // && time <= 20191017

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


    env.readFile(fileInputFormat, filePath).setParallelism(3)
      .filter(new HttpStatus200Filter).setParallelism(3)
      .map(t => {
        val splits = t.split("\\|", -1)
        val domain = splits(5)
        val url = splits(6)
        val topUrl = url.split("\\?")(0)
        val user = splits(0)
        val httpStatus = splits(13).toLong
        val webLen = splits(16).toLong



        //        (splits(3), splits(10), topUrl + "|" + randomNumber, user, httpStatus, webLen, domain)
        (webLen, domain, topUrl)
      }).groupBy(1)
      .reduceGroup(it => {
        val lenMap = new mutable.HashMap[Int, mutable.HashSet[String]]()
        var domain = ""
        while (it.hasNext) {
          val tup = it.next()
          val webLen = tup._1.toInt
          domain = tup._2
          val topUrl = tup._3
          if (webLen <= 10000) {
            val urlSet = lenMap.getOrElse(webLen, new mutable.HashSet[String]())
            urlSet.add(topUrl)
            lenMap.put(webLen, urlSet)
          }
        }


        val sb = new StringBuilder()
        sb.append()
        for ((k, v) <- lenMap) {
          if (v.size > 50) {
            sb.append("\n\t" + k + "\t" + v.size + "\n")
            for (url <- v) {
              sb.append("\t\t" + url + "\n")
            }
          }
        }
        if (sb.toString().length > 5) {
          "\n" + domain + sb.toString()
        } else {
          "null"
        }

      }).filter(str => {
      if (str.equals("null")) {
        false
      } else {
        true
      }
    })
      .writeAsText(resultPath, FileSystem.WriteMode.OVERWRITE).setParallelism(1)

    env.execute("error-len")

  }


  class HttpStatus200Filter extends RichFilterFunction[String] {
    val keySet = new mutable.HashSet[String]()

    override def open(parameters: Configuration): Unit = {
      val filterKeys = "css|png|js|jepg|icon|jpg|gif|ico|do|xlsx|dwr|action|xls|dll"
      val keys = filterKeys.split("\\|", -1)
      keys.foreach(i => keySet.add(i))
    }


    override def filter(value: String): Boolean = {
      val splits = value.split("\\|", -1)
      try {
        val topUrl = splits(6).split("\\?", -1)(0)
        val words = topUrl.reverse.split("\\.", 2)
        val suffix = words(0).reverse
        //        !keySet.contains(suffix) && splits(8).length == 0 && splits(6).length > 0
        //        splits(0).length > 0 && !splits(0).contains("@")
        splits(13).toInt == 200 && !keySet.contains(suffix) && splits(8).length == 0 && splits(6).length > 0


      } catch {
        case e: Exception => false
      }
    }
  }


}
