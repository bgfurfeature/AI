package com.bgfurfeature.hbase

import java.util

import org.apache.hadoop.hbase.client.Put
import org.apache.hadoop.hbase.util.Bytes

import scala.reflect.ClassTag

/**
  * Created by devops on 2017/4/7.
  */
trait HBase {

  def getBytes(string: String) = Bytes.toBytes(string)

  case class  DataFormatForTable(columnFamily: String, qualifier: String, content: String)

  // batch put to hbase
  def toJavaList(list: List[Put]) = {

    val arrayList = new util.ArrayList[Put]()

    list.foreach(arrayList.add)

    arrayList

  }

}
