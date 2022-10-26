package cn.ffcs.is.mss.analyzer.druid.model.scala

import com.metamx.common.scala.untyped.Dict
import com.metamx.tranquility.typeclass.Timestamper
import io.druid.query.aggregation.LongSumAggregatorFactory
import org.joda.time.{DateTime, DateTimeZone}
import org.json.JSONObject

/**
  * DNS协议话单
  * res.qhmsg.com  |           |    5    |1523438110255|1523438110255|      1      |      0       |     0    |20180411171510
  * QueryDomainName|QueryResult|ReplyCode|  QueryTime  | ResponseTime|RequestNumber|ResponseNumber|Answer rrs|   writetime
  * QueryResult:取查询响应中的解析结果，对于存在多个解析结果的情况，使用分号分隔
  * Answer rrs:0为解析错误
  *
  * ReplyCode:
  * 0 : 没有错误。
  * 1 : 报文格式错误(Format error) - 服务器不能理解请求的报文;
  * 2 : 服务器失败(Server failure) - 因为服务器的原因导致没办法处理这个请求;
  * 3 : 名字错误(Name Error) - 只有对授权域名解析服务器有意义，指出解析的域名不存在;
  * 4 : 没有实现(Not Implemented) - 域名服务器不支持查询类型;
  * 5 : 拒绝(Refused) - 服务器由于设置的策略拒绝给出应答.比如，服务器不希望对某些请求者给出应答，或者服务器不希望进行某些操作（比如区域传送zone transfer）;
  * [6,15] : 保留值，暂未使用。
  *
  * @param timeStamp
  * @param queryDomainName
  * @param queryResult
  * @param replyCode
  * @param delay
  * @param requestNumber
  * @param responseNumber
  * @param answerRrs
  */
case class DnsModel(var timeStamp: Long,var queryDomainName: String,var queryResult: String,
                    var replyCode: String,var delay: Long,var requestNumber: Long,
                    var responseNumber: Long, answerRrs: String){

  def this(){
    this(0L,null,null,null,0L,0L,0L,null)
  }


  /**
    * 使用字符串拼接成json串
    * @return
    */
  override def toString: String = {
    val stringBuilder = new StringBuilder
    stringBuilder.append("{")
    stringBuilder.append("\"timeStamp\":\"" + new DateTime(timeStamp, DateTimeZone.forID(DnsModel.druidFormat)) + "\",")
    stringBuilder.append("\"queryDomainName\":\"" + queryDomainName + "\",")
    stringBuilder.append("\"queryResult\":\"" + queryResult + "\",")
    stringBuilder.append("\"replyCode\":\"" + replyCode + "\",")
    stringBuilder.append("\"delay\":" + delay + ",")
    stringBuilder.append("\"requestNumber\":" + requestNumber + ",")
    stringBuilder.append("\"responseNumber\":" + responseNumber + ",")
    stringBuilder.append("\"answerRrs\":\"" + answerRrs + "\"")
    stringBuilder.append("}")
    stringBuilder.toString
  }


  /**
    * 生成json串
    * @return
    */
  def toJson: String = {
    val jsonObject = new JSONObject
    jsonObject.put("timeStamp", new DateTime(timeStamp, DateTimeZone.forID(DnsModel.druidFormat)))
    jsonObject.put("queryDomainName", queryDomainName)
    jsonObject.put("queryResult", queryResult)
    jsonObject.put("replyCode", replyCode)
    jsonObject.put("delay", delay)
    jsonObject.put("requestNumber", requestNumber)
    jsonObject.put("responseNumber", responseNumber)
    jsonObject.put("answerRrs", answerRrs)
    jsonObject.toString
  }
}

object DnsModel{

  val druidFormat = "+08:00"

  val Columns = IndexedSeq("queryDomainName","queryResult","replyCode","answerRrs")

  val Metrics =  Seq(new LongSumAggregatorFactory("delay", "delay"),
    new LongSumAggregatorFactory("requestNumber", "requestNumber"),
    new LongSumAggregatorFactory("responseNumber", "responseNumber"))

  implicit val simpleEventTimestamper = new Timestamper[DnsModel] {
    def timestamp(dnsModel: DnsModel) = new DateTime(dnsModel.timeStamp)
  }

  def fromMap(d: Dict): DnsModel = {
    DnsModel(
      d("timeStamp").toString.toLong,
      d("queryDomainName").toString,
      d("queryResult").toString,
      d("replyCode").toString,
      d("delay").toString.toLong,
      d("requestNumber").toString.toLong,
      d("responseNumber").toString.toLong,
      d("answerRrs").toString
    )
  }

  /**
    * 根据话单生成DnsModel
    * @param line
    * @return
    */
  def getDnsModel(line : String):Option[DnsModel] ={
    val values = line.split("\\|", -1)

    if (values.length == 9) {
      //查询时间戳
      var timeStamp = 0L
      try{
        timeStamp = values(3).trim.toLong
      }catch{
        case e : NumberFormatException => e.printStackTrace()
      }

      //写入时间戳
      var responseTimeStamp = 0L
      try{
        responseTimeStamp = values(4).trim.toLong
      }catch{
        case e : NumberFormatException => e.printStackTrace()
      }


      //请求查询的DNS域名
      val queryDomainName = values(0)

      //DNS的解析结果
      val queryResult = values(1)

      //DNS响应码
      val replyCode = values(2)

      //时延
      var delay = 0L
      if (responseTimeStamp > 0 && timeStamp > 0 && responseTimeStamp > timeStamp) {
        delay = responseTimeStamp - timeStamp
      }

      //DNS的请求次数
      var requestNumber = 0L
      try{
        requestNumber = values(5).trim.toLong
      }catch{
        case e : NumberFormatException => e.printStackTrace()
      }

      //响应数目
      var responseNumber = 0L
      try{
        responseNumber = values(6).trim.toLong
      }catch{
        case e : NumberFormatException => e.printStackTrace()
      }

      //Answer rrs=0为解析错误
      val answerRrs = values(7)
      Some(new DnsModel(timeStamp, queryDomainName, queryResult, replyCode, delay, requestNumber, responseNumber, answerRrs))
    }else{
      None
    }
  }
}
