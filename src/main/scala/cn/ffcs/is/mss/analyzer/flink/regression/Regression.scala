/*
 * @project Default (Template) Project
 * @company Fujian Fujitsu Communication Software Co., Ltd.
 * @author chenwei
 * @date 2019-08-07 22:57:46
 * @version v1.0
 * @update [no] [date YYYY-MM-DD] [name] [description]
 */
package cn.ffcs.is.mss.analyzer.flink.regression

import java.sql.{Date, Timestamp}
import java.util.Properties

import cn.ffcs.is.mss.analyzer.bean._
import cn.ffcs.is.mss.analyzer.druid.model.scala.OperationModel
import cn.ffcs.is.mss.analyzer.flink.regression.utils.RegressionUtil
import cn.ffcs.is.mss.analyzer.flink.sink.MySQLSink
import cn.ffcs.is.mss.analyzer.utils._
import cn.ffcs.is.mss.analyzer.utils.druid.entity._
import org.apache.flink.api.common.accumulators.LongCounter
import org.apache.flink.api.common.state.{ValueState, ValueStateDescriptor}
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.functions.{AssignerWithPunctuatedWatermarks, ProcessFunction}
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.watermark.Watermark
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaConsumer, FlinkKafkaProducer}
import org.apache.flink.streaming.util.serialization.SimpleStringSchema
import org.apache.flink.util.Collector

import scala.collection.mutable
import scala.collection.JavaConversions._
import scala.collection.mutable.ArrayBuffer
import scala.util.Random
/**
  *
  * @author chenwei
  * @date 2019-08-07 22:56:00
  * @title Regression3
  * @update [no] [date YYYY-MM-DD] [name] [description]
  */
object Regression {

  def main(args: Array[String]): Unit = {

    //根据传入的参数解析配置文件
    //    val confProperties = new IniProperties(args0)
    val confProperties = new IniProperties(args(0))
    //该任务的名字
    val jobName = confProperties.getValue(Constants.FLINK_REGRESSION_CONFIG, Constants.REGRESSION_JOB_NAME)
    //kafka Source的名字
    val kafkaSourceName = confProperties.getValue(Constants.FLINK_REGRESSION_CONFIG, Constants.REGRESSION_KAFKA_SOURCE_NAME)
    //整体用户mysql sink的名字
    val allUserSqlSinkName = confProperties.getValue(Constants.FLINK_REGRESSION_CONFIG, Constants.REGRESSION_ALL_USER_SQL_SINK_NAME)
    //单系统mysql sink的名字
    val singleSystemSqlSinkName = confProperties.getValue(Constants.FLINK_REGRESSION_CONFIG, Constants.REGRESSION_SINGLE_SYSTEM_SQL_SINK_NAME)
    //单省mysql sink的名字
    val singleProvinceSqlSinkName = confProperties.getValue(Constants.FLINK_REGRESSION_CONFIG, Constants.REGRESSION_SINGLE_PROVINCE_SQL_SINK_NAME)

    //kafka Source的并行度
    val kafkaSourceParallelism = confProperties.getIntValue(Constants.FLINK_REGRESSION_CONFIG, Constants.REGRESSION_KAFKA_SOURCE_PARALLELISM)
    //整体回归并行度
    val allUserRegressionParallelism = confProperties.getIntValue(Constants.FLINK_REGRESSION_CONFIG, Constants.REGRESSION_ALL_USER_REGRESSION_PARALLELISM)
    //单个省回归并行度
    val singleProvinceRegressionParallelism = confProperties.getIntValue(Constants.FLINK_REGRESSION_CONFIG, Constants.REGRESSION_SINGLE_PROVINCE_REGRESSION_PARALLELISM)
    //单个系统huigui并行度
    val singleSystemRegressionParallelism = confProperties.getIntValue(Constants.FLINK_REGRESSION_CONFIG, Constants.REGRESSION_SINGLE_SYSTEM_REGRESSION_PARALLELISM)
    //写入mysql的并行度
    val sqlSinkParallelism = confProperties.getIntValue(Constants.FLINK_REGRESSION_CONFIG, Constants.REGRESSION_SQL_SINK_PARALLELISM)

    //kafka的服务地址
    val brokerList = confProperties.getValue(Constants.FLINK_COMMON_CONFIG, Constants.KAFKA_BOOTSTRAP_SERVERS)
    //flink消费的group.id
    val groupId = confProperties.getValue(Constants.FLINK_REGRESSION_CONFIG, Constants.REGRESSION_GROUP_ID)
    //kafka的topic
    val topic = confProperties.getValue(Constants.OPERATION_FLINK_TO_DRUID_CONFIG, Constants.OPERATION_TO_KAFKA_TOPIC)


    //flink全局变量
    val parameters: Configuration = new Configuration()

    //c3p0连接池配置文件路径
    parameters.setString(Constants.c3p0_CONFIG_PATH, confProperties.getValue(Constants.FLINK_COMMON_CONFIG, Constants.c3p0_CONFIG_PATH))

    //druid的broker节点
    parameters.setString(Constants.DRUID_BROKER_HOST_PORT, confProperties.getValue(Constants.FLINK_COMMON_CONFIG, Constants.DRUID_BROKER_HOST_PORT))
    //druid时间格式
    parameters.setString(Constants.DRUID_TIME_FORMAT, confProperties.getValue(Constants.FLINK_COMMON_CONFIG, Constants.DRUID_TIME_FORMAT))
    //druid数据开始的时间
    parameters.setLong(Constants.DRUID_DATA_START_TIMESTAMP, confProperties.getLongValue(Constants.FLINK_COMMON_CONFIG, Constants.DRUID_DATA_START_TIMESTAMP))

    //业务话单在druid的表名
    parameters.setString(Constants.DRUID_OPERATION_TABLE_NAME, confProperties.getValue(Constants.FLINK_COMMON_CONFIG, Constants.DRUID_OPERATION_TABLE_NAME))

    parameters.setDouble(Constants.REGRESSION_ALL_USER_REGRESSION_K,
      confProperties.getFloatValue(Constants.FLINK_REGRESSION_CONFIG, Constants.REGRESSION_ALL_USER_REGRESSION_K))
    parameters.setLong(Constants.REGRESSION_ALL_USER_REGRESSION_SCOPE_OF_REGRESSION,
      confProperties.getLongValue(Constants.FLINK_REGRESSION_CONFIG, Constants.REGRESSION_ALL_USER_REGRESSION_SCOPE_OF_REGRESSION))
    parameters.setLong(Constants.REGRESSION_ALL_USER_REGRESSION_SCOPE_OF_WARN,
      confProperties.getLongValue(Constants.FLINK_REGRESSION_CONFIG, Constants.REGRESSION_ALL_USER_REGRESSION_SCOPE_OF_WARN))
    parameters.setString(Constants.REGRESSION_ALL_USER_REGRESSION_WARN_LEVEL,
      confProperties.getValue(Constants.FLINK_REGRESSION_CONFIG, Constants.REGRESSION_ALL_USER_REGRESSION_WARN_LEVEL))

    parameters.setDouble(Constants.REGRESSION_SINGLE_PROVINCE_REGRESSION_K,
      confProperties.getFloatValue(Constants.FLINK_REGRESSION_CONFIG, Constants.REGRESSION_SINGLE_PROVINCE_REGRESSION_K))
    parameters.setLong(Constants.REGRESSION_SINGLE_PROVINCE_REGRESSION_SCOPE_OF_REGRESSION,
      confProperties.getLongValue(Constants.FLINK_REGRESSION_CONFIG, Constants.REGRESSION_SINGLE_PROVINCE_REGRESSION_SCOPE_OF_REGRESSION))
    parameters.setLong(Constants.REGRESSION_SINGLE_PROVINCE_REGRESSION_SCOPE_OF_WARN,
      confProperties.getLongValue(Constants.FLINK_REGRESSION_CONFIG, Constants.REGRESSION_SINGLE_PROVINCE_REGRESSION_SCOPE_OF_WARN))
    parameters.setString(Constants.REGRESSION_SINGLE_PROVINCE_REGRESSION_WARN_LEVEL,
      confProperties.getValue(Constants.FLINK_REGRESSION_CONFIG, Constants.REGRESSION_SINGLE_PROVINCE_REGRESSION_WARN_LEVEL))

    parameters.setDouble(Constants.REGRESSION_SINGLE_SYSTEM_REGRESSION_K,
      confProperties.getFloatValue(Constants.FLINK_REGRESSION_CONFIG, Constants.REGRESSION_SINGLE_SYSTEM_REGRESSION_K))
    parameters.setLong(Constants.REGRESSION_SINGLE_SYSTEM_REGRESSION_SCOPE_OF_REGRESSION,
      confProperties.getLongValue(Constants.FLINK_REGRESSION_CONFIG, Constants.REGRESSION_SINGLE_SYSTEM_REGRESSION_SCOPE_OF_REGRESSION))
    parameters.setLong(Constants.REGRESSION_SINGLE_SYSTEM_REGRESSION_SCOPE_OF_WARN,
      confProperties.getLongValue(Constants.FLINK_REGRESSION_CONFIG, Constants.REGRESSION_SINGLE_SYSTEM_REGRESSION_SCOPE_OF_WARN))
    parameters.setString(Constants.REGRESSION_SINGLE_SYSTEM_REGRESSION_WARN_LEVEL,
      confProperties.getValue(Constants.FLINK_REGRESSION_CONFIG, Constants.REGRESSION_SINGLE_SYSTEM_REGRESSION_WARN_LEVEL))


    val checkpointInterval = confProperties.getLongValue(Constants.FLINK_REGRESSION_CONFIG, Constants.REGRESSION_CHECKPOINT_INTERVAL)

    //设置kafka消费者相关配置
    val props = new Properties()
    //设置kafka集群地址
    props.setProperty("bootstrap.servers", brokerList)
    //设置flink消费的group.id
    props.setProperty("group.id", groupId)

    //获取kafka消费者
    val consumer = new FlinkKafkaConsumer[String](topic, new SimpleStringSchema, props).setStartFromGroupOffsets()
    val producer = new FlinkKafkaProducer[String]("allRegressionOut", new SimpleStringSchema, props)
    //获取ExecutionEnvironment
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    //设置check pointing的间隔
    env.enableCheckpointing(checkpointInterval)
    //设置流的时间为EventTime
    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)
    //设置flink全局变量
    env.getConfig.setGlobalJobParameters(parameters)


