package cn.ffcs.is.mss.analyzer.druid.model.scala



import com.metamx.common.scala.untyped.Dict
import com.metamx.tranquility.typeclass.Timestamper
import io.druid.query.aggregation.LongSumAggregatorFactory
import org.joda.time.{DateTime, DateTimeZone}
import org.json.JSONObject


/**
  * 传输层话单
  * 10.133.10.99|7138|4C:09:B4:F6:DC:F0|10.140.9.246|53|00:24:AC:BC:4D:9A|4|0|0|79|79|1|1|0|0|0|0|0|0|1|1|1|0|0|0|0|0|0|1528441794550|1528441794551|0|0|0|56|0
  * 源IP|源端口|源MAC|目的IP|目的PORT|目的MAC|协议类型|VLAN号，第一层vlan|
 * 采集分段标识(探针号)|流入字节数|流出字节数|流入包数|流出包数|流入重传包数|流出重传包数|流入重传时延(总时延)-ms|
 * 流出重传时延(总时延)-ms|流入RESET数量|流出RESET数量|连接是否成功（1-成功、0-失败、2-分割）|1.4层连接：会话数-连接失败；2.7层连接：会话数|
 * 流入小包数：根据配置规定判断小包字节数|流出小包数：根据配置规定判断小包字节数|流入中包数：根据配置规定判断中包字节数|流出中包数：根据配置规定判断中包字节数|
 * 流入大包数：根据配置规定判断大包字节数|流出大包数；根据配置规定判断大包字节数|流入零窗口数|流出零窗口数|开始时间戳-ms|结束时间戳-ms|SYN时间戳-ms|SYN ACK时间戳-ms|A
 * CK时间戳-ms|Time to live|TCP协议或UDP协议（1：TCP、0：UDP）
  * @param timeStamp
  * @param sourceIp
  * @param sourcePort
  * @param sourceMac
  * @param destinationIp
  * @param destinationPort
  * @param destinationMac
  * @param protocolId
  * @param vlanId
  * @param probeId
  * @param inputOctets
  * @param outputOctets
  * @param octets
  * @param inputPacket
  * @param outputPacket
  * @param packet
  * @param inputRetransPacket
  * @param outputRetransPacket
  * @param retransPacket
  * @param inputRetransDelay
  * @param outputRetransDelay
  * @param retransDelay
  * @param inputRest
  * @param outputRest
  * @param rest
  * @param isSucceed
  * @param inputSmallPacket
  * @param outputSmallPacket
  * @param smallPacket
  * @param inputMediumPacket
  * @param outputMediumPacket
  * @param mediumPacket
  * @param inputLargePacket
  * @param outputLargePacket
  * @param largePacket
  * @param inputZeroWindow
  * @param outputZeroWindow
  * @param zeroWindow
  * @param finishTime
  * @param synTime
  * @param synAckTime
  * @param ackTime
  * @param ttl
  * @param protocol
  * @param connCount
  */
