package com.usercase.request

import com.usercase.request.http.HttpData
import com.usercase.request.parser.RespondParserReflect
import org.json.JSONObject

import scala.collection.mutable
import scala.io.Source

/**
  * Created by C.J.YOU on 2017/2/21.
  */
object Start {

  private var httpData: HttpData = null

  // List -> HashMap
  def listToHashMap(p: List[String]) = {
    val map = new mutable.HashMap[String, String]()
    p.foreach { item =>
      map.+=(Tuple2(item.split("=")(0),item.split("=")(1)))
    }
    map
  }

  def main(args: Array[String]) {

    val xmlFile = ""

    val httpRequestFilePath = "F:\\datatest\\telecom\\wokong\\http"

    val requestHeaderPath = "F:\\datatest\\telecom\\wokong\\header"

    val myFlect = new RespondParserReflect("com.usercase.request.parser.FXRespondParser")

    val data = Source.fromFile(requestHeaderPath).getLines().toList

    httpData = new HttpData(data(0), data(1))

    Source.fromFile(httpRequestFilePath).getLines().foreach{ line =>

      val ls = line.split("\t")
      val http = ls(0)
      val param = listToHashMap(ls(1).split(",").toList)
      val method = ls(2)


      val res = myFlect.runMethod(method, http, param, httpData).split("=")(1)

      val jSONObject = new JSONObject(res).getJSONObject("result")

      println(jSONObject.getJSONObject("code_info").getJSONArray("ehf_").length())

    }
  }

}
