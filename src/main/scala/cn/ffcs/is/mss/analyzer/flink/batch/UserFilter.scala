package cn.ffcs.is.mss.analyzer.flink.batch

import org.apache.flink.api.common.functions.RichFilterFunction
import org.apache.flink.api.common.io.FilePathFilter
import org.apache.flink.api.java.io.TextInputFormat
import org.apache.flink.api.scala.ExecutionEnvironment
import org.apache.flink.core.fs.{FileSystem, Path}
import org.apache.flink.api.scala._
import org.apache.flink.configuration.Configuration

import scala.collection.mutable

object UserFilter {
  def main(args: Array[String]): Unit = {
    val filePath = "hdfs://10.142.82.181:8020/user/hive/warehouse/mssflow"
    val resultPath = "hdfs://10.142.82.181:8020/kelai"

    val env = ExecutionEnvironment.getExecutionEnvironment

    val fileInputFormat = new TextInputFormat(new Path(filePath))
    fileInputFormat.setNestedFileEnumeration(true)

    fileInputFormat.setFilesFilter(new FilePathFilter {
      override def filterPath(filePath: Path): Boolean = {
        //如果是目录不过滤
        if ("^time=.*".r.findAllIn(filePath.getName).nonEmpty) {
          val time = filePath.getName.substring(5, 13).toLong
          if (!(time == 20200926)) {
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


    val hdfsLines = env.readFile(fileInputFormat, filePath).setParallelism(4)
      .filter(new UserNameFilterFunction)
      .writeAsText(resultPath, FileSystem.WriteMode.OVERWRITE).setParallelism(1)

    env.execute("kelai")

  }

  class UserNameFilterFunction extends RichFilterFunction[String] {
    val maliciousIp = new mutable.HashSet[String]()

    override def open(parameters: Configuration): Unit = {
      val maliciouse = "88.84.200.139,40.73.25.111,58.59.2.26,197.3.7.157,132.234.183.115,132.103.137.157,197.254.28.103,172.254.107.118,183.101.208.41,51.75.202.218,51.68.141.2,151.84.222.52,113.161.66.214,137.38.10.220,135.198.7.26,133.75.50.130,134.105.72.53,134.233.134.60,148.204.64.136,132.234.182.101,16.119.162.156,135.67.163.76,135.193.250.58,132.108.165.137,172.16.50.193,172.16.50.224,172.16.10.23,172.22.9.233,172.22.100.56,172.22.13.53,10.183.83.79,172.22.47.103,172.22.54.19,172.22.13.205,10.252.26.124,172.16.10.34,172.16.10.28,172.16.10.30,172.16.10.35,172.16.10.29,172.16.10.27,172.16.10.31,10.183.94.211"
      val splits = maliciouse.split(",")
      for (i <- splits) {
        maliciousIp.add(i)
      }


    }

    override def filter(value: String): Boolean = {
      val values = value.split("\\|", -1)
      try {
        values(23).equals("http") || values(23).equals("https")
      } catch {
        case e: Exception => false

      }
    }
  }

}
