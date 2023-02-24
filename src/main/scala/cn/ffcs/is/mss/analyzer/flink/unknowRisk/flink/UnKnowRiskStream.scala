package cn.ffcs.is.mss.analyzer.flink.unknowRisk.flink

import cn.ffcs.is.mss.analyzer.bean.UnKnowRiskEntity
import cn.ffcs.is.mss.analyzer.utils.GetInputKafkaValue.getInputKafkaValue
import cn.ffcs.is.mss.analyzer.druid.model.scala.OperationModel
import cn.ffcs.is.mss.analyzer.flink.sink.MySQLSink
import cn.ffcs.is.mss.analyzer.flink.unknowRisk.funcation.IndexOfSimilarityUtile
import cn.ffcs.is.mss.analyzer.flink.unknowRisk.funcation.UnknownRiskUtil.{getUrlParameterMap, getUrlParameterTup}
import cn.ffcs.is.mss.analyzer.utils.druid.entity._
import cn.ffcs.is.mss.analyzer.utils.{Constants, IniProperties, JedisUtil, TimeUtil, _}
import org.apache.flink.api.common.accumulators.LongCounter
import org.apache.flink.api.common.functions.{RichFilterFunction, RichMapFunction}
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.functions.ProcessFunction
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaConsumer, FlinkKafkaProducer}
import org.apache.flink.streaming.util.serialization.SimpleStringSchema
import org.apache.flink.util.Collector
import org.apache.hadoop.fs.{FileSystem, Path}
import org.json.JSONArray
import redis.clients.jedis.{Jedis, JedisPool}

import java.io.{BufferedReader, InputStreamReader}
import java.net.{URI, URLDecoder}
import java.sql.Timestamp
import java.util
import java.util.{Date, Properties}
import scala.collection.JavaConversions._
import scala.collection.mutable
import scala.util.control.Breaks.{break, breakable}

/**
 * @ClassName UnKnowRiskStream
 * @author hanyu
 * @date 2021/11/23 10:48
 * @description
 * @update [no][date YYYY-MM-DD][name][description]
 * */
