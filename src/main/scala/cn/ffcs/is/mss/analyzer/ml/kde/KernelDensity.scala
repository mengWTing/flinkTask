package cn.ffcs.is.mss.analyzer.ml.kde

import cn.ffcs.is.mss.analyzer.ml.kde.Kernel.Kernel

object KernelDensity {

  /**
    * 对x点计算概率密度
    *
    * @param kernel
    * @param array
    * @param x
    * @param bandwidth
    * @return
    */
  def scoreSamples(kernel: Kernel, array: Array[Double], x: Double, bandwidth: Double): Double = {
    var result = 0.0

    array.foreach(xi => {
      result += Kernel.getK(kernel, xi, x, bandwidth)
    })

    result / array.length / bandwidth
  }


  /**
    * 对x点计算概率密度
    *
    * @param kernel
    * @param array
    * @param x
    * @param bandwidth
    * @return
    */
  def scoreSamplesCircle(kernel: Kernel, array: Array[Double], x: Double, bandwidth: Double, start: Double, end: Double): Double = {
    var result = 0.0


    val mid = start + (end - start) / 2
    val delta = x - mid
    array.map(value => {
      var newValue = value + delta
      if (newValue > end){
        newValue = newValue % end
      }
      newValue
    })

    array.sorted.foreach(xi => {
      result += Kernel.getK(kernel, xi, x, bandwidth)
    })

    result / array.length / bandwidth
  }
}
