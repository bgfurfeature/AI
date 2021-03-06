package com.usercase.request.timer

import java.util.TimerTask

import com.bgfurfeature.config.Dom4jParser
import com.bgfurfeature.log.CLogger
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
class MyTimerTask(parser:Dom4jParser)
  extends TimerTask with  CLogger {

  var plate_form_id = parser.getParameterByTagName("Plateform.name")

  var classDefine = "RelectClass." + plate_form_id

  val reflectClassName = parser.getParameterByTagName(classDefine)

  val RequestFilePath = parser.getParameterByTagName("Plateform.path") + "_http_" + plate_form_id  + "_test"

  val baseFile = parser.getParameterByTagName("Plateform.path") + "_base_" + plate_form_id

  var data_base = parser.getParameterByTagName("File.base_data")

  val myFlect = new RespondParserReflect(reflectClassName)

  val httpData = HttpData.getInstance

  val notice = JsonTypeNotice.getInstance.asInstanceOf[JsonTypeNotice]

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

  override def run(): Unit = {

    var res = new ListBuffer[JSONObject]

    // dataUpdate == true 重新获取url列表
    DataPrepare.loadTestData(baseData = data_base, url = baseFile, RequestFilePath)

    plate_form_id match {

      case "WK" =>
        // 登陆
        httpData.login
        if(httpData.getLoginCookie == ""){

          notice.emailNotice("can't_login_plateform!!!!")

        } else  {

          res.++=(httpTest(RequestFilePath, myFlect))
          // notice.notice(res)

        }
      case "FX" =>

        // 初始化token，误多次频繁请求，不然获取不到有效值
        httpData.token

        if(httpData.getToken == "") {

          notice.emailNotice("can't_get_token!!!!")

        } else  {

          res.++=(httpTest(RequestFilePath, myFlect))
          // notice.notice(res)

        }

      // 模拟交易
      case "Stread" =>


    }

    // notice.notice(res)

    res.foreach{ jSONObject =>

      warnLog(logFileInfo, jSONObject.toString)

       if(jSONObject.get("status").toString == "false") {
         // errorLog(logFileInfo, jSONObject.toString)
         println(jSONObject)
       }

    }


  }

}


/**
  * 伴生对象
  */
object MyTimerTask {

  def apply(parser:Dom4jParser): MyTimerTask = new MyTimerTask(parser)

}
