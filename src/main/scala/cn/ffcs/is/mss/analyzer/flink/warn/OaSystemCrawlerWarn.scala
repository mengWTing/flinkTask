package cn.ffcs.is.mss.analyzer.flink.warn

import java.io.{BufferedReader, InputStreamReader}
import java.net.URI
import java.sql.Timestamp
import java.time.Duration
import java.util.Properties

import cn.ffcs.is.mss.analyzer.bean.OaSystemCrawlerWarningEntity
import cn.ffcs.is.mss.analyzer.flink.sink.{MySQLSink, Sink}
import cn.ffcs.is.mss.analyzer.flink.source.Source
import cn.ffcs.is.mss.analyzer.utils.{Constants, IniProperties, JsonUtil}
import org.apache.flink.api.common.accumulators.LongCounter
import org.apache.flink.api.common.eventtime.{SerializableTimestampAssigner, WatermarkStrategy}
import org.apache.flink.api.common.functions.RichMapFunction
import org.apache.flink.api.common.state.{ValueState, ValueStateDescriptor}
import org.apache.flink.configuration.{ConfigOptions, Configuration}
import org.apache.flink.streaming.api.functions.{KeyedProcessFunction, ProcessFunction}
import org.apache.flink.streaming.api.scala.{StreamExecutionEnvironment, _}
import org.apache.flink.streaming.api.windowing.assigners.SlidingProcessingTimeWindows
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.util.Collector
import org.apache.hadoop.fs.{FileSystem, Path}

import scala.collection.JavaConversions._
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

/**
 * @title OaSystemBatchDownLoadWarn
 * @author hanyu
 * @date 2021-06-01 16:18
 * @description OA系统公文（明文、密文）、通讯录的爬虫检测
 * @update [no][date YYYY-MM-DD][name][description]
 */
