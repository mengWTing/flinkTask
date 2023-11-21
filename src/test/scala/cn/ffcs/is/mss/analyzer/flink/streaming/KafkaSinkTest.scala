package cn.ffcs.is.mss.analyzer.flink.streaming

import cn.ffcs.is.mss.analyzer.flink.sink.Sink
import cn.ffcs.is.mss.analyzer.utils.{Constants, IniProperties}
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.connector.base.DeliveryGuarantee
import org.apache.flink.connector.kafka.sink.{KafkaRecordSerializationSchema, KafkaSink}
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment

/**
 * @ClassName:
 * @Author: mengwenting
 * @Date: 2023/7/27 11:38
 * @Description:
 * @update:
 */
object KafkaSinkTest {
  def main(args: Array[String]): Unit = {
        val args0 = "G:\\ffcs\\4_Code\\mss\\src\\main\\resources\\flink.ini"
        val confProperties = new IniProperties(args0)
//    val confProperties = new IniProperties(args(0))

    //kafka的服务地址
    val brokerList = confProperties.getValue(Constants.FLINK_COMMON_CONFIG, Constants
      .KAFKA_BOOTSTRAP_SERVERS)
    //kafka sink topic
    val kafkaSinkTopic = confProperties.getValue(Constants.NEW_VERSION_TEST_CONFIG,
      Constants.NEW_VERSION_TEST_CONFIG_KAFKA_SINK_TOPIC)
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    val dataStream = env.socketTextStream("192.168.1.22", 8888)

    val ds = dataStream.filter(_.length > 3)
      ds.print()

    val kafkaSink = Sink.kafkaSink(brokerList, kafkaSinkTopic)
    ds.sinkTo(kafkaSink)

    env.execute("KafkaSink")
  }
}
