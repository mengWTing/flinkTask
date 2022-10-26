package cn.ffcs.is.mss.analyzer.utils2

import java.math.BigInteger


/**
 * Auther chenwei
 * Description ip相关计算的工具类 支持IPV6和IPV4
 * Date: Created in 2018/8/15 16:06
 */
object IpUtil {

  val ZERO = "0"
  val DOTTED = "."
  val DOTTED_REGULAR = "\\."
  val FRACTIONAL = ":"
  val FRACTIONAL_DOUBLE = "::"
  val IPV6_ZERO_PART = "0000"
  val MARK_SYMBOL = "[/]"
  val RANGE_SYMBOL = "[~|-]"


  /**
   * Auther chenwei
   * Description 判断是否是点分十进制表示法的IPV4地址
   * 1.1.1.1
   * Date: Created in 2018/8/15 15:56
   *
   * @param ip
   * @return
   */
  def isDottedDecimalNotationIPV4(ip: String): Boolean = {

    //如果ip为空或为空字符串返回false
    if (ip == null || ip.length == 0) {
      return false
    }

    //按照.对其进行分割
    val ipParts = ip.split(DOTTED_REGULAR, -1)

    //如果分割结果长度不等于4则为false
    if (ipParts.length == 4) {

      //如果分割后的结果都能转为数字且在0到255之间则为点分十进制表示法的IPV4地址
      for (ipPart: String <- ipParts) {
        try {
          val ipPartInt = Predef.augmentString(ipPart).toInt
          if (ipPartInt < 0 || ipPartInt > 255) {
            return false
          }
        } catch {
          case ex: NumberFormatException => {
            //            ex.printStackTrace()
            return false
          }
        }
      }
      return true
    }

    return false
  }

  /**
   * Auther chenwei
   * Description 是否是IPV6中的非法字符
   * IPV6只允许0-9 a-z A-Z不区分大小写
   * Date: Created in 2018/8/15 18:01
   *
   * @param char
   * @return
   */
  def isIPV6IllegalCharacter(char: Char): Boolean = {

    if ((char < '0') ||
      ('9' < char && char < 'A') ||
      ('Z' < char && char < 'a') ||
      ('z' < char)) {
      return true
    } else {
      return false
    }
  }

  /**
   * Auther chenwei
   * Description 是否是冒分十六进制表示法的IPV6地址的一部分
   * 09aZ -> true
   * Date: Created in 2018/8/16 09:26
   *
   * @param ipPart
   * @return
   */
  def isFractionalHexadecimalNotationIPV6Part(ipPart: String): Boolean = {
    //如果ip为空或者为空字符串返回false
    if (ipPart == null || ipPart.length == 0) {
      return false
    }

    if (ipPart.length > 4) {
      return false
    }

    for (ch: Char <- ipPart.toCharArray) {
      if (IpUtil.isIPV6IllegalCharacter(ch)) {
        return false
      }
    }
    return true

  }

  /**
   * Auther chenwei
   * Description 是否是冒分十六进制表示法的IPV6地址的一部分
   * XXXX
   * XXXX:XXXX
   * Date: Created in 2018/8/16 09:26
   *
   * @param ip
   * @return
   */
  def isFractionalHexadecimalNotationIPV6Parts(ip: String): Boolean = {
    //如果ip为空或为空字符串返回false
    if (ip == null || ip.length == 0 || FRACTIONAL.equals(ip)) {
      return false
    }
    //按照:对其进行分割
    val ipParts = ip.split(FRACTIONAL, -1)
    if (ipParts.length <= 8) {

      for (ipPart: String <- ipParts) {
        if (!IpUtil.isFractionalHexadecimalNotationIPV6Part(ipPart)) {
          return false
        }
      }
      return true
    }

    return false

  }

  /**
   * Auther chenwei
   * Description 判断是否是冒分十六进制表示法的IPV6地址
   * 格式为X:X:X:X:X:X:X:X，其中每个X表示地址中的16b，以十六进制表示，例如：
   * 　　ABCD:EF01:2345:6789:ABCD:EF01:2345:6789
   * 　　这种表示法中，每个X的前导0是可以省略的，例如：
   * 　　2001:0DB8:0000:0023:0008:0800:200C:417A→
   * 2001: DB8:   0:  23:   8: 800:200C:417A
   * Date: Created in 2018/8/15 16:47
   *
   * @param ip
   * @return
   */
  def isFractionalHexadecimalNotationIPV6(ip: String): Boolean = {
    //如果ip为空或为空字符串返回false
    if (ip == null || ip.length == 0) {
      return false
    }

    //按照:对其进行分割
    val ipParts = ip.split(FRACTIONAL, -1)
    if (ipParts.length == 8) {
      return IpUtil.isFractionalHexadecimalNotationIPV6Parts(ip)
    }

    return false
  }


