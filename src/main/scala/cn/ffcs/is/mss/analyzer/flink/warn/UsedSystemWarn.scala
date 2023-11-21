package cn.ffcs.is.mss.analyzer.flink.warn

import java.io.{File, PrintWriter}
import java.sql.Timestamp
import java.time.Duration
import java.util.Properties

import cn.ffcs.is.mss.analyzer.bean.BbasUsedSystemWarnEntity
import cn.ffcs.is.mss.analyzer.druid.model.scala.OperationModel
import cn.ffcs.is.mss.analyzer.flink.sink.MySQLSink
import cn.ffcs.is.mss.analyzer.flink.source.Source
import cn.ffcs.is.mss.analyzer.ml.iforest.IForest
import cn.ffcs.is.mss.analyzer.utils.druid.entity._
import cn.ffcs.is.mss.analyzer.utils._
import org.apache.flink.api.common.accumulators.LongCounter
import org.apache.flink.api.common.eventtime.{SerializableTimestampAssigner, WatermarkStrategy}
import org.apache.flink.api.common.functions.Partitioner
import org.apache.flink.api.common.state.{ValueState, ValueStateDescriptor}
import org.apache.flink.configuration.{ConfigOptions, Configuration}
import org.apache.flink.streaming.api.functions.ProcessFunction
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.util.Collector

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.collection.JavaConversions._
object UsedSystemWarn {

