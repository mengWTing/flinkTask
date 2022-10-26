/*
 * @project test-netty
 * @company Fujian Fujitsu Communication Software Co., Ltd.
 * @author chenwei
 * @date 2019-10-28 14:46:17
 * @version v1.0
 * @update [no] [date YYYY-MM-DD] [name] [description]
 */
package cn.ffcs.is.mss.analyzer.ml.tree.server

/**
  *
  * @author chenwei
  * @date 2019-10-28 14:46:17
  * @title Response
  * @update [no] [date YYYY-MM-DD] [name] [description]
  */
case class Response(id:String, timeStamp:Long, requestStr:String, responseStr: String, timeOut:Long) extends Serializable {

  override def toString: String = {

    "{" +
      "id:" + id + "," +
      "timeStamp:" + timeStamp.toString + "," +
      "request:" + requestStr +
      "response:" + requestStr +
      "timeOut:" + timeOut.toString +
      "}"

  }

}
