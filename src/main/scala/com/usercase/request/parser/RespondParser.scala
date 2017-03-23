package com.usercase.request.parser

import com.usercase.request.http.HttpData
import org.json.JSONObject

/**
  * Created by C.J.YOU on 2017/2/27.
  */
class RespondParser {

  // init
  def init(url: String, parameter: scala.collection.mutable.HashMap[String,String], _responder: HttpData) = {

    val res = new Result

    val respond = _responder.requestWK(url, parameter)

    val resp = respond._1

    (res, resp, respond._2)

  }

  // 将其中重复代码提取出来
  def combineAllMethod(resp: String, res: Result, args:Array[String]) = {

    if (resp != "{}") {

      val data = new JSONObject(resp)

      val status = data.getJSONObject(args(0)).get(args(1)).toString

      if( status == "1") {

        if(args(4) == "length") {

          val sents = data.getJSONObject(args(2)).getJSONArray(args(3)).length()

          res.resultFormat((sents > 0).toString, sents.toString)

        } else if (args(4) == "str"){

          val operate_code = data.getJSONObject(args(2)).get(args(3))

          res.resultFormat( (operate_code != "" ).toString, operate_code.toString)
        }

      } else

        res.resultFormat("false", "None")

    } else {

      res.resultFormat("false", "None")

    }

    res.result


  }


}