  def main(args: Array[String]): Unit = {


//    val args0 = "/Users/chenwei/IdeaProjects/mss/src/main/resources/flink.ini"


    //根据传入的参数解析配置文件
//    val confProperties = new IniProperties(args0)
    val confProperties = new IniProperties(args(0))
    //该任务的名字
    val jobName = confProperties.getValue(Constants.FLINK_USED_SYSTEM_CONFIG, Constants.USED_SYSTEM_JOB_NAME)
    //kafka Source的名字
    val kafkaSourceName = confProperties.getValue(Constants.FLINK_USED_SYSTEM_CONFIG, Constants.USED_SYSTEM_KAFKA_SOURCE_NAME)
    //mysql sink的名字
    val sqlSinkName = confProperties.getValue(Constants.FLINK_USED_SYSTEM_CONFIG, Constants.USED_SYSTEM_SQL_SINK_NAME)

    //kafka Source的并行度
    val kafkaSourceParallelism = confProperties.getIntValue(Constants.FLINK_USED_SYSTEM_CONFIG, Constants.USED_SYSTEM_KAFKA_SOURCE_PARALLELISM)
    //对数据处理的并行度
    val dealParallelism = confProperties.getIntValue(Constants.FLINK_USED_SYSTEM_CONFIG, Constants.USED_SYSTEM_DEAL_PARALLELISM)
    //写入mysql的并行度
    val sqlSinkParallelism = confProperties.getIntValue(Constants.FLINK_USED_SYSTEM_CONFIG, Constants.USED_SYSTEM_SQL_SINK_PARALLELISM)

    //kafka的服务地址
    val brokerList = confProperties.getValue(Constants.FLINK_COMMON_CONFIG, Constants.KAFKA_BOOTSTRAP_SERVERS)
    //flink消费的group.id
    val groupId = confProperties.getValue(Constants.FLINK_USED_SYSTEM_CONFIG, Constants.USED_SYSTEM_GROUP_ID)
    //kafka的topic
    val topic = confProperties.getValue(Constants.OPERATION_FLINK_TO_DRUID_CONFIG, Constants.OPERATION_TO_KAFKA_TOPIC)


    //flink全局变量
    val parameters: Configuration = new Configuration()
    //日期配置文件的路径
    parameters.setString(Constants.DATE_CONFIG_PATH, confProperties.getValue(Constants.FLINK_COMMON_CONFIG, Constants.DATE_CONFIG_PATH))
    //c3p0连接池配置文件路径
    parameters.setString(Constants.c3p0_CONFIG_PATH, confProperties.getValue(Constants.FLINK_COMMON_CONFIG, Constants.c3p0_CONFIG_PATH))
    //check pointing的间隔
    val checkpointInterval = confProperties.getLongValue(Constants.FLINK_USED_SYSTEM_CONFIG, Constants.USED_SYSTEM_CHECKPOINT_INTERVAL)

    //druid的broker节点
    parameters.setString(Constants.DRUID_BROKER_HOST_PORT, confProperties.getValue(Constants.FLINK_COMMON_CONFIG, Constants.DRUID_BROKER_HOST_PORT))
    //druid时间格式
    parameters.setString(Constants.DRUID_TIME_FORMAT,confProperties.getValue(Constants.FLINK_COMMON_CONFIG,Constants.DRUID_TIME_FORMAT))
    //druid数据开始的时间
    parameters.setLong(Constants.DRUID_DATA_START_TIMESTAMP, confProperties.getLongValue(Constants.FLINK_COMMON_CONFIG, Constants.DRUID_DATA_START_TIMESTAMP))

    //五元组话单在druid的表名
    parameters.setString(Constants.DRUID_OPERATION_TABLE_NAME, confProperties.getValue(Constants.FLINK_COMMON_CONFIG, Constants.DRUID_OPERATION_TABLE_NAME))
    parameters.setString(Constants.USED_SYSTEM_DELTA_PER, confProperties.getValue(Constants.FLINK_USED_SYSTEM_CONFIG, Constants.USED_SYSTEM_DELTA_PER))

    //获取ExecutionEnvironment
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    //设置check pointing的间隔
//    env.enableCheckpointing(checkpointInterval)
    //设置flink全局变量
    env.getConfig.setGlobalJobParameters(parameters)

    //获取kafka消费者
    val consumer = Source.kafkaSource(topic, groupId, brokerList)
    // 获取kafka数据
    val dStream = env.fromSource(consumer, WatermarkStrategy.noWatermarks(), kafkaSourceName).setParallelism(kafkaSourceParallelism)
      .uid(kafkaSourceName).name(kafkaSourceName)
//    var path = "/Users/chenwei/Downloads/mss.1525735052963.txt"
//    path = "/Users/chenwei/Downloads/14021053@HQ/14021053@HQ.txt"
//    val dStream = env.readTextFile(path, "iso-8859-1").setParallelism(1)


    //        ip-地点关联文件路径
    val placePath = confProperties.getValue(Constants.OPERATION_FLINK_TO_DRUID_CONFIG, Constants.OPERATION_PLACE_PATH)
    //        host-系统名关联文件路径
    val systemPath = confProperties.getValue(Constants.OPERATION_FLINK_TO_DRUID_CONFIG, Constants.OPERATION_SYSTEM_PATH)
    //    用户名-常用登录地关联文件路径
    val usedPlacePath = confProperties.getValue(Constants.OPERATION_FLINK_TO_DRUID_CONFIG, Constants.OPERATION_USEDPLACE_PATH)

    OperationModel.setPlaceMap(placePath)
    OperationModel.setSystemMap(systemPath)
    OperationModel.setMajorMap(systemPath)
    OperationModel.setUsedPlacesMap(usedPlacePath)


    val usedSystemWarnStream = dStream
//      .map(OperationModel.getOperationModel _)
//      .filter(_.isDefined)
//      .map(_.head)
//      .map(JsonUtil.toJson(_))
      .map(JsonUtil.fromJson[OperationModel] _)
      .filter(operationModel => !"未知系统".equals(operationModel.loginSystem))
//      .map(operationModel => (TimeUtil.getDayStartTime(operationModel.timeStamp), operationModel.userName, mutable.Map[String,Long]((operationModel.loginSystem,operationModel.connCount))))
      .map(operationModel => (operationModel.timeStamp, operationModel.userName, mutable.Map[String,Long]((operationModel.loginSystem,operationModel.connCount))))
      .assignTimestampsAndWatermarks(
        WatermarkStrategy.forBoundedOutOfOrderness[(Long, String, mutable.Map[String,Long])](Duration.ofSeconds(10))
          .withTimestampAssigner(new SerializableTimestampAssigner[(Long, String, mutable.Map[String, Long])] {
            override def extractTimestamp(element: (Long, String, mutable.Map[String, Long]), recordTimestamp: Long): Long = {
              element._1
            }
          })
      )
      .keyBy(_._2)
      .window(TumblingEventTimeWindows.of(Time.days(1), Time.hours(0)))
      .reduce((o1,o2) =>{
        o2._3.foreach(tuple => o1._3.put(tuple._1,o1._3.getOrElseUpdate(tuple._1,0) + tuple._2))
        (o1._1,o2._2,o1._3)
      })
      .partitionCustom(new Partitioner[String] {
          def partition(key: String, numPartitions: Int): Int = {
            key.hashCode.abs % numPartitions
        }
      }, x => x._2)
      .process(new UsedPlaceDeal()).setParallelism(dealParallelism)

    usedSystemWarnStream.addSink(new MySQLSink).uid(sqlSinkName).name(sqlSinkName)
      .setParallelism(sqlSinkParallelism)

    env.execute(jobName)
  }

