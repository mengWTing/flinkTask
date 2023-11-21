package cn.ffcs.is.mss.analyzer.flink.warn

import java.net.URLDecoder
import java.sql.Timestamp
import java.time.Duration
import java.util.Properties

import cn.ffcs.is.mss.analyzer.bean.{FscanWebLeakWarnEntity, MasterTcpWarnEntity}
import cn.ffcs.is.mss.analyzer.druid.model.scala.OperationModel
import cn.ffcs.is.mss.analyzer.flink.sink.{MySQLSink, Sink}
import cn.ffcs.is.mss.analyzer.flink.source.Source
import com.twitter.logging.config.BareFormatterConfig.intoOption
import org.apache.flink.configuration.Configuration
import cn.ffcs.is.mss.analyzer.utils.{Constants, IniProperties, JsonUtil}
import org.apache.flink.api.common.eventtime.{SerializableTimestampAssigner, WatermarkStrategy}
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.api.common.functions.{ReduceFunction, RichFlatMapFunction}
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.util.Collector
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.functions.ProcessFunction
import org.apache.flink.streaming.api.windowing.assigners.SlidingEventTimeWindows
import org.json.JSONObject

import scala.util.control.Breaks.{break, breakable}
import scala.collection.mutable

/**
 * @ClassName:
 * @Author: mengwenting
 * @Date: 2023/6/27 10:30
 * @Description:
 * @update:
 */
object FscanWebMasterWarn {
  def main(args: Array[String]): Unit = {
    //    val args0 = "G:\\ffcs\\4_Code\\mss\\src\\main\\resources\\flink.ini"
    //    val confProperties = new IniProperties(args0)
    val confProperties = new IniProperties(args(0))

    //任务的名字 -- web master放在一个 相同数据源
    val jobName = confProperties.getValue(Constants.FSCAN_ABNORMAL_ANALYSE_WEB_MASTER_CONFIG, Constants
      .FSCAN_ABNORMAL_ANALYSE_WEB_MASTER_CONFIG_JOB_NAME)
    //kafka Source的名字
    val kafkaSourceName = confProperties.getValue(Constants.FSCAN_ABNORMAL_ANALYSE_WEB_MASTER_CONFIG, Constants.FSCAN_ABNORMAL_ANALYSE_WEB_MASTER_CONFIG_KAFKA_SOURCE_NAME)
    //mysql sink的名字
    val sqlSinkName = confProperties.getValue(Constants.FSCAN_ABNORMAL_ANALYSE_WEB_MASTER_CONFIG, Constants.FSCAN_ABNORMAL_ANALYSE_WEB_MASTER_CONFIG_SQL_SINK_NAME)
    //kafka sink的名字
    val kafkaSinkName = confProperties.getValue(Constants.FSCAN_ABNORMAL_ANALYSE_WEB_MASTER_CONFIG, Constants.FSCAN_ABNORMAL_ANALYSE_WEB_MASTER_CONFIG_KAFKA_SINK_NAME)

    //并行度
    val sourceParallelism = confProperties.getIntValue(Constants.FSCAN_ABNORMAL_ANALYSE_WEB_MASTER_CONFIG,
      Constants.FSCAN_ABNORMAL_ANALYSE_WEB_MASTER_CONFIG_SOURCE_PARALLELISM)
    val sinkParallelism = confProperties.getIntValue(Constants.FSCAN_ABNORMAL_ANALYSE_WEB_MASTER_CONFIG,
      Constants.FSCAN_ABNORMAL_ANALYSE_WEB_MASTER_CONFIG_SINK_PARALLELISM)
    val dealParallelism = confProperties.getIntValue(Constants.FSCAN_ABNORMAL_ANALYSE_WEB_MASTER_CONFIG,
      Constants.FSCAN_ABNORMAL_ANALYSE_WEB_MASTER_CONFIG_DEAL_PARALLELISM)

    //check pointing的间隔
    val checkpointInterval = confProperties.getLongValue(Constants.FSCAN_ABNORMAL_ANALYSE_WEB_MASTER_CONFIG,
      Constants.FSCAN_ABNORMAL_ANALYSE_WEB_MASTER_CONFIG_CHECKPOINT_INTERVAL)

    //kafka的服务地址
    val brokerList = confProperties.getValue(Constants.FLINK_COMMON_CONFIG, Constants
      .KAFKA_BOOTSTRAP_SERVERS)
    //flink消费的group.id
    val groupId = confProperties.getValue(Constants.FSCAN_ABNORMAL_ANALYSE_WEB_MASTER_CONFIG, Constants
      .FSCAN_ABNORMAL_ANALYSE_WEB_MASTER_CONFIG_GROUP_ID)

    //kafka source 的topic--web异常 + 主机存活探测
    val webMasterTopic = confProperties.getValue(Constants.FSCAN_ABNORMAL_ANALYSE_WEB_MASTER_CONFIG, Constants
      .FSCAN_ABNORMAL_ANALYSE_WEB_MASTER_CONFIG_KAFKA_SOURCE_TOPIC)
    //kafka sink topic  --web
    val kafkaSinkWebTopic = confProperties.getValue(Constants.FSCAN_ABNORMAL_ANALYSE_WEB_MASTER_CONFIG,
      Constants.FSCAN_ABNORMAL_ANALYSE_WEB_KAFKA_SINK_TOPIC)
    //kafka sink topic  --master
    val kafkaSinkMasterTopic = confProperties.getValue(Constants.FSCAN_ABNORMAL_ANALYSE_WEB_MASTER_CONFIG,
      Constants.FSCAN_ABNORMAL_ANALYSE_MASTER_KAFKA_SINK_TOPIC)

    //写入kafka的并行度
    val kafkaSinkParallelism = confProperties.getIntValue(Constants.FSCAN_ABNORMAL_ANALYSE_WEB_MASTER_CONFIG,
      Constants.FSCAN_ABNORMAL_ANALYSE_WEB_MASTER_CONFIG_SQL_SINK_PARALLELISM)

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

    //获取kafka--Web消费者和Master消费者
    val webMasterConsumer = Source.kafkaSource(webMasterTopic, groupId, brokerList)
    //获取kafka生产者 -- web
    val producerWeb = Sink.kafkaSink(brokerList, kafkaSinkWebTopic)
    //获取kafka生产者 -- Master
    val producerMaster = Sink.kafkaSink(brokerList, kafkaSinkMasterTopic)

    //获取ExecutionEnvironment
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    //设置check pointing的间隔
    //env.enableCheckpointing(checkpointInterval)
    //设置Flink全局变量
    env.getConfig.setGlobalJobParameters(parameters)

    //获取Kafka数据流
    val webMasterStream = env.fromSource(webMasterConsumer, WatermarkStrategy.noWatermarks(), kafkaSourceName).setParallelism(sourceParallelism)


    val webLeakData = webMasterStream
      .filter(_.split("\\|", -1).length >= 33).setParallelism(dealParallelism)
      .flatMap(new FlowRichFlatMapFunction).setParallelism(dealParallelism)
      //时间戳,url,用户请求方式,用户请求内容
      .process(new WebValueProcessFunction).setParallelism(dealParallelism)


    val masterDetectData = webMasterStream.filter(_.split("\\|", -1).length >= 33)
      .flatMap(new MasterDetectFlatMapFunction).setParallelism(dealParallelism)
      .assignTimestampsAndWatermarks(
        WatermarkStrategy.forBoundedOutOfOrderness[(Long, String, String, String, String, String, Long, Long, Long)](Duration.ofSeconds(10))
          .withTimestampAssigner(new SerializableTimestampAssigner[(Long, String, String, String, String, String, Long, Long, Long)] {
            override def extractTimestamp(element: (Long, String, String, String, String, String, Long, Long, Long), recordTimestamp: Long): Long = {
              element._1
            }
          })
      ).setParallelism(dealParallelism)
      .keyBy(_._3)
      .window(SlidingEventTimeWindows.of(Time.seconds(30), Time.seconds(30)))
      //      //数据输出的格式: timeStamp, userName, sourceIp, 所有的sourcePort,
      //      // 所有的destinationIp, 所有的destinationPort, 总上行流量, 总下行流量, 主机ip探测次数
      //      .reduce((o1, o2) => (o2._1, o2._2, o2._3, o2._4, o1._5 + o2._5, o1._6 + o2._6, o1._7 + o2._7))
      .reduce(new DestinationIpReduceFunction).setParallelism(dealParallelism)
      .process(new MasterSurvivalProcessFunction).setParallelism(dealParallelism)


    val webValue: DataStream[(Object, Boolean)] = webLeakData.map(_._1)
    val alertKafkaWebValue = webLeakData.map(_._2)

    val masterValue: DataStream[(Object, Boolean)] = masterDetectData.map(_._1)
    val alertKafkaMasterValue = masterDetectData.map(_._2)

    webValue.addSink(new MySQLSink).uid("web Leak").name("web Leak")
      .setParallelism(sinkParallelism)

    masterValue.addSink(new MySQLSink).uid("master Detect").name("master Detect")
      .setParallelism(sinkParallelism)

    webValue
      .map(o => {JsonUtil.toJson(o._1.asInstanceOf[FscanWebLeakWarnEntity])})
      .sinkTo(producerWeb)
      .setParallelism(kafkaSinkParallelism)

    masterValue
      .map(o => {JsonUtil.toJson(o._1.asInstanceOf[MasterTcpWarnEntity])})
      .sinkTo(producerMaster)
      .setParallelism(kafkaSinkParallelism)

    //将web异常告警数据写入告警库topic
    val warningProducer = Sink.kafkaSink(brokerList, warningSinkTopic)

    alertKafkaWebValue.sinkTo(warningProducer).setParallelism(kafkaSinkParallelism)
    alertKafkaMasterValue.sinkTo(warningProducer).setParallelism(kafkaSinkParallelism)

    env.execute(jobName)
  }

