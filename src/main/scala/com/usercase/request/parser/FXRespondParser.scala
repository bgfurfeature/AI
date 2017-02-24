package com.usercase.request.parser

import com.usercase.request.http.HttpData
import org.json.JSONObject

/**
  * Created by C.J.YOU on 2017/2/21.
  */
class FXRespondParser(var url: String, parameter: scala.collection.mutable.HashMap[String,String], _responder: HttpData){


  // 定义同http请求返回数据的解析函数(返回： url + 异常与否 + 中间结果)

  // 模糊搜索
  def btsearch = {

    val res = new Result()

    res.format("url",url).put("interfaceType","btsearch").put("接口:","模糊搜索")

    val resp = _responder.request(url, parameter.+=("token" -> _responder.token))

    val data = new JSONObject(resp)
    
    val status = data.getJSONObject("head").get("status").toString

    if( status == "1") {

      val body = data.getJSONObject("body").getJSONObject("prompt").getJSONArray("basic").length()

      res.resultFormat((body > 0).toString, body.toString)

    } else

      res.resultFormat("false", "None")

  }

  // 获取回测结果
  def btresult = {

    val res = new Result()

    res.format("url",url).put("interfaceType","btresult").put("接口:","获取回测结果")

    val forUrl = url

    url = "http://61.147.114.67/cgi-bin/backtest/kensho/1/btsentence.fcgi?start_time=2016-05-12&end_time=2016-09-12&sonditions=[]&base_sessionid=-1"

    val sessionid = btsentence.get("result").toString

    if(sessionid != "") {

      val resp = _responder.request(
        forUrl,
        parameter.+=("token" -> _responder.token).+=("sessionid" -> sessionid)
      )
      val data = new JSONObject(resp)

      val status = data.getJSONObject("head").get("status").toString

      if(status == "1") {

        val operate_code = data.getJSONObject("head").get("operate_code")

        res.resultFormat( (operate_code != "" ).toString, operate_code.toString)

      } else

        res.resultFormat( "false", "None")

    }

  }

  //  获取收益率走势
  def btyield = {

    val res = new Result()

    res.format("url",url).put("interfaceType","btyield").put("接口:","获取收益率走势")

    val forUrl = url

     url = "http://61.147.114.67/cgi-bin/backtest/kensho/1/btsentence.fcgi?start_time=2016-05-12&end_time=2016-09-12&sonditions=[]&base_sessionid=-1"

    val sessionid = btsentence.get("result").toString

    if(sessionid != "") {

      val resp = _responder.request(
        forUrl,
        parameter.+=("token" -> _responder.token).+=("sessionid" -> sessionid)
      )

      val data = new JSONObject(resp)

      val status = data.getJSONObject("head").get("status").toString

      if( status == "1") {

        val operate_code = data.getJSONObject("head").get("operate_code")

        res.resultFormat((operate_code != "" ).toString, operate_code.toString)

      } else

        res.resultFormat("false", "None")

    }

  }

  // 请求回测结果
  def btsentence = {

    val res = new Result()

    res.format("url",url).put("interfaceType","btsentence").put("接口:","请求回测结果")


    val resp = _responder.request(url, parameter.+=("token" -> _responder.token))

    val data = new JSONObject(resp)

    val status = data.getJSONObject("head").get("status").toString

    if( status == "1") {

      val body = data.getJSONObject("body").get("bt_session")

      res.resultFormat((body!= "").toString, body.toString)

    } else

      res.resultFormat("false", "None")
  }


 // 获取语句
  def hotsuggest = {

    allsuggest

  }
  // 获取更多语句
  def allsuggest  = {

    val res = new Result()

    res.format("url",url).put("interfaceType","allsuggest").put("接口:","获取更多语句")


    val resp = _responder.request(url, parameter.+=("token" -> _responder.token))

    val data = new JSONObject(resp)

    val status = data.getJSONObject("head").get("status").toString

    if( status == "1") {

      val sents = data.getJSONObject("body").getJSONArray("sentences").length()

      res.resultFormat((sents > 0).toString, sents.toString)

    } else

      res.resultFormat("false", "None")

  }

}