object OaSystemCrawlerWarn {
  def main(args: Array[String]): Unit = {
    //val args0 = "./src/main/resources/flink.ini"
    //根据传入的参数解析配置文件

    //val confProperties = new IniProperties(args0)
    val confProperties = new IniProperties(args(0))

    //该任务的名字
    val jobName = confProperties.getValue(Constants.OA_SYSTEM_CRAWLER_WARN_CONFIG,
      Constants.OA_SYSTEM_CRAWLER_WARN_JOB_NAME)

    //source并行度
    val sourceParallelism = confProperties.getIntValue(Constants
      .OA_SYSTEM_CRAWLER_WARN_CONFIG, Constants.OA_SYSTEM_CRAWLER_WARN_KAFKA_SOURCE_PARALLELISM)
    //deal并行度
    val dealParallelism = confProperties.getIntValue(Constants
      .OA_SYSTEM_CRAWLER_WARN_CONFIG, Constants.OA_SYSTEM_CRAWLER_WARN_DEAL_PARALLELISM)
    //sink的并行度
    val sinkParallelism = confProperties.getIntValue(Constants.OA_SYSTEM_CRAWLER_WARN_CONFIG,
      Constants.OA_SYSTEM_CRAWLER_WARN_KAFKA_SINK_PARALLELISM)

    //kafka的服务地址
    val brokerList = confProperties.getValue(Constants.FLINK_COMMON_CONFIG, Constants
      .KAFKA_BOOTSTRAP_SERVERS)
    //flink消费的group.id
    val groupId = confProperties.getValue(Constants.OA_SYSTEM_CRAWLER_WARN_CONFIG,
      Constants.OA_SYSTEM_CRAWLER_WARN_GROUP_ID)
    //kafka source的topic
    val sourceTopic = confProperties.getValue(Constants.OPERATION_FLINK_TO_DRUID_CONFIG, Constants
      .OPERATION_TOPIC)
    //kafka sink的topic
    val sinkTopic = confProperties.getValue(Constants.OA_SYSTEM_CRAWLER_WARN_CONFIG, Constants
      .OA_SYSTEM_CRAWLER_WARN_KAFKA_SINK_TOPIC)
    //OA 公文 url
    val oaSystemOffUrl = confProperties.getValue(Constants.OA_SYSTEM_CRAWLER_WARN_CONFIG,
      Constants.OA_SYSTEM_CRAWLER_WARN_OFFICIAL_URL)
    val oaSystemAddrUrl = confProperties.getValue(Constants.OA_SYSTEM_CRAWLER_WARN_CONFIG,
      Constants.OA_SYSTEM_CRAWLER_WARN_ADDRESS_URL)

    val warningSinkTopic = confProperties.getValue(Constants.WARNING_FLINK_TO_DRUID_CONFIG, Constants
      .WARNING_TOPIC)

    //flink全局变量
    val parameters: Configuration = new Configuration()
    //c3p0连接池配置文件路径
    parameters.setString(Constants.c3p0_CONFIG_PATH, confProperties.getValue(Constants
      .FLINK_COMMON_CONFIG, Constants.c3p0_CONFIG_PATH))

    parameters.setString(Constants.FILE_SYSTEM_TYPE, confProperties.getValue(Constants
      .FLINK_COMMON_CONFIG, Constants.FILE_SYSTEM_TYPE))
    //运维人员名单列表
    parameters.setString(Constants.OPERATION_PERSONNEL_DOWNLOAD_OPERATION_PERSONNEL_PATH_OA,
      confProperties.getValue(Constants.OA_SYSTEM_CRAWLER_WARN_CONFIG, Constants.OPERATION_PERSONNEL_DOWNLOAD_OPERATION_PERSONNEL_PATH_OA))
    //允许的在线时长
    parameters.setLong(Constants.OA_SYSTEM_CRAWLER_WARN_ONLINE_TIME_ALLOW,
      confProperties.getLongValue(Constants.OA_SYSTEM_CRAWLER_WARN_CONFIG, Constants.OA_SYSTEM_CRAWLER_WARN_ONLINE_TIME_ALLOW))
    //判断为爬虫的时长阈值
    parameters.setLong(Constants.OA_SYSTEM_CRAWLER_WARN_ONLINE_TIME_DECIDE,
      confProperties.getLongValue(Constants.OA_SYSTEM_CRAWLER_WARN_CONFIG, Constants.OA_SYSTEM_CRAWLER_WARN_ONLINE_TIME_DECIDE))
    //通讯录url
    parameters.setString(Constants.OA_SYSTEM_CRAWLER_WARN_ADDRESS_URL,
      confProperties.getValue(Constants.OA_SYSTEM_CRAWLER_WARN_CONFIG, Constants.OA_SYSTEM_CRAWLER_WARN_ADDRESS_URL))
    //公文URL
    parameters.setString(Constants.OA_SYSTEM_CRAWLER_WARN_OFFICIAL_URL,
      confProperties.getValue(Constants.OA_SYSTEM_CRAWLER_WARN_CONFIG, Constants.OA_SYSTEM_CRAWLER_WARN_OFFICIAL_URL))
    //允许在线时长内浏览下载次数
    parameters.setInteger(Constants.OA_SYSTEM_CRAWLER_WARN_COUNT_ALLOW,
      confProperties.getIntValue(Constants.OA_SYSTEM_CRAWLER_WARN_CONFIG, Constants.OA_SYSTEM_CRAWLER_WARN_COUNT_ALLOW))

    //check pointing的间隔
    val checkpointInterval = confProperties.getLongValue(Constants
      .OA_SYSTEM_CRAWLER_WARN_CONFIG, Constants.OA_SYSTEM_CRAWLER_WARN_CHECKPOINT_INTERVAL)

    //获取kafka消费者
    val consumer = Source.kafkaSource(sourceTopic, groupId, brokerList)
    //获取kafka 生产者
    val producer = Sink.kafkaSink(brokerList, sinkTopic)
    //获取ExecutionEnvironment
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    //设置checkpoint
    //env.enableCheckpointing(checkpointInterval)
    env.getConfig.setGlobalJobParameters(parameters)
    env.getConfig.setAutoWatermarkInterval(0)

    //获取Kafka数据流
    val dataStream = env.fromSource(consumer, WatermarkStrategy.noWatermarks(), "kafkaSource").setParallelism(sourceParallelism)

    //过滤出访问OA的公文及通讯录的数据
    val oaSystemWarnStream = dataStream.filter(t => {
      var flag = true
      val dataArr = t.split("\\|", -1)
      if (dataArr.length > 10) {
        flag = (dataArr(8).contains(oaSystemOffUrl) || dataArr(8).contains(oaSystemAddrUrl)) && dataArr(0).nonEmpty
      }
      flag
    }).setParallelism(dealParallelism)
      .assignTimestampsAndWatermarks(
        WatermarkStrategy.forBoundedOutOfOrderness[String](Duration.ofSeconds(10))
          .withTimestampAssigner(new SerializableTimestampAssigner[String] {
            override def extractTimestamp(element: String, recordTimestamp: Long): Long = {
                element.split("\\|", -1)(10).trim.toLong
            }
          })
      )

    val timeValue = oaSystemWarnStream
      .map(new RichMapFunction[String, (String, Long, mutable.HashSet[String],
        mutable.HashSet[String], mutable.HashSet[String])] {
        override def map(value: String): (String, Long, mutable.HashSet[String],
          mutable.HashSet[String], mutable.HashSet[String]) = {
          val valueArr = value.split("\\|", -1)
          val oaSystemSourceIp = new mutable.HashSet[String]()
          val oaSystemDesIp = new mutable.HashSet[String]()
          val oaSystemReferUrl = new mutable.HashSet[String]()

          val userName = valueArr(0)
          val desIp = valueArr(1)
          val sourceIp = valueArr(3)
          val referUrl = valueArr(8)
          val timestamp = valueArr(10).trim.toLong

          oaSystemSourceIp.+=(sourceIp)
          oaSystemDesIp.add(desIp)
          oaSystemReferUrl.add(referUrl)
          //用户名，时间，源ip，目的ip，url
          (userName, timestamp, oaSystemSourceIp, oaSystemDesIp, oaSystemReferUrl)
        }
      }).setParallelism(dealParallelism)
      .keyBy(_._1)
      .process(new OaSystemCrawlerWarnProcessFuncation).setParallelism(dealParallelism)

    val timeValues = timeValue.map(_._1)
    val timeAlertKafkaValue = timeValue.map(_._2)

    //    val urlValue = oaSystemWarnStream.process(new UrlWarnProcessFuncation).setParallelism(dealParallelism)
    val urlValue = oaSystemWarnStream.map(new RichMapFunction[String, (String, ArrayBuffer[Long], mutable.HashSet[String],
      mutable.HashSet[String], mutable.HashSet[String])] {
      override def map(value: String): (String, ArrayBuffer[Long], mutable.HashSet[String],
        mutable.HashSet[String], mutable.HashSet[String]) = {
        val valueArr = value.split("\\|", -1)
        val oaSystemSourceIp = new mutable.HashSet[String]()
        val oaSystemDesIp = new mutable.HashSet[String]()
        val oaSystemReferUrl = new mutable.HashSet[String]()
        val timeStampAb = new ArrayBuffer[Long]()

        val userName = valueArr(0)
        val desIp = valueArr(1)
        val sourceIp = valueArr(3)
        val referUrl = valueArr(8)
        val timestamp = valueArr(10).trim.toLong

        oaSystemSourceIp.+=(sourceIp)
        oaSystemDesIp.add(desIp)
        oaSystemReferUrl.add(referUrl)
        timeStampAb.add(timestamp)
        //用户名，时间，源ip，目的ip，url
        (userName, timeStampAb, oaSystemSourceIp, oaSystemDesIp, oaSystemReferUrl)
      }
    }).setParallelism(dealParallelism)
      .keyBy(_._1)
      .window(SlidingProcessingTimeWindows.of(Time.minutes(30), Time.minutes(30)))
      .reduce((t1, t2) => {
        (t1._1, t1._2.++(t2._2), t1._3.++(t2._3), t1._4.++(t2._4), t1._5.++(t2._5))
      })
      .process(new UrlTimeWindowsFuncation).setParallelism(dealParallelism)

    val urlValues = urlValue.map(_._1)
    val urlAlertKafkaValue = urlValue.map(_._2)
    timeValues.addSink(new MySQLSink)
    urlValues.addSink(new MySQLSink)
    //写入云网kafka
    timeValue
      .map(o => {
        JsonUtil.toJson(o._1._1.asInstanceOf[OaSystemCrawlerWarningEntity])
      })
      .sinkTo(producer)
      .setParallelism(1)

    urlValue
      .map(o => {
        JsonUtil.toJson(o._1._1.asInstanceOf[OaSystemCrawlerWarningEntity])
      })
      .sinkTo(producer)
      .setParallelism(1)


    //将告警数据写入告警库topic
    val warningProducer = Sink.kafkaSink(brokerList, warningSinkTopic)
    timeAlertKafkaValue.sinkTo(warningProducer).setParallelism(sinkParallelism)

    urlAlertKafkaValue.sinkTo(warningProducer).setParallelism(sinkParallelism)


    env.execute(jobName)
  }

