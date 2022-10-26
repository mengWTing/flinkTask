package cn.ffcs.is.mss.analyzer.flink.behavior.user

import cn.ffcs.is.mss.analyzer.druid.model.scala.OperationModel
import cn.ffcs.is.mss.analyzer.flink.behavior.user.process.CalculationProbabilityUtil

/**
 * @Auther chenwei
 * @Description
 * @Date: Created in 2019/12/15 23:25
 * @Modified By
 */
final case class UserBehaviorModel(username:String, timeStamp: Long, sourceIp: String,
                                   browser: String, operationSystem: String, system: String,
                                   url: String) {
  override def toString: String = {
    username + "|" + timeStamp + "|" + sourceIp + "|" + browser + "|" + operationSystem +
      "|" + system + "|" + url
  }
}

object UserBehaviorModel {

  /**
   * 原始数据转UserBehaviorModel
   * @param line
   * @return
   */
  def rawDataToUserBehaviorModel(line: String): Option[UserBehaviorModel] = {
    try {
      val operationModelOption = OperationModel.getOperationModel(line)
      if (operationModelOption.isDefined) {
        val operationModel = operationModelOption.head
        val username = CalculationProbabilityUtil.getUsername(line, operationModel)
        val timeStamp = CalculationProbabilityUtil.getTimeStamp(line, operationModel)
        val sourceIp = CalculationProbabilityUtil.getSourceIp(line, operationModel)
        val browser = CalculationProbabilityUtil.getBrowser(line, operationModel)
        val operationSystem = CalculationProbabilityUtil.getOperationSystem(line, operationModel)
        val system = CalculationProbabilityUtil.getSystem(line, operationModel)
        val url = CalculationProbabilityUtil.getUrl(line, operationModel)
        val userBehaviorModel = UserBehaviorModel(username, timeStamp, sourceIp, browser,
          operationSystem, system, url)
        if (!"未知系统".equals(system)) {
          return Some(userBehaviorModel)
        }
      }
      None
    } catch {
      case e: Exception => None
    }

  }

  /**
   * 处理之后的临时数据转UserBehaviorModel
   * @param line
   * @return
   */
  def tmpDataToUserBehaviorModel(line: String): Option[UserBehaviorModel] = {

    val values = line.split("\\|", -1)
    try Some(UserBehaviorModel(values(0), values(1).toLong, values(2), values(3), values(4),
      values(5), values(6)))
    catch {
      case e: Exception => {
        None
      }
    }

  }
}