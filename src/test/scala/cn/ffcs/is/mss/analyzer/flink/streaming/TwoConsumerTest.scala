package cn.ffcs.is.mss.analyzer.flink.streaming

import cn.ffcs.is.mss.analyzer.flink.source.Source
import cn.ffcs.is.mss.analyzer.utils.{Constants, IniProperties}
import org.apache.flink.api.common.eventtime.WatermarkStrategy
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.streaming.api.scala._

/**
 * @ClassName:
 * @Author: mengwenting
 * @Date: 2023/8/28 16:34
 * @Description:
 * @update:
 */
object TwoConsumerTest {
  def main(args: Array[String]): Unit = {
    val args0 = "G:\\ffcs\\4_Code\\mss\\src\\main\\resources\\flink.ini"
    val confProperties = new IniProperties(args0)

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)

    //    //设置kafka消费者相关配置
    //    val props = new Properties()
    //kafka的服务地址
    val brokerList = confProperties.getValue(Constants.LOCAL_TWO_GROUP_CONFIG, Constants
      .LOCAL_KAFKA_BOOTSTRAP_SERVERS)
    //flink消费的group.id
    val groupId = confProperties.getValue(Constants.LOCAL_TWO_GROUP_CONFIG, Constants
      .LOCAL_DATA_GROUP_ID)
    //设置kafka集群地址
    //    props.setProperty("bootstrap.servers", brokerList)
    //设置flink消费的group.id
    //    props.setProperty("group.id", groupId)

//    val topics = new util.LinkedList[String]()
//    topics.add("dataGroupOne")
//    topics.add("dataGroupTwo")

    //获取kafka消费者
//    val kafkaSource = KafkaSource.builder[String]()
//      .setProperties(new Properties())
//      .setTopics(topics)
//      .setGroupId(groupId)
//      .setBootstrapServers(brokerList)
//      .setStartingOffsets(OffsetsInitializer.committedOffsets(OffsetResetStrategy.LATEST))
//      .setValueOnlyDeserializer(new SimpleStringSchema())
//      .build()
    val consumerOne = Source.kafkaSource("dataGroupOne", groupId, brokerList)
    val consumerTwo = Source.kafkaSource("dataGroupTwo", groupId+ "2", brokerList)

    val value1 = env.fromSource(consumerOne, WatermarkStrategy.noWatermarks(), "KafkaSource").setParallelism(1)
    val value2 = env.fromSource(consumerTwo, WatermarkStrategy.noWatermarks(), "KafkaSource").setParallelism(1)
    //    val value1 = env.addSource(consumerOne)
    //    val value2 = env.addSource(consumerTwo)
//
//        val valueAll: ConnectedStreams[String, String] = value1.connect(value2)
//        val valueLast = valueAll.map(new CoMapFunction[String, String, String] {
//          override def map1(value: String): String = {
//            val strings = value.split("\\|", -1)
//            //返回用户名字段
//            val userName = strings(0)
//            userName
//          }
//
//          override def map2(value: String): String = {
//            val strings = value.split("\\|", -1)
//            //返回地点字段
//            val place = strings(1)
//            place
//          }
//        })
//        valueLast.print()
//    //    valueAll.process(new ValueAllProcessFunction)
value1.print()
    value2.print()
    env.execute("two_topic")
  }
}
