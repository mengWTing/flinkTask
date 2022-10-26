package cn.ffcs.is.mss.analyzer.flink.regression


import java.text.DecimalFormat
import java.util
import java.util.{Date, Properties}

import cn.ffcs.is.mss.analyzer.bean._
import cn.ffcs.is.mss.analyzer.druid.model.scala.OperationModel
import cn.ffcs.is.mss.analyzer.flink.regression.utils.{HoltWintersModel, RegressionUtil}
import cn.ffcs.is.mss.analyzer.flink.sink.MySQLSink
import cn.ffcs.is.mss.analyzer.utils.druid.entity.Aggregation
import cn.ffcs.is.mss.analyzer.utils._
import org.apache.flink.api.common.accumulators.LongCounter
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.functions.{AssignerWithPunctuatedWatermarks, ProcessFunction}
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.streaming.api.watermark.Watermark
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer
import org.apache.flink.streaming.util.serialization.SimpleStringSchema
import org.apache.flink.api.scala._
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.util.Collector

import scala.collection.JavaConverters._
import scala.collection.JavaConversions._
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

object HoltWintersAllRegression {
  def main(args: Array[String]): Unit = {
    val confProperties = new IniProperties(args(0))
    //            val args0 = "G:\\福富Flink\\mss\\src\\main\\resources\\flink.ini"
    //            val confProperties = new IniProperties(args0)
    //任务名
    val jobName = confProperties.getValue(Constants.FLINK_HOLT_WINTERS_REGRESSION_CONFIG, Constants
      .FLINK_HOLT_WINTERS_REGRESSION_JOB_NAME)
    //kafka Source的名字
    val kafkaSourceName = confProperties.getValue(Constants.FLINK_HOLT_WINTERS_REGRESSION_CONFIG, Constants
      .FLINK_HOLT_WINTERS_REGRESSION_KAFKA_NAME)
    //sink的名字
    val regressionSinkName = confProperties.getValue(Constants.FLINK_HOLT_WINTERS_REGRESSION_CONFIG, Constants
      .FLINK_HOLT_WINTERS_REGRESSION_SINK_NAME)
    //kafka并行度
    val kafkaSourceParallelism = confProperties.getIntValue(Constants.FLINK_HOLT_WINTERS_REGRESSION_CONFIG, Constants
      .FLINK_HOLT_WINTERS_REGRESSION_KAFKA_SOURCE_PARALLELISM)
    //处理并行度
    val dealParallelism = confProperties.getIntValue(Constants.FLINK_HOLT_WINTERS_REGRESSION_CONFIG, Constants
      .FLINK_HOLT_WINTERS_REGRESSION_DEAL_PARALLELISM)
    val sinkParallelism = confProperties.getIntValue(Constants.FLINK_HOLT_WINTERS_REGRESSION_CONFIG, Constants
      .FLINK_HOLT_WINTERS_REGRESSION_MYSQL_SINK_PARALLELISM)


    //kafka的服务地址
    val brokerList = confProperties.getValue(Constants.FLINK_COMMON_CONFIG, Constants.KAFKA_BOOTSTRAP_SERVERS)
    //flink消费的group.id
    val groupId = confProperties.getValue(Constants.FLINK_HOLT_WINTERS_REGRESSION_CONFIG, Constants
      .FLINK_HOLT_WINTERS_REGRESSION_ALL_GROUP_ID)
    //kafka的topic
    val topic = confProperties.getValue(Constants.OPERATION_FLINK_TO_DRUID_CONFIG, Constants.OPERATION_TO_KAFKA_TOPIC)


    val parameters: Configuration = new Configuration()


    //c3p0连接池配置文件路径
    parameters.setString(Constants.c3p0_CONFIG_PATH, confProperties.getValue(Constants.FLINK_COMMON_CONFIG, Constants
      .c3p0_CONFIG_PATH))



    //alpha值
    parameters.setDouble(Constants.FLINK_HOLT_WINTERS_REGRESSION_ALPHA, confProperties.getFloatValue(Constants
      .FLINK_HOLT_WINTERS_REGRESSION_CONFIG, Constants.FLINK_HOLT_WINTERS_REGRESSION_ALPHA))
    //beta值
    parameters.setDouble(Constants.FLINK_HOLT_WINTERS_REGRESSION_BETA, confProperties.getFloatValue(Constants
      .FLINK_HOLT_WINTERS_REGRESSION_CONFIG, Constants.FLINK_HOLT_WINTERS_REGRESSION_BETA))
    //gamma值
    parameters.setDouble(Constants.FLINK_HOLT_WINTERS_REGRESSION_GAMMA, confProperties.getFloatValue(Constants
      .FLINK_HOLT_WINTERS_REGRESSION_CONFIG, Constants.FLINK_HOLT_WINTERS_REGRESSION_GAMMA))
    //period
    parameters.setInteger(Constants.FLINK_HOLT_WINTERS_REGRESSION_PERIOD, confProperties.getIntValue(Constants
      .FLINK_HOLT_WINTERS_REGRESSION_CONFIG, Constants.FLINK_HOLT_WINTERS_REGRESSION_PERIOD))
    //统计时长
    parameters.setInteger(Constants.FLINK_HOLT_WINTERS_REGRESSION_HISTORY_LEN, confProperties.getIntValue(Constants
      .FLINK_HOLT_WINTERS_REGRESSION_CONFIG, Constants.FLINK_HOLT_WINTERS_REGRESSION_HISTORY_LEN))

    //产生告警的范围
    parameters.setLong(Constants.FLINK_HOLT_WINTERS_REGRESSION_ALL_USER_REGRESSION_SCOPE_OF_WARN,
      confProperties.getLongValue(Constants.FLINK_HOLT_WINTERS_REGRESSION_CONFIG, Constants
        .FLINK_HOLT_WINTERS_REGRESSION_ALL_USER_REGRESSION_SCOPE_OF_WARN))

    //告警等级

    parameters.setString(Constants.FLINK_HOLT_WINTERS_REGRESSION_ALL_USER_REGRESSION_WARN_LEVEL, confProperties
      .getValue(Constants.FLINK_HOLT_WINTERS_REGRESSION_CONFIG, Constants
        .FLINK_HOLT_WINTERS_REGRESSION_ALL_USER_REGRESSION_WARN_LEVEL))


    //druid的broker节点
    parameters.setString(Constants.DRUID_BROKER_HOST_PORT, confProperties.getValue(Constants.FLINK_COMMON_CONFIG,
      Constants.DRUID_BROKER_HOST_PORT))
    //druid时间格式
    parameters.setString(Constants.DRUID_TIME_FORMAT, confProperties.getValue(Constants.FLINK_COMMON_CONFIG,
      Constants.DRUID_TIME_FORMAT))
    //druid数据开始的时间
    parameters.setLong(Constants.DRUID_DATA_START_TIMESTAMP, confProperties.getLongValue(Constants
      .FLINK_COMMON_CONFIG, Constants.DRUID_DATA_START_TIMESTAMP))
    //业务话单在druid的表名
    parameters.setString(Constants.DRUID_OPERATION_TABLE_NAME, confProperties.getValue(Constants.FLINK_COMMON_CONFIG,
      Constants.DRUID_OPERATION_TABLE_NAME))


    val props = new Properties()
    props.setProperty("bootstrap.servers", brokerList)
    props.setProperty("group.id", groupId + "ALL")
    props.setProperty("auto.offset.reset", "earliest")

    val consumer = new FlinkKafkaConsumer[String](topic, new SimpleStringSchema, props).setStartFromEarliest()

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    //设置check pointing的间隔
    //    env.enableCheckpointing(100000)
    //设置流的时间为EventTime
    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)
    //设置flink全局变量
    env.getConfig.setGlobalJobParameters(parameters)

