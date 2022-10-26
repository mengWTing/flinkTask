package cn.ffcs.is.mss.analyzer.flink.unknowRisk.model

import com.metamx.common.scala.untyped.Dict
import com.metamx.tranquility.typeclass.Timestamper
import io.druid.query.aggregation.LongSumAggregatorFactory
import org.joda.time.{DateTime, DateTimeZone}
import org.json.JSONObject

import java.util.Date

/**
 * @title WarningDbModel
 * @author hanyu
 * @date 2021-10-25 09:15
 * @description
 * @update [no][date YYYY-MM-DD][name][description]
 */
case class WarningDbModel(var userName: String, var alertName: String, var alertTime: Long, var connCount: Long,
                          var loginMajor: String, var loginSystem: String, var usedPlace: String,
                          var isRemote: Int, var alertSrcIp: String,
                          var alertSrcPort: String, var alertDestIp: String, var alertDestPort: String,
                          var url: String, var httpStatus: Int, var packageType: String, var packageValue: String) {

  def this() {
    this(null, null, 0L, 0L, null, null, null, -1, null, null, null, null, null, 0, null, null)
  }

  /**
   * 使用字符串拼接成json串
   *
   * @return
   */
  override def toString: String = {
    val stringBuilder = new StringBuilder
    stringBuilder.append("{")
    stringBuilder.append("\"userName\":\"" + userName + "\",")
    stringBuilder.append("\"alertName\":\"" + alertName + "\",")
    stringBuilder.append("\"alertTime\":\"" + new DateTime(alertTime, DateTimeZone.forID(WarningDbModel.druidFormat))
      + "\",")
    stringBuilder.append("\"connCount\":\"" + connCount + "\",")
    stringBuilder.append("\"loginMajor\":\"" + loginMajor + "\",")
    stringBuilder.append("\"loginSystem\":\"" + loginSystem + "\",")
    stringBuilder.append("\"usedPlace\":\"" + usedPlace + "\",")
    stringBuilder.append("\"isRemote\":\"" + isRemote + "\",")
    stringBuilder.append("\"alertSrcIp\":\"" + alertSrcIp + "\",")
    stringBuilder.append("\"alertSrcPort\":\"" + alertSrcPort + "\",")
    stringBuilder.append("\"alertDestIp\":\"" + alertDestIp + "\",")
    stringBuilder.append("\"alertDestPort\":\"" + alertDestPort + "\",")
    stringBuilder.append("\"url\":\"" + url + "\",")
    stringBuilder.append("\"httpStatus\":\"" + httpStatus + "\",")
    stringBuilder.append("\"packageType\":\"" + packageType + "\",")
    stringBuilder.append("\"packageValue\":\"" + packageValue + "\"")
    stringBuilder.append("}")
    stringBuilder.toString
  }

  override def equals(obj: Any): Boolean = {
    if (obj.isInstanceOf[WarningDbModel]) {
      val dealModel = obj.asInstanceOf[WarningDbModel]
      dealModel.canEquals(this) && super.equals(dealModel) &&
        dealModel.toString.equals(super.toString)
    }
    else false
  }

  def canEquals(obj: Any): Boolean = obj.isInstanceOf[WarningDbModel]

  override def hashCode: Int = toString.hashCode

  /**
   * 生成json串
   *
   * @return
   */
  def toJson: String = {
    val jsonObject = new JSONObject
    jsonObject.put("userName", userName)
    jsonObject.put("alertName", alertName)
    jsonObject.put("alertTime", new DateTime(alertTime, DateTimeZone.forID(WarningDbModel.druidFormat)))
    jsonObject.put("connCount", connCount)
    jsonObject.put("loginMajor", loginMajor)
    jsonObject.put("loginSystem", loginSystem)
    jsonObject.put("usedPlace", usedPlace)
    jsonObject.put("isRemote", isRemote)
    jsonObject.put("alertSrcIp", alertSrcIp)
    jsonObject.put("alertSrcPort", alertSrcPort)
    jsonObject.put("alertDestIp", alertDestIp)
    jsonObject.put("alertDestPort", alertDestPort)
    jsonObject.put("url", url)
    jsonObject.put("httpStatus", httpStatus)
    jsonObject.put("packageType", packageType)
    jsonObject.put("packageValue", packageValue)
    jsonObject.toString
  }

}