  class FlowRichFlatMapFunction extends RichFlatMapFunction[String, (OperationModel, String, String, String)] { //url,用户请求方式,用户请求内容
    override def flatMap(value: String, out: Collector[(OperationModel, String, String, String)]): Unit = {
      val operationValue = OperationModel.getOperationModel(value).get
      val urlStr = URLDecoder.decode(OperationModel.getUrl(value).replaceAll("%(?![0-9a-fA-F]{2})", "%25"), "utf-8").toLowerCase()
      val requestMode = value.split("\\|", -1)(29) //请求模式(1,2,3)
      val requestContent = value.split("\\|", -1)(30) //请求内容
      if (operationValue.isDefined && !requestMode.equals("")) {
        val webInputValue = (operationValue,urlStr,requestMode,requestContent)
        out.collect(webInputValue)
      }
    }
  }

  class WebValueProcessFunction extends ProcessFunction[(OperationModel, String, String, String), ((Object, Boolean), String)]{
    //    var webRuleStr = """{"1": [{"name":"poc-yaml-74cms-sqli","path":"/index.php?m=&c=AjaxPersonal&a=company_focus&company_id[0]=match&company_id[1][0]=aaaaaaa\\\") and extractvalue(1,concat(0x7e,md5(99999999))) -- a"},{"name":"poc-yaml-springcloud-cve-2019-3799","path":"/test/pathtraversal/master/..%252F..%252F..%252F..%252F..%252F..%252Fetc%252fpasswd"},{"name":"poc-yaml-spring-cloud-cve-2020-5410","path":"/..%252F..%252F..%252F..%252F..%252F..%252F..%252F..%252F..%252F..%252F..%252Fetc%252Fpasswd%23/a"}],"3":[{"name":"poc-yaml-activemq-cve-2016-3088","path":"/fileserver/^.txt"},{"name":"poc-yaml-iis-put-getshell","path":"/^.txt"},{"name":"poc-yaml-hikvision-unauthenticated-rce-cve-2021-36260","path":"/SDK/webLanguage"}],"2":[{"name":"poc-yaml-draytek-cve-2020-8515","path":"/cgi-bin/mainfunction.cgi"},{"name":"poc-yaml-dlink-cve-2020-9376-dump-credentials","path":"/getcfg.php"},{"name":"poc-yaml-dlink-dsl-2888a-rce","path":"/"}]}"""
    var getsMap = mutable.HashMap[String, String]()
    var postsMap = mutable.HashMap[String, String]()
    var othersMap = mutable.HashMap[String, String]()
    var webRuleMap = mutable.HashMap[String, mutable.HashMap[String, String]]()

