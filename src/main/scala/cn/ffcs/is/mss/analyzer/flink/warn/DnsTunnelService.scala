package cn.ffcs.is.mss.analyzer.flink.warn

import java.io.{BufferedReader, InputStreamReader, LineNumberReader}
import java.net.URI
import java.sql.Timestamp
import java.util.Properties

import cn.ffcs.is.mss.analyzer.bean.DnsTunnelServiceEntity
import cn.ffcs.is.mss.analyzer.druid.model.scala.DnsModel
import cn.ffcs.is.mss.analyzer.flink.sink.{MySQLSink, Sink}
import cn.ffcs.is.mss.analyzer.flink.source.Source
import cn.ffcs.is.mss.analyzer.ml.svm._
import cn.ffcs.is.mss.analyzer.utils.{Constants, IniProperties, JsonUtil}
import org.apache.flink.api.common.accumulators.LongCounter
import org.apache.flink.api.common.eventtime.WatermarkStrategy
import org.apache.flink.api.common.functions.RichFilterFunction
import org.apache.flink.configuration.{ConfigOptions, Configuration}
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.functions.ProcessFunction
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaConsumer, FlinkKafkaProducer}
import org.apache.flink.streaming.util.serialization.SimpleStringSchema
import org.apache.flink.util.Collector
import org.apache.hadoop.fs.{FileSystem, Path}

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.util.control.Breaks.{break, breakable}

/**
 * @title Dns隐秘隧道检测
 * @author hanyu
 * @date 2020-09-27 14:14
 * @description
 * @update [no][date YYYY-MM-DD][name][description]
 */
