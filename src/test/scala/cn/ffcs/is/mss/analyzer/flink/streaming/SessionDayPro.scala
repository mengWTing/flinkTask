package cn.ffcs.is.mss.analyzer.flink.streaming

import cn.ffcs.is.mss.analyzer.utils.{Constants, IniProperties}
import org.apache.flink.api.common.functions.MapFunction
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.api.scala._

/**
 * @ClassName:
 * @Author: mengwenting
 * @Date: 2023/11/20 15:37
 * @Description:
 * @update:
 */
object SessionDayPro {
  def main(args: Array[String]): Unit = {
    val args0 = "G:\\ffcs\\4_Code\\mss\\src\\main\\resources\\flink.ini"
    val confProperties = new IniProperties(args0)

    //任务的名字
    val jobName = confProperties.getValue(Constants.BASTION_SESSION_DAY_COUNT_ANALYSE_CONFIG, Constants
      .BASTION_SESSION_DAY_COUNT_ANALYSE_CONFIG_JOB_NAME)
    //kafka Source的名字
    val kafkaSourceName = confProperties.getValue(Constants.BASTION_SESSION_DAY_COUNT_ANALYSE_CONFIG,
      Constants.BASTION_SESSION_DAY_COUNT_ANALYSE_CONFIG_KAFKA_SOURCE_NAME)
    //mysql sink的名字
    val sqlSinkName = confProperties.getValue(Constants.BASTION_SESSION_DAY_COUNT_ANALYSE_CONFIG,
      Constants.BASTION_SESSION_DAY_COUNT_ANALYSE_CONFIG_SQL_SINK_NAME)
    //kafka sink的名字
    val kafkaSinkName = confProperties.getValue(Constants.BASTION_SESSION_DAY_COUNT_ANALYSE_CONFIG,
      Constants.BASTION_SESSION_DAY_COUNT_ANALYSE_CONFIG_KAFKA_SINK_NAME)

    //并行度
    val sourceParallelism = confProperties.getIntValue(Constants.BASTION_SESSION_DAY_COUNT_ANALYSE_CONFIG,
      Constants.BASTION_SESSION_DAY_COUNT_ANALYSE_CONFIG_SOURCE_PARALLELISM)
    val sinkParallelism = confProperties.getIntValue(Constants.BASTION_SESSION_DAY_COUNT_ANALYSE_CONFIG,
      Constants.BASTION_SESSION_DAY_COUNT_ANALYSE_CONFIG_SINK_PARALLELISM)
    val dealParallelism = confProperties.getIntValue(Constants.BASTION_SESSION_DAY_COUNT_ANALYSE_CONFIG,
      Constants.BASTION_SESSION_DAY_COUNT_ANALYSE_CONFIG_DEAL_PARALLELISM)

    //check pointing的间隔
    val checkpointInterval = confProperties.getLongValue(Constants.BASTION_SESSION_DAY_COUNT_ANALYSE_CONFIG,
      Constants.BASTION_SESSION_DAY_COUNT_ANALYSE_CONFIG_CHECKPOINT_INTERVAL)

    //kafka的服务地址
    val brokerList = confProperties.getValue(Constants.FLINK_STREAM_PARK_COMMON_CONFIG, Constants
      .KAFKA_BOOTSTRAP_SERVERS_TEST_ENV)
    //flink消费的group.id
    val groupId = confProperties.getValue(Constants.BASTION_SESSION_DAY_COUNT_ANALYSE_CONFIG, Constants
      .BASTION_SESSION_DAY_COUNT_ANALYSE_CONFIG_GROUP_ID)

    //kafka source 的topic
    val sourceTopic = confProperties.getValue(Constants.BASTION_SESSION_DAY_COUNT_ANALYSE_CONFIG, Constants
      .BASTION_SESSION_DAY_COUNT_ANALYSE_CONFIG_KAFKA_SOURCE_TOPIC)
    //kafka sink topic
    val kafkaSinkTopic = confProperties.getValue(Constants.BASTION_SESSION_DAY_COUNT_ANALYSE_CONFIG,
      Constants.BASTION_SESSION_DAY_COUNT_ANALYSE_CONFIG_KAFKA_SINK_TOPIC)
    //写入kafka的并行度
    val kafkaSinkParallelism = confProperties.getIntValue(Constants.BASTION_SESSION_DAY_COUNT_ANALYSE_CONFIG,
      Constants.BASTION_SESSION_DAY_COUNT_ANALYSE_CONFIG_SINK_PARALLELISM)

    //flink全局变量
    val parameters: Configuration = new Configuration()
    //获取kafka消费者
    //    val kafkaSource = Source.kafkaSource(sourceTopic, groupId, brokerList)
    //获取kafka生产者
    //    val producer = Sink.kafkaSink(brokerList, kafkaSinkTopic)

    //获取ExecutionEnvironment
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    //设置check pointing的间隔
    //    env.enableCheckpointing(checkpointInterval)
    //设置Flink全局变量
    env.getConfig.setGlobalJobParameters(parameters)
    //flink1.14默认事件时间
    //获取Kafka数据流
    //    val dataSource = env.fromSource(kafkaSource,WatermarkStrategy.noWatermarks(),"kafkaSource")
//    val dataSource = env.socketTextStream("192.168.10.103",8888).setParallelism(1)

//    dataSource.map(new sessionMapFunction)
  }

//  class sessionMapFunction extends MapFunction[String, ()]

}
