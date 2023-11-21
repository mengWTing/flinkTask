package cn.ffcs.is.mss.analyzer.flink.warn

import java.sql.Timestamp
import java.util.Properties

import cn.ffcs.is.mss.analyzer.bean.BbasSameTimeDifferentPlaceWarnEntity
import cn.ffcs.is.mss.analyzer.druid.model.scala.OperationModel
import cn.ffcs.is.mss.analyzer.flink.sink.{MySQLSink, Sink}
import cn.ffcs.is.mss.analyzer.flink.source.Source
import cn.ffcs.is.mss.analyzer.utils.GetInputKafkaValue.getInputKafkaValue
import cn.ffcs.is.mss.analyzer.utils.{Constants, IniProperties, JsonUtil, TimeUtil}
import org.apache.flink.api.common.accumulators.LongCounter
import org.apache.flink.api.common.eventtime.WatermarkStrategy
import org.apache.flink.api.common.state.{ValueState, ValueStateDescriptor}
import org.apache.flink.configuration.{ConfigOptions, Configuration}
import org.apache.flink.streaming.api.functions.KeyedProcessFunction
import org.apache.flink.streaming.api.scala._
import org.apache.flink.util.Collector

import scala.collection.mutable

/*
*
 * @param null
 *@return
 *@Author
 *@Date 2020/9/11 17:24
 *@Description
 *@Update [no][date YYYY-MM-DD][name][description]
 */
object SameTimeDifferentPlaceWarn {

