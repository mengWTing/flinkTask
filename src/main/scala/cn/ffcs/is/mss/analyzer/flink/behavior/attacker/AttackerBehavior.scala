package cn.ffcs.is.mss.analyzer.flink.behavior.attacker

import cn.ffcs.is.mss.analyzer.bean.{AttackerBehaviorScoreEntity, AttackerBehaviorUserNameEntity}
import cn.ffcs.is.mss.analyzer.flink.behavior.attacker.score.ProcessMain
import cn.ffcs.is.mss.analyzer.flink.sink.MySQLSink
import cn.ffcs.is.mss.analyzer.flink.unknowRisk.model.WarningDbModel
import cn.ffcs.is.mss.analyzer.utils.{Constants, IniProperties, JsonUtil}
import org.apache.flink.api.common.accumulators.LongCounter
import org.apache.flink.api.common.functions.RichMapFunction
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.functions.{AssignerWithPunctuatedWatermarks, ProcessFunction}
import org.apache.flink.streaming.api.scala.{StreamExecutionEnvironment, _}
import org.apache.flink.streaming.api.watermark.Watermark
import org.apache.flink.streaming.api.windowing.assigners.SlidingEventTimeWindows
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaConsumer, FlinkKafkaProducer}
import org.apache.flink.streaming.util.serialization.SimpleStringSchema
import org.apache.flink.util.Collector
import org.apache.kafka.clients.producer.ProducerConfig

import java.sql.Timestamp
import java.util.Properties
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

/**
 * @ClassName AttackerBehavior
 * @author hanyu
 * @date 2022/3/14 11:42
 * @description
 * @update [no][date YYYY-MM-DD][name][description]
 * */
object AttackerBehavior {
  def main(args: Array[String]): Unit = {
    //根据传入的参数解析配置文件
    val confProperties = new IniProperties(args(0))
    //        val confProperties = new IniProperties("G:\\ffcs_flink\\mss\\src\\main\\resources\\flink.ini")

    //kafka的服务地址
    val brokerList = confProperties.getValue(Constants.FLINK_COMMON_CONFIG, Constants.KAFKA_BOOTSTRAP_SERVERS)
    //flink消费的group.id
    val groupId = confProperties.getValue(Constants.ATTACKER_BEHAVIOR_CONFIG, Constants.ATTACKER_BEHAVIOR_GROUP_ID)

    //该任务的名字
    val jobName = confProperties.getValue(Constants.ATTACKER_BEHAVIOR_CONFIG, Constants.ATTACKER_BEHAVIOR_JOB_NAME)
    //source并行度
    val sourceParallelism = confProperties.getIntValue(Constants.ATTACKER_BEHAVIOR_CONFIG, Constants
      .ATTACKER_BEHAVIOR_SOURCE_PARALLELISM)
    //处理的并行度
    val dealParallelism = confProperties.getIntValue(Constants.ATTACKER_BEHAVIOR_CONFIG, Constants
      .ATTACKER_BEHAVIOR_DEAL_PARALLELISM)
    //Sink并行度
    val sinkParallelism = confProperties.getIntValue(Constants.ATTACKER_BEHAVIOR_CONFIG, Constants
      .ATTACKER_BEHAVIOR_SINK_PARALLELISM)
    //check pointing的间隔
    val checkpointInterval = confProperties.getLongValue(Constants.ATTACKER_BEHAVIOR_CONFIG, Constants
      .ATTACKER_BEHAVIOR_CHECKPOINT_INTERVAL)
    //该话单的topic
    val topic = confProperties.getValue(Constants.ATTACKER_BEHAVIOR_CONFIG, Constants.ATTACKER_BEHAVIOR_SOURCE_TOPIC)

    val sinkTopic = confProperties.getValue(Constants.ATTACKER_BEHAVIOR_CONFIG, Constants
      .ATTACKER_BEHAVIOR_SINK_TOPIC)

    //全局变量
    //flink全局变量
    val parameters: Configuration = new Configuration()
    //攻击链路

    parameters.setString(Constants.ATTACKER_BEHAVIOR_ALERT_NAME_LINK, confProperties.getValue(Constants.
      ATTACKER_BEHAVIOR_CONFIG, Constants.ATTACKER_BEHAVIOR_ALERT_NAME_LINK))
    parameters.setString(Constants.ATTACKER_BEHAVIOR_PERSONAL_INFO, confProperties.getValue(Constants
      .ATTACKER_BEHAVIOR_CONFIG, Constants.ATTACKER_BEHAVIOR_PERSONAL_INFO))
    parameters.setString(Constants.c3p0_CONFIG_PATH, confProperties.getValue(Constants
      .FLINK_COMMON_CONFIG, Constants.c3p0_CONFIG_PATH))
    parameters.setString(Constants.FILE_SYSTEM_TYPE, confProperties.getValue(Constants
      .FLINK_COMMON_CONFIG, Constants.FILE_SYSTEM_TYPE))

    //设置kafka消费者相关配置
    val props = new Properties()
    //设置kafka集群地址
    props.setProperty("bootstrap.servers", brokerList)
    props.setProperty("group.id", groupId)
    props.setProperty("request.timeout.ms", "600000")
    props.setProperty(ProducerConfig.MAX_BLOCK_MS_CONFIG, "600000")
    props.setProperty(ProducerConfig.BUFFER_MEMORY_CONFIG, "67108864")
    //获取kafka消费者
    val consumer = new FlinkKafkaConsumer[String](topic, new SimpleStringSchema, props)

    //获取kafka 生产者
    val producer = new FlinkKafkaProducer[String](brokerList, sinkTopic, new SimpleStringSchema())

    //获取ExecutionEnvironment
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    //设置check pointing的间隔
    //    env.enableCheckpointing(checkpointInterval)
    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)
    env.getConfig.setGlobalJobParameters(parameters)


