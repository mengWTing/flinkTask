
package cn.ffcs.is.mss.analyzer.flink.keLaiFlow

import java.util.Properties

import cn.ffcs.is.mss.analyzer.utils.{Constants, IniProperties, KeLaiTimeUtils}
import org.apache.flink.api.common.accumulators.LongCounter
import org.apache.flink.api.common.functions.RichFlatMapFunction
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.scala.{StreamExecutionEnvironment, _}
import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaConsumer, FlinkKafkaProducer}
import org.apache.flink.streaming.util.serialization.SimpleStringSchema
import org.apache.flink.util.Collector
import org.json.JSONObject

import scala.util.control.Breaks._


object QuintetKeLai {
  def main(args: Array[String]): Unit = {

    //测试配置信息
    //val confPath = "D:\\ffxm\\mss\\src\\main\\resources\\flink.ini"
    //val confProperties: IniProperties = new IniProperties(confPath)

    val confProperties = new IniProperties(args(0))
    //任务名称
    val jobName = confProperties.getValue(Constants.QUINTET_KELAI_FLINK_TO_KAFKA_CONFIG, Constants
      .QUINTET_KELAI_JOB_NAME)
    //kafka的服务地址
    val brokerList = confProperties.getValue(Constants.FLINK_COMMON_CONFIG, Constants
      .KAFKA_BOOTSTRAP_SERVERS)

    //kafka Source的名字
    val kafkaSourceName = confProperties.getValue(Constants.QUINTET_KELAI_FLINK_TO_KAFKA_CONFIG, Constants
      .QUINTET_KELAI_KAFKA_SOURCE_NAME)
    //kafka Source的并行度
    val kafkaSourceParallelism = confProperties.getIntValue(Constants.QUINTET_KELAI_FLINK_TO_KAFKA_CONFIG,
      Constants.QUINTET_KELAI_KAFKA_SOURCE_PARALLELISM)
    //kafka Source的topic
    val topic = confProperties.getValue(Constants.QUINTET_KELAI_FLINK_TO_KAFKA_CONFIG, Constants
      .QUINTET_KELAI_KAFKA_SOURCE_TOPIC)

    //kafka Sink的名字
    val kafkaSinkName = confProperties.getValue(Constants.QUINTET_KELAI_FLINK_TO_KAFKA_CONFIG, Constants
      .QUINTET_KELAI_KAFKA_SINK_NAME)
    //写入kafka的并行度
    val kafkaSinkParallelism = confProperties.getIntValue(Constants.QUINTET_KELAI_FLINK_TO_KAFKA_CONFIG,
      Constants.QUINTET_KELAI_KAFKA_SINK_PARALLELISM)
    //kafka Sink的topic
    val kafkaSinkTopic = confProperties.getValue(Constants.QUINTET_KELAI_FLINK_TO_KAFKA_CONFIG, Constants
      .QUINTET_KELAI_KAFKA_SINK_TOPIC)

    //flink消费的group.id
    val groupId = confProperties.getValue(Constants.QUINTET_KELAI_FLINK_TO_KAFKA_CONFIG, Constants
      .QUINTET_KELAI_GROUP_ID)
    //check pointing的间隔
    val checkpointInterval = confProperties.getLongValue(Constants.QUINTET_KELAI_FLINK_TO_KAFKA_CONFIG,
      Constants.QUINTET_KELAI_CHECKPOINT_INTERVAL)
    //算子并行度
    val dealParallelism = confProperties.getIntValue(Constants.QUINTET_KELAI_FLINK_TO_KAFKA_CONFIG, Constants
      .QUINTET_KELAI_DEAL_PARALLELISM)
    //过滤字符串长度
    val quintetKelaiLength = confProperties.getIntValue(Constants.QUINTET_KELAI_FLINK_TO_KAFKA_CONFIG, Constants
      .QUINTET_KELAI_LENGTH)

    //设置kafka消费者相关配置
    val props = new Properties()
    //设置kafka集群地址
    props.setProperty("bootstrap.servers", brokerList)
    //设置flink消费的group.id
    props.setProperty("group.id", groupId)

    //获取ExecutionEnvironment
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    //设置check pointing的间隔
    env.enableCheckpointing(checkpointInterval)
    //设置流的时间为EventTime
    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)

    //获取kafka消费者
    val consumer = new FlinkKafkaConsumer[String](topic, new SimpleStringSchema, props).setStartFromLatest()
    //获取kafka生产者
    val producer = new FlinkKafkaProducer[String](brokerList, kafkaSinkTopic, new SimpleStringSchema())

    //获取kafka数据
    env.addSource(consumer).setParallelism(kafkaSourceParallelism)
    //env.socketTextStream("192.168.1.105",8888)
      .filter(_.length > quintetKelaiLength).setParallelism(kafkaSinkParallelism)
      .flatMap(new DnsFlatMapFunction).setParallelism(kafkaSinkParallelism)
      .addSink(producer).setParallelism(kafkaSinkParallelism)