    override def open(parameters: Configuration): Unit = {

      val webRuleStr = """{"1":[{"name":"poc-yaml-74cms-sqli","path":"/index.php?m=^&company_id[0]=^&company_id[1][0]=^extractvalue^concat^md5"},{"name":"poc-yaml-springboot-cve-2021-21234","path":"/manage/log/view?filename=^&base="},{"name":"poc-yaml-springboot-cve-2021-21234","path":"/log/view?filename=^&base="},{"name":"poc-yaml-springboot-env-unauth","path":"/actuator/env"},{"name":"poc-yaml-springcloud-cve-2019-3799","path":"/pathtraversal/master/"},{"name":"poc-yaml-springcloud-cve-2019-3799","path":"/pathtraversal/master/^etc^passwd"},{"name":"poc-yaml-spring-cloud-cve-2020-5405","path":"etc/resolv.conf"},{"name":"poc-yaml-spring-cloud-cve-2020-5410","path":"etc^passwd#/a"},{"name":"poc-yaml-spring-core-rce","path":"/tomcatwar.jsp?data=^&word=echo"},{"name":"poc-yaml-spring-cve-2016-4977","path":"/oauth/authorize?response_type=$^&client_id=^&scope=^&redirect_uri="},{"name":"poc-yaml-supervisord-cve-2017-11610","path":"/RPC2"},{"name":"poc-yaml-tamronos-iptv-rce","path":"/api/ping?count=^&host=;echo^$(expr^&port=^&source=^&type="},{"name":"poc-yaml-telecom-gateway-default-password","path":"/manager/index.php"},{"name":"poc-yaml-tensorboard-unauth","path":"'/plugins_listing'"},{"name":"poc-yaml-terramaster-cve-2020-15568","path":"/include/exportUser.php?type=^&cla=^&func=^&opt="},{"name":"poc-yaml-terramaster-tos-rce-cve-2020-28188","path":"/include/makecvs.php?Event=^php^echo^md5^unlink^FILE^>>^www^.php^chmod^www"},{"name":"poc-yaml-thinkadmin-v6-readfile","path":"/admin.html?s=^/api.Update/get/encode/34392q302x2r1b37382p382x2r1b1a1a1b2x322s2t3c1a342w34"},{"name":"poc-yaml-thinkcmf-lfi","path":"/?a=^&templateFile="},{"name":"poc-yaml-thinkcmf-write-shell","path":"/index.php?a=^&content=%3C?php+file_put_contents^.php^%3C?php^echo"},{"name":"poc-yaml-tianqing-info-leak","path":"/dbstat/gettablessize"},{"name":"poc-yaml-tomcat-cve-2017-12615-rce","path":"}}.jsp'"},{"name":"poc-yaml-tomcat-cve-2018-11759","path":"/jkstatus;"},{"name":"poc-yaml-tomcat-cve-2018-11759","path":"/jkstatus;?cmd="},{"name":"poc-yaml-tomcat-manager-weak","path":"/manager/html"},{"name":"poc-yaml-tongda-meeting-unauthorized-access","path":"/general/calendar/arrange/get_cal_list.php?starttime=^&endtime=^&view="},{"name":"poc-yaml-tongda-oa-v11.9-api.ali.php-fileupload","path":"/inc/package/work.php?id=^/myoa/attach/approve_center/"},{"name":"tongda-user-session-disclosure","path":"/mobile/auth_mobi.php?isAvatar=^&uid=^&P_VER="},{"name":"poc-yaml-tpshop-directory-traversal","path":"/index.php/Home/uploadify/fileList?type="},{"name":"poc-yaml-tpshop-sqli","path":"/mobile/index/index2/id/1^select^from^concat^md5^rand^information_schema.tables^group^by"},{"name":"poc-yaml-tvt-nvms-1000-file-read-cve-2019-20085","path":"/Pages/login.htm"},{"name":"poc-yaml-tvt-nvms-1000-file-read-cve-2019-20085","path":"/windows/win.ini"},{"name":"poc-yaml-ueditor-cnvd-2017-20077-file-upload","path":"/ueditor/net/controller.ashx?action=^&encode="},{"name":"poc-yaml-uwsgi-cve-2018-7490","path":"etc/passwd"},{"name":"poc-yaml-vmware-vcenter-arbitrary-file-read","path":"/eam/vib?id="},{"name":"poc-yaml-vmware-vcenter-unauthorized-rce-cve-2021-21972","path":"/ui/vropspluginui/rest/services/uploadova"},{"name":"poc-yaml-vmware-vcenter-unauthorized-rce-cve-2021-21972","path":"/ui/vropspluginui/rest/services/getstatus"},{"name":"poc-yaml-weaver-ebridge-file-read","path":"/wxjsapi/saveYZJFile?fileName=^&downloadUrl="},{"name":"poc-yaml-weaver-ebridge-file-read","path":"/file/fileNoLogin/^var"},{"name":"poc-yaml-weaver-E-Cology-getSqlData-sqli","path":"/Api/portal/elementEcodeAddon/getSqlData?sql="},{"name":"poc-yaml-weaver-oa-eoffice-v9-upload-getshell","path":"/images/logo/logo-eoffice.php"},{"name":"poc-yaml-weblogic-cve-2020-14750","path":"/console/images/%2E./console.portal"},{"name":"poc-yaml-weblogic-ssrf","path":"/uddiexplorer/SearchPublicRegistries.jsp?rdoSearch=^&txtSearchname=^&txtSearchkey=&txtSearchfor=&selfor=^&btnSubmit=Search&operator="},{"name":"poc-yaml-weiphp-path-traversal","path":"/public/index.php/home/file/user_pics"},{"name":"poc-yaml-weiphp-path-traversal","path":"/public/uploads/picture/^img"},{"name":"poc-yaml-weiphp-sql","path":"/public/index.php/home/index/bind_follow/?publicid=^&is_ajax=^&uid[0]=^&uid[1]=^updatexml^concat^md5"},{"name":"poc-yaml-wordpress-cve-2019-19985-infoleak","path":"/wp-admin/admin.php?page=^&report=^&status=all"},{"name":"poc-yaml-wordpress-ext-adaptive-images-lfi","path":"/wp-content/plugins/adaptive-images/adaptive-images-script.php?adaptive-images-settings[source_file]=^/wp-config.php"},{"name":"poc-yaml-wordpress-ext-mailpress-rce","path":"/wp-content/plugins/mailpress/mp-includes/action.php?action=^&id="},{"name":"poc-yaml-wuzhicms-v410-sqli","path":"/api/sms_check.php?param=^updatexml(1,concat(0x7e,(SELECT^MD5"},{"name":"poc-yaml-xiuno-bbs-cvnd-2019-01348-reinstallation","path":"/install/"},{"name":"poc-yaml-xunchi-cnvd-2020-23735-file-read","path":"/backup/auto.php?password=^&path=^backup/auto.php"},{"name":"poc-yaml-yapi-rce","path":"/api/group/list"},{"name":"poc-yaml-yapi-rce","path":"/api/project/get?id="},{"name":"poc-yaml-yapi-rce","path":"/mock/^project_id"},{"name":"poc-yaml-yccms-rce","path":"/admin/?a=^;print("},{"name":"poc-yaml-yongyou-u8-oa-sqli","path":"/yyoa/common/js/menu/test.jsp?doType=^&S1=^SELECT^md5"},{"name":"poc-yaml-youphptube-encoder-cve-2019-5127","path":"/objects/getImage.php?base64Url=^&format=png"},{"name":"poc-yaml-youphptube-encoder-cve-2019-5129","path":"/objects/getSpiritsFromVideo.php?base64Url=^&format=jpg"},{"name":"poc-yaml-yungoucms-sqli","path":"/member/cart/Fastpay&shopid=-1^union^select^md5"},{"name":"poc-yaml-zabbix-authentication-bypass","path":"/zabbix.php?action=^&dashboardid="},{"name":"poc-yaml-zabbix-cve-2016-10134-sqli","path":"/jsrpc.php?type=^&mode=^&method=^&profileIdx=^&resourcetype=^&profileIdx2=updatexml^concat^md5"},{"name":"poc-yaml-zcms-v3-sqli","path":"/admin/cms_channel.php?del=^AND^(SELECT^FROM^COUNT^CONCAT^FLOOR(RAND^INFORMATION_SCHEMA.CHARACTER_SETS^GROUP^BY"},{"name":"poc-yaml-zeit-nodejs-cve-2020-5284-directory-traversal","path":"next/static/^/server/pages-manifest.json"},{"name":"poc-yaml-zeroshell-cve-2019-12725-rce","path":"/cgi-bin/kerbynet?Action=^&Section=NoAuthREQ^&User=^type="},{"name":"poc-yaml-zzcms-zsmanage-sqli","path":"/user/zsmanage.php"},{"name":"poc-yaml-74cms-sqli","path":"/index.php?m=&c=AjaxPersonal&a=company_focus&company_id[0]=match&company_id[1][0]^extractvalue(1,concat("},{"name":"poc-yaml-etouch-v2-sqli","path":"/upload/mobile/index.php?c=^&price_max=1.0^AND^(SELECT^FROM^COUNT^CONCAT^FLOOR(RAND^INFORMATION_SCHEMA.CHARACTER_SETS^GROUP^BY"},{"name":"poc-yaml-exchange-cve-2021-26855-ssrf","path":"/owa/auth/x.js"},{"name":"poc-yaml-e-zkeco-cnvd-2020-57264-read-file","path":"/iclock/ccccc/windows/win.ini"},{"name":"poc-yaml-ezoffice-downloadhttp.jsp-filedownload","path":"/defaultroot/site/templatemanager/downloadhttp.jsp?fileName=^/public/edit/jsp/config.jsp"},{"name":"poc-yaml-fangweicms-sqli","path":"/index.php?m=^&a=^&id=^UNION^ALL^SELECT^CONCAT^md5"},{"name":"poc-yaml-feifeicms-lfr","path":"/index.php?s=Admin-Data-down&id=^/Conf/config.php"},{"name":"poc-yaml-finecms-sqli","path":"/index.php?c=^&m=^&auth=^&param=action=sql^sql=^select^md5"},{"name":"poc-yaml-finereport-directory-traversal","path":"/report/ReportServer?op=^&cmd=get_geo_json&resourcepath=privilege.xml"},{"name":"poc-yaml-fineReport-v8.0-arbitrary-file-read","path":"/WebReport/ReportServer?op=^&cmd=get_geo_json&resourcepath=privilege.xml"},{"name":"poc-yaml-flexpaper-cve-2018-11686","path":"/php/setup.php?step=^&PDF2SWF_PATH=printf^%%"},{"name":"poc-yaml-flexpaper-cve-2018-11686","path":"/php/^pdf2swf"},{"name":"poc-yaml-flink-jobmanager-cve-2020-17519-lfi","path":"/jobmanager/logs/^etc^passwd"},{"name":"poc-yaml-fortigate-cve-2018-13379-readfile","path":"/remote/fgt_lang?lang=^/dev/cmdb/sslvpn_websession"},{"name":"poc-yaml-frp-dashboard-unauth","path":"/api/proxy/tcp"},{"name":"poc-yaml-gateone-cve-2020-35736","path":"/downloads/^etc/passwd"},{"name":"poc-yaml-glassfish-cve-2017-1000028-lfi","path":"/theme/META-INF/^/META-INF/MANIFEST.MF"},{"name":"poc-yaml-gocd-cve-2021-43287","path":"/go/add-on/business-continuity/api/plugin?folderName=&pluginName=^/etc/passwd"},{"name":"poc-yaml-gocd-cve-2021-43287","path":"/go/add-on/business-continuity/api/plugin?folderName=&pluginName=^/windows/win.ini"},{"name":"poc-yaml-go-pprof-leak","path":"/debug/pprof/"},{"name":"poc-yaml-go-pprof-leak","path":"/debug/pprof/goroutine?debug="},{"name":"poc-yaml-h2-database-web-console-unauthorized-access","path":"/h2-console"},{"name":"poc-yaml-h2-database-web-console-unauthorized-access","path":"/h2-console/^token"},{"name":"poc-yaml-h3c-imc-rce","path":"/imc/javax.faces.resource/dynamiccontent.properties.xhtml"},{"name":"poc-yaml-h3c-secparh-any-user-login","path":"/audit/gui_detail_view.php?token=^&id=^&uid=^&login=admin"},{"name":"poc-yaml-h5s-video-platform-cnvd-2020-67113-unauth","path":"/api^/GetSrc"},{"name":"poc-yaml-h5s-video-platform-cnvd-2020-67113-unauth","path":"/api^/GetDevice"},{"name":"poc-yaml-hadoop-yarn-unauth","path":"/cluster/info"},{"name":"poc-yaml-hanming-video-conferencing-file-read","path":"/register/toDownload.do?fileName=^/windows/win.ini"},{"name":"poc-yaml-hanming-video-conferencing-file-read","path":"/register/toDownload.do?fileName^/etc/passwd"},{"name":"poc-yaml-hikvision-cve-2017-7921","path":"/system/deviceInfo?auth="},{"name":"hikvision-gateway-data-file-read","path":"/login.php::$DATA"},{"name":"poc-yaml-hikvision-info-leak","path":"/config/user.xml"},{"name":"hikvision-showfile-file-read","path":"/serverLog/showFile.php?fileName=^/web/html/main.php"},{"name":"poc-yaml-influxdb-unauth","path":"/query?q=show^users"},{"name":"poc-yaml-influxdb-unauth","path":"/ping"},{"name":"poc-yaml-jboss-cve-2010-1871","path":"/admin-console/index.seam?actionOutcome=/pwn.xhtml?pwned="},{"name":"poc-yaml-jboss-unauth","path":"/jmx-console/"},{"name":"poc-yaml-jeewms-showordownbyurl-fileread","path":"/systemController/showOrDownByurl.do?down=&dbPath=^/etc/passwd"},{"name":"poc-yaml-jeewms-showordownbyurl-fileread","path":"/systemController/showOrDownByurl.do?down=&dbPath=^/Windows/win.ini"},{"name":"poc-yaml-jellyfin-file-read-cve-2021-21402","path":"/Audio/1/hls/^Windows^/stream.mp3/"},{"name":"poc-yaml-jenkins-cve-2018-1000600","path":"/org.jenkinsci.plugins.github.config.GitHubTokenCredentialsCreator/createTokenByPassword?apiUrl="},{"name":"poc-yaml-jenkins-cve-2018-1000861-rce","path":"/org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition/checkScriptCompile?value=^name=^root=^@Grab(group=^module=^version=^import^Payload"},{"name":"poc-yaml-jetty-cve-2021-28164","path":"/WEB-INF/web.xml"},{"name":"poc-yaml-jira-cve-2019-8442","path":"/META-INF/maven/com.atlassian.jira/atlassian-jira-webapp/pom.xml"},{"name":"poc-yaml-jira-cve-2019-8449","path":"/groupuserpicker?query=^&maxResults=^&showAvatar="},{"name":"poc-yaml-jira-cve-2019-11581","path":"/ContactAdministrators!default.jspa"},{"name":"poc-yaml-jira-cve-2020-14179","path":"/QueryComponent!Default.jspa"},{"name":"poc-yaml-jira-cve-2020-14181","path":"/ViewUserHover.jspa?username="},{"name":"poc-yaml-jira-ssrf-cve-2019-8451","path":"/plugins/servlet/gadgets/makeRequest?url=^://"},{"name":"poc-yaml-joomla-cve-2018-7314-sql","path":"/index.php?option=^&task=^&id=^&sessionid=^'^AND^EXTRACTVALUE^md5"},{"name":"poc-yaml-jumpserver-unauth-rce","path":"/users/connection-token/"},{"name":"poc-yaml-jumpserver-unauth-rce","path":"/users/connection-token/?user-only="},{"name":"poc-yaml-jumpserver-unauth-rce","path":"/authentication/connection-token/"},{"name":"poc-yaml-jumpserver-unauth-rce","path":"/authentication/connection-token/?user-only="},{"name":"poc-yaml-jupyter-notebook-unauthorized-access","path":"/terminals/3"},{"name":"poc-yaml-kibana-cve-2018-17246","path":"/console/api_server?sense_version=@SENSE_VERSION&apis=^/etc/passwd"},{"name":"poc-yaml-kibana-unauth","path":"/app/kibana"},{"name":"poc-yaml-kingdee-eas-directory-traversal","path":"/appmonitor/protected/selector/server_file/files?folder="},{"name":"poc-yaml-kingdee-eas-directory-traversal","path":"/appmonitor/protected/selector/server_file/files?folder=/&suffix="},{"name":"poc-yaml-kingsoft-v8-file-read","path":"/htmltopdf/downfile.php?filename=/windows/win.ini"},{"name":"poc-yaml-kong-cve-2020-11710-unauth","path":"/status"},{"name":"poc-yaml-kubernetes-unauth","path":"/api/v1/nodes"},{"name":"poc-yaml-kyan-network-monitoring-account-password-leakage","path":"/hosts"},{"name":"poc-yaml-lanproxy-cve-2021-3019-lfi","path":"/conf/config.properties"},{"name":"poc-yaml-laravel-improper-webdir","path":"/storage/logs/laravel.log"},{"name":"poc-yaml-maccms-rce","path":"/index.php?m=^&wd=^printf^md5"},{"name":"poc-yaml-metinfo-cve-2019-16996-sqli","path":"/admin/?n=^&c=^&a=^&app_type=^&id=^union^SELECT^limit"},{"name":"poc-yaml-metinfo-cve-2019-17418-sqli","path":"/admin/?n=^&c=^&a=^&editor=^&word=^&appno=^&site=admin"},{"name":"poc-yaml-metinfo-file-read","path":"/include/thumb.php?dir=http/^/config/config_db.php"},{"name":"poc-yaml-mpsec-isg1000-file-read","path":"/webui/?g=^&file_name=^/etc/passwd"},{"name":"poc-yaml-msvod-sqli","path":"/images/lists?cid=^)^ORDER^BY^desc^extractvalue^rand^concat"},{"name":"poc-yaml-myucms-lfr","path":"/index.php/bbs/index/download?url=/etc/passwd&name=^&local="},{"name":"poc-yaml-nagio-cve-2018-10735","path":"/nagiosql/admin/commandline.php?cname='^union^select^concat^md5"},{"name":"poc-yaml-nagio-cve-2018-10736","path":"/nagiosql/admin/info.php?key1='^union^select^concat^md5"},{"name":"poc-yaml-natshell-arbitrary-file-read","path":"/download.php?file=^/etc/passwd"},{"name":"poc-yaml-nextjs-cve-2017-16877","path":"/_next/^/etc/passwd"},{"name":"poc-yaml-nexusdb-cve-2020-24571-path-traversal","path":"/windows/win.ini"},{"name":"poc-yaml-nexus-default-password","path":"/service/local/authentication/login"},{"name":"poc-yaml-node-red-dashboard-file-read-cve-2021-3223","path":"/ui_base/js/^settings.js"},{"name":"poc-yaml-novnc-url-redirection-cve-2021-3654","path":"//baidu.com/"},{"name":"poc-yaml-ns-asg-file-read","path":"/admin/cert_download.php?file=^&certfile="},{"name":"poc-yaml-nsfocus-uts-password-leak","path":"/system/accountmanage/account"},{"name":"poc-yaml-nuuo-file-inclusion","path":"/css_parser.php?css="},{"name":"poc-yaml-odoo-file-read","path":"/base_import/static/c:/windows/win.ini"},{"name":"poc-yaml-odoo-file-read","path":"/base_import/static/etc/passwd"},{"name":"poc-yaml-openfire-cve-2019-18394-ssrf","path":"/getFavicon?host="},{"name":"poc-yaml-opentsdb-cve-2020-35476-rce","path":"/opentsdb_header.jpg"},{"name":"poc-yaml-opentsdb-cve-2020-35476-rce","path":"start=^&end=^&m=sum:^&o=&yrange=[0:system^style=^json"},{"name":"poc-yaml-pbootcms-database-file-download","path":"/data/pbootcms.db"},{"name":"poc-yaml-phpcms-cve-2018-19127s","path":"/type.php?template=^@unlink(file);echo^md5^GET^rss"},{"name":"poc-yaml-phpcms-cve-2018-19127","path":"/data/cache_template/rss.tpl.php?1="},{"name":"poc-yaml-phpmyadmin-cve-2018-12613-file-inclusion","path":"/index.php?target=^/etc/passwd"},{"name":"poc-yaml-phpok-sqli","path":"/api.php?c=^&f=^&token=^&id=^&sort=^and^extractvalue^concat^md5"},{"name":"poc-yaml-phpshe-sqli","path":"/include/plugin/payment/alipay/pay.php?id=pay^where^union^select^CONCAT^md5"},{"name":"poc-yaml-phpstudy-backdoor-rce","path":"/index.php"},{"name":"poc-yaml-phpstudy-nginx-wrong-resolve","path":"/index.html/.php"},{"name":"poc-yaml-phpstudy-nginx-wrong-resolve","path":"/index.html/.xxx"},{"name":"poc-yaml-powercreator-arbitrary-file-upload","path":"/ResourcePic/^ASPX"},{"name":"poc-yaml-prometheus-url-redirection-cve-2021-29622","path":"/new/newhttps:/baidu.com"},{"name":"poc-yaml-pulse-cve-2019-11510","path":"/dana-na/"},{"name":"poc-yaml-pulse-cve-2019-11510","path":"/dana/html5acc/guacamole/"},{"name":"poc-yaml-pulse-cve-2019-11510","path":"/etc/passwd?/dana/html5acc/guacamole/"},{"name":"poc-yaml-qilin-bastion-host-rce","path":"/get_luser_by_sshport.php?clientip=^echo^?php^echo^md5^unlink^FILE^/opt/freesvr/web/htdocs/freesvr/audit/^clientport="},{"name":"poc-yaml-qizhi-fortressaircraft-unauthorized","path":"/audit/gui_detail_view.php?token=^&id=^&uid=^or^print^chr^&login="},{"name":"poc-yaml-qnap-cve-2019-7192","path":"/photo/slideshow.php?album="},{"name":"poc-yaml-rabbitmq-default-password","path":"/api/whoami"},{"name":"poc-yaml-rails-cve-2018-3760-rce","path":"/assets/file:^/etc/passwd"},{"name":"poc-yaml-razor-cve-2018-8770","path":"/generate.php"},{"name":"poc-yaml-rconfig-cve-2019-16663","path":"/install/lib/ajaxHandlers/ajaxServerSettingsChk.php?rootUname=^expr"},{"name":"poc-yaml-resin-cnnvd-200705-315","path":"/web-inf/"},{"name":"poc-yaml-resin-inputfile-fileread-or-ssrf","path":"/resin-doc/resource/tutorial/jndi-appconfig/test?inputFile=^index.jsp"},{"name":"poc-yaml-resin-viewfile-fileread","path":"/resin-doc/viewfile/?file=index.jsp"},{"name":"poc-yaml-ruijie-eweb-rce-cnvd-2021-09650","path":"/guest_auth/^.php"},{"name":"poc-yaml-ruijie-uac-cnvd-2021-14536","path":"/login.php"},{"name":"poc-yaml-ruoyi-management-fileread","path":"/common/download/resource?resource=/profile/^/etc/passwd"},{"name":"poc-yaml-ruoyi-management-fileread","path":"/common/download/resource?resource=/profile/^/Windows/win.ini"},{"name":"poc-yaml-saltstack-cve-2021-25282-file-write","path":"/run"},{"name":"poc-yaml-sangfor-ad-download.php-filedownload","path":"/report/download.php?pdf=^/etc/hosts"},{"name":"poc-yaml-sangfor-ba-rce","path":"/tool/log/c.php?strip_slashes=^&host="},{"name":"poc-yaml-sangfor-edr-arbitrary-admin-login","path":"/ui/login.php?user=admin"},{"name":"poc-yaml-sangfor-edr-tool-rce","path":"/tool/log/c.php?strip_slashes=printf&host="},{"name":"poc-yaml-seacms-before-v992-rce","path":"/comment/api/index.php?gid=^&page=^&rlist^hex/@eval^GET"},{"name":"poc-yaml-seacms-before-v992-rce","path":"/data/mysqli_error_trace.php?_=printf^md5"},{"name":"poc-yaml-seacms-sqli","path":"/comment/api/index.php?gid=^&page=^&rlist[]=^extractvalue^concat_ws^select^md5"},{"name":"poc-yaml-secnet-ac-default-password","path":"/login.html"},{"name":"poc-yaml-seeyon-a6-employee-info-leak","path":"/DownExcelBeanServlet?contenttype=^&contentvalue=&state=^&per_id="},{"name":"poc-yaml-seeyon-a6-test-jsp-sql","path":"/common/js/menu/test.jsp?doType=^&S1=^SELECT^md5"},{"name":"poc-yaml-seeyon-ajax-unauthorized-access","path":"/seeyon/thirdpartyController.do.css^ajax.do"},{"name":"poc-yaml-seeyon-ajax-unauthorized-access","path":"/seeyon/personalBind.do.jpg^ajax.do?method=^&managerName=^&managerMethod="},{"name":"poc-yaml-seeyon-cnvd-2020-62422-readfile","path":"/seeyon/webmail.do?method=^&filename=^&filePath=^/conf/datasourceCtp.properties"},{"name":"poc-yaml-seeyon-oa-a8-m-information-disclosure","path":"/seeyon/management/index.jsp"},{"name":"poc-yaml-seeyon-oa-cookie-leak","path":"/seeyon/main.do?method="},{"name":"poc-yaml-seeyon-session-leak","path":"/ext/https/getSessionList.jsp?cmd="},{"name":"poc-yaml-seeyon-setextno-jsp-sql","path":"/ext/trafaxserver/ExtnoManage/setextno.jsp?user_ids=(^)^union^all^select^md5"},{"name":"poc-yaml-seeyon-wooyun-2015-0108235-sqli","path":"/ext/trafaxserver/downloadAtt.jsp?attach_ids=^and^1=2^union^select^md5"},{"name":"poc-yaml-seeyon-wooyun-2015-148227","path":"/NCFindWeb?service=^filename=WEB-INF/web.xml"},{"name":"poc-yaml-shiziyu-cms-apicontroller-sqli","path":"/index.php?s=^&goods_id=1^and^updatexml"},{"name":"poc-yaml-shopxo-cnvd-2021-15822","path":"/public/index.php?s=/index/qrcode/download/url/L2V0Yy9wYXNzd2Q="},{"name":"poc-yaml-showdoc-uploadfile","path":"/Public/Uploads/"},{"name":"poc-yaml-solarwinds-cve-2020-10148","path":"/web.config.i18n.ashx?l=en-US&v="},{"name":"poc-yaml-solr-cve-2019-0193","path":"/solr/admin/cores?wt=json"},{"name":"poc-yaml-solr-fileread","path":"/solr/admin/cores?indexInfo=^&wt=json"},{"name":"poc-yaml-solr-velocity-template-rce","path":"/select?q=^&wt=velocity&v.template=custom&v.template.custom="},{"name":"poc-yaml-sonarqube-cve-2020-27986-unauth","path":"/api/settings/values"},{"name":"poc-yaml-sonicwall-ssl-vpn-rce","path":"/cgi-bin/jarrewrite.sh"},{"name":"poc-yaml-spark-api-unauth","path":"/v1/submissions"},{"name":"poc-yaml-74cms-sqli-2","path":"/plus/ajax_officebuilding.php?act=^&key=^md5"},{"name":"poc-yaml-active-directory-certsrv-detect","path":"/certsrv/certrqad.asp"},{"name":"poc-yaml-activemq-cve-2016-3088","path":"/admin/test/index.jsp"},{"name":"poc-yaml-activemq-cve-2016-3088","path":"/api/^.jsp"},{"name":"poc-yaml-alibaba-canal-info-leak","path":"/api/v1/canal/config/1/1"},{"name":"poc-yaml-alibaba-nacos-v1-auth-bypass","path":"/nacos/v1/auth/users?pageNo=^&pageSize="},{"name":"poc-yaml-amtt-hiboss-server-ping-rce","path":"/manager/radius/server_ping.php?ip=^echo^md5^unlink^FILE^id="},{"name":"poc-yaml-apache-ambari-default-password","path":"/users/admin?fields=^,privileges/PrivilegeInfo/cluster_name,privileges/PrivilegeInfo/permission_name"},{"name":"poc-yaml-apache-flink-upload-rce","path":"/jars"},{"name":"poc-yaml-apache-httpd-cve-2021-40438-ssrf","path":"unix:^http://"},{"name":"poc-yaml-apache-httpd-cve-2021-41773-path-traversal","path":"/cgi-bin/^/etc/passwd"},{"name":"poc-yaml-apache-httpd-cve-2021-41773-path-traversal","path":"/icons/^/etc/passwd"},{"name":"poc-yaml-apache-kylin-unauth-cve-2020-13937","path":"/kylin/api/admin/config"},{"name":"poc-yaml-apache-nifi-api-unauthorized-access","path":"/nifi-api/flow/current-user"},{"name":"poc-yaml-aspcms-backend-leak","path":"/plug/oem/AspCms_OEMFun.asp"},{"name":"poc-yaml-bt742-pma-unauthorized-access","path":"/pma/"},{"name":"poc-yaml-cacti-weathermap-file-write","path":"/plugins/weathermap/editor.php?plug=^&mapname=^&action=^&param=&param2=&debug=^&node_name=&node_x=&node_y=&node_new_name=&node_label=&node_infourl=&node_hover=&node_iconfilename=--NONE--&link_name=&link_bandwidth_in=&link_bandwidth_out=&link_target=&link_width=&link_infourl=&link_hover=&map_title=^&map_legend=^&map_stamp=^&map_linkdefaultwidth="},{"name":"poc-yaml-cacti-weathermap-file-write","path":"/plugins/weathermap/configs/^.php"},{"name":"poc-yaml-cisco-cve-2020-3452-readfile","path":"CSCOT^oem-customization?app=^&type=^&platform=^&resource-type=^&name="},{"name":"poc-yaml-citrix-cve-2019-19781-path-traversal","path":"/vpns/cfg/smb.conf"},{"name":"poc-yaml-citrix-xenmobile-cve-2020-8209","path":"/jsp/help-sb-download.jsp?sbFileName=^/etc/passwd"},{"name":"poc-yaml-coldfusion-cve-2010-2861-lfi","path":"/CFIDE/administrator/enter.cfm?locale=^/lib/password.properties"},{"name":"poc-yaml-confluence-cve-2015-8399","path":"/spaces/viewdefaultdecorator.action?decoratorName"},{"name":"poc-yaml-confluence-cve-2021-26085-arbitrary-file-read","path":"/s/^/_/;/WEB-INF/web.xml"},{"name":"poc-yaml-consul-rexec-rce","path":"/agent/self"},{"name":"poc-yaml-coremail-cnvd-2019-16798","path":"/mailsms/s?func=^&dumpConfig=/"},{"name":"poc-yaml-couchcms-cve-2018-7662","path":"/includes/mysql2i/mysql2i.func.php"},{"name":"poc-yaml-couchcms-cve-2018-7662","path":"/addons/phpmailer/phpmailer.php"},{"name":"poc-yaml-couchdb-unauth","path":"/_config"},{"name":"poc-yaml-craftcms-seomatic-cve-2020-9757-rce","path":"/actions/seomatic/meta-container/meta-link-container/?uri="},{"name":"poc-yaml-craftcms-seomatic-cve-2020-9757-rce","path":"/actions/seomatic/meta-container/all-meta-containers?uri="},{"name":"poc-yaml-CVE-2017-7504-Jboss-serialization-RCE","path":"/jbossmq-httpil/HTTPServerILServlet"},{"name":"Spring-Cloud-CVE-2022-22947","path":"/actuator/gateway/routes/"},{"name":"poc-yaml-CVE-2022-22954-VMware-RCE","path":"/catalog-portal/ui/oauth/verify?error=&deviceUdid=^freemarker.template.utility.Execute"},{"name":"Confluence-CVE-2022-26134","path":"/${(#a=@org.apache.commons.io.IOUtils@toString(@java.lang.Runtime@getRuntime().exec(^id^).getInputStream()^utf-8^com.opensymphony.webwork.ServletActionContext@getResponse().setHeader^X-Cmd-Response"},{"name":"poc-yaml-dedecms-carbuyaction-fileinclude","path":"/plus/carbuyaction.php?dopost=^&code="},{"name":"poc-yaml-dedecms-cve-2018-6910","path":"/include/downmix.inc.php"},{"name":"poc-yaml-dedecms-cve-2018-7700-rce","path":"/tag_test_action.php?url=^&token=&partcode=^echo^md5^dede:field"},{"name":"poc-yaml-dedecms-guestbook-sqli","path":"/plus/guestbook.php"},{"name":"poc-yaml-dedecms-guestbook-sqli","path":"/plus/guestbook.php?action=admin&job=^&id=^&msg=^select^md5"},{"name":"poc-yaml-dedecms-guestbook-sqli","path":"/plus/guestbook.php"},{"name":"poc-yaml-dedecms-membergroup-sqli","path":"/member/ajax_membergroup.php?action=post&membergroup=^md5"},{"name":"poc-yaml-dedecms-url-redirection","path":"/plus/download.php?open=^&link="},{"name":"poc-yaml-discuz-ml3x-cnvd-2019-22239","path":"/forum.php"},{"name":"poc-yaml-discuz-v72-sqli","path":"/faq.php?action=^&gids^md5^from^mysql.user^limit^information_schema.tables^group^by"},{"name":"poc-yaml-discuz-wechat-plugins-unauth","path":"/plugin.php?id=^&ac="},{"name":"poc-yaml-discuz-wooyun-2010-080723","path":"/viewthread.php?tid="},{"name":"poc-yaml-django-CVE-2018-14574","path":"www.example.com"},{"name":"poc-yaml-dlink-cve-2020-25078-account-disclosure","path":"/config/getuser?index="},{"name":"poc-yaml-dlink-dsl-2888a-rce","path":"/page/login/login.html"},{"name":"poc-yaml-dlink-dsl-2888a-rce","path":"/cgi-bin/execute_cmd.cgi?timestamp=^&cmd="},{"name":"poc-yaml-docker-registry-api-unauth","path":"/v2/_catalog"},{"name":"poc-yaml-dotnetcms-sqli","path":"/user/City_ajax.aspx"},{"name":"poc-yaml-dotnetcms-sqli","path":"/user/City_ajax.aspx?CityId=^sys.fn_sqlvarbasetostr^HashBytes^MD5"},{"name":"poc-yaml-druid-monitor-unauth","path":"/druid/index.html"},{"name":"poc-yaml-duomicms-sqli","path":"/duomiphp/ajax.php?action=^&id=^&uid=^and^extractvalue^concat_ws^md5"},{"name":"poc-yaml-dvr-cve-2018-9995","path":"/device.rsp?opt=^&cmd="},{"name":"poc-yaml-weaver-oa-arbitrary-file-upload","path":"/page/exportImport/fileTransfer/^.jsp"},{"name":"poc-yaml-ecology-filedownload-directory-traversal","path":"/weaver/ln.FileDownload?fpath=^/ecology/WEB-INF/web.xml"},{"name":"poc-yaml-ecology-springframework-directory-traversal","path":"/weaver/org.springframework.web.servlet.ResourceServlet?resource=/WEB-INF/web.xml"},{"name":"poc-yaml-ecology-syncuserinfo-sqli","path":"/mobile/plugin/SyncUserInfo.jsp?userIdentifiers=^union^select"},{"name":"poc-yaml-ecology-v8-sqli","path":"/js/hrm/getdata.jsp?cmd=^&sql=select"},{"name":"poc-yaml-ecshop-collection-list-sqli","path":"/user.php?act="},{"name":"poc-yaml-ecshop-login-sqli","path":"/user.php?act=login"},{"name":"poc-yaml-eea-info-leak-cnvd-2021-10543","path":"/authenticationserverservlet"},{"name":"poc-yaml-elasticsearch-cve-2015-3337-lfi","path":"/_plugin/head/^/etc/passwd"},{"name":"poc-yaml-elasticsearch-cve-2015-5531","path":"/_snapshot/^/backdata%^fconfig^elasticsearch.yml"},{"name":"poc-yaml-elasticsearch-unauth","path":"/_cat"},{"name":"e-office-v9-upload-cnvd-2021-49104","path":"/images/logo/logo-eoffice.txt"},{"name":"e-office-v10-sql-inject","path":"/eoffice10/server/ext/system_support/leave_record.php?flow_id=^&run_id=^&table_field=^&table_field_name=^&max_rows="},{"name":"poc-yaml-etcd-unauth","path":"?quorum=^&recursive=^&sorted="}],"3":[{"name":"poc-yaml-gilacms-cve-2020-5515","path":"/admin/sql?query=^md5"},{"name":"poc-yaml-hikvision-unauthenticated-rce-cve-2021-36260","path":"/SDK/webLanguage"},{"name":"poc-yaml-hjtcloud-directory-file-leak","path":"/him/api/rest/V1.0/system/log/list?filePath="},{"name":"Hotel-Internet-Manage-RCE","path":"/manager/radius/server_ping.php?ip=^cat^/etc/passwd^id="},{"name":"poc-yaml-huawei-home-gateway-hg659-fileread","path":"/lib/^etc/passwd"},{"name":"poc-yaml-ifw8-router-cve-2019-16313","path":"/index.htm?PAGE=web"},{"name":"poc-yaml-ifw8-router-cve-2019-16313","path":"/action/usermanager.htm"},{"name":"poc-yaml-jetty-cve-2021-28164","path":"/WEB-INF/web.xml"},{"name":"poc-yaml-jira-cve-2019-8442","path":"/META-INF/maven/com.atlassian.jira/atlassian-jira-webapp/pom.xml"},{"name":"poc-yaml-jira-cve-2019-8449","path":"/rest/api/latest/groupuserpicker?query=^&maxResults=^&showAvatar="},{"name":"poc-yaml-jira-cve-2019-11581","path":"/secure/ContactAdministrators!default.jspa"},{"name":"poc-yaml-joomla-cve-2015-7297-sqli","path":"/index.php?option=^&view=^&list[ordering]=&item_id=^&type_id=^&list[select]=updatexml^concat^md5"},{"name":"poc-yaml-joomla-cve-2017-8917-sqli","path":"/index.php?option=^&view=^&layout=^&list[fullordering]=updatexml"},{"name":"poc-yaml-activemq-cve-2016-3088","path":"/fileserver/^.txt"},{"name":"poc-yaml-couchdb-cve-2017-12635","path":"users/org.couchdb.user:"},{"name":"poc-yaml-elasticsearch-cve-2015-5531","path":"/_snapshot/"},{"name":"poc-yaml-elasticsearch-cve-2015-5531","path":"/_snapshot/"},{"name":"poc-yaml-etcd-unauth","path":"/v2/keys/^?dir="},{"name":"poc-yaml-etcd-unauth","path":"/v2/keys/^prevExist=false"}],"2":[{"name":"poc-yaml-telecom-gateway-default-password","path":"/manager/login.php"},{"name":"poc-yaml-thinkphp5023-method-rce","path":"/index.php?s=captcha"},{"name":"tongda-insert-sql-inject","path":"/general/document/index.php/recv/register/insert"},{"name":"poc-yaml-tongda-oa-v11.9-api.ali.php-fileupload","path":"/mobile/api/api.ali.php"},{"name":"poc-yaml-tongda-oa-v11.9-api.ali.php-fileupload","path":"/inc/package/work.php?id=^/myoa/attach/approve_center/"},{"name":"tongda-v2017-uploadfile","path":"/module/ueditor/php/action_upload.php?action="},{"name":"poc-yaml-typecho-rce","path":"/install.php?finish"},{"name":"poc-yaml-vbulletin-cve-2019-16759-bypass","path":"/ajax/render/widget_tabbedcontainer_tab_panel"},{"name":"poc-yaml-vmware-vcenter-cve-2021-21985-rce","path":"/h5-vsan/rest/proxy/service/com.vmware.vsan.client.services.capability.VsanCapabilityProvider/getClusterCapabilityData"},{"name":"poc-yaml-vmware-vcenter-cve-2021-21985-rce","path":"/h5-vsan/rest/proxy/service/vmodlContext/loadVmodlPackages"},{"name":"poc-yaml-vmware-vcenter-cve-2021-21985-rce","path":"/h5-vsan/rest/proxy/service/systemProperties/getProperty"},{"name":"poc-yaml-vmware-vrealize-cve-2021-21975-ssrf","path":"/casa/nodes/thumbprints"},{"name":"poc-yaml-weaver-oa-eoffice-v9-upload-getshell","path":"/general/index/UploadFile.php?m=^&uploadType=^&userId="},{"name":"poc-yaml-weblogic-console-weak","path":"/console/j_security_check"},{"name":"poc-yaml-weblogic-cve-2017-10271","path":"/wls-wsat/CoordinatorPortType"},{"name":"poc-yaml-weblogic-cve-2019-2729-2","path":"/_async/AsyncResponseService"},{"name":"poc-yaml-webmin-cve-2019-15107-rce","path":"/password_change.cgi"},{"name":"poc-yaml-weiphp-path-traversal","path":"/public/index.php/material/Material/_download_imgage?media_id=^&picUrl=^/config/database.php"},{"name":"poc-yaml-wifisky-default-password-cnvd-2021-39012","path":"/login.php?action=^&type=admin"},{"name":"poc-yaml-wordpress-ext-mailpress-rce","path":"/wp-content/plugins/mailpress/mp-includes/action.php"},{"name":"poc-yaml-xdcms-sql","path":"/index.php?m=^&f="},{"name":"poc-yaml-yapi-rce","path":"/api/user/reg"},{"name":"poc-yaml-yapi-rce","path":"/api/project/add"},{"name":"poc-yaml-yapi-rce","path":"/api/interface/add"},{"name":"poc-yaml-yapi-rce","path":"/api/plugin/advmock/save"},{"name":"poc-yaml-yapi-rce","path":"/api/project/del"},{"name":"poc-yaml-yonyou-grp-u8-sqli","path":"/Proxy"},{"name":"poc-yaml-yonyou-nc-arbitrary-file-upload","path":"/servlet/FileReceiveServlet"},{"name":"poc-yaml-yonyou-nc-bsh-servlet-bshservlet-rce","path":"/servlet/~ic/bsh.servlet.BshServlet"},{"name":"poc-yaml-zabbix-default-password","path":"/index.php"},{"name":"poc-yaml-zimbra-cve-2019-9670-xxe","path":"/Autodiscover/Autodiscover.xml"},{"name":"poc-yaml-zzcms-zsmanage-sqli","path":"/user/zs.php?do="},{"name":"poc-yaml-eyou-email-system-rce","path":"/webadm/?q=^&action="},{"name":"poc-yaml-f5-cve-2021-22986","path":"/mgmt/tm/util/bash"},{"name":"poc-yaml-f5-cve-2021-22986","path":"/mgmt/tm/util/bash"},{"name":"poc-yaml-f5-tmui-cve-2020-5902-rce","path":"/tmui/login.jsp/^/tmui/locallb/workspace/fileRead.jsp"},{"name":"poc-yaml-flexpaper-cve-2018-11686","path":"/php/change_config.php"},{"name":"poc-yaml-gitlab-graphql-info-leak-cve-2020-26413","path":"/api/graphql"},{"name":"poc-yaml-gitlab-ssrf-cve-2021-22214","path":"/api/v4/ci/lint"},{"name":"poc-yaml-gitlist-rce-cve-2018-1000533","path":"/tree/a/search"},{"name":"poc-yaml-harbor-cve-2019-16097","path":"/api/users"},{"name":"poc-yaml-hikvision-intercom-service-default-password","path":"/authorize.action"},{"name":"poc-yaml-hjtcloud-arbitrary-fileread","path":"/fileDownload?action="},{"name":"poc-yaml-jenkins-unauthorized-access","path":"/script"},{"name":"poc-yaml-jira-cve-2019-11581","path":"/secure/ContactAdministrators.jspa"},{"name":"poc-yaml-joomla-component-vreview-sql","path":"/index.php?option=^&task="},{"name":"poc-yaml-joomla-ext-zhbaidumap-cve-2018-6605-sqli","path":"/index.php?option=^&no_html=^&format=^&task="},{"name":"poc-yaml-kingsoft-v8-default-password","path":"/inter/ajax.php?cmd="},{"name":"poc-yaml-kyan-network-monitoring-account-password-leakage","path":"/login.php"},{"name":"poc-yaml-landray-oa-custom-jsp-fileread","path":"/sys/ui/extend/varkind/custom.jsp"},{"name":"poc-yaml-laravel-cve-2021-3129","path":"/_ignition/execute-solution"},{"name":"poc-yaml-maccmsv10-backdoor","path":"/extend/Qcloud/Sms/Sms.php"},{"name":"poc-yaml-metinfo-cve-2019-16997-sqli","path":"/admin/?n=^&c=^&a="},{"name":"poc-yaml-minio-default-password","path":"/minio/webrpc"},{"name":"poc-yaml-mongo-express-cve-2019-10758","path":"/checkValid"},{"name":"poc-yaml-nagio-cve-2018-10737","path":"/nagiosql/admin/logbook.php"},{"name":"poc-yaml-nagio-cve-2018-10738","path":"/nagiosql/admin/menuaccess.php"},{"name":"poc-yaml-netentsec-icg-default-password","path":"/user/login/checkPermit"},{"name":"poc-yaml-netentsec-ngfw-rce","path":"/directdata/direct/router"},{"name":"poc-yaml-netgear-cve-2017-5521","path":"/passwordrecovered.cgi?id="},{"name":"poc-yaml-nexus-cve-2019-7238","path":"/service/extdirect"},{"name":"poc-yaml-nexus-cve-2020-10199","path":"/rest/beta/repositories/go/group"},{"name":"poc-yaml-nexus-cve-2020-10204","path":"/extdirect"},{"name":"poc-yaml-nhttpd-cve-2019-16278","path":"/bin/sh^HTTP/1.0"},{"name":"poc-yaml-nps-default-password","path":"/login/verify"},{"name":"poc-yaml-panabit-gateway-default-password","path":"/login/userverify.cgi"},{"name":"poc-yaml-pandorafms-cve-2019-20224-rce","path":"/pandora_console/index.php?sec=^&sec2=^/netflow/nf_live_view&pure="},{"name":"poc-yaml-php-cgi-cve-2012-1823","path":"/index.php?-d+allow_url_include=on^//input"},{"name":"poc-yaml-phpmyadmin-setup-deserialization","path":"/scripts/setup.php"},{"name":"poc-yaml-phpunit-cve-2017-9841-rce","path":"/vendor/phpunit/phpunit/src/Util/PHP/eval-stdin.php"},{"name":"poc-yaml-powercreator-arbitrary-file-upload","path":"/upload/UploadResourcePic.ashx?ResourceID="},{"name":"poc-yaml-pyspider-unauthorized-access","path":"/debug/pyspidervulntest/run"},{"name":"poc-yaml-qnap-cve-2019-7192","path":"/photo/p/api/album.php"},{"name":"poc-yaml-qnap-cve-2019-7192","path":"/photo/p/api/video.php"},{"name":"poc-yaml-rockmongo-default-password","path":"more=^&host=^&username=^&password=^&db=&lang=^&expire="},{"name":"poc-yaml-ruijie-eg-cli-rce","path":"/login.php"},{"name":"poc-yaml-ruijie-eg-cli-rce","path":"/cli.php?a="},{"name":"poc-yaml-ruijie-eg-file-read","path":"/download.php?a="},{"name":"poc-yaml-ruijie-eweb-rce-cnvd-2021-09650","path":"/guest_auth/guestIsUp.php"},{"name":"poc-yaml-ruijie-nbr1300g-cli-password-leak","path":"/WEB_VMS/LEVEL15/"},{"name":"poc-yaml-saltstack-cve-2020-16846","path":"/run"},{"name":"poc-yaml-samsung-wea453e-default-pwd","path":"/main.ehp"},{"name":"poc-yaml-samsung-wea453e-rce","path":"/tmp/^.txt"},{"name":"poc-yaml-sangfor-edr-cssp-rce","path":"/api/edr/sangforinter/v2/cssp/slog_client?token="},{"name":"poc-yaml-satellian-cve-2020-7980-rce","path":"/cgi-bin/libagent.cgi?type="},{"name":"poc-yaml-seacms-rce","path":"/search.php?print("},{"name":"poc-yaml-seacmsv645-command-exec","path":"/search.php?searchtype="},{"name":"poc-yaml-seacms-v654-rce","path":"/search.php"},{"name":"poc-yaml-secnet-ac-default-password","path":"/login.cgi"},{"name":"poc-yaml-seeyon-oa-a8-m-information-disclosure","path":"/seeyon/management/index.jsp"},{"name":"poc-yaml-seeyon-oa-cookie-leak","path":"/seeyon/thirdpartyController.do"},{"name":"poc-yaml-showdoc-default-password","path":"/server/index.php?s="},{"name":"poc-yaml-showdoc-uploadfile","path":"/index.php?s="},{"name":"poc-yaml-skywalking-cve-2020-9483-sqli","path":"/graphql"},{"name":"poc-yaml-solr-cve-2019-0193","path":"/solr/^/dataimport?command=^&debug=^&wt=^&indent=^&verbose=^&clean=^&commit=^&optimize=^&dataConfig=^name=^streamsrc^type=^loggerLevel=^;stream=^true^name=^datasource=^processor=^rootEntity=^forEach=^transformer=^column=^name="},{"name":"poc-yaml-solr-fileread","path":"/solr/^/config"},{"name":"poc-yaml-solr-fileread","path":"/solr/^/debug/dump?param="},{"name":"poc-yaml-solr-fileread","path":"/solr/^/config"},{"name":"poc-yaml-solr-fileread","path":"/solr/^/debug/dump?param="},{"name":"poc-yaml-solr-velocity-template-rce","path":"/solr/^/config"},{"name":"poc-yaml-spon-ip-intercom-ping-rce","path":"/php/ping.php"},{"name":"poc-yaml-alibaba-canal-default-password","path":"/api/v1/user/login"},{"name":"poc-yaml-alibaba-nacos-v1-auth-bypass","path":"/nacos/v^/auth/users?username=^&password="},{"name":"poc-yaml-apache-druid-cve-2021-36749","path":"/druid/indexer/v^/sampler?for="},{"name":"poc-yaml-apache-flink-upload-rce","path":"/jars/upload"},{"name":"poc-yaml-apache-httpd-cve-2021-41773-rce","path":"/cgi-bin/^/bin/sh"},{"name":"poc-yaml-apache-ofbiz-cve-2018-8033-xxe","path":"/webtools/control/xmlrpc"},{"name":"poc-yaml-apache-ofbiz-cve-2020-9496-xml-deserialization","path":"/webtools/control/xmlrpc"},{"name":"poc-yaml-chinaunicom-modem-default-password","path":"/cu.html"},{"name":"poc-yaml-citrix-cve-2020-8191-xss","path":"/menu/stapp"},{"name":"poc-yaml-citrix-cve-2020-8193-unauthorized","path":"/pcidss/report?type=^&sid=^&username=^&set="},{"name":"poc-yaml-confluence-cve-2019-3396-lfi","path":"/rest/tinymce/^/macro/preview"},{"name":"poc-yaml-confluence-cve-2021-26084","path":"/pages/createpage-entervariables.action?SpaceKey="},{"name":"Spring-Cloud-CVE-2022-22947","path":"/actuator/gateway/routes/"},{"name":"Spring-Cloud-CVE-2022-22947","path":"/actuator/gateway/refresh"},{"name":"poc-yaml-datang-ac-default-password-cnvd-2021-04128","path":"/login.cgi"},{"name":"poc-yaml-dlink-850l-info-leak","path":"/hedwig.cgi"},{"name":"poc-yaml-dlink-cve-2019-16920-rce","path":"/apply_sec.cgi"},{"name":"poc-yaml-dlink-cve-2019-17506","path":"/getcfg.php"},{"name":"poc-yaml-draytek-cve-2020-8515","path":"/cgi-bin/mainfunction.cgi"},{"name":"poc-yaml-drupal-cve-2014-3704-sqli","path":"/?q=^&destination="},{"name":"poc-yaml-drupal-cve-2018-7600-rce","path":"/user/register?element_parents=^&ajax_form=^&_wrapper_format="},{"name":"poc-yaml-drupal-cve-2018-7600-rce","path":"/?q=^&name[#post_render][]=^&name[#type]=^&name[#markup]="},{"name":"poc-yaml-drupal-cve-2019-6340","path":"/node/?_format="},{"name":"poc-yaml-weaver-oa-arbitrary-file-upload","path":"/page/exportImport/uploadOperation.jsp"},{"name":"poc-yaml-ecology-javabeanshell-rce","path":"/weaver/bsh.servlet.BshServlet"},{"name":"poc-yaml-ecology-validate-sqli","path":"/cpt/manage/validate.jsp?sourcestring="},{"name":"poc-yaml-ecology-workflowcentertreedata-sqli","path":"/mobile/browser/WorkflowCenterTreeData.jsp"},{"name":"poc-yaml-ecshop-cnvd-2020-58823-sqli","path":"/delete_cart_goods.php"},{"name":"poc-yaml-ecshop-rce","path":"/user.php"},{"name":"poc-yaml-elasticsearch-cve-2014-3120","path":"/_search"},{"name":"e-office-v9-upload-cnvd-2021-49104","path":"/general/index/UploadFile.php?m=^&uploadType=^&userId="}]}"""
      val json = new JSONObject(webRuleStr)
      //请求方式（1-GET、2-POST、3-其他）
      val getsMapAll = eachMap("1", json, getsMap)
      val postsMapAll = eachMap("2", json, postsMap)
      val othersMapAll = eachMap("3", json, othersMap)

      webRuleMap.+= ("1" -> getsMapAll)
      webRuleMap.+= ("2" -> postsMapAll)
      webRuleMap.+= ("3" -> othersMapAll)
      //数据结构为:
      //Map(2 -> Map(/getcfg.php -> poc-yaml-dlink-cve-2020-9376-dump-credentials, / -> poc-yaml-dlink-dsl-2888a-rce, /cgi-bin/mainfunction.cgi -> poc-yaml-draytek-cve-2020-8515), 1 -> Map(/..%252F..%252F..%252F..%252F..%252F..%252F..%252F..%252F..%252F..%252F..%252Fetc%252Fpasswd%23/a -> poc-yaml-spring-cloud-cve-2020-5410, /test/pathtraversal/master/..%252F..%252F..%252F..%252F..%252F..%252Fetc%252fpasswd -> poc-yaml-springcloud-cve-2019-3799, /index.php?m=&c=AjaxPersonal&a=company_focus&company_id[0]=match&company_id[1][0]=aaaaaaa\") and extractvalue(1,concat(0x7e,md5(99999999))) -- a -> poc-yaml-74cms-sqli), 3 -> Map(/^.txt -> poc-yaml-iis-put-getshell, /fileserver/^.txt -> poc-yaml-activemq-cve-2016-3088, /SDK/webLanguage -> poc-yaml-hikvision-unauthenticated-rce-cve-2021-36260))
    }


