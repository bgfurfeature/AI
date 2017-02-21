package com.usercase.request.parser

import com.usercase.request.http.HttpData

/**
  * Created by C.J.YOU on 2017/2/21.
  */
class RespondParser(url: String, parameter: scala.collection.mutable.HashMap[String,String],_responder: HttpData){

  // 定义同http请求返回数据的解析函数
  def http = {

    println("nihao http!!")

    url +  ":false"

  }

  def stock = {
    println("nihao stock!!")
    val resp = _responder.request(url, parameter)
    url + "=" + resp
  }

}
