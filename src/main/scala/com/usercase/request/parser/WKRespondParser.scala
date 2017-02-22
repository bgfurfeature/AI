package com.usercase.request.parser

import com.usercase.request.http.HttpData

/**
  * Created by C.J.YOU on 2017/2/21.
  */
class WKRespondParser(url: String, parameter: scala.collection.mutable.HashMap[String,String], _responder: HttpData) {

  def login = {

    _responder.requestWK(url, parameter)

  }

}
