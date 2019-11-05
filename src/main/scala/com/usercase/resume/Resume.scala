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

  case class Comapny(docId:String, companyFirst:String, jobTitleFirst:String,
                     startedFirst:String, companySecond:String, jobTitleSecond:String,
                     startedSecond:String)

  def main(args: Array[String]): Unit = {

    val Array(dir, peopleFile, changeFile, allInAndOutPath, allPath) = args
    val sc = new SparkContext(new SparkConf().setAppName("Resume"))
    // 代码中设置 LOGGER
    sc.setLogLevel("WARN")
    val data = sc.textFile(dir).map(_.split("\t"))

    val dataFormat = data.map {
      case Array(docId, companyFirst, jobTitleFirst, startedFirst, companySecond, jobTitleSecond,
      startedSecond) =>
      new Comapny(docId, companyFirst, jobTitleFirst, startedFirst, companySecond,
        jobTitleSecond, startedSecond)
    }
    // 统计有多少人头贡献了公司跳转经历
    val people = data.map(x => (x(0), 1)).reduceByKey(_ + _)
      .sortBy(_._2, ascending = false, numPartitions = 1).map(x => x._1 + "," + x._2)
      .saveAsTextFile(peopleFile)

    // 统计 A -> B 跳转形式的数量分布
    val changeExpr = data.map(x => ((x(1), x(4)), x(7).toInt)).reduceByKey(_ + _)
      .map(x => (x._2, 1)).reduceByKey(_ + _)
      .sortBy(_._2, ascending = false, numPartitions = 1).map(x => x._1 + "," + x._2)
      .saveAsTextFile(changeFile)

    // 统计每家公司 入职工和出职工的数量分布
    val allInAndOut = data.flatMap { x =>
      val lb = new ListBuffer[(String, Int)]
      lb.+=((x(1), x(7).toInt))
      lb.+=((x(4), x(7).toInt))
      lb
    }.reduceByKey(_+ _)
      .sortBy(_._2, ascending = false, numPartitions = 1).map(x => x._1 + "\t" + x._2)
      .saveAsTextFile(allInAndOutPath)

    val inputs = data.map(x => (x(1), x(7).toInt)).reduceByKey(_+ _)
    val outputs = data.map(x => (x(4), x(7).toInt)).reduceByKey(_ + _)

    inputs.fullOuterJoin(outputs).map { x =>
      val key = x._1
      val value1 = x._2._1.getOrElse(0)
      val value2 = x._2._2.getOrElse(0)
      val value3 = value1 + value2
      (key, value1, value2, value3)
    }.sortBy(_._4, ascending = false, numPartitions = 1)
      .map { case (key, value1, value2, value3) => key + "," + value1+ "," + value2 + "," +
        value3}
      .saveAsTextFile(allPath)

  }

}
