/*
 * @project mss
 * @company Fujian Fujitsu Communication Software Co., Ltd.
 * @author chenwei
 * @date 2019-01-22 14:43:59
 * @version v1.0
 * @update [no] [date YYYY-MM-DD] [name] [description]
 */
package cn.ffcs.is.mss.analyzer.flink.warn

import cn.ffcs.is.mss.analyzer.bean.BbasDownloadCountWarnImproveEntity
import cn.ffcs.is.mss.analyzer.flink.sink.MySQLSink
import cn.ffcs.is.mss.analyzer.ml.iforest.IForest
import cn.ffcs.is.mss.analyzer.utils._
import cn.ffcs.is.mss.analyzer.utils.druid.entity._
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.ProcessFunction
import org.apache.flink.streaming.api.functions.source.{RichParallelSourceFunction, SourceFunction}
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer
import org.apache.flink.streaming.util.serialization.SimpleStringSchema
import org.apache.flink.util.Collector
import org.apache.hadoop.fs.{FileSystem, Path}
import org.json.JSONArray

import java.io.{BufferedReader, InputStreamReader}
import java.net.URI
import java.sql.Timestamp
import java.util.Properties
import scala.collection.JavaConversions._
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

/**
 *
 * @author chenwei
 * @date 2019-01-22 14:43:59
 * @title DownloadCount 下载次数
 * @update [no] [date YYYY-MM-DD] [name] [description]
 */
object DownloadCount {