  def main(args: Array[String]): Unit = {
    //    val args0 = "/Users/chenwei/IdeaProjects/mss/src/main/resources/flink.ini"
    //根据传入的参数解析配置文件
    //    val confProperties = new IniProperties(args0)
    val confProperties = new IniProperties(args(0))

    //该任务的名字
    val jobName = confProperties.getValue(Constants.FLINK_SAME_TIME_DIFFERENT_PLACE_CONFIG, Constants
      .SAME_TIME_DIFFERENT_PLACE_JOB_NAME)
    //kafka Source的名字
    val kafkaSourceName = confProperties.getValue(Constants.FLINK_SAME_TIME_DIFFERENT_PLACE_CONFIG, Constants
      .SAME_TIME_DIFFERENT_PLACE_KAFKA_SOURCE_NAME)
    //mysql sink的名字
    val sqlSinkName = confProperties.getValue(Constants.FLINK_SAME_TIME_DIFFERENT_PLACE_CONFIG, Constants
      .SAME_TIME_DIFFERENT_PLACE_SQL_SINK_NAME)

    //kafka Source的并行度
    val kafkaSourceParallelism = confProperties.getIntValue(Constants.FLINK_SAME_TIME_DIFFERENT_PLACE_CONFIG,
      Constants.SAME_TIME_DIFFERENT_PLACE_KAFKA_SOURCE_PARALLELISM)
    //对数据处理的并行度
    val dealParallelism = confProperties.getIntValue(Constants.FLINK_SAME_TIME_DIFFERENT_PLACE_CONFIG, Constants
      .SAME_TIME_DIFFERENT_PLACE_DEAL_PARALLELISM)
    //写入mysql的并行度
    val sqlSinkParallelism = confProperties.getIntValue(Constants.FLINK_SAME_TIME_DIFFERENT_PLACE_CONFIG, Constants
      .SAME_TIME_DIFFERENT_PLACE_SQL_SINK_PARALLELISM)

    //kafka的服务地址
    val brokerList = confProperties.getValue(Constants.FLINK_COMMON_CONFIG, Constants.KAFKA_BOOTSTRAP_SERVERS)
    //flink消费的group.id
    val groupId = confProperties.getValue(Constants.FLINK_SAME_TIME_DIFFERENT_PLACE_CONFIG, Constants
      .SAME_TIME_DIFFERENT_PLACE_GROUP_ID)
    //kafka的topic
    val topic = confProperties.getValue(Constants.OPERATION_FLINK_TO_DRUID_CONFIG, Constants.OPERATION_TO_KAFKA_TOPIC)
    //kafka sink 的topic
    val kafkaSinkTopic = confProperties.getValue(Constants.FLINK_SAME_TIME_DIFFERENT_PLACE_CONFIG, Constants
      .SAME_TIME_DIFFERENT_KAFKA_SINK_TOPIC)
    val warningSinkTopic = confProperties.getValue(Constants.WARNING_FLINK_TO_DRUID_CONFIG, Constants
      .WARNING_TOPIC)

    //kafka sink的名字
    val kafkaSinkName = confProperties.getValue(Constants.FLINK_SAME_TIME_DIFFERENT_PLACE_CONFIG, Constants
      .SAME_TIME_DIFFERENT_KAFKA_SINK_NAME)
    //写入kafka的并行度
    val kafkaSinkParallelism = confProperties.getIntValue(Constants.FLINK_SAME_TIME_DIFFERENT_PLACE_CONFIG, Constants
      .SAME_TIME_DIFFERENT_KAFKA_SINK_PARALLELISM)


    //flink全局变量
    val parameters: Configuration = new Configuration()
    //日期配置文件的路径
    parameters.setString(Constants.DATE_CONFIG_PATH, confProperties.getValue(Constants.FLINK_COMMON_CONFIG, Constants
      .DATE_CONFIG_PATH))
    //c3p0连接池配置文件路径
    parameters.setString(Constants.c3p0_CONFIG_PATH, confProperties.getValue(Constants.FLINK_COMMON_CONFIG, Constants
      .c3p0_CONFIG_PATH))
    //允许两个省登录的时间间隔
    parameters.setLong(Constants.SAME_TIME_DIFFERENT_PROVINCE_DELTA_TIMESTAMP, confProperties.getLongValue(Constants
      .FLINK_SAME_TIME_DIFFERENT_PLACE_CONFIG, Constants.SAME_TIME_DIFFERENT_PROVINCE_DELTA_TIMESTAMP))
    //允许两个办公区登录的时间间隔
    parameters.setLong(Constants.SAME_TIME_DIFFERENT_CITY_DELTA_TIMESTAMP, confProperties.getLongValue(Constants
      .FLINK_SAME_TIME_DIFFERENT_PLACE_CONFIG, Constants.SAME_TIME_DIFFERENT_CITY_DELTA_TIMESTAMP))
    //check pointing的间隔
    val checkpointInterval = confProperties.getLongValue(Constants.FLINK_SAME_TIME_DIFFERENT_PLACE_CONFIG, Constants
      .SAME_TIME_DIFFERENT_PLACE_CHECKPOINT_INTERVAL)


    //获取ExecutionEnvironment
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    //设置check pointing的间隔
//    env.enableCheckpointing(checkpointInterval)
    //设置流的时间为EventTime
    //设置flink全局变量
    env.getConfig.setGlobalJobParameters(parameters)

    //获取kafka消费者
    val consumer = Source.kafkaSource(topic, groupId, brokerList)
    // 获取kafka数据
    val dStream = env.fromSource(consumer, WatermarkStrategy.noWatermarks(), kafkaSourceName).setParallelism(kafkaSourceParallelism)
          .uid(kafkaSourceName).name(kafkaSourceName)
    //    var path = "/Users/chenwei/Downloads/mss.1525735052963.txt"
    //    path ="/Users/chenwei/Downloads/14021053@HQ/14021053@HQ的副本.txt"
    //    val dStream = env.readTextFile(path, "iso-8859-1")


    //    ip-地点关联文件路径
    //    val placePath = confProperties.getValue(Constants.OPERATION_FLINK_TO_DRUID_CONFIG, Constants
    // .OPERATION_PLACE_PATH)
    //    host-系统名关联文件路径
    //    val systemPath = confProperties.getValue(Constants.OPERATION_FLINK_TO_DRUID_CONFIG, Constants
    // .OPERATION_SYSTEM_PATH)
    //    用户名-常用登录地关联文件路径
    //    val usedPlacePath = confProperties.getValue(Constants.OPERATION_FLINK_TO_DRUID_CONFIG, Constants
    // .OPERATION_USEDPLACE_PATH)

    //    OperationModel.setPlaceMap(placePath)
    //    OperationModel.setSystemMap(systemPath)
    //    OperationModel.setMajorMap(systemPath)
    //    OperationModel.setUsedPlacesMap(usedPlacePath)


    val sameTimeDifferentPlaceStream = dStream
      //      .map(OperationModel.getOperationModel _)
      //      .filter(_.isDefined)
      //      .map(_.head)
      //      .map(JsonUtil.toJson(_))
      .map(JsonUtil.fromJson[OperationModel] _)
      .filter(tuple => !"未知地点".equals(tuple.loginPlace) && !"匿名用户".equals(tuple.userName))
      .keyBy(_.userName)
      .process(new SameTimeDifferentPlaceWarnDeal()).setMaxParallelism(dealParallelism)

    val value: DataStream[(Object, Boolean)] = sameTimeDifferentPlaceStream.map(_._1)
    val alertKafkaValue = sameTimeDifferentPlaceStream.map(_._2)
    value.addSink(new MySQLSink).uid(sqlSinkName).name(sqlSinkName)
      .setParallelism(sqlSinkParallelism)

    //获取kafka的生产者
    val producer = Sink.kafkaSink(brokerList, kafkaSinkTopic)
    val warningProducer = Sink.kafkaSink(brokerList, warningSinkTopic)

    value
      .map(o => {
        JsonUtil.toJson(o._1.asInstanceOf[BbasSameTimeDifferentPlaceWarnEntity])
      })
      .sinkTo(producer)
      .uid(kafkaSinkName)
      .setParallelism(kafkaSinkParallelism)

    alertKafkaValue.sinkTo(warningProducer).setParallelism(sqlSinkParallelism)
    env.execute(jobName)
  }


