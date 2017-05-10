package com.inmind.idlg.hadoop;

import com.inmind.idmg.dedup.rpc.DedupReply;
import com.inmind.idmg.dedup.rpc.DedupRequest;
import com.inmind.idmg.dedup.rpc.Feature;
import com.inmind.idmg.dedup.rpc.ResumeDedupServiceGrpc;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.okhttp.OkHttpChannelBuilder;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

/**
 * Created by devops on 2017/4/10.
 * 简历去重的grpc客户端
 */
public class DistinctClient {

  private final ManagedChannel channel;
  private final ResumeDedupServiceGrpc.ResumeDedupServiceBlockingStub blockingStub;
  private static Logger logger = LoggerFactory.getLogger(DistinctClient.class);


  /**
   * Construct client connecting to HelloWorld server at {@code host:port}.
   */
  public DistinctClient(String host, int port) {
    this(OkHttpChannelBuilder.forAddress(host, port).usePlaintext(true));
  }

  /**
   * Construct client for accessing RouteGuide server using the existing channel.
   */
  DistinctClient(ManagedChannelBuilder<?> channelBuilder) {
    channel = channelBuilder.build();
    blockingStub = ResumeDedupServiceGrpc.newBlockingStub(channel);
  }

  public void shutdown() throws InterruptedException {
    channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
  }

  public DedupReply doDistinct(ArrayList<Feature> features, String docId) {
    DedupRequest dedupRequest = DedupRequest.newBuilder().setDocid(docId).addAllFeatures(features).build();
    // addFeatures(new Feature.Builder()
    // .setValue(value).setTypeValue(type)).build();
    DedupReply reply = null;
    try {
      reply = blockingStub.doDedup(dedupRequest);
    } catch (Exception e) {
      logger.error("DedupRequest is error ！");
    }
    return reply;
  }

}
