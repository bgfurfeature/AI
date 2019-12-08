package com.bgfurfeature.kafka

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}

class KafkaSink[K,V](createProducer: ()=> KafkaProducer[K,V]) extends  Serializable {
  // 这样能够避免运行时产生的 NotSerializable

  lazy val producer = createProducer()

  def send(topic: String, key: K, value: V) = {
    producer.send(new ProducerRecord[K,V](topic, key, value))
  }

  def send(topic: String, value: V) = {
      producer.send(new ProducerRecord[K,V](topic, value))
  }


}

object KafkaSink {

  import  scala.collection.JavaConversions._

  def apply[K,V](config:Map[String, Object]) = {
    val createProducerFunc = () => {
      // 新建kafakProducer
      val producer = new KafkaProducer[K,V](config)
      sys.addShutdownHook{
        // 确保Executor的JVM关闭前，能将Kafka Producer缓存中所有信息写入kafka
        producer.close()
      }
      producer
    }
    new KafkaSink(createProducerFunc)
  }
}