object WarningDbModel {

  //写入druid的时间戳格式
  val druidFormat = "+08:00"

  val Columns = IndexedSeq("userName", "alertName", "alertTime", "loginMajor", "loginSystem", "usedPlace", "isRemote",
    "alertSrcIp", "alertSrcPort", "alertDestIp", "alertDestPort", "url", "httpStatus", "packageType", "packageValue")

  val Metrics = Seq(
    new LongSumAggregatorFactory("connCount", "connCount"))


  implicit val simpleEventTimestamper = new Timestamper[WarningDbModel] {
    def timestamp(warningDbModel: WarningDbModel) = new DateTime(warningDbModel.alertTime)
  }


  def fromMap(d: Dict): WarningDbModel = {
    new WarningDbModel(
      d("userName").toString,
      d("alertName").toString,
      d("alertTime").toString.toLong,
      d("connCount").toString.toLong,
      d("loginMajor").toString,
      d("loginSystem").toString,
      d("usedPlace").toString,
      d("isRemote").toString.toInt,
      d("alertSrcIp").toString,
      d("alertSrcPort").toString,
      d("alertDestIp").toString,
      d("alertDestPort").toString,
      d("url").toString,
      d("httpStatus").toString.toInt,
      d("packageType").toString,
      d("packageValue").toString)

  }

  //  private val REGEX: String = "\\|"
  //
  //  protected var placeMap: util.Map[String, String] = null
  //  protected var systemMap: util.Map[String, String] = null
  //  protected var majorMap: util.Map[String, String] = null
  //  protected var usedPlacesMap: util.Map[String, util.TreeSet[String]] = null
  //
  //  protected val sysIdMap: util.Map[String, String] = new util.HashMap[String, String]()
  //  sysIdMap.put("YWCB", "运维成本")
  //  sysIdMap.put("MATERIAL", "物流管理")
  //  sysIdMap.put("PROJECT", "计划建设")
  //  sysIdMap.put("CONTRACT", "合同管理")
  //  sysIdMap.put("ICT", "ICT")
  //  sysIdMap.put("ANALYSIS", "合同解析")
  //
  //  var sysidPattern: Pattern = Pattern.compile(".*value\\(sysid\\)=(.*?)&value.*")
  //  var GCFZ: String = "工辅"

  def getWarningDbModel(line: String): Option[WarningDbModel] = {
    //    val date = new Date()
    var userName = ""
    var alertName = ""
    var alertTime = 0L
    //    var alertTime = date.getTime
    var loginMajor = ""
    var loginSystem = ""
    var usedPlace = ""
    var isRemote = -1
    var alertSrcIp = ""
    var alertSrcPort = ""
    var alertDestIp = ""
    var alertDestPort = ""
    var url = ""
    var httpStatus = 0
    var packageType = ""
    var packageValue = ""
    val values = line.split("\\|", -1)
    try {
      userName = values(0).trim
    } catch {
      case e: Exception =>
    }
    try {
      alertName = values(1).trim
    } catch {
      case e: Exception =>
    }

    try {
      alertTime = values(2).trim.toLong
    } catch {
      case e: Exception =>
    }

    try {
      alertSrcIp = values(7).trim
    } catch {
      case e: Exception =>
    }

    try {
      alertSrcPort = values(8).trim
    } catch {
      case e: Exception =>
    }

    try {
      alertDestIp = values(9).trim
    } catch {
      case e: Exception =>
    }

    try {
      alertDestPort = values(10).trim
    } catch {
      case e: Exception =>
    }

    try {
      url = values(11).trim
    } catch {
      case e: Exception =>
    }

    try {
      httpStatus = values(12).trim.toInt
    } catch {
      case e: Exception =>
    }
    try
      packageType = values(13).trim
    catch {
      case e: Exception =>
    }
    try
      packageValue = values(14).trim
    catch {
      case e: Exception =>
    }
    val connCount = 1L
    if (values.length == 15) {
      try {
        loginMajor = values(3).trim
      } catch {
        case e: Exception =>
      }
      try {
        loginSystem = values(4).trim
      } catch {
        case e: Exception =>
      }
      try {
        usedPlace = values(5).trim
      } catch {
        case e: Exception =>
      }
      if (values(6).equals("true")) {
        isRemote = 1
      } else {
        isRemote = 0
      }
      Some(new WarningDbModel(userName, alertName, alertTime, connCount, loginMajor, loginSystem, usedPlace, isRemote,
        alertSrcIp, alertSrcPort, alertDestIp, alertDestPort, url, httpStatus, packageType, packageValue))

    }
    //    else if (values.length == 16) {
    //      val host = values(15)
    //      //登录系统
    //      loginSystem = getAlertSystem(host, url)
    //      //登录专业
    //      loginMajor = getAlertMajor(host)
    //      //常用登录地
    //      usedPlace = getAlertUsedPlace(userName)
    //      //本次登录地
    //      val loginPlace = getNowPlace(alertSrcIp)
    //      //是否异地登录
    //      val Remote = getRemote(loginPlace, usedPlace)
    //      if (Remote.equals("true")) {
    //        isRemote = 1
    //      } else {
    //        isRemote = 0
    //      }
    //      Some(new WarningDbModel(userName, alertName, alertTime, connCount, loginMajor, loginSystem, usedPlace, isRemote,
    //        alertSrcIp, alertSrcPort, alertDestIp, alertDestPort, url, httpStatus, packageType, packageValue))
    //
    //    }
    else {
      None
    }


  }

