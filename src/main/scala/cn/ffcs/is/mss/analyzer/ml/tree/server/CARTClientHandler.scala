/*
 * @project test-netty
 * @company Fujian Fujitsu Communication Software Co., Ltd.
 * @author chenwei
 * @date 2019-10-28 16:17:34
 * @version v1.0
 * @update [no] [date YYYY-MM-DD] [name] [description]
 */
package cn.ffcs.is.mss.analyzer.ml.tree.server

import cn.ffcs.is.mss.analyzer.utils.JsonUtil
import io.netty.channel.{ChannelHandlerContext, ChannelInboundHandlerAdapter}

/**
  *
  * @author chenwei
  * @date 2019-10-28 16:17:34
  * @title CARTClientHandler
  * @update [no] [date YYYY-MM-DD] [name] [description]
  */
class CARTClientHandler extends ChannelInboundHandlerAdapter {

  override def channelRead(ctx: ChannelHandlerContext, msg: Any): Unit = {
    val response = JsonUtil.fromJson[Response](msg.toString)

    val channel = ctx.channel()
    val defaultFuture = DefaultFuture.FUTURES.get(response.id)
    defaultFuture.received(channel, response)

  }

  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable): Unit = {
    cause.printStackTrace()
    ctx.close()
  }
}
