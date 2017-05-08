package com.inmind.idlg.serving;

import com.inmind.idlg.hadoop.DistinctClient;
import com.inmind.idmg.dedup.rpc.DedupReply;
import com.inmind.idmg.dedup.rpc.Feature;
import com.inmind.idmg.fingerprint.rpc.EduExpr;
import com.inmind.idmg.fingerprint.rpc.FingerPrintReply;
import com.inmind.idmg.fingerprint.rpc.FingerPrintRequest;
import com.inmind.idmg.fingerprint.rpc.ReplayFeature;
import com.inmind.idmg.fingerprint.rpc.ResumeFeature;
import com.inmind.idmg.fingerprint.rpc.ResumeFingerPrintServiceGrpc;
import com.inmind.idmg.fingerprint.rpc.WorkExpr;
import com.sangupta.murmur.Murmur2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.grpc.stub.StreamObserver;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

/**
 * Created by Jerry on 2017/4/21.
 * 继承根据proto编译生成的代码实现自定义服务
 */
public class ResumeFingerPrintRpcServing extends ResumeFingerPrintServiceGrpc
    .ResumeFingerPrintServiceImplBase {

  private static final Logger LOGGER = LoggerFactory.getLogger(ResumeFingerPrintRpcServing.class);
  private static final long MURMUR_SEED = 0x7f3a21eaL;
  private static String docId = "";
  private DistinctClient distinctClient;

  public ResumeFingerPrintRpcServing() {
    distinctClient = new DistinctClient("hg005", 20299);
  }

  public static ReplayFeature.Builder generateResumeFeature(String one, String two,
                                                            ReplayFeature.Type type,
                                                            String number) {
    // generate hash id
    String hashStringKey = convertString(one, two, "$$");
    long hashId = generateMurMurHashId(hashStringKey);
    return ReplayFeature.newBuilder().setType(type).setFingerPrint(hashId).setNumber(number);
  }

  public static Feature generateFeature(String one, String two, Feature.Type type) {
    // generate hash id
    String hashStringKey = convertString(one.trim(), two.trim(), "$$");
    long hashId = generateMurMurHashId(hashStringKey);
    return Feature.newBuilder().setType(type)
        .setValue(hashId).build();
  }

  private static String convertString(String one, String two, String separate) {
    return one + separate + two;
  }

  private static long generateMurMurHashId(String src) {

    byte[] bytes = src.getBytes();
    long murmurId = Murmur2.hash64(bytes, bytes.length, MURMUR_SEED);
    return murmurId;
  }

  /**
   * 工作经历排序
   */
  private static List<WorkExpr> sortWorkExprs(List<WorkExpr> workExprs) {
    List<WorkExpr> workExprsValue = new ArrayList<>();
    List<WorkExpr> sortedWorkExprs = new ArrayList<>();
    for (int i = 0; i < workExprs.size(); i++) {
      workExprsValue.add(workExprs.get(i));
    }
    Collections.sort(workExprsValue, (a, b) -> {
      int s = a.getStartedAt().compareTo(b.getStartedAt());
      return -s;
    });
    for (int i = 0; i < workExprsValue.size(); i++) {
      sortedWorkExprs.add(workExprsValue.get(i));
    }
    return sortedWorkExprs;
  }

  /**
   * 教育经历排序
   */
  private static List<EduExpr> sortEduExprs(List<EduExpr> eduExprs) {
    List<EduExpr> eduExprsValue = new ArrayList<>();
    List<EduExpr> sortedEduExprs = new ArrayList<>();
    for (int i = 0; i < eduExprs.size(); i++) {
      eduExprsValue.add(eduExprs.get(i));
    }
    Collections.sort(eduExprsValue, (a, b) -> {
      int s = a.getStartedAt().compareTo(b.getStartedAt());
      return -s;
    });
    for (int i = 0; i < eduExprsValue.size(); i++) {
      sortedEduExprs.add(eduExprsValue.get(i));
    }
    return sortedEduExprs;
  }

  private void complete(StreamObserver<FingerPrintReply> responseObserver, ArrayList<ReplayFeature>
      replyFeatures) {
    responseObserver.onNext(FingerPrintReply.newBuilder().addAllFingerPrint(replyFeatures).build());
    responseObserver.onCompleted();
  }

  @Override
  public void doFingerPrint(FingerPrintRequest fingerPrintRequest,
                            StreamObserver<FingerPrintReply> streamObserver) {
    JsonObject returnObj = new JsonObject();
    List<ResumeFeature> resumeFeatureList = fingerPrintRequest.getFeaturesList();
    ArrayList<Feature> features = new ArrayList<>();
    ArrayList<ReplayFeature.Builder> replayFeatureBuilders = new ArrayList<>();
    ArrayList<ReplayFeature> replayFeatures = new ArrayList<>();
    int requestCount = resumeFeatureList.size();
    // LOGGER.info("request number:" + requestCount);
    returnObj.put("request number", requestCount);
    for (int i = 0; i < requestCount; i++) {
      LOGGER.info("get resumeFeature:" + i + " --> ReplayFeature size:" + replayFeatureBuilders
          .size());
      JsonObject workObj = new JsonObject();
      JsonObject eduObj = new JsonObject();
      ResumeFeature resumeFeature = resumeFeatureList.get(i);
      String name = resumeFeature.getName();
      String mobile = resumeFeature.getPhone();
      String email = resumeFeature.getEmail();
      String number = resumeFeature.getNumber();
      int countWork = resumeFeature.getWorkExprsCount();
      int countEdu = resumeFeature.getEduExprsCount();
      if (countWork > 0) {
        List<WorkExpr> workExprs = resumeFeature.getWorkExprsList();
        // LOGGER.info("work exprs :" + countWork);
        returnObj.put("work exprs count", countWork);
        workObj.put("time", 0);
        List<WorkExpr> sorted = sortWorkExprs(workExprs);
        for (int j = 0; j < sorted.size(); j++) {
          WorkExpr workExpr = sorted.get(j);
          String company = workExpr.getCompany();
          String startedAt = workExpr.getStartedAt();
          String endedAt = workExpr.getEndedAt();
          if (!"".equals(company)) {
            if (!"".equals(startedAt)) {
              workObj.put("startedAt", startedAt.substring(0, startedAt.lastIndexOf("-")));
              workObj.put("company", company);
              if (!"".equals(endedAt) && !"至今".equals(endedAt)) {
                workObj.put("endedAt", endedAt.substring(0, endedAt.lastIndexOf("-")));
                workObj.put("time", 1);
                break;
              }
            }
          }
        }
        returnObj.put("work expr", workObj);
        // LOGGER.info("work expr :" + workObj);
      }
      if (countEdu > 0) {
        // LOGGER.info("edu exprs :" + countEdu);
        returnObj.put("edu exprs count", countEdu);
        List<EduExpr> eduExprs = resumeFeature.getEduExprsList();
        List<EduExpr> sorted = sortEduExprs(eduExprs);
        eduObj.put("time", 0);
        for (int j = 0; j < sorted.size(); j++) {
          EduExpr eduExpr = resumeFeature.getEduExprs(j);
          String school = eduExpr.getSchool();
          String major = eduExpr.getMajor();
          String startedAt = eduExpr.getStartedAt();
          String endedAt = eduExpr.getEndedAt();
          if (!"".equals(school)) {
            if (!"".equals(startedAt)) {
              eduObj.put("school", school);
              eduObj.put("major", major);
              eduObj.put("startedAt", startedAt.substring(0, startedAt.lastIndexOf("-")));
              if (!"".equals(endedAt) && !"至今".equals(endedAt)) {
                eduObj.put("endedAt", endedAt.substring(0, endedAt.lastIndexOf("-")));
                eduObj.put("time", 1);
                break;
              }
            }
          }
        }
        // LOGGER.info("edu expr :" + eduObj);
        returnObj.put("edu expr", eduObj);
      }
      returnObj.put("name", name);
      returnObj.put("mobile", mobile);
      returnObj.put("email", email);
      Boolean nameFlag = !"".equals(name) && !name.contains("女士") && !name.contains("小姐") &&
          !name.contains("先生");
      Boolean mobilFlag = !"".equals(mobile);
      Boolean emailFlag = !"".equals(email);
      Boolean workExprFlag = workObj != null && !"".equals(workObj.getString("company", ""));
      Boolean eduExprFlag = eduObj != null && !"".equals(eduObj.getString("school", ""));
      // LOGGER.info("resume feature number:" + number);
      returnObj.put("resume feature number id", number);
      if (mobilFlag && nameFlag) {
        // generate hash id
        replayFeatureBuilders.add(generateResumeFeature(mobile, name, ReplayFeature.Type
                .PHONE_AND_NAME,
            number));
        features.add(generateFeature(mobile, name, Feature.Type.PHONE_AND_NAME));
        // LOGGER.info("type:" + 1 + ", mobile expr:" + mobile + " ,name:" + name);
      }
      if (emailFlag && nameFlag) {
        // generate hash id
        replayFeatureBuilders.add(generateResumeFeature(email, name, ReplayFeature.Type
                .EMAIL_AND_NAME,
            number));
        features.add(generateFeature(email, name, Feature.Type.EMAIL_AND_NAME));
        // LOGGER.info("type:" + 2 + ", email expr:" + email + " ,name:" + name);
      }
      if (workExprFlag) {
        // generate hash id
        String company = workObj.getString("company", "");
        if (mobilFlag) {
          replayFeatureBuilders.add(generateResumeFeature(mobile, company, ReplayFeature.Type
              .PHONE_AND_COMPANY, number));
          features.add(generateFeature(mobile, company, Feature.Type.PHONE_AND_COMPANY));
          // LOGGER.info("type:" + 0 + ", mobile expr:" + mobile + " ,company:" + company);
        }
        if (emailFlag) {
          // generate hash id
          replayFeatureBuilders.add(generateResumeFeature(email, company, ReplayFeature.Type
              .EMAIL_AND_COMPANY, number));
          features.add(generateFeature(email, company, Feature.Type.EMAIL_AND_COMPANY));
          // LOGGER.info("type:" + 3 + ", email expr:" + email + " ,company:" + company);
        }
      }
      if (eduExprFlag) {
        // generate hash id
        String school = eduObj.getString("school", "");
        if (mobilFlag) {
          replayFeatureBuilders.add(generateResumeFeature(mobile, school, ReplayFeature.Type
              .PHONE_AND_SCHOOL, number));
          features.add(generateFeature(mobile, school, Feature.Type.PHONE_AND_SCHOOL));
          // LOGGER.info("type:" + 4 + ", mobile expr:" + mobile + " ,school:" + school);
        }
        if (emailFlag) {
          // generate hash id
          replayFeatureBuilders.add(generateResumeFeature(email, school, ReplayFeature.Type
              .EMAIL_AND_SCHOOL, number));
          features.add(generateFeature(email, school, Feature.Type.EMAIL_AND_SCHOOL));
          // LOGGER.info("type:" + 5 + ", email expr:" + email + " ,school:" + eduObj);
        }
      }
      // 需要增加开始时间在内
      if (nameFlag && workExprFlag) {
        // generate hash id
        int time = workObj.getInteger("time", 0);
        if (time == 1) {
          replayFeatureBuilders.add(generateResumeFeature(name, workObj.toString(),
              ReplayFeature.Type
                  .NAME_AND_FIRST_WORK, number));
          features.add(generateFeature(name, workObj.toString(), Feature.Type
              .NAME_AND_FIRST_WORK));
          // LOGGER.info("type:" + 6 + ", name expr:" + name + " ,workExpr:" + workObj);
        }
      }
      if (nameFlag && eduExprFlag) {
        // generate hash id
        int time = eduObj.getInteger("time", 0);
        if (time == 1) {
          replayFeatureBuilders.add(generateResumeFeature(name, eduObj.toString(),
              ReplayFeature.Type.NAME_AND_FIRST_EDUCATION, number));
          features.add(generateFeature(name, eduObj.toString(), Feature.Type
              .NAME_AND_FIRST_EDUCATION));
          // LOGGER.info("type:" + 7 + ", name :" + name + " ,eduExpr:" + eduObj);
        }
      }
      // 需要增加开始和结束的时间在内
      if (workExprFlag && eduExprFlag) {
        // generate hash id
        int timeWork = workObj.getInteger("time", 0);
        int timeEdu = eduObj.getInteger("time", 0);
        Boolean flag = timeWork == 1 && timeEdu == 1;
        if (flag) {
          replayFeatureBuilders.add(generateResumeFeature(workObj.toString(),
              eduObj.toString(), ReplayFeature.Type
                  .FIRST_WORK_AND_FIRST_EDUCATION, number));
          features.add(generateFeature(workObj.toString(),
              eduObj.toString(), Feature.Type.FIRST_WORK_AND_FIRST_EDUCATION));
          // LOGGER.info("type:" + 8 + ", work expr:" + workObj + " ,eduExpr:" + eduObj);
        }
      }
    }
    // LOGGER.info("reply size:" + replayFeatureBuilders.size());
    returnObj.put("reply size", replayFeatureBuilders.size());
    Boolean isDup = false;
    docId = "";
    if (features.size() > 0) {
      // dedup grpc service
      LOGGER.info("grpc server dedup distinct request feature size:" + features.size());
      DedupReply reply = distinctClient.doDistinct(features, ""); // docId 空字符串代表不需记录
      if (reply != null) {
        isDup = reply.getIsDup();
        docId = reply.getDupDocid();
        returnObj.put("isDup", isDup);
        returnObj.put("firstDetected", reply.getFirstDetectedBy());
        LOGGER.info("this resume is dedup or not:" + isDup + ", is first Detected By:" +
            reply.getFirstDetectedBy() + ", docId is:" + docId);
      } else {
        LOGGER.info("grpc server dedup distinct reply is null.....");
      }
    }
    returnObj.put("doc id", docId);
    LOGGER.info("return value --->>>:" + returnObj);
    // 增加docId
    for (int i = 0; i < replayFeatureBuilders.size(); i++) {
      replayFeatures.add(replayFeatureBuilders.get(i).setDocId(docId).build());
    }
    // reply
    complete(streamObserver, replayFeatures);

  }
}