  /**
   * Auther chenwei
   * Description 判断是否是0位压缩表示法的IPV6地址
   * 在某些情况下，一个IPv6地址中问可能包含很长的一段0，可以把连续的一段0压缩为“::”。
   * 但为保证地址解析的唯一性，地址中”::”只能出现一次，例如：
   * 　　FF01:0:0:0:0:0:0:1101 → FF01::1101
   * 　　0:0:0:0:0:0:0:1 → ::1
   * 　　0:0:0:0:0:0:0:0 → ::
   * Date: Created in 2018/8/15 17:05
   *
   * @param ip
   * @return
   */
  def isZeroBitCompressionNotationIPV6(ip: String): Boolean = {
    //如果ip为空或为空字符串返回false
    if (ip == null || ip.length == 0) {
      return false
    }

    //按照::对其进行分割 eg: FF01:FF01::1101:1101 -> [FF01:FF01,1101:1101]
    val ipParts = ip.split(FRACTIONAL_DOUBLE, -1)
    if (ipParts.length == 2) {

      //eg FF01:FF01 -> [FF01,FF01]
      val ipPart1s = ipParts(0).split(FRACTIONAL, -1)
      //eg 1101:1101 -> [1101,1101]
      val ipPart2s = ipParts(1).split(FRACTIONAL, -1)

      if (ipPart1s.length + ipPart2s.length > 8) {
        return false
      }

      val tempIpPart = ipPart1s ++ ipPart2s
      for (i: Int <- Predef.refArrayOps(tempIpPart).indices) {

        if (tempIpPart(i).length == 0) {
          if (i != 0 && i != tempIpPart.length - 1) {
            return false
          }
        } else {
          if (!IpUtil.isFractionalHexadecimalNotationIPV6Part(tempIpPart(i))) {
            return false
          }
        }

      }
      return true
    }

    return false
  }

  /**
   * Auther chenwei
   * Description 判断是否是内嵌IPv4地址表示法的IPV6地址
   * 为了实现IPv4-IPv6互通，IPv4地址会嵌入IPv6地址中，此时地址常表示为：X:X:X:X:X:X:d.d.d.d
   * 前96b采用冒分十六进制表示，而最后32b地址则使用IPv4的点分十进制表示，
   * XXXX:XXXX:XXXX:XXXX:XXXX:XXXX:ddd.ddd.ddd.ddd
   * 例如::192.168.0.1与::FFFF:192.168.0.1就是两个典型的例子，注意在前96b中，压缩0位的方法依旧适用
   * Date: Created in 2018/8/15 18:12
   *
   * @param ip
   * @return
   */
  def isEmbeddedIPV4AddressNotationIPV6(ip: String): Boolean = {
    //如果ip为空或为空字符串返回false
    if (ip == null || ip.length == 0) {
      return false
    }

    //按照::对其进行分割 eg FFFF:FFFF::FFFF:192.168.0.1 -> [FFFF:FFFF,FFFF:192.168.0.1]
    val ipParts = ip.split(FRACTIONAL_DOUBLE, -1)
    if (ipParts.length == 2) {

      //eg FFFF:FFFF -> [FFFF,FFFF]
      val ipPartFractional1s = ipParts(0).split(FRACTIONAL)
      //eg FFFF:192.168.0.1 -> [FFFF,192.168.0.1]
      val ipPartFractional2s = ipParts(1).split(FRACTIONAL)

      if ((ipPartFractional1s.length + ipPartFractional2s.length) > 7) {
        return false
      }

      if (ipParts(0).length != 0 && !IpUtil.isFractionalHexadecimalNotationIPV6Parts(ipParts(0))) {
        return false
      }

      for (i: Int <- Predef.intWrapper(0) until ipPartFractional2s.length - 1) {
        if (!IpUtil.isFractionalHexadecimalNotationIPV6Parts(ipPartFractional2s(i))) {
          return false
        }
      }

      return IpUtil.isDottedDecimalNotationIPV4(ipPartFractional2s(ipPartFractional2s.length - 1))

    } else {

      //按照:对其进行分割
      val ipPartFractionals = ip.split(FRACTIONAL, -1)
      if (ipPartFractionals.length == 7) {
        for (i: Int <- Predef.intWrapper(0) until ipPartFractionals.length - 1) {
          if (!IpUtil.isFractionalHexadecimalNotationIPV6Parts(ipPartFractionals(i))) {
            return false
          }
        }

        return IpUtil.isDottedDecimalNotationIPV4(ipPartFractionals(6))

      }

      return false

    }

  }


