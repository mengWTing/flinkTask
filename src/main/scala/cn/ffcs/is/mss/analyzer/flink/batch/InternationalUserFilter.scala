package cn.ffcs.is.mss.analyzer.flink.batch

import org.apache.flink.api.common.functions.RichFilterFunction
import org.apache.flink.api.common.io.FilePathFilter
import org.apache.flink.api.java.io.TextInputFormat
import org.apache.flink.api.scala.{ExecutionEnvironment, _}
import org.apache.flink.core.fs.{FileSystem, Path}

object InternationalUserFilter {
  def main(args: Array[String]): Unit = {
    val filePath = "hdfs://10.142.82.181:8020/user/hive/warehouse/mssflow"
    val resultPath = "hdfs://10.142.82.181:8020/international"

    val env = ExecutionEnvironment.getExecutionEnvironment

    val fileInputFormat = new TextInputFormat(new Path(filePath))
    fileInputFormat.setNestedFileEnumeration(true)

    fileInputFormat.setFilesFilter(new FilePathFilter {
      override def filterPath(filePath: Path): Boolean = {
        //如果是目录不过滤
        if ("^time=.*".r.findAllIn(filePath.getName).nonEmpty) {
          val time = filePath.getName.substring(5, 13).toLong
          if (!(time >= 20190521)) {
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


    val hdfsLines = env.readFile(fileInputFormat, filePath).setParallelism(10)
      .filter(new UserNameFilterFunction)
      .writeAsText(resultPath, FileSystem.WriteMode.OVERWRITE).setParallelism(1)

    env.execute("userFilter")

  }

  class UserNameFilterFunction extends RichFilterFunction[String] {
    override def filter(value: String): Boolean = {
      val values = value.split("\\|", -1)
      try {
        values(0).contains("UR") || values(0).contains("US")
      } catch {
        case e: Exception => false

      }
    }
  }

}
