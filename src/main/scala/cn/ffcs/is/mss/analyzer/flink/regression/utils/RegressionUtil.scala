package cn.ffcs.is.mss.analyzer.flink.regression.utils

import java.sql.{Date, Timestamp}
import java.util

import cn.ffcs.is.mss.analyzer.utils.{DruidUtil, TimeUtil}
import cn.ffcs.is.mss.analyzer.utils.druid.entity._

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.collection.JavaConversions._
object RegressionUtil {

  /**
    * 查询druid然后填充空的值
    *
    * @param startTimeStamp
    * @param endTimeStamp
    * @param entity
    * @param aggregationsSet
    * @param map
    */
  def queryDruidFillEmptyValue(startTimeStamp: Long, endTimeStamp: Long, entity: Entity,
                               aggregationsSet: collection.Set[Aggregation],
                               map: mutable.Map[Long, mutable.Map[Aggregation, Double]]): Unit = {

    //查询startTimeStamp到endTimeStamp范围内druid的数据
    val resultList = DruidUtil.query(entity)
    for (i <- resultList) {

      val timeStamp = i.getOrDefault(Entity.timestamp, "0").toLong

      aggregationsSet.foreach(aggregations => {
        map.getOrElseUpdate(timeStamp, mutable.Map[Aggregation, Double]())
          .put(aggregations, i.getOrDefault(aggregations.toString, "0").toDouble)
      })
    }

    for (time <- startTimeStamp until endTimeStamp by TimeUtil.MINUTE_MILLISECOND) {
      if (!map.contains(time)) {
        aggregationsSet.foreach(aggregations => {
          map.getOrElseUpdate(time, mutable.Map[Aggregation, Double]()).put(aggregations, 1.0)
        })
      }
    }
  }

  def queryDruidFillSingleValue(dimensionName: String, startTimeStamp: Long, endTimeStamp: Long, entity: Entity,
                                aggregationSet: collection.Set[Aggregation],
                                map: mutable.Map[String, util.TreeMap[Long, mutable.Map[Aggregation, Double]]]): Unit = {
    val resultList = DruidUtil.query(entity)
    for (i <- resultList) {
      val timeStamp = i.getOrDefault(Entity.timestamp, "0").toLong
      val field = i(dimensionName)
      aggregationSet.foreach(aggregation => {
        map.getOrElseUpdate(field, new util.TreeMap[Long, mutable.Map[Aggregation, Double]])
          .getOrElseUpdate(timeStamp, new mutable.HashMap[Aggregation, Double]).put(aggregation, i.getOrDefault
        (aggregation.toString, "0").toDouble)
      })
    }
    for (time <- startTimeStamp until endTimeStamp by TimeUtil.MINUTE_MILLISECOND) {
      for (fieldName <- map.keySet) {
        if (!map(fieldName).contains(time)) {
          aggregationSet.foreach(aggregation => {
            map(fieldName).getOrElseUpdate(time, mutable.Map[Aggregation, Double]()).put(aggregation, 1.0)
          })
        }
      }
    }

  }

  /**
    * 获取插入数据库的回归值
    * @param arrayBuffer
    * @return
    */
  def getRegressionText(arrayBuffer: ArrayBuffer[Long]): String = {
    val stringBuilder = new StringBuilder()

    for (i <- arrayBuffer.indices) {
      stringBuilder.append(arrayBuffer(i)).append("|")
    }

    for (i <- arrayBuffer.length until 1440) {
      stringBuilder.append("0").append("|")
    }
    if (stringBuilder.nonEmpty) {
      stringBuilder.deleteCharAt(stringBuilder.length - 1)
    }

    stringBuilder.toString()
  }

  /**
    * 获取告警等级
    * @param realValue
    * @param regressionValue
    * @param array
    * @return
    */
  def getWarnLevel(realValue: Double, regressionValue: Double, array: Array[Double]): Int = {
    //计算偏差差值(真实值-预测值)/预测值
    val deviationPer = (realValue - regressionValue) / regressionValue

    for (i <- array.indices.reverse) {
      if (array(i) < deviationPer) {
        return i + 1
      }
    }

    return 0
  }

  /**
    * 获取告警等级数组
    * @param warnLevel
    * @return
    */
  def getWarnLevelArray(warnLevel: String): Array[Double] = {

    val warnLevels = warnLevel.split("\\|", -1)
    warnLevels.map(_.toDouble).sorted
  }

