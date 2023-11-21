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

object HoltWintersSystemRegression {
  def main(args: Array[String]): Unit = {
    val confProperties = new IniProperties(args(0))

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
      .FLINK_HOLT_WINTERS_REGRESSION_SYSTEM_GROUP_ID)
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
    props.setProperty("group.id", groupId + "SYSTEM")
    props.setProperty("auto.offset.reset","earliest")

    val consumer = new FlinkKafkaConsumer[String](topic, new SimpleStringSchema, props).setStartFromEarliest()

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    //设置check pointing的间隔
    env.enableCheckpointing(100000)
    //设置流的时间为EventTime
    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)
    //设置flink全局变量
    env.getConfig.setGlobalJobParameters(parameters)

    val dStream = env.addSource(consumer).setParallelism(kafkaSourceParallelism)
      .uid(kafkaSourceName).name(kafkaSourceName)

    val operationModelStream = dStream
      .map(JsonUtil.fromJson[OperationModel] _).setParallelism(3)
      .assignTimestampsAndWatermarks(new AssignerWithPunctuatedWatermarks[OperationModel] {
        override def checkAndGetNextWatermark(lastElement: OperationModel, extractedTimestamp: Long): Watermark =
          new Watermark(extractedTimestamp - 10000)

        override def extractTimestamp(element: OperationModel, previousElementTimestamp: Long): Long = {
          element.timeStamp
        }
      }).setParallelism(3)
      .map(tuple => (tuple.timeStamp / 1000 / 60 * 1000 * 60, tuple.loginSystem, tuple.loginPlace, tuple.connCount,
        tuple.octets, Set[String](tuple.userName))).setParallelism(3)


    val singleSystemOperationModelStream = operationModelStream
      .filter(tuple => !"未知系统".equals(tuple._2)).setParallelism(3)
      .keyBy(1)
      .timeWindow(Time.minutes(1), Time.minutes(1))
      .reduce((o1, o2) => {
        (o1._1, o1._2, "", o1._4 + o2._4, o1._5 + o2._5, o1._6 ++ o2._6)
      })
      .keyBy(1)
      .process(new HWSingleSystemRegression).setParallelism(3)

    singleSystemOperationModelStream.addSink(new MySQLSink).setParallelism(1)
      .uid("singleSystem").name("singleSystem")


    env.execute("SYSTEM_" + jobName)

  }

  class HWSingleSystemRegression extends ProcessFunction[(Long, String, String, Long, Long, Set[String]), (Object,
    Boolean)] {


    var tableName: String = _
    var historyLen: Int = _
    var searchTimestamp: Long = _
    var scopeOfWarn: Long = _
    var warnLevelArray: Array[Double] = _
    var alpha: Double = _
    var beta: Double = _
    var gamma: Double = _
    var period: Int = _
    var startTimestamp: Long = _
    var predictValue: mutable.Map[String, mutable.Map[Aggregation, ArrayBuffer[Long]]] = new mutable.HashMap[String,
      mutable.Map[Aggregation, ArrayBuffer[Long]]]

    var historyData: mutable.Map[String, util.TreeMap[Long, mutable.Map[Aggregation, Double]]] = _

    lazy val aggregationsMap = mutable.Map[Aggregation, (Class[_ <: Object], Class[_ <: Object])]()

    private val messagesReceived = new LongCounter()
    private val warnSend = new LongCounter()
    private val regressionSend = new LongCounter()

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


      aggregationsMap.put(Aggregation.connCount, (classOf[BbasSingleSystemConncountRegression1Entity],
        classOf[BbasSingleSystemConncountWarn1Entity]))
      aggregationsMap.put(Aggregation.octets, (classOf[BbasSingleSystemOctetsRegression1Entity],
        classOf[BbasSingleSystemOctetsWarn1Entity]))
      aggregationsMap.put(Aggregation.userNameCount, (classOf[BbasSingleSystemUsercountRegression1Entity],
        classOf[BbasSingleSystemUsercountWarn1Entity]))


      doubleDF = new DecimalFormat("0.00000")


      //从druid中查询出历史基线的数据(包含当天已过分钟的数据)
      //将这些数据放入到historyData中
      val date = new Date
      //从druid中查询出这个时间减10分钟之前的历史基线数据
      searchTimestamp = date.getTime / 1000 / 60 * 1000 * 60 - 10 * TimeUtil.MINUTE_MILLISECOND

      val todayTimestamp = TimeUtil.getDayStartTime(searchTimestamp)
      startTimestamp = todayTimestamp - historyLen * TimeUtil.DAY_MILLISECOND

      historyData = new mutable.HashMap[String, util.TreeMap[Long, mutable.Map[Aggregation, Double]]]

      for (i <- startTimestamp until todayTimestamp by 7 * TimeUtil.DAY_MILLISECOND) {
        val entity = RegressionUtil.getAllSingleSystemQueryEntity(i, i + 7 * TimeUtil.DAY_MILLISECOND, tableName,
          aggregationsMap.keySet)


        RegressionUtil.queryDruidFillSingleValue("loginSystem", i, i + 7 * TimeUtil.DAY_MILLISECOND, entity,
          aggregationsMap.keySet, historyData)

      }

      val entity = RegressionUtil.getAllSingleSystemQueryEntity(todayTimestamp, searchTimestamp, tableName,
        aggregationsMap.keySet)

      RegressionUtil.queryDruidFillSingleValue("loginSystem", todayTimestamp, searchTimestamp, entity,
        aggregationsMap.keySet, historyData)


      getRuntimeContext.addAccumulator("SingleSystemRegression: Messages received", messagesReceived)
      getRuntimeContext.addAccumulator("SingleSystemRegression: Warn send", warnSend)
      getRuntimeContext.addAccumulator("SingleSystemRegression: Regression send", regressionSend)

    }


    override def processElement(value: (Long, String, String, Long, Long, Set[String]), ctx: ProcessFunction[(Long,
      String, String, Long, Long, Set[String]), (Object, Boolean)]#Context, out: Collector[(Object, Boolean)]): Unit = {
      messagesReceived.add(1)
      val timestamp = value._1
      val dataTime = new Date(timestamp)
      val dayInWeek = dataTime.getDay
      if (timestamp >= searchTimestamp) {

        //系统名
        val systemName = value._2

        val newConnectCount = value._4
        val newOctetsCount = value._5
        val newUserCount = value._6.size

        val countMap = mutable.Map[Aggregation, Double]()
        countMap.put(Aggregation.connCount, newConnectCount)
        countMap.put(Aggregation.octets, newOctetsCount)
        countMap.put(Aggregation.userNameCount, newUserCount)

        //如果预测值中不包含这个系统
        if (!predictValue.contains(systemName) && historyData.contains(systemName)) {
          //补全中间缺少的分钟
          val startTimeStampTemp = searchTimestamp

          //如果数据的时间大于 存储的历史数据的时间,补全这部分缺少的
          if (startTimeStampTemp < timestamp - TimeUtil.MINUTE_MILLISECOND) {
            val entity = RegressionUtil.getSingleSystemQueryEntity(startTimeStampTemp, timestamp, tableName, systemName,
              aggregationsMap.keySet)
            RegressionUtil.queryDruidFillEmptyValue(startTimeStampTemp, timestamp, entity, aggregationsMap.keySet,
              historyData(systemName))
          }


          val timeKeySet = historyData(systemName).keySet

          val littleSize = timeKeySet.filter(t => t < timestamp).size
          if (littleSize < period * historyLen) {
            val startTime = timeKeySet.min - TimeUtil.DAY_MILLISECOND
            val endTime = timeKeySet.min
            val entity = RegressionUtil.getSingleSystemQueryEntity(startTime, endTime,
              tableName, systemName, aggregationsMap.keySet)
            RegressionUtil.queryDruidFillEmptyValue(startTime, endTime, entity, aggregationsMap.keySet,
              historyData(systemName))
          }

          val trainData = new mutable.HashMap[Aggregation, ArrayBuffer[Double]]

          val newSortedTimeKeySet = new ArrayBuffer[Long]
          historyData(systemName).foreach(tuple => {
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


          aggregationsMap.keySet.foreach(aggregation => {
            val map = predictValue.getOrElse(systemName, new mutable.HashMap[Aggregation, ArrayBuffer[Long]]())
            val ab = new ArrayBuffer[Long]()
            val dimensionData = trainData(aggregation).toArray

            val pre = HoltWintersModel.forecast(dimensionData.map(java.lang.Double.valueOf), alpha, beta,
              gamma, period, 1, false)
            val preValue = pre(pre.size - 1)

            val passTime = dataTime.getHours * 60 + dataTime.getMinutes

            newSortedTimeKeySet.takeRight(passTime).foreach(t => {
              val historyValue = historyData(systemName)(t)(aggregation)
              ab.append(doubleDF.format(historyValue).toDouble.asInstanceOf[Long])

            })
            ab.append(doubleDF.format(preValue).toDouble.asInstanceOf[Long])
            map.put(aggregation, ab)
            predictValue.put(systemName, map)
          })
        } else if (!predictValue.contains(systemName) && !historyData.contains(systemName)) {
          //之前的历史数据中没有出现过这个系统
          val newSystem = historyData.getOrElseUpdate(systemName, new util.TreeMap[Long, mutable.Map[Aggregation,
            Double]])
          val nowDay = TimeUtil.getDayStartTime(timestamp)
          val start = nowDay - historyLen * TimeUtil.DAY_MILLISECOND
          for (time <- start until timestamp by TimeUtil.MINUTE_MILLISECOND) {
            aggregationsMap.keySet.foreach(aggregations => {
              newSystem.getOrElseUpdate(time, mutable.Map[Aggregation, Double]()).put(aggregations, 0.0)
            })
          }
          historyData.put(systemName, newSystem)

          val trainData = new mutable.HashMap[Aggregation, ArrayBuffer[Double]]

          val newSortedTimeKeySet = new ArrayBuffer[Long]
          historyData(systemName).foreach(tuple => {
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


          aggregationsMap.keySet.foreach(aggregation => {
            val map = predictValue.getOrElse(systemName, new mutable.HashMap[Aggregation, ArrayBuffer[Long]]())
            val ab = new ArrayBuffer[Long]()
            val dimensionData = trainData(aggregation).toArray

            val pre = HoltWintersModel.forecast(dimensionData.map(java.lang.Double.valueOf), alpha, beta,
              gamma, period, 1, false)
            val preValue = pre(pre.size - 1)

            val passTime = dataTime.getHours * 60 + dataTime.getMinutes

            newSortedTimeKeySet.takeRight(passTime).foreach(t => {
              val historyValue = historyData(systemName)(t)(aggregation)
              ab.append(doubleDF.format(historyValue).toDouble.asInstanceOf[Long])

            })
            ab.append(doubleDF.format(preValue).toDouble.asInstanceOf[Long])
            map.put(aggregation, ab)
            predictValue.put(systemName, map)
          })
        }

        //当有一段时间没有出现时 ,补足数据, 清理掉超过historyLen的数据
        val endT = historyData(systemName).lastKey()
        //      if (historyData(systemName).lastKey() < timestamp - TimeUtil.MINUTE_MILLISECOND){
        //        for()
        //      }
        for (time <- endT until timestamp by TimeUtil.MINUTE_MILLISECOND) {
          if (!historyData(systemName).contains(time)) {
            aggregationsMap.keySet.foreach(aggregations => {
              historyData(systemName).getOrElseUpdate(time, mutable.Map[Aggregation, Double]()).put(aggregations, 0.0)
            })
          }
        }
        val removeHistoryTimeList = new ArrayBuffer[Long]()
        if (historyData(systemName).size() > (historyLen + 1) * 1440) {
          val nowDay = TimeUtil.getDayStartTime(timestamp)
          val start = nowDay - historyLen * TimeUtil.DAY_MILLISECOND
          val timeSet = historyData(systemName).keySet()
          for (t <- timeSet) {
            if (t < start) {
              removeHistoryTimeList.append(t)
            }
          }
        }
        removeHistoryTimeList.foreach(t => {
          historyData(systemName).remove(t)
        })

        //本次数据放入历史数据中
        countMap.foreach(tuple => {
          val timeMap = historyData.getOrElse(systemName, new util.TreeMap[Long, mutable.Map[Aggregation, Double]] {})
          val aggMap = timeMap.getOrElse(timestamp, new mutable.HashMap[Aggregation, Double]())
          aggMap.put(tuple._1, tuple._2)

          timeMap.put(timestamp, aggMap)
          historyData.put(systemName, timeMap)
        })


        countMap.foreach(tuple => {
          if (RegressionUtil.isWarn(predictValue(systemName)(tuple._1).last, historyData(systemName),
            tuple._1, timestamp, scopeOfWarn, warnLevelArray)) {
            val warnLevel = RegressionUtil.getWarnLevel(tuple._2, predictValue(systemName)(tuple._1).last,
              warnLevelArray)

            out.collect((RegressionUtil.getSingleSystemWarnObject(systemName, tuple._2, predictValue(systemName)
            (tuple._1).last, timestamp, warnLevel, aggregationsMap(tuple._1)._2), true))
            warnSend.add(1)
          }
        })


        //进行下次的预测 如果timestamp 是23:59分, 改用下一天的周几的数据进行预测(并且清理掉这个星期最早一天的数据)
        if (dataTime.getHours == 23 && dataTime.getMinutes == 59) {
          //改用下一天的数据组进行预测
          val nextWeekDay = new Date(timestamp + TimeUtil.MINUTE_MILLISECOND)


          val trainData = new mutable.HashMap[Aggregation, ArrayBuffer[Double]]

          var count = 0

          val sortedTimeSet = historyData(systemName).keySet()
          val removeTimeList = new ArrayBuffer[Long]
          for (t <- sortedTimeSet) {
            val day = new Date(t).getDay
            if (day == nextWeekDay.getDay && t <= timestamp) {
              aggregationsMap.keySet.foreach(aggregation => {
                val ab = trainData.getOrElse(aggregation, new ArrayBuffer[Double]())
                ab.append(historyData(systemName)(t)(aggregation))
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
            historyData(systemName).remove(t)
          })

          aggregationsMap.keySet.foreach(aggregation => {
            val ab = new ArrayBuffer[Long]()
            val dimensionData = trainData(aggregation).toArray
            val pre = HoltWintersModel.forecast(dimensionData.map(java.lang.Double.valueOf), alpha, beta, gamma,
              period, 1, false)
            val preValue = pre(pre.size - 1)
            ab.append(doubleDF.format(preValue).toDouble.asInstanceOf[Long])
            predictValue(systemName).put(aggregation, ab)

          })
        } else {
          //用同一天的数据进行预测
          val trainData = new mutable.HashMap[Aggregation, ArrayBuffer[Double]]

          historyData(systemName).foreach(tuple => {
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
            val ab = predictValue.getOrElse(systemName, new mutable.HashMap[Aggregation, ArrayBuffer[Long]])(aggregation)

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
            //          map.put(aggregation, ab)
            //          preMap.put(systemName, map)
          })
        }

        //回归值写入数据库
        predictValue(systemName).foreach(tuple => {
          out.collect((RegressionUtil.getSingleSystemRegressionObject(systemName, RegressionUtil.getRegressionText(tuple
            ._2), timestamp + TimeUtil.MINUTE_MILLISECOND, aggregationsMap(tuple._1)._1), true))
          regressionSend.add(1)
        })
      }
    }
  }

}