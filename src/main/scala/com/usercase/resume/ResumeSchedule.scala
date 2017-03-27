package com.usercase.resume

import java.util.zip.ZipInputStream

import com.bgfurfeature.config.Dom4jParser
import com.bgfurfeature.spark.Spark
import io.vertx.core.json.JsonObject
import org.apache.commons.httpclient.HttpClient
import org.apache.commons.httpclient.methods.{PostMethod, StringRequestEntity}
import org.apache.commons.httpclient.params.HttpClientParams
import org.apache.commons.io.output.ByteArrayOutputStream
import org.apache.hadoop.fs.FSDataInputStream
import org.apache.hadoop.io.BytesWritable

/**
  * Created by devops on 2017/3/27.
  */
object ResumeSchedule {

  def postAndReturnString(client: HttpClient, url: String, body: String): String  = {

    try {

      val httpPost = new PostMethod(url)
      httpPost.setRequestEntity(new StringRequestEntity(body, "application/octet-stream", "utf8"))
      val code = client.executeMethod(httpPost)
      if (code != 200) return null
      httpPost.getResponseBodyAsString

    } catch {
      case e: Exception => e.printStackTrace
        null

    }
  }


  val stringBuilder = new StringBuilder

  def main(args: Array[String]): Unit = {

    val Array(xml) = args

    val parse = Dom4jParser.apply(xmlFilePath = xml)

    val dir = parse.getParameterByTagName("monitor.dir")

    val appName = parse.getParameterByTagName("spark.appName")

    val master = parse.getParameterByTagName("spark.master")

    val batchDuration = parse.getParameterByTagName("spark.batchDuration")

    val spark = Spark.apply(master , appName, batchDuration.toInt)

    val ssc = spark.ssc

    val data = ssc.textFileStream(directory = dir)


    data.foreachRDD { rdd =>

      rdd.foreach(x => stringBuilder.++=(x))

      val reqObj = new JsonObject()
      val fileName = "file_name.pdf"
      val fileContent = stringBuilder.toString().getBytes
      reqObj.put("name", fileName)
      reqObj.put("content", fileContent)

      println("reqObj:" + reqObj)
      val retVal = postAndReturnString(
        new HttpClient(new HttpClientParams),
        "http://localhost:7778/api/extract_by_content",
        reqObj.encode);

      println("retVal:" + retVal)

      stringBuilder.clear()


    }

    // ssc.sparkContext.textFile(dir + "/*").foreach(println)

    ssc.start()
    ssc.awaitTermination()


  }

}