  /**
    * 增加最新统计的值
    *
    * @param current
    * @param timeStamp
    * @param value
    * @param aggregation
    */
  def updateValue(current: mutable.Map[Long, mutable.Map[Aggregation, Double]], timeStamp: Long, value: Double,
                  aggregation: Aggregation): Unit = {
    current.getOrElseUpdate(timeStamp, mutable.Map[Aggregation, Double]()).put(aggregation, value)
  }


  /**
    * 计算预测值
    *
    * @param timeStamp
    * @param aggregation
    * @param k
    * @param map
    * @return
    */
  def predict(timeStamp: Long, aggregation: Aggregation, k: Double, map: mutable.Map[Long, mutable.Map[Aggregation,
    Double]]): Double = {

    val arrayBuffer = ArrayBuffer[(Double, Double)]()
    //生成预测的arrayBuffer
    map.foreach(tuple => arrayBuffer += ((tuple._1.toDouble / TimeUtil.MINUTE_MILLISECOND, tuple._2(aggregation))))
    return LocallyWeightedLinearRegression.predict(arrayBuffer, k, (timeStamp + TimeUtil.MINUTE_MILLISECOND) /
      TimeUtil.MINUTE_MILLISECOND)
  }


  def isWarnNew(regressionValue: mutable.Map[Aggregation, ArrayBuffer[Long]], map: mutable.Map[Aggregation,
    ArrayBuffer[Double]], aggregation: Aggregation, currentCount: mutable.Map[Aggregation, Double], scopeOfWarn: Long,
                array: Array[Double]): Boolean = {
    val rangeMax = map.get(aggregation).get.takeRight(scopeOfWarn.toInt).max
    val regression = math.max(rangeMax, regressionValue.get(aggregation).get.last)
    if (RegressionUtil.getWarnLevel(currentCount.get(aggregation).get, regression, array) > 0) {
      return true
    } else {
      return false
    }
  }


  /**
    * 判断是否出告警
    * 根据当前值一定范围内的最大值来判断
    * @param regressionValue
    * @param map
    * @param aggregation
    * @param timeStamp
    * @param scopeOfWarn
    * @param array
    * @return
    */
  def isWarn(regressionValue: Long, map: mutable.Map[Long, mutable.Map[Aggregation, Double]],
             aggregation: Aggregation, timeStamp: Long, scopeOfWarn: Long, array: Array[Double]): Boolean = {

    //计算范围内的最大值，除了当前值的最大值
    val t = map.filter(tuple => {
      timeStamp - tuple._1 < scopeOfWarn &&
        timeStamp != tuple._1})
      .values.map(_.getOrElse(aggregation, 0.0))

    var rangeMax = 0.0
    if (t.nonEmpty) {
      rangeMax = t.max
    }
    //取范围内的最大值和回归值中较大的作为判断告警的回归值
    val regression = math.max(rangeMax, regressionValue)
    //如果告警等级大于0则产生告警
    if (RegressionUtil.getWarnLevel(map(timeStamp)(aggregation), regression, array) > 0) {
      return true
    } else {
      return false
    }
  }


  /**
    * 根据计算的结果通过反射获取告警对象
    *
    * @param realValue
    * @param regressionValue
    * @param timeStamp
    * @param warnLevel
    * @param warnObject
    * @return
    */
  def getAllUserWarnObject(realValue: Double, regressionValue: Double, timeStamp: Long, warnLevel: Int, warnObject: Class[_ <: Object]): Object = {

    val warnEntity = warnObject.newInstance().asInstanceOf[ {
      def setRealValue(long: java.lang.Long)
      def setRegressionValue(long: java.lang.Long)
      def setWarnDatetime(timestamp: Timestamp)
      def setWarnLevel(integer: java.lang.Integer)}]

    warnEntity.setRealValue(realValue.toLong)
    warnEntity.setRegressionValue(regressionValue.toLong)
    warnEntity.setWarnDatetime(new Timestamp(timeStamp))
    warnEntity.setWarnLevel(warnLevel)

    warnEntity.asInstanceOf[Object]
  }

