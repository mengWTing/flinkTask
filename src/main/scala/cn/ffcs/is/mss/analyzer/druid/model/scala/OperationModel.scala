package cn.ffcs.is.mss.analyzer.druid.model.scala

import java.io.{BufferedReader, InputStreamReader}
import java.net.{URI, URLDecoder}
import java.util
import java.util.regex.Pattern

import com.metamx.common.scala.untyped.Dict
import com.metamx.tranquility.typeclass.Timestamper
import io.druid.query.aggregation.LongSumAggregatorFactory
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}
import org.joda.time.{DateTime, DateTimeZone}
import org.json.JSONObject

/**
 * 44000403@GD|10.140.9.19|80|10.18.247.226|61361|cwfz-gd.mss.ctc.com|http://cwfz-gd.mss.ctc.com/fssc/dwr/call/plaincall
 * /writeoffRuleDWR.getCapitalToBePaid.dwr||http://cwfz-gd.mss.ctc.com/
 * fssc/wfclient.messageEntryMss.do?cmd=prepare&isControlled=N&id=4028bdb077e464ec0177f6c7bdcc52bd|
 * Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 10.0; WOW64; Trident/7.0; .NET4.0C; .NET4.0E; hwvcloud4; hwcloud4)|
 * 1615360196665|449|1952|200|1615360196704|1615360196665|73|1615360196704|0|0|1615360196738|0|0|tcp|0|0|0|0||2||
 * text/javascript;charset=UTF-8|MSSnaplog-00-jituan-20210310150956_7281.txt
 * ---------------------------------------------------------------------------------------------------------------------
 * 用户名|用户访问的目标ipv4或ipv6地址|用户访问的目标端口号|用户访问目标时，使用的ipv4或ipv6|
 * 用户访问目标时使用的端口号|域名|用户访问的目标网站的URL|cmd/act（先空着）	用户具体的操作|
 * 链接源信息|Use Agent信息|每个请求包的时间点|发给用户的业务字节数|
 * 用户发出的业务字节数|http状态码|网页最后1个Response消息时间点|
 * 网页第1个GET请求时间点网页|网页大小|
 * 终端收到目标服务器响应的第一个数据响应包（第一个respone包）的时间点|
 * 终端向网页发起连接请求的时间点|TCP三次握手的[ACK]的时间点|
 * 终端向服务器端发送[FIN.ACk]的时间点|DNS查询请求时间点|DNS查询响应时间点|
 * 协议类型|Sql	URL或者form item是否包含sql语句|是否是下载行为（1-是、0-否）|
 * 是否下载成功（1-成功、0-失败）|下载的文件大小|下载的文件名|
 * 请求方式（1-GET、2-POST、3-其他）|请求内容|返回包类型|返回内容存储文件名
 *
 * @param userName
 * @param destinationIp
 * @param destinationPort
 * @param loginMajor
 * @param loginSystem
 * @param sourceIp
 * @param sourcePort
 * @param loginPlace
 * @param usedPlace
 * @param isRemote
 * @param operate
 * @param timeStamp
 * @param inputOctets
 * @param outputOctets
 * @param octets
 * @param connCount
 * @param isDownload
 * @param isDownSuccess
 * @param downFileSize
 * @param downFileName
 */