case class QuintetModel(var timeStamp: Long,
                        var sourceIp: String,
                        var sourcePort: String,
                        var sourceMac: String,
                        var destinationIp: String,
                        var destinationPort: String,
                        var destinationMac: String,
                        var protocolId: String,
                        var vlanId: String,
                        var probeId: String,
                        var inputOctets: Long,
                        var outputOctets: Long,
                        var octets: Long,
                        var inputPacket: Long,
                        var outputPacket: Long,
                        var packet: Long,
                        var inputRetransPacket: Long,
                        var outputRetransPacket: Long,
                        var retransPacket: Long,
                        var inputRetransDelay: Long,
                        var outputRetransDelay: Long,
                        var retransDelay: Long,
                        var inputRest: Long,
                        var outputRest: Long,
                        var rest: Long,
                        var isSucceed: String,
                        var inputSmallPacket: Long,
                        var outputSmallPacket: Long,
                        var smallPacket: Long,
                        var inputMediumPacket: Long,
                        var outputMediumPacket: Long,
                        var mediumPacket: Long,
                        var inputLargePacket: Long,
                        var outputLargePacket: Long,
                        var largePacket: Long,
                        var inputZeroWindow: Long,
                        var outputZeroWindow: Long,
                        var zeroWindow: Long,
                        var finishTime: Long,
                        var synTime: Long,
                        var synAckTime: Long,
                        var ackTime: Long,
                        var ttl: Long,
                        var protocol: String,
                        var connCount: Long) {

  def this(){
    this(0L,null,null,null,null,null,null,null,null,null,0L,0L,0L,0L,0L,0L,0L,0L,0L,0L,0L,0L,0L,
      0L,0L,null,0L,0L,0L,0L,0L,0L,0L,0L,0L,0L,0L,0L,0L,0L,0L,0L,0L,null,0L)
  }


  override def toString: String = {
    var str = "{"
    str = str + "\"timeStamp\":\"" + new DateTime(timeStamp, DateTimeZone.forID(QuintetModel.druidFormat)) + "\","
    str = str + "\"sourceIp\":\"" + sourceIp + "\","
    str = str + "\"sourcePort\":\"" + sourcePort + "\","
    str = str + "\"sourceMac\":\"" + sourceMac + "\","
    str = str + "\"destinationIp\":\"" + destinationIp + "\","
    str = str + "\"destinationPort\":\"" + destinationPort + "\","
    str = str + "\"destinationMac\":\"" + destinationMac + "\","
    str = str + "\"protocolId\":\"" + protocolId + "\","
    str = str + "\"vlanId\":\"" + vlanId + "\","
    str = str + "\"probeId\":\"" + probeId + "\","
    str = str + "\"inputOctets\":" + inputOctets + ","
    str = str + "\"outputOctets\":" + outputOctets + ","
    str = str + "\"octets\":" + octets + ","
    str = str + "\"inputPacket\":" + inputPacket + ","
    str = str + "\"outputPacket\":" + outputPacket + ","
    str = str + "\"packet\":" + packet + ","
    str = str + "\"inputRetransPacket\":" + inputRetransPacket + ","
    str = str + "\"outputRetransPacket\":" + outputRetransPacket + ","
    str = str + "\"retransPacket\":" + retransPacket + ","
    str = str + "\"inputRetransDelay\":" + inputRetransDelay + ","
    str = str + "\"outputRetransDelay\":" + outputRetransDelay + ","
    str = str + "\"retransDelay\":" + retransDelay + ","
    str = str + "\"inputRest\":" + inputRest + ","
    str = str + "\"outputRest\":" + outputRest + ","
    str = str + "\"rest\":" + rest + ","
    str = str + "\"isSucceed\":\"" + isSucceed + "\","
    str = str + "\"inputSmallPacket\":" + inputSmallPacket + ","
    str = str + "\"outputSmallPacket\":" + outputSmallPacket + ","
    str = str + "\"smallPacket\":" + smallPacket + ","
    str = str + "\"inputMediumPacket\":" + inputMediumPacket + ","
    str = str + "\"outputMediumPacket\":" + outputMediumPacket + ","
    str = str + "\"mediumPacket\":" + mediumPacket + ","
    str = str + "\"inputLargePacket\":" + inputLargePacket + ","
    str = str + "\"outputLargePacket\":" + outputLargePacket + ","
    str = str + "\"largePacket\":" + largePacket + ","
    str = str + "\"inputZeroWindow\":" + inputZeroWindow + ","
    str = str + "\"outputZeroWindow\":" + outputZeroWindow + ","
    str = str + "\"zeroWindow\":" + zeroWindow + ","
    str = str + "\"finishTime\":" + finishTime + ","
    str = str + "\"synTime\":" + synTime + ","
    str = str + "\"synAckTime\":" + synAckTime + ","
    str = str + "\"ackTime\":" + ackTime + ","
    str = str + "\"ttl\":" + ttl + ","
    str = str + "\"protocol\":\"" + protocol + "\","
    str = str + "\"connCount\":" + connCount + "}"
    str
  }

  def toJson: String = {
    val jsonObject = new JSONObject
    jsonObject.put("timeStamp", new DateTime(timeStamp, DateTimeZone.forID(QuintetModel.druidFormat)))
    jsonObject.put("sourceIp", sourceIp)
    jsonObject.put("sourcePort", sourcePort)
    jsonObject.put("sourceMac", sourceMac)
    jsonObject.put("destinationIp", destinationIp)
    jsonObject.put("destinationPort", destinationPort)
    jsonObject.put("destinationMac", destinationMac)
    jsonObject.put("protocolId", protocolId)
    jsonObject.put("vlanId", vlanId)
    jsonObject.put("probeId", probeId)
    jsonObject.put("inputOctets", inputOctets)
    jsonObject.put("outputOctets", outputOctets)
    jsonObject.put("octets", octets)
    jsonObject.put("inputPacket", inputPacket)
    jsonObject.put("outputPacket", outputPacket)
    jsonObject.put("packet", packet)
    jsonObject.put("inputRetransPacket", inputRetransPacket)
    jsonObject.put("outputRetransPacket", outputRetransPacket)
    jsonObject.put("retransPacket", retransPacket)
    jsonObject.put("inputRetransDelay", inputRetransDelay)
    jsonObject.put("outputRetransDelay", outputRetransDelay)
    jsonObject.put("retransDelay", retransDelay)
    jsonObject.put("inputRest", inputRest)
    jsonObject.put("outputRest", outputRest)
    jsonObject.put("rest", rest)
    jsonObject.put("isSucceed", isSucceed)
    jsonObject.put("inputSmallPacket", inputSmallPacket)
    jsonObject.put("outputSmallPacket", outputSmallPacket)
    jsonObject.put("smallPacket", smallPacket)
    jsonObject.put("inputMediumPacket", inputMediumPacket)
    jsonObject.put("outputMediumPacket", outputMediumPacket)
    jsonObject.put("mediumPacket", mediumPacket)
    jsonObject.put("inputLargePacket", inputLargePacket)
    jsonObject.put("outputLargePacket", outputLargePacket)
    jsonObject.put("largePacket", largePacket)
    jsonObject.put("inputZeroWindow", inputZeroWindow)
    jsonObject.put("outputZeroWindow", outputZeroWindow)
    jsonObject.put("zeroWindow", zeroWindow)
    jsonObject.put("finishTime", finishTime)
    jsonObject.put("synTime", synTime)
    jsonObject.put("synAckTime", synAckTime)
    jsonObject.put("ackTime", ackTime)
    jsonObject.put("ttl", ttl)
    jsonObject.put("protocol", protocol)
    jsonObject.put("connCount", connCount)
    jsonObject.toString
  }
}


