package com.usercase.request

import java.util.{Calendar, Date, Timer}

import com.bgfurfeature.config.Dom4jParser
import com.bgfurfeature.log.CLogger
import com.usercase.request.http._
import com.usercase.request.timer.MyTimerTask

import scala.io.Source

/**
  * Created by C.J.YOU on 2017/2/21.
  */
object Start  extends CLogger {

  // 初始化
  def init(xmlFile: String) = {


  }

  def main(args: Array[String]) {

    val Array(xmlFile) = args

    val parser = Dom4jParser.apply(xmlFilePath = xmlFile)

    val requestHeaderPath = parser.getParameterByTagName("File.header")

    val logConfigFile =  parser.getParameterByTagName("Logger.path")


    logConfigure(logConfigFile)

    val header = Source.fromFile(requestHeaderPath).getLines().toList

    JsonTypeNotice.apply(parser)


    val PERIOD_TIME = 60 * 1000 * 10


    val task = new MyTimerTask(parser = parser)

    task.run()
    val timer = new Timer()
    val cal = Calendar.getInstance()
    cal.setTime(new Date())
    cal.set(Calendar.MINUTE, cal.get(Calendar.MINUTE) + 1)
    val startData = cal.getTime
    timer.schedule(task, startData ,PERIOD_TIME)  // 10 Min 之后开始每一分钟跑一次

  }

}
