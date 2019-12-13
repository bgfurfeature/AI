package com.usercase.userbehavior.strategy

import com.bgfurfeature.config.Conf

import scala.collection.mutable.ArrayBuffer

/**
  * Author: lolaliva
  * Date: 12:20 PM 2019/12/13
  * mapWithState需要依赖的State设计
  *
  * mapWithState内部实现由于涉及多个map的合并，每个batch来的key，不存在map中，就会新增一个map存储。
  * 当map的个数超过一定阈值，就要合并
  * 通过使用外部redis来存储map的内容，通过获取redis数据来增量更新数据
  */
class FrequencyStrategy extends  RealStrategy {
  val MAX = 25
  override def getKeyFields = Array(Conf.INDEX_LOG_USER, Conf.INDEX_LOG_ITEM)

  override def update(log: Seq[Array[String]], previous: ArrayBuffer[Long]) = {
    var logTime = log.map(_(Conf.INDEX_LOG_TIME).toLong)
    if (logTime.length > MAX) {
      println("exceed max length:\n" + log.map { _.mkString("\t") }.mkString("\n"))
    }
    logTime = logTime.slice(logTime.length - MAX, logTime.length)
    val x = previous
    var status = trim(x)

    if (math.random < 0.00005)
    println(s"[processed]: qq:${log(0)(Conf.INDEX_LOG_ITEM)}, order:${log(0)(Conf.INDEX_LOG_USER)}${logTime(0)}" +
    s" at ${System.currentTimeMillis() / 1000}, x=${status.mkString(",")}")

    val shouldRemove = logTime.length + status.length - MAX
    if (shouldRemove > 0)
    for (i <- 0 until shouldRemove if status.length > 0)
    status.remove(0)

    status.toSeq ++= logTime

    status
  }

  def remove(arr: ArrayBuffer[Long], ele: Long) = {
  while (arr.length > 0 && arr(0) <= ele)
  arr.remove(0)
  arr
  }

}
