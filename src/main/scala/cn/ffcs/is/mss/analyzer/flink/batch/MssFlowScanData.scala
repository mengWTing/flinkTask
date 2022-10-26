package cn.ffcs.is.mss.analyzer.flink.batch

import java.util

import org.apache.flink.api.common.functions.RichFilterFunction
import org.apache.flink.api.common.io.FilePathFilter
import org.apache.flink.api.java.io.TextInputFormat
import org.apache.flink.api.scala.ExecutionEnvironment
import org.apache.flink.core.fs.{FileSystem, Path}
import org.apache.flink.api.scala._
import org.apache.flink.configuration.Configuration

//import scala.actors.threadpool.Arrays
//import scala.collection.mutable
//import scala.collection.mutable.ArrayBuffer

/**
 * @title MssFlowScanData
 * @author hanyu
 * @date 2021-03-22 10:22
 * @description
 * @update [no][date YYYY-MM-DD][name][description]
 */
object MssFlowScanData {
  def main(args: Array[String]): Unit = {
    val inputPath = "hdfs://A5-302-HW-XH628-027:8020/user/hive/warehouse/mssflow"
    val outputPath = "hdfs://A5-302-HW-XH628-027:8020/scanData/scan.txt"
    val fileInputFormat = new TextInputFormat(new Path(inputPath))
    fileInputFormat.setNestedFileEnumeration(true)
    //注意时间  反着的
    fileInputFormat.setFilesFilter(new FilePathFilter {
      override def filterPath(filePath: Path): Boolean = {

        //如果是目录不过滤
        if ("^time=.*".r.findAllIn(filePath.getName).nonEmpty) {
          val time = filePath.getName.substring(5, 13).toLong
          time < 20201101


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
      .filter(new ScanDataFilterFuncation).setParallelism(50)
      .writeAsText(outputPath, FileSystem.WriteMode.OVERWRITE).setParallelism(1)
    env.execute("GET_SCAN_DATA")
  }

  class ScanDataFilterFuncation extends RichFilterFunction[String] {
    var matchArr = new Array[String](11)

    override def open(parameters: Configuration): Unit = {
      val matchStr = "scanbot|gobuster|crowdStrike|nessus|hydra|nmap|sqlmap|pangolin|appScan|goby"
      matchArr = matchStr.split("\\|", -1)

    }

    override def filter(value: String): Boolean = {
      val operationArr = value.split("\\|", -1)
      if (operationArr.length >= 10 && scanStringMatch(operationArr(9))) {
        true
      } else {
        false
      }

    }

    def scanStringMatch(value: String): Boolean = {
      val lowerCase = value.toLowerCase
      var flag = false
      for (i <- matchArr) {
        if (lowerCase.contains(i)) {
          flag = true
        }
      }
      flag
    }
  }


}
