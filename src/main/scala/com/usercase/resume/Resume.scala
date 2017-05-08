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
object Resume {

  def main(args: Array[String]): Unit = {

    val Array(dir, peopleFile, changeFile) = args
    val sc = new SparkContext(new SparkConf().setAppName("Resume"))
    // 代码中设置 LOGGER
    sc.setLogLevel("WARN")
    val data = sc.textFile(dir).map(_.split("\t"))
    // 统计有多少人头贡献了公司跳转经历
    val people = data.map(x => (x(0), 1)).reduceByKey(_ + _)
      .sortBy(_._2, ascending = false, numPartitions = 1).map(x => x._1 + "," + x._2)
      .saveAsTextFile(peopleFile)

    // 统计 A -> B 跳转形式的数量分布
    val changeExpr = data.map(x => ((x(1), x(4)), x(7).toInt)).reduceByKey(_ + _)
      .map(x => (x._2, 1)).reduceByKey(_ + _)
      .sortBy(_._2, ascending = false, numPartitions = 1).map(x => x._1 + "," + x._2)
      .saveAsTextFile(changeFile)

  }

}