object UnKnowRiskStream {
  def main(args: Array[String]): Unit = {
    val confProperties = new IniProperties(args(0))

    //该任务的名字
    val jobName = confProperties.getValue(Constants.UN_KNOW_RISK_WARN_CONFIG,
      Constants.UN_KNOW_RISK_WARN_JOB_NAME)

    //source并行度
    val sourceParallelism = confProperties.getIntValue(Constants
      .UN_KNOW_RISK_WARN_CONFIG, Constants.UN_KNOW_RISK_WARN_KAFKA_SOURCE_PARALLELISM)
    //deal并行度
    val dealParallelism = confProperties.getIntValue(Constants
      .UN_KNOW_RISK_WARN_CONFIG, Constants.UN_KNOW_RISK_WARN_DEAL_PARALLELISM)
    //sink的并行度
    val sinkParallelism = confProperties.getIntValue(Constants.UN_KNOW_RISK_WARN_CONFIG,
      Constants.UN_KNOW_RISK_WARN_KAFKA_SINK_PARALLELISM)

    //kafka的服务地址
    val brokerList = confProperties.getValue(Constants.FLINK_COMMON_CONFIG, Constants
      .KAFKA_BOOTSTRAP_SERVERS)
    //flink消费的group.id
    val groupId = confProperties.getValue(Constants.UN_KNOW_RISK_WARN_CONFIG,
      Constants.UN_KNOW_RISK_WARN_GROUP_ID)
    //kafka source的topic
    val sourceTopic = confProperties.getValue(Constants.OPERATION_FLINK_TO_DRUID_CONFIG, Constants
      .OPERATION_TOPIC)
    //kafka sink的topic
    val sinkTopic = confProperties.getValue(Constants.UN_KNOW_RISK_WARN_CONFIG, Constants
      .UN_KNOW_RISK_WARN_KAFKA_SINK_TOPIC)
    //check pointing的间隔
    val checkpointInterval = confProperties.getLongValue(Constants.UN_KNOW_RISK_WARN_CONFIG,
      Constants.UN_KNOW_RISK_WARN_CHECKPOINT_INTERVAL)


    //设置kafka消费者相关配置
    val props = new Properties()
    //设置kafka集群地址
    props.setProperty("bootstrap.servers", brokerList)
    //设置flink消费的group.id
    props.setProperty("group.id", groupId)

    //获取kafka消费者
    val consumer = new FlinkKafkaConsumer[String](sourceTopic, new SimpleStringSchema,
      props).setStartFromGroupOffsets()
    //获取kafka生产者
    val producer = new FlinkKafkaProducer[String](brokerList, sinkTopic, new
        SimpleStringSchema())

    //ip-地点关联文件路径
    val placePath = confProperties.getValue(Constants.OPERATION_FLINK_TO_DRUID_CONFIG, Constants
      .OPERATION_PLACE_PATH)
    //host-系统名关联文件路径
    val systemPath = confProperties.getValue(Constants.OPERATION_FLINK_TO_DRUID_CONFIG, Constants
      .OPERATION_SYSTEM_PATH)
    //用户名-常用登录地关联文件路径
    val usedPlacePath = confProperties.getValue(Constants.OPERATION_FLINK_TO_DRUID_CONFIG,
      Constants.OPERATION_USEDPLACE_PATH)

    val warningSinkTopic = confProperties.getValue(Constants.WARNING_FLINK_TO_DRUID_CONFIG, Constants
      .WARNING_TOPIC)

    //flink全局变量
    val parameters: Configuration = new Configuration()
    //c3p0连接池配置文件路径
    parameters.setString(Constants.c3p0_CONFIG_PATH, confProperties.getValue(Constants
      .FLINK_COMMON_CONFIG, Constants.c3p0_CONFIG_PATH))
    //文件系统类型
    parameters.setString(Constants.FILE_SYSTEM_TYPE, confProperties.getValue(Constants
      .FLINK_COMMON_CONFIG, Constants.FILE_SYSTEM_TYPE))
    //redis
    parameters.setString(Constants.REDIS_PACKAGE_PROPERTIES, confProperties.getValue(Constants
      .FLINK_COMMON_CONFIG, Constants.REDIS_PACKAGE_PROPERTIES))

    //druid的broker节点
    parameters.setString(Constants.DRUID_BROKER_HOST_PORT, confProperties.getValue(Constants.FLINK_COMMON_CONFIG, Constants.DRUID_BROKER_HOST_PORT))
    //druid时间格式
    parameters.setString(Constants.DRUID_TIME_FORMAT, confProperties.getValue(Constants.FLINK_COMMON_CONFIG, Constants.DRUID_TIME_FORMAT))
    //druid数据开始的时间
    parameters.setLong(Constants.UN_KNOW_RISK_WARN_DRUID_DATA_START_TIMESTAMP, confProperties.getLongValue(Constants.UN_KNOW_RISK_WARN_CONFIG, Constants.UN_KNOW_RISK_WARN_DRUID_DATA_START_TIMESTAMP))
    //druid的表名
    parameters.setString(Constants.WARNING_WARNING_TABLE_NAME, confProperties.getValue(Constants.WARNING_FLINK_TO_DRUID_CONFIG, Constants.WARNING_WARNING_TABLE_NAME))
    //日期配置文件的路径
    parameters.setString(Constants.DATE_CONFIG_PATH, confProperties.getValue(Constants.FLINK_COMMON_CONFIG, Constants.DATE_CONFIG_PATH))
    parameters.setString(Constants.UN_KNOW_RISK_WARN_CMD_SIMILARITY, confProperties.getValue(Constants.UN_KNOW_RISK_WARN_CONFIG, Constants.UN_KNOW_RISK_WARN_CMD_SIMILARITY))
    parameters.setString(Constants.UN_KNOW_RISK_WARN_SIMILARITY, confProperties.getValue(Constants.UN_KNOW_RISK_WARN_CONFIG, Constants.UN_KNOW_RISK_WARN_SIMILARITY))
    parameters.setString(Constants.UN_KNOW_RISK_WARN_SAMPLE_PERIOD, confProperties.getValue(Constants.UN_KNOW_RISK_WARN_CONFIG, Constants.UN_KNOW_RISK_WARN_SAMPLE_PERIOD))
    parameters.setString(Constants.UN_KNOW_RISK_WARN_CMD_LIST, confProperties.getValue(Constants.UN_KNOW_RISK_WARN_CONFIG, Constants.UN_KNOW_RISK_WARN_CMD_LIST))
    //获取ExecutionEnvironment
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    //设置check pointing的间隔
    env.enableCheckpointing(checkpointInterval)
    //设置flink全局变量
    env.getConfig.setGlobalJobParameters(parameters)
    env.setStreamTimeCharacteristic(TimeCharacteristic.ProcessingTime)

    val dStream = env.addSource(consumer).setParallelism(sourceParallelism)
    val value = dStream.map(new RichMapFunction[String, (Option[OperationModel], String)] {
      override def open(parameters: Configuration): Unit = {
        OperationModel.setPlaceMap(placePath)
        OperationModel.setSystemMap(systemPath)
        OperationModel.setMajorMap(systemPath)
        OperationModel.setUsedPlacesMap(usedPlacePath)
      }

      override def map(value: String): (Option[OperationModel], String) = (OperationModel
        .getOperationModel(value), value)
    }).setParallelism(sinkParallelism)
      .filter(_._1.isDefined).setParallelism(sinkParallelism)
      .map(t => (t._1.head, t._2)).setParallelism(sinkParallelism)
      .filter(new isUsername).setParallelism(sinkParallelism)
      .process(new UnKnownProcessFunction).setParallelism(dealParallelism)
    val unknowValue = value.map(_._1)
    val alertKafkaValue = value.map(_._2)


    unknowValue.addSink(new MySQLSink)
      .setParallelism(sinkParallelism)

    unknowValue
      .filter(_._1.asInstanceOf[UnKnowRiskEntity].getIsUnKnowRisk == 1)
      .map(o => {
        JsonUtil.toJson(o._1.asInstanceOf[UnKnowRiskEntity])
      })
      .addSink(producer)
      .setParallelism(sinkParallelism)

    //将告警数据写入告警库topic
    val warningProducer = new FlinkKafkaProducer[String](brokerList, warningSinkTopic, new
        SimpleStringSchema())

    alertKafkaValue.addSink(warningProducer).setParallelism(sinkParallelism)


    env.execute(jobName)
  }