  class SameTimeDifferentPlaceWarnDeal extends KeyedProcessFunction[String, OperationModel, ((Object, Boolean), String)] {

    lazy val firstStage: ValueState[OperationModel] = getRuntimeContext
      .getState(new ValueStateDescriptor[OperationModel]("todayLoginPlaceStage", classOf[OperationModel]))
    lazy val lastStage: ValueState[OperationModel] = getRuntimeContext
      .getState(new ValueStateDescriptor[OperationModel]("lastLoginStage", classOf[OperationModel]))

    //记录今天所有的登录地
    lazy val todayLoginPlaceStage: ValueState[(String, Long)] = getRuntimeContext
      .getState(new ValueStateDescriptor[(String, Long)]("todayLoginPlaceStage", classOf[(String, Long)]))
    //记录上次登录地
    lazy val lastLoginStage: ValueState[OperationModel] = getRuntimeContext
      .getState(new ValueStateDescriptor[OperationModel]("lastLoginStage", classOf[OperationModel]))

    var provinceDeltaTimestamp = 0L
    var cityDeltaTimestamp = 0L
    private val messagesReceived = new LongCounter()
    private val messagesSend = new LongCounter()

    override def open(parameters: Configuration): Unit = {

      getRuntimeContext.addAccumulator("SameTimeDifferentPlaceWarn: Messages received", messagesReceived)
      getRuntimeContext.addAccumulator("SameTimeDifferentPlaceWarn: Messages send", messagesSend)

      //获取全局配置
      val globConf = getRuntimeContext.getExecutionConfig.getGlobalJobParameters.asInstanceOf[Configuration]
      provinceDeltaTimestamp = globConf.getLong(ConfigOptions.key(Constants.SAME_TIME_DIFFERENT_PROVINCE_DELTA_TIMESTAMP).longType().defaultValue(21600000L))
      cityDeltaTimestamp = globConf.getLong(ConfigOptions.key(Constants.SAME_TIME_DIFFERENT_CITY_DELTA_TIMESTAMP).longType().defaultValue(10800000L))
    }


