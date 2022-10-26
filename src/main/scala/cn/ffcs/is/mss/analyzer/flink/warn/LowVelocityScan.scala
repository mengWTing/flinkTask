package cn.ffcs.is.mss.analyzer.flink.warn

import java.sql.Timestamp
import java.util.Properties
import cn.ffcs.is.mss.analyzer.bean.{DdosWarnEntity, LowVelocityScanEntity}
import cn.ffcs.is.mss.analyzer.druid.model.scala.QuintetModel
import cn.ffcs.is.mss.analyzer.flink.sink.MySQLSink
import cn.ffcs.is.mss.analyzer.utils.{Constants, IniProperties, JsonUtil}
import org.apache.flink.api.common.accumulators.LongCounter
import org.apache.flink.api.common.functions.RichMapFunction
import org.apache.flink.api.common.state.{ValueState, ValueStateDescriptor}
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.functions.{AssignerWithPunctuatedWatermarks, ProcessFunction}
import org.apache.flink.streaming.api.scala.{StreamExecutionEnvironment, _}
import org.apache.flink.streaming.api.watermark.Watermark
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaConsumer, FlinkKafkaProducer}
import org.apache.flink.streaming.util.serialization.SimpleStringSchema
import org.apache.flink.util.Collector

import scala.collection.JavaConversions._
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

/**
 * @title LowVelocityScan
 * @author hanyu
 * @date 2020-08-24 11:55
 * @description 基于持续增量模型的端口扫描
 * @update [no][date YYYY-MM-DD][name][description]
 */