  //  def getAlertSystem(host: String, url: String): String = {
  //
  //    if (systemMap != null && host != null && url != null) {
  //      if (GCFZ.equals(getAlertMajor(host))) {
  //        val matcher = sysidPattern.matcher(url)
  //        if (matcher.find) {
  //          val systemTemp = sysIdMap.get(matcher.group(1))
  //          val system = systemMap.get(host)
  //          val words = system.split("-", -1)
  //          if (words.length == 2 && systemTemp != null) {
  //            return systemTemp + "-" + words(1)
  //          }
  //        }
  //
  //      }
  //      if (systemMap.containsKey(host)) {
  //        return systemMap.get(host)
  //      }
  //    }
  //    "未知系统"
  //  }

  //  def getAlertMajor(host: String): String = {
  //    if (majorMap != null) {
  //      val major = majorMap.get(host)
  //      if (major != null && major.length > 0) {
  //        return major
  //      }
  //    }
  //    "未知专业"
  //  }

  //  def getAlertUsedPlace(userName: String): String = {
  //    if (usedPlacesMap != null) {
  //      val usedPlacesSet = usedPlacesMap.get(userName)
  //      if (usedPlacesSet != null) {
  //        val iterator = usedPlacesSet.iterator()
  //        var usedPlace: String = null
  //        while (iterator.hasNext) {
  //          if (usedPlace == null) {
  //            usedPlace = iterator.next()
  //          } else {
  //            usedPlace = iterator.next() + "^" + usedPlace
  //          }
  //        }
  //
  //        if (usedPlace != null) {
  //          return usedPlace
  //        }
  //      }
  //    }
  //    "常用登录地不详"
  //
  //  }

  //  def getNowPlace(ip: String): String = {
  //
  //    if (placeMap != null && ip != null) {
  //      if (placeMap.containsKey(ip)) {
  //        return placeMap.get(ip)
  //      } else {
  //        val ipPart = ip.split("\\.", -1)
  //        if (ipPart.length == 4) {
  //          val ipTwo = ipPart(0) + "." + ipPart(1)
  //          val ipThree = ipTwo + "." + ipPart(2)
  //          if (placeMap.containsKey(ipThree)) {
  //            return placeMap.get(ipThree)
  //          } else if (placeMap.containsKey(ipTwo)) {
  //            return placeMap.get(ipTwo)
  //          } else if (placeMap.containsKey(ipPart(0))) {
  //            return placeMap.get(ipPart(0))
  //          }
  //        }
  //      }
  //    }
  //    "未知地点"
  //  }

