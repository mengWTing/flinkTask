package cn.ffcs.is.mss.analyzer.flink.druid

import java.util.Properties

import cn.ffcs.is.mss.analyzer.druid.model.scala.ArpModel
import cn.ffcs.is.mss.analyzer.utils.{Constants, IniProperties, JsonUtil}
import com.metamx.tranquility.flink.BeamSink
import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaConsumer, FlinkKafkaProducer}
import org.apache.flink.streaming.util.serialization.SimpleStringSchema
import org.apache.flink.streaming.api.scala._

import scala.collection.mutable

object ArpFlink {
  def main(args: Array[String]): Unit = {

    //根据传入的参数解析配置文件
    val confProperties = new IniProperties(args(0))

    //kafka的服务地址
    val brokerList = confProperties.getValue(Constants.FLINK_COMMON_CONFIG, Constants.KAFKA_BOOTSTRAP_SERVERS)
    //kafka的zk地址
    val kafkaZk = confProperties.getValue(Constants.FLINK_COMMON_CONFIG, Constants.KAFKA_ZOOKEEPER_CONNECT)
    //flink消费的group.id
    val groupId = confProperties.getValue(Constants.FLINK_COMMON_CONFIG, Constants.GROUP_ID)
    //druid的zk地址
    val druidZk = confProperties.getValue(Constants.FLINK_COMMON_CONFIG, Constants.TRANQUILITY_ZK_CONNECT)

    //该任务的名字
    val jobName = confProperties.getValue(Constants.ARP_FLINK_TO_DRUID_CONFIG, Constants.ARP_JOB_NAME)
    //kafka Source的名字
    val kafkaSourceName = confProperties.getValue(Constants.ARP_FLINK_TO_DRUID_CONFIG, Constants.ARP_KAFKA_SOURCE_NAME)
    //tranquility sink的名字
    val tranquilitySinkName = confProperties.getValue(Constants.ARP_FLINK_TO_DRUID_CONFIG, Constants.ARP_TRANQUILITY_SINK_NAME)
    //kafka sink的名字
    val kafkaSinkName = confProperties.getValue(Constants.ARP_FLINK_TO_DRUID_CONFIG, Constants.ARP_KAFKA_SINK_NAME)

    //话单处理的并行度
    val dealParallelism = confProperties.getIntValue(Constants.ARP_FLINK_TO_DRUID_CONFIG, Constants.ARP_DEAL_PARALLELISM)
    //check pointing的间隔
    val checkpointInterval = confProperties.getLongValue(Constants.ARP_FLINK_TO_DRUID_CONFIG, Constants.ARP_CHECKPOINT_INTERVAL)

    //该话单的topic
    val topic = confProperties.getValue(Constants.ARP_FLINK_TO_DRUID_CONFIG, Constants.ARP_TOPIC)
    //写入kafka的topic
    val toKafkaTopic = confProperties.getValue(Constants.ARP_FLINK_TO_DRUID_CONFIG, Constants.ARP_TO_KAFKA_TOPIC)

    //该话单在druid的表名
    val tableName = confProperties.getValue(Constants.FLINK_COMMON_CONFIG, Constants.DRUID_ARP_TABLE_NAME)

    //设置写入druid时需要的配置
    val conf: mutable.HashMap[String, String] = mutable.HashMap[String, String]()
    //设置话单的表名
    conf("druid.arp.source") = tableName
    //设置druid集群zookeeper集群的地址
    conf("tranquility.zk.connect") = druidZk

    //设置kafka消费者相关配置
    val props = new Properties()
    //设置kafka集群地址
    props.setProperty("bootstrap.servers", brokerList)
    //设置kafka的zookeeper集群地址
    props.setProperty("zookeeper.connect", kafkaZk)
    //设置flink消费的group.id
    props.setProperty("group.id", groupId)

    //获取kafka消费者
    val consumer = new FlinkKafkaConsumer[String](topic, new SimpleStringSchema, props)
    //获取kafka生产者
    val producer = new FlinkKafkaProducer[String](brokerList, toKafkaTopic, new SimpleStringSchema())

    //获取ExecutionEnvironment
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    //设置check pointing的间隔
    env.enableCheckpointing(checkpointInterval)

    //获取kafka数据
    val dStream = env.addSource(consumer).uid(kafkaSourceName).name(kafkaSourceName).setParallelism(dealParallelism)

    //将从kafka收到的数据包装成ArpModel
    val arpModelStream = dStream
      .map(ArpModel.getArpModel _).setParallelism(dealParallelism)
      .filter(_.isDefined).setParallelism(dealParallelism)
      .map(_.head).setParallelism(dealParallelism)

    //发送至druid
    arpModelStream.addSink(new BeamSink[ArpModel](new ArpModelBeamFactory(conf)))
      .name(tranquilitySinkName).uid(tranquilitySinkName).setParallelism(1)

    //发送至kafka
    arpModelStream.map(JsonUtil.toJson(_)).addSink(producer).uid(kafkaSinkName).name(kafkaSinkName).setParallelism(dealParallelism)

    //执行该任务
    env.execute(jobName)

  }
}