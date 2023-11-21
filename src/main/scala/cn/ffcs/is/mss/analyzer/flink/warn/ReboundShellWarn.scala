package cn.ffcs.is.mss.analyzer.flink.warn

import cn.ffcs.is.mss.analyzer.bean.ReboundShellWarnEntity
import cn.ffcs.is.mss.analyzer.druid.model.scala.OperationModel
import cn.ffcs.is.mss.analyzer.flink.sink.{MySQLSink, Sink}
import cn.ffcs.is.mss.analyzer.utils.{Constants, IniProperties}
import com.twitter.logging.config.BareFormatterConfig.{intoList, intoOption}
import org.apache.flink.api.common.functions.RichFlatMapFunction
import org.apache.flink.api.common.state.{ValueState, ValueStateDescriptor}
import org.apache.flink.configuration.{ConfigOptions, Configuration}
import org.apache.flink.streaming.api.scala.function.ProcessAllWindowFunction
import org.apache.flink.streaming.api.scala.{StreamExecutionEnvironment, _}
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.api.windowing.windows.TimeWindow
import org.apache.flink.util.Collector
import org.apache.hadoop.fs.{FileSystem, Path}
import java.io.{BufferedReader, InputStreamReader}
import java.net.{URI, URLDecoder}
import java.sql.Timestamp
import java.time.Duration
import java.util.Properties

import cn.ffcs.is.mss.analyzer.flink.source.Source
import org.apache.flink.api.common.eventtime.{SerializableTimestampAssigner, WatermarkStrategy}
import org.apache.flink.streaming.api.windowing.assigners.SlidingProcessingTimeWindows

import scala.collection.JavaConversions._
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

/**
 * @ClassName ReboundShellWarn
 * @author hanyu
 * @date 2023/1/31 10:48
 * @description
 * @update [no][date YYYY-MM-DD][name][description]
 * */
