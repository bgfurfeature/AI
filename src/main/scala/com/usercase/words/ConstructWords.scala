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

import scala.collection.mutable
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

    val ls = new ListBuffer[String]

    try {

    val array = new json.JSONObject(respond).getJSONArray("data")

      for (index <- 0 until array.length) {

        val jSONObject = new json.JSONObject(array.get(index).toString)

        val k = jSONObject.get("k").toString
        val v = jSONObject.get("v").toString
        if (k.length == size) {
          ls.+=(k + " -> " + v)
        }

      }
    } catch {
      case e:Exception =>
    }

    ls.+=("---------------------------------------------------")

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

  def generateWords(file: String, list: List[String], k: Int) = {

    val words = getAllWords(list, k)

    FileUtil.normalWriteToFile(path = file , words.toArray.toSeq.asInstanceOf[Seq[String]], isAppend = false)


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

    val res = new mutable.HashSet[String]

    for(index <- 0 until length) {

      val word = list(index)

      val task = new Task(word = word, size)

      val f = ThreadPool.WORDS_COMPLETION_SERVICE.submit(task).get()

      if(f.size > 1) {

        println("stop for a while......")

        Thread.sleep(1000)

        println("wake up ........")

        val value = f

        res.++=(value)

        res.foreach(println)


      }


    }

    // 从任务队列中提取已完成的，高并发
    /*for(index <- 0 until length) {

      // println("threadNUMBER:" + index)

      val f = ThreadPool.WORDS_COMPLETION_SERVICE.take().get()

       if(f.size > 1) {

        f.distinct.foreach(println)

        println("stop for a while......")
        Thread.sleep(100)
        println("wake up ........")

        val value = f

        res.++=(value)
      }

    }*/

    FileUtil.normalWriteToFile(path = "F:\\datatest\\data\\words", res.toList, isAppend = false)

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

    val string = "csdplxdomgjy"
    val list = new ListBuffer[String]
    string.foreach{ x =>
      list.+=(x.toString)
    }

    val size = 4

    // getAllWords
    generateWords(file, list.toList, size)
    // 获取单词意思
    sug(file, size, "")


  }

}
