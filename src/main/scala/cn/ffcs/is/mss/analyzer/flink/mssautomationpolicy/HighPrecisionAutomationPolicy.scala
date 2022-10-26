package cn.ffcs.is.mss.analyzer.flink.mssautomationpolicy

import cn.ffcs.is.mss.analyzer.bean.MssAutomationPolicyEntity
import cn.ffcs.is.mss.analyzer.druid.model.scala.AlertModel
import cn.ffcs.is.mss.analyzer.utils._
import org.apache.flink.api.common.accumulators.LongCounter
import org.apache.flink.api.common.functions.RichFilterFunction
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.functions.ProcessFunction
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction
import org.apache.flink.streaming.api.scala.{StreamExecutionEnvironment, _}
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer
import org.apache.flink.streaming.util.serialization.SimpleStringSchema
import org.apache.flink.util.Collector
import org.apache.hadoop.fs.{FileSystem, Path}

import java.io.{BufferedReader, InputStreamReader}
import java.net.URI
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.Properties
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

/**
 * @ClassName HightPrecisionPluggingPolicy
 * @author hanyu
 * @date 2022/1/17 15:37
 * @description M域自动封堵数据提供
 * @update [no][date YYYY-MM-DD][name][description]
 * */
object HighPrecisionAutomationPolicy {
  def main(args: Array[String]): Unit = {
    val confProperties = new IniProperties(args(0))
    //    val args0 = "G:\\ffcs_flink\\mss\\src\\main\\resources\\flink.ini"
    //    val confProperties = new IniProperties(args0)
    //该任务的名字
    val jobName = confProperties.getValue(Constants.M_AUTOMATION_POLICY_CONFIG, Constants.M_AUTOMATION_POLICY_JOB_NAME)

    val kafkaSourceParallelism = confProperties.getIntValue(Constants.M_AUTOMATION_POLICY_CONFIG, Constants
      .M_AUTOMATION_POLICY_KAFKA_SOURCE_PARALLELISM)
    val sqlSinkParallelism = confProperties.getIntValue(Constants.M_AUTOMATION_POLICY_CONFIG, Constants
      .M_AUTOMATION_POLICY_KAFKA_SINK_PARALLELISM)
    val dealParallelism = confProperties.getIntValue(Constants.M_AUTOMATION_POLICY_CONFIG, Constants
      .M_AUTOMATION_POLICY_DEAL_PARALLELISM)

    //kafka的服务地址
    val brokerList = confProperties.getValue(Constants.FLINK_COMMON_CONFIG, Constants.KAFKA_BOOTSTRAP_SERVERS)
    //flink消费的group.id
    val groupId = confProperties.getValue(Constants.M_AUTOMATION_POLICY_CONFIG, Constants.M_AUTOMATION_POLICY_GROUP_ID)
    //kafka的topic
    val sourceTopic = confProperties.getValue(Constants.FLINK_ALERT_STATISTICS_CONFIG, Constants.ALERT_STATISTICS_KAFKA_TOPIC)

    //check pointing的间隔
    val checkpointInterval = confProperties.getLongValue(Constants.M_AUTOMATION_POLICY_CONFIG, Constants
      .M_AUTOMATION_POLICY_CHECKPOINT_INTERVAL)
    val falseAlertName = confProperties.getValue(Constants.M_AUTOMATION_POLICY_CONFIG, Constants.M_AUTOMATION_POLICY_FALSE_ALERT_NAME)
    val parameters: Configuration = new Configuration()
    //c3p0连接池配置文件路径
    parameters.setString(Constants.M_AUTOMATION_POLICY_C3P0, confProperties.getValue(Constants
      .M_AUTOMATION_POLICY_CONFIG, Constants.M_AUTOMATION_POLICY_C3P0))
    parameters.setString(Constants.M_AUTOMATION_POLICY_TRUE_ALERT_NAME, confProperties.getValue(Constants.
      M_AUTOMATION_POLICY_CONFIG, Constants.M_AUTOMATION_POLICY_TRUE_ALERT_NAME))
    parameters.setString(Constants.M_AUTOMATION_POLICY_SYSTEM_KEY, confProperties.getValue(Constants.
      M_AUTOMATION_POLICY_CONFIG, Constants.M_AUTOMATION_POLICY_SYSTEM_KEY))
    parameters.setString(Constants.FILE_SYSTEM_TYPE, confProperties.getValue(Constants
      .FLINK_COMMON_CONFIG, Constants.FILE_SYSTEM_TYPE))
    //设置kafka消费者相关配置
    val props = new Properties()
    //设置kafka集群地址
    props.setProperty("bootstrap.servers", brokerList)
    //设置flink消费的group.id
    props.setProperty("group.id", groupId)

    //获取kafka消费者
    //    val consumer = new FlinkKafkaConsumer[String](sourceTopic, new SimpleStringSchema, props).setStartFromGroupOffsets()
    val consumer = new FlinkKafkaConsumer[String](sourceTopic, new SimpleStringSchema, props)
      .setStartFromLatest()
    //获取ExecutionEnvironment
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    //设置check pointing的间隔
    //env.enableCheckpointing(checkpointInterval)
    //设置流的时间为EventTime
    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)
    //设置flink全局变量
    env.getConfig.setGlobalJobParameters(parameters)


