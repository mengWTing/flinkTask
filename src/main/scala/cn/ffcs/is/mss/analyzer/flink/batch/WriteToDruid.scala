package cn.ffcs.is.mss.analyzer.flink.batch

import java.util.Properties

import cn.ffcs.is.mss.analyzer.druid.model.scala.MailModel
import cn.ffcs.is.mss.analyzer.flink.druid.MailModelBeamFactory
import cn.ffcs.is.mss.analyzer.utils.{Constants, IniProperties}
import com.metamx.tranquility.flink.BeamSink
import org.apache.flink.api.common.io.FilePathFilter
import org.apache.flink.api.java.io.TextInputFormat
import org.apache.flink.core.fs.Path
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaConsumer, FlinkKafkaProducer}
import org.apache.flink.streaming.util.serialization.SimpleStringSchema
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.flink.streaming.api.scala._
import scala.collection.mutable

/**
  * @title WriteToDruid
  * @author liangzhaosuo
  * @date 2020-11-25 11:18
  * @description
  * @update [no][date YYYY-MM-DD][name][description]
  */


object WriteToDruid {
  def main(args: Array[String]): Unit = {

//    val args0 = "E:\\ffcs\\mss\\src\\main\\resources\\flink.ini"
//    val confProperties = new IniProperties(args0)

        val confProperties = new IniProperties(args(0))

    //druid的zk地址
    val druidZk = confProperties.getValue(Constants.FLINK_COMMON_CONFIG, Constants.TRANQUILITY_ZK_CONNECT)

    //该任务的名字
    val jobName = confProperties.getValue(Constants.MAIL_FLINK_TO_DRUID_CONFIG, Constants.MAIL_JOB_NAME)

    //tranquility sink的名字
    val tranquilitySinkName = confProperties.getValue(Constants.MAIL_FLINK_TO_DRUID_CONFIG, Constants
      .MAIL_TRANQUILITY_SINK_NAME)

    //该话单在druid的表名
    val tableName = confProperties.getValue(Constants.FLINK_COMMON_CONFIG, Constants.DRUID_MAIL_TABLE_NAME)


    //设置写入druid时需要的配置
    val conf: mutable.HashMap[String, String] = mutable.HashMap[String, String]()
    //设置话单的表名
    conf("druid.mail.source") = tableName
    //设置druid集群zookeeper集群的地址
    conf("tranquility.zk.connect") = druidZk


    val env = StreamExecutionEnvironment.getExecutionEnvironment
    //    val dStream = env.addSource(consumer).name(kafkaSourceName).uid(kafkaSourceName).setParallelism
    // (dealParallelism)
        val filePath = "hdfs://10.142.82.181:8020/eMailSecurityShuJu/2020-11-12--17/_part-0-0.pending"
//        val filePath = "hdfs://10.142.82.181:8020/testData.txt"

//    val filePath = "E:\\ffcs\\mss\\src\\main\\scala\\cn\\ffcs\\is\\mss\\analyzer\\flink\\batch\\data"
    val fileInputFormat = new TextInputFormat(new Path(filePath))
    fileInputFormat.setNestedFileEnumeration(true)

    //    fileInputFormat.setFilesFilter(new FilePathFilter {
    //      override def filterPath(filePath: Path): Boolean = {
    //        //如果是目录不过滤
    ////        if ("^2020.*".r.findAllIn(filePath.getName).nonEmpty) {
    ////          //          val time = filePath.getName.substring(5, 13).toLong
    ////          // && time <= 20190910
    ////          //          if (!(time >= 20190401 && time <= 20190430)) {
    ////          //            println(filePath)
    ////          //            true
    ////          //          } else {
    ////          //            false
    ////          //          }
    ////          println(filePath)
    ////          return false
    ////        } else {
    ////          //如果是.txt文件
    //////          if (".pending$".r.findAllIn(filePath.getName).nonEmpty) {
    //////            false
    //////          } else {
    //////            true
    //////          }
    ////          false
    ////        }
    //        false
    //      }
    //    })


    val dStream = env.readFile(fileInputFormat, filePath).setParallelism(1)
    val mailModelStream = dStream.map(MailModel.getMailModel _).setParallelism(1)
      .filter(_.isDefined).setParallelism(1)
      .map(_.head).setParallelism(1)


    //发送至druid
    mailModelStream.addSink(new BeamSink[MailModel](new MailModelBeamFactory(conf)))
      .name(tranquilitySinkName).uid(tranquilitySinkName).setParallelism(1)

    //发送至kafka
    //    mailModelStream.map(JsonUtil.toJson(_)).addSink(producer).uid(kafkaSinkName).name(kafkaSinkName)
    //      .setParallelism(3)

    env.execute(jobName)
  }
}
