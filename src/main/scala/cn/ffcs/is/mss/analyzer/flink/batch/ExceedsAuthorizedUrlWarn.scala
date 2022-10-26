/*
 * @project mss
 * @company Fujian Fujitsu Communication Software Co., Ltd.
 * @author chenwei
 * @date 2020-03-31 15:15:42
 * @version v1.0
 * @update [no] [date YYYY-MM-DD] [name] [description]
 */
package cn.ffcs.is.mss.analyzer.flink.batch

import java.io.{BufferedReader, InputStreamReader}
import java.net.{URI, URLDecoder}

import cn.ffcs.is.mss.analyzer.ml.tree.{CART, DecisionTreeNode}
import cn.ffcs.is.mss.analyzer.ml.utils.MlUtil
import org.apache.flink.api.common.functions.RichMapFunction
import org.apache.flink.api.common.io.FilePathFilter
import org.apache.flink.api.java.io.TextInputFormat
import org.apache.flink.api.scala._
import org.apache.flink.configuration.Configuration
import org.apache.flink.core.fs.{FileSystem, Path}

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

/**
 *
 * @author chenwei
 * @date 2020-03-31 15:15:42
 * @title ExceedsAuthorizedUrlWarn
 * @update [no] [date YYYY-MM-DD] [name] [description]
 */
object ExceedsAuthorizedUrlWarn {

