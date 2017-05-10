package com.inmind.idlg.serving;

import java.nio.file.Files;
import java.nio.file.Paths;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

/**
 * Created by Jerry on 2017/4/21.
 * grpc serving 服务
 */
public class ResumeFingerPrintGrpcMain {

  private static final Logger LOGGER = LoggerFactory.getLogger(ResumeFingerPrintGrpcMain.class);

  public static void main(String[] args) throws Exception {

    if (args.length < 1) {
      System.out.println("Usage ResumeFingerPrintGrpcMain <conf>");
      return;
    }
    String configStr = new String(Files.readAllBytes(Paths.get(args[0])));
    JsonObject config = new JsonObject(configStr);
    ResumeFingerPrintRpcServing resumeFingerPrintRpcServing = new ResumeFingerPrintRpcServing();
    int port = config.getInteger("fingerprint.grpc.port", 17182);
    int level = config.getInteger("log.level", 0);
    LOGGER.info("listen port:" + port + " , log level:" + level);

    Server server = ServerBuilder.forPort(port)
        .addService(resumeFingerPrintRpcServing).build();
    server.start();
    server.awaitTermination();
  }

}
