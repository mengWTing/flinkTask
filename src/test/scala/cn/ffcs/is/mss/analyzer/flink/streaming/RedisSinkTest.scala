package cn.ffcs.is.mss.analyzer.flink.streaming

import java.io.{BufferedReader, InputStreamReader}
import java.net.URI
import java.util.Properties

import cn.ffcs.is.mss.analyzer.utils.{Constants, IniProperties, JedisUtil}
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.ProcessFunction
import org.apache.flink.streaming.api.scala.{StreamExecutionEnvironment, _}
import org.apache.flink.util.Collector
import org.apache.hadoop.fs.{FileSystem, Path}
import redis.clients.jedis.{Jedis, JedisPool, Pipeline}

import scala.collection.mutable.ArrayBuffer

/**
 * @ClassName:
 * @Author: mengwenting
 * @Date: 2023/8/3 16:15
 * @Description:
 * @update:
 */
object RedisSinkTest {
  def main(args: Array[String]): Unit = {
    //根据传入的参数解析配置文件
    val args0 = "G:\\ffcs\\4_Code\\mss\\src\\main\\resources\\flink.ini"
    val confProperties = new IniProperties(args0)
    //    val confProperties = new IniProperties(args`(0))
    //任务的名字
    val jobName = confProperties.getValue(Constants.PACKAGE_ANALYZE_TO_REDIS_CONFIG, Constants
      .PACKAGE_ANALYZE_TO_REDIS_JOB_NAME)

    //并行度
    val sourceParallelism = confProperties.getIntValue(Constants.PACKAGE_ANALYZE_TO_REDIS_CONFIG,
      Constants.PACKAGE_ANALYZE_TO_REDIS_SOURCE_PARALLELISM)
    val dealParallelism = confProperties.getIntValue(Constants.PACKAGE_ANALYZE_TO_REDIS_CONFIG,
      Constants.PACKAGE_ANALYZE_TO_REDIS_DEAL_PARALLELISM)
    val sinkParallelism = confProperties.getIntValue(Constants.PACKAGE_ANALYZE_TO_REDIS_CONFIG,
      Constants.PACKAGE_ANALYZE_TO_REDIS_SINK_PARALLELISM)

    //check pointing的间隔
    val checkpointInterval = confProperties.getLongValue(Constants.PACKAGE_ANALYZE_TO_REDIS_CONFIG,
      Constants.PACKAGE_ANALYZE_TO_REDIS_CHECKPOINT_INTERVAL)

    //flink全局变量
    val parameters: Configuration = new Configuration()
    //配置文件系统类型
    parameters.setString(Constants.FILE_SYSTEM_TYPE, confProperties.getValue(Constants
      .FLINK_COMMON_CONFIG, Constants.FILE_SYSTEM_TYPE))
    parameters.setString(Constants.REDIS_PACKAGE_PROPERTIES, confProperties.getValue(Constants
      .FLINK_COMMON_CONFIG, Constants.REDIS_PACKAGE_PROPERTIES))
    parameters.setInteger(Constants.PACKAGE_ANALYZE_TO_REDIS_DATA_TTL, confProperties.getIntValue(Constants
      .PACKAGE_ANALYZE_TO_REDIS_CONFIG, Constants.PACKAGE_ANALYZE_TO_REDIS_DATA_TTL))
    parameters.setInteger(Constants.PACKAGE_ANALYZE_TO_REDIS_DATA_COUNT, confProperties.getIntValue(Constants
      .PACKAGE_ANALYZE_TO_REDIS_CONFIG, Constants.PACKAGE_ANALYZE_TO_REDIS_DATA_COUNT))


//    //设置kafka消费者相关配置
//    val props = new Properties()
//    //设置kafka集群地址
//    props.setProperty("bootstrap.servers", brokerList)
//    //设置flink消费的group.id
//    props.setProperty("group.id", groupId)
//    //获取kafka消费者
//    val consumer = new FlinkKafkaConsumer[String](topic, new SimpleStringSchema, props)
//      .setStartFromGroupOffsets()

    //获取ExecutionEnvironment
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    //设置check pointing的间隔
    //env.enableCheckpointing(checkpointInterval)
    //设置flink全局变量
    env.getConfig.setGlobalJobParameters(parameters)
    //    val builder = new FlinkJedisPoolConfig.Builder().setHost("10.142.82.184").setPort(6379).setPassword("DG#YURFD#@").build()
    //获取数据流keyAndValue.length == 2 && keyAndValue(0).contains("txt")
//    val dStream = env.addSource(consumer).setParallelism(sourceParallelism)
    val dStream = env.socketTextStream("192.168.1.23", 8888)
    dStream
      //      .process(new DataToArrayFuncation())
      //
      //      .addSink(new RedisSink[Array[String]](builder, new RedisSinkExample()))
      //      .filter(_.length < 5000).setParallelism(sourceParallelism)
      .process(new PackageAnalizeToRedisFuncation()).setParallelism(sinkParallelism)
    //      .map(_.toString).setParallelism(dealParallelism)
      .print()

    env.execute(jobName)
  }