object ReboundShellWarn {
  def main(args: Array[String]): Unit = {
//    val args0 = "G:\\ffcs_flink\\mss\\src\\main\\resources\\flink.ini"
//    val confProperties = new IniProperties(args0)
    val confProperties = new IniProperties(args(0))

    //任务的名字
    val jobName = confProperties.getValue(Constants.REBOUND_SHELL_WARN_CONFIG, Constants
      .REBOUND_SHELL_WARN_CONFIG_JOB_NAME)


    //并行度
    val sourceParallelism = confProperties.getIntValue(Constants.REBOUND_SHELL_WARN_CONFIG,
      Constants.REBOUND_SHELL_WARN_CONFIG_SOURCE_PARALLELISM)
    val dealParallelism = confProperties.getIntValue(Constants.REBOUND_SHELL_WARN_CONFIG,
      Constants.REBOUND_SHELL_WARN_CONFIG_DEAL_PARALLELISM)
    val sinkParallelism = confProperties.getIntValue(Constants.REBOUND_SHELL_WARN_CONFIG,
      Constants.REBOUND_SHELL_WARN_CONFIG_SINK_PARALLELISM)

    //check pointing的间隔
    val checkpointInterval = confProperties.getLongValue(Constants.REBOUND_SHELL_WARN_CONFIG,
      Constants.REBOUND_SHELL_WARN_CONFIG_CHECKPOINT_INTERVAL)

    val timeWindow = confProperties.getLongValue(Constants.REBOUND_SHELL_WARN_CONFIG,
      Constants.REBOUND_SHELL_WARN_CONFIG_TIME_WINDOW)

    //kafka的服务地址
    val brokerList = confProperties.getValue(Constants.FLINK_COMMON_CONFIG, Constants
      .KAFKA_BOOTSTRAP_SERVERS)
    //flink消费的group.id
    val groupId = confProperties.getValue(Constants.REBOUND_SHELL_WARN_CONFIG, Constants
      .REBOUND_SHELL_WARN_CONFIG_GROUP_ID)
    //kafka source topic
    val topic = confProperties.getValue(Constants.REBOUND_SHELL_WARN_CONFIG, Constants
      .REBOUND_SHELL_WARN_CONFIG_SOURCE_TOPIC)
    //告警入topic
    val kafkaSinkTopic = confProperties.getValue(Constants.REBOUND_SHELL_WARN_CONFIG,
      Constants.REBOUND_SHELL_WARN_CONFIG_SINK_TOPIC)
    // 告警库topic
    val warningSinkTopic = confProperties.getValue(Constants.WARNING_FLINK_TO_DRUID_CONFIG, Constants
      .WARNING_TOPIC)

    //flink全局变量
    val parameters: Configuration = new Configuration()
    //配置文件系统类型
    parameters.setString(Constants.FILE_SYSTEM_TYPE, confProperties.getValue(Constants
      .FLINK_COMMON_CONFIG, Constants.FILE_SYSTEM_TYPE))
    //c3p0连接池配置文件路径
    parameters.setString(Constants.c3p0_CONFIG_PATH, confProperties.getValue(Constants
      .FLINK_COMMON_CONFIG, Constants.c3p0_CONFIG_PATH))
    //内网ip文件路径
    parameters.setString(Constants.ACTIVE_OUTREACH_ANALYZE_IP_WHITE_LIST, confProperties.getValue(Constants.
      ACTIVE_OUTREACH_ANALYZE_CONFIG, Constants.ACTIVE_OUTREACH_ANALYZE_IP_WHITE_LIST))
    //端口白名单
    parameters.setString(Constants.ACTIVE_OUTREACH_ANALYZE_PORT_WHITE_LIST, confProperties.getValue(Constants.
      ACTIVE_OUTREACH_ANALYZE_CONFIG, Constants.ACTIVE_OUTREACH_ANALYZE_PORT_WHITE_LIST))
    //特殊ip名单
    parameters.setString(Constants.ACTIVE_OUTREACH_ANALYZE_SPECIAL_IP, confProperties.getValue(Constants.
      ACTIVE_OUTREACH_ANALYZE_CONFIG, Constants.ACTIVE_OUTREACH_ANALYZE_SPECIAL_IP))
    //办公网ip文件路径
    parameters.setString(Constants.ACTIVE_OUTREACH_ANALYZE_OFFICE_IP, confProperties.getValue(Constants.
      ACTIVE_OUTREACH_ANALYZE_CONFIG, Constants.ACTIVE_OUTREACH_ANALYZE_OFFICE_IP))
    parameters.setString(Constants.REBOUND_SHELL_WARN_CONFIG_REBOUND_ORDER_FLAG, confProperties.getValue(Constants.
      REBOUND_SHELL_WARN_CONFIG, Constants.REBOUND_SHELL_WARN_CONFIG_REBOUND_ORDER_FLAG))


    val placePath = confProperties.getValue(Constants.OPERATION_FLINK_TO_DRUID_CONFIG, Constants
      .OPERATION_PLACE_PATH)
    //host-系统名关联文件路径
    val systemPath = confProperties.getValue(Constants.OPERATION_FLINK_TO_DRUID_CONFIG, Constants
      .OPERATION_SYSTEM_PATH)
    //用户名-常用登录地关联文件路径
    val usedPlacePath = confProperties.getValue(Constants.OPERATION_FLINK_TO_DRUID_CONFIG,
      Constants.OPERATION_USEDPLACE_PATH)

    //获取kafka消费者
    val consumer = Source.kafkaSource(topic, groupId, brokerList)
    //获取kafka 生产者
    val producer = Sink.kafkaSink(brokerList, kafkaSinkTopic)

    //获取ExecutionEnvironment
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    //设置check pointing的间隔
    //    env.enableCheckpointing(checkpointInterval)
    //设置flink全局变量
    env.getConfig.setGlobalJobParameters(parameters)
    env.getConfig.setAutoWatermarkInterval(0)
    val valueStream = env.fromSource(consumer, WatermarkStrategy.noWatermarks(), "kafkaSource").setParallelism(sourceParallelism)
      .filter(_.split("\\|", -1).length >= 33).setParallelism(1)
      .flatMap(new RichFlatMapFunction[String, (Long, String, String, String, String)] {
        override def flatMap(value: String, out: Collector[(Long, String, String, String, String)]): Unit = {
          //          try {
          val operationValue: OperationModel = OperationModel.getOperationModel(value).get
          val urlStr = URLDecoder.decode(OperationModel.getUrl(value).replaceAll("%(?![0-9a-fA-F]{2})", "%25"),
            "utf-8").toLowerCase()
          val connectionNetType = value.split("\\|", -1)(23)
          if (operationValue.isDefined) {
            val reboundShellStr = (operationValue.timeStamp, operationValue.userName, operationValue.sourceIp + ":" +
              operationValue.sourcePort + "|" + operationValue.destinationIp + ":" + operationValue.destinationPort,
              urlStr, connectionNetType)
            out.collect(reboundShellStr)
          }
          //          } catch {
          //            case _: Exception  => ""
          //          }
        }
      }
      ).setParallelism(1)
      .filter(_.isDefined)
      .assignTimestampsAndWatermarks(
        WatermarkStrategy.forBoundedOutOfOrderness[(Long, String, String, String, String)](Duration.ofSeconds(10))
          .withTimestampAssigner(new SerializableTimestampAssigner[(Long, String, String, String, String)] {
            override def extractTimestamp(element: (Long, String, String, String, String), recordTimestamp: Long): Long = {
              element._1
            }
          })
      ).setParallelism(dealParallelism)

    val sinkData: DataStream[((Object, Boolean), String)] = valueStream
      .windowAll(SlidingProcessingTimeWindows.of(Time.seconds(90), Time.seconds(90)))
      .process(new ReboundShellProcessFunction).setParallelism(1)

    val mysqlSinkData = sinkData.map(_._1)
    mysqlSinkData.addSink(new MySQLSink)


    env.execute(jobName)
  }


