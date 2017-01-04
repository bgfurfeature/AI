package com.bgfurfeature.elasticsearch

import org.apache.spark.{SparkConf, SparkContext}
import org.elasticsearch.spark.rdd.EsSpark

/**
  * Created by C.J.YOU on 2017/1/4.
  */
object SparkES {

  def main(args: Array[String]) {

    val conf = new SparkConf()
        .set("es.nodes","127.0.0.1")
        .set("es.port","9200")
        .set("es.nodes.wan.only","true")
        .setAppName("es").setMaster("local")
    val sc = new SparkContext(conf)

    val numbers = Map("one" -> 1, "two" -> 2, "three" -> 3)
    val airports = Map("OTP" -> "Otopeni", "SFO" -> "San Fran")

    val esrdd = EsSpark.esRDD(sc, airports)
    EsSpark.saveToEs(esrdd,"spark/docs")





  }

}
