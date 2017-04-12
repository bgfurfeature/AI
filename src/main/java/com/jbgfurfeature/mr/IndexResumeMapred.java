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
import org.apache.hadoop.mapreduce.lib.input.CombineTextInputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.io.IOException;
import java.net.InetAddress;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

/**
 * Created by koth on 2017/4/11.
 */
public class IndexResumeMapred extends Configured implements Tool {

  private static Logger logger = LoggerFactory.getLogger(IndexResumeMapred.class);

  public static class Map extends Mapper<LongWritable, Text, Text, Text> {

    private TransportClient transportClient;
    private Counter skipCounter;
    private Counter errCounter;
    private Counter successCounter;


    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
      super.setup(context);
      transportClient = new PreBuiltTransportClient(Settings.EMPTY)
          .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("hg001"),
              9300))
          .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("hg002"),
              9300));
      skipCounter = context.getCounter("index", "skipped");
      errCounter = context.getCounter("index", "errorBatch");
      successCounter = context.getCounter("index", "success");
    }

    @Override
    protected void cleanup(Context context) throws IOException, InterruptedException {
      super.cleanup(context);
      transportClient.close();
    }

    @Override
    public void run(Context context) throws IOException, InterruptedException {
    	// setup -> .. -> cleanup
    	setup(context);
      int batched = 0;
      BulkRequestBuilder bulkRequest = transportClient.prepareBulk();
      while (context.nextKeyValue()) {
        String line = context.getCurrentValue().toString();
        line = line.trim();
        String[] strings = line.split("\t");
        if (strings.length == 2) {
          bulkRequest.add(transportClient.prepareIndex("xman", "resume", strings[0])
              .setSource(strings[1], XContentType.JSON));
          batched += 1;
          if (batched >= 20) {
            BulkResponse bulkResponse = bulkRequest.get();
            if (bulkResponse.hasFailures()) {
              // process failures by iterating through each bulk response item
              errCounter.increment(1);
            } else {
              successCounter.increment(batched);
            }
            batched = 0;
            bulkRequest = transportClient.prepareBulk();
          }
        } else {
          skipCounter.increment(1);
        }
      }
      if (batched > 0) {
        BulkResponse bulkResponse = bulkRequest.get();
        if (bulkResponse.hasFailures()) {
          // process failures by iterating through each bulk response item
          errCounter.increment(1);
        } else {
          successCounter.increment(batched);
        }
      }
    }


  }


  public void configJob(Job job, String input, String output) throws Exception {
    job.setJarByClass(IndexResumeMapred.class);
    job.setJobName("IndexResumeMapred-koth");
    job.setMapperClass(IndexResumeMapred.Map.class);
    Path outPath = new Path(output);
    FileSystem fs = FileSystem.get(job.getConfiguration());
    fs.delete(outPath, true);
    FileInputFormat.setInputPaths(job, input);
    FileOutputFormat.setOutputPath(job, outPath);
    // combine small size file for little map number
    job.setInputFormatClass(CombineTextInputFormat.class);
    job.setOutputFormatClass(TextOutputFormat.class);
    job.setMapOutputKeyClass(Text.class);
    job.setMapOutputValueClass(Text.class);
    job.setNumReduceTasks(0);
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(Text.class);
  }

  @SuppressWarnings("RegexpSinglelineJava")
  public int run(String[] args) throws Exception {
    if (args.length < 2) {
      System.err
          .println("Usage: IndexResumeMapred <Input> <Output>");
      System.exit(1);
    }
    Configuration conf = new Configuration();
    conf.setBoolean("mapreduce.input.fileinputformat.input.dir.recursive", true);
    conf.setBoolean(MRJobConfig.MAPREDUCE_JOB_USER_CLASSPATH_FIRST, true);
    // combine size set
    conf.setLong("mapreduce.input.fileinputformat.split.maxsize", 512 * 1024 * 1024);
    Job job = Job.getInstance(conf);
    configJob(job, args[0], args[1]);
    job.waitForCompletion(true);
    return 0;
  }

  @SuppressWarnings("RegexpSinglelineJava")
  public static void main(String[] args) throws Exception {
    int exitCode = new IndexResumeMapred().run(args);
    System.exit(exitCode);
  }
}
