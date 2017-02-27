package com.usercase.request

import com.bgfurfeature.log.CLogger
import com.bgfurfeature.util.FileUtil
import org.json
import org.json.JSONObject
import org.jsoup.Connection.Method
import org.jsoup.Jsoup

import scala.collection.mutable.ListBuffer
import scala.io.Source

/**
  * Created by C.J.YOU on 2017/2/23.
  */
object DataPrepare  extends CLogger {
  
  var js: JSONObject = null

  // wookong data load
  def loadTestData(baseData:String, url:String, savePath: String)= {

    Source.fromFile(baseData).getLines().foreach { x =>

      val jsTemp= new JSONObject(x)

      js = jsTemp

    }

    if(js.get("dataUpdate").toString == "true") {

      warnLog(logFileInfo, "重新获取测试url列表！！")

      val urlsLB = new ListBuffer[String]

      val uid = js.get("uid").toString

      Source.fromFile(url).getLines().toList.map(_.split("\t")).map(x=> (x(0),x(1))).foreach {
          // 股票，行业，和概念的关联资讯 https
          case (url_, "getRelatedInfo") =>

            val info_type_list_stock = js.get("info_type_list_stock").toString
            val info_type_list_hy = js.get("info_type_list_hy").toString
            val info_type_list_gn = js.get("info_type_list_gn").toString

            js.get("stock").toString.split(",").foreach{ x =>

              val finalUrl = url_ + "\t" + s"query_type=1&key=$x&start_id=0&info_type_list=$info_type_list_stock&start_time=0" + "\tgetRelatedInfo"

              urlsLB.+=(finalUrl)
            }

            js.get("hy").toString.split(",").foreach{ x =>

              val finalUrl = url_ + "\t" + s"query_type=2&key=$x&start_id=0&info_type_list=$info_type_list_hy&start_time=0" + "\tgetRelatedInfo"
              urlsLB.+=(finalUrl)

            }

            js.get("gn").toString.split(",").foreach { x =>

              val finalUrl = url_ + "\t" + s"query_type=3&key=$x&start_id=0&info_type_list=$info_type_list_gn&start_time=0" + "\tgetRelatedInfo"

              urlsLB.+=(finalUrl)

            }
          // 股票收益率
          case (url_, "getRateLine") =>

            js.get("stock").toString.split(",").foreach{ x =>

              val finalUrl = url_ + "\t" + s"query_type=stock&query_key=$x&query_date=today" + "\tgetRateLine"

              urlsLB.+=(finalUrl)
            }

          // 单只股票月热度数据
          case (url_, "getHotRecord")=>

            js.get("stock").toString.split(",").foreach{ x =>

              val finalUrl = url_ + "\t" + s"query_type=0&key_name=$x&time_type=month" + "\tgetHotRecord"

              urlsLB.+=(finalUrl)
            }

          // 单只股票实时数据
          case (url_, "getSingleRealTime") =>

            js.get("stock").toString.split(",").foreach{ x =>

              val finalUrl = url_ + "\t" + s"stock=$x&minute_data=minute_data&hour_data=index" + "\tgetSingleRealTime"

              urlsLB.+=(finalUrl)
            }

          // A股市场实时热度
          case (url_, "getRealTimeHot") =>

            val finalUrl_trend = url_ + "\t" + s"minute_data=minute_data" + "\tgetSingleRealTime"
            val finalUrl_hot = url_ + "\t" + s"hour_data=index" + "\tgetSingleRealTime"

            urlsLB.+=(finalUrl_trend)
            urlsLB.+=(finalUrl_hot)

          // 大盘数据
          case (url_, "getCurve")  =>

            js.get("stock").toString.split(",").foreach{ x =>

              val finalUrl = url_ + "\t" + s"code=$x" + "\tgetCurve"

              urlsLB.+=(finalUrl)
            }

          // 行业和概念热度
          case (url_, "getHyAndGn") =>

            js.get("hy").toString.split(",").foreach{ x =>

              val finalUrl_min = url_ + "\t" + s"name=$x&query_type=1&minute_data=minute_data" + "\tgetHyAndGn"
              // val finalUrl_hour = url_ + "\t" + s"name=$x&query_type=1&minute_data=minute_data" + "\tgetHyAndGn"
              urlsLB.+=(finalUrl_min)
            }

            js.get("gn").toString.split(",").foreach{ x =>

              val finalUrl_min = url_ + "\t" + s"name=$x&query_type=2&minute_data=minute_data" + "\tgetHyAndGn"
              // val finalUrl_hour = url_ + "\t" + s"name=$x&query_type=1&minute_data=minute_data" + "\tgetHyAndGn"
              urlsLB.+=(finalUrl_min)
            }

          // 新闻趋势
          case (url_, "getNewTrend") =>
            js.get("stock").toString.split(",").foreach{ x =>

              val finalUrl = url_ + "\t" + s"query_type=1&key_name=$x" + "\tgetNewTrend"

              urlsLB.+=(finalUrl)
            }

          // 关联谱图
          case (url_, "getRelaeshg") =>

            js.get("stock").toString.split(",").foreach{ x =>

              val finalUrl = url_ + "\t" + s"query_type=1&key_name=$x" + "\tgetRelaeshg"

              urlsLB.+=(finalUrl)
            }


          // 行业，概念热度数据

          case (url_, "getHotData") =>

            js.get("hy").toString.split(",").foreach{ x =>

              val finalUrl_min = url_ + "\t" + s"hottype=hy&hotval=$x" + "\tgetHotData"
              // val finalUrl_hour = url_ + "\t" + s"name=$x&query_type=1&minute_data=minute_data" + "\tgetHyAndGn"
              urlsLB.+=(finalUrl_min)
            }

            js.get("gn").toString.split(",").foreach{ x =>

              val finalUrl_min = url_ + "\t" + s"hottype=gn&hotval=$x" + "\tgetHotData"
              // val finalUrl_hour = url_ + "\t" + s"name=$x&query_type=1&minute_data=minute_data" + "\tgetHyAndGn"
              urlsLB.+=(finalUrl_min)
            }

          case (url_, "allsuggest") =>

            val finalUrl_min = url_ + "\t" + s"uid=$uid&after_sentence=%E6%9F%A5%E7%9C%8B%E7%83%AD%E5%BA%A6%E8%BF%9E%E7%BB%ADx%E5%B0%8F%E6%97%B6%E7%AD%89%E4%BA%8Ex&count=8&flag=6" + "\tallsuggest"

            urlsLB.+=(finalUrl_min)


          case (url_, "btsentence") =>

            val finalUrl_min = url_ + "\t" + s"uid=$uid&sonditions=[]&start_time=2016-05-12&end_time=2016-09-12&base_sessionid=-1" + "\tbtsentence"

            urlsLB.+=(finalUrl_min)


          case (url_, "hotsuggest") =>

            val finalUrl_min = url_ + "\t" + s"uid=$uid&flag=1&count=8" + "\thotsuggest"

            urlsLB.+=(finalUrl_min)


          case (url_, "btyield") =>
            val finalUrl_min = url_ + "\t" + s"uid=$uid" + "\tbtyield"

            urlsLB.+=(finalUrl_min)

          case (url_, "btresult") =>
            val finalUrl_min = url_ + "\t" + s"uid=$uid&pos=0&count=10" + "\tbtresult"

            urlsLB.+=(finalUrl_min)


          case (url_, "btsearch") =>

            val finalUrl_min = url_ + "\t" + s"uid=$uid&sonditions=%E6%80%BB%E8%82%A1%E6%9C%AC," + "\tbtsearch"

            urlsLB.+=(finalUrl_min)

      }

      FileUtil.normalWriteToFile(savePath ,urlsLB.toSeq)

    } else {

      warnLog(logFileInfo, "等待测试url列表更新， 暂没有更新！！")

    }

  }


