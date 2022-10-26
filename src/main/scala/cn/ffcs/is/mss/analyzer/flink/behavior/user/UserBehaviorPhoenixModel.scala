package cn.ffcs.is.mss.analyzer.flink.behavior.user

/**
 * @Auther chenwei
 *         CREATE TABLE IF NOT EXISTS USER_BEHAVIOR(
 *         USER_NAME VARCHAR NOT NULL,
 *         TIMESTAMP TIMESTAMP NOT NULL,
 *         SOURCE_IP VARCHAR,
 *         SOURCE_PORT INTEGER,
 *         PLACE VARCHAR,
 *         DESTINATION_IP VARCHAR,
 *         DESTINATION_PORT INTEGER,
 *         HOST VARCHAR,
 *         SYSTEM VARCHAR,
 *         URL VARCHAR,
 *         HTTP_STATUS VARCHAR,
 *         REFERENCE VARCHAR,
 *         USER_AGENT VARCHAR,
 *         BROWSER VARCHAR,
 *         OPERATION_SYSTEM VARCHAR,
 *         IS_DOWNLOAD VARCHAR,
 *         IS_DOWN_SUCCESS VARCHAR,
 *         DOWN_FILE_SIZE BIGINT,
 *         DOWN_FILE_NAME VARCHAR,
 *         FORM_VALUE VARCHAR,
 *         INPUT_OCTETS BIGINT,
 *         OUTPUT_OCTETS BIGINT,
 *         TIME_PROBABILITY DOUBLE,
 *         SYSTEM_TIME_PROBABILITY DOUBLE,
 *         OPERATION_SYSTEM_TIME_PROBABILITY DOUBLE,
 *         BROWSER_TIME_PROBABILITY DOUBLE,
 *         SOURCE_IP_TIME_PROBABILITY DOUBLE,
 *         SOURCE_IP_PROBABILITY DOUBLE,
 *         HTTP_STATUS_PROBABILITY DOUBLE,
 *         URL_PARAM_PROBABILITY DOUBLE,
 *         SYSTEM_COUNT_PROBABILITY DOUBLE,
 *         PROBABILITY DOUBLE,
 *         WARN_TYPE VARCHAR,
 *         CONSTRAINT PK PRIMARY KEY (USER_NAME, TIMESTAMP DESC));
 * @Description
 * @Date: Created in 2019/12/11 16:13
 * @Modified By
 */
final case class UserBehaviorPhoenixModel(userName: String, timeStamp: Long, sourceIp: String,
                                          sourcePort: Int, place: String, destinationIp: String,
                                          destinationPort: Int, host: String, system: String,
                                          url: String, httpStatus: String, reference: String,
                                          userAgent: String, browser:String, operationSystem:String,
                                          isDownload: String, isDownSuccess: String, downFileSize: Long,
                                          downFileName: String, formValue: String, inputOctets: Long,
                                          outputOctets: Long, timeProbability: Double,
                                          systemTimeProbability: Double, operationSystemTimeProbability: Double,
                                          browserTimeProbability: Double, sourceIpTimeProbability: Double,
                                          sourceIpProbability: Double, httpStatusProbability: Double,
                                          urlParamProbability: Double, systemCountProbability: Double,
                                          probability: Double, warnType:String) {

  def this() {


    this(null, 0L, null, 0, null, null, 0, null, null, null, null, null, null, null, null,
      null, null, 0L, null, null, 0L, 0L, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, null)
  }


}