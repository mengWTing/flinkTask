package cn.ffcs.is.mss.analyzer.flink.indicators

import java.beans.PropertyDescriptor
import java.io.{BufferedReader, InputStreamReader}
import java.net.URI
import java.sql.Timestamp
import java.util.Properties

import cn.ffcs.is.mss.analyzer.bean.{BbasExtremelyActiveSystemEntity, _}
import cn.ffcs.is.mss.analyzer.druid.model.scala.OperationModel
import cn.ffcs.is.mss.analyzer.flink.sink.MySQLSink
import cn.ffcs.is.mss.analyzer.utils._
import cn.ffcs.is.mss.analyzer.utils.druid.entity._
import javax.persistence.Column
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.functions.{AssignerWithPunctuatedWatermarks, ProcessFunction}
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.watermark.Watermark
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaConsumer, FlinkKafkaProducer}
import org.apache.flink.streaming.util.serialization.SimpleStringSchema
import org.apache.flink.util.Collector
import org.apache.hadoop.fs.{FileSystem, Path}

import scala.collection.mutable
import scala.collection.JavaConversions._

/**
 * @Auther chenwei
 * @Description
 * @Date: Created in 2018/12/20 11:34
 * @Modified By
 */
object AbnormalSystem {

  def main(args: Array[String]): Unit = {

    //根据传入的参数解析配置文件
    //val args0 = "/Users/chenwei/IdeaProjects/mss/src/main/resources/flink.ini"
    //val confProperties = new IniProperties(args0)
    val confProperties = new IniProperties(args(0))

    //该任务的名字
    val jobName = confProperties.getValue(Constants.FLINK_ABNORMAL_SYSTEM_CONFIG, Constants
      .ABNORMAL_SYSTEM_JOB_NAME)
    //kafka Source的名字
    val kafkaSourceName = confProperties.getValue(Constants.FLINK_ABNORMAL_SYSTEM_CONFIG, Constants
      .ABNORMAL_SYSTEM_KAFKA_SOURCE_NAME)
    //mysql sink的名字
    val mysqlSinkName = confProperties.getValue(Constants.FLINK_ABNORMAL_SYSTEM_CONFIG, Constants
      .ABNORMAL_SYSTEM_MYSQL_SINK_NAME)
    //kafka sink 的名字
    val kafkaSinkName = confProperties.getValue(Constants.FLINK_ABNORMAL_SYSTEM_CONFIG,
      Constants.ABNORMAL_SYSTEM_KAFKA_SINK_NAME)

    //kafka Source的并行度
    val kafkaSourceParallelism = confProperties.getIntValue(Constants.FLINK_ABNORMAL_SYSTEM_CONFIG,
      Constants.ABNORMAL_SYSTEM_KAFKA_SOURCE_PARALLELISM)
    //kafka sink 的并行度
    val kafkaSinkParallelism = confProperties.getIntValue(Constants.FLINK_ABNORMAL_SYSTEM_CONFIG,
      Constants.ABNORMAL_SYSTEM_KAFKA_SINK_PARALLELISM)
    //对数据处理的并行度
    val dealParallelism = confProperties.getIntValue(Constants.FLINK_ABNORMAL_SYSTEM_CONFIG,
      Constants.ABNORMAL_SYSTEM_DEAL_PARALLELISM)
    //写入mysql的并行度
    val mysqlSinkParallelism = confProperties.getIntValue(Constants.FLINK_ABNORMAL_SYSTEM_CONFIG,
      Constants.ABNORMAL_SYSTEM_MYSQL_SINK_PARALLELISM)

    //kafka的服务地址
    val brokerList = confProperties.getValue(Constants.FLINK_COMMON_CONFIG, Constants
      .KAFKA_BOOTSTRAP_SERVERS)
    //flink消费的group.id
    val groupId = confProperties.getValue(Constants.FLINK_ABNORMAL_SYSTEM_CONFIG, Constants
      .ABNORMAL_SYSTEM_GROUP_ID)
    //kafka source 的topic
    val sourceTopic = confProperties.getValue(Constants.OPERATION_FLINK_TO_DRUID_CONFIG, Constants.OPERATION_TO_KAFKA_TOPIC)
    //kafka sink 的topic
    val sinkTopic = confProperties.getValue(Constants.FLINK_ABNORMAL_SYSTEM_CONFIG,Constants.ABNORMAL_SYSTEM_KAFKA_SINK_TOPIC)


    //flink全局变量
    val parameters: Configuration = new Configuration()
    //c3p0连接池配置文件路径
    parameters.setString(Constants.c3p0_CONFIG_PATH, confProperties.getValue(Constants.FLINK_COMMON_CONFIG, Constants.c3p0_CONFIG_PATH))
    //druid的broker节点
    parameters.setString(Constants.DRUID_BROKER_HOST_PORT, confProperties.getValue(Constants
      .FLINK_COMMON_CONFIG, Constants.DRUID_BROKER_HOST_PORT))
    //druid时间格式
    parameters.setString(Constants.DRUID_TIME_FORMAT, confProperties.getValue(Constants
      .FLINK_COMMON_CONFIG, Constants.DRUID_TIME_FORMAT))
    //druid数据开始的时间
    parameters.setLong(Constants.DRUID_DATA_START_TIMESTAMP, confProperties.getLongValue
    (Constants.FLINK_COMMON_CONFIG, Constants.DRUID_DATA_START_TIMESTAMP))
    //业务话单在druid中的表名
    parameters.setString(Constants.DRUID_OPERATION_TABLE_NAME, confProperties.getValue(Constants
      .FLINK_COMMON_CONFIG, Constants.DRUID_OPERATION_TABLE_NAME))
    parameters.setInteger(Constants.ABNORMAL_SYSTEM_TOP_K, confProperties.getIntValue(Constants
      .FLINK_ABNORMAL_SYSTEM_CONFIG, Constants.ABNORMAL_SYSTEM_TOP_K))


    //check pointing的间隔
    val checkpointInterval = confProperties.getLongValue(Constants.FLINK_ABNORMAL_SYSTEM_CONFIG,
      Constants.ABNORMAL_SYSTEM_CHECKPOINT_INTERVAL)

    //
    val waterMark = confProperties.getLongValue(Constants.FLINK_ABNORMAL_SYSTEM_CONFIG, Constants
      .ABNORMAL_SYSTEM_WATERMARK)
    //聚合窗口大小
    val windowSize = confProperties.getLongValue(Constants.FLINK_ABNORMAL_SYSTEM_CONFIG, Constants
      .ABNORMAL_SYSTEM_WINDOW_SIZE)
    //聚合滑动窗口大小
    val slideSize = confProperties.getLongValue(Constants.FLINK_ABNORMAL_SYSTEM_CONFIG, Constants
      .ABNORMAL_SYSTEM_SLIDE_SIZE)


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
    val consumer = new FlinkKafkaConsumer[String](sourceTopic, new SimpleStringSchema, props)
      .setStartFromGroupOffsets()

    //获取kafka生产者
    val producer = new FlinkKafkaProducer[String](brokerList, sinkTopic, new SimpleStringSchema())

    // 获取kafka数据
    val dStream = env.addSource(consumer).setParallelism(kafkaSourceParallelism)
      .uid(kafkaSourceName).name(kafkaSourceName)

    val abnormalSystemStream = dStream
      .map(tuple => {
        JsonUtil.fromJson[OperationModel](tuple).timeStamp
      })
      .assignTimestampsAndWatermarks(new AssignerWithPunctuatedWatermarks[Long] {
        override def checkAndGetNextWatermark(lastElement: Long, extractedTimestamp: Long)
        : Watermark =
          new Watermark(extractedTimestamp - waterMark)

        override def extractTimestamp(element: Long, previousElementTimestamp: Long): Long =
          element
      }).setParallelism(dealParallelism)
      .timeWindowAll(Time.milliseconds(windowSize), Time.milliseconds(slideSize))
      .reduce((o1, o2) => {
        if (o1 > o2) o1 else o2
      })
      .process(new AbnormalSystemProcessFunction)

    abnormalSystemStream.addSink(new MySQLSink)
      .uid(mysqlSinkName)
      .name(mysqlSinkName)
      .setParallelism(mysqlSinkParallelism)

    abnormalSystemStream.map(o => {
      JsonUtil.toJson(o._1.asInstanceOf[BbasExtremelyActiveSystemEntity])
    })
      .addSink(producer)
      .uid(kafkaSinkName)
      .name(kafkaSinkName)
      .setParallelism(kafkaSinkParallelism)



    env.execute(jobName)

  }

