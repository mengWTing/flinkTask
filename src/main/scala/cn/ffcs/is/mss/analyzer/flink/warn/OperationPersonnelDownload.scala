package cn.ffcs.is.mss.analyzer.flink.warn

import java.io.{BufferedReader, InputStreamReader}
import java.net.URI
import java.sql.Timestamp
import java.util.Properties
import java.util.regex.Pattern
import cn.ffcs.is.mss.analyzer.bean.BbasOperationPersonnelDownloadEntity
import cn.ffcs.is.mss.analyzer.druid.model.scala.OperationModel
import cn.ffcs.is.mss.analyzer.flink.sink.MySQLSink
import cn.ffcs.is.mss.analyzer.flink.unknowRisk.funcation.UnknownRiskUtil.getInputKafkavalue
import cn.ffcs.is.mss.analyzer.utils.{Constants, IniProperties, JsonUtil}
import org.apache.flink.api.common.functions.RichMapFunction
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.ProcessFunction
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaConsumer, FlinkKafkaProducer}
import org.apache.flink.streaming.util.serialization.SimpleStringSchema
import org.apache.flink.util.Collector

import scala.collection.mutable

/**
 * @Auther chenwei
 * @Description 操作人员下载
 * @Date: Created in 2018/11/21 16:49
 * @Modified By
 */
