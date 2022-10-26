package cn.ffcs.is.mss.analyzer.flink.keLaiFlow

import java.util.Properties

import cn.ffcs.is.mss.analyzer.utils.{Constants, IniProperties, KeLaiTimeUtils}
import org.apache.flink.api.common.accumulators.LongCounter
import org.apache.flink.api.common.functions.RichFlatMapFunction
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.scala.{StreamExecutionEnvironment, _}
import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaConsumer, FlinkKafkaProducer}
import org.apache.flink.util.Collector
import org.json.{JSONArray, JSONException, JSONObject}

import scala.collection.mutable
import scala.util.control.Breaks._

/**
 * @Title OperationKeLai
 * @Author ZF
 * @Date 2020-06-18 14:23
 * @Description 读取 kafka kelai_Operation数据
 * @update [no][date YYYY-MM-DD][name][description]
 */
object OperationKeLai {
  def main(args: Array[String]): Unit = {

    //测试配置信息
    //val confPath = "D:\\ffxm\\mss\\src\\main\\resources\\flink.ini"
    //val confProperties: IniProperties = new IniProperties(confPath)

    val confProperties = new IniProperties(args(0))

    //任务名称
    val jobName = confProperties.getValue(Constants.OPERATION_KELAI_FLINK_TO_KAFKA_CONFIG, Constants
      .OPERATION_KELAI_JOB_NAME)
    //kafka的服务地址
    val brokerList = confProperties.getValue(Constants.FLINK_COMMON_CONFIG, Constants
      .KAFKA_BOOTSTRAP_SERVERS)
    //flink消费的group.id
    val groupId = confProperties.getValue(Constants.OPERATION_KELAI_FLINK_TO_KAFKA_CONFIG, Constants
      .OPERATION_KELAI_GROUP_ID)

    //kafka Source的名字
    val kafkaSourceName = confProperties.getValue(Constants.OPERATION_KELAI_FLINK_TO_KAFKA_CONFIG, Constants
      .OPERATION_KELAI_KAFKA_SOURCE_NAME)
    //kafka Source的并行度
    val kafkaSourceParallelism = confProperties.getIntValue(Constants.OPERATION_KELAI_FLINK_TO_KAFKA_CONFIG,
      Constants.OPERATION_KELAI_KAFKA_SOURCE_PARALLELISM)
    //kafka Source的topic
    val topic = confProperties.getValue(Constants.OPERATION_KELAI_FLINK_TO_KAFKA_CONFIG, Constants
      .OPERATION_KELAI_KAFKA_SOURCE_TOPIC)

    //kafka Sink的名字
    val kafkaSinkName = confProperties.getValue(Constants.OPERATION_KELAI_FLINK_TO_KAFKA_CONFIG, Constants
      .OPERATION_KELAI_KAFKA_SINK_NAME)
    //写入kafka的并行度
    val kafkaSinkParallelism = confProperties.getIntValue(Constants.OPERATION_KELAI_FLINK_TO_KAFKA_CONFIG,
      Constants.OPERATION_KELAI_KAFKA_SINK_PARALLELISM)
    //kafka Sink的topic
    val kafkaSinkTopic = confProperties.getValue(Constants.OPERATION_KELAI_FLINK_TO_KAFKA_CONFIG, Constants
      .OPERATION_KELAI_KAFKA_SINK_TOPIC)

    //check pointing的间隔
    val checkpointInterval = confProperties.getLongValue(Constants.OPERATION_KELAI_FLINK_TO_KAFKA_CONFIG,
      Constants.OPERATION_KELAI_CHECKPOINT_INTERVAL)
    //算子并行度
    val dealParallelism = confProperties.getIntValue(Constants.OPERATION_KELAI_FLINK_TO_KAFKA_CONFIG, Constants
      .OPERATION_KELAI_DEAL_PARALLELISM)
    //过滤字符串长度
    val operationKelaiLength = confProperties.getIntValue(Constants.OPERATION_KELAI_FLINK_TO_KAFKA_CONFIG, Constants
      .OPERATION_KELAI_LENGTH)
    //该话单在druid的表名
    val tableName = confProperties.getValue(Constants.FLINK_COMMON_CONFIG, Constants.DRUID_DNS_TABLE_NAME)
    //druid的zk地址
    val druidZk = confProperties.getValue(Constants.FLINK_COMMON_CONFIG, Constants.TRANQUILITY_ZK_CONNECT)

    //设置写入druid时需要的配置
    val conf: mutable.HashMap[String, String] = mutable.HashMap[String, String]()
    //设置话单的表名
    conf("druid.arp.source") = tableName
    //设置druid集群zookeeper集群的地址
    conf("tranquility.zk.connect") = druidZk

    //设置kafka消费者相关配置
    val props = new Properties()
    //设置kafka集群地址
    props.setProperty("bootstrap.servers", brokerList)
    //设置flink消费的group.id
    props.setProperty("group.id", groupId + "test")

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
      .filter(_.length > operationKelaiLength).setParallelism(dealParallelism)
      .flatMap(new OperationFlatMapFunction).setParallelism(dealParallelism)
      .addSink(producer).setParallelism(kafkaSinkParallelism)

    env.execute(jobName)
  }


