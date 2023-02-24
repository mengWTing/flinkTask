/*
 * @project mss
 * @company Fujian Fujitsu Communication Software Co., Ltd.
 * @author chenwei
 * @date 2020-04-13 10:23:14
 * @version v1.0
 * @update [no] [date YYYY-MM-DD] [name] [description]
 */
package cn.ffcs.is.mss.analyzer.flink.warn

import java.io.{BufferedReader, InputStreamReader}
import java.net.URI
import java.sql.Timestamp
import java.util.Properties

import cn.ffcs.is.mss.analyzer.bean.BbasDirectoryTraversalWarnEntity
import cn.ffcs.is.mss.analyzer.druid.model.scala.OperationModel
import cn.ffcs.is.mss.analyzer.flink.sink.MySQLSink
import cn.ffcs.is.mss.analyzer.ml.tree.{CART, DecisionTreeNode}
import cn.ffcs.is.mss.analyzer.ml.utils.MlUtil
import cn.ffcs.is.mss.analyzer.utils.{Constants, IniProperties, JsonUtil}
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.functions.ProcessFunction
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaConsumer, FlinkKafkaProducer}
import org.apache.flink.streaming.util.serialization.SimpleStringSchema
import org.apache.flink.util.Collector

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

/**
 *
 * @author chenwei
 * @date 2020-04-13 10:23:14
 * @title DirectoryTraversalWarn 目录遍历
 * @update [no] [date YYYY-MM-DD] [name] [description]
 */
object DirectoryTraversalWarn {

  def main(args: Array[String]): Unit = {

    //val args0 = "./src/main/resources/flink.ini"
    //根据传入的参数解析配置文件
    //val confProperties = new IniProperties(args0)
    val confProperties = new IniProperties(args(0))

    //该任务的名字
    val jobName = confProperties.getValue(Constants.DIRECTORY_TRAVERSAL_CONFIG,
      Constants.DIRECTORY_TRAVERSAL_JOB_NAME)
    //kafka Source的名字
    val kafkaSourceName = confProperties.getValue(Constants.DIRECTORY_TRAVERSAL_CONFIG,
      Constants.DIRECTORY_TRAVERSAL_KAFKA_SOURCE_NAME)
    //mysql sink的名字
    val sqlSinkName = confProperties.getValue(Constants.DIRECTORY_TRAVERSAL_CONFIG,
      Constants.DIRECTORY_TRAVERSAL_SQL_SINK_NAME)
    //kafka sink名字
    val kafkaSinkName = confProperties.getValue(Constants.DIRECTORY_TRAVERSAL_CONFIG,
      Constants.DIRECTORY_TRAVERSAL_KAFKA_SINK_NAME)

    //kafka Source的并行度
    val kafkaSourceParallelism = confProperties.getIntValue(Constants
      .DIRECTORY_TRAVERSAL_CONFIG, Constants
      .DIRECTORY_TRAVERSAL_KAFKA_SOURCE_PARALLELISM)
    //对数据处理的并行度
    val dealParallelism = confProperties.getIntValue(Constants
      .DIRECTORY_TRAVERSAL_CONFIG, Constants.DIRECTORY_TRAVERSAL_DEAL_PARALLELISM)
    //写入mysql的并行度
    val sqlSinkParallelism = confProperties.getIntValue(Constants
      .DIRECTORY_TRAVERSAL_CONFIG, Constants.DIRECTORY_TRAVERSAL_SQL_SINK_PARALLELISM)
    //kafka sink的并行度
    val kafkaSinkParallelism = confProperties.getIntValue(Constants.DIRECTORY_TRAVERSAL_CONFIG,
      Constants.DIRECTORY_TRAVERSAL_KAFKA_SINK_PARALLELISM)

    val warningSinkTopic = confProperties.getValue(Constants.WARNING_FLINK_TO_DRUID_CONFIG, Constants
      .WARNING_TOPIC)

    //kafka的服务地址
    val brokerList = confProperties.getValue(Constants.FLINK_COMMON_CONFIG, Constants
      .KAFKA_BOOTSTRAP_SERVERS)
    //flink消费的group.id
    val groupId = confProperties.getValue(Constants.DIRECTORY_TRAVERSAL_CONFIG,
      Constants.DIRECTORY_TRAVERSAL_GROUP_ID)
    //kafka source的topic
    val sourceTopic = confProperties.getValue(Constants.OPERATION_FLINK_TO_DRUID_CONFIG, Constants
      .OPERATION_TOPIC)
    //kafka sink的topic
    val sinkTopic = confProperties.getValue(Constants.DIRECTORY_TRAVERSAL_CONFIG, Constants
      .DIRECTORY_TRAVERSAL_KAFKA_SINK_TOPIC)



    //flink全局变量
    val parameters: Configuration = new Configuration()
    //文件类型配置
    parameters.setString(Constants.FILE_SYSTEM_TYPE, confProperties.getValue(Constants
      .FLINK_COMMON_CONFIG, Constants.FILE_SYSTEM_TYPE))
    //c3p0连接池配置文件路径
    parameters.setString(Constants.c3p0_CONFIG_PATH, confProperties.getValue(Constants
      .FLINK_COMMON_CONFIG, Constants.c3p0_CONFIG_PATH))
    //目录遍历训练集文件路径
    parameters.setString(Constants.DIRECTORY_TRAVERSAL_TRAIN_DATA_PATH, confProperties.getValue(Constants.
      DIRECTORY_TRAVERSAL_CONFIG, Constants.DIRECTORY_TRAVERSAL_TRAIN_DATA_PATH))

    //check pointing的间隔
    val checkpointInterval = confProperties.getLongValue(Constants
      .DIRECTORY_TRAVERSAL_CONFIG, Constants.DIRECTORY_TRAVERSAL_CHECKPOINT_INTERVAL)

    //ip-地点关联文件路径
    parameters.setString(Constants
      .OPERATION_PLACE_PATH, confProperties.getValue(Constants.OPERATION_FLINK_TO_DRUID_CONFIG,
      Constants.OPERATION_PLACE_PATH))
    //host-系统名关联文件路径
    parameters.setString(Constants.OPERATION_SYSTEM_PATH, confProperties
      .getValue(Constants.OPERATION_FLINK_TO_DRUID_CONFIG, Constants.OPERATION_SYSTEM_PATH))
    //用户名-常用登录地关联文件路径
    parameters.setString(Constants.OPERATION_USEDPLACE_PATH, confProperties
      .getValue(Constants.OPERATION_FLINK_TO_DRUID_CONFIG, Constants.OPERATION_USEDPLACE_PATH))

    //获取ExecutionEnvironment
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    //设置check pointing的间隔
//    env.enableCheckpointing(checkpointInterval)
    //设置流的时间为EventTime
    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)
    //设置flink全局变量
    env.getConfig.setGlobalJobParameters(parameters)

