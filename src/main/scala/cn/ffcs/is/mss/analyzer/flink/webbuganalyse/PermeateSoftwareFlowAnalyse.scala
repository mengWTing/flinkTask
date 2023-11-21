package cn.ffcs.is.mss.analyzer.flink.webbuganalyse

import java.net.URLDecoder
import java.sql.Timestamp
import java.time.Duration
import java.util.Properties

import cn.ffcs.is.mss.analyzer.bean.{AntSwordWarnEntity, PermeateSoftwareFlowWarnEntity}
import cn.ffcs.is.mss.analyzer.druid.model.scala.OperationModel
import cn.ffcs.is.mss.analyzer.flink.sink.{MySQLSink, Sink}
import cn.ffcs.is.mss.analyzer.flink.source.Source
import cn.ffcs.is.mss.analyzer.flink.webbuganalyse.utils.EnglishOrCode
import cn.ffcs.is.mss.analyzer.utils.{Constants, IniProperties, JsonUtil, KeLaiTimeUtils}
import org.apache.flink.api.common.eventtime.{SerializableTimestampAssigner, WatermarkStrategy}
import org.apache.flink.api.common.functions.{RichFlatMapFunction, RichMapFunction}
import org.apache.flink.configuration.{ConfigOptions, Configuration}
import org.apache.flink.streaming.api.functions.ProcessFunction
import org.apache.flink.streaming.api.scala.{StreamExecutionEnvironment, _}
import org.apache.flink.streaming.api.windowing.assigners.SlidingEventTimeWindows
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.util.Collector
import org.json.{JSONArray, JSONException, JSONObject}

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.util.control.Breaks._

/**
 * @ClassName PermeateSoftwareFlowAnalyse
 * @author hanyu
 * @date 2022/9/16 10:57
 * @description
 * @update [no][date YYYY-MM-DD][name][description]
 **/