  // pick 50 test stock hy, gn

  def pickData(file:String) =  {

    val stock = Source.fromFile(file).getLines().mkString(",")
    println(stock)

  }

  // http://fanyi.baidu.com/sug?kw=loud
  // data.array遍历得到jsonObject（k -> v）

  def sug(words:List[String], size: Int) = {

    val word = ""

    val respond = Jsoup.
      connect("http://fanyi.baidu.com/sug")
      .data("kw",s"$word")
      .ignoreContentType(true)
      .header("Content-Type","application/x-www-form-urlencoded; charset=UTF-8")
      .method(Method.POST)
      .execute().body()

    val array = new json.JSONObject(respond).getJSONArray("data")

    val ls = new ListBuffer[String]

    for(index <- 0 until array.length) {

      val jSONObject = new json.JSONObject(array.get(index).toString)

      ls.+=(jSONObject.get("k").toString + " -> " + jSONObject.get("v").toString)

    }

    FileUtil.normalWriteToFile(path = "", ls, isAppend = true)



  }


  // http://fanyi.baidu.com/v2transapi?from=en&to=zh&query=so&simple_means_flag=3
  // trans_result.data.dst
  def getMeaning = {

    val respond = Jsoup.
      connect("http://fanyi.baidu.com/v2transapi")
      .data("from","en").data("to","zh").data("query","loud").data("simple_means_flag","3")
      .header("Accept","*/*")
      .ignoreContentType(true)
      .header("Content-Type","application/x-www-form-urlencoded; charset=UTF-8")
      .method(Method.POST)
      .execute().body()

    val js = new json.JSONObject(new json.JSONObject(respond).getJSONObject("trans_result").getJSONArray("data").get(0).toString)

    println(js.get("src").toString + " -> " + js.get("dst").toString)

  }
  def main(args: Array[String]) {


    // generator data
    // loadTestData(baseData = "F://datatest//telecom//wokong//baseData", url = "F:/datatest/telecom/wokong/url_http", "")

    // 50 stock
    // pickData("F:\\datatest\\telecom\\wokong\\stock")

    // getMeaning

    sug

  }

}
