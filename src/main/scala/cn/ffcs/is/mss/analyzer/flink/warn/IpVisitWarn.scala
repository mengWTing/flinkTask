package cn.ffcs.is.mss.analyzer.flink.warn

import java.io._
import java.net.URI
import java.sql.Timestamp
import java.time.Duration
import java.util.Properties

import cn.ffcs.is.mss.analyzer.bean.{AlarmRulesEntity, IpVisitWarnMergeEntity, IpasIpVisitWarnEntity}
import cn.ffcs.is.mss.analyzer.druid.model.scala.QuintetModel
import cn.ffcs.is.mss.analyzer.flink.sink.{MySQLSink, Sink}
import cn.ffcs.is.mss.analyzer.flink.source.Source
import cn.ffcs.is.mss.analyzer.ml.iforest.IForest
import cn.ffcs.is.mss.analyzer.utils
import cn.ffcs.is.mss.analyzer.utils._
import cn.ffcs.is.mss.analyzer.utils.druid.entity._
import javax.persistence.Table
import org.apache.flink.api.common.functions.{RichFlatMapFunction, RichMapFunction}
import org.apache.flink.configuration.{ConfigOptions, Configuration}
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.functions.{AssignerWithPunctuatedWatermarks, ProcessFunction}
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.watermark.Watermark
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaConsumer, FlinkKafkaProducer}
import org.apache.flink.streaming.util.serialization.SimpleStringSchema
import org.apache.flink.util.Collector
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.flink.api.common.accumulators.LongCounter
import org.apache.flink.api.common.eventtime.{SerializableTimestampAssigner, WatermarkStrategy}
import org.apache.flink.streaming.api.windowing.assigners.SlidingEventTimeWindows
import org.json.{JSONArray, JSONObject}

import scala.collection.JavaConversions._
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

object IpVisitWarn {

  def main(args: Array[String]): Unit = {


    //根据传入的参数解析配置文件
    //val args0 = "/Users/chenwei/IdeaProjects/mss/src/main/resources/flink.ini"
    //val confProperties = new IniProperties(args0)
    val confProperties = new IniProperties(args(0))

    //该任务的名字
    val jobName = confProperties.getValue(Constants.FLINK_IP_VISIT_CONFIG, Constants
      .IP_VISIT_JOB_NAME)
    //kafka Source的名字
    val kafkaSourceName = confProperties.getValue(Constants.FLINK_IP_VISIT_CONFIG, Constants
      .IP_VISIT_KAFKA_SOURCE_NAME)
    //mysql sink的名字
    val sqlSinkName = confProperties.getValue(Constants.FLINK_IP_VISIT_CONFIG, Constants
      .IP_VISIT_SQL_SINK_NAME)
    //kafka sink 的名字
    val kafkaSinkName = confProperties.getValue(Constants.FLINK_IP_VISIT_CONFIG,
      Constants.IP_VISIT_ALERT_KAFKA_SINK_NAME)

    //kafka Source的并行度
    val kafkaSourceParallelism = confProperties.getIntValue(Constants.FLINK_IP_VISIT_CONFIG,
      Constants.IP_VISIT_KAFKA_SOURCE_PARALLELISM)
    //kafka sink 并行度
    val kafkaSinkParallelism = confProperties.getIntValue(Constants.FLINK_IP_VISIT_CONFIG,
      Constants.IP_VISIT_SQL_SINK_PARALLELISM)
    //对数据处理的并行度
    val dealParallelism = confProperties.getIntValue(Constants.FLINK_IP_VISIT_CONFIG, Constants
      .IP_VISIT_DEAL_PARALLELISM)
    //查询druid判断是否异常的并行度
    val queryParallelism = confProperties.getIntValue(Constants.FLINK_IP_VISIT_CONFIG, Constants
      .IP_VISIT_QUERY_PARALLELISM)
    //写入mysql的并行度
    val sqlSinkParallelism = confProperties.getIntValue(Constants.FLINK_IP_VISIT_CONFIG,
      Constants.IP_VISIT_SQL_SINK_PARALLELISM)

    //kafka的服务地址
    val brokerList = confProperties.getValue(Constants.FLINK_COMMON_CONFIG, Constants
      .KAFKA_BOOTSTRAP_SERVERS)
    //flink消费的group.id
    val groupId = confProperties.getValue(Constants.FLINK_IP_VISIT_CONFIG, Constants
      .IP_VISIT_GROUP_ID)
    //kafka的topic
    val topic = confProperties.getValue(Constants.QUINTET_FLINK_TO_DRUID_CONFIG, Constants
      .QUINTET_TO_KAFKA_TOPIC)
    //kafka sink topic
    val kafkaSinkTopic = confProperties.getValue(Constants.FLINK_IP_VISIT_CONFIG,
      Constants.IP_VISIT_ALERT_KAFKA_SINK_TOPIC)


    //flink全局变量
    val parameters: Configuration = new Configuration()
    //配置文件系统类型
    parameters.setString(Constants.FILE_SYSTEM_TYPE, confProperties.getValue(Constants
      .FLINK_COMMON_CONFIG, Constants.FILE_SYSTEM_TYPE))
    //日期配置文件的路径
    parameters.setString(Constants.DATE_CONFIG_PATH, confProperties.getValue(Constants
      .FLINK_COMMON_CONFIG, Constants.DATE_CONFIG_PATH))
    //c3p0连接池配置文件路径
    parameters.setString(Constants.c3p0_CONFIG_PATH, confProperties.getValue(Constants
      .FLINK_COMMON_CONFIG, Constants.c3p0_CONFIG_PATH))

    //druid的broker节点
    parameters.setString(Constants.DRUID_BROKER_HOST_PORT, confProperties.getValue(Constants
      .FLINK_COMMON_CONFIG, Constants.DRUID_BROKER_HOST_PORT))
    //druid时间格式
    parameters.setString(Constants.DRUID_TIME_FORMAT, confProperties.getValue(Constants
      .FLINK_COMMON_CONFIG, Constants.DRUID_TIME_FORMAT))
    //druid数据开始的时间
    parameters.setLong(Constants.DRUID_DATA_START_TIMESTAMP, confProperties.getLongValue
    (Constants.FLINK_COMMON_CONFIG, Constants.DRUID_DATA_START_TIMESTAMP))

    //判断源ip个数是否异常时环比参考的范围
    parameters.setLong(Constants.IP_VISIT_SOURCE_IP_COUNT_INTERVAL_TIME, confProperties
      .getLongValue(Constants.FLINK_IP_VISIT_CONFIG, Constants
        .IP_VISIT_SOURCE_IP_COUNT_INTERVAL_TIME))
    //判断源ip个数是否异常时生成树的个数
    parameters.setInteger(Constants.IP_VISIT_SOURCE_IP_COUNT_NUMBER_OF_SUBTREE, confProperties
      .getIntValue(Constants.FLINK_IP_VISIT_CONFIG, Constants
        .IP_VISIT_SOURCE_IP_COUNT_NUMBER_OF_SUBTREE))
    //判断源ip个数是否异常时聚类的迭代次数
    parameters.setInteger(Constants.IP_VISIT_SOURCE_IP_COUNT_ITERS, confProperties.getIntValue
    (Constants.FLINK_IP_VISIT_CONFIG, Constants.IP_VISIT_SOURCE_IP_COUNT_ITERS))
    //判断源ip个数是否异常时抽取的样本个数
    parameters.setInteger(Constants.IP_VISIT_SOURCE_IP_COUNT_SAMPLE_SIZE, confProperties
      .getIntValue(Constants.FLINK_IP_VISIT_CONFIG, Constants.IP_VISIT_SOURCE_IP_COUNT_SAMPLE_SIZE))

    //判断源ip访问是否异常时环比参考的范围
    parameters.setLong(Constants.IP_VISIT_SOURCE_IP_INTERVAL_TIME, confProperties.getLongValue
    (Constants.FLINK_IP_VISIT_CONFIG, Constants.IP_VISIT_SOURCE_IP_INTERVAL_TIME))
    //判断源ip访问是否异常时生成树的个数
    parameters.setInteger(Constants.IP_VISIT_SOURCE_IP_NUMBER_OF_SUBTREE, confProperties
      .getIntValue(Constants.FLINK_IP_VISIT_CONFIG, Constants.IP_VISIT_SOURCE_IP_NUMBER_OF_SUBTREE))
    //判断源ip访问是否异常时聚类的迭代次数
    parameters.setInteger(Constants.IP_VISIT_SOURCE_IP_ITERS, confProperties.getIntValue
    (Constants.FLINK_IP_VISIT_CONFIG, Constants.IP_VISIT_SOURCE_IP_ITERS))
    //判断源ip访问是否异常时抽取的样本个数
    parameters.setInteger(Constants.IP_VISIT_SOURCE_IP_SAMPLE_SIZE, confProperties.getIntValue
    (Constants.FLINK_IP_VISIT_CONFIG, Constants.IP_VISIT_SOURCE_IP_SAMPLE_SIZE))
    //五元组话单在druid的表名
    parameters.setString(Constants.DRUID_QUINTET_TABLE_NAME, confProperties.getValue(Constants
      .FLINK_COMMON_CONFIG, Constants.DRUID_QUINTET_TABLE_NAME))

    //check pointing的间隔
    val checkpointInterval = confProperties.getLongValue(Constants.FLINK_IP_VISIT_CONFIG,
      Constants.IP_VISIT_CHECKPOINT_INTERVAL)
    val warningSinkTopic = confProperties.getValue(Constants.WARNING_FLINK_TO_DRUID_CONFIG, Constants
      .WARNING_TOPIC)

    //获取kafka消费者
    val consumer = Source.kafkaSource(topic, groupId, brokerList)
    //获取kafka 生产者
    val producer = Sink.kafkaSink(brokerList, kafkaSinkTopic)

    //获取ExecutionEnvironment
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    //设置check pointing的间隔
//    env.enableCheckpointing(checkpointInterval)
    //设置flink全局变量
    env.getConfig.setGlobalJobParameters(parameters)


    //获取kafka数据
    val dStream = env.fromSource(consumer, WatermarkStrategy.noWatermarks(), kafkaSourceName).setParallelism(kafkaSourceParallelism)
    .uid(kafkaSourceName).name(kafkaSourceName)

    //val path = "/Users/chenwei/Downloads/mss.1528437418083.txt"
    //val dStream = env.readTextFile(path, "iso-8859-1")

    val tcpHighRiskPortSet = getTcpHighRiskPort(confProperties.getValue(Constants
      .FLINK_IP_VISIT_CONFIG, Constants.IP_VISIT_TCP_HIGH_RISK_PATH))
    val intranetSet = getIntranetSet(confProperties.getValue(Constants
      .OPERATION_FLINK_TO_DRUID_CONFIG, Constants.OPERATION_PLACE_PATH))
    val httpPortSet = getHttpPortSet()

    val sourceOperationModelStream = dStream
      //.map(QuintetModel.getQuintetModel _)
      //.filter(_.isDefined)
      //.map(_.head)
      //.map(JsonUtil.toJson(_))
      .map(JsonUtil.fromJson[QuintetModel] _).setParallelism(dealParallelism)
      //只包含链接成功的
      //非http协议的
      //端口号在10000以下的
      //过滤公网的攻击
      //传输层是tcp协议的
      .filter(tuple => {
        "0".equals(tuple.isSucceed) &&
          tuple.inputOctets > 0 &&
          tuple.outputOctets > 0 &&
          !"1".equals(tuple.protocolId) &&
          10000 >= tuple.destinationPort.toInt &&
          "^42.99.32.*$".r.findFirstIn(tuple.destinationIp).isEmpty &&
          "^42.99.33.*$".r.findFirstIn(tuple.destinationIp).isEmpty &&
          "^42.99.34.*$".r.findFirstIn(tuple.destinationIp).isEmpty &&
          80 != tuple.destinationPort.toInt &&
          8835 != tuple.destinationPort.toInt &&
          "tcp".equals(tuple.protocol)
      }).setParallelism(dealParallelism)
      .filter(quintetModel => isIntranet(quintetModel.sourceIp, intranetSet)).setParallelism(dealParallelism)
      .map(tuple => (tuple.timeStamp.toLong / TimeUtil.MINUTE_MILLISECOND * TimeUtil
        .MINUTE_MILLISECOND,
        tuple.destinationIp, tuple.destinationPort, Set[String](tuple.sourceIp))).setParallelism(dealParallelism)
      .assignTimestampsAndWatermarks(
        WatermarkStrategy.forBoundedOutOfOrderness[(Long, String, String, Set[String])](Duration.ofSeconds(10))
          .withTimestampAssigner(new SerializableTimestampAssigner[(Long, String, String, Set[String])] {
            override def extractTimestamp(element: (Long, String, String, Set[String]), recordTimestamp: Long): Long = {
              element._1
            }
          })
      ).setParallelism(dealParallelism)
      .keyBy(x=>(x._2,x._3))
      .window(SlidingEventTimeWindows.of(Time.minutes(1), Time.minutes(1)))
      .reduce((value1, value2) => (value1._1, value1._2, value1._3, value1._4 ++ value2._4))
      .setParallelism(queryParallelism)
      .flatMap(new QueryWarnSourceIpCountRichFlatMapFunction).setParallelism(queryParallelism)
      .flatMap(new QueryWarnSourceRichFlatMapFunction).setParallelism(queryParallelism)
      .filter(value => !isHttpPort(value._3, httpPortSet))
      .map(new ProduceMap(tcpHighRiskPortSet))

    //      .print()
    //      .writeAsText("/Users/chenwei/Downloads/warn2/mss/warn.txt").setParallelism(1)

    //    operationModelStream.writeAsText("hdfs://A5-302-HW-XH628-027:8020/chenw/tempResult")
    //    .setParallelism(1)


    sourceOperationModelStream.map(_._1).addSink(new MySQLSink).setParallelism(sqlSinkParallelism)
      .uid(sqlSinkName).name(sqlSinkName)

    sourceOperationModelStream
      .map(_._1._1.asInstanceOf[IpasIpVisitWarnEntity]).setParallelism(1)
      .assignTimestampsAndWatermarks(
        WatermarkStrategy.forBoundedOutOfOrderness[IpasIpVisitWarnEntity](Duration.ofSeconds(10))
          .withTimestampAssigner(new SerializableTimestampAssigner[IpasIpVisitWarnEntity] {
            override def extractTimestamp(element: IpasIpVisitWarnEntity, recordTimestamp: Long): Long = {
              element.getWarnDate.getTime
            }
          })
      ).setParallelism(1)
      .windowAll(SlidingEventTimeWindows.of(Time.minutes(1), Time.minutes(1)))
      .reduce((o1, o2) => {
        val timeStamp = if (o1.getWarnDate.getTime > o2.getWarnDate.getTime) o1.getWarnDate else
          o2.getWarnDate

        o1.setWarnDate(timeStamp)
        o1
      })
      .flatMap(new QueryAlertRule).setParallelism(1)
      .map(new MergeIpVisitWarn).setParallelism(dealParallelism)
      .filter(_.isDefined).setParallelism(dealParallelism)
      .map(_.head).setParallelism(dealParallelism)

    sourceOperationModelStream.map(o => {
      JsonUtil.toJson(o._1._1.asInstanceOf[IpasIpVisitWarnEntity])
    })

      .sinkTo(producer)
      .uid(kafkaSinkName)
      .name(kafkaSinkName)
      .setParallelism(kafkaSinkParallelism)
    //将告警数据写入告警库topic
    val warningProducer = Sink.kafkaSink(brokerList, warningSinkTopic)
    sourceOperationModelStream.map(_._2).sinkTo(warningProducer).setParallelism(kafkaSinkParallelism)
    env.execute(jobName)

  }