  /**
    * 根据计算的结果通过反射获取回归对象
    *
    * @param regressionValueText
    * @param timeStamp
    * @param regressionObject
    * @return
    */
  def getAllUserRegressionObject(regressionValueText: String, timeStamp: Long, regressionObject: Class[_ <: Object]): Object = {

    val regressionEntity = regressionObject.newInstance().asInstanceOf[ {def setRegressionValueText(string: String)
      def setRegressionDate(date: Date)}]

    regressionEntity.setRegressionDate(new Date(timeStamp))
    regressionEntity.setRegressionValueText(regressionValueText)

    regressionEntity.asInstanceOf[Object]
  }

  /**
    * 根据时间戳获取整体回归的查询串
    * @param startTimeStamp
    * @param endTimeStamp
    * @return
    */
  def getAllUserQueryEntity(startTimeStamp: Long, endTimeStamp: Long, tableName: String,
                            aggregationsSet: collection.Set[Aggregation]): Entity = {
    val entity = new Entity()
    entity.setTableName(tableName)
    entity.setDimensionsSet(Dimension.getDimensionsSet())
    entity.setAggregationsSet(aggregationsSet.map(tuple => Aggregation.getAggregation(tuple)))
    entity.setGranularity(Granularity.getGranularity(Granularity.periodMinute, 1))
    entity.setStartTimeStr(startTimeStamp)
    entity.setEndTimeStr(endTimeStamp)
    return entity
  }


  /**
    * 根据计算的结果通过反射获取告警对象
    * @param systemName
    * @param realValue
    * @param regressionValue
    * @param timeStamp
    * @param warnLevel
    * @param warnObject
    * @return
    */
  def getSingleSystemWarnObject(systemName:String,realValue:Double,regressionValue:Double,timeStamp:Long,warnLevel:Int,warnObject: Class[_<:Object]): Object ={

    val warnEntity = warnObject.newInstance().asInstanceOf[{
      def setSystemName(systemName: String)
      def setRealValue(long: java.lang.Long)
      def setRegressionValue(long: java.lang.Long)
      def setWarnDatetime(timestamp: Timestamp)
      def setWarnLevel(integer: java.lang.Integer)}]

    warnEntity.setSystemName(systemName)
    warnEntity.setRealValue(realValue.toLong)
    warnEntity.setRegressionValue(regressionValue.toLong)
    warnEntity.setWarnDatetime(new Timestamp(timeStamp))
    warnEntity.setWarnLevel(warnLevel)

    warnEntity.asInstanceOf[Object]
  }

  /**
    * 根据计算的结果通过反射获取回归对象
    * @param systemName
    * @param regressionValueText
    * @param timeStamp
    * @param regressionObject
    * @return
    */
  def getSingleSystemRegressionObject(systemName:String,regressionValueText:String,timeStamp:Long,regressionObject: Class[_<:Object]): Object ={

    val regressionEntity = regressionObject.newInstance().asInstanceOf[{
      def setSystemName(systemName: String)
      def setRegressionValueText(string: String)
      def setRegressionDate(date: Date)}]

    regressionEntity.setSystemName(systemName)
    regressionEntity.setRegressionDate(new Date(timeStamp))
    regressionEntity.setRegressionValueText(regressionValueText)
    regressionEntity.asInstanceOf[Object]
  }

  /**
    * 根据时间戳获取查询指定系统数据的查询串
    * @param startTimeStamp
    * @param endTimeStamp
    * @return
    */
  def getSingleSystemQueryEntity(startTimeStamp: Long, endTimeStamp: Long, tableName : String,
                                 systemName: String, aggregationsSet : collection.Set[Aggregation]): Entity ={
    val entity = new Entity()
    entity.setTableName(tableName)
    entity.setDimensionsSet(Dimension.getDimensionsSet(Dimension.loginSystem))
    entity.setAggregationsSet(aggregationsSet.map(tuple => Aggregation.getAggregation(tuple)))
    entity.setGranularity(Granularity.getGranularity(Granularity.periodMinute, 1))
    entity.setFilter(Filter.getFilter(Filter.selector, Dimension.loginSystem, systemName))
    entity.setStartTimeStr(startTimeStamp)
    entity.setEndTimeStr(endTimeStamp)
    entity
  }