object QuintetModel{

  val druidFormat = "+08:00"

  val Columns = IndexedSeq("sourceIp","sourcePort","sourceMac","destinationIp",
    "destinationPort","destinationMac","protocolId","vlanId","probeId","protocol","isSucceed")

  val Metrics = Seq(new LongSumAggregatorFactory("timeStamp","timeStamp"),
    new LongSumAggregatorFactory("inputOctets","inputOctets"),
    new LongSumAggregatorFactory("outputOctets","outputOctets"),
    new LongSumAggregatorFactory("octets","octets"),
    new LongSumAggregatorFactory("inputPacket","inputPacket"),
    new LongSumAggregatorFactory("outputPacket","outputPacket"),
    new LongSumAggregatorFactory("packet","packet"),
    new LongSumAggregatorFactory("inputRetransPacket","inputRetransPacket"),
    new LongSumAggregatorFactory("outputRetransPacket","outputRetransPacket"),
    new LongSumAggregatorFactory("retransPacket","retransPacket"),
    new LongSumAggregatorFactory("inputRetransDelay","inputRetransDelay"),
    new LongSumAggregatorFactory("outputRetransDelay","outputRetransDelay"),
    new LongSumAggregatorFactory("retransDelay","retransDelay"),
    new LongSumAggregatorFactory("inputRest","inputRest"),
    new LongSumAggregatorFactory("outputRest","outputRest"),
    new LongSumAggregatorFactory("rest","rest"),
    new LongSumAggregatorFactory("inputSmallPacket","inputSmallPacket"),
    new LongSumAggregatorFactory("outputSmallPacket","outputSmallPacket"),
    new LongSumAggregatorFactory("smallPacket","smallPacket"),
    new LongSumAggregatorFactory("inputMediumPacket","inputMediumPacket"),
    new LongSumAggregatorFactory("outputMediumPacket","outputMediumPacket"),
    new LongSumAggregatorFactory("mediumPacket","mediumPacket"),
    new LongSumAggregatorFactory("inputLargePacket","inputLargePacket"),
    new LongSumAggregatorFactory("outputLargePacket","outputLargePacket"),
    new LongSumAggregatorFactory("largePacket","largePacket"),
    new LongSumAggregatorFactory("inputZeroWindow","inputZeroWindow"),
    new LongSumAggregatorFactory("outputZeroWindow","outputZeroWindow"),
    new LongSumAggregatorFactory("zeroWindow","zeroWindow"),
    new LongSumAggregatorFactory("finishTime","finishTime"),
    new LongSumAggregatorFactory("synTime","synTime"),
    new LongSumAggregatorFactory("synAckTime","synAckTime"),
    new LongSumAggregatorFactory("ackTime","ackTime"),
    new LongSumAggregatorFactory("ttl","ttl"),
    new LongSumAggregatorFactory("connCount","connCount"))