  /**
   * 判断访问目的IP目的PORT的源IP个数是否正常
   */
  class QueryWarnSourceIpCountRichFlatMapFunction extends RichFlatMapFunction[(Long, String, String, Set[String]), (Long, String, String)] {

    //判断源ip个数是否异常时环比参考的范围
    var sourceIpCountIntervalTime: Long = 0
    //判断源ip个数是否异常时生成树的个数
    var sourceIpCountNumberOfSubtree: Int = 0
    //判断源ip个数是否异常时聚类的迭代次数
    var sourceIpCountIters: Int = 0
    //判断源ip个数是否异常时抽取的样本个数
    var sourceIpCountSampleSize: Int = 0

    private val timeIndexMap = mutable.Map[Long, mutable.Map[Long, Int]]()
    private val timeStrMap = mutable.Map[(Long, Long), (Array[String], Array[String])]()

    private val messagesReceived = new LongCounter()
    private val messagesSend = new LongCounter()

    var tableName: String = null

    override def open(parameters: Configuration): Unit = {

      val globConf = getRuntimeContext.getExecutionConfig.getGlobalJobParameters.asInstanceOf[Configuration]

      DateUtil.ini(globConf.getString(ConfigOptions.key(Constants.FILE_SYSTEM_TYPE).stringType().defaultValue("")),
        globConf.getString(ConfigOptions.key(Constants.DATE_CONFIG_PATH).stringType().defaultValue("")))
      DruidUtil.setDruidHostPortSet(globConf.getString(ConfigOptions.key(Constants.DRUID_BROKER_HOST_PORT).stringType().defaultValue("")))
      DruidUtil.setTimeFormat(globConf.getString(ConfigOptions.key(Constants.DRUID_TIME_FORMAT).stringType().defaultValue("")))
      DruidUtil.setDateStartTimeStamp(globConf.getLong(ConfigOptions.key(Constants.DRUID_DATA_START_TIMESTAMP).longType().defaultValue(0L)))

      sourceIpCountIntervalTime = globConf.getLong(ConfigOptions.key(Constants.IP_VISIT_SOURCE_IP_COUNT_INTERVAL_TIME).longType().defaultValue(300000L))
      sourceIpCountNumberOfSubtree = globConf.getInteger(ConfigOptions.key(Constants.IP_VISIT_SOURCE_IP_COUNT_NUMBER_OF_SUBTREE).intType().defaultValue(500))
      sourceIpCountIters = globConf.getInteger(ConfigOptions.key(Constants.IP_VISIT_SOURCE_IP_COUNT_ITERS).intType().defaultValue(100))
      sourceIpCountSampleSize = globConf.getInteger(ConfigOptions.key(Constants.IP_VISIT_SOURCE_IP_COUNT_SAMPLE_SIZE).intType().defaultValue(128))


      getRuntimeContext.addAccumulator("SourceIpCountRichFlat:Messages received", messagesReceived)
      getRuntimeContext.addAccumulator("SourceIpCountRichFlat:Messages send", messagesSend)

      tableName = globConf.getString(ConfigOptions.key(Constants.DRUID_QUINTET_TABLE_NAME).stringType().defaultValue(""))
    }