  /**
   * Auther chenwei
   * Description 将内嵌IPv4地址表示法的IPV6地址转换成点分十进制表示法的IPV4地址
   * XXXX:XXXX:XXXX:XXXX:XXXX:XXXX:ddd.ddd.ddd.ddd -> ddd.ddd.ddd.ddd
   * XXXX:XXXX:XXXX:XXXX:XXXX::XXXX:ddd.ddd.ddd.ddd -> ddd.ddd.ddd.ddd
   * XXXX:XXXX:XXXX:XXXX:XXXX:XXXX::ddd.ddd.ddd.ddd -> ddd.ddd.ddd.ddd
   * Date: Created in 2018/8/15 18:11
   *
   * @param ip
   * @return
   */
  def embeddedIPV4AddressNotationIPV6ToDottedDecimalNotationIPV4(ip: String): String = {

    if (IpUtil.isEmbeddedIPV4AddressNotationIPV6(ip)) {
      val ipParts = ip.split(FRACTIONAL, -1)
      return ipParts(ipParts.length - 1)

    }

    return null

  }


  /**
   * Auther chenwei
   * Description 将点分十进制表示法的IPV4地址转换成内嵌IPv4地址表示法的IPV6地址
   * ddd.ddd.ddd.ddd -> ::ddd.ddd.ddd.ddd
   * Date: Created in 2018/8/16 10:55
   *
   * @param ip
   * @return
   */
  def dottedDecimalNotationIPV4ToEmbeddedIPV4AddressNotationIPV6(ip: String): String = {

    if (IpUtil.isDottedDecimalNotationIPV4(ip)) {
      return FRACTIONAL_DOUBLE + ip
    }

    return null

  }


  /**
   * Auther chenwei
   * Description 将点分十进制表示法的IPV4地址转换成冒分十六进制表示法的IPV6地址的一部分
   * ddd.ddd.ddd.ddd -> XXXX:XXXX
   * Date: Created in 2018/8/16 11:05
   *
   * @param ip
   * @return
   */
  def dottedDecimalNotationIPV4ToFractionalHexadecimalNotationIPV6Parts(ip: String): String = {
    if (IpUtil.isDottedDecimalNotationIPV4(ip)) {
      val ipParts = Predef.refArrayOps(ip.split(DOTTED_REGULAR)).map(Predef.augmentString(_).toInt)
      return ((ipParts(0) << 8) + ipParts(1)).toHexString.toUpperCase() + FRACTIONAL + ((ipParts(2) << 8) + ipParts(3)).toHexString.toUpperCase
    }
    return null
  }

  /**
   * Auther chenwei
   * Description 将冒分十六进制表示法的IPV6地址的一部分转换成点分十进制表示法的IPV4地址
   * XXXX:XXXX -> null
   * 0000 -> 0.0
   * ffff -> 255.255
   * -> 0.0
   * Date: Created in 2018/8/16 11:36
   *
   * @param ipPart
   * @return
   */
  def fractionalHexadecimalNotationIPV6PartToDottedDecimalNotationIPV4(ipPart: String): String = {

    if (IpUtil.isFractionalHexadecimalNotationIPV6Part(ipPart)) {

      val num = if (ipPart.length == 0) 0 else Integer.parseInt(ipPart, 16)

      return ((num & 0xff00) >> 8) + DOTTED + (num & 0xff)

    } else if (ipPart != null && ipPart.length == 0) {
      return ZERO + DOTTED + ZERO
    }
    return null
  }

