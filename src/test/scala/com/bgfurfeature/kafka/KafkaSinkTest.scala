import java.util.Properties
import com.bgfurfeature.kafka.KafkaSink
import com.dyuproject.protostuff.StringSerializer
import org.apache.spark.SparkConf
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.{Duration, StreamingContext}

import scala.collection.mutable

object KafkaSinkTest extends App {


  var sparkConf = new SparkConf()

  var ssc = new StreamingContext(sparkConf, Duration(10000))

  val kafkaProducer: Broadcast[KafkaSink[String, String]] = {
    val kafkaProducerConfig = {
      val p = new Properties()
      p.setProperty("bootstrap.servers", "")
      p.setProperty("key.serializer", classOf[StringSerializer].getName)
      p.setProperty("value.serializer", classOf[StringSerializer].getName)
      p
    }
    println("kafka producer init done!")
    import scala.collection.JavaConversions._
    ssc.sparkContext.broadcast[KafkaSink[String, String]](KafkaSink.apply[String, String]
      (kafkaProducerConfig.toMap))
  }


  var rddQueue = new mutable.Queue[RDD[(String, String)]]()

  var inputSteam = ssc.queueStream(rddQueue)
    .foreachRDD(rdd => {
      if (!rdd.isEmpty()) {
        rdd.foreach(record => {
          kafkaProducer.value.send("", record._1, record._2)
          // 做一些必要的操作
        })
      }
    })

  ssc.start()

  rddQueue += ssc.sparkContext.sequenceFile[String, String]("", 2)

  ssc.awaitTermination()
}