object DnsTunnelService {
  def main(args: Array[String]): Unit = {
    //    val confPath = "G:\\福富Flink\\mss\\src\\main\\resources\\flink.ini"
    //    val confProperties = new IniProperties(confPath)
    val confProperties = new IniProperties(args(0))

    //任务的名字
    val jobName = confProperties.getValue(Constants.DNS_TUNNENL_SERVICE_CONFIG, Constants
      .DNS_TUNNENL_SERVICE_JOB_NAME)

    //并行度
    val sourceParallelism = confProperties.getIntValue(Constants.DNS_TUNNENL_SERVICE_CONFIG,
      Constants.DNS_TUNNENL_SERVICE_SOURCE_PARALLELISM)
    val sinkParallelism = confProperties.getIntValue(Constants.DNS_TUNNENL_SERVICE_CONFIG,
      Constants.DNS_TUNNENL_SERVICE_SINK_PARALLELISM)
    val dealParallelism = confProperties.getIntValue(Constants.DNS_TUNNENL_SERVICE_CONFIG,
      Constants.DNS_TUNNENL_SERVICE_DEAL_PARALLELISM)

    val warningSinkTopic = confProperties.getValue(Constants.WARNING_FLINK_TO_DRUID_CONFIG, Constants
      .WARNING_TOPIC)

    //check pointing的间隔
    val checkpointInterval = confProperties.getLongValue(Constants.DNS_TUNNENL_SERVICE_CONFIG,
      Constants.DNS_TUNNENL_SERVICE_CHECKPOINT_INTERVAL)

    //kafka的服务地址
    val brokerList = confProperties.getValue(Constants.FLINK_COMMON_CONFIG, Constants
      .KAFKA_BOOTSTRAP_SERVERS)
    //flink消费的group.id
    val groupId = confProperties.getValue(Constants.DNS_TUNNENL_SERVICE_CONFIG, Constants
      .DNS_TUNNENL_SERVICE_GROUP_ID)
    //kafka source 的topic
    val topic = confProperties.getValue(Constants.DNS_TUNNENL_SERVICE_CONFIG, Constants
      .DNS_TUNNENL_SERVICE_KAFKA_SOURCE_TOPIC)
    //kafka sink topic
    val kafkaSinkTopic = confProperties.getValue(Constants.DNS_TUNNENL_SERVICE_CONFIG,
      Constants.DNS_TUNNENL_SERVICE_KAFKA_SINK_TOPIC)

    //flink全局变量
    val parameters: Configuration = new Configuration()
    //文件类型配置
    parameters.setString(Constants.FILE_SYSTEM_TYPE, confProperties.getValue(Constants
      .FLINK_COMMON_CONFIG, Constants.FILE_SYSTEM_TYPE))
    //c3p0连接池配置文件路径
    parameters.setString(Constants.c3p0_CONFIG_PATH, confProperties.getValue(Constants
      .FLINK_COMMON_CONFIG, Constants.c3p0_CONFIG_PATH))
    //训练集文件路径
    parameters.setString(Constants.DNS_TUNNENL_SERVICE_TRAIN_DATA_PATH, confProperties.getValue(Constants.
      DNS_TUNNENL_SERVICE_CONFIG, Constants.DNS_TUNNENL_SERVICE_TRAIN_DATA_PATH))
    //dns 白名单
    parameters.setString(Constants.DNS_TUNNENL_SERVICE_WHITE_LIST, confProperties.getValue(Constants.
      DNS_TUNNENL_SERVICE_CONFIG, Constants.DNS_TUNNENL_SERVICE_WHITE_LIST))
    //svm参数
    parameters.setInteger(Constants.DNS_TUNNENL_SERVICE_SVM_PUN_FACTOR, confProperties.getIntValue(Constants.
      DNS_TUNNENL_SERVICE_CONFIG, Constants.DNS_TUNNENL_SERVICE_SVM_PUN_FACTOR))
    parameters.setDouble(Constants.DNS_TUNNENL_SERVICE_SVM_TOL_LIMIT, confProperties.getValue(Constants.
      DNS_TUNNENL_SERVICE_CONFIG, Constants.DNS_TUNNENL_SERVICE_SVM_TOL_LIMIT).toDouble)
    parameters.setInteger(Constants.DNS_TUNNENL_SERVICE_SVM_MAXPASSES, confProperties.getIntValue(Constants.
      DNS_TUNNENL_SERVICE_CONFIG, Constants.DNS_TUNNENL_SERVICE_SVM_MAXPASSES))
    //输出标准
    parameters.setInteger(Constants.DNS_TUNNENL_SERVICE_SVM_VERIFY_DATA, confProperties.getIntValue(Constants.
      DNS_TUNNENL_SERVICE_CONFIG, Constants.DNS_TUNNENL_SERVICE_SVM_VERIFY_DATA))
    parameters.setDouble(Constants.DNS_TUNNENL_SERVICE_SVM_MODEL_ACCURACY, confProperties.getFloatValue(Constants.
      DNS_TUNNENL_SERVICE_CONFIG, Constants.DNS_TUNNENL_SERVICE_SVM_MODEL_ACCURACY))

    //获取kafka消费者
    val consumer = Source.kafkaSource(topic, groupId, brokerList)
    //获取kafka 生产者
    val producer = Sink.kafkaSink(brokerList, kafkaSinkTopic)

    //获取ExecutionEnvironment
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    //设置check pointing的间隔
    //env.enableCheckpointing(checkpointInterval)
    //设置flink全局变量
    env.getConfig.setGlobalJobParameters(parameters)

    //获取数据流
      val dStream = env.fromSource(consumer, WatermarkStrategy.noWatermarks(), "kafkaSource").setParallelism(sourceParallelism)
  //    val dStream = env.socketTextStream("192.168.1.105", 8899)
      .map(JsonUtil.fromJson[DnsModel] _).setParallelism(dealParallelism)
      //过滤白名单中的dns
      .filter(new WhiteListFilterFunction).setParallelism(dealParallelism)
      .process(new DnsTunnelServiceMlPro).setParallelism(dealParallelism)

    val value = dStream.map(_._1)
    val alertKafkaValue = dStream.map(_._2)
    //写入mysql
    value.addSink(new MySQLSink).setParallelism(sinkParallelism)

    dStream
      .map(o => {
        JsonUtil.toJson(o._1._1.asInstanceOf[DnsTunnelServiceEntity])
      })
      .sinkTo(producer)
      .setParallelism(sinkParallelism)

    //将告警数据写入告警库topic
    val warningProducer = Sink.kafkaSink(brokerList, warningSinkTopic)
    alertKafkaValue.sinkTo(warningProducer).setParallelism(sinkParallelism)

    env.execute(jobName)

  }