object PermeateSoftwareFlowAnalyse {
  def main(args: Array[String]): Unit = {

    val confProperties = new IniProperties(args(0))
    //任务名称
    val jobName = confProperties.getValue(Constants.TEST_PERMEATE_SOFTWARE_FLOW_ANALYSE_CONFIG, Constants
      .TEST_PERMEATE_SOFTWARE_FLOW_ANALYSE_JOB_NAME)
    //kafka的服务地址
    val brokerList = confProperties.getValue(Constants.FLINK_COMMON_CONFIG, Constants
      .KAFKA_BOOTSTRAP_SERVERS)
    //flink消费的group.id
    val groupId = confProperties.getValue(Constants.TEST_PERMEATE_SOFTWARE_FLOW_ANALYSE_CONFIG, Constants
      .TEST_PERMEATE_SOFTWARE_FLOW_ANALYSE_GROUP_ID)

    //Source的并行度
    val sourceParallelism = confProperties.getIntValue(Constants.PERMEATE_SOFTWARE_FLOW_ANALYSE_CONFIG,
      Constants.PERMEATE_SOFTWARE_FLOW_ANALYSE_SOURCE_PARALLELISM)
    //算子并行度
    val dealParallelism = confProperties.getIntValue(Constants.PERMEATE_SOFTWARE_FLOW_ANALYSE_CONFIG, Constants
      .PERMEATE_SOFTWARE_FLOW_ANALYSE_DEAL_PARALLELISM)
    //Sink的并行度
    val sinkParallelism = confProperties.getIntValue(Constants.PERMEATE_SOFTWARE_FLOW_ANALYSE_CONFIG,
      Constants.PERMEATE_SOFTWARE_FLOW_ANALYSE_SINK_PARALLELISM)

    //kafka Source的topic -----渗透软件流+cs
    val permeateCsTopic = confProperties.getValue(Constants.OPERATION_KELAI_FLINK_TO_KAFKA_CONFIG, Constants
      .OPERATION_KELAI_KAFKA_SOURCE_TOPIC)
    //kafka source 的topic -----蚁剑
    val antSourceTopic = confProperties.getValue(Constants.OPERATION_FLINK_TO_DRUID_CONFIG,
      Constants.OPERATION_TOPIC)

    //kafka Sink的topic -----软件渗透流+cs
    val kafkaSinkTopicPerCs = confProperties.getValue(Constants.TEST_PERMEATE_SOFTWARE_FLOW_ANALYSE_CONFIG, Constants
      .TEST_PERMEATE_SOFTWARE_FLOW_ANALYSE_SINK_TOPIC)
    //kafka Sink的topic -----蚁剑
    val kafkaSinkTopicAnt = confProperties.getValue(Constants.FLINK_ANT_SWORD_CONFIG, Constants
      .FLINK_ANT_SWORD_CONFIG_KAFKA_SINK_TOPIC)

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
    //文件系统类型
    parameters.setString(Constants.FILE_SYSTEM_TYPE, confProperties.getValue(Constants.FLINK_COMMON_CONFIG, Constants.FILE_SYSTEM_TYPE))
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

    //获取ExecutionEnvironment
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    //设置check pointing的间隔
    //    env.enableCheckpointing(checkpointInterval)
    //设置flink全局变量
    env.getConfig.setGlobalJobParameters(parameters)

    //获取kafka消费者 -----渗透软件流+cs
    val permeateCsConsumer = Source.kafkaSource(permeateCsTopic, groupId, brokerList)
    //获取kafka消费者 -----蚁剑检测
    val antConsumer = Source.kafkaSource(antSourceTopic, groupId+"ANT", brokerList)
    //告警数据写入云网平台的topic
    val producerPerCs = Sink.kafkaSink(brokerList, kafkaSinkTopicPerCs)
    val producerAnt = Sink.kafkaSink(brokerList, kafkaSinkTopicAnt)
    //将告警数据写入告警库的topic
    val warningProducer = Sink.kafkaSink(brokerList, warningSinkTopic)
    //获取kafka数据
    val valuePre = env.fromSource(permeateCsConsumer, WatermarkStrategy.noWatermarks(), "kafkaSource").setParallelism(sourceParallelism)
      .filter(_.nonEmpty).setParallelism(dealParallelism)
      .flatMap(new OperationValueFlatMapFunction).setParallelism(dealParallelism)
      .filter(_.nonEmpty).setParallelism(dealParallelism)
      .assignTimestampsAndWatermarks(
        WatermarkStrategy.forBoundedOutOfOrderness[String](Duration.ofSeconds(10))
          .withTimestampAssigner(new SerializableTimestampAssigner[String] {
            override def extractTimestamp(element: String, recordTimestamp: Long): Long = {
              element.split("\\|", -1)(3).toLong
            }
          })
      )

    //------------渗透软件识别-------------
    val alertValuePermeate = valuePre.map(new SrcAddDesIpMapFunction)
      .keyBy(_._1)
      .window(SlidingEventTimeWindows.of(Time.minutes(timeWindows), Time.minutes(timeWindows)))
      // (srcAddDesIp-String, userName-String, requestArrivalTime-String,
      // hostSet-HashSet, urlSet-HashSet, cookieSet-HashSet, xffSet-HashSet, userAgentSet-HashSet,
      // acceptSet-HashSet, contentLengthSet-HashSet, cacheControlSet-HashSet)
      .reduce((t1, t2) => {
        (t1._1, t1._2, t1._3,
          t1._4.++(t2._4), t1._5.++(t2._5), t1._6.++(t2._6), t1._7.++(t2._7),
          t1._8.++(t2._8), t1._9.++(t2._9), t1._10.++(t2._10), t1._11.++(t2._11))
      })
      .process(new WebVulnerabilityProcessFunction)

    //-----------cs工具异常使用识别------------
    val alertValueCobalt = valuePre.process(new CsProcessFunction).setParallelism(dealParallelism)

    //-----蚁剑工具异常检测-----
    val antStream = env.fromSource(antConsumer, WatermarkStrategy.noWatermarks(), "kafkaSource").setParallelism(sourceParallelism)
    val antSwardWarnStream = antStream
      .map(new RichMapFunction[String, (Option[OperationModel], String)] {
        override def map(value: String): (Option[OperationModel], String) = (OperationModel.getOperationModel(value), value)
      }).setParallelism(1)
      .filter(_._1.isDefined).setParallelism(dealParallelism)
      .map(t => (t._1.head, t._2)).setParallelism(dealParallelism)
      .process(new antSwardProcessFunction)

    //写入告警表
    alertValuePermeate.addSink(new MySQLSink)
    alertValueCobalt.map(_._1).addSink(new MySQLSink)
    antSwardWarnStream.map(_._1).addSink(new MySQLSink).uid("ant_sword").name("ant_sword")

    //-----permeate写入发送至风控平台的kafka-----
    alertValuePermeate
      .map(o => {
        JsonUtil.toJson(o._1.asInstanceOf[PermeateSoftwareFlowWarnEntity])
      })
      .sinkTo(producerPerCs)
      .setParallelism(sinkParallelism)

    alertValuePermeate.map(m => {
      var inPutKafkaValue = ""
      try {
        val entity = m._1.asInstanceOf[PermeateSoftwareFlowWarnEntity]
        inPutKafkaValue = entity.getUsername + "|" + entity.getAlertType + "|" + entity.getAlerttime + "|" +
          "" + "|" + "" + "|" + "" + "|" +
          "" + "|" + entity.getSourceip + "|" + "" + "|" +
          entity.getDesip + "|" + "" + "|" + "" + "|" +
          "" + "|" + "" + "|" + ""
      } catch {
        case e: Exception => {
        }
      }
      inPutKafkaValue
    }).filter(_.nonEmpty).sinkTo(warningProducer).setParallelism(sinkParallelism)

    //-----cs写入发送至风控平台的kafka-----
    alertValueCobalt
      .map(o => {
        JsonUtil.toJson(o._1._1.asInstanceOf[PermeateSoftwareFlowWarnEntity])
      })
      .sinkTo(producerPerCs)
      .setParallelism(sinkParallelism)

    alertValueCobalt.map(_._2).sinkTo(warningProducer).setParallelism(sinkParallelism)

    //-----蚁剑写入发送至风控平台的kafka-----
    antSwardWarnStream
      .map(o => {
        JsonUtil.toJson(o._1._1.asInstanceOf[AntSwordWarnEntity])
      })
      .sinkTo(producerAnt)
      .setParallelism(sinkParallelism)

    antSwardWarnStream.map(_._2).sinkTo(warningProducer).setParallelism(sinkParallelism)
    env.execute(jobName)
  }


