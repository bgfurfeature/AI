package com.usercase.request

import java.util.{Calendar, Date, Timer}

import com.bgfurfeature.config.Dom4jParser
import com.bgfurfeature.log.CLogger
import com.usercase.request.http.{HttpData, JsonTypeNotice}
import com.usercase.request.timer.MyTimerTask
import org.apache.log4j.{Level, Logger}

import scala.io.Source

/**
  * Created by C.J.YOU on 2017/2/21.
  * http 请求响应测试
  */
object Start  extends CLogger {

  // 初始化
  def init(xmlFile: String) = {

    val parser = Dom4jParser.apply(xmlFilePath = xmlFile)

    val requestHeaderPath = parser.getParameterByTagName("File.header")

    val logConfigFile =  parser.getParameterByTagName("Logger.path")

    HttpData.apply("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.143 Safari/537.36","", parser)

    JsonTypeNotice.apply(parser)

    logConfigure(logConfigFile)

    val header = Source.fromFile(requestHeaderPath).getLines().toList

    parser

  }

  val PERIOD_TIME = 60 * 1000 * 10

  def main(args: Array[String]) {

    val Array(xmlFile) = args

    Logger.getLogger(classOf[CLogger]).setLevel(Level.ERROR)

    val task = new MyTimerTask(parser = init(xmlFile))

    task.run()
    val timer = new Timer()
    val cal = Calendar.getInstance()
    cal.setTime(new Date())
    cal.set(Calendar.MINUTE, cal.get(Calendar.MINUTE) + 1)
    val startData = cal.getTime
    timer.schedule(task, startData ,PERIOD_TIME)  // 10 Min 之后开始每一分钟跑一次

  }

}
