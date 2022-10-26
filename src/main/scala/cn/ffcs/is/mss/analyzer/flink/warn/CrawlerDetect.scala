package cn.ffcs.is.mss.analyzer.flink.warn

import java.sql.Timestamp
import java.util.Properties
import cn.ffcs.is.mss.analyzer.bean.CrawlerWarningEntity
import cn.ffcs.is.mss.analyzer.druid.model.scala.OperationModel
import cn.ffcs.is.mss.analyzer.flink.sink.MySQLSink
import cn.ffcs.is.mss.analyzer.flink.unknowRisk.funcation.UnknownRiskUtil.getInputKafkavalue
import cn.ffcs.is.mss.analyzer.utils.{Constants, IniProperties, JsonUtil}
import org.apache.flink.api.common.accumulators.LongCounter
import org.apache.flink.api.common.functions.{RichFilterFunction, RichMapFunction}
import org.apache.flink.api.common.state.{ValueState, ValueStateDescriptor}
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.datastream.DataStreamSink
import org.apache.flink.streaming.api.functions.{AssignerWithPunctuatedWatermarks, ProcessFunction}
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.util.Collector
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.watermark.Watermark
import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaConsumer, FlinkKafkaProducer}
import org.apache.flink.streaming.util.serialization.SimpleStringSchema
import org.apache.kafka.common.metrics.stats.Histogram.ConstantBinScheme

import scala.collection.mutable

/**
 * 用于检测用户是否为爬虫行为
 */
