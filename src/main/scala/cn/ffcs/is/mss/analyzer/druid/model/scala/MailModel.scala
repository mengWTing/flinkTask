package cn.ffcs.is.mss.analyzer.druid.model.scala

import com.metamx.common.scala.untyped.Dict
import com.metamx.tranquility.typeclass.Timestamper
import io.druid.query.aggregation.LongSumAggregatorFactory
import org.joda.time.{DateTime, DateTimeZone}
import org.json.JSONObject

/**
  * @title MailModel
  * @author liangzhaosuo
  * @date 2020-11-18 14:51
  * @description
  * @update [no][date YYYY-MM-DD][name][description]
  */


case class MailModel(var createTime: Long, var funcName: String, var interfaceType: String, var sourceIp: String,
                     var logType: String, var timeStamp: Long, var operationType: String, var provinceCode: String,
                     var provinceName: String, var sysCode: String, var sysName: String, var userName: String, var
                     level: Int) {


  def this() {
    this(0L, null, null, null, null, 0L, null, null, null, null, null, null, 0)
  }

  override def toString: String = {
    val stringBuilder = new StringBuilder

    stringBuilder.append("{")
    stringBuilder.append("\"createTime\":\"" + new DateTime(createTime, DateTimeZone.forID(MailModel.druidFormat)) +
      "\",")
    stringBuilder.append("\"funcName\":\"" + funcName + "\",")
    stringBuilder.append("\"interfaceType\":\"" + interfaceType + "\",")
    stringBuilder.append("\"sourceIp\":\"" + sourceIp + "\",")
    stringBuilder.append("\"logType\":\"" + logType + "\",")
    stringBuilder.append("\"timeStamp\":\"" + new DateTime(timeStamp, DateTimeZone.forID(MailModel.druidFormat)) +
      "\",")
    stringBuilder.append("\"operationType\":\"" + operationType + "\",")
    stringBuilder.append("\"provinceCode\":\"" + provinceCode + "\",")
    stringBuilder.append("\"provinceName\":\"" + provinceName + "\",")
    stringBuilder.append("\"sysCode\":\"" + sysCode + "\",")
    stringBuilder.append("\"sysName\":\"" + sysName + "\",")
    stringBuilder.append("\"userName\":\"" + userName + "\",")
    stringBuilder.append("\"level\":" + level)
    stringBuilder.append("}")

    stringBuilder.toString()
  }


  override def equals(obj: scala.Any): Boolean = {
    if (obj.isInstanceOf[MailModel]) {
      val model = obj.asInstanceOf[MailModel]
      model.canEqual(this) && super.equals(model) && model.toString.equals(super.toString)
    }
    else false
  }

  override def hashCode(): Int = toString.hashCode


  def toJson: String = {
    val jsonObject = new JSONObject
    jsonObject.put("createTime", new DateTime(createTime, DateTimeZone.forID(MailModel.druidFormat)))
    jsonObject.put("funcName", funcName)
    jsonObject.put("interfaceType", interfaceType)
    jsonObject.put("sourceIp", sourceIp)
    jsonObject.put("logType", logType)
    jsonObject.put("timeStamp", new DateTime(timeStamp, DateTimeZone.forID(MailModel.druidFormat)))
    jsonObject.put("operationType", operationType)
    jsonObject.put("provinceCode", provinceCode)
    jsonObject.put("provinceName", provinceName)
    jsonObject.put("sysCode", sysCode)
    jsonObject.put("sysName", sysName)
    jsonObject.put("userName", userName)
    jsonObject.put("level", level)
    jsonObject.toString()
  }
}

object MailModel {
  val druidFormat = "+08:00"
  val Columns = IndexedSeq("funcName", "interfaceType", "sourceIp", "logType", "operationType", "provinceCode",
    "provinceName", "sysCode", "sysName", "userName")
  val Metrics = Seq(new LongSumAggregatorFactory("level", "level"))

  implicit val simpleEventTimestamper = new Timestamper[MailModel] {
    def timestamp(mailModel: MailModel) = new DateTime(mailModel.timeStamp)
  }


  def fromMap(d: Dict): MailModel = {
    new MailModel(d("createTime").toString.toLong,
      d("funcName").toString,
      d("interfaceType").toString,
      d("sourceIp").toString,
      d("logType").toString,
      d("timeStamp").toString.toLong,
      d("operationType").toString,
      d("provinceCode").toString,
      d("provinceName").toString,
      d("sysCode").toString,
      d("sysName").toString,
      d("userName").toString,
      d("level").toString.toInt
    )
  }

  def getMailModel(line: String): Option[MailModel] = {
    try {
      val json = new JSONObject(line)
      val createTime = json.getLong("createTime")
      val funcName = json.getJSONArray("funcPath").getJSONObject(0).getString("funcName")
      val interfaceType = json.getString("interfaceType")
      val sourceIp = json.getString("ip")
      val logType = json.getString("logType")
      val timeStamp = json.getLong("operationTime")
      val operationType = json.getString("operationType")
      val provinceCode = json.getString("provinceCode")
      val provinceName = json.getString("provinceName")
      val sysCode = json.getString("sysCode")
      val sysName = json.getString("sysName")
      val userName = json.getString("userName")
      val level = json.getInt("level")
      Some(new MailModel(createTime, funcName, interfaceType, sourceIp, logType, timeStamp, operationType,
        provinceCode, provinceName, sysCode, sysName, userName, level))
    } catch {
      case e: Exception => None
    }


  }


}
