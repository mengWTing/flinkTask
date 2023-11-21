package cn.ffcs.is.mss.analyzer.flink.webbuganalyse

import cn.ffcs.is.mss.analyzer.bean.PermeateSoftwareFlowWarnEntity
import cn.ffcs.is.mss.analyzer.flink.sink.MySQLSink
import cn.ffcs.is.mss.analyzer.flink.webbuganalyse.utils.EnglishOrCode
import cn.ffcs.is.mss.analyzer.utils.{Constants, IniProperties, JsonUtil, KeLaiTimeUtils}
import org.apache.flink.api.common.functions.{RichFlatMapFunction, RichMapFunction}
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.functions.{AssignerWithPunctuatedWatermarks, ProcessFunction}
import org.apache.flink.streaming.api.scala.{StreamExecutionEnvironment, _}
import org.apache.flink.streaming.api.watermark.Watermark
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaConsumer, FlinkKafkaProducer}
import org.apache.flink.streaming.util.serialization.SimpleStringSchema
import org.apache.flink.util.Collector
import org.json.{JSONArray, JSONException, JSONObject}

import java.sql.Timestamp
import java.util.Properties
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.util.control.Breaks._

/**
 * @ClassName PermeateSoftwareFlowAnalyse
 * @author hanyu
 * @date 2022/9/16 10:57
 * @description
 * @update [no][date YYYY-MM-DD][name][description]
 * */
object PerPermeate {
  def main(args: Array[String]): Unit = {


    val confProperties = new IniProperties(args(0))
    //任务名称
    val jobName = confProperties.getValue(Constants.PERMEATE_SOFTWARE_FLOW_ANALYSE_CONFIG, Constants
      .PERMEATE_SOFTWARE_FLOW_ANALYSE_JOB_NAME)
    //kafka的服务地址
    val brokerList = confProperties.getValue(Constants.FLINK_COMMON_CONFIG, Constants
      .KAFKA_BOOTSTRAP_SERVERS)
    //flink消费的group.id
    val groupId = confProperties.getValue(Constants.PERMEATE_SOFTWARE_FLOW_ANALYSE_CONFIG, Constants
      .PERMEATE_SOFTWARE_FLOW_ANALYSE_GROUP_ID)

    //Source的并行度
    val sourceParallelism = confProperties.getIntValue(Constants.PERMEATE_SOFTWARE_FLOW_ANALYSE_CONFIG,
      Constants.PERMEATE_SOFTWARE_FLOW_ANALYSE_SOURCE_PARALLELISM)
    //算子并行度
    val dealParallelism = confProperties.getIntValue(Constants.PERMEATE_SOFTWARE_FLOW_ANALYSE_CONFIG, Constants
      .PERMEATE_SOFTWARE_FLOW_ANALYSE_DEAL_PARALLELISM)
    //Sink的并行度
    val sinkParallelism = confProperties.getIntValue(Constants.PERMEATE_SOFTWARE_FLOW_ANALYSE_CONFIG,
      Constants.PERMEATE_SOFTWARE_FLOW_ANALYSE_SINK_PARALLELISM)

    //kafka Source的topic
    val topic = confProperties.getValue(Constants.OPERATION_KELAI_FLINK_TO_KAFKA_CONFIG, Constants
      .OPERATION_KELAI_KAFKA_SOURCE_TOPIC)
    //kafka Sink的topic
    val kafkaSinkTopic = confProperties.getValue(Constants.PERMEATE_SOFTWARE_FLOW_ANALYSE_CONFIG, Constants
      .PERMEATE_SOFTWARE_FLOW_ANALYSE_SINK_TOPIC)
    //告警库topic
    val warningSinkTopic = confProperties.getValue(Constants.WARNING_FLINK_TO_DRUID_CONFIG, Constants
      .WARNING_TOPIC)

    //check pointing的间隔
    val checkpointInterval = confProperties.getLongValue(Constants.PERMEATE_SOFTWARE_FLOW_ANALYSE_CONFIG,
      Constants.PERMEATE_SOFTWARE_FLOW_ANALYSE_CHECKPOINT_INTERVAL)

    //时间窗口
    val timeWindows = confProperties.getLongValue(Constants.PERMEATE_SOFTWARE_FLOW_ANALYSE_CONFIG,
      Constants.PERMEATE_SOFTWARE_FLOW_ANALYSE_TIME_WINDOW)

    //全局变量
    val parameters: Configuration = new Configuration()
    //c3p0连接池配置文件路径
    parameters.setString(Constants.c3p0_CONFIG_PATH, confProperties.getValue(Constants.FLINK_COMMON_CONFIG, Constants
      .c3p0_CONFIG_PATH))
    //User Agent List
    parameters.setString(Constants.PERMEATE_SOFTWARE_FLOW_ANALYSE_USER_AGENT_LIST, confProperties.getValue(
      Constants.PERMEATE_SOFTWARE_FLOW_ANALYSE_CONFIG, Constants.PERMEATE_SOFTWARE_FLOW_ANALYSE_USER_AGENT_LIST))
    //Accept Str
    parameters.setString(Constants.PERMEATE_SOFTWARE_FLOW_ANALYSE_ACCEPT, confProperties.getValue(
      Constants.PERMEATE_SOFTWARE_FLOW_ANALYSE_CONFIG, Constants.PERMEATE_SOFTWARE_FLOW_ANALYSE_ACCEPT))
    //XFF flag
    parameters.setString(Constants.PERMEATE_SOFTWARE_FLOW_ANALYSE_XFF_FLAG, confProperties.getValue(
      Constants.PERMEATE_SOFTWARE_FLOW_ANALYSE_CONFIG, Constants.PERMEATE_SOFTWARE_FLOW_ANALYSE_XFF_FLAG))
    //cookie
    parameters.setString(Constants.PERMEATE_SOFTWARE_FLOW_ANALYSE_COOKIE_ICE, confProperties.getValue(
      Constants.PERMEATE_SOFTWARE_FLOW_ANALYSE_CONFIG, Constants.PERMEATE_SOFTWARE_FLOW_ANALYSE_COOKIE_ICE))
    //regex
    parameters.setString(Constants.PERMEATE_SOFTWARE_FLOW_ANALYSE_URL_REGEX, confProperties.getValue(
      Constants.PERMEATE_SOFTWARE_FLOW_ANALYSE_CONFIG, Constants.PERMEATE_SOFTWARE_FLOW_ANALYSE_URL_REGEX))
    //设置kafka消费者相关配置
    val props = new Properties()
    //设置kafka集群地址
    props.setProperty("bootstrap.servers", brokerList)
    //设置flink消费的group.id
    props.setProperty("group.id", groupId)

    //获取ExecutionEnvironment
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    //设置check pointing的间隔
    //    env.enableCheckpointing(checkpointInterval)
    //设置flink时间
    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)
    //设置flink全局变量
    env.getConfig.setGlobalJobParameters(parameters)

