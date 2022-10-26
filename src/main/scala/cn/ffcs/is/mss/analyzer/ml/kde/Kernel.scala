package cn.ffcs.is.mss.analyzer.ml.kde

/**
  * 核函数类型
  * https://en.wikipedia.org/wiki/Kernel_(statistics)#Kernel_functions_in_common_use
  */
object Kernel extends Enumeration {
  type Kernel = Function3[Double, Double, Double, Double]

  /**
    * 高斯核函数
    * (2π)^(-1/2) * exp((-1/2) * (u^2))
    */
  val Gaussian = (xi: Double, x: Double, bandwidth: Double) => {
    val u = getU(xi, x, bandwidth)
    var result = 1.0
    result *= math.pow(math.Pi * 2.0, -0.5)
    result *= math.exp(-0.5 * math.pow(u, 2.0))
    result
  }

  /**
    * tophat核函数
    * if abs(u) <= 1
    * 1/2
    */
  val Tophat = (xi: Double, x: Double, bandwidth: Double) => {
    val u = getU(xi, x, bandwidth)
    if (math.abs(u) < 1) 0.5 else 0
  }

  /**
    * epanechnikov核函数
    * if abs(u) <= 1
    * 3/4 (1 - u^2)                              ^
    */
  val Epanechnikov = (xi: Double, x: Double, bandwidth: Double) => {
    val u = getU(xi, x, bandwidth)
    if (math.abs(u) < 1) {
      var result = 1.0
      result *= 0.75
      result *= 1.0 - math.pow(u, 2.0)
      result
    } else {
      0
    }

  }

  /**
    * cosine核函数
    * if abs(u) <= 1
    * (π/4) * cos((π/2) * u)
    */
  val Cosine = (xi: Double, x: Double, bandwidth: Double) => {
    val u = getU(xi, x, bandwidth)
    if (math.abs(u) <= 1) {
      var result = 1.0
      result *= math.Pi / 4.0
      result *= math.cos(math.Pi / 2.0 * u)
      result
    } else {
      0
    }

  }

  /**
    * tricube核函数
    * if abs(u) <= 1
    * (70/81) * (1 - |u|^3)^3
    */
  val Tricube = (xi: Double, x: Double, bandwidth: Double) => {
    val u = getU(xi, x, bandwidth)
    if (math.abs(u) <= 1) {
      var result = 1.0
      result *= 70.0 / 81.0
      result *= math.pow(1.0 - math.pow(math.abs(u), 3.0), 3.0)
      result
    } else {
      0
    }

  }

  /**
    * quartic核函数
    * if abs(u) <= 1
    * (15/16) * (1 - |u|^2)^2
    */
  val Quartic = (xi: Double, x: Double, bandwidth: Double) => {
    val u = getU(xi, x, bandwidth)
    if (math.abs(u) <= 1) {
      var result = 1.0
      result *= 15.0 / 16.0
      result *= math.pow(1.0 - math.pow(u, 2.0), 2.0)
      result
    } else {
      0
    }

  }

  /**
    * triweight核函数
    * if abs(u) <= 1
    * (35/32) * (1 - u^2)^3
    */
  val Triweight = (xi: Double, x: Double, bandwidth: Double) => {
    val u = getU(xi, x, bandwidth)
    if (math.abs(u) <= 1) {
      var result = 1.0
      result *= 35.0 / 32.0
      result *= math.pow(1.0 - math.pow(u, 2.0), 3.0)
      result
    } else {
      0
    }

  }

  /**
    * triangular核函数
    * if abs(u) <= 1
    * 1 - |u|
    */
  val Triangular = (xi: Double, x: Double, bandwidth: Double) => {
    val u = getU(xi, x, bandwidth)
    if (math.abs(u) <= 1) {
      1.0 - math.abs(u)
    } else {
      0
    }

  }

  /**
    * Logistic核函数
    * 1 / (exp(u) + 2 + exp(-u))
    */
  val Logistic = (xi: Double, x: Double, bandwidth: Double) => {
    val u = getU(xi, x, bandwidth)
    1.0 / (math.exp(u) + 2.0 + math.exp(-u))

  }

  /**
    * Sigmoid function核函数
    * (2/π) * (1 / (exp(u)+exp(-u)))
    */
  val Sigmoid = (xi: Double, x: Double, bandwidth: Double) => {
    val u = getU(xi, x, bandwidth)
    2.0 / math.Pi / (math.exp(u) + math.exp(-u))

  }

  /**
    * Silverman核函数
    * (1/2) * exp(-1 * |u| * 2^(-11/2)) * sin(|u| * 2^(-1/2) + π/4)
    */
  val Silverman = (xi: Double, x: Double, bandwidth: Double) => {
    val u = getU(xi, x, bandwidth)
    var result = 1.0
    result *= 0.5
    result *= math.exp(-1 * math.abs(u) * math.pow(2, -0.5))
    result *= math.sin(math.abs(u) * math.pow(2, -0.5) + math.Pi / 4.0)
    result
  }


  /**
    * 根据传入的核函数及参数计算结果
    *
    * @param kernel
    * @param xi
    * @param x
    * @param bandwidth
    * @return
    */
  def getK(kernel: Kernel, xi: Double, x: Double, bandwidth: Double): Double = {
    kernel(xi, x, bandwidth)
  }

  /**
    * 根据bandwidth标准化
    *
    * @param xi
    * @param x
    * @param bandwidth
    * @return
    */
  def getU(xi: Double, x: Double, bandwidth: Double): Double = {

    (xi - x) / bandwidth

  }


}