    def eachMap(ruleId: String, rule: JSONObject, eachMap: mutable.HashMap[String, String]): mutable.HashMap[String, String] = {
      val array = rule.getJSONArray(ruleId)
      val length = array.length()

      for (i <- 0 until length){
        val obj = array.getJSONObject(i)
        val name = obj.get("name")
        val path = obj.get("path")
        eachMap += (path.toString -> name.toString)
      }
      eachMap
    }

    override def processElement(value: (OperationModel, String, String, String), ctx: ProcessFunction[(OperationModel, String, String, String), ((Object, Boolean), String)]#Context,
                                out: Collector[((Object, Boolean), String)]): Unit = {

      //operationValue,urlStr,requestMode,requestContent
      val warnMessage = identify((webRuleMap, value))

      //(url, reqType, reqCont, pocName)
      val url = warnMessage._1
      val requestType = warnMessage._2
      val requestContent = warnMessage._3
      val pocName = warnMessage._4

      val entity = new FscanWebLeakWarnEntity
      entity.setAlertTime(new Timestamp(value._1.timeStamp))
      entity.setUserName(value._1.userName)
      entity.setSourceIp(value._1.sourceIp)
      entity.setSourcePort(value._1.sourcePort)
      entity.setDestinationIp(value._1.destinationIp)
      entity.setDestinationPort(value._1.destinationPort)
      entity.setLoginPlace(value._1.loginPlace)
      entity.setLoginSystem(value._1.loginSystem)
      entity.setLoginMajor(value._1.loginMajor)
      if ("1".equals(requestType)) {
        entity.setRequestType("GET")
      } else if ("2".equals(requestType)) {
        entity.setRequestType("POST")
      } else {
        entity.setRequestType("其他")
      }
      entity.setRequestContent(requestContent)
      entity.setUrl(url)
      entity.setAlertName(pocName)
      entity.setAlertType("Web漏洞异常行为")

      val inputKafkaValue = value._1.userName + "|" + "Fscan扫描工具检测Web漏洞用户行为异常" + "|" + value._1.timeStamp + "|" +
        value._1.loginMajor + "|" + value._1.loginSystem + "|" + "" + "|" +
        "" + "|" + value._1.sourceIp + "|" + value._1.sourcePort + "|" +
        value._1.destinationIp + "|" + value._1.destinationPort + "|" + url + "|" +
        "" + "|" + "" + "|" + ""
      //返回url, requestType, requestContent, pocName
      if (!url.equals("") && !requestType.equals("") && !pocName.equals("") && !requestContent.equals("")) {
        out.collect((entity, true), inputKafkaValue)
      }
    }