  class UsedPlaceDeal extends ProcessFunction[(Long, String, mutable.Map[String,Long]),(Object,Boolean)] {

    private val messagesReceived = new LongCounter()
    private val messagesSend = new LongCounter()

    var dealParallelism = 1
    var isMapRemoved = false
    var druidDateStartTime = 0L
    var lastTime = 0L
    var deltaPer = 0.3
    var tableName = ""
    //记录用户历史登录系统占比
    lazy val historyLoginStage: ValueState[mutable.Map[String,mutable.Map[Long,mutable.Map[String,Long]]]] = getRuntimeContext
      .getState(new ValueStateDescriptor[mutable.Map[String,mutable.Map[Long,mutable.Map[String,Long]]]]
      ("historyLoginStage", classOf[mutable.Map[String,mutable.Map[Long,mutable.Map[String,Long]]]]))

    //记录不用系统用户数
    lazy val userCountStage: ValueState[mutable.Map[String,Long]] = getRuntimeContext
      .getState(new ValueStateDescriptor[mutable.Map[String,Long]]("userCountStage", classOf[mutable.Map[String,Long]]))

    var historyLoginOpen: mutable.Map[String,mutable.Map[Long,mutable.Map[String,Long]]] = null
    override def open(parameters: Configuration): Unit = {
      getRuntimeContext.addAccumulator("SameTimeDifferentPlaceWarn: Messages received", messagesReceived)
      getRuntimeContext.addAccumulator("SameTimeDifferentPlaceWarn: Messages send", messagesSend)

      //获取全局配置
      val globConf = getRuntimeContext.getExecutionConfig.getGlobalJobParameters.asInstanceOf[Configuration]
      druidDateStartTime = globConf.getLong(ConfigOptions.key(Constants.DRUID_DATA_START_TIMESTAMP).longType().defaultValue(0L))
      DruidUtil.setDruidHostPortSet(globConf.getString(ConfigOptions.key(Constants.DRUID_BROKER_HOST_PORT).stringType().defaultValue("")))
      DruidUtil.setTimeFormat(globConf.getString(ConfigOptions.key(Constants.DRUID_TIME_FORMAT).stringType().defaultValue("")))
      DruidUtil.setDateStartTimeStamp(globConf.getLong(ConfigOptions.key(Constants.DRUID_DATA_START_TIMESTAMP).longType().defaultValue(0L)))

      tableName = globConf.getString(ConfigOptions.key(Constants.DRUID_OPERATION_TABLE_NAME).stringType().defaultValue(""))
      dealParallelism = globConf.getInteger(ConfigOptions.key(Constants.USED_SYSTEM_DEAL_PARALLELISM).intType().defaultValue(0))
      if(historyLoginOpen == null){
        historyLoginOpen = queryHistoryLoginConnCount(TimeUtil.getDayStartTime(System.currentTimeMillis()))
      }

      deltaPer = globConf.getDouble(ConfigOptions.key(Constants.USED_SYSTEM_DELTA_PER).doubleType().defaultValue(0.3))
    }