    //获取kafka消费者
    val consumer = new FlinkKafkaConsumer[String](topic, new SimpleStringSchema, props).setStartFromLatest()
    //告警数据写入云网平台的topic
    val producer = new FlinkKafkaProducer[String](brokerList, kafkaSinkTopic, new SimpleStringSchema())
    //将告警数据写入告警库的topic
    val warningProducer = new FlinkKafkaProducer[String](brokerList, warningSinkTopic, new
        SimpleStringSchema())
    //获取kafka数据
    val value = env.addSource(consumer).setParallelism(sourceParallelism)
      .filter(_.nonEmpty).setParallelism(dealParallelism)
      .flatMap(new OperationValueFlatMapFunction).setParallelism(dealParallelism)
      .filter(_.nonEmpty).setParallelism(dealParallelism)
      .assignTimestampsAndWatermarks(new AssignerWithPunctuatedWatermarks[String] {
        override def checkAndGetNextWatermark(lastElement: String, extractedTimestamp: Long): Watermark = {
          new Watermark(extractedTimestamp - 10000)
        }
        override def extractTimestamp(element: String, previousElementTimestamp: Long): Long = {
          element.split("\\|", -1)(3) .toLong
        }
      }).setParallelism(dealParallelism)

    val alertValue = value.map(new SrcAddDesIpMapFunction)
      .filter(_.!=(null))
      .keyBy(_._1)
      .timeWindow(Time.minutes(timeWindows), Time.minutes(timeWindows))
      .reduce((t1, t2) => {
        (t1._1, t1._2, t1._3,
          t1._4.++(t2._4), t1._5.++(t2._5), t1._6.++(t2._6), t1._7.++(t2._7),
          t1._8.++(t2._8), t1._9.++(t2._9), t1._10.++(t2._10), t1._11.++(t2._11))
      })
      .process(new WebVulnerabilityProcessFunction)
    //写入告警表
    alertValue.addSink(new MySQLSink)