  class AbnormalSystemProcessFunction extends ProcessFunction[Long, (Object, Boolean)] {


    var sqlHelper: SQLHelper = _

    val classMap = mutable.Map[Class[_ <: Object], (String, String)]()

    var K = 10
    var tableName = ""

    override def open(parameters: Configuration): Unit = {

      //获取全局变量
      val globConf = getRuntimeContext.getExecutionConfig.getGlobalJobParameters.asInstanceOf[Configuration]

      //根据c3p0配置文件,初始化c3p0连接池
      val c3p0Properties = new Properties()
      val c3p0ConfigPath = globConf.getString(Constants.c3p0_CONFIG_PATH, "")
      val fs = FileSystem.get(URI.create(c3p0ConfigPath), new org.apache.hadoop.conf.Configuration())
      val fsDataInputStream = fs.open(new Path(c3p0ConfigPath))
      val bufferedReader = new BufferedReader(new InputStreamReader(fsDataInputStream))
      c3p0Properties.load(bufferedReader)
      C3P0Util.ini(c3p0Properties)

      //操作数据库的类
      sqlHelper = new SQLHelper()

      DruidUtil.setDruidHostPortSet(globConf.getString(Constants.DRUID_BROKER_HOST_PORT, ""))
      DruidUtil.setTimeFormat(globConf.getString(Constants.DRUID_TIME_FORMAT, ""))
      DruidUtil.setDateStartTimeStamp(globConf.getLong(Constants.DRUID_DATA_START_TIMESTAMP, 0L))

      tableName = globConf.getString(Constants.DRUID_OPERATION_TABLE_NAME, "")
      classMap.put(classOf[BbasSqlInjectionWarnEntity], ("WARN_DATETIME", "LOGIN_SYSTEM"))
      classMap.put(classOf[BbasXssInjectionWarnEntity], ("WARN_DATETIME", "LOGIN_SYSTEM"))
      classMap.put(classOf[BbasOperationPersonnelDownloadEntity], ("WARN_DATETIME", "LOGIN_SYSTEM"))
      classMap.put(classOf[BbasSingleSystemConncountWarnEntity], ("WARN_DATETIME", "SYSTEM_NAME"))
      classMap.put(classOf[BbasSingleSystemUsercountWarnEntity], ("WARN_DATETIME", "SYSTEM_NAME"))
      classMap.put(classOf[BbasSingleSystemOctetsWarnEntity], ("WARN_DATETIME", "SYSTEM_NAME"))
      K = globConf.getInteger(Constants.ABNORMAL_SYSTEM_TOP_K, 10)

    }

