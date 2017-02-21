package com.bgfurfeature.threadpool

import java.util.concurrent.{ExecutorCompletionService, Executors}

/**
  * Created by C.J.YOU on 2017/2/21.
  */
object ThreadPool {

  val pool = Executors.newCachedThreadPool()

  val COMPLETION_SERVICE  = new ExecutorCompletionService[String](pool)


}