  class WebVulnerabilityProcessFunction extends ProcessFunction[(String, String, String, mutable.HashSet[String],
    mutable.HashSet[String], mutable.HashSet[String], mutable.HashSet[String], mutable.HashSet[String],
    mutable.HashSet[String], mutable.HashSet[String], mutable.HashSet[String]), (Object, Boolean)] {
    // (srcAddDesIp-String, userName-String, requestArrivalTime-String,
    // hostSet-HashSet, urlSet-HashSet, cookieSet-HashSet, xffSet-HashSet, userAgentSet-HashSet,
    // acceptSet-HashSet, contentLengthSet-HashSet, cacheControlSet-HashSet)
    var usrAgentAb = new mutable.ArrayBuffer[String]()
    var acceptAb = new mutable.ArrayBuffer[String]()
    var xffFlagAb = new mutable.ArrayBuffer[String]()
    var contentLengthAb = new mutable.ArrayBuffer[String]()
    var usrAgentStr = "Mozilla/5.0(WindowsNT6.1;WOW64)AppleWebKit/535.1(KHTML,likeGecko)Chrome/14.0.835.163Safari/535.1|Mozilla/5.0(WindowsNT6.1;WOW64;rv:6.0)Gecko/20100101Firefox/6.0|Mozilla/5.0(WindowsNT6.1;WOW64)AppleWebKit/534.50(KHTML,likeGecko)Version/5.1Safari/534.50\"BOpera/9.80(WindowsNT6.1;U;zh-cn)Presto/2.9.168Version/11.5|Mozilla/5.0(compatible;MSIE9.0;WindowsNT6.1;Win64;x64;Trident/5.0;.NETCLR2.0.50727;SLCC2;.NETCLR3.5.30729;.NETCLR3.0.30729;MediaCenterPC6.0;InfoPath.3;.NET4.0C;TabletPC2.0;.NET4.0E)|Mozilla/4.0(compatible;MSIE8.0;WindowsNT6.1;WOW64;Trident/4.0;SLCC2;.NETCLR2.0.50727;.NETCLR3.5.30729;.NETCLR3.0.30729;MediaCenterPC6.0;.NET4.0C;InfoPath.3)|Mozilla/4.0(compatible;MSIE8.0;WindowsNT5.1;Trident/4.0;GTB7.0)|Mozilla/4.0(compatible;MSIE7.0;WindowsNT5.1),7|Mozilla/4.0(compatible;MSIE6.0;WindowsNT5.1;SV1)|Mozilla/5.0(Windows;U;WindowsNT6.1;)AppleWebKit/534.12(KHTML,likeGecko)Maxthon/3.0Safari/534.12|Mozilla/4.0(compatible;MSIE7.0;WindowsNT6.1;WOW64;Trident/5.0;SLCC2;.NETCLR2.0.50727;.NETCLR3.5.30729;.NETCLR3.0.30729;MediaCenterPC6.0;InfoPath.3;.NET4.0C;.NET4.0E)|Mozilla/4.0(compatible;MSIE7.0;WindowsNT6.1;WOW64;Trident/5.0;SLCC2;.NETCLR2.0.50727;.NETCLR3.5.30729;.NETCLR3.0.30729;MediaCenterPC6.0;InfoPath.3;.NET4.0C;.NET4.0E;SE2.XMetaSr1.0)|Mozilla/5.0(Windows;U;WindowsNT6.1;en-US)AppleWebKit/534.3(KHTML,likeGecko)Chrome/6.0.472.33Safari/534.3SE2.XMetaSr|Mozilla/5.0(compatible;MSIE9.0;WindowsNT6.1;WOW64;Trident/5.0;SLCC2;.NETCLR2.0.50727;.NETCLR3.5.30729;.NETCLR3.0.30729;MediaCenterPC6.0;InfoPath.3;.NET4.0C;.NET4.0E)|Mozilla/5.0(WindowsNT6.1)AppleWebKit/535.1(KHTML,likeGecko)Chrome/13.0.782.41Safari/535.1QQBrowser/6.9.11079.20|Mozilla/4.0(compatible;MSIE7.0;WindowsNT6.1;WOW64;Trident/5.0;SLCC2;.NETCLR2.0.50727;.NETCLR3.5.30729;.NETCLR3.0.30729;MediaCenterPC6.0;InfoPath.3;.NET4.0C;.NET4.0E)QQBrowser/6.9.11079|Mozilla/5.0(compatible;MSIE9.0;WindowsNT6.1;WOW64;Trident/5.0)"
    var acceptStr = "text/html,image/gif,image/jpeg,*;q=.2,*/*;q=.2|text/html,image/gif,image/jpeg,;q=.2,/;q=.2|text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9"
    var xffFlagStr = ""
    var cookieStr = ""
    var urlRegex = ""

    override def open(parameters: Configuration): Unit = {
      val globConf = getRuntimeContext.getExecutionConfig.getGlobalJobParameters.asInstanceOf[Configuration]
      xffFlagStr = globConf.getString(ConfigOptions.key(Constants.PERMEATE_SOFTWARE_FLOW_ANALYSE_XFF_FLAG).stringType().defaultValue(""))
      cookieStr = globConf.getString(ConfigOptions.key(Constants.PERMEATE_SOFTWARE_FLOW_ANALYSE_COOKIE_ICE).stringType().defaultValue(""))
      urlRegex = globConf.getString(ConfigOptions.key(Constants.PERMEATE_SOFTWARE_FLOW_ANALYSE_URL_REGEX).stringType().defaultValue(""))

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
      entity.setSourcePort("")
      entity.setMethod("")
      entity.setConnection("")

      val isXffAlert: ArrayBuffer[String] = getIsXffInject(xffSet, xffFlagAb)
      val urlIsJavaCode: ArrayBuffer[String] = getIsJavaCode(urlSet)
      val isIceScorpion: ArrayBuffer[ArrayBuffer[String]] = getIsIceScorpionAlert(userAgentSet, usrAgentAb, acceptSet, acceptAb, cookieSet, cookieStr,
        contentLengthSet, contentLengthAb, cacheControlSet)
      val isGodzilla: ArrayBuffer[ArrayBuffer[String]] = getIsGodzilla(cookieSet, acceptSet, cacheControlSet)
      val isUrlWebShell: ArrayBuffer[String] = getUrlWebShellTrait(urlSet, urlRegex)
      val isShiroLeak: ArrayBuffer[String] = getIsShiroLeak(cookieSet)

      if (isXffAlert.nonEmpty) {
        entity.setAlertType("XFF注入攻击")
        entity.setAlertXff(isXffAlert.toString().substring(12, isXffAlert.toString().length - 1))
        out.collect(entity, true)
      }
      if (urlIsJavaCode.nonEmpty) {
        entity.setAlertType("struts2远程代码注入")
        entity.setAlertUrl(urlIsJavaCode.toString().substring(12, urlIsJavaCode.toString().length - 1))
        out.collect(entity, true)
      }

      if (isGodzilla.nonEmpty) {
        entity.setAlertType("疑似哥斯拉软件渗透")
        entity.setAlertCookie(isGodzilla(0).toString().substring(12, isGodzilla(0).toString.length - 1))
        entity.setAlertAccept(isGodzilla(1).toString().substring(12, isGodzilla(1).toString.length - 1))
        out.collect(entity, true)
      }
      if (isIceScorpion != null) {
        entity.setAlertType("疑似冰蝎软件渗透")
        entity.setAlertUseragent(isIceScorpion(0).toString().substring(12, isIceScorpion(0).toString.length - 1))
        entity.setAlertAccept(isIceScorpion(1).toString().substring(12, isIceScorpion(1).toString.length - 1))
        entity.setAlertCookie(isIceScorpion(4).toString().substring(12, isIceScorpion(4).toString.length - 1))
        out.collect(entity, true)
      }
      if (isUrlWebShell.nonEmpty) {
        entity.setAlertType("Web-Shell")
        entity.setAlertUrl(isUrlWebShell.toString().substring(12, isUrlWebShell.toString().length - 1))
        out.collect(entity, true)
      }
      if (isShiroLeak.nonEmpty) {
        entity.setAlertType("Shiro漏洞利用")
        entity.setAlertCookie(isShiroLeak.toString().substring(12, isShiroLeak.toString().length - 1))
        out.collect(entity, true)
      }
    }

    def getIsShiroLeak(cookieSet: mutable.HashSet[String]): ArrayBuffer[String] = {
      val isShiroLeak = new ArrayBuffer[String]()
      for (i <- cookieSet) {
        val cookieStr = i.toLowerCase().replaceAll(" ", "")
        if (cookieStr.contains("rememberme=deleteme") || cookieStr.contains("rememberme=1")) {
          isShiroLeak.append(i)
        }
      }
      isShiroLeak
    }

    //userAgentSet, usrAgentAb, acceptSet, acceptAb, cookieSet, cookieStr,
    //contentLengthSet, contentLengthAb, cacheControlSet
    def getIsIceScorpionAlert(userAgentSet: mutable.HashSet[String], usrAgentAb: ArrayBuffer[String],
                              acceptSet: mutable.HashSet[String], acceptAb: ArrayBuffer[String],
                              cookieSet: mutable.HashSet[String], cookieStr: String,
                              contentLengthSet: mutable.HashSet[String], contentLengthAb: mutable.ArrayBuffer[String],
                              cacheControlSet: mutable.HashSet[String]): ArrayBuffer[ArrayBuffer[String]] = {

      val iceScorpionAlertBuffer = new ArrayBuffer[ArrayBuffer[String]]()

      val UaTrait: ArrayBuffer[String] = getFirstSetContainsSecondAbFunction(userAgentSet, usrAgentAb)
      val AcceptTrait: ArrayBuffer[String] = getFirstSetContainsSecondAbFunction(acceptSet, acceptAb)
      val contentLengthTrait: ArrayBuffer[String] = getFirstSetContainsSecondAbFunction(contentLengthSet, contentLengthAb)

      val cacheControlTrait: ArrayBuffer[String] = getIsCacheControl(cacheControlSet)
      val cookieTrait = getIsCookie(cookieSet)

      iceScorpionAlertBuffer.+=(UaTrait)
      iceScorpionAlertBuffer.+=(AcceptTrait)
      iceScorpionAlertBuffer.+=(contentLengthTrait)
      iceScorpionAlertBuffer.+=(cacheControlTrait)
      iceScorpionAlertBuffer.+=(cookieTrait)

      //一组有两个以上ArrayBuffer不为空时取
      var count = 0
      for (innerBf <- iceScorpionAlertBuffer) {
        if (innerBf.nonEmpty) {
          count += 1
        }
      }
      if (count > 2) {
        iceScorpionAlertBuffer
      } else {
        null
      }
    }

    //新增
    def getIsCacheControl(value: mutable.HashSet[String]): ArrayBuffer[String] = {
      val cacheControlBuffer = new ArrayBuffer[String]()
      for (cacheControl <- value) {
        if (cacheControl.contains("no-cache")) {
          cacheControlBuffer.append(cacheControl)
        }
      }
      cacheControlBuffer
    }

    //新增
    def getIsCookie(value: mutable.HashSet[String]): ArrayBuffer[String] = {
      val cookieBuffer = new ArrayBuffer[String]()
      for (cookie <- value) {
        val flagArr = cookieStr.split("\\|", -1)
        if (cookie.toLowerCase().contains(flagArr(0)) && cookie.toLowerCase().contains(flagArr(1))) {
          cookieBuffer.append(cookie)
        }
      }
      cookieBuffer
    }

    def getUrlWebShellTrait(urlSet: mutable.HashSet[String], urlRegex: String): ArrayBuffer[String] = {
      val urlWebShellBuffer = new ArrayBuffer[String]()
      //      var urlTrait = (false, "")
      //  \.(php|php5|jsp|asp|jspx|asa)\?(\w){1,20}=\d{2,10}^\.(PHP|jsp|asp|jspx|asa)
      val urlRegexArr = urlRegex.split("\\^", -1)
      for (url <- urlSet) {
        if (urlRegexArr(0).r.findAllMatchIn(url.toLowerCase()).nonEmpty ||
          urlRegexArr(1).r.findAllMatchIn(url.toLowerCase()).nonEmpty) {
          //          urlTrait = (true, url)
          urlWebShellBuffer.append(url)
        }
      }
      urlWebShellBuffer
    }

    def getIsGodzilla(cookieSet: mutable.HashSet[String], acceptSet: mutable.HashSet[String],
                      cacheControlSet: mutable.HashSet[String]): ArrayBuffer[ArrayBuffer[String]] = {

      val godzillaBuffer = new ArrayBuffer[ArrayBuffer[String]]()

      val cookieBuffer: ArrayBuffer[String] = cookie(cookieSet)
      val acceptBuffer: ArrayBuffer[String] = accept(acceptSet)
      val cacheBuffer: ArrayBuffer[String] = cache(cacheControlSet)

      val buffer_3 = new ArrayBuffer[ArrayBuffer[String]]()
      buffer_3.+=(cookieBuffer)
      buffer_3.+=(acceptBuffer)
      buffer_3.+=(cacheBuffer)

      // if (tuple3._1.nonEmpty && (tuple3._2.nonEmpty || tuple3._3.nonEmpty)) {
      if (buffer_3(0).nonEmpty && (buffer_3(1).nonEmpty || buffer_3(2).nonEmpty)) {
        godzillaBuffer.+=(cookieBuffer)
        godzillaBuffer.+=(acceptBuffer)
        //        (true, cookieTrait._2, acceptTrait._2)
      }
      godzillaBuffer
    }

    def cookie(value: mutable.HashSet[String]): ArrayBuffer[String] = {
      val cookieBuffer: ArrayBuffer[String] = new ArrayBuffer[String]()
      for (cookie <- value) {
        if (cookie.endsWith(";")) {
          cookieBuffer.append(cookie)
        }
      }
      cookieBuffer
    }

    def accept(value: mutable.HashSet[String]): ArrayBuffer[String] = {
      val acceptBuffer: ArrayBuffer[String] = new ArrayBuffer[String]()
      val accept = "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,/;q=0.8"
      for (i <- value) {
        if (i.replaceAll(" ", "").contains(accept)) {
          acceptBuffer.append(i)
        }
      }
      acceptBuffer
    }

    def cache(value: mutable.HashSet[String]): ArrayBuffer[String] = {
      val cacheBuffer = new ArrayBuffer[String]()
      for (cacheControl <- value) {
        if (cacheControl.replaceAll(" ", "").contains("no-store,no-cache,must-revalidate")) {
          cacheBuffer.append(cacheControl)
        }
      }
      cacheBuffer
    }

    def getIsXffInject(xffSet: mutable.HashSet[String], xffFlagAb: ArrayBuffer[String]): ArrayBuffer[String] = {
      var isXffInjectBuffer = new ArrayBuffer[String]()
      //todo 这里for循环删除
      //for (i <- xffSet) {
      val funBuffer = getFirstSetContainsSecondAbFunction(xffSet, xffFlagAb)
      if (funBuffer.nonEmpty) {
        //          isXffInjectBuffer.append(i)
        isXffInjectBuffer = funBuffer
      }
      //    }
      isXffInjectBuffer
    }

    def getFirstSetContainsSecondAbFunction(value: mutable.HashSet[String], flag: mutable.ArrayBuffer[String]):
    ArrayBuffer[String] = {

      val firstSetContainsSecondAbBuffer = new ArrayBuffer[String]()
      for (i <- flag) {
        for (j <- value) {
          //todo contains or equals
          if (j.replaceAll(" ", "").equals(i)) {
            //            firstSetContainsSecondAbBuffer.append(j)
            firstSetContainsSecondAbBuffer.append(i)
          }
        }
      }
      //保证了一个flag中的值只被追加一次, 拿到flag中匹配到的所有值(一个窗口中的set)
      firstSetContainsSecondAbBuffer
    }


    def getIsJavaCode(value: mutable.HashSet[String]): ArrayBuffer[String] = {
      val isJavaAb = new ArrayBuffer[String]()
      for (i <- value) {
        val urlStr = i.replaceAll("/", " ")
        val isJavaCode = EnglishOrCode.IsJavaCode(urlStr)
        if (isJavaCode) {
          isJavaAb.append(i)
        }
      }
      isJavaAb
    }

  }