    //    //写入发送至风控平台的kafka
    //    alertValue
    //      .map(o => {
    //        JsonUtil.toJson(o._1.asInstanceOf[PermeateSoftwareFlowWarnEntity])
    //      })
    //      .addSink(producer)
    //      .setParallelism(sinkParallelism)
    //

    //    alertValue.map(m => {
    //      var inPutKafkaValue = ""
    //      try {
    //        val entity = m._1.asInstanceOf[PermeateSoftwareFlowWarnEntity]
    //        inPutKafkaValue = entity.getUsername + "|" + entity.getAlertType + "|" + entity.getAlerttime + "|" +
    //          "" + "|" + "" + "|" + "" + "|" +
    //          "" + "|" + entity.getSourceip + "|" + "" + "|" +
    //          entity.getDesip + "|" + "" + "|" + "" + "|" +
    //          "" + "|" + "" + "|" + ""
    //      } catch {
    //        case e: Exception => {
    //        }
    //      }
    //      inPutKafkaValue
    //    }).filter(_.nonEmpty).addSink(warningProducer).setParallelism(sinkParallelism)

    env.execute(jobName)
  }


  class WebVulnerabilityProcessFunction extends ProcessFunction[(String, String, String, mutable.HashSet[String],
    mutable.HashSet[String], mutable.HashSet[String], mutable.HashSet[String], mutable.HashSet[String],
    mutable.HashSet[String], mutable.HashSet[String], mutable.HashSet[String]), (Object, Boolean)] {

    var usrAgentAb = new mutable.ArrayBuffer[String]()
    var acceptAb = new mutable.ArrayBuffer[String]()
    var xffFlagAb = new mutable.ArrayBuffer[String]()
    var contentLengthAb = new mutable.ArrayBuffer[String]()
    var usrAgentStr = ""
    var acceptStr = ""
    var xffFlagStr = ""
    var cookieStr = ""
    var urlRegex = ""

    override def open(parameters: Configuration): Unit = {
      val globConf = getRuntimeContext.getExecutionConfig.getGlobalJobParameters.asInstanceOf[Configuration]
      usrAgentStr = globConf.getString(Constants.PERMEATE_SOFTWARE_FLOW_ANALYSE_USER_AGENT_LIST, "")
      acceptStr = globConf.getString(Constants.PERMEATE_SOFTWARE_FLOW_ANALYSE_ACCEPT, "")
      xffFlagStr = globConf.getString(Constants.PERMEATE_SOFTWARE_FLOW_ANALYSE_XFF_FLAG, "")
      cookieStr = globConf.getString(Constants.PERMEATE_SOFTWARE_FLOW_ANALYSE_COOKIE_ICE, "")
      urlRegex = globConf.getString(Constants.PERMEATE_SOFTWARE_FLOW_ANALYSE_URL_REGEX, "")
      for (i <- usrAgentStr.split("\\|", -1)) {
        usrAgentAb.append(i)
      }

      for (i <- acceptStr.split("\\|", -1)) {
        acceptAb.append(i)
      }

      for (i <- xffFlagStr.split("\\|", -1)) {
        xffFlagAb.append(i)
      }
      contentLengthAb.append("16")
      contentLengthAb.append("5740")
      contentLengthAb.append("5720")

    }


    override def processElement(value: (String, String, String, mutable.HashSet[String], mutable.HashSet[String],
      mutable.HashSet[String], mutable.HashSet[String], mutable.HashSet[String], mutable.HashSet[String],
      mutable.HashSet[String], mutable.HashSet[String]), ctx: ProcessFunction[(String, String, String,
      mutable.HashSet[String], mutable.HashSet[String], mutable.HashSet[String], mutable.HashSet[String],
      mutable.HashSet[String], mutable.HashSet[String], mutable.HashSet[String], mutable.HashSet[String]),
      (Object, Boolean)]#Context, out: Collector[(Object, Boolean)]): Unit = {
      //(srcAddDesIp,userName,requestArrivalTime,hostSet,urlSet,cookieSet,xffSet,userAgentSet,acceptSet,contentLengthSet,cacheControlSet)
      val sourceIp = value._1.split("-", -1)(0)
      val desIp = value._1.split("-", -1)(1)
      val username = value._2
      val alertTime = value._3.toLong
      val hostSet = value._4
      val urlSet = value._5
      val cookieSet = value._6
      val xffSet = value._7
      val userAgentSet = value._8
      val acceptSet = value._9
      val contentLengthSet = value._10
      val cacheControlSet = value._11


      val entity = new PermeateSoftwareFlowWarnEntity()
      entity.setSourceip(sourceIp)
      entity.setDesip(desIp)
      entity.setUsername(username)
      entity.setAlerttime(new Timestamp(alertTime))
      entity.setAlertHost(hostSet.mkString("|"))
      val isXffAlert = getIsXffInject(xffSet, xffFlagAb)
      val urlIsJavaCode = getIsJavaCode(urlSet)
      val isIceScorpion = getIsIceScorpionAlert(userAgentSet, usrAgentAb, acceptSet, acceptAb, cookieSet, cookieStr,
        contentLengthSet, contentLengthAb, cacheControlSet)
      val isGodzilla = getIsGodzilla(cookieSet, acceptSet, cacheControlSet)
      val isUrlWebShell = getUrlWebShellTrait(urlSet)
      val isShiroLeak = getIsShiroLeak(cookieSet)

      if (isXffAlert._1) {
        entity.setAlertType("XFF注入攻击")
        entity.setAlertXff(isXffAlert._2)
        out.collect(entity, true)
      } else if (urlIsJavaCode._1) {
        entity.setAlertType("struts2远程代码注入")
        entity.setAlertUrl(urlIsJavaCode._2)
        out.collect(entity, true)
      } else if (isGodzilla._1) {
        entity.setAlertType("疑似哥斯拉软件渗透")
        entity.setAlertCookie(isGodzilla._2)
        entity.setAlertAccept(isGodzilla._3)
        out.collect(entity, true)
      } else if (isIceScorpion._1) {
        entity.setAlertType("疑似冰蝎软件渗透")
        entity.setAlertUseragent(isIceScorpion._2)
        entity.setAlertAccept(isIceScorpion._3)
        entity.setAlertCookie(isIceScorpion._6)
        out.collect(entity, true)
      } else if (isUrlWebShell._1) {
        entity.setAlertType("Web-Shell")
        entity.setAlertUrl(isUrlWebShell._2)
        out.collect(entity, true)
      } else if (isShiroLeak._1) {
        entity.setAlertType("Shiro漏洞利用")
        entity.setAlertCookie(isShiroLeak._2)
        out.collect(entity, true)
      }

    }

    def getIsShiroLeak(cookieSet: mutable.HashSet[String]): (Boolean, String) = {
      var shiroTrait = (false, "")
      for (i <- cookieSet) {
        val cookieStr = i.toLowerCase().replaceAll(" ", "")
        if (cookieStr.contains("rememberme=deleteme") || cookieStr.contains("rememberme=1")) {
          shiroTrait = (true, i)
        }
      }
      shiroTrait
    }


    def getIsIceScorpionAlert(userAgentSet: mutable.HashSet[String], usrAgentAb: ArrayBuffer[String],
                              acceptSet: mutable.HashSet[String], acceptAb: ArrayBuffer[String],
                              cookieSet: mutable.HashSet[String], cookieStr: String,
                              contentLengthSet: mutable.HashSet[String], contentLengthAb: mutable.ArrayBuffer[String],
                              cacheControlSet: mutable.HashSet[String]): (Boolean, String, String, String, String, String) = {

      val UaTrait = getFirstSetContainsSecondAbFunction(userAgentSet, usrAgentAb)
      val AcceptTrait = getFirstSetContainsSecondAbFunction(acceptSet, acceptAb)
      val contentLengthTrait = getFirstSetContainsSecondAbFunction(contentLengthSet, contentLengthAb)

      var cacheControlTrait = (false, "")
      for (cacheControl <- cacheControlSet) {
        if (cacheControl.contains("no-cache")) {
          cacheControlTrait = (true, cacheControl)
        }
      }

      var cookieTrait = (false, "")
      for (cookie <- cookieSet) {
        val flagArr = cookieStr.split("\\|", -1)
        if (cookie.toLowerCase().contains(flagArr(0)) && cookie.toLowerCase().contains(flagArr(1))) {
          cookieTrait = (true, cookie)
        }
      }
      val tuple1 = (UaTrait._1, AcceptTrait._1, contentLengthTrait._1, cacheControlTrait._1, cookieTrait._1)

      val booleanArr = "true".r.findAllMatchIn(tuple1.toString()).toArray
      if (booleanArr.length > 2) {
        (true, UaTrait._2, AcceptTrait._2, contentLengthTrait._2, cacheControlTrait._2, cookieTrait._2)
      } else {
        (false, "", "", "", "", "")
      }


    }

    def getUrlWebShellTrait(urlSet: mutable.HashSet[String]): (Boolean, String) = {
      var urlTrait = (false, "")
      val urlRegexArr = urlRegex.split("\\^", -1)
      for (url <- urlSet) {
        if (urlRegexArr(0).r.findAllMatchIn(url.toLowerCase()).nonEmpty ||
          urlRegexArr(1).r.findAllMatchIn(url.toLowerCase()).nonEmpty) {
          urlTrait = (true, url)
        }
      }
      urlTrait
    }


    def getIsGodzilla(cookieSet: mutable.HashSet[String], acceptSet: mutable.HashSet[String],
                      cacheControlSet: mutable.HashSet[String]): (Boolean, String, String) = {
      val accept = "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,/;q=0.8"
      var cookieTrait = (false, "")
      var acceptTrait = (false, "")
      var cacheControlTrait = (false, "")

      for (cookie <- cookieSet) {
        if (cookie.endsWith(";")) {
          cookieTrait = (true, cookie)
        }
      }

      for (i <- acceptSet) {
        if (i.replaceAll(" ", "").contains(accept)) {
          acceptTrait = (true, i)
        }
      }

      for (cacheControl <- cacheControlSet) {
        if (cacheControl.replaceAll(" ", "").contains("no-store,no-cache,must-revalidate")) {
          cacheControlTrait = (true, cacheControl)
        }
      }

      if (cookieTrait._1 && (acceptTrait._1 || cacheControlTrait._1)) {
        (true, cookieTrait._2, acceptTrait._2)
      } else {
        (false, "", "")
      }

    }


    def getIsXffInject(xffSet: mutable.HashSet[String], xffFlagAb: ArrayBuffer[String]): (Boolean, String) = {
      getFirstSetContainsSecondAbFunction(xffSet, xffFlagAb)
    }

    def getFirstSetContainsSecondAbFunction(value: mutable.HashSet[String], flag: mutable.ArrayBuffer[String]): (Boolean, String) = {
      for (i <- flag) {
        for (j <- value) {
          if (j.replaceAll(" ", "").contains(i)) {
            return (true, j)
          }
        }
      }
      (false, "")
    }

    def getIsJavaCode(value: mutable.HashSet[String]): (Boolean, String) = {
      for (i <- value) {
        val urlStr = i.replaceAll("/", " ")
        val isJavaCode = EnglishOrCode.IsJavaCode(urlStr)
        if (isJavaCode) {
          return (true, i)
        }
      }
      (false, "")
    }

  }

  class SrcAddDesIpMapFunction extends RichMapFunction[String, (String, String, String, mutable.HashSet[String],
    mutable.HashSet[String], mutable.HashSet[String], mutable.HashSet[String], mutable.HashSet[String],
    mutable.HashSet[String], mutable.HashSet[String], mutable.HashSet[String])] {
    override def map(value: String): (String, String, String, mutable.HashSet[String],
      mutable.HashSet[String], mutable.HashSet[String], mutable.HashSet[String], mutable.HashSet[String],
      mutable.HashSet[String], mutable.HashSet[String], mutable.HashSet[String]) = {
      val hostSet = new mutable.HashSet[String]()
      val urlSet = mutable.HashSet[String]()
      val cookieSet = mutable.HashSet[String]()
      val xffSet = mutable.HashSet[String]()
      val userAgentSet = mutable.HashSet[String]()
      val acceptSet = mutable.HashSet[String]()
      val contentLengthSet = mutable.HashSet[String]()
      val cacheControlSet = mutable.HashSet[String]()
      val flowValue = value.split("\\|", -1)
      if (flowValue.length == 12) {
        val sourceIP = flowValue(0)
        val destinationIp = flowValue(1)
        val userName = flowValue(2)
        val requestArrivalTime = flowValue(3)
        val host = flowValue(4)
        val url = flowValue(5)
        val cookie = flowValue(6)
        val xff = flowValue(7)
        val useAgent = flowValue(8)
        val accept = flowValue(9)
        val contentLength = flowValue(10)
        val cacheControl = flowValue(11)

        val srcAddDesIp = sourceIP + "-" + destinationIp
        hostSet.+=(host)
        urlSet.+=(url)
        cookieSet.+=(cookie)
        xffSet.+=(xff)
        userAgentSet.+=(useAgent)
        acceptSet.+=(accept)
        contentLengthSet.+=(contentLength)
        cacheControlSet.+=(cacheControl)

        (srcAddDesIp, userName, requestArrivalTime, hostSet, urlSet, cookieSet, xffSet, userAgentSet, acceptSet,
          contentLengthSet, cacheControlSet)
      } else {
        null
      }

    }
  }

  class OperationValueFlatMapFunction extends RichFlatMapFunction[String, String] {
    override def flatMap(in: String, out: Collector[String]): Unit = {
      val json = new JSONObject(in)
      val recordsArray: JSONArray = json.getJSONArray("records")
      val recordLength = recordsArray.length()
      breakable {
        for (i <- 0 until recordLength) {
          val recordsObject = recordsArray.getJSONObject(i)
          val userName = "匿名用户" //用户名
          val destinationIp = recordsObject.get("req_flow_receiver_ip_addr") //目标IP 1
          val sourceIP = recordsObject.get("req_flow_sender_ip_addr") //源IP 3
          val reqTime = recordsObject.get("first_req_pkt_time").toString //每个请求包的时间点
          val requestArrivalTime = KeLaiTimeUtils.getKeLaiTime(reqTime)
          if ("-".equals(sourceIP)) {
            break()
          }
          //获取client_data里的值
          var refere = ""
          var protocol = ""
          var host = ""
          var url = ""
          var cookie = ""
          var useAgent = ""
          var XFF = ""
          var accept = ""
          var contentLength = ""
          var cacheControl = ""

          var clientDataobject = new JSONObject()
          try {
            clientDataobject = recordsObject.getJSONObject("client_data")
          } catch {
            case e: JSONException => clientDataobject = new JSONObject()
          }
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
              host = clientDataobject.getString("Host") //域名
            } catch {
              case e: JSONException => host = ""
            }

            try {
              url = protocol + host + clientDataobject.getString("URL") //url
            } catch {
              case e: JSONException => url = ""
            }

            try {
              cookie = clientDataobject.getString("Cookie") //Cookie
            } catch {
              case e: JSONException => cookie = ""
            }

            try {
              XFF = clientDataobject.getString("XFF") //XFF
            } catch {
              case e: JSONException => XFF = ""
            }

            try {
              useAgent = clientDataobject.getString("UserAgent") //User Agent
            } catch {
              case e: JSONException => useAgent = ""
            }

            try {
              accept = clientDataobject.getString("Accept") //Accept
            } catch {
              case e: JSONException => accept = ""
            }

            try {
              contentLength = clientDataobject.getString("ContentLength") //contentLength
            } catch {
              case e: JSONException => accept = ""
            }

            try {
              cacheControl = clientDataobject.getString("CacheControl") //cacheControl
            } catch {
              case e: JSONException => accept = ""
            }
          }

          val builder: StringBuilder = new StringBuilder
          builder.append(sourceIP + "|" + destinationIp + "|" + userName + "|" + requestArrivalTime + "|" + host + "|"
            + url + "|" + cookie + "|" + XFF + "|" + useAgent + "|" + accept + "|" + contentLength + "|" + cacheControl)
          out.collect(builder.toString)
        }

      }
    }
  }
}