    override def flatMap(value: (Long, String, String, Set[String]), out: Collector[(Long, String, String)]): Unit = {

      val int = (math.random * 5).toInt

      if (int == 0) {
        messagesReceived.add(1)


        val map = timeIndexMap.getOrElse(value._1, getTimeIndex(value._1, sourceIpCountIntervalTime, value._1 - TimeUtil.DAY_MILLISECOND * 10))


        if (!timeIndexMap.contains(value._1)) {
          timeIndexMap(value._1) = map
        }


        //查询目的ip指定目的端口在指定时间范围内的源ip个数
        val sourceIpCountQueryResult = DruidUtil.query(getSourceIpCountQueryEntity(value._1, value._2, value._3))
        if (sourceIpCountQueryResult != null) {

          //对每个目的ip解析查询结果，使用iforest对其进行判断，看是否出告警

          val destinationPortSourceIpCountQueryResults = mutable.Map[String, ArrayBuffer[Array[Double]]]()


          for (i <- sourceIpCountQueryResult) {
            //按照目的port当做key分组
            val key = i.get(Dimension.destinationPort.toString)
            val samples = destinationPortSourceIpCountQueryResults.getOrElse(key, {
              val temp = ArrayBuffer[Array[Double]]()
              for (i <- 0 to map.values.max) {
                temp += Array[Double](0.0)
              }
              temp
            })


            val sourceIpCount = i.get(Aggregation.sourceIpCount.toString).toDouble
            if (samples(map(i.get(Entity.timestamp).toLong))(0) < sourceIpCount) {
              samples(map(i.get(Entity.timestamp).toLong)) = Array[Double](sourceIpCount)
            }
            destinationPortSourceIpCountQueryResults.put(key, samples)

          }

          //记录异常的目的port
          for (destinationPortSourceIpCountQueryResult <- destinationPortSourceIpCountQueryResults) {

            val destinationPort = destinationPortSourceIpCountQueryResult._1

            val samples = destinationPortSourceIpCountQueryResult._2

            val iForest = new IForest()


            val predictResult = iForest.predict(samples.toArray, sourceIpCountNumberOfSubtree,
              sourceIpCountIters, sourceIpCountSampleSize, Array[Double](value._4.size), true,
              false)

            if (!predictResult) {
              out.collect((value._1, value._2, destinationPort))
              messagesSend.add(1)
            }
          }
        }
      }
    }

    /**
     * 获取指定目的ip指定目的端口在指定时间范围内的源ip个数查询json
     *
     * @param timeStamp
     * @param destinationIp
     * @param destinationPort
     * @return
     */
    def getSourceIpCountQueryEntity(timeStamp: Long, destinationIp: String, destinationPort: String): Entity = {
      val entity = new Entity()
      entity.setTableName(tableName)
      entity.setDimensionsSet(Dimension.getDimensionsSet(Dimension.destinationPort))
      entity.setAggregationsSet(Aggregation.getAggregationsSet(Aggregation.sourceIpCount))
      entity.setGranularity(Granularity.getGranularity(Granularity.periodMinute, 1))


      entity.setFilter(Filter.getFilter(Filter.and,
        Filter.getFilter(Filter.selector, Dimension.destinationPort, destinationPort),
        Filter.getFilter(Filter.selector, Dimension.destinationIp, destinationIp),
        Filter.getFilter(Filter.selector, Dimension.isSucceed, "0"),
        Filter.getFilter(Filter.not,
          Filter.getFilter(Filter.selector, Dimension.protocolId, "1"))))

      val timeStr = timeStrMap.getOrElse((timeStamp, TimeUtil.MINUTE_MILLISECOND),
        getTimeStr2(timeStamp, sourceIpCountIntervalTime, timeStamp - TimeUtil.DAY_MILLISECOND * 10, TimeUtil.MINUTE_MILLISECOND))
      if (!timeStrMap.contains((timeStamp, TimeUtil.MINUTE_MILLISECOND))) {
        timeStrMap((timeStamp, TimeUtil.MINUTE_MILLISECOND)) = timeStr
      }
      entity.setStartTimeStr(timeStr._1)
      entity.setEndTimeStr(timeStr._2)
      return entity
    }


  }


  class QueryWarnSourceRichFlatMapFunction extends RichFlatMapFunction[(Long, String, String), (Long, String, String, String, Double)] {

    //判断源ip访问是否异常时环比参考的范围
    var sourceIpIntervalTime: Long = 0
    //判断源ip访问是否异常时生成树的个数
    var sourceIpNumberOfSubtree: Int = 0
    //判断源ip访问是否异常时聚类的迭代次数
    var sourceIpIters: Int = 0
    //判断源ip访问是否异常时抽取的样本个数
    var sourceIpSampleSize: Int = 0

    private val timeIndexMap = mutable.Map[Long, mutable.Map[Long, Int]]()
    private val timeStrMap = mutable.Map[(Long, Long), (Array[String], Array[String])]()

    private val messagesReceived = new LongCounter()
    private val messagesSend = new LongCounter()

    var tableName: String = null

    override def open(parameters: Configuration): Unit = {

      val globConf = getRuntimeContext.getExecutionConfig.getGlobalJobParameters.asInstanceOf[Configuration]

      DateUtil.ini(globConf.getString(ConfigOptions.key(Constants.FILE_SYSTEM_TYPE).stringType().defaultValue("")),
        globConf.getString(ConfigOptions.key(Constants.DATE_CONFIG_PATH).stringType().defaultValue("")))
      DruidUtil.setDruidHostPortSet(globConf.getString(ConfigOptions.key(Constants.DRUID_BROKER_HOST_PORT).stringType().defaultValue("")))
      DruidUtil.setTimeFormat(globConf.getString(ConfigOptions.key(Constants.DRUID_TIME_FORMAT).stringType().defaultValue("")))
      DruidUtil.setDateStartTimeStamp(globConf.getLong(ConfigOptions.key(Constants.DRUID_DATA_START_TIMESTAMP).longType().defaultValue(0L)))

      sourceIpIntervalTime = globConf.getLong(ConfigOptions.key(Constants.IP_VISIT_SOURCE_IP_INTERVAL_TIME).longType().defaultValue(300000L))
      sourceIpNumberOfSubtree = globConf.getInteger(ConfigOptions.key(Constants.IP_VISIT_SOURCE_IP_NUMBER_OF_SUBTREE).intType().defaultValue(500))
      sourceIpIters = globConf.getInteger(ConfigOptions.key(Constants.IP_VISIT_SOURCE_IP_ITERS).intType().defaultValue(100))
      sourceIpSampleSize = globConf.getInteger(ConfigOptions.key(Constants.IP_VISIT_SOURCE_IP_SAMPLE_SIZE).intType().defaultValue(128))

      getRuntimeContext.addAccumulator("SourceRichFlat:Messages received", messagesReceived)
      getRuntimeContext.addAccumulator("SourceRichFlat:Messages send", messagesSend)

      globConf.getString(ConfigOptions.key(Constants.DRUID_QUINTET_TABLE_NAME).stringType().defaultValue(""))
    }

    override def flatMap(value: (Long, String, String), out: Collector[(Long, String, String, String, Double)]): Unit = {

      messagesReceived.add(1)

      val int = (math.random * 0).toInt

      if (int == 0) {
        val map = timeIndexMap.getOrElse(value._1, getTimeIndex(value._1, sourceIpIntervalTime, value._1 - TimeUtil.DAY_MILLISECOND * 10))

        if (!timeIndexMap.contains(value._1)) {
          timeIndexMap(value._1) = map
        }
        val sourceIpQueryResults = mutable.Map[String, ArrayBuffer[Array[Double]]]()
        val curConnCounts = mutable.Map[String, Double]()
        //查询目的ip指定目的端口在指定时间范围内的源ip的访问情况
        val sourceIpQueryResult = DruidUtil.query(getSourceIpQueryEntity(value._1, value._2, value._3))
        if (sourceIpQueryResult != null) {

          for (i <- sourceIpQueryResult) {

            val key = i.get(Dimension.sourceIp.toString)
            val samples = sourceIpQueryResults.getOrElse(key, {
              val temp = ArrayBuffer[Array[Double]]()
              for (i <- 0 to map.values.max) {
                temp += Array[Double](0.0)
              }
              temp
            })


            val time = i.get(Entity.timestamp).toLong
            val connCount = i.get(Aggregation.connCount.toString).toDouble

            if (time == value._1) {
              curConnCounts.put(key, connCount)
            }


            if (samples(map(i.get(Entity.timestamp).toLong))(0) < connCount) {
              samples(map(i.get(Entity.timestamp).toLong)) = Array[Double](connCount)
            }
            sourceIpQueryResults.put(key, samples)
          }

        }

        for (sourceIpQueryResult <- sourceIpQueryResults) {
          val sourceIp = sourceIpQueryResult._1
          if (curConnCounts.contains(sourceIp)) {
            val samples = sourceIpQueryResult._2

            val iForest = new IForest()

            val curConnCount = curConnCounts.getOrElse(sourceIp, 0.0)
            val predictResult = iForest.predict(samples.toArray, sourceIpNumberOfSubtree,
              sourceIpSampleSize, sourceIpIters, Array[Double](curConnCount), true, false)

            if (!predictResult) {
              out.collect((value._1, value._2, value._3, sourceIp, curConnCount))
              messagesSend.add(1)
            }
          }

        }
      }

    }

