package com.usercase.request.parser

import com.usercase.request.http.HttpData

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
  def runMethod(methodName: String, url: String, parameter: scala.collection.mutable.HashMap[String,String], httpData: HttpData) = {

    val constructor = RPClass.getConstructor(classOf[String], classOf[mutable.HashMap[String,String]], classOf[HttpData])

    val RPObject = constructor.newInstance(url, parameter, httpData)

    val result  = RPClass.getMethod(methodName).invoke(RPObject)

    result.toString

  }



}
