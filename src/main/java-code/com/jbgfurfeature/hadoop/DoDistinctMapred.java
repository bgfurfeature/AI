package com.inmind.idlg.hadoop;

import com.inmind.idmg.dedup.rpc.DedupReply;
import com.inmind.idmg.dedup.rpc.Feature;
import com.sangupta.murmur.Murmur2;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Counter;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.MRJobConfig;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.CombineTextInputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;


/**
 * Created by devops on 2017/4/7.
 * 按照提取的特征组合进行去重逻辑
 * 输入文件格式：
 * 1.不进行DoFilterSameDocId操作 -（姓名__@__phone__@__email \t resume_extractor_json）
 * 2.进行DoFilterSameDocId -（docId \t resume_extractor_json）
 * 输出文件格式（docId \t resume_extractor_json）
 */
public class DoDistinctMapred extends Configured implements Tool {

  private static Logger logger = LoggerFactory.getLogger(DoDistinctMapred.class);

  public static HashMap map = new HashMap<Integer, String>();

  static {
    map.put(Feature.Type.PHONE_AND_NAME_VALUE, "photeAndNameCounter");
    map.put(Feature.Type.EMAIL_AND_NAME_VALUE, "eamilAndNameCounter");
    map.put(Feature.Type.PHONE_AND_COMPANY_VALUE, "phoneAndCompanyCounter");
    map.put(Feature.Type.EMAIL_AND_COMPANY_VALUE, "emailAndCompanyConuter");
    map.put(Feature.Type.PHONE_AND_SCHOOL_VALUE, "phoneAndSchoolCounter");
    map.put(Feature.Type.EMAIL_AND_SCHOOL_VALUE, "emailAndSchoolCounter");
    map.put(Feature.Type.NAME_AND_FIRST_EDUCATION_VALUE, "nameAndFirstEduExprCounter");
    map.put(Feature.Type.NAME_AND_FIRST_WORK_VALUE, "nameAndFirstWorkExprCounter");
    map.put(Feature.Type.FIRST_WORK_AND_FIRST_EDUCATION_VALUE, "firstEduAndWorkExprCounter");
  }

  private static final long MURMUR_SEED = 0x7f3a21eaL;

  public DoDistinctMapred() {

    super(new Configuration());

  }