    /**
     * 获取指定目的ip指定目的端口在指定时间范围内的不同源ip的访问情况查询json
     *
     * @param timeStamp
     * @param destinationIp
     * @param destinationPort
     * @return
     */
    def getSourceIpQueryEntity(timeStamp: Long, destinationIp: String, destinationPort: String): Entity = {
      val entity = new Entity()
      entity.setTableName(tableName)
      entity.setDimensionsSet(Dimension.getDimensionsSet(Dimension.sourceIp))
      entity.setAggregationsSet(Aggregation.getAggregationsSet(Aggregation.connCount))
      entity.setGranularity(Granularity.getGranularity(Granularity.periodMinute, 1))

      entity.setFilter(
        Filter.getFilter(Filter.and,
          Filter.getFilter(Filter.selector, Dimension.destinationPort, destinationPort),
          Filter.getFilter(Filter.selector, Dimension.destinationIp, destinationIp),
          Filter.getFilter(Filter.selector, Dimension.isSucceed, "0"),
          Filter.getFilter(Filter.not,
            Filter.getFilter(Filter.selector, Dimension.protocolId, "1"))))

      val timeStr = timeStrMap.getOrElse((timeStamp, TimeUtil.MINUTE_MILLISECOND),
        getTimeStr2(timeStamp, sourceIpIntervalTime, timeStamp - TimeUtil.DAY_MILLISECOND * 10, TimeUtil.MINUTE_MILLISECOND))
      if (!timeStrMap.contains((timeStamp, TimeUtil.MINUTE_MILLISECOND))) {
        timeStrMap((timeStamp, TimeUtil.MINUTE_MILLISECOND)) = timeStr
      }
      entity.setStartTimeStr(timeStr._1)
      entity.setEndTimeStr(timeStr._2)

      return entity
    }

  }


  /**
   * 根据当前时间戳获取查询范围
   *
   * @param timeStamp
   * @return
   */
  def getTimeStr2(timeStamp: Long, intervalTime: Long, druidStartTime: Long, granularityTime: Long): (Array[String], Array[String]) = {


    val startArrayBuffer = ArrayBuffer[String]()
    val endArrayBuffer = ArrayBuffer[String]()

    val flag = if (DateUtil.getFlag(timeStamp) > 0) 1 else 0

    var time = (timeStamp + granularityTime) / granularityTime * granularityTime

    while (time > druidStartTime) {

      if ((if (DateUtil.getFlag(time) > 0) 1 else 0) == flag) {
        startArrayBuffer += Entity.getQueryTimeStr(time - intervalTime - granularityTime)
        endArrayBuffer += Entity.getQueryTimeStr(if (endArrayBuffer.isEmpty) time else time + intervalTime)
      }

      time -= TimeUtil.DAY_MILLISECOND
    }

    (startArrayBuffer.toArray, endArrayBuffer.toArray)
  }


  /**
   * 根据时间计算索引
   *
   * @param timeStamp
   * @return
   */
  def getTimeIndex(timeStamp: Long, intervalTime: Long, druidStartTime: Long): mutable.Map[Long, Int] = {
    val map = mutable.Map[Long, Int]()

    val flag = if (DateUtil.getFlag(timeStamp) > 0) 1 else 0


    var time = (timeStamp + TimeUtil.MINUTE_MILLISECOND) / TimeUtil.MINUTE_MILLISECOND * TimeUtil.MINUTE_MILLISECOND

    var index = 0
    while (time > druidStartTime) {

      if ((if (DateUtil.getFlag(time) > 0) 1 else 0) == flag) {
        val startTime = time - intervalTime - TimeUtil.MINUTE_MILLISECOND
        val endTime = if (map.isEmpty) time else time + intervalTime

        for (i <- endTime to(startTime, -TimeUtil.MINUTE_MILLISECOND)) {
          map.put(i, index)
        }
        index += 1
      }

      time -= TimeUtil.DAY_MILLISECOND
    }
    map
  }

  /**
   * @author chenwei
   *         date:  Created in 2018/12/18 22:05
   *         description 获取查询的时间戳
   * @param timeStamp
   * @param intervalTime
   * @param druidStartTime
   * @param granularityTime
   * @return
   */
  def getTimeStr(timeStamp: Long, intervalTime: Long, druidStartTime: Long, granularityTime: Long): (Array[String], Array[String]) = {


    val startArrayBuffer = ArrayBuffer[String]()
    val endArrayBuffer = ArrayBuffer[String]()

    val flag = if (DateUtil.getFlag(timeStamp) > 0) 1 else 0

    var time = timeStamp + granularityTime

    while (time > druidStartTime) {

      if ((if (DateUtil.getFlag(time) > 0) 1 else 0) == flag) {
        startArrayBuffer += Entity.getQueryTimeStr(time - intervalTime - granularityTime)
        endArrayBuffer += Entity.getQueryTimeStr(if (endArrayBuffer.isEmpty) time - granularityTime else time + intervalTime)
      }

      time -= TimeUtil.DAY_MILLISECOND
    }

    (startArrayBuffer.toArray, endArrayBuffer.toArray)
  }

  /**
   * Auther chenwei
   * Description 获取tcp高危漏洞集合
   * Date: Created in 2018/9/26 14:45
   *
   * @param path
   * @return
   */
  def getTcpHighRiskPort(path: String): mutable.Set[Int] = {
    val tcpHighRiskPortSet = mutable.Set[Int]()
    val fs = FileSystem.get(URI.create(path), new org.apache.hadoop.conf
    .Configuration())
    val fsDataInputStream = fs.open(new Path(path))
    val bufferedReader = new BufferedReader(new InputStreamReader(fsDataInputStream))
    var line = bufferedReader.readLine()
    while (line != null) {
      tcpHighRiskPortSet.add(line.trim.toInt)
      line = bufferedReader.readLine()
    }

    tcpHighRiskPortSet
  }

  class ProduceMap(tcpHighRiskPortSet: mutable.Set[Int]) extends RichMapFunction[(Long, String, String, String, Double), ((Object, Boolean), String)] {

    var inPutKafkaValue = ""
    override def map(value: (Long, String, String, String, Double)): ((Object, Boolean), String) = {
      val ipasIpVisitWarnEntity = new IpasIpVisitWarnEntity()
      ipasIpVisitWarnEntity.setDestinationIp(value._2)
      ipasIpVisitWarnEntity.setDestinationPort(value._3)
      ipasIpVisitWarnEntity.setGranularity("60")
      ipasIpVisitWarnEntity.setRuleId(1)
      ipasIpVisitWarnEntity.setRuleName("")
      ipasIpVisitWarnEntity.setSourceIp(value._4)
      ipasIpVisitWarnEntity.setSourcePort("")
      ipasIpVisitWarnEntity.setVisitCount(value._5.toInt)
      ipasIpVisitWarnEntity.setWarnDate(new Timestamp(value._1))
      ipasIpVisitWarnEntity.setWarnName(getWarnName(value._3.toInt, tcpHighRiskPortSet))

      if (ipasIpVisitWarnEntity.getWarnName.nonEmpty && ipasIpVisitWarnEntity.getWarnName != null && !ipasIpVisitWarnEntity.getWarnName.equals("其他")) {
        val inPutKafkaValue = "未知用户" + "|" + "访问高危漏洞告警" + "|" + value._1 + "|" +
          "" + "|" + "" + "|" + "" + "|" +
          "" + "|" + value._4 + "|" + "" + "|" +
          value._2 + "|" + value._3 + "|" + "" + "|" +
          "" + "|" + "" + "|" + ""
      } else {
        val inPutKafkaValue = "未知用户" + "|" + "IP互访" + "|" + value._1 + "|" +
          "" + "|" + "" + "|" + "" + "|" +
          "" + "|" + value._4 + "|" + "" + "|" +
          value._2 + "|" + value._3 + "|" + "" + "|" +
          "" + "|" + "" + "|" + ""
      }

      ((ipasIpVisitWarnEntity, true), inPutKafkaValue)
    }

  }

