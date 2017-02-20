package com.usercase.stock

import com.bgfurfeature.config.Dom4jParser
import com.bgfurfeature.kafka.{KafkaConsumer, KafkaProducer}
import com.bgfurfeature.log.CLogger
import com.bgfurfeature.spark.Spark
import com.bgfurfeature.util.{FileUtil, StringUtil}
import org.apache.spark.rdd.RDD

import scala.collection.mutable

/**
  * Created by C.J.YOU on 2016/12/23.
  * 根据 本地 化的原始数据集 提取股票热度数据
  * 与现有在线接入的数据对比
  * 整个流程分为： 从历史数据和
  * 在线实时接入的原始数据中提取热度
  * 两个阶段
  *
  */
object SHStockHeatExceptionLevel extends  CLogger {

  /**
    * 解压缩电信数据
    * @param line 源数据字符串
    * @return 解压缩后的字符串数组
    */
  def flatMapFun(line: String): scala.collection.mutable.MutableList[String] = {

    val lineList: scala.collection.mutable.MutableList[String] = mutable.MutableList[String]()
    // val res = StringUtil.parseJsonObject(line)
    // 解密操作是由庞负责
    val res = StringUtil.parseNotZlibJsonObject(line)

    if(res.nonEmpty){
      lineList.+=(res)
    }

    lineList

  }

  def reformatData(str: String) = str.replaceAll("\n","").replaceAll("\r","")

  // 去噪操作
  def filterNoiseData(rdd: RDD[String]) = {

    val data = rdd.map(_.split("\t")).map(x => (x.slice(0,2).mkString("\t"), x.slice(2,5).mkString("\t"), x.slice(5,8).mkString("\t"))).cache()
    // (tsMin + "\t" + ad , stock + "\t" + ts + "\t" + type, ua + "\t" + host + "\t" + url)

    val getKeyValue = data.map{ x =>

      val info = x._2.split("\t")
      val key = x._1 + "\t" + info(0)
      val value = info(1) + "\t" + info(2)
      (key, (value, x._3)) // (tsMin + "\t" + ad + "\t" + stock, (ts + "\t" + type, ua + "\t" + host + "\t" + url))
    }
    // key count
    val getKeyCount = data.map(x =>(x._1 + "\t" + x._2.split("\t")(0), 1))
      .reduceByKey(_ + _)
      .map { line =>
        val key = line._1
        val count = line._2
        if(count > 0 && count <= 4)  (key, "1" + "\t" + count)
        else if(count > 4 && count <= 20) (key, "2" + "\t" + count)
        else if(count > 20 && count <= 50) (key, "3" + "\t" + count)
        else if(count > 50 && count <= 100) (key, "4" + "\t" + count)
        else (key, "5" + "\t" + count)  // (tsMin + "\t" + ad + "\t" + stock, level)
      }

    // get final result
    val res = getKeyValue.leftOuterJoin(getKeyCount).map { x =>

      val optionValue = x._2._2.getOrElse("6" + "\t" + 0)

      val finalString = x._1 + "\t" + x._2._1._1 + "\t" + optionValue + "\t" + x._2._1._2

      // tsMin + "\t" + ad + "\t" + stock , ts + "\t" + type , level + "\t" + count  , ua + "\t" + host + "\t" + url)

      finalString

    }

    res

  }

  def main(args: Array[String]) {

    if(args.length < 5) {

      sys.error("args [xmlFile，dataDir, savePath, isSendToKafkaOrNot, getStreamingOrLocal]")
      sys.exit(-1)

    }

    val Array(xmlFile, dataDir, savePath, isSendToKafkaOrNot, getStreamingOrLocal) = args

    val parser = Dom4jParser.apply(xmlFilePath = xmlFile)

    val spark = Spark.apply(
      parser.getParameterByTagName("spark.master"),
      parser.getParameterByTagName("spark.appName"),
      parser.getParameterByTagName("spark.batchDuration").toInt
    )

    logConfigure(parser.getParameterByTagName("logger.configure"))

    val ssc = spark.ssc
    val sc = spark.sc

    val kafkaProducer = KafkaProducer.apply(parser)

    // local 获取数据
    if(getStreamingOrLocal == "LocalData") {

      val data = sc.textFile(dataDir)

      val res = filterNoiseData(data)

      //  saveFile
      FileUtil.normalWriteToFile(savePath, res.collect(), isAppend = true)

      // 结果数据是否发送到kafka
      if(isSendToKafkaOrNot == "True") {

        res.foreach { msg => kafkaProducer.send(msg) }

      }

    } else if(getStreamingOrLocal == "Streaming") {

      // 从实时流里获取数据
      val kafkaData = KafkaConsumer.apply(parser).getStreaming(ssc)

      val rdd = kafkaData.flatMap(x =>flatMapFun(x._2)).filter(x => x != "" || !x.isEmpty).map(reformatData)
        .foreachRDD { org =>

        val data = org.filter(x => x != "" || !x.isEmpty).map(reformatData)

        val res = filterNoiseData(data)

        //  saveFile
        FileUtil.saveData(savePath, res.collect())

        // 结果数据是否发送到kafka
        if(isSendToKafkaOrNot == "True") {

          res.foreach { msg => kafkaProducer.send(msg) }

        }

      }

      ssc.start()
      ssc.awaitTermination()

    } else {
      error("[wrong] data source for processing")
    }

  }

}