  public static Feature generateFeature(String one, String two, Feature.Type type) {
    // generate hash id
    String hashStringKey = convertString(one, two, "$$");
    long hashId = generateMurMurHashId(hashStringKey);
    return  Feature.newBuilder().setType(type)
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

  // 获取最早的工作经历 type = 1
  //
  // 获取 最在教育经历 （JSONArray） type = 2
  private static JsonObject getEarlierEduExprOrWorkExpr(JsonArray jsonArray, int type) {
    int size = jsonArray.size();
    JsonObject retObj = new JsonObject();
    JsonObject finalResult = null;
    String startedAt = "0000-00-00T00:00:00.000Z";
    String endedAt = "0000-00-00T00:00:00.000Z";
    for (int i = 0; i < size; i++) {
      JsonObject temp = jsonArray.getJsonObject(i);
      if (temp.containsKey("endedAt")) {
        String ended = temp.getString("endedAt");
        if (ended.compareTo(endedAt) > 0) {
          endedAt = ended;
          finalResult = temp;
        }
      } else if (temp.containsKey("startedAt")) {
        String started = temp.getString("startedAt");
        if (started.compareTo(startedAt) > 0) {
          startedAt = started;
          finalResult = temp;
        }
      } else { // 不存在时间，但是只存在公司 或者 学校名的情况
        if (finalResult == null) {
          finalResult = temp;
        }
      }
    }
    // 提取出 需要的，关键字，其他附加信息去掉
    if (finalResult != null) {
      // work expr
      if (type == 1) {
        if (finalResult.containsKey("org")) {
          String company = finalResult.getJsonObject("org").getString("name", "");
          retObj.put("company", company.trim());
        }
      } else if (type == 2) { // edu expr
        if (finalResult.containsKey("school")) {
          String school = finalResult.getJsonObject("school").getString("title", "");
          retObj.put("school_title", school.trim());
        }
        if (finalResult.containsKey("major")) {
          String major = finalResult.getJsonObject("major").getString("title", "");
          retObj.put("school_major", major.trim());
        }
      }
      // 时间精确到月
      String started = finalResult.getString("startedAt", "");
      String ended = finalResult.getString("endedAt", "");
      if (!"".equals(started) && !"".equals(ended)) {
        retObj.put("startedAt", started.substring(0, started.lastIndexOf("-")));
        retObj.put("endedAt", ended.substring(0, ended.lastIndexOf("-")));
        retObj.put("time", 1);
      } else {
        retObj.put("time", 0);
      }
    }
    return retObj;
  }

  public static class Map extends Mapper<LongWritable, Text, Text, Text> {
    private ArrayList<Counter> counterList = new ArrayList<>();
    private Counter errorCounter;
    private Counter successCounter;
    private Counter noFieldCounter;
    private Counter notFileCounter;
    private DistinctClient distinctClient;


    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
      super.setup(context);
      distinctClient = new DistinctClient("hg005", 20299);
      // grpc 服务器异常
      errorCounter = context.getCounter("features", "errorCounter");
      // 存在关键信息缺失
      noFieldCounter = context.getCounter("features", "noFieldCounter");
      for (int i = 0; i < map.size(); i++) {
        counterList.add(context.getCounter("features", map.get(i).toString()));
      }
      // 没有冲突的计数
      successCounter = context.getCounter("features", "successCounter");
      // 不是中文简历等文件计数
      notFileCounter = context.getCounter("features", "notFileCounter");
    }

    @Override
    protected void cleanup(Context context) throws IOException, InterruptedException {
      super.cleanup(context);
      distinctClient.shutdown();
    }

    static boolean isEmpty(String str) {
      return str == null || str.isEmpty();
    }

    @Override
    protected void map(LongWritable key, Text value, Context context)
        throws IOException, InterruptedException {
      ArrayList features = new ArrayList<Feature>();
      String info = value.toString();
      // 提取特征处理
      String[] strings = info.split("\t");
      if (strings.length == 2) {
        String basicInfo = strings[0];
        JsonObject infoObject = new JsonObject(strings[1]);
        // 过滤 attachments 文件信息过滤
        String resumeFileName = "";
        String docId = "";
        JsonArray attachments = infoObject.getJsonArray("attachments", null);
        if (attachments != null) {
          JsonObject attachment = attachments.getJsonObject(attachments.size() - 1);
          resumeFileName = attachment.getString("name", "");
          String uri = attachment.getString("uri", "");
          docId = uri.substring(uri.lastIndexOf("/") + 1);
        }
        logger.info("resume file info: " + basicInfo + " fileName:" + resumeFileName);
        if (!"".equals(resumeFileName) && !resumeFileName.contains("_英文_") &&
            !resumeFileName.contains("index.htm") && !resumeFileName.contains("list.htm")) {
          String name = infoObject.getString("chineseName", "").trim();
          Boolean nameFlag = !"".equals(name) && !name.contains("女士") && !name.contains("小姐") &&
              !name.contains("先生");
          String mobile = infoObject.getString("mobile", "").trim();
          Boolean mobilFlag = !"".equals(mobile);
          String email = infoObject.getString("privateEmail", "").trim();
          Boolean emailFlag = !"".equals(email);
          JsonObject workExpr = new JsonObject();
          JsonObject eduExpr = new JsonObject();
          // 获取所有 的工作经历
          JsonArray workExprs = infoObject.getJsonArray("workExperiences", null);
          if (workExprs != null) {
            workExpr = getEarlierEduExprOrWorkExpr(workExprs, 1);
          }
          logger.info("workExpr info:" + workExpr.toString());
          // 教育经历
          JsonArray eduExprs = infoObject.getJsonArray("educationExperiences", null);
          if (eduExprs != null) {
            eduExpr = getEarlierEduExprOrWorkExpr(eduExprs, 2);
          }

          logger.info("EduExpr info :" + eduExpr.toString());
          // 特征提取完毕， 开始确认组合特征，判断特征是否存在重复
          String company = workExpr.getString("company", "");
          String major = eduExpr.getString("school_major", "");
          String school = eduExpr.getString("school_title", "");
          Boolean eduExprFlag = !"".equals(school) && !"".equals(major);
          Boolean workExprFlag = !"".equals(company);
          if (mobilFlag && nameFlag) {
            // generate hash id
            features.add(generateFeature(mobile, name, Feature.Type.PHONE_AND_NAME));
          }
          if (emailFlag && nameFlag) {
            // generate hash id
            features.add(generateFeature(email, name, Feature.Type.EMAIL_AND_NAME));
          }
          if (mobilFlag && workExprFlag) {
            // generate hash id
            features.add(generateFeature(mobile, company, Feature.Type.PHONE_AND_COMPANY));
          }
          if (emailFlag && workExprFlag) {
            // generate hash id
            features.add(generateFeature(email, company, Feature.Type.EMAIL_AND_COMPANY));
          }
          if (eduExprFlag && mobilFlag) {
            // generate hash id
            features.add(generateFeature(mobile, school, Feature.Type.PHONE_AND_SCHOOL));
          }
          if (eduExprFlag && emailFlag) {
            // generate hash id
            features.add(generateFeature(email, school, Feature.Type.EMAIL_AND_SCHOOL));
          }
          // 需要 增加起止时间在内
          if (nameFlag && workExprFlag) {
            // generate hash id
            if (workExpr.getInteger("time", 0) == 1) {
              features.add(generateFeature(name, workExpr.toString(), Feature.Type.NAME_AND_FIRST_WORK));
            }
          }
          if (nameFlag && eduExprFlag) {
            // generate hash id
            if (eduExpr.getInteger("time", 0) == 1) {
              features.add(generateFeature(name, eduExpr.toString(), Feature.Type.NAME_AND_FIRST_EDUCATION));
            }
          }
          if (workExprFlag && eduExprFlag) {
            // generate hash id
            if (workExpr.getInteger("time", 0) == 1 && eduExpr.getInteger("time", 0) == 1) {
              features.add(generateFeature(workExpr.toString(), eduExpr.toString(), Feature.Type
                  .FIRST_WORK_AND_FIRST_EDUCATION));
            }
          }
          if (features.size() > 0) {
            // dedup grpc service
            logger.info("grpc server request feature size:" + features.size());
            DedupReply reply = distinctClient.doDistinct(features, docId);
            if (reply != null) {
              if (reply.getIsDup()) {
                int detectBy = reply.getFirstDetectedBy();
                counterList.get(detectBy).increment(1);
                logger.info("Duplicate doc id: " + reply.getDupDocid() + " is firstDetectedBy: "
                    + detectBy);
              } else {
                context.write(new Text(docId), new Text(strings[1]));
                successCounter.increment(1);
              }
            } else {
              errorCounter.increment(1);
            }
          } else {
            noFieldCounter.increment(1);
          }
        } else {
          notFileCounter.increment(1);
        }
      }
    }
  }