  def main(args: Array[String]): Unit = {

    //根据传入的参数解析配置文件
    //val args0 = "/Users/chenwei/IdeaProjects/mss/src/main/resources/flink.ini"
    //val confProperties = new IniProperties(args0)
    val confProperties = new IniProperties(args(0))


    val timeStamp = TimeUtil.getDayStartTime(System.currentTimeMillis())

    //该任务的名字
    val jobName = confProperties.getValue(Constants.FLINK_DOWNLOAD_COUNT_CONFIG, Constants.DOWNLOAD_COUNT_JOB_NAME)
    //druid Source的名字
    val druidSourceName = confProperties.getValue(Constants.FLINK_DOWNLOAD_COUNT_CONFIG, Constants.DOWNLOAD_COUNT_DRUID_SOURCE_NAME)
    //mysql sink的名字
    val sqlSinkName = confProperties.getValue(Constants.FLINK_DOWNLOAD_COUNT_CONFIG, Constants.DOWNLOAD_COUNT_SQL_SINK_NAME)
    //kafka sink的名字
    val kafkaSinkName = confProperties.getValue(Constants.FLINK_DOWNLOAD_COUNT_CONFIG, Constants.DOWNLOAD_COUNT_KAFKA_SINK_NAME)

    //druid Source的并行度
    val druidSourceParallelism = confProperties.getIntValue(Constants.FLINK_DOWNLOAD_COUNT_CONFIG, Constants.DOWNLOAD_COUNT_DRUID_SOURCE_PARALLELISM)
    //对数据处理的并行度
    val dealParallelism = confProperties.getIntValue(Constants.FLINK_DOWNLOAD_COUNT_CONFIG, Constants.DOWNLOAD_COUNT_DEAL_PARALLELISM)
    //写入mysql的并行度
    val sqlSinkParallelism = confProperties.getIntValue(Constants.FLINK_DOWNLOAD_COUNT_CONFIG, Constants.DOWNLOAD_COUNT_SQL_SINK_PARALLELISM)
    //写入kafka的并行度
    val kafkaSinkParallelism = confProperties.getIntValue(Constants.FLINK_DOWNLOAD_COUNT_CONFIG, Constants.DOWNLOAD_COUNT_KAFKA_SINK_PARALLELISM)

    //kafka的服务地址
    val brokerList = confProperties.getValue(Constants.FLINK_COMMON_CONFIG, Constants.KAFKA_BOOTSTRAP_SERVERS)
    //flink消费的group.id
    val groupId = confProperties.getValue(Constants.FLINK_OPERATION_PERSONNEL_DOWNLOAD_CONFIG, Constants.OPERATION_PERSONNEL_DOWNLOAD_GROUP_ID)
    //kafka的topic
    val kafkaSourceTopic = confProperties.getValue(Constants.OPERATION_FLINK_TO_DRUID_CONFIG, Constants.OPERATION_TOPIC)
    //kafka sink 的topic
    val kafkaSinkTopic = confProperties.getValue(Constants.FLINK_DOWNLOAD_COUNT_CONFIG, Constants.DOWNLOAD_COUNT_ALERT_KAFKA_SINK_TOPIC)

    //flink全局变量
    val parameters: Configuration = new Configuration()
    //配置文件系统类型
    parameters.setString(Constants.FILE_SYSTEM_TYPE, confProperties.getValue(Constants.FLINK_COMMON_CONFIG, Constants.FILE_SYSTEM_TYPE))
    //日期配置文件的路径
    parameters.setString(Constants.DATE_CONFIG_PATH, confProperties.getValue(Constants.FLINK_COMMON_CONFIG, Constants.DATE_CONFIG_PATH))
    //c3p0连接池配置文件路径
    parameters.setString(Constants.c3p0_CONFIG_PATH, confProperties.getValue(Constants.FLINK_COMMON_CONFIG, Constants.c3p0_CONFIG_PATH))
    //druid的broker节点
    parameters.setString(Constants.DRUID_BROKER_HOST_PORT, confProperties.getValue(Constants.FLINK_COMMON_CONFIG, Constants.DRUID_BROKER_HOST_PORT))
    //druid时间格式
    parameters.setString(Constants.DRUID_TIME_FORMAT, confProperties.getValue(Constants.FLINK_COMMON_CONFIG, Constants.DRUID_TIME_FORMAT))
    //druid数据开始的时间
    parameters.setLong(Constants.DRUID_DATA_START_TIMESTAMP, confProperties.getLongValue(Constants.FLINK_COMMON_CONFIG, Constants.DRUID_DATA_START_TIMESTAMP))
    //五元组话单在druid的表名
    parameters.setString(Constants.DRUID_OPERATION_TABLE_NAME, confProperties.getValue(Constants.FLINK_COMMON_CONFIG, Constants.DRUID_OPERATION_TABLE_NAME))

    //获取kafka生产者
    val producer = new FlinkKafkaProducer[String](brokerList, kafkaSinkTopic, new SimpleStringSchema())

    //获取ExecutionEnvironment
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    //设置flink全局变量
    env.getConfig.setGlobalJobParameters(parameters)

    //设置kafka消费者相关配置
    val props = new Properties()
    //设置kafka集群地址
    props.setProperty("bootstrap.servers", brokerList)
    //设置flink消费的group.id
    props.setProperty("group.id", groupId)


    // 获取kafka数据
    val downloadCountStream = env.addSource(new QueryDruidSource(timeStamp)).setParallelism(druidSourceParallelism)
      .uid(druidSourceName).name(druidSourceName)
      .keyBy(_._1)
      .reduce((t1, t2) => {
        if (t1._3 >= t2._3) {
          (t1._1, t1._2.++(t2._2), t1._3)
        } else {
          (t1._1, t1._2.++(t2._2), t2._3)
        }
      }).setParallelism(dealParallelism)
      //      .print()

      .process(new DownloadCountProcess(timeStamp)).setParallelism(dealParallelism)

    //将告警写入数据库
    downloadCountStream.addSink(new MySQLSink).setParallelism(sqlSinkParallelism).uid(sqlSinkName).name(sqlSinkName)

    //将告警发送至安管平台
    downloadCountStream
      .map(o => {
        JsonUtil.toJson(o._1.asInstanceOf[BbasDownloadCountWarnImproveEntity])
      })
      .addSink(producer)
      .uid(kafkaSinkName)
      .name(kafkaSinkName)
      .setParallelism(kafkaSinkParallelism)

    env.execute(jobName)

  }

  /**
   *
   *
   * @return
   * @author PlatinaBoy
   * @date 2021/7/23 16:43
   * @description 自定义Druidsource
   * @update [no][date YYYY-MM-DD][name][description]
   */

  class QueryDruidSource(timeStamp: Long) extends RichParallelSourceFunction[(String, mutable.HashSet[String], Double)] {

    var druidStartTime = 0L
    var tableName = ""
    val fileNameSet = new mutable.HashSet[String]


    override def open(parameters: Configuration): Unit = {
      val globConf = getRuntimeContext.getExecutionConfig.getGlobalJobParameters.asInstanceOf[Configuration]

      DateUtil.ini(globConf.getString(Constants.FILE_SYSTEM_TYPE, ""), globConf.getString(Constants.DATE_CONFIG_PATH, ""))
      DruidUtil.setDruidHostPortSet(globConf.getString(Constants.DRUID_BROKER_HOST_PORT, ""))
      DruidUtil.setTimeFormat(globConf.getString(Constants.DRUID_TIME_FORMAT, ""))
      DruidUtil.setDateStartTimeStamp(globConf.getLong(Constants.DRUID_DATA_START_TIMESTAMP, 0L))


      tableName = globConf.getString(Constants.DRUID_OPERATION_TABLE_NAME, "")
    }


