package com.usercase.request.http

import java.util

import org.jsoup.Jsoup

/**
  * Created by C.J.YOU on 2017/2/21.
  */
class HttpData(userAgent:String, cookie:String) {

  /**
    * 格式化成http请求的参数
    * @param parameter 参数HashMap
    * @return 参数
    */
  private def getParameters(parameter: scala.collection.mutable.HashMap[String,String]): String = {

    var strParam:String = ""
    val iterator = parameter.keySet.iterator

    while(iterator.hasNext) {

      val key = iterator.next()

      if(parameter.get(key) != null) {

        strParam += key + "=" + parameter.get(key).get

        if(iterator.hasNext) strParam += "&"

      }
    }

    strParam

  }

  /**
    * 得到最终的url
    * @param url 一级url域名
    * @param parameters 参数内容map
    * @return url
    */
  private  def getUrl(url:String, parameters:scala.collection.mutable.HashMap[String,String]): String = {

    val strParam = getParameters(parameters)
    var strUrl = url

    if (strParam != null) {

      if (url.indexOf("?") >= 0)
        strUrl += "&" + strParam
      else
        strUrl += "?" + strParam

    }

    strUrl
  }

  /**
    * @param cookieStr cookie
    * @return 返回键值对的cookie
    */
  private  def getCookies(cookieStr: String): util.HashMap[String, String] = {

    val cookieMap = new util.HashMap[String, String]()
    val cookieArr = cookieStr.split(";")

    for (line <- cookieArr) {

      val lineArr = line.split("=")

      if (lineArr.length > 1) {
        cookieMap.put(lineArr(0), lineArr(1))
      }

    }

    cookieMap

  }

  def request(strUrl:String, parameters:scala.collection.mutable.HashMap[String,String]): String = {

    val finalUrl = getUrl(strUrl, parameters)

    val respond = Jsoup.connect(finalUrl)
      .userAgent(userAgent).timeout(50000)
      .cookies(getCookies(cookie)).execute()
      .body()

    respond



  }

}
