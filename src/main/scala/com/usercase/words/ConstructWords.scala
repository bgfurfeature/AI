package com.usercase.words

import java.util
import java.util.concurrent.Callable

import com.bgfurfeature.algo.Arrange
import com.bgfurfeature.threadpool.ThreadPool
import com.bgfurfeature.util.FileUtil
import com.usercase.request.util.TypeTransform
import org.json
import org.jsoup.Connection.Method
import org.jsoup.Jsoup

import scala.collection.mutable.ListBuffer
import scala.io.Source

/**
  * Created by C.J.YOU on 2017/3/1.
  */

class Task(word:String, size: Int) extends Callable[ListBuffer[String]] {

  override def call(): ListBuffer[String] = {

    val respond = Jsoup.
      connect("http://fanyi.baidu.com/sug")
      .data("kw",s"$word")
      .ignoreContentType(true)
      .header("Content-Type","application/x-www-form-urlencoded; charset=UTF-8")
      .method(Method.POST)
      .execute().body()

    val array = new json.JSONObject(respond).getJSONArray("data")

    val ls = new ListBuffer[String]

    try {
      for (index <- 0 until array.length) {

        val jSONObject = new json.JSONObject(array.get(index).toString)

        val k = jSONObject.get("k").toString
        val v = jSONObject.get("v").toString
        if (k.length == size) {
          ls.+=(k + " -> " + v)
          println((k + " -> " + v))
        }

      }
    } catch {
      case e:Exception =>
    }

    ls.+=("\n --------------------------------------------------- \n")

    ls

  }

}

object ConstructWords {

  // http 长短连接测试
  def request(url: String) = {

    val js = Jsoup.connect(url).header("Connection","keep-alive")
      .ignoreContentType(true)
      .method(Method.GET)
      .timeout(3000).execute()

    println(TypeTransform.jMapToString(js.headers()))

  }


  // pick 50 test stock hy, gn
  def pickData(file:String) =  {

    val stock = Source.fromFile(file).getLines().mkString(",")
    println(stock)

  }

  def getAllWords(list: List[String], k: Int) = {

    val array = new util.ArrayList[String]()

    list.foreach(array.add)

    val arrage = new Arrange()
     arrage.arrangeSelect(array, new util.ArrayList[String](), k)

    arrage.dataG


  }

  // http://fanyi.baidu.com/sug?kw=loud
  // data.array遍历得到jsonObject（k -> v）
  def sug(file:String, size: Int, start: String) = {

    val list = Source.fromFile(file).getLines().toList
      .filter{ x=>
        if(start != "") {
          x.startsWith(start)
        } else
          true
      }

    val length  = list.size

    println("length:" + length)

    val res = new ListBuffer[String]

    for(index <- 0 until length) {

      val word = list(index)

      val task = new Task(word = word, size)

      ThreadPool.WORDS_COMPLETION_SERVICE.submit(task)


    }

    for(index <- 0 until length) {

      // println("threadNUMBER:" + index)

      val f = ThreadPool.WORDS_COMPLETION_SERVICE.take().get()

      val value = f

      res.++=(value)

    }

    FileUtil.normalWriteToFile(path = "F:\\datatest\\data\\words", res, isAppend = false)

  }


  // http://fanyi.baidu.com/v2transapi?from=en&to=zh&query=so&simple_means_flag=3
  // trans_result.data.dst
  def getMeaning = {

    val respond = Jsoup.
      connect("http://fanyi.baidu.com/v2transapi")
      .data("from","en").data("to","zh").data("query","loud").data("simple_means_flag","3")
      .header("Accept","*/*")
      .ignoreContentType(true)
      .header("Content-Type","application/x-www-form-urlencoded; charset=UTF-8")
      .method(Method.POST)
      .execute().body()

    val js = new json.JSONObject(new json.JSONObject(respond).getJSONObject("trans_result").getJSONArray("data").get(0).toString)

    println(js.get("src").toString + " -> " + js.get("dst").toString)

  }
  def main(args: Array[String]) {

    // 50 stock
    // pickData("F:\\datatest\\telecom\\wokong\\stock")

    // getMeaning

    // sug

    val file = "F:\\datatest\\data\\words_list"


    val string = ""
    val list1 = new ListBuffer[Char]
    string.foreach{ x =>
      list1.+=(x)
    }
    // getAllWords
    val list = List("g","g","k","n","n","o","o","a","y","s","i","s")
    val size = 4

    // val words = getAllWords(list, k = size)

    // FileUtil.normalWriteToFile(path = file , words.toArray.toSeq.asInstanceOf[Seq[String]], isAppend = false)

    sug(file, size, "")


  }

}