    //获取kafka数据
    val dStream = env.addSource(consumer).setParallelism(kafkaSourceParallelism)
      .uid(kafkaSourceName).name(kafkaSourceName)

    val operationModelStream = dStream
      //将kafka收到的json串转成对象
      .map(JsonUtil.fromJson[OperationModel] _)
      .assignTimestampsAndWatermarks(new AssignerWithPunctuatedWatermarks[OperationModel] {
        override def checkAndGetNextWatermark(lastElement: OperationModel, extractedTimestamp: Long): Watermark =
          new Watermark(extractedTimestamp - 10000)

        override def extractTimestamp(element: OperationModel, previousElementTimestamp: Long): Long =
          element.timeStamp
      })
      .map(tuple => (tuple.timeStamp / 1000 / 60 * 1000 * 60, tuple.loginSystem, tuple.loginPlace, tuple.connCount, tuple.octets, Set[String](tuple.userName)))

    val allOperationModelStream = operationModelStream
      .keyBy(0)
      .timeWindow(Time.minutes(1), Time.minutes(1))
      .reduce((o1, o2) => {
        (o1._1, "", "", o1._4 + o2._4, o1._5 + o2._5, o1._6 ++ o2._6)
      })
      .map(t => {
        (t._1, t._4, t._5, t._6)
      })
      .process(new AllRegression).setParallelism(allUserRegressionParallelism)

    allOperationModelStream.addSink(new MySQLSink).setParallelism(sqlSinkParallelism)
      .uid(allUserSqlSinkName).name(allUserSqlSinkName)

    val singleSystemOperationModelStream = operationModelStream
      //过滤未知系统的访问
      .filter(tuple => !"未知系统".equals(tuple._2))
      .keyBy(1)
      .timeWindow(Time.minutes(1), Time.minutes(1))
      .reduce((o1, o2) => {
        (o1._1, o1._2, "", o1._4 + o2._4, o1._5 + o2._5, o1._6 ++ o2._6)
      })
      .map(t => {
        (t._1, t._2, t._4, t._5, t._6)
      })
      .keyBy(1)
      .process(new SingleSystemRegression).setParallelism(singleSystemRegressionParallelism)

    singleSystemOperationModelStream.addSink(new MySQLSink).setParallelism(sqlSinkParallelism)
      .uid(singleSystemSqlSinkName).name(singleSystemSqlSinkName)


