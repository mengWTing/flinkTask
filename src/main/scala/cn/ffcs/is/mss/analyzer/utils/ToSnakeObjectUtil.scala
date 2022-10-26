package cn.ffcs.is.mss.analyzer.utils

import com.fasterxml.jackson.databind.{ObjectMapper, PropertyNamingStrategy}

/**
  * @Title Test
  * @Author ZF
  * @Date 2020-06-19 12:06
  * @Description 将下划线转换为驼峰的形式，例如：user_name-->userName
  *             列子：
  *              val s:String = "{\"id\":\"2\",\"user_name\":\"3\"}"
  *              toSnakeObject(s,classOf[AlarmRulesEntity])
  * @update [no][date YYYY-MM-DD][name][description]
  */
object ToSnakeObjectUtil {
  def  toSnakeObject[T](json:String,clazz:Class[T]) ={
    val mapper = new ObjectMapper()
    mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE)
    val snakeObject = mapper.readValue(json, clazz)
    snakeObject
  }
}
