package cn.ffcs.is.mss.analyzer.flink.druid

import java.util.Properties

import cn.ffcs.is.mss.analyzer.druid.model.scala.QuintetModel
import cn.ffcs.is.mss.analyzer.utils.{Constants, IniProperties, JsonUtil}
import com.metamx.tranquility.flink.BeamSink
import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaConsumer, FlinkKafkaProducer}
import org.apache.flink.streaming.util.serialization.SimpleStringSchema
import org.apache.flink.streaming.api.scala._
import org.apache.kafka.clients.producer.ProducerConfig

import scala.collection.mutable

object QuintetFlink {
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
    val jobName = confProperties.getValue(Constants.QUINTET_FLINK_TO_DRUID_CONFIG, Constants.QUINTET_JOB_NAME)
    //kafka Source的名字
    val kafkaSourceName = confProperties.getValue(Constants.QUINTET_FLINK_TO_DRUID_CONFIG, Constants
      .QUINTET_KAFKA_SOURCE_NAME)
    //tranquility sink的名字
    val tranquilitySinkName = confProperties.getValue(Constants.QUINTET_FLINK_TO_DRUID_CONFIG, Constants
      .QUINTET_TRANQUILITY_SINK_NAME)
    //kafka sink的名字
    val kafkaSinkName = confProperties.getValue(Constants.QUINTET_FLINK_TO_DRUID_CONFIG, Constants
      .QUINTET_KAFKA_SINK_NAME)

    //话单处理的并行度
    val dealParallelism = confProperties.getIntValue(Constants.QUINTET_FLINK_TO_DRUID_CONFIG, Constants
      .QUINTET_DEAL_PARALLELISM)
    //check pointing的间隔
    val checkpointInterval = confProperties.getLongValue(Constants.QUINTET_FLINK_TO_DRUID_CONFIG, Constants
      .QUINTET_CHECKPOINT_INTERVAL)

    //该话单的topic
    val topic = confProperties.getValue(Constants.QUINTET_FLINK_TO_DRUID_CONFIG, Constants.QUINTET_TOPIC)
    //写入kafka的topic
    val toKafkaTopic = confProperties.getValue(Constants.QUINTET_FLINK_TO_DRUID_CONFIG, Constants
      .QUINTET_TO_KAFKA_TOPIC)

    //该话单在druid的表名
    val tableName = confProperties.getValue(Constants.FLINK_COMMON_CONFIG, Constants.DRUID_QUINTET_TABLE_NAME)

    //设置写入druid时需要的配置
    val conf: mutable.HashMap[String, String] = mutable.HashMap[String, String]()
    //设置话单的表名
    conf("druid.quintet.source") = tableName
    //设置druid集群zookeeper集群的地址
    conf("tranquility.zk.connect") = druidZk

    //设置kafka消费者相关配置
    val props = new Properties()
    //设置kafka集群地址
    props.setProperty("bootstrap.servers", brokerList)
    //设置kafka的zookeeper集群地址
    //    props.setProperty("zookeeper.connect", kafkaZk)
    //设置flink消费的group.id
    props.setProperty("group.id", groupId)
    //props.setProperty("request.timeout.ms", "120000")
    props.setProperty("request.timeout.ms", "600000")
    props.setProperty(ProducerConfig.MAX_BLOCK_MS_CONFIG, "600000")
    props.setProperty(ProducerConfig.BUFFER_MEMORY_CONFIG, "67108864")

    //获取kafka消费者
    val consumer = new FlinkKafkaConsumer[String](topic, new SimpleStringSchema, props)
    //获取kafka生产者
    val producer = new FlinkKafkaProducer[String](toKafkaTopic, new SimpleStringSchema(), props)

    //获取ExecutionEnvironment
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    //设置check pointing的间隔
    env.enableCheckpointing(checkpointInterval)

    //获取kakfa数据
    val dStream = env.addSource(consumer).name(kafkaSourceName).uid(kafkaSourceName).setParallelism(dealParallelism)

    //将从kafka收到的数据包装成QuintetModel
    val quintetModelStream = dStream
      .map(QuintetModel.getQuintetModel _).setParallelism(dealParallelism)
      .filter(_.isDefined).setParallelism(dealParallelism)
      .map(_.head).setParallelism(dealParallelism)

    //发送至druid
    quintetModelStream.addSink(new BeamSink[QuintetModel](new QuintetModelBeamFactory(conf)))
      .name(tranquilitySinkName).uid(tranquilitySinkName).setParallelism(1)

    //发送至kafka
    quintetModelStream.map(JsonUtil.toJson(_)).addSink(producer).uid(kafkaSinkName).name(kafkaSinkName)
      .setParallelism(3)

    //执行该任务
    env.execute(jobName)

  }
}