    override def processElement(value: (Long, String, mutable.Map[String,Long]), ctx: ProcessFunction[(Long, String, mutable.Map[String,Long]), (Object,Boolean)]#Context, out: Collector[(Object,Boolean)]): Unit = {

      messagesReceived.add(1)
      if (!isMapRemoved) {
        historyLoginOpen = historyLoginOpen.filter(tuple => {
          (tuple._1.hashCode.abs % dealParallelism) == (value._2.hashCode.abs % dealParallelism)
        })
        isMapRemoved = true
      }
      println(value)
      val userName = value._2
      val timeStamp = value._1
      val currentLogin = value._3
      val systemSet = currentLogin.keySet

      var historyLogin = historyLoginStage.value()
      if (historyLogin == null){
        historyLogin = historyLoginOpen
      }


      //如果系统用户数为空，或者执行到了下一天更新系统用户数
      var userCount = userCountStage.value()
      if (userCount == null || lastTime != timeStamp){
        userCount = querySystemLoginUserCount(timeStamp)
      }

      val todayLoginPer = getTodayLoginPer(currentLogin,systemSet)
      //判断今日访问是否异常
      if (!new IForest().predict(getHistoryLoginPerArray(historyLogin.getOrElse(userName,mutable.Map[Long,mutable.Map[String,Long]]()),systemSet),
        500,128,10,getTodayLoginPerArray(todayLoginPer,systemSet),true,false)){
        val avg = getHistoryLoginAvg(historyLogin.getOrElse(userName,mutable.Map[Long,mutable.Map[String,Long]]()),systemSet)
        systemSet.foreach(systemName =>{
          if(todayLoginPer(systemName) - avg.getOrElse(systemName,0.0) > deltaPer){

            val bbasUsedSystemWarnEntity = new BbasUsedSystemWarnEntity()
            bbasUsedSystemWarnEntity.setDayProportion(todayLoginPer(systemName))
            bbasUsedSystemWarnEntity.setHistoryProportion(avg.getOrElse(systemName,0.0).toDouble)
            bbasUsedSystemWarnEntity.setLoginSystem(systemName)
            bbasUsedSystemWarnEntity.setUserCount(userCount(systemName))
            bbasUsedSystemWarnEntity.setUserName(userName)
            bbasUsedSystemWarnEntity.setWarnDatetime(new Timestamp(timeStamp))
            out.collect((bbasUsedSystemWarnEntity,true))
            messagesSend.add(1)
          }

        })

      }

      //更新今日访问的情况
      historyLogin.getOrElseUpdate(userName,mutable.Map[Long,mutable.Map[String,Long]]()).put(timeStamp,currentLogin)
      historyLoginStage.update(historyLogin)
      lastTime = timeStamp
    }

    /**
      * 根据昨天的访问情况，获取昨天访问不同系统的占比
      * @param currentLogin
      * @param systemSet
      * @return
      */
    def getTodayLoginPer(currentLogin: mutable.Map[String,Long], systemSet: collection.Set[String]): mutable.Map[String,Double] ={
      val todayLoginPer = mutable.Map[String,Double]()
      val sum = currentLogin.values.sum
      systemSet.foreach(systemName => todayLoginPer.put(systemName,currentLogin.getOrElse(systemName,0L).toDouble / sum))

      todayLoginPer
    }


    /**
      * 根据昨天的访问情况，获取昨天访问不同系统的占比
      * @param todayLoginPer
      * @return
      */
    def getTodayLoginPerArray(todayLoginPer: mutable.Map[String,Double], systemSet: collection.Set[String]): Array[Double] ={
      val arrayBuffer = ArrayBuffer[Double]()
      systemSet.foreach(systemName => arrayBuffer.append(todayLoginPer.getOrElse(systemName,0.0)))
      arrayBuffer.toArray
    }


    /**
      * 根据历史的访问情况，获取历史每天访问不同系统的占比
      * @param userHistoryLogin
      * @param systemSet
      * @return
      */
    def getHistoryLoginPerArray(userHistoryLogin: mutable.Map[Long,mutable.Map[String,Long]],
                                systemSet: collection.Set[String]): Array[Array[Double]] ={
      val arrayBuffer = ArrayBuffer[Array[Double]]()

      userHistoryLogin.foreach(tuple => {
        val arrayBufferTemp = ArrayBuffer[Double]()
        val sum = tuple._2.values.sum
        systemSet.foreach(systemName => arrayBufferTemp.append(tuple._2.getOrElse(systemName,0L).toDouble / sum))
        arrayBuffer.append(arrayBufferTemp.toArray)
      })

      arrayBuffer.toArray
    }

