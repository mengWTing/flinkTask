package cn.ffcs.is.mss.analyzer.druid.model.scala

import com.metamx.common.scala.untyped.Dict
import com.metamx.tranquility.typeclass.Timestamper
import org.joda.time.{DateTime, DateTimeZone}
import org.json.JSONObject

import io.druid.query.aggregation.LongSumAggregatorFactory

/**
  * Arp协议话单
  * 1526452481983|00:24:AC:BC:4D:9A|10.125.255.132|4C:09:B4:F6:DC:D0|10.125.255.131|     1      |    586     |20180516143441
  * startTime    |     srcMac      |     srcIp    |      desMac     |     desIp    |responseCode|responseTime|writetime
  * ms                                                                              0超时,1正常  | 超时时为0
  * @param timeStamp
  * @param sourceMac
  * @param sourceIp
  * @param destinationMac
  * @param destinationIp
  * @param responseCode
  * @param responseTime
  */
case class ArpModel(var timeStamp: Long,var sourceMac: String,var sourceIp: String,
                    var destinationMac: String,var destinationIp: String,var responseCode: String,
                    var responseTime: Long) {

  def this(){
    this(0L,null,null,null,null,null,0L)
  }

  /**
    * 使用字符串拼接成json串
    * @return
    */
  override def toString: String = {
    val stringBuilder = new StringBuilder

    stringBuilder.append("{")
    stringBuilder.append("\"timeStamp\":\"" + new DateTime(timeStamp, DateTimeZone.forID(ArpModel.druidFormat)) + "\",")
    stringBuilder.append("\"sourceMac\":\"" + sourceMac + "\",")
    stringBuilder.append("\"sourceIp\":\"" + sourceIp + "\",")
    stringBuilder.append("\"destinationMac\":\"" + destinationMac + "\",")
    stringBuilder.append("\"destinationIp\":\"" + destinationIp + "\",")
    stringBuilder.append("\"responseCode\":\"" + responseCode + "\",")
    stringBuilder.append("\"responseTime\":" + responseTime)
    stringBuilder.append("}")
    return stringBuilder.toString
  }

  /**
    * 生成json串
    * @return
    */
  def toJson: String = {
    val jsonObject = new JSONObject
    jsonObject.put("timeStamp", new DateTime(timeStamp, DateTimeZone.forID(ArpModel.druidFormat)))
    jsonObject.put("sourceMac", sourceMac)
    jsonObject.put("sourceIp", sourceIp)
    jsonObject.put("destinationMac", destinationMac)
    jsonObject.put("destinationIp", destinationIp)
    jsonObject.put("responseCode", responseCode)
    jsonObject.put("responseTime", responseTime)
    jsonObject.toString
  }


}

object ArpModel{

  val druidFormat = "+08:00"
  val Columns = IndexedSeq("timeStamp", "sourceMac", "sourceIp", "destinationMac", "destinationIp","responseCode")
  val Metrics = Seq(new LongSumAggregatorFactory("responseTime", "responseTime"))

  implicit val simpleEventTimestamper = new Timestamper[ArpModel] {
    def timestamp(arpModel: ArpModel) = new DateTime(arpModel.timeStamp)
  }

  def fromMap(d: Dict): ArpModel = {
    ArpModel(
      d("timeStamp").toString.toLong,
      d("sourceMac").toString,
      d("sourceIp").toString,
      d("destinationMac").toString,
      d("destinationIp").toString,
      d("responseCode").toString,
      d("responseTime").toString.toLong
    )
  }

  /**
    * 根据话单生成ArpModel
    * @param line
    * @return
    */
  def getArpModel(line : String):Option[ArpModel] ={
    val values = line.split("\\|", -1)

    if (values.length == 8) {
      //时间戳
      var timeStamp = 0L
      try{
        timeStamp = values(0).trim.toLong
      }catch{
        case e : NumberFormatException => e.printStackTrace()
      }

      //源mac
      val sourceMac = values(1)

      //源ip
      val sourceIp = values(2)

      //目的mac
      val destinationMac = values(3)

      //目的ip
      val destinationIp = values(4)

      //响应情况
      val responseCode = values(5)

      //响应时间
      var responseTime = 0L
      try{
        responseTime = values(6).trim.toLong
      }catch{
        case e : NumberFormatException => e.printStackTrace()
      }

      Some(new ArpModel(timeStamp, sourceMac, sourceIp, destinationMac, destinationIp, responseCode, responseTime))
    }else{
      None
    }

  }

}