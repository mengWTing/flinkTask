package cn.ffcs.is.mss.analyzer.flink.warn

import cn.ffcs.is.mss.analyzer.bean.AbnormalFlownWarnEntity
import cn.ffcs.is.mss.analyzer.druid.model.scala.OperationModel
import cn.ffcs.is.mss.analyzer.flink.sink.MySQLSink
import cn.ffcs.is.mss.analyzer.utils._
import cn.ffcs.is.mss.analyzer.utils.druid.entity._
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.functions.{AssignerWithPunctuatedWatermarks, ProcessFunction}
import org.apache.flink.streaming.api.scala.{StreamExecutionEnvironment, _}
import org.apache.flink.streaming.api.watermark.Watermark
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaConsumer, FlinkKafkaProducer}
import org.apache.flink.streaming.util.serialization.SimpleStringSchema
import org.apache.flink.util.Collector

import java.sql.Timestamp
import java.util.{Date, Properties}
import scala.collection.JavaConversions._
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

/**
 * @author liangzhaosuo
 * @date 2020/06/28 14:17
 * @description 判断一个用户的上行流量或下行流量是否异常
 * @update [no][date YYYY-MM-DD][name][description]
 */
object AbnormalFlowWarn {
  def main(args: Array[String]): Unit = {
    //根据传入的参数解析配置文件
    //    val args0 = "E:\\ffcs\\mss\\src\\main\\resources\\flink.ini"
    //    val confProperties = new IniProperties(args0)
    val confProperties = new IniProperties(args(0))

    //该任务的名字
    val jobName = confProperties.getValue(Constants.FLINK_ABNORMAL_FLOW_CONFIG, Constants
      .FLINK_ABNORMAL_FLOW_JOB_NAME)
    //kafka Source的名字
    val kafkaSourceName = confProperties.getValue(Constants.FLINK_ABNORMAL_FLOW_CONFIG, Constants
      .FLINK_ABNORMAL_FLOW_KAFKA_SOURCE_NAME)
    //mysql sink的名字
    val sqlSinkName = confProperties.getValue(Constants.FLINK_ABNORMAL_FLOW_CONFIG, Constants
      .FLINK_ABNORMAL_FLOW_SQL_SINK_NAME)
    //kafka sink的名字
    val kafkaSinkName = confProperties.getValue(Constants.FLINK_ABNORMAL_FLOW_CONFIG, Constants
      .FLINK_ABNORMAL_FLOW_KAFKA_SINK_NAME)


    //kafka Source的并行度
    val kafkaSourceParallelism = confProperties.getIntValue(Constants.FLINK_ABNORMAL_FLOW_CONFIG,
      Constants.FLINK_ABNORMAL_FLOW_KAFKA_SOURCE_PARALLELISM)
    //对数据处理的并行度
    val dealParallelism = confProperties.getIntValue(Constants.FLINK_ABNORMAL_FLOW_CONFIG,
      Constants.FLINK_ABNORMAL_FLOW_DEAL_PARALLELISM)
    //写入mysql的并行度
    val sqlSinkParallelism = confProperties.getIntValue(Constants.FLINK_ABNORMAL_FLOW_CONFIG,
      Constants.FLINK_ABNORMAL_FLOW_SQL_SINK_PARALLELISM)
    //写入kafka的并行度
    val kafkaSinkParallelism = confProperties.getIntValue(Constants.FLINK_ABNORMAL_FLOW_CONFIG,
      Constants.FLINK_ABNORMAL_FLOW_KAFKA_SINK_PARALLELISM)
    //check pointing的间隔
    val checkpointInterval = confProperties.getLongValue(Constants.FLINK_ABNORMAL_FLOW_CONFIG,
      Constants.FLINK_ABNORMAL_FLOW_CHECKPOINT_INTERVAL)

    //kafka的服务地址
    val brokerList = confProperties.getValue(Constants.FLINK_COMMON_CONFIG, Constants
      .KAFKA_BOOTSTRAP_SERVERS)
    //flink消费的group.id
    val groupId = confProperties.getValue(Constants.FLINK_ABNORMAL_FLOW_CONFIG, Constants
      .FLINK_ABNORMAL_FLOW_GROUP_ID)
    //kafka source 的topic
    val kafkaSourceTopic = confProperties.getValue(Constants.OPERATION_FLINK_TO_DRUID_CONFIG,
      Constants.OPERATION_TO_KAFKA_TOPIC)
    //kafka sink 的topic
    val kafkaSinkTopic = confProperties.getValue(Constants.FLINK_ABNORMAL_FLOW_CONFIG, Constants
      .FLINK_ABNORMAL_FLOW_KAFKA_SINK_TOPIC)

    val warningSinkTopic = confProperties.getValue(Constants.WARNING_FLINK_TO_DRUID_CONFIG, Constants
      .WARNING_TOPIC)


    val parameters: Configuration = new Configuration()


    //c3p0连接池配置文件路径
    parameters.setString(Constants.c3p0_CONFIG_PATH, confProperties.getValue(Constants.FLINK_COMMON_CONFIG, Constants
      .c3p0_CONFIG_PATH))
    parameters.setInteger(Constants.FLINK_ABNORMAL_FLOW_STATISTICS_LENGTH, confProperties.getIntValue(Constants
      .FLINK_ABNORMAL_FLOW_CONFIG, Constants.FLINK_ABNORMAL_FLOW_STATISTICS_LENGTH))
    parameters.setDouble(Constants.FLINK_ABNORMAL_FLOW_THRESHOLD_RATIO, confProperties.getFloatValue(Constants
      .FLINK_ABNORMAL_FLOW_CONFIG, Constants.FLINK_ABNORMAL_FLOW_THRESHOLD_RATIO))
    parameters.setLong(Constants.FLINK_ABNORMAL_FLOW_BASE_COUNT, confProperties.getLongValue(Constants
      .FLINK_ABNORMAL_FLOW_CONFIG, Constants.FLINK_ABNORMAL_FLOW_BASE_COUNT))

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

    //获取ExecutionEnvironment
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    //设置check pointing的间隔
    env.enableCheckpointing(checkpointInterval)
    //设置flink时间
    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)
    //设置flink全局变量
    env.getConfig.setGlobalJobParameters(parameters)

