/*
 * @project mss
 * @company Fujian Fujitsu Communication Software Co., Ltd.
 * @author chenwei
 * @date 2019-12-02 18:29:10
 * @version v1.0
 * @update [no] [date YYYY-MM-DD] [name] [description]
 */
package cn.ffcs.is.mss.analyzer.ml.tree.server

import io.netty.buffer.Unpooled
import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.DelimiterBasedFrameDecoder
import io.netty.handler.codec.string.StringDecoder

/**
 *
 * @author chenwei
 * @date 2019-12-02 18:29:10
 * @title ClientChannelHandler
 * @update [no] [date YYYY-MM-DD] [name] [description]
 */
class ClientChannelHandler extends ChannelInitializer[SocketChannel] {
  override def initChannel(c: SocketChannel): Unit = {
    c.pipeline().addLast(new DelimiterBasedFrameDecoder(10240, Unpooled.copiedBuffer("\u000A".getBytes())))
    c.pipeline().addLast(new StringDecoder())
    c.pipeline().addLast(new CARTClientHandler)
  }
}
