package com.usercase.resume

import com.bgfurfeature.config.Dom4jParser
import com.usercase.resume.input.RawFileInputFormat
import io.vertx.core.json.JsonObject
import org.apache.commons.httpclient.HttpClient
import org.apache.commons.httpclient.methods.{PostMethod, StringRequestEntity}
import org.apache.commons.httpclient.params.HttpClientParams
import org.apache.commons.io.output.ByteArrayOutputStream
import org.apache.hadoop.io.{BytesWritable, Text}
import org.apache.spark.sql.SparkSession

import scala.collection.mutable.ListBuffer

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


  val byteArray = new ListBuffer[Byte]()

  val stringBuilder = new StringBuilder

  def main(args: Array[String]): Unit = {

    val Array(xml) = args

    // val xml = "/Users/devops/workspace/shell/conf/resume_conf.xml"

    val parse = Dom4jParser.apply(xmlFilePath = xml)

    val dir = parse.getParameterByTagName("monitor.dir")

    val appName = parse.getParameterByTagName("spark.appName")

    val master = parse.getParameterByTagName("spark.master")

    val batchDuration = parse.getParameterByTagName("spark.batchDuration")


    val session = SparkSession.builder().appName(appName).master(master).getOrCreate()

    val sc = session.sparkContext

    val conf = sc.hadoopConfiguration


    // val file = sc.newAPIHadoopRDD[Text, BytesWritable, RawFileInputFormat](conf, classOf[RawFileInputFormat], classOf[Text], classOf[BytesWritable])

    val file = sc.newAPIHadoopFile[Text, BytesWritable, RawFileInputFormat](dir + "/*")

    file.foreach { case(key, value) =>

      val reqObj = new JsonObject()
      val fileName = key.toString
      val fileContent = value.getBytes

      reqObj.put("name", fileName)
      reqObj.put("content", fileContent)

      println("reqObj:" + reqObj)

      val retVal = postAndReturnString(
        new HttpClient(new HttpClientParams), "http://localhost:7778/api/extract_by_content",
        reqObj.encode());

      println("retVal:" + retVal)

    }


  }

}