object CrawlerDetect {
  def main(args: Array[String]): Unit = {
    //        val args0 = "E:\\ffcs\\mss\\src\\main\\resources\\flink.ini";
    //        val confProperties = new IniProperties(args0)

    val confProperties = new IniProperties(args(0))

    //任务名
    val jobName = confProperties.getValue(Constants.FLINK_CRAWLER_DETECT_CONFIG, Constants.CRAWLER_DETECT_JOB_NAME)
    //kafka source的名字
    val kafkaSourceName = confProperties.getValue(Constants.FLINK_CRAWLER_DETECT_CONFIG, Constants
      .CRAWLER_DETECT_KAFKA_SOURCE_NAME)
    //mysql sink的名字
    val sqlSinkName = confProperties.getValue(Constants.FLINK_CRAWLER_DETECT_CONFIG, Constants
      .CRAWLER_DETECT_SQL_SINK_NAME)

    //一些并行度的配置
    val kafkaSourceParallelism = confProperties.getIntValue(Constants.FLINK_CRAWLER_DETECT_CONFIG, Constants
      .CRAWLER_DETECT_KAFKA_SOURCE_PARALLELISM)
    val dealParallelism = confProperties.getIntValue(Constants.FLINK_CRAWLER_DETECT_CONFIG, Constants
      .CRAWLER_DETECT_DEAL_PARALLELISM)
    val sinkParallelism = confProperties.getIntValue(Constants.FLINK_CRAWLER_DETECT_CONFIG, Constants
      .CRAWLER_DETECT_SINK_PARALLELISM)
    //kafka的服务地址
    val brokerList = confProperties.getValue(Constants.FLINK_COMMON_CONFIG, Constants.KAFKA_BOOTSTRAP_SERVERS)
    //flink消费的group.id
    val groupId = confProperties.getValue(Constants.FLINK_CRAWLER_DETECT_CONFIG, Constants.CRAWLER_DETECT_GROUP_ID)
    //kafka的topic
    val topic = confProperties.getValue(Constants.OPERATION_FLINK_TO_DRUID_CONFIG, Constants.OPERATION_TOPIC)
    //kafka sink 的topic
    val kafkaSinkTopic = confProperties.getValue(Constants.FLINK_CRAWLER_DETECT_CONFIG, Constants
      .CRAWLER_DETECT_KAFKA_SINK_TOPIC)
    val warningSinkTopic = confProperties.getValue(Constants.WARNING_FLINK_TO_DRUID_CONFIG, Constants
      .WARNING_TOPIC)

    //kafka sink的名字
    val kafkaSinkName = confProperties.getValue(Constants.FLINK_CRAWLER_DETECT_CONFIG, Constants
      .CRAWLER_DETECT_KAFKA_SINK_NAME)
    //写入kafka的并行度
    val kafkaSinkParallelism = confProperties.getIntValue(Constants.FLINK_CRAWLER_DETECT_CONFIG, Constants
      .CRAWLER_DETECT_KAFKA_SINK_PARALLELISM)


    //flink全局配置
    val parameters: Configuration = new Configuration()
    //c3p0连接池配置文件路径
    parameters.setString(Constants.c3p0_CONFIG_PATH, confProperties.getValue(Constants.FLINK_COMMON_CONFIG, Constants
      .c3p0_CONFIG_PATH))
    //允许的在线时长
    parameters.setLong(Constants.CRAWLER_DETECT_ONLINE_TIME_ALLOW, confProperties.getLongValue(Constants
      .FLINK_CRAWLER_DETECT_CONFIG, Constants.CRAWLER_DETECT_ONLINE_TIME_ALLOW))
    //判断为爬虫的时长阈值
    parameters.setLong(Constants.CRAWLER_DETECT_ONLINE_TIME_DECIDE, confProperties.getLongValue(Constants
      .FLINK_CRAWLER_DETECT_CONFIG, Constants.CRAWLER_DETECT_ONLINE_TIME_DECIDE))
    //需要过滤的关键字
    parameters.setString(Constants.CRAWLER_DETECT_FILTER_KEY, confProperties.getValue(Constants
      .FLINK_CRAWLER_DETECT_CONFIG, Constants.CRAWLER_DETECT_FILTER_KEY))
    //文件系统类型
    parameters.setString(Constants.FILE_SYSTEM_TYPE, confProperties.getValue(Constants.FLINK_COMMON_CONFIG, Constants
      .FILE_SYSTEM_TYPE))
    //机器人埋点URL
    parameters.setString(Constants.CRAWLER_DETECT_ROBOT_URL, confProperties.getValue(Constants
      .FLINK_CRAWLER_DETECT_CONFIG, Constants.CRAWLER_DETECT_ROBOT_URL))

    //check pointing 间隔
    val checkpointInterval = confProperties.getLongValue(Constants.FLINK_CRAWLER_DETECT_CONFIG, Constants
      .CRAWLER_DETECT_CHECKPOINT_INTERVAL)

    //ip-地点关联文件路径
    val placePath = confProperties.getValue(Constants.OPERATION_FLINK_TO_DRUID_CONFIG, Constants
      .OPERATION_PLACE_PATH)
    //host-系统名关联文件路径
    val systemPath = confProperties.getValue(Constants.OPERATION_FLINK_TO_DRUID_CONFIG, Constants
      .OPERATION_SYSTEM_PATH)
    //用户名-常用登录地关联文件路径
    val usedPlacePath = confProperties.getValue(Constants.OPERATION_FLINK_TO_DRUID_CONFIG,
      Constants.OPERATION_USEDPLACE_PATH)


    //获取ExecutionEnvironment
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    //设置check pointing的间隔
    env.enableCheckpointing(checkpointInterval)
    //设置流的时间为EventTime
    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)
    //设置flink全局变量
    env.getConfig.setGlobalJobParameters(parameters)

    //设置kafka消费者相关配置
    val props = new Properties()
    //设置kafka集群地址
    props.setProperty("bootstrap.servers", brokerList)
    //设置flink消费的group.id
    props.setProperty("group.id", groupId)



    //        OperationModel.setPlaceMap("E:\\ffcs\\mss\\src\\main\\resources\\place简化地址.txt")
    //        OperationModel.setSystemMap("E:\\ffcs\\mss\\src\\main\\resources\\system.txt")
    //        OperationModel.setMajorMap("E:\\ffcs\\mss\\src\\main\\resources\\system.txt")
    //        OperationModel.setUsedPlacesMap("E:\\ffcs\\mss\\src\\main\\resources\\usedPlace.txt")
    //    //    val streamData = env.readTextFile("C:\\Users\\Administrator\\Desktop\\mss.1527031232994.txt")
    //        val streamData = env.socketTextStream("192.168.1.106", 5555)

