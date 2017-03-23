package com.usercase.request.parser

import com.usercase.request.http.HttpData
import org.json.JSONObject

import scala.collection.mutable

/**
  * Created by C.J.YOU on 2017/2/21.
  */
class RespondParserReflect(className: String) {

  val RPClass = Class.forName(className)

  /**
    * 通过反射机制自动获取 不同url对应的数据解析方法
    * @param methodName 方法名
    * @param url  请求url
    * @param parameter 参数
    * @return
    */

  type T = (String, String, mutable.HashMap[String,String], HttpData)

  def runMethod(t: T):JSONObject= {

    val constructor = RPClass.getConstructor(classOf[String], classOf[mutable.HashMap[String,String]], classOf[HttpData])

    val RPObject = constructor.newInstance(t._2, t._3, t._4)

    val result  = RPClass.getMethod(t._1).invoke(RPObject)

    result.asInstanceOf[JSONObject].put("RespondParser",className)

  }



}
