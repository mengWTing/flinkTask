package cn.ffcs.is.mss.analyzer.flink.warn

import java.sql.Timestamp
import java.util.Properties

import cn.ffcs.is.mss.analyzer.bean.CrawlerWarningEntity
import cn.ffcs.is.mss.analyzer.druid.model.scala.OperationModel
import cn.ffcs.is.mss.analyzer.flink.warn.CrawlerDetect.DetectCrawlerFunction
import cn.ffcs.is.mss.analyzer.utils.{Constants, IniProperties, JsonUtil}
import org.apache.flink.api.common.accumulators.LongCounter
import org.apache.flink.api.common.functions.{RichFilterFunction, RichMapFunction}
import org.apache.flink.api.common.io.FilePathFilter
import org.apache.flink.api.common.state.{ValueState, ValueStateDescriptor}
import org.apache.flink.api.java.io.TextInputFormat
import org.apache.flink.api.scala.ExecutionEnvironment
import org.apache.flink.configuration.Configuration
import org.apache.flink.core.fs.{FileSystem, Path}
import org.apache.flink.streaming.api.functions.{AssignerWithPunctuatedWatermarks, ProcessFunction}
import org.apache.flink.streaming.api.watermark.Watermark
import org.apache.flink.util.Collector
import org.apache.flink.streaming.api.scala._

import scala.collection.mutable

/**
  * 用于检测用户是否为爬虫行为
  */
