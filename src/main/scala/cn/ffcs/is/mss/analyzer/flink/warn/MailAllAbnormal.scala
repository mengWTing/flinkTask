package cn.ffcs.is.mss.analyzer.flink.warn

import java.io.{BufferedReader, InputStreamReader}
import java.net.URI
import java.sql.Timestamp
import java.util.{Date, Properties}

import cn.ffcs.is.mss.analyzer.bean.MailAbnormalLoginIpEntity
import cn.ffcs.is.mss.analyzer.druid.model.scala.MailModel
import cn.ffcs.is.mss.analyzer.flink.sink.MySQLSink
import cn.ffcs.is.mss.analyzer.utils.druid.entity._
import cn.ffcs.is.mss.analyzer.utils._
import org.apache.flink.api.common.accumulators.LongCounter
import org.apache.flink.api.common.functions.RichFilterFunction
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.environment.LocalStreamEnvironment
import org.apache.flink.streaming.api.functions.ProcessFunction
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaConsumer, FlinkKafkaProducer}
import org.apache.flink.streaming.util.serialization.SimpleStringSchema
import org.apache.flink.util.Collector
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.flink.streaming.api.scala._

import scala.collection.JavaConversions._
import scala.collection.mutable

/**
 * @title MailAllAbnormal
 * @author PlatinaBoy
 * @date 2021-05-11 10:34
 * @description
 * @update [no][date YYYY-MM-DD][name][description]
 */
