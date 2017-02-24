package com.usercase.request.http

import com.bgfurfeature.config.Dom4jParser
import com.bgfurfeature.log.CLogger
import org.json.JSONObject

import scala.collection.mutable

/**
  * Created by C.J.YOU on 2017/2/24.
  */
class JsonTypeNotice(dom4jParser: Dom4jParser) extends Notice(dom4jParser: Dom4jParser) with CLogger {


  // 报警处理， 同一个接口，报警一次
  private val set = new mutable.HashSet[String]

  def clearSet = set.empty

  def notice(jSONObject: JSONObject) = {

    val interfaceType = jSONObject.get("interfaceType").toString

    if(!set.contains(interfaceType)) {

      if(jSONObject.get("status").toString == "false") {

        errorLog(logFileInfo, jSONObject.toString)

        val rt = jSONObject.get("RT").toString.toLong

        if(rt > 3000) {

          emailNotice(s"""$interfaceType:RTime_is_over:$rt:ms"""")

          set.+=(interfaceType)

        } else {

          val result = jSONObject.get("result").toString

          emailNotice(s"$interfaceType:data_return_exception:result=$result")

          set.+=(interfaceType)

        }

      }

    }

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
