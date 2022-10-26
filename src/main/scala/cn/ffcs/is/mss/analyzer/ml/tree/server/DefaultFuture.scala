/*
 * @project mss
 * @company Fujian Fujitsu Communication Software Co., Ltd.
 * @author chenwei
 * @date 2019-12-02 18:28:39
 * @version v1.0
 * @update [no] [date YYYY-MM-DD] [name] [description]
 */
package cn.ffcs.is.mss.analyzer.ml.tree.server

import java.util.concurrent.{ConcurrentHashMap, TimeUnit}
import java.util.concurrent.locks.ReentrantLock

import io.netty.channel.Channel

import scala.util.control.Breaks

/**
 *
 * @author chenwei
 * @date 2019-12-02 18:28:39
 * @title DefaultFuture
 * @update [no] [date YYYY-MM-DD] [name] [description]
 */
class DefaultFuture(id:String, channel : Channel, request:Request) {

  //RPC响应消息
  var response: Response = _
  var timeOut: Long = _
  //响应消息处理互斥锁，get()、doReceived()、setCallback()方法中使用
  private val lock = new ReentrantLock
  //请求响应模式Condition，通过get()中的await和doReceived()中的signal完成IO异步转RPC同步
  private val done = lock.newCondition

  def this(channel: Channel, timeOut: Long, request: Request) {
    this(request.id, channel, request)
    this.timeOut = timeOut
    DefaultFuture.FUTURES.put(request.id, this)
    DefaultFuture.CHANNELS.put(request.id, channel)

  }

  def received(channel: Channel, response: Response): Unit = {

    try {

      val defaultFuture = DefaultFuture.FUTURES.remove(response.id)
      if (defaultFuture != null) {
        doReceived(response)
      }

    } finally {
      DefaultFuture.CHANNELS.remove(response.id)
    }

  }

  //将响应结果放入response，通知在done上等待的业务线程
  def doReceived(response: Response): Unit = {
    lock.lock()
    try {
      this.response = response
      if (done != null) {
        done.signal()
      }

    } finally {
      lock.unlock()
    }
  }

  def get(): Response = {
    if (timeOut <= 0) {
      timeOut = Long.MaxValue
    }

    if (!isDone()) {
      val time = System.currentTimeMillis()
      lock.lock()
      try {
        val loop = new Breaks;
        loop.breakable {
          while (!isDone()) {
            done.await(timeOut, TimeUnit.MILLISECONDS)
            if (isDone() || System.currentTimeMillis() - time > timeOut) {
              loop.break()
            }
          }
        }
      } finally {
        lock.unlock()
      }

    }
    this.response

  }

  def isDone(): Boolean = {
    response != null
  }

}

object DefaultFuture {
  //<请求ID,消息通道> 的映射关系
  lazy val CHANNELS = new ConcurrentHashMap[String, Channel]()
  //<请求ID,未完成状态的RPC请求> 的映射关系
  lazy val FUTURES = new ConcurrentHashMap[String, DefaultFuture]()
}
