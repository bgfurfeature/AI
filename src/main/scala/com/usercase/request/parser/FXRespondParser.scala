package com.usercase.request.parser

import com.usercase.request.http.HttpData
import org.json.JSONObject

/**
  * Created by C.J.YOU on 2017/2/21.
  */
class FXRespondParser(url: String, parameter: scala.collection.mutable.HashMap[String,String], _responder: HttpData){

  // 定义同http请求返回数据的解析函数

  /** status == 1 方法
    * get_news_trend
    * ajax_get_relate_shg
    */

  def get_single_real_time_hot = {

    val resp = _responder.request(url, parameter)

    url + "=" + new JSONObject(resp).get("status") == "-1"

  }
  def get_related_info = {

    val resp = _responder.request(url, parameter)

    url + "=" + new JSONObject(resp).get("status") == "-1"

  }

  def getHotData = {

    val resp = _responder.request(url, parameter)
    url + "=" + new JSONObject(resp).get("status") == "-1"

  }

}