  /**
   * 获取告警名
   *
   * @param port
   * @param tcpHighRiskPortSet
   * @return
   */
  def getWarnName(port: Int, tcpHighRiskPortSet: mutable.Set[Int]): String = {
    if (tcpHighRiskPortSet.contains(port)) {
      return "访问高危漏洞" + port + "告警"
    } else {
      return "其他"
    }
  }

  class QueryAlertRule extends RichFlatMapFunction[IpasIpVisitWarnEntity, (AlarmRulesEntity, Long)] {

    var sqlHelper: SQLHelper = null
    var DATA_BASE_NAME = ""
    var tableName = ""
    var druidStartTime = 0L

    override def open(parameters: Configuration): Unit = {
      //获取全局变量
      val globConf = getRuntimeContext.getExecutionConfig.getGlobalJobParameters
        .asInstanceOf[Configuration]

      DateUtil.ini(globConf.getString(ConfigOptions.key(Constants.FILE_SYSTEM_TYPE).stringType().defaultValue("")),
        globConf.getString(ConfigOptions.key(Constants.DATE_CONFIG_PATH).stringType().defaultValue("")))
      DruidUtil.setDruidHostPortSet(globConf.getString(ConfigOptions.key(Constants.DRUID_BROKER_HOST_PORT).stringType().defaultValue("")))
      DruidUtil.setTimeFormat(globConf.getString(ConfigOptions.key(Constants.DRUID_TIME_FORMAT).stringType().defaultValue("")))
      DruidUtil.setDateStartTimeStamp(globConf.getLong(ConfigOptions.key(Constants.DRUID_DATA_START_TIMESTAMP).longType().defaultValue(0L)))
      druidStartTime = globConf.getLong(ConfigOptions.key(Constants.DRUID_DATA_START_TIMESTAMP).longType().defaultValue(0L))

      //根据c3p0配置文件,初始化c3p0连接池
      val c3p0Properties = new Properties()
      val c3p0ConfigPath = globConf.getString(ConfigOptions.key(Constants.c3p0_CONFIG_PATH).stringType().defaultValue(""))
      val fs = FileSystem.get(URI.create(c3p0ConfigPath), new org.apache.hadoop.conf
      .Configuration())
      val fsDataInputStream = fs.open(new Path(c3p0ConfigPath))
      val bufferedReader = new BufferedReader(new InputStreamReader(fsDataInputStream))
      c3p0Properties.load(bufferedReader)
      C3P0Util.ini(c3p0Properties)


      //操作数据库的类
      sqlHelper = new SQLHelper()
      DATA_BASE_NAME = sqlHelper.database
    }

    override def flatMap(value: IpasIpVisitWarnEntity, out: Collector[(AlarmRulesEntity, Long)]): Unit = {

      //查询配置的规则
      val alarmRulesEntityList = sqlHelper.query(classOf[AlarmRulesEntity])
      if (alarmRulesEntityList != null && alarmRulesEntityList.size() > 0) {
        for (alarmRulesEntity <- alarmRulesEntityList) {
          out.collect((alarmRulesEntity.asInstanceOf[AlarmRulesEntity], value.getWarnDate.getTime))
        }
      }

    }

  }

  class MergeIpVisitWarn extends RichMapFunction[(AlarmRulesEntity, Long), Option[(Object, Boolean)]] {

    var sqlHelper: SQLHelper = null
    val SPLIT_REGEX = ";"
    val SOURCE_IP_FIELD_NAME = "SOURCE_IP"
    val DESTINATION_IP_FIELD_NAME = "DESTINATION_IP"
    val DESTINATION_PORT_FIELD_NAME = "DESTINATION_PORT"
    val COUNT_FIELD_NAME = "COUNTS"
    val WARN_DATETIME_FIELD_NAME = "WARN_DATE"
    val WARN_CLASS = classOf[IpasIpVisitWarnEntity]
    var DATA_BASE_NAME = ""
    var tableName = ""
    var granularityTime = 15L * TimeUtil.MINUTE_MILLISECOND
    var druidStartTime = 0L


    val mergeNumberOfSubtree = 500
    val mergeSampleSize = 128
    val mergeIters = 100

    val messagesReceived = new LongCounter()
    val sqlQuery = new LongCounter()
    val druidQuery = new LongCounter()
    val sqlQueryed = new LongCounter()
    val druidQueryed = new LongCounter()

    override def open(parameters: Configuration): Unit = {

      //获取全局变量
      val globConf = getRuntimeContext.getExecutionConfig.getGlobalJobParameters.asInstanceOf[Configuration]

      DateUtil.ini(globConf.getString(ConfigOptions.key(Constants.FILE_SYSTEM_TYPE).stringType().defaultValue("")), globConf.getString(ConfigOptions.key(Constants.DATE_CONFIG_PATH).stringType().defaultValue("")))
      DruidUtil.setDruidHostPortSet(globConf.getString(ConfigOptions.key(Constants.DRUID_BROKER_HOST_PORT).stringType().defaultValue("")))
      DruidUtil.setTimeFormat(globConf.getString(ConfigOptions.key(Constants.DRUID_TIME_FORMAT).stringType().defaultValue("")))
      DruidUtil.setDateStartTimeStamp(globConf.getLong(ConfigOptions.key(Constants.DRUID_DATA_START_TIMESTAMP).longType().defaultValue(0L)))
      druidStartTime = globConf.getLong(ConfigOptions.key(Constants.DRUID_DATA_START_TIMESTAMP).longType().defaultValue(0L))


      //根据c3p0配置文件,初始化c3p0连接池
      val c3p0Properties = new Properties()
      val c3p0ConfigPath = globConf.getString(ConfigOptions.key(Constants.c3p0_CONFIG_PATH).stringType().defaultValue(""))
      val fs = FileSystem.get(URI.create(c3p0ConfigPath), new org.apache.hadoop.conf.Configuration())
      val fsDataInputStream = fs.open(new Path(c3p0ConfigPath))
      val bufferedReader = new BufferedReader(new InputStreamReader(fsDataInputStream))
      c3p0Properties.load(bufferedReader)
      C3P0Util.ini(c3p0Properties)


      //操作数据库的类
      sqlHelper = new SQLHelper()
      DATA_BASE_NAME = sqlHelper.database
      tableName = globConf.getString(ConfigOptions.key(Constants.DRUID_QUINTET_TABLE_NAME).stringType().defaultValue(""))

      getRuntimeContext.addAccumulator("MergeIpVisitWarn: Messages received", messagesReceived)
      getRuntimeContext.addAccumulator("MergeIpVisitWarn: sqlQuery", sqlQuery)
      getRuntimeContext.addAccumulator("MergeIpVisitWarn: druidQuery", druidQuery)
      getRuntimeContext.addAccumulator("MergeIpVisitWarn: sqlQueryed", sqlQueryed)
      getRuntimeContext.addAccumulator("MergeIpVisitWarn: druidQueryed", druidQueryed)
    }