case class OperationModel(var userName: String, var destinationIp: String, var destinationPort: String,
                          var loginMajor: String, var loginSystem: String, var sourceIp: String,
                          var sourcePort: String, var loginPlace: String, var usedPlace: String,
                          var isRemote: String, var operate: String, var timeStamp: Long,
                          var inputOctets: Long, var outputOctets: Long, var octets: Long,
                          var connCount: Long, var isDownload: String, var isDownSuccess: String,
                          var downFileSize: Long, var downFileName: String, var httpStatus: Int, var
                          packageType: String, var packagePath: String) {


  def this() {
    this(null, null, null, null, null, null, null, null, null, null, null, 0L, 0L, 0L, 0L, 0L, null, null, 0L, null,
      0, null, null)
  }


  /**
   * 使用字符串拼接成json串
   *
   * @return
   */
  override def toString: String = {
    val stringBuilder = new StringBuilder
    stringBuilder.append("{")
    stringBuilder.append("\"timeStamp\":\"" + new DateTime(timeStamp, DateTimeZone.forID(OperationModel.druidFormat))
      + "\",")
    stringBuilder.append("\"userName\":\"" + userName + "\",")
    stringBuilder.append("\"destinationIp\":\"" + destinationIp + "\",")
    stringBuilder.append("\"destinationPort\":\"" + destinationPort + "\",")
    stringBuilder.append("\"loginMajor\":\"" + loginMajor + "\",")
    stringBuilder.append("\"loginSystem\":\"" + loginSystem + "\",")
    stringBuilder.append("\"sourceIp\":\"" + sourceIp + "\",")
    stringBuilder.append("\"sourcePort\":\"" + sourcePort + "\",")
    stringBuilder.append("\"loginPlace\":\"" + loginPlace + "\",")
    stringBuilder.append("\"usedPlace\":\"" + usedPlace + "\",")
    stringBuilder.append("\"isRemote\":\"" + isRemote + "\",")
    stringBuilder.append("\"operate\":\"" + operate + "\",")
    stringBuilder.append("\"inputOctets\":" + inputOctets + ",")
    stringBuilder.append("\"outputOctets\":" + outputOctets + ",")
    stringBuilder.append("\"octets\":" + octets + ",")
    stringBuilder.append("\"connCount\":" + connCount + ",")
    stringBuilder.append("\"isDownload\":\"" + isDownload + "\",")
    stringBuilder.append("\"isDownSuccess\":\"" + isDownSuccess + "\",")
    stringBuilder.append("\"downFileSize\":" + downFileSize + ",")
    stringBuilder.append("\"downFileName\":\"" + downFileName + "\",")
    stringBuilder.append("\"httpStatus\":\"" + httpStatus + "\",")
    stringBuilder.append("\"packageType\":\"" + packageType + "\",")
    stringBuilder.append("\"packagePath\":\"" + packagePath + "\"")

    stringBuilder.append("}")
    stringBuilder.toString
  }

  override def equals(obj: Any): Boolean = {
    if (obj.isInstanceOf[OperationModel]) {
      val dealModel = obj.asInstanceOf[OperationModel]
      dealModel.canEquals(this) && super.equals(dealModel) &&
        dealModel.toString.equals(super.toString)
    }
    else false
  }

  def canEquals(obj: Any): Boolean = obj.isInstanceOf[OperationModel]

  override def hashCode: Int = toString.hashCode

  /**
   * 生成json串
   *
   * @return
   */
  def toJson: String = {
    val jsonObject = new JSONObject
    jsonObject.put("timeStamp", new DateTime(timeStamp, DateTimeZone.forID(OperationModel.druidFormat)))
    jsonObject.put("userName", userName)
    jsonObject.put("destinationIp", destinationIp)
    jsonObject.put("destinationPort", destinationPort)
    jsonObject.put("loginMajor", loginMajor)
    jsonObject.put("loginSystem", loginSystem)
    jsonObject.put("sourceIp", sourceIp)
    jsonObject.put("sourcePort", sourcePort)
    jsonObject.put("loginPlace", loginPlace)
    jsonObject.put("usedPlace", usedPlace)
    jsonObject.put("isRemote", isRemote)
    jsonObject.put("operate", operate)
    jsonObject.put("inputOctets", inputOctets)
    jsonObject.put("outputOctets", outputOctets)
    jsonObject.put("octets", octets)
    jsonObject.put("connCount", connCount)
    jsonObject.put("isDownload", isDownload)
    jsonObject.put("isDownSuccess", isDownSuccess)
    jsonObject.put("downFileSize", downFileSize)
    jsonObject.put("downFileName", downFileName)
    jsonObject.put("httpStatus", httpStatus)
    jsonObject.put("packageType", packageType)
    jsonObject.put("packagePath", packagePath)
    jsonObject.toString
  }

}


