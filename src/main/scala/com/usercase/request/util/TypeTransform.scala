package com.usercase.request.util

import scala.collection.mutable

/**
  * Created by C.J.YOU on 2017/2/22.
  */
object TypeTransform {

  // List -> HashMap
  def listToHashMap(p: List[String]) = {
    val map = new mutable.HashMap[String, String]()
    p.foreach { item =>
      map.+=(Tuple2(item.split("=")(0),item.split("=")(1)))
    }
    map
  }

  // JAVA Map -> String
  def jMapToString(cookies: java.util.Map[String, String]) = {

    var res = ""

    val en = cookies.entrySet().iterator()

    while (en.hasNext) {

      val item = en.next()

      res += item.getKey + "=" + item.getValue

      if(en.hasNext) {
        res += ";"
      }

    }

    res

  }

}
