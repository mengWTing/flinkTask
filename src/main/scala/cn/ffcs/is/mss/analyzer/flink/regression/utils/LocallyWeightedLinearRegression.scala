package cn.ffcs.is.mss.analyzer.flink.regression.utils

import java.util

import Jama.Matrix

import scala.collection.JavaConversions._
import scala.collection.mutable.ArrayBuffer

object LocallyWeightedLinearRegression {


  def predict(list: util.List[util.Map[String,String]],keyName: String,valueName : String,k: Double,
              timeStamp: Long, value : Long):Double={

    val arrayBuffer = ArrayBuffer[(Double,Double)]()

    for (map <- list){

      val index = (timeStamp - map.get(keyName).toLong) / 1000 / 60 + 1
      arrayBuffer += ((index,map.get(valueName).toDouble))
    }

    arrayBuffer += ((1,value))

    predict(arrayBuffer,k,0)
  }

  def predict(arrayBuffer: ArrayBuffer[(Double,Double)], k: Double, value: Double, start: Int, end: Int): Double ={

    for (i <- 1 to start) {
      arrayBuffer -= arrayBuffer(i)
    }

    for (i <- arrayBuffer.length - 1 to end by -1){
      arrayBuffer -= arrayBuffer(i)
    }

    predict(arrayBuffer,k,value)
  }


  def predict(arrayBuffer: ArrayBuffer[(Double,Double)], k: Double, value: Double): Double ={

    val x = getX(arrayBuffer.toArray)
    val y = getY(arrayBuffer.toArray)
    val w = getW(x, k, value)
    val xt = x.transpose

    try {
      val result = xt.times(w).times(x).inverse.times(xt).times(w).times(y)
      if(value == 0){
        result.get(0,0)
      }else {
        value * result.get(0, 0)
      }
    }catch {
      case e: RuntimeException => e.printStackTrace()
        0
    }

  }


  /**
    * 获取y矩阵
    * @param array
    * @return
    */
  def getY(array: Array[(Double,Double)]): Matrix = {

    val y = new Array[Array[Double]](array.length)

    for (i <- array.indices){
      y(i) = new Array[Double](1)
      y(i)(0) = array(i)._2
    }

    new Matrix(y)
  }


  /**
    * 获取x矩阵
    * @param array
    * @return
    */
  def getX(array: Array[(Double,Double)]): Matrix = {
    val x = new Array[Array[Double]](array.length)

    for (i <- array.indices){
      x(i) = new Array[Double](1)
      x(i)(0) = array(i)._1
    }

    new Matrix(x)
  }

  /**
    * 计算权重矩阵W(使用指数衰减函数)
    * w(i) = exp( - (x(i) - x)2 / 2k2)
    * src/main/resources/formula/权重矩阵计算公式.png
    * @param x
    * @param k
    * @return
    */
  def getW(x: Matrix, k: Double, value: Double): Matrix = {
    val length = x.getRowDimension
    val w = new Array[Array[Double]](length)

    for (i <- 0 until  length){
      w(i) = new Array[Double](length)
      w(i)(i) = Math.exp(Math.pow(x.get(i,0) - value, 2.0) / (-2.0 * k * k))
    }
    new Matrix(w)
  }
}