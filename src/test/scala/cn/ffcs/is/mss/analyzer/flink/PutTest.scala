package scala.cn.ffcs.is.mss.analyzer.flink

import java.net.URLDecoder


/**
 * @ClassName:
 * @Author: mengwenting
 * @Date: 2023/3/14 14:34
 * @Description:
 * @update:
 */
object PutTest {
  def main(args: Array[String]): Unit = {

    val url = "http://gcfz-gx.mss.ctc.com/msscpmis//cgi-bin/mainfunction.cgi/index_newmenu.js?1669685716009"
    val urlValueDec = URLDecoder.decode(url.replaceAll("%(?![0-9a-fA-F]{2})", "%25"), "utf-8")
    println(urlValueDec)


//    val pathValue: Array[String] = flagPath.split("\\^", -1)
//    val pathLength = pathValue.length
//    val builder = new StringBuilder
//    var matchSubStr : Int = 0
//    for (i <- 0 until(pathLength)){
//      if (url.contains(pathValue(i))){
//        matchSubStr += 1
//      }
//    }
//    if (matchSubStr >= 2){
//      builder.append(url)
//    }
    //println(builder)
  }
}
