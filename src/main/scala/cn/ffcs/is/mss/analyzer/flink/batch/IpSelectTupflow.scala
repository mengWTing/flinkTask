package cn.ffcs.is.mss.analyzer.flink.batch

import org.apache.flink.api.common.functions.{RichFilterFunction, RichMapFunction}
import org.apache.flink.api.common.io.FilePathFilter
import org.apache.flink.api.java.io.TextInputFormat
import org.apache.flink.api.scala.{ExecutionEnvironment, _}
import org.apache.flink.core.fs.{FileSystem, Path}

/**
 * @title IpSelectTupflow
 * @author hanyu
 * @date 2021-06-02 10:58
 * @description
 * @update [no][date YYYY-MM-DD][name][description]
 */
object IpSelectTupflow {
  def main(args: Array[String]): Unit = {
    val inputPath = "hdfs://A5-302-HW-XH628-027:8020/user/hive/warehouse/tupflow"
    val outputPath = "hdfs://A5-302-HW-XH628-027:8020/scanData/tupflowsip.txt"
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

      .filter(new TupFilterFuncation).setParallelism(50)
      .map(new TupGetSourceIpFuncation).setParallelism(50)
      .distinct().setParallelism(50)
      .writeAsText(outputPath, FileSystem.WriteMode.OVERWRITE).setParallelism(1)
    env.execute("TUP_IP_DATA")


    class TupGetSourceIpFuncation extends RichMapFunction[String, String] {


      override def map(value: String): String = {
        val valueArr = value.split("\\|", -1)
        val ipArr = valueArr(0).split("\\.", -1)
        ipArr(0) + "." + ipArr(1) + "." + ipArr(2)
      }
    }

  }

  class TupFilterFuncation extends RichFilterFunction[String] {
    override def filter(value: String): Boolean = {
      val valueArr = value.split("\\|", -1)
      valueArr.length >= 2 && valueArr(0).split("\\.", -1).length == 4

    }
  }

}