  class UnKnownProcessFunction extends ProcessFunction[(OperationModel, String), ((Object, Boolean), String)] {
    //计数器申明
    val inCmdMessage: LongCounter = new LongCounter()
    val inTraitMessage: LongCounter = new LongCounter()
    val inCmdDetectionMessage: LongCounter = new LongCounter()
    val inTraitDetectionMessage: LongCounter = new LongCounter()
    val inTraitQueryMessage: LongCounter = new LongCounter()
    val orderMessage1: LongCounter = new LongCounter()
    val orderMessage2: LongCounter = new LongCounter()
    val orderMessageEstimate: LongCounter = new LongCounter()
    val orderMessageForeach: LongCounter = new LongCounter()
    val orderMessageForeach2: LongCounter = new LongCounter()
    //redis
    var jedisPool: JedisPool = _
    var jedis: Jedis = _
    var tableName: String = null
    var packageValue: String = _
    var packageType: String = _
    var url: String = _
    var ua: String = _
    var urlAB = new mutable.ArrayBuffer[String]
    var packageTypeAB = new mutable.ArrayBuffer[String]
    var packageValueAB = new mutable.ArrayBuffer[String]
    var alertNameAB = new mutable.ArrayBuffer[String]
    var userNameAB = new mutable.ArrayBuffer[String]
    var allValueAB = new mutable.ArrayBuffer[(String, String)]
    var cmdSimilarityValue: Double = 0.00D
    var trainSimilarityValue: Double = 0.00D
    var trainUnSimilarityValue: Double = 0.00D
    var samplePeriod: Int = 0
    var a: Double = 0.00D
    var b: Double = 0.00D
    var c: Double = 0.00D
    var d: Double = 0.00D
    var e: Double = 0.00D
    var operationModelValue: OperationModel = _
    var operationValue: String = ""
    var urlTest: (String, String) = _
    var urlMap = new mutable.HashMap[String, String]
    var cmdList = new mutable.ArrayBuffer[String]


