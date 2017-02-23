package com.usercase.request

import com.bgfurfeature.config.Dom4jParser
import com.usercase.request.http.{HttpData, Notice}
import com.usercase.request.parser.RespondParserReflect
import com.usercase.request.util.{RespondTime, TypeTransform}

import scala.io.Source

/**
  * Created by C.J.YOU on 2017/2/21.
  */
object Start {


  private  var httpData:HttpData = null


  // 请求接口
  def httpTest(fileName:String, rclass:RespondParserReflect) = {

    Source.fromFile(fileName).getLines().foreach{ line =>

      val ls = line.split("\t")
      val http = ls(0)

      val param = TypeTransform.listToHashMap(ls(1).split("&").toList)

      val method = ls(2)

      // println(ls.foreach(println))

      val parameter = (method, http, param, httpData)

      val result = RespondTime.time(parameter, rclass.runMethod)


      println(result)

      /*if(result.get("status").toString != "true")
        println(result)*/



    }
  }

  def main(args: Array[String]) {

    val Array(xmlFile) = args

    val parser = Dom4jParser.apply(xmlFilePath = xmlFile)

    val httpRequestFilePath = parser.getParameterByTagName("File.url")

    val wkhttpRequestFilePath = parser.getParameterByTagName("File.url_wk")

    val requestHeaderPath = parser.getParameterByTagName("File.header")

    val reflectClassName = parser.getParameterByTagName("RelectClass.FX")

    val reflectClassName2 = parser.getParameterByTagName("RelectClass.WK")

    val myFlectfx = new RespondParserReflect(reflectClassName)

    val myFlectwk = new RespondParserReflect(reflectClassName2)

    val header = Source.fromFile(requestHeaderPath).getLines().toList

    httpData = HttpData.apply(header(0), "", parser)

    Notice.apply(parser)

    // FX test

    // httpTest(httpRequestFilePath, myFlectfx)

    // wk test

     httpTest(wkhttpRequestFilePath, myFlectwk)

  }

}
