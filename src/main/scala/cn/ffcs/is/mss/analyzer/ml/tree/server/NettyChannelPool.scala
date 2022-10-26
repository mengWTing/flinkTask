/*
 * @project test-netty
 * @company Fujian Fujitsu Communication Software Co., Ltd.
 * @author chenwei
 * @date 2019-10-28 15:58:42
 * @version v1.0
 * @update [no] [date YYYY-MM-DD] [name] [description]
 */
package cn.ffcs.is.mss.analyzer.ml.tree.server

import io.netty.bootstrap.Bootstrap
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.channel.{Channel, ChannelOption}
import io.netty.handler.logging.{LogLevel, LoggingHandler}

import scala.util.Random

/**
  *
  * @author chenwei
  * @date 2019-10-28 15:58:42
  * @title NettyChannelPool
  * @update [no] [date YYYY-MM-DD] [name] [description]
  */
class NettyChannelPool(MAX_CHANNEL_COUNT:Int) {
  var channels: Array[Channel] = new Array[Channel](MAX_CHANNEL_COUNT)
  var locks: Array[Any] = new Array[Any](MAX_CHANNEL_COUNT)
  val group = new NioEventLoopGroup(10)

  def this() {
    this(4)
  }


  def connect(host: String, port: Int): Unit = {
    for (i <- channels.indices) {
      channels(i) = connectToServer(host: String, port: Int)
    }

  }

  def connectToServer(host: String, port: Int): Channel = {

    val bootstrap = new Bootstrap
    bootstrap.group(group)
      .channel(classOf[NioSocketChannel])
      .option(ChannelOption.SO_KEEPALIVE, java.lang.Boolean.TRUE)
      .option(ChannelOption.TCP_NODELAY, java.lang.Boolean.TRUE)
      .handler(new LoggingHandler(LogLevel.INFO))
      .handler(new ClientChannelHandler)

    val channelFuture = bootstrap.connect(host, port)
    val channel = channelFuture.sync.channel

    channel
  }

  def getChannel(): Channel = {

    channels(Random.nextInt(channels.length))

  }

  def close(): Unit = {
    for (channel <- channels) {
      channel.close()
      channel.closeFuture().sync()
    }

    group.shutdownGracefully()
  }
}