    //获取kafka数据
    val dStream = env.addSource(consumer).setParallelism(kafkaSourceParallelism)
    //    val dStream = env.socketTextStream("192.168.1.22", 9980)
    val value = dStream
      .map(line => {
        try {
          Some(JsonUtil.fromJson[AlertModel](line))
        } catch {
          case e: Exception => None
        }
      })
      .filter(_.isDefined)
      .map(_.head)
      .filter(_.alertName != falseAlertName)
      .filter(new RichFilterFunction[AlertModel] {
        override def filter(value: AlertModel): Boolean = {
          val username = value.alertUsername
          username.contains("@")
        }
      })
      .process(new AutomationPolicyProcessFunction()).setParallelism(dealParallelism)
    value.addSink(new RiskControlSQLSink()).setParallelism(sqlSinkParallelism)
    env.execute(jobName)
  }


  class AutomationPolicyProcessFunction() extends ProcessFunction[AlertModel, (Object, Boolean)] {

    var systemKeyMap =  new mutable.HashMap[String, String]
    var alertDocMap = new mutable.HashMap[String, String]
    var alertNameAb = new ArrayBuffer[String]()


    override def open(parameters: Configuration): Unit = {
      val globConf = getRuntimeContext.getExecutionConfig.getGlobalJobParameters.asInstanceOf[Configuration]
      val trueAlertName = globConf.getString(Constants.M_AUTOMATION_POLICY_TRUE_ALERT_NAME, "")
      val systemKey = globConf.getString(Constants.M_AUTOMATION_POLICY_SYSTEM_KEY, "")
      if (trueAlertName.nonEmpty) {
        for (i <- trueAlertName.split("\\|", -1)) {
          alertNameAb.append(i)
        }
      }
      if (systemKey.nonEmpty) {
        for (i <- systemKey.split("\\^", -1)) {
          val system = i.split("\\|", -1)
          if (system.length == 2) {
            systemKeyMap.put(system(0), system(1))
          }
        }
      }
    }

    override def processElement(value: AlertModel, ctx: ProcessFunction[AlertModel, (Object, Boolean)]#Context,
                                out: Collector[(Object, Boolean)]): Unit = {
      val alertNameValue = value.alertName
      val alertDoc = value.alertDescription
      if (alertDoc.nonEmpty && alertDoc != null) {
        val docArr = alertDoc.split(";", -1)
        for (i <- docArr) {
          val doc = i.split("=", -1)
          if (doc.length == 2) {
            alertDocMap.put(doc(0), doc(1))
          }
        }
      }
      if (alertNameAb.contains(alertNameValue) && alertDocMap.nonEmpty) {
        val entity = new MssAutomationPolicyEntity()
        entity.setUserName(value.alertUsername)
        entity.setMetaIp(value.alertSrcIp)
        entity.setDestinationIp(value.alertDestIp)
        entity.setAlarmDetail(alertDoc)

        if (alertDoc.contains("WARN_DATETIME") && alertDoc.contains("LOGIN_SYSTEM")) {
          try {
            val systemValue = systemKeyMap(alertDocMap("LOGIN_SYSTEM"))
            if (systemValue.nonEmpty && systemValue != "None") {
              if (systemValue.contains("\\.")) {
                entity.setDomainName(systemValue)
              } else {
                entity.setBusinessSystem(systemValue)
              }
            }
            entity.setAlarmTime(new Timestamp(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(alertDocMap("WARN_DATETIME")).getTime))
            out.collect(entity.asInstanceOf[Object], false)
          } catch {
            case e: Exception => None
          }
        }
      }


    }
  }

  class RiskControlSQLSink() extends RichSinkFunction[(Object, Boolean)] {
    var sqlHelper: SQLHelper = _
    val messagesReceived = new LongCounter()
    val messagesExecuteSucceed = new LongCounter()
    val messagesExecuteFail = new LongCounter()

    override def open(parameters: Configuration): Unit = {
      //获取全局变量
      val globConf = getRuntimeContext.getExecutionConfig.getGlobalJobParameters
        .asInstanceOf[Configuration]

      //根据c3p0配置文件,初始化c3p0连接池
      val c3p0Properties = new Properties()
      val c3p0ConfigPath = globConf.getString(Constants.M_AUTOMATION_POLICY_C3P0, "")
      val fileSystemType = globConf.getString(Constants.FILE_SYSTEM_TYPE, "file:///")

      val fs = FileSystem.get(URI.create(fileSystemType), new org.apache.hadoop.conf.Configuration())
      val fsDataInputStream = fs.open(new Path(c3p0ConfigPath))
      val bufferedReader = new BufferedReader(new InputStreamReader(fsDataInputStream))
      c3p0Properties.load(bufferedReader)
      C3P0Util.ini(c3p0Properties)

      //操作数据库的类
      sqlHelper = new SQLHelper()

      //增加Accumulator 记录收到的数据个数、执行成功个数、执行失败的个数
      getRuntimeContext.addAccumulator("MySQLSink: Messages received", messagesReceived)
      getRuntimeContext.addAccumulator("MySQLSink: Messages execute succeed", messagesExecuteSucceed)
      getRuntimeContext.addAccumulator("MySQLSink: Messages execute fail", messagesExecuteFail)
    }

    override def invoke(value: (Object, Boolean)): Unit = {
      //记录收到的数据数
      messagesReceived.add(1)
      //记录是否更新成功
      var isSucceed = false
      //如果数据库中已经包含该条数据，就更新，否则插入
      val queryResult = sqlHelper.query(value._1, value._2)
      if (queryResult != null && queryResult.size() > 0) {
        isSucceed = sqlHelper.update(value._1)
      } else {
        isSucceed = sqlHelper.insert(value._1)
      }
      if (isSucceed) {
        messagesExecuteSucceed.add(1)
      } else {
        messagesExecuteFail.add(1)
      }
    }

    override def close(): Unit = {
      sqlHelper = null
    }


  }
}