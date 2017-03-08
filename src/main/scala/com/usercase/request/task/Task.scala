package com.usercase.request.task

import java.util.concurrent.Callable

import com.usercase.request.http.HttpData
import com.usercase.request.parser.{RespondParserReflect, Result}
import com.usercase.request.util.{RespondTime, TypeTransform}
import org.json
import org.json.JSONObject

/**
  * Created by C.J.YOU on 2017/2/21.
  */
class Task (line: String, httpData: HttpData, rclass:RespondParserReflect) extends Callable[JSONObject] {

  override def call(): (JSONObject) = {

    var result: JSONObject = new json.JSONObject()

    var methodG = ""

    var httpG = ""

    try {
      val ls = line.split("\t")

      val http = ls(0)

      val param = TypeTransform.listToHashMap(ls(1).split("&").toList)

      val method = ls(2)

      val parameter = (method, http, param, httpData)

      methodG = method

      httpG = http

      result = RespondTime.time(parameter, rclass.runMethod)

    } catch {

      case e:Exception =>

        result = new Result().resultFormat().put("RT","3000")
          .put("TaskError","Exception").put("interfaceType",s"$methodG")
          .put("url",s"$httpG")

    }


    result


  }
}