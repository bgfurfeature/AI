package com.usercase.test

import com.bgfurfeature.hbase.HBaseUtil
import org.apache.hadoop.hbase.client.Scan
import org.apache.hadoop.hbase.mapreduce.TableInputFormat

import scala.collection.mutable

/**
  * Created by devops on 2017/4/7.
  */
object TestHbase {

  def main(args: Array[String]): Unit = {

    import org.apache.spark.{SparkConf, SparkContext}

    val sc = new SparkContext(new SparkConf().setAppName("TestHbase").setMaster("local"))

    val hbase = new HBaseUtil

    val property = new mutable.HashMap[String, String]()
    property.+=(TableInputFormat.INPUT_TABLE -> "resume_file")

    val hBaseRDD = hbase.getHBaseDataThroughNewAPI(sc, property.toMap, new Scan())

    println(hBaseRDD.count())

  }


}