  class PackageAnalizeToRedisFuncation extends ProcessFunction[String, Object] {
    var jedisPool: JedisPool = _
    var jedis: Jedis = _
    var messageArray = new ArrayBuffer[Array[String]]()
    var redisInPutNum: Int = 0
    var redisInPutTTL: Int = 0
    var pipeline: Pipeline = _

    override def open(parameters: Configuration): Unit = {
      val globConf = getRuntimeContext.getExecutionConfig.getGlobalJobParameters
        .asInstanceOf[Configuration]
      //online 配置
      //根据redis配置文件,初始化redis连接池
//      val redisProperties = new Properties()
//      val jedisConfigPath = globConf.getString(Constants.REDIS_PACKAGE_PROPERTIES, "")
//      val fileSystemType = globConf.getString(Constants.FILE_SYSTEM_TYPE, "/")
//      val fs = FileSystem.get(URI.create(fileSystemType), new org.apache.hadoop.conf
//      .Configuration())
//      val fsDataInputStream = fs.open(new Path(jedisConfigPath))
//      val bufferedReader = new BufferedReader(new InputStreamReader(fsDataInputStream))
//      redisProperties.load(bufferedReader)

        //本地测试
            val redisProperties = new Properties()
            val fs = FileSystem.get(URI.create("file:///"), new org.apache.hadoop.conf.Configuration())
//            val fsDataInputStream = fs.open(new Path("G:\\福富Flink\\mss\\src\\main\\resources\\redis.properties"))
            val fsDataInputStream = fs.open(new Path("G:\\ffcs\\4_Code\\redispackage.properties"))
            val bufferedReader = new BufferedReader(new InputStreamReader(fsDataInputStream))
            redisProperties.load(bufferedReader)

      jedisPool = JedisUtil.getJedisPool(redisProperties)
      jedis = jedisPool.getResource

    }

    override def processElement(value: String, ctx: ProcessFunction[String, Object]#Context,
                                out: Collector[Object]): Unit = {
      val globalConf = getRuntimeContext.getExecutionConfig.getGlobalJobParameters.asInstanceOf[Configuration]
      redisInPutNum = globalConf.getInteger(Constants.PACKAGE_ANALYZE_TO_REDIS_DATA_COUNT, 500)
      redisInPutTTL = globalConf.getInteger(Constants.PACKAGE_ANALYZE_TO_REDIS_DATA_TTL, 180)
//      redisInPutNum = 1
//      redisInPutTTL = 1000

      val keyAndValue: Array[String] = value.split("\\|", 2)
      //      if (keyAndValue.length == 2) {
      //        jedis.set(keyAndValue(0), keyAndValue(1), "NX", "EX", redisInPutTTL)
      //        out.collect("123")
      //      }
      //
      //    }

      if (messageArray.length < redisInPutNum && keyAndValue.length == 2 && keyAndValue(0).contains("txt")) {
        messageArray.append(keyAndValue)
      } else {
        pipeline = jedis.pipelined()
        for (i <- messageArray) {
          //          pipeline.hset("package", i(0), i(1))
          //          pipeline.expire("package",redisInPutTime)
          pipeline.set(i(0), i(1), "NX", "EX", redisInPutTTL)
        }
        // pipeline.syncAndReturnAll()
        pipeline.sync()
        messageArray.clear()
        //        jedis.disconnect()
        val str = jedis.get("MSSnaplog-00-jituan-20230803171635_2111.txt")
        out.collect(str)
//        out.collect(messageArray)

      }

    }

  }
}
