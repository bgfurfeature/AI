package com.bgfurfeature.nlp
import breeze.linalg.Vector
import org.apache.spark.graphx.{Edge, Graph}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.mllib.feature.Word2VecModel

/**
  * Created by C.J.YOU on 2017/3/9.
  */
class SentenceRank(size: Int, sentences: Seq[(Int, Seq[String])], word2VectorModel: Word2VecModel)
                                                                                            extends TextRank {

  // 构建句向量，通过词向量模型将句子词以向量的形式表示出来
  def generateSentenceVector(words: Seq[String]) = {

    val wordVectors = Vector.zeros[Double](size)

    var docVectors = Vector.zeros[Double](size)

    var count = 0.0

    words.foreach { word =>

      val vector = word2VectorModel.transform(word).toArray

      wordVectors.+=(Vector.apply(vector))  // 每个向量数值向相加，不是向量数组

      count += 1.0

    }

    if(count > 0) {

      println(s"构建文本向量，词的个数为:$count")
      docVectors = wordVectors./=(count)
    }

    docVectors

  }

  // 计算 字、词、句向量之间的余弦值
  def  cosSimilarity(
                        firstVector: scala.collection.immutable.Vector[Double],
                       secondVector: scala.collection.immutable.Vector[Double]) = {

    val up = firstVector.zip(secondVector).map {
      case(v, s) => v * s
    }.sum

    val fDown = math.sqrt(firstVector.map(item => math.pow(item, 2.0)).sum)

    val sDown = math.sqrt(secondVector.map(item => math.pow(item, 2.0)).sum)

    up / (fDown * sDown)

  }

  /**
    * 生成 边关系序列
    * @return 返回( 句1 id， 句2id， 句与句相似度)
    */
  def generateEdges = {

    val sentenceVectors = sentences.map(x => (x._1, generateSentenceVector(x._2)))

    val nums = Array.range(0, sentenceVectors.size)

    // 产生组合对
    val combinations = nums.combinations(2).toList

    val vectorPair = combinations.map { x =>

        val a = sentenceVectors(x.head)._2

        val b = sentenceVectors(x(1))._2

        ((x.head, a), (x(1), b))
    }.map { x =>
      (x._1._1, x._2._1, cosSimilarity(x._1._2.toArray.toVector, x._2._2.toArray.toVector))
    }.toSeq

    vectorPair

    // 构建关键词图的边，可认为：如果两个节点的余弦相似度高于某一个阈值，则这两个节点之间存在一条边。
    // 也可以是所有句子之间都存在边


  }



}

object SentenceRank  {

  val sc = new SparkContext(new SparkConf().setAppName("TEXT-RANK").setMaster("local"))

  def main(args: Array[String]) {

    // val word2VecModel = Word2VecModel.load(sc, path = "/home")

    val sr = new SentenceRank(100, Seq((1, Seq("i","am","jerryou"))), null)

    /* val edges = sr.generateEdges
     val path = sr.saveTextFile(sc, edges ,path = "")*/

    val edges = Seq((1,2,6.0),(1,3,7.5),(2,4, 5.5))

    val edgesRdd = sc.parallelize(edges).map(x => Edge(x._1, x._2, x._3))

    // attr, srcId, dstId
    println(s"edgeRdd Items : ")
    edgesRdd.foreach{ x =>
      println(x.attr)
    }

    // 构建图
    val graph = Graph.fromEdges(edgesRdd, defaultValue = -1 )

    // 获取句与句之间的textRank
    sr.textRank(graph, numIter = 10, df =  0.85)

    graph.edges.foreach { x =>
      println(s"edge:${x.attr},${x.srcId},${x.dstId}")
    }

    // 节点信息
    graph.vertices.foreach { x =>
      println(s"vertices:${x._1},${x._2}")
    }

    // 边的triplet 表示边和节点信息
    graph.triplets.foreach { x =>
      println(s"triples:${x.attr},${x.srcId},${x.dstId}")
    }

    //  a to b -> b to a
    graph.reverse.edges.foreach { x =>
      println(s"reverse edge:${x.attr},${x.srcId},${x.dstId}")
    }

    // 节点度的信息
    graph.inDegrees.foreach { x =>

      println(s"inDegress: ${x._1},${x._2}")
    }


  }


}
