package com.usercase.resume.test

import com.hankcs.hanlp.HanLP
import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming.{Seconds, StreamingContext}

/**
  * Created by devops on 2017/3/29.
  */
object Test {

  def main(args: Array[String]): Unit = {

    /*val session = SparkSession.builder().appName("Test").master("local").getOrCreate()

    val sc = session.sparkContext

    val ssc = new StreamingContext(sc, Seconds(10))

    val data = ssc.textFileStream("hdfs://server1:9000/resume/monit")

    ssc.start()
    ssc.awaitTermination()*/

    val placeSegment = HanLP.newSegment().enablePlaceRecognize(true)

    val orgSegment = HanLP.newSegment().enableOrganizationRecognize(true)

    val sentence = "内蒙古祈蒙药业（集团）有限公司"

    val list = placeSegment.seg(sentence)

    val org = orgSegment.seg(sentence)

    println(list, org)

  }

}
