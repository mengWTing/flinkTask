package cn.ffcs.is.mss.analyzer.flink.sink

import org.apache.flink.connector.base.DeliveryGuarantee
import org.apache.flink.connector.kafka.sink.{KafkaRecordSerializationSchema, KafkaSink}
import org.apache.flink.streaming.util.serialization.SimpleStringSchema

/**
 * @ClassName:
 * @Author: mengwenting
 * @Date: 2023/7/21 16:24
 * @Description:
 * @update:
 */
object Sink {
  def kafkaSink(brokerList: String, sinkTopic: String): KafkaSink[String] = {
    val kafkaSink: KafkaSink[String] = KafkaSink.builder[String]()
      .setBootstrapServers(brokerList)
      .setRecordSerializer(KafkaRecordSerializationSchema.builder()
        .setTopic(sinkTopic)
        .setValueSerializationSchema(new SimpleStringSchema())
        .build()
      )
      .setDeliverGuarantee(DeliveryGuarantee.AT_LEAST_ONCE)
      .build()
    kafkaSink
  }
}
