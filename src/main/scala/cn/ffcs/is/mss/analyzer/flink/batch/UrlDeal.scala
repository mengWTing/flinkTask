package cn.ffcs.is.mss.analyzer.flink.batch


import cn.ffcs.is.mss.analyzer.utils.IniProperties
import org.apache.flink.api.common.functions.{RichFilterFunction, RichFlatMapFunction}
import org.apache.flink.api.common.io.FilePathFilter
import org.apache.flink.api.common.operators.Order
import org.apache.flink.api.java.io.TextInputFormat
import org.apache.flink.api.scala._
import org.apache.flink.core.fs.{FileSystem, Path}
import org.apache.flink.util.Collector

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.util.Random

object UrlDeal {
  def main(args: Array[String]): Unit = {
    val confProperties = new IniProperties(args(0))
    val filePath = "hdfs://10.142.82.181:8020/user/hive/warehouse/mssflow"

    val resultPath = "hdfs://10.142.82.181:8020/urlsort.csv"


    val env = ExecutionEnvironment.getExecutionEnvironment

    val fileInputFormat = new TextInputFormat(new Path(filePath))
    fileInputFormat.setNestedFileEnumeration(true)

    fileInputFormat.setFilesFilter(new FilePathFilter {
      override def filterPath(filePath: Path): Boolean = {
        //如果是目录不过滤
        if ("^time=.*".r.findAllIn(filePath.getName).nonEmpty) {
          val time = filePath.getName.substring(5, 13).toLong
          // && time <= 20190910
          if (!(time >= 20191217 && time <= 20191218)) {
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

    env.readFile(fileInputFormat, filePath).setParallelism(4)
      .filter(new OneFilter).map(t => {
      val url = t.split("\\|", -1)(6)
      if (url.contains("?")) {
        val paramStr = url.split("\\?")(1)
        val topUrl = url.split("\\?")(0)
        val kvGroup = paramStr.split("&")
        val numberMap = new mutable.HashMap[String, Long]
        for (kv <- kvGroup) {
          val kvSplits = kv.split("=")
          if (kvSplits.size == 2) {
            val k = kvSplits(0)
            val v = kvSplits(1)
            try {
              val longvalue = v.toLong
              numberMap.put(k, longvalue)
            } catch {
              case e: Exception =>
            }
          }
        }

        val countMap = new mutable.HashMap[String, Long]()
        if (numberMap.size > 0) {
          for ((k, number) <- numberMap) {
            if ((number >= 1546272000L && number <= 1577808000) || (number >= 1546272000000L && number <=
              1577808000000L) || (number >= 20191210 && number <= 20191231)) {
            } else {
              countMap.put(k, number)
            }
          }
        }
        val random = new Random()
        val randomNumber = random.nextInt(50)

        (randomNumber + "|" + topUrl, countMap, url)
      } else {
        ("", new mutable.HashMap[String, Long], "")
      }

    }).setParallelism(3)
      .filter(t => {
        t._1.length > 0 && t._2.size > 0
      }).groupBy(0).reduceGroup(it => {
      val staticMap = new mutable.HashMap[String, mutable.HashSet[Long]]()
      val urlSet = new ArrayBuffer[String]()
      var topUrl = ""
      while (it.hasNext) {
        val tuple = it.next()
        val url = tuple._3
        val numberMap = tuple._2
        topUrl = tuple._1.split("\\|")(1)
        urlSet.append(url)
        for ((k, v) <- numberMap) {
          val valueSet = staticMap.getOrElse(k, new mutable.HashSet[Long]())
          valueSet.add(v)
          staticMap.put(k, valueSet)

        }
      }
      var numberCount = 0L
      for ((k, v) <- staticMap) {
        numberCount += v.size.toLong
      }
      (topUrl, urlSet, numberCount)
    }).groupBy(0)
      .reduceGroup(it => {
        val urlAB = new ArrayBuffer[String]()
        var numberCount = 0L
        while (it.hasNext) {
          val tup = it.next()
          urlAB.appendAll(tup._2)
          numberCount += tup._3
        }
        (urlAB, numberCount)
      })
      .sortPartition(1, Order.DESCENDING).setParallelism(1)
      .flatMap(new MyFlatMapFunction).setParallelism(1)
      .writeAsText(resultPath, FileSystem.WriteMode.OVERWRITE).setParallelism(1)

    env.execute("urlDeal")
  }

  class MyFlatMapFunction extends RichFlatMapFunction[(ArrayBuffer[String], Long), String] {

    override def flatMap(value: (ArrayBuffer[String], Long), out: Collector[String]): Unit = {
      val ab = value._1
      for (url <- ab) {
        out.collect(url)
      }
    }
  }


  class OneFilter extends RichFilterFunction[String] {
    override def filter(value: String): Boolean = {
      val splits = value.split("\\|", -1)
      if (splits.size > 6) {
        val url = splits(6)
        if (url.contains("?")) {
          if (url.split("\\?").size == 2) {
            val paramStr = url.split("\\?")(1)
            val kvGroup = paramStr.split("&")
            var tag = false
            for (kv <- kvGroup) {
              val kvSplits = kv.split("=")
              if (kvSplits.size == 2) {
                val v = kvSplits(1)
                try {
                  val longvalue = v.toLong
                  tag = true
                } catch {
                  case e: NumberFormatException =>
                }
              }
            }
            return tag
          } else {
            return false
          }
        } else {
          return false
        }
      } else {
        return false
      }
    }
  }

}