    def identify(in: (mutable.HashMap[String, mutable.HashMap[String, String]], (OperationModel, String, String, String))):
    (String, String, String, String) = {
      //operationValue,urlStr,requestMode,requestContent
      val ruleMap: mutable.HashMap[String, mutable.HashMap[String, String]] = in._1
      val ruleGetsMap: mutable.HashMap[String, String] = ruleMap("1")
      val rulePostsMap: mutable.HashMap[String, String] = ruleMap("2")
      val ruleOthersMap: mutable.HashMap[String, String] = ruleMap("3")

      val url = in._2._2
      val requestType = in._2._3
      val requestContent = in._2._4
      var fourValue = ("", "", "", "")

      //用url判断对应Map中的key值是否符合漏洞规则，符合输出warn
      val getWarn = urlMatchPath("1", requestType, requestContent, ruleGetsMap, url)
      val postWarn = urlMatchPath("2", requestType, requestContent, rulePostsMap, url)
      val otherWarn = urlMatchPath("3", requestType, requestContent, ruleOthersMap, url)

      if ("1".equals(requestType)){
        fourValue = getWarn
      }else if ("2".equals(requestType)){
        fourValue = postWarn
      }else {
        fourValue = otherWarn
      }
      fourValue
    }

    def urlMatchPath(ruleId: String, reqType: String, reqCont:String, map: mutable.HashMap[String, String], url: String): (String, String, String, String) = {
      var pocName = ""
      for (i <- map) {
        val pathRuleSplitValue = i._1.split("\\^", -1)
        val pathLength = pathRuleSplitValue.length
        var matchSubStr : Int = 0

        breakable {
          for (i <- 0 until pathLength) {
            if (!url.contains(pathRuleSplitValue(i))){
              break()
            }else {
              matchSubStr += 1
            }
          }
          if (matchSubStr.equals(pathLength)){
            pocName = i._2
          }
        }
      }
      (url, reqType, reqCont, pocName)
    }
  }

  class MasterDetectFlatMapFunction extends RichFlatMapFunction[String, (Long, String, String, String, String, String, Long, Long, Long)] {
    //timeStamp, userName, sourceIp, sourcePort, destinationIp, destinationPort, inputOctet, outputOctet ,connCount
    override def flatMap(value: String, out: Collector[(Long, String, String, String, String, String, Long, Long, Long)]): Unit = {
      val operationValue = OperationModel.getOperationModel(value).get
      if (operationValue.isDefined && operationValue.inputOctets / 1024 <= 0.1 && operationValue.outputOctets / 1024 <= 0.1){
        val data = (operationValue.timeStamp, operationValue.userName, operationValue.sourceIp, operationValue.sourcePort,
          operationValue.destinationIp, operationValue.destinationPort, operationValue.inputOctets, operationValue.outputOctets, operationValue.connCount)
        out.collect(data)
      }
    }
  }

  //timeStamp, userName, sourceIp, destinationIp, inputOctet, outputOctet ,connCount
  class MasterSurvivalProcessFunction extends ProcessFunction[(Long, String, String, String, String, String, Long, Long, Long), ((Object, Boolean), String)]{
    override def processElement(value: (Long, String, String, String, String, String, Long, Long, Long),
                                ctx: ProcessFunction[(Long, String, String, String, String, String, Long, Long, Long), ((Object, Boolean), String)]#Context,
                                out: Collector[((Object, Boolean), String)]): Unit = {
      val detectCount = value._9
      if (value.isDefined && detectCount > 600){
        val entity = new MasterTcpWarnEntity
        entity.setAlertTime(new Timestamp(value._1))
        entity.setUserName(value._2)
        entity.setSourceIp(value._3)
        entity.setSourcePorts(value._4)
        entity.setDestinationIps(value._5)
        entity.setDestinationPorts(value._6)
        entity.setInputOctets(value._7)
        entity.setOutputOctets(value._8)
        entity.setScanCount(value._9)

        val inputKafkaValue = value._2 + "|" + "Fscan扫描工具主机存活探测异常行为" + "|" + value._1 + "|" +
          "" + "|" + "" + "|" + "" + "|" +
          "" + "|" + value._3 + "|" + value._4 + "|" +
          value._5 + "|" + value._6 + "|" + "" + "|" +
          "" + "|" + "" + "|" + ""
        out.collect((entity, true), inputKafkaValue)
      }
    }
  }

  class DestinationIpReduceFunction extends ReduceFunction[(Long, String, String, String, String, String, Long, Long, Long)] {
    override def reduce(value1: (Long, String, String, String, String, String, Long, Long, Long),
                        value2: (Long, String, String, String, String, String, Long, Long, Long))
    : (Long, String, String, String, String, String, Long, Long, Long) = {
      //数据输出的格式: timeStamp, userName, sourceIp, 所有的sourcePort, 所有的destinationIp, 所有的destinationPort, 总上行流量, 总下行流量, 主机ip探测次数
      val timeStamp = value2._1
      val userName = value2._2
      val sourceIp = value2._3
      var sourcePorts = ""
      var destinationIps = ""
      var destinationPorts = ""
      if (value2._4.equals(value1._4)) {
        sourcePorts = value2._4
      } else {
        sourcePorts = value1._4 + "|" + value2._4
      }

      if (value2._5.equals(value1._5)) {
        destinationIps = value2._5
      } else {
        destinationIps = value1._5 + "|" + value2._5
      }

      if (value2._6.equals(value1._6)) {
        destinationPorts = value2._6
      } else {
        destinationPorts = value1._6 + "|" + value2._6
      }
      val inputOctets = value1._7 + value2._7
      val outputOctets = value1._8 + value2._8
      val scanCount = value1._9 + value2._9

      (timeStamp, userName, sourceIp, sourcePorts, destinationIps, destinationPorts, inputOctets, outputOctets, scanCount)
    }
  }
}