object OperationModel {

  //写入druid的时间戳格式
  val druidFormat = "+08:00"


  val Columns = IndexedSeq("userName", "destinationIp", "destinationPort", "loginMajor", "loginSystem",
    "sourceIp", "sourcePort", "loginPlace", "usedPlace", "isRemote", "operate", "isDownload", "isDownSuccess",
    "downFileName", "httpStatus")

  val Metrics = Seq(new LongSumAggregatorFactory("inputOctets", "inputOctets"),
    new LongSumAggregatorFactory("outputOctets", "outputOctets"),
    new LongSumAggregatorFactory("octets", "octets"),
    new LongSumAggregatorFactory("connCount", "connCount"),
    new LongSumAggregatorFactory("downFileSize", "downFileSize"))


  implicit val simpleEventTimestamper = new Timestamper[OperationModel] {
    def timestamp(operationModel: OperationModel) = new DateTime(operationModel.timeStamp)
  }


  def fromMap(d: Dict): OperationModel = {
    new OperationModel(
      d("userName").toString,
      d("destinationIp").toString,
      d("destinationPort").toString,
      d("loginMajor").toString,
      d("loginSystem").toString,
      d("sourceIp").toString,
      d("sourcePort").toString,
      d("loginPlace").toString,
      d("usedPlace").toString,
      d("isRemote").toString,
      d("operate").toString,
      d("timeStamp").toString.toLong,
      d("inputOctets").toString.toLong,
      d("outputOctets").toString.toLong,
      d("octets").toString.toLong,
      d("connCount").toString.toLong,
      d("isDownload").toString,
      d("isDownSuccess").toString,
      d("downFileSize").toString.toLong,
      d("downFileName").toString,
      d("httpStatus").toString.toInt,
      d("packageType").toString,
      d("packagePath").toString)

  }


  private val REGEX: String = "\\|"

  protected var placeMap: util.Map[String, String] = null
  protected var systemMap: util.Map[String, String] = null
  protected var majorMap: util.Map[String, String] = null
  protected var usedPlacesMap: util.Map[String, util.TreeSet[String]] = null

  protected val sysIdMap: util.Map[String, String] = new util.HashMap[String, String]()
  sysIdMap.put("YWCB", "运维成本")
  sysIdMap.put("MATERIAL", "物流管理")
  sysIdMap.put("PROJECT", "计划建设")
  sysIdMap.put("CONTRACT", "合同管理")
  sysIdMap.put("ICT", "ICT")
  sysIdMap.put("ANALYSIS", "合同解析")

  var sysidPattern: Pattern = Pattern.compile(".*value\\(sysid\\)=(.*?)&value.*")
  var GCFZ: String = "工辅"

