package com.usercase.request

import com.bgfurfeature.config.Dom4jParser
import com.usercase.request.http.{HttpData, Notice}
import com.usercase.request.parser.RespondParserReflect
import com.usercase.request.util.TypeTransform

import scala.io.Source

/**
  * Created by C.J.YOU on 2017/2/21.
  */
object Start {

  def main(args: Array[String]) {

    val Array(xmlFile) = args

    val parser = Dom4jParser.apply(xmlFilePath = xmlFile)

    val httpRequestFilePath = parser.getParameterByTagName("File.url")

    val requestHeaderPath = parser.getParameterByTagName("File.header")

    val reflectClassName = parser.getParameterByTagName("RelectClass.FX")
    val reflectClassName2 = parser.getParameterByTagName("RelectClass.WK")

    val myFlectfx = new RespondParserReflect(reflectClassName)

    val myFlectwk = new RespondParserReflect(reflectClassName2)

    val header = Source.fromFile(requestHeaderPath).getLines().toList

    val httpData = HttpData.apply(header(0), "", parser)

    Notice.apply(parser)

    Source.fromFile(httpRequestFilePath).getLines().foreach{ line =>

      val ls = line.split("\t")
      val http = ls(0)
      val param = TypeTransform.listToHashMap(ls(1).split("&").toList)
      val method = ls(2)

      // println(ls.foreach(println))

      val res = myFlectfx.runMethod(method, http, param, httpData)

      // val res2 = myFlectwk.runMethod(method, http, param, httpData)

      println(res)

    }
  }

}
