package cn.ffcs.is.mss.analyzer.flink.batch

import java.io.{BufferedReader, InputStreamReader}
import java.{lang, util}
import java.net.URI
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.{Calendar, Date, GregorianCalendar, Properties}
import cn.ffcs.is.mss.analyzer.bean.{ErrorWebLengthEntity, WebShellScanAlarmFailEntity, WebShellScanAlarmSucceedEntity}
import cn.ffcs.is.mss.analyzer.flink.sink.{DataSetKafkaSink, DataSetMySQLSink}
import cn.ffcs.is.mss.analyzer.utils._
import org.apache.flink.api.common.functions._
import org.apache.flink.api.common.io.FilePathFilter
import org.apache.flink.api.java.io.TextInputFormat
import org.apache.flink.api.scala.ExecutionEnvironment
import org.apache.flink.configuration.Configuration
import org.apache.flink.core.fs.{FileSystem, Path}
import org.apache.flink.api.scala._
import org.apache.flink.util.Collector

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.util.Random
import scala.util.control.Breaks._


/**
 * webshell 扫描检测
 *
 *
 */
object Scan {
  def main(args: Array[String]): Unit = {
    val confProperties = new IniProperties(args(0))

    val jobName = confProperties.getValue(Constants.SCAN_DETECT_CONFIG, Constants.SCAN_DETECT_JOB_NAME)
    val filePath = confProperties.getValue(Constants.SCAN_DETECT_CONFIG, Constants.SCAN_DETECT_DATA_PATH)
    val dealParallelism = confProperties.getIntValue(Constants.SCAN_DETECT_CONFIG, Constants
      .SCAN_DETECT_DEAL_PARALLELISM)
    val sinkParallelism = confProperties.getIntValue(Constants.SCAN_DETECT_CONFIG, Constants
      .SCAN_DETECT_SINK_PARALLELISM)
    val failScanDataResultPath = confProperties.getValue(Constants.SCAN_DETECT_CONFIG, Constants
      .SCAN_DETECT_FAIL_SCAN_DATA_PATH)
    val fileSystemType = confProperties.getValue(Constants.FLINK_COMMON_CONFIG, Constants.FILE_SYSTEM_TYPE)
    val historyDataFile = confProperties.getValue(Constants.SCAN_DETECT_CONFIG, Constants.SCAN_DETECT_HISTORY_DATA_FILE)
    val sinkName = confProperties.getValue(Constants.SCAN_DETECT_CONFIG, Constants.SCAN_DETECT_ALARM_SINK_NAME)
    //    val historyDataNewFile = confProperties.getValue(Constants.SCAN_DETECT_CONFIG, Constants
    //      .SCAN_DETECT_HISTORY_DATA_NEW_FILE)
    val dayLong = confProperties.getIntValue(Constants.SCAN_DETECT_CONFIG, Constants.SCAN_DETECT_DAY_LONG)

    //kafka sink的并行度
    val kafkaSinkParallelism = confProperties.getIntValue(Constants.SCAN_DETECT_CONFIG,
      Constants.SCAN_DETECT_KAFKA_SINK_PARALLELISM)
    //kafka sink的topic
    val sinkTopic = confProperties.getValue(Constants
      .SCAN_DETECT_CONFIG, Constants.SCAN_DETECT_KAFKA_SINK_TOPIC)


    val parameters: Configuration = new Configuration()

    //c3p0连接池配置文件路径
    parameters.setString(Constants.c3p0_CONFIG_PATH, confProperties.getValue(Constants.FLINK_COMMON_CONFIG, Constants
      .c3p0_CONFIG_PATH))
    //文件系统类型
    parameters.setString(Constants.FILE_SYSTEM_TYPE, confProperties.getValue(Constants
      .FLINK_COMMON_CONFIG, Constants.FILE_SYSTEM_TYPE))
    //打散的url的数量
    parameters.setInteger(Constants.SCAN_DETECT_RANDOM_SIZE, confProperties.getIntValue(Constants.SCAN_DETECT_CONFIG,
      Constants.SCAN_DETECT_RANDOM_SIZE))
    //时间段分割的session间隔 毫秒值
    parameters.setLong(Constants.SCAN_DETECT_TIME_INTERVAL, confProperties.getLongValue(Constants.SCAN_DETECT_CONFIG,
      Constants.SCAN_DETECT_TIME_INTERVAL))
    //#一个url被多少人访问(大于等于)则丢弃
    parameters.setInteger(Constants.SCAN_DETECT_USER_COUNT_THRESHOLD, confProperties.getIntValue(Constants
      .SCAN_DETECT_CONFIG, Constants.SCAN_DETECT_USER_COUNT_THRESHOLD))
    //#一个连续的访问时间段中 含有多少个不同的url 判断为扫描
    parameters.setInteger(Constants.SCAN_DETECT_URL_COUNT_THRESHOLD, confProperties.getIntValue(Constants
      .SCAN_DETECT_CONFIG, Constants.SCAN_DETECT_URL_COUNT_THRESHOLD))
    //一个用户和ip访问超过多少个url则进行统计
    parameters.setInteger(Constants.SCAN_DETECT_TOTAL_THRESHOLD, confProperties.getIntValue(Constants
      .SCAN_DETECT_CONFIG, Constants.SCAN_DETECT_TOTAL_THRESHOLD))
    //过滤掉的资源文件或其他类型
    parameters.setString(Constants.SCAN_DETECT_FILTER_KEYS, confProperties.getValue(Constants.SCAN_DETECT_CONFIG,
      Constants.SCAN_DETECT_FILTER_KEYS))
    //url白名单
    parameters.setString(Constants.SCAN_DETECT_WHITE_LIST, confProperties.getValue(Constants.SCAN_DETECT_CONFIG,
      Constants.SCAN_DETECT_WHITE_LIST))

    //设置时间戳的格式化
    parameters.setString(Constants.TIME_STAMP_FORMAT, confProperties.getValue(Constants.FLINK_COMMON_CONFIG,
      Constants.TIME_STAMP_FORMAT))
    //时间的格式化
    parameters.setString(Constants.DATE_FORMAT, confProperties.getValue(Constants.FLINK_COMMON_CONFIG, Constants
      .DATE_FORMAT))
    //判断是否访问成功的页面长度阈值
    parameters.setInteger(Constants.SCAN_DETECT_WEB_LEN_THRESHOLD, confProperties.getIntValue(Constants
      .SCAN_DETECT_CONFIG, Constants.SCAN_DETECT_WEB_LEN_THRESHOLD))
    parameters.setInteger(Constants.SCAN_DETECT_DAY_LONG, confProperties.getIntValue(Constants.SCAN_DETECT_CONFIG,
      Constants.SCAN_DETECT_DAY_LONG))

    //kafka的服务地址
    parameters.setString(Constants.KAFKA_BOOTSTRAP_SERVERS, confProperties.getValue(Constants.FLINK_COMMON_CONFIG,
      Constants.KAFKA_BOOTSTRAP_SERVERS))


    val env = ExecutionEnvironment.getExecutionEnvironment
    env.getConfig.setGlobalJobParameters(parameters)

    val fileInputFormatAllData = new TextInputFormat(new Path(filePath))
    fileInputFormatAllData.setNestedFileEnumeration(true)

    //    val fileInputFormatDealData = new TextInputFormat(new Path(historyDataNewFile))

    //    val isExist = FileSystem.get(URI.create(fileSystemType)).exists(new Path(historyDataNewFile))
    val nowTime = new GregorianCalendar()
    var left: Int = 0
    var right: Int = 0
    //    if (!isExist) {
    //读取全部数据
    right = getNDays(nowTime, 0)
    left = getNDays(nowTime, -dayLong)
    //    } else {
    //      //读取最近一天的数据
    //      right = getNDays(nowTime, 0)
    //      left = getNDays(nowTime, -1)
    //    }

    //存储最近一天的扫描失败的详细信息文件
    val resultPath = failScanDataResultPath + (right - 1)

    fileInputFormatAllData.setFilesFilter(new FilePathFilter {
      override def filterPath(filePath: Path): Boolean = {
        //如果是目录不过滤
        if ("^time=.*".r.findAllIn(filePath.getName).nonEmpty) {
          val time = filePath.getName.substring(5, 13).toLong
          // && time <= 20190910
          if (!(time >= left && time < right)) {
            //time >= left && time < right
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

    //从mss文件中读取最新数据或是全部数据
    val succeedAndFailData = env.readFile(fileInputFormatAllData, filePath).setParallelism(dealParallelism)


    val allDataWhite = succeedAndFailData
      .filter(new WebShellFilter(true)).setParallelism(dealParallelism)
      .map(new SplitMapFunction).setParallelism(dealParallelism)

    val allDataWebshell = succeedAndFailData
      .filter(new WebShellFilter(false)).setParallelism(dealParallelism)
      .map(new SplitMapFunction).setParallelism(dealParallelism)


    //少量用户
    val littleData = allDataWebshell
      //按照打散后的url进行分组
      .groupBy(2)
      .reduceGroup(it => {
        var url = ""
        val userSet = new mutable.HashSet[String]()
        val srcIpTimeList = new ArrayBuffer[(String, Long, String, String)]()
        breakable {
          while (it.hasNext) {
            val tup = it.next()
            url = tup._3.split("\\|")(0)
            val userName = tup._4
            val srcIp = tup._1
            val destIp = tup._8
            //没有用户名的匿名用户
            if (userName.length > 0 && !userName.equals("-")) {
              userSet.add(userName)
            } else {
              userSet.add("匿名用户" + "-" + srcIp)
            }
            //userSet.add(userName + "-" + srcIp)
            if (srcIp.length > 0 && !srcIp.equals("-")) {
              try {
                srcIpTimeList.append((userName + "-" + srcIp, tup._2.toLong, tup._5 + "|" + tup._6 + "|" + tup._7,
                  destIp))
              } catch {
                case e: Exception =>
              }

            }
            if (userSet.size >= 3) {
              break()
            }
          }
        }
        (url, srcIpTimeList, userSet)
      }).setParallelism(dealParallelism)
      //      .filter(new UrlAccessUserNumberCountFilter).setParallelism(dealParallelism)
      .groupBy(0)
      .reduceGroup(it => {
        val newSet = new mutable.HashSet[String]()
        val itList = it.toList
        val srcIpTimeList = new ArrayBuffer[(String, Long, String, String)]()
        var url = ""
        breakable {
          for (tup <- itList) {
            url = tup._1
            srcIpTimeList ++= tup._2
            newSet ++= tup._3
            if (newSet.size >= 3) {
              break
            }
          }
        }

        (url, srcIpTimeList, newSet)
      }).setParallelism(dealParallelism)

    //许多用户
    val manyData = allDataWhite
      .groupBy(2)
      .reduceGroup(it => {
        var url = ""
        val sdf = new SimpleDateFormat("yyyyMMdd")
        val userSet = new mutable.HashSet[String]()
        val dateSet = new mutable.HashSet[String]()
        while (it.hasNext) {
          val tup = it.next()
          url = tup._3.split("\\|")(0)
          val userName = tup._4
          val srcIp = tup._1
          val destIp = tup._8
          //
          if (userName.length > 0 && !userName.equals("-")) {
            userSet.add(userName)
          } else {
            if (srcIp.length > 0 && !srcIp.equals("-")) {
              //没有用户名的匿名用户
              userSet.add("匿名用户" + "-" + srcIp)
            }
          }
          //          userSet.add(userName + "-" + srcIp)
          try {
            dateSet.add(sdf.format(new Date(tup._2.toLong)))
          } catch {
            case e: Exception =>
          }
        }
        (url, userSet, dateSet)
      }).setParallelism(dealParallelism)
      .groupBy(0)
      .reduceGroup(it => {
        val newDateSet = new mutable.HashSet[String]()
        val newUserSet = new mutable.HashSet[String]()
        var url = ""
        while (it.hasNext) {
          val tup = it.next()
          url = tup._1
          newUserSet ++= tup._2
          newDateSet ++= tup._3
        }
        (url, newUserSet, newDateSet)
      }).setParallelism(dealParallelism)

    //许多用户


    //第一 次运行,从全量数据中过滤出被许多人访问的url

    //之后运行,直接读取存储的文件,  并且将最新数据中的许多人访问的数据保存

    //第一次运行,直接可以计算意思webshell

    //之后运行, 需要和读取的数据集进行union

    var littleUserData = littleData.filter(new UrlAccessUserNumberCountFilter).setParallelism(dealParallelism)
    var newMany = manyData.filter(new ManyUserNumberCountFilter).setParallelism(dealParallelism)
      .map(t => {
        (t._1, new ArrayBuffer[
          (String, Long, String, String)](), t._3)
      }).setParallelism(dealParallelism)
    var writeHistoryData: DataSet[(String, mutable.HashSet[String])] = null
    //    if (isExist) {
    //      //直接读取存储的文件
    //      val historyData = env.readFile(fileInputFormatDealData, historyDataNewFile).setParallelism(dealParallelism).map(new
    //          HistoryDataMapFunction).setParallelism(dealParallelism)
    //
    //      //union 最新的和历史的多人访问过的url
    //      newMany = newMany.union(historyData)
    //
    //      //最新的无人访问的url和历史的多人访问的url group-reduce, 去掉最新中在历史中可以找到的url
    //      littleUserData = littleUserData.union(newMany)
    //        .groupBy(0)
    //        .reduceGroup(it => {
    //          var isLittle = true
    //          val ab = new ArrayBuffer[(String, Long, String, String)]()
    //          var url = ""
    //          while (it.hasNext) {
    //            val tup = it.next()
    //            url = tup._1
    //            if (tup._2.size > 0) {
    //              ab ++= tup._2
    //            } else {
    //              isLittle = false
    //            }
    //          }
    //          if (isLittle) {
    //            (url, ab, new mutable.HashSet[String]())
    //          } else {
    //            (url, new ArrayBuffer[(String, Long, String, String)](), new mutable.HashSet[String]())
    //          }
    //        }).setParallelism(dealParallelism)
    //        .filter(_._2.size > 0).setParallelism(dealParallelism)
    //
    //      //将两部分数据合并为一个
    //      writeHistoryData = newMany.groupBy(0)
    //        .reduceGroup(new ManyGroupFunction).setParallelism(dealParallelism)
    //        .filter(_._2.size > 0).setParallelism(dealParallelism)
    //    } else {
    writeHistoryData = newMany.map(t => {
      (t._1, t._3)
    }).setParallelism(dealParallelism)
    //    }


    //将被很多人访问的数据写入文件
    writeHistoryData.map(t => {
      val sb: StringBuilder = new StringBuilder()
      val url = t._1
      sb.append(url + "|")
      val dateStr = t._2.mkString(",")
      sb.append(dateStr)
      sb.toString()
    }).setParallelism(dealParallelism)
      .writeAsText(historyDataFile, FileSystem.WriteMode.OVERWRITE).setParallelism(1)


    //进行后续运算的数据集
    val usedData = littleUserData
      .map(t => {
        (t._1, t._2)
      }).setParallelism(dealParallelism)
      .flatMap(new ScanStatusFlatMap).setParallelism(dealParallelism)
      .groupBy(0)
      .reduceGroup(it => {
        val sortedMap = new util.TreeMap[Long, String]()
        var srcipAndUser = ""
        while (it.hasNext) {
          val tup = it.next()
          srcipAndUser = tup._1
          val time = tup._2
          val info = tup._3
          sortedMap.put(time, info)
        }
        (srcipAndUser, sortedMap)
      }).setParallelism(dealParallelism)
      .map(new TimeGroupFunction).setParallelism(dealParallelism)
      .filter(t => {
        //过滤掉成功和失败都为空的数据
        if (t._2.size > 0 || t._3.size > 0) {
          true
        } else {
          false
        }
      }).setParallelism(dealParallelism)


    //成功和失败的数据一起写入告警表
    val sinkData = usedData.flatMap(new AlarmFlatMapFunction).setParallelism(dealParallelism)

    //写入数据库
    sinkData.map(t => {
      (t._1, t._2)
    }).setParallelism(dealParallelism)
      .output(new DataSetMySQLSink).name(sinkName).setParallelism(sinkParallelism)


    //写入kafka
    sinkData
      .filter(_._3 == 1).setParallelism(dealParallelism)
      .map(o => JsonUtil.toJson(o._1.asInstanceOf[WebShellScanAlarmSucceedEntity])).setParallelism(dealParallelism)
      .output(new DataSetKafkaSink(sinkTopic)).setParallelism(kafkaSinkParallelism)



    //失败的数据另外写入到文件中
    usedData.map(t => {
      (t._1, t._2)
    }).filter(_._2.size > 0)
      //失败的写入文件
      .map(new FailScanMapStrFunction)
      .filter(!_.equals("null"))
      .writeAsText(resultPath, FileSystem.WriteMode.OVERWRITE).setParallelism(1)


    env.execute(jobName)

    //将历史数据文件改名(防止下次运行时自动删除此文件)
    //    val fs = org.apache.hadoop.fs.FileSystem.get(URI.create(fileSystemType), new org.apache.hadoop.conf.Configuration())
    //    if (fs.exists(new org.apache.hadoop.fs.Path(historyDataNewFile))) {
    //      fs.delete(new org.apache.hadoop.fs.Path(historyDataNewFile), false)
    //    }
    //    fs.rename(new org.apache.hadoop.fs.Path(historyDataFile), new org.apache.hadoop.fs.Path(historyDataNewFile))
    //    fs.close()
  }


  /**
   * 用于将两个多人访问的url集合合并成一个,并去掉最早一天日期
   */
  class ManyGroupFunction extends RichGroupReduceFunction[(String, ArrayBuffer[(String, Long, String, String)], mutable
  .HashSet[String]), (String, mutable.HashSet[String])] {
    var firstDay: String = _


    override def open(parameters: Configuration): Unit = {
      val globConf = getRuntimeContext.getExecutionConfig.getGlobalJobParameters.asInstanceOf[Configuration]

      val format = globConf.getString(Constants.DATE_FORMAT, "")
      val dayLen = globConf.getInteger(Constants.SCAN_DETECT_DAY_LONG, 0)
      val nowTime = new GregorianCalendar()
      firstDay = getNDays(nowTime, -dayLen).toString

    }


    override def reduce(value: lang.Iterable[(String, ArrayBuffer[(String, Long, String, String)], mutable
    .HashSet[String])],
                        out: Collector[(String, mutable.HashSet[String])]): Unit = {
      val it = value.iterator().asScala
      var url = ""
      val ab = new mutable.HashSet[String]()
      while (it.hasNext) {
        val tup = it.next()
        url = tup._1
        val oldSet = tup._3

        ab ++= oldSet
      }

      if (ab.contains(firstDay)) {
        ab.remove(firstDay)
      }


      out.collect((url, ab))
    }
  }

  /**
   * 将读取到的历史数据输出为(String,new ArrayBuffer[(String, Long, String)],HashSet[String]) 方便后续操作
   */
  class HistoryDataMapFunction extends RichMapFunction[String, (String, ArrayBuffer[(String, Long, String, String)],
    mutable
    .HashSet[String])] {


    override def map(value: String): (String, ArrayBuffer[(String, Long, String, String)], mutable.HashSet[String]) = {
      val splits = value.split("\\|")
      val url = splits(0)
      val dateSplits = splits(1).split(",")
      val dateSet = new mutable.HashSet[String]()
      for (date <- dateSplits) {
        dateSet.add(date)
      }
      //被很多人访问的url的 第二个元素的长度是0
      (url, new ArrayBuffer[(String, Long, String, String)](), dateSet)
    }
  }

  /**
   * 将从mssflow新读入许多人访问过的url的数据处理成(String,new ArrayBuffer[(String, Long, String)],HashSet[String])
   */
  class ManyUserMapFunction extends RichMapFunction[(String, ArrayBuffer[(String, Long, String)], mutable
  .HashSet[String]), (String, ArrayBuffer[(String, Long, String)], mutable.HashSet[String])] {
    var format: String = _
    var sdf: SimpleDateFormat = _

    override def open(parameters: Configuration): Unit = {
      val globConf = getRuntimeContext.getExecutionConfig.getGlobalJobParameters.asInstanceOf[Configuration]
      format = globConf.getString(Constants.DATE_FORMAT, "")
      sdf = new SimpleDateFormat(format)
    }

    override def map(value: (String, ArrayBuffer[(String, Long, String)], mutable.HashSet[String])): (String,
      ArrayBuffer[(String, Long, String)], mutable.HashSet[String]) = {
      var url = ""
      val ab = value._2
      val dateSet = new mutable.HashSet[String]()
      for (record <- ab) {
        val timestamp = record._2
        val date = sdf.format(new Date(timestamp))
        dateSet.add(date)
        url = record._3.split("\\|")(0)
      }
      //被很多人访问的 第二个元素的长度是0
      (url, new ArrayBuffer[(String, Long, String)](), dateSet)
    }
  }

  /**
   * 将失败扫描的数据集合并成字符串输出  同时去掉零散的几个url
   */
  class FailScanMapStrFunction extends RichMapFunction[(String, mutable.HashMap[(Long, Long), ArrayBuffer[String]]),
    String] {
    var sdf: SimpleDateFormat = _
    var urlCountThreshold: Int = _

    override def open(parameters: Configuration): Unit = {
      val globConf = getRuntimeContext.getExecutionConfig.getGlobalJobParameters.asInstanceOf[Configuration]
      val format = globConf.getString(Constants.TIME_STAMP_FORMAT, "")
      urlCountThreshold = globConf.getInteger(Constants.SCAN_DETECT_URL_COUNT_THRESHOLD, 0)
      sdf = new SimpleDateFormat(format)
    }

    override def map(value: (String, mutable.HashMap[(Long, Long), ArrayBuffer[String]])): String = {
      val sb: StringBuilder = new mutable.StringBuilder()
      val nameAndSrcIp = value._1
      sb.append("\n" + nameAndSrcIp + "\n")
      val map = value._2
      var count = 0
      for ((k, v) <- map) {
        if (v.size > urlCountThreshold) {
          count += 1
          sb.append("\n\t" + sdf.format(new Date(k._1)) + "----" + sdf.format(new Date(k._2)) + "\n")
          for (urlInfo <- v) {
            val splits = urlInfo.split("\\|")
            val url = splits(0)
            val timestamp = splits(1).toLong
            val httpStatus = splits(2)
            val webLen = splits(3)
            sb.append("\t\t" + url + "|" + sdf.format(new Date(timestamp)) + "|" + httpStatus + "|" + webLen + "\n")
          }
        }
      }
      if (count > 0) {
        sb.toString()
      } else {
        "null"
      }

    }
  }


  /**
   * 用于拆分开分组聚合之后的数据,并且根据相应状态码和页面长度判断访问是否成功
   */

  class ScanStatusFlatMap extends RichFlatMapFunction[(String, ArrayBuffer[(String, Long, String, String)]), (String,
    Long, String)] {
    //拆分开
    //存放错误页面的域名和对应的weblen
    val errorMap = new mutable.HashMap[String, Int]()
    var sqlHelper: SQLHelper = _
    var webLenThreshold: Int = _

    override def open(parameters: Configuration): Unit = {
      //获取全局变量
      val globConf = getRuntimeContext.getExecutionConfig.getGlobalJobParameters
        .asInstanceOf[Configuration]

      webLenThreshold = globConf.getInteger(Constants.SCAN_DETECT_WEB_LEN_THRESHOLD, 0)

      //根据c3p0配置文件,初始化c3p0连接池
      val c3p0Properties = new Properties()
      val c3p0ConfigPath = globConf.getString(Constants.c3p0_CONFIG_PATH, "")
      val fileSystem = globConf.getString(Constants.FILE_SYSTEM_TYPE, "")

      val fs = org.apache.hadoop.fs.FileSystem.get(URI.create(fileSystem), new org.apache.hadoop.conf.Configuration())
      val fsDataInputStream = fs.open(new org.apache.hadoop.fs.Path(c3p0ConfigPath))
      val bufferedReader = new BufferedReader(new InputStreamReader(fsDataInputStream))
      c3p0Properties.load(bufferedReader)
      C3P0Util.ini(c3p0Properties)

      //操作数据库的类
      sqlHelper = new SQLHelper()
      val errorDataList = sqlHelper.query(classOf[ErrorWebLengthEntity])

      if (errorDataList != null && errorDataList.size() > 0) {
        for (it <- errorDataList) {
          val errorEntity = it.asInstanceOf[ErrorWebLengthEntity]
          val domainName = errorEntity.getDomainName
          val errorWebLength = errorEntity.getErrorWebLength
          errorMap.put(domainName, errorWebLength)
        }
      }
    }

    override def flatMap(value: (String, ArrayBuffer[(String, Long, String, String)]), out: Collector[(String, Long,
      String)
    ]) = {
      val url = value._1
      val ab = value._2
      for (tri <- ab) {
        val srcIPAndUser = tri._1
        val time = tri._2
        val info = tri._3.split("\\|", -1)
        val webLen = info(1).toInt
        val httpStatus = info(0).toInt
        val domain = info(2)
        val destIp = tri._4
        if (httpStatus < 300 && httpStatus >= 200) {
          if (errorMap.getOrElse(domain, -1) == webLen || webLen == 0 || webLen < webLenThreshold) {
            //扫描失败为0
            out.collect((srcIPAndUser, time, url + "|" + 0 + "|" + time + "|" + httpStatus + "|" + webLen + "|" +
              destIp))
          } else {
            //扫描成功为1
            out.collect((srcIPAndUser, time, url + "|" + 1 + "|" + time + "|" + httpStatus + "|" + webLen + "|" +
              destIp))
          }
        } else {
          //扫描失败为0
          out.collect((srcIPAndUser, time, url + "|" + 0 + "|" + time + "|" + httpStatus + "|" + webLen + "|" + destIp))
        }
      }
    }
  }

  /**
   * 将原始数据map为(srcIP,time,url+"|"+randomInt,username,httpStatus,webLen,domain,destIp)
   * ***对数据进行打散  防止数据倾斜
   */
  class SplitMapFunction extends RichMapFunction[String, (String, String, String, String, Long, Long, String, String)] {

    var randomCount: Int = _

    override def open(parameters: Configuration): Unit = {
      val globConf = getRuntimeContext.getExecutionConfig.getGlobalJobParameters.asInstanceOf[Configuration]
      randomCount = globConf.getInteger(Constants.SCAN_DETECT_RANDOM_SIZE, 0)
    }


    override def map(value: String): (String, String, String, String, Long, Long, String, String) = {

      val splits = value.split("\\|", -1)
      val destIp = splits(1)
      val domain = splits(5)
      val url = splits(6)
      val topUrl = url.split("\\?")(0)
      val user = splits(0)
      var httpStatus = -1L
      try {
        httpStatus = splits(13).toLong
      } catch {
        case e: Exception => httpStatus = -1L
      }
      var webLen = 0L
      try {
        webLen = splits(16).toLong
      } catch {
        case e: Exception => webLen = 0L
      }

      val random = new Random()
      val randomNumber = random.nextInt(randomCount)
      (splits(3), splits(10), topUrl + "|" + randomNumber, user, httpStatus, webLen, domain, destIp)
    }
  }


  /**
   * 将根据源ip聚合后的数据中的疑似扫描的url按照时间进行分组(并且分成扫描成功和扫描失败的两个数据集 发送给下游)
   */

  class TimeGroupFunction extends RichMapFunction[(String, util.TreeMap[Long, String]), (String, mutable.HashMap[(Long,
    Long), ArrayBuffer[String]], ArrayBuffer[String])] {
    var leftTimestamp = 0L
    var rightTimestamp = 0L
    //session会话的最大断开时间
    var sessionInterval: Long = _
    //一段时间url数量阈值
    var urlCountThreshold: Int = _
    //同一源ip访问的url的阈值
    var totalThreshold: Int = _


    override def open(parameters: Configuration): Unit = {
      val globConf = getRuntimeContext.getExecutionConfig.getGlobalJobParameters.asInstanceOf[Configuration]
      val format = globConf.getString(Constants.DATE_FORMAT, "")
      val sdf = new SimpleDateFormat(format)
      val now = new GregorianCalendar()
      val timeStr = getNDays(now, -1).toString
      val yesterday = sdf.parse(timeStr)
      leftTimestamp = yesterday.getTime
      //yesterday.getTime
      rightTimestamp = leftTimestamp + 24 * 60 * 60 * 1000L


      sessionInterval = globConf.getLong(Constants.SCAN_DETECT_TIME_INTERVAL, 0L)
      urlCountThreshold = globConf.getInteger(Constants.SCAN_DETECT_URL_COUNT_THRESHOLD, 0)
      totalThreshold = globConf.getInteger(Constants.SCAN_DETECT_TOTAL_THRESHOLD, 0)
    }


    override def map(value: (String, util.TreeMap[Long, String])): (String, mutable.HashMap[(Long, Long),
      ArrayBuffer[String]], ArrayBuffer[String]) = {


      var urlInfo = ""
      var count = 0L
      var leftTime = 0L
      var rightTime = 0L
      val userMap = new mutable.HashMap[(Long, Long), mutable.HashMap[String, ArrayBuffer[(Int, Long, Int, Int,
        String)]]]()
      val srcIPAndUser = value._1
      val urlMap = new mutable.HashMap[String, ArrayBuffer[(Int, Long, Int, Int, String)]]()

      var sizeCount = 0L

      val itLen = value._2.keySet().size()

      for ((k, v) <- value._2) {
        urlInfo = v

        count += 1L
        val nowTime = k
        //如果是最后一个数据, 则直接放入到userMap中(有些不好,这样可能会把最后一个不是扫描的添加到非扫描的集合中,导致数量达到阈值,判断为扫描)
        if (sizeCount == itLen - 1 && itLen != 1) {
          rightTime = nowTime
          addMap(urlMap, urlInfo)
          userMap.put((leftTime, rightTime), urlMap.clone())
          urlMap.clear()
        } else {
          if (rightTime != 0L) {
            if (nowTime - rightTime > sessionInterval) {
              //如果跟之前的时间差大于设定的session阈值,则放入到userMap中,新的数据令起一个起始时间
              userMap.put((leftTime, rightTime), urlMap.clone())
              urlMap.clear()
              leftTime = nowTime
              rightTime = nowTime
              addMap(urlMap, urlInfo)
            } else {
              rightTime = nowTime
              addMap(urlMap, urlInfo)
            }
          } else {
            //如果两边时间都为0 则两边时间都初始化为当前数据的时间
            addMap(urlMap, urlInfo)
            leftTime = nowTime
            rightTime = nowTime
            if (itLen == 1) {
              userMap.put((leftTime, rightTime), urlMap.clone())
              urlMap.clear()
            }
          }
        }
        sizeCount += 1L
      }
      val timeMapFail = new mutable.HashMap[(Long, Long), ArrayBuffer[String]]()
      val scanSucceedList = new ArrayBuffer[String]()
      //      if (count > totalThreshold) {
      for ((k, v) <- userMap) {
        val urlList = new ArrayBuffer[String]()
        //          if (v.size > urlCountThreshold) {
        //时间分段
        if (k._1 >= leftTimestamp) {
          for ((topUrl, infos) <- v) {
            //(url,(count,ArrayBuffer[(isSucceed,time,httpStatus,webLen)]))

            for ((isSucceed, time, httpStatus, webLen, destIp) <- infos) {
              if (isSucceed == 0) {
                //访问失败的
                urlList.append(topUrl + "|" + time + "|" + httpStatus + "|" + webLen + "|" + destIp)
              } else {
                scanSucceedList.append(topUrl + "|" + time + "|" + httpStatus + "|" + webLen + "|" + destIp)
              }
            }
          }
          if (urlList.size > 0) {
            timeMapFail.put((k._1, k._2), urlList)
          }
        } else if (k._2 > leftTimestamp && k._1 < leftTimestamp) {
          for ((topUrl, infos) <- v) {
            //url|是否成功|时间
            for ((isSucceed, time, httpStatus, webLen, destIp) <- infos) {
              if (time > leftTimestamp) {
                if (isSucceed == 0) {
                  //访问失败的
                  urlList.append(topUrl + "|" + time + "|" + httpStatus + "|" + webLen + "|" + destIp)
                } else {
                  scanSucceedList.append(topUrl + "|" + time + "|" + httpStatus + "|" + webLen + "|" + destIp)
                }
              }
            }
          }
          if (urlList.size > 0) {
            timeMapFail.put((leftTimestamp, k._2), urlList)
          }
        }
        //          }
      }
      //      }
      (srcIPAndUser, timeMapFail, scanSucceedList)
    }
  }


  //  class TimeGroupFunction extends RichGroupReduceFunction[(String, Long, String), (String, mutable.HashMap[(Long,
  //    Long), ArrayBuffer[String]], ArrayBuffer[String])] {
  //    var leftTimestamp = 0L
  //    var rightTimestamp = 0L
  //    //session会话的最大断开时间
  //    var sessionInterval: Long = _
  //    //一段时间url数量阈值
  //    var urlCountThreshold: Int = _
  //    //同一源ip访问的url的阈值
  //    var totalThreshold: Int = _
  //
  //
  //    override def open(parameters: Configuration): Unit = {
  //      val globConf = getRuntimeContext.getExecutionConfig.getGlobalJobParameters.asInstanceOf[Configuration]
  //      val format = globConf.getString(Constants.DATE_FORMAT, "")
  //      val sdf = new SimpleDateFormat(format)
  //      val now = new GregorianCalendar()
  //      val timeStr = getNDays(now, -1).toString
  //      val yesterday = sdf.parse(timeStr)
  //      leftTimestamp = yesterday.getTime
  //      //yesterday.getTime
  //      rightTimestamp = leftTimestamp + 24 * 60 * 60 * 1000L
  //
  //
  //      sessionInterval = globConf.getLong(Constants.SCAN_DETECT_TIME_INTERVAL, 0L)
  //      urlCountThreshold = globConf.getInteger(Constants.SCAN_DETECT_URL_COUNT_THRESHOLD, 0)
  //      totalThreshold = globConf.getInteger(Constants.SCAN_DETECT_TOTAL_THRESHOLD, 0)
  //    }
  //
  //
  //    override def reduce(value: lang.Iterable[(String, Long, String)], out: Collector[(String, mutable.HashMap[(Long,
  //      Long), ArrayBuffer[String]], ArrayBuffer[String])]): Unit = {
  //
  //      val it = value.iterator().asScala
  //      var urlInfo = ""
  //      var count = 0L
  //      var leftTime = 0L
  //      var rightTime = 0L
  //      val userMap = new mutable.HashMap[(Long, Long), mutable.HashMap[String, ArrayBuffer[(Int, Long, Int, Int)]]]()
  //      var srcIPAndUser = ""
  //      val urlMap = new mutable.HashMap[String, ArrayBuffer[(Int, Long, Int, Int)]]()
  //
  //      var sizeCount = 0L
  //      val itList = it.toList
  //      val itLen = itList.size
  //
  //      for (tup <- itList) {
  //        srcIPAndUser = tup._1
  //        urlInfo = tup._3
  //
  //        count += 1L
  //        val nowTime = tup._2.toLong
  //        //如果是最后一个数据, 则直接放入到userMap中(有些不好,这样可能会把最后一个不是扫描的添加到非扫描的集合中,导致数量达到阈值,判断为扫描)
  //        if (sizeCount == itLen - 1 && itLen != 1) {
  //          rightTime = nowTime
  //          addMap(urlMap, urlInfo)
  //          userMap.put((leftTime, rightTime), urlMap.clone())
  //          urlMap.clear()
  //        } else {
  //          if (rightTime != 0L) {
  //            if (nowTime - rightTime > sessionInterval) {
  //              //如果跟之前的时间差大于设定的session阈值,则放入到userMap中,新的数据令起一个起始时间
  //              userMap.put((leftTime, rightTime), urlMap.clone())
  //              urlMap.clear()
  //              leftTime = nowTime
  //              rightTime = nowTime
  //              addMap(urlMap, urlInfo)
  //            } else {
  //              rightTime = nowTime
  //              addMap(urlMap, urlInfo)
  //            }
  //          } else {
  //            //如果两边时间都为0 则两边时间都初始化为当前数据的时间
  //            addMap(urlMap, urlInfo)
  //            leftTime = nowTime
  //            rightTime = nowTime
  //            if (itLen == 1) {
  //              userMap.put((leftTime, rightTime), urlMap.clone())
  //              urlMap.clear()
  //            }
  //          }
  //        }
  //        sizeCount += 1L
  //      }
  //      val timeMapFail = new mutable.HashMap[(Long, Long), ArrayBuffer[String]]()
  //      val scanSucceedList = new ArrayBuffer[String]()
  //      //      if (count > totalThreshold) {
  //      for ((k, v) <- userMap) {
  //        val urlList = new ArrayBuffer[String]()
  //        //          if (v.size > urlCountThreshold) {
  //        //时间分段
  //        if (k._1 >= leftTimestamp) {
  //          for ((topUrl, infos) <- v) {
  //            //(url,(count,ArrayBuffer[(isSucceed,time,httpStatus,webLen)]))
  //
  //            for ((isSucceed, time, httpStatus, webLen) <- infos) {
  //              if (isSucceed == 0) {
  //                //访问失败的
  //                urlList.append(topUrl + "|" + time)
  //              } else {
  //                scanSucceedList.append(topUrl + "|" + time + "|" + httpStatus + "|" + webLen)
  //              }
  //            }
  //          }
  //          if (urlList.size > 0) {
  //            timeMapFail.put((k._1, k._2), urlList)
  //          }
  //        } else if (k._2 > leftTimestamp && k._1 < leftTimestamp) {
  //          for ((topUrl, infos) <- v) {
  //            //url|是否成功|时间
  //            for ((isSucceed, time, httpStatus, webLen) <- infos) {
  //              if (time > leftTimestamp) {
  //                if (isSucceed == 0) {
  //                  //访问失败的
  //                  urlList.append(topUrl + "|" + time)
  //                } else {
  //                  scanSucceedList.append(topUrl + "|" + time + "|" + httpStatus + "|" + webLen)
  //                }
  //              }
  //            }
  //          }
  //          if (urlList.size > 0) {
  //            timeMapFail.put((leftTimestamp, k._2), urlList)
  //          }
  //        }
  //        //          }
  //      }
  //      //      }
  //      out.collect((srcIPAndUser, timeMapFail, scanSucceedList))
  //    }
  //  }

  /**
   * 将成功和失败的扫描数据集写出为告警对象
   */

  class AlarmFlatMapFunction extends RichFlatMapFunction[(String, mutable.HashMap[(Long, Long), ArrayBuffer[String]],
    ArrayBuffer[String]), (Object, Boolean, Int)] {
    var urlCountThreshold: Int = _

    override def open(parameters: Configuration): Unit = {
      val globConf = getRuntimeContext.getExecutionConfig.getGlobalJobParameters.asInstanceOf[Configuration]
      urlCountThreshold = globConf.getInteger(Constants.SCAN_DETECT_URL_COUNT_THRESHOLD, 0)


    }

    override def flatMap(value: (String, mutable.HashMap[(Long, Long), ArrayBuffer[String]], ArrayBuffer[String]),
                         out: Collector[(Object, Boolean, Int)]): Unit = {
      val splits = value._1.split("-", -1)
      var userName = "匿名用户"
      try {
        userName = splits(0)
        if (userName.length == 0) {
          userName = "匿名用户"
        }
      } catch {
        case e: Exception => userName = "匿名用户"
      }

      var srcIp = "266.266.266.266"
      try {
        srcIp = splits(1)
      } catch {
        case e: Exception => srcIp = "266.266.266.266"
      }


      //失败的
      for ((timeGroup, urlList) <- value._2) {
        val alarmFailEntity = new WebShellScanAlarmFailEntity
        val count = urlList.size.toLong
        val startTime = timeGroup._1.toLong
        val endTime = timeGroup._2.toLong
        if (count > urlCountThreshold) {
          alarmFailEntity.setAttackTimeStart(new Timestamp(startTime))
          alarmFailEntity.setAttackTimeEnd(new Timestamp(endTime))
          alarmFailEntity.setUserName(userName)
          alarmFailEntity.setSourceIp(srcIp)
          alarmFailEntity.setCount(count)
          alarmFailEntity.setDangerLevel(2)

          out.collect((alarmFailEntity, true, 0))
        }

      }

      //成功的
      for (urlInfo <- value._3) {
        val urlInfoSplits = urlInfo.split("\\|")
        if (!urlInfoSplits(0).contains("jsessionid=")) {
          val alarmSucceedEntity = new WebShellScanAlarmSucceedEntity
          alarmSucceedEntity.setUserName(userName)
          alarmSucceedEntity.setSourceIp(srcIp)
          alarmSucceedEntity.setAttackUrl(urlInfoSplits(0))
          alarmSucceedEntity.setAttackTime(new Timestamp(urlInfoSplits(1).toLong))
          alarmSucceedEntity.setHttpStatus(urlInfoSplits(2).toInt)
          alarmSucceedEntity.setWebLen(urlInfoSplits(3).toInt)
          alarmSucceedEntity.setDestinationIp(urlInfoSplits(4))
          alarmSucceedEntity.setDangerLevel(3)

          out.collect((alarmSucceedEntity, true, 1))
        }
      }
    }
  }

  /**
   * 保留访问人数多的url
   */
  class ManyUserNumberCountFilter extends RichFilterFunction[(String, mutable.HashSet[String], mutable
  .HashSet[String])] {
    var userCountThreshold: Int = _

    override def open(parameters: Configuration): Unit = {
      val globConf = getRuntimeContext.getExecutionConfig.getGlobalJobParameters.asInstanceOf[Configuration]
      userCountThreshold = globConf.getInteger(Constants.SCAN_DETECT_USER_COUNT_THRESHOLD, 0)
    }


    override def filter(value: (String, mutable.HashSet[String], mutable.HashSet[String])): Boolean = {
      if (value._2.size < userCountThreshold) {
        false
      } else {
        true
      }
    }
  }


  /**
   * 滤掉大于设定阈值的用户访问过的url
   */
  class UrlAccessUserNumberCountFilter extends RichFilterFunction[(String, ArrayBuffer[(String, Long, String, String)],
    mutable.HashSet[String])] {
    var userCountThreshold: Int = _

    override def open(parameters: Configuration): Unit = {
      val globConf = getRuntimeContext.getExecutionConfig.getGlobalJobParameters.asInstanceOf[Configuration]
      userCountThreshold = globConf.getInteger(Constants.SCAN_DETECT_USER_COUNT_THRESHOLD, 0)
    }


    override def filter(value: (String, ArrayBuffer[(String, Long, String, String)], mutable.HashSet[String]))
    : Boolean = {
      if (value._3.size >= userCountThreshold) {
        false
      } else {
        true
      }
    }
  }

  /**
   * 用于过滤掉不符合扫描的原始话单
   */
  class WebShellFilter(isWhite: Boolean) extends RichFilterFunction[String] {
    val keySet = new mutable.HashSet[String]()
    val whiteList = new mutable.HashSet[String]()

    override def open(parameters: Configuration): Unit = {
      val globConf = getRuntimeContext.getExecutionConfig.getGlobalJobParameters.asInstanceOf[Configuration]

      val filterKeys = globConf.getString(Constants.SCAN_DETECT_FILTER_KEYS, "")
      val whiteUrls = globConf.getString(Constants.SCAN_DETECT_WHITE_LIST, "")
      val urls = whiteUrls.split("\\|", -1)
      urls.foreach(i => whiteList.add(i))

      val keys = filterKeys.split("\\|", -1)
      keys.foreach(i => keySet.add(i))
    }


    override def filter(value: String): Boolean = {
      val splits = value.split("\\|", -1)
      try {
        val topUrl = splits(6).split("\\?", -1)(0)
        val words = topUrl.reverse.split("\\.", 2)
        val suffix = words(0).reverse
        //        var tag = false
        //        breakable{
        //          for (i <- whiteList) {
        //            val pattern = i
        //            if (Pattern.matches(pattern, topUrl)) {
        //              tag = true
        //              break()
        //            }
        //          }
        //        }
        if (isWhite) {
          //保留所有url中被多人访问用
          !keySet.contains(suffix) && !whiteList.contains(topUrl) && splits(6).length > 0
        } else {
          //保留没有refer的webshell检测用
          !keySet.contains(suffix) && !whiteList.contains(topUrl) && splits(8).length == 0 && splits(6).length > 0
        }

        //        splits(0).length>0 && !splits(0).contains("@")
      } catch {
        case e: Exception => false
      }
    }
  }

  /**
   * 获得n天前的日期
   * 返回 类型 20190823
   *
   * @param day
   */
  def getNDays(calendar: Calendar, day: Int): Int = {

    calendar.add(Calendar.DATE, day)
    val date = calendar.getTime
    val df = new SimpleDateFormat("yyyyMMdd")
    df.format(date).toInt
  }


  def addMap(map: mutable.HashMap[String, ArrayBuffer[(Int, Long, Int, Int, String)]], urlInfo: String): Unit = {
    val splits = urlInfo.split("\\|")
    val topUrl = splits(0)
    val isSucceed = splits(1).toInt
    val time = splits(2).toLong
    val httpStatus = splits(3).toInt
    val webLen = splits(4).toInt
    val destIp = splits(5)
    val Status = map.getOrElse(topUrl, new ArrayBuffer[(Int, Long, Int, Int, String)]())
    Status.append((isSucceed, time, httpStatus, webLen, destIp))

    map.put(topUrl, Status)

  }
}