    //设置kafka消费者相关配置
    val props = new Properties
    props.setProperty("bootstrap.servers", brokerList)
    props.setProperty("group.id", groupId)

    //获取kafka消费者
    val consumer = new FlinkKafkaConsumer[String](kafkaSourceTopic, new SimpleStringSchema, props)
      .setStartFromGroupOffsets()
    //获取kafka生产者
    val producer = new FlinkKafkaProducer[String](kafkaSinkTopic, new SimpleStringSchema, props)

    //将告警数据写入告警库topic
    val warningProducer = new FlinkKafkaProducer[String](brokerList, warningSinkTopic, new
        SimpleStringSchema())

    val alertData = env.addSource(consumer).setParallelism(kafkaSourceParallelism)
      .uid(kafkaSourceName).name(kafkaSourceName)
      .map(JsonUtil.fromJson[OperationModel] _).setParallelism(dealParallelism)
      .filter(_.userName != "匿名用户").setParallelism(dealParallelism)
      .assignTimestampsAndWatermarks(new AssignerWithPunctuatedWatermarks[OperationModel] {
        override def
        checkAndGetNextWatermark(lastElement: OperationModel, extractedTimestamp: Long): Watermark = {
          new Watermark(extractedTimestamp - 10000)
        }

        override def extractTimestamp(element: OperationModel, previousElementTimestamp: Long): Long = {
          element.timeStamp
        }
      }).setParallelism(dealParallelism)
      .map(model => (model.timeStamp / 1000 / 60 * 1000 * 60 + "|" + model.userName, model.inputOctets, model
        .outputOctets, model.sourceIp, model.destinationIp))
      .setParallelism(dealParallelism)
      .keyBy(0)
      .timeWindow(Time.minutes(1), Time.minutes(1))
      .reduce((o1, o2) => (o2._1, o1._2 + o2._2, o1._3 + o2._3, o2._4, o2._5))
      .process(new FlowDetectProcess).setParallelism(dealParallelism)