    env.execute(jobName)
  }

  /**
   * 输入：从kafka读取出来的数据
   * 输出：DnsModel类型的json字符串
   */
  class DnsFlatMapFunction extends RichFlatMapFunction[String, String] {
    val flatMapReceiveMessage: LongCounter = new LongCounter()
    val flatMapSuccessMessage: LongCounter = new LongCounter()

    override def open(parameters: Configuration): Unit = {
      getRuntimeContext.addAccumulator("flatMap message receive:", flatMapReceiveMessage)
      getRuntimeContext.addAccumulator("flatMap message success:", flatMapSuccessMessage)
    }

    override def flatMap(in: String, out: Collector[String]): Unit = {
      flatMapReceiveMessage.add(1)

      val json = new JSONObject(in)
      val recordsArray = json.getJSONArray("records")
      val recordLength = recordsArray.length()
      breakable {
      for (i <- 0 until recordLength) {
        val recordsObject = recordsArray.getJSONObject(i)
        val sourceIp = recordsObject.get("client_ip_addr") //源IP
        if("-".equals(sourceIp)){
          break()
        }
        val sourcePort = recordsObject.get("client_port") //源端口
        val sourceMac = recordsObject.get("client_netsegment_id") //源MAC
        val destinationIp = recordsObject.get("server_ip_addr") //目的IP
        val destinationPort = recordsObject.get("server_port") //目的PORT
        val destinationMac = recordsObject.get("client_netsegment_id") //目的MAC
        val protocolId = recordsObject.get("protocol") //协议类型
        val vlanId = recordsObject.get("vlan_id") //VLAN号，第一层vlan
        val probeId = recordsObject.getString("time") //采集分段标识(探针号)
        val commonTime = KeLaiTimeUtils.getKeLaiTime(probeId) //公用时间
        val inputOctets = recordsObject.get("client_total_byte") //流入字节数
        val outputOctets = recordsObject.get("server_total_byte") //流出字节数
        val inputPacket = recordsObject.get("client_total_packet") //流入包数
        val outputPacket = recordsObject.get("server_total_packet") //流出包数
        val inputRetransPacket = recordsObject.get("client_tcp_retransmission_packet") //流入重传包数
        val outputRetransPacket = recordsObject.get("server_tcp_retransmission_packet") //流出重传包数
        val clientTcpTime = recordsObject.get("client_tcp_total_retrans_time").toString
        val serverTcpTime = recordsObject.get("server_tcp_total_retrans_time").toString
        val inputRetransDelay = KeLaiTimeUtils.getKeLaiTimeDefault(clientTcpTime, commonTime) //流入重传时延(总时延)-ms
        val outputRetransDelay = KeLaiTimeUtils.getKeLaiTimeDefault(serverTcpTime, commonTime) //流出重传时延(总时延)-ms
        val inputRest = recordsObject.get("client_tcp_rst_packet") //流入RESET数量
        val outputRest = recordsObject.get("server_tcp_rst_packet") //流出RESET数量
        val isSucceed = recordsObject.get("tcp_status") //连接是否成功（1-成功、0-失败、2-分割）
        val inputSmallPacket = recordsObject.get("client_min_ack_delay") //流入小包数
        val outputSmallPacket = recordsObject.get("client_max_ack_delay") //流入大包数
        val inputMediumPacket = recordsObject.get("client_avg_ack_delay") //流入中包数
        val outputMediumPacket = recordsObject.get("server_min_ack_delay") //流出小包数
        val inputLargePacket = recordsObject.get("server_max_ack_delay") //流出大包数
        val outputLargePacket = recordsObject.get("server_avg_ack_delay") //流出中包数
        val clientTime = recordsObject.get("client_tcp_window_0").toString
        val serverTime = recordsObject.get("server_tcp_window_0").toString
        val inputZeroWindow = KeLaiTimeUtils.getKeLaiTimeDefault(clientTime, commonTime) //流入零窗口数
        val outputZeroWindow = KeLaiTimeUtils.getKeLaiTimeDefault(serverTime, commonTime) //流出零窗口数
        val flowStartTime = recordsObject.get("flow_start_time").toString
        val flowEndTime = recordsObject.get("flow_end_time").toString
        val startTime = KeLaiTimeUtils.getKeLaiTimeDefault(flowStartTime, commonTime) //开始时间戳-ms 秒
        val finishTime = KeLaiTimeUtils.getKeLaiTimeDefault(flowEndTime, commonTime) //结束时间戳-ms
        val synTime = commonTime //SYN时间戳-ms
        val synAckTime = commonTime //SYN ACK时间戳-ms
        val ackTime = commonTime //ACK时间戳-ms
        val ttl = commonTime //Time to live
        val tcpOrUdp = 1
        val builder: StringBuilder = new StringBuilder
        builder.append(sourceIp + "|" + sourcePort + "|" + sourceMac + "|" + destinationIp + "|" +
          destinationPort + "|" + destinationMac + "|" + protocolId + "|" + vlanId + "|" + probeId + "|" +
          inputOctets + "|" + outputOctets + "|" + inputPacket + "|" + outputPacket + "|" + inputRetransPacket + "|" +
          outputRetransPacket + "|" + inputRetransDelay + "|" + outputRetransDelay + "|" + inputRest + "|" +
          outputRest + "|" + isSucceed + "|" + inputSmallPacket + "|" + outputSmallPacket + "|" +
          inputMediumPacket + "|" + outputMediumPacket + "|" + inputLargePacket + "|" + outputLargePacket + "|" +
          inputZeroWindow + "|" + outputZeroWindow + "|" + startTime + "|" + finishTime + "|" + synTime + "|" +
          synAckTime + "|" + ackTime + "|" + ttl + "|" + tcpOrUdp
        )
        flatMapSuccessMessage.add(1)
        out.collect(builder.toString)
      }
    }
    }

  }

}