    override def map(value: (AlarmRulesEntity, Long)): Option[(Object, Boolean)] = {


      val alarmRulesEntity = value._1
      val timeStamp = value._2
      //获取告警名称
      val warnName = alarmRulesEntity.getAlarmname()
      //获取是否自动判断是否出告警
      var automatic = -1
      try {
        if (alarmRulesEntity.getAutomatic != null) {
          automatic = alarmRulesEntity.getAutomatic.intValue()
        }
      } catch {
        case e: Exception =>
      }
      //判断规则指定的阈值
      var ruleCount = Long.MaxValue
      try {
        if (alarmRulesEntity.getMorenumber != null) {
          ruleCount = alarmRulesEntity.getMorenumber.longValue()
        }
      } catch {
        case e: Exception =>
      }


      //获取sql
      val sql = getSql(alarmRulesEntity, timeStamp, SOURCE_IP_FIELD_NAME, DESTINATION_IP_FIELD_NAME, DESTINATION_PORT_FIELD_NAME, COUNT_FIELD_NAME, WARN_DATETIME_FIELD_NAME, SPLIT_REGEX, WARN_CLASS, DATA_BASE_NAME)

      if (sql != null) {
        //执行sql
        sqlQuery.add(1)
        val sqlResultList = sqlHelper.query(sql)
        sqlQueryed.add(1)
        if (sqlResultList != null && sqlResultList.size() > 0) {


          val valueMap = mutable.Map[(String, String, String), Double]()

          for (map <- sqlResultList) {

            //获取数据库查到的次数
            val counts = map.get(COUNT_FIELD_NAME).toString.toLong

            //获取sourceIp
            var sourceIp = ""
            if (map.containsKey(SOURCE_IP_FIELD_NAME)) {
              sourceIp = map.get(SOURCE_IP_FIELD_NAME).toString
            }

            //获取destinationIp
            var destinationIp = ""
            if (map.containsKey(DESTINATION_IP_FIELD_NAME)) {
              destinationIp = map.get(DESTINATION_IP_FIELD_NAME).toString
            }

            //获取destinationPort
            var destinationPort = ""
            if (map.containsKey(DESTINATION_PORT_FIELD_NAME)) {
              destinationPort = map.get(DESTINATION_PORT_FIELD_NAME).toString
            }

            valueMap.put((sourceIp, destinationIp, destinationPort), counts)

          }

          //获取查询druid的entity
          druidQuery.add(1)
          val entity = getEntity(alarmRulesEntity, tableName, timeStamp, druidStartTime, granularityTime, valueMap.keySet.toSeq)
          druidQueryed.add(1)

          //获取历史记录里的访问次数
          var druidCounts: mutable.Map[(String, String, String), mutable.ArrayBuffer[Array[Double]]] = null
          if (automatic == 1 && entity != null) {
            druidCounts = getDruidCountsMap(entity)
          }

          for ((key, counts) <- valueMap) {

            //是否自动计算数量（0不是，1是）
            automatic match {
              //如果automatic为0,判断是否大于阈值，大于阈值出告警
              case 0 => {
                if (ruleCount < counts) {
                  val ipVisitWarnMergeEntity = new IpVisitWarnMergeEntity
                  ipVisitWarnMergeEntity.setWarnDatetime(new Timestamp(timeStamp))
                  ipVisitWarnMergeEntity.setWarnName(warnName)
                  ipVisitWarnMergeEntity.setDestinationIp(key._2)
                  ipVisitWarnMergeEntity.setDestinationPort(key._3)
                  ipVisitWarnMergeEntity.setInfo(counts.toString)
                  ipVisitWarnMergeEntity.setSourceIp(key._1)
                  return Some((ipVisitWarnMergeEntity.asInstanceOf[Object], false))
                }
              }

              //如果automatic为1,跟历史记录做异常检测,异常出告警
              case 1 => {


                val druidCount = druidCounts.getOrElse(key, mutable.ArrayBuffer[Array[Double]]())


                val iForest = new IForest()


                val predictResult = iForest.predict(druidCount.toArray, mergeNumberOfSubtree,
                  mergeSampleSize, mergeIters, Array[Double](counts), true, false)

                if (!predictResult) {

                  val ipVisitWarnMergeEntity = new IpVisitWarnMergeEntity
                  ipVisitWarnMergeEntity.setWarnDatetime(new Timestamp(timeStamp))
                  ipVisitWarnMergeEntity.setWarnName(warnName)
                  ipVisitWarnMergeEntity.setDestinationIp(key._2)
                  ipVisitWarnMergeEntity.setDestinationPort(key._3)
                  ipVisitWarnMergeEntity.setInfo(counts.toString)
                  ipVisitWarnMergeEntity.setSourceIp(key._1)
                  return Some((ipVisitWarnMergeEntity.asInstanceOf[Object], false))

                }

              }
              case _ =>
            }
          }
        }
      }

      None
    }

    /**
     * @author chenwei
     *         date:  Created in 2018/12/24 15:19
     *         description 将druid查询的结果转成map[sourceIp|destinationIp|destinationPort,count]
     * @param entity
     * @return
     */
    def getDruidCountsMap(entity: Entity): mutable.Map[(String, String, String), mutable.ArrayBuffer[Array[Double]]] = {

      val druidCountsMap = mutable.Map[(String, String, String), mutable.ArrayBuffer[Array[Double]]]()

      val resultList = DruidUtil.query(entity)
      //val resultList : java.util.List[java.util.Map[String, String]] = null

      if (resultList != null && resultList.size() > 0) {
        for (map <- resultList) {
          var count = Double.MaxValue
          if (map.containsKey(Aggregation.sourceIpCount.toString)) {
            count = map.get(Aggregation.sourceIpCount.toString).toDouble
          }

          if (map.containsKey(Aggregation.destinationIpCount.toString)) {
            count = map.get(Aggregation.destinationIpCount.toString).toDouble
          }

          if (map.containsKey(Aggregation.destinationPortCount.toString)) {
            count = map.get(Aggregation.destinationPortCount.toString).toDouble
          }


          var sourceIp = ""
          if (map.containsKey(Dimension.sourceIp.toString)) {
            sourceIp = map.get(Dimension.sourceIp.toString)
          }

          var destinationIp = ""
          if (map.containsKey(Dimension.destinationIp.toString)) {
            destinationIp = map.get(Dimension.destinationIp.toString)
          }

          var destinationPort = ""
          if (map.containsKey(Dimension.destinationPort.toString)) {
            destinationPort = map.get(Dimension.destinationPort.toString)
          }

          val key = (sourceIp, destinationIp, destinationPort)
          val countsArrayBuffer = druidCountsMap.getOrElse(key, mutable.ArrayBuffer[Array[Double]]())
          countsArrayBuffer.append(Array[Double](count))

          druidCountsMap.put(key, countsArrayBuffer)

        }
      }

      druidCountsMap
    }


    /**
     * @author chenwei
     *         date:  Created in 2018/12/21 11:45
     *         description 获取DimensionsSet
     * @param alarmRulesEntity
     * @return
     */
    def getDimensionsSet(alarmRulesEntity: AlarmRulesEntity): java.util.Set[String] = {


      val dimensionsSet = mutable.Set[String]()

      val statisticDimension = alarmRulesEntity.getStatisticDimension
      //如果源IP条件是0(无条件)忽略
      //如果是1(完全相同), 2(存在于)添加到dimensionsSet
      var srcConditions = -1
      try {
        if (alarmRulesEntity.getSrcconditions != null) {
          srcConditions = alarmRulesEntity.getSrcconditions.intValue()
        }
      } catch {
        case e: Exception =>
      }
      srcConditions match {
        //case 0 => if(statisticDimension != null && statisticDimension != 0) dimensionsSet.add(Dimension.sourceIp.toString)
        case 1 => dimensionsSet.add(Dimension.sourceIp.toString)
        case 2 => dimensionsSet.add(Dimension.sourceIp.toString)
        case _ =>
      }

      //如果目的IP条件是0(无条件)忽略
      //如果是1(完全相同), 2(存在于)添加到dimensionsSet
      var destConditions = -1
      try {
        if (alarmRulesEntity.getDestconditions != null) {
          destConditions = alarmRulesEntity.getDestconditions.intValue()
        }
      } catch {
        case e: Exception =>
      }
      destConditions match {
        //case 0 => if(statisticDimension != null && statisticDimension != 1) dimensionsSet.add(Dimension.destinationIp.toString)
        case 1 => dimensionsSet.add(Dimension.destinationIp.toString)
        case 2 => dimensionsSet.add(Dimension.destinationIp.toString)
        case _ =>
      }

      //如果目的PORT条件是0(无条件)忽略
      //如果是1(完全相同), 2(存在于)添加到dimensionsSet
      var portConditions = -1
      try {
        if (alarmRulesEntity.getPortconditions != null) {
          portConditions = alarmRulesEntity.getPortconditions.intValue()
        }
      } catch {
        case e: Exception =>
      }
      portConditions match {
        //case 0 => if(statisticDimension != null && statisticDimension != 2) dimensionsSet.add(Dimension.destinationPort.toString)
        case 1 => dimensionsSet.add(Dimension.destinationPort.toString)
        case 2 => dimensionsSet.add(Dimension.destinationPort.toString)
        case _ =>
      }

      dimensionsSet
    }

    /**
     * @author chenwei
     *         date:  Created in 2018/12/21 11:46
     *         description 获取AggregationsSet
     * @param alarmRulesEntity
     * @return
     */
    def getAggregationsSet(alarmRulesEntity: AlarmRulesEntity): java.util.Set[JSONObject] = {


      //根据统计维度(0源IP, 1目的IP, 2端口)添加统计项
      var statisticDimension = -1
      try {
        if (alarmRulesEntity.getStatisticDimension != null) {
          statisticDimension = alarmRulesEntity.getStatisticDimension.intValue()
        }
      } catch {
        case e: Exception =>
      }
      statisticDimension match {
        case 0 => Aggregation.getAggregationsSet(Aggregation.sourceIpCount)
        case 1 => Aggregation.getAggregationsSet(Aggregation.destinationIpCount)
        case 2 => Aggregation.getAggregationsSet(Aggregation.destinationPortCount)
        case 3 => null
      }


    }


    /**
     * @author chenwei
     *         date:  Created in 2018/12/21 11:46
     *         description 获取Filter
     * @param alarmRulesEntity
     * @return
     */
    def getFilter(keySet: Seq[(String, String, String)]): JSONObject = {


      val jsonArray = new JSONArray

      for ((sourceIp, destinationIp, destinationPort) <- keySet) {
        //根据sourceIp设置过滤条件
        var srcIpFilter: JSONObject = null
        if (sourceIp != null && sourceIp.length > 0) {
          srcIpFilter = Filter.getFilter(Filter.selector, Dimension.sourceIp, sourceIp)
        }

        //根据destinationIp设置过滤条件
        var dstIpFilter: JSONObject = null
        if (destinationIp != null && destinationIp.length > 0) {
          dstIpFilter = Filter.getFilter(Filter.selector, Dimension.destinationIp, destinationIp)
        }

        //根据存在于的destinationPort设置过滤条件

        var dstPortFilter: JSONObject = null
        if (destinationPort != null && destinationPort.length > 0) {
          dstPortFilter = Filter.getFilter(Filter.selector, Dimension.destinationPort, destinationPort)

        }


        val singleJsonArray = new JSONArray()
        if (srcIpFilter != null) {
          singleJsonArray.put(srcIpFilter)
        }
        if (dstIpFilter != null) {
          singleJsonArray.put(dstIpFilter)
        }
        if (dstPortFilter != null) {
          singleJsonArray.put(dstPortFilter)
        }

        if (singleJsonArray.length() > 0) {
          jsonArray.put(Filter.getFilter(Filter.and, singleJsonArray))
        }
      }

      if (jsonArray.length() > 0) {
        return Filter.getFilter(Filter.or, jsonArray)
      } else {
        return null
      }

    }