  class OperationFlatMapFunction extends RichFlatMapFunction[String, String] {
    val flatMapReceiveMessage: LongCounter = new LongCounter()
    val flatMapSuccessMessage: LongCounter = new LongCounter()

    override def open(parameters: Configuration): Unit = {
      getRuntimeContext.addAccumulator("flatMap message receive:", flatMapReceiveMessage)
      getRuntimeContext.addAccumulator("flatMap message success:", flatMapSuccessMessage)
    }

    override def flatMap(in: String, out: Collector[String]): Unit = {
      flatMapReceiveMessage.add(1)
      val json = new JSONObject(in)
      val recordsArray: JSONArray = json.getJSONArray("records")

      val recordLength = recordsArray.length()
      breakable {
        for (i <- 0 until recordLength) {
          val recordsObject = recordsArray.getJSONObject(i)

          val userName = "" //用户名 0
//          if(recordsObject.get("client_data").toString.nonEmpty){
//            val userName =json.getJSONObject(recordsObject.get("client_data").toString).get("Cookie")
//          }else{
//            val userName = "匿名用户"
//          }
          val destinationIp = recordsObject.get("req_flow_receiver_ip_addr") //目标IP 1
          val destinationPort = recordsObject.get("req_flow_receiver_port") //目标端口 2
          val sourceIP = recordsObject.get("req_flow_sender_ip_addr") //源IP 3
          if ("-".equals(sourceIP)) {
            break()
          }
          val sourcePort = recordsObject.get("req_flow_sender_port") //源端口 4
          //获取client_data里的值
          var clientDataobject = new JSONObject()
          try {
            clientDataobject = recordsObject.getJSONObject("client_data")
          } catch {
            case e: JSONException => clientDataobject = new JSONObject()
          }

          var refere = ""
          var protocol = ""
          var host = ""
          var url = ""
          var cmdAct = ""
          var useAgent = ""

          if (clientDataobject.length() > 0) {
            try {
              refere = clientDataobject.getString("Referer") //refre
              val httpsIndex = refere.indexOf(":", 1)
              protocol = refere.substring(0, httpsIndex + 3) //协议类型
            } catch {
              case e: Exception => {
                refere = ""
                protocol = "http://"
              }
            }

            try {
              host = clientDataobject.getString("Host") //域名 5
            } catch {
              case e: JSONException => host = ""
            }

            try {
              url = protocol + host + clientDataobject.getString("URL") //url 6
            } catch {
              case e: JSONException => url = ""
            }

            cmdAct = "KeLai" //操作

            try {
              useAgent = clientDataobject.getString("UserAgent") //User Agent
            } catch {
              case e: JSONException => useAgent = ""
            }

          }

          val reqTime = recordsObject.get("first_req_pkt_time").toString//每个请求包的时间点
          val resTime = recordsObject.get("first_res_pkt_time").toString//终端收到目标服务器响应的第一个数据响应包（第一个respone包）的时间点
          val lastReqTime = recordsObject.get("last_req_pkt_time").toString//请求结束时间
          val lastResTime = recordsObject.get("last_res_pkt_time").toString//响应结束时间
          val resTransPortTime = recordsObject.get("response_transport_time").toString//响应传输时间
          val transHandleTime = recordsObject.get("trans_handle_time").toString//交易处理时间
          val requestArrivaltime = KeLaiTimeUtils.getKeLaiTime(reqTime)
          //请求包的时间点
          val acctinPutoctets = recordsObject.get("response_byte") //发给用户的业务字节数
          val acctoutPutOctets = recordsObject.get("request_byte") //用户发出的业务字节数

          var httpStatus = -1L
          try {
            httpStatus = recordsObject.getString("bargain_recog_code_server").toLong //http状态码
          } catch {
            case e: Exception => httpStatus = -1L
          }
          val lastResponseArrivalTime = recordsObject.get("client_ip_addr") //网页最后1个Response消息时间点
          val firstRequestArrivalTime = recordsObject.get("server_ip_addr") //网页第1个GET请求时间点网页
          var contentLength = ""
          try {
            contentLength = recordsObject.getString("return_code_value") //网页大小
          } catch {
            case e: Exception => contentLength = ""
          }

          val firstResponseArrivalTime = KeLaiTimeUtils.getKeLaiTimeDefault(resTime, requestArrivaltime)
          //终端收到目标服务器响应的第一个数据响应包（第一个respone包）的时间点
          val synArrivalTime = KeLaiTimeUtils.getKeLaiTimeDefault(lastReqTime, requestArrivaltime) //终端向网页发起连接请求的时间点
          val ackArrivalTime = 0L //TCP三次握手的[ACK]的时间点
          val finArrivalTime = KeLaiTimeUtils.getKeLaiTimeDefault(lastResTime, requestArrivaltime) //终端向服务器端发送[FIN
          // .ACk]的时间点
          val dnsRequestArrivalTime = KeLaiTimeUtils.getKeLaiTimeDefault(resTransPortTime, requestArrivaltime)
          //DNS查询请求时间点
          val dnsResponseArrivalTime = KeLaiTimeUtils.getKeLaiTimeDefault(transHandleTime, requestArrivaltime)
          //DNS查询响应时间点
          val sql = recordsObject.get("res_status") //是否包含sql语句
          val isDownload = recordsObject.get("bargain_status") //是否是下载行为
          val isDownSuccess = recordsObject.get("total_packet") //是否下载成功
          val downFileSize = recordsObject.get("total_byte") //下载的文件大小
          val downFilename = "" //下载的文件名
          val packageType = "" //返回包类型
          val packagePath = "" //返回内容存储文件名


          val builder: StringBuilder = new StringBuilder
          builder.append(userName + "|" + destinationIp + "|" + destinationPort + "|" + sourceIP + "|" + sourcePort
            + "|" + host + "|" + url + "|" + cmdAct + "|" + refere + "|" + useAgent + "|" + requestArrivaltime + "|" +
            acctinPutoctets
            + "|" + acctoutPutOctets + "|" + httpStatus + "|" + lastResponseArrivalTime + "|" + firstRequestArrivalTime
            + "|" + contentLength + "|" + firstResponseArrivalTime + "|" + synArrivalTime + "|" + ackArrivalTime + "|"
            + finArrivalTime
            + "|" + dnsRequestArrivalTime + "|" + dnsResponseArrivalTime + "|" + protocol + "|" + sql + "|" +
            isDownload + "|" + isDownSuccess
            + "|" + downFileSize + "|" + downFilename+ "|" + packageType+ "|" + packagePath)
          flatMapSuccessMessage.add(1)
          out.collect(builder.toString)
        }
      }

    }
  }

}

