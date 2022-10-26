///*
// * @project mss
// * @company Fujian Fujitsu Communication Software Co., Ltd.
// * @author chenwei
// * @date 2019-05-24 19:59:26
// * @version v1.0
// * @update [no] [date YYYY-MM-DD] [name] [description]
// */
//package cn.ffcs.is.mss.analyzer.flink.warn
//
//import java.sql.Timestamp
//import java.util.Properties
//
//import cn.ffcs.is.mss.analyzer.druid.model.scala.OperationModel
//import cn.ffcs.is.mss.analyzer.flink.sink.MySQLSink
//import cn.ffcs.is.mss.analyzer.utils.{Constants, IniProperties, JsonUtil}
//import org.apache.flink.api.common.accumulators.LongCounter
//import org.apache.flink.api.common.functions.{RichFilterFunction, RichMapFunction}
//import org.apache.flink.api.common.state.{ValueState, ValueStateDescriptor}
//import org.apache.flink.configuration.Configuration
//import org.apache.flink.streaming.api.TimeCharacteristic
//import org.apache.flink.streaming.api.functions.{AssignerWithPunctuatedWatermarks, ProcessFunction}
//import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
//import org.apache.flink.util.Collector
//import org.apache.flink.streaming.api.scala._
//import org.apache.flink.streaming.api.watermark.Watermark
//import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer09
//import org.apache.flink.streaming.util.serialization.SimpleStringSchema
//
//import scala.collection.mutable
//
///**
//  * 用于检测用户是否为爬虫行为
//  */
//object CrawlerDetectNew {
//  def main(args: Array[String]): Unit = {
//
//
//
//    //获取ExecutionEnvironment
//    val env = StreamExecutionEnvironment.getExecutionEnvironment
//    //设置check pointing的间隔
//    //设置流的时间为EventTime
//    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)
//    //设置flink全局变量
//
//
//    val streamData = env.readTextFile("/Users/chenwei/Downloads/32456024-out.txt")
//
//    val out = streamData
//      //            .map(OperationModel.getOperationModel _)
//      //            .filter(_.isDefined)
//      //            .map(_.head)
//      //            .map(JsonUtil.toJson(_))
//      .map(new RichMapFunction[String, (Option[OperationModel], String)] {
//      override def open(parameters: Configuration): Unit = {
//
//        val placePath = "/Users/chenwei/Documents/mss/入库关联文件/place简化地址.txt"
//        val systemPath = "/Users/chenwei/Documents/mss/入库关联文件/system.txt"
//        val usedPlacePath = "/Users/chenwei/Documents/mss/入库关联文件/usedPlace.txt"
//
//        OperationModel.setPlaceMap(placePath)
//        OperationModel.setSystemMap(systemPath)
//        OperationModel.setMajorMap(systemPath)
//        OperationModel.setUsedPlacesMap(usedPlacePath)
//      }
//
//      override def map(value: String): (Option[OperationModel], String) = {
//
//        val v = value.split("\\|", -1)
//        if (v.length>6) {
//          val url = v(6)
//          (OperationModel.getOperationModel(value), url)
//        }else{
//          (OperationModel.getOperationModel(value), "")
//        }
//      }
//    }).filter(t=>{t._2!="" &&t._1.isDefined}).setParallelism(1)
//      .map(operationModel => {
//        //去掉毫秒
//        operationModel._1.head.timeStamp = operationModel._1.head.timeStamp / 1000 * 1000
//        (operationModel._1.head, operationModel._2)
//      }).setParallelism(1)
//      .filter(new RichFilterFunction[(OperationModel, String)] {
//        val keySet = new mutable.HashSet[String]()
//
//        override def open(parameters: Configuration): Unit = {
//          val globalConf = getRuntimeContext.getExecutionConfig.getGlobalJobParameters.asInstanceOf[Configuration]
//          val filterKeys = "css|png"
//          val keys = filterKeys.split("\\|")
//          for(i<-keys) {
//            keySet.add(i)
//          }
//        }
//
//        override def filter(tuple: (OperationModel, String)): Boolean = {
//
//          val words = tuple._2.reverse.split("\\.", 2)
//          val sufix = words(0).reverse
//          !"未知地点".equals(tuple._1.loginPlace) && !"匿名用户".equals(tuple._1.userName) && !"未知系统".equals(tuple._1.loginSystem) && ! keySet.contains(sufix)
//        }
//      }).setParallelism(1)
//      .map(_._1)
//      .assignTimestampsAndWatermarks(new AssignerWithPunctuatedWatermarks[OperationModel] {
//        var lastMaxTimestamp: Long = Long.MinValue
//
//        override def checkAndGetNextWatermark(lastElement: OperationModel, extractedTimestamp: Long): Watermark = {
//          val timestamp = lastElement.timeStamp - 10000
//          if (timestamp > lastMaxTimestamp) {
//            lastMaxTimestamp = timestamp
//            new Watermark(lastMaxTimestamp)
//          } else {
//            null
//          }
//        }
//
//
//        override def extractTimestamp(element: OperationModel, previousElementTimestamp: Long): Long =
//          element.timeStamp
//      }).setParallelism(1)
//      //.keyBy(_.userName)
//      //.process(new DetectCrawlerFunction).setParallelism(1)
//
//        .print()
//
//
//    env.execute()
//  }
//
//
//  class DetectCrawlerFunction extends ProcessFunction[OperationModel, (Object, Boolean)] {
//
//    //记录这一用户第一次操作
//    lazy val firstOperation: ValueState[OperationModel] = getRuntimeContext
//      .getState(new ValueStateDescriptor[OperationModel]("firstOperation", classOf[OperationModel]))
//    //记录这一用户最后一次操作
//    lazy val lastOperation: ValueState[OperationModel] = getRuntimeContext
//      .getState(new ValueStateDescriptor[OperationModel]("lastOperation", classOf[OperationModel]))
//    //记录这一用户的登陆地
//    lazy val loginPlace: ValueState[collection.mutable.Set[String]] = getRuntimeContext
//      .getState(new ValueStateDescriptor[collection.mutable.Set[String]]("loginPlace", classOf[collection.mutable.Set[String]]))
//    //记录这一用户的登录系统
//    lazy val loginSystem: ValueState[collection.mutable.Set[String]] = getRuntimeContext
//      .getState(new ValueStateDescriptor[collection.mutable.Set[String]]("loginSystem", classOf[collection.mutable.Set[String]]))
//    //记录这一用户的srcIp
//    lazy val srcIp: ValueState[collection.mutable.Set[String]] = getRuntimeContext
//      .getState(new ValueStateDescriptor[mutable.Set[String]]("srcIp", classOf[collection.mutable.Set[String]]))
//    //记录这一用户的destIp
//    lazy val destIp: ValueState[collection.mutable.Set[String]] = getRuntimeContext
//      .getState(new ValueStateDescriptor[mutable.Set[String]]("destIp", classOf[collection.mutable.Set[String]]))
//    //记录这一用户的访问次数
//    lazy val accessTimes: ValueState[Long] = getRuntimeContext
//    .getState(new ValueStateDescriptor[Long]("accessTimes", classOf[Long]))
//
//    private val messagesReceived = new LongCounter()
//    private val messagesSend = new LongCounter()
//
//    var allowTime: Long = 0L
//    var decideTime: Long = 0L
//
//    override def open(parameters: Configuration): Unit = {
//      getRuntimeContext.addAccumulator("DetectCrawlerFunction: Messages received", messagesReceived)
//      getRuntimeContext.addAccumulator("DetectCrawlerFunction: Messages send", messagesSend)
//      //全局配置
//      val globalConf = getRuntimeContext.getExecutionConfig.getGlobalJobParameters.asInstanceOf[Configuration]
//      allowTime = 100000L
//      decideTime = 100000L
//
//    }
//
//    override def onTimer(timestamp: Long, ctx: ProcessFunction[OperationModel, (Object, Boolean)]#OnTimerContext, out: Collector[(Object, Boolean)]): Unit = {
//      val last = lastOperation.value()
//      if (last != null) {
//        //如果大于设定的许可时间都没有访问,则清空这个用户下面的记录
//        if (timestamp - last.timeStamp > allowTime - 1) {
//          firstOperation.clear()
//          lastOperation.clear()
//          loginSystem.clear()
//          loginPlace.clear()
//          accessTimes.clear()
//          srcIp.clear()
//          destIp.clear()
//        }
//      }
//    }
//
//    override def processElement(i: OperationModel, context: ProcessFunction[OperationModel, (Object, Boolean)]#Context, collector: Collector[(Object, Boolean)]): Unit = {
//      messagesReceived.add(1)
//      //设置定时器.........在大于设定的许可时间后,执行定时器
//      val currentTime = i.timeStamp
//
//      context.timerService().registerEventTimeTimer(currentTime + allowTime)
//
//      //对这一用户操作进行判断
//      val time = i.timeStamp
//      val first = firstOperation.value()
//      val last = lastOperation.value()
//      val accessCount = accessTimes.value()
//      //如果是第一次登录,则最早操作和最近操作是一样的
//      if (first == null) {
//        firstOperation.update(i)
//        lastOperation.update(i)
//        loginPlace.update(collection.mutable.Set[String](i.loginPlace))
//        loginSystem.update(collection.mutable.Set[String](i.loginSystem))
//        accessTimes.update(1L)
//        srcIp.update(mutable.Set[String](i.sourceIp))
//        destIp.update(mutable.Set[String](i.destinationIp))
//      } else {
//        val lastTime = last.timeStamp
//        val firstTime = first.timeStamp
//        //如果本次的时间减去最后一次操作时间大于设定的允许时间,则认为不是爬虫行为
//        if (time - lastTime > allowTime) {
//          //更新第一次和最后一次
//          firstOperation.update(i)
//          lastOperation.update(i)
//          loginPlace.update(collection.mutable.Set[String](i.loginPlace))
//          loginSystem.update(collection.mutable.Set[String](i.loginSystem))
//          accessTimes.update(1L)
//          srcIp.update(mutable.Set[String](i.sourceIp))
//          destIp.update(mutable.Set[String](i.destinationIp))
//        } else {
//          //对乱序的数据的时间进行处理
//          if (time < firstTime) {
//            firstOperation.update(i)
//          } else if (time > lastTime) {
//            lastOperation.update(i)
//          }
//          loginSystem.update(loginSystem.value().+(i.loginSystem))
//          loginPlace.update(loginPlace.value().+(i.loginPlace))
//          accessTimes.update(accessCount + 1L)
//          srcIp.update(srcIp.value().+(i.sourceIp))
//          destIp.update(destIp.value().+(i.destinationIp))
//          //如果本次的时间减去最后一次的操作时间小于设定的允许时间, 但是本次时间减去第一次时间大于设定的决定时间
//          if (time - firstTime > decideTime) {
//            val srcIpSet = srcIp.value()
//            val destIpSet = destIp.value()
//            val loginPlaceSet = loginPlace.value()
//            val loginSystemSet = loginSystem.value()
//            val connCount = accessTimes.value()
//            val firstEntity = firstOperation.value()
//            val lastEntity = lastOperation.value()
//
//
//          }
//        }
//      }
//
//    }
//  }
//
//
//}