    implicit def reflector(ref: AnyRef) = new {
      def getV(name: String): Any = {

        var result: Object = null
        try {

          //获取类型
          val clazz = ref.getClass

          //获取所有字段,根据在数据库的字段名过滤出字段
          val fields = clazz.getDeclaredFields.find(field => {
            //获取get方法
            val propertyDescriptor = new PropertyDescriptor(field.getName, clazz)
            val method = propertyDescriptor.getReadMethod
            //获取Column注解然后获取数据库的字段名
            val annotation = method.getAnnotation(classOf[Column])
            annotation != null && annotation.name().equals(name)
          })

          //根据字段获取其值
          if (fields.nonEmpty) {
            val propertyDescriptor = new PropertyDescriptor(fields.head.getName, clazz)
            val method = propertyDescriptor.getReadMethod
            result = method.invoke(ref)
          }
        } catch {
          case e: Exception =>
        }
        return result

      }

      def setV(name: String, value: Any): Unit = {

        try {

          //获取类型
          val clazz = ref.getClass

          //获取所有字段,根据在数据库的字段名过滤出字段
          val fields = clazz.getDeclaredFields.find(field => {
            //获取get方法
            val propertyDescriptor = new PropertyDescriptor(field.getName, clazz)
            val method = propertyDescriptor.getReadMethod
            //获取Column注解然后获取数据库的字段名
            val annotation = method.getAnnotation(classOf[Column])
            annotation != null && annotation.name().equals(name)
          })

          //根据字段获取其set方法
          if (fields.nonEmpty) {
            val propertyDescriptor = new PropertyDescriptor(fields.head.getName, clazz)
            val method = propertyDescriptor.getWriteMethod

            method.invoke(ref, value.asInstanceOf[Object])
          }
        } catch {
          case e: Exception => e.printStackTrace()
        }

      }

    }