  //  def getRemote(place: String, usedPlace: String): String = {
  //    if ("未知地点".equals(place) || usedPlace == null || usedPlace.isEmpty) {
  //      return false.toString
  //    } else {
  //      if (usedPlacesMap != null && usedPlacesMap.containsKey(usedPlace)) {
  //        return (!usedPlacesMap.get(usedPlace).contains(place)).toString
  //      }
  //    }
  //
  //    return false.toString
  //
  //  }

  //  def setPlaceMap(path: String): Unit = {
  //
  //    val fileSystem = FileSystem.get(URI.create(path), new Configuration)
  //    val fsDataInputStream = fileSystem.open(new Path(path))
  //    val bufferedReader = new BufferedReader(new InputStreamReader(fsDataInputStream))
  //
  //    val placeMap = new util.Hashtable[String, String]
  //    var line = bufferedReader.readLine()
  //
  //    while (line != null) {
  //      val values = line.split(REGEX, -1)
  //      if (values.length == 2) {
  //        placeMap.put(values(0).trim, values(1).trim)
  //      }
  //      line = bufferedReader.readLine()
  //    }
  //
  //    //    bufferedReader.close()
  //    //    fsDataInputStream.close()
  //
  //    this.placeMap = placeMap
  //  }
  //
  //  def setSystemMap(path: String): Unit = {
  //    val fileSystem = FileSystem.get(URI.create(path), new Configuration)
  //    val fsDataInputStream = fileSystem.open(new Path(path))
  //    val bufferedReader = new BufferedReader(new InputStreamReader(fsDataInputStream))
  //
  //    val systemMap = new util.Hashtable[String, String]
  //
  //    var line = bufferedReader.readLine
  //
  //
  //    while (line != null) {
  //      val values = line.split(REGEX, -1)
  //      if (values.length == 3) {
  //        val host = values(0).trim.toLowerCase
  //        val system = values(2).trim.toLowerCase
  //        systemMap.put(host, system)
  //      }
  //      line = bufferedReader.readLine()
  //    }
  //
  //
  //    //    bufferedReader.close()
  //    //    fsDataInputStream.close()
  //    this.systemMap = systemMap
  //  }
  //
  //  def setMajorMap(path: String): Unit = {
  //    val fileSystem = FileSystem.get(URI.create(path), new Configuration)
  //    val fsDataInputStream = fileSystem.open(new Path(path))
  //    val bufferedReader = new BufferedReader(new InputStreamReader(fsDataInputStream))
  //
  //    val majorMap = new util.Hashtable[String, String]
  //    var line = bufferedReader.readLine
  //
  //
  //    while (line != null) {
  //      val values = line.split(REGEX, -1)
  //      if (values.length == 3) {
  //        val host = values(0).trim.toLowerCase
  //        val major = values(1).trim.toLowerCase
  //        majorMap.put(host, major)
  //      }
  //      line = bufferedReader.readLine()
  //    }
  //
  //
  //    //    bufferedReader.close()
  //    //    fsDataInputStream.close()
  //    this.majorMap = majorMap
  //  }
  //
  //  def setUsedPlacesMap(path: String): Unit = {
  //    val fileSystem = FileSystem.get(URI.create(path), new Configuration())
  //    val fsDataInputStream = fileSystem.open(new Path(path))
  //    val bufferedReader = new BufferedReader(new InputStreamReader(fsDataInputStream))
  //
  //    val usedPlacesMap = new util.Hashtable[String, util.TreeSet[String]]
  //    var line = bufferedReader.readLine()
  //    while (line != null) {
  //      val values = line.split(REGEX, 2)
  //      if (values.length > 1) {
  //        val usedPlaces = values(1).trim.split(REGEX, -1)
  //        val usedPlacesSet = new util.TreeSet[String]()
  //
  //        usedPlaces.foreach(usedPlacesSet.add(_))
  //
  //        usedPlacesMap.put(values(0).trim, usedPlacesSet)
  //      }
  //      line = bufferedReader.readLine()
  //    }
  //
  //
  //    //    bufferedReader.close()
  //    //    fsDataInputStream.close()
  //    this.usedPlacesMap = usedPlacesMap
  //  }

}


