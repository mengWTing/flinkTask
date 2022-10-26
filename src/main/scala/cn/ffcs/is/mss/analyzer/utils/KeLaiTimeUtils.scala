package cn.ffcs.is.mss.analyzer.utils

/**
 * @title KeLaiTimeUtils
 * @author ZF
 * @date 2020-08-19 16:24
 * @description
 * @update [no][date YYYY-MM-DD][name][description]
 */
object KeLaiTimeUtils {

  def getKeLaiTime(in: String): Long = {
    val timeLength = in.length
    var returnTIme = 0L
    if (timeLength < 20 && timeLength > 10) {
      if (timeLength == 10) {
        returnTIme = in.toLong * 1000
      } else if (timeLength == 13) {
        returnTIme = in.toLong
      } else if (timeLength == 19) {
        returnTIme = in.toLong / 1000000
      }
    }
    returnTIme
  }

  def getKeLaiTimeDefault(in: String, in2: Long): Long = {
    var returnTIme = 0L
    val inTime = getKeLaiTime(in)
    if (inTime == 0) {
      returnTIme = in2
    } else {
      returnTIme = inTime
    }
    returnTIme
  }
}