    override def open(parameters: Configuration): Unit = {
      val globConf = getRuntimeContext.getExecutionConfig.getGlobalJobParameters.asInstanceOf[Configuration]
      val fileSystemType = globConf.getString(Constants.FILE_SYSTEM_TYPE, "/")
      val jedisConfigPath = globConf.getString(Constants.REDIS_PACKAGE_PROPERTIES, "")
      //根据redis配置文件,初始化redis连接池
      val redisProperties = new Properties()
      val fs = FileSystem.get(URI.create(fileSystemType), new org.apache.hadoop.conf
      .Configuration())
      val fsDataInputStream = fs.open(new Path(jedisConfigPath))
      val bufferedReader = new BufferedReader(new InputStreamReader(fsDataInputStream))
      redisProperties.load(bufferedReader)
      jedisPool = JedisUtil.getJedisPool(redisProperties)
      jedis = jedisPool.getResource
      //Druid
      DateUtil.ini(globConf.getString(Constants.FILE_SYSTEM_TYPE, ""), globConf.getString(Constants.DATE_CONFIG_PATH, ""))
      DruidUtil.setDruidHostPortSet(globConf.getString(Constants.DRUID_BROKER_HOST_PORT, ""))
      DruidUtil.setTimeFormat(globConf.getString(Constants.DRUID_TIME_FORMAT, ""))
      DruidUtil.setDateStartTimeStamp(globConf.getLong(Constants.DRUID_DATA_START_TIMESTAMP, 0L))
      tableName = globConf.getString(Constants.WARNING_WARNING_TABLE_NAME, "")
      cmdSimilarityValue = globConf.getDouble(Constants.UN_KNOW_RISK_WARN_CMD_SIMILARITY, 0.90D)
      trainSimilarityValue = globConf.getDouble(Constants.UN_KNOW_RISK_WARN_SIMILARITY, 0.98D)
      trainUnSimilarityValue = globConf.getDouble(Constants.UN_KNOW_RISK_WARN_UN_SIMILARITY, 0.01D)
      samplePeriod = globConf.getInteger(Constants.UN_KNOW_RISK_WARN_SAMPLE_PERIOD, 30)
      val cmdStr = globConf.getString(Constants.UN_KNOW_RISK_WARN_CMD_LIST, "")
      for (i <- cmdStr.split("\\|", -1)) {
        cmdList.append(i)
      }
      getRuntimeContext.addAccumulator("In Cmd Message", inCmdMessage)
      getRuntimeContext.addAccumulator("In Cmd Detection Message", inCmdDetectionMessage)
      getRuntimeContext.addAccumulator("In Trait Message", inTraitMessage)
      getRuntimeContext.addAccumulator("In Trait Detection Message", inTraitDetectionMessage)
      getRuntimeContext.addAccumulator("In Trait Query Message", inTraitQueryMessage)
      getRuntimeContext.addAccumulator("Order Message 1", orderMessage1)
      getRuntimeContext.addAccumulator("Order Message 2", orderMessage2)
      getRuntimeContext.addAccumulator("Order Message Estimate", orderMessageEstimate)
      getRuntimeContext.addAccumulator("Order Message Foreach", orderMessageForeach)
      getRuntimeContext.addAccumulator("Order Message Foreach2", orderMessageForeach2)

    }