    val singleProvinceOperationModelStream = operationModelStream
      //过滤未知地点的访问
      .filter(tuple => !"未知地点".equals(tuple._3))
      .keyBy(2)
      .timeWindow(Time.minutes(1), Time.minutes(1))
      .reduce((o1, o2) => {
        (o1._1, "", o1._3, o1._4 + o2._4, o1._5 + o2._5, o1._6 ++ o2._6)
      })
      .map(t => {
        (t._1, t._3, t._4, t._5, t._6)
      })
      .keyBy(1)
      .process(new SingleProvinceRegression).setParallelism(singleProvinceRegressionParallelism)

    singleProvinceOperationModelStream.addSink(new MySQLSink).setParallelism(sqlSinkParallelism)
      .uid(singleProvinceSqlSinkName).name(singleProvinceSqlSinkName)

    env.execute(jobName)
  }

  class AllRegression extends ProcessFunction[(Long, Long, Long, Set[String]), (Object, Boolean)] {

    var tableName: String = null
    var k = 0.0
    var scopeOfRegression: Long = 0L
    var scopeOfWarn: Long = 0L
    var warnLevelArray: Array[Double] = null

    lazy val aggregationsMap = mutable.Map[Aggregation, (Class[_ <: Object], Class[_ <: Object])]()

    private val messagesReceived = new LongCounter()
    private val warnSend = new LongCounter()
    private val regressionSend = new LongCounter()


    //记录真实值map[时间戳，map[值类型,值] ]
    var valueStage : mutable.Map[Long, mutable.Map[Aggregation, Double]] = null
    //记录当天回归值map[值类型,值]
    var regressionStage : mutable.Map[Aggregation, ArrayBuffer[Long]] = null
    //记录regressionStage保存的是哪一天的回归值。解决内存泄漏的问题
    var regressionTimeStage: mutable.Map[Aggregation, Long] = null

    override def open(parameters: Configuration): Unit = {
      //获取全局配置
      val globConf = getRuntimeContext.getExecutionConfig.getGlobalJobParameters.asInstanceOf[Configuration]
      //获取表名
      tableName = globConf.getString(Constants.DRUID_OPERATION_TABLE_NAME, "")
      //获取局部加权k值
      k = globConf.getDouble(Constants.REGRESSION_ALL_USER_REGRESSION_K, 1.0)
      //获取回归范围
      scopeOfRegression = globConf.getLong(Constants.REGRESSION_ALL_USER_REGRESSION_SCOPE_OF_REGRESSION, TimeUtil.MINUTE_MILLISECOND * 500)
      //获取告警范围
      scopeOfWarn = globConf.getLong(Constants.REGRESSION_ALL_USER_REGRESSION_SCOPE_OF_WARN, TimeUtil.MINUTE_MILLISECOND * 10)
      //获取告警等级数组
      warnLevelArray = RegressionUtil.getWarnLevelArray(globConf.getString(Constants.REGRESSION_ALL_USER_REGRESSION_WARN_LEVEL, "0.5|1|2|4|8"))

      //设置druid的broker的host和port
      DruidUtil.setDruidHostPortSet(globConf.getString(Constants.DRUID_BROKER_HOST_PORT, ""))
      //设置写入druid的时间格式
      DruidUtil.setTimeFormat(globConf.getString(Constants.DRUID_TIME_FORMAT, ""))
      //设置druid开始的时间
      DruidUtil.setDateStartTimeStamp(globConf.getLong(Constants.DRUID_DATA_START_TIMESTAMP, 0L))

      //设置回归值类型和对应得告警表和回归表
      aggregationsMap.put(Aggregation.connCount, (classOf[BbasAllUserConncountRegressionEntity], classOf[BbasAllUserConncountWarnEntity]))
      aggregationsMap.put(Aggregation.octets, (classOf[BbasAllUserOctetsRegressionEntity], classOf[BbasAllUserOctetsWarnEntity]))
      aggregationsMap.put(Aggregation.userNameCount, (classOf[BbasAllUserUsercountRegressionEntity], classOf[BbasAllUserUsercountWarnEntity]))

      getRuntimeContext.addAccumulator("AllUserRegression: Messages received", messagesReceived)
      getRuntimeContext.addAccumulator("AllUserRegression: Warn send", warnSend)
      getRuntimeContext.addAccumulator("AllUserRegression: Regression send", regressionSend)

    }

    override def processElement(value: (Long, Long, Long, Set[String]), ctx: ProcessFunction[(Long, Long, Long, Set[String]), (Object, Boolean)]#Context, out: Collector[(Object, Boolean)]): Unit = {

      messagesReceived.add(1)
      //时间戳
      val timeStamp = value._1
      //链接次数
      val connCount = value._2
      //流量值
      val octets = value._3
      //用户数
      val userCount = value._4.size.toDouble

      //当前聚合的值
      val countMap = mutable.Map[Aggregation, Double]()
      countMap.put(Aggregation.connCount, connCount)
      countMap.put(Aggregation.octets, octets)
      countMap.put(Aggregation.userNameCount, userCount)

      //获取之前记录的值,如果为空则新建
      var current = valueStage
      if (current == null) {
        current = mutable.Map[Long, mutable.Map[Aggregation, Double]]()
      }


      //如果有回归范围内不存在的值，查询druid填充
      var startTimeStamp = timeStamp
      if (current.nonEmpty){
        startTimeStamp = current.keys.min
      }
      if (timeStamp - startTimeStamp < scopeOfRegression) {
        val queryStartTimeStamp = timeStamp - scopeOfRegression
        val queryEndTimeStampTemp = startTimeStamp
        val entity = getAllUserQueryEntity(tableName, queryStartTimeStamp, queryEndTimeStampTemp, aggregationsMap.keySet)
        RegressionUtil.queryDruidFillEmptyValue(queryStartTimeStamp, queryEndTimeStampTemp, entity, aggregationsMap.keySet, current)
        startTimeStamp = queryStartTimeStamp
      }

      val endTimeStamp = current.keys.max + TimeUtil.MINUTE_MILLISECOND
      for (i <- endTimeStamp until timeStamp by TimeUtil.MINUTE_MILLISECOND){
        if (!current.contains(i)) {
          countMap.keySet.foreach(aggregations => {
            current.getOrElseUpdate(i, mutable.Map[Aggregation, Double]()).put(aggregations, 0)
          })
        }
      }

      //如果有回归范围外的值，删除其值
      while (timeStamp - startTimeStamp > scopeOfRegression) {
        current.remove(startTimeStamp)
        startTimeStamp += TimeUtil.MINUTE_MILLISECOND
      }


      //增加这一分钟的值
      current.put(timeStamp, countMap)

      //获取今天的回归值
      var regression = regressionStage

      //如果之前的回归值不为空的话,进行判断如果出告警的生成告警对象
      if (regression != null) {

        for ((aggregation, count) <- countMap) {
          if (RegressionUtil.isWarn(regression(aggregation).last, current, aggregation, timeStamp, scopeOfWarn, warnLevelArray)) {
            val warnLevel = RegressionUtil.getWarnLevel(count, regression(aggregation).last, warnLevelArray)
            out.collect((getAllUserWarnObject(count, regression(aggregation).last, timeStamp, warnLevel, aggregationsMap(aggregation)._2), true))
            warnSend.add(1)
          }
        }

      } else {

        //如果为空则根据真实值填回归值
        regression = mutable.Map[Aggregation, ArrayBuffer[Long]]()

        for (aggregation <- aggregationsMap.keys) {
          val arrayBuffer = ArrayBuffer[Long]()
          for (i <- 0 until getIndex(timeStamp + TimeUtil.MINUTE_MILLISECOND)) {
            val v = current.getOrElse(TimeUtil.getDayStartTime(timeStamp + TimeUtil.MINUTE_MILLISECOND) + i * TimeUtil.MINUTE_MILLISECOND, mutable.Map[Aggregation, Double]()).getOrElse(aggregation, 0.0).toLong
            if (v > 0) {
              arrayBuffer.add(v * (110 - Random.nextInt(20)) / 100)
            }else{
              arrayBuffer.append(0)
            }

          }
          regression.put(aggregation, arrayBuffer)
        }
      }

      var regressionTimeMap = regressionTimeStage
      if (regressionTimeMap == null) {
        regressionTimeMap = mutable.Map[Aggregation, Long]()
      }

      //计算回归值
      for (aggregation <- countMap.keys) {

        //如果要预测新一天的值，将记录的回归值清空
        val regressionTime = regressionTimeMap.getOrElse(aggregation, 0L)
        if (TimeUtil.getDayStartTime(regressionTime) != TimeUtil.getDayStartTime(timeStamp + TimeUtil.MINUTE_MILLISECOND)
          && regression.contains(aggregation)) {
          regression.remove(aggregation)
          regressionTimeMap.put(aggregation, timeStamp + TimeUtil.MINUTE_MILLISECOND)
        }

        val arrayBuffer = regression.getOrElse(aggregation, ArrayBuffer[Long]())
        //如果存在之前某一分钟没有值的情况，则补零
        for (i <- arrayBuffer.length until getIndex(timeStamp + TimeUtil.MINUTE_MILLISECOND)) {
          arrayBuffer.append(0)
        }
        //增加这一分钟的回归值
        arrayBuffer.append(RegressionUtil.predict(timeStamp, aggregation, k, current).toLong)
        regression.put(aggregation, arrayBuffer)
      }

      //生成回归对象写入数据库
      for ((aggregation, arrayBuffer) <- regression) {
        out.collect((getAllUserRegressionObject(RegressionUtil.getRegressionText(arrayBuffer),
          timeStamp + TimeUtil.MINUTE_MILLISECOND, aggregationsMap(aggregation)._1), true))
        regressionSend.add(1)
      }

      //更新回归值
      regressionStage = regression
      //更新真实值
      valueStage = current
      //更新回归时间
      regressionTimeStage = regressionTimeMap
    }

    /**
      * 根据计算的结果通过反射获取告警对象
      *
      * @param realValue
      * @param regressionValue
      * @param timeStamp
      * @param warnLevel
      * @param warnObject
      * @return
      */
    def getAllUserWarnObject(realValue: Double, regressionValue: Double, timeStamp: Long, warnLevel: Int, warnObject: Class[_ <: Object]): Object = {

      val warnEntity = warnObject.newInstance().asInstanceOf[ {
        def setRealValue(long: java.lang.Long)
        def setRegressionValue(long: java.lang.Long)
        def setWarnDatetime(timestamp: Timestamp)
        def setWarnLevel(integer: java.lang.Integer)}]

      warnEntity.setRealValue(realValue.toLong)
      warnEntity.setRegressionValue(regressionValue.toLong)
      warnEntity.setWarnDatetime(new Timestamp(timeStamp))
      warnEntity.setWarnLevel(warnLevel)

      warnEntity.asInstanceOf[Object]
    }

    /**
      * 根据计算的结果通过反射获取回归对象
      *
      * @param regressionValueText
      * @param timeStamp
      * @param regressionObject
      * @return
      */
    def getAllUserRegressionObject(regressionValueText: String, timeStamp: Long, regressionObject: Class[_ <: Object]): Object = {

      val regressionEntity = regressionObject.newInstance().asInstanceOf[ {def setRegressionValueText(string: String)
        def setRegressionDate(date: Date)}]

      regressionEntity.setRegressionDate(new Date(timeStamp))
      regressionEntity.setRegressionValueText(regressionValueText)

      regressionEntity.asInstanceOf[Object]
    }

    /**
      * 根据时间戳获取整体回归的查询串
      *
      * @param startTimeStamp
      * @param endTimeStamp
      * @return
      */
    def getAllUserQueryEntity(tableName: String, startTimeStamp: Long, endTimeStamp: Long,
                              aggregationsSet: collection.Set[Aggregation]): Entity = {
      val entity = new Entity()
      entity.setTableName(tableName)
      entity.setDimensionsSet(Dimension.getDimensionsSet())
      entity.setAggregationsSet(aggregationsSet.map(tuple => Aggregation.getAggregation(tuple)))
      entity.setGranularity(Granularity.getGranularity(Granularity.periodMinute, 1))
      entity.setStartTimeStr(startTimeStamp)
      entity.setEndTimeStr(endTimeStamp)
      return entity
    }

  }


  class SingleSystemRegression extends ProcessFunction[(Long, String, Long, Long, Set[String]), (Object, Boolean)] {

    var tableName: String = null
    var k = 0.0
    var scopeOfRegression: Long = 0L
    var scopeOfWarn: Long = 0L
    var warnLevelArray: Array[Double] = null

    lazy val aggregationsMap = mutable.Map[Aggregation, (Class[_ <: Object], Class[_ <: Object])]()

    val messagesReceived = new LongCounter()
    val warnSend = new LongCounter()
    val regressionSend = new LongCounter()

    /**
      * 记录真实值map[时间戳，map[值类型,值] ]
      */
    lazy val valueStage: ValueState[mutable.Map[Long, mutable.Map[Aggregation, Double]]] = getRuntimeContext
      .getState(new ValueStateDescriptor[mutable.Map[Long, mutable.Map[Aggregation, Double]]]
      ("valueStage", classOf[mutable.Map[Long, mutable.Map[Aggregation, Double]]]))
    /**
      * 记录当天回归值map[值类型,值]
      */
    lazy val regressionStage: ValueState[mutable.Map[Aggregation, ArrayBuffer[Long]]] = getRuntimeContext
      .getState(new ValueStateDescriptor[mutable.Map[Aggregation, ArrayBuffer[Long]]]
      ("regressionStage", classOf[mutable.Map[Aggregation, ArrayBuffer[Long]]]))

    /**
     * 记录regressionStage保存的是哪一天的回归值。解决内存泄漏的问题
     */
    lazy val regressionTimeStage: ValueState[mutable.Map[Aggregation, Long]] = getRuntimeContext
      .getState(new ValueStateDescriptor[mutable.Map[Aggregation, Long]]
      ("regressionTimeStage", classOf[mutable.Map[Aggregation, Long]]))
    override def open(parameters: Configuration): Unit = {
      //获取全局配置
      val globConf = getRuntimeContext.getExecutionConfig.getGlobalJobParameters.asInstanceOf[Configuration]
      //获取表名
      tableName = globConf.getString(Constants.DRUID_OPERATION_TABLE_NAME, "")
      //获取局部加权k值
      k = globConf.getDouble(Constants.REGRESSION_SINGLE_SYSTEM_REGRESSION_K, 1.0)
      //获取回归范围
      scopeOfRegression = globConf.getLong(Constants.REGRESSION_SINGLE_SYSTEM_REGRESSION_SCOPE_OF_REGRESSION, TimeUtil.MINUTE_MILLISECOND * 500)
      //获取告警范围
      scopeOfWarn = globConf.getLong(Constants.REGRESSION_SINGLE_SYSTEM_REGRESSION_SCOPE_OF_WARN, TimeUtil.MINUTE_MILLISECOND * 10)
      //获取告警等级数组
      warnLevelArray = RegressionUtil.getWarnLevelArray(globConf.getString(Constants.REGRESSION_SINGLE_SYSTEM_REGRESSION_WARN_LEVEL, "0.5|1|2|4|8"))

      //设置druid的broker的host和port
      DruidUtil.setDruidHostPortSet(globConf.getString(Constants.DRUID_BROKER_HOST_PORT, ""))
      //设置写入druid的时间格式
      DruidUtil.setTimeFormat(globConf.getString(Constants.DRUID_TIME_FORMAT, ""))
      //设置druid开始的时间
      DruidUtil.setDateStartTimeStamp(globConf.getLong(Constants.DRUID_DATA_START_TIMESTAMP, 0L))

      //设置回归值类型和对应得告警表和回归表
      aggregationsMap.put(Aggregation.connCount, (classOf[BbasSingleSystemConncountRegressionEntity], classOf[BbasSingleSystemConncountWarnEntity]))
      aggregationsMap.put(Aggregation.octets, (classOf[BbasSingleSystemOctetsRegressionEntity], classOf[BbasSingleSystemOctetsWarnEntity]))
      aggregationsMap.put(Aggregation.userNameCount, (classOf[BbasSingleSystemUsercountRegressionEntity], classOf[BbasSingleSystemUsercountWarnEntity]))

      getRuntimeContext.addAccumulator("SingleSystemRegression: Messages received", messagesReceived)
      getRuntimeContext.addAccumulator("SingleSystemRegression: Warn send", warnSend)
      getRuntimeContext.addAccumulator("SingleSystemRegression: Regression send", regressionSend)

    }

    override def processElement(value: (Long, String, Long, Long, Set[String]), ctx: ProcessFunction[(Long, String, Long, Long, Set[String]), (Object, Boolean)]#Context, out: Collector[(Object, Boolean)]): Unit = {

      messagesReceived.add(1)
      val timeStamp = value._1
      val systemName = value._2
      val connCount = value._3
      val octets = value._4
      val userCount = value._5.size.toDouble

      //当前聚合的值
      val countMap = mutable.Map[Aggregation, Double]()
      countMap.put(Aggregation.connCount, connCount)
      countMap.put(Aggregation.octets, octets)
      countMap.put(Aggregation.userNameCount, userCount)

      //获取之前记录的值
      var current = valueStage.value()
      if (current == null) {
        current = mutable.Map[Long, mutable.Map[Aggregation, Double]]()
      }


      //如果有回归范围内不存在的值，查询druid填充
      var startTimeStamp = timeStamp
      if (current.nonEmpty){
        startTimeStamp = current.keys.min
      }
      if (timeStamp - startTimeStamp < scopeOfRegression) {
        val startTimeStampTemp = timeStamp - scopeOfRegression
        val endTimeStampTemp = startTimeStamp
        val entity = getSingleSystemQueryEntity(tableName, startTimeStampTemp, endTimeStampTemp, systemName, aggregationsMap.keySet)
        RegressionUtil.queryDruidFillEmptyValue(startTimeStampTemp, endTimeStampTemp, entity, aggregationsMap.keySet, current)
        startTimeStamp = startTimeStampTemp
      }

      val endTimeStamp = current.keys.max + TimeUtil.MINUTE_MILLISECOND
      for (i <- endTimeStamp until timeStamp by TimeUtil.MINUTE_MILLISECOND){
        if (!current.contains(i)) {
          countMap.keySet.foreach(aggregations => {
            current.getOrElseUpdate(i, mutable.Map[Aggregation, Double]()).put(aggregations, 0)
          })
        }
      }

      //如果有回归范围外的值，删除其值
      while (timeStamp - startTimeStamp > scopeOfRegression) {
        current.remove(startTimeStamp)
        startTimeStamp += TimeUtil.MINUTE_MILLISECOND
      }

      //增加这一分钟的值
      current.put(timeStamp, countMap)

      //获取上次的回归值
      var regression = regressionStage.value()



      //如果回归值不为空
      if (regression != null) {
        //进行判断如果出告警的生成告警对象
        for ((aggregation, count) <- countMap) {
          if (RegressionUtil.isWarn(regression(aggregation).last, current, aggregation, timeStamp, scopeOfWarn, warnLevelArray)) {
            val warnLevel = RegressionUtil.getWarnLevel(count, regression(aggregation).last, warnLevelArray)
            out.collect((getSingleSystemWarnObject(systemName, count, regression(aggregation).last, timeStamp, warnLevel, aggregationsMap(aggregation)._2), true))
            warnSend.add(1)
          }
        }

      } else {
        //如果为空则根据真实值填回归值
        regression = mutable.Map[Aggregation, ArrayBuffer[Long]]()

        for (aggregation <- aggregationsMap.keys) {
          val arrayBuffer = ArrayBuffer[Long]()
          for (i <- 0 until getIndex(timeStamp + TimeUtil.MINUTE_MILLISECOND)) {
            val v = current.getOrElse(TimeUtil.getDayStartTime(timeStamp + TimeUtil.MINUTE_MILLISECOND) + i * TimeUtil.MINUTE_MILLISECOND, mutable.Map[Aggregation, Double]()).getOrElse(aggregation, 0.0).toLong
            if (v > 0) {
              arrayBuffer.add(v * (110 - Random.nextInt(20)) / 100)
            }else{
              arrayBuffer.append(0)
            }
          }
          regression.put(aggregation, arrayBuffer)
        }

      }

      var regressionTimeMap = regressionTimeStage.value()
      if (regressionTimeMap == null) {
        regressionTimeMap = mutable.Map[Aggregation, Long]()
      }

      //计算回归值
      for (aggregation <- countMap.keys) {
        val regressionTime = regressionTimeMap.getOrElse(aggregation, 0L)
        if (TimeUtil.getDayStartTime(regressionTime) != TimeUtil.getDayStartTime(timeStamp + TimeUtil.MINUTE_MILLISECOND)
          && regression.contains(aggregation)) {
          regression.remove(aggregation)
          regressionTimeMap.put(aggregation, timeStamp + TimeUtil.MINUTE_MILLISECOND)
        }

        val arrayBuffer = regression.getOrElse(aggregation, ArrayBuffer[Long]())
        for (i <- arrayBuffer.length until getIndex(timeStamp + TimeUtil.MINUTE_MILLISECOND)) {
          arrayBuffer.append(0)
        }
        arrayBuffer.append(RegressionUtil.predict(timeStamp, aggregation, k, current).toLong)
        regression.put(aggregation, arrayBuffer)
      }


      for ((aggregation, arrayBuffer) <- regression) {
        out.collect((getSingleSystemRegressionObject(systemName, RegressionUtil.getRegressionText(arrayBuffer),
          timeStamp + TimeUtil.MINUTE_MILLISECOND, aggregationsMap(aggregation)._1), true))
        regressionSend.add(1)
      }

      regressionStage.update(regression)
      valueStage.update(current)
      regressionTimeStage.update(regressionTimeMap)

    }

    /**
      * 根据时间戳获取查询指定系统数据的查询串
      *
      * @param tableName
      * @param startTimeStamp
      * @param endTimeStamp
      * @param systemName
      * @param aggregationsSet
      * @return
      */
    def getSingleSystemQueryEntity(tableName: String, startTimeStamp: Long, endTimeStamp: Long, systemName: String, aggregationsSet: collection.Set[Aggregation]): Entity = {
      val entity = new Entity()
      entity.setTableName(tableName)
      entity.setDimensionsSet(Dimension.getDimensionsSet(Dimension.loginSystem))
      entity.setAggregationsSet(aggregationsSet.map(tuple => Aggregation.getAggregation(tuple)))
      entity.setGranularity(Granularity.getGranularity(Granularity.periodMinute, 1))
      entity.setFilter(Filter.getFilter(Filter.selector, Dimension.loginSystem, systemName))
      entity.setStartTimeStr(startTimeStamp)
      entity.setEndTimeStr(endTimeStamp)
      entity
    }

    /**
      * 根据计算的结果通过反射获取告警对象
      *
      * @param systemName
      * @param realValue
      * @param regressionValue
      * @param timeStamp
      * @param warnLevel
      * @param warnObject
      * @return
      */
    def getSingleSystemWarnObject(systemName: String, realValue: Double, regressionValue: Double, timeStamp: Long, warnLevel: Int, warnObject: Class[_ <: Object]): Object = {

      val warnEntity = warnObject.newInstance().asInstanceOf[ {
        def setSystemName(systemName: String)
        def setRealValue(long: java.lang.Long)
        def setRegressionValue(long: java.lang.Long)
        def setWarnDatetime(timestamp: Timestamp)
        def setWarnLevel(integer: java.lang.Integer)}]

      warnEntity.setSystemName(systemName)
      warnEntity.setRealValue(realValue.toLong)
      warnEntity.setRegressionValue(regressionValue.toLong)
      warnEntity.setWarnDatetime(new Timestamp(timeStamp))
      warnEntity.setWarnLevel(warnLevel)

      warnEntity.asInstanceOf[Object]
    }

    /**
      * 根据计算的结果通过反射获取回归对象
      *
      * @param systemName
      * @param regressionValueText
      * @param timeStamp
      * @param regressionObject
      * @return
      */
    def getSingleSystemRegressionObject(systemName: String, regressionValueText: String, timeStamp: Long, regressionObject: Class[_ <: Object]): Object = {

      val regressionEntity = regressionObject.newInstance().asInstanceOf[ {
        def setSystemName(systemName: String)
        def setRegressionValueText(string: String)
        def setRegressionDate(date: Date)}]

      regressionEntity.setSystemName(systemName)
      regressionEntity.setRegressionDate(new Date(timeStamp))
      regressionEntity.setRegressionValueText(regressionValueText)
      regressionEntity.asInstanceOf[Object]
    }

  }

  class SingleProvinceRegression extends ProcessFunction[(Long, String, Long, Long, Set[String]), (Object, Boolean)] {

    var tableName: String = null
    var k = 0.0
    var scopeOfRegression: Long = 0L
    var scopeOfWarn: Long = 0L
    var warnLevelArray: Array[Double] = null

    lazy val aggregationsMap = mutable.Map[Aggregation, (Class[_ <: Object], Class[_ <: Object])]()

    val messagesReceived = new LongCounter()
    val warnSend = new LongCounter()
    val regressionSend = new LongCounter()

    /**
      * 记录真实值map[时间戳，map[值类型,值] ]
      */
    lazy val valueStage: ValueState[mutable.Map[Long, mutable.Map[Aggregation, Double]]] = getRuntimeContext
      .getState(new ValueStateDescriptor[mutable.Map[Long, mutable.Map[Aggregation, Double]]]
      ("valueStage", classOf[mutable.Map[Long, mutable.Map[Aggregation, Double]]]))
    /**
      * 记录当天回归值map[值类型,值]
      */
    lazy val regressionStage: ValueState[mutable.Map[Aggregation, ArrayBuffer[Long]]] = getRuntimeContext
      .getState(new ValueStateDescriptor[mutable.Map[Aggregation, ArrayBuffer[Long]]]
      ("regressionStage", classOf[mutable.Map[Aggregation, ArrayBuffer[Long]]]))

    /**
     * 记录regressionStage保存的是哪一天的回归值。解决内存泄漏的问题
     */
    lazy val regressionTimeStage: ValueState[mutable.Map[Aggregation, Long]] = getRuntimeContext
      .getState(new ValueStateDescriptor[mutable.Map[Aggregation, Long]]
      ("regressionTimeStage", classOf[mutable.Map[Aggregation, Long]]))
    override def open(parameters: Configuration): Unit = {
      //获取全局配置
      val globConf = getRuntimeContext.getExecutionConfig.getGlobalJobParameters.asInstanceOf[Configuration]
      //获取表名
      tableName = globConf.getString(Constants.DRUID_OPERATION_TABLE_NAME, "")
      //获取局部加权k值
      k = globConf.getDouble(Constants.REGRESSION_SINGLE_SYSTEM_REGRESSION_K, 1.0)
      //获取回归范围
      scopeOfRegression = globConf.getLong(Constants.REGRESSION_SINGLE_PROVINCE_REGRESSION_SCOPE_OF_REGRESSION, TimeUtil.MINUTE_MILLISECOND * 500)
      //获取告警范围
      scopeOfWarn = globConf.getLong(Constants.REGRESSION_SINGLE_PROVINCE_REGRESSION_SCOPE_OF_WARN, TimeUtil.MINUTE_MILLISECOND * 10)
      //获取告警等级数组
      warnLevelArray = RegressionUtil.getWarnLevelArray(globConf.getString(Constants.REGRESSION_SINGLE_PROVINCE_REGRESSION_WARN_LEVEL, "0.5|1|2|4|8"))

      //设置druid的broker的host和port
      DruidUtil.setDruidHostPortSet(globConf.getString(Constants.DRUID_BROKER_HOST_PORT, ""))
      //设置写入druid的时间格式
      DruidUtil.setTimeFormat(globConf.getString(Constants.DRUID_TIME_FORMAT, ""))
      //设置druid开始的时间
      DruidUtil.setDateStartTimeStamp(globConf.getLong(Constants.DRUID_DATA_START_TIMESTAMP, 0L))

      //设置回归值类型和对应得告警表和回归表
      aggregationsMap.put(Aggregation.connCount, (classOf[BbasSingleProvinceConncountRegressionEntity], classOf[BbasSingleProvinceConncountWarnEntity]))
      aggregationsMap.put(Aggregation.octets, (classOf[BbasSingleProvinceOctetsRegressionEntity], classOf[BbasSingleProvinceOctetsWarnEntity]))
      aggregationsMap.put(Aggregation.userNameCount, (classOf[BbasSingleProvinceUsercountRegressionEntity], classOf[BbasSingleProvinceUsercountWarnEntity]))

      getRuntimeContext.addAccumulator("SingleProvinceRegression: Messages received", messagesReceived)
      getRuntimeContext.addAccumulator("SingleProvinceRegression: Warn send", warnSend)
      getRuntimeContext.addAccumulator("SingleProvinceRegression: Regression send", regressionSend)

    }

    override def processElement(value: (Long, String, Long, Long, Set[String]), ctx: ProcessFunction[(Long, String, Long, Long, Set[String]), (Object, Boolean)]#Context, out: Collector[(Object, Boolean)]): Unit = {

      val timeStamp = value._1
      val provinceName = value._2
      val connCount = value._3
      val octets = value._4
      val userCount = value._5.size.toDouble

      //当前聚合的值
      val countMap = mutable.Map[Aggregation, Double]()
      countMap.put(Aggregation.connCount, connCount)
      countMap.put(Aggregation.octets, octets)
      countMap.put(Aggregation.userNameCount, userCount)

      //获取之前记录的值
      var current = valueStage.value()
      if (current == null) {
        current = mutable.Map[Long, mutable.Map[Aggregation, Double]]()
      }



      //如果有回归范围内不存在的值，查询druid填充
      var startTimeStamp = timeStamp
      if (current.nonEmpty){
        startTimeStamp = current.keys.min
      }
      if (timeStamp - startTimeStamp < scopeOfRegression) {
        val startTimeStampTemp = timeStamp - scopeOfRegression
        val endTimeStampTemp = startTimeStamp
        val entity = getSingleProvinceQueryEntity(tableName, startTimeStampTemp, endTimeStampTemp, provinceName, aggregationsMap.keySet)
        RegressionUtil.queryDruidFillEmptyValue(startTimeStampTemp, endTimeStampTemp, entity, aggregationsMap.keySet, current)
        startTimeStamp = startTimeStampTemp
      }

      val endTimeStamp = current.keys.max + TimeUtil.MINUTE_MILLISECOND
      for (i <- endTimeStamp until timeStamp by TimeUtil.MINUTE_MILLISECOND){
        if (!current.contains(i)) {
          countMap.keySet.foreach(aggregations => {
            current.getOrElseUpdate(i, mutable.Map[Aggregation, Double]()).put(aggregations, 0)
          })
        }
      }

      //如果有回归范围外的值，删除其值
      while (timeStamp - startTimeStamp > scopeOfRegression) {
        current.remove(startTimeStamp)
        startTimeStamp += TimeUtil.MINUTE_MILLISECOND
      }

      //增加这一分钟的值
      current.put(timeStamp, countMap)
      //获取上次的回归值
      var regression = regressionStage.value()

      //如果回归值不为空
      if (regression != null) {
        //进行判断如果出告警的生成告警对象
        for ((aggregation, count) <- countMap) {
          if (RegressionUtil.isWarn(regression(aggregation).last, current, aggregation, timeStamp, scopeOfWarn, warnLevelArray)) {
            val warnLevel = RegressionUtil.getWarnLevel(count, regression(aggregation).last, warnLevelArray)
            out.collect((getSingleProvinceWarnObject(provinceName, count, regression(aggregation).last, timeStamp, warnLevel, aggregationsMap(aggregation)._2), true))
            warnSend.add(1)
          }
        }

      } else {
        //如果为空则根据真实值填回归值
        regression = mutable.Map[Aggregation, ArrayBuffer[Long]]()

        for (aggregation <- aggregationsMap.keys) {
          val arrayBuffer = ArrayBuffer[Long]()
          for (i <- 0 until getIndex(timeStamp + TimeUtil.MINUTE_MILLISECOND)) {
            val v = current.getOrElse(TimeUtil.getDayStartTime(timeStamp + TimeUtil.MINUTE_MILLISECOND) + i * TimeUtil.MINUTE_MILLISECOND, mutable.Map[Aggregation, Double]()).getOrElse(aggregation, 0.0).toLong
            if (v > 0) {
              arrayBuffer.add(v * (110 - Random.nextInt(20)) / 100)
            }else{
              arrayBuffer.append(0)
            }
          }
          regression.put(aggregation, arrayBuffer)
        }

      }

      var regressionTimeMap = regressionTimeStage.value()
      if (regressionTimeMap == null) {
        regressionTimeMap = mutable.Map[Aggregation, Long]()
      }

      //计算回归值
      for (aggregation <- countMap.keys) {

        val regressionTime = regressionTimeMap.getOrElse(aggregation, 0L)
        if (TimeUtil.getDayStartTime(regressionTime) != TimeUtil.getDayStartTime(timeStamp + TimeUtil.MINUTE_MILLISECOND)
          && regression.contains(aggregation)) {
          regression.remove(aggregation)
          regressionTimeMap.put(aggregation, timeStamp + TimeUtil.MINUTE_MILLISECOND)
        }


        val arrayBuffer = regression.getOrElse(aggregation, ArrayBuffer[Long]())
        for (i <- arrayBuffer.length until getIndex(timeStamp + TimeUtil.MINUTE_MILLISECOND)) {
          arrayBuffer.append(0)
        }
        arrayBuffer.append(RegressionUtil.predict(timeStamp, aggregation, k, current).toLong)
        regression.put(aggregation, arrayBuffer)
      }


      for ((aggregation, arrayBuffer) <- regression) {
        out.collect((getSingleProvinceRegressionObject(provinceName, RegressionUtil.getRegressionText(arrayBuffer),
          timeStamp + TimeUtil.MINUTE_MILLISECOND, aggregationsMap(aggregation)._1), true))
        regressionSend.add(1)
      }

      regressionStage.update(regression)
      valueStage.update(current)
      regressionTimeStage.update(regressionTimeMap)

    }

    /**
      * 获取查询druid的entity
      *
      * @param tableName
      * @param startTimeStamp
      * @param endTimeStamp
      * @param provinceName
      * @param aggregationsSet
      * @return
      */
    def getSingleProvinceQueryEntity(tableName: String, startTimeStamp: Long, endTimeStamp: Long, provinceName: String, aggregationsSet: collection.Set[Aggregation]): Entity = {
      val entity = new Entity()
      entity.setTableName(tableName)
      entity.setDimensionsSet(Dimension.getDimensionsSet(Dimension.loginPlace))
      entity.setAggregationsSet(aggregationsSet.map(tuple => Aggregation.getAggregation(tuple)))
      entity.setGranularity(Granularity.getGranularity(Granularity.periodMinute, 1))
      entity.setFilter(Filter.getFilter(Filter.selector, Dimension.loginPlace, provinceName))
      entity.setStartTimeStr(startTimeStamp)
      entity.setEndTimeStr(endTimeStamp)
      entity
    }

    /**
      * 根据计算的结果通过反射获取告警对象
      *
      * @param provinceName
      * @param realValue
      * @param regressionValue
      * @param timeStamp
      * @param warnLevel
      * @param warnObject
      * @return
      */
    def getSingleProvinceWarnObject(provinceName: String, realValue: Double, regressionValue: Double,
                                    timeStamp: Long, warnLevel: Int, warnObject: Class[_ <: Object]): Object = {

      val warnEntity = warnObject.newInstance().asInstanceOf[ {
        def setProvinceName(provinceName: String)
        def setRealValue(long: java.lang.Long)
        def setRegressionValue(long: java.lang.Long)
        def setWarnDatetime(timestamp: Timestamp)
        def setWarnLevel(integer: java.lang.Integer)}]

      warnEntity.setProvinceName(provinceName)
      warnEntity.setRealValue(realValue.toLong)
      warnEntity.setRegressionValue(regressionValue.toLong)
      warnEntity.setWarnDatetime(new Timestamp(timeStamp))
      warnEntity.setWarnLevel(warnLevel)

      warnEntity.asInstanceOf[Object]
    }

    /**
      * 根据计算的结果通过反射获取回归对象
      *
      * @param provinceName
      * @param regressionValueText
      * @param timeStamp
      * @param regressionObject
      * @return
      */
    def getSingleProvinceRegressionObject(provinceName: String, regressionValueText: String,
                                          timeStamp: Long, regressionObject: Class[_ <: Object]): Object = {

      val regressionEntity = regressionObject.newInstance().asInstanceOf[ {
        def setProvinceName(systemName: String)
        def setRegressionValueText(string: String)
        def setRegressionDate(date: Date)}]

      regressionEntity.setProvinceName(provinceName)
      regressionEntity.setRegressionDate(new Date(timeStamp))
      regressionEntity.setRegressionValueText(regressionValueText)

      regressionEntity.asInstanceOf[Object]
    }
  }

  /**
    * 计算timeStamp是一天中的第几分钟
    * @param timeStamp
    * @return
    */
  def getIndex(timeStamp: Long): Int = {
    val indexTime = TimeUtil.getMinuteStartTime(timeStamp)
    val startTimeStamp = TimeUtil.getDayStartTime(indexTime)
    val duringTime = indexTime - startTimeStamp
    (duringTime.toInt / TimeUtil.MINUTE_MILLISECOND).toInt
  }
}
