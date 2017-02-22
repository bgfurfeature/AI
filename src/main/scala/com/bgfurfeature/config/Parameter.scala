package com.bgfurfeature.config

import scala.collection.mutable

/**
  * Created by C.J.YOU on 2016/12/9.
  * tagName: File.url
  */
abstract class Parameter {

  def getParameterByTagName(tagName: String):String

  def getAllParameter: mutable.HashMap[String, String]

}
