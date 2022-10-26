/*
 * @project mss
 * @company Fujian Fujitsu Communication Software Co., Ltd.
 * @author chenwei
 * @date 2019-04-04 10:29:25
 * @version v1.0
 * @update [no] [date YYYY-MM-DD] [name] [description]
 */
package cn.ffcs.is.mss.analyzer.utils

import java.util.Properties

import redis.clients.jedis.{JedisPool, JedisPoolConfig}

/**
  *
  * @author chenwei
  * @date 2019-04-04 10:29:25
  * @title JedisUtil
  * @update [no] [date YYYY-MM-DD] [name] [description]
  */
object JedisUtil {
  def getJedisPool(properties: Properties): JedisPool = {

    val config = new JedisPoolConfig()
    //连接耗尽时是否阻塞, false报异常,ture阻塞直到超时, 默认true
    config.setBlockWhenExhausted(properties.getProperty("redis.blockWhenExhausted", "true").trim
    ().toBoolean)

    //设置的逐出策略类名, 默认DefaultEvictionPolicy(当连接超过最大空闲时间,或连接数超过最大空闲连接数)
    config.setEvictionPolicyClassName(properties.getProperty("redis.evictionPolicyClassName",
      "org.apache.commons.pool2.impl.DefaultEvictionPolicy").trim())

    //是否启用pool的jmx管理功能, 默认true
    config.setJmxEnabled(properties.getProperty("redis.jmxEnabled", "true").trim().toBoolean)

    //MBean ObjectName = new ObjectName("org.apache.commons.pool2:type=GenericObjectPool,name=" +
    // "pool" + i); 默 认为"pool", JMX不熟,具体不知道是干啥的...默认就好.
    config.setJmxNamePrefix(properties.getProperty("redis.jmxNamePrefix", "pool").trim())

    //是否启用后进先出, 默认true
    config.setLifo(properties.getProperty("redis.lifo", "true").trim().toBoolean)

    //最大空闲连接数, 默认8个
    config.setMaxIdle(properties.getProperty("redis.maxIdle", "8").trim().toInt)

    //最大连接数, 默认8个
    config.setMaxTotal(properties.getProperty("redis.maxTotal", "8").trim().toInt)

    //获取连接时的最大等待毫秒数(如果设置为阻塞时BlockWhenExhausted),如果超时就抛异常, 小于零:阻塞不确定的时间,  默认-1
    config.setMaxWaitMillis(properties.getProperty("redis.maxWaitMillis", "-1").trim().toInt)

    //逐出连接的最小空闲时间 默认1800000毫秒(30分钟)
    config.setMinEvictableIdleTimeMillis(properties.getProperty("redis.minEvictableIdleTimeMillis", "1800000").trim().toInt)

    //最小空闲连接数, 默认0
    config.setMinIdle(properties.getProperty("redis.minIdle", "0").trim().toInt)

    //每次逐出检查时 逐出的最大数目 如果为负数就是 : 1/abs(n), 默认3
    config.setNumTestsPerEvictionRun(properties.getProperty("redis.numTestsPerEvictionRun", "3")
      .trim().toInt)

    //对象空闲多久后逐出, 当空闲时间>该值 且 空闲连接>最大空闲数 时直接逐出,不再根据MinEvictableIdleTimeMillis判断  (默认逐出策略)
    config.setSoftMinEvictableIdleTimeMillis(properties.getProperty("redis.softMinEvictableIdleTimeMillis", "1800000").trim().toInt)

    //在获取连接的时候检查有效性, 默认false
    config.setTestOnBorrow(properties.getProperty("redis.testOnBorrow", "false").trim().toBoolean)

    //在空闲时检查有效性, 默认false
    config.setTestWhileIdle(properties.getProperty("redis.testWhileIdle", "false").trim().toBoolean)

    //逐出扫描的时间间隔(毫秒) 如果为负数,则不运行逐出线程, 默认-1
    config.setTimeBetweenEvictionRunsMillis(properties.getProperty("redis.timeBetweenEvictionRunsMillis", "-1").trim().toInt)

    //redis host
    val host = properties.getProperty("redis.host").trim()
    //redis port
    val port = properties.getProperty("redis.port").trim().toInt
    //超时时间
    val timeOut = properties.getProperty("redis.timeout").trim().toInt
    //redis密码
    val password = properties.getProperty("redis.password").trim()

    //创建redis连接池
    new JedisPool(config, host, port, timeOut, password)
  }

}
