package cn.ffcs.is.mss.analyzer.flink.batch

import org.apache.flink.api.common.functions.RichFilterFunction
import org.apache.flink.api.common.io.FilePathFilter
import org.apache.flink.api.java.io.TextInputFormat
import org.apache.flink.api.scala.{ExecutionEnvironment, _}
import org.apache.flink.configuration.Configuration
import org.apache.flink.core.fs.{FileSystem, Path}

import scala.collection.mutable

object TupFilter {
  def main(args: Array[String]): Unit = {
    val filePath = "hdfs://10.142.82.181:8020/user/hive/warehouse/mssflow"
    val resultPath = "hdfs://10.142.82.181:8020/Not10Ip"

    val env = ExecutionEnvironment.getExecutionEnvironment

    val fileInputFormat = new TextInputFormat(new Path(filePath))
    fileInputFormat.setNestedFileEnumeration(true)

    fileInputFormat.setFilesFilter(new FilePathFilter {
      override def filterPath(filePath: Path): Boolean = {
        //如果是目录不过滤
        if ("^time=.*".r.findAllIn(filePath.getName).nonEmpty) {
          val time = filePath.getName.substring(5, 13).toLong
          if (!(time < 20191016)) {
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


    val hdfsLines = env.readFile(fileInputFormat, filePath).setParallelism(5)
      .filter(new UserNameFilterFunction).setParallelism(4)
      //      .map(t => {
      //        val splits = t.split("\\|", -1)
      //        (splits(0) + "|" + splits(3), 0)
      //      }).groupBy(0)
      //      .reduceGroup(it => {
      //        val ipMap = new mutable.HashMap[String, Long]()
      //        while (it.hasNext) {
      //          val tup = it.next()
      //          val ip = tup._1
      //          val count = ipMap.getOrElse(ip, 0L)
      //          ipMap.put(ip, count + 1L)
      //        }
      //        val sb: mutable.StringBuilder = new mutable.StringBuilder()
      //
      //        for ((k, v) <- ipMap) {
      //          sb.append(k + "|" + v)
      //        }
      //        sb.toString()
      //      })
      .writeAsText(resultPath, FileSystem.WriteMode.OVERWRITE).setParallelism(1)

    env.execute("not10.140.9.11")

  }

  class UserNameFilterFunction extends RichFilterFunction[String] {
    val maliciousIp = new mutable.HashSet[String]()

    override def open(parameters: Configuration): Unit = {
      val cIps = "10.0.0,10.0.1,10.0.101,10.0.2,10.0.254,10.0.3,10.200.144,10.67.67,11.1.1,2.0.1,2.0.10,2.0.11,2.0.13,2.0.14,2.0.15,2.0.19,2.0.2,2.0.21,2.0.25,2.0.26,2.0.27,2.0.28,2.0.29,2.0.30,2.0.31,2.0.32,2.0.33,2.0.34,2.0.35,2.0.36,2.0.37,2.0.39,2.0.4.0"
      val splits = cIps.split(",")
      for (i <- splits) {
        maliciousIp.add(i)
      }
    }

    override def filter(value: String): Boolean = {
      val splits = value.split("\\|", -1)
      try {
        val destIp = splits(1)
        val dIp = destIp.substring(0, destIp.lastIndexOf("."))
        val srcIp = splits(3)
        val sIp = srcIp.substring(0, srcIp.lastIndexOf("."))
        if (maliciousIp.contains(sIp) && !destIp.equals("10.140.9.11")) {
          //        if (maliciousIp.contains(sIp) && destIp.equals("10.140.9.11")) {
          true
        } else {
          false
        }

        //        val lastIndex = srcIP.lastIndexOf(".")
        //        val cIp = srcIP.substring(0, lastIndex)
        //        if (maliciousIp.contains(cIp)) {
        //          true
        //        } else {
        //          false
        //        }
      } catch {
        case e: Exception => false
      }
    }
  }

}
