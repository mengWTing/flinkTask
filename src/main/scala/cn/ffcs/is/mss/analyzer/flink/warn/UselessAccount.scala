package cn.ffcs.is.mss.analyzer.flink.warn

import java.sql.Timestamp
import java.util.{Date, Properties}
import cn.ffcs.is.mss.analyzer.bean.{UselessAccountEntity, UselessAccountImproveEntity}
import cn.ffcs.is.mss.analyzer.druid.model.scala.OperationModel
import cn.ffcs.is.mss.analyzer.flink.sink.MySQLSink
import cn.ffcs.is.mss.analyzer.flink.unknowRisk.funcation.UnknownRiskUtil.getInputKafkavalue
import cn.ffcs.is.mss.analyzer.utils.{Constants, IniProperties, JsonUtil, TimeUtil}
import org.apache.flink.api.common.state.{ValueState, ValueStateDescriptor}
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.functions.{AssignerWithPunctuatedWatermarks, ProcessFunction}
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.streaming.api.watermark.Watermark
import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaConsumer, FlinkKafkaProducer}
import org.apache.flink.streaming.util.serialization.SimpleStringSchema
import org.apache.flink.util.Collector
import org.apache.flink.streaming.api.scala._

/**
 * @title UselessAccount
 * @author liangzhaosuo
 * @date 2020-12-31 9:47
 * @description 无用的账户 30天未登录
 * @update [no][date YYYY-MM-DD][name][description]
 */


object UselessAccount {
  def main(args: Array[String]): Unit = {
    val confProperties = new IniProperties(args(0))
    //该任务的名字
    val jobName = confProperties.getValue(Constants.USELESS_ACCOUNT_CONFIG, Constants
      .USELESS_ACCOUNT_JOB_NAME)
    //kafka Source的名字
    val kafkaSourceName = confProperties.getValue(Constants.USELESS_ACCOUNT_CONFIG, Constants
      .USELESS_ACCOUNT_KAFKA_SOURCE_NAME)
    //mysql sink的名字
    val sqlSinkName = confProperties.getValue(Constants.USELESS_ACCOUNT_CONFIG, Constants
      .USELESS_ACCOUNT_SQL_SINK_NAME)
    //check pointing的间隔
    val checkpointInterval = confProperties.getLongValue(Constants.USELESS_ACCOUNT_CONFIG,
      Constants.USELESS_ACCOUNT_CHECKPOINT_INTERVAL)

    //kafka Source的并行度
    val kafkaSourceParallelism = confProperties.getIntValue(Constants.USELESS_ACCOUNT_CONFIG,
      Constants.USELESS_ACCOUNT_SOURCE_PARALLELISM)
    //对数据处理的并行度
    val dealParallelism = confProperties.getIntValue(Constants.USELESS_ACCOUNT_CONFIG,
      Constants.USELESS_ACCOUNT_DEAL_PARALLELISM)
    //写入mysql的并行度
    val sqlSinkParallelism = confProperties.getIntValue(Constants.USELESS_ACCOUNT_CONFIG,
      Constants.USELESS_ACCOUNT_SINK_PARALLELISM)


    //kafka的服务地址
    val brokerList = confProperties.getValue(Constants.FLINK_COMMON_CONFIG, Constants
      .KAFKA_BOOTSTRAP_SERVERS)
    //flink消费的group.id
    val groupId = confProperties.getValue(Constants.USELESS_ACCOUNT_CONFIG, Constants
      .USELESS_ACCOUNT_GROUP_ID)
    //kafka source 的topic
    val kafkaSourceTopic = confProperties.getValue(Constants.OPERATION_FLINK_TO_DRUID_CONFIG,
      Constants.OPERATION_TO_KAFKA_TOPIC)
    val warningSinkTopic = confProperties.getValue(Constants.WARNING_FLINK_TO_DRUID_CONFIG, Constants
      .WARNING_TOPIC)
    //flink全局变量
    val parameters: Configuration = new Configuration()
    //c3p0连接池配置文件路径
    parameters.setString(Constants.c3p0_CONFIG_PATH, confProperties.getValue(Constants
      .FLINK_COMMON_CONFIG, Constants.c3p0_CONFIG_PATH))

    //文件系统类型
    parameters.setString(Constants.FILE_SYSTEM_TYPE, confProperties.getValue(Constants
      .FLINK_COMMON_CONFIG, Constants.FILE_SYSTEM_TYPE))

    //判断为无用账号的时间
    parameters.setInteger(Constants.USELESS_ACCOUNT_DECIDE_TIME_LENGTH, confProperties.getIntValue(Constants
      .USELESS_ACCOUNT_CONFIG, Constants.USELESS_ACCOUNT_DECIDE_TIME_LENGTH))


    //获取ExecutionEnvironment
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    //设置check pointing的间隔
    env.enableCheckpointing(checkpointInterval)
    //设置flink全局变量
    env.getConfig.setGlobalJobParameters(parameters)
    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)