    val dStream = env.addSource(consumer).setParallelism(5)
      .uid(kafkaSourceName).name(kafkaSourceName)

    val operationModelStream = dStream
      .map(JsonUtil.fromJson[OperationModel] _).setParallelism(5)
      .assignTimestampsAndWatermarks(new AssignerWithPunctuatedWatermarks[OperationModel] {
        override def checkAndGetNextWatermark(lastElement: OperationModel, extractedTimestamp: Long): Watermark =
          new Watermark(extractedTimestamp - 10000)

        override def extractTimestamp(element: OperationModel, previousElementTimestamp: Long): Long = {
          element.timeStamp
        }
      }).setParallelism(5)
      .map(tuple => (tuple.timeStamp / 1000 / 60 * 1000 * 60, tuple.loginSystem, tuple.loginPlace, tuple.connCount,
        tuple.octets, Set[String](tuple.userName))).setParallelism(5)


    val allOperationModelStream = operationModelStream
      .keyBy(0)
      .timeWindow(Time.minutes(1), Time.minutes(1))
      .reduce((o1, o2) => {
        (o1._1, "1", "", o1._4 + o2._4, o1._5 + o2._5, o1._6 ++ o2._6)
      })
      .keyBy(1)
      .process(new AllHWRegression).setParallelism(1)

