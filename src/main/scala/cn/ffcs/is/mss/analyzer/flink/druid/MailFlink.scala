package cn.ffcs.is.mss.analyzer.flink.druid

import java.util.Properties

import cn.ffcs.is.mss.analyzer.druid.model.scala.MailModel
import cn.ffcs.is.mss.analyzer.utils.{Constants, IniProperties, JsonUtil}
import org.apache.flink.streaming.api.scala.{StreamExecutionEnvironment, _}
import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaConsumer, FlinkKafkaProducer}
import org.apache.flink.streaming.util.serialization.SimpleStringSchema

/**
 * @title MailFlink
 * @author liangzhaosuo
 * @date 2020-11-19 17:57
 * @description 将数据写入kafka->德鲁伊
 * @update [no][date YYYY-MM-DD][name][description]
 */


object MailFlink {
  def main(args: Array[String]): Unit = {
    val confProperties = new IniProperties(args(0))

    //读取的kafka的服务地址
    val fromBrokerList = confProperties.getValue(Constants.MAIL_FLINK_TO_DRUID_CONFIG, Constants
      .MAIL_END_TO_END_KAFKA_SERVER)
    //写出的kafka的服务地址
    val toBrokerList = confProperties.getValue(Constants.FLINK_COMMON_CONFIG, Constants.KAFKA_BOOTSTRAP_SERVERS)

    //flink消费的group.id
    val conSumergroupId = confProperties.getValue(Constants.MAIL_FLINK_TO_DRUID_CONFIG, Constants.MAIL_KAFKA_SOURCE_GROUPID)
    val produceGroupId = confProperties.getValue(Constants.MAIL_FLINK_TO_DRUID_CONFIG, Constants.MAIL_KAFKA_SOURCE_GROUPID)
    //druid的zk地址
    val druidZk = confProperties.getValue(Constants.FLINK_COMMON_CONFIG, Constants.TRANQUILITY_ZK_CONNECT)

    //该任务的名字
    val jobName = confProperties.getValue(Constants.MAIL_FLINK_TO_DRUID_CONFIG, Constants.MAIL_JOB_NAME)
    //kafka Source的名字
    val kafkaSourceName = confProperties.getValue(Constants.MAIL_FLINK_TO_DRUID_CONFIG, Constants
      .MAIL_KAFKA_SOURCE_NAME)
    //tranquility sink的名字
    val tranquilitySinkName = confProperties.getValue(Constants.MAIL_FLINK_TO_DRUID_CONFIG, Constants
      .MAIL_TRANQUILITY_SINK_NAME)
    //kafka sink的名字
    val kafkaSinkName = confProperties.getValue(Constants.MAIL_FLINK_TO_DRUID_CONFIG, Constants
      .MAIL_KAFKA_SINK_NAME)

    //处理的并行度
    val dealParallelism = confProperties.getIntValue(Constants.MAIL_FLINK_TO_DRUID_CONFIG, Constants
      .MAIL_DEAL_PARALLELISM)
    //check pointing的间隔
    val checkpointInterval = confProperties.getLongValue(Constants.MAIL_FLINK_TO_DRUID_CONFIG, Constants
      .MAIL_CHECKPOINT_INTERVAL)

    //topic
    val topic = confProperties.getValue(Constants.MAIL_FLINK_TO_DRUID_CONFIG, Constants.MAIL_TOPIC)
    //写入kafka的topic
    val toKafkaTopic = confProperties.getValue(Constants.MAIL_FLINK_TO_DRUID_CONFIG, Constants
      .MAIL_TO_KAFKA_TOPIC)

    //kafka消费者相关配置
    val propsFrom = new Properties()
    //设置kafka集群地址
    propsFrom.setProperty("bootstrap.servers", fromBrokerList)
    //设置flink消费的group.id
    propsFrom.setProperty("group.id", conSumergroupId)

    //写出kafka的配置
    val propsTo = new Properties()
    //设置kafka集群地址
    propsTo.setProperty("bootstrap.servers", toBrokerList)
    propsTo.setProperty("group.id", produceGroupId)

    //获取kafka消费者
    val consumer = new FlinkKafkaConsumer[String](topic, new SimpleStringSchema, propsFrom).setStartFromGroupOffsets()
    //获取kafka生产者
    val producer = new FlinkKafkaProducer[String](toKafkaTopic, new SimpleStringSchema(), propsTo)

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.addSource(consumer).setParallelism(3)
      .map(MailModel.getMailModel _).setParallelism(3)
      .filter(_.isDefined).setParallelism(3)
      .map(_.head).setParallelism(3)
      .map(JsonUtil.toJson(_)).setParallelism(3)
      .addSink(producer).setParallelism(3)

    env.execute(jobName)
  }
}