  def getOperationModel(line: String): Option[OperationModel] = {
    val values = line.split("\\|", -1)

    if (values.length == 29 || values.length == 25 || values.length == 31 || values.length == 33) {
      //人力编码 0
      val userName = getUserName(line)

      //目的IP 1
      val destinationIp = getDstIp(line)

      //目的PORT 2
      val destinationPort = getDstPort(line)

      //本次访问的url 6
      val url = getUrl(line)

      //本次访问的host 5
      val host = getHost(line)

      //登录系统
      val loginSystem = getSystem(host, url)

      //登录专业
      val loginMajor = getMajor(host)

      //源IP 3
      val sourceIp = getSrcIp(line)

      //源PORT 4
      val sourcePort = getSrcPort(line)

      //http相应状态码 13
      val httpStatus = getHttpStatus(line)

      //常用登录地
      val usedPlace = getUsedPlace(userName)

      //本次登录地
      val loginPlace = getPlace(sourceIp)

      //是否异地登录
      val isRemote = getIsRemote(loginPlace, usedPlace)

      //根据url匹配的操作
      val operate = getOperate(url, loginMajor, loginSystem)


      //时间戳
      var timeStamp = 0L
      try {
        timeStamp = values(10).trim.toLong
      } catch {
        case e: NumberFormatException =>
        //          e.printStackTrace()
      }

      //上行流量
      var inputOctets = 0L
      try {
        inputOctets = values(11).trim.toLong
      } catch {
        case e: NumberFormatException =>
        //          e.printStackTrace()
      }

      //下行流量
      var outputOctets = 0L
      try {
        outputOctets = values(12).trim.toLong
      } catch {
        case e: NumberFormatException =>
        //          e.printStackTrace()
      }

      //总流量
      val octets: Long = inputOctets + outputOctets

      //连接次数
      val connCount = 1L

      if (values.length == 25) {
        Some(new OperationModel(userName, destinationIp, destinationPort, loginMajor, loginSystem,
          sourceIp, sourcePort, loginPlace, usedPlace, isRemote, operate, timeStamp, inputOctets,
          outputOctets, octets, connCount, "", "", 0L, "", httpStatus, "", ""))
      } else {


        //是否是下载
        val isDownload = values(25)

        //是否下载成功
        val isDownSuccess = values(26)

        //下载文件大小
        var downFileSize = 0L
        try {
          downFileSize = values(27).trim.toLong
        } catch {
          case e: NumberFormatException =>
          //          e.printStackTrace()
        }

        //下载文件名
        val downFileName = getDownFileName(line)

        if (values.length == 33) {
          val packageType = values(31)
          val packagePath = values(32)
          Some(new OperationModel(userName, destinationIp, destinationPort, loginMajor, loginSystem,
            sourceIp, sourcePort, loginPlace, usedPlace, isRemote, operate, timeStamp, inputOctets,
            outputOctets, octets, connCount, isDownload, isDownSuccess, downFileSize, downFileName, httpStatus,
            packageType, packagePath))
        } else {
          Some(new OperationModel(userName, destinationIp, destinationPort, loginMajor, loginSystem,
            sourceIp, sourcePort, loginPlace, usedPlace, isRemote, operate, timeStamp, inputOctets,
            outputOctets, octets, connCount, isDownload, isDownSuccess, downFileSize, downFileName, httpStatus,
            "", ""))
        }


      }
    } else {
      None
    }
  }

  /**
   * 去掉host的端口部分
   *
   * @param line
   * @return
   */
  def getHost(line: String): String = {
    val host = line.split("\\|", -1)(5)
    if (host.contains(":")) {
      host.split(":", -1)(0).toLowerCase()
    } else {
      host.toLowerCase()
    }
  }

  /**
   * 根据IP获取登录地
   *
   * @param ip
   * @return
   */
  def getPlace(ip: String): String = {

    if (placeMap != null && ip != null) {
      if (placeMap.containsKey(ip)) {
        return placeMap.get(ip)
      } else {
        val ipPart = ip.split("\\.", -1)
        if (ipPart.length == 4) {

          val ipTwo = ipPart(0) + "." + ipPart(1)
          val ipThree = ipTwo + "." + ipPart(2)
          if (placeMap.containsKey(ipThree)) {
            return placeMap.get(ipThree)
          } else if (placeMap.containsKey(ipTwo)) {
            return placeMap.get(ipTwo)
          } else if (placeMap.containsKey(ipPart(0))) {
            return placeMap.get(ipPart(0))
          }
        }
      }
    }
    "未知地点"
  }


  /**
   * 根据系统名获取专业名
   *
   * @param host
   * @return
   */
  def getMajor(host: String): String = {
    if (majorMap != null) {
      val major = majorMap.get(host)
      if (major != null && major.length > 0) {
        return major
      }
    }
    "未知专业"
  }

  /**
   * 根据host获取登录系统
   *
   * @param host
   * @param url
   * @return
   */
  def getSystem(host: String, url: String): String = {
    if (systemMap != null && host != null && url != null) {
      if (GCFZ.equals(getMajor(host))) {
        val matcher = sysidPattern.matcher(url)
        if (matcher.find) {
          val systemTemp = sysIdMap.get(matcher.group(1))
          val system = systemMap.get(host)
          val words = system.split("-", -1)
          if (words.length == 2 && systemTemp != null) {
            return systemTemp + "-" + words(1)
          }
        }

      }
      if (systemMap.containsKey(host)) {
        return systemMap.get(host)
      }
    }
    "未知系统"
  }


