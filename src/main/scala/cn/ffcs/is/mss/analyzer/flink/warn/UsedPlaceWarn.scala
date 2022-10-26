package cn.ffcs.is.mss.analyzer.flink.warn

import java.io.{BufferedReader, InputStreamReader}
import java.net.URI
import java.sql.Timestamp
import java.util.Properties

import cn.ffcs.is.mss.analyzer.bean.BbasUsedPlaceWarnEntity
import cn.ffcs.is.mss.analyzer.druid.model.scala.OperationModel
import cn.ffcs.is.mss.analyzer.flink.sink.MySQLSink
import cn.ffcs.is.mss.analyzer.utils.{Constants, IniProperties, JsonUtil}
import org.apache.flink.api.common.functions.RichMapFunction
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer
import org.apache.hadoop.fs.{FileSystem, Path}

import scala.collection.mutable


object UsedPlaceWarn {

  def main(args: Array[String]): Unit = {

//        val args0 = "/Users/chenwei/IdeaProjects/mss/src/main/resources/flink.ini"


    //根据传入的参数解析配置文件
//        val confProperties = new IniProperties(args0)
    val confProperties = new IniProperties(args(0))
    //该任务的名字
    val jobName = confProperties.getValue(Constants.FLINK_USED_PLACE_CONFIG, Constants.USED_PLACE_JOB_NAME)
    //kafka Source的名字
    val kafkaSourceName = confProperties.getValue(Constants.FLINK_USED_PLACE_CONFIG, Constants.USED_PLACE_KAFKA_SOURCE_NAME)
    //mysql sink的名字
    val sqlSinkName = confProperties.getValue(Constants.FLINK_USED_PLACE_CONFIG, Constants.USED_PLACE_SQL_SINK_NAME)

    //kafka Source的并行度
    val kafkaSourceParallelism = confProperties.getIntValue(Constants.FLINK_USED_PLACE_CONFIG, Constants.USED_PLACE_KAFKA_SOURCE_PARALLELISM)
    //对数据处理的并行度
    val dealParallelism = confProperties.getIntValue(Constants.FLINK_USED_PLACE_CONFIG, Constants.USED_PLACE_DEAL_PARALLELISM)
    //写入mysql的并行度
    val sqlSinkParallelism = confProperties.getIntValue(Constants.FLINK_USED_PLACE_CONFIG, Constants.USED_PLACE_SQL_SINK_PARALLELISM)

    //kafka的服务地址
    val brokerList = confProperties.getValue(Constants.FLINK_COMMON_CONFIG, Constants.KAFKA_BOOTSTRAP_SERVERS)
    //flink消费的group.id
    val groupId = confProperties.getValue(Constants.FLINK_USED_PLACE_CONFIG, Constants.USED_PLACE_GROUP_ID)
    //kafka的topic
    val topic = confProperties.getValue(Constants.OPERATION_FLINK_TO_DRUID_CONFIG, Constants.OPERATION_TO_KAFKA_TOPIC)


    //flink全局变量
    val parameters: Configuration = new Configuration()
    //日期配置文件的路径
    parameters.setString(Constants.DATE_CONFIG_PATH, confProperties.getValue(Constants.FLINK_COMMON_CONFIG, Constants.DATE_CONFIG_PATH))
    //c3p0连接池配置文件路径
    parameters.setString(Constants.c3p0_CONFIG_PATH, confProperties.getValue(Constants.FLINK_COMMON_CONFIG, Constants.c3p0_CONFIG_PATH))
    //check pointing的间隔
    val checkpointInterval = confProperties.getLongValue(Constants.FLINK_USED_PLACE_CONFIG, Constants.USED_PLACE_CHECKPOINT_INTERVAL)

    //druid的broker节点
    parameters.setString(Constants.DRUID_BROKER_HOST_PORT, confProperties.getValue(Constants.FLINK_COMMON_CONFIG, Constants.DRUID_BROKER_HOST_PORT))
    //druid时间格式
    parameters.setString(Constants.DRUID_TIME_FORMAT,confProperties.getValue(Constants.FLINK_COMMON_CONFIG,Constants.DRUID_TIME_FORMAT))
    //druid数据开始的时间
    parameters.setLong(Constants.DRUID_DATA_START_TIMESTAMP, confProperties.getLongValue(Constants.FLINK_COMMON_CONFIG, Constants.DRUID_DATA_START_TIMESTAMP))

    //五元组话单在druid的表名
    parameters.setString(Constants.DRUID_OPERATION_TABLE_NAME, confProperties.getValue(Constants.FLINK_COMMON_CONFIG, Constants.DRUID_OPERATION_TABLE_NAME))

    //获取ExecutionEnvironment
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    //设置check pointing的间隔
    env.enableCheckpointing(checkpointInterval)
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
    val consumer = new FlinkKafkaConsumer[String](topic, new SimpleStringSchema, props).setStartFromGroupOffsets()
    // 获取kafka数据
    val dStream = env.addSource(consumer).setParallelism(kafkaSourceParallelism)
      .uid(kafkaSourceName).name(kafkaSourceName)
//    var path = "/Users/chenwei/Downloads/mss.1525735052963.txt"
//    path = "/Users/chenwei/Downloads/14021053@HQ/14021053@HQ.txt"
//    val dStream = env.readTextFile(path, "iso-8859-1").setParallelism(1)


//        ip-地点关联文件路径
//    val placePath = confProperties.getValue(Constants.OPERATION_FLINK_TO_DRUID_CONFIG, Constants.OPERATION_PLACE_PATH)
//        host-系统名关联文件路径
//    val systemPath = confProperties.getValue(Constants.OPERATION_FLINK_TO_DRUID_CONFIG, Constants.OPERATION_SYSTEM_PATH)
//    用户名-常用登录地关联文件路径
//    val usedPlacePath = confProperties.getValue(Constants.OPERATION_FLINK_TO_DRUID_CONFIG, Constants.OPERATION_USEDPLACE_PATH)

//    OperationModel.setPlaceMap(placePath)
//    OperationModel.setSystemMap(systemPath)
//    OperationModel.setMajorMap(systemPath)
//    OperationModel.setUsedPlacesMap(usedPlacePath)


    val usedPlaceWarnStream = dStream
//            .map(OperationModel.getOperationModel _)
//            .filter(_.isDefined)
//            .map(_.head)
//            .map(JsonUtil.toJson(_))
      .map(JsonUtil.fromJson[OperationModel] _)
      .filter(operationModel => {
        !"未知地点".equals(operationModel.loginPlace) &&
        !"常用登录地不详".equals(operationModel.usedPlace) &&
        !operationModel.usedPlace.split("\\|",-1).toSet.contains(operationModel.loginSystem)
      })
      .map(new RichMapFunction[OperationModel,(Object,Boolean)]{

        val clusterDescribeMap: mutable.Map[Int,String] = mutable.Map()
        val clusterIdMap: mutable.Map[String,Int] = mutable.Map()

        override def open(parameters: Configuration): Unit = {
          //获取全局配置
          val globConf = getRuntimeContext.getExecutionConfig.getGlobalJobParameters.asInstanceOf[Configuration]

          //类描述文件路径
          val clusterDescribePath = globConf.getString(Constants.USED_PLACE_CLUSTER_DESCRIBE_PATH,"")
          //用户名对应的类id文件
          val clusterIdPath = globConf.getString(Constants.USED_PLACE_CLUSTER_ID_PATH,"")

          val clusterDescribeFileSystem = FileSystem.get(URI.create(clusterDescribePath), new org.apache.hadoop.conf.Configuration())
          val clusterDescribeFsDataInputStream = clusterDescribeFileSystem.open(new Path(clusterDescribePath))
          val clusterDescribeBufferedReader = new BufferedReader(new InputStreamReader(clusterDescribeFsDataInputStream))
          var line = clusterDescribeBufferedReader.readLine()
          while (line != null) {
            val values = line.split("\\|", 2)
            if (values.length == 2){
              clusterDescribeMap.put(values(0).toInt,values(1))
            }
            line = clusterDescribeBufferedReader.readLine()
          }

          clusterDescribeBufferedReader.close()
          clusterDescribeFsDataInputStream.close()
          clusterDescribeFileSystem.close()


          val clusterIdFileSystem = FileSystem.get(URI.create(clusterIdPath), new org.apache.hadoop.conf.Configuration())
          val clusterIdFsDataInputStream = clusterIdFileSystem.open(new Path(clusterIdPath))
          val clusterIdBufferedReader = new BufferedReader(new InputStreamReader(clusterIdFsDataInputStream))

          line = clusterIdBufferedReader.readLine()
          while (line != null) {
            val values = line.split("\\|", 2)
            if (values.length == 2){
              clusterIdMap.put(values(0),values(1).toInt)
            }
            line = clusterIdBufferedReader.readLine()
          }

          clusterIdBufferedReader.close()
          clusterIdFsDataInputStream.close()
          clusterIdFileSystem.close()
        }

        override def map(value: OperationModel): (Object,Boolean) = {
          val bbasUsedPlaceWarnEntity = new BbasUsedPlaceWarnEntity()
          val clusterId = clusterIdMap.getOrElse(value.userName,-1)
          bbasUsedPlaceWarnEntity.setClusterId(clusterId)
          bbasUsedPlaceWarnEntity.setClusterDescribe(clusterDescribeMap.getOrElse(clusterId,""))
          bbasUsedPlaceWarnEntity.setDestinationIp(value.destinationIp)
          bbasUsedPlaceWarnEntity.setLoginPlace(value.loginPlace)
          bbasUsedPlaceWarnEntity.setLoginSystem(value.loginSystem)
          bbasUsedPlaceWarnEntity.setOperation(value.operate)
          bbasUsedPlaceWarnEntity.setSourceIp(value.sourceIp)
          bbasUsedPlaceWarnEntity.setUsedLoginPlace(value.usedPlace)
          bbasUsedPlaceWarnEntity.setUserName(value.userName)
          bbasUsedPlaceWarnEntity.setWarnDatetime(new Timestamp(value.timeStamp))
          (bbasUsedPlaceWarnEntity.asInstanceOf[Object],true)
        }
      })

    usedPlaceWarnStream.addSink(new MySQLSink).uid(sqlSinkName).name(sqlSinkName)
      .setParallelism(sqlSinkParallelism)

    env.execute(jobName)

  }

}
