package com.usercase.request.parser

import org.json.JSONObject

/**
  * Created by C.J.YOU on 2017/2/23.
  */
object Result {

  def  resultFormat(request:String, status: String, result: String, interfaceType:String = "None"): JSONObject = {

    val res = s"""{"url": "${request}","status":"${status}" ,"result": "${result}","interfaceType": "${interfaceType}"}"""

    new JSONObject(res)

  }

}
