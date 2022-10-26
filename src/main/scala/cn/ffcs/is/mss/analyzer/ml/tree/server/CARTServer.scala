/*
 * @project mss
 * @company Fujian Fujitsu Communication Software Co., Ltd.
 * @author chenwei
 * @date 2019-12-02 18:25:00
 * @version v1.0
 * @update [no] [date YYYY-MM-DD] [name] [description]
 */
package cn.ffcs.is.mss.analyzer.ml.tree.server

import cn.ffcs.is.mss.analyzer.ml.tree.{CART, DecisionTreeNode}
import cn.ffcs.is.mss.analyzer.ml.utils.MlUtil
import cn.ffcs.is.mss.analyzer.utils.JsonUtil
import io.netty.bootstrap.ServerBootstrap
import io.netty.buffer.Unpooled
import io.netty.channel.{ChannelHandlerContext, ChannelInboundHandlerAdapter, ChannelInitializer, ChannelOption}
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.DelimiterBasedFrameDecoder
import io.netty.handler.codec.string.StringDecoder

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.io.Source

/**
 *
 * @author chenwei
 * @date 2019-12-02 18:25:00
 * @title CARTServer
 * @update [no] [date YYYY-MM-DD] [name] [description]
 */
class CARTServer(hostname : String, port : Int, decisionTreeNode : DecisionTreeNode) {

  def bind(): Unit = {
    //线程组
    val bossGroup = new NioEventLoopGroup()
    val workerGroup = new NioEventLoopGroup(100)


    //服务器引导
    val serverBootstrap = new ServerBootstrap()
    //初始化线程池
    serverBootstrap.group(bossGroup, workerGroup)
    //设置channel
    serverBootstrap.channel(classOf[NioServerSocketChannel])
    //设置channel的参数,请求队列大小为10240
    serverBootstrap.option(ChannelOption.SO_BACKLOG, new Integer(10240))
    serverBootstrap.childOption(ChannelOption.SO_KEEPALIVE, java.lang.Boolean.TRUE)
    serverBootstrap.childOption(ChannelOption.TCP_NODELAY, java.lang.Boolean.TRUE)

    serverBootstrap.childHandler(new CARTServerChannelHandler(decisionTreeNode))


    val future = serverBootstrap.bind(hostname, port).sync()

    future.channel().closeFuture().sync()

    bossGroup.shutdownGracefully()
    workerGroup.shutdownGracefully()

  }

  class CARTServerChannelHandler(decisionTreeNode: DecisionTreeNode) extends ChannelInitializer[SocketChannel] {

    //初始化channel
    override def initChannel(c: SocketChannel): Unit = {
      c.pipeline().addLast(new DelimiterBasedFrameDecoder(10240, Unpooled.copiedBuffer("\u000A".getBytes())))
      c.pipeline().addLast(new StringDecoder())
      c.pipeline().addLast(new CARTServerInboundHandler(decisionTreeNode))

    }
  }


  /**
   *
   * @param decisionTreeNode
   */
  class CARTServerInboundHandler(decisionTreeNode: DecisionTreeNode) extends ChannelInboundHandlerAdapter {

    override def channelRead(ctx: ChannelHandlerContext, msg: Any): Unit = {
      val request = JsonUtil.fromJson[Request](msg.toString)
      val timestamp = System.currentTimeMillis()


      //获取样本点
      val sample = CARTServer.getSample(request.requestStr, CARTServer.charIndexMap, CARTServer.indexCharMap)
      //进行预测
      val responseStr = CART.predict(decisionTreeNode, sample)


      val response = JsonUtil.toJson(new Response(request.id, timestamp, request.requestStr, responseStr, timestamp - request.timeStamp)) + "\n"

      val messageByteBuf = Unpooled.buffer(response.length)
      messageByteBuf.writeBytes(response.toString.getBytes)

      ctx.write(messageByteBuf)
    }

    override def channelReadComplete(ctx: ChannelHandlerContext): Unit = {
      ctx.flush()
    }

    override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable): Unit = {
      cause.printStackTrace()
      ctx.close()
    }

  }

}

object CARTServer {

  val hostname = "10.142.224.188"
  val port = 10000

  val charIndexMap = mutable.Map[Character, Integer]()
  val indexCharMap = mutable.Map[Integer, Character]()

  def main(args: Array[String]): Unit = {

    //val input_path = "/Users/chenwei/Downloads/param4.txt";

    val input_path = "/project/flink/conf/param4.txt";

    val targetArrayList = ArrayBuffer[String]()
    val sampleArrayList = ArrayBuffer[String]()

    for (line <- Source.fromFile(input_path, "UTF-8").getLines()) {
      val values = line.split("\\|", 2)

      targetArrayList.append(values(0))
      sampleArrayList.append(values(1))
      for (ch <- values(1).toCharArray) {
        if (!charIndexMap.contains(ch)) {
          charIndexMap.put(ch, charIndexMap.size)
          indexCharMap.put(indexCharMap.size, ch)
        }
      }
    }

    val samples = new Array[Array[Int]](sampleArrayList.size)
    val targets = new Array[String](sampleArrayList.size)
    val samplesStr = new Array[String](sampleArrayList.size)

    for (i <- sampleArrayList.indices) {
      val sampleStr = sampleArrayList(i)

      samples(i) = getSample(sampleStr, charIndexMap, indexCharMap);
      targets(i) = targetArrayList(i);
      samplesStr(i) = sampleArrayList(i) + "|" + targetArrayList(i);

    }

    val trainTestSplit = MlUtil.trainTestSplit(samples, targets, 0.5, samplesStr);
    val decisionTreeNode = CART.fit(trainTestSplit.trainSamples, trainTestSplit.trainTarget);

    println("train complete!")
    new CARTServer(hostname, port, decisionTreeNode).bind()

  }

  /**
   * 将字符串转换成样本数组
   *
   * @param sampleStr
   * @param charIndexMap
   * @param indexCharMap
   * @return
   */
  def getSample(sampleStr: String, charIndexMap: mutable.Map[Character, Integer],
                indexCharMap: mutable.Map[Integer, Character]): Array[Int] = {

    val sample = new Array[Int](charIndexMap.size + 12)

    if (sampleStr != null && sampleStr.length > 0) {
      for (ch <- sampleStr.toCharArray()) {
        if (charIndexMap.contains(ch)) {
          sample(charIndexMap(ch)) += 1
        } else {
          sample(charIndexMap.size + 11) += 1

        }

      }
    }

    for (j <- 0 until 5) {

      sample(j + charIndexMap.size) = if (j < sampleStr.length) sampleStr.charAt(j) else 0

    }

    for (j <- 0 until 5) {
      sample(j + charIndexMap.size + 5) = if (j < sampleStr.length()) sampleStr
        .charAt(sampleStr.length() - j - 1) else 0
    }

    sample(charIndexMap.size + 10) = sampleStr.length()

    sample
  }
}
