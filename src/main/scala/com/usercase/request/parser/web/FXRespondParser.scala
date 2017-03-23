package com.usercase.request.parser.web

import com.usercase.request.http.HttpData
import com.usercase.request.parser.{RespondParser, Result}
import org.json.JSONObject

/**
  * Created by C.J.YOU on 2017/2/21.
  */
class FXRespondParser(var url: String, parameter: scala.collection.mutable.HashMap[String,String], _responder: HttpData)
  extends RespondParser {


  // ajax_result_stock
  def getResultStrock = {

    val res = new Result()

    val respond = _responder.requestWK(url, parameter.+=("sessionid" -> sessionid))

    val resp = respond._1

    url = respond._2

    println(resp)

  }



  // 多个接口间结果的使用
  var sessionid = ""


  // 定义同http请求返回数据的解析函数(返回： url + 异常与否 + 中间结果)

  // 模糊搜索
  def btsearch = {

    val res = new Result()

    val respond = _responder.request(url, parameter.+=("token" -> _responder.getToken))

    val resp = respond._1

    url = respond._2

    res.format("url",url).put("interfaceType","btsearch").put("接口:","模糊搜索")

    if (resp != "{}") {

      val data = new JSONObject(resp)

      val status = data.getJSONObject("head").get("status").toString

      if (status == "1") {

        val body = data.getJSONObject("body").getJSONObject("prompt").getJSONArray("basic").length()

        res.resultFormat((body > 0).toString, body.toString)

      } else

        res.resultFormat("false", "None")
    } else

      res.resultFormat("false", "None")


  }

  // 获取回测结果
  def btresult = {

    val res = new Result()

    res.format("interfaceType","btresult").put("接口:","获取回测结果")

    val forUrl = url

    // url = "http://61.147.114.67/cgi-bin/backtest/kensho/1/btsentence.fcgi?start_time=2016-05-12&end_time=2016-09-12&sonditions=[]&base_sessionid=-1"

    // val sessionid = btsentence.get("result").toString

    if(sessionid != "") {

      val respond = _responder.request(
        forUrl,
        parameter.+=("token" -> _responder.getToken).+=("sessionid" -> sessionid)
      )

      res.format("url",respond._2)

      val data = new JSONObject(respond._1)

      combineAllMethod(respond._1, res, Array("head", "status", "head","operate_code", "str"))

    }

  }

  //  获取收益率走势
  def btyield = {

    val res = new Result()

    res.format("interfaceType","btyield").put("接口:","获取收益率走势")

    val forUrl = url

     // url = "http://61.147.114.67/cgi-bin/backtest/kensho/1/btsentence.fcgi?start_time=2016-05-12&end_time=2016-09-12&sonditions=[]&base_sessionid=-1"

    // val sessionid = btsentence.get("result").toString

    if(sessionid != "") {

      val respond = _responder.request(
        forUrl,
        parameter.+=("token" -> _responder.getToken).+=("sessionid" -> sessionid)
      )

      url = respond._2

      res.format("url",url)

      combineAllMethod(respond._1, res, Array("head", "status", "head","operate_code", "str"))

    }

  }

  // 请求回测结果
  def btsentence = {

    val res = new Result()

    val respond = _responder.request(url, parameter.+=("token" -> _responder.getToken))

    url = respond._2

    res.format("url",url).put("interfaceType","btsentence").put("接口:","请求回测结果")

    combineAllMethod(respond._1, res, Array("head", "status", "body","bt_session", "str"))

    res.result

  }


 // 获取语句
  def hotsuggest = {

    allsuggest

  }
  // 获取更多语句
  def allsuggest  = {

    val res = new Result()

    val respond = _responder.request(url, parameter.+=("token" -> _responder.getToken))

    url = respond._2

    res.format("url",url).put("interfaceType","allsuggest").put("接口:","获取更多语句")

    combineAllMethod(respond._1, res, Array("head", "status", "body", "sentences", "length"))


  }

}