  /**
   * Auther chenwei
   * Description 将冒分十六进制表示法的IPV6地址的一部分转换成点分十进制表示法的IPV4地址
   * XXXX:XXXX:XXXX -> null
   * 0000:XXXX:XXXX -> ddd.ddd.ddd
   * XXXX:XXXX -> ddd.ddd.ddd.ddd
   * XXXX -> 0.0.ddd.ddd
   * -> 0.0.0.0
   * Date: Created in 2018/8/16 11:05
   *
   * @param ip
   * @return
   */
  def fractionalHexadecimalNotationIPV6PartsToDottedDecimalNotationIPV4(ip: String): String = {
    if (IpUtil.isFractionalHexadecimalNotationIPV6Parts(ip)) {
      val ipParts = ip.split(FRACTIONAL, -1)
      for (i: Int <- Predef.intWrapper(0) until ipParts.length - 2) {
        if (!IPV6_ZERO_PART.equals(ipParts(i))) {
          return null
        }
      }

      if (ipParts.length >= 2) {
        return IpUtil.fractionalHexadecimalNotationIPV6PartToDottedDecimalNotationIPV4(ipParts(ipParts.length - 2)) + DOTTED +
          IpUtil.fractionalHexadecimalNotationIPV6PartToDottedDecimalNotationIPV4(ipParts(ipParts.length - 1))
      } else if (ipParts.length == 1) {
        return IpUtil.fractionalHexadecimalNotationIPV6PartToDottedDecimalNotationIPV4(ZERO) + DOTTED +
          IpUtil.fractionalHexadecimalNotationIPV6PartToDottedDecimalNotationIPV4(ipParts(0))
      }
    } else if (ip != null && ip.length == 0) {
      return IpUtil.fractionalHexadecimalNotationIPV6PartToDottedDecimalNotationIPV4("") + DOTTED +
        IpUtil.fractionalHexadecimalNotationIPV6PartToDottedDecimalNotationIPV4("")
    }
    return null
  }

  /**
   * Auther chenwei
   * Description 将内嵌IPv4地址表示法的IPV6地址转换为0位压缩表示法的IPV6地址
   * ::FFFF:192.168.0.1
   * Date: Created in 2018/8/16 11:00
   *
   * @param ip
   * @return
   */
  def embeddedIPV4AddressNotationIPV6ToZeroBitCompressionNotationIPV6(ip: String): String = {
    if (IpUtil.isEmbeddedIPV4AddressNotationIPV6(ip)) {
      val ipReversePart = Predef.augmentString(ip).reverse.split(FRACTIONAL, 2)

      return Predef.augmentString(ipReversePart(1)).reverse + FRACTIONAL +
        IpUtil.dottedDecimalNotationIPV4ToFractionalHexadecimalNotationIPV6Parts(Predef.augmentString(ipReversePart(0)).reverse)

    }

    return null
  }

  /**
   * Auther chenwei
   * Description 将内嵌IPv4地址表示法的IPV6地址转换为冒分十六进制表示法的IPV6地址
   * ::FFFF:192.168.0.1
   * Date: Created in 2018/8/16 11:00
   *
   * @param ip
   * @return
   */
  def embeddedIPV4AddressNotationIPV6ToFractionalHexadecimalNotationIPV6(ip: String): String = {
    if (IpUtil.isEmbeddedIPV4AddressNotationIPV6(ip)) {
      return IpUtil.zeroBitCompressionNotationIPV6ToFractionalHexadecimalNotationIPV6(IpUtil.embeddedIPV4AddressNotationIPV6ToZeroBitCompressionNotationIPV6(ip))

    }

    return null
  }


  /**
   * Auther chenwei
   * Description 将0位压缩表示法的IPV6地址转换为冒分十六进制表示法的IPV6地址
   * ::1 -> 0000:0000:0000:0000:0000:0000:0000:1
   * Date: Created in 2018/8/16 12:59
   *
   * @param ip
   * @return
   */
  def zeroBitCompressionNotationIPV6ToFractionalHexadecimalNotationIPV6(ip: String): String = {
    if (IpUtil.isZeroBitCompressionNotationIPV6(ip)) {
      val ipPart = ip.split(FRACTIONAL_DOUBLE, -1)
      val stringBuffer = new StringBuffer()
      stringBuffer.append(ipPart(0))

      val sum1 = if (ipPart(0).length == 0) 0 else ipPart(0).split(FRACTIONAL).length
      val sum2 = if (ipPart(1).length == 0) 0 else ipPart(1).split(FRACTIONAL).length

      for (i: Int <- Predef.intWrapper(sum1) until 8 - sum2) {
        if (stringBuffer.length() != 0) {
          stringBuffer.append(FRACTIONAL)
        }
        stringBuffer.append(IPV6_ZERO_PART)
      }

      if (ipPart(1).length != 0) {
        stringBuffer.append(FRACTIONAL)
        stringBuffer.append(ipPart(1))
      }

      return stringBuffer.toString

    }

    return null
  }

