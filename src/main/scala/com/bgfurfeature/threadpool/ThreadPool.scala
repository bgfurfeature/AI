package com.bgfurfeature.threadpool

import java.util.concurrent.{ExecutorCompletionService, Executors}

import org.json.JSONObject

/**
  * Created by C.J.YOU on 2017/2/21.
  */
object ThreadPool {

  private val pool = Executors.newCachedThreadPool()

  val COMPLETION_SERVICE  = new ExecutorCompletionService[JSONObject](pool)


}