object OperationPersonnelDownload {
  def main(args: Array[String]): Unit = {
    //根据传入的参数解析配置文件
    //val args0 = "/Users/chenwei/IdeaProjects/mss/src/main/resources/flink.ini"
    //val confProperties = new IniProperties(args0)
    val confProperties = new IniProperties(args(0))

    //该任务的名字
    val jobName = confProperties.getValue(Constants.FLINK_OPERATION_PERSONNEL_DOWNLOAD_CONFIG, Constants.OPERATION_PERSONNEL_DOWNLOAD_JOB_NAME)
    //kafka Source的名字
    val kafkaSourceName = confProperties.getValue(Constants.FLINK_OPERATION_PERSONNEL_DOWNLOAD_CONFIG, Constants.OPERATION_PERSONNEL_DOWNLOAD_KAFKA_SOURCE_NAME)
    //mysql sink的名字
    val sqlSinkName = confProperties.getValue(Constants.FLINK_OPERATION_PERSONNEL_DOWNLOAD_CONFIG, Constants.OPERATION_PERSONNEL_DOWNLOAD_SQL_SINK_NAME)
    //kafka sink的名字
    val kafkaSinkName = confProperties.getValue(Constants.FLINK_OPERATION_PERSONNEL_DOWNLOAD_CONFIG, Constants.OPERATION_PERSONNEL_DOWNLOAD_KAFKA_SINK_NAME)

    //kafka Source的并行度
    val kafkaSourceParallelism = confProperties.getIntValue(Constants.FLINK_OPERATION_PERSONNEL_DOWNLOAD_CONFIG, Constants.OPERATION_PERSONNEL_DOWNLOAD_KAFKA_SOURCE_PARALLELISM)
    //对数据处理的并行度
    val dealParallelism = confProperties.getIntValue(Constants.FLINK_OPERATION_PERSONNEL_DOWNLOAD_CONFIG, Constants.OPERATION_PERSONNEL_DOWNLOAD_DEAL_PARALLELISM)
    //写入mysql的并行度
    val sqlSinkParallelism = confProperties.getIntValue(Constants.FLINK_OPERATION_PERSONNEL_DOWNLOAD_CONFIG, Constants.OPERATION_PERSONNEL_DOWNLOAD_SQL_SINK_PARALLELISM)
    //写入kafka的并行度
    val kafkaSinkParallelism = confProperties.getIntValue(Constants.FLINK_OPERATION_PERSONNEL_DOWNLOAD_CONFIG, Constants.OPERATION_PERSONNEL_DOWNLOAD_KAFKA_SINK_PARALLELISM)

    //kafka的服务地址
    val brokerList = confProperties.getValue(Constants.FLINK_COMMON_CONFIG, Constants.KAFKA_BOOTSTRAP_SERVERS)
    //flink消费的group.id
    val groupId = confProperties.getValue(Constants.FLINK_OPERATION_PERSONNEL_DOWNLOAD_CONFIG, Constants.OPERATION_PERSONNEL_DOWNLOAD_GROUP_ID)
    //kafka的topic
    val kafkaSourceTopic = confProperties.getValue(Constants.OPERATION_FLINK_TO_DRUID_CONFIG, Constants.OPERATION_TOPIC)
    //kafka sink 的topic
    val kafkaSinkTopic = confProperties.getValue(Constants.FLINK_OPERATION_PERSONNEL_DOWNLOAD_CONFIG, Constants.OPERATION_PERSONNEL_DOWNLOAD_ALERT_KAFKA_SINK_TOPIC)
    val warningSinkTopic = confProperties.getValue(Constants.WARNING_FLINK_TO_DRUID_CONFIG, Constants
      .WARNING_TOPIC)

    //flink全局变量
    val parameters: Configuration = new Configuration()
    //日期配置文件的路径
    parameters.setString(Constants.DATE_CONFIG_PATH, confProperties.getValue(Constants.FLINK_COMMON_CONFIG, Constants.DATE_CONFIG_PATH))
    //c3p0连接池配置文件路径
    parameters.setString(Constants.c3p0_CONFIG_PATH, confProperties.getValue(Constants.FLINK_COMMON_CONFIG, Constants.c3p0_CONFIG_PATH))
    //运维人员名单列表
    parameters.setString(Constants.OPERATION_PERSONNEL_DOWNLOAD_OPERATION_PERSONNEL_PATH, confProperties.getValue(Constants.FLINK_OPERATION_PERSONNEL_DOWNLOAD_CONFIG, Constants.OPERATION_PERSONNEL_DOWNLOAD_OPERATION_PERSONNEL_PATH))

    //check pointing的间隔
    val checkpointInterval = confProperties.getLongValue(Constants.FLINK_OPERATION_PERSONNEL_DOWNLOAD_CONFIG, Constants.OPERATION_PERSONNEL_DOWNLOAD_CHECKPOINT_INTERVAL)


    //获取ExecutionEnvironment
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    //设置check pointing的间隔
    env.enableCheckpointing(checkpointInterval)
    //设置flink全局变量
    env.getConfig.setGlobalJobParameters(parameters)

    //设置kafka消费者相关配置
    val props = new Properties()
    //设置kafka集群地址
    props.setProperty("bootstrap.servers", brokerList)
    //设置flink消费的group.id
    props.setProperty("group.id", groupId)

    //获取kafka消费者
    val consumer = new FlinkKafkaConsumer[String](kafkaSourceTopic, new SimpleStringSchema, props).setStartFromGroupOffsets()
    //获取kafka生产者
    val producer = new FlinkKafkaProducer[String](brokerList, kafkaSinkTopic, new SimpleStringSchema())
    val warningProducer = new FlinkKafkaProducer[String](brokerList, warningSinkTopic, new
        SimpleStringSchema())

    // 获取kafka数据
    val dStream = env.addSource(consumer).setParallelism(kafkaSourceParallelism)
      .uid(kafkaSourceName).name(kafkaSourceName)

    //ip-地点关联文件路径
    val placePath = confProperties.getValue(Constants.OPERATION_FLINK_TO_DRUID_CONFIG, Constants.OPERATION_PLACE_PATH)
    //host-系统名关联文件路径
    val systemPath = confProperties.getValue(Constants.OPERATION_FLINK_TO_DRUID_CONFIG, Constants.OPERATION_SYSTEM_PATH)
    //用户名-常用登录地关联文件路径
    val usedPlacePath = confProperties.getValue(Constants.OPERATION_FLINK_TO_DRUID_CONFIG, Constants.OPERATION_USEDPLACE_PATH)

    val operationPersonnelDownloadStream = dStream
      .map(new RichMapFunction[String, (Option[OperationModel], String)] {
        override def open(parameters: Configuration): Unit = {
          OperationModel.setPlaceMap(placePath)
          OperationModel.setSystemMap(systemPath)
          OperationModel.setMajorMap(systemPath)
          OperationModel.setUsedPlacesMap(usedPlacePath)
        }

        override def map(value: String): (Option[OperationModel], String) = (OperationModel.getOperationModel(value), value)
      }).setParallelism(1)
      .filter(_._1.isDefined).setParallelism(dealParallelism)
      .map(t => (t._1.head, t._2)).setParallelism(dealParallelism)
      .process(new OperationPersonnelDownloadProcess).setParallelism(dealParallelism)

    val value: DataStream[(Object, Boolean)] = operationPersonnelDownloadStream.map(_._1)
    val alertKafkaValue = operationPersonnelDownloadStream.map(_._2)


    value.addSink(new MySQLSink).uid(sqlSinkName).name(sqlSinkName)
      .setParallelism(sqlSinkParallelism)

    value
      .map(o => {
        JsonUtil.toJson(o._1.asInstanceOf[BbasOperationPersonnelDownloadEntity])
      })
      .addSink(producer)
      .uid(kafkaSinkName)
      .name(kafkaSinkName)
      .setParallelism(kafkaSinkParallelism)
    alertKafkaValue.addSink(warningProducer).setParallelism(kafkaSinkParallelism)
    env.execute(jobName)

  }


