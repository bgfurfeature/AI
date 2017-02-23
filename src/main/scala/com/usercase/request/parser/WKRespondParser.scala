package com.usercase.request.parser

import com.usercase.request.http.HttpData
import org.json.JSONObject

/**
  * Created by C.J.YOU on 2017/2/21.
  */
class WKRespondParser(url: String, parameter: scala.collection.mutable.HashMap[String,String], _responder: HttpData) {

  // 登陆操作
  def login = {

    _responder.requestWK(url, parameter)

  }

  // 11. ajax_get_hy_and_gn_hot.php
  def getHyAndGn = {

    val resp = _responder.requestWK(url, parameter)

    if(resp != "") {

      val status = new JSONObject(resp).get("status").toString

      if(status == "1") {

        val size = new JSONObject(resp).getJSONObject("visit").get("0").toString

        Result.resultFormat(url, (size != "").toString, size.toString)

      } else {

        Result.resultFormat(url , "false", "None")

      }

    } else {

      Result.resultFormat(url , "false", "None")

    }

  }

  //10. ajax_get_grail.php

  def getGrail = {

    getCurve

  }

  // 9: ajax_get_curve.php
  def getCurve = {

    val resp = _responder.requestWK(url, parameter)

    val status = new JSONObject(resp).get("status").toString

    if(status == "1") {

      if(url.contains("ajax_get_grail")) {

        val codeInfo = new JSONObject(resp).getJSONObject("result").getJSONObject("code_info").get("open").toString

        Result.resultFormat(url, (codeInfo != "").toString, codeInfo.toString)

      } else {

        val codeInfo = new JSONObject(resp).getJSONObject("result").getJSONArray("code_info").length()

        Result.resultFormat(url, (codeInfo > 0).toString, codeInfo.toString, interfaceType = "大盘数据接口")
      }


    } else {

      Result.resultFormat(url , "false", "None", interfaceType = "大盘数据接口")

    }

  }

  // 8. ajax_get_single_real_time_hot.php
  def getSingleRealTime = {

    getRealTimeHot

  }

  // 7. ajax_get_real_time_hot
  def getRealTimeHot = {

    val resp = _responder.requestWK(url, parameter)

    val status = new JSONObject(resp).get("status").toString

    if(status == "1") {

      val visit = new JSONObject(resp).getJSONObject("visit").get("0").toString

      Result.resultFormat(url, (visit != "").toString, visit.toString)

    } else {

      Result.resultFormat(url , "false", "None")

    }

  }

  // 6. ajax_get_news_trend.php
  def getNewTrend = {

    val resp = _responder.requestWK(url, parameter)

    if(resp != "") {

      val status = new JSONObject(resp).get("status").toString

      if(status == "1") {

        val size = new JSONObject(resp).getJSONArray("infotrend").length()

        Result.resultFormat(url, (size > 0).toString, size.toString)

      } else {

        Result.resultFormat(url , "false", "None")

      }

    } else {

      Result.resultFormat(url , "false", "None")

    }

  }

  // 5. ajax_get_hotrecord.php
  def getHotRecord = {

    val resp = _responder.requestWK(url, parameter)

    if(resp != "") {

      val status = new JSONObject(resp).get("status").toString

      if(status == "1") {

        val size = new JSONObject(resp).getJSONArray("visit").length()

        Result.resultFormat(url, (size > 0).toString, size.toString)

      } else {

        Result.resultFormat(url , "false", "None")

      }

    } else {

      Result.resultFormat(url , "false", "None")

    }

  }

  // 4. ajax_get_relate_shg.php
  // 关联谱图数据
  def getRelaeshg = {

    val resp = _responder.requestWK(url, parameter)

    if(resp != "") {

      val status = new JSONObject(resp).get("status").toString

      if(status == "1") {

        val jSONObject = new JSONObject(resp)

        val event = jSONObject.getJSONArray("event").length()

        val industry = jSONObject.getJSONArray("industry").length()

        val notion = jSONObject.getJSONArray("notion").length()

        val stock = jSONObject.getJSONArray("stock").length()

        val size = (event > 0 ) || industry > 0 || notion > 0 || stock > 0

        Result.resultFormat(url, size.toString, "event:"+event + ",industry:" + industry +",notion:"+ notion + ",stock:" + stock)

      } else {

        Result.resultFormat(url , "false", "None", interfaceType = "关联谱图数据接口")

      }

    } else {

      Result.resultFormat(url , "false", "None")

    }
  }

