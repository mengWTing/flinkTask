package cn.ffcs.is.mss.analyzer.flink.warn

import java.sql.Timestamp
import java.util.Properties

import cn.ffcs.is.mss.analyzer.bean.NewVersionDataTestEntity
import cn.ffcs.is.mss.analyzer.druid.model.scala.OperationModel
import cn.ffcs.is.mss.analyzer.flink.sink.{MySQLSink, Sink}
import cn.ffcs.is.mss.analyzer.flink.source.Source
import cn.ffcs.is.mss.analyzer.utils.{Constants, IniProperties, JsonUtil}
import com.twitter.logging.config.BareFormatterConfig.intoOption
import org.apache.flink.api.common.eventtime.WatermarkStrategy
import org.apache.flink.api.common.functions.RichFlatMapFunction
import org.apache.flink.configuration.Configuration
import org.apache.flink.connector.kafka.source.KafkaSource
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.functions.ProcessFunction
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer
import org.apache.flink.streaming.util.serialization.SimpleStringSchema
import org.apache.flink.util.Collector
import org.apache.flink.streaming.api.scala._


/**
 * @ClassName:
 * @Author: mengwenting
 * @Date: 2023/7/19 15:13
 * @Description:
 * @update:
 */
object KafkaSourceTest {
  def main(args: Array[String]): Unit = {
    //    val args0 = "G:\\ffcs\\4_Code\\mss\\src\\main\\resources\\flink.ini"
    //    val confProperties = new IniProperties(args0)
    val confProperties = new IniProperties(args(0))

    //任务的名字
    val jobName = confProperties.getValue(Constants.NEW_VERSION_TEST_CONFIG, Constants
      .NEW_VERSION_TEST_CONFIG_JOB_NAME)
    //kafka Source的名字
    val kafkaSourceName = confProperties.getValue(Constants.NEW_VERSION_TEST_CONFIG, Constants.NEW_VERSION_TEST_CONFIG_KAFKA_SOURCE_NAME)
    //mysql sink的名字
    val sqlSinkName = confProperties.getValue(Constants.NEW_VERSION_TEST_CONFIG, Constants.NEW_VERSION_TEST_CONFIG_SQL_SINK_NAME)
    //kafka sink的名字
    val kafkaSinkName = confProperties.getValue(Constants.NEW_VERSION_TEST_CONFIG, Constants.NEW_VERSION_TEST_CONFIG_KAFKA_SINK_NAME)

    //并行度
    val sourceParallelism = confProperties.getIntValue(Constants.NEW_VERSION_TEST_CONFIG,
      Constants.NEW_VERSION_TEST_CONFIG_SOURCE_PARALLELISM)
    val sinkParallelism = confProperties.getIntValue(Constants.NEW_VERSION_TEST_CONFIG,
      Constants.NEW_VERSION_TEST_CONFIG_SINK_PARALLELISM)
    val dealParallelism = confProperties.getIntValue(Constants.NEW_VERSION_TEST_CONFIG,
      Constants.NEW_VERSION_TEST_CONFIG_DEAL_PARALLELISM)

    //check pointing的间隔
    val checkpointInterval = confProperties.getLongValue(Constants.NEW_VERSION_TEST_CONFIG,
      Constants.NEW_VERSION_TEST_CONFIG_CHECKPOINT_INTERVAL)

    //kafka的服务地址
    val brokerList = confProperties.getValue(Constants.FLINK_COMMON_CONFIG, Constants
      .KAFKA_BOOTSTRAP_SERVERS)
    //flink消费的group.id
    val groupId = confProperties.getValue(Constants.NEW_VERSION_TEST_CONFIG, Constants
      .NEW_VERSION_TEST_CONFIG_GROUP_ID)

    //kafka source 的topic
    val sourceTopic = confProperties.getValue(Constants.NEW_VERSION_TEST_CONFIG, Constants
      .NEW_VERSION_TEST_CONFIG_KAFKA_SOURCE_TOPIC)
    //kafka sink topic
    val kafkaSinkTopic = confProperties.getValue(Constants.NEW_VERSION_TEST_CONFIG,
      Constants.NEW_VERSION_TEST_CONFIG_KAFKA_SINK_TOPIC)
    //写入kafka的并行度
    val kafkaSinkParallelism = confProperties.getIntValue(Constants.NEW_VERSION_TEST_CONFIG,
      Constants.NEW_VERSION_TEST_CONFIG_SINK_PARALLELISM)

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

    //设置Kafka消费者相关配置
    val props = new Properties()
    //设置Kafka集群地址
    props.setProperty("bootstrap.servers", brokerList)
    //设置flink消费的group.id
    props.setProperty("group.id", groupId + "test")
    //获取kafka消费者
    val kafkaSource = Source.kafkaSource(sourceTopic, groupId, brokerList)
    //获取kafka生产者
    val producer = Sink.kafkaSink(brokerList, kafkaSinkTopic)

    //获取ExecutionEnvironment
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    //设置check pointing的间隔
//    env.enableCheckpointing(checkpointInterval)
    //设置Flink全局变量
    env.getConfig.setGlobalJobParameters(parameters)
//    env.getConfig.setAutoWatermarkInterval(200L)

    //获取Kafka数据流
    val dataSource = env.fromSource(kafkaSource,WatermarkStrategy.noWatermarks(),"kafkaSource")

    val dataStream = dataSource.filter(_.split("\\|", -1).length >= 33)
      .flatMap(new DataFlatMapFunction).setParallelism(dealParallelism)
      .process(new DataProcessFunction)

    val mysqlValue: DataStream[(Object, Boolean)] = dataStream.map(_._1)
    mysqlValue.addSink(new MySQLSink).uid("new version").name("new version")
      .setParallelism(sinkParallelism)

    mysqlValue
      .map(o => {JsonUtil.toJson(o._1.asInstanceOf[NewVersionDataTestEntity])})
//      .addSink(producer)
      .sinkTo(producer)
      .setParallelism(kafkaSinkParallelism)

    env.execute(jobName)
  }

  class DataFlatMapFunction extends RichFlatMapFunction[String, OperationModel]{
    override def flatMap(value: String, out: Collector[OperationModel]): Unit = {
      val operationValue = OperationModel.getOperationModel(value).get
      if (operationValue.isDefined) {
        out.collect(operationValue)
      }
    }
  }

  class DataProcessFunction extends ProcessFunction[OperationModel, ((Object, Boolean), String)] {
    override def processElement(value: OperationModel, ctx: ProcessFunction[OperationModel, ((Object, Boolean), String)]#Context,
                                out: Collector[((Object, Boolean), String)]): Unit = {
      val entity = new NewVersionDataTestEntity
      entity.setAlertTime(new Timestamp(value.timeStamp))
      entity.setUserName(value.userName)
      entity.setSourceIp(value.sourceIp)
      entity.setSourcePort(value.sourcePort)
      entity.setDestinationIp(value.destinationIp)
      entity.setDestinationPort(value.destinationPort)

      val inputKafkaValue = value.userName + "|" + "新版本测试" + "|" + value.timeStamp + "|" +
        "" + "|" + "" + "|" + "" + "|" +
        "" + "|" + value.sourceIp + "|" + value.sourcePort + "|" +
        value.destinationIp + "|" + value.destinationPort + "|" + "" + "|" +
        "" + "|" + "" + "|" + ""

      if (value.userName.nonEmpty){
        out.collect((entity, true), inputKafkaValue)
      }
    }
  }
}
