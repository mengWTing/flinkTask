/*
 * @project mss
 * @company Fujian Fujitsu Communication Software Co., Ltd.
 * @author chenwei
 * @date 2019-12-02 18:23:14
 * @version v1.0
 * @update [no] [date YYYY-MM-DD] [name] [description]
 */
package cn.ffcs.is.mss.analyzer.ml.tree.server

import java.util.UUID

/**
 *
 * @author chenwei
 * @date 2019-12-02 18:23:14
 * @title Request
 * @update [no] [date YYYY-MM-DD] [name] [description]
 */
case class Request(id:String, timeStamp:Long, requestStr:String)  extends Serializable {


  override def toString: String = {
    "{" +
      "id:" + id +"," +
      "timeStamp:" + timeStamp.toString + "," +
      "request:" + requestStr +
      "}"

  }

}

object Request {

  def getNewUUID(): String = {
    UUID.randomUUID().toString

  }
}