  class ReboundShellProcessFunction extends ProcessAllWindowFunction[(Long, String, String, String, String),
    ((Object, Boolean), String), TimeWindow] {
    var portWhiteList = ""
    var ipWhiteListPlath = ""
    var officePlaceListPlath = ""
    var specialIp = ""
    var timeWindow = ""
    var innerNetIp = new mutable.HashSet[String]()
    var officePlaceIp = new mutable.HashSet[String]()
    var trainFlag = ""
    var trainFlagAb = new ArrayBuffer[String]()


    //时间戳，用户名，源ip|源端口|目的ip|目的端口，url，协议类型，返回包
    lazy val timeStampAB: ValueState[ArrayBuffer[Long]] =
      getRuntimeContext.getState(new ValueStateDescriptor[ArrayBuffer[Long]]("timeStampArr",
        classOf[ArrayBuffer[Long]]))
    lazy val userNameAB: ValueState[ArrayBuffer[String]] =
      getRuntimeContext.getState(new ValueStateDescriptor[ArrayBuffer[String]]("userNameArr",
        classOf[ArrayBuffer[String]]))
    lazy val sourceIpPortDestIpPortAB: ValueState[ArrayBuffer[String]] =
      getRuntimeContext.getState(new ValueStateDescriptor[ArrayBuffer[String]]("sourceIpPortDestIpPort",
        classOf[ArrayBuffer[String]]))
    lazy val urlAB: ValueState[ArrayBuffer[String]] =
      getRuntimeContext.getState(new ValueStateDescriptor[ArrayBuffer[String]]("urlArr",
        classOf[ArrayBuffer[String]]))
    lazy val connectionNetTypeAB: ValueState[ArrayBuffer[String]] =
      getRuntimeContext.getState(new ValueStateDescriptor[ArrayBuffer[String]]("connectionNetTypeArr",
        classOf[ArrayBuffer[String]]))

    override def open(parameters: Configuration): Unit = {
      val globConf = getRuntimeContext.getExecutionConfig.getGlobalJobParameters.asInstanceOf[Configuration]
      portWhiteList = globConf.getString(ConfigOptions.key(Constants.ACTIVE_OUTREACH_ANALYZE_PORT_WHITE_LIST).stringType().defaultValue(""))
      ipWhiteListPlath = globConf.getString(ConfigOptions.key(Constants.ACTIVE_OUTREACH_ANALYZE_IP_WHITE_LIST).stringType().defaultValue(""))
      officePlaceListPlath = globConf.getString(ConfigOptions.key(Constants.ACTIVE_OUTREACH_ANALYZE_OFFICE_IP).stringType().defaultValue(""))
      specialIp = globConf.getString(ConfigOptions.key(Constants.ACTIVE_OUTREACH_ANALYZE_SPECIAL_IP).stringType().defaultValue(""))
      trainFlag = globConf.getString(ConfigOptions.key(Constants.REBOUND_SHELL_WARN_CONFIG_REBOUND_ORDER_FLAG).stringType().defaultValue(""))

      val systemType = globConf.getString(ConfigOptions.key(Constants.FILE_SYSTEM_TYPE).stringType().defaultValue(""))
      val fs = FileSystem.get(URI.create(systemType), new org.apache.hadoop.conf.Configuration())

      val fsDataInputStream = fs.open(new Path(ipWhiteListPlath))
      val bufferedReader = new BufferedReader(new InputStreamReader(fsDataInputStream))

      var line: String = bufferedReader.readLine()
      while (line != null) {
        val splits = line.split("\\|", -1)
        if (splits.length > 1) {
          innerNetIp += splits(0)
        }
        line = bufferedReader.readLine()
      }

      val officePlaceDataInputStream = fs.open(new Path(officePlaceListPlath))
      val officePlaceBR = new BufferedReader(new InputStreamReader(officePlaceDataInputStream))

      var officeLine: String = officePlaceBR.readLine()
      while (officeLine != null) {
        val splits = officeLine.split("\\|", -1)
        if (splits.length > 1) {
          officePlaceIp += splits(0)
        }
        officeLine = officePlaceBR.readLine()
      }
      val trainArr = trainFlag.split("\\|", -1)
      for (i <- trainArr) {
        trainFlagAb.append(i)
      }

    }

    override def clear(context: Context): Unit = {
      timeStampAB.clear()
      userNameAB.clear()
      sourceIpPortDestIpPortAB.clear()
      urlAB.clear()
      connectionNetTypeAB.clear()
    }


    override def process(context: Context, elements: Iterable[(Long, String, String, String, String)],
                         out: Collector[((Object, Boolean), String)]): Unit = {
      elements.foreach(windowValue => {
        if (timeStampAB.value() == null) {
          //时间戳，用户名，源ip，源端口，目的ip，目的端口，url，协议类型，返回包
          timeStampAB.update(ArrayBuffer[Long](windowValue._1))
          userNameAB.update(ArrayBuffer[String](windowValue._2))
          sourceIpPortDestIpPortAB.update(ArrayBuffer[String](windowValue._3))
          urlAB.update(ArrayBuffer[String](windowValue._4))
          connectionNetTypeAB.update(ArrayBuffer[String](windowValue._5))
        } else {
          timeStampAB.update(timeStampAB.value().++(windowValue._1))
          userNameAB.update(userNameAB.value().++(windowValue._2))
          sourceIpPortDestIpPortAB.update(sourceIpPortDestIpPortAB.value().++(windowValue._3))
          urlAB.update(urlAB.value().++(windowValue._4))
          connectionNetTypeAB.update(connectionNetTypeAB.value().++(windowValue._5))
        }
      })
      val sourceDestIpArr = new ArrayBuffer[ArrayBuffer[String]]()
      val sourcePortDestIpPortArr = new ArrayBuffer[ArrayBuffer[String]]()
      if (sourceIpPortDestIpPortAB.value().nonEmpty) {
        for (ipPortStr <- sourceIpPortDestIpPortAB.value()) {
          val ipPortSplitArr = ipPortStr.split("\\|", -1)
          val allIpPort = new ArrayBuffer[String]()
          val allIp = new ArrayBuffer[String]()
          allIpPort.add(ipPortSplitArr(0))
          allIpPort.add(ipPortSplitArr(1))
          allIp.add(ipPortSplitArr(0).split(":", -1)(0))
          allIp.add(ipPortSplitArr(1).split(":", -1)(0))
          sourceDestIpArr.add(allIp)
          sourcePortDestIpPortArr.add(allIpPort)
        }
      }
      //新增内网（已经攻陷）->办公网终端（已经攻陷）->外网
      if (sourceDestIpArr.nonEmpty & sourcePortDestIpPortArr.nonEmpty) {
        val ipChainValue = GetChainOfIpArrayBuffer(sourceDestIpArr)
        for (i <- ipChainValue._1.indices) {
          val chainArr = ipChainValue._1(i).split("\\|", -1)
          if (chainArr.length == 3) {
            val innerSourceBool = innerIpVerdict(chainArr(0), innerNetIp, specialIp)
            val officeSourceBool = innerIpVerdict(chainArr(1), officePlaceIp, specialIp)
            val innerDestBool = innerIpVerdict(chainArr(2), innerNetIp, specialIp)
            if (innerSourceBool && officeSourceBool && !innerDestBool) {
              val entity = new ReboundShellWarnEntity()
              val indexV = ipChainValue._2(i)
              entity.setAlertTime(new Timestamp(timeStampAB.value()(indexV)))
              entity.setSourceIp(chainArr(0))
              entity.setShellIp(chainArr(1))
              entity.setShellDesIp(chainArr(2))
              entity.setShellInfo("0")
              val inPutKafkaValue = "未知用户" + "|" + "反弹shell" + chainArr(0) + "-" + chainArr(1) + "-" + chainArr(2) +
                "|" + timeStampAB.value()(indexV) + "|" + "" + "|" + "" + "|" + "" + "|" + "" + "|" + chainArr(0) +
                "|" + "" + "|" + chainArr(2) + "|" + "" + "|" + "" + "|" + "" + "|" + "" + "|" + ""
              out.collect((entity, true), inPutKafkaValue)
            }
          }
        }
        //反弹shell特征匹配
        val ipPortChainValue = GetChainOfIpArrayBuffer(sourcePortDestIpPortArr)
        if (ipPortChainValue._1.nonEmpty) {
          //          for (chain <- ipPortChainValue._1) {
          //            val chainArr = chain.split("\\|", -1)
          //            val bool = ArrayIsRight(chainArr, 3, ":", 2)
          //            if (bool) {
          //              val entity = new ReboundShellWarnEntity()
          //              val indexV = ipPortChainValue._2.indexOf(chain)
          //              entity.setAlertTime(new Timestamp(timeStampAB.value()(indexV)))
          //              entity.setSourceIp(chainArr(0).split(":", -1)(0))
          //              entity.setSourceIpPort(chainArr(0).split(":", -1)(1))
          //              entity.setShellIp(chainArr(1).split(":", -1)(0))
          //              entity.setShellIpPort(chainArr(1).split(":", -1)(1))
          //              entity.setShellDesIp(chainArr(2).split(":", -1)(0))
          //              entity.setShellDesIpPort(chainArr(2).split(":", -1)(1))
          //              entity.setShellInfo(connectionNetTypeAB.value()(indexV))
          //              val inPutKafkaValue = "未知用户" + "|" + "反弹shell" + chainArr(0).split(":", -1)(0) +
          //                "-" + chainArr(1).split(":", -1)(0) + "-" + chainArr(2).split(":", -1)(0) +
          //                "|" + timeStampAB.value()(indexV) + "|" + "" + "|" + "" + "|" + "" + "|" + "" +
          //                "|" + chainArr(0).split(":", -1)(0) + "|" + chainArr(0).split(":", -1)(1) +
          //                "|" + chainArr(2).split(":", -1)(0) + "|" + chainArr(2).split(":", -1)(1) +
          //                "|" + "" + "|" + "" + "|" + "" + "|" + ""
          //
          //              out.collect((entity, true), inPutKafkaValue)
          //            }
          //          }
          for (i <- ipPortChainValue._1.indices) {
            val chainArr = ipPortChainValue._1(i).split("\\|", -1)
            val bool = ArrayIsRight(chainArr, 3, ":", 2)
            if (bool) {
              val entity = new ReboundShellWarnEntity()
              val indexV = ipPortChainValue._2(i)
              entity.setAlertTime(new Timestamp(timeStampAB.value()(indexV)))
              entity.setSourceIp(chainArr(0).split(":", -1)(0))
              entity.setSourceIpPort(chainArr(0).split(":", -1)(1))
              entity.setShellIp(chainArr(1).split(":", -1)(0))
              entity.setShellIpPort(chainArr(1).split(":", -1)(1))
              entity.setShellDesIp(chainArr(2).split(":", -1)(0))
              entity.setShellDesIpPort(chainArr(2).split(":", -1)(1))
              entity.setShellInfo(connectionNetTypeAB.value()(indexV))
              val inPutKafkaValue = "未知用户" + "|" + "反弹shell" + chainArr(0).split(":", -1)(0) +
                "-" + chainArr(1).split(":", -1)(0) + "-" + chainArr(2).split(":", -1)(0) +
                "|" + timeStampAB.value()(indexV) + "|" + "" + "|" + "" + "|" + "" + "|" + "" +
                "|" + chainArr(0).split(":", -1)(0) + "|" + chainArr(0).split(":", -1)(1) +
                "|" + chainArr(2).split(":", -1)(0) + "|" + chainArr(2).split(":", -1)(1) +
                "|" + "" + "|" + "" + "|" + "" + "|" + ""

              out.collect((entity, true), inPutKafkaValue)
            }

          }
        }
      }
      //url 特征匹配
      if (urlAB.value().nonEmpty)
        for (i <- urlAB.value().indices) {
          for (j <- trainFlag) {
            if (urlAB.value()(i).contains(j)) {
              val entity = new ReboundShellWarnEntity()
              entity.setAlertTime(new Timestamp(timeStampAB.value()(i)))
              val ipPortString = sourceIpPortDestIpPortAB.value()(i).split("\\|", -1)
              val bool = ArrayIsRight(ipPortString, 2, ":", 2)
              if (bool) {
                entity.setSourceIp(ipPortString(0).split(":", -1)(0))
                entity.setSourceIpPort(ipPortString(0).split(":", -1)(1))
                entity.setShellIp("未知ip")
                entity.setShellIpPort("未知端口")
                entity.setShellDesIp(ipPortString(1).split(":", -1)(0))
                entity.setShellDesIpPort(ipPortString(1).split(":", -1)(0))
                entity.setShellInfo(urlAB.value()(i))
                val inPutKafkaValue = "未知用户" + "|" + "反弹shell" + ipPortString(0).split(":", -1)(0) +
                  "-" + "未知ip" + "-" + ipPortString(0).split(":", -1)(0) +
                  "|" + timeStampAB.value()(i) + "|" + "" + "|" + "" + "|" + "" + "|" + "" +
                  "|" + ipPortString(0).split(":", -1)(0) + "|" + ipPortString(0).split(":", -1)(1) +
                  "|" + ipPortString(0).split(":", -1)(0) + "|" + ipPortString(1).split(":", -1)(0) +
                  "|" + urlAB.value()(i) + "|" + "" + "|" + "" + "|" + ""
                out.collect((entity, true), inPutKafkaValue)
              }

            }
          }
        }


    }

    def GetChainOfIpArrayBuffer(inPutValueAb: ArrayBuffer[ArrayBuffer[String]]): (ArrayBuffer[String], ArrayBuffer[Int]) = {
      val outPutValue = new ArrayBuffer[String]()
      val index = new ArrayBuffer[Int]()
      for (i <- inPutValueAb.indices) {
        for (j <- i until inPutValueAb.length) {
          if (inPutValueAb.get(i).get(1) == inPutValueAb.get(j).get(0)) {
            outPutValue.add(inPutValueAb.get(i).get(0) + "|" + inPutValueAb.get(i).get(1) + "|" + inPutValueAb.get(j).get(1))
            index.add(j)
          }
        }
      }
      (outPutValue, index)
    }

  }

  def innerIpVerdict(ipStr: String, innerIp: mutable.HashSet[String], specialIp: String): Boolean = {
    val index1 = ipStr.lastIndexOf(".")
    if (!(-1).equals(index1)) {
      val destIpStr1 = ipStr.substring(0, index1)
      val index2 = destIpStr1.lastIndexOf(".")
      if (!(-1).equals(index2)) {
        val destipStr2 = ipStr.substring(0, index2)
        innerIp.contains(ipStr) || innerIp.contains(destIpStr1) || innerIp.contains(destipStr2) || specialIp.contains(destipStr2)
      } else {
        false
      }
    } else {
      false
    }
  }

  def ArrayIsRight(value: Array[String], valueLen: Int, firstFlag: String, firstLen: Int): Boolean = {
    if (value.length == valueLen) {
      for (i <- value.indices) {
        val splitArr = value(i).split(firstFlag, -1)
        if (splitArr.length != firstLen) {
          return false
        }
      }
      true
    } else {
      false
    }
  }

}
