package cn.ffcs.is.mss.analyzer.flink.streaming

import cn.ffcs.is.mss.analyzer.druid.model.scala.OperationModel
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.functions.AssignerWithPunctuatedWatermarks
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.watermark.Watermark
import org.apache.flink.streaming.api.windowing.time.Time
/**
 * @ClassName:
 * @Author: mengwenting
 * @Date: 2023/5/24 16:47
 * @Description:
 * @update:
 */
object WordCountStreaming {
  def main(args: Array[String]): Unit = {
    /**
     * 1、创建flink的运行环境
     * 这是flink程序的入口
     */
    val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment

    /**
     * 2、读取数据
     * DataStream相当于spark中的DStream
     */
    val linesDS = env.socketTextStream("192.168.1.24", 8888)

    /**
     * 3、开启socket
     * 在虚拟机中输入  nc -lk 8888  回车
     */

    //先不做处理，直接打印处理
    //流处理不能使用foreach循环打印
    linesDS.print()

    /**
     * 4、启动flink程序（运行该代码）
     */
    env.execute("wordcount")//给该程序起个名字
  }
}