  class OaSystemCrawlerWarnProcessFuncation extends KeyedProcessFunction[String, (String, Long, mutable.HashSet[String],
    mutable.HashSet[String], mutable.HashSet[String]), ((Object, Boolean), String)] {
    //记录这一用户的srcIp
    lazy val srcIp: ValueState[collection.mutable.HashSet[String]] = getRuntimeContext
      .getState(new ValueStateDescriptor[mutable.HashSet[String]]("srcIp", classOf[collection.mutable.HashSet[String]]))
    lazy val destIp: ValueState[collection.mutable.HashSet[String]] = getRuntimeContext
      .getState(new ValueStateDescriptor[mutable.HashSet[String]]("destIp", classOf[collection.mutable.HashSet[String]]))
    //记录这一用户的访问的URl
    lazy val refererUrl: ValueState[mutable.HashSet[String]] = getRuntimeContext
      .getState(new ValueStateDescriptor[mutable.HashSet[String]]("refererUrl", classOf[mutable.HashSet[String]]))
    //记录这一用户第一次访问时间
    lazy val firstTime: ValueState[Long] = getRuntimeContext
      .getState(new ValueStateDescriptor[Long]("firstTime", classOf[Long]))
    //记录这一用户最后一次访问时间
    lazy val lastTime: ValueState[Long] = getRuntimeContext
      .getState(new ValueStateDescriptor[Long]("firstTime", classOf[Long]))

    private val messagesReceived = new LongCounter()
    private val messagesSend = new LongCounter()

    var allowTime: Long = 0L
    var decideTime: Long = 0L
    var countAllow: Int = 0
    var OaUrl: String = _
    var AddressUrl: String = _
    var opPath = ""
    var operationPersonnelMap = new mutable.HashMap[String, String]()
    var inputKafkaValue = ""

    override def open(parameters: Configuration): Unit = {
      getRuntimeContext.addAccumulator("DetectCrawlerFunction: Messages received by time", messagesReceived)
      getRuntimeContext.addAccumulator("DetectCrawlerFunction: Messages send by time", messagesSend)
      //全局配置
      val globalConf = getRuntimeContext.getExecutionConfig.getGlobalJobParameters.asInstanceOf[Configuration]
      allowTime = globalConf.getLong(ConfigOptions.key(Constants.OA_SYSTEM_CRAWLER_WARN_ONLINE_TIME_ALLOW).longType().defaultValue(0L))
      decideTime = globalConf.getLong(ConfigOptions.key(Constants.OA_SYSTEM_CRAWLER_WARN_ONLINE_TIME_DECIDE).longType().defaultValue(0L))
      OaUrl = globalConf.getString(ConfigOptions.key(Constants.OA_SYSTEM_CRAWLER_WARN_OFFICIAL_URL).stringType().defaultValue(""))
      AddressUrl = globalConf.getString(ConfigOptions.key(Constants.OA_SYSTEM_CRAWLER_WARN_ADDRESS_URL).stringType().defaultValue(""))
      countAllow = globalConf.getInteger(ConfigOptions.key(Constants.OA_SYSTEM_CRAWLER_WARN_COUNT_ALLOW).intType().defaultValue(0))
      opPath = globalConf.getString(ConfigOptions.key(Constants.OPERATION_PERSONNEL_DOWNLOAD_OPERATION_PERSONNEL_PATH_OA).stringType().defaultValue(""))

      val systemType = globalConf.getString(ConfigOptions.key(Constants.FILE_SYSTEM_TYPE).stringType().defaultValue(""))
      val fs = FileSystem.get(URI.create(systemType), new org.apache.hadoop.conf.Configuration())
      val fsDataInputStream = fs.open(new Path(opPath))
      val bufferedReader = new BufferedReader(new InputStreamReader(fsDataInputStream))
      //本地测试
      //      val stream = new FileReader(new File(""))
      //      val bufferedReader = new BufferedReader(stream)

      var line: String = bufferedReader.readLine()
      while (line != null) {
        val values = line.split("\\|", -1)
        if (values.length == 6) {
          operationPersonnelMap.put((values(2) + "@HQ").toUpperCase, values(0) + "|" + values(1) + "|" + values(3) + "|" + values(5))
        }
        line = bufferedReader.readLine()

      }


    }

    override def onTimer(timestamp: Long, ctx: KeyedProcessFunction[String, (String, Long, mutable.HashSet[String],
      mutable.HashSet[String], mutable.HashSet[String]), ((Object, Boolean), String)]#OnTimerContext,
                         out: Collector[((Object, Boolean), String)]): Unit = {

      val last = lastTime.value()
      //如果大于设定的许可时间都没有访问,则清空这个用户下面的记录
      if (timestamp - last > allowTime - 1) {
        srcIp.clear()
        destIp.clear()
        refererUrl.clear()
        firstTime.clear()
        lastTime.clear()

      }
    }

    override def processElement(i: (String, Long, mutable.HashSet[String],
      mutable.HashSet[String], mutable.HashSet[String]),
                                ctx: KeyedProcessFunction[String, (String, Long, mutable.HashSet[String],
                                  mutable.HashSet[String], mutable.HashSet[String]), ((Object, Boolean), String)]#Context,
                                out: Collector[((Object, Boolean), String)]): Unit = {
      messagesReceived.add(1)
      //设置定时器.........在大于设定的许可时间后,执行定时器 （keyby之后的一类数据）
      val userName = i._1
      val osSystemTimestamp = i._2
      val oaSystemSourceIp = i._3
      val oaSystemDesIp = i._4
      val oaSystemReferUrl = i._5
      ctx.timerService().registerEventTimeTimer(osSystemTimestamp + allowTime)


      //对这一用户操作进行判断
      val first = firstTime.value()
      val last = lastTime.value()
      //如果是第一次登录,则最早操作和最近操作是一样的

      if (first != 0) {
        srcIp.update(oaSystemSourceIp)
        destIp.update(oaSystemDesIp)
        refererUrl.update(oaSystemReferUrl)
        firstTime.update(osSystemTimestamp)
        lastTime.update(osSystemTimestamp)
      } else {
        //如果本次的时间减去最后一次操作时间大于设定的允许时间,则认为不是爬虫行为
        if (Math.abs(osSystemTimestamp - last) > allowTime) {
          //更新第一次和最后一次
          srcIp.update(oaSystemSourceIp)
          destIp.update(oaSystemDesIp)
          refererUrl.update(oaSystemReferUrl)
          firstTime.update(osSystemTimestamp)
          lastTime.update(osSystemTimestamp)
        } else {
          //对乱序的数据的时间进行处理
          if (osSystemTimestamp < first) {
            firstTime.update(osSystemTimestamp)
          } else if (osSystemTimestamp > last) {
            lastTime.update(osSystemTimestamp)
          }
          srcIp.update(srcIp.value().++(oaSystemSourceIp))
          destIp.update(destIp.value().++(oaSystemDesIp))
          refererUrl.update(refererUrl.value().++(oaSystemReferUrl))

          //如果本次的时间减去最后一次的操作时间小于设定的允许时间, 但是本次时间减去第一次时间大于设定的决定时间(refererUrl.value().size>= countAllow)
          if (Math.abs(osSystemTimestamp - first) > decideTime) {
            val srcIpSet = srcIp.value()
            val destIpSet = destIp.value()
            val firstValue = firstTime.value()
            val lastValue = lastTime.value()
            val urlOaSet = new mutable.HashSet[String]
            val urlAddressSet = new mutable.HashSet[String]

            val crawlerWarningEntity = new OaSystemCrawlerWarningEntity

            //判断是否为运维人员
            if (operationPersonnelMap.contains(userName.toUpperCase())) {
              //为运维人员
              //set运维1
              crawlerWarningEntity.setIsOm(1)
              val operationPersonnelInfo = operationPersonnelMap(userName.toUpperCase)
              val strings = operationPersonnelInfo.split("\\|", -1)
              crawlerWarningEntity.setPersonalName(strings(0))
              crawlerWarningEntity.setDepartmentName(strings(1))
              crawlerWarningEntity.setPersonalName(strings(2))
              crawlerWarningEntity.setJobName(strings(3))
            } else {
              //set非运维人员0
              crawlerWarningEntity.setIsOm(0)
            }
            for (url <- refererUrl.value()) {
              if (url.contains(OaUrl)) {
                urlOaSet.add(url)
              }
              if (url.contains(AddressUrl)) {
                urlAddressSet.add(url)
              }
            }
            crawlerWarningEntity.setOfficalUrl(urlOaSet.mkString("|"))
            crawlerWarningEntity.setAddrUrl(urlAddressSet.mkString("|"))
            crawlerWarningEntity.setStartTime(new Timestamp(firstValue))
            crawlerWarningEntity.setEndTime(new Timestamp(lastValue))
            crawlerWarningEntity.setUserName(userName)
            crawlerWarningEntity.setSourceIp(srcIpSet.mkString("|"))
            crawlerWarningEntity.setDesIp(destIpSet.mkString("|"))
            crawlerWarningEntity.setCount(refererUrl.value().size.toLong)

            val inPutKafkaValue = userName + "|" + "OA爬虫检测" + "|" + firstValue + "|" +
              "" + "|" + "" + "|" + "" + "|" +
              "" + "|" + srcIpSet.mkString("|") + "|" + "" + "|" +
              destIpSet.mkString("|") + "|" + "" + "|" + "" + "|" +
              "" + "|" + "" + "|" + ""

            out.collect((crawlerWarningEntity, true), inPutKafkaValue)
            messagesSend.add(1)
          }
        }
      }

    }
  }


