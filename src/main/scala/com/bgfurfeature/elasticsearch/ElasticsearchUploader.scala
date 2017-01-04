package com.bgfurfeature.elasticsearch

import org.elasticsearch.action.bulk.{BackoffPolicy, BulkProcessor, BulkRequest, BulkResponse}
import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.common.unit.{ByteSizeUnit, ByteSizeValue, TimeValue}
import org.slf4j.{Logger, LoggerFactory}

/**
  * Created by C.J.YOU on 2017/1/4.
  */
class ElasticsearchUploader {

  private val logger: Logger = LoggerFactory.getLogger(classOf[ElasticsearchUploader])
  protected var client: TransportClient = null
  protected var listener: BulkProcessor.Listener = null
  private var bulkProcessor: BulkProcessor = null

  def init(): Unit = {
    // on startup
    try {
//      client = new PreBuiltTransportClient(Settings.EMPTY)
//        .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("localhost"), 9200))
    }
    catch {
      case e: Exception => {
        e.printStackTrace
      }
    }

    listener = new BulkProcessor.Listener() {
      @Override def beforeBulk(l: Long, bulkRequest: BulkRequest ) {
        logger.info("bulk request numberOfActions:" + bulkRequest.numberOfActions)
      }

      @Override def afterBulk(l: Long, bulkRequest: BulkRequest , bulkResponse: BulkResponse ) {
        logger.info("bulk response has failures: " + bulkResponse.hasFailures)
      }
      @Override def afterBulk(l: Long, bulkRequest: BulkRequest, throwable: Throwable) {
        logger.warn("bulk failed: " + throwable)
      }
    }

    bulkProcessor = BulkProcessor.builder(client, listener)
      .setBulkActions(10000)
      .setBulkSize(new ByteSizeValue(5, ByteSizeUnit.MB))
      .setFlushInterval(TimeValue.timeValueSeconds(5))
      .setConcurrentRequests(10)
      .setBackoffPolicy(BackoffPolicy.exponentialBackoff(TimeValue.timeValueMillis(100), 3))
      .build

  }

}