    //设置kafka消费者相关配置
    val props = new Properties()
    //设置kafka集群地址
    props.setProperty("bootstrap.servers", brokerList)
    //设置flink消费的group.id
    props.setProperty("group.id", groupId)

    //获取kafka消费者
    val consumer = new FlinkKafkaConsumer[String](sourceTopic, new SimpleStringSchema, props)
      .setStartFromGroupOffsets()
    // 获取kafka数据
    val dStream = env.addSource(consumer).setParallelism(kafkaSourceParallelism)
      .uid(kafkaSourceName).name(kafkaSourceName)

    val directoryTraversalStream = dStream.process(new DetectionDirectoryTraversal)
      .setParallelism(dealParallelism)

    val value: DataStream[(Object, Boolean)] = directoryTraversalStream.map(_._1)
    val alertKafkaValue = directoryTraversalStream.map(_._2)
    value.addSink(new MySQLSink)
      .uid(sqlSinkName)
      .name(sqlSinkName)
      .setParallelism(sqlSinkParallelism)

    //获取kafka生产者
    val producer = new FlinkKafkaProducer[String](brokerList, sinkTopic, new SimpleStringSchema())
    directoryTraversalStream.map(o => JsonUtil.toJson(o._1._1.asInstanceOf[BbasDirectoryTraversalWarnEntity]))
      .addSink(producer)
      .uid(kafkaSinkName)
      .name(kafkaSinkName)
      .setParallelism(kafkaSinkParallelism)

    //将告警数据写入告警库topic
    val warningProducer = new FlinkKafkaProducer[String](brokerList, warningSinkTopic, new
        SimpleStringSchema())
    alertKafkaValue.addSink(warningProducer).setParallelism(kafkaSinkParallelism)

