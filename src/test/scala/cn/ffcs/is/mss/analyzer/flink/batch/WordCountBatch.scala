package cn.ffcs.is.mss.analyzer.flink.batch

import org.apache.flink.api.scala.ExecutionEnvironment
import org.apache.flink.api.scala._

/**
 * @ClassName:
 * @Author: mengwenting
 * @Date: 2023/5/30 10:09
 * @Description:
 * @update:
 */
object WordCountBatch {
  def main(args: Array[String]) {
    //批处理程序，需要创建ExecutionEnvironment
    val env = ExecutionEnvironment.getExecutionEnvironment
    //fromElements(elements:_*) --- 从一个给定的对象序列中创建一个数据流，所有的对象必须是相同类型的。
    val text = env.fromElements(
      "Who's there?",
      "I think I hear them. Stand, oh! Who's there?","hah")

    val counts = text.flatMap { _.toLowerCase.split("\\W+") filter { _.nonEmpty } }
      .map { (_, 1) }
      .groupBy(0)//根据第一个元素分组
      .sum(1)

    //打印
    counts.print()
  }

}