    override def processElement(value: (OperationModel, String), ctx: ProcessFunction[(OperationModel, String),
      ((Object, Boolean), String)]#Context, out: Collector[((Object, Boolean), String)]): Unit = {
      operationModelValue = value._1
      operationValue = value._2
      val operationArr = operationValue.split("\\|", -1)
      //获取数据源package及packageTpy
      packageValue = jedis.get(operationModelValue.packagePath)
      if (packageValue != null && packageValue.nonEmpty) {
        if (packageValue.length > 1000) {
          packageValue = packageValue.substring(0, 1000)

        } else {
          packageValue = packageValue
        }
      }
      packageType = operationModelValue.packageType
      url = operationArr(6)
      ua = operationArr(9)
      urlTest = getUrlParameterTup(url)
      urlMap = getUrlParameterMap(url)
      //todo java 代码执行检测
      if (getIsJavaCodeInject(url, ".", samplePeriod * 3) || getIsJavaCodeInject(ua, ".", samplePeriod * 3)) {
        val unKnowRiskEntity = new UnKnowRiskEntity()
        unKnowRiskEntity.setUserName(operationModelValue.userName)
        unKnowRiskEntity.setSourceIp(operationModelValue.sourceIp)
        unKnowRiskEntity.setAlertTime(new Timestamp(operationModelValue.timeStamp))
        unKnowRiskEntity.setSourcePort(operationModelValue.sourcePort)
        unKnowRiskEntity.setDesIp(operationModelValue.destinationIp)
        unKnowRiskEntity.setDesPort(operationModelValue.destinationPort)
        unKnowRiskEntity.setLoginMajor(operationModelValue.loginMajor)
        unKnowRiskEntity.setLoginPlace(operationModelValue.loginPlace)
        unKnowRiskEntity.setLoginSystem(operationModelValue.loginPlace)
        unKnowRiskEntity.setIsRemote(operationModelValue.isRemote)
        unKnowRiskEntity.setUrl(operationArr(6))
        unKnowRiskEntity.setIsUnKnowRisk(1)
        unKnowRiskEntity.setRiskName("未知风险-代码注入")
        unKnowRiskEntity.setCmdInfo("")
        val outValue = getInputKafkaValue(value._1, url, "未知风险-代码注入", packageValue)
        out.collect((unKnowRiskEntity.asInstanceOf[Object], false), outValue)
      }


      if (urlTest._1.nonEmpty && urlTest._1 != null && urlTest._2.nonEmpty && urlTest._2 != null) {
        orderMessage1.add(1)
        if (urlTest._1.contains("cmd") && urlMap.contains("cmd")) {
          orderMessage2.add(1)
          val unKnowRiskEntity = new UnKnowRiskEntity()
          unKnowRiskEntity.setUserName(operationModelValue.userName)
          unKnowRiskEntity.setSourceIp(operationModelValue.sourceIp)
          unKnowRiskEntity.setAlertTime(new Timestamp(operationModelValue.timeStamp))
          unKnowRiskEntity.setSourcePort(operationModelValue.sourcePort)
          unKnowRiskEntity.setDesIp(operationModelValue.destinationIp)
          unKnowRiskEntity.setDesPort(operationModelValue.destinationPort)
          unKnowRiskEntity.setLoginMajor(operationModelValue.loginMajor)
          unKnowRiskEntity.setLoginPlace(operationModelValue.loginPlace)
          unKnowRiskEntity.setLoginSystem(operationModelValue.loginPlace)
          unKnowRiskEntity.setIsRemote(operationModelValue.isRemote)
          unKnowRiskEntity.setUrl(operationArr(6))
          unKnowRiskEntity.setIsUnKnowRisk(1)
          unKnowRiskEntity.setRiskName("未知风险-命令执行")
          if (urlMap("cmd").nonEmpty && urlMap("cmd") != null && urlMap("cmd") != "null" && urlMap("cmd").contains(" ")) {
            for (i <- cmdList) {
              if (urlMap("cmd").contains(i)) {
                orderMessageEstimate.add(1)
                unKnowRiskEntity.setCmdInfo(urlMap("cmd"))
                val outValue = getInputKafkaValue(value._1, url, "未知风险-命令执行", packageValue)
                out.collect((unKnowRiskEntity.asInstanceOf[Object], false), outValue)
              }
            }
          }

          val valueArr = urlMap.values.mkString("|").split("\\|", -1)
          for (i <- valueArr) {
            if (i.matches("[(./)(bin/)(sbin/)].*(.)") && (i.contains("./") || i.contains("bin/") || i.contains("sbin/"))) {
              orderMessageEstimate.add(1)
              unKnowRiskEntity.setCmdInfo(i)
              val outValue = getInputKafkaValue(value._1, url, "未知风险-命令执行", packageValue)
              out.collect((unKnowRiskEntity.asInstanceOf[Object], false), outValue)
            }


          }
        }
      }


      if (operationArr.length > 7 && operationArr(7) != null && operationArr(7).nonEmpty) {
        inCmdMessage.add(1)
        val timeLong = new Date().getTime
        val queryResultList: util.List[util.Map[String, String]] = DruidUtil.query(getDruidWarnQueryEntity(timeLong - TimeUtil.DAY_MILLISECOND * samplePeriod,
          timeLong, operationModelValue.destinationIp, tableName, samplePeriod))
        val unKnowRiskEntity = new UnKnowRiskEntity()
        unKnowRiskEntity.setUserName(operationModelValue.userName)
        unKnowRiskEntity.setSourceIp(operationModelValue.sourceIp)
        unKnowRiskEntity.setAlertTime(new Timestamp(operationModelValue.timeStamp))
        unKnowRiskEntity.setSourcePort(operationModelValue.sourcePort)
        unKnowRiskEntity.setDesIp(operationModelValue.destinationIp)
        unKnowRiskEntity.setDesPort(operationModelValue.destinationPort)
        unKnowRiskEntity.setLoginMajor(operationModelValue.loginMajor)
        unKnowRiskEntity.setLoginPlace(operationModelValue.loginPlace)
        unKnowRiskEntity.setLoginSystem(operationModelValue.loginPlace)
        unKnowRiskEntity.setIsRemote(operationModelValue.isRemote)
        unKnowRiskEntity.setCmdInfo(operationArr(7))
        unKnowRiskEntity.setUrl(operationArr(6))
        breakable {
          if (queryResultList != null && queryResultList.nonEmpty) {
            inCmdDetectionMessage.add(1)
            for (queryValue <- queryResultList) {
              urlAB.+=(queryValue.get(Dimension.url.toString))
              packageTypeAB.+=(queryValue.get(Dimension.packageType.toString))
              packageValueAB.+=(queryValue.get(Dimension.packageValue.toString))
              alertNameAB.+=(queryValue.get(Dimension.alertName.toString))
              userNameAB.+=(queryValue.get(Dimension.userName.toString))
            }
          } else {
            unKnowRiskEntity.setIsUnKnowRisk(1)
            unKnowRiskEntity.setRiskName("未知风险-首次出现")
            val outValue = getInputKafkaValue(value._1, url, "未知风险-首次出现", packageValue)

            out.collect((unKnowRiskEntity.asInstanceOf[Object], false), outValue)
            break()
          }
          if (urlAB.length == packageValueAB.length && urlAB.length == alertNameAB.length) {
            for (i <- urlAB.indices) {
              allValueAB.+=((urlAB(i), packageValueAB(i)))
            }
            for (i <- allValueAB) {
              //(url参数key集合，url参数值集合)
              val urlSample = getUrlParameterTup(i._1)
              if (packageValue != null && packageValue.nonEmpty && i._2 != null && i._2.nonEmpty) {
                c = IndexOfSimilarityUtile.IndexOfSimilarity(packageValue, i._2).sim()
              }
              a = IndexOfSimilarityUtile.IndexOfSimilarity(urlTest._1, urlSample._1).sim()
              b = IndexOfSimilarityUtile.IndexOfSimilarity(urlTest._2, urlSample._2).sim()
              if ((a + b + c) / 3 > cmdSimilarityValue && (packageTypeAB.contains(packageType) || userNameAB.contains(operationModelValue.userName))) {
                unKnowRiskEntity.setIsUnKnowRisk(0)
                unKnowRiskEntity.setRiskName(alertNameAB.toSet.toString())
                out.collect((unKnowRiskEntity.asInstanceOf[Object], false), "")
                break()
              } else {
                unKnowRiskEntity.setIsUnKnowRisk(1)
                unKnowRiskEntity.setRiskName("未知风险-参数异常")
                val outValue = getInputKafkaValue(value._1, url, "未知风险-参数异常", packageValue)

                out.collect((unKnowRiskEntity.asInstanceOf[Object], false), outValue)
                break()
              }
            }
          }
        }
        urlAB.clear()
        packageValueAB.clear()
        alertNameAB.clear()
        packageTypeAB.clear()
        allValueAB.clear()
        userNameAB.clear()
      }
      //todo
      if (operationArr.length > 7 && operationArr(7) != null && operationArr(7).nonEmpty) {
        inTraitMessage.add(1)
        //获取数据源package及packageTpy
        //        packageValue = jedis.get(operationModelValue.packagePath)
        //        packageType = operationModelValue.packageType
        //        url = operationArr(6)
        //        urlTest = getUrlParameter(url)
        val timeLong2 = new Date().getTime
        val queryResultList: util.List[util.Map[String, String]] = DruidUtil.query(getDruidWarnQueryEntity(timeLong2 - TimeUtil.DAY_MILLISECOND * samplePeriod,
          timeLong2, operationModelValue.destinationIp, tableName, samplePeriod))
        if (queryResultList != null && queryResultList.nonEmpty) {
          inTraitQueryMessage.add(1)
          for (queryValue <- queryResultList) {
            urlAB.+=(queryValue.get(Dimension.url.toString))
            packageValueAB.+=(queryValue.get(Dimension.packageValue.toString))
            packageTypeAB.+=(queryValue.get(Dimension.packageType.toString))
            alertNameAB.+=(queryValue.get(Dimension.alertName.toString))
            userNameAB.+=(queryValue.get(Dimension.userName.toString))
          }
        }
        breakable {
          for (i <- urlAB.indices) {
            inTraitDetectionMessage.add(1)
            val urlSampleKey = getUrlParameterTup(urlAB(i))._1
            val urlSampleValue = getUrlParameterTup(urlAB(i))._2
            val urlTestKey = urlTest._1
            val urlTestValue = urlTest._2
            if (urlSampleKey == "null" || urlSampleValue == "null" || urlTestKey == "null" || urlTestValue == "null") {
              break()
            } else {
              if (packageValueAB(i) != null && packageValueAB(i).nonEmpty && packageValue != null && packageValue.nonEmpty) {
                e = IndexOfSimilarityUtile.IndexOfSimilarity(packageValue, packageValueAB(i)).sim()
              }
              d = IndexOfSimilarityUtile.IndexOfSimilarity(urlSampleValue, urlTestValue).sim()
              if (urlSampleKey.equals(urlTestKey)) {
                val unKnowRiskEntity = new UnKnowRiskEntity()
                unKnowRiskEntity.setUserName(operationModelValue.userName)
                unKnowRiskEntity.setSourceIp(operationModelValue.sourceIp)
                unKnowRiskEntity.setSourcePort(operationModelValue.sourcePort)
                unKnowRiskEntity.setAlertTime(new Timestamp(value._1.timeStamp))
                unKnowRiskEntity.setDesIp(operationModelValue.destinationIp)
                unKnowRiskEntity.setDesPort(operationModelValue.destinationPort)
                unKnowRiskEntity.setLoginMajor(operationModelValue.loginMajor)
                unKnowRiskEntity.setLoginPlace(operationModelValue.loginPlace)
                unKnowRiskEntity.setLoginSystem(operationModelValue.loginPlace)
                unKnowRiskEntity.setIsRemote(operationModelValue.isRemote)
                unKnowRiskEntity.setCmdInfo(operationArr(7))
                unKnowRiskEntity.setUrl(operationArr(6))
                if ((d + e) / 2 > trainSimilarityValue) {
                  break()
                } else if (d < trainUnSimilarityValue) {
                  if (packageTypeAB.contains(packageType) || userNameAB.contains(operationModelValue.userName)) {
                    unKnowRiskEntity.setIsUnKnowRisk(0)
                    unKnowRiskEntity.setRiskName(alertNameAB.toSet.toString())
                    out.collect((unKnowRiskEntity.asInstanceOf[Object], false), "")
                    break()
                  } else {
                    unKnowRiskEntity.setIsUnKnowRisk(1)
                    unKnowRiskEntity.setRiskName("未知风险-特征")
                    val outValue = getInputKafkaValue(value._1, url, "未知风险-特征", packageValue)

                    out.collect((unKnowRiskEntity.asInstanceOf[Object], false), outValue)
                    break()
                  }
                } else {
                  break()
                }
              }

            }
          }
        }
        urlAB.clear()
        packageValueAB.clear()
        packageTypeAB.clear()
        alertNameAB.clear()
        userNameAB.clear()

      }


    }

