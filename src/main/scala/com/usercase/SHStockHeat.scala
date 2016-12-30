package com.usercase

import com.bgfurfeature.util.{FileUtil, TimeUtil}
import com.usercase.stock.StockMatcherRegex
import org.apache.flink.api.scala.{DataSet, ExecutionEnvironment}
import org.apache.flink.api.scala._
/**
  * Created by C.J.YOU on 2016/12/30.
  */
object SHStockHeat {

  // 去噪操作
  def filterNoiseData(ds: DataSet[String]) = {

    val data = ds.filter(!_.contains("youchaojiang")).map(x => x.split("\t")).filter(x => x.length >= 7)
      .map(x => (TimeUtil.getMinute(x(0)) + "\t" + x(1), StockMatcherRegex.VisitAndSearch(x), x(2) + "\t" + x(3) + "\t" + x(4)))
      .filter(x => x._2 != null).map(x => x._1 + "\t" + x._2 + "\t" + x._3)

    // (tsMin + "\t" + ad , stock + "\t" + ts + "\t" + type, ua + "\t" + host + "\t" + url)

    data

  }

  def main(args: Array[String]) {

    val batchEnv = ExecutionEnvironment.getExecutionEnvironment

    val Array(dataDir, savePath) = args

    val data = batchEnv.readTextFile(filePath = dataDir)

    val res = filterNoiseData(data)

    FileUtil.normalWriteToFile(savePath, res.collect(), isAppend = true)

  }

}
