package com.bgfurfeature.flink.batch

import org.apache.flink.api.scala.{DataSet, ExecutionEnvironment}

/**
  * Created by C.J.YOU on 2016/11/22.
  */
object DataSetExample {

  def main(args: Array[String]) {

    val batchEnv = ExecutionEnvironment.getExecutionEnvironment
    // Delta Iterations
    // read the initial data sets
    val initialSolutionSet: DataSet[(Long, Double)] = batchEnv.fromElements((1L,1.0))

    val initialWorkset: DataSet[(Long, Double)] = batchEnv.fromElements((1L,1.0),(2L, 2.0))

    val maxIterations = 100
    val keyPosition = 0




  }

}
