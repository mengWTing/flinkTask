package scala.cn.ffcs.is.mss.analyzer.flink

import java.net.URLDecoder
import org.apache.flink.api.scala._
import java.net.{URI, URLDecoder}


import cn.ffcs.is.mss.analyzer.druid.model.scala.OperationModel
import com.twitter.logging.config.BareFormatterConfig.intoOption
import cn.ffcs.is.mss.analyzer.utils.{Constants, IniProperties}
import org.apache.flink.api.common.functions.{RichFlatMapFunction, RichMapFunction}
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.util.Collector

/**
 * @ClassName:
 * @Author: mengwenting
 * @Date: 2023/3/17 11:15
 * @Description:
 * @update:
 */
object UrlTest {
  def main(args: Array[String]): Unit = {
    val srcIpPortDesIp = "123-234-345"
    val strings = srcIpPortDesIp.split("-", -1)
    val sourceIp = strings(0)
    val sourcePort = strings(1)
    val destinationIp = strings(2)
    println(sourceIp)
    println(sourcePort)
    println(destinationIp)
  }
}
