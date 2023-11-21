package cn.ffcs.is.mss.analyzer.flink.streaming

import org.apache.flink.api.common.eventtime.WatermarkStrategy
import org.apache.flink.connector.kafka.source.KafkaSource
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.streaming.util.serialization.SimpleStringSchema
import org.apache.flink.streaming.api.scala._

/**
 * @ClassName:
 * @Author: mengwenting
 * @Date: 2023/7/21 10:40
 * @Description:
 * @update:
 */
object NewVersionTimeTest {
  def main(args: Array[String]): Unit = {
    /**
     * 引入扩展包 ：  flink-connector-kafka
     * 从kafka中读取数据得到数据流
     */
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    val topic = "mssOperation"
    val groupId = "new-version"
    val bootStraps = "192.168.1.101:9092,192.168.1.102:9092,192.168.1.103:9092,192.168.1.104:9092,192.168.1.105:9092"

    //    val props = new Properties
    //    props.setProperty("bootstrap.servers", bootStraps)
    //    props.setProperty("group.id", groupId)
    //    val consumer = new FlinkKafkaConsumer[String]("kafkaSourceTopic", new SimpleStringSchema, props)

    val kafkaSource = KafkaSource.builder[String]()
      .setTopics(topic)
      .setGroupId(groupId)
      .setBootstrapServers(bootStraps)
      .setStartingOffsets(OffsetsInitializer.latest())
      .setValueOnlyDeserializer(new SimpleStringSchema())
      .build()
    //    val value: DataStreamSource[String] = env.addSource(consumer)

    //    val ds = env.fromSource(kafkaSource, WatermarkStrategy.noWatermarks(), "ka")
    val streamData = env.fromSource(kafkaSource,WatermarkStrategy.noWatermarks(),"mssOperation")
    streamData
      .filter(_.split("\\|", -1).length >= 33)
      .print()

    env.execute("new-version-test")
  }

}
