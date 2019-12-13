package com.bgfurfeature.config

import scala.collection.mutable.ArrayBuffer

/**
  * com.bgfurfeature.config
  * Created by C.J.YOU on 2019/12/13.
  */
object Conf {

  // parameters configuration
  val nGram = 3
  val updateFreq = 300000 // 5min

  // api configuration
  val segmentorHost = "http://localhost:8282"

  // spark configuration
  val master = "spark://localhost:7077"
  val localDir = "/Users/Program/scala/data/tmp"
  val perMaxRate = "5"
  val interval = 3 // seconds
  val parallelNum = "15"
  val executorMem = "1G"
  val concurrentJobs = "5"
  val coresMax = "3"

  // kafka configuration
  val brokers = "localhost:9091,localhost:9092"
  val zk = "localhost:2181"
  val group = "wordFreqGroup"
  val topics = "test"

  // mysql configuration
  val mysqlConfig = Map("url" -> "jdbc:mysql://localhost:3306/word_freq?characterEncoding=UTF-8", "username" -> "root", "password" -> "root")
  val maxPoolSize = 5
  val minPoolSize = 2

  // user behavior
  type DT = (String, ArrayBuffer[(Long, Long)])

  // Kafka记录
  val INDEX_LOG_TIME = 0
  val INDEX_LOG_USER = 1
  val INDEX_LOG_ITEM = 2
  val SEPERATOR = "\t"

  // 窗口配置
  val INDEX_TIEMSTAMP = 1
  val MAX_CNT = 25
  val EXPIRE_DURATION = 60 * 60 * 24 * 3
  var windowSize = 72 * 3600

  // redis config
  val RECORD_SZ = 2
  var redisIp = "127.0.0.1"
  var redisPort = 6379
  var passwd = ""
  val checkpointDir = "/Users/xiaolitao/Program/scala/userBehaviorStatistic/tmp"
  val streamIntervel = 3
  val partitionNumber = 2
  val batchSize = 64

}