  class UrlTimeWindowsFuncation extends ProcessFunction[(String, mutable.ArrayBuffer[Long], mutable.HashSet[String],
    mutable.HashSet[String], mutable.HashSet[String]), ((Object, Boolean), String)] {
    private val messagesReceived = new LongCounter()
    private val messagesSend = new LongCounter()

    var allowTime: Long = 0L
    var decideTime: Long = 0L
    var countAllow: Int = 0
    var OaUrl: String = _
    var AddressUrl: String = _
    var opPath = ""
    var operationPersonnelMap = new mutable.HashMap[String, String]()
    var inputKafkaValue = ""

    override def open(parameters: Configuration): Unit = {
      getRuntimeContext.addAccumulator("DetectCrawlerFunction: Messages received by url", messagesReceived)
      getRuntimeContext.addAccumulator("DetectCrawlerFunction: Messages send by url", messagesSend)
      //全局配置
      val globalConf = getRuntimeContext.getExecutionConfig.getGlobalJobParameters.asInstanceOf[Configuration]

      allowTime = globalConf.getLong(ConfigOptions.key(Constants.OA_SYSTEM_CRAWLER_WARN_ONLINE_TIME_ALLOW).longType().defaultValue(0L))
      decideTime = globalConf.getLong(ConfigOptions.key(Constants.OA_SYSTEM_CRAWLER_WARN_ONLINE_TIME_DECIDE).longType().defaultValue(0L))
      OaUrl = globalConf.getString(ConfigOptions.key(Constants.OA_SYSTEM_CRAWLER_WARN_OFFICIAL_URL).stringType().defaultValue(""))
      AddressUrl = globalConf.getString(ConfigOptions.key(Constants.OA_SYSTEM_CRAWLER_WARN_ADDRESS_URL).stringType().defaultValue(""))
      countAllow = globalConf.getInteger(ConfigOptions.key(Constants.OA_SYSTEM_CRAWLER_WARN_COUNT_ALLOW).intType().defaultValue(0))
      opPath = globalConf.getString(ConfigOptions.key(Constants.OPERATION_PERSONNEL_DOWNLOAD_OPERATION_PERSONNEL_PATH_OA).stringType().defaultValue(""))

      val systemType = globalConf.getString(ConfigOptions.key(Constants.FILE_SYSTEM_TYPE).stringType().defaultValue(""))
      val fs = FileSystem.get(URI.create(systemType), new org.apache.hadoop.conf.Configuration())
      val fsDataInputStream = fs.open(new Path(opPath))
      val bufferedReader = new BufferedReader(new InputStreamReader(fsDataInputStream))

      var line: String = bufferedReader.readLine()
      while (line != null) {
        val values = line.split("\\|", -1)
        if (values.length == 6) {
          operationPersonnelMap.put((values(2) + "@HQ").toUpperCase, values(0) + "|" + values(1) + "|" + values(3) + "|" + values(5))
        }
        line = bufferedReader.readLine()

      }


    }

    override def processElement(value: (String, ArrayBuffer[Long], mutable.HashSet[String], mutable.HashSet[String],
      mutable.HashSet[String]), ctx: ProcessFunction[(String, ArrayBuffer[Long], mutable.HashSet[String],
      mutable.HashSet[String], mutable.HashSet[String]), ((Object, Boolean), String)]#Context,
                                out: Collector[((Object, Boolean), String)]): Unit = {
      val userName = value._1
      val osSystemTimestamp = value._2
      val oaSystemSourceIp = value._3
      val oaSystemDesIp = value._4
      val oaSystemReferUrl = value._5
      messagesReceived.add(1)
      if (oaSystemReferUrl.size > countAllow) {
        val urlOaSet = new mutable.HashSet[String]
        val urlAddressSet = new mutable.HashSet[String]
        val crawlerWarningEntity = new OaSystemCrawlerWarningEntity
        //判断是否为运维人员
        if (operationPersonnelMap.contains(userName.toUpperCase())) {
          //为运维人员
          //set运维1
          crawlerWarningEntity.setIsOm(1)
          val operationPersonnelInfo = operationPersonnelMap(userName.toUpperCase)
          val strings = operationPersonnelInfo.split("\\|", -1)
          crawlerWarningEntity.setPersonalName(strings(0))
          crawlerWarningEntity.setDepartmentName(strings(1))
          crawlerWarningEntity.setPersonalName(strings(2))
          crawlerWarningEntity.setJobName(strings(3))
        } else {
          //set非运维人员0
          crawlerWarningEntity.setIsOm(0)
        }
        for (url <- oaSystemReferUrl) {
          if (url.contains(OaUrl) && url.contains("id=")) {
            urlOaSet.add(url)
          }
          if (url.contains(AddressUrl) && url.contains("marapq=")) {
            urlAddressSet.add(url)
          }
        }
        crawlerWarningEntity.setOfficalUrl(urlOaSet.mkString("|"))
        crawlerWarningEntity.setAddrUrl(urlAddressSet.mkString("|"))
        crawlerWarningEntity.setStartTime(new Timestamp(osSystemTimestamp.head))
        crawlerWarningEntity.setEndTime(new Timestamp(osSystemTimestamp.last))
        crawlerWarningEntity.setUserName(userName)
        crawlerWarningEntity.setSourceIp(oaSystemSourceIp.mkString("|"))
        crawlerWarningEntity.setDesIp(oaSystemDesIp.mkString("|"))
        crawlerWarningEntity.setCount(oaSystemReferUrl.size.toLong)

        val inPutKafkaValue = userName + "|" + "OA爬虫检测" + "|" + osSystemTimestamp.head + "|" +
          "" + "|" + "" + "|" + "" + "|" +
          "" + "|" + oaSystemSourceIp.mkString("|") + "|" + "" + "|" +
          oaSystemDesIp.mkString("|") + "|" + "" + "|" + "" + "|" +
          "" + "|" + "" + "|" + ""

        out.collect((crawlerWarningEntity, true), inPutKafkaValue)
        urlOaSet.clear()
        urlAddressSet.clear()

        messagesSend.add(1)
      }


    }
  }
}