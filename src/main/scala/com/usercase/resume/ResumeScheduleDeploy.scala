package com.usercase.resume

import com.bgfurfeature.util.BloomFilter
import io.vertx.core.json.{JsonArray, JsonObject}
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.log4j.{Level, Logger}
import org.apache.spark.{SparkConf, SparkContext}
import org.slf4j.Logger

import scala.collection.mutable.ListBuffer

/**
  * Created by devops on 2017/4/6.
  */
object ResumeScheduleDeploy {

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

  def main(args: Array[String]): Unit = {


    val Array(dir, dst) = args

    val sc = new SparkContext(new SparkConf().setAppName("ResumeExtraction"))

    // 代码中设置 LOGGER
    sc.setLogLevel("WARN")
    // sc.setLogLevel("DEBUG")
    // sc.setLogLevel("ERROR")
    // sc.setLogLevel("INFO")

    // hadoop configuration
    val hadoopConf = sc.hadoopConfiguration

    val fs  = FileSystem.get(hadoopConf)

    getDirectorFiles(fs, dir)


    println("allFileS: " + allFiles.size)

    val data=  (0 until  allFiles.size).map { index =>


      val data = sc.textFile(path = allFiles(index))

      data

    }


    val bf = (0 to 5).map(index => BloomFilter.apply(24))

    val all_data = sc.union(data)


    val result = all_data.map { info =>

      var flag = 1
      var key = ""

      val infoSplit = info.split("\t")

      if(infoSplit.size >= 2) {

        val key = infoSplit(0)
        val data = new JsonObject(infoSplit(1))

        val keySpilt = key.split("__@__")
        val name = keySpilt(0)
        val mobile = keySpilt(1)
        val email = keySpilt(2)
        // rule one if exist contact
        if(mobile != "" || email != "") {

          val workExpr = data.getJsonArray("workExperiences", new JsonArray())

          val projExpr = data.getJsonArray("educationExperiences", new JsonArray())

          if(name != "") {

            val bfOne = bf.apply(1)
            val bfTwo = bf.apply(2)

            if(mobile != "") {
              if(!bfOne.contain(name + mobile)) {
                bfOne.add(name + mobile)
              } else {
                flag = 2
              }
            }

            if(email != "") {
              if(!bfTwo.contain(name + email)) {
                bfTwo.add(name + email)
              } else {
                flag = 2
              }
            }
          } else if(workExpr.size() > 0) {


          } else if(projExpr.size() > 0 ){

          }

        } else {
          // no contact
          if(name != "") {

          }

        }



      }

      (key, flag)

    }


    println("all_data count:" + all_data.count())



  }

}
