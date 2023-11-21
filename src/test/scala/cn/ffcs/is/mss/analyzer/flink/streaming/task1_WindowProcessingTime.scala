package cn.ffcs.is.mss.analyzer.flink.streaming

import java.time.Duration

import cn.ffcs.is.mss.analyzer.druid.model.scala.OperationModel
import cn.ffcs.is.mss.analyzer.utils.{IniProperties, JsonUtil}
import org.apache.flink.api.common.eventtime.{SerializableTimestampAssigner, WatermarkStrategy}
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.streaming.api.windowing.assigners.{TumblingEventTimeWindows, TumblingProcessingTimeWindows}
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.api.scala._

/**
 * @ClassName:
 * @Author: mengwenting
 * @Date: 2023/8/9 17:29
 * @Description:
 * @update:
 */
object task1_WindowProcessingTime {
  def main(args: Array[String]): Unit = {

    val args0 = "G:\\ffcs\\4_Code\\mss\\src\\main\\resources\\flink.ini"
    val confProperties = new IniProperties(args0)

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    val sourceData = env.socketTextStream("192.168.1.22", 8888)
    env.setParallelism(1)

    sourceData
      .map(JsonUtil.fromJson[OperationModel] _)
      .filter(_.userName != "匿名用户")
      .map(model => (model.timeStamp / 1000 / 60 * 1000 * 60, model.userName, model.inputOctets, model
        .outputOctets, model.sourceIp, model.destinationIp))
      .keyBy(x=>x._1)
      .window(TumblingProcessingTimeWindows.of(Time.seconds(10)))
      .reduce((x1, x2)=>(x1._1.max(x2._1), x2._2, x1._3+x2._3, x1._4+x2._4, x1._5+"|"+x2._5, x1._6+"|"+x2._6))
      .print()


    env.execute("task_01_ProcessingTime")
  }
}
