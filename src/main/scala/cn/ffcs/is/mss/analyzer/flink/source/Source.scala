package cn.ffcs.is.mss.analyzer.flink.source

import java.util.Properties

import org.apache.flink.api.common.serialization.{DeserializationSchema, SerializationSchema}
import org.apache.flink.connector.kafka.source.KafkaSource
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer
import org.apache.flink.api.common.typeinfo.BasicTypeInfo
import org.apache.flink.api.common.typeinfo.TypeInformation
import java.nio.charset.StandardCharsets

import org.apache.flink.streaming.util.serialization.SimpleStringSchema

/**
 * @ClassName:
 * @Author: mengwenting
 * @Date: 2023/7/20 15:31
 * @Description:
 * @update:
 */
object Source {
  def kafkaSource(sourceTopic: String, groupId: String, brokerList: String): KafkaSource[String] = {
    val kafkaSource = KafkaSource.builder[String]()
      // Kafka消费者的各种配置文件，此处省略配置
      .setProperties(new Properties())
      .setTopics(sourceTopic)
      .setGroupId(groupId)
      .setBootstrapServers(brokerList)
      .setStartingOffsets(OffsetsInitializer.earliest())
      .setValueOnlyDeserializer(new SimpleStringSchema())
      .build()
    kafkaSource
  }
}