  /**
   * Auther chenwei
   * Description 规范化冒分十六进制表示法的IPV6地址
   * 1.填充省略的前导0
   * 2.小写字母转大写
   * Date: Created in 2018/8/16 14:05
   *
   * @param ip
   * @return
   */
  def fractionalHexadecimalNotationIPV6Normalization(ip: String): String = {
    if (IpUtil.isFractionalHexadecimalNotationIPV6(ip)) {
      val ipParts = ip.split(FRACTIONAL, -1)
      val stringBuffer = new StringBuffer()

      for (ipPart: String <- ipParts) {
        for (i: Int <- Predef.intWrapper(0) until 4 - ipPart.length) {
          stringBuffer.append(ZERO)
        }
        stringBuffer.append(ipPart).append(FRACTIONAL)
      }

      return stringBuffer.deleteCharAt(stringBuffer.length() - 1).toString.toUpperCase()

    }
    return null
  }


  /**
   * Auther chenwei
   * Description 计算两个冒分十六进制表示ipv6地址之间的距离
   * Date: Created in 2018/8/16 14:10
   */
  def fractionalHexadecimalNotationIPV6Distance(ip1: String, ip2: String): BigInteger = {

    if (!IpUtil.isFractionalHexadecimalNotationIPV6(ip1) || !IpUtil.isFractionalHexadecimalNotationIPV6(ip2)) {
      return null
    }

    var num1: BigInteger = BigInteger.ZERO
    var num2: BigInteger = BigInteger.ZERO

    val displacementNum = BigInteger.valueOf(1 << 16)

    val ipPart1s = ip1.split(FRACTIONAL, -1)
    val ipPart2s = ip2.split(FRACTIONAL, -1)

    for (ipPart1: String <- ipPart1s) {
      num1 = num1.multiply(displacementNum).add(BigInteger.valueOf(Integer.parseInt(ipPart1, 16)))
    }


    for (ipPart2: String <- ipPart2s) {
      num2 = num2.multiply(displacementNum).add(BigInteger.valueOf(Integer.parseInt(ipPart2, 16)))
    }

    return num2.subtract(num1)
  }

  /**
   * Auther chenwei
   * Description 将点分十进制表示法的IPV4地址转换为冒分十六进制表示法的IPV6地址
   * Date: Created in 2018/8/16 15:21
   *
   * @param ip
   * @return
   */
  def dottedDecimalNotationIPV4ToFractionalHexadecimalNotationIPV6(ip: String): String = {
    if (IpUtil.isDottedDecimalNotationIPV4(ip)) {
      return IpUtil.embeddedIPV4AddressNotationIPV6ToFractionalHexadecimalNotationIPV6(IpUtil.dottedDecimalNotationIPV4ToEmbeddedIPV4AddressNotationIPV6(ip))
    }

    return null
  }

  /**
   * Auther chenwei
   * Description 把ip转成规范化的冒分十六进制表示法IPV6地址
   * Date: Created in 2018/8/16 15:07
   *
   * @param ip
   * @return
   */
  def getFractionalHexadecimalNotationIPV6(ip: String): String = {

    if (IpUtil.isDottedDecimalNotationIPV4(ip)) {
      return IpUtil.fractionalHexadecimalNotationIPV6Normalization(IpUtil.dottedDecimalNotationIPV4ToFractionalHexadecimalNotationIPV6(ip))
    } else if (IpUtil.isFractionalHexadecimalNotationIPV6(ip)) {
      return IpUtil.fractionalHexadecimalNotationIPV6Normalization(ip)
    } else if (IpUtil.isZeroBitCompressionNotationIPV6(ip)) {
      return IpUtil.fractionalHexadecimalNotationIPV6Normalization(IpUtil.zeroBitCompressionNotationIPV6ToFractionalHexadecimalNotationIPV6(ip))
    } else if (IpUtil.isEmbeddedIPV4AddressNotationIPV6(ip)) {
      return IpUtil.fractionalHexadecimalNotationIPV6Normalization(IpUtil.embeddedIPV4AddressNotationIPV6ToFractionalHexadecimalNotationIPV6(ip))
    } else {
      return null
    }

  }