  /**
   *
   *
   * @return
   * @author hanuyu
   * @date 2020/10/23 09:13
   * @description 过滤配置中的白名单
   *              匹配上白名单为False 过滤
   *              未匹配上白名单为true进入模型匹配
   * @update [no][date YYYY-MM-DD][name][description]
   */
  class WhiteListFilterFunction extends RichFilterFunction[DnsModel] {
    var dnsWhiteArr: List[String] = _

    override def open(parameters: Configuration): Unit = {
      val globConf = getRuntimeContext.getExecutionConfig.getGlobalJobParameters.asInstanceOf[Configuration]
      val dnsWhiteList = globConf.getString(ConfigOptions.key(Constants.DNS_TUNNENL_SERVICE_WHITE_LIST).stringType().defaultValue(""))
      dnsWhiteArr = dnsWhiteList.trim.split("\\|", -1).toList
    }

    override def filter(value: DnsModel): Boolean = {
      var flag = true
      breakable {
        for (i <- dnsWhiteArr) {
          if (value.queryDomainName.contains(i)) {
            flag = false
            break()
          }
        }
      }
      flag
    }
  }

  /**
   *
   *
   * @return
   * @author hanyu
   * @date 2020/10/23 09:15
   * @description svm 判别流数据是否为隐秘隧道
   * @update [no][date YYYY-MM-DD][name][description]
   */
  class DnsTunnelServiceMlPro extends ProcessFunction[DnsModel, ((Object, Boolean), String)] {
    //svm参数：svmC：惩罚因子，svmTol：容忍极限值  svmMaxPasses：拉格朗日乘子得最多迭代次数
    var svmC = 0D
    var svmTol: Double = 0.00D
    var svmMaxPasses = 0
    val inputKafkaValue: String = ""

    //训练样本数据地址
    var sampleDataPath = ""

    //-------------------------特征工程-------------------------
    var charIndex = new mutable.LinkedHashMap[String, Int]()

    //训练数据arr
    var featureArr = new StringBuffer()
    //测试数据arr
    var predictArr = new StringBuffer()
    //数据中间存储容器
    var midDataArr = new mutable.ArrayBuffer[Array[String]]()
    //样本行数
    var sampleLine = 0
    //特征个数
    var traitNum = 0
    //类型
    var classType = 0

    //初始化svm相关对象
    var svmTrainData: SvmTrainData = new SvmTrainData()
    var svmMidPreData: SvmPrediceData = null
    val smo = new SvmSimplifiedSmo()
    var svModel: SvmModel = null

    //训练数据和model准确度
    var verifyDataCount = 0
    var modelAccuracy = 0D

    //计数器
    val modelscc: LongCounter = new LongCounter()