  /**
   * 根据url登录专业登录系统获取操作
   *
   * @param url
   * @param loginMajor
   * @param loginSystem
   * @return
   */
  def getOperate(url: String, loginMajor: String, loginSystem: String) = {
    "未识别操作"
  }


  /**
   * 根据常用登录地和本次登录地，判断是否异地登录
   *
   * @param place
   * @param usedPlace
   * @return
   */
  def getIsRemote(place: String, usedPlace: String): String = {
    if ("未知地点".equals(place) || usedPlace == null || usedPlace.length == 0) {
      return false.toString
    } else {
      if (usedPlacesMap != null && usedPlacesMap.containsKey(usedPlace)) {
        return (!usedPlacesMap.get(usedPlace).contains(place)).toString
      }
    }

    return false.toString

  }

  /**
   * 根据用户名获取其常用登录地
   *
   * @param userName
   * @return
   */
  def getUsedPlace(userName: String): String = {
    if (usedPlacesMap != null) {
      val usedPlacesSet = usedPlacesMap.get(userName)
      if (usedPlacesSet != null) {
        val iterator = usedPlacesSet.iterator()
        var usedPlace: String = null
        while (iterator.hasNext) {
          if (usedPlace == null) {
            usedPlace = iterator.next()
          } else {
            usedPlace = iterator.next() + "|" + usedPlace
          }
        }

        if (usedPlace != null) {
          return usedPlace
        }
      }
    }
    "常用登录地不详"

  }

  /**
   * 获取用户名
   *
   * @param line
   * @return
   */
  def getUserName(line: String): String = {

    val userNameField = line.split("\\|", -1)(0)
    val userName = userNameField.toUpperCase.trim
    if (userName.length == 0) {
      return "匿名用户"
    }
    userName
  }

  /**
   * 获取源IP
   *
   * @param line
   * @return
   */
  def getSrcIp(line: String): String = {
    getIp(line.split("\\|", -1)(3))
  }

  /**
   * 获取目的IP
   *
   * @param line
   * @return
   */
  def getDstIp(line: String): String = {
    getIp(line.split("\\|", -1)(1))
  }

  /**
   * 获取IP
   *
   * @param ipField
   * @return
   */
  def getIp(ipField: String): String = {

    val ip = ipField.trim
    if (ip.length == 0) {
      return ""
    }
    ip
  }

  /**
   * 获取源IP
   *
   * @param line
   * @return
   */
  def getSrcPort(line: String): String = {
    getPort(line.split("\\|", -1)(4))
  }

  /**
   * 获取目的PORT
   *
   * @param line
   * @return
   */
  def getDstPort(line: String): String = {
    getPort(line.split("\\|", -1)(2))
  }

  /**
   * 获取PORT
   *
   * @param portField
   * @return
   */
  def getPort(portField: String): String = {
    val port = portField.trim
    if (port.length == 0) {
      return null
    }
    return port
  }

  /**
   * 获取相应状态码
   *
   * @param line
   * @return
   */
  def getHttpStatus(line: String): Int = {
    val httpStatusField = line.split("\\|", -1)(13)
    val httpStatus = httpStatusField.trim
    if (httpStatus.length == 0) {
      return -1
    }
    if (httpStatus.contains(":")) {
      return -1
    }

    //    httpStatus.toInt
    try {
      httpStatus.toInt
    } catch {
      case e: Exception =>
        return -1
    }
  }


  /**
   * 读取hdfs上的ip-地点关联文件
   *
   * @param path
   * @return
   */
  def setPlaceMap(path: String): Unit = {

    val fileSystem = FileSystem.get(URI.create(path), new Configuration)
    val fsDataInputStream = fileSystem.open(new Path(path))
    val bufferedReader = new BufferedReader(new InputStreamReader(fsDataInputStream))

    val placeMap = new util.Hashtable[String, String]
    var line = bufferedReader.readLine()

    while (line != null) {
      val values = line.split(REGEX, -1)
      if (values.length == 2) {
        placeMap.put(values(0).trim, values(1).trim)
      }
      line = bufferedReader.readLine()
    }

    //    bufferedReader.close()
    //    fsDataInputStream.close()

    this.placeMap = placeMap
  }

