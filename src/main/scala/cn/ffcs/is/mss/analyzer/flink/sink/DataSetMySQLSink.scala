package cn.ffcs.is.mss.analyzer.flink.sink

import java.io.{BufferedReader, InputStreamReader}
import java.net.URI
import java.util.Properties

import cn.ffcs.is.mss.analyzer.utils.{C3P0Util, Constants, SQLHelper}
import org.apache.flink.api.common.accumulators.LongCounter
import org.apache.flink.api.common.io.RichOutputFormat
import org.apache.flink.configuration.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}

class DataSetMySQLSink extends RichOutputFormat[(Object, Boolean)] {
  var sqlHelper: SQLHelper = _
  val messagesReceived = new LongCounter()
  val messagesExecuteSucceed = new LongCounter()
  val messagesExecuteFail = new LongCounter()


  override def configure(parameters: Configuration) = {

  }

  override def writeRecord(value: (Object, Boolean)) = {
    //记录收到的数据数
    messagesReceived.add(1)

    //记录是否更新成功
    var isSucceed = false
    //如果数据库中已经包含该条数据，就更新，否则插入
    val queryResult = sqlHelper.query(value._1, value._2)
    if (queryResult != null && queryResult.size() > 0) {
      isSucceed = sqlHelper.update(value._1)
    } else {
      isSucceed = sqlHelper.insert(value._1)
    }
    if (isSucceed) {
      messagesExecuteSucceed.add(1)
    } else {
      messagesExecuteFail.add(1)
    }
  }

  override def close() = {
    sqlHelper = null
  }

  override def open(taskNumber: Int, numTasks: Int) = {
    //获取全局变量
    val globConf = getRuntimeContext.getExecutionConfig.getGlobalJobParameters
      .asInstanceOf[Configuration]

    //根据c3p0配置文件,初始化c3p0连接池
    val c3p0Properties = new Properties()
    val c3p0ConfigPath = globConf.getString(Constants.c3p0_CONFIG_PATH, "")
    val systemType = globConf.getString(Constants.FILE_SYSTEM_TYPE, "")
    val fs = FileSystem.get(URI.create(systemType), new org.apache.hadoop.conf.Configuration())
    val fsDataInputStream = fs.open(new Path(c3p0ConfigPath))
    val bufferedReader = new BufferedReader(new InputStreamReader(fsDataInputStream))
    c3p0Properties.load(bufferedReader)
    C3P0Util.ini(c3p0Properties)

    //操作数据库的类
    sqlHelper = new SQLHelper()

    //增加Accumulator 记录收到的数据个数、执行成功个数、执行失败的个数
    getRuntimeContext.addAccumulator("MySQLSink: Messages received", messagesReceived)
    getRuntimeContext.addAccumulator("MySQLSink: Messages execute succeed", messagesExecuteSucceed)
    getRuntimeContext.addAccumulator("MySQLSink: Messages execute fail", messagesExecuteFail)


  }
}