  // 3. ajax_get_rateline.php
  // 收益率数据接口
  def getRateLine = {

    val resp = _responder.requestWK(url, parameter)

    if(resp != "") {

      val size = new JSONObject(resp).getJSONObject("body").getJSONArray("list").length()

        Result.resultFormat(url, (size > 0).toString, size.toString)

    } else {

      Result.resultFormat(url , "false", "None")

    }

  }

  // 2. ajax_get_related_info.php
  // query_type 1 - 股票资讯 2 - 行业资讯 3 - 概念资讯
  // info_type_list //格式如 1,1,1,1,1,1
  //第一个数字1表示要获取关联的新闻，如果填0表示不获取
  //第二个数字1表示要获取关联的快讯，如果填0表示不获取
  //第三个数字1表示要获取关联的达人观点，如果填0表示不获取
  //第四个数字1表示要获取关联的股票，如果填0表示不获取
  //第五个数字1表示要获取关联的行业,如果填0表示不获取
  //第六个数字1表示要获取关联的概念，如果填0表示不获取
  //第七个数字1表示要获取关联的公告，如果填0表示不获取
  //第八个数字1表示要获取关联的研报，如果填0表示不获取
  // fast_info: 快讯
  // notice: 公告
  // report: 研报
  // me_media： 达人
  // news: 新闻
  // industry: 行业
  // stock : 股票
  // notion: 概念

  val relatedInfo = Map(
    0 -> "news",
    1 -> "fast_info",
    2 -> "me_media",
    3 -> "stock",
    4 -> "industry",
    5 -> "notion",
    6 -> "notice",
    7 -> "report"
  )
  def getRelatedInfo = {

    val resp = _responder.requestWK(url, parameter)

    if(resp != "") {

      val jSONObject = new JSONObject(resp)

       val status = jSONObject.get("status").toString

       if(status == "1") {

         var golbalString = ""

         var status = "true"

         val urlType = parameter.get("info_type_list").get.split(",").zipWithIndex.foreach {

           case ("0", index) =>

           case ("1", index) =>

             val stype = relatedInfo.get(index).get

             val typeInfo = jSONObject.getJSONArray(stype).length()

             if(typeInfo <= 0) {
               status = "false"
               golbalString += stype + ":" + typeInfo + ","
             }


         }

         Result.resultFormat(url , status , golbalString)

         /*val news = jSONObject.getJSONArray("news").length()

         val fast_info = jSONObject.getJSONArray("fast_info").length()

         val me_media = jSONObject.getJSONArray("me_media").length()

         val stock = jSONObject.getJSONArray("stock").length()

         val industry = jSONObject.getJSONArray("industry").length()

         val notion = jSONObject.getJSONArray("notion").length()

         val notice = jSONObject.getJSONArray("notice").length()

         val report = jSONObject.getJSONArray("report").length()

         val size = (news > 0 ) && industry > 0 && notion > 0 && stock > 0 && fast_info >0 && me_media > 0 && notice > 0 && report > 0

         Result.resultFormat(url, size.toString,
             "news:"+ news + ",industry:" + industry +",notion:"+ notion + ",stock:" + stock +
             ",me_media:" + me_media +",fast_info:"+ fast_info + ",notice:" + notice + ",report:" +
               report, interfaceType = "关联资讯数据接口")*/

       } else {

         Result.resultFormat(url , "false", "None")

       }

    } else {

     Result.resultFormat(url , "false", "None")

    }


  }

  // 1. ajax_get_hot_data.php
  def getHotData = {

    val resp = _responder.requestWK(url, parameter)

    val status = new JSONObject(resp).get("status").toString

    if(status == "1") {

      val size = new JSONObject(resp).getJSONObject("result").getJSONObject("code_info").getJSONArray("shf_").length()

      Result.resultFormat(url, (size > 0).toString, size.toString)

    } else {

      Result.resultFormat(url , "false", "None")

    }

  }

}