    /**
     * @author chenwei
     *         date:  Created in 2018/12/18 22:05
     *         description 获取查询druid的entity
     * @param tableName
     * @param dimensionsSet
     * @param aggregationsSet
     * @param granularity
     * @param filter
     * @param timeStamp
     * @param intervalTime
     * @param druidStartTime
     * @param granularityTime
     * @return
     */
    def getEntity(alarmRulesEntity: AlarmRulesEntity, tableName: String, timeStamp: Long, druidStartTime: Long, granularityTime: Long, keySet: Seq[(String, String, String)]): Entity = {

      if (alarmRulesEntity != null && tableName != null) {
        val dimensionsSet = getDimensionsSet(alarmRulesEntity)
        val aggregationsSet = getAggregationsSet(alarmRulesEntity)
        val granularity = getGranularity(alarmRulesEntity)
        val filter = getFilter(keySet)
        val intervalTime = getIntervalTime(alarmRulesEntity)

        if (dimensionsSet != null && aggregationsSet != null && granularity != null && intervalTime > 0) {

          val entity = new Entity()
          entity.setTableName(tableName)
          entity.setDimensionsSet(dimensionsSet)
          entity.setAggregationsSet(aggregationsSet)
          entity.setGranularity(granularity)
          if (filter != null) {
            entity.setFilter(filter)
          }

          val timeStr = getTimeStr(timeStamp, intervalTime, timeStamp - TimeUtil.DAY_MILLISECOND * 10, granularityTime)
          entity.setStartTimeStr(timeStr._1)
          entity.setEndTimeStr(timeStr._2)

          return entity
        }
      }

      return null
    }

    /**
     * @author chenwei
     *         date:  Created in 2018/12/21 11:55
     *         description 获取Granularity
     * @param alarmRulesEntity
     */
    def getGranularity(alarmRulesEntity: AlarmRulesEntity): JSONObject = {

      //时间力度(0秒，1分，2小时，3天)
      var timeIntensity = -1
      try {
        if (alarmRulesEntity.getTimeintensity != null) {
          timeIntensity = alarmRulesEntity.getTimeintensity.intValue()
        }
      } catch {
        case e: Exception =>
      }
      var timeLength = -1
      try {
        if (alarmRulesEntity.getTimelength != null) {
          timeLength = alarmRulesEntity.getTimelength.intValue()
        }
      } catch {
        case e: Exception =>
      }

      if (timeLength <= 0) {
        return null
      }

      timeIntensity match {
        case 0 => Granularity.getGranularity(Granularity.periodSecond, timeLength)
        case 1 => Granularity.getGranularity(Granularity.periodMinute, timeLength)
        case 2 => Granularity.getGranularity(Granularity.periodHour, timeLength)
        case 3 => Granularity.getGranularity(Granularity.periodDay, timeLength)
        case _ => null
      }


    }

  }


  /**
   * @author chenwei
   *         date:  Created in 2018/12/21 15:47
   *         description 获取IntervalTime
   * @param alarmRulesEntity
   * @return
   */
  def getIntervalTime(alarmRulesEntity: AlarmRulesEntity): Long = {

    //时间力度(0秒，1分，2小时，3天)
    var timeIntensity = -1

    try {
      if (alarmRulesEntity.getTimeintensity != null) {
        timeIntensity = alarmRulesEntity.getTimeintensity.intValue()
      }
    } catch {
      case e: Exception =>
    }
    var timeLength = -1
    try {
      if (alarmRulesEntity.getTimelength != null) {
        timeLength = alarmRulesEntity.getTimelength.intValue()
      }
    } catch {
      case e: Exception =>
    }

    if (timeLength <= 0) {
      return -1
    }

    timeIntensity match {
      case 0 => timeLength * TimeUtil.SECOND_MILLISECOND
      case 1 => timeLength * TimeUtil.MINUTE_MILLISECOND
      case 2 => timeLength * TimeUtil.HOUR_MILLISECOND
      case 3 => timeLength * TimeUtil.DAY_MILLISECOND
      case _ => -1
    }
  }


  /**
   * 生成查询语句
   *
   * @param alarmRulesEntity
   * @param srcIpFieldName
   * @param destIpFieldName
   * @param destPortFieldName
   * @param countFieldName
   * @param splitRegex
   * @param clazz
   * @return
   */
  def getSql(alarmRulesEntity: AlarmRulesEntity, timeStamp: Long, srcIpFieldName: String,
             destIpFieldName: String, destPortFieldName: String,
             countFieldName: String, warnDateTimeFieldName: String, splitRegex: String, clazz: Class[_ <: Object], dataBaseName: String): String = {

    val sqlStringBuilder = new mutable.StringBuilder()

    val selectSql = generateSelectSql(alarmRulesEntity, srcIpFieldName, destIpFieldName,
      destPortFieldName, countFieldName)
    val fromSql = generateFromSql(clazz, dataBaseName)
    val whereSql = generateWhereSql(alarmRulesEntity, timeStamp, srcIpFieldName, destIpFieldName,
      destPortFieldName, warnDateTimeFieldName, splitRegex: String)
    val groupBySql = generateGroupBySql(alarmRulesEntity, srcIpFieldName, destIpFieldName,
      destPortFieldName)

    if (selectSql == null || fromSql == null || whereSql == null || groupBySql == null) {
      return null
    }

    val srcConditions = alarmRulesEntity.getSrcconditions
    val destConditions = alarmRulesEntity.getDestconditions
    val portConditions = alarmRulesEntity.getPortconditions
    val statisticDimension = alarmRulesEntity.getStatisticDimension
    if (statisticDimension == null) {
      return null
    }


    try {

      statisticDimension.intValue() match {
        case 0 => if (srcConditions != null) {
          if (srcConditions != 0) {
            return null
          }
        }
        case 1 => if (destConditions != null) {
          if (destConditions != 0) {
            return null
          }
        }
        case 2 => if (portConditions != null) {
          if (portConditions != 0) {
            return null
          }
        }
        case _ => return null
      }
    } catch {
      case e: Exception =>
    }


    sqlStringBuilder.append(selectSql)
    sqlStringBuilder.append(fromSql)
    sqlStringBuilder.append(whereSql)
    sqlStringBuilder.append(groupBySql)

    sqlStringBuilder.toString()


  }


  /**
   * @author chenwei
   *         date:  Created in 2018/12/24 10:19
   *         description 生成select语句
   * @param alarmRulesEntity
   * @param srcIpFieldName
   * @param destIpFieldName
   * @param destPortFieldName
   * @param countFieldName
   * @return
   */
  def generateSelectSql(alarmRulesEntity: AlarmRulesEntity, srcIpFieldName: String,
                        destIpFieldName: String, destPortFieldName: String,
                        countFieldName: String): String = {

    val select = " SELECT "
    val selectStringBuilder = new mutable.StringBuilder()


    selectStringBuilder.append(select)

    val srcConditions = alarmRulesEntity.getSrcconditions
    val destConditions = alarmRulesEntity.getDestconditions
    val portConditions = alarmRulesEntity.getPortconditions
    val statisticDimension = alarmRulesEntity.getStatisticDimension

    if (srcConditions != null) {
      if (srcConditions == 1 || srcConditions == 2 || srcConditions == 3) {
        selectStringBuilder.append(" ")
          .append(srcIpFieldName)
          .append(",")
      }

    }

    if (destConditions != null) {
      if (destConditions == 1 || destConditions == 2 || destConditions == 3) {
        selectStringBuilder.append(" ")
          .append(destIpFieldName)
          .append(",")
      }
    }


    if (portConditions != null) {
      if (portConditions == 1 || portConditions == 2 || portConditions == 3) {
        selectStringBuilder.append(" ")
          .append(destPortFieldName)
          .append(",")

      }

    }

    if (statisticDimension != null) {
      val countSql = generateCountSql(statisticDimension, srcIpFieldName, destIpFieldName, destPortFieldName, countFieldName)


      if (countSql != null && countSql.length > 0) {

        selectStringBuilder.append(countSql)

        return selectStringBuilder.toString()
      }
    }
    null

  }

