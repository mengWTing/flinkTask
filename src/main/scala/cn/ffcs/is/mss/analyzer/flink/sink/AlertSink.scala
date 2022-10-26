package cn.ffcs.is.mss.analyzer.flink.sink

import java.text.SimpleDateFormat
import java.util.Date

import cn.ffcs.is.mss.analyzer.utils.Constants
import javax.xml.namespace.QName
import org.apache.cxf.endpoint.Client
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction
import org.apache.cxf.jaxws.endpoint.dynamic.JaxWsDynamicClientFactory
import org.apache.flink.api.common.accumulators.LongCounter
import org.json.{JSONArray, JSONObject}

/**
  * @Auther chenwei
  * @Description
  * @Date: Created in 2018/11/26 10:33
  * @Modified By
  */
class AlertSink extends RichSinkFunction[(String,Long)] {


  var ALERT_URL = ""
  var NAMESPACE_URI = ""
  var LOCAL_PART = ""

  var SUBSOC_ID = 0L
  var KEY = ""
  var INTERFACE_TYPE = 0L
  var TIMESTAMP_FORMAT = ""

  var dcf: JaxWsDynamicClientFactory = null
  var client: Client = null
  var qName: QName = null

  val messagesReceived = new LongCounter()
  val messagesExecuteSucceed = new LongCounter()
  val messagesExecuteFail = new LongCounter()

  override def open(parameters: Configuration): Unit = {

    //获取全局变量
    val globConf = getRuntimeContext.getExecutionConfig.getGlobalJobParameters
      .asInstanceOf[Configuration]

    ALERT_URL = globConf.getString(Constants.ALERT_STATISTICS_ALERT_URL, "")
    NAMESPACE_URI = globConf.getString(Constants.ALERT_STATISTICS_NAMESPACE_URI, "")
    LOCAL_PART = globConf.getString(Constants.ALERT_STATISTICS_LOCAL_PART, "")
    SUBSOC_ID = globConf.getLong(Constants.ALERT_STATISTICS_SUBSOC_ID, 0L)
    KEY = globConf.getString(Constants.ALERT_STATISTICS_KEY, "")
    INTERFACE_TYPE = globConf.getLong(Constants.ALERT_STATISTICS_INTERFACE_TYPE, 0L)
    TIMESTAMP_FORMAT = globConf.getString(Constants.ALERT_STATISTICS_TIMESTAMP_FORMAT, "")

    //增加Accumulator 记录收到的数据个数、执行成功个数、执行失败的个数
    getRuntimeContext.addAccumulator("AlertSink: Messages received", messagesReceived)
    getRuntimeContext.addAccumulator("AlertSink: Messages execute succeed", messagesExecuteSucceed)
    getRuntimeContext.addAccumulator("AlertSink: Messages execute fail", messagesExecuteFail)

  }

  override def invoke(value: (String, Long)): Unit = {

    messagesReceived.add(1)

    try {

      val name = getQName(ALERT_URL, NAMESPACE_URI, LOCAL_PART, 10)
      val objects = client.invoke(name, getAlert(value).toString)
      val jsonObject = new JSONObject(objects(0).toString)
      if ("200" == jsonObject.getString("code")) {
        messagesExecuteSucceed.add(1)
      } else {
        messagesExecuteFail.add(1)
      }
    } catch {
      case e: Exception => {
        qName = null
        client = null
        dcf = null
        messagesExecuteFail.add(1)
      }
    }

  }

  override def close(): Unit = {

    qName = null
    if (client != null) {
      client.destroy()
    }
    dcf = null

  }


  /**
   * @author chenwei
   *         date:  Created in 2018/12/10 10:00
   *         description 获取QName
   * @param ALERT_URL
   * @param NAMESPACE_URI
   * @param LOCAL_PART
   * @return
   */
  def getQName(ALERT_URL: String, NAMESPACE_URI: String, LOCAL_PART: String, retry: Int): QName = {

    // 获取dcf，如果失败则重试
    var i = 0
    while (dcf == null) {
      Thread.sleep(1000 * i * math.pow(2, i).toInt)
      dcf = JaxWsDynamicClientFactory.newInstance
      i += 1
      if (i > retry) {
        return null
      }
    }


    // 获取client，如果失败则重试
    i = 0
    while (client == null) {
      Thread.sleep(1000 * i * math.pow(2, i).toInt)
      client = dcf.createClient(ALERT_URL)
      i += 1
      if (i > retry) {
        return null
      }
    }

    // 获取qName，如果失败则重试
    i = 0
    while (qName == null) {
      Thread.sleep(1000 * i * math.pow(2, i).toInt)
      qName = new QName(NAMESPACE_URI, LOCAL_PART)
      i += 1
      if (i > retry) {
        return null
      }
    }
    qName
  }

  def getAlert(value: (String, Long)): JSONArray = {

    val jsonObject = new JSONObject()
    jsonObject.put("subsoc_id", SUBSOC_ID)
    jsonObject.put("key", KEY)
    jsonObject.put("interface_type", INTERFACE_TYPE)
    jsonObject.put("timestamp", new SimpleDateFormat(TIMESTAMP_FORMAT).format(new Date(value._2)))
    jsonObject.put("data", new JSONArray(value._1))

    new JSONArray().put(jsonObject)
  }

}