object LowVelocityScan {
  def main(args: Array[String]): Unit = {
    val confProperties = new IniProperties(args(0))
    //任务的名字
    val jobName = confProperties.getValue(Constants.LOW_VELOCITY_SCAN_CONFIG, Constants
      .LOW_VELOCITY_SCAN_JOB_NAME)


    //并行度
    val sourceParallelism = confProperties.getIntValue(Constants.LOW_VELOCITY_SCAN_CONFIG,
      Constants.LOW_VELOCITY_SCAN_SOURCE_PARALLELISM)
    val sinkParallelism = confProperties.getIntValue(Constants.LOW_VELOCITY_SCAN_CONFIG,
      Constants.LOW_VELOCITY_SCAN_SINK_PARALLELISM)
    val dealParallelism = confProperties.getIntValue(Constants.LOW_VELOCITY_SCAN_CONFIG,
      Constants.LOW_VELOCITY_SCAN_DEAL_PARALLELISM)


    //check pointing的间隔
    val checkpointInterval = confProperties.getLongValue(Constants.LOW_VELOCITY_SCAN_CONFIG,
      Constants.LOW_VELOCITY_SCAN_CHECKPOINT_INTERVAL)


    //kafka的服务地址
    val brokerList = confProperties.getValue(Constants.FLINK_COMMON_CONFIG, Constants
      .KAFKA_BOOTSTRAP_SERVERS)
    //flink消费的group.id
    val groupId = confProperties.getValue(Constants.LOW_VELOCITY_SCAN_CONFIG, Constants
      .LOW_VELOCITY_SCAN_GROUP_ID)
    //kafka source 的topic
    val topic = confProperties.getValue(Constants.LOW_VELOCITY_SCAN_CONFIG, Constants
      .LOW_VELOCITY_SCAN_KAFKA_SOURCE_TOPIC)
    //kafka sink topic
    val kafkaSinkTopic = confProperties.getValue(Constants.LOW_VELOCITY_SCAN_CONFIG,
      Constants.LOW_VELOCITY_SCAN_KAFKA_SINK_TOPIC)
    //kafka sink的名字
    val kafkaSinkName = confProperties.getValue(Constants.LOW_VELOCITY_SCAN_CONFIG, Constants.LOW_VELOCITY_SCAN_KAFKA_SINK_NAME)
    //写入kafka的并行度
    val kafkaSinkParallelism = confProperties.getIntValue(Constants.LOW_VELOCITY_SCAN_CONFIG, Constants.LOW_VELOCITY_SCAN_KAFKA_SINK_PARALLELISM)


    //水平统计窗口大小
    val levelStatisticsTimeWindow = confProperties.getIntValue(Constants.LOW_VELOCITY_SCAN_CONFIG,
      Constants.LOW_VELOCITY_SCAN_LEVEL_STATISTICS_TIMEWINDOW)
    //水平过滤阈值
    val levelFilterSize = confProperties.getIntValue(Constants.LOW_VELOCITY_SCAN_CONFIG,
      Constants.LOW_VELOCITY_SCAN_LEVEL_FILTER_SIZE)

    //垂直统计窗口大小
    val verticalStatisticsTimeWindow = confProperties.getIntValue(Constants.LOW_VELOCITY_SCAN_CONFIG,
      Constants.LOW_VELOCITY_SCAN_VERTICAL_STATISTICS_TIMEWINDOW)
    //垂直过滤阈值
    val verticalFilterSize = confProperties.getIntValue(Constants.LOW_VELOCITY_SCAN_CONFIG,
      Constants.LOW_VELOCITY_SCAN_VERTICAL_FILTER_SIZE)

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


    //水平最大时间窗口
    parameters.setLong(Constants.LOW_VELOCITY_SCAN_LEVEL_STATISTICS_TIMESTEP_SIZE_MAX, confProperties.getLongValue(Constants.
      LOW_VELOCITY_SCAN_CONFIG, Constants.LOW_VELOCITY_SCAN_LEVEL_STATISTICS_TIMESTEP_SIZE_MAX))
    //水平统计窗口数
    parameters.setInteger(Constants.LOW_VELOCITY_SCAN_LEVEL_STATISTICS_TIMEWINDOW_COUNT, confProperties.getIntValue(Constants.
      LOW_VELOCITY_SCAN_CONFIG, Constants.LOW_VELOCITY_SCAN_LEVEL_STATISTICS_TIMEWINDOW_COUNT))
    //水平熵
    parameters.setDouble(Constants.LOW_VELOCITY_SCAN_LEVEL_COMENTROPY, confProperties.getFloatValue(Constants.
      LOW_VELOCITY_SCAN_CONFIG, Constants.LOW_VELOCITY_SCAN_LEVEL_COMENTROPY))
    //水平概率
    parameters.setDouble(Constants.LOW_VELOCITY_SCAN_LEVEL_INPUTOCTES_PROBABILITY, confProperties.getFloatValue(Constants.
      LOW_VELOCITY_SCAN_CONFIG, Constants.LOW_VELOCITY_SCAN_LEVEL_INPUTOCTES_PROBABILITY))


    //垂直最大时间窗口
    parameters.setLong(Constants.LOW_VELOCITY_SCAN_VERTICAL_STATISTICS_TIMESTEP_SIZE_MAX, confProperties.getLongValue(Constants.
      LOW_VELOCITY_SCAN_CONFIG, Constants.LOW_VELOCITY_SCAN_VERTICAL_STATISTICS_TIMESTEP_SIZE_MAX))
    //垂直统计窗口数
    parameters.setInteger(Constants.LOW_VELOCITY_SCAN_VERTICAL_STATISTICS_TIMEWINDOW_COUNT, confProperties.getIntValue(Constants.
      LOW_VELOCITY_SCAN_CONFIG, Constants.LOW_VELOCITY_SCAN_VERTICAL_STATISTICS_TIMEWINDOW_COUNT))
    //垂直熵
    parameters.setDouble(Constants.LOW_VELOCITY_SCAN_VERTICAL_COMENTROPY, confProperties.getFloatValue(Constants.
      LOW_VELOCITY_SCAN_CONFIG, Constants.LOW_VELOCITY_SCAN_VERTICAL_COMENTROPY))
    //垂直概率
    parameters.setDouble(Constants.LOW_VELOCITY_SCAN_VERTICAL_INPUTOCTES_PROBABILITY, confProperties.getFloatValue(Constants.
      LOW_VELOCITY_SCAN_CONFIG, Constants.LOW_VELOCITY_SCAN_VERTICAL_INPUTOCTES_PROBABILITY))


    //设置kafka消费者相关配置
    val props = new Properties()
    //设置kafka集群地址
    props.setProperty("bootstrap.servers", brokerList)
    //设置flink消费的group.id
    props.setProperty("group.id", groupId + "test")
    //获取kafka消费者
    val consumer = new FlinkKafkaConsumer[String](topic, new SimpleStringSchema, props)
      .setStartFromLatest()
    //获取kafka 生产者
    val producer = new FlinkKafkaProducer[String](brokerList, kafkaSinkTopic, new
        SimpleStringSchema())


    //获取ExecutionEnvironment
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    //设置check pointing的间隔
    //env.enableCheckpointing(checkpointInterval)
    //设置流的时间为EventTime
    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)
    //设置flink全局变量
    env.getConfig.setGlobalJobParameters(parameters)


