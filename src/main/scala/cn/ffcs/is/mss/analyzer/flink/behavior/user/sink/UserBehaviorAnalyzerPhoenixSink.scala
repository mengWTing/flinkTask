/*
 * @project mss
 * @company Fujian Fujitsu Communication Software Co., Ltd.
 * @author chenwei
 * @date 2019-12-04 15:36:57
 * @version v1.0
 * @update [no] [date YYYY-MM-DD] [name] [description]
 */
package cn.ffcs.is.mss.analyzer.flink.behavior.user.sink

import java.io.{BufferedReader, InputStreamReader}
import java.net.URI
import java.sql.{Connection, DriverManager, Timestamp}
import java.util.Properties

import cn.ffcs.is.mss.analyzer.flink.behavior.user.UserBehaviorPhoenixModel
import cn.ffcs.is.mss.analyzer.utils.{C3P0Util, Constants}
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.sink.{RichSinkFunction, SinkFunction}
import org.apache.hadoop.fs.{FileSystem, Path}
/**
 *
 * @author chenwei
 * @date 2019-12-04 15:36:57
 * @title UserBehaviorAnalyzerPhoenixSink
 * @update [no] [date YYYY-MM-DD] [name] [description]
 */
/**
 * 将用户行为分析结果写入hbase的sink
 */
class UserBehaviorAnalyzerPhoenixSink extends RichSinkFunction[UserBehaviorPhoenixModel] {

  var url = ""
  var upsertSql = ""

  var connection : Connection = _
  override def open(parameters: Configuration): Unit = {

    //获取全局配置
    val globConf = getRuntimeContext.getExecutionConfig.getGlobalJobParameters
      .asInstanceOf[Configuration]
    Class.forName(globConf.getString(Constants.USER_BEHAVIOR_ANALYZER_PHOENIX_DRIVER_CLASS,
      "org.apache.phoenix.jdbc.PhoenixDriver"))
    upsertSql = globConf.getString(Constants.USER_BEHAVIOR_ANALYZER_UPSERT_HBASE_SQL, "")
    url = globConf.getString(Constants.USER_BEHAVIOR_ANALYZER_PHOENIX_URL, "")

    ////根据c3p0配置文件,初始化c3p0连接池
    //val c3p0Properties = new Properties()
    ////val c3p0ConfigPath = "/project/flink/conf/c3p0-phoenix.properties"
    //val c3p0ConfigPath = "/Users/chenwei/IdeaProjects/mss/src/main/resources/c3p0-phoenix.properties"
    //val fs = FileSystem.get(URI.create(c3p0ConfigPath), new org.apache.hadoop.conf.Configuration())
    //val fsDataInputStream = fs.open(new Path(c3p0ConfigPath))
    //val bufferedReader = new BufferedReader(new InputStreamReader(fsDataInputStream))
    //c3p0Properties.load(bufferedReader)
    //C3P0Util.iniPhoenix(c3p0Properties)
  }

  override def invoke(value: UserBehaviorPhoenixModel, context: SinkFunction.Context): Unit = {
    if (connection == null || connection.isClosed){
      connection = getConnection(url)
    }

    val insertPrepareStatement = connection.prepareStatement(upsertSql)
    insertPrepareStatement.setString(1, value.userName)
    insertPrepareStatement.setTimestamp(2, new Timestamp(value.timeStamp))
    insertPrepareStatement.setString(3, value.sourceIp)
    insertPrepareStatement.setInt(4, value.sourcePort)
    insertPrepareStatement.setString(5, value.place)
    insertPrepareStatement.setString(6, value.destinationIp)
    insertPrepareStatement.setInt(7, value.destinationPort)
    insertPrepareStatement.setString(8, value.host)
    insertPrepareStatement.setString(9, value.system)
    insertPrepareStatement.setString(10, value.url)
    insertPrepareStatement.setString(11, value.httpStatus)
    insertPrepareStatement.setString(12, value.reference)
    insertPrepareStatement.setString(13, value.userAgent)
    insertPrepareStatement.setString(14, value.browser)
    insertPrepareStatement.setString(15, value.operationSystem)
    insertPrepareStatement.setString(16, value.isDownload)
    insertPrepareStatement.setString(17, value.isDownSuccess)
    insertPrepareStatement.setLong(18, value.downFileSize)
    insertPrepareStatement.setString(19, value.downFileName)
    insertPrepareStatement.setString(20, value.formValue)
    insertPrepareStatement.setLong(21, value.inputOctets)
    insertPrepareStatement.setLong(22, value.outputOctets)
    insertPrepareStatement.setDouble(23, value.timeProbability)
    insertPrepareStatement.setDouble(24, value.systemTimeProbability)
    insertPrepareStatement.setDouble(25, value.operationSystemTimeProbability)
    insertPrepareStatement.setDouble(26, value.browserTimeProbability)
    insertPrepareStatement.setDouble(27, value.sourceIpTimeProbability)
    insertPrepareStatement.setDouble(28, value.sourceIpProbability)
    insertPrepareStatement.setDouble(29, value.httpStatusProbability)
    insertPrepareStatement.setDouble(30, value.urlParamProbability)
    insertPrepareStatement.setDouble(31, value.systemCountProbability)
    insertPrepareStatement.setDouble(32, value.probability)
    insertPrepareStatement.setString(33, value.warnType)
    insertPrepareStatement.execute()
    connection.commit()
    connection.close()
  }

  def getConnection(url: String): Connection = {
    DriverManager.getConnection(url)
  }
}
