package cn.ffcs.is.mss.analyzer.utils

import cn.ffcs.is.mss.analyzer.druid.model.scala.OperationModel

/**
 * @ClassName:
 * @Author: mengwenting
 * @Date: 2023/2/7 16:39
 * @Description:
 * @update:
 */
object GetInputKafkaValue {
  def getInputKafkaValue(operation: OperationModel, url: String, alertName: String, packageValue: String): String = {
    var inPutKafkaValue = ""
    var returnValue = ""
    if (packageValue != null && packageValue.length > 200) {
      returnValue = packageValue.substring(0, 200)
    } else {
      returnValue = packageValue
    }

    if (operation != null) {
      if (operation.usedPlace.split("\\|", -1).length > 1) {
        inPutKafkaValue = operation.userName + "|" + alertName + "|" + operation.timeStamp + "|" +
          operation.loginMajor + "|" + operation.loginSystem + "|" + operation.usedPlace.replaceAll("\\|", "^") + "|" +
          operation.isRemote + "|" + operation.sourceIp + "|" + operation.sourcePort + "|" +
          operation.destinationIp + "|" + operation.destinationPort + "|" + url + "|" +
          operation.httpStatus + "|" + operation.packageType + "|" + returnValue
      } else {
        inPutKafkaValue = operation.userName + "|" + alertName + "|" + operation.timeStamp + "|" +
          operation.loginMajor + "|" + operation.loginSystem + "|" + operation.usedPlace + "|" +
          operation.isRemote + "|" + operation.sourceIp + "|" + operation.sourcePort + "|" +
          operation.destinationIp + "|" + operation.destinationPort + "|" + url + "|" +
          operation.httpStatus + "|" + operation.packageType + "|" + returnValue
      }
      inPutKafkaValue
    } else {
      ""
    }

  }
}
