/*
 * @project mss
 * @company Fujian Fujitsu Communication Software Co., Ltd.
 * @author chenwei
 * @date 2019-05-27 15:19:39
 * @version v1.0
 * @update [no] [date YYYY-MM-DD] [name] [description]
 */
package cn.ffcs.is.mss.analyzer.flink.batch

import java.io.{BufferedReader, InputStreamReader}
import java.lang
import java.net.URI
import java.text.SimpleDateFormat

import cn.ffcs.is.mss.analyzer.druid.model.scala.OperationModel
import org.apache.flink.api.common.functions.{RichFilterFunction, RichGroupReduceFunction, RichMapFunction}
import org.apache.flink.api.common.io.FilePathFilter
import org.apache.flink.api.java.io.TextInputFormat
import org.apache.flink.api.scala._
import org.apache.flink.configuration.Configuration
import org.apache.flink.core.fs.{FileSystem, Path}
import org.apache.flink.util.Collector

import scala.collection.mutable
/**
  *
  * @author chenwei
  * @date 2019-05-27 15:19:39
  * @title GetSystemDate
  * @update [no] [date YYYY-MM-DD] [name] [description]
  */
object GetSystemDate {

  def main(args: Array[String]): Unit = {
    val filePath = "hdfs://A5-302-HW-XH628-027:8020/user/hive/warehouse/mssflow"
    val resultPath = "hdfs://A5-302-HW-XH628-027:8020/chenw/psystem/psystem"


    val env = ExecutionEnvironment.getExecutionEnvironment


    val fileInputFormat = new TextInputFormat(new Path(filePath))
    fileInputFormat.setNestedFileEnumeration(true)

    fileInputFormat.setFilesFilter(new FilePathFilter {
      override def filterPath(filePath: Path): Boolean = {

        //如果是目录不过滤
        if ("^time=.*".r.findAllIn(filePath.getName).nonEmpty) {

          val time = filePath.getName.substring(5, 13).toLong
          20190510 > time
        } else {
          //如果是.txt文件
          if (".*txt$".r.findAllIn(filePath.getName).nonEmpty) {
            false
            //            if(filePath.hashCode() % 10000 == 0){
            //              println(filePath)
            //              false
            //            }else{
            //              true
            //            }
            //如果不是txt文件，过滤
          } else {
            true
          }

        }
      }
    })

    val hdfsLines = env.readFile(fileInputFormat, filePath).setParallelism(10)
      .filter(new MyFilterFunction).setParallelism(10)
      .map(new MyMap())
      .filter(_.isDefined)
      .map(_.head)
      .groupBy(_.userName)
      .reduceGroup(new MyReduceGroup)
      .writeAsText(resultPath, FileSystem.WriteMode.OVERWRITE).setParallelism(1)

    env.execute("hahaha")

  }

  class MyReduceGroup extends RichGroupReduceFunction[OperationModel, String] {


    override def reduce(values: lang.Iterable[OperationModel], out: Collector[String]): Unit = {

      val systemMap = mutable.Map[String, Long]()
      val placeMap = mutable.Map[String, Long]()
      var userName = ""

      val iterator = values.iterator()
      while (iterator.hasNext) {
        val operationModel = iterator.next()
        if (userName == null || userName.equals("")) {
          userName = operationModel.userName
        }
        systemMap.put(operationModel.loginSystem, systemMap.getOrElse(operationModel.loginSystem, 0L) + 1)
        placeMap.put(operationModel.loginPlace, systemMap.getOrElse(operationModel.loginPlace, 0L) + 1)
      }

      val stringBuilder = new mutable.StringBuilder()

      stringBuilder.append(userName)
      stringBuilder.append(",")
      for ((k, v) <- systemMap) {
        stringBuilder.append(k)
        stringBuilder.append(":")
        stringBuilder.append(v)
        stringBuilder.append(";")
      }
      stringBuilder.append(",")
      for ((k, v) <- placeMap) {
        stringBuilder.append(k)
        stringBuilder.append(":")
        stringBuilder.append(v)
        stringBuilder.append(";")
      }
      out.collect(stringBuilder.toString())

    }
  }

  class MyFilterFunction extends RichFilterFunction[String] {

    val map = mutable.Map[String, mutable.Set[(Long, Long)]]()
    val suffixSet = mutable.Set[String]()

    override def open(parameters: Configuration): Unit = {

      val simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
      suffixSet.add("css")
      suffixSet.add("png")
      suffixSet.add("js")
      suffixSet.add("jepg")
      suffixSet.add("icon")
      suffixSet.add("jpg")
      suffixSet.add("gif")
      suffixSet.add("ico")

      val path = "hdfs://A5-302-HW-XH628-027:8020/chenw/c/c"
      val fs = org.apache.hadoop.fs.FileSystem.get(URI.create(path), new org.apache.hadoop.conf.Configuration())
      val fsDataInputStream = fs.open(new org.apache.hadoop.fs.Path(path))
      val bufferedReader = new BufferedReader(new InputStreamReader(fsDataInputStream))

      var line = bufferedReader.readLine()
      while (line != null) {
        val values = line.split("\\,", -1)
        if (values.length == 8) {
          val userName = values(0)
          if (!"匿名用户".equals(userName)) {
            val startTime = simpleDateFormat.parse(values(1)).getTime
            val endTime = simpleDateFormat.parse(values(2)).getTime

            val set = map.getOrElse(userName, mutable.Set[(Long, Long)]())
            set.add((startTime, endTime))
            map.put(userName, set)
          }

        }
        line = bufferedReader.readLine()
      }


    }

    override def filter(value: String): Boolean = {
      val values = value.split("\\|", -1)
      val userName = values(0)
      if (values.length>10) {
        val timeStamp = values(10).toLong

        if (!map.contains(userName)) {
          return false
        } else {
          val set = map(userName)

          for ((t1, t2) <- set) {
            if (t1 < timeStamp && timeStamp < t2) {

              val url = values(6)
              val words = url.reverse.split("\\.", 2)
              val suffix = words(0).reverse
              if (!suffixSet.contains(suffix)) {
                return true
              }

            }
          }

          return false
        }
      }else{
        return false
      }


    }
  }

  class MyMap extends RichMapFunction[String, Option[OperationModel]] {

    override def open(parameters: Configuration): Unit = {
      val placePath = "hdfs://A5-302-HW-XH628-027:8020/project/mss/conf/place.txt"
      val systemPath = "hdfs://A5-302-HW-XH628-027:8020/project/mss/conf/system.txt"
      val usedPlacePath = "hdfs://A5-302-HW-XH628-027:8020/project/mss/conf/usedPlace.txt"


      //val placePath = "/Users/chenwei/Documents/mss/入库关联文件/place简化地址.txt"
      //val systemPath = "/Users/chenwei/Documents/mss/入库关联文件/system.txt"
      //val usedPlacePath = "/Users/chenwei/Documents/mss/入库关联文件/usedPlace.txt"

      OperationModel.setPlaceMap(placePath)
      OperationModel.setSystemMap(systemPath)
      OperationModel.setMajorMap(systemPath)
      OperationModel.setUsedPlacesMap(usedPlacePath)
    }

    override def map(value: String): Option[OperationModel] = {

      OperationModel.getOperationModel(value)

    }

  }
}
