package scala.cn.ffcs.is.mss.analyzer.flink

import scala.collection.immutable.HashSet
import scala.collection.mutable

/**
 * @ClassName:
 * @Author: mengwenting
 * @Date: 2023/4/10 17:03
 * @Description:
 * @update:
 */
object HashSetTest {
  def main(args: Array[String]): Unit = {
    var nameSet= new mutable.HashSet[String]()
    val name1: String = "zihao"
//    val name2: String = "xizihao"
//    val name3: String = "xiaoxiaozihao"
    val allNameSet = nameSet.+=(name1)
    var placeSet = new mutable.HashSet[String]
    val place1 = "beijing"
//    val place2 = "shanghai"
//    val place3 = "hangzhou"
    val allPlaceSet = placeSet.+=(place1)
    val set = allNameSet.++(allPlaceSet)
    println(set)
  }
}
