package com.bgfurfeature.nlp

import org.apache.spark.{SparkConf, SparkContext}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.reflect.ClassTag

/**
  * Created by C.J.YOU on 2017/3/9.
  */
/**
  * WordRank
  * @param windowSize 提取词的窗口大小
  * @param segWords  分词后的结果
  */
class WordRank[F: ClassTag, S, T](windowSize: Int, segWords: Seq[String]) extends TextRank {

  // 将segWord生成索引值映射图
  val segWordsIdMap = segWords.distinct.zipWithIndex.toMap

  /**
    * 窗口方式提取词与词之间的关系
    *
    * @return 词之间的边集
    */
  def extractorWord = {

    // 按照指定的窗口提取结果存为wordsInWindows[[1,2,3],[2,3,4],[3,4,5]]
    // 每个步长提取一个窗口大小的seq
    val wordsInWindows = new ListBuffer[Seq[String]]

    val lastIndexPosition = segWords.size - windowSize

    (0 to lastIndexPosition).foreach{ pos =>

      wordsInWindows.+=(segWords.slice(pos, pos + windowSize).distinct)

    }

    // 利用共现的关系 确定哪些词之间有联系
    // 那个窗口包含此词，认为该窗口中所有词与该词有联系
    // word1 - edge - word1
    val wordWithEdges = segWords.toSet[String].flatMap { word =>

      val wordConnectWord = new mutable.HashSet[(String, String)]

      val wordEdges = new mutable.HashSet[String]

      wordsInWindows.foreach { window =>

        if(window.contains(word))
        // 没有指向自身的边
          window.foreach(item => if(word != item) wordConnectWord.+=((word, item)))
      }

      wordConnectWord

    }

    // 将词与词之间的的关系，使用此的索引值替换建立
    val edgesDescWithID = wordWithEdges.map { case(srcWord, destWord) =>
      segWordsIdMap.getOrElse(srcWord, -1) + "\t" + segWordsIdMap.getOrElse(destWord, -1)
    }.toSeq

    edgesDescWithID

  }

}

object WordRank {

  val sc = new SparkContext(new SparkConf().setAppName("TEXT-RANK").setMaster("local"))

  def main(args: Array[String]) {

    val tr = new WordRank(3, mutable.Seq("1","2","3","4","5","3","1"))

    val path = "hdfs://61.147.114.85:9000/telecom/edge_cache_edges"

    // 保存边关系
    // tr.saveTextFile(sc, tr.edgesDescWithID, path + "_edges")

    // 保存词的映射
    // tr.saveTextFile(sc, tr.segWordsIdMap.toSeq, path + "_map" )

    val graph = tr.generateGraph(sc, path)

    // 每条边自身包括： 权重，源节点，未节点, 默认权重为 1
    graph.edges.foreach(println)

    // 计算rank
    tr.textRank(graph, 10, 0.85)

  }

}

