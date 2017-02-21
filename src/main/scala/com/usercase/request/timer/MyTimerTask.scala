package com.usercase.request.timer

import java.util.TimerTask

/**
  * Created by C.J.YOU on 2017/2/21.
  * 定时开始请求的Task类
  */
class MyTimerTask() extends TimerTask {

  override def run(): Unit = {

  }
}


/**
  * 伴生对象
  */
object MyTimerTask {

  def apply(): MyTimerTask = new MyTimerTask()

}