object MailAllAbnormal {
  def main(args: Array[String]): Unit = {
    //    val args0 = "E:\\ffcs\\mss\\src\\main\\resources\\flink.ini"
    //    val confProperties = new IniProperties(args0)
    val confProperties = new IniProperties(args(0))

    //该任务的名字
    val jobName = confProperties.getValue(Constants.MAIL_LOGIN_ABNORMAL_CONFIG,
      Constants.MAIL_LOGIN_ABNORMAL_JOB_NAME)
    //kafka Source的名字
    val kafkaSourceName = confProperties.getValue(Constants.MAIL_LOGIN_ABNORMAL_CONFIG,
      Constants.MAIL_LOGIN_ABNORMAL_KAFKA_SOURCE_NAME)
    //mysql sink的名字
    val sqlSinkName = confProperties.getValue(Constants.MAIL_LOGIN_ABNORMAL_CONFIG,
      Constants.MAIL_LOGIN_ABNORMAL_SQL_SINK_NAME)
    //kafka sink名字
    val kafkaSinkName = confProperties.getValue(Constants.MAIL_LOGIN_ABNORMAL_CONFIG,
      Constants.MAIL_LOGIN_ABNORMAL_KAFKA_SINK_NAME)

    //kafka Source的并行度
    val kafkaSourceParallelism = confProperties.getIntValue(Constants
      .MAIL_LOGIN_ABNORMAL_CONFIG, Constants
      .MAIL_LOGIN_ABNORMAL_KAFKA_SOURCE_PARALLELISM)
    //对数据处理的并行度
    val dealParallelism = confProperties.getIntValue(Constants
      .MAIL_LOGIN_ABNORMAL_CONFIG, Constants.MAIL_LOGIN_ABNORMAL_DEAL_PARALLELISM)
    //写入mysql的并行度
    val sqlSinkParallelism = confProperties.getIntValue(Constants
      .MAIL_LOGIN_ABNORMAL_CONFIG, Constants.MAIL_LOGIN_ABNORMAL_SQL_SINK_PARALLELISM)
    //kafka sink的并行度
    val kafkaSinkParallelism = confProperties.getIntValue(Constants.MAIL_LOGIN_ABNORMAL_CONFIG,
      Constants.MAIL_LOGIN_ABNORMAL_KAFKA_SINK_PARALLELISM)

    val warningSinkTopic = confProperties.getValue(Constants.WARNING_FLINK_TO_DRUID_CONFIG, Constants
      .WARNING_TOPIC)

    //kafka的服务地址
    val brokerList = confProperties.getValue(Constants.FLINK_COMMON_CONFIG, Constants
      .KAFKA_BOOTSTRAP_SERVERS)
    //flink消费的group.id
    val groupId = confProperties.getValue(Constants.MAIL_LOGIN_ABNORMAL_CONFIG,
      Constants.MAIL_LOGIN_ABNORMAL_GROUP_ID)
    //kafka source的topic
    val sourceTopic = confProperties.getValue(Constants.MAIL_FLINK_TO_DRUID_CONFIG, Constants
      .MAIL_TO_KAFKA_TOPIC)
    //kafka sink的topic
    val sinkTopic = confProperties.getValue(Constants.MAIL_LOGIN_ABNORMAL_CONFIG, Constants
      .MAIL_LOGIN_ABNORMAL_KAFKA_SINK_TOPIC)


    //flink全局变量
    val parameters: Configuration = new Configuration()
    //文件类型配置

    val fileSystem = confProperties.getValue(Constants.FLINK_COMMON_CONFIG, Constants.FILE_SYSTEM_TYPE)
    parameters.setString(Constants.FILE_SYSTEM_TYPE, fileSystem)
    //c3p0连接池配置文件路径
    parameters.setString(Constants.c3p0_CONFIG_PATH, confProperties.getValue(Constants
      .FLINK_COMMON_CONFIG, Constants.c3p0_CONFIG_PATH))
    //领导邮箱列表文件
    val allMailPath = confProperties.getValue(Constants.MAIL_LOGIN_ABNORMAL_CONFIG, Constants
      .MAIL_LOGIN_ABNORMAL_LEADER_NAME_LIST)
    parameters.setString(Constants.MAIL_LOGIN_ABNORMAL_LEADER_NAME_LIST, allMailPath)
    //mail在druid的表名
    parameters.setString(Constants.DRUID_MAIL_TABLE_NAME, confProperties.getValue(Constants.FLINK_COMMON_CONFIG,
      Constants.DRUID_MAIL_TABLE_NAME))

    parameters.setString(Constants.DRUID_BROKER_HOST_PORT, confProperties.getValue(Constants.FLINK_COMMON_CONFIG,
      Constants.DRUID_BROKER_HOST_PORT))
    //druid时间格式
    parameters.setString(Constants.DRUID_TIME_FORMAT, confProperties.getValue(Constants.FLINK_COMMON_CONFIG,
      Constants.DRUID_TIME_FORMAT))
    //druid数据开始的时间
    parameters.setLong(Constants.DRUID_DATA_START_TIMESTAMP, confProperties.getLongValue(Constants
      .FLINK_COMMON_CONFIG, Constants.DRUID_DATA_START_TIMESTAMP))

    //统计时长
    parameters.setInteger(Constants.MAIL_LOGIN_ABNORMAL_HISTORY_LENGTH, confProperties.getIntValue(Constants
      .MAIL_LOGIN_ABNORMAL_CONFIG, Constants.MAIL_LOGIN_ABNORMAL_HISTORY_LENGTH))

    //check pointing的间隔
    val checkpointInterval = confProperties.getLongValue(Constants
      .MAIL_LOGIN_ABNORMAL_CONFIG, Constants.MAIL_LOGIN_ABNORMAL_CHECKPOINT_INTERVAL)

    //获取ExecutionEnvironment
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    //设置流的时间为ProcessTime
    env.setStreamTimeCharacteristic(TimeCharacteristic.ProcessingTime)
    //设置flink全局变量
    env.getConfig.setGlobalJobParameters(parameters)

    //设置kafka消费者相关配置
    val props = new Properties()
    //设置kafka集群地址
    props.setProperty("bootstrap.servers", brokerList)
    //设置flink消费的group.id
    props.setProperty("group.id", groupId)

    //    val allNameSet = readLeaderNameSet(allMailPath, fileSystem)
    //获取kafka消费者
    val consumer = new FlinkKafkaConsumer[String](sourceTopic, new SimpleStringSchema, props)
      .setStartFromGroupOffsets()
    //获取kafka生产者
    val producer = new FlinkKafkaProducer[String](sinkTopic, new SimpleStringSchema, props)
    // 获取kafka数据
    val warnData = env.addSource(consumer).setParallelism(kafkaSourceParallelism)
      .uid(kafkaSourceName).name(kafkaSourceName)
      .map(JsonUtil.fromJson[MailModel] _).setParallelism(dealParallelism)
      .process(new AbnormalProcess()).setParallelism(dealParallelism)

    val value = warnData.map(_._1)
    val alertKafakValue = warnData.map(_._2)
    value.addSink(new MySQLSink)
      .uid(sqlSinkName)
      .name(sqlSinkName)
      .setParallelism(sqlSinkParallelism)

    warnData.map(m => JsonUtil.toJson(m._1._1.asInstanceOf[MailAbnormalLoginIpEntity]))
      .addSink(producer)
      .uid(kafkaSinkName)
      .name(kafkaSinkName)
      .setParallelism(kafkaSinkParallelism)

    //将告警数据写入告警库topic
    val warningProducer = new FlinkKafkaProducer[String](brokerList, warningSinkTopic,
      new SimpleStringSchema())
    alertKafakValue.addSink(warningProducer).setParallelism(kafkaSinkParallelism)
    env.execute(jobName)

  }

  class AbnormalProcess() extends ProcessFunction[MailModel, ((Object, Boolean), String)] {

    val userInfoMap = new mutable.HashMap[String, mutable.HashMap[String, mutable.HashSet[String]]]()
    var tableName = ""
    var historyLen: Int = _
    val aggregationSet = new mutable.HashSet[Aggregation]
    val inputKafkaValue = ""

    val messagesReceived = new LongCounter()
    val messagesExecuteSucceed = new LongCounter()
    val messagesExecuteFail = new LongCounter()


