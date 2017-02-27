package com.usercase.request.task

import java.util.concurrent.Callable

import com.usercase.request.http.HttpData
import com.usercase.request.parser.RespondParserReflect
import com.usercase.request.util.{RespondTime, TypeTransform}
import org.json.JSONObject

/**
  * Created by C.J.YOU on 2017/2/21.
  */
class Task (line: String, httpData: HttpData, rclass:RespondParserReflect) extends Callable[JSONObject] {

  override def call(): (JSONObject) = {

    var result: JSONObject = null

    try {
      val ls = line.split("\t")

      val http = ls(0)

      val param = TypeTransform.listToHashMap(ls(1).split("&").toList)

      val method = ls(2)

      val parameter = (method, http, param, httpData)

      result = RespondTime.time(parameter, rclass.runMethod)

    } catch {

      case e:Exception =>

        result.put("RT","3000")

    }


    result


  }
}