  public void configJob(Job job, String input, String output) throws Exception {
    job.setJarByClass(DoDistinctMapred.class);
    job.setJobName("DoDistinctMapred-youcj");
    job.setMapperClass(DoDistinctMapred.Map.class);
    job.setNumReduceTasks(0);
    Path outPath = new Path(output);
    FileSystem fs = FileSystem.get(job.getConfiguration());
    fs.delete(outPath, true);
    FileInputFormat.setInputPaths(job, input);
    FileOutputFormat.setOutputPath(job, outPath);
    job.setInputFormatClass(CombineTextInputFormat.class);
    job.setOutputFormatClass(TextOutputFormat.class);
    job.setMapOutputKeyClass(Text.class);
    job.setMapOutputValueClass(Text.class);
  }

  @SuppressWarnings("RegexpSinglelineJava")
  public int run(String[] args) throws Exception {
    if (args.length < 2) {
      System.err
          .println("Usage: DoDistinctMapred <Input> <Output>");
      System.exit(1);
    }
    Configuration conf = new Configuration();
    conf.setBoolean("mapreduce.input.fileinputformat.input.dir.recursive", true);
    conf.setBoolean(MRJobConfig.MAPREDUCE_JOB_USER_CLASSPATH_FIRST, true);
    conf.setLong("mapreduce.input.fileinputformat.split.maxsize", 128 * 1024 * 1024);
    Job job = Job.getInstance(conf);
    configJob(job, args[0], args[1]);
    job.waitForCompletion(true);
    return 0;
  }

  @SuppressWarnings("RegexpSinglelineJava")
  public static void main(String[] args) throws Exception {
    int exitCode = new DoDistinctMapred().run(args);
    System.exit(exitCode);
  }
}