    //读取样本数据训练
    override def open(parameters: Configuration): Unit = {
      val globConf = getRuntimeContext.getExecutionConfig.getGlobalJobParameters.asInstanceOf[Configuration]
       sampleDataPath = globConf.getString(ConfigOptions.key(Constants.DNS_TUNNENL_SERVICE_TRAIN_DATA_PATH).stringType().defaultValue(""))
      //svm内部逻辑三个参数
      svmC = globConf.getDouble(ConfigOptions.key(Constants.DNS_TUNNENL_SERVICE_SVM_PUN_FACTOR).doubleType().defaultValue(0.0D))
      svmTol = globConf.getDouble(ConfigOptions.key(Constants.DNS_TUNNENL_SERVICE_SVM_TOL_LIMIT).doubleType().defaultValue(0.01D))
      svmMaxPasses = globConf.getInteger(ConfigOptions.key(Constants.DNS_TUNNENL_SERVICE_SVM_MAXPASSES).intType().defaultValue(0))


      //标准参数
      verifyDataCount = globConf.getInteger(ConfigOptions.key(Constants.DNS_TUNNENL_SERVICE_SVM_VERIFY_DATA).intType().defaultValue(0))
      modelAccuracy = globConf.getDouble(ConfigOptions.key(Constants.DNS_TUNNENL_SERVICE_SVM_MODEL_ACCURACY).doubleType().defaultValue(0D))


      //计数器
      getRuntimeContext.addAccumulator("model train acc", modelscc)

      //      val fs = FileSystem.get(URI.create("file:///"), new org.apache.hadoop.conf.Configuration())
      //      val fsDataInputStream = fs.open(new Path(sampleDataPath))
      //      val bufferedReader = new BufferedReader(new InputStreamReader(fsDataInputStream))
      /**
       *
       *
       * @return
       * @author hanyu
       * @date 2020/10/23 09:29
       * @description 读取hdfs 读取训练样本 获取最大的char_index
       * @update [no][date YYYY-MM-DD][name][description]
       */
      val systemType = globConf.getString(ConfigOptions.key(Constants.FILE_SYSTEM_TYPE).stringType().defaultValue(""))
      val fs = FileSystem.get(URI.create(systemType), new org.apache.hadoop.conf.Configuration())
      val fsDataInputStream = fs.open(new Path(sampleDataPath))
      val bufferedReader = new BufferedReader(new InputStreamReader(fsDataInputStream))
      var strLineTrait: String = bufferedReader.readLine()
      var index = 0

      while (strLineTrait != null) {
        //获取行数返回
        sampleLine += 1
        val sampleDataStr = strLineTrait.split("\\|", -1)(1)
        sampleDataStr.foreach(i => {
          if (!charIndex.contains(i.toString)) {
            charIndex.put(i.toString, index)
            index += 1
          }
        })
        strLineTrait = bufferedReader.readLine()
      }
      /**
       *
       *
       * @return
       * @author hanyu
       * @date 2020/10/23 09:31
       * @description 根据char_index 对训练样本进行特征工程
       *             样本转化为char对应char_index 出现次数。
       * @update [no][date YYYY-MM-DD][name][description]
       */

      val systemTypeTrait = globConf.getString(ConfigOptions.key(Constants.FILE_SYSTEM_TYPE).stringType().defaultValue(""))
      val fsTrait = FileSystem.get(URI.create(systemTypeTrait), new org.apache.hadoop.conf.Configuration())
      val fsDataInputStreamTrait = fsTrait.open(new Path(sampleDataPath))
      val bufferedReaderTrait = new BufferedReader(new InputStreamReader(fsDataInputStreamTrait))
      var strLine: String = bufferedReaderTrait.readLine()

      //      val fsTrait = FileSystem.get(URI.create("file:///"), new org.apache.hadoop.conf.Configuration())
      //      val fsDataInputStreamTrait = fsTrait.open(new Path(sampleDataPath))
      //      val bufferedReaderTrait = new BufferedReader(new InputStreamReader(fsDataInputStreamTrait))
      //      var strLine: String = bufferedReaderTrait.readLine()
      var indexNum = 0
      while (strLine != null) {
        var traitListToArr = List.fill(charIndex.size)("0")
        val simpleData: Array[String] = strLine.split("\\|", -1)
        val simpleDataCharArr = simpleData(1).toCharArray
        simpleDataCharArr.foreach(i => {
          if (charIndex.contains(i.toString)) {
            val i1 = charIndex(i.toString)
            indexNum = traitListToArr(i1).toInt + 1
            traitListToArr = traitListToArr.updated(i1, indexNum.toString)
          }
        })
        //特征个数
        traitNum = charIndex.size
        //将数据标签添加至首位
        traitListToArr = traitListToArr.::(simpleData(0))
        //添加每行数据至中间存储容器
        midDataArr.+=(traitListToArr.toArray)
        //装载每行数据至训练svmdata
        svmTrainData = new SvmTrainReader().getSVMData(traitListToArr.toArray)

        strLine = bufferedReaderTrait.readLine()
      }

      //测试数据Array[String] 组成的ArrayBuffer
      val preducetRandomAb: mutable.ArrayBuffer[Array[String]] = randomReaadFun(midDataArr, verifyDataCount)
      //获取训练数据
      val predictReader = new SvmPredictReader
      preducetRandomAb.foreach(i => {
        svmMidPreData = predictReader.getSVMDataAll(preducetRandomAb.indexOf(i), preducetRandomAb.size, i, i.length - 1)
      })
      //训练model 准确率大于阈值返回model
      breakable {
        while (true) {
          val svModeTest = smo.train(svmTrainData, traitNum, sampleLine, svmC, svmTol, svmMaxPasses)

          val doubles: Array[Double] = smo.predict(svModeTest, svmMidPreData.getX, svmMidPreData.getY, svmTrainData)

          if (doubles(0) > modelAccuracy) {
            svModel = svModeTest
            break()
          }
        }
      }
      //训练model
      modelscc.add(1)

    }


