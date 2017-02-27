package com.usercase.request.http

import com.bgfurfeature.config.Dom4jParser
import com.bgfurfeature.log.CLogger
import org.json.JSONObject

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/**
  * Created by C.J.YOU on 2017/2/24.
  */
class JsonTypeNotice(dom4jParser: Dom4jParser) extends Notice(dom4jParser: Dom4jParser) with CLogger {


  // 报警处理， 同一个接口，报警一次
  private val set = new mutable.HashSet[String]

  def clearSet = set.empty

  def notice(resLB: ListBuffer[JSONObject]) = {

    resLB.foreach { jSONObject =>

      warnLog(logFileInfo, jSONObject.toString)

      if(jSONObject.has("url") && jSONObject.has("interfaceType") ) {

        val url = jSONObject.get("url").toString

        val interfaceType = jSONObject.get("interfaceType").toString

        if (jSONObject.get("status").toString == "false") {

          errorLog(logFileInfo, jSONObject.toString)

        if (!set.contains(interfaceType)) {

            val rt = jSONObject.get("RT").toString.toLong

            if (rt > 3000) {

              emailNotice(s"""$url:RTime_is_over:$rt:ms"""")

              set.+=(interfaceType)

            } else {

              val result = jSONObject.get("result").toString

              emailNotice(s"$url:data_return_exception:result=$result")

              set.+=(interfaceType)

            }

          }

        }
      }

    }

    clearSet

  }

}

object JsonTypeNotice {

  private var JsonTypeNotice: Notice = null

  def apply(parser: Dom4jParser): Notice = {
    if(JsonTypeNotice == null)
      JsonTypeNotice = new JsonTypeNotice(parser)
    JsonTypeNotice
  }

  def getInstance = JsonTypeNotice

}
