package com.usercase.request

import com.bgfurfeature.util.FileUtil
import org.json.JSONObject

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.io.Source

/**
  * Created by C.J.YOU on 2017/2/23.
  */
object DataPrepare {

  val baseDataMap = new mutable.HashMap[String, String]()

  def loadTestData(baseData:String, url:String)= {

    Source.fromFile(baseData).getLines().foreach { x =>

      println(x)
      val js = new JSONObject(x)
      baseDataMap.+=(("stock", js.get("stock").toString))
      baseDataMap.+=(("hy", js.get("hy").toString))
      baseDataMap.+=(("gn", js.get("gn").toString))
      baseDataMap.+=(("info_type_list_stock", js.get("info_type_list_stock").toString))
      baseDataMap.+=(("info_type_list_hy", js.get("info_type_list_hy").toString))
      baseDataMap.+=(("info_type_list_gn", js.get("info_type_list_gn").toString))

      /*baseDataMap.+=(("stock","600170,601018"))
      baseDataMap.+=(("hy","有色金属"))
      baseDataMap.+=(("gn","大盘"))
      baseDataMap.+=(("info_type_list_stock","1,1,1,1,1,1,1,1"))
      baseDataMap.+=(("info_type_list_hy","0,1,0,0,0,0,0,0"))
      baseDataMap.+=(("info_type_list_gn","0,1,0,0,0,0,0,0"))*/
    }

    val urlsLB = new ListBuffer[String]

    Source.fromFile(url).getLines().toList.map(_.split("\t")).map(x=> (x(0),x(1))).foreach {

      case (url_, "getRelatedInfo") =>

        val info_type_list_stock = baseDataMap.get("info_type_list_stock").get
        val info_type_list_hy = baseDataMap.get("info_type_list_hy").get
        val info_type_list_gn = baseDataMap.get("info_type_list_gn").get

        baseDataMap.get("stock").get.split(",").foreach{ x =>

          val finalUrl = url_ + "\t" + s"query_type=1&key=$x&start_id=0&info_type_list=$info_type_list_stock&start_time=0" + "\tgetRelatedInfo"

          urlsLB.+=(finalUrl)
        }

        baseDataMap.get("hy").get.split(",").foreach{ x =>

          val finalUrl = url_ + "\t" + s"query_type=2&key=$x&start_id=0&info_type_list=$info_type_list_hy&start_time=0" + "\tgetRelatedInfo"
          urlsLB.+=(finalUrl)

        }

        baseDataMap.get("gn").get.split(",").foreach { x =>

          val finalUrl = url_ + "\t" + s"query_type=3&key=$x&start_id=0&info_type_list=$info_type_list_gn&start_time=0" + "\tgetRelatedInfo"

          urlsLB.+=(finalUrl)

        }

      case (url_, "getRateLine") =>

        baseDataMap.get("stock").get.split(",").foreach{ x =>

          val finalUrl = url_ + "\t" + s"query_type=stock&query_key=$x&query_date=week" + "\tgetRateLine"

          urlsLB.+=(finalUrl)
        }
    }

    FileUtil.normalWriteToFile("F://datatest//telecom//wokong//WR_http",urlsLB.toSeq)

  }

  def main(args: Array[String]) {


    // generator data
    loadTestData(baseData = "F://datatest//telecom//wokong//baseData",url = "F://datatest//telecom//wokong//url")



  }

}