    //判断数据流中数据是否满足告警条件
    override def processElement(value: DnsModel, ctx: ProcessFunction[DnsModel, ((Object, Boolean), String)]#Context,
                                out: Collector[((Object, Boolean), String)]): Unit = {
      val dnsValue = value.queryDomainName
      var preListArr = List.fill(traitNum)("0")
      var indexFlag = 0
      dnsValue.toCharArray.foreach(i => {
        if (charIndex.contains(i.toString)) {
          val maybeInt = charIndex(i.toString)
          indexFlag = preListArr(maybeInt).toInt + 1

          preListArr = preListArr.updated(maybeInt, indexFlag.toString)

        }
      })
      preListArr = preListArr.::("1")
      val preArr = preListArr.toArray
      val preducetData: SvmPrediceData = new SvmPredictReader().getSvmData(preArr, preArr.length - 1)
      val resultNum: Array[Double] = smo.predict(svModel, preducetData.getX, preducetData.getY, svmTrainData)
      //小于0属于隐秘隧道   大于0属于非隐秘隧道
      if (resultNum(1) < 0 || dnsValue.contains("jddebug")) {
        val dnsTunnelServiceEntity = new DnsTunnelServiceEntity()
        dnsTunnelServiceEntity.setAlertTime(new Timestamp(value.timeStamp))
        dnsTunnelServiceEntity.setAnswerRrs(value.answerRrs)
        dnsTunnelServiceEntity.setQueryResult(value.queryResult)
        dnsTunnelServiceEntity.setReplayCode(value.replyCode)
        dnsTunnelServiceEntity.setQueryDomainName(value.queryDomainName)
        val inputKafkaValue = "未知用户" + "|" + "Dns隐秘隧道检测" + "|" + value.timeStamp + "|" +
          "" + "|" + "" + "|" + "" + "|" +
          "" + "|" + "" + "|" + "" + "|" +
          "" + "|" + "" + "|" + value.queryDomainName + "|" +
          "" + "|" + "" + "|" + ""
        out.collect((dnsTunnelServiceEntity.asInstanceOf[Object], false), inputKafkaValue)

      }


    }

  }

  /**
   *
   *
   * @return ArrayBuffer[Array[String]
   * @author hanyu
   * @date 2020/10/19 09:34
   * @description 从训练数据中选取若干个数据组成测试数据Array[String] 组成ArryBuffer
   * @update [no][date YYYY-MM-DD][name][description]
   */
  def randomReaadFun(allData: mutable.ArrayBuffer[Array[String]], randomLength: Int): mutable.ArrayBuffer[Array[String]] = {
    val resultAb = new mutable.ArrayBuffer[Array[String]]
    if (allData.length <= randomLength) {
      allData
    } else {
      val randomSet = new mutable.HashSet[Int]()
      while (randomSet.size < randomLength) {
        val indexNum = (Math.random() * allData.length).toInt
        randomSet.add(indexNum)
      }
      randomSet.foreach((i: Int) => resultAb.append(allData(i)))
      resultAb
    }

  }

  /**
   *
   *
   * @return int
   * @author hanyu
   * @date 2020/10/14 10:14
   * @description 统计字符串中数字字母小数点个数
   * @update [no][date YYYY-MM-DD][name][description]
   */
  def countStringNum(countStr: String): Int = {
    var digit = 0
    for (ch <- countStr.toCharArray) {
      if (ch.isDigit) {
        digit += 1
      }
    }
    digit

  }

}

