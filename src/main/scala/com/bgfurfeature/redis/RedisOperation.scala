package com.bgfurfeature.redis

import com.bgfurfeature.config.Conf
import redis.clients.jedis.Jedis
object RedisOperation {
  var readJedis: Jedis = _
  var writeJedis: Jedis = _

  def checkAlive {
    if (!isConnected(readJedis))
      readJedis = reConect(readJedis)
    if (!isConnected(writeJedis))
      writeJedis = reConect(writeJedis)
  }

  def getConn(ip: String, port: Int, passwd: String) = {
    val jedis = new Jedis(ip, port, 5000)
    jedis.connect
    if (passwd.length > 0)
      jedis.auth(passwd)
    jedis
  }

  def isConnected(jedis: Jedis) = jedis != null && jedis.isConnected

  def reConect(jedis: Jedis) = {
    println("reconnecting ...")
    disConnect(jedis)
    getConn(Conf.redisIp, Conf.redisPort, Conf.passwd)
  }

  def disConnect(jedis: Jedis) {
    if (jedis != null && jedis.isConnected()) {
      jedis.close
    }
  }

}