    //获取消费者
    val consumer = new FlinkKafkaConsumer[String](topic, new SimpleStringSchema, props).setStartFromLatest()

    //获取kafka的生产者
    val producer = new FlinkKafkaProducer[String](brokerList, kafkaSinkTopic, new SimpleStringSchema())
    val warningProducer = new FlinkKafkaProducer[String](brokerList, warningSinkTopic, new
        SimpleStringSchema())

    //获取kafka数据
    val streamData = env.addSource(consumer).setParallelism(kafkaSourceParallelism)
      .uid(kafkaSourceName).name(kafkaSourceName)


    val sinkData = streamData
      //            .map(OperationModel.getOperationModel _)
      //            .filter(_.isDefined)
      //            .map(_.head)
      //            .map(JsonUtil.toJson(_))
      .map(new RichMapFunction[String, (Option[OperationModel], String)] {
        override def open(parameters: Configuration): Unit = {
          OperationModel.setPlaceMap(placePath)
          OperationModel.setSystemMap(systemPath)
          OperationModel.setMajorMap(systemPath)
          OperationModel.setUsedPlacesMap(usedPlacePath)
        }

        override def map(value: String): (Option[OperationModel], String) = {

          val v = value.split("\\|", -1)
          if (v.length > 6) {
            val url = v(6)
            (OperationModel.getOperationModel(value), url)
          } else {
            (OperationModel.getOperationModel(value), "")
          }
        }
      }).setParallelism(dealParallelism)
      .filter(t => {
        t._2 != "" && t._1.isDefined
      }).setParallelism(dealParallelism)
      .map(operationModel => {
        //去掉毫秒
        operationModel._1.head.timeStamp = operationModel._1.head.timeStamp / 1000 * 1000
        (operationModel._1.head, operationModel._2)
      }).setParallelism(dealParallelism)
      .filter(new RichFilterFunction[(OperationModel, String)] {
        val keySet = new mutable.HashSet[String]()
        val urlSet = new mutable.HashSet[String]()

        override def open(parameters: Configuration): Unit = {
          val globalConf = getRuntimeContext.getExecutionConfig.getGlobalJobParameters.asInstanceOf[Configuration]
          val filterKeys = globalConf.getString(Constants.CRAWLER_DETECT_FILTER_KEY, "")
          val robotUrls = globalConf.getString(Constants.CRAWLER_DETECT_ROBOT_URL, "")
          val keys = filterKeys.split("\\|")
          for (i <- keys) {
            keySet.add(i)
          }
          val urlSplits = robotUrls.split("\\|")
          for (i <- urlSplits) {
            urlSet.add(i)
          }
        }

        override def filter(tuple: (OperationModel, String)): Boolean = {

          val words = tuple._2.reverse.split("\\.", 2)
          val firstUrl = tuple._2.split("\\?")(0)
          val sufix = words(0).reverse
          !"未知地点".equals(tuple._1.loginPlace) && !"匿名用户".equals(tuple._1.userName) && !"未知系统".equals(tuple
            ._1.loginSystem) && !keySet.contains(sufix) && !urlSet.contains(firstUrl)
        }
      }).setParallelism(dealParallelism)
      .map(_._1).setParallelism(dealParallelism)
      .assignTimestampsAndWatermarks(new AssignerWithPunctuatedWatermarks[OperationModel] {
        var lastMaxTimestamp: Long = Long.MinValue

        override def checkAndGetNextWatermark(lastElement: OperationModel, extractedTimestamp: Long): Watermark = {
          val timestamp = lastElement.timeStamp - 10000
          if (timestamp > lastMaxTimestamp) {
            lastMaxTimestamp = timestamp
            new Watermark(lastMaxTimestamp)
          } else {
            null
          }
        }


        override def extractTimestamp(element: OperationModel, previousElementTimestamp: Long): Long =
          element.timeStamp
      }).setParallelism(dealParallelism)
      .keyBy(_.userName)
      .process(new DetectCrawlerFunction).setParallelism(dealParallelism)