    override def open(parameters: Configuration): Unit = {
      val globConf = getRuntimeContext.getExecutionConfig.getGlobalJobParameters.asInstanceOf[Configuration]


      historyLen = globConf.getInteger(Constants.MAIL_LOGIN_ABNORMAL_HISTORY_LENGTH, 0)

      //mail的druid表名
      tableName = globConf.getString(Constants.DRUID_MAIL_TABLE_NAME, "")
      //设置druid的broker的host和port
      DruidUtil.setDruidHostPortSet(globConf.getString(Constants.DRUID_BROKER_HOST_PORT, ""))
      //设置写入druid的时间格式
      DruidUtil.setTimeFormat(globConf.getString(Constants.DRUID_TIME_FORMAT, ""))
      //设置druid开始的时间
      DruidUtil.setDateStartTimeStamp(globConf.getLong(Constants.DRUID_DATA_START_TIMESTAMP, 0L))

      //      println(userInfoMap)
      //增加Accumulator 记录收到的数据个数、执行成功个数、执行失败的个数
      getRuntimeContext.addAccumulator("Messages received", messagesReceived)
      getRuntimeContext.addAccumulator("Messages execute succeed", messagesExecuteSucceed)
      getRuntimeContext.addAccumulator("Messages execute fail", messagesExecuteFail)
      // TODO:  对历史的ip进行ip归属地查询
    }

    override def processElement(value: MailModel, ctx: ProcessFunction[MailModel, ((Object, Boolean), String)]#Context,
                                out: Collector[((Object, Boolean), String)]): Unit = {
      messagesReceived.add(1L)
      val date = new Date

      val endTimestamp = TimeUtil.getDayStartTime(date.getTime)

      val startTimestamp = endTimestamp - historyLen * TimeUtil.DAY_MILLISECOND
      for (i <- startTimestamp until endTimestamp by 1 * TimeUtil.DAY_MILLISECOND) {

        val queryEntity = getUserIpQueryDruidEntity(i, i + 1 * TimeUtil.DAY_MILLISECOND, tableName,
          aggregationSet, value.userName)
        queryDruidFillSrcIp("sourceIp", value.userName, queryEntity, userInfoMap)

      }
      val userName = value.userName
      val srcIp = value.sourceIp
      val operationTime = value.timeStamp
      val createTime = new Date().getTime
      val userIpMap = userInfoMap.getOrElseUpdate(userName,
        new mutable.HashMap[String, mutable.HashSet[String]]())
      if (userIpMap.nonEmpty) {
        val ipSet = userIpMap("sourceIp")
        if (!ipSet.contains(srcIp)) {
          messagesExecuteSucceed.add(1L)
          val entity = new MailAbnormalLoginIpEntity
          entity.setUserName(userName)
          entity.setLoginIp(srcIp)
          entity.setOperationTime(new Timestamp(operationTime))
          entity.setCreateTime(new Timestamp(createTime))
          val inputKafkaValue = userName + "|" + "邮件全部异常告警" + "|" + operationTime + "|" +
            "" + "|" + "" + "|" + "" + "|" +
            "" + "|" + "" + "|" + "" + "|" +
            "" + "|" + "" + "|" + "" + "|" +
            "" + "|" + "" + "|" + ""
          out.collect((entity.asInstanceOf[Object], true), inputKafkaValue)
        }
      } else {
        messagesExecuteFail.add(1L)
      }


    }
  }

  def getUserIpQueryDruidEntity(startTimestamp: Long, endTimestamp: Long, tableName: String, aggregationSet: collection
  .Set[Aggregation], userName: String): Entity = {
    val entity = new Entity()
    entity.setTableName(tableName)
    entity.setDimensionsSet(Dimension.getDimensionsSet(Dimension.sourceIp))
    entity.setAggregationsSet(aggregationSet.map(tuple => Aggregation.getAggregation(tuple)))
    entity.setGranularity(Granularity.getGranularity(Granularity.periodHour, 1))
    entity.setFilter(Filter.getFilter(Filter.selector, Dimension.userName, userName))
    entity.setStartTimeStr(startTimestamp)
    entity.setEndTimeStr(endTimestamp)
    entity
  }

  def queryDruidFillSrcIp(dimensionName: String, userName: String, entity: Entity, map: mutable.HashMap[String,
    mutable.HashMap[String, mutable.HashSet[String]]]): Unit = {
    val resultList = DruidUtil.query(entity)
    for (i <- resultList) {
      val srcIp = i(dimensionName)
      map.getOrElseUpdate(userName, new mutable.HashMap[String, mutable.HashSet[String]])
        .getOrElseUpdate("sourceIp", new mutable.HashSet[String]).add(srcIp)
    }
  }


  def readLeaderNameSet(filePath: String, fileSystemType: String): mutable.HashSet[String] = {
    val nameSet = new mutable.HashSet[String]

    val fs = FileSystem.get(URI.create(fileSystemType), new org.apache.hadoop.conf.Configuration())
    val fsDataInputStream = fs.open(new Path(filePath))
    val bufferedReader = new BufferedReader(new InputStreamReader(fsDataInputStream))

    var line = bufferedReader.readLine()
    while (line != null) {
      nameSet.add(line.trim)
      line = bufferedReader.readLine()
    }
    nameSet
  }
}
