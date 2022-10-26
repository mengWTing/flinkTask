package cn.ffcs.is.mss.analyzer.flink.alert

import java.util.Properties

import cn.ffcs.is.mss.analyzer.utils.{Constants, IniProperties}
import org.apache.flink.streaming.api.scala.{StreamExecutionEnvironment, _}
import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaConsumer, FlinkKafkaProducer}
import org.apache.flink.streaming.util.serialization.SimpleStringSchema
/**
 * @title AlertKafkaTokafka
 * @author hanyu
 * @date 2021-07-02 11:35
 * @description
 * @update [no][date YYYY-MM-DD][name][description]
 */
object AlertKafkaTokafka {
  def main(args: Array[String]): Unit = {
    val confProperties = new IniProperties(args(0))
    //该任务的名字
    val jobName = "ALERT_KAFKA_TO_KAFKA"
    //kafka的服务地址
    //kafka的服务地址
    val inBrokerList = confProperties.getValue(Constants.FLINK_ALERT_STATISTICS_CONFIG, Constants
      .ALERT_STATISTICS_TO_RISK_CONTROL_KAFKA_BROKER_LIST)
    val outBrokerList =confProperties.getValue(Constants.FLINK_ALERT_STATISTICS_CONFIG, Constants
      .ALERT_STATISTICS_TO_RISK_CONTROL_KAFKA_BROKER_LIST_OUT)
    val inTopic = confProperties.getValue(Constants.FLINK_ALERT_STATISTICS_CONFIG, Constants
      .ALERT_STATISTICS_TO_RISK_CONTROL_KAFKA_TOPIC)
    val outTopic = confProperties.getValue(Constants.FLINK_ALERT_STATISTICS_CONFIG, Constants
      .ALERT_STATISTICS_TO_RISK_CONTROL_KAFKA_TOPIC_OUT)

    //flink消费的group.id
    val props = new Properties()
    //设置kafka集群地址
    props.setProperty("bootstrap.servers", inBrokerList)
    //设置flink消费的group.id
    props.setProperty("group.id", "ALERT_KAFKA_TO_KAFKA")
    val consumer = new FlinkKafkaConsumer[String](inTopic, new SimpleStringSchema, props)
      .setStartFromLatest()
    val producer = new FlinkKafkaProducer[String](outBrokerList, outTopic, new SimpleStringSchema())

    //获取ExecutionEnvironment
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    //设置checkpoint
    //env.enableCheckpointing(checkpointInterval)
    env.addSource(consumer).setParallelism(1)
      .addSink(producer).setParallelism(1)
    env.execute(jobName)





  }

}

