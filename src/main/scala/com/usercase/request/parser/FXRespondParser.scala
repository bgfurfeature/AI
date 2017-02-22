package com.usercase.request.parser

import com.usercase.request.http.HttpData
import org.json.JSONObject

/**
  * Created by C.J.YOU on 2017/2/21.
  */
class FXRespondParser(var url: String, parameter: scala.collection.mutable.HashMap[String,String], _responder: HttpData){

  // 定义同http请求返回数据的解析函数(返回： url + 异常与否 + 中间结果 + 相应时间)

  // 模糊搜索
  def btsearch = {

    val resp = _responder.request(url, parameter.+=("token" -> _responder.token))

    val data = new JSONObject(resp)



    val status = data.getJSONObject("head").get("status").toString

    if( status == "1") {

      val body = data.getJSONObject("body").getJSONObject("prompt").getJSONArray("basic").length()

      url + " : " + (body > 0 )+ " : " + body

    } else

      url + " : " + false + " : " + ""

  }

  // 获取回测结果
  def btresult = {

    val forUrl = url

    url = "http://61.147.114.67/cgi-bin/backtest/kensho/1/btsentence.fcgi?start_time=2016-05-12&end_time=2016-09-12&sonditions=[]&base_sessionid=-1"

    val sessionid = btsentence.split(" : ")(2)

    if(sessionid != "") {

      val resp = _responder.request(
        forUrl,
        parameter.+=("token" -> _responder.token).+=("sessionid" -> sessionid)
      )
      val data = new JSONObject(resp)

      val status = data.getJSONObject("head").get("status").toString

      if(status == "1") {

        val operate_code = data.getJSONObject("head").get("operate_code")

        url + " : " + (operate_code != "") + " : " + ""

      } else

        url + " : " + false + " : " + ""

    }

  }

  //  获取收益率走势
  def btyield = {

    val forUrl = url

     url = "http://61.147.114.67/cgi-bin/backtest/kensho/1/btsentence.fcgi?start_time=2016-05-12&end_time=2016-09-12&sonditions=[]&base_sessionid=-1"

    val sessionid = btsentence.split(" : ")(2)

    if(sessionid != "") {

      val resp = _responder.request(
        forUrl,
        parameter.+=("token" -> _responder.token).+=("sessionid" -> sessionid)
      )

      val data = new JSONObject(resp)

      val status = data.getJSONObject("head").get("status").toString

      if( status == "1") {

        val operate_code = data.getJSONObject("head").get("operate_code")

        url + " : " + (operate_code != "") + " : " + ""

      } else

        url + " : " + false + " : " + ""

    }

  }

  // 请求回测结果
  def btsentence = {

    val resp = _responder.request(url, parameter.+=("token" -> _responder.token))

    val data = new JSONObject(resp)

    val status = data.getJSONObject("head").get("status").toString

    if( status == "1") {

      val body = data.getJSONObject("body").get("bt_session")

      url + " : " + (body != "") + " : " + body

    } else

      url + " : " + false + " : " + ""
  }


 // 获取语句
  def hotsuggest = {

    allsuggest

  }
  // 获取更多语句
  def allsuggest: String = {

    val resp = _responder.request(url, parameter.+=("token" -> _responder.token))

    val data = new JSONObject(resp)

    val status = data.getJSONObject("head").get("status").toString

    if( status == "1") {

      val sents = data.getJSONObject("body").getJSONArray("sentences").length()

      url + " : " + (sents > 0)

    } else

      url + " : " + false
  }
  def get_related_info = {

    val resp = _responder.request(url, parameter)

    url + " : " + new JSONObject(resp).get("status") == "1"

  }

   //
  def getHotData: String = {

    val resp = _responder.request(url, parameter)

    val status = new JSONObject(resp).get("status").toString

     if(status == "1") {

      val size = new JSONObject(resp).getJSONObject("result").getJSONObject("code_info").getJSONArray("ehf_").length()

       url + " : " + (size > 0)

    } else {

       url + " : " + false
     }

  }

}
