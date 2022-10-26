package cn.ffcs.is.mss.analyzer.flink.keLaiFlow

import java.util.Properties
import cn.ffcs.is.mss.analyzer.utils.{Constants, IniProperties, JsonUtil, KeLaiTimeUtils}
import org.apache.flink.api.common.accumulators.LongCounter
import org.apache.flink.api.common.functions.{FlatMapFunction, RichFlatMapFunction}
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.TimeCharacteristic
import org.json.{JSONException, JSONObject}
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaConsumer, FlinkKafkaProducer}
import org.apache.flink.util.Collector


object DnsKeLai {
  def main(args: Array[String]): Unit = {

    //测试配置信息
    //  val confPath = "D:\\ffxm\\mss\\src\\main\\resources\\flink.ini"
    //  val confProperties: IniProperties = new IniProperties(confPath)

    val confProperties = new IniProperties(args(0))

    //任务名称
    val jobName = confProperties.getValue(Constants.DNS_KELAI_FLINK_TO_KAFKA_CONFIG, Constants
      .DNS_KELAI_JOB_NAME)

    //kafka的服务地址
    val brokerList = confProperties.getValue(Constants.FLINK_COMMON_CONFIG, Constants
      .KAFKA_BOOTSTRAP_SERVERS)
    //check pointing的间隔
    val checkpointInterval = confProperties.getLongValue(Constants.DNS_KELAI_FLINK_TO_KAFKA_CONFIG,
      Constants.DNS_KELAI_CHECKPOINT_INTERVAL)
    //flink消费的group.id
    val groupId = confProperties.getValue(Constants.DNS_KELAI_FLINK_TO_KAFKA_CONFIG, Constants
      .DNS_KELAI_GROUP_ID)

    //kafka的source topic
    val topic = confProperties.getValue(Constants.DNS_KELAI_FLINK_TO_KAFKA_CONFIG, Constants
      .DNS_KELAI_KAFKA_SOURCE_TOPIC)
    //kafka Source的并行度
    val kafkaSourceParallelism = confProperties.getIntValue(Constants.DNS_KELAI_FLINK_TO_KAFKA_CONFIG,
      Constants.DNS_KELAI_KAFKA_SOURCE_PARALLELISM)

    //kafka Sink的topic
    val kafkaSinkTopic = confProperties.getValue(Constants.DNS_KELAI_FLINK_TO_KAFKA_CONFIG, Constants
      .DNS_KELAI_KAFKA_SINK_TOPIC)

    //写入kafka的并行度
    val kafkaSinkParallelism = confProperties.getIntValue(Constants.DNS_KELAI_FLINK_TO_KAFKA_CONFIG,
      Constants.DNS_KELAI_KAFKA_SINK_PARALLELISM)

    //算子并行度
    val dealParallelism = confProperties.getIntValue(Constants.DNS_KELAI_FLINK_TO_KAFKA_CONFIG, Constants
      .DNS_KELAI_DEAL_PARALLELISM)
    //过滤字符串长度
    val dnsKelaiLength = confProperties.getIntValue(Constants.DNS_KELAI_FLINK_TO_KAFKA_CONFIG, Constants
      .DNS_KELAI_LENGTH)


    //    //设置kafka消费者相关配置
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
    //env.socketTextStream("192.168.1.105", 8888)
      .filter(_.length > dnsKelaiLength).setParallelism(dealParallelism)
      .flatMap(new DnsFlatMapFunction).setParallelism(dealParallelism)
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
      for (i <- 0 until recordLength) {
        val recordsObject = recordsArray.getJSONObject(i)
        var client_dataObject = new JSONObject()
        try {
          client_dataObject = recordsObject.getJSONObject("client_data")
        } catch {
          case e: JSONException => client_dataObject = new JSONObject()
        }

        var queryResult = ""
        if (client_dataObject.length() > 0) {
          //todo URL
          queryResult = client_dataObject.getString("URL")
        }

        val queryDomainName = recordsObject.get("stat_field_value1") //查询域名
        val replyCode = recordsObject.get("return_code_value") //回复代码
        val reqtime = recordsObject.get("first_req_pkt_time").toString//请求时间戳
        val restime = recordsObject.get("first_res_pkt_time").toString//响应时间戳
        val queryTime = KeLaiTimeUtils.getKeLaiTime(reqtime)
        val responseTime = KeLaiTimeUtils.getKeLaiTime(restime)
        val requestNumber = recordsObject.get("request_packet")//DNS的请求次数
        val responseNumber = recordsObject.get("response_packet")//响应数目
        val server_dataObject = recordsObject.getJSONObject("server_data")//响应内容
        val answerRrs = server_dataObject.get("StatusCode")
        val writetime = responseTime


        val builder: StringBuilder = new StringBuilder
        builder.append(queryDomainName + "|" + queryResult + "|" + replyCode + "|" + queryTime + "|" + responseTime
          + "|" + requestNumber + "|" + responseNumber + "|" + answerRrs + "|" + writetime)
        flatMapSuccessMessage.add(1)
        out.collect(builder.toString)
      }
    }
  }

}

