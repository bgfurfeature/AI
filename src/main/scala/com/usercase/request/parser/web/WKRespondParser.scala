package com.usercase.request.parser.web

import com.usercase.request.http.HttpData
import com.usercase.request.parser.RespondParser
import org.json.JSONObject

import scala.collection.mutable.ListBuffer

/**
  * Created by C.J.YOU on 2017/2/21.
  */
class WKRespondParser(var url: String, parameter: scala.collection.mutable.HashMap[String,String], _responder: HttpData)
  extends RespondParser  {

  // 登陆操作
  def login = {

    _responder.requestWK(url, parameter)

  }

  // 12.http://stock.iwookong.com/ajax/infocenter/ajax_get_back_test.php?
  // stocks_info=%2C600000%2C0.34%2C600030%2C0.45&start_time=%2C2017-02-13&end_time=%2C2017-02-28
  // body.list.length
  // body.count > 0

  def getBackTest = {

    val tuple = init(url, parameter, _responder)
    val res = tuple._1
    val resp = tuple._2
    url = tuple._3

    res.format("url",url).put("interfaceType","getBackTest").put("接口:","回测接口")

    if(resp != "{}") {

        val size = new JSONObject(resp).getJSONObject("body").get("count").toString

        res.resultFormat((size != "0").toString, size.toString)

    } else {

      res.resultFormat("false", "None")

    }


  }


  // 11. ajax_get_hy_and_gn_hot.php
  def getHyAndGn = {

    val tuple = init(url, parameter, _responder)
    val res = tuple._1
    val resp = tuple._2
    url = tuple._3

    res.format("url",url).put("interfaceType","getHyAndGn").put("接口:","行业，热度数据")

    if(resp != "{}") {

      val status = new JSONObject(resp).get("status").toString

      if(status == "1") {

        val size = new JSONObject(resp).getJSONObject("visit").get("0").toString

        res.resultFormat((size != "").toString, size.toString)


      } else {

        res.resultFormat( "false", "None")

      }

    } else {

      res.resultFormat("false", "None")

    }

  }

  //10. ajax_get_grail.php

  def getGrail = {

    val js = getCurve

    if(js.has("接口")) {
      js.remove("接口")
    }
    js.put("接口","大盘数据曲线")

  }

  // 9: ajax_get_curve.php
  def getCurve = {

    val tuple = init(url, parameter, _responder)
    val res = tuple._1
    val resp = tuple._2
    url = tuple._3

    res.format("url",url).put("interfaceType","getCurve").put("接口","股票大盘数据")

    val status = new JSONObject(resp).get("status").toString

    if(resp != "{}") {

      if(status == "1") {

        if(url.contains("ajax_get_grail")) {

          val codeInfo = new JSONObject(resp).getJSONObject("result").getJSONObject("code_info").get("open").toString

          res.resultFormat((codeInfo != "").toString, codeInfo.toString)

        } else {

          val codeInfo = new JSONObject(resp).getJSONObject("result").getJSONArray("code_info").length()

          res.resultFormat((codeInfo > 0).toString, codeInfo.toString)
        }


      } else {

        res.resultFormat( "false", "None")

      }

    }else {

      res.resultFormat( "false", "None")

    }

  }

  // 8. ajax_get_single_real_time_hot.php
  def getSingleRealTime = {

    getRealTimeHot

  }

  // 7. ajax_get_real_time_hot
  def getRealTimeHot = {

    val tuple = init(url, parameter, _responder)
    val res = tuple._1
    val resp = tuple._2
    url = tuple._3

    res.format("url",url).put("interfaceType","ajax_get_real_time_hot").put("接口:","A股市场实时热度数据")

    val status = new JSONObject(resp).get("status").toString

    if(status == "1") {

      val visit = new JSONObject(resp).getJSONObject("visit").get("0").toString

      res.resultFormat((visit != "").toString, visit.toString)

    } else {

      res.resultFormat( "false", "None")

    }

  }

  // 6. ajax_get_news_trend.php
  def getNewTrend = {

    val tuple = init(url, parameter, _responder)
    val res = tuple._1
    val resp = tuple._2
    url = tuple._3

    res.format("url",url).put("interfaceType","ajax_get_news_trend").put("接口:","新闻走势")

    if(resp != "{}") {

      val status = new JSONObject(resp).get("status").toString

      if(status == "1") {

        val size = new JSONObject(resp).getJSONArray("infotrend").length()

        res.resultFormat((size > 0).toString, size.toString)

      } else {

        res.resultFormat("false", "None")

      }

    } else {

      res.resultFormat("false", "None")

    }

  }

  // 5. ajax_get_hotrecord.php
  def getHotRecord = {

    val tuple = init(url, parameter, _responder)
    val res = tuple._1
    val resp = tuple._2
    url = tuple._3

    res.format("url",url).put("interfaceType","ajax_get_hotrecord").put("接口:","单只股票月热度数据")

    if(resp != "{}") {

      val status = new JSONObject(resp).get("status").toString

      if(status == "1") {

        val size = new JSONObject(resp).getJSONArray("visit").length()

        res.resultFormat((size > 0).toString, size.toString)

      } else {

        res.resultFormat("false", "None")

      }

    } else {

      res.resultFormat("false", "None")

    }

  }

  // 4. ajax_get_relate_shg.php
  // 关联谱图数据
  def getRelaeshg = {

    val tuple = init(url, parameter, _responder)
    val res = tuple._1
    val resp = tuple._2
    url = tuple._3

    res.format("url",url).put("interfaceType","ajax_get_relate_shg").put("接口:","关联谱图数据")

    if(resp != "{}") {

      val items = Array("event","industry","notion","stock")

      val status = new JSONObject(resp).get("status").toString

      if(status == "1") {

        val jSONObject = new JSONObject(resp)

        var flag = "false"

        var lb = new ListBuffer[String]

        items.foreach { item =>

          val value = jSONObject.getJSONArray(item).length()

          if(value > 0)
            flag = "true"

          lb.+=(s"$item:$value")

        }

        res.resultFormat(flag,lb.mkString(","))

      } else {

        res.resultFormat("false", "None")

      }

    } else {

      res.resultFormat("false", "None")

    }
  }

  // 3. ajax_get_rateline.php
  // 收益率数据接口
  def getRateLine = {

    val tuple = init(url, parameter, _responder)
    val res = tuple._1
    val resp = tuple._2
    url = tuple._3

    res.format("url",url).put("interfaceType","ajax_get_rateline").put("接口:","收益率数据接口")

    if(resp != "{}") {

      val size = new JSONObject(resp).getJSONObject("body").getJSONArray("list").length()

        res.resultFormat((size > 0).toString, size.toString)

    } else {

      res.resultFormat("false", "None")

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

    val tuple = init(url, parameter, _responder)
    val res = tuple._1
    val resp = tuple._2
    url = tuple._3

    res.format("url",url).put("interfaceType","ajax_get_related_info").put("接口:","关联资讯")

    if(resp != "{}") {

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
          res.resultFormat(status , if(golbalString.nonEmpty) golbalString else "all works right")

       } else {

         res.resultFormat("false", "None")

       }

    } else {

     res.resultFormat("false", "None")

    }


  }

  // 1. ajax_get_hot_data.php
  // ajax_get_hot_data.php	1=1	getHotData
  // ajax_get_hot_data.php	hottype=hy&hotval=有色金属	getHotData
  // ajax_get_hot_data.php	hottype=gn&hotval=大盘	getHotData
  def getHotData = {

    val tuple = init(url, parameter, _responder)
    val res = tuple._1
    val resp = tuple._2
    url = tuple._3

    res.format("url",url).put("interfaceType","ajax_get_hot_data").put("接口:","所有热度排行")

    val items = Array("ehf_","ehs_","ehv_","euf_","eus_","euv_","ghf_","ghs_","ghv_","guf_","gus_","guv_","hhf_","hhs_","hhv_","huf_","hus_","huv_","shf_","shv_","shs_","suf_","sus_","suv_")

    if(resp != "{}") {

      val status = new JSONObject(resp).get("status").toString

      if(status == "1") {

        var flag = "true"

        var lb = new ListBuffer[String]

        val codeInfo = new JSONObject(resp).getJSONObject("result").getJSONObject("code_info")

        items.foreach { item =>

          if(codeInfo.has(item)) {

            val size = codeInfo.getJSONArray(item).length()

            if(size < 0)
              flag = "false"

            lb.+=(s"$item:$size")

          }
        }

        res.resultFormat(flag, lb.mkString(","))

      } else {

        res.resultFormat("false", "None")

      }

    } else {

      res.resultFormat("false", "None")

    }

  }

}
