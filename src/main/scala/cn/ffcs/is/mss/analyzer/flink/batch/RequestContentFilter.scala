package cn.ffcs.is.mss.analyzer.flink.batch

import org.apache.flink.api.common.functions.RichFilterFunction
import org.apache.flink.api.common.io.FilePathFilter
import org.apache.flink.api.java.io.TextInputFormat
import org.apache.flink.api.scala.ExecutionEnvironment
import org.apache.flink.core.fs.{FileSystem, Path}
import org.apache.flink.api.scala._
/**
 * @ClassName:
 * @Author: mengwenting
 * @Date: 2023/5/16 15:44
 * @Description:
 * @update:
 */
object RequestContentFilter {
  def main(args: Array[String]): Unit = {
    val inputPath = "hdfs://A5-302-HW-XH628-027:8020/user/hive/warehouse/mssflow"
    val outputPath = "hdfs://A5-302-HW-XH628-027:8020/filterData/requestContent.txt"
    val fileInputFormat: TextInputFormat = new TextInputFormat(new Path(inputPath))
    fileInputFormat.setNestedFileEnumeration(true)

    fileInputFormat.setFilesFilter(new FilePathFilter {
      override def filterPath(filePath: Path): Boolean = {

        //如果是目录不过滤
        if ("^time=.*".r.findAllIn(filePath.getName).nonEmpty) {
          val time = filePath.getName.substring(5, 13).toLong
          if (!(time >= 20230513 && time <= 20230514)) {
            true
          } else {
            false
          }
        } else {
          //如果是.txt文件
          if (".*txt$".r.findAllIn(filePath.getName).nonEmpty) {
            false
          } else {
            true
          }
        }
      }
    })

    val env = ExecutionEnvironment.getExecutionEnvironment

    env.readFile(fileInputFormat, inputPath).setParallelism(10)
      .filter(new RequestContentFilterFunction).setParallelism(30)
      .writeAsText(outputPath, FileSystem.WriteMode.OVERWRITE).setParallelism(1)
    env.execute("OPERATION_REQUEST_CONTENT_DATA")
  }

  class RequestContentFilterFunction extends RichFilterFunction[String] {
    override def filter(value: String): Boolean = {
      val operationArr: Array[String] = value.split("\\|", -1)
      val requestContent = operationArr(30)
      if (operationArr.length >= 31 && requestContent != null) {
        true
      }else{
        false
      }
    }
  }
}
