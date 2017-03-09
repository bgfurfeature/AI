package com.bgfurfeature.nlp

import com.bgfurfeature.graphx.Analytics._
import org.apache.spark.SparkContext
import org.apache.spark.graphx.lib.PageRank
import org.apache.spark.graphx.{Graph, GraphLoader}

import scala.reflect.ClassTag

/**
  * Created by C.J.YOU on 2017/3/9.
  */
class TextRank {

  /**
    * 缓存边关系文件到hdfs
    * 缓存中间结果集
    *
    * @param sc sc
    * @param data 结果数据集t
    * @param path  hdfs位置
    * @tparam R  数据泛型
    * @return 文件位置
    */
  def saveTextFile[R: ClassTag](sc: SparkContext, data: Seq[R], path: String) = {

    sc.parallelize(data).saveAsTextFile(path)

    path

  }

  /**
    * 使用边生成图
    *
    * @param sc sc
    * @param fileFrom 边集文件位置
    * @return  图
    */
  def generateGraph(sc: SparkContext, fileFrom: String)= {

    GraphLoader.edgeListFile(sc, fileFrom)

  }

  /**
    * 利用pageRank 计算排名
    *
    * @param graph  图
    * @param numIter 迭代次数
    * @param df 阻尼系数
    * @param outFileName  排名结果文件位置
    */
  def textRank(graph: Graph[Int, Int], numIter: Int, df: Double, outFileName: String = "") = {

    val rank = PageRank.run(graph,numIter, 1 - df)
      .vertices.cache()

    println("GRAPHX: Total rank: " + rank.map(_._2).reduce(_ + _))

    val rankF =  rank.map { case (id, r) =>

      id + "\t" + r

    }

    if (!outFileName.isEmpty) {

      warn("Saving pageranks of pages to " + outFileName)

      rankF.saveAsTextFile(outFileName)

    } else {

      rankF.foreach(println)

    }

  }

}