  /**
    * 根据计算的结果通过反射获取告警对象
    * @param provinceName
    * @param realValue
    * @param regressionValue
    * @param timeStamp
    * @param warnLevel
    * @param warnObject
    * @return
    */
  def getSingleProvinceWarnObject(provinceName:String,realValue:Double,regressionValue:Double,
                                  timeStamp:Long,warnLevel:Int,warnObject: Class[_<:Object]): Object ={

    val warnEntity = warnObject.newInstance().asInstanceOf[{
      def setProvinceName(provinceName: String)
      def setRealValue(long: java.lang.Long)
      def setRegressionValue(long: java.lang.Long)
      def setWarnDatetime(timestamp: Timestamp)
      def setWarnLevel(integer: java.lang.Integer)}]

    warnEntity.setProvinceName(provinceName)
    warnEntity.setRealValue(realValue.toLong)
    warnEntity.setRegressionValue(regressionValue.toLong)
    warnEntity.setWarnDatetime(new Timestamp(timeStamp))
    warnEntity.setWarnLevel(warnLevel)

    warnEntity.asInstanceOf[Object]
  }

  /**
    * 根据计算的结果通过反射获取回归对象
    * @param provinceName
    * @param regressionValueText
    * @param timeStamp
    * @param regressionObject
    * @return
    */
  def getSingleProvinceRegressionObject(provinceName:String,regressionValueText:String,
                                        timeStamp:Long,regressionObject: Class[_<:Object]): Object ={

    val regressionEntity = regressionObject.newInstance().asInstanceOf[{
      def setProvinceName(systemName: String)
      def setRegressionValueText(string: String)
      def setRegressionDate(date: Date)}]

    regressionEntity.setProvinceName(provinceName)
    regressionEntity.setRegressionDate(new Date(timeStamp))
    regressionEntity.setRegressionValueText(regressionValueText)

    regressionEntity.asInstanceOf[Object]
  }


  /**
    * 根据时间戳获取查询指定系统数据的查询串
    * @param startTimeStamp
    * @param endTimeStamp
    * @param tableName
    * @param provinceName
    * @param aggregationsSet
    * @return
    */
  def getSingleProvinceQueryEntity(startTimeStamp: Long, endTimeStamp: Long, tableName : String,
                                   provinceName: String, aggregationsSet : collection.Set[Aggregation]): Entity ={
    val entity = new Entity()
    entity.setTableName(tableName)
    entity.setDimensionsSet(Dimension.getDimensionsSet(Dimension.loginPlace))
    entity.setAggregationsSet(aggregationsSet.map(tuple => Aggregation.getAggregation(tuple)))
    entity.setGranularity(Granularity.getGranularity(Granularity.periodMinute,1))
    entity.setFilter(Filter.getFilter(Filter.selector,Dimension.loginPlace,provinceName))
    entity.setStartTimeStr(startTimeStamp)
    entity.setEndTimeStr(endTimeStamp)
    entity
  }

  def getAllSingleProvinceQueryEntity(startTimeStamp: Long, endTimeStamp: Long, tableName: String,
                                      aggregationsSet: collection.Set[Aggregation]): Entity = {
    val entity = new Entity()
    entity.setTableName(tableName)
    entity.setDimensionsSet(Dimension.getDimensionsSet(Dimension.loginPlace))
    entity.setAggregationsSet(aggregationsSet.map(tuple => Aggregation.getAggregation(tuple)))
    entity.setGranularity(Granularity.getGranularity(Granularity.periodMinute, 1))
//    entity.setFilter(Filter.getFilter(Filter.selector, Dimension.loginPlace, "江苏"))
    entity.setStartTimeStr(startTimeStamp)

    entity.setEndTimeStr(endTimeStamp)
    entity
  }

  def getAllSingleSystemQueryEntity(startTimeStamp: Long, endTimeStamp: Long, tableName: String,
                                    aggregationsSet: collection.Set[Aggregation]): Entity = {
    val entity = new Entity()
    entity.setTableName(tableName)
    entity.setDimensionsSet(Dimension.getDimensionsSet(Dimension.loginSystem))
    entity.setAggregationsSet(aggregationsSet.map(tuple => Aggregation.getAggregation(tuple)))
    entity.setGranularity(Granularity.getGranularity(Granularity.periodMinute, 1))
//    entity.setFilter(Filter.getFilter(Filter.selector, Dimension.loginSystem, "采购辅助-全国"))
    entity.setStartTimeStr(startTimeStamp)
    entity.setEndTimeStr(endTimeStamp)
    entity
  }


}
