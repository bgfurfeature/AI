package com.usercase.resume

import com.bgfurfeature.config.Dom4jParser
import com.bgfurfeature.log.CLogger
import com.usercase.resume.input.RawFileInputFormat
import io.vertx.core.json.JsonObject
import org.apache.commons.httpclient.HttpClient
import org.apache.commons.httpclient.methods.{PostMethod, StringRequestEntity}
import org.apache.commons.httpclient.params.HttpClientParams
import org.apache.commons.io.output.ByteArrayOutputStream
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.hadoop.io.{BytesWritable, Text}
import org.apache.spark.sql.SparkSession

import scala.collection.mutable.ListBuffer

/**
  * Created by devops on 2017/3/27.
  */
object ResumeSchedule  extends  CLogger {


  val allFiles = new ListBuffer[String]

  def getDirectorFiles(fs: FileSystem, path: String): Unit = {

    val files = fs.listStatus(new Path(path))

    val dirSize = files.filter(_.isDirectory).size

    if(dirSize > 0) {

     files.foreach { x =>

       if(x.isDirectory) {

        getDirectorFiles(fs, x.getPath.toString)

       } else {

        allFiles.+=(x.getPath.toString)

       }

     }
    } else {

      allFiles.+=(path)

    }


  }


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


  def main(args: Array[String]): Unit = {

    val Array(xml) = args

    // val xml = "/Users/devops/workspace/shell/conf/resume_conf.xml"

    val parse = Dom4jParser.apply(xmlFilePath = xml)

    val dir = parse.getParameterByTagName("monitor.dir")

    val dst = parse.getParameterByTagName("monitor.dst")

    val appName = parse.getParameterByTagName("spark.appName")

    val master = parse.getParameterByTagName("spark.master")

    val batchDuration = parse.getParameterByTagName("spark.batchDuration")

    val httpUrl = parse.getParameterByTagName("router.extract_by_content")

    val loggerPath = parse.getParameterByTagName("logger.configure")

    logConfigure(loggerPath)

    val session = SparkSession.builder().appName(appName).master(master).getOrCreate()

    val sc = session.sparkContext

    // hadoop configuration
    val hadoopConf = sc.hadoopConfiguration

    // mv org resume data to dst path
    val fs  = FileSystem.get(hadoopConf)

    // val file = sc.newAPIHadoopRDD[Text, BytesWritable, RawFileInputFormat](conf, classOf[RawFileInputFormat], classOf[Text], classOf[BytesWritable])

    // new api in mapreduce.input...

    getDirectorFiles(fs, dir)

    println("allFileS: " + allFiles.size)

    allFiles.foreach { path =>

      println("dir: " + path)

      val file = sc.newAPIHadoopFile[Text, BytesWritable, RawFileInputFormat](path)

      val files = file.map(_._1.toString).collect()

      file.foreach { case(key, value) =>

        val reqObj = new JsonObject()

        if(key != null && value != null) {

          val fileName = key.toString
          val fileContent = value.getBytes

          reqObj.put("name", fileName)
          reqObj.put("content", fileContent)

          // "http://localhost:7778/api/extract_by_content"
          val retVal = postAndReturnString(new HttpClient(new HttpClientParams), httpUrl, reqObj.encode())

          warnLog(logFileInfo, ("\"reqObj\":" + reqObj.toString + "," + "\"retVal\":" + retVal))

        }


      }

      warn(s""""files":${files.size}""")

      // 判断文件是否存在，存在则删除
      files.foreach { fileName =>

        val path = new Path(dst + "/" + fileName)

        val isExist = fs.exists(path)

        if(isExist) fs.deleteOnExit(path)

      }

      fs.close()


    }


  }

}