  class SrcAddDesIpMapFunction extends RichMapFunction[String, (String, String, String, mutable.HashSet[String],
    mutable.HashSet[String], mutable.HashSet[String], mutable.HashSet[String], mutable.HashSet[String],
    mutable.HashSet[String], mutable.HashSet[String], mutable.HashSet[String])] {
    //map后结果为:
    // (srcAddDesIp-String, userName-String, requestArrivalTime-String,
    // hostSet-HashSet, urlSet-HashSet, cookieSet-HashSet, xffSet-HashSet, userAgentSet-HashSet,
    // acceptSet-HashSet, contentLengthSet-HashSet, cacheControlSet-HashSet)
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
      //      if (flowValue.length == 12) {
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
      //      }

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
          val sourcePort = recordsObject.get("req_flow_sender_port") //源端口
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
          var method = ""
          var connection = ""

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
              case e: JSONException => contentLength = ""
            }

            try {
              cacheControl = clientDataobject.getString("CacheControl") //cacheControl
            } catch {
              case e: JSONException => cacheControl = ""
            }
          }

          try {
            method = clientDataobject.getString("Method") //cacheControl
          } catch {
            case e: JSONException => method = ""
          }

          try {
            connection = clientDataobject.getString("Connection") //cacheControl
          } catch {
            case e: JSONException => connection = ""
          }

          val builder: StringBuilder = new StringBuilder()
          builder.append(sourceIP + "|" + destinationIp + "|" + userName + "|" + requestArrivalTime + "|" + host + "|"
            + url + "|" + cookie + "|" + XFF + "|" + useAgent + "|" + accept + "|" + contentLength + "|" + cacheControl+ "|" + sourcePort +"|" + method+"|" + connection+"|" + protocol)
          out.collect(builder.toString)
        }

      }
    }
  }

  class CsProcessFunction extends ProcessFunction[(String), ((Object, Boolean), String)] {

    //http-beacon通信中，默认使用get方法向/dpixel、/__utm.gif、/pixel.gif等地址发起请求，此集合为相关特征
    var beaconGet = new mutable.ArrayBuffer[String]()

    override def open(parameters: Configuration): Unit = {
      //全局配置
      val globalConf = getRuntimeContext.getExecutionConfig.getGlobalJobParameters.asInstanceOf[Configuration]
      //初始化beaconGet特征集合
      beaconGet = beaconGet:+"/ca"
      beaconGet = beaconGet:+"/dpixel"
      beaconGet = beaconGet:+"/__utm.gif"
      beaconGet = beaconGet:+"/pixel.gif"
      beaconGet = beaconGet:+"/dot.gif"
      beaconGet = beaconGet:+"/updates.rss"
      beaconGet = beaconGet:+"/fwlink"
      beaconGet = beaconGet:+"/cm"
      beaconGet = beaconGet:+"/cx"
      beaconGet = beaconGet:+"/match"
      beaconGet = beaconGet:+"/visit.js"
      beaconGet = beaconGet:+"/load"
      beaconGet = beaconGet:+"/push"
      beaconGet = beaconGet:+"/ptj"
      beaconGet = beaconGet:+"/j.ad"
      beaconGet = beaconGet:+"/ga.js"
      beaconGet = beaconGet:+"/en_US"
      beaconGet = beaconGet:+"/all.js"
      beaconGet = beaconGet:+"/activity"
      beaconGet = beaconGet:+"/IE9CompatViewList.xml"
    }

    /**
     * @title
     * CS流量特征分析检测
     * @description
     * * 1、检查端口是否为50050   cs默认端口为50050
     * * 2、通过checksum8 算法判断 是否等于 92 或93
     * * 3、检测http-beacon通信中，默认使用get方法向/dpixel、/__utm.gif、/pixel.gif等地址发起请求 判断url是否在这个集合内
     * * 4、疑似通过post方式向c2服务器发起数据回传请求CobaltStrike渗透，检测请求方式为POST请求，url包含“/submit.php?id=” connection连接包含"keep-alive"内容
     * 参考资料 https://paper.seebug.org/1922/
     * @author kimchie
     * @updateTime
     * @throws
     */
    override def processElement(value: (String), ctx: ProcessFunction[(String),
      ((Object, Boolean), String)]#Context, out: Collector[((Object, Boolean), String)]): Unit = {

      val flowValue = value.split("\\|", -1)
      if (flowValue.length == 16) {
        val sourceIp = flowValue(0)
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
        val sourcePort = flowValue(12)
        val method = flowValue(13)
        val connection = flowValue(14)
        val protocol = flowValue(15)


        var isCobaltStrike = false

        val entity = new PermeateSoftwareFlowWarnEntity()
        entity.setSourceip(sourceIp)
        entity.setDesip(destinationIp)
        entity.setUsername(userName)
        entity.setAlerttime(new Timestamp(requestArrivalTime.toLong))
        entity.setAlertHost(host)
        entity.setSourcePort(sourcePort)
        entity.setAlertXff(xff)
        entity.setAlertUrl(protocol+host+url)
        entity.setAlertAccept(accept)
        entity.setAlertUseragent(useAgent)
        entity.setMethod(method)
        entity.setConnection(connection)
        entity.setAlertCookie(cookie)
        //50050端口cs工具默认端口
        if(sourcePort!=null && sourcePort.equals("50050")){
          entity.setAlertType("端口疑似CobaltStrike渗透")
          isCobaltStrike = true
        }

        //强特征  checksum8
        //val  uri = "/Yle2"
        //http-beacon通信中，默认使用get方法向/dpixel、/__utm.gif、/pixel.gif等地址发起请求，
        // 同时请求头存在cookie字段并且值为base64编码后的非对算算法加密数据。
        if (isStager(url) || isStagerX64(url)) {
          entity.setAlertType("checksum8检测疑似CobaltStrike渗透")
          isCobaltStrike = true
        }
        if (beaconGet.contains(url.trim)) {
          entity.setAlertType("beaconGet检测疑似CobaltStrike渗透")
          isCobaltStrike = true
        }


        if (method=="POST" && url.indexOf("/submit.php?id=") > -1 && connection.indexOf("keep-alive") > -1) {
          entity.setAlertType("疑似通过post方式向c2服务器发起数据回传请求CobaltStrike渗透")
          isCobaltStrike = true
        }

        val inputKafkaValue = userName + "|" + "CS工具异常使用检测" + "|" + requestArrivalTime + "|" +
          "" + "|" + "" + "|" + "" + "|" +
          "" + "|" + sourceIp + "|" + sourcePort + "|" +
          destinationIp + "|" + "" + "|" + url + "|" +
          "" + "|" + "" + "|" + ""

        if (isCobaltStrike) {
          out.collect((entity, true), inputKafkaValue)
        }
      }
    }

    //强特征  checksum8
    def checksum8(text: String): Int = {
      if (text.length() < 4) {
        return 0
      }
      if (text.indexOf("/")!= text.lastIndexOf("/")) {
        return 0
      }
      var str = text.replace("/", "")
      var sum = 0
      for (x <- 0 to str.length()-1) {
        sum += str.charAt(x)
      }
      sum % 256
    }

    def isStager(uri: String): Boolean = {
      checksum8(uri) == 92L
    }

    def isStagerX64(uri: String): Boolean = {
      (checksum8(uri) == 93L) && (uri.matches("/[A-Za-z0-9]{4}"))
    }

  }

  class antSwardProcessFunction extends ProcessFunction[(OperationModel, String), ((Object, Boolean), String)] {
    var groupSplit: Char = _
    var kvSplit: Char = _
    var ruleList: List[String] = _

    override def open(parameters: Configuration): Unit = {

      val globalConf = getRuntimeContext.getExecutionConfig.getGlobalJobParameters.asInstanceOf[Configuration]
      groupSplit = globalConf.getInteger(ConfigOptions.key(Constants.XSS_INJECTION_GROUP_SPLIT).intType().defaultValue(0)).asInstanceOf[Char]
      kvSplit = globalConf.getInteger(ConfigOptions.key(Constants.XSS_INJECTION_KV_SPLIT).intType().defaultValue(0)).asInstanceOf[Char]
      ruleList = initCheckAntSwardDataRuleMap
    }

    override def processElement(value: (OperationModel, String), ctx: ProcessFunction[(OperationModel, String), ((Object, Boolean), String)]#Context,
                                out: Collector[((Object, Boolean), String)]): Unit = {
      var isAntSward = false
      val values = value._2.split("\\|", -1)
      val userAgent = values(9)
      val formValues = values(30) //请求内容
      val url = values(6)
      //判断userAgent是否是蚁剑
      if (userAgent.indexOf("antSword") > -1) {
        isAntSward = true
      }

      if (values.length >= 31) {
        if (formValues != null && formValues.length > 0) {
          for (formValue <- formValues.split(groupSplit)) {
            val kvValues = formValue.split(kvSplit)
            if (kvValues != null && kvValues.length == 2) {
              var urlValue = ""
              try {
                urlValue = URLDecoder.decode(kvValues(1).replaceAll("%(?![0-9a-fA-F]{2})", "%25")
                  , "utf-8")
              } catch {
                case e: Exception => {
                }
              }
              for (rule <- ruleList) {
                if (plusPercent(urlValue).indexOf(rule) > -1) {
                  isAntSward = true
                }
              }
            }
          }
        }
      }

      def plusPercent(str: String): String = {
        val stringBuffer = new StringBuffer()
        for (char <- str.toCharArray) {

          stringBuffer.append(char)
          if (char.equals('%')) {
            stringBuffer.append("%")
          }
        }
        stringBuffer.toString
      }

      if (isAntSward) {
        val antSwordWarnEntity = new AntSwordWarnEntity
        antSwordWarnEntity.setAlertTime(new Timestamp(value._1.timeStamp))
        antSwordWarnEntity.setUserName(value._1.userName)
        antSwordWarnEntity.setLoginSystem(value._1.loginSystem)
        antSwordWarnEntity.setDestinationIp(value._1.destinationIp)
        antSwordWarnEntity.setLoginPlace(value._1.loginPlace)
        antSwordWarnEntity.setSourceIp(value._1.sourceIp)
        antSwordWarnEntity.setHttpStatus(value._1.httpStatus)
        if (formValues.length > 1000) {
          antSwordWarnEntity.setFormValue(formValues.substring(0, 1000))
        } else {
          antSwordWarnEntity.setFormValue(formValues)
        }

        val inputKafkaValue = value._1.userName + "|" + "蚁剑工具异常使用行为检测: " + "|" + value._1.timeStamp + "|" +
          "" + "|" + value._1.loginSystem + "|" + "" + "|" +
          "" + "|" + value._1.sourceIp + "|" + "" + "|" +
          value._1.destinationIp + "|" + "" + "|" + url + "|" +
          value._1.httpStatus + "|" + "" + "|" + ""

        out.collect((antSwordWarnEntity, false), inputKafkaValue)
      }
    }

    /**
     * @title 初始化验证蚁剑规则的数据
     * @description
     * @author kimchie
     * @updateTime
     * @throws
     */
    def initCheckAntSwardDataRuleMap: List[String] = {
      var datas: List[String] = List("@ini_s")
      //base64
      datas = datas :+ "QGluaV9z"
      //chr
      datas = datas :+ "cHr(64).ChR(105).ChR(1 10).ChR(105).ChR(95).ChR(115)"
      //chr16
      datas = datas :+ "cHr(0x40).ChR(0x69).ChR(0x6e).ChR(0x69).ChR(0x5f).ChR(0x73)"
      //rot13
      datas = datas :+ "@vav_f"
      datas
    }
  }
}