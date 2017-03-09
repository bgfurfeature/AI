package com.bgfurfeature.nlp

import com.bgfurfeature.graphx.Analytics._
import org.apache.spark.SparkContext
import org.apache.spark.graphx.lib.PageRank
import org.apache.spark.graphx.{Graph, GraphLoader}

import scala.reflect.ClassTag

/**
  * Created by C.J.YOU on 2017/3/9.
  * 自动文摘（Automatic Summarization）的方法主要有两种：Extraction和Abstraction
  * 目前主要方法有：
    基于统计： 统计词频，位置等信息，计算句子权值，再简选取权值高的句子作为文摘，
               特点：简单易用，但对词句的使用大多仅停留在表面信息。
    基于图模型： 构建拓扑结构图，对词句进行排序。例如，TextRank/LexRank
    基于潜在语义： 使用主题模型，挖掘词句隐藏信息。例如，采用LDA，HMM
    基于整数规划： 将文摘问题转为整数线性规划，求全局最优解。
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
  def generateGraph(sc: SparkContext, fileFrom: String) = {

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
  def textRank[T: ClassTag,S: ClassTag](graph: Graph[T, S], numIter: Int, df: Double, outFileName: String = "") = {

    // 使用以实现的pageRank算法，可基于投票原理和计算公式自定义
    val rank = PageRank.run(graph, numIter, 1 - df)
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
