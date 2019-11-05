package com.usercase.resume.cluster

import org.apache.spark.mllib.feature.{Word2Vec, Word2VecModel}
import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by Jerry on 2017/5/9.
  */
object WordVectorModel {

  def main(args: Array[String]): Unit = {

    val sc = new SparkContext(new SparkConf().setMaster("local").setAppName("W2v"))
    sc.setLogLevel("ERROR")
    val filePath = args(0)
    val load = args(1).toInt
    val its = args(2)
    val sizeOfVetor = args(3)
    // 加载和切分数据 分别是起始id，目标id，以及两者的相似度
    val data = sc.textFile(filePath).map { x =>
      val sp = x.split("\t")
      sp(1) + "\t" + sp(4)
    }.map(_.split("\t").toSeq)
    val word2vec = new Word2Vec()
    word2vec.setMinCount(1)
    word2vec.setNumIterations(its.toInt)
    word2vec.setVectorSize(sizeOfVetor.toInt)
    var model:Word2VecModel = null
    if (load == -1) {
      println("train model.....")
      model = word2vec.fit(data)
      model.save(sc, path = "file:///Users/devops/workspace/shell/company/word2Vetor-model")
    } else {
      println("load model......")
      model = Word2VecModel.load(sc, path = "file:///Users/devops/workspace/shell/company/word2Vetor-model")
    }
    println("predict data........")
    val test = data.randomSplit(Array(0.8f, 0.2f), 0x0001L).apply(1)
    test.foreach{ x =>
      x.foreach { y =>
        model.findSynonyms(y, 5).foreach(println)
      }
    }
  }

}
