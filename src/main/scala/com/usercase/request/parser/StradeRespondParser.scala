package com.usercase.request.parser

import com.usercase.request.http.HttpData
import org.json.JSONObject

/**
  * Created by C.J.YOU on 2017/3/3.
  */
class StradeRespondParser( var url: String, parameter: scala.collection.mutable.HashMap[String,String], _responder: HttpData)

  extends RespondParser {

  // 行情数据插件接口
  // 股指
  def realInfoIndex = {

    val tuple = init(url, parameter, _responder)
    val res = tuple._1
    val resp = tuple._2
    url = tuple._3

    res.format("url",url).put("interfaceType","getBackTest").put("接口:","回测接口")

    if(resp != "{}") {

      val size = new JSONObject(resp).getJSONObject("body").get("count").toString

      res.resultFormat((size != "0").toString, size.toString)

    } else {

      res.resultFormat("false", "None")

    }

  }



}
