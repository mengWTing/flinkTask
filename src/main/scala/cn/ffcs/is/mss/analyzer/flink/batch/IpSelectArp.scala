package cn.ffcs.is.mss.analyzer.flink.batch

import cn.ffcs.is.mss.analyzer.druid.model.scala.OperationModel
import cn.ffcs.is.mss.analyzer.druid.model.scala.OperationModel.getOperationModel
import cn.ffcs.is.mss.analyzer.utils.JsonUtil
import org.apache.flink.api.common.functions.{RichFilterFunction, RichMapFunction}
import org.apache.flink.api.common.io.FilePathFilter
import org.apache.flink.api.java.io.TextInputFormat
import org.apache.flink.api.scala.ExecutionEnvironment
import org.apache.flink.core.fs.{FileSystem, Path}
import org.apache.flink.api.scala._
import org.apache.flink.configuration.Configuration

/**
 * @title IpSelectArp
 * @author hanyu
 * @date 2021-06-02 10:58
 * @description
 * @update [no][date YYYY-MM-DD][name][description]
 */
object IpSelectArp {
  def main(args: Array[String]): Unit = {
    val inputPath = "hdfs://A5-302-HW-XH628-027:8020/user/hive/warehouse/mssflow"
    val outputPath = "hdfs://A5-302-HW-XH628-027:8020/scanData/103104.txt"
    val fileInputFormat = new TextInputFormat(new Path(inputPath))

    fileInputFormat.setNestedFileEnumeration(true)
    //注意时间  反着的
    fileInputFormat.setFilesFilter(new FilePathFilter {
      override def filterPath(filePath: Path): Boolean = {
        //如果是目录不过滤
        if ("^time=.*".r.findAllIn(filePath.getName).nonEmpty) {
          val time = filePath.getName.substring(5, 13).toLong
          if (!(time >= 20220618 && time <= 20220622)) {
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
    env.readFile(fileInputFormat, inputPath).setParallelism(15)
      //      .filter(new ScanDataFilterFuncation).setParallelism(20)
      .filter(_.split("\\|", -1).length >= 10).setParallelism(16)
      .filter(new FilterFuncationForIP)
      .map(new ArpGetSourceIpFunction).setParallelism(20)
      .filter(_.nonEmpty).setParallelism(17)
      //      .map(t => {(t, 0)}).setParallelism(19)
      //      .groupBy(0)
      //      .reduce((a, b) => a)
      //      .distinct().setParallelism(60)
      .writeAsText(outputPath, FileSystem.WriteMode.OVERWRITE).setParallelism(1)
    env.execute("Operation_System")


    class ArpGetSourceIpFunction extends RichMapFunction[String, String] {
      //      override def open(parameters: Configuration): Unit = {
      //        OperationModel.setPlaceMap("hdfs://A5-302-HW-XH628-027:8020/project/mss/conf/place.txt")
      //        OperationModel.setSystemMap("hdfs://A5-302-HW-XH628-027:8020/project/mss/conf/system.txt")
      //        OperationModel.setMajorMap("hdfs://A5-302-HW-XH628-027:8020/project/mss/conf/system.txt")
      //        OperationModel.setUsedPlacesMap("hdfs://A5-302-HW-XH628-027:8020/project/mss/conf/usedPlace.txt")
      //      }
      //
      //      override def map(value: String): String = {
      //        val valueArr = value.split("\\|", -1)
      //        val maybeModel = OperationModel.getOperationModel(value)
      //        var outString = ""
      //        if (maybeModel.isDefined) {
      //          outString = valueArr(5) + "|" + maybeModel.head.destinationIp+maybeModel.head.loginMajor + "|" + maybeModel.head.loginSystem
      //        }
      //        outString
      //
      //      }

      override def map(value: String): String = {
        val strings = value.split("\\|", -1)
        var outString = ""
        outString = strings(0) + strings(3) + strings(1) + strings(6)

        outString

      }
    }

  }

  class FilterFuncationForIP extends RichFilterFunction[String] {
    override def filter(value: String): Boolean = {
      val valueArr = value.split("\\|", -1)
      valueArr(3).equals("10.3.104.171") || valueArr(3).equals("10.3.104.172")

    }
  }

}