    val value: DataStream[(Object, Boolean)] = sinkData.map(_._1)
    val alertKafkaValue = sinkData.map(_._2)

    value
      .addSink(new MySQLSink).uid(sqlSinkName).name(sqlSinkName).setParallelism(sinkParallelism)

    value
      .map(o => {
        JsonUtil.toJson(o._1.asInstanceOf[CrawlerWarningEntity])
      })
      .addSink(producer)
      .uid(kafkaSinkName)
      .setParallelism(kafkaSinkParallelism)

    alertKafkaValue.addSink(warningProducer).setParallelism(kafkaSinkParallelism)

    env.execute(jobName)
  }


  class DetectCrawlerFunction extends ProcessFunction[OperationModel, ((Object, Boolean), String)] {

    //记录这一用户第一次操作
    lazy val firstOperation: ValueState[OperationModel] = getRuntimeContext
      .getState(new ValueStateDescriptor[OperationModel]("firstOperation", classOf[OperationModel]))
    //记录这一用户最后一次操作
    lazy val lastOperation: ValueState[OperationModel] = getRuntimeContext
      .getState(new ValueStateDescriptor[OperationModel]("lastOperation", classOf[OperationModel]))
    //记录这一用户的登陆地
    lazy val loginPlace: ValueState[collection.mutable.Set[String]] = getRuntimeContext
      .getState(new ValueStateDescriptor[collection.mutable.Set[String]]("loginPlace", classOf[collection.mutable
      .Set[String]]))
    //记录这一用户的登录系统
    lazy val loginSystem: ValueState[collection.mutable.Set[String]] = getRuntimeContext
      .getState(new ValueStateDescriptor[collection.mutable.Set[String]]("loginSystem", classOf[collection.mutable
      .Set[String]]))
    //记录这一用户的srcIp
    lazy val srcIp: ValueState[collection.mutable.Set[String]] = getRuntimeContext
      .getState(new ValueStateDescriptor[mutable.Set[String]]("srcIp", classOf[collection.mutable.Set[String]]))
    //记录这一用户的destIp
    lazy val destIp: ValueState[collection.mutable.Set[String]] = getRuntimeContext
      .getState(new ValueStateDescriptor[mutable.Set[String]]("destIp", classOf[collection.mutable.Set[String]]))
    //记录这一用户的访问次数
    lazy val accessTimes: ValueState[Long] = getRuntimeContext
      .getState(new ValueStateDescriptor[Long]("accessTimes", classOf[Long]))

    private val messagesReceived = new LongCounter()
    private val messagesSend = new LongCounter()

    var allowTime: Long = 0L
    var decideTime: Long = 0L

    override def open(parameters: Configuration): Unit = {
      getRuntimeContext.addAccumulator("DetectCrawlerFunction: Messages received", messagesReceived)
      getRuntimeContext.addAccumulator("DetectCrawlerFunction: Messages send", messagesSend)
      //全局配置
      val globalConf = getRuntimeContext.getExecutionConfig.getGlobalJobParameters.asInstanceOf[Configuration]
      allowTime = globalConf.getLong(Constants.CRAWLER_DETECT_ONLINE_TIME_ALLOW, 0L)
      decideTime = globalConf.getLong(Constants.CRAWLER_DETECT_ONLINE_TIME_DECIDE, 0L)

    }

    override def onTimer(timestamp: Long, ctx: ProcessFunction[OperationModel, ((Object, Boolean), String)]#OnTimerContext,
                         out: Collector[((Object, Boolean), String)]): Unit = {
      val last = lastOperation.value()
      if (last != null) {
        //如果大于设定的许可时间都没有访问,则清空这个用户下面的记录
        if (timestamp - last.timeStamp > allowTime - 1) {
          firstOperation.clear()
          lastOperation.clear()
          loginSystem.clear()
          loginPlace.clear()
          accessTimes.clear()
          srcIp.clear()
          destIp.clear()
        }
      }
    }

    override def processElement(i: OperationModel, context: ProcessFunction[OperationModel, ((Object, Boolean), String)
    ]#Context, collector: Collector[((Object, Boolean), String)]): Unit = {
      messagesReceived.add(1)
      //设置定时器.........在大于设定的许可时间后,执行定时器
      val currentTime = i.timeStamp

      context.timerService().registerEventTimeTimer(currentTime + allowTime)

      //对这一用户操作进行判断
      val time = i.timeStamp
      val first = firstOperation.value()
      val last = lastOperation.value()
      val accessCount = accessTimes.value()
      //如果是第一次登录,则最早操作和最近操作是一样的
      if (first == null) {
        firstOperation.update(i)
        lastOperation.update(i)
        loginPlace.update(collection.mutable.Set[String](i.loginPlace))
        loginSystem.update(collection.mutable.Set[String](i.loginSystem))
        accessTimes.update(1L)
        srcIp.update(mutable.Set[String](i.sourceIp))
        destIp.update(mutable.Set[String](i.destinationIp))
      } else {
        val lastTime = last.timeStamp
        val firstTime = first.timeStamp
        //如果本次的时间减去最后一次操作时间大于设定的允许时间,则认为不是爬虫行为
        if (time - lastTime > allowTime) {
          //更新第一次和最后一次
          firstOperation.update(i)
          lastOperation.update(i)
          loginPlace.update(collection.mutable.Set[String](i.loginPlace))
          loginSystem.update(collection.mutable.Set[String](i.loginSystem))
          accessTimes.update(1L)
          srcIp.update(mutable.Set[String](i.sourceIp))
          destIp.update(mutable.Set[String](i.destinationIp))
        } else {
          //对乱序的数据的时间进行处理
          if (time < firstTime) {
            firstOperation.update(i)
          } else if (time > lastTime) {
            lastOperation.update(i)
          }
          loginSystem.update(loginSystem.value().+(i.loginSystem))
          loginPlace.update(loginPlace.value().+(i.loginPlace))
          accessTimes.update(accessCount + 1L)
          srcIp.update(srcIp.value().+(i.sourceIp))
          destIp.update(destIp.value().+(i.destinationIp))
          //如果本次的时间减去最后一次的操作时间小于设定的允许时间, 但是本次时间减去第一次时间大于设定的决定时间
          if (time - firstTime > decideTime) {
            val srcIpSet = srcIp.value()
            val destIpSet = destIp.value()
            val loginPlaceSet = loginPlace.value()
            val loginSystemSet = loginSystem.value()
            val connCount = accessTimes.value()
            val firstEntity = firstOperation.value()
            val lastEntity = lastOperation.value()

            val crawlerWarningEntity = new CrawlerWarningEntity
            crawlerWarningEntity.setUserName(first.userName)
            crawlerWarningEntity.setStartDatetime(new Timestamp(firstEntity.timeStamp))
            crawlerWarningEntity.setEndDatetime(new Timestamp(lastEntity.timeStamp))
            crawlerWarningEntity.setConncount(connCount)
            crawlerWarningEntity.setLoginPlace(loginPlaceSet.mkString(";"))
            crawlerWarningEntity.setLoginSystem(loginSystemSet.mkString(";"))
            crawlerWarningEntity.setSourceIp(srcIpSet.mkString(";"))
            crawlerWarningEntity.setDestinationIp(destIpSet.mkString(";"))
            val outValue = getInputKafkavalue(i, "", "爬虫检测", "")

            collector.collect((crawlerWarningEntity, true), outValue)
            messagesSend.add(1)
          }
        }
      }

    }
  }


}