    override def run(ctx: SourceFunction.SourceContext[(String, mutable.HashSet[String], Double)]): Unit = {
      val dayQueryResultList = DruidUtil.query(getDayQueryEntity(timeStamp - TimeUtil
        .DAY_MILLISECOND, timeStamp, tableName))
      if (dayQueryResultList != null) {

        for (dayMap <- dayQueryResultList) {
          //获取用户名
          val userName = dayMap.get(Dimension.userName.toString)
          //获取系统名
          val loginSystem = dayMap.get(Dimension.loginSystem.toString)
          //获取下载文件名
          val downFileName = dayMap.get(Dimension.downFileName.toString)
          fileNameSet.add(downFileName)
          //获取当天下载次数
          val dayConnCount = dayMap.get(Aggregation.connCount.toString).toDouble
          if (!"匿名用户".equals(userName) && !"未知系统".equals(loginSystem) && downFileName.nonEmpty && !"null".equals(downFileName)) {
            ctx.collect((userName + "|" + loginSystem, fileNameSet, dayConnCount))
          }
        }
      }
    }

    override def cancel(): Unit = {

    }

    /**
     *
     *
     * @return
     * @author PlatinaBoy
     * @date 2021/7/23 11:44
     * @description 获取druid 查询entity
     * @update [no][date YYYY-MM-DD][name][description]
     */
    def getDayQueryEntity(startTime: Long, endTime: Long, tableName: String): Entity = {

      val entity = new Entity()
      entity.setAggregationsSet(Aggregation.getAggregationsSet(Aggregation.connCount))
      entity.setDimensionsSet(Dimension.getDimensionsSet(Dimension.userName, Dimension.loginSystem, Dimension.downFileName))
      entity.setStartTimeStr(startTime)
      entity.setEndTimeStr(endTime)

      val jsonArray = new JSONArray()
      jsonArray.put(Filter.getFilter(Filter.selector, Dimension.isDownload, "1"))
      jsonArray.put(Filter.getFilter(Filter.selector, Dimension.isDownSuccess, "1"))
      entity.setFilter(Filter.getFilter(Filter.and, jsonArray))
      entity.setGranularity(Granularity.getGranularity(Granularity.periodDay, 1))
      entity.setTableName(tableName)

      entity
    }
  }

