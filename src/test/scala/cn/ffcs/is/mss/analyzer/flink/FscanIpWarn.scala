package scala.cn.ffcs.is.mss.analyzer.flink

import cn.ffcs.is.mss.analyzer.druid.model.scala.OperationModel
import cn.ffcs.is.mss.analyzer.utils._
import org.apache.flink.streaming.api.functions.{AssignerWithPunctuatedWatermarks, ProcessFunction}
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.watermark.Watermark
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.util.Collector


/**
 * @ClassName:
 * @Author: mengwenting
 * @Date: 2023/3/2 16:55
 * @Description:
 * @update:
 */
object FscanIpWarn {
  def main(args: Array[String]): Unit = {

    //处理数据之前判空
    val args0 = "G:\\ffcs\\4_Code\\mss\\src\\main\\resources\\flink.ini"
    val confProperties = new IniProperties(args0)

    //该任务的名字
    val jobName = confProperties.getValue(Constants.FSCAN_ABNORMAL_ANALYSE_CONFIG,
      Constants.FSCAN_ABNORMAL_ANALYSE_CONFIG_JOB_NAME)
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    val inputData = env.socketTextStream("192.168.1.24", 8888)
    inputData.setParallelism(1)

    val outputData = inputData
      .map(JsonUtil.fromJson[OperationModel] _)
      .map(operationModel => (operationModel.timeStamp, operationModel.userName, operationModel.sourceIp,
        operationModel.destinationIp, operationModel.inputOctets, operationModel.outputOctets, 1))
      .filter(operationModel => operationModel._5/1024 <= 0.1 || operationModel._6/1024 <= 0.1)
      .assignTimestampsAndWatermarks(new AssignerWithPunctuatedWatermarks[(Long, String, String, String, Long, Long, Int)] {
        override def checkAndGetNextWatermark(lastElement: (Long, String, String, String, Long, Long, Int), extractedTimestamp: Long): Watermark =
          new Watermark(extractedTimestamp - 10000L)

        override def extractTimestamp(element: (Long, String, String, String, Long, Long, Int), previousElementTimestamp: Long): Long =
          element._1
      })
        .keyBy(3)
      .timeWindow(Time.seconds(30), Time.seconds(30))
        //.reduce(new FscanIpReduceFunction)
      .reduce((op1, op2) => (op1._1, op1._2, op1._3, op1._4, op1._5+op2._5, op1._6+op2._6, op1._7+op2._7))
      .process(new FscanIpProcessFunction).setParallelism(1)
      .print()

    env.execute(jobName)
  }

  class FscanIpProcessFunction extends ProcessFunction[(Long, String, String, String, Long, Long, Int), String] {
    override def processElement(value: (Long, String, String, String, Long, Long, Int), ctx: ProcessFunction[(Long, String, String, String, Long, Long, Int), String]#Context,
                                out: Collector[String]): Unit = {

//      val output = "告警时间: " + value._1 + "|用户: " + value._2 + "|源Ip: " + value._3 +
//      "|目的Ip: " + value._4 + "|接收或发送字节数小于0.1Kb的次数: " + value._7
      if (value._7 >=2){
        out.collect("告警时间: " + value._1 + "|用户: " + value._2 + "|源Ip: " + value._3 +
          "|目的Ip: " + value._4 + "|主机探测扫描次数: " + value._7)
      }
    }
  }
}