    allOperationModelStream.addSink(new MySQLSink).setParallelism(1)
      .uid("allOperation").name("allOperation")


    env.execute("ALL_" + jobName)


  }

  class AllHWRegression extends ProcessFunction[(Long, String, String, Long, Long, Set[String]), (Object, Boolean)] {

    var tableName: String = _
    var historyLen: Int = _
    var searchTimestamp: Long = _
    var scopeOfWarn: Long = _
    var warnLevelArray: Array[Double] = _
    var alpha: Double = _
    var beta: Double = _
    var gamma: Double = _
    var period: Int = _

    var predictValue: mutable.Map[Aggregation, ArrayBuffer[Long]] = new mutable.HashMap[Aggregation,
      ArrayBuffer[Long]]()

    var historyData: util.TreeMap[Long, mutable.Map[Aggregation, Double]] = _

    lazy val aggregationsMap = mutable.Map[Aggregation, (Class[_ <: Object], Class[_ <: Object])]()

    val messagesReceived = new LongCounter()
    val warnSend = new LongCounter()
    val regressionSend = new LongCounter()
    val allMessage = new LongCounter()
    var doubleDF: DecimalFormat = _

    override def open(parameters: Configuration): Unit = {

      val globConf = getRuntimeContext.getExecutionConfig.getGlobalJobParameters.asInstanceOf[Configuration]
      tableName = globConf.getString(Constants.DRUID_OPERATION_TABLE_NAME, "")
      historyLen = globConf.getInteger(Constants.FLINK_HOLT_WINTERS_REGRESSION_HISTORY_LEN, 0)
      //获取告警范围
      scopeOfWarn = globConf.getLong(Constants.FLINK_HOLT_WINTERS_REGRESSION_ALL_USER_REGRESSION_SCOPE_OF_WARN,
        TimeUtil.MINUTE_MILLISECOND * 10)
      //获取告警等级数组
      warnLevelArray = RegressionUtil.getWarnLevelArray(globConf.getString(Constants
        .FLINK_HOLT_WINTERS_REGRESSION_ALL_USER_REGRESSION_WARN_LEVEL, "0.5|1|2|4|8"))

      alpha = globConf.getDouble(Constants.FLINK_HOLT_WINTERS_REGRESSION_ALPHA, 0.0)
      beta = globConf.getDouble(Constants.FLINK_HOLT_WINTERS_REGRESSION_BETA, 0.0)
      gamma = globConf.getDouble(Constants.FLINK_HOLT_WINTERS_REGRESSION_GAMMA, 0.0)
      historyLen = globConf.getInteger(Constants.FLINK_HOLT_WINTERS_REGRESSION_HISTORY_LEN, 0)
      period = globConf.getInteger(Constants.FLINK_HOLT_WINTERS_REGRESSION_PERIOD, 0)




      //设置druid的broker的host和port
      DruidUtil.setDruidHostPortSet(globConf.getString(Constants.DRUID_BROKER_HOST_PORT, ""))
      //设置写入druid的时间格式
      DruidUtil.setTimeFormat(globConf.getString(Constants.DRUID_TIME_FORMAT, ""))
      //设置druid开始的时间
      DruidUtil.setDateStartTimeStamp(globConf.getLong(Constants.DRUID_DATA_START_TIMESTAMP, 0L))


      aggregationsMap.put(Aggregation.connCount, (classOf[BbasAllUserConncountRegression1Entity],
        classOf[BbasAllUserConncountWarn1Entity]))
      aggregationsMap.put(Aggregation.octets, (classOf[BbasAllUserOctetsRegression1Entity],
        classOf[BbasAllUserOctetsWarn1Entity]))
      aggregationsMap.put(Aggregation.userNameCount, (classOf[BbasAllUserUsercountRegression1Entity],
        classOf[BbasAllUserUsercountWarn1Entity]))

      doubleDF = new DecimalFormat("0.00000")


      //从druid中查询出历史基线的数据(包含当天已过分钟的数据)
      //将这些数据放入到historyData中
      val date = new Date
      //从druid中查询出这个时间减10分钟之前的历史基线数据
      searchTimestamp = date.getTime / 1000 / 60 * 1000 * 60 - 15 * TimeUtil.MINUTE_MILLISECOND
      historyData = new util.TreeMap[Long, mutable.Map[Aggregation, Double]]()

      val startTimestamp = TimeUtil.getDayStartTime(searchTimestamp) - historyLen * TimeUtil.DAY_MILLISECOND

      val entity = RegressionUtil.getAllUserQueryEntity(startTimestamp, searchTimestamp, tableName, aggregationsMap
        .keySet)

      RegressionUtil.queryDruidFillEmptyValue(startTimestamp, searchTimestamp, entity, aggregationsMap.keySet,
        historyData)


      getRuntimeContext.addAccumulator("AllUserRegression: Messages received", messagesReceived)
      getRuntimeContext.addAccumulator("AllUserRegression: Warn send", warnSend)
      getRuntimeContext.addAccumulator("AllUserRegression: Regression send", regressionSend)
      getRuntimeContext.addAccumulator("AllMessage", allMessage)

    }

    override def processElement(value: (Long, String, String, Long, Long, Set[String]), ctx: ProcessFunction[(Long,
      String, String, Long, Long, Set[String]), (Object, Boolean)]#Context, out: Collector[(Object, Boolean)]): Unit = {


      val timestamp = value._1
      allMessage.add(1L)
      val dataTime = new Date(timestamp)
      val dayInWeek = dataTime.getDay
      // 当数据的时间大于等于druid中查询出数据的时间时 ,开始计算
      if (timestamp >= searchTimestamp) {
        messagesReceived.add(1)
        val newConnectCount = value._4
        val newOctetsCount = value._5
        val newUserCount = value._6.size

        //当前聚合的值
        val countMap = mutable.Map[Aggregation, Double]()
        countMap.put(Aggregation.connCount, newConnectCount.toDouble)
        countMap.put(Aggregation.octets, newOctetsCount.toDouble)
        countMap.put(Aggregation.userNameCount, newUserCount.toDouble)

        //如果预测值为空
        if (predictValue.isEmpty) {
          //补全中间缺少的分钟
          //          val startTimeStampTemp = searchTimestamp
          println(newConnectCount + "------------------------------------------------------" + timestamp)

          //如果数据的时间大于 存储的历史数据的时间,补全这部分缺少的
          //          if (startTimeStampTemp < timestamp - TimeUtil.MINUTE_MILLISECOND) {
          //            val entity = RegressionUtil.getAllUserQueryEntity(startTimeStampTemp, timestamp
          //              , tableName, aggregationsMap.keySet)
          //            RegressionUtil.queryDruidFillEmptyValue(startTimeStampTemp, timestamp, entity,
          // aggregationsMap.keySet,
          //              historyData)
          //          }

          //todo 需要完善: 当当前数据的时间在历史数据中不足设定的时间长度,需要不足之前的数据

          //          val timeKeySet = historyData.keySet

          //          val littleSize = timeKeySet.count(t => t < timestamp)
          //          if (littleSize < period * historyLen) {
          //            val startTime = timeKeySet.min - TimeUtil.DAY_MILLISECOND
          //            val endTime = timeKeySet.min
          //            val entity = RegressionUtil.getAllUserQueryEntity(startTime, endTime,
          //              tableName, aggregationsMap.keySet)
          //            RegressionUtil.queryDruidFillEmptyValue(startTime, endTime, entity, aggregationsMap.keySet,
          //              historyData)
          //          }


          val trainData = new mutable.HashMap[Aggregation, ArrayBuffer[Double]]()

          val newSortedTimeKeySet = new ArrayBuffer[Long]

          historyData.foreach(tuple => {
            val t = tuple._1
            newSortedTimeKeySet.append(t)
            val day = new Date(t).getDay
            if (day == dayInWeek && t < timestamp) {
              aggregationsMap.keySet.foreach(aggregation => {
                val ab = trainData.getOrElse(aggregation, new ArrayBuffer[Double]())
                ab.append(tuple._2(aggregation))
                trainData.put(aggregation, ab)
              })
            }
          })


          //进行预测

          aggregationsMap.keySet.foreach(aggregation => {

            val ab = new ArrayBuffer[Long]()
            val dimensionData = trainData(aggregation).toArray
            val pre = HoltWintersModel.forecast(dimensionData.map(java.lang.Double.valueOf), alpha, beta, gamma,
              period, 1, false)
            val preValue = pre(pre.size - 1)

            val passTime = dataTime.getHours * 60 + dataTime.getMinutes
            //今天的预测值(还没有进行上下浮动10%)
            newSortedTimeKeySet.takeRight(passTime).foreach(t => {
              val his = historyData(t)(aggregation)
              ab.append(doubleDF.format(his).toDouble.asInstanceOf[Long])
            })
            ab.append(doubleDF.format(preValue).toDouble.asInstanceOf[Long])
            predictValue.put(aggregation, ab)

          })
        }

        //当一段时间没有出现数据,补足数据 并清理掉超过historyLen的数据
        val endT = historyData.lastKey()

        for (time <- endT until timestamp by TimeUtil.MINUTE_MILLISECOND) {
          if (!historyData.contains(time)) {
            aggregationsMap.keySet.foreach(aggregations => {
              historyData.getOrElseUpdate(time, mutable.Map[Aggregation, Double]()).put(aggregations, 0.0)
            })
          }
        }

        val removeHistoryTimeList = new ArrayBuffer[Long]()
        if (historyData.size() > (historyLen + 1) * 1440) {
          val nowDay = TimeUtil.getDayStartTime(timestamp)
          val start = nowDay - historyLen * TimeUtil.DAY_MILLISECOND
          val timeSet = historyData.keySet()
          for (t <- timeSet) {
            if (t < start) {
              removeHistoryTimeList.append(t)
            }
          }
        }
        removeHistoryTimeList.foreach(t => {
          historyData.remove(t)
        })
        ///////////////////////////////////////////////////////////
        //本次数据放入历史数据中

        countMap.foreach(tuple => {
          val aggMap = historyData.getOrElse(timestamp, new mutable.HashMap[Aggregation, Double]())
          aggMap.put(tuple._1, tuple._2)

          historyData.put(timestamp, aggMap)
        })
        println(historyData.lastKey() + "--------------------" + historyData.get(historyData.lastKey()))


        //直接用保存的预测值和现在的值进行比较 判断是否出告警
        countMap.foreach(tuple => {
          if (RegressionUtil.isWarn(predictValue(tuple._1).last, historyData, tuple._1,
            timestamp, scopeOfWarn, warnLevelArray)) {
            val warnLevel = RegressionUtil.getWarnLevel(tuple._2, predictValue(tuple._1).last, warnLevelArray)

            out.collect((RegressionUtil.getAllUserWarnObject(tuple._2, predictValue(tuple._1).last, timestamp,
              warnLevel, aggregationsMap(tuple._1)._2), true))
            warnSend.add(1)
          }
        })


        //进行下次的预测如果timestamp 是23:59分, 改用下一天的周几的数据进行预测(并且清理掉这个星期最早一天的数据)


        //todo 需要完善当没有同类型数据进入,不会触发相应的算子,当再次出现同类型数据时,会导致这两个时间段之间的数据缺失


        if (dataTime.getHours == 23 && dataTime.getMinutes == 59) {
          //改用下一天的数据组进行预测
          val nextWeekDay = new Date(timestamp + TimeUtil.MINUTE_MILLISECOND)


          val trainData = new mutable.HashMap[Aggregation, ArrayBuffer[Double]]()

          var count = 0
          val sortedTimeSet = historyData.keySet()
          val removeTimeList = new ArrayBuffer[Long]
          for (t <- sortedTimeSet) {
            val day = new Date(t).getDay
            if (day == nextWeekDay.getDay && t <= timestamp) {
              aggregationsMap.keySet.foreach(aggregation => {
                val ab = trainData.getOrElse(aggregation, new ArrayBuffer[Double]())
                ab.append(historyData(t)(aggregation))
                trainData.put(aggregation, ab)
              })
            }
            if (count < period) {
              removeTimeList.append(t)
            }
            count += 1
          }
          //清理同一个星期的最早的数据
          removeTimeList.foreach(t => {
            historyData.remove(t)
          })

          aggregationsMap.keySet.foreach(aggregation => {
            val ab = new ArrayBuffer[Long]()
            val dimensionData = trainData(aggregation).toArray

            val pre = HoltWintersModel.forecast(dimensionData.map(java.lang.Double.valueOf), alpha, beta, gamma,
              period, 1, false)
            val preValue = pre(pre.size - 1)

            ab.append(doubleDF.format(preValue).toDouble.asInstanceOf[Long])
            predictValue.put(aggregation, ab)
          })


        } else {
          //用同一天的数据进行预测

          val trainData = new mutable.HashMap[Aggregation, ArrayBuffer[Double]]()
          historyData.foreach(tuple => {
            val t = tuple._1
            val day = new Date(t).getDay
            if (day == dayInWeek && t <= timestamp) {
              aggregationsMap.keySet.foreach(aggregation => {
                val ab = trainData.getOrElse(aggregation, new ArrayBuffer[Double]())
                ab.append(tuple._2(aggregation))
                trainData.put(aggregation, ab)
              })
            }
          })


          aggregationsMap.keySet.foreach(aggregation => {
            val ab = predictValue.getOrElse(aggregation, new ArrayBuffer[Long])
            val dataCount = dataTime.getHours * 60 + dataTime.getMinutes + 1
            //补预测值
            if (dataCount != ab.size) {
              for (i <- 0 until dataCount - ab.size) {
                ab.append(0)
              }
            }
            val dimensionData = trainData(aggregation).toArray

            val pre = HoltWintersModel.forecast(dimensionData.map(java.lang.Double.valueOf), alpha, beta, gamma,
              period, 1, false)
            val preValue = pre(pre.size - 1)

            ab.append(doubleDF.format(preValue).toDouble.asInstanceOf[Long])
            predictValue.put(aggregation, ab)
          })
        }

        //回归值写入数据库
        predictValue.foreach(tuple => {
          out.collect((RegressionUtil.getAllUserRegressionObject(RegressionUtil.getRegressionText(tuple._2),
            timestamp + TimeUtil.MINUTE_MILLISECOND, aggregationsMap(tuple._1)._1), true))
          regressionSend.add(1)
        })
      }
    }
  }

}