    override def processElement(value: Long, ctx: ProcessFunction[Long, (Object, Boolean)
    ]#Context, out: Collector[(Object, Boolean)]): Unit = {

      val time = value / TimeUtil.MINUTE_MILLISECOND * TimeUtil.MINUTE_MILLISECOND - TimeUtil.MINUTE_MILLISECOND

      val abnormalSystemMap = mutable.Map[String, Double]()
      for ((clazz, (whereFiledName, systemFiledName)) <- classMap) {

        val list = sqlHelper.query(clazz, getWhereSql(time, whereFiledName))
        if (list != null && list.size() > 0) {
          for (o <- list) {
            val systemName = o.getV(systemFiledName).toString
            if ("未知系统".equals(systemName)) {
              abnormalSystemMap.put(systemName, abnormalSystemMap.getOrElse(systemName, 0.0) + K)
            }

          }

        }

      }


      if (abnormalSystemMap.size < K) {

        val list = DruidUtil.query(getEntity(time, K, tableName))
        for (i <- 0 until list.size()) {
          val systemName = list.get(i).get(Dimension.loginSystem.toString)
          abnormalSystemMap.put(systemName, abnormalSystemMap.getOrElse(systemName, 0.0) + (list.size() - i))
        }

      }


      abnormalSystemMap.toList.sortBy(_._2).take(10).foreach(tuple => {

        val systemName = tuple._1
        val value = tuple._2
        val bbasExtremelyActiveSystemEntity = new BbasExtremelyActiveSystemEntity
        bbasExtremelyActiveSystemEntity.setSystemName(systemName)
        bbasExtremelyActiveSystemEntity.setActiveDatetime(new Timestamp(time - TimeUtil.MINUTE_MILLISECOND))
        bbasExtremelyActiveSystemEntity.setInformation(value.toString)
//        bbasExtremelyActiveSystemEntity.setSourceIp()
//        bbasExtremelyActiveSystemEntity.setDestIp()
        out.collect((bbasExtremelyActiveSystemEntity, true))
      })


    }


    /**
     * @author chenwei
     *         date:  Created in 2018/12/20 10:47
     *         description 获取查询druid的entity
     * @param time
     * @param limit
     * @param tableName
     * @return
     */
    def getEntity(time: Long, limit: Int, tableName: String): Entity = {

      val entity = new Entity()
      entity.setDimensionsSet(Dimension.getDimensionsSet(Dimension.loginSystem))
      entity.setAggregationsSet(Aggregation.getAggregationsSet(Aggregation.userNameCount))
      entity.setLimitSpec(LimitSpec.getLimitSpec(Aggregation.userNameCount, LimitSpec.Direction.descending, LimitSpec.DimensionOrder.numeric, limit))
      entity.setStartTimeStr(time - TimeUtil.MINUTE_MILLISECOND)
      entity.setEndTimeStr(time)
      entity.setTableName(tableName)
      entity.setGranularity(Granularity.getGranularity(Granularity.periodMinute, 1))
      entity.setFilter(Filter.getFilter(Filter.not, Filter.getFilter(Filter.selector, Dimension.loginSystem, "未知系统")))
      entity
    }


    /**
     * @author chenwei
     *         date:  Created in 2018/12/19 13:33
     *         description 获取时间
     *         取当前分钟开始的时间戳，减去一分钟
     * @param time
     * @return
     */
    def getTime(time: Long): Long = {
      time / TimeUtil.MINUTE_MILLISECOND * TimeUtil.MINUTE_MILLISECOND - TimeUtil.MINUTE_MILLISECOND
    }

    /**
     * @author chenwei
     *         date:  Created in 2018/12/19 13:34
     *         description 根据条件凭借where语句
     * @param time
     * @param filedName
     * @return
     */
    def getWhereSql(time: Long, filedName: String): String = {

      val stringBuilder = new StringBuilder
      stringBuilder.append("WHERE '")
      stringBuilder.append(new Timestamp(time - TimeUtil.MINUTE_MILLISECOND))
      stringBuilder.append("' <= ")
      stringBuilder.append(filedName)
      stringBuilder.append(" AND ")
      stringBuilder.append(filedName)
      stringBuilder.append(" < '")
      stringBuilder.append(new Timestamp(time))
      stringBuilder.append("'")


      stringBuilder.toString()
    }
  }

}
