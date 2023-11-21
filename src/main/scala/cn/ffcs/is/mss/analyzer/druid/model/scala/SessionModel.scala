package cn.ffcs.is.mss.analyzer.druid.model.scala

import org.joda.time.DateTime

/**
 * @ClassName:
 * @Author: mengwenting
 * @Date: 2023/11/19 12:24
 * @Description:
 * @update:
 */

/**
 * {"timeStamp": "2023-11-09 09:23:59","sessionId": "sessionId_668505439da5","sessionType": 0,
 * "sessionProtocol": 0,"sessionStart": 0,"sessionStop": 0,"omSource": 0,"sessionFileSize": "sessionFileSize_1207d819e521",
 * "tenantId": "tenantId_fb6a225cb869","vpcName": "vpcName_6316331d4c44","serverName": "serverName_b8831d82f571",
 * "serverUser": "serverUser_4f0b1be019bd","serverIp": "serverIp_977c5b3837b6","serverPort": "serverPort_0193599bae1b",
 * "instanceId": "instanceId_c53ce4f6f306","clientUser": "clientUser_e19f103384e6","clientUname": "clientUname_87206aff07d4",
 * "clientIp": "clientIp_77e8ad4915ba","authRuleNames": ["authRuleNames_54d80c0dacdc"],"isPlayed": 0}
 */
case class SessionModel(var timeStamp: DateTime, var sessionId: String, sessionType: Int,
                        var sessionProtocol: Int, var sessionStart: Int, var sessionStop: Int,
                        var omSource: Int, var sessionFileSize: String, var tenantId: String,
                        var vpcName: String, var serverName: String, var serverUser: String,
                        var serverIp: String, var serverPort: String, var instanceId: String,
                        var clientUser: String, var clientUname: String, var clientIp: String,
                        var authRuleNames: Array[String], var isPlayed: Int) {
  def this() {
    this(null, null, 0, 0, 0, 0, 0, null, null, null, null, null, null, null, null, null, null, null, null, 0)
  }

  override def equals(obj: Any): Boolean = {
    if (obj.isInstanceOf[SessionModel]) {
      val dealModel = obj.asInstanceOf[SessionModel]
      dealModel.canEquals(this) && super.equals(dealModel) &&
        dealModel.toString.equals(super.toString)
    }
    else false
  }
  def canEquals(obj: Any): Boolean = obj.isInstanceOf[SessionModel]

  override def hashCode: Int = toString.hashCode

}



