package com.bgfurfeature.graphx

import org.apache.spark.mllib.clustering.PowerIterationClustering
import org.apache.spark.{SparkConf, SparkContext}

import scala.collection.mutable.ListBuffer

/**
  * Created by Jerry on 2017/4/24.
  * PowerIterationClustering实现了快速迭代算法
  */
object Pic {

  def main(args: Array[String]): Unit = {

    val sc = new SparkContext(new SparkConf().setMaster("local").setAppName("Pic"))

    // val sc = SparkSession.builder().master("local").appName("pic").getOrCreate().sparkContext

    sc.setLogLevel("ERROR")

    val filePath = args(0)
    val cluster = args(1)
    val its = args(2)
    // 加载和切分数据 分别是起始id，目标id，以及两者的相似度
    val data = sc.textFile(filePath)

    // get company map
    val company = data.flatMap { x =>
      val list = new ListBuffer[String]
      val parts = x.split("\t")
      list.+=(parts(0))
      list.+=:(parts(1))
      list
    }.distinct().zipWithIndex()

    val companyReverse = company.map(x => (x._2, x._1))

    val mapreverse = companyReverse.collectAsMap()

    companyReverse.saveAsTextFile(path = "file:///Users/devops/workspace/shell/company/c-map")

    val companyMap = company.collectAsMap()

    val bc = sc.broadcast(companyMap)

    val similarities = data.map { x =>
      val list = x.split("\t")
      if (!list(0).contains("-") && !list(1).contains("-") && !list(2).contains("-")) {
        (bc.value.get(list(0)).get, bc.value.get(list(1)).get, list(2).toDouble)
      } else (-1.toLong, -1.toLong, -1.toDouble)
    }.filter(_._1 != -1).cache()

    val reduce = similarities.map(x => (x._1 + ":" + x._2, x._3)).reduceByKey(_ + _)
    val count = reduce.count()
    println("count<<<<<<<<<<<<<<<<<<:" + count)
    val max = reduce.map(_._2).max()
    val min = reduce.map(_._2).min()
    val normalize = max - min
    val similarity = reduce.map { line =>
      val x = line._1.split(":")
      (x(0).toLong, x(1).toLong, line._2)
    }.cache()

    similarity.map(x => (mapreverse.get(x._1).get, mapreverse.get(x._2).get, x._3))
      .saveAsTextFile(path =
      "file:///Users/devops/workspace/shell/company/data")
    // 使用快速迭代算法将数据分为两类
    val pic = new PowerIterationClustering().setK(k = cluster.toInt).setMaxIterations(maxIterations = its.toInt)
    val model = pic.run(similarity)
    model.save(sc, path = "file:///Users/devops/workspace/shell/company/model")
    //打印出所有的簇
    /*model.assignments.foreach { a =>
      println(s"${a.id} -> ${a.cluster}")
    }*/

    val change = model.assignments.map { a =>
      (a.cluster, mapreverse.get(a.id))
    }.sortByKey(ascending = true)

    change.saveAsTextFile(path = "file:///Users/devops/workspace/shell/company/cluster-result")

  }

}
