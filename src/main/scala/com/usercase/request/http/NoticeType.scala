package com.usercase.request.http

/**
  * Created by C.J.YOU on 2017/2/24.
  */
trait NoticeType {

  def emailNotice(msg: String)

  def textNotice(msg: String)


}