    //设置kafka消费者相关配置
    val props = new Properties()
    //设置kafka集群地址
    props.setProperty("bootstrap.servers", brokerList)
    //设置flink消费的group.id
    props.setProperty("group.id", groupId)

    //获取kafka消费者
    val consumer = new FlinkKafkaConsumer[String](kafkaSourceTopic, new SimpleStringSchema,
      props).setStartFromGroupOffsets()

    val warningProducer = new FlinkKafkaProducer[String](brokerList, warningSinkTopic, new
        SimpleStringSchema())

    val alertValue = env.addSource(consumer).setParallelism(kafkaSourceParallelism)
      .uid(kafkaSourceName).name(kafkaSourceName)
      .map(JsonUtil.fromJson[OperationModel] _).setParallelism(dealParallelism)
      .filter(_.userName != "匿名用户").setParallelism(dealParallelism)
      .assignTimestampsAndWatermarks(new AssignerWithPunctuatedWatermarks[OperationModel] {
        override def
        checkAndGetNextWatermark(lastElement: OperationModel, extractedTimestamp: Long): Watermark =
          new Watermark(extractedTimestamp - 10000)

        override def extractTimestamp(element: OperationModel, previousElementTimestamp: Long): Long = {
          element.timeStamp
        }
      }).setParallelism(dealParallelism)
      .keyBy(_.userName)
      .process(new UselessAccountProcessFunction)

    alertValue.map(_._1).addSink(new MySQLSink).setParallelism(sqlSinkParallelism)
      .uid(sqlSinkName).name(sqlSinkName)

    alertValue.map(_._2).addSink(warningProducer).setParallelism(sqlSinkParallelism)


    env.execute(jobName)
  }

  class UselessAccountProcessFunction extends ProcessFunction[OperationModel, ((Object, Boolean), String)] {


    var decideLength: Int = _

    lazy val userAccountInfo: ValueState[OperationModel] = getRuntimeContext.getState(new
        ValueStateDescriptor[OperationModel]("operation", classOf[OperationModel]))


    override def open(parameters: Configuration): Unit = {
      val globConf = getRuntimeContext.getExecutionConfig.getGlobalJobParameters.asInstanceOf[Configuration]
      decideLength = globConf.getInteger(Constants.USELESS_ACCOUNT_DECIDE_TIME_LENGTH, 0)


    }


    override def processElement(value: OperationModel, ctx: ProcessFunction[OperationModel, ((Object, Boolean), String)
    ]#Context, out: Collector[((Object, Boolean), String)]): Unit = {
      val currentTime = value.timeStamp


      val beforeOperation = userAccountInfo.value()

      if (beforeOperation == null) {
        userAccountInfo.update(value)
        ctx.timerService().registerEventTimeTimer(currentTime + decideLength * TimeUtil.DAY_MILLISECOND)
      } else {
        val beforeTime = beforeOperation.timeStamp
        if (currentTime > beforeTime) {
          userAccountInfo.update(value)
          ctx.timerService().registerEventTimeTimer(currentTime + decideLength * TimeUtil.DAY_MILLISECOND)
        }
      }
    }

    override def onTimer(timestamp: Long, ctx: ProcessFunction[OperationModel, ((Object, Boolean), String)]#OnTimerContext,
                         out: Collector[((Object, Boolean), String)]): Unit = {

      val operationModel = userAccountInfo.value()
      val userName = operationModel.userName
      val operationTime = operationModel.timeStamp
      //      val uselessAccount = new UselessAccountEntity
      val uselessAccount = new UselessAccountImproveEntity
      uselessAccount.setCreateTime(new Timestamp(new Date().getTime))
      uselessAccount.setUserName(userName)
      uselessAccount.setLatestLoginTime(new Timestamp(operationTime))
      uselessAccount.setLastSourceIp(operationModel.sourceIp)
      uselessAccount.setLastLoginPlace(operationModel.loginPlace)
      uselessAccount.setLastLoginMajor(operationModel.loginMajor)
      uselessAccount.setLastLoginSystem(operationModel.loginSystem)
      uselessAccount.setLastDescIp(operationModel.destinationIp)
      uselessAccount.setLastUsedPlace(operationModel.usedPlace)
      uselessAccount.setLastIsDownload(operationModel.isDownload)
      uselessAccount.setLastIsDownloadSuccess(operationModel.isDownSuccess)
      uselessAccount.setLastIsRemote(operationModel.isRemote)
      uselessAccount.setLastDownloadFile(operationModel.downFileName)
      userAccountInfo.clear()
      val outValue = getInputKafkavalue(operationModel, "", "无用账户检测", "")

      out.collect((uselessAccount, false), outValue)
    }
  }

}
