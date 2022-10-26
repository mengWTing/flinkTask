package cn.ffcs.is.mss.analyzer.flink.sink

import java.util.Properties

import cn.ffcs.is.mss.analyzer.utils.Constants
import org.apache.flink.api.common.io.RichOutputFormat
import org.apache.flink.configuration.Configuration
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}

class DataSetKafkaSink(sinkTopic: String) extends RichOutputFormat[String] {
  var producer: KafkaProducer[String, String] = _


  override def writeRecord(record: String) = {
    producer.send(new ProducerRecord[String, String](sinkTopic, System.currentTimeMillis().toString, record))
  }

  override def open(taskNumber: Int, numTasks: Int) = {
    //获取全局变量
    val globConf = getRuntimeContext.getExecutionConfig.getGlobalJobParameters
      .asInstanceOf[Configuration]
    val brokerList = globConf.getString(Constants.KAFKA_BOOTSTRAP_SERVERS, "")
    val prop = new Properties()
    prop.setProperty("bootstrap.servers", brokerList)
    prop.setProperty("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    prop.setProperty("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")

    producer = new KafkaProducer[String, String](prop)

  }

  override def configure(parameters: Configuration) = {

  }

  override def close() = {
    producer.close()

  }
}
