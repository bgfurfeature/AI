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

  def main(args: Array[String]): Unit = {


    val Array(dir, dst) = args

    val sc = new SparkContext(new SparkConf().setAppName("ResumeExtraction"))

    // 代码中设置 LOGGER
    sc.setLogLevel("WARN")
    // sc.setLogLevel("DEBUG")
    // sc.setLogLevel("ERROR")
    // sc.setLogLevel("INFO")


  }

}