  /**
   * Auther chenwei
   * Description 计算两个ip之间的距离
   * Date: Created in 2018/8/16 14:57
   *
   * @param ip1
   * @param ip2
   */
  def ipDistance(ip1: String, ip2: String): BigInteger = {
    return IpUtil.fractionalHexadecimalNotationIPV6Distance(IpUtil.getFractionalHexadecimalNotationIPV6(ip1), IpUtil.getFractionalHexadecimalNotationIPV6(ip2))
  }


  /**
   * Auther chenwei
   * Description 是否是掩码表示法的ipv6地址
   * FFFF:FFFF:FFFF:FFFF:FFFF:FFFF:FFFF:FFFF/32
   * Date: Created in 2018/8/17 09:27
   *
   * @param ip
   * @return
   */
  def isMaskNotationIPV6(ip: String): Boolean = {
    if (ip == null || ip.length == 0) {
      return false
    }

    val ipMarkParts = ip.split(MARK_SYMBOL, -1)
    if (ipMarkParts.length != 2) {
      return false
    }

    try {
      val markDigits = ipMarkParts(1).toInt
      if (markDigits < 0 || 128 < markDigits) {
        return false
      }
    } catch {
      case ex: NumberFormatException => {
        //      ex.printStackTrace()
        return false
      }
    }

    return isEmbeddedIPV4AddressNotationIPV6(ipMarkParts(0)) || isFractionalHexadecimalNotationIPV6(ipMarkParts(0)) || isZeroBitCompressionNotationIPV6(ipMarkParts(0))

  }

  /**
   * Auther chenwei
   * Description 是否是二进制表示的ip
   * Date: Created in 2018/8/16 16:30
   *
   * @param binaryIp
   * @return
   */
  def isBinaryIPV4(binaryIp: String): Boolean = {

    if (binaryIp == null || binaryIp.length == 0) {
      return false
    }

    var num = 0L
    for (ch <- binaryIp.toCharArray) {
      num *= 2
      if (ch == '1') {
        num += 1
      } else if (ch != '0') {
        return false
      }
    }


    return num >= 0 && num < (1L << 32)
  }


  /**
   * Auther chenwei
   * Description 是否是点分二进制法表示的ip
   * Date: Created in 2018/8/16 16:30
   *
   * @param binaryIp
   * @return
   */
  def isDottedBinaryNotationIPV4(binaryIp: String): Boolean = {

    if (binaryIp == null || binaryIp.length == 0) {
      return false
    }

    var num = 0L
    var dottedCount = 0
    for (ch <- binaryIp.toCharArray) {
      num *= 2
      if (ch == '1') {
        num += 1
      } else if (ch == '.') {
        dottedCount += 1
      } else if (ch != '0') {
        return false
      }
    }

    if (dottedCount != 3) {
      return false
    }

    return num >= 0 && num < (1L << 32)
  }

  /**
   * Auther chenwei
   * Description 将二进制表示的ip转换成点分十六进制法
   * Date: Created in 2018/8/16 16:29
   *
   * @param binaryIp
   * @return
   */
  def binaryIpToDottedHexadecimalNotationIPV4(binaryIp: String): String = {
    if (isBinaryIPV4(binaryIp)) {
      var num = 0L
      for (ch <- binaryIp.toCharArray) {
        num *= 2
        if (ch == '1') {
          num += 1
        }
      }

      return ((num & 0xff000000) >> 24).toHexString.toUpperCase() + DOTTED +
        ((num & 0x00ff0000) >> 16).toHexString.toUpperCase() + DOTTED +
        ((num & 0x0000ff00) >> 8).toHexString.toUpperCase() + DOTTED +
        (num & 0x000000ff).toHexString.toUpperCase()

    }
    return null
  }

  /**
   * Auther chenwei
   * Description 点分十六进制法的非法字符
   * Date: Created in 2018/8/16 17:03
   *
   * @param char
   * @return
   */
  def isDottedHexadecimalNotationIPV4IllegalCharacter(char: Char): Boolean = {

    if ((char < '0') ||
      ('9' < char && char < 'A') ||
      ('F' < char && char < 'a') ||
      ('f' < char)) {
      return true
    } else {
      return false
    }
  }