  class OperationPersonnelDownloadProcess extends ProcessFunction[(OperationModel, String), ((Object, Boolean), String)] {

    var operationPersonnelMap: mutable.Map[String, (String, String, String, String)] = null


    override def open(parameters: Configuration): Unit = {

      val globConf = getRuntimeContext.getExecutionConfig.getGlobalJobParameters.asInstanceOf[Configuration]

      operationPersonnelMap = mutable.Map[String, (String, String, String, String)]()
      val personnelPath = globConf.getString(Constants.OPERATION_PERSONNEL_DOWNLOAD_OPERATION_PERSONNEL_PATH, "")

      val fileSystem = org.apache.hadoop.fs.FileSystem.get(URI.create(personnelPath), new org.apache.hadoop.conf.Configuration())


      val personnelFsDataInputStream = fileSystem.open(new org.apache.hadoop.fs.Path(personnelPath))
      val personnelBufferedReader = new BufferedReader(new InputStreamReader(personnelFsDataInputStream))

      var line = personnelBufferedReader.readLine()

      while (line != null) {
        val values = line.split(";", -1)
        if (values.length == 6) {
          operationPersonnelMap.put((values(2) + "@HQ").toUpperCase, (values(0), values(1), values(3), values(5)))
        }
        line = personnelBufferedReader.readLine()

      }

      personnelBufferedReader.close()
      personnelFsDataInputStream.close()


    }

    override def processElement(value: (OperationModel, String), ctx: ProcessFunction[(OperationModel, String),
      ((Object, Boolean), String)]#Context, out: Collector[((Object, Boolean), String)]): Unit = {

      val values = value._2.split("\\|", -1)
      val url = values(6)
      if (values.length == 31 && "1".equals(values(25)) && "1".equals(values(26)) && operationPersonnelMap.contains(values(0).toUpperCase)) {


        val operationPersonnelInfo = operationPersonnelMap(values(0).toUpperCase)
        val bbasOperationPersonnelDownloadEntity = new BbasOperationPersonnelDownloadEntity()
        bbasOperationPersonnelDownloadEntity.setWarnDatetime(new Timestamp(value._1.timeStamp))
        bbasOperationPersonnelDownloadEntity.setUsername(value._1.userName)
        bbasOperationPersonnelDownloadEntity.setCompanyName(operationPersonnelInfo._1)
        bbasOperationPersonnelDownloadEntity.setDepartmentName(operationPersonnelInfo._2)
        bbasOperationPersonnelDownloadEntity.setPersonalName(operationPersonnelInfo._3)
        bbasOperationPersonnelDownloadEntity.setJobName(operationPersonnelInfo._4)
        bbasOperationPersonnelDownloadEntity.setDownloadFimeName(values(28))
        bbasOperationPersonnelDownloadEntity.setDownloadSystem(value._1.loginSystem)
        bbasOperationPersonnelDownloadEntity.setSourceIp(value._1.sourceIp)
        bbasOperationPersonnelDownloadEntity.setDestinationIp(value._1.destinationIp)
        bbasOperationPersonnelDownloadEntity.setUrl(values(6))

        val outValue = getInputKafkavalue(value._1, url, "运维人员下载", "")

        out.collect((bbasOperationPersonnelDownloadEntity, true), outValue)

      }

    }

  }
}
