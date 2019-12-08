package com.bgfurfeature.kafka

import com.bgfurfeature.config.Parameter
import kafka.serializer.StringDecoder
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.ReceiverInputDStream
import org.apache.spark.streaming.kafka.KafkaUtils


/*
 kafka消费者
 */
class KafkaConsumerCustom(parameter: Parameter) {

  private val zkQuorum = parameter.getParameterByTagName("kafka.zkQuorum")

  private  val numThreads = parameter.getParameterByTagName("kafka.numThreads")

  private  val topics = parameter.getParameterByTagName("kafka.consumerTopic").split(",").map((_, numThreads.toInt)).toMap

  private  val groupId = parameter.getParameterByTagName("kafka.groupId")

  private def createStream(
                    ssc: StreamingContext,
                    zkQuorum: String,
                    groupId: String,
                    topics: Map[String, Int],
                    storageLevel: StorageLevel = StorageLevel.MEMORY_AND_DISK_SER
                  ): ReceiverInputDStream[(String, String)] = {

    // 0.9.0.0  a replacement for the older Scala-based simple and high-level consumers -> New Consumer Configs
    //  Old Consumer Configs
    val kafkaParams = Map[String, String](
      "zookeeper.connect" -> zkQuorum,
      "group.id" -> groupId,
      "zookeeper.session.timeout.ms" -> "68000",
      "zookeeper.connection.timeout.ms" -> "105000",
      "zookeeper.sync.time.ms" -> "12000",
      "rebalance.max.retries"->"6",
      "rebalance.backoff.ms"->"9800",
      "auto.offset.reset" -> "largest")
    KafkaUtils.createStream[String, String, StringDecoder, StringDecoder](
      ssc, kafkaParams, topics, storageLevel)
    KafkaUtils
  }

  def getStreaming(ssc: StreamingContext)  = createStream(ssc, zkQuorum, groupId, topics)

}

object KafkaConsumerCustom {


  private var kc: KafkaConsumerCustom = null

  def apply(parameter: Parameter): KafkaConsumerCustom = {

    if(kc == null) kc = new KafkaConsumerCustom(parameter)

    kc

  }

  def getInstance = kc


}