  /**
   * Auther chenwei
   * Description 是否是点分十六进制法表示的ipv4地址
   * Date: Created in 2018/8/16 16:52
   *
   * @param ip
   * @return
   */
  def isDottedHexadecimalNotationIPV4(ip: String): Boolean = {
    if (ip == null || ip.length == 0) {
      return false
    }

    val ipParts = ip.split(DOTTED_REGULAR)
    if (ipParts.length != 4) {
      return false
    }

    for (ipPart <- ipParts) {
      if (ipPart.length < 1 || ipPart.length > 2) {
        return false
      }

      for (ch <- ipPart.toCharArray) {
        if (isDottedHexadecimalNotationIPV4IllegalCharacter(ch)) {
          return false
        }
      }
    }

    return true
  }

  /**
   * Auther chenwei
   * Description 将点分十六进制法转换成点分十进制法
   * Date: Created in 2018/8/16 16:51
   *
   * @param ip
   * @return
   */
  def dottedHexadecimalNotationIPV4ToDottedDecimalNotationIPV4(ip: String): String = {
    if (isDottedHexadecimalNotationIPV4(ip)) {
      val ipParts = ip.split(DOTTED_REGULAR)
      val stringBuffer = new StringBuffer()
      for (ipPart <- ipParts) {
        stringBuffer.append(Integer.parseInt(ipPart, 16)).append(DOTTED)
      }

      return stringBuffer.deleteCharAt(stringBuffer.length() - 1).toString
    }

    return null
  }

  /**
   * Auther chenwei
   * Description 将二进制表示的ip转换成点分十进制法
   * Date: Created in 2018/8/16 16:29
   *
   * @param binaryIp
   * @return
   */
  def binaryIpToDottedDecimalNotationIPV4(binaryIp: String): String = {
    if (isBinaryIPV4(binaryIp)) {
      return dottedHexadecimalNotationIPV4ToDottedDecimalNotationIPV4(binaryIpToDottedHexadecimalNotationIPV4(binaryIp))
    }
    return null
  }

  /**
   * Auther chenwei
   * Description 将点分十进制法转换成二进制表示的ip
   * Date: Created in 2018/8/16 18:06
   *
   * @param ip
   * @return
   */
  def dottedDecimalNotationIPV4ToBinaryIPV4(ip: String): String = {

    if (isDottedDecimalNotationIPV4(ip)) {
      val ipParts = ip.split(DOTTED_REGULAR, -1)
      val stringBuffer = new StringBuffer()
      for (ipPart <- ipParts) {
        val binaryIpPart = Integer.parseInt(ipPart).toBinaryString
        for (i <- binaryIpPart.length until 8) {
          stringBuffer.append(ZERO)
        }
        stringBuffer.append(binaryIpPart)
      }

      return stringBuffer.toString
    }

    return null
  }

  /**
   * Auther chenwei
   * Description 将点分十进制法转换成点分二进制表示的ip
   * Date: Created in 2018/8/16 18:06
   *
   * @param ip
   * @return
   */
  def dottedDecimalNotationIPV4ToDottedBinaryNotationIPV4(ip: String): String = {

    if (isDottedDecimalNotationIPV4(ip)) {
      val ipParts = ip.split(DOTTED_REGULAR, -1)
      val stringBuffer = new StringBuffer()
      for (ipPart <- ipParts) {
        val binaryIpPart = Integer.parseInt(ipPart).toBinaryString
        for (i <- binaryIpPart.length until 8) {
          stringBuffer.append(ZERO)
        }
        stringBuffer.append(binaryIpPart).append(DOTTED)
      }

      return stringBuffer.deleteCharAt(stringBuffer.length() - 1).toString
    }

    return null
  }


  /**
   * Auther chenwei
   * Description 是否为ipv4的掩码表示法
   * 1.1.1.1/24
   * Date: Created in 2018/8/16 18:14
   *
   * @param ip
   * @return
   */
  def isMaskNotationIPV4(ip: String): Boolean = {
    if (ip == null || ip.length == 0) {
      return false
    }

    val markIpParts = ip.split(MARK_SYMBOL, -1)
    if (markIpParts.length != 2) {
      return false
    }

    if (!isDottedDecimalNotationIPV4(markIpParts(0))) {
      return false
    }

    try {
      val markDigits = markIpParts(1).toInt
      if (markDigits < 0 || markDigits > 32) {
        return false
      }
    } catch {
      case ex: NumberFormatException => {
        //        ex.printStackTrace()
        return false
      }
    }

    return true

  }

