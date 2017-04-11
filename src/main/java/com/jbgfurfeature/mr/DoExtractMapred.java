package com.bgfurfeature.mr;

import com.usercase.resume.input.MultipleFileInputFormat;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapreduce.Counter;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;

import java.io.IOException;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;


public class DoExtractMapred extends Configured implements Tool {
  private static Logger logger = LoggerFactory.getLogger(DoExtractMapred.class);

  public DoExtractMapred() {
    super(new Configuration());
  }

  public DoExtractMapred(Configuration conf) {
    super(conf);
    // TODO Auto-generated constructor stub
  }

  public static String postAndReturnString(HttpClient client, String url, String body) {
    try {
      PostMethod httpPost = new PostMethod(url);
      httpPost.setRequestEntity(new StringRequestEntity(body, "application/octet-stream", "utf8"));
      int code = client.executeMethod(httpPost);
      if (code != 200) {
        return null;
      }
      return httpPost.getResponseBodyAsString();
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  public static class Map extends Mapper<Text, BytesWritable, Text, Text> {

    private HttpClient client;
    private Counter successCounter;
    private Counter errorCounter;
    private Counter allEmptyCounter;
    private Counter saveToHbaseErrorCounter;
    private Counter postprocessErrorCounter;
    private Counter extractErrorCounter;
    private Counter skipCounter;
    private Counter engCounter;
    private Table htable;

    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
      super.setup(context);
      HttpClientParams params = new HttpClientParams();
      client = new HttpClient(params);
      successCounter = context.getCounter("extract", "extractResumeSucc");
      errorCounter = context.getCounter("extract", "postError");
      extractErrorCounter = context.getCounter("extract", "extractError");
      saveToHbaseErrorCounter = context.getCounter("extract", "saveToHbaseError");
      postprocessErrorCounter = context.getCounter("extract", "postprocessError");
      allEmptyCounter = context.getCounter("extract", "nofieldExtract");
      skipCounter = context.getCounter("extract", "skipped");
      engCounter = context.getCounter("extract", "english");
      htable = ConnectionFactory.createConnection().getTable(TableName.valueOf("resume_file"));
    }

    @Override
    protected void cleanup(Context context) throws IOException, InterruptedException {
      super.cleanup(context);
      htable.close();
    }

    static boolean isEmpty(String str) {
      return str == null || str.isEmpty();
    }

    @Override
    protected void map(Text key, BytesWritable value, Context context)
        throws IOException, InterruptedException {
      JsonObject reqObj = new JsonObject();
      String fileName = key.toString();
      if (!fileName.endsWith(".doc") && !fileName.endsWith(".docx") && !fileName.endsWith(".pdf")
          && !fileName.endsWith(".htm") && !fileName.endsWith(".html")) {
        skipCounter.increment(1);
        return;
      }
      if (fileName.indexOf("index.htm") != -1 || fileName.indexOf("list.htm") != -1) {
        skipCounter.increment(1);
        return;
      }
      if (fileName.indexOf("_英文_") != -1) {
        engCounter.increment(1);
        return;
      }
      byte[] fileContent = value.getBytes();
      reqObj.put("name", fileName);
      reqObj.put("content", fileContent);
      String retVal = postAndReturnString(client,
          "http://localhost:7778/api/extract_by_content",
          reqObj.encode());
      if (retVal != null) {
        JsonObject obj = new JsonObject(retVal);
        int status = obj.getInteger("statusCode", 0);
        if (status == 0 && obj.containsKey("data")) {
          JsonObject resume = obj.getJsonObject("data");
          String text = obj.getString("text", null);
          if (text != null && !text.isEmpty()) {
            try {
              int pos = fileName.lastIndexOf('.');
              String suffix = fileName.substring(pos);
              String md5 = DigestUtils.md5Hex(fileContent);
              String uploadUrl = ""; // putToHbase(fileName, fileContent, md5);
              if (uploadUrl != null) {
                logger.info("uploaded file " + fileName + " to: " + uploadUrl);
                JsonArray attaches = new JsonArray();
                attaches.add(new JsonObject().put("name", fileName)
                    .put("uri", uploadUrl).put("fileType", suffix.substring(1)).put("type", 1));
                resume.put("attachments", attaches);

                resume.put("originResumeContent", text);
                String name = resume.getString("chineseName", "");
                String mobile = resume.getString("mobile", "");
                String email = resume.getString("privateEmail", "");
                if (isEmpty(email) && isEmpty(mobile)) {
                  allEmptyCounter.increment(1);
                  return;
                }
                String newKey = name + "__@__" + mobile + "__@__" + email;
                context.write(new Text(newKey), new Text(resume.encode()));
                successCounter.increment(1);
              } else {
                saveToHbaseErrorCounter.increment(1);
              }
            } catch (Exception e) {
              postprocessErrorCounter.increment(1);
            }
          }
        } else {
          extractErrorCounter.increment(1);
        }
      } else {
        errorCounter.increment(1);
      }
    }

  }


  public void configJob1(Job job, String input, String output) throws Exception {
    job.setJarByClass(DoExtractMapred.class);
    job.setJobName("chenyaowen-DoExtractMapred-new");
    job.setMapperClass(Map.class);
    job.setNumReduceTasks(0);


    Path outpath = new Path(output);
    FileSystem fs = FileSystem.get(job.getConfiguration());
    fs.delete(outpath, true);

    FileInputFormat.setInputPaths(job, input);
    FileOutputFormat.setOutputPath(job, outpath);

    job.setInputFormatClass(MultipleFileInputFormat.class);
    job.setOutputFormatClass(TextOutputFormat.class);

    job.setMapOutputKeyClass(Text.class);
    job.setMapOutputValueClass(Text.class);


  }

  @SuppressWarnings("RegexpSinglelineJava")
  public int run(String[] args) throws Exception {
    if (args.length < 2) {
      System.err
          .println("Usage: DoExtractMapred <Config> <Input> <Output>");
      System.exit(1);
    }
    JobConf conf = new JobConf();
    conf.setBoolean("mapreduce.input.fileinputformat.input.dir.recursive", true);
    Job job1 = new Job(conf);
    configJob1(job1, args[0], args[1]);
    job1.waitForCompletion(true);
    return 0;
  }

  @SuppressWarnings("RegexpSinglelineJava")
  public static void main(String[] args) throws Exception {
    int exitCode = new DoExtractMapred().run(args);
    System.exit(exitCode);
  }
}