    //获取kakfa数据
    val streamValue = env.addSource(consumer).setParallelism(sourceParallelism)
      //    val streamValue = env.socketTextStream("192.168.1.22",9999)
      .map(new RawDataToAttackerBehaviorModelMap)
      .filter(_._1.nonEmpty)
      .assignTimestampsAndWatermarks(new AssignerWithPunctuatedWatermarks[(String, mutable.ArrayBuffer[WarningDbModel])] {
        override def checkAndGetNextWatermark(lastElement: (String, mutable.ArrayBuffer[WarningDbModel]),
                                              extractedTimestamp: Long): Watermark =
          new Watermark(extractedTimestamp - 10000)

        override def extractTimestamp(element: (String, mutable.ArrayBuffer[WarningDbModel]), previousElementTimestamp: Long): Long =
          element._2.head.alertTime
      })
      .keyBy(_._1)
      //      .window(SlidingEventTimeWindows.of(Time.days(5), Time.days(1)))
      .window(SlidingEventTimeWindows.of(Time.days(2), Time.days(1)))
      .reduce((o1, o2) => {
        (o1._1, o1._2 ++ o2._2)
      }).setParallelism(dealParallelism)
      //            .partitionCustom(new Partitioner[String] {
      //              def partition(key: String, numPartitions: Int): Int = {
      //                key.hashCode.abs % numPartitions
      //              }
      //            }, 0)
      .process(new AttackerBehaviorProcessFunction).setParallelism(dealParallelism)



