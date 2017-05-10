package com.inmind.idlg.hadoop;

import com.inmind.idmg.fingerprint.rpc.FingerPrintReply;
import com.inmind.idmg.fingerprint.rpc.FingerPrintRequest;
import com.inmind.idmg.fingerprint.rpc.ReplayFeature;
import com.inmind.idmg.fingerprint.rpc.ResumeFeature;
import com.inmind.idmg.fingerprint.rpc.ResumeFingerPrintServiceGrpc;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.okhttp.OkHttpChannelBuilder;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

/**
 * Created by Jerry on 2017/4/24.
 * grpc client
 */
public class FingerPrintClient {
  private final ManagedChannel channel;
  private final ResumeFingerPrintServiceGrpc.ResumeFingerPrintServiceBlockingStub blockingStub;
  private static Logger logger = LoggerFactory.getLogger(FingerPrintClient.class);

  /**
   * Construct client connecting to HelloWorld server at {@code host:port}.
   */
  public FingerPrintClient(String host, int port) {
    this(OkHttpChannelBuilder.forAddress(host, port).usePlaintext(true));
  }

  /**
   * Construct client for accessing RouteGuide server using the existing channel.
   */
  FingerPrintClient(ManagedChannelBuilder<?> channelBuilder) {
    channel = channelBuilder.build();
    blockingStub = ResumeFingerPrintServiceGrpc.newBlockingStub(channel);
  }

  public void shutdown() throws InterruptedException {
    channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
  }

  public FingerPrintReply doFingerPrint(ArrayList<ResumeFeature> resumeFeatures) {

    FingerPrintRequest request = FingerPrintRequest.newBuilder().addAllFeatures(resumeFeatures)
        .build();

    FingerPrintReply reply = null;

    try {
      reply = blockingStub.doFingerPrint(request);
    } catch (Exception e) {
      logger.error("FingerPrintRequest doFingerPrint error :" + e.getMessage());
    }

    return reply;
  }

  public static void main(String[] args) throws Exception {
    FingerPrintClient client = new FingerPrintClient("hg000", 20399);
    ArrayList<ResumeFeature> list = new ArrayList<>();
    list.add(ResumeFeature.newBuilder()
        .setName("闵令永")
        .setPhone("18618251981")
        .setEmail("191102988@qq.com")
        .setNumber("No.1")
        .build());
    FingerPrintReply reply = client.doFingerPrint(list);
    if (reply != null) {
      int size = reply.getFingerPrintCount();
      for (int i = 0; i < size; i++) {
        ReplayFeature replayFeature = reply.getFingerPrint(i);
        String number = replayFeature.getNumber();
        long fp = replayFeature.getFingerPrint();
        String typeV = replayFeature.getType().name();
        String docId = replayFeature.getDocId();
        logger.info("number:" + number + ", finger print:" + fp + ", typeValue:" + typeV
            + ", " + "doc id:" + docId);
      }
      logger.info("features:" + size);
    }
  }


}