  /**
   * Auther chenwei
   * Description 是否是ip范围表示法
   * 1.1.1.1-2.2.2.2
   * 1.1.1.1~2.2.2.2
   * Date: Created in 2018/8/16 18:20
   *
   * @param ip
   * @return
   */
  def isRangeNotationIPV4(ip: String): Boolean = {
    if (ip == null || ip.length == 0) {
      return false
    }

    val ipRangeParts = ip.split(RANGE_SYMBOL, -1)
    if (ipRangeParts.length != 2) {
      return false
    }

    return isDottedDecimalNotationIPV4(ipRangeParts(0)) && isDottedDecimalNotationIPV4(ipRangeParts(1))

  }

  /**
   * Auther chenwei
   * Description 将二进制表示的ip转成Long格式
   * Date: Created in 2018/8/17 10:07
   *
   * @param binaryIp
   * @return
   */
  def binaryIpToLong(binaryIp: String): Long = {
    if (isBinaryIPV4(binaryIp)) {
      return java.lang.Long.parseLong(binaryIp, 2)
    }
    return 0L
  }

  def dottedDecimalNotationIPV4ToLong(ip:String):Long={
    if (isDottedDecimalNotationIPV4(ip)) {
      val binaryIp = dottedDecimalNotationIPV4ToBinaryIPV4(ip)
      val stringBuffer = new StringBuffer()
      val binaryIpParts = binaryIp.split(DOTTED_REGULAR, -1)
      for (binaryIpPart <- binaryIpParts) {
        for (i <- binaryIpPart.length to 8) {
          stringBuffer.append("0")
        }
        stringBuffer.append(binaryIpPart)
      }
      return java.lang.Long.parseLong(stringBuffer.toString, 2)
    }
    return 0L
  }

  /**
   * Auther chenwei
   * Description 将点分二进制表示的ip转成Long格式
   * Date: Created in 2018/8/17 10:07
   *
   * @param binaryIp
   * @return
   */
  def dottedBinaryNotationIPV4ToLong(binaryIp: String): Long = {
    if (isDottedBinaryNotationIPV4(binaryIp)) {
      val stringBuffer = new StringBuffer()
      val binaryIpParts = binaryIp.split(DOTTED_REGULAR, -1)
      for (binaryIpPart <- binaryIpParts) {
        for (i <- binaryIpPart.length to 8) {
          stringBuffer.append("0")
        }
        stringBuffer.append(binaryIpPart)
      }
      return java.lang.Long.parseLong(stringBuffer.toString, 2)
    }
    return 0L
  }

  /**
   * Auther chenwei
   * Description 将掩码表示法的ipv4地址转换成ip范围表示法的ipv4地址
   * Date: Created in 2018/8/17 09:40
   *
   * @param ip
   * @return
   */
  def maskNotationIPV4ToRangeNotationIPV4(ip: String): String = {
    if (isRangeNotationIPV4(ip)) {

      val markIpParts = ip.split(MARK_SYMBOL, -1)
      val binaryIp = binaryIpToLong(dottedDecimalNotationIPV4ToBinaryIPV4(markIpParts(0)))
      val markDigits = markIpParts(1).toInt

      return (binaryIp & getMarkStartNum(markDigits, 32)) + "-" + (binaryIp | getMarkEndNum(markDigits, 32))

    }

    return null
  }

  /**
   * Auther chenwei
   * Description 根据掩码位数计算开始时的掩码
   * Date: Created in 2018/8/17 09:59
   *
   * @param markDigits
   * @return
   */
  def getMarkStartNum(markDigits: Int, length: Int): Long = {
    var markNum = 0L
    for (i <- 0 until markDigits) {
      markNum = (markNum << 1) + 1
    }

    return markNum << (length - markDigits)
  }

  /**
   * Auther chenwei
   * Description 根据计算结束时的掩码
   * Date: Created in 2018/8/17 09:59
   *
   * @return
   */
  def getMarkEndNum(markDigits: Int, length: Int): Long = {
    var markNum = 0L
    for (i <- markDigits until length) {
      markNum = (markNum << 1) + 1
    }

    return markNum
  }

}
