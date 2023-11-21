package cn.ffcs.is.mss.analyzer.flink.streaming

import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.time.{Duration, LocalDateTime}
import java.time.format.DateTimeFormatter
import java.util.Date

import cn.ffcs.is.mss.analyzer.flink.sink.Sink
import cn.ffcs.is.mss.analyzer.flink.source.Source
import cn.ffcs.is.mss.analyzer.utils.{Constants, IniProperties, JsonUtil}
import com.twitter.conversions.time
import org.apache.flink.api.common.eventtime.{SerializableTimestampAssigner, WatermarkStrategy}
import org.apache.flink.api.common.functions.MapFunction
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}
import org.apache.flink.api.scala._
import org.apache.flink.streaming.api.functions.ProcessFunction
import org.apache.flink.streaming.api.windowing.assigners.SlidingEventTimeWindows
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.util.Collector
import org.json.JSONObject



/**
 * @ClassName:
 * @Author: mengwenting
 * @Date: 2023/11/17 15:14
 * @Description:
 * @update:
 */
object SessionCountDay {
  def main(args: Array[String]): Unit = {
        val args0 = "G:\\ffcs\\4_Code\\mss\\src\\main\\resources\\flink.ini"
        val confProperties = new IniProperties(args0)

    //任务的名字
    val jobName = confProperties.getValue(Constants.BASTION_SESSION_DAY_COUNT_ANALYSE_CONFIG, Constants
      .BASTION_SESSION_DAY_COUNT_ANALYSE_CONFIG_JOB_NAME)
    //kafka Source的名字
    val kafkaSourceName = confProperties.getValue(Constants.BASTION_SESSION_DAY_COUNT_ANALYSE_CONFIG,
      Constants.BASTION_SESSION_DAY_COUNT_ANALYSE_CONFIG_KAFKA_SOURCE_NAME)
    //mysql sink的名字
    val sqlSinkName = confProperties.getValue(Constants.BASTION_SESSION_DAY_COUNT_ANALYSE_CONFIG,
      Constants.BASTION_SESSION_DAY_COUNT_ANALYSE_CONFIG_SQL_SINK_NAME)
    //kafka sink的名字
    val kafkaSinkName = confProperties.getValue(Constants.BASTION_SESSION_DAY_COUNT_ANALYSE_CONFIG,
      Constants.BASTION_SESSION_DAY_COUNT_ANALYSE_CONFIG_KAFKA_SINK_NAME)

    //并行度
    val sourceParallelism = confProperties.getIntValue(Constants.BASTION_SESSION_DAY_COUNT_ANALYSE_CONFIG,
      Constants.BASTION_SESSION_DAY_COUNT_ANALYSE_CONFIG_SOURCE_PARALLELISM)
    val sinkParallelism = confProperties.getIntValue(Constants.BASTION_SESSION_DAY_COUNT_ANALYSE_CONFIG,
      Constants.BASTION_SESSION_DAY_COUNT_ANALYSE_CONFIG_SINK_PARALLELISM)
    val dealParallelism = confProperties.getIntValue(Constants.BASTION_SESSION_DAY_COUNT_ANALYSE_CONFIG,
      Constants.BASTION_SESSION_DAY_COUNT_ANALYSE_CONFIG_DEAL_PARALLELISM)

    //check pointing的间隔
    val checkpointInterval = confProperties.getLongValue(Constants.BASTION_SESSION_DAY_COUNT_ANALYSE_CONFIG,
      Constants.BASTION_SESSION_DAY_COUNT_ANALYSE_CONFIG_CHECKPOINT_INTERVAL)

    //kafka的服务地址
    val brokerList = confProperties.getValue(Constants.FLINK_STREAM_PARK_COMMON_CONFIG, Constants
      .KAFKA_BOOTSTRAP_SERVERS_TEST_ENV)
    //flink消费的group.id
    val groupId = confProperties.getValue(Constants.BASTION_SESSION_DAY_COUNT_ANALYSE_CONFIG, Constants
      .BASTION_SESSION_DAY_COUNT_ANALYSE_CONFIG_GROUP_ID)

    //kafka source 的topic
    val sourceTopic = confProperties.getValue(Constants.BASTION_SESSION_DAY_COUNT_ANALYSE_CONFIG, Constants
      .BASTION_SESSION_DAY_COUNT_ANALYSE_CONFIG_KAFKA_SOURCE_TOPIC)
    //kafka sink topic
    val kafkaSinkTopic = confProperties.getValue(Constants.BASTION_SESSION_DAY_COUNT_ANALYSE_CONFIG,
      Constants.BASTION_SESSION_DAY_COUNT_ANALYSE_CONFIG_KAFKA_SINK_TOPIC)
    //写入kafka的并行度
    val kafkaSinkParallelism = confProperties.getIntValue(Constants.BASTION_SESSION_DAY_COUNT_ANALYSE_CONFIG,
      Constants.BASTION_SESSION_DAY_COUNT_ANALYSE_CONFIG_SINK_PARALLELISM)

    //flink全局变量
    val parameters: Configuration = new Configuration()
    //获取kafka消费者
//    val kafkaSource = Source.kafkaSource(sourceTopic, groupId, brokerList)
    //获取kafka生产者
//    val producer = Sink.kafkaSink(brokerList, kafkaSinkTopic)

    //获取ExecutionEnvironment
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    //设置check pointing的间隔
    //    env.enableCheckpointing(checkpointInterval)
    //设置Flink全局变量
    env.getConfig.setGlobalJobParameters(parameters)
    //flink1.14默认事件时间
    //获取Kafka数据流
//    val dataSource = env.fromSource(kafkaSource,WatermarkStrategy.noWatermarks(),"kafkaSource")
    val dataSource = env.socketTextStream("192.168.10.103",8888).setParallelism(1)
    dataSource
      .map(new SessionDataMapFunction).setParallelism(dealParallelism)
      .assignTimestampsAndWatermarks(
        WatermarkStrategy.forBoundedOutOfOrderness[(Long, Long)](Duration.ofSeconds(10))
          .withTimestampAssigner(new SerializableTimestampAssigner[(Long, Long)] {
            override def extractTimestamp(element: (Long, Long), recordTimestamp: Long): Long = {
              element._1
            }
          })
      ).setParallelism(dealParallelism)
      .keyBy(x=> x._1)
      .window(SlidingEventTimeWindows.of(Time.days(1), Time.days(1)))
      .reduce((o1, o2) => (o1._1, o1._2+o2._2))
      .process(new sessionDayCountProcessFunction)
      .print()
    env.execute()
//
//    val mysqlValue: DataStream[(Object, Boolean)] = dataStream.map(_._1)
//    mysqlValue.addSink(new MySQLSink).uid("new version").name("new version")
//      .setParallelism(sinkParallelism)

    //输出样例:
    //2023-11-17:1000 2023-11-18:2000 2023-11-19:3000 格式放在表里日期:会话总数
    //一条日志为一个会话
  }

  //将String类型时间转为Long类型
  class SessionDataMapFunction extends MapFunction[String, (Long, Long)]{ //时间戳,数量1
    override def map(value: String): (Long, Long) = {
      val jsonObj: JSONObject = new JSONObject(value)
      val timeStampStr: String = jsonObj.getString("timeStamp") //"2023-11-09 19:23:59"
      val dateStr = timeStampStr.split(" ")(0) //"2023-11-09"
      var ts = new SimpleDateFormat("yyyy-MM-dd").parse(dateStr).getTime //1699459200000
      val tsFinal = ts + 1000 //水位线左闭右开 多1s
//      val dtTime = new Timestamp(ts) //2023-11-09 00:00:00.0 Timestamp类型
      //      val dateStrReplaced = dateStr.replaceAll("-", "")
      val count = 1
      (tsFinal, count)
    }
  }

  class sessionDayCountProcessFunction extends ProcessFunction[(Long, Long), String] {
    override def processElement(value: (Long, Long), ctx: ProcessFunction[(Long, Long), String]#Context,
                                out: Collector[String]): Unit = {
      val dt = new Timestamp(value._1)
      out.collect(dt + ": " + value._2)
    }
  }

}
