package com.usercase.request.timer

import java.util.TimerTask

import com.bgfurfeature.config.Dom4jParser
import com.bgfurfeature.threadpool.ThreadPool
import com.usercase.request.DataPrepare
import com.usercase.request.http.{HttpData, JsonTypeNotice}
import com.usercase.request.parser.RespondParserReflect
import com.usercase.request.task.Task
import org.json.JSONObject

import scala.collection.mutable.ListBuffer
import scala.io.Source

/**
  * Created by C.J.YOU on 2017/2/21.
  * 定时开始请求的Task类
  */
class MyTimerTask(parser:Dom4jParser) extends TimerTask {

  val reflectClassName = parser.getParameterByTagName("RelectClass.FX")

  val reflectClassName2 = parser.getParameterByTagName("RelectClass.WK")

  val httpRequestFilePath = parser.getParameterByTagName("File.url_fx")

  val wkhttpRequestFilePath = parser.getParameterByTagName("File.url_wk")

  val data_fx = parser.getParameterByTagName("File.data_pre_fx")

  var data_wk = parser.getParameterByTagName("File.data_pre_wk")

  var data_base = parser.getParameterByTagName("File.base_data")

  val myFlectfx = new RespondParserReflect(reflectClassName)

  val myFlectwk = new RespondParserReflect(reflectClassName2)

  val httpData = HttpData.apply("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.143 Safari/537.36","", parser)


  // 请求接口
  def httpTest(fileName:String, rclass:RespondParserReflect) = {

    val resLb = new ListBuffer[JSONObject]

    val items = Source.fromFile(fileName).getLines().toList

    items.foreach { line =>

      val task = new Task(line, httpData, rclass)

      ThreadPool.COMPLETION_SERVICE.submit(task)

    }

    val threadsize = items.size

    for(x <- 0 until threadsize) {

      val result = ThreadPool.COMPLETION_SERVICE.take().get()

      resLb.+=(result)

    }

    resLb

  }

  val notice = JsonTypeNotice.getInstance.asInstanceOf[JsonTypeNotice]

  override def run(): Unit = {


    var res = new ListBuffer[JSONObject]

    DataPrepare.loadTestData(baseData = data_base, urls = List(data_wk, data_fx))

    // FX test
    if(httpData.token != "") {

      res.++=(httpTest(httpRequestFilePath, myFlectfx))

    } else {

      notice.emailNotice("can't_get_token!!!!")

    }

    // fx_res.foreach(println)
    // wk test
    if(httpData.login != "") {

      res.++=(httpTest(wkhttpRequestFilePath, myFlectwk))

    } else {
      notice.emailNotice("plateform_can_not_login_!!!!")
    }

    res.foreach {x =>

      notice.notice(x)

    }

    notice.clearSet

  }

}


/**
  * 伴生对象
  */
object MyTimerTask {

  def apply(parser:Dom4jParser): MyTimerTask = new MyTimerTask(parser)

}
