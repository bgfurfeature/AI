package com.usercase.userbehavior.strategy

import com.bgfurfeature.util.Tool

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

/**
  * Author: lolaliva
  * Date: 12:18 PM 2019/12/13 
  */
trait RealStrategy extends Serializable {
  val TRIM_DURATION = 3600 * 72

  def getKeyFields: Array[Int]

  def update(log: Seq[Array[String]], previous: ArrayBuffer[Long]): mutable.IndexedBuffer[Long]

  def trim(timestamps: ArrayBuffer[Long]) = trimHelper(timestamps, TRIM_DURATION)

  def trimHelper(timestamps: ArrayBuffer[Long], duration: Long) = {
    var i = 0
    while (i < timestamps.length && Tool.isInvalidate(timestamps(i), duration)) i += 1
    timestamps.slice(i, timestamps.length)
  }

}