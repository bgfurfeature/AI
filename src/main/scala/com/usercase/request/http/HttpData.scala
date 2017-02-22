package com.usercase.request.http

import java.util

import com.bgfurfeature.config.Dom4jParser
import com.usercase.request.util.TypeTransform
import org.json.JSONObject
import org.jsoup.Connection.Method
import org.jsoup.Jsoup

/**
  * Created by C.J.YOU on 2017/2/21.
  */
class HttpData(userAgent:String, cookie:String, parser:Dom4jParser) {

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
  private def getCookies(cookieStr: String): util.Map[String, String] = {

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

  /**
    * 消息通知
    * @param strUrl
    * @param parameters
    * @return
    */
  def notification(strUrl:String, parameters:scala.collection.mutable.HashMap[String,String]) = {

    val finalUrl = getUrl(strUrl, parameters)

    val respond = Jsoup.connect(finalUrl)
      .timeout(5000)
      .execute()
      .body()

    respond

  }

  def requestWK(strUrl:String, parameters:scala.collection.mutable.HashMap[String,String]): String = {

    val finalUrl = getUrl(strUrl, parameters)

    val respond = Jsoup.connect(finalUrl)
      .timeout(5000)
      .userAgent(userAgent)
      .cookies(getCookies(getCookie))
      .execute()
      .body()

    respond

  }

  def request(strUrl:String, parameters:scala.collection.mutable.HashMap[String,String]): String = {

    val finalUrl = getUrl(strUrl, parameters)

    println(finalUrl)

    val respond = Jsoup.connect(finalUrl)
      .timeout(10000)
      .execute()
      .body()

    respond

  }

  def getCookie = {

    var cookiesG = ""

    // login
    val login = "http://stock.iwookong.com/ajax/login/user_login.php"

    try {
      val loginRespond = Jsoup.connect(login)
        .data(
          "platform_id", parser.getParameterByTagName("Login.platform_id"),
          "user_name", parser.getParameterByTagName("Login.user_name"),
          "password", parser.getParameterByTagName("Login.password"),
          "autologin", parser.getParameterByTagName("Login.autologin")
        ).method(Method.POST)
        .userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.143 Safari/537.36")
        .timeout(5000)
        .execute()

      val cookies = TypeTransform.jMapToString(loginRespond.cookies())

      cookiesG = cookies

    } finally {

      if(cookiesG == "")
        Notice.getInstance.emailNotice(" can't not login !!!!" )
    }


    cookiesG

  }

  def token = {

    var token = ""
    // get token
    val  strUrl = "http://61.147.114.67/cgi-bin/backtest/user/1/user_login.fcgi"

    try {
      val respond = Jsoup.connect(strUrl)
        .data(
          "platform_id", parser.getParameterByTagName("LoginMD.platform_id"),
          "user_name", parser.getParameterByTagName("LoginMD.user_name"),
          "password", parser.getParameterByTagName("LoginMD.password")
        ).timeout(5000)
        .execute()

      val tokenR = new JSONObject(respond.body())
        .getJSONObject("result")
        .getJSONObject("user_info")
        .get("token").toString

      token = tokenR

    } catch {
      case e: Exception =>
    } finally {
      // token warning
      if(token == "")

        Notice.getInstance.emailNotice(" can't not get token!!!! ")
    }

    token

  }

}

object HttpData {

  private var hp: HttpData = null

  def apply(userAgent: String, cookie: String, parser:Dom4jParser ): HttpData = {

    if(hp == null)
      hp = new HttpData(userAgent, cookie, parser)

    hp

  }

  def getInstance = hp

  // 定期需要更新hp
}
