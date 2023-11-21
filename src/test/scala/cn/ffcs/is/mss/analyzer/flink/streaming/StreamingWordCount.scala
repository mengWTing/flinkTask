package cn.ffcs.is.mss.analyzer.flink.streaming

import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.api.scala._
import org.apache.flink.streaming.api.TimeCharacteristic


/**
 * @ClassName:
 * @Author: mengwenting
 * @Date: 2023/5/30 9:59
 * @Description:
 * @update:
 */
object StreamingWordCount {
  def main(args: Array[String]) {
    /*
    在Flink程序中首先需要创建一个StreamExecutionEnvironment
    （如果你在编写的是批处理程序，需要创建ExecutionEnvironment），它被用来设置运行参数。
    当从外部系统读取数据的时候，它也被用来创建源（sources）。
     */
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setStreamTimeCharacteristic(TimeCharacteristic.ProcessingTime)
    val text = env.socketTextStream("192.168.1.24", 8888)

    val counts = text.flatMap { _.toLowerCase.split("\\W+") filter { _.nonEmpty } }//nonEmpty非空的
      .map { (_, 1) }
      .keyBy(0)//通过Tuple的第一个元素进行分组
      .timeWindow(Time.seconds(5))//Windows 根据某些特性将每个key的数据进行分组 (例如:在5秒内到达的数据).
      .sum(1)

    //将结果流在终端输出
    counts.print
    //开始执行计算
    env.execute("Window Stream WordCount")
  }
}