  /**
   * @author chenwei
   *         date:  Created in 2018/12/21 17:40
   *         description
   * @param dimension
   * @param srcIpFieldName
   * @param destIpFieldName
   * @param destPortFieldName
   * @param countFieldName
   * @return
   */
  def generateCountSql(dimension: Int, srcIpFieldName: String,
                       destIpFieldName: String, destPortFieldName: String,
                       countFieldName: String): String = {
    val dimensionFieldName = dimension match {
      case 0 => srcIpFieldName

      case 1 => destIpFieldName

      case 2 => destPortFieldName

      case _ => null

    }

    if (dimensionFieldName != null) {
      " COUNT( DISTINCT " + dimensionFieldName + " ) AS  " + countFieldName + "  "
    } else {
      null
    }
  }

  /**
   * @author chenwei
   *         date:  Created in 2018/12/24 10:37
   *         description 生成from语句
   * @param clazz
   * @return
   */
  def generateFromSql(clazz: Class[_ <: Object], dataBaseName: String): String = {
    val fromStringBuilder = new mutable.StringBuilder()


    var tableName = ""

    if (clazz != null) {
      try { //获取该类的表明
        val table = clazz.getAnnotation(classOf[Table])
        if (table != null) {
          tableName = table.name()
        }
      } catch {
        case e: Exception =>

      }
    }


    if (tableName != null && tableName.length > 0 && dataBaseName != null && dataBaseName.length > 0) {
      fromStringBuilder.append(" FROM ")
        .append(dataBaseName)
        .append(".")
        .append(tableName)
        .append(" ")
    }

    if (fromStringBuilder.nonEmpty) {
      fromStringBuilder.toString()
    } else {
      null
    }
  }

  /**
   * @author chenwei
   *         date:  Created in 2018/12/24 10:05
   *         description 生成where条件
   * @param alarmRulesEntity
   * @param srcIpFieldName
   * @param destIpFieldName
   * @param destPortFieldName
   * @param countFieldName
   * @return
   */
  def generateWhereSql(alarmRulesEntity: AlarmRulesEntity, timeStamp: Long, srcIpFieldName: String,
                       destIpFieldName: String, destPortFieldName: String, warnDateTimeFieldName: String, splitRegex: String): String = {


    val where = " WHERE "
    val and = " AND "
    val whereStringBuilder = new mutable.StringBuilder()


    whereStringBuilder.append(where)

    //添加源IP过滤条件
    val srcConditions = alarmRulesEntity.getSrcconditions

    val srcIp = alarmRulesEntity.getSrcip
    if (srcConditions != null && srcIp != null) {
      val srcIpWhere = generateWhereInSql(srcIp, srcConditions, srcIpFieldName, splitRegex)
      if (srcIpWhere != null) {
        if (whereStringBuilder.length > where.length) {
          whereStringBuilder.append(and)
        }

        whereStringBuilder.append(srcIpWhere)
      }
    }

    //添加目的IP过滤条件
    val destConditions = alarmRulesEntity.getDestconditions
    val dstIp = alarmRulesEntity.getDestip
    if (destConditions != null && dstIp != null) {
      val dstIpWhere = generateWhereInSql(dstIp, destConditions, destIpFieldName, splitRegex)
      if (dstIpWhere != null) {
        if (whereStringBuilder.length > where.length) {
          whereStringBuilder.append(and)
        }
        whereStringBuilder.append(dstIpWhere)
      }
    }

    //添加目的PORT过滤条件
    val portConditions = alarmRulesEntity.getPortconditions
    val dstPort = alarmRulesEntity.getPorts
    if (portConditions != null && dstPort != null) {
      val dstPortWhere = generateWhereInSql(dstPort, portConditions, destPortFieldName, splitRegex)
      if (dstPortWhere != null) {
        if (whereStringBuilder.length > where.length) {
          whereStringBuilder.append(and)
        }
        whereStringBuilder.append(dstPortWhere)
      }
    }

    //添加时间过滤条件
    val intervalTime = getIntervalTime(alarmRulesEntity)
    if (intervalTime >= 0) {

      if (whereStringBuilder.length > where.length) {
        whereStringBuilder.append(" AND ")
      }
      whereStringBuilder.append(" '")
        .append(new Timestamp(timeStamp - getIntervalTime(alarmRulesEntity)))
        .append("' <= ")
        .append(warnDateTimeFieldName)


      whereStringBuilder.append(" AND ")
        .append(warnDateTimeFieldName)
        .append(" < ")
        .append(" '")
        .append(new Timestamp(timeStamp))
        .append("' ")
      whereStringBuilder.toString()
    } else {
      null
    }
  }

  /**
   * @author chenwei
   *         date:  Created in 2018/12/21 18:00
   *         description 生成wherein语句
   * @param value
   * @param conditions
   * @param filedName
   * @return
   */
  def generateWhereInSql(value: String, conditions: Int, filedName: String, splitRegex: String): String = {

    if (value != null && value.length > 0) {
      if (conditions == 2 || conditions == 3) {
        val whereIn = concatToWhereIn(value, splitRegex: String)
        if (whereIn != null) {
          if (conditions == 2) {
            return filedName + " IN (" + whereIn + ")"
          } else {
            return filedName + " NOT IN (" + whereIn + ")"
          }
        }
      }
    }
    return null
  }

  /**
   * 把 ,分割的多个数据拆分成 '',''.....的形式
   *
   * @param params
   * @return
   */
  def concatToWhereIn(params: String, splitRegex: String): String = {
    val sb = new StringBuilder
    if (params != null && params.length > 0 && splitRegex != null && splitRegex.length > 0) {
      val split = params.split(splitRegex)
      for (str <- split) {
        sb.append("'").append(str).append("',")
      }

      if (sb.nonEmpty) {
        return sb.substring(0, sb.length - 1)
      }
    }
    null
  }

  /**
   * @author chenwei
   *         date:  Created in 2018/12/24 10:28
   *         description 生成groupBy语句
   * @todo 只参考srcConditions、destConditions、portConditions,不参考statisticDimension
   * @param alarmRulesEntity
   * @param srcIpFieldName
   * @param destIpFieldName
   * @param destPortFieldName
   * @return
   */
  def generateGroupBySql(alarmRulesEntity: AlarmRulesEntity, srcIpFieldName: String,
                         destIpFieldName: String, destPortFieldName: String): String = {

    val groupBy = " GROUP BY "

    val groupByStringBuilder = new mutable.StringBuilder()
    groupByStringBuilder.append(groupBy)

    val srcConditions = alarmRulesEntity.getSrcconditions
    val destConditions = alarmRulesEntity.getDestconditions
    val portConditions = alarmRulesEntity.getPortconditions

    if (srcConditions != null) {
      if (srcConditions == 1 || srcConditions == 2 || srcConditions == 3) {
        groupByStringBuilder.append(" ")
          .append(srcIpFieldName)
          .append(",")

      }
    }

    if (destConditions != null) {
      if (destConditions == 1 || destConditions == 2 || destConditions == 3) {
        groupByStringBuilder.append(" ")
          .append(destIpFieldName)
          .append(",")

      }
    }

    if (portConditions != null) {
      if (portConditions == 1 || portConditions == 2 || portConditions == 3) {
        groupByStringBuilder.append(" ")
          .append(destPortFieldName)
          .append(",")

      }
    }

    if (groupByStringBuilder.length > groupBy.length) {
      groupByStringBuilder.deleteCharAt(groupByStringBuilder.length - 1)
      groupByStringBuilder.toString()
    } else {
      ""
    }

  }


  /**
   * 判断是否是内网ip
   *
   * @param ip
   * @return
   */
  def isIntranet(ip: String, placeSet: Set[String]): Boolean = {

    if (placeSet != null && ip != null) {
      if (placeSet.contains(ip)) {
        return true
      } else {
        val ipPart = ip.split("\\.", -1)
        if (ipPart.length == 4) {

          val ipTwo = ipPart(0) + "." + ipPart(1)
          val ipThree = ipTwo + "." + ipPart(2)
          if (placeSet.contains(ipThree)) {
            return true
          } else if (placeSet.contains(ipTwo)) {
            return true
          } else if (placeSet.contains(ipPart(0))) {
            return true
          }
        }
      }
    }

    false
  }

  /**
   * 判断是否是内网ip
   *
   * @param
   * @return
   */
  def isHttpPort(port: String, httpPortSet: Set[String]): Boolean = {
    httpPortSet.contains(port)
  }

  def getIntranetSet(path: String): Set[String] = {
    val intranetSet = mutable.Set[String]()
    val fs = FileSystem.get(URI.create(path), new org.apache.hadoop.conf
    .Configuration())
    val fsDataInputStream = fs.open(new Path(path))
    val bufferedReader = new BufferedReader(new InputStreamReader(fsDataInputStream))
    var line = bufferedReader.readLine()
    while (line != null) {
      val values = line.split("\\|", -1)
      if (values.length == 2) {
        intranetSet.add(values(0).trim)
        line = bufferedReader.readLine()
      }
    }

    intranetSet.toSet
  }

  def getHttpPortSet(): Set[String] = {
    val httpPortSet = mutable.Set[String]()
    httpPortSet.add("443")
    httpPortSet.add("80")
    httpPortSet.add("8080")
    httpPortSet.add("8081")
    httpPortSet.add("81")
    httpPortSet.toSet
  }
}