    //获取数据流
    val dStream = env.addSource(consumer).setParallelism(sourceParallelism)
    //流处理
    val streamData = dStream.map(JsonUtil.fromJson[QuintetModel] _).setParallelism(dealParallelism)
      .assignTimestampsAndWatermarks(new AssignerWithPunctuatedWatermarks[QuintetModel] {
        override def checkAndGetNextWatermark(lastElement: QuintetModel, extractedTimestamp: Long): Watermark = {
          new Watermark(extractedTimestamp - 10000)
        }

        override def extractTimestamp(element: QuintetModel, previousElementTimestamp: Long): Long = {
          element.timeStamp
        }
      }).setParallelism(dealParallelism)


    // todo 根据后续使用情况增加修改参数配置
    /**
     * @return
     * @author hanyu
     * @date 2020/8/24 16:34
     * @description 水平扫描处理
     * @update [no][date YYYY-MM-DD][name][description]
     */
    val levelSinkData = streamData.map(new LevelScanMapFunction).setParallelism(dealParallelism)
      .keyBy(_._1)
      //todo 调参statisticsTimeWindow
      .timeWindow(Time.minutes(levelStatisticsTimeWindow), Time.minutes(levelStatisticsTimeWindow))
      .reduce((t1, t2) => {
        (t1._1, t1._2.++(t2._2), t1._3.++(t2._3), t1._4.++(t2._4), t1._5.++(t2._5))
      })
      //todo 调参filterSize
      .filter(_._2.size > levelFilterSize).setParallelism(dealParallelism)
      .keyBy(_._1)
      .process(new LevelScanProcessFunction).setParallelism(dealParallelism)
    levelSinkData
      .addSink(new MySQLSink).uid("level Scan").name("level Scan").setParallelism(sinkParallelism)


    /**
     *
     *
     * @return
     * @author hanyu
     * @date 2020/8/24 16:34
     * @description 垂直扫描处理
     * @update [no][date YYYY-MM-DD][name][description]
     */
    val verticalSinkData = streamData.map(new VerticalScanMapFunction).setParallelism(dealParallelism)
      .keyBy(_._1)
      //todo 调参statisticsTimeWindow
      .timeWindow(Time.minutes(verticalStatisticsTimeWindow), Time.minutes(verticalStatisticsTimeWindow))
      .reduce((t1, t2) => {
        (t1._1, t1._2.++(t2._2), t1._3.++(t2._3), t1._4.++(t2._4), t1._5.++(t2._5))

      })
      //todo 调参filterSize
      .filter(_._2.size > verticalFilterSize).setParallelism(dealParallelism)
      .keyBy(_._1)
      .process(new VerticalScanProcessFunction).setParallelism(dealParallelism)
    verticalSinkData
      .addSink(new MySQLSink).uid("vertical Scan").name("vertical Scan").setParallelism(sinkParallelism)

    verticalSinkData.union(levelSinkData)
      .map(o => {
        JsonUtil.toJson(o._1.asInstanceOf[LowVelocityScanEntity])
      })
      .addSink(producer)
      .setParallelism(kafkaSinkParallelism)

    //将告警数据写入告警库topic
    val warningProducer = new FlinkKafkaProducer[String](brokerList, warningSinkTopic, new
        SimpleStringSchema())
    verticalSinkData.union(levelSinkData)
      .map(m => {
      var inPutKafkaValue = ""
      try {
        val entity = m._1.asInstanceOf[LowVelocityScanEntity]
        inPutKafkaValue = "未知用户" + "|" + "慢速扫描" + "|" + entity.getAlertTime.getTime + "|" +
          "" + "|" + "" + "|" + "" + "|" +
          "" + "|" + entity.getSourceIp + "|" + "" + "|" +
          entity.getDestinationIp + "|" + entity.getDestinationPort+ "|" + "" + "|" +
          "" + "|" + "" + "|" + ""
      } catch {
        case e: Exception => {
        }
      }
      inPutKafkaValue
    }).addSink(warningProducer).setParallelism(kafkaSinkParallelism)


