package scala.cn.ffcs.is.mss.analyzer.flink

import java.sql.Timestamp
import java.util.Properties

import cn.ffcs.is.mss.analyzer.bean.PermeateSoftwareFlowWarnEntity
import cn.ffcs.is.mss.analyzer.flink.sink.MySQLSink
import cn.ffcs.is.mss.analyzer.flink.webbuganalyse.utils.EnglishOrCode
import cn.ffcs.is.mss.analyzer.utils.{Constants, IniProperties, KeLaiTimeUtils}
import org.apache.flink.api.common.functions.{RichFlatMapFunction, RichMapFunction}
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.functions.{AssignerWithPunctuatedWatermarks, ProcessFunction}
import org.apache.flink.streaming.api.scala.{StreamExecutionEnvironment, _}
import org.apache.flink.streaming.api.watermark.Watermark
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaConsumer, FlinkKafkaProducer}
import org.apache.flink.streaming.util.serialization.SimpleStringSchema
import org.apache.flink.util.Collector
import org.json.{JSONArray, JSONException, JSONObject}

import scala.collection.JavaConversions._
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.util.control.Breaks._
/**
 * @ClassName:
 * @Author: mengwenting
 * @Date: 2023/4/13 16:19
 * @Description:
 * @update:
 */
object ArrayBufferTest {
  def main(args: Array[String]): Unit = {
    val outerBuffer = new ArrayBuffer[ArrayBuffer[String]]
    val buffer1 = new ArrayBuffer[String]()
//    buffer1.append(null)
//    buffer1.append("")
//    buffer1.append("")
    if (buffer1.nonEmpty){
      println("buffer1不为空")
    }else{
      println("buffer1为空")
    }
    val buffer2 = new ArrayBuffer[String]()
    buffer2.append("AA")
    buffer2.append("BB")
    buffer2.append("CC")
    outerBuffer.+=(buffer1)
    val buffer = outerBuffer.+=(buffer2)
    println(buffer)
    var count = 0
    for(buffer <- outerBuffer){
      if (buffer.nonEmpty){
        count += 1
      }
    }
    println(count)
    println(outerBuffer.size)
    println(outerBuffer(1).isEmpty)
    println(outerBuffer(1).toString())
    println(outerBuffer(1).toString().substring(12, outerBuffer(1).toString.length - 1))
  }
}
