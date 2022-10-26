package cn.ffcs.is.mss.analyzer.druid.model.scala

import java.beans.{IntrospectionException, PropertyDescriptor}
import java.lang.reflect.InvocationTargetException
import java.text.SimpleDateFormat
import java.util.Date

import javax.persistence.Column
import org.json.JSONObject

/**
  * @Auther chenwei
  * @Description
  * @Date: Created in 2018/11/26 14:47
  * @Modified By
  */
case class AlertModel(alertName: String, alertTimestamp: String, alertType: String,
                      alertLevel: Int, alertRegion: String, alertBusiness: String,
                      alertDomain: String, alertSrcIp: String, alertSrcPort: String,
                      alertDestIp: String, alertDestPort: String, alertTimes: Int,
                      alertIp: String, alertDevice: String, alertDescription: String,
                      alertId: String, alertRuleId: String, alertStatus: Int,
                      alertUsername: String, eventTimeStamp: Long, alertAssembly: Int) {

  def this() {
    this(null, null, null, 0, null, null, null, null, null, null, null, 0, null, null, null, null, null, 0, null, 0L, 1)
  }

  def this(builder: AlertModel.Builder) {
    this(builder.alertName, builder.alertTimestamp, builder.alertType, builder.alertLevel,
      builder.alertRegion, builder.alertBusiness, builder.alertDomain, builder.alertSrcIp,
      builder.alertSrcPort, builder.alertDestIp, builder.alertDestPort, builder.alertTimes,
      builder.alertIp, builder.alertDevice, builder.alertDescription, builder.alertId,
      builder.alertRuleId, builder.alertStatus, builder.alertUsername, builder.eventTimeStamp,
      builder.alertAssembly)
  }


  /**
    * 使用字符串拼接成json串
    *
    * @returnF
    */
  override def toString: String = {
    val stringBuilder = new StringBuilder
    stringBuilder.append("{")
    stringBuilder.append("\"alert_name\":\"" + alertName + "\",")
    stringBuilder.append("\"alert_timestamp\":\"" + alertTimestamp + "\",")
    stringBuilder.append("\"alert_type\":\"" + alertType + "\",")
    stringBuilder.append("\"alert_level\":" + alertLevel + ",")
    stringBuilder.append("\"alert_region\":\"" + alertRegion + "\",")
    stringBuilder.append("\"alert_business\":\"" + alertBusiness + "\",")
    stringBuilder.append("\"alert_domain\":\"" + alertDomain + "\",")
    stringBuilder.append("\"alert_srcip\":\"" + alertSrcIp + "\",")
    stringBuilder.append("\"alert_srcport\":\"" + alertSrcPort + "\",")
    stringBuilder.append("\"alert_destip\":\"" + alertDestIp + "\",")
    stringBuilder.append("\"alert_destport\":\"" + alertDestPort + "\",")
    stringBuilder.append("\"alert_times\":\"" + alertTimes + "\",")
    stringBuilder.append("\"alert_ip\":\"" + alertIp + "\",")
    stringBuilder.append("\"alert_device\":\"" + alertDevice + "\",")
    stringBuilder.append("\"alert_description\":\"" + alertDescription + "\",")
    stringBuilder.append("\"alert_id\":\"" + alertId + "\",")
    stringBuilder.append("\"alert_ruleid\":\"" + alertRuleId + "\",")
    stringBuilder.append("\"alert_status\":" + alertStatus + ",")
    stringBuilder.append("\"alert_username\":\"" + alertUsername + ",\"")
    stringBuilder.append("\"alert_assembly\":\"" + alertAssembly + "\"")
    stringBuilder.append("}")
    stringBuilder.toString
  }


  override def equals(obj: Any): Boolean = {
    if (obj.isInstanceOf[AlertModel]) {
      val alertModel = obj.asInstanceOf[AlertModel]
      alertModel.canEquals(this) && super.equals(alertModel) &&
        alertModel.toString.equals(super.toString)
    }
    else false
  }

  def canEquals(obj: Any): Boolean = obj.isInstanceOf[AlertModel]

  override def hashCode: Int = toString.hashCode

  /**
    * 生成json串
    *
    * @return
    */
  def toJson: String = {
    val jsonObject = new JSONObject
    jsonObject.put("alert_name", alertName)
    jsonObject.put("alert_timestamp", alertTimestamp)
    jsonObject.put("alert_type", alertType)
    jsonObject.put("alert_level", alertLevel)
    jsonObject.put("alert_region", alertRegion)
    jsonObject.put("alert_business", alertBusiness)
    jsonObject.put("alert_domain", alertDomain)
    jsonObject.put("alert_srcip", alertSrcIp)
    jsonObject.put("alert_srcport", alertSrcPort)
    jsonObject.put("alert_destip", alertDestIp)
    jsonObject.put("alert_destport", alertDestPort)
    jsonObject.put("alert_times", alertTimes)
    jsonObject.put("alert_ip", alertIp)
    jsonObject.put("alert_device", alertDevice)
    jsonObject.put("alert_description", alertDescription)
    jsonObject.put("alert_id", alertId)
    jsonObject.put("alert_ruleid", alertRuleId)
    jsonObject.put("alert_status", alertStatus)
    jsonObject.put("alert_username", alertUsername)
    jsonObject.put("alert_assembly", alertAssembly)
    jsonObject.toString
  }

}

object AlertModel {