  /**
   * 读取hdfs上的host-系统名关联文件
   *
   * @param path
   * @return
   */
  def setSystemMap(path: String): Unit = {
    val fileSystem = FileSystem.get(URI.create(path), new Configuration)
    val fsDataInputStream = fileSystem.open(new Path(path))
    val bufferedReader = new BufferedReader(new InputStreamReader(fsDataInputStream))

    val systemMap = new util.Hashtable[String, String]

    var line = bufferedReader.readLine


    while (line != null) {
      val values = line.split(REGEX, -1)
      if (values.length == 3) {
        val host = values(0).trim.toLowerCase
        val system = values(2).trim.toLowerCase
        systemMap.put(host, system)
      }
      line = bufferedReader.readLine()
    }


    //    bufferedReader.close()
    //    fsDataInputStream.close()
    this.systemMap = systemMap
  }

  /**
   * 读取hdfs上的host-专业名关联文件
   *
   * @param path
   * @return
   */
  def setMajorMap(path: String): Unit = {
    val fileSystem = FileSystem.get(URI.create(path), new Configuration)
    val fsDataInputStream = fileSystem.open(new Path(path))
    val bufferedReader = new BufferedReader(new InputStreamReader(fsDataInputStream))

    val majorMap = new util.Hashtable[String, String]
    var line = bufferedReader.readLine


    while (line != null) {
      val values = line.split(REGEX, -1)
      if (values.length == 3) {
        val host = values(0).trim.toLowerCase
        val major = values(1).trim.toLowerCase
        majorMap.put(host, major)
      }
      line = bufferedReader.readLine()
    }


    //    bufferedReader.close()
    //    fsDataInputStream.close()
    this.majorMap = majorMap
  }

  /**
   * 读取hdfs上的用户名-常用登录地关联文件
   *
   * @param path
   * @return
   */
  def setUsedPlacesMap(path: String): Unit = {
    val fileSystem = FileSystem.get(URI.create(path), new Configuration())
    val fsDataInputStream = fileSystem.open(new Path(path))
    val bufferedReader = new BufferedReader(new InputStreamReader(fsDataInputStream))

    val usedPlacesMap = new util.Hashtable[String, util.TreeSet[String]]
    var line = bufferedReader.readLine()
    while (line != null) {
      val values = line.split(REGEX, 2)
      if (values.length > 1) {
        val usedPlaces = values(1).trim.split(REGEX, -1)
        val usedPlacesSet = new util.TreeSet[String]()

        usedPlaces.foreach(usedPlacesSet.add(_))

        usedPlacesMap.put(values(0).trim, usedPlacesSet)
      }
      line = bufferedReader.readLine()
    }


    //    bufferedReader.close()
    //    fsDataInputStream.close()
    this.usedPlacesMap = usedPlacesMap
  }

  /**
   * 获取下载文件名，将url的中午转码
   *
   * @param line
   * @return
   */
  def getDownFileName(line: String): String = {
    val downFileName = line.split("\\|", -1)(28)
    URLDecoder.decode(downFileName.replaceAll("%(?![0-9a-fA-F]{2})", "%25"), "utf-8")
  }

  /**
   * 获取url
   *
   * @param line
   * @return
   */
  def getUrl(line: String): String = {
    val values = line.split("\\|", -1)
    if (values.length > 6) {
      try {
        return URLDecoder.decode(values(6), "utf-8").trim
      } catch {
        case e: Exception =>
      }
      values(6).trim
    } else {
      ""
    }
  }


  /**
   * 获取user agent
   *
   * @param line
   * @return
   */
  def getUserAgent(line: String): String = {
    val values = line.split("\\|", -1)
    if (values.length > 9) values(9) else ""
  }

}
