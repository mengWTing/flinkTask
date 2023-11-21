package cn.ffcs.is.mss.analyzer.flink.streaming

import java.time.Duration
import java.util.Properties

import cn.ffcs.is.mss.analyzer.druid.model.scala.OperationModel
import cn.ffcs.is.mss.analyzer.utils.{Constants, IniProperties, JsonUtil}
import org.apache.flink.api.common.eventtime.{SerializableTimestampAssigner, WatermarkStrategy}
import org.apache.flink.configuration.{ConfigOption, ConfigOptions, Configuration}
import org.apache.flink.streaming.api.functions.ProcessFunction
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.windowing.assigners.SlidingEventTimeWindows
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.util.Collector

/**
 * @ClassName:
 * @Author: mengwenting
 * @Date: 2023/8/15 14:38
 * @Description:
 * @update:
 */
object GetValueTest {
  def main(args: Array[String]): Unit = {
    //根据传入的参数解析配置文件
        val args0 = "G:\\ffcs\\4_Code\\mss\\src\\main\\resources\\flink.ini"
        val confProperties = new IniProperties(args0)

    //flink消费的group.id
    val groupId = "getValueTest"
    val parameters: Configuration = new Configuration()

    parameters.setInteger(Constants.FLINK_ABNORMAL_FLOW_STATISTICS_LENGTH, confProperties.getIntValue(Constants
      .FLINK_ABNORMAL_FLOW_CONFIG, Constants.FLINK_ABNORMAL_FLOW_STATISTICS_LENGTH))
    parameters.setDouble(Constants.FLINK_ABNORMAL_FLOW_THRESHOLD_RATIO, confProperties.getFloatValue(Constants
      .FLINK_ABNORMAL_FLOW_CONFIG, Constants.FLINK_ABNORMAL_FLOW_THRESHOLD_RATIO))
    parameters.setLong(Constants.FLINK_ABNORMAL_FLOW_BASE_COUNT, confProperties.getLongValue(Constants
      .FLINK_ABNORMAL_FLOW_CONFIG, Constants.FLINK_ABNORMAL_FLOW_BASE_COUNT))

    //获取ExecutionEnvironment
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    //设置flink全局变量
    env.getConfig.setGlobalJobParameters(parameters)
    //设置kafka消费者相关配置
    val props = new Properties
    val sourceData = env.socketTextStream("192.168.1.22", 8888)
    sourceData
      .map(JsonUtil.fromJson[OperationModel] _).setParallelism(1)
      .filter(_.userName != "匿名用户").setParallelism(1)
      .assignTimestampsAndWatermarks(
        WatermarkStrategy.forBoundedOutOfOrderness[OperationModel](Duration.ofSeconds(2))
          .withTimestampAssigner(new SerializableTimestampAssigner[OperationModel] {
            override def extractTimestamp(element: OperationModel, recordTimestamp: Long): Long =
              element.timeStamp
          })
      ).setParallelism(1)
      .map(model => (model.timeStamp / 1000 / 60 * 1000 * 60 + "|" + model.userName, model.inputOctets, model
        .outputOctets, model.sourceIp, model.destinationIp))
      .setParallelism(1)
      .keyBy(x => x._1)
      .window(SlidingEventTimeWindows.of(Time.minutes(1), Time.minutes(1)))
      .reduce((o1, o2) => (o2._1, o1._2 + o2._2, o1._3 + o2._3, o2._4, o2._5))
      .process(new GetDataProcessFunction).setParallelism(1)
      .print()

    env.execute("test")
  }

  class GetDataProcessFunction extends ProcessFunction[(String, Long, Long, String, String), Int] {
    var historyLen: Int = _

    override def open(parameters: Configuration): Unit = {
      val globConf = getRuntimeContext.getExecutionConfig.getGlobalJobParameters.asInstanceOf[Configuration]
      historyLen = globConf.getInteger(ConfigOptions.key(Constants.FLINK_ABNORMAL_FLOW_STATISTICS_LENGTH).intType().defaultValue(0),2)
//      val value1: ConfigOption[Integer] = ConfigOptions.key(Constants.FLINK_ABNORMAL_FLOW_STATISTICS_LENGTH).intType().defaultValue(0)
//      val value: ConfigOptions.TypedConfigOptionBuilder[Integer] = ConfigOptions.key(Constants.FLINK_ABNORMAL_FLOW_STATISTICS_LENGTH).intType()

    }
    override def processElement(value: (String, Long, Long, String, String),
                                ctx: ProcessFunction[(String, Long, Long, String, String), Int]#Context,
                                out: Collector[Int]): Unit = {
      val splits = value._1.split("\\|")
      val timestamp: Long = splits(0).toLong
      if(timestamp > 0){
        out.collect(historyLen)
      }
    }
  }
}