    env.execute(jobName)

  }


  /**
   *
   *
   * @return (Object, Boolean)
   * @author hanyu
   * @date 2020/8/28 9:10
   * @description 将水平扫描的聚合数据进行处理
   * @update [no][date YYYY-MM-DD][name][description]
   */
  class LevelScanProcessFunction extends ProcessFunction[(String, mutable.HashSet[String], mutable.ArrayBuffer[Long],
    mutable.HashSet[String], mutable.HashSet[Long]), (Object, Boolean)] {
    val levelReceiveMessage: LongCounter = new LongCounter()
    val levelResultToMysqlMessage: LongCounter = new LongCounter()
    val levelResultNoScanMessage: LongCounter = new LongCounter()
    val levelIntoDecideMessage: LongCounter = new LongCounter()


    //时间窗口内dip的集合容器
    lazy val levelPreDipVessel: ValueState[mutable.HashSet[String]] =
      getRuntimeContext.getState(new ValueStateDescriptor[mutable.HashSet[String]]("preDipVessel", classOf[mutable.HashSet[String]]))
    //协议类型容器protocolAb
    lazy val levelProtocolVessel: ValueState[mutable.HashSet[String]] =
      getRuntimeContext.getState(new ValueStateDescriptor[mutable.HashSet[String]]("protocolVessel", classOf[mutable.HashSet[String]]))
    //报文长度容器
    lazy val levelInputOctetsVessel: ValueState[mutable.HashSet[Long]] =
      getRuntimeContext.getState(new ValueStateDescriptor[mutable.HashSet[Long]]("inputOctetsVessel", classOf[mutable.HashSet[Long]]))
    //predip长度集合
    lazy val levelPerDipLenVessel: ValueState[ArrayBuffer[Int]] =
      getRuntimeContext.getState(new ValueStateDescriptor[ArrayBuffer[Int]]("perDipLenVessel", classOf[ArrayBuffer[Int]]))


    var levelTimeStepSize: Long = 0L
    var levelTimeWindowCount: Int = 0
    var levelPreDipComentropy = 0.00D
    var levelProbability = 0.00D


    override def open(parameters: Configuration): Unit = {
      //全局配置
      val globalConf = getRuntimeContext.getExecutionConfig.getGlobalJobParameters.asInstanceOf[Configuration]
      //todo 调参并写入ini 修改LOW_VELOCITY_SCAN_STATISTICS_TIMESTEP_SIZE_MAX
      levelTimeStepSize = globalConf.getLong(Constants.LOW_VELOCITY_SCAN_LEVEL_STATISTICS_TIMESTEP_SIZE_MAX, 0L)
      levelTimeWindowCount = globalConf.getInteger(Constants.LOW_VELOCITY_SCAN_LEVEL_STATISTICS_TIMEWINDOW_COUNT, 0)
      levelPreDipComentropy = globalConf.getDouble(Constants.LOW_VELOCITY_SCAN_LEVEL_COMENTROPY, 0.00D)
      levelProbability = globalConf.getDouble(Constants.LOW_VELOCITY_SCAN_LEVEL_INPUTOCTES_PROBABILITY, 0.00D)

      getRuntimeContext.addAccumulator("level received", levelReceiveMessage)
      getRuntimeContext.addAccumulator("level resultToMysql Message", levelResultToMysqlMessage)
      getRuntimeContext.addAccumulator("level resultNoScan Message", levelResultNoScanMessage)
      getRuntimeContext.addAccumulator("level intoDecide Message ", levelIntoDecideMessage)

    }


    override def onTimer(timestamp: Long, ctx: ProcessFunction[(String, mutable.HashSet[String], ArrayBuffer[Long],
      mutable.HashSet[String], mutable.HashSet[Long]), (Object, Boolean)]#OnTimerContext,
                         out: Collector[(Object, Boolean)]): Unit = {

      levelPreDipVessel.clear()
      levelProtocolVessel.clear()
      levelInputOctetsVessel.clear()
      levelPerDipLenVessel.clear()
    }

    /**
     *
     *
     * @return Boolean
     * @author hanyu
     * @date 2020/8/28 9:12
     * @description 对协议和报文长度进行数理统计
     * @update [no][date YYYY-MM-DD][name][description]
     */
    def getLevelProtocolAndInputOctes(protocolData: mutable.HashSet[String], inputOctesData: mutable.HashSet[Long], probabilityF: Double): Boolean = {
      if (protocolData.nonEmpty && inputOctesData.nonEmpty) {
        val inputOctesMap = inputOctesData.map((_, 1)).groupBy(_._1).mapValues(_.size)
        val inputCount = inputOctesMap.filterKeys(_ >= 64).filterKeys(_ <= 128).size

        //todo 0.8 需要调参写入ini
        if (inputCount.toDouble / inputOctesMap.size.toDouble >= probabilityF) {
          true
        } else {
          false
        }
      } else {
        false
      }
    }


    /**
     *
     *
     * @return Double
     * @author hanyu
     * @date 2020/8/28 9:13
     * @description 持续增量模型的熵值计算
     * @update [no][date YYYY-MM-DD][name][description]
     */
    def getLevelComentropy(value: ArrayBuffer[Int]): Double = {
      val ints = new ArrayBuffer[Int]()
      for (i <- value.indices) {
        if (i < value.size - 1) {

          ints.add(value(i + 1) - value(i))
        }
      }

      val intToInt = ints.map((_, 1)).groupBy(_._1).mapValues(_.size).values
      var end = 0.0000D
      for (i <- intToInt) {
        val prob = i.toDouble / (value.size - 1)
        end -= prob * (Math.log(prob) / Math.log(2))
      }

      end.formatted("%.4f").toDouble

    }

    override def processElement(value: (String, mutable.HashSet[String], ArrayBuffer[Long], mutable.HashSet[String], mutable.HashSet[Long]),
                                ctx: ProcessFunction[(String, mutable.HashSet[String], ArrayBuffer[Long], mutable.HashSet[String],
                                  mutable.HashSet[Long]), (Object, Boolean)]#Context,
                                out: Collector[(Object, Boolean)]): Unit = {
      val time = value._3.get(0)

      val predip = value._2
      val protocol = value._4
      val inputOctets = value._5
      val preDipLen = value._2.size
      levelReceiveMessage.add(1)
      if (levelPreDipVessel.value() == null) {
        //todo 调参timeStepSize
        ctx.timerService().registerEventTimeTimer(time + levelTimeStepSize)

        levelPreDipVessel.update(predip)
        levelProtocolVessel.update(protocol)
        levelInputOctetsVessel.update(inputOctets)
        levelPerDipLenVessel.update(ArrayBuffer[Int](preDipLen))

      } else {

        levelPreDipVessel.update(levelPreDipVessel.value().++(predip))
        levelProtocolVessel.update(levelProtocolVessel.value().++(protocol))
        levelInputOctetsVessel.update(levelInputOctetsVessel.value().++(inputOctets))
        levelPerDipLenVessel.update(levelPerDipLenVessel.value().+=(levelPreDipVessel.value().++(predip).size))

        if (levelPerDipLenVessel.value().size == levelTimeWindowCount) {
          levelIntoDecideMessage.add(1)
          val comentropy: Double = getLevelComentropy(levelPerDipLenVessel.value())
          val flag = getLevelProtocolAndInputOctes(levelProtocolVessel.value(), levelInputOctetsVessel.value(), levelProbability)

          //todo 2.0 调参preDipComentropy
          if (comentropy < levelPreDipComentropy && flag) {
            //写入告警表
            val lowScanEntity = new LowVelocityScanEntity
            lowScanEntity.setLowScanType(0)
            lowScanEntity.setSourceIp(value._1.split("-")(0))
            lowScanEntity.setDestinationPort(value._1.split("-")(1))
            lowScanEntity.setDestinationIp(levelPreDipVessel.value().mkString("|"))
            lowScanEntity.setComentropy(comentropy.toLong)
            lowScanEntity.setProtocolId(levelProtocolVessel.value().mkString("|"))
            lowScanEntity.setInputoctets(levelInputOctetsVessel.value().mkString("|"))
            lowScanEntity.setAlertTime(new Timestamp(value._3.last))
            out.collect(lowScanEntity, true)

            levelPreDipVessel.clear()
            levelProtocolVessel.clear()
            levelInputOctetsVessel.clear()
            levelPerDipLenVessel.clear()
            levelResultToMysqlMessage.add(1)
          } else {
            levelPreDipVessel.clear()
            levelProtocolVessel.clear()
            levelInputOctetsVessel.clear()
            levelPerDipLenVessel.clear()
            levelResultNoScanMessage.add(1)

          }
        }
      }
    }
  }


  //sip dppt dip time(zeroWindow Long) protocol(String) inputOctets(Long)
  class LevelScanMapFunction extends RichMapFunction[QuintetModel, (String, mutable.HashSet[String],
    mutable.ArrayBuffer[Long], mutable.HashSet[String], mutable.HashSet[Long])] {
    override def map(levelValue: QuintetModel): (String, mutable.HashSet[String], mutable.ArrayBuffer[Long],
      mutable.HashSet[String], mutable.HashSet[Long]) = {

      val levelSip = levelValue.sourceIp
      val levelDpt = levelValue.destinationPort
      val levelDip = levelValue.destinationIp
      val levelStartTime = levelValue.timeStamp
      val levelProtocolId = levelValue.protocol
      val levelInputOctets = levelValue.inputOctets

      val levelSipDpt = levelSip + "-" + levelDpt
      val levelDipSet = new mutable.HashSet[String]()
      val levelStartTimeAb = mutable.ArrayBuffer[Long]()
      val levelProtocolSet = mutable.HashSet[String]()
      val levelInputOctetsSet = mutable.HashSet[Long]()
      levelDipSet.+=(levelDip)
      levelStartTimeAb.add(levelStartTime)
      levelProtocolSet.add(levelProtocolId)
      levelInputOctetsSet.add(levelInputOctets)
      //sip_dpt 目的ip，开始时间，协议类型，报文长度
      (levelSipDpt, levelDipSet, levelStartTimeAb, levelProtocolSet, levelInputOctetsSet)

    }
  }

  class VerticalScanMapFunction extends RichMapFunction[QuintetModel, (String, mutable.HashSet[String],
    mutable.ArrayBuffer[Long], mutable.HashSet[String], mutable.HashSet[Long])] {
    override def map(verticalValue: QuintetModel): (String, mutable.HashSet[String], ArrayBuffer[Long], mutable.HashSet[String],
      mutable.HashSet[Long]) = {
      val verticalSip = verticalValue.sourceIp
      val verticalDpt = verticalValue.destinationPort
      val verticalDip = verticalValue.destinationIp
      val verticalStartTime = verticalValue.timeStamp
      val verticalProtocolId = verticalValue.protocol
      val verticalInputOctets = verticalValue.inputOctets

      val verticalSipDip = verticalSip + "-" + verticalDip
      val verticalDptSet = new mutable.HashSet[String]()
      val verticalStartTimeAb = mutable.ArrayBuffer[Long]()
      val verticalProtocolSet = mutable.HashSet[String]()
      val verticalInputOctetsSet = mutable.HashSet[Long]()

      verticalDptSet.+=(verticalDpt)
      verticalStartTimeAb.add(verticalStartTime)
      verticalProtocolSet.add(verticalProtocolId)
      verticalInputOctetsSet.add(verticalInputOctets)

      (verticalSipDip, verticalDptSet, verticalStartTimeAb, verticalProtocolSet, verticalInputOctetsSet)
    }
  }

  /**
   *
   *
   * @return (Object, Boolean)
   * @author hanyu
   * @date 2020/8/28 9:15
   * @description 对垂直扫描聚合后的数据进行处理
   * @update [no][date YYYY-MM-DD][name][description]
   */
  class VerticalScanProcessFunction extends ProcessFunction[(String, mutable.HashSet[String], mutable.ArrayBuffer[Long],
    mutable.HashSet[String], mutable.HashSet[Long]), (Object, Boolean)] {
    val verticalReceiveMessage: LongCounter = new LongCounter()
    val verticalResultToMysqlMessage: LongCounter = new LongCounter()
    val verticalResultNoScanMessage: LongCounter = new LongCounter()
    val verticalIntoDecideMessage: LongCounter = new LongCounter()

    //时间窗口内dpt的集合容器
    lazy val verticalPreDptVessel: ValueState[mutable.HashSet[String]] =
      getRuntimeContext.getState(new ValueStateDescriptor[mutable.HashSet[String]]("preDptVessel", classOf[mutable.HashSet[String]]))
    //协议类型容器protocolAb
    lazy val verticalProtocolVessel: ValueState[mutable.HashSet[String]] =
      getRuntimeContext.getState(new ValueStateDescriptor[mutable.HashSet[String]]("protocolVessel", classOf[mutable.HashSet[String]]))
    //报文长度容器
    lazy val verticalInputOctetsVessel: ValueState[mutable.HashSet[Long]] =
      getRuntimeContext.getState(new ValueStateDescriptor[mutable.HashSet[Long]]("inputOctetsVessel", classOf[mutable.HashSet[Long]]))
    //predip长度集合
    lazy val verticalPerDptLenVessel: ValueState[ArrayBuffer[Int]] =
      getRuntimeContext.getState(new ValueStateDescriptor[ArrayBuffer[Int]]("perDptLenVessel", classOf[ArrayBuffer[Int]]))

    var verticalTimeStepSize: Long = 0L
    var verticalTimeWindowCount: Int = 0
    var verticalPreDipComentropy: Double = 0.00D
    var verticalProbability: Double = 0.00D

    override def open(parameters: Configuration): Unit = {
      //全局配置
      val globalConf = getRuntimeContext.getExecutionConfig.getGlobalJobParameters.asInstanceOf[Configuration]
      //todo 调参并写入ini 修改LOW_VELOCITY_SCAN_STATISTICS_TIMESTEP_SIZE_MAX
      verticalTimeStepSize = globalConf.getLong(Constants.LOW_VELOCITY_SCAN_VERTICAL_STATISTICS_TIMESTEP_SIZE_MAX, 0L)
      verticalTimeWindowCount = globalConf.getInteger(Constants.LOW_VELOCITY_SCAN_VERTICAL_STATISTICS_TIMEWINDOW_COUNT, 0)
      verticalPreDipComentropy = globalConf.getDouble(Constants.LOW_VELOCITY_SCAN_VERTICAL_COMENTROPY, 0.00D)
      verticalProbability = globalConf.getDouble(Constants.LOW_VELOCITY_SCAN_VERTICAL_INPUTOCTES_PROBABILITY, 0.00D)

      getRuntimeContext.addAccumulator("vertical Received", verticalReceiveMessage)
      getRuntimeContext.addAccumulator("vertical ResultToMysql Message", verticalResultToMysqlMessage)
      getRuntimeContext.addAccumulator("vertical ResultNoScan Message", verticalResultNoScanMessage)
      getRuntimeContext.addAccumulator("vertical IntoDecide Message", verticalIntoDecideMessage)

    }

    /**
     *
     *
     * @return Boolean
     * @author hanyu
     * @date 2020/8/28 9:15
     * @description 垂直扫描的协议、报文长度进行数理统计
     * @update [no][date YYYY-MM-DD][name][description]
     */
    def getVerticalProtocolAndInputOctes(protocolData: mutable.HashSet[String], InputOctesData: mutable.HashSet[Long], probabilityF: Double): Boolean = {
      if (protocolData.nonEmpty && InputOctesData.nonEmpty) {

        val inPutOctesMap = InputOctesData.map((_, 1)).groupBy(_._1).mapValues(_.size)
        val inPutCount = inPutOctesMap.filterKeys(_ >= 64).filterKeys(_ <= 128).size
        //todo 0.8 需要调参写入ini
        if (inPutCount.toDouble / inPutOctesMap.size.toDouble >= probabilityF) {
          true
        } else {
          false
        }
      } else {
        false
      }
    }

    // todo 触发器不触发，存在bug
    //    override def onTimer(timestamp: Long, ctx: ProcessFunction[(String, mutable.HashSet[String], ArrayBuffer[Long],
    //      mutable.HashSet[String], mutable.HashSet[Long]), (Object, Boolean)]#OnTimerContext,
    //                         out: Collector[(Object, Boolean)]): Unit = {
    //
    //      verticalPreDptVessel.clear()
    //      verticalProtocolVessel.clear()
    //      verticalInputOctetsVessel.clear()
    //      verticalPerDptLenVessel.clear()
    //
    //    }

    /**
     *
     *
     * @return Double
     * @author hanyu
     * @date 2020/8/28 9:13
     * @description 持续增量模型的熵值计算
     * @update [no][date YYYY-MM-DD][name][description]
     */
    def getVerticalComentropy(value: ArrayBuffer[Int]): Double = {

      val ints = new ArrayBuffer[Int]()
      for (i <- value.indices) {
        if (i < value.size - 1) {
          ints.add(value(i + 1) - value(i))
        }
      }

      val intToInt = ints.map((_, 1)).groupBy(_._1).mapValues(_.size).values
      var end = 0.00D
      for (i <- intToInt) {
        val prob = i.toDouble / (value.size - 1)
        end -= prob * (Math.log(prob) / Math.log(2))
      }
      end.formatted("%.2f").toDouble

    }

    override def processElement(value: (String, mutable.HashSet[String], ArrayBuffer[Long], mutable.HashSet[String],
      mutable.HashSet[Long]), ctx: ProcessFunction[(String, mutable.HashSet[String], ArrayBuffer[Long], mutable.HashSet[String],
      mutable.HashSet[Long]), (Object, Boolean)]#Context, out: Collector[(Object, Boolean)]): Unit = {

      val time = value._3.get(0)

      val predpt = value._2
      val protocol = value._4
      val inputOctets = value._5
      val preDptLen = value._2.size
      verticalReceiveMessage.add(1)
      if (verticalPreDptVessel.value() == null) {
        //todo 存在触发器bug
        //        //todo 调参timeStepSize
        //        ctx.timerService().registerEventTimeTimer(time + verticalTimeStepSize)
        verticalPreDptVessel.update(predpt)
        verticalProtocolVessel.update(protocol)
        verticalInputOctetsVessel.update(inputOctets)
        verticalPerDptLenVessel.update(ArrayBuffer[Int](preDptLen))

      } else {

        verticalPreDptVessel.update(verticalPreDptVessel.value().++(predpt))
        verticalProtocolVessel.update(verticalProtocolVessel.value().++(protocol))
        verticalInputOctetsVessel.update(verticalInputOctetsVessel.value().++(inputOctets))
        verticalPerDptLenVessel.update(verticalPerDptLenVessel.value().+=(verticalPreDptVessel.value().++(predpt).size))
        //        perDptLenVessel.update(ArrayBuffer[Int](preDptVessel.value().++(predpt).size))
        if (verticalPerDptLenVessel.value().size == verticalTimeWindowCount) {
          verticalIntoDecideMessage.add(1)
          val comentropy: Double = getVerticalComentropy(verticalPerDptLenVessel.value())
          val flag = getVerticalProtocolAndInputOctes(verticalProtocolVessel.value(), verticalInputOctetsVessel.value(), verticalProbability)
          //todo 2.0 调参preDipComentropy
          if (comentropy < verticalPreDipComentropy && flag) {
            //写入告警表
            val lowScanEntity = new LowVelocityScanEntity
            lowScanEntity.setLowScanType(1)
            lowScanEntity.setSourceIp(value._1.split("\\-")(0))
            lowScanEntity.setDestinationPort(verticalPreDptVessel.value().mkString("|"))
            lowScanEntity.setDestinationIp(value._1.split("\\-")(1))
            lowScanEntity.setComentropy(comentropy.toLong)
            lowScanEntity.setProtocolId(verticalProtocolVessel.value().mkString("|"))
            lowScanEntity.setInputoctets(verticalInputOctetsVessel.value().mkString("|"))
            lowScanEntity.setAlertTime(new Timestamp(value._3.last))
            out.collect(lowScanEntity, true)

            verticalPreDptVessel.clear()
            verticalProtocolVessel.clear()
            verticalInputOctetsVessel.clear()
            verticalPerDptLenVessel.clear()
            verticalResultToMysqlMessage.add(1)
          } else {
            verticalPreDptVessel.clear()
            verticalProtocolVessel.clear()
            verticalInputOctetsVessel.clear()
            verticalPerDptLenVessel.clear()
            verticalIntoDecideMessage.add(1)
          }
        }
      }
    }
  }

}

