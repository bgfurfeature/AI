package com.usercase.userbehavior

import com.bgfurfeature.config.Conf
import com.bgfurfeature.redis.RedisOperation
import com.bgfurfeature.redis.RedisOperation.{checkAlive, reConect, readJedis, writeJedis}
import com.bgfurfeature.util.Tool
import redis.clients.jedis.exceptions.JedisConnectionException

import scala.collection.mutable.ArrayBuffer

/**
  * Author: lolaliva
  * Date: 11:54 AM ${Date} 
  */
object RedisDao {

  def constructKey(uid: String) = uid + "_freq"

  def updateRedis(_records: Seq[Conf.DT]) {
    val start = System.currentTimeMillis()
    val records = _records.map[Conf.DT, ArrayBuffer[(String, ArrayBuffer[(Long, Long)])]] {
      case (uid, updates) =>
        (constructKey(uid), updates)
    }
    val uids = records.map[String](x => x._1)
    val rawVals = batchGet(uids)
    val recordAndVal = records.zip[Array[Byte]](rawVals)
    val set = new ArrayBuffer[(Array[Byte], Array[Byte])]
    for (((key, updates), rawVal) <- recordAndVal) {
      updateRedis(key, updates, rawVal, set)
    }
    batchSet(set)
    if (math.random < 0.1)
      println(s"deal with ${records.length} records in ${System.currentTimeMillis() - start} ms.")
  }

  def updateRedis(key: String, updates: ArrayBuffer[(Long, Long)], rawVal: Array[Byte],
                  set: ArrayBuffer[(Array[Byte], Array[Byte])]) {
    var table = Array[Array[Long]]()
    if (rawVal != null && rawVal.length > 0) {
      val record = rawVal
      table = Tool.decodeArray(record)
      table = trimTable(table)
    }

    val newTable = updateRecord(table, updates, key)
    if (newTable.length != 0) {
      //encode array to byte array
      val newValue = Tool.encodeArray(newTable)
      set += ((key.getBytes, newValue))
    }
  }

  def updateRecord(_table: Array[Array[Long]], _vals: ArrayBuffer[(Long, Long)], key: String) = {

    val table = _table.map { x => (x(0), x(1)) }
    // print some log for observation
    if (table.length > 0 && math.random < 0.1) {
      println(s"query $key and get ${table.mkString("\t")}")
    }
    val vals = _vals.map[(Long, Long)](x => (x._1, x._2))
    val union = (table ++ vals).sorted

    val newTable = ArrayBuffer[Array[Long]]()
    var preItem = -1L
    var cnt = 0

    var i = union.length - 1
    while (i >= 0) {
      val (item, timestamp) = union(i)
      if (!Tool.isInvalidate(timestamp, Conf.windowSize)) {
        if (item == preItem) {
          cnt += 1
        } else {
          preItem = item
          cnt = 1
        }

        if (cnt <= Conf.MAX_CNT) {
          newTable += Array(item, timestamp)
        }
      }
      i -= 1
    }

    newTable.reverse.toArray
  }

  def trimTable(table: Array[Array[Long]]) = {
    val buf = new ArrayBuffer[Array[Long]]
    for (i <- 0 until table.length) {
      if (table(i).length == Conf.RECORD_SZ && table(i)(Conf.INDEX_TIEMSTAMP) > 0)
        buf += table(i)
      else {
        println("discard " + table(i).mkString(","))
      }
    }
    buf.toArray
  }

  def batchSet(kvs: Seq[(Array[Byte], Array[Byte])]) {
    try {
      checkAlive
      var i = 0
      while (i < kvs.length) {
        val pipeline = RedisOperation.writeJedis.pipelined
        val target = i + Conf.batchSize
        //println(s"set ${new String(kvs(0)._1)} to ${kvs(0)._2}")
        while (i < target && i < kvs.length) {
          pipeline.set(kvs(i)._1, kvs(i)._2)
          pipeline.expire(kvs(i)._1, Conf.EXPIRE_DURATION)
          i += 1
        }
        pipeline.sync
      }
    } catch {
      case connEx: JedisConnectionException => reConect(RedisOperation.writeJedis)
      case ex: Exception                    => ex.printStackTrace
    } finally {

    }
  }

  def batchGet(keys: Seq[String]) = {
    val res = ArrayBuffer[Array[Byte]]()
    try {
      checkAlive
      var i = 0
      while (i < keys.length) {
        val target = i + Conf.batchSize
        while (i < target && i < keys.length) {
          val resp = RedisOperation.readJedis.get(keys(i).getBytes)
          res += resp
          i += 1
        }
      }
      res
    } catch {
      case connEx: JedisConnectionException => reConect(RedisOperation.readJedis)
      case ex: Exception                    => ex.printStackTrace
    }
    res
  }

}
