package com.usercase.request.util

import com.usercase.request.http.HttpData
import org.json.JSONObject

import scala.collection.mutable

/**
  * Created by C.J.YOU on 2017/2/23.
  */
object RespondTime {


  type T = (String, String, mutable.HashMap[String,String], HttpData)

  // 返回： url + 异常与否 + 中间结果 + 相应时间
  def time(parameter:T ,function: (T) => JSONObject): JSONObject = {

    val startTime = System.currentTimeMillis()

    val res = function(parameter._1,parameter._2,parameter._3,parameter._4)

    val endTime = System.currentTimeMillis()

    val timeR = (endTime - startTime).toString

    res.put("RT", timeR)

  }

}