  def main(args: Array[String]): Unit = {
    val inputPath = "hdfs://A5-302-HW-XH628-027:8020/user/hive/warehouse/mssflow"

    val outputPath = "hdfs://A5-302-HW-XH628-027:8020/chenw/URL.txt"
    val fileInputFormat = new TextInputFormat(new Path(inputPath))
    fileInputFormat.setNestedFileEnumeration(true)

    fileInputFormat.setFilesFilter(new FilePathFilter {
      override def filterPath(filePath: Path): Boolean = {

        //如果是目录不过滤
        if ("^time=.*".r.findAllIn(filePath.getName).nonEmpty) {
          val time = filePath.getName.substring(5, 13).toLong
          time < 20200301

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
    val hdfsLines = env.readFile(fileInputFormat, inputPath).setParallelism(100)
      .map(line => {
        val values = line.split("\\|", -1)
        if (values.length == 31) {
          if (values(29).equals("1")) {
            Some(values(6))
          }else{
            None
          }
        }else{
          None
        }
      }).setParallelism(100)
      //.filter(tuple => {
      //  if (tuple.isDefined){
      //    tuple.head.contains("?") && ( tuple.head.startsWith("http://hrzp") ||
      //      tuple.head.startsWith("http://cwfz") ||
      //      tuple.head.startsWith("http://mssportal.") || tuple.head.startsWith("http://law-hq") ||
      //    tuple.head.startsWith("http://cfmf"))
      //  }else{
      //    false
      //  }
      //}).setParallelism(100)
      .filter(tuple => {
        if (tuple.isDefined){
          tuple.head.contains("?") && !(tuple.head.startsWith("http://cwfz") ||
            tuple.head.startsWith("http://hrzp") ||
            tuple.head.startsWith("http://cfmf"))
        }else{
          false
        }
      }).setParallelism(100)
      //.map(t => (t.head, 1L))
      //.groupBy(0)
      //.reduce((t1 , t2) => (t1._1, t1._2 + t2._2))
      .map(t => {
        val url = t.head
        (url.split("\\?", 2)(0), url)
      }).setParallelism(100)
      .groupBy(0)
      .reduce((o1, o2) => (o1._1, o1._2))
      .map(_._2)
      .map(new ClassMap).setParallelism(99)
      .filter(t => {
        val typeLine = t._1
        val typeValue = typeLine.split("\\|", -1)
        val set = typeValue.toSet
        //@todo 根据实际情况修改
        !set.contains("2") && !set.contains("17") && !set.contains("1")
      })
      .writeAsText(outputPath, FileSystem.WriteMode.OVERWRITE).setParallelism(1)
    env.execute()

  }

  class ClassMap extends RichMapFunction[String, (String, String)] {

    var decisionTreeNode: DecisionTreeNode = _

    val charIndexMap = mutable.Map[Character, Integer]()
    val indexCharMap = mutable.Map[Integer, Character]()


    override def open(parameters: Configuration): Unit = {
      val path = "hdfs://A5-302-HW-XH628-027:8020/project/mss/data/param42.txt";

      val fileSystem = org.apache.hadoop.fs.FileSystem.get(URI.create(path), new org.apache.hadoop.conf.Configuration)
      val fsDataInputStream = fileSystem.open(new org.apache.hadoop.fs.Path(path))
      val bufferedReader = new BufferedReader(new InputStreamReader(fsDataInputStream))

      var line = bufferedReader.readLine


      val targetArrayList = ArrayBuffer[String]()
      val sampleArrayList = ArrayBuffer[String]()

      while (line != null) {
        val values = line.split("\\|", 2)

        val t = (Math.random() * 25).toInt
        if (t == 0) {
          sampleArrayList.append(values(1))
          targetArrayList.append(values(0))
          for (ch <- values(1).toCharArray) {
            if (!charIndexMap.contains(ch)) {
              charIndexMap.put(ch, charIndexMap.size)
              indexCharMap.put(indexCharMap.size, ch)
            }
          }
        }

        line = bufferedReader.readLine()
      }


      val samples = new Array[Array[Int]](sampleArrayList.size)
      val targets = new Array[String](sampleArrayList.size)
      val samplesStr = new Array[String](sampleArrayList.size)

      for (i <- sampleArrayList.indices) {
        val sampleStr = sampleArrayList(i)

        samples(i) = getSample(sampleStr, charIndexMap, indexCharMap);
        targets(i) = targetArrayList(i);
        samplesStr(i) = sampleArrayList(i) + "|" + targetArrayList(i);

      }

      val trainTestSplit = MlUtil.trainTestSplit(samples, targets, 0.5, samplesStr);
      decisionTreeNode = CART.fit(trainTestSplit.trainSamples, trainTestSplit.trainTarget);

    }


    override def map(in: String): (String, String) = {


      val url = in
      val urlParamArray = getUrlParamArray(url)

      val stringBuilder = new mutable.StringBuilder()
      stringBuilder.append("|")
      if (urlParamArray != null && urlParamArray.nonEmpty) {


        for (v <- urlParamArray) {
          var isDouble = false
          try {
            if (0 < v.toDouble && v.toDouble < 1.0) {
              isDouble = true
            }
          } catch {
            case exception: Exception =>
          }
          if (isDouble) {
            stringBuilder.append(17)
            stringBuilder.append("|")
          } else {
            stringBuilder.append(getType(v))
            stringBuilder.append("|")
          }
        }

      }

      (stringBuilder.toString(), url)
    }

    def getType(string: String): String = {
      val sample = getSample(string, charIndexMap, indexCharMap)
      //进行预测
      CART.predict(decisionTreeNode, sample)
    }


    def getUrlParamArray(URL: String): Array[String] = {

      val urlParamMap = mutable.Map[String, String]()

      val url = try {
        URLDecoder.decode(URL, "utf-8").toLowerCase
      } catch {
        case e: Exception => URL.toLowerCase()
      }

      val arrayBuffer = new ArrayBuffer[String]()

      if (url.contains("?")) {
        val params = url.split("\\?", 2)(1)

        params.split("&", -1).filter(t => t.contains("=")).foreach(t => {
          val (k, v) = t.split("=", 2) match {
            case Array(k, v) => (k, v)
          }
          arrayBuffer.append(v)
        })
      }

      arrayBuffer.toArray
    }

    def getUrlParamMap(URL: String): Map[String, String] = {

      val urlParamMap = mutable.Map[String, String]()

      val url = try {
        URLDecoder.decode(URL, "utf-8").toLowerCase
      } catch {
        case e: Exception => URL.toLowerCase()
      }


      if (url.contains("?")) {
        val params = url.split("\\?", 2)(1)

        params.split("&", -1).filter(t => t.contains("=")).foreach(t => {
          val (k, v) = t.split("=", 2) match {
            case Array(k, v) => (k, v)
          }
          urlParamMap.put(k, v)
        })
      }

      urlParamMap.toMap
    }

    def getPreUrl(URL: String): String = {

      try {
        URLDecoder.decode(URL, "utf-8").toLowerCase.split("\\?", 2)(0)
      } catch {
        case e: Exception => {
          ""
        }
      }

    }


    def getSample(sampleStr: String, charIndexMap: mutable.Map[Character, Integer],
                  indexCharMap: mutable.Map[Integer, Character]): Array[Int] = {

      val sample = new Array[Int](charIndexMap.size + 12)

      if (sampleStr != null && sampleStr.length > 0) {
        for (ch <- sampleStr.toCharArray()) {
          if (charIndexMap.contains(ch)) {
            sample(charIndexMap(ch)) += 1
          } else {
            sample(charIndexMap.size + 11) += 1

          }

        }
      }

      for (j <- 0 until 5) {

        sample(j + charIndexMap.size) = if (j < sampleStr.length) sampleStr.charAt(j) else 0

      }

      for (j <- 0 until 5) {
        sample(j + charIndexMap.size + 5) = if (j < sampleStr.length()) sampleStr
          .charAt(sampleStr.length() - j - 1) else 0
      }

      sample(charIndexMap.size + 10) = sampleStr.length()

      sample
    }
  }
}
