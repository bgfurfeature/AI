package com.inmind.idlg.hadoop;

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
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.CombineTextInputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;

import java.io.IOException;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

/**
 * Created by devops on 2017/4/11.
 * 简历解析后的文件按docId先去重
 * 输入文件格式（姓名__@__phone__@__email \t resume_extractor_json）
 * 输出文件格式（docId \t resume_extractor_json）
 */
public class DoFilterSameDocId extends Configured implements Tool {

  private static Logger logger = LoggerFactory.getLogger(DoFilterSameDocId.class);

  public  static class Map extends Mapper<LongWritable, Text, Text, Text> {
    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
      super.setup(context);
    }

    @Override
    protected void cleanup(Context context) throws IOException, InterruptedException {
      super.cleanup(context);
    }

    @Override
    protected void map(LongWritable longWritable, Text text, Context context)
        throws IOException, InterruptedException {
      String info = text.toString();
      String docId = "";
      // 提取特征处理
      String[] strings = info.split("\t");
      if (strings.length == 2) {
        JsonObject infoObject = new JsonObject(strings[1]);
        // 过滤 attachments 文件信息过滤
        JsonArray attachments = infoObject.getJsonArray("attachments", null);
        if (attachments != null) {
          JsonObject attachment = attachments.getJsonObject(attachments.size() - 1);
          String uri = attachment.getString("uri", "");
          docId = uri.substring(uri.lastIndexOf("/") + 1);
        }
        if (!"".equals(docId)) {
          context.write(new Text(docId), new Text(infoObject.toString()));
        }
      }
    }
  }

  public static class Reduce extends Reducer<Text, Text, Text, Text> {

    private Counter docDupCounter;
    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
      super.setup(context);
      docDupCounter = context.getCounter("reduce", "reduceDocDupCounter");
    }

    @Override
    protected void cleanup(Context context) throws IOException, InterruptedException {
      super.cleanup(context);
    }

    @Override
    protected void reduce(Text text, Iterable<Text> iterable, Context context) throws IOException,
        InterruptedException {
      int count = 0;
      String finalContent = "";
      for (Text item: iterable) {
        count += 1;
        if ("".equals(finalContent)) {
          finalContent = item.toString();
        }
      }
      String[] strings = finalContent.split("\t");
      if (strings.length >= 2) {
        String key = strings[0];
        String value = strings[1];
        context.write(text, new Text(value));
      }
      if (count > 1) {
        docDupCounter.increment(count - 1);
        logger.info("Doc id " + text.toString() + " Dup count: " +  (count - 1));
      }
    }
  }

  public void configJob(Job job, String input, String output) throws Exception {
    job.setJarByClass(DoFilterSameDocId.class);
    job.setJobName("DoFilterSameDocId-youcj");
    job.setMapperClass(DoFilterSameDocId.Map.class);
    Path outPath = new Path(output);
    FileSystem fs = FileSystem.get(job.getConfiguration());
    fs.delete(outPath, true);
    FileInputFormat.setInputPaths(job, input);
    FileOutputFormat.setOutputPath(job, outPath);
    job.setInputFormatClass(CombineTextInputFormat.class);
    job.setOutputFormatClass(TextOutputFormat.class);
    job.setMapOutputKeyClass(Text.class);
    job.setMapOutputValueClass(Text.class);
    job.setReducerClass(DoFilterSameDocId.Reduce.class);
    job.setNumReduceTasks(10);
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(Text.class);
  }

  @SuppressWarnings("RegexpSinglelineJava")
  public int run(String[] args) throws Exception {
    if (args.length < 2) {
      System.err
          .println("Usage: DoFilterSameDocId <Input> <Output>");
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
    int exitCode = new DoFilterSameDocId().run(args);
    System.exit(exitCode);
  }
}