object CrawlerDetectBatch {
  def main(args: Array[String]): Unit = {
    //    val args0 = "E:\\ffcs\\mss\\src\\main\\resources\\flink.ini";
    //    val confProperties = new IniProperties(args0)

    val confProperties = new IniProperties(args(0))
    val filePath = "hdfs://A5-302-HW-XH628-027:8020/user/hive/warehouse/mssflow"
    val resultPath = "hdfs://A5-302-HW-XH628-027:8020/crawlerBatch"

    //一些并行度的配置
    val kafkaSourceParallelism = confProperties.getIntValue(Constants.FLINK_CRAWLER_DETECT_CONFIG, Constants.CRAWLER_DETECT_KAFKA_SOURCE_PARALLELISM)
    val dealParallelism = confProperties.getIntValue(Constants.FLINK_CRAWLER_DETECT_CONFIG, Constants.CRAWLER_DETECT_DEAL_PARALLELISM)
    val sinkParallelism = confProperties.getIntValue(Constants.FLINK_CRAWLER_DETECT_CONFIG, Constants.CRAWLER_DETECT_SINK_PARALLELISM)
    //kafka的服务地址


    //flink全局配置
    val parameters: Configuration = new Configuration()

    //允许的在线时长
    parameters.setLong(Constants.CRAWLER_DETECT_ONLINE_TIME_ALLOW, confProperties.getLongValue(Constants.FLINK_CRAWLER_DETECT_CONFIG, Constants.CRAWLER_DETECT_ONLINE_TIME_ALLOW))
    //判断为爬虫的时长阈值
    parameters.setLong(Constants.CRAWLER_DETECT_ONLINE_TIME_DECIDE, confProperties.getLongValue(Constants.FLINK_CRAWLER_DETECT_CONFIG, Constants.CRAWLER_DETECT_ONLINE_TIME_DECIDE))
    //需要过滤的关键字
    parameters.setString(Constants.CRAWLER_DETECT_FILTER_KEY, confProperties.getValue(Constants.FLINK_CRAWLER_DETECT_CONFIG, Constants.CRAWLER_DETECT_FILTER_KEY))


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
    val env = ExecutionEnvironment.getExecutionEnvironment

    //设置flink全局变量
    env.getConfig.setGlobalJobParameters(parameters)


    val fileInputFormat = new TextInputFormat(new Path(filePath))
    fileInputFormat.setNestedFileEnumeration(true)

    fileInputFormat.setFilesFilter(new FilePathFilter {
      override def filterPath(filePath: Path): Boolean = {
        //如果是目录不过滤
        if ("^time=.*".r.findAllIn(filePath.getName).nonEmpty) {
          val time = filePath.getName.substring(5, 13).toLong
          if (!(time >= 20190510)) {
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

    val streamData = env.readFile(fileInputFormat, filePath).setParallelism(10)
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
      }).filter(_._1.isDefined).setParallelism(dealParallelism)
      .map(operationModel => {
        //去掉毫秒
        operationModel._1.head.timeStamp = operationModel._1.head.timeStamp / 1000 * 1000
        (operationModel._1.head, operationModel._2)
      }).setParallelism(dealParallelism)
      .filter(new RichFilterFunction[(OperationModel, String)] {
        val keySet = new mutable.HashSet[String]()

        override def open(parameters: Configuration): Unit = {
          val globalConf = getRuntimeContext.getExecutionConfig.getGlobalJobParameters.asInstanceOf[Configuration]
          val filterKeys = globalConf.getString(Constants.CRAWLER_DETECT_FILTER_KEY, "")
          val keys = filterKeys.split("\\|")
          keys.foreach(_ => keySet.add(_))
        }

        override def filter(tuple: (OperationModel, String)): Boolean = {

          val words = tuple._2.reverse.split("\\.", 2)
          val sufix = words(0).reverse
          !"未知地点".equals(tuple._1.loginPlace) && !"匿名用户".equals(tuple._1.userName) && !"未知系统".equals(tuple._1.loginSystem) && !keySet.contains(sufix)
        }
      }).setParallelism(dealParallelism)
      .map(_._1)

      .groupBy(_.userName)

      //.process(new DetectCrawlerFunction).setParallelism(dealParallelism)



//      .writeAsText(resultPath, FileSystem.WriteMode.OVERWRITE).setParallelism(1)




    //    OperationModel.setPlaceMap("E:\\ffcs\\mss\\src\\main\\resources\\place简化地址.txt")
    //    OperationModel.setSystemMap("E:\\ffcs\\mss\\src\\main\\resources\\system.txt")
    //    OperationModel.setMajorMap("E:\\ffcs\\mss\\src\\main\\resources\\system.txt")
    //    OperationModel.setUsedPlacesMap("E:\\ffcs\\mss\\src\\main\\resources\\usedPlace.txt")
    ////    val streamData = env.readTextFile("C:\\Users\\Administrator\\Desktop\\mss.1527031232994.txt")
    //    val streamData = env.socketTextStream("192.168.1.106", 5555)


    val out = streamData

  }


  class DetectCrawlerFunction extends ProcessFunction[OperationModel, (Object, Boolean)] {

    //记录这一用户第一次操作
    lazy val firstOperation: ValueState[OperationModel] = getRuntimeContext
      .getState(new ValueStateDescriptor[OperationModel]("firstOperation", classOf[OperationModel]))
    //记录这一用户最后一次操作
    lazy val lastOperation: ValueState[OperationModel] = getRuntimeContext
      .getState(new ValueStateDescriptor[OperationModel]("lastOperation", classOf[OperationModel]))
    //记录这一用户的登陆地
    lazy val loginPlace: ValueState[collection.mutable.Set[String]] = getRuntimeContext
      .getState(new ValueStateDescriptor[collection.mutable.Set[String]]("loginPlace", classOf[collection.mutable.Set[String]]))
    //记录这一用户的登录系统
    lazy val loginSystem: ValueState[collection.mutable.Set[String]] = getRuntimeContext
      .getState(new ValueStateDescriptor[collection.mutable.Set[String]]("loginSystem", classOf[collection.mutable.Set[String]]))
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

    override def onTimer(timestamp: Long, ctx: ProcessFunction[OperationModel, (Object, Boolean)]#OnTimerContext, out: Collector[(Object, Boolean)]): Unit = {
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

    override def processElement(i: OperationModel, context: ProcessFunction[OperationModel, (Object, Boolean)]#Context, collector: Collector[(Object, Boolean)]): Unit = {
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
            collector.collect((crawlerWarningEntity, true))
            messagesSend.add(1)
          }
        }
      }

    }
  }


}

