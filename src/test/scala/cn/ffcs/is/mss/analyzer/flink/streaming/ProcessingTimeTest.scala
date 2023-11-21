package cn.ffcs.is.mss.analyzer.flink.streaming

import java.time.Duration
import java.util.Properties

import cn.ffcs.is.mss.analyzer.flink.warn.KafkaSourceTest.DataFlatMapFunction
import cn.ffcs.is.mss.analyzer.utils.{Constants, IniProperties}
import org.apache.flink.api.common.eventtime.{SerializableTimestampAssigner, WatermarkStrategy}
import org.apache.flink.api.common.functions.{ReduceFunction, RichFlatMapFunction}
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.functions.ProcessFunction
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.windowing.assigners.{TumblingProcessingTimeWindows, WindowAssigner}
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.util.Collector

/**
 * @ClassName:
 * @Author: mengwenting
 * @Date: 2023/7/20 16:04
 * @Description:
 * @update:
 */
object ProcessingTimeTest {
  def main(args: Array[String]): Unit = {
        val args0 = "G:\\ffcs\\4_Code\\mss\\src\\main\\resources\\flink.ini"
        val confProperties = new IniProperties(args0)
//    val confProperties = new IniProperties(args(0))

    //任务的名字
    val jobName = confProperties.getValue(Constants.NEW_VERSION_TEST_CONFIG, Constants
      .NEW_VERSION_TEST_CONFIG_JOB_NAME)

    //flink全局变量
    val parameters: Configuration = new Configuration()

    //获取ExecutionEnvironment
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    //设置check pointing的间隔
    //    env.enableCheckpointing(checkpointInterval)
    //设置Flink全局变量
    env.getConfig.setGlobalJobParameters(parameters)

    //获取socket数据源
    val sourceData = env.socketTextStream("192.168.1.22", 8888)
    sourceData
        .flatMap(new SourceFlatMapFunction).setParallelism(1)
        .assignTimestampsAndWatermarks(
         WatermarkStrategy.forBoundedOutOfOrderness[(Long, String, String)](Duration.ofSeconds(10))
        .withTimestampAssigner(new SerializableTimestampAssigner[(Long, String, String)] {
          override def extractTimestamp(element: (Long, String, String), recordTimestamp: Long): Long = {
            element._1
          }
        }))
        .keyBy(_._2) //生效
        .window(TumblingProcessingTimeWindows.of(Time.seconds(10)))
        .reduce(new DataReduceFunction) //生效
        .print()
//      .process(new SourceProcessFunction)


    env.execute(jobName)
  }

  //模拟数据格式  1600017000|M|11.2.3.4
  class SourceFlatMapFunction extends RichFlatMapFunction[String, (Long, String, String)] {
    override def flatMap(value: String, out: Collector[(Long, String, String)]): Unit = {
      var data = (0L, "", "")
      val timeStamp = value.split("\\|", -1)(0).toLong
      val userName = value.split("\\|", -1)(1)
      val sourceIp = value.split("\\|", -1)(2)
      data = (timeStamp, userName, sourceIp)
      out.collect(data)
    }
  }

  class SourceProcessFunction extends ProcessFunction[(Long, String, String), String] {
    override def processElement(value: (Long, String, String), ctx: ProcessFunction[(Long, String, String), String]#Context,
                                out: Collector[String]): Unit = {
      out.collect("userName: " + value._2)
    }
  }

  class DataReduceFunction extends ReduceFunction[(Long, String, String)]{
    override def reduce(value1: (Long, String, String), value2: (Long, String, String)):
    (Long, String, String) = {
      val timeStamp = value2._1
      val userName = value1._2 + "|" + value2._2
      val sourceIp = value2._3
      (timeStamp, userName, sourceIp)
    }
  }

}
