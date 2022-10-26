package cn.ffcs.is.mss.analyzer.flink.batch

import cn.ffcs.is.mss.analyzer.druid.model.scala.OperationModel
import cn.ffcs.is.mss.analyzer.utils.{Constants, IniProperties}
import org.apache.flink.api.common.functions.{RichFilterFunction, RichMapFunction}
import org.apache.flink.api.common.io.FilePathFilter
import org.apache.flink.api.java.io.TextInputFormat
import org.apache.flink.api.scala.ExecutionEnvironment
import org.apache.flink.core.fs.{FileSystem, Path}
import org.apache.flink.api.scala._
import org.apache.flink.configuration.Configuration

import scala.collection.mutable

object HQFilter {
  def main(args: Array[String]): Unit = {
    val filePath = "hdfs://A5-302-HW-XH628-027:8020/user/hive/warehouse/mssflow/"
    val resultPath = "hdfs://A5-302-HW-XH628-027:8020/11ri"


    val confProperties = new IniProperties(args(0))
    val fileInputFormat = new TextInputFormat(new Path(filePath))
    fileInputFormat.setNestedFileEnumeration(true)
    val parameters: Configuration = new Configuration()
    //ip-地点关联文件路径
    val placePath = confProperties.getValue(Constants.OPERATION_FLINK_TO_DRUID_CONFIG, Constants
      .OPERATION_PLACE_PATH)
    //host-系统名关联文件路径
    val systemPath = confProperties.getValue(Constants.OPERATION_FLINK_TO_DRUID_CONFIG, Constants
      .OPERATION_SYSTEM_PATH)
    //用户名-常用登录地关联文件路径
    val usedPlacePath = confProperties.getValue(Constants.OPERATION_FLINK_TO_DRUID_CONFIG,
      Constants.OPERATION_USEDPLACE_PATH)
    parameters.setString(Constants.CRAWLER_DETECT_FILTER_KEY, confProperties.getValue(Constants.FLINK_CRAWLER_DETECT_CONFIG, Constants.CRAWLER_DETECT_FILTER_KEY))

    fileInputFormat.setFilesFilter(new FilePathFilter {
      override def filterPath(filePath: Path): Boolean = {
        //如果是目录不过滤
        if ("^time=.*".r.findAllIn(filePath.getName).nonEmpty) {
          val time = filePath.getName.substring(5, 13).toLong
          if (!(time >= 20190611 && time <= 20190612)) {
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
    val env = ExecutionEnvironment.getExecutionEnvironment


    val hdfsLines = env.readFile(fileInputFormat, filePath).setParallelism(10)
      .filter(new HQFilterFunction)
        .map(new RichMapFunction[String, (Option[OperationModel], String)] {
      override def open(parameters: Configuration): Unit = {
        OperationModel.setPlaceMap(placePath)
        OperationModel.setSystemMap(systemPath)
        OperationModel.setMajorMap(systemPath)
        OperationModel.setUsedPlacesMap(usedPlacePath)
      }

      override def map(value: String): (Option[OperationModel], String) = {
        val values = value.split("\\|", -1)
        val url = values(6)
        (OperationModel.getOperationModel(value), url)
      }
    }).filter(_._1.isDefined).setParallelism(10)
      .map(operationModel => {
        (operationModel._1.head, operationModel._2)
      }).setParallelism(10)
      .filter(new RichFilterFunction[(OperationModel, String)] {
        val keySet = new mutable.HashSet[String]()

        override def open(parameters: Configuration): Unit = {
          val globalConf = getRuntimeContext.getExecutionConfig.getGlobalJobParameters.asInstanceOf[Configuration]
          val filterKeys = "css|png|js|jepg|icon|jpg|gif|ico"
          val keys = filterKeys.split("\\|",-1)
          keys.foreach(_ => keySet.add(_))
        }

        override def filter(tuple: (OperationModel, String)): Boolean = {

          val words = tuple._2.reverse.split("\\.", 2)
          val sufix = words(0).reverse
          !"未知地点".equals(tuple._1.loginPlace) && !"匿名用户".equals(tuple._1.userName) && !"未知系统".equals(tuple._1.loginSystem) && !keySet.contains(sufix)
        }
      }).setParallelism(10)
        .map(t=>{
          val userName = t._1.userName
          (userName,(t._1,1))
        })
          .groupBy(0)
        .reduceGroup(it=>{
          var name =""
          val sysSet = new mutable.HashSet[String]()
          val srcIpSet= new mutable.HashSet[String]()
          val destIpSet= new mutable.HashSet[String]()
          var count=0
          var place =""
          while(it.hasNext) {

            val tup=it.next()
            val mo = tup._2._1
            name = mo.userName
            place=mo.loginPlace
            sysSet.add(mo.loginSystem)
            srcIpSet.add(mo.sourceIp)
            destIpSet.add(mo.destinationIp)
            count+=1
          }
          name+","+place+","+count+","+sysSet.mkString(";")+","+srcIpSet.mkString(";")+","+destIpSet.mkString(";")
        })
      .writeAsText(resultPath, FileSystem.WriteMode.OVERWRITE).setParallelism(1)

    env.execute("hqFilter")
  }

  class HQFilterFunction extends RichFilterFunction[String] {
    override def filter(value: String): Boolean = {
      val splits = value.split("\\|", -1)
      if (splits.length >= 20 && splits(10).toLong >= 1560243600000L && splits(10).toLong <= 1560330000000L) {
        return true
      }
      false
    }
  }


}
