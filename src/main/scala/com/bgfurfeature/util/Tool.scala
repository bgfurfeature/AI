package com.bgfurfeature.util

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}

import com.bgfurfeature.config.Conf

import scala.collection.mutable.ArrayBuffer

/**
  * com.bgfurfeature.util
  * Created by C.J.YOU on 2019/12/13.
  */
object Tool {

  def decodeArray(input: Array[Byte]): Array[Array[Long]] = {
    val bais = new ByteArrayInputStream(input)
    val dis = new java.io.DataInputStream(bais)
    val len = dis.readInt()
    val buf = new ArrayBuffer[Array[Long]]()
    for (i <- 0 until len) {
      val arr = new ArrayBuffer[Long]()
      for (j <- 0 until Conf.RECORD_SZ)
        arr += dis.readLong
      buf += arr.toArray
    }
    buf.toArray
  }

  def encodeArray(newTable: Array[Array[Long]]): Array[Byte] = {
    val baos = new ByteArrayOutputStream
    val dos = new java.io.DataOutputStream(baos)
    dos.writeInt(newTable.length)
    for (i <- 0 until newTable.length)
      for (j <- 0 until Conf.RECORD_SZ) {
        dos.writeLong(newTable(i)(j))
      }
    baos.toByteArray
  }

  def isInvalidate(timestamp: Long, duration: Long) = {
    val threshold = System.currentTimeMillis / 1000 - duration
    timestamp < threshold
  }

}