  implicit val simpleEventTimestamper = new Timestamper[QuintetModel] {
    def timestamp(quintetModel: QuintetModel) = new DateTime(quintetModel.timeStamp)
  }

  def fromMap(d: Dict): QuintetModel = {
    QuintetModel(
      d("timeStamp").toString.toLong,
      d("sourceIp").toString,
      d("sourcePort").toString,
      d("sourceMac").toString,
      d("destinationIp").toString,
      d("destinationPort").toString,
      d("destinationMac").toString,
      d("protocolId").toString,
      d("vlanId").toString,
      d("probeId").toString,
      d("inputOctets").toString.toLong,
      d("outputOctets").toString.toLong,
      d("octets").toString.toLong,
      d("inputPacket").toString.toLong,
      d("outputPacket").toString.toLong,
      d("packet").toString.toLong,
      d("inputRetransPacket").toString.toLong,
      d("outputRetransPacket").toString.toLong,
      d("retransPacket").toString.toLong,
      d("inputRetransDelay").toString.toLong,
      d("outputRetransDelay").toString.toLong,
      d("retransDelay").toString.toLong,
      d("inputRest").toString.toLong,
      d("outputRest").toString.toLong,
      d("rest").toString.toLong,
      d("isSucceed").toString,
      d("inputSmallPacket").toString.toLong,
      d("outputSmallPacket").toString.toLong,
      d("smallPacket").toString.toLong,
      d("inputMediumPacket").toString.toLong,
      d("outputMediumPacket").toString.toLong,
      d("mediumPacket").toString.toLong,
      d("inputLargePacket").toString.toLong,
      d("outputLargePacket").toString.toLong,
      d("largePacket").toString.toLong,
      d("inputZeroWindow").toString.toLong,
      d("outputZeroWindow").toString.toLong,
      d("zeroWindow").toString.toLong,
      d("finishTime").toString.toLong,
      d("synTime").toString.toLong,
      d("synAckTime").toString.toLong,
      d("ackTime").toString.toLong,
      d("ttl").toString.toLong,
      d("protocol").toString,
      d("connCount").toString.toLong
      )
  }

  /**
    * 根据话单生成QuintetModel
    * @param line
    * @return
    */
  def getQuintetModel(line : String): Option[QuintetModel] ={

    val values = line.split("\\|", -1)

    if (values.length == 35) {
      //时间戳
      var timeStamp = 0L
      try{
        timeStamp = values(28).trim.toLong
      }catch{
        case e : NumberFormatException =>
          //e.printStackTrace()
      }

      //源IP
      val sourceIp = values(0)

      //源port
      val sourcePort = values(1)

      //源MAC
      val sourceMac = values(2)

      //目的IP
      val destinationIp = values(3)

      //目的PORT
      val destinationPort = values(4)

      //目的MAC
      val destinationMac = values(5)

      //协议类型，参考附表PortocolID字段定义
      val protocolId = values(6)

      //VLAN号，第一层vlan
      val vlanId = values(7)

      //采集分段标识(探针号)
      val probeId = values(8)

      //流入字节数
      var inputOctets = 0L
      try{
        inputOctets = values(9).trim.toLong
      }catch{
        case e : NumberFormatException =>
          //e.printStackTrace()
      }

      //流出字节数
      var outputOctets = 0L
      try{
        outputOctets = values(10).trim.toLong
      }catch{
        case e : NumberFormatException =>
          //e.printStackTrace()
      }

      //总字节数
      val octets = inputOctets + outputOctets


      //流入包数
      var inputPacket = 0L
      try{
        inputPacket = values(11).trim.toLong
      }catch{
        case e : NumberFormatException =>
          //e.printStackTrace()
      }

      //流出包数
      var outputPacket = 0L
      try{
        outputPacket = values(12).trim.toLong
      }catch{
        case e : NumberFormatException =>
          //e.printStackTrace()
      }

      //总包数
      val packet = inputPacket + outputPacket

      //流入重传包数
      var inputRetransPacket = 0L
      try{
        inputRetransPacket = values(13).trim.toLong
      }catch{
        case e : NumberFormatException =>
          //e.printStackTrace()
      }

      //流出重传包数
      var outputRetransPacket = 0L
      try{
        outputRetransPacket = values(14).trim.toLong
      }catch{
        case e : NumberFormatException =>
          //e.printStackTrace()
      }

      //重传包数
      val retransPacket = inputRetransPacket + outputRetransPacket

      //流入重传时延(总时延)-ms
      var inputRetransDelay = 0L
      try{
        inputRetransDelay = values(15).trim.toLong
      }catch{
        case e : NumberFormatException =>
          //e.printStackTrace()
      }

      //流出重传时延(总时延)-ms
      var outputRetransDelay = 0L
      try{
        outputRetransDelay = values(16).trim.toLong
      }catch{
        case e : NumberFormatException =>
          //e.printStackTrace()
      }

      //总时延
      val retransDelay = inputRetransDelay + outputRetransDelay

      //流入RESET数量
      var inputRest = 0L
      try{
        inputRest = values(17).trim.toLong
      }catch{
        case e : NumberFormatException =>
          //e.printStackTrace()
      }

      //流出RESET数量
      var outputRest = 0L
      try{
        outputRest = values(18).trim.toLong
      }catch{
        case e : NumberFormatException =>
          //e.printStackTrace()
      }

      //RESET数量
      val rest = inputRest + outputRest

      //连接是否成功（1-成功、0-失败、2-分割）
      //1.4层连接：会话数-连接失败；2.7层连接：会话数
      val isSucceed = values(19)

      //流入小包数：根据配置规定判断小包字节数
      var inputSmallPacket = 0L
      try{
        inputSmallPacket = values(20).trim.toLong
      }catch{
        case e : NumberFormatException =>
          //e.printStackTrace()
      }

      //流出小包数：根据配置规定判断小包字节数
      var outputSmallPacket = 0L
      try{
        outputSmallPacket = values(21).trim.toLong
      }catch{
        case e : NumberFormatException =>
          //e.printStackTrace()
      }

      //小包数：根据配置规定判断小包字节数
      val smallPacket = inputSmallPacket + outputSmallPacket

      //流入中包数：根据配置规定判断中包字节数
      var inputMediumPacket = 0L
      try{
        inputMediumPacket = values(22).trim.toLong
      }catch{
        case e : NumberFormatException =>
          //e.printStackTrace()
      }

      //流出中包数：根据配置规定判断中包字节数
      var outputMediumPacket = 0L
      try{
        outputMediumPacket = values(23).trim.toLong
      }catch{
        case e : NumberFormatException =>
          //e.printStackTrace()
      }

      //中包数：根据配置规定判断中包字节数
      val mediumPacket = inputMediumPacket + outputMediumPacket

      //流入大包数：根据配置规定判断大包字节数
      var inputLargePacket = 0L
      try{
        inputLargePacket = values(24).trim.toLong
      }catch{
        case e : NumberFormatException =>
          //e.printStackTrace()
      }

      //流出大包数；根据配置规定判断大包字节数
      var outputLargePacket = 0L
      try{
        outputLargePacket = values(25).trim.toLong
      }catch{
        case e : NumberFormatException =>
          //e.printStackTrace()
      }

      //大包数；根据配置规定判断大包字节数
      val largePacket = inputLargePacket + outputLargePacket

      //流入零窗口数
      var inputZeroWindow = 0L
      try{
        inputZeroWindow = values(26).trim.toLong
      }catch{
        case e : NumberFormatException =>
          //e.printStackTrace()
      }

      //流出零窗口数
      var outputZeroWindow = 0L
      try{
        outputZeroWindow = values(27).trim.toLong
      }catch{
        case e : NumberFormatException =>
          //e.printStackTrace()
      }

      //零窗口数
      val zeroWindow = inputZeroWindow + outputZeroWindow

      //结束时间戳-ms
      var finishTime = 0L
      try{
        finishTime = values(29).trim.toLong
        if (finishTime >= timeStamp){
          finishTime = finishTime - timeStamp
        }
      }catch{
        case e : NumberFormatException =>
          //e.printStackTrace()
      }

      //SYN时间戳-ms
      var synTime = 0L
      try{
        synTime = values(30).trim.toLong
        if (synTime >= timeStamp){
          synTime = synTime - timeStamp
        }
      }catch{
        case e : NumberFormatException =>
          //e.printStackTrace()
      }

      //SYN ACK时间戳-ms
      var synAckTime = 0L
      try{
        synAckTime = values(31).trim.toLong
        if (synAckTime >= timeStamp) {
          synAckTime = synAckTime - timeStamp
        }
      }catch{
        case e : NumberFormatException =>
          //e.printStackTrace()
      }

      //ACK时间戳-ms
      var ackTime = 0L
      try{
        ackTime = values(32).trim.toLong
        if (ackTime >= timeStamp) {
          ackTime = ackTime - timeStamp
        }
      }catch{
        case e : NumberFormatException =>
          //e.printStackTrace()
      }

      //Time to live
      var ttl = 0L
      try{
        ttl = values(33).trim.toLong
      }catch{
        case e : NumberFormatException =>
          //e.printStackTrace()
      }

      //TCP协议或UDP协议（1：TCP、0：UDP）
      val protocol = if ("0".equals(values(34).trim)) "udp" else "tcp"

      //连接次数
      val connCount = 1L

      Some(new QuintetModel(timeStamp, sourceIp, sourcePort, sourceMac, destinationIp,
        destinationPort, destinationMac, protocolId, vlanId, probeId, inputOctets, outputOctets,
        octets, inputPacket, outputPacket, packet, inputRetransPacket, outputRetransPacket,
        retransPacket, inputRetransDelay, outputRetransDelay, retransDelay, inputRest, outputRest,
        rest, isSucceed, inputSmallPacket, outputSmallPacket, smallPacket, inputMediumPacket,
        outputMediumPacket, mediumPacket, inputLargePacket, outputLargePacket, largePacket,
        inputZeroWindow, outputZeroWindow, zeroWindow, finishTime, synTime, synAckTime, ackTime,
        ttl, protocol, connCount))
    }else{
      None
    }
  }

}
