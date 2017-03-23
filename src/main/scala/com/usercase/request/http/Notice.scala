package com.usercase.request.http

import com.bgfurfeature.config.Dom4jParser
import com.usercase.request.util.TypeTransform

/**
  * Created by C.J.YOU on 2017/2/22.
  */
class Notice(parser:Dom4jParser) extends  NoticeType {

  def emailNotice(msg: String) = {

    val url = parser.getParameterByTagName("EmailMessage.url")

    val toUser = parser.getParameterByTagName("EmailMessage.receiver")

    val emailMessage =  if(msg == "") parser.getParameterByTagName("EmailMessage.context") else  msg

    HttpData.getInstance.notification(url, TypeTransform.listToHashMap(s"toUser=$toUser&msg=$emailMessage".split("&").toList))

  }

  def textNotice(msg: String) = {

    val url = parser.getParameterByTagName("TextMessage.url")

    val phone = parser.getParameterByTagName("TextMessage.receiver")

    val textMessage = if(msg == "") parser.getParameterByTagName("TextMessage.context") else msg

    HttpData.getInstance.notification(url, TypeTransform.listToHashMap(s"c=$phone,m=$textMessage".split(",").toList))


  }


}


object Notice {

  private var notice: Notice = null

  def apply(parser: Dom4jParser): Notice = {
    if(notice == null)
      notice = new Notice(parser)
    notice
  }

  def getInstance = notice

}