    //画像写入告警表
    streamValue.addSink(new MySQLSink).setParallelism(sinkParallelism)
    //获取攻击评分
    //    val attackerScore = streamValue.map(o => {
    //      try {
    //        val valueEntity = o._1.asInstanceOf[AttackerBehaviorUserNameEntity]
    //        (valueEntity.getAttackBehaviorTime.getTime,
    //          valueEntity.getAttackName + ";" +
    //            valueEntity.getAttackSourceIp + ";" + valueEntity.getAttackSourcePort + ";" + valueEntity.getAttackDesIp + ";" +
    //            valueEntity.getAttackDesPort + ";" +
    //            valueEntity.getAttackStartTime + ";" + valueEntity.getAttackStopTime + ";" +
    //            valueEntity.getAttackOrganization + ";" + valueEntity.getAttackUsedPlace + ";" +
    //            valueEntity.getAttackLoginMajor + ";" + valueEntity.getAttackLoginSystem + ";" +
    //            valueEntity.getAlertNameDetect + ";" + valueEntity.getAlertNameDeliver + ";" + valueEntity.getAlertNameInfiltrate + ";" +
    //            valueEntity.getAlertNameBreakThrough + ";" + valueEntity.getAlertNameControl + ";" + valueEntity.getAlertNameDestroy
    //        )
    //      }
    //    })
    //      .process(new AttackerScoreFunction).setParallelism(dealParallelism)


    val attackerScore = streamValue
      .process(new GetAttackerScoreFunction).setParallelism(dealParallelism)

    attackerScore.addSink(new MySQLSink).setParallelism(sinkParallelism)
    //todo 危险用户发送下游kafka
    attackerScore
      .map(o => {
        JsonUtil.toJson(o._1.asInstanceOf[AttackerBehaviorScoreEntity])
      })
      .addSink(producer)
      .setParallelism(sinkParallelism)

