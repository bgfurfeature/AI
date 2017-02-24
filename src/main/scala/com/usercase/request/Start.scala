package com.usercase.request

import com.bgfurfeature.config.Dom4jParser
import com.bgfurfeature.log.CLogger
import com.usercase.request.http._
import com.usercase.request.parser.RespondParserReflect
import com.usercase.request.util.{RespondTime, TypeTransform}
import org.json.JSONObject

import scala.collection.mutable.ListBuffer
import scala.io.Source

/**
  * Created by C.J.YOU on 2017/2/21.
  */
object Start  extends CLogger {


  private  var httpData:HttpData = null


  // 请求接口
  def httpTest(fileName:String, rclass:RespondParserReflect) = {

    val resLb = new ListBuffer[JSONObject]

    Source.fromFile(fileName).getLines().foreach { line =>

      val ls = line.split("\t")
      val http = ls(0)

      val param = TypeTransform.listToHashMap(ls(1).split("&").toList)

      val method = ls(2)

      // println(ls.foreach(println))

      val parameter = (method, http, param, httpData)

      val result = RespondTime.time(parameter, rclass.runMethod)

      resLb.+=(result)

    }

    resLb

  }

  def main(args: Array[String]) {

    val Array(xmlFile) = args

    val parser = Dom4jParser.apply(xmlFilePath = xmlFile)

    val httpRequestFilePath = parser.getParameterByTagName("File.url_fx")

    val httpRequestFilePathTest = parser.getParameterByTagName("File.url_test")

    val wkhttpRequestFilePath = parser.getParameterByTagName("File.url_wk")

    val requestHeaderPath = parser.getParameterByTagName("File.header")

    val reflectClassName = parser.getParameterByTagName("RelectClass.FX")

    val reflectClassName2 = parser.getParameterByTagName("RelectClass.WK")

    val logConfigFile =  parser.getParameterByTagName("Logger.path")

    val myFlectfx = new RespondParserReflect(reflectClassName)

    val myFlectwk = new RespondParserReflect(reflectClassName2)


    logConfigure(logConfigFile)

    val header = Source.fromFile(requestHeaderPath).getLines().toList

    httpData = HttpData.apply("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.143 Safari/537.36","", parser)

    JsonTypeNotice.apply(parser)

    // FX test
    val fx_res = httpTest(httpRequestFilePath, myFlectfx)

    // fx_res.foreach(println)

    // wk test
    val res = httpTest(wkhttpRequestFilePath, myFlectwk).++=:(fx_res)

    val notice = JsonTypeNotice.getInstance.asInstanceOf[JsonTypeNotice]

    res.foreach { x=>

      notice.notice(x)

    }

    notice.clearSet



  }
}