  /**
   *
   *
   * @return
   * @author PlatinaBoy
   * @date 2021/7/23 11:44
   * @description 下载异常检测业务逻辑
   * @update [no][date YYYY-MM-DD][name][description]
   */
  class DownloadCountProcess(timeStamp: Long) extends ProcessFunction[(String, mutable.HashSet[String], Double), (Object,
    Boolean)] {

    var sqlHelper: SQLHelper = null
    var druidStartTime = 0L
    var tableName = ""
    var startTimeStr: Array[String] = null
    var endTimeStr: Array[String] = null

    override def open(parameters: Configuration): Unit = {
      //获取全局变量
      val globConf = getRuntimeContext.getExecutionConfig.getGlobalJobParameters
        .asInstanceOf[Configuration]

      DateUtil.ini(globConf.getString(Constants.FILE_SYSTEM_TYPE, ""), globConf.getString(Constants.DATE_CONFIG_PATH, ""))
      DruidUtil.setDruidHostPortSet(globConf.getString(Constants.DRUID_BROKER_HOST_PORT, ""))
      DruidUtil.setTimeFormat(globConf.getString(Constants.DRUID_TIME_FORMAT, ""))
      DruidUtil.setDateStartTimeStamp(globConf.getLong(Constants.DRUID_DATA_START_TIMESTAMP, 0L))
      druidStartTime = globConf.getLong(Constants.DRUID_DATA_START_TIMESTAMP, 0L)

      //根据c3p0配置文件,初始化c3p0连接池
      val c3p0Properties = new Properties()
      val c3p0ConfigPath = globConf.getString(Constants.c3p0_CONFIG_PATH, "")
      val fs = FileSystem.get(URI.create(c3p0ConfigPath), new org.apache.hadoop.conf
      .Configuration())
      val fsDataInputStream = fs.open(new Path(c3p0ConfigPath))
      val bufferedReader = new BufferedReader(new InputStreamReader(fsDataInputStream))
      c3p0Properties.load(bufferedReader)
      C3P0Util.ini(c3p0Properties)


      //操作数据库的类
      sqlHelper = new SQLHelper()
      tableName = globConf.getString(Constants.DRUID_OPERATION_TABLE_NAME, "")
      //根据工作日休息日，获取历史查询的开始和结束时间戳
      val timeStr = getTimeStr(timeStamp, druidStartTime)
      startTimeStr = timeStr._1
      endTimeStr = timeStr._2
    }

    override def processElement(value: (String, mutable.HashSet[String], Double), ctx: ProcessFunction[(String,
      mutable.HashSet[String], Double), (Object,
      Boolean)]#Context, out: Collector[(Object, Boolean)]): Unit = {
      val valueArr = value._1.split("\\|", -1)
      //获取用户名
      val userName = valueArr(0)
      //获取系统名
      val loginSystem = valueArr(1)
      //获取当天下载次数
      val dayConnCount = value._3

      //查找历史下载情况
      val historyQueryResultList = DruidUtil.query(getHistoryQueryEntity(startTimeStr,
        endTimeStr, userName, loginSystem, tableName))

      //获取所有记录
      val arrayBuffer = mutable.ArrayBuffer[Double]()
      val samples = mutable.ArrayBuffer[Array[Double]]()
      for (historyMap <- historyQueryResultList) {
        val historyConnCount = historyMap.get(Aggregation.connCount.toString).toDouble
        samples.append(Array[Double](historyConnCount))
        arrayBuffer.append(historyConnCount)
      }

      val iForest = new IForest()
      val result = iForest.predict(samples.toArray, 5000, 128, 10, Array[Double](dayConnCount),
        true, false)
      if (!result && value._2.size > 1) {
        val bbasDownloadCountWarnEntity = new BbasDownloadCountWarnImproveEntity()
        bbasDownloadCountWarnEntity.setCompanyName("")
        bbasDownloadCountWarnEntity.setDepartmentName("")
        bbasDownloadCountWarnEntity.setDownloadCount(dayConnCount.toInt)
        bbasDownloadCountWarnEntity.setDownloadSystem(loginSystem)
        bbasDownloadCountWarnEntity.setJobName("")
        bbasDownloadCountWarnEntity.setPersonalName("")
        bbasDownloadCountWarnEntity.setUsername(userName)
        bbasDownloadCountWarnEntity.setDownloadFileName(value._2.mkString("|"))
        bbasDownloadCountWarnEntity.setWarnDatetime(new Timestamp(timeStamp - TimeUtil.DAY_MILLISECOND))
        out.collect((bbasDownloadCountWarnEntity, true))
      }


    }

    override def close(): Unit = {

    }

    /**
     *
     * @param startTime
     * @param endTime
     * @param userName
     * @return
     */
    def getHistoryQueryEntity(startTime: Array[String], endTime: Array[String], userName: String,
                              loginSystem: String, tableName: String)
    : Entity = {
      val entity = new Entity()
      entity.setAggregationsSet(Aggregation.getAggregationsSet(Aggregation.connCount))
      entity.setDimensionsSet(Dimension.getDimensionsSet(Dimension.userName))
      entity.setStartTimeStr(startTime)
      entity.setEndTimeStr(endTime)

      val jsonArray = new JSONArray()
      jsonArray.put(Filter.getFilter(Filter.selector, Dimension.isDownload, "1"))
      jsonArray.put(Filter.getFilter(Filter.selector, Dimension.isDownSuccess, "1"))
      jsonArray.put(Filter.getFilter(Filter.selector, Dimension.userName, userName))
      jsonArray.put(Filter.getFilter(Filter.selector, Dimension.loginSystem, loginSystem))
      entity.setFilter(Filter.getFilter(Filter.and, jsonArray))
      entity.setGranularity(Granularity.getGranularity(Granularity.periodDay, 1))
      entity.setTableName(tableName)

      entity
    }

    /**
     *
     * @param startTime
     * @param druidStartTime
     * @return
     */
    def getTimeStr(startTime: Long, druidStartTime: Long): (Array[String], Array[String]) = {


      val startArrayBuffer = ArrayBuffer[String]()
      val endArrayBuffer = ArrayBuffer[String]()

      val flag = if (DateUtil.getFlag(startTime) > 0) 1 else 0

      var time = startTime - TimeUtil.DAY_MILLISECOND

      while (time > druidStartTime) {

        if ((if (DateUtil.getFlag(time) > 0) 1 else 0) == flag) {
          startArrayBuffer += Entity.getQueryTimeStr(time)
          endArrayBuffer += Entity.getQueryTimeStr(time + TimeUtil.DAY_MILLISECOND)
        }

        time -= TimeUtil.DAY_MILLISECOND
      }

      (startArrayBuffer.toArray, endArrayBuffer.toArray)
    }
  }

}