    val value: DataStream[(Object, Boolean)] = alertData.map(_._1)
    val alertKafkaValue = alertData.map(_._2)
    value.addSink(new MySQLSink)
      .uid(sqlSinkName)
      .name(sqlSinkName)
      .setParallelism(sqlSinkParallelism)

    alertData.map(m => JsonUtil.toJson(m._1._1.asInstanceOf[AbnormalFlownWarnEntity])).setParallelism(dealParallelism)
      .addSink(producer)
      .uid(kafkaSinkName)
      .name(kafkaSinkName)
      .setParallelism(kafkaSinkParallelism)

  alertKafkaValue.addSink(warningProducer).setParallelism(kafkaSinkParallelism)
  //alertKafkaValue.print()

    env.execute(jobName)


  }

  /**
   * @title FlowDetectProcess
   * @author liangzhaosuo
   * @date 2020-06-16 10:32
   * @description 对用户的流量进行检测,如果超过了历史的门限流量,则进行告警
   * @update [no][date YYYY-MM-DD][name][description]
   */
  class FlowDetectProcess extends ProcessFunction[(String, Long, Long, String, String), ((Object, Boolean), String)] {

    //[用户名,(上行流量阈值,下行流量阈值)]
    var flowThresholdMap = new mutable.HashMap[String, (Double, Double)]
    val historyData = new mutable.HashMap[String, mutable.HashMap[Aggregation, ArrayBuffer[Double]]]()
    var tableName: String = _
    var historyLen: Int = _
    val aggregationSet = new mutable.HashSet[Aggregation]
    var thresholdRatio: Double = _
    var baseCount: Long = _
    val inputKafkaValue: String = ""

    override def open(parameters: Configuration): Unit = {
      val globConf = getRuntimeContext.getExecutionConfig.getGlobalJobParameters.asInstanceOf[Configuration]

      historyLen = globConf.getInteger(Constants.FLINK_ABNORMAL_FLOW_STATISTICS_LENGTH, 0)
      thresholdRatio = globConf.getDouble(Constants.FLINK_ABNORMAL_FLOW_THRESHOLD_RATIO, 0.0)
      baseCount = globConf.getLong(Constants.FLINK_ABNORMAL_FLOW_BASE_COUNT, 0L)

      aggregationSet.add(Aggregation.inputOctets)
      aggregationSet.add(Aggregation.outputOctets)

      tableName = globConf.getString(Constants.DRUID_OPERATION_TABLE_NAME, "")
      //设置druid的broker的host和port
      DruidUtil.setDruidHostPortSet(globConf.getString(Constants.DRUID_BROKER_HOST_PORT, ""))
      //设置写入druid的时间格式
      DruidUtil.setTimeFormat(globConf.getString(Constants.DRUID_TIME_FORMAT, ""))
      //设置druid开始的时间
      DruidUtil.setDateStartTimeStamp(globConf.getLong(Constants.DRUID_DATA_START_TIMESTAMP, 0L))

      val date = new Date

      val endTimestamp = TimeUtil.getDayStartTime(date.getTime)
      val startTimestamp = endTimestamp - historyLen * TimeUtil.DAY_MILLISECOND
      for (i <- startTimestamp until endTimestamp by 6 * TimeUtil.HOUR_MILLISECOND) {
        val queryEntity = getAllUserQueryEntity(i, i + 6 * TimeUtil.HOUR_MILLISECOND, tableName, aggregationSet)
        queryDruidFillUserValue("userName", queryEntity, aggregationSet, historyData)
      }


      for ((k, v) <- historyData) {
        val userName = k
        val inputAb = v(Aggregation.inputOctets)
        val outputAb = v(Aggregation.outputOctets)
        val inputThreshold = getZScore(inputAb, thresholdRatio, baseCount)
        val outputThreshold = getZScore(outputAb, thresholdRatio, baseCount)
        flowThresholdMap.put(userName, (inputThreshold, outputThreshold))
      }

    }


    override def processElement(value: (String, Long, Long, String, String), ctx: ProcessFunction[(String, Long,
      Long, String, String), ((Object, Boolean), String)]#Context, out: Collector[((Object, Boolean), String)]): Unit = {

      val splits = value._1.split("\\|")
      val timestamp = splits(0).toLong
      val endTimestamp = TimeUtil.getDayStartTime(timestamp)
      val startTimestamp = endTimestamp - historyLen * TimeUtil.DAY_MILLISECOND
      val userName = splits(1)
      val inputOctets = value._2
      val outputOctets = value._3
      val sourceIp = value._4
      val destinationIp = value._5
      val inPutKafkaValue = userName + "|" + "上下行流量异常" + "|" + timestamp + "|" +
        "" + "|" + "" + "|" + "" + "|" +
        "" + "|" + sourceIp + "|" + "" + "|" +
        destinationIp + "|" + "" + "|" + "" + "|" +
        "" + "|" + "" + "|" + ""
      if (flowThresholdMap.contains(userName)) {
        val thresholdTup = flowThresholdMap(userName)
        val entity = generateWarnEntity(thresholdTup, inputOctets, outputOctets, userName, sourceIp, destinationIp,
          timestamp)
        if (entity != null) {
          out.collect(((entity.asInstanceOf[Object], true), inPutKafkaValue))
        }
      } else {
        val queryEntity = getQueryDruidEntity(startTimestamp, endTimestamp, tableName, aggregationSet, userName)
        val resultList = DruidUtil.query(queryEntity)
        val inputOctetsAb = new ArrayBuffer[Double]()
        val outputOctetsAb = new ArrayBuffer[Double]()
        for (i <- resultList) {
          inputOctetsAb.add(i.getOrDefault(Aggregation.inputOctets.toString, "0").toDouble)
          outputOctetsAb.add(i.getOrDefault(Aggregation.outputOctets.toString, "0").toDouble)
        }
        //上行流量阈值
        val inputThreshold = getZScore(inputOctetsAb, thresholdRatio, baseCount)
        //下行流量阈值
        val outputThreshold = getZScore(outputOctetsAb, thresholdRatio, baseCount)
        flowThresholdMap.put(userName, (inputThreshold, outputThreshold))
        val entity = generateWarnEntity((inputThreshold, outputThreshold), inputOctets, outputOctets, userName,
          sourceIp, destinationIp, timestamp)
        if (entity != null) {
          out.collect(((entity.asInstanceOf[Object], true), inPutKafkaValue))
        }
      }
    }

    /**
     *
     * @param startTimeStamp
     * @param endTimeStamp
     * @param tableName
     * @param aggregationSet
     * @param userName
     * @return druid查询对象
     * @author liangzhaosuo
     * @date 2020/06/24 14:25
     * @description 根据开始时间,结束时间,表名进行,用户名等信息,构建查询对象
     * @update [no][date YYYY-MM-DD][name][description]
     */
    def getQueryDruidEntity(startTimeStamp: Long, endTimeStamp: Long, tableName: String, aggregationSet: collection
    .Set[Aggregation], userName: String): Entity = {
      val entity = new Entity
      entity.setTableName(tableName)
      entity.setDimensionsSet(Dimension.getDimensionsSet(Dimension.userName))
      entity.setAggregationsSet(aggregationSet.map(tuple => Aggregation.getAggregation(tuple)))
      entity.setGranularity(Granularity.getGranularity(Granularity.periodMinute, 1))
      entity.setFilter(Filter.getFilter(Filter.selector, Dimension.userName, userName))
      entity.setStartTimeStr(startTimeStamp)
      entity.setEndTimeStr(endTimeStamp)
      entity
    }

    def getAllUserQueryEntity(startTimeStamp: Long, endTimeStamp: Long, tableName: String, aggregationSet: collection
    .Set[Aggregation]): Entity = {
      val entity = new Entity()
      entity.setTableName(tableName)
      entity.setDimensionsSet(Dimension.getDimensionsSet(Dimension.userName))
      entity.setAggregationsSet(aggregationSet.map(tuple => Aggregation.getAggregation(tuple)))
      entity.setGranularity(Granularity.getGranularity(Granularity.periodMinute, 1))
      entity.setStartTimeStr(startTimeStamp)
      entity.setEndTimeStr(endTimeStamp)
      entity
    }

    def queryDruidFillUserValue(dimensionName: String, entity: Entity, aggregationSet: collection.Set[Aggregation],
                                map: mutable.Map[String, mutable.HashMap[Aggregation, ArrayBuffer[Double]]]): Unit = {
      val resultList = DruidUtil.query(entity)
      for (i <- resultList) {
        val field = i(dimensionName)
        aggregationSet.foreach(aggregation => {
          map.getOrElseUpdate(field, new mutable.HashMap[Aggregation, ArrayBuffer[Double]])
            .getOrElseUpdate(aggregation, new ArrayBuffer[Double]).append(i.getOrDefault(aggregation.toString, "0")
            .toDouble)
        })
      }
    }


    /**
     *
     * @param octetsAb  上行或下行流量
     * @param threshold 设定的阈值比
     * @return 异常界限值
     * @author liangzhaosuo
     * @date 2020/06/28 16:07
     * @description 根据如数的上行或下行流量以及阈值比,进行异常值的计算
     * @update [no][date YYYY-MM-DD][name][description]
     */
    def getZScore(octetsAb: ArrayBuffer[Double], threshold: Double, baseCount: Long): Double = {
      val resultList = new ArrayBuffer[Double]()
      val mean = octetsAb.sum / octetsAb.length
      var sum = 0.0
      for (i <- octetsAb) {
        sum += Math.pow(i - mean, 2)
      }
      val std = Math.sqrt(sum / (octetsAb.length - 1))
      for (i <- octetsAb) {
        val zScore = (i - mean) / std
        if (Math.abs(zScore) > threshold && i > baseCount) {
          resultList.add(i)
        }
      }
      if (resultList.nonEmpty) {
        resultList.min
      } else {
        Double.MaxValue
      }

    }

    /**
     *
     * @param threshold     上行流量和下行流量阈值
     * @param inputOctets   上行流量
     * @param outputOctets  下行流量
     * @param userName      用户名
     * @param sourceIp      源IP
     * @param destinationIp 目的IP
     * @param timestamp     毫秒值时间戳
     * @return 告警对象
     * @author liangzhaosuo
     * @date 2020/06/29 9:26
     * @description 根据本次流量和流量阈值进行对比,判断是否生成告警
     * @update [no][date YYYY-MM-DD][name][description]
     */
    def generateWarnEntity(threshold: (Double, Double), inputOctets: Long, outputOctets: Long, userName: String,
                           sourceIp: String, destinationIp: String, timestamp: Long): AbnormalFlownWarnEntity = {
      var warnType = 0
      var octets = 0L
      if (inputOctets > threshold._1 && outputOctets > threshold._2) {
        warnType = 3
        octets = inputOctets + outputOctets
      } else if (inputOctets > threshold._1) {
        warnType = 1
        octets = inputOctets
      } else if (outputOctets > threshold._2) {
        warnType = 2
        octets = outputOctets
      }
      if (warnType > 0) {
        val warnEntity = new AbnormalFlownWarnEntity
        warnEntity.setUserName(userName)
        warnEntity.setSourceIp(sourceIp)
        warnEntity.setDestinationIp(destinationIp)
        warnEntity.setOctets(octets)
        warnEntity.setWarnType(warnType)
        warnEntity.setWarnTime(new Timestamp(timestamp))
        warnEntity
      } else {
        null
      }
    }
  }

}