    /**
      * 根据历史的访问情况，获取历史访问不同系统占比的均值
      * @param userHistoryLogin
      * @param systemSet
      * @return
      */
    def getHistoryLoginAvg(userHistoryLogin: mutable.Map[Long,mutable.Map[String,Long]],
                                systemSet: collection.Set[String]): mutable.Map[String,Double] ={
      val sumMap = mutable.Map[String,Long]()
      val countMap = mutable.Map[String,Long]()
      userHistoryLogin.foreach(tuple => {
        systemSet.foreach(systemName =>{
          sumMap.put(systemName, sumMap.getOrElse(systemName, 0L) + tuple._2.getOrElse(systemName,0L))
          countMap.put(systemName, countMap.getOrElse(systemName, 0L) + (if(tuple._2.contains(systemName)) 1 else 0))
        })
      })

      val avg = mutable.Map[String,Double]()
      systemSet.foreach(systemName => avg.put(systemName,sumMap(systemName).toDouble / countMap(systemName)))

      return avg

    }
    /**
      * 查询druid获取每个系统的用户数
      * @param timeStamp
      * @return
      */
    def querySystemLoginUserCount(timeStamp: Long):mutable.Map[String,Long] = {
      val userCount = mutable.Map[String,Long]()
      val queryResult = DruidUtil.query(getQuerySystemLoginUserCountEntity(timeStamp))

      queryResult.foreach(tuple =>
        userCount.put(tuple(Dimension.loginSystem.toString),tuple(Aggregation.userNameCount.toString).toDouble.toLong))

      userCount
    }

    /**
      * 获取每个系统用户数的entity
      * @param timeStamp
      * @return
      */
    def getQuerySystemLoginUserCountEntity(timeStamp : Long): Entity ={

      val entity = new Entity()
      entity.setTableName(tableName)
      entity.setDimensionsSet(Dimension.getDimensionsSet(Dimension.loginSystem))
      entity.setAggregationsSet(Aggregation.getAggregationsSet(Aggregation.userNameCount))
      entity.setGranularity(Granularity.getGranularity(Granularity.periodDay,1))
      entity.setStartTimeStr(timeStamp)
      entity.setEndTimeStr(timeStamp + TimeUtil.DAY_MILLISECOND)

      entity
    }

    /**
      * 查询历史每天每个用户访问不同系统的次数
      * 考虑到一次查所以历史可能会无法返回结果，所以采取一天一天查的方法
      * @param timeStamp
      */
    def queryHistoryLoginConnCount(timeStamp : Long): mutable.Map[String,mutable.Map[Long,mutable.Map[String,Long]]] = {

      val userHistoryLogin = mutable.Map[String,mutable.Map[Long,mutable.Map[String,Long]]]()
      for (time <- timeStamp until druidDateStartTime by TimeUtil.DAY_MILLISECOND * -1) {

        val queryResult = DruidUtil.query(getQueryHistoryLoginConnCountEntity(time))
        queryResult.foreach(map => {
          val userName = map.getOrElse(Dimension.userName.toString, "")
          val loginSystem = map.getOrElse(Dimension.loginSystem.toString, "")
          val connCount = map.getOrElse(Aggregation.connCount.toString, "0").toLong
          val loginTime = map.getOrElse(Entity.timestamp, "0").toLong
          userHistoryLogin.getOrElseUpdate(userName, mutable.Map[Long, mutable.Map[String, Long]]())
            .getOrElseUpdate(loginTime, mutable.Map[String, Long]())
            .getOrElseUpdate(loginSystem, connCount)

        })
        val write = new PrintWriter(new File("hdfs://A5-302-HW-XH628-027:8020/user/hive/warehouse/mssflow"))
        write.println(time)
        write.close()
      }

      userHistoryLogin
    }

    /**
      * 获取历史每天每个用户访问不同系统次数的entity
      * 考虑到一次查所以历史可能会无法返回结果，所以采取一天一天查的方法
      * @param timeStamp
      * @return
      */
    def getQueryHistoryLoginConnCountEntity(timeStamp : Long): Entity = {
      val entity = new Entity()
      entity.setDimensionsSet(Dimension.getDimensionsSet(Dimension.loginSystem,Dimension.userName))
      entity.setAggregationsSet(Aggregation.getAggregationsSet(Aggregation.connCount))
      entity.setTableName(tableName)
      entity.setGranularity(Granularity.getGranularity(Granularity.periodDay,1))
      entity.setStartTimeStr(timeStamp - TimeUtil.DAY_MILLISECOND)
      entity.setEndTimeStr(timeStamp)

      entity
    }

  }


}