    env.execute(jobName)


  }


  class RawDataToAttackerBehaviorModelMap extends RichMapFunction[String, (String,
    mutable.ArrayBuffer[WarningDbModel])] {

    override def map(value: String): (String, mutable.ArrayBuffer[WarningDbModel]) = {
      val models = new mutable.ArrayBuffer[WarningDbModel]()
      val attackBehaviorModelOpt = WarningDbModel.getWarningDbModel(value)
      if (attackBehaviorModelOpt.isDefined) {
        val attackBehaviorModel = attackBehaviorModelOpt.head
        if (attackBehaviorModel.alertName.nonEmpty) {
          models.append(attackBehaviorModel)
          if (!"未知用户".equals(attackBehaviorModel.userName) && !"匿名用户".equals(attackBehaviorModel.userName)) {
            (attackBehaviorModel.userName, models)
          } else {
            ("未知用户" + "|" + attackBehaviorModel.alertSrcIp, models)
          }
        } else {
          ("", models)
        }
      } else {
        ("", models)
      }
    }
  }

  class AttackerBehaviorProcessFunction extends ProcessFunction[(String, mutable.ArrayBuffer[WarningDbModel]),
    (Object, Boolean)] {
    private val messagesReceived = new LongCounter()
    private val knowMessagesReceived = new LongCounter()
    private val unKnowMessagesReceived = new LongCounter()
    private val messagesSend = new LongCounter()
    var alertNameLink = ""
    var personalInfo = ""

    var alertNameLinkMap = new mutable.HashMap[String, String]
    var personalInfoHashSet = new mutable.HashSet[String]

    var sourceIPHashSet = new mutable.HashSet[String]
    var sourcePortHashSet = new mutable.HashSet[String]
    var desIpHashSet = new mutable.HashSet[String]
    var desPortHashSet = new mutable.HashSet[String]
    var logInMajorHashSet = new mutable.HashSet[String]
    var logInSystemHashSet = new mutable.HashSet[String]
    var usedPlaceHashSet = new mutable.HashSet[String]
    var alertNameHashSet = new mutable.HashSet[String]
    var alertTimeHashSet = new mutable.HashSet[Long]

    var detect = new mutable.HashSet[String]
    var deliver = new mutable.HashSet[String]
    var infiltrate = new mutable.HashSet[String]
    var breakThrough = new mutable.HashSet[String]
    var control = new mutable.HashSet[String]
    var destroy = new mutable.HashSet[String]

    override def open(parameters: Configuration): Unit = {
      val globConf = getRuntimeContext.getExecutionConfig.getGlobalJobParameters.asInstanceOf[Configuration]
      alertNameLink = globConf.getString(Constants.ATTACKER_BEHAVIOR_ALERT_NAME_LINK, "")
      personalInfo = globConf.getString(Constants.ATTACKER_BEHAVIOR_PERSONAL_INFO, "")
      val strings = alertNameLink.split("\\|", -1)
      for (i <- strings) {
        val linkArr = i.split(":", -1)
        alertNameLinkMap.put(linkArr(0), linkArr(1))

      }
      for (j <- personalInfo.split("\\|", -1)) {
        personalInfoHashSet.add(j)
      }

      getRuntimeContext.addAccumulator("Message Received", messagesReceived)
      getRuntimeContext.addAccumulator("Have Name User Message Received", knowMessagesReceived)
      getRuntimeContext.addAccumulator("No Name User Message Received", unKnowMessagesReceived)
      getRuntimeContext.addAccumulator("Messages send", messagesSend)

    }
    //
    //    override def onTimer(timestamp: Long, ctx: ProcessFunction[(String, ArrayBuffer[WarningDbModel]),
    //      (Object, Boolean)]#OnTimerContext, out: Collector[(Object, Boolean)]): Unit = {
    //      //触发清空
    //
    //    }

    override def processElement(value: (String, mutable.ArrayBuffer[WarningDbModel]), ctx: ProcessFunction[(String,
      mutable.ArrayBuffer[WarningDbModel]), (Object, Boolean)]#Context, out: Collector[(Object, Boolean)]): Unit = {
      val userName = value._1
      val modelAB = value._2
      if (modelAB.nonEmpty) {
        messagesReceived.add(1)
        for (i <- modelAB) {
          sourceIPHashSet.add(i.alertSrcIp)
          sourcePortHashSet.add(i.alertSrcPort)
          desIpHashSet.add(i.alertDestIp)
          desPortHashSet.add(i.alertDestPort)
          logInMajorHashSet.add(i.loginMajor)
          logInSystemHashSet.add(i.loginSystem)
          usedPlaceHashSet.add(i.usedPlace)
          alertNameHashSet.add(i.alertName)
          alertTimeHashSet.add(i.alertTime)
        }
      }
      for (alertName <- alertNameHashSet) {
        try {
          val linkIndex = alertNameLinkMap(alertName)
          linkIndex match {
            case "1" => detect.add(alertName)
            case "2" => deliver.add(alertName)
            case "3" => infiltrate.add(alertName)
            case "4" => breakThrough.add(alertName)
            case "5" => control.add(alertName)
            case "6" => destroy.add(alertName)
          }
        } catch {
          case e: Exception =>
        }
      }
      val entity = new AttackerBehaviorUserNameEntity()
      val strings: mutable.HashSet[String] = sourceIPHashSet.intersect(personalInfoHashSet)

      if (personalInfoHashSet.contains(userName) || strings.nonEmpty) {
        entity.setAttackOrganization("0")
      } else {
        entity.setAttackOrganization("1")
      }

      val max: Long = alertTimeHashSet.max
      val min: Long = alertTimeHashSet.min

      if (!userName.contains("|")) {
        knowMessagesReceived.add(1)
        entity.setAttackName(userName)
        entity.setAttackSourceIp(sourceIPHashSet.mkString("|"))
      } else {
        unKnowMessagesReceived.add(1)
        entity.setAttackName(userName.split("\\|", -1)(0))
        entity.setAttackSourceIp(userName.split("\\|", -1)(1))
      }
      entity.setAttackBehaviorTime(new Timestamp(max))
      entity.setAttackSourcePort(sourcePortHashSet.mkString("|"))
      entity.setAttackDesIp(desIpHashSet.mkString("|"))
      entity.setAttackDesPort(desPortHashSet.mkString("|"))
      entity.setAttackLoginMajor(logInMajorHashSet.mkString("|"))
      entity.setAttackLoginSystem(logInSystemHashSet.mkString("|"))
      entity.setAttackUsedPlace(usedPlaceHashSet.mkString("|"))
      entity.setAttackStartTime(new Timestamp(min))
      entity.setAttackStopTime(new Timestamp(max))
      entity.setAlertNameDetect(detect.mkString("|"))
      entity.setAlertNameDeliver(deliver.mkString("|"))
      entity.setAlertNameInfiltrate(infiltrate.mkString("|"))
      entity.setAlertNameBreakThrough(breakThrough.mkString("|"))
      entity.setAlertNameControl(control.mkString("|"))
      entity.setAlertNameDestroy(destroy.mkString("|"))
      out.collect((entity.asInstanceOf[Object], true))
      sourceIPHashSet.clear()
      sourcePortHashSet.clear()
      desIpHashSet.clear()
      desPortHashSet.clear()
      logInMajorHashSet.clear()
      logInSystemHashSet.clear()
      usedPlaceHashSet.clear()
      alertNameHashSet.clear()
      alertTimeHashSet.clear()
      detect.clear()
      deliver.clear()
      infiltrate.clear()
      breakThrough.clear()
      control.clear()
      destroy.clear()
      messagesSend.add(1)

    }
  }

  //  class AttackerScoreFunction extends ProcessFunction[(Long, String), (Object, Boolean)] {
  //    override def processElement(value: (Long, String), ctx: ProcessFunction[(Long, String), (Object, Boolean)]#Context,
  //                                out: Collector[(Object, Boolean)]): Unit = {
  //      val attackBehaviorTime = value._1
  //      val attackerInfo = value._2
  //      val infoArr = attackerInfo.split(";", -1)
  //      var userName = ""
  //      if (infoArr.head.nonEmpty) {
  //        if (!infoArr.head.equals("未知用户")) {
  //          userName = infoArr.head
  //        } else {
  //          userName = infoArr(1)
  //        }
  //      }
  //      val attackerScore: String = ProcessMain.getScore(attackerInfo)
  //      val entity = new AttackerBehaviorScoreEntity
  //      entity.setAttackerTiame(new Timestamp(attackBehaviorTime))
  //      entity.setAttackerUsername(userName)
  //      entity.setAttackerScore(attackerScore)
  //      out.collect((entity.asInstanceOf[Object], true))
  //
  //    }
  //  }

  class GetAttackerScoreFunction extends ProcessFunction[(Object, Boolean), (Object, Boolean)] {
    override def processElement(value: (Object, Boolean), ctx: ProcessFunction[(Object, Boolean), (Object, Boolean)]#Context,
                                out: Collector[(Object, Boolean)]): Unit = {

      try {
        val valueEntity = value._1.asInstanceOf[AttackerBehaviorUserNameEntity]
        val attackBehaviorTime = valueEntity.getAttackBehaviorTime.getTime
        val attackerInfo = valueEntity.getAttackName + ";" +
          valueEntity.getAttackSourceIp + ";" + valueEntity.getAttackSourcePort + ";" + valueEntity.getAttackDesIp + ";" +
          valueEntity.getAttackDesPort + ";" +
          valueEntity.getAttackStartTime + ";" + valueEntity.getAttackStopTime + ";" +
          valueEntity.getAttackOrganization + ";" + valueEntity.getAttackUsedPlace + ";" +
          valueEntity.getAttackLoginMajor + ";" + valueEntity.getAttackLoginSystem + ";" +
          valueEntity.getAlertNameDetect + ";" + valueEntity.getAlertNameDeliver + ";" + valueEntity.getAlertNameInfiltrate + ";" +
          valueEntity.getAlertNameBreakThrough + ";" + valueEntity.getAlertNameControl + ";" + valueEntity.getAlertNameDestroy
        var userName = ""
        if (valueEntity.getAttackName.equals("未知用户")) {
          userName = valueEntity.getAttackSourceIp
        } else {
          userName = valueEntity.getAttackName
        }
        val attackerScore: String = ProcessMain.getScore(attackerInfo)
        val entity = new AttackerBehaviorScoreEntity
        entity.setAttackerTiame(new Timestamp(attackBehaviorTime))
        entity.setAttackerUsername(userName)
        entity.setAttackerScore(attackerScore)
        out.collect((entity.asInstanceOf[Object], true))
      } catch {
        case e: Exception =>
      }
    }
  }

}