    override def processElement(value: OperationModel, ctx: KeyedProcessFunction[String, OperationModel, ((Object, Boolean), String)
    ]#Context, out: Collector[((Object, Boolean), String)]): Unit = {

      messagesReceived.add(1)

      //更新今日登录地
      val currentTodayLoginPlace: (String, Long) = todayLoginPlaceStage.value match {
        case null =>
          (value.loginPlace, value.timeStamp)
        case (todayLoginPlace, lastModified) =>
          if (TimeUtil.getDayStartTime(lastModified) != TimeUtil.getDayStartTime(value.timeStamp)) {
            (value.loginPlace, value.timeStamp)
          } else {
            (getTodayLoginPlace(value.loginPlace, todayLoginPlace), value.timeStamp)
          }
      }
      todayLoginPlaceStage.update(currentTodayLoginPlace)


      val currentLogin = value
      val lastLogin = lastLoginStage.value()
      if (lastLogin != null && currentLogin.timeStamp >= lastLogin.timeStamp) {


        //计算两次登录时间差
        val timeDifference = currentLogin.timeStamp - lastLogin.timeStamp
        //如果两次登录时间差超过配置，并且两次登录地不一样进行判断
        if (timeDifference < provinceDeltaTimestamp && !currentLogin.loginPlace.equals(lastLogin.loginPlace)) {

          val currentLoginValue = currentLogin.loginPlace.split("-")
          val lastLoginValue = lastLogin.loginPlace.split("-")
          //如果登录的省不一样，出告警
          if (!currentLoginValue(0).equals(lastLoginValue(0))) {

            val bbasSameTimeDifferentPlaceWarnEntity = new BbasSameTimeDifferentPlaceWarnEntity()
            bbasSameTimeDifferentPlaceWarnEntity.setDestinationIp(currentLogin.destinationIp)
            bbasSameTimeDifferentPlaceWarnEntity.setWarnDatetime(new Timestamp(currentLogin.timeStamp))
            bbasSameTimeDifferentPlaceWarnEntity.setLoginSystem(currentLogin.loginSystem)
            bbasSameTimeDifferentPlaceWarnEntity.setLoginPlace(currentLogin.loginPlace)
            bbasSameTimeDifferentPlaceWarnEntity.setOperation(currentLogin.operate)
            bbasSameTimeDifferentPlaceWarnEntity.setSourceIp(currentLogin.sourceIp)
            bbasSameTimeDifferentPlaceWarnEntity.setUserName(currentLogin.userName)

            bbasSameTimeDifferentPlaceWarnEntity.setLastLoginDatetime(new Timestamp(lastLogin.timeStamp))
            bbasSameTimeDifferentPlaceWarnEntity.setLastLoginPlace(lastLogin.loginPlace)
            bbasSameTimeDifferentPlaceWarnEntity.setTodayLoginPlace(currentTodayLoginPlace._1)

            val outValue = getInputKafkaValue(currentLogin, "", "同一时间多地登录", "")

            out.collect((bbasSameTimeDifferentPlaceWarnEntity, true), outValue)
            messagesSend.add(1)

          } else {
            //如果登录的省一样，但是两次的时间差小于允许两个办公区的时间差，出告警
            if (timeDifference < cityDeltaTimestamp) {
              val bbasSameTimeDifferentPlaceWarnEntity = new BbasSameTimeDifferentPlaceWarnEntity()
              bbasSameTimeDifferentPlaceWarnEntity.setDestinationIp(currentLogin.destinationIp)
              bbasSameTimeDifferentPlaceWarnEntity.setWarnDatetime(new Timestamp(currentLogin.timeStamp))
              bbasSameTimeDifferentPlaceWarnEntity.setLoginSystem(currentLogin.loginSystem)
              bbasSameTimeDifferentPlaceWarnEntity.setLoginPlace(currentLogin.loginPlace)
              bbasSameTimeDifferentPlaceWarnEntity.setOperation(currentLogin.operate)
              bbasSameTimeDifferentPlaceWarnEntity.setSourceIp(currentLogin.sourceIp)
              bbasSameTimeDifferentPlaceWarnEntity.setUserName(currentLogin.userName)

              bbasSameTimeDifferentPlaceWarnEntity.setLastLoginDatetime(new Timestamp(lastLogin.timeStamp))
              bbasSameTimeDifferentPlaceWarnEntity.setLastLoginPlace(lastLogin.loginPlace)
              bbasSameTimeDifferentPlaceWarnEntity.setTodayLoginPlace(currentTodayLoginPlace._1)

              val outValue = getInputKafkaValue(currentLogin, "", "同一时间多地登录", "")

              out.collect((bbasSameTimeDifferentPlaceWarnEntity, true), outValue)
              messagesSend.add(1)
            }
          }

        }

      }

      if (lastLogin == null || currentLogin.timeStamp >= lastLogin.timeStamp) {
        lastLoginStage.update(currentLogin)
      }

    }


    //获取当日登录地
    def getTodayLoginPlace(loginPlace: String, todayLoginPlace: String): String = {

      if (todayLoginPlace == null || todayLoginPlace.length == 0 || loginPlace.equals(todayLoginPlace)) {
        return loginPlace
      }

      val todayLoginPlaces = todayLoginPlace.split("\\|", -1)
      for (t <- todayLoginPlaces) {
        if (loginPlace.equals(t)) {
          return todayLoginPlace
        }
      }

      return todayLoginPlace + "|" + loginPlace
    }
  }

}