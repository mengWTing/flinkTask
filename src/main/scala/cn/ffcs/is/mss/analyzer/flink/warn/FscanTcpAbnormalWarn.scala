package cn.ffcs.is.mss.analyzer.flink.warn

import java.net.URLDecoder
import java.sql.Timestamp
import java.time.Duration
import java.util.Properties

import cn.ffcs.is.mss.analyzer.bean.MasterTcpWarnEntity
import cn.ffcs.is.mss.analyzer.druid.model.scala.OperationModel
import cn.ffcs.is.mss.analyzer.druid.model.scala.QuintetModel
import cn.ffcs.is.mss.analyzer.flink.sink.{MySQLSink, Sink}
import cn.ffcs.is.mss.analyzer.flink.source.Source
import com.twitter.logging.config.BareFormatterConfig.intoOption
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaConsumer, FlinkKafkaProducer}
import org.apache.flink.streaming.util.serialization.SimpleStringSchema
import cn.ffcs.is.mss.analyzer.utils.{Constants, IniProperties, JsonUtil}
import org.apache.flink.api.common.eventtime.{SerializableTimestampAssigner, WatermarkStrategy}
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.api.common.functions.{ReduceFunction, RichFlatMapFunction, RichMapFunction}
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.streaming.api.functions.AssignerWithPunctuatedWatermarks
import org.apache.flink.util.Collector
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.functions.ProcessFunction
import org.apache.flink.streaming.api.watermark.Watermark
import org.apache.flink.streaming.api.windowing.assigners.SlidingEventTimeWindows
import org.json.JSONObject

import scala.util.control.Breaks.{break, breakable}
import scala.collection.mutable

/**
 * @ClassName:
 * @Author: mengwenting
 * @Date: 2023/6/21 15:53
 * @Description:
 * @update:
 */
object FscanTcpAbnormalWarn {
  def main(args: Array[String]): Unit = {
    //    val args0 = "G:\\ffcs\\4_Code\\mss\\src\\main\\resources\\flink.ini"
    //    val confProperties = new IniProperties(args0)
    val confProperties = new IniProperties(args(0))

    //任务的名字
    val jobName = confProperties.getValue(Constants.FSCAN_ABNORMAL_ANALYSE_TCP_CONFIG, Constants
      .FSCAN_ABNORMAL_ANALYSE_TCP_CONFIG_JOB_NAME)
    //kafka Source的名字
    val kafkaSourceName = confProperties.getValue(Constants.FSCAN_ABNORMAL_ANALYSE_TCP_CONFIG, Constants.FSCAN_ABNORMAL_ANALYSE_TCP_CONFIG_KAFKA_SOURCE_NAME)
    //mysql sink的名字
    val sqlSinkName = confProperties.getValue(Constants.FSCAN_ABNORMAL_ANALYSE_TCP_CONFIG, Constants.FSCAN_ABNORMAL_ANALYSE_TCP_CONFIG_SQL_SINK_NAME)
    //kafka sink的名字
    val kafkaSinkName = confProperties.getValue(Constants.FSCAN_ABNORMAL_ANALYSE_TCP_CONFIG, Constants.FSCAN_ABNORMAL_ANALYSE_TCP_CONFIG_KAFKA_SINK_NAME)

    //并行度
    val sourceParallelism = confProperties.getIntValue(Constants.FSCAN_ABNORMAL_ANALYSE_TCP_CONFIG,
      Constants.FSCAN_ABNORMAL_ANALYSE_TCP_CONFIG_SOURCE_PARALLELISM)
    val sinkParallelism = confProperties.getIntValue(Constants.FSCAN_ABNORMAL_ANALYSE_TCP_CONFIG,
      Constants.FSCAN_ABNORMAL_ANALYSE_TCP_CONFIG_SINK_PARALLELISM)
    val dealParallelism = confProperties.getIntValue(Constants.FSCAN_ABNORMAL_ANALYSE_TCP_CONFIG,
      Constants.FSCAN_ABNORMAL_ANALYSE_TCP_CONFIG_DEAL_PARALLELISM)

    //check pointing的间隔
    val checkpointInterval = confProperties.getLongValue(Constants.FSCAN_ABNORMAL_ANALYSE_TCP_CONFIG,
      Constants.FSCAN_ABNORMAL_ANALYSE_TCP_CONFIG_CHECKPOINT_INTERVAL)

    //kafka的服务地址
    val brokerList = confProperties.getValue(Constants.FLINK_COMMON_CONFIG, Constants
      .KAFKA_BOOTSTRAP_SERVERS)
    //flink消费的group.id
    val groupId = confProperties.getValue(Constants.FSCAN_ABNORMAL_ANALYSE_TCP_CONFIG, Constants
      .FSCAN_ABNORMAL_ANALYSE_TCP_CONFIG_GROUP_ID)

    //kafka source 的topic--tcpScan异常
    val tcpScanTopic = confProperties.getValue(Constants.FSCAN_ABNORMAL_ANALYSE_TCP_CONFIG, Constants
      .FSCAN_ABNORMAL_ANALYSE_TCP_CONFIG_KAFKA_SOURCE_TOPIC)
    //kafka sink topic  --tcp
    val kafkaSinkTcpMasterTopic = confProperties.getValue(Constants.FSCAN_ABNORMAL_ANALYSE_TCP_CONFIG,
      Constants.FSCAN_ABNORMAL_ANALYSE_TCP_KAFKA_SINK_TOPIC)

    //写入kafka的并行度
    val kafkaSinkParallelism = confProperties.getIntValue(Constants.FSCAN_ABNORMAL_ANALYSE_TCP_CONFIG,
      Constants.FSCAN_ABNORMAL_ANALYSE_TCP_CONFIG_SQL_SINK_PARALLELISM)

    val warningSinkTopic = confProperties.getValue(Constants.WARNING_FLINK_TO_DRUID_CONFIG, Constants
      .WARNING_TOPIC)
    //flink全局变量
    val parameters: Configuration = new Configuration()
    //配置文件系统类型
    parameters.setString(Constants.FILE_SYSTEM_TYPE, confProperties.getValue(Constants
      .FLINK_COMMON_CONFIG, Constants.FILE_SYSTEM_TYPE))
    //c3p0连接池配置文件路径
    parameters.setString(Constants.c3p0_CONFIG_PATH, confProperties.getValue(Constants
      .FLINK_COMMON_CONFIG, Constants.c3p0_CONFIG_PATH))

    //获取kafka--tcp消费者
    val tcpConsumer = Source.kafkaSource(tcpScanTopic, groupId, brokerList)
    //获取kafka生产者 -- Tcp
    val producerTcpMaster = Sink.kafkaSink(brokerList, kafkaSinkTcpMasterTopic)

    //获取ExecutionEnvironment
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    //设置check pointing的间隔
    //env.enableCheckpointing(checkpointInterval)
    //设置Flink全局变量
    env.getConfig.setGlobalJobParameters(parameters)

    //获取Kafka数据流
    val tcpStream = env.fromSource(tcpConsumer, WatermarkStrategy.noWatermarks(), kafkaSourceName).setParallelism(sourceParallelism)

    val tcpScanData = tcpStream.map(JsonUtil.fromJson[QuintetModel] _).setParallelism(dealParallelism)
      .assignTimestampsAndWatermarks(
        WatermarkStrategy.forBoundedOutOfOrderness[QuintetModel](Duration.ofSeconds(10))
          .withTimestampAssigner(new SerializableTimestampAssigner[QuintetModel] {
            override def extractTimestamp(element: QuintetModel, recordTimestamp: Long): Long = {
              element.timeStamp
            }
          })
      )
      .map(new TcpScanMapFunction).setParallelism(dealParallelism)
      .keyBy(x=>(x._2,x._3,x._4,x._6))
      .window(SlidingEventTimeWindows.of(Time.seconds(30), Time.seconds(30)))
      .reduce(new TcpScanReduceFunction).setParallelism(dealParallelism)
      .process(new ScanTypeProcessFunction).setParallelism(dealParallelism)

    val tcpValue: DataStream[(Object, Boolean)] = tcpScanData.map(_._1)
    val alertKafkaTcpScanValue = tcpScanData.map(_._2)

    tcpValue.addSink(new MySQLSink).uid("tcp Scan").name("tcp Scan")
      .setParallelism(sinkParallelism)

    tcpValue
      .map(o => {JsonUtil.toJson(o._1.asInstanceOf[MasterTcpWarnEntity])})
      .sinkTo(producerTcpMaster)
      .setParallelism(kafkaSinkParallelism)

    //将web异常告警数据写入告警库topic
    val warningProducer = Sink.kafkaSink(brokerList, warningSinkTopic)
    alertKafkaTcpScanValue.sinkTo(warningProducer).setParallelism(kafkaSinkParallelism)

    env.execute(jobName)
  }

  class TcpScanMapFunction extends RichMapFunction[QuintetModel, (Long, String, String, String, String, String, Long, Long, Long, Long, Long, String, Long)] {
    override def map(value: QuintetModel):
    //(timeStamp, sourceIp, sourcePort, destinationIp, destinationPort, protocolId, smallPacket, packet, synPre, synAckTtl, reset, protocol, connCount)
    (Long, String, String, String, String, String, Long, Long, Long, Long, Long, String, Long) = {
      val timeStamp = value.timeStamp
      val sourceIp = value.sourceIp
      val sourcePort = value.sourcePort
      val destinationIp = value.destinationIp
      val destinationPort = value.destinationPort
      val protocolId = value.protocolId
      val smallPacket = value.smallPacket
      val packet = value.packet
      val synTime = value.synTime
      val synAckTime = value.synAckTime
      val ackTime = value.ackTime
      val ttl = value.ttl
      val reset = value.rest
      val protocol = value.protocol
      val connCount = value.connCount
      var synPre = 0
      if (synTime > 0) {
        synPre = 1
      }
      var synAckTtl = 0
      if (synTime > 0 && synAckTime > 0 && ackTime > 0 && ttl > 0) {
        synAckTtl = 1
      }

      (timeStamp, sourceIp, sourcePort, destinationIp, destinationPort, protocolId, smallPacket, packet, synPre, synAckTtl, reset, protocol, connCount)
    }
  }

  class ScanTypeProcessFunction extends ProcessFunction[(Long, String, String, String, String, String, Long, Long, Long, Long, Long, String, Long), ((Object, Boolean), String)] {

    override def processElement(value: (Long, String, String, String, String, String, Long, Long, Long, Long, Long, String, Long),
                                ctx: ProcessFunction[(Long, String, String, String, String, String, Long, Long, Long, Long, Long, String, Long), ((Object, Boolean), String)]#Context,
                                out: Collector[((Object, Boolean), String)]): Unit = {
      //(timeStamp, sourceIp, sourcePort, destinationIp, destinationPort, protocolId, smallPacket, packet, synPre, synAckTtl, reset, protocol, connCount)
      val timeStamp = value._1
      val sourceIp = value._2
      val sourcePort = value._3
      val destinationIp = value._4
      val destinationPorts = value._5
      val protocolId = value._6
      val smallPacketSum = value._7
      val packetSum = value._8
      val synSum = value._9
      val synAckTtl = value._10
      val resetSum = value._11
      val protocol = value._12
      val scanCount = value._13

      // 相同sourceIp sourcePort destinationIp protocolId,
      // smallPacket/packet>=50% || synTime数量 30s>=50次 && reset数量 30s>=50次 || 判断目的端口数量30s>=50次
      val rule1Bool = packetRule(smallPacketSum, packetSum)
      val rule2Bool = synAndResetRule(synSum, resetSum)
      val rule3Bool = portRule(scanCount)

      val bool = tcpScanAbnormal(rule1Bool, rule2Bool, rule3Bool)

      //Tcp和SYN的判断方式来自一批数据,取最后一条数据
      //      //synTime, synAckTime, ackTime, 拿最后一条数据做判断
      val scanType = tcpOrSynType(protocol, synAckTtl)

      val entity = new MasterTcpWarnEntity
      entity.setAlertTime(new Timestamp(timeStamp))
      entity.setUserName("匿名用户")
      entity.setSourceIp(sourceIp)
      entity.setSourcePorts(sourcePort)
      entity.setDestinationIps(destinationIp)
      entity.setDestinationPorts(destinationPorts)
      entity.setScanCount(scanCount)

      val inputKafkaValue = "" + "|" + "Fscan扫描工具tcp横向端口扫描行为异常" + "|" + timeStamp + "|" +
        "" + "|" + "" + "|" + "" + "|" +
        "" + "|" + sourceIp + "|" + sourcePort + "|" +
        destinationIp + "|" + destinationPorts + "|" + "" + "|" +
        "" + "|" + "" + "|" + ""
      if (!scanType.equals("") && bool) {
        out.collect((entity, true), inputKafkaValue)
      }
    }

    def tcpScanAbnormal(r1: Boolean, r2: Boolean, r3: Boolean): Boolean = {
      var abnormal = false
      if (r1 && r2 || r1 && r3 || r2 && r3) {
        abnormal = true
      }
      abnormal
    }
    def packetRule(smallSum: Long, pacSum: Long): Boolean = {
      if (smallSum.toFloat / pacSum >= 0.5) {
        true
      } else {
        false
      }
    }
    def synAndResetRule(synSum: Long, resetSum: Long): Boolean = {
      if (synSum >= 600 && resetSum >= 600) {
        true
      } else {
        false
      }
    }
    def portRule(scanSum: Long): Boolean = {
      if (scanSum >= 600) {
        true
      } else {
        false
      }
    }

    def tcpOrSynType(protocol: String, synAckTtl: Long): String = {
      var scanType = ""
      //在tcp的条件下还满足四个字段不为空,则认为是Syn类型,否则认为是Tcp,否则为Udp方式
      if ("tcp".equals(protocol)) {
        if (1.equals(synAckTtl)) {
          scanType = "synScan端口异常扫描"
        } else {
          scanType = "tcpScan端口异常扫描"
        }
      } else {
        scanType = "udpScan端口异常扫描"
      }
      scanType
    }
  }

  class TcpScanReduceFunction extends ReduceFunction[(Long, String, String, String, String, String, Long, Long, Long, Long, Long, String, Long)] {
    override def reduce(value1: (Long, String, String, String, String, String, Long, Long, Long, Long, Long, String, Long),
                        value2: (Long, String, String, String, String, String, Long, Long, Long, Long, Long, String, Long))
    : (Long, String, String, String, String, String, Long, Long, Long, Long, Long, String, Long) = {
      //(timeStamp, sourceIp, sourcePort, destinationIp, destinationPort, protocolId, smallPacket, packet, synPre, synAckTtl, reset, protocol, connCount)
      val timeStamp = value2._1
      val sourceIp = value2._2
      val sourcePort = value2._3
      val destinationIp = value2._4
      var destinationPorts = ""
      if(value2._5.equals(value1._5)){
        destinationPorts = value2._5
      }else{
        destinationPorts = value2._5 + "|" + value1._5
      }
      val protocolId = value2._6
      val smallPacketSum = value1._7 + value2._7
      val packetSum = value1._8 + value2._8
      val synSum = value1._9 + value2._9
      val synAckTtlValue = value2._10
      val resetSum = value1._11 + value2._11
      val protocol = value2._12
      val scanCount = value1._13 + value2._13

      (timeStamp, sourceIp, sourcePort, destinationIp, destinationPorts, protocolId, smallPacketSum, packetSum, synSum, synAckTtlValue, resetSum, protocol, scanCount)
    }
  }
}