  class Builder(var alertName: String, var alertTimestamp: String, var alertType: String,
                var alertLevel: Int, var alertRegion: String, var alertBusiness: String,
                var alertDomain: String, var alertSrcIp: String, var alertSrcPort: String,
                var alertDestIp: String, var alertDestPort: String, var alertTimes: Int,
                var alertIp: String, var alertDevice: String, var alertDescription: String,
                var alertId: String, var alertRuleId: String, var alertStatus: Int,
                var alertUsername: String, var eventTimeStamp: Long, var alertAssembly: Int) {


    def this() {
      this(null, null, null, 0, null, null, null, null, null, null, null, 0, null, null, null, null, null, 0, null,
        0L, 1)
    }

    def alertName(alertName: String): Builder = {
      this.alertName = alertName
      this
    }

    def alertTimestamp(alertTimestamp: String): Builder = {
      this.alertTimestamp = alertTimestamp
      this
    }

    def alertType(alertType: String): Builder = {
      this.alertType = alertType
      this
    }

    def alertLevel(alertLevel: Int): Builder = {
      this.alertLevel = alertLevel
      this
    }

    def alertRegion(alertRegion: String): Builder = {
      this.alertRegion = alertRegion
      this
    }

    def alertBusiness(alertBusiness: String): Builder = {
      this.alertBusiness = alertBusiness
      this
    }

    def alertDomain(alertDomain: String): Builder = {
      this.alertDomain = alertDomain
      this
    }

    def alertSrcIp(alertSrcIp: String): Builder = {
      this.alertSrcIp = alertSrcIp
      this
    }

    def alertSrcPort(alertSrcPort: String): Builder = {
      this.alertSrcPort = alertSrcPort
      this
    }

    def alertDestIp(alertDestIp: String): Builder = {
      this.alertDestIp = alertDestIp
      this
    }

    def alertDestPort(alertDestPort: String): Builder = {
      this.alertDestPort = alertDestPort
      this
    }

    def alertTimes(alertTimes: Int): Builder = {
      this.alertTimes = alertTimes
      this
    }

    def alertIp(alertIp: String): Builder = {
      this.alertIp = alertIp
      this
    }

    def alertDevice(alertDevice: String): Builder = {
      this.alertDevice = alertDevice
      this
    }

    def alertDescription(alertDescription: String): Builder = {
      this.alertDescription = alertDescription
      this
    }

    def alertId(alertId: String): Builder = {
      this.alertId = alertId
      this
    }

    def alertRuleId(alertRuleId: String): Builder = {
      this.alertRuleId = alertRuleId
      this
    }

    def alertStatus(alertStatus: Int): Builder = {
      this.alertStatus = alertStatus
      this
    }

    def alertUsername(alertUsername: String): Builder = {
      this.alertUsername = alertUsername
      this
    }

    def eventTimeStamp(eventTimeStamp: Long): Builder = {
      this.eventTimeStamp = eventTimeStamp
      this
    }

    def alertAssembly(alertAssembly: Int): Builder = {
      this.alertAssembly = alertAssembly
      this
    }

    def build(): AlertModel = {
      new AlertModel(this)
    }
  }

  def getBuilder(): Builder = {

    new Builder()
  }

  /**
    *
    * @param alertTimeStampFormat
    * @param time
    * @return
    * @author liangzhaosuo
    * @date 2020/09/01 14:30
    * @description 将时间转换成时间戳
    * @update [no][date YYYY-MM-DD][name][description]
    */
  def getAlertTime(alertTimeStampFormat: String, time: String): Long = {
    val sdf = new SimpleDateFormat(alertTimeStampFormat)
    val date = sdf.parse(time)
    date.getTime
  }


  /**
    * 将时间戳格式化
    *
    * @param alertTimeStampFormat
    * @param alertTimestamp
    * @return
    */
  def getAlertTimestamp(alertTimeStampFormat: String, alertTimestamp: Long): String = {
    val simpleDateFormat = new SimpleDateFormat(alertTimeStampFormat)
    simpleDateFormat.format(new Date(alertTimestamp))
  }

  /**
    * 根据ORM映射的对象获取告警描述
    *
    * @param o
    * @return
    */
  def getAlertDescription(o: Object): String = {
    try { //根据字段获取其get方法
      val clazz = o.getClass
      val fields = clazz.getDeclaredFields
      val stringBuilder = new StringBuilder
      for (field <- fields) { //使其可以访问私有字段
        field.setAccessible(true)
        val propertyDescriptor = new PropertyDescriptor(field.getName, clazz)
        if (propertyDescriptor.getReadMethod != null) {
          val method = propertyDescriptor.getReadMethod
          //获取其对应数据库的字段名
          val column = method.getAnnotation(classOf[Column])
          val invoke = method.invoke(o)
          var value = ""
          if (invoke == null) {
            value = "NULL"
          } else {
            value = invoke.toString
          }
          stringBuilder.append(column.name + "=" + value + ";")
        }
      }
      if (stringBuilder.length > 1) stringBuilder.deleteCharAt(stringBuilder.length - 1) {
        return stringBuilder.toString
      }
    } catch {
      case e: InvocationTargetException =>
        e.printStackTrace()
      case e: IllegalAccessException =>
        e.printStackTrace()
      case e: IntrospectionException =>
        e.printStackTrace()
    }
    ""
  }


  /**
    * 获取值，如果为空则填NULL
    *
    * @param value
    * @return
    */
  def getValue(value: String): String = {
    if (value == null || value.length == 0) {
      "NULL"
    } else {
      value
    }
  }


  def getAlertId(alertId: String): String = {
    if (alertId == null || alertId.length == 0) {
      (math.random * 10000).toInt.toString
    } else {
      alertId
    }
  }
}