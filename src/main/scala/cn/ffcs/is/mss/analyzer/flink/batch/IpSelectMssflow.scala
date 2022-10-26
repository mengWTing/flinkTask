package cn.ffcs.is.mss.analyzer.flink.batch

import org.apache.flink.api.common.functions.{RichFilterFunction, RichMapFunction}
import org.apache.flink.api.common.io.FilePathFilter
import org.apache.flink.api.java.io.TextInputFormat
import org.apache.flink.api.scala.ExecutionEnvironment
import org.apache.flink.configuration.Configuration
import org.apache.flink.core.fs.{FileSystem, Path}
import org.apache.flink.api.scala._

/**
 * @title IpSelect
 * @author hanyu
 * @date 2021-03-31 14:17
 * @description
 * @update [no][date YYYY-MM-DD][name][description]
 */
object IpSelectMssflow {

  def main(args: Array[String]): Unit = {
    val inputPath = "hdfs://A5-302-HW-XH628-027:8020/user/hive/warehouse/mssflow"

    val outputPath = "hdfs://A5-302-HW-XH628-027:8020/scanData/mssflowsip.txt"
    val fileInputFormat = new TextInputFormat(new Path(inputPath))
    fileInputFormat.setNestedFileEnumeration(true)
    //注意时间  反着的
    fileInputFormat.setFilesFilter(new FilePathFilter {
      override def filterPath(filePath: Path): Boolean = {
        //如果是目录不过滤
        if ("^time=.*".r.findAllIn(filePath.getName).nonEmpty) {
          val time = filePath.getName.substring(5, 13).toLong
          if (!(time >= 20210301 && time <= 20210602)) {
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
    env.readFile(fileInputFormat, inputPath).setParallelism(50)
      //      .filter(new ScanDataFilterFuncation).setParallelism(20)
      //      .filter(t => {
      //        val splits = t.split("\\|", -1)
      //        var flag = false
      //        if(splits.length>4){
      //          flag = (splits(0).contains("136.6.248.194")||splits(3).contains("10.140.19.40")||splits(0).contains("132.96.191.103")||splits(3).contains("132.96.191.103"))
      //        }
      //        flag
      //      })
      .filter(new FilterFuncation).setParallelism(50)
      .map(new MssGetSourceIpFuncation).setParallelism(50)
      .distinct().setParallelism(50)

      .writeAsText(outputPath, FileSystem.WriteMode.OVERWRITE).setParallelism(1)
    env.execute("MSS_IP_DATA")
  }

  //  class ScanDataFilterFuncation extends RichFilterFunction[String] {
  //    var matchArr = new Array[String](2)
  //
  //    override def open(parameters: Configuration): Unit = {
  //      val matchStr = "10.140.19.40"
  //      matchArr = matchStr.split("\\|", -1)
  //
  //    }
  //
  //    override def filter(value: String): Boolean = {
  //      val operationArr = value.split("\\|", -1)
  //      operationArr.length >= 10 && (matchArr.contains(operationArr(1)) || matchArr.contains(operationArr(3)))
  //    }
  //
  //
  //  }


  class MssGetSourceIpFuncation extends RichMapFunction[String, String] {


    override def map(value: String): String = {
      val valueArr = value.split("\\|", -1)
      val ipArr = valueArr(3).split("\\.", -1)

      ipArr(0) + "." + ipArr(1) + "." + ipArr(2)


    }
  }

  class FilterFuncation extends RichFilterFunction[String] {
    override def filter(value: String): Boolean = {
      val valueArr = value.split("\\|", -1)
      valueArr.length >= 4 && valueArr(3).split("\\.", -1).length == 4

    }
  }

}