    env.execute(jobName)
  }

  class DetectionDirectoryTraversal extends ProcessFunction[String, ((Object, Boolean), String)] {

    //类型在样本里的位置
    val typeIndexMap = mutable.Map[String, Integer]()
    val indexTypeMap = mutable.Map[Integer, String]()
    var decisionTreeNode: DecisionTreeNode = null

    val inputKafkaValue = ""

    override def open(parameters: Configuration): Unit = {

      val globConf = getRuntimeContext.getExecutionConfig.getGlobalJobParameters
        .asInstanceOf[Configuration]

      //训练数据路径
      val trainDataPath = globConf.getString(Constants.DIRECTORY_TRAVERSAL_TRAIN_DATA_PATH, "")
      val fileSystemType = globConf.getString(Constants.FILE_SYSTEM_TYPE, "file://")

      //ip-地点关联文件路径
      OperationModel.setPlaceMap(globConf.getString(Constants.OPERATION_PLACE_PATH, ""))
      //host-系统名关联文件路径
      OperationModel.setSystemMap(globConf.getString(Constants.OPERATION_SYSTEM_PATH, ""))
      OperationModel.setMajorMap(globConf.getString(Constants.OPERATION_SYSTEM_PATH, ""))
      //用户名-常用登录地关联文件路径
      OperationModel.setUsedPlacesMap(globConf.getString(Constants.OPERATION_USEDPLACE_PATH, ""))

      //保存样本和标记
      val targetArrayList = ArrayBuffer[String]()
      val sampleArrayList = ArrayBuffer[String]()

      //读取训练数据
      val fileSystem = org.apache.hadoop.fs.FileSystem.get(URI.create(fileSystemType), new org.apache.hadoop.conf.Configuration)
      val fsDataInputStream = fileSystem.open(new org.apache.hadoop.fs.Path(trainDataPath))
      val bufferedReader = new BufferedReader(new InputStreamReader(fsDataInputStream))
      var line = bufferedReader.readLine()

      while (line != null) {
        val values = line.split("\\|", 2)
        targetArrayList.append(values(0))
        sampleArrayList.append(values(1))
        //@todo 需要优化多种操作系统下的分割方式，
        for (str <- values(1).split("/", -1)) {
          //获取目录对应的类型
          val `type` = getType(str)
          if (!typeIndexMap.contains(`type`)) {
            typeIndexMap.put(`type`, typeIndexMap.size)
            indexTypeMap.put(indexTypeMap.size, `type`)
          }
        }
        line = bufferedReader.readLine()
      }

      val samples = new Array[Array[Int]](sampleArrayList.size)
      val targets = new Array[String](sampleArrayList.size)
      val samplesStr = new Array[String](sampleArrayList.size)

      for (i <- sampleArrayList.indices) {
        val sampleStr = sampleArrayList(i)

        samples(i) = getSample(sampleStr, typeIndexMap, indexTypeMap)
        targets(i) = targetArrayList(i)
        samplesStr(i) = sampleArrayList(i) + "|" + targetArrayList(i)

      }

      val trainTestSplit = MlUtil.trainTestSplit(samples, targets, 0.5, samplesStr)
      decisionTreeNode = CART.fit(trainTestSplit.trainSamples, trainTestSplit.trainTarget)

      targetArrayList.clear()
      sampleArrayList.clear()
    }


    override def processElement(value: String, ctx: ProcessFunction[String, ((Object, Boolean),String)
    ]#Context, out: Collector[((Object, Boolean), String)]): Unit = {


      val operationModelOption = OperationModel.getOperationModel(value)
      if (operationModelOption.isDefined) {
        val operationModel = operationModelOption.head
        val url = OperationModel.getUrl(value)
        if (isDirectoryTraversal(getUrlPath(url), decisionTreeNode, typeIndexMap, indexTypeMap)) {
          val bbasDirectoryTraversalWarnEntity = new BbasDirectoryTraversalWarnEntity()
          bbasDirectoryTraversalWarnEntity.setWarnDatetime(new Timestamp(operationModel.timeStamp))
          bbasDirectoryTraversalWarnEntity.setUsername(operationModel.userName)
          bbasDirectoryTraversalWarnEntity.setLoginSystem(operationModel.loginSystem)
          bbasDirectoryTraversalWarnEntity.setDestinationIp(operationModel.destinationIp)
          bbasDirectoryTraversalWarnEntity.setLoginPlace(operationModel.loginPlace)
          bbasDirectoryTraversalWarnEntity.setSourceIp(operationModel.sourceIp)
          bbasDirectoryTraversalWarnEntity.setUrl(url)
          bbasDirectoryTraversalWarnEntity.setHttpStatus(operationModel.httpStatus)
          val inputKafkaValue = operationModel.userName + "|" + "目录遍历告警" + "|" + operationModel.timeStamp + "|" +
            "" + "|" + operationModel.loginSystem + "|" + "" + "|" +
            "" + "|" + operationModel.sourceIp + "|" + "" + "|" +
            operationModel.destinationIp + "|" + "" + "|" + url + "|" +
            operationModel.httpStatus + "|" + "" + "|" + ""
          out.collect((bbasDirectoryTraversalWarnEntity.asInstanceOf[Object], false),inputKafkaValue)

        }

      }

    }

    /**
     * @todo
     *       之后需要优化
     * 获取url路径的部分
     * @param url
     * @return
     */
    def getUrlPath(url:String): String = {
      val values = url.split("/", 4)
      values(values.length - 1).split("\\?", -1)(0)
    }



    /**
     * 判断是否为目录遍历
     *
     * @param url
     * @param decisionTreeNode
     * @param typeIndexMap
     * @param indexTypeMap
     * @return
     */
    def isDirectoryTraversal(url: String, decisionTreeNode: DecisionTreeNode,
                             typeIndexMap: mutable.Map[String, Integer],
                             indexTypeMap: mutable.Map[Integer, String]): Boolean = {
      val result = CART.predict(decisionTreeNode, getSample(url, typeIndexMap, indexTypeMap))
      "2".equals(result)
    }

    def getSample(sampleStr: String, typeIndexMap: mutable.Map[String, Integer],
                  indexTypeMap: mutable.Map[Integer, String]): Array[Int] = {

      val sample = new Array[Int](typeIndexMap.size + 2)

      if (sampleStr != null && sampleStr.length > 0) {

        val partStr = sampleStr.split("/", -1)
        for (str <- partStr) {
          val `type` = getType(str)
          if (typeIndexMap.contains(`type`)) {
            sample(typeIndexMap(`type`)) += 1
          } else {
            sample(typeIndexMap.size) += 1
          }
        }

        sample(typeIndexMap.size + 1) = partStr.length
      }

      sample
    }

    /**
     * @todo
     *       之后根据数据进行优化
     * 获取类型
     *
     * @param string
     * @return
     */
    def getType(string: String): String = {

      //字母的个数
      var letterCount = 0L
      //数字的个数
      var digitCount = 0L
      //符号的个数
      var symbolCount = 0L
      //非Ascii码符号的个数
      var otherSymbolCount = 0L

      //如果为空返回空字符串
      if (string == null || string.length == 0) {
        return ""
      }

      //只保留ascii符号
      val stringBuilder = new mutable.StringBuilder()
      for (ch <- string.toCharArray) {
        ch.isLetterOrDigit
        if (ch.isDigit) {
          digitCount += 1
          stringBuilder.append(ch)
        } else if (ch.isLetter) {
          letterCount += 1
          stringBuilder.append(ch)
        } else if (0 < ch && ch < 128) {
          symbolCount += 1
          stringBuilder.append(ch)
        } else {
          otherSymbolCount += 1
        }
      }

      //如果符号的个数为0
      if (symbolCount == 0) {
        //只包含英文的返回"a"
        if (letterCount == string.length) {
          "a"
          //只包含数字的返回"0"
        } else if (digitCount == string.length) {
          "0"
          //只包含非ascii码的
        } else if (otherSymbolCount == string.length) {
          "otherSymbol"
          //只包含英文和数字的
        } else if (letterCount + digitCount == string.length) {
          "letterAndDigit"
        } else {
          "other"
        }
      } else {
        //考虑到可能返回的数据量过大的问题，只返回前5个字符
        if (stringBuilder.length > 5) {
          stringBuilder.substring(0, 5)
        } else {
          stringBuilder.toString()
        }
      }

    }
  }

}