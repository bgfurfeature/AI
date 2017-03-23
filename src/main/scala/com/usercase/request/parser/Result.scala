package com.usercase.request.parser

import org.json
import org.json.JSONObject

/**
  * Created by C.J.YOU on 2017/2/23.
  */
class Result {

  private var jSONObject:JSONObject = new json.JSONObject()

  def result =  jSONObject

  def format(key:String, value: String) = {


    this.jSONObject = jSONObject.put(key,value)

    this.jSONObject

  }

  def  resultFormat(status: String = "false", result: String = "None"): JSONObject = {

    this.jSONObject = jSONObject.put("status",status).put("result",result)

    this.jSONObject

  }

}
