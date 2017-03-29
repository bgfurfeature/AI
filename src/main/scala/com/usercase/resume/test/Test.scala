package com.usercase.resume.test

import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming.{Seconds, StreamingContext}

/**
  * Created by devops on 2017/3/29.
  */
object Test {

  def main(args: Array[String]): Unit = {

    val session = SparkSession.builder().appName("Test").master("local").getOrCreate()

    val sc = session.sparkContext

    val ssc = new StreamingContext(sc, Seconds(10))

    val data = ssc.textFileStream("hdfs://server1:9000/resume/monit")

    ssc.start()
    ssc.awaitTermination()

  }

}
