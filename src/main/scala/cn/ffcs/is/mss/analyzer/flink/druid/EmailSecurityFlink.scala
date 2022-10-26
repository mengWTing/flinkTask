package cn.ffcs.is.mss.analyzer.flink.druid

import java.time.ZoneId
import java.util.Properties

import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.streaming.api.scala.{StreamExecutionEnvironment, _}
import org.apache.flink.streaming.connectors.fs.StringWriter
import org.apache.flink.streaming.connectors.fs.bucketing.{BucketingSink, DateTimeBucketer}
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer

/**
 * @title EmailSecurityFlink
 * @author ZF
 * @date 2020-11-12 9:22
 * @description 将 email数据 kafka数据写入Hdfs
 * @update [no][date YYYY-MM-DD][name][description]
 */
object EmailSecurityFlink {
  def main(args: Array[String]): Unit = {
    val topic = args(0)
    val groupId = args(1)
    val formatStrings = args(2)
    //设置kafka消费者相关配置
    val props = new Properties()
    //设置kafka集群地址
    props.setProperty("bootstrap.servers", "10.140.50.130:9092,10.140.50.131:9092,10.140.50.132:9092," +
      "10.140.50.133:9092,10.140.50.134:9092")
    //设置flink消费的group.id
    props.setProperty("group.id", groupId)
    props.setProperty("auto.offset.reset", "earliest")

    val consumer = new FlinkKafkaConsumer[String](topic, new SimpleStringSchema(), props)
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    val dStream = env.addSource(consumer).setParallelism(3)



    val fileSink = new BucketingSink[String]("/eMailSecurityShuJu")
    fileSink.setBucketer(new DateTimeBucketer(formatStrings, ZoneId.of("Asia/Shanghai")))
    fileSink.setWriter(new StringWriter[String]())
    fileSink.setBatchSize(1024 * 1024 * 128) // 当一个桶部分文件大于这个大小时，将启动一个新的桶部分文件
    fileSink.setInactiveBucketCheckInterval(5 * 60 * 1000)//设置不活动桶的检查间隔的默认时间。


//    val fileSink = StreamingFileSink
//      .forRowFormat(new Path("hdfs:///eMailSecurityShuJu"), new SimpleStringEncoder[String]("UTF-8"))
//      .withRollingPolicy(
//        DefaultRollingPolicy.create()
//          .withRolloverInterval(TimeUnit.MINUTES.toMillis(5))
//          .withInactivityInterval(TimeUnit.MINUTES.toMillis(5))
//          .withMaxPartSize(128 * 1024 * 1024)
//          .build())
//      .build()

    dStream.addSink(fileSink).setParallelism(1)
    env.execute("EmailSecurityFlink")

  }
}