    override def close(): Unit = {

    }

    def getIsJavaCodeInject(urlValue: String, flag: String, threshold: Int): Boolean = {
      val urlStr = URLDecoder.decode(urlValue.replaceAll("%(?![0-9a-fA-F]{2})", "%25"), "utf-8").toLowerCase()
      val valueCharArr = urlValue.toCharArray
      var num = 0
      for (urlChar <- valueCharArr) {
        if (urlChar.toString.equals(flag)) {
          num = num + 1
        }
      }
      num > threshold && (urlStr.contains("org") || urlStr.contains("apache") || urlStr.contains("get") || urlStr.contains("put") || urlStr.contains("map") || urlStr.contains("string"))

    }

    def getDruidWarnQueryEntity(startTime: Long, endTime: Long, desIp: String, tableName: String, samplePeriod: Int)
    : Entity = {
      val entity = new Entity()

      entity.setAggregationsSet(Aggregation.getAggregationsSet(Aggregation.connCount))
      entity.setDimensionsSet(Dimension.getDimensionsSet(Dimension.userName, Dimension.url, Dimension.packageValue, Dimension.packageType, Dimension.alertName))
      entity.setStartTimeStr(startTime)
      entity.setEndTimeStr(endTime)

      val jsonArray = new JSONArray()
      jsonArray.put(Filter.getFilter(Filter.selector, Dimension.alertDestIp, desIp))
      entity.setFilter(Filter.getFilter(Filter.and, jsonArray))
      entity.setGranularity(Granularity.getGranularity(Granularity.periodDay, 1))
      entity.setTableName(tableName)

      entity
    }


  }

  class isUsername extends RichFilterFunction[(OperationModel, String)] {
    override def filter(value: (OperationModel, String)): Boolean = {
      val username = value._1.userName
      !(username == null || username.length != 11 || username.charAt(8) != '@' || "匿名用户".equals(username))
    }
  }
}
