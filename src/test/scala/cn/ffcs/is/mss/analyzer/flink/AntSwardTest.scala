package cn.ffcs.is.mss.analyzer.flink

import java.net.URLDecoder
import java.sql.Timestamp
import java.util

import cn.ffcs.is.mss.analyzer.bean.BbasXssInjectionWarnEntity
import cn.ffcs.is.mss.analyzer.druid.model.scala.OperationModel
import cn.ffcs.is.mss.analyzer.utils.{Constants, IniProperties}
import org.apache.flink.api.common.functions.{RichFlatMapFunction, RichMapFunction}
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.api.scala._
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.ProcessFunction
import org.apache.flink.util.Collector
/**
 * @ClassName:
 * @Author: mengwenting
 * @Date: 2023/5/6 16:07
 * @Description:
 * @update:
 */
object AntSwardTest {
  def main(args: Array[String]): Unit = {
    val args0 = "G:\\ffcs\\4_Code\\mss\\src\\main\\resources\\flink.ini"
    val confProperties = new IniProperties(args0)

    //任务的名字
    val jobName = confProperties.getValue(Constants.FLINK_ANT_SWORD_CONFIG, Constants
      .FLINK_ANT_SWORD_CONFIG_JOB_NAME)
    //flink全局变量
    val parameters: Configuration = new Configuration()

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.getConfig.setGlobalJobParameters(parameters)

    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)
    val inputValue = env.socketTextStream("192.168.1.24", 8888).setParallelism(1)
    inputValue
      .map(new RichMapFunction[String, (Option[OperationModel], String)] {
        override def map(value: String): (Option[OperationModel], String) = (OperationModel.getOperationModel(value), value)
      }).setParallelism(1)
      .filter(_._1.isDefined).setParallelism(2)
      .map(t => (t._1.head, t._2)).setParallelism(2)
      .process(new AntSwardRealProcessFunction)
      .print()
    env.execute(jobName)
  }

  class AntSwardRealProcessFunction extends ProcessFunction[(OperationModel, String), String]{
    var groupSplit: Char = _
    var kvSplit: Char = _
    var ruleList: List[String] = _

    override def open(parameters: Configuration): Unit = {
      val globalConf = getRuntimeContext.getExecutionConfig.getGlobalJobParameters.asInstanceOf[Configuration]
      groupSplit = globalConf.getInteger(Constants.XSS_INJECTION_GROUP_SPLIT, 0).asInstanceOf[Char]
      kvSplit = globalConf.getInteger(Constants.XSS_INJECTION_KV_SPLIT, 0).asInstanceOf[Char]
      ruleList = initCheckAntSwardDataRuleMap
    }

    override def processElement(value: (OperationModel, String), ctx: ProcessFunction[(OperationModel, String), String]#Context,
                                out: Collector[String]): Unit = {
      var isAntSward = false
      val values = value._2.split("\\|", -1)
      val userAgent = values(9)
      val formValues = values(30) //请求内容
      val url = values(6)

      //判断userAgent是否是蚁剑
      if (userAgent.indexOf("antSword") > -1) {
        isAntSward = true
      }

      if (values.length >= 31){
        if (formValues != null && formValues.length > 0){
          for (formValue <- formValues.split(groupSplit)){
//            println("单个formValue: " + formValue)
            val kvValues = formValue.split(kvSplit)
//            println("切分kvValues: " + kvValues)
            if (kvValues != null && kvValues.length == 2){
//            if (kvValues != null){
              var urlValue = ""
              try {
                urlValue = URLDecoder.decode(kvValues(1).replaceAll("%(?![0-9a-fA-F]{2})", "%25")
                  , "utf-8")
//                println("urlValue: " + urlValue)
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
        val bbasXssInjectionWarnEntity = new BbasXssInjectionWarnEntity
        bbasXssInjectionWarnEntity.setWarnDatetime(new Timestamp(value._1.timeStamp))
        bbasXssInjectionWarnEntity.setUsername(value._1.userName)
        bbasXssInjectionWarnEntity.setLoginSystem(value._1.loginSystem)
        bbasXssInjectionWarnEntity.setDestinationIp(value._1.destinationIp)
        bbasXssInjectionWarnEntity.setLoginPlace(value._1.loginPlace)
        bbasXssInjectionWarnEntity.setSourceIp(value._1.sourceIp)
        bbasXssInjectionWarnEntity.setHttpStatus(value._1.httpStatus)
        if (formValues.length > 1000) {
          bbasXssInjectionWarnEntity.setFormValue(formValues.substring(0, 1000))
        } else {
          bbasXssInjectionWarnEntity.setFormValue(formValues)
        }
        //        out.collect((bbasXssInjectionWarnEntity.asInstanceOf[Object], false))
        out.collect("蚁剑使用: " + isAntSward + " url: " + url + " userAgent: " + userAgent)
      }else{
        out.collect("非蚁剑使用行为")
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
