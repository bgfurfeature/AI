package com.inmind.idlg.hadoop;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
import org.apache.hadoop.hbase.mapreduce.TableMapper;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Counter;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.MultipleOutputs;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

/**
 * Created by Jerry on 2017/5/3.
 */
public class DoExtractorFromHbase extends Configured implements Tool {

  public static Logger logger = LoggerFactory.getLogger(DoExtractorFromHbase.class);

  public static class MyMapper extends TableMapper<Text, Text> {

    private String columnF;
    private String qualify;
    private Counter number;
    private Counter effectiveNumber;
    private HashMap updateTimeStampMap = new HashMap<String, Integer>();
    private MultipleOutputs<Text, Text> mos;
    private long maxTimeStamp = 0;

    /**
     * read cache files
     */
    private void readCachFiles(String cacheFilePath) {
      BufferedReader reader = null;
      try {
        reader = new BufferedReader(new FileReader(cacheFilePath));
        String line;
        line = reader.readLine();
        while (line != null) {
          line = reader.readLine();
          String[] tokens = line.split("\t");
          if (tokens.length == 2) {
            updateTimeStampMap.put(tokens[0], Integer.parseInt(tokens[1]));
          }
        }
      } catch (IOException e) {
        e.printStackTrace();
      } finally {
        if (reader != null) {
          try {
            reader.close();
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      }
    }

    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
      super.setup(context);
      URI[] urls = context.getCacheFiles();
      URI timeStamp = null;
      for (URI url : urls) {
        String path = url.getPath().toString();
        if ("update_time_stamp.txt".equals(path.substring(path.lastIndexOf("/") + 1))) {
          timeStamp = url;
          break;
        }
      }
      if (timeStamp != null) {
        readCachFiles(timeStamp.getPath());
        logger.info("cache file timeStamp:" + timeStamp.getPath());
      }
      number = context.getCounter("map-update", "rowKeyCounter");
      columnF = context.getConfiguration().get("map_columnFamily", "");
      qualify = context.getConfiguration().get("map_quality", "");
      logger.info("columnFamily is:" + columnF + ", quality is:" + qualify);
      effectiveNumber = context.getCounter("table-get", "effectiveNumberCounter");
      mos = new MultipleOutputs<>(context);
    }

    @Override
    protected void cleanup(Context context) throws IOException, InterruptedException {
      super.cleanup(context);
      logger.info("maxTimeStamp is:" + maxTimeStamp);
    }

    @Override
    protected void map(ImmutableBytesWritable row, Result result, Context
        context) throws IOException, InterruptedException {
      number.increment(1);
      String rowKey = Bytes.toString(row.get());
      if (!"".equals(columnF) && !"".equals(qualify) && result != null) {
        effectiveNumber.increment(1);
        String infoObject = Bytes.toString(result.getValue(columnF.getBytes(), qualify.getBytes()));
        long timeStamp = result.getColumnLatestCell(columnF.getBytes(), qualify.getBytes()).getTimestamp();
        if (timeStamp > maxTimeStamp) {
          maxTimeStamp = timeStamp;
        }
        context.write(new Text("MaxTimeStamp"), new Text(String.valueOf(maxTimeStamp)));
        mos.write("data",  new Text(rowKey), new Text(infoObject.toString()), "data/");
      }
    }
  }

  public void configJob(Job job, String mapTableName, long startTime, long endTime, String output)
   throws
      IOException,
      ClassNotFoundException, InterruptedException {
    job.setJarByClass(DoExtractorFromHbase.class);
    Path outPath = new Path(output);
    Path deletePath = new Path(output+ "/data");
    FileSystem fs = FileSystem.get(job.getConfiguration());
    fs.delete(deletePath, true);
    FileOutputFormat.setOutputPath(job, outPath);
    job.setInputFormatClass(TextInputFormat.class);
    job.setOutputFormatClass(TextOutputFormat.class);
    job.setMapOutputKeyClass(Text.class);
    job.setMapOutputValueClass(Text.class);
    job.setNumReduceTasks(1);
    Scan scan = new Scan();
    scan.setCaching(500);        // 1 is the default in Scan, which will be bad for MapReduce jobs
    scan.setCacheBlocks(false);  // don't set to true for MR jobs
    scan.setTimeRange(startTime, endTime);
    MultipleOutputs.addNamedOutput(job,"data", TextOutputFormat.class, Text.class, Text.class);
    TableMapReduceUtil.initTableMapperJob(mapTableName, scan, DoExtractorFromHbase.MyMapper.class,
        Text.class, Text.class, job);
    job.waitForCompletion(true);
  }

  @Override
  @SuppressWarnings("RegexpSinglelineJava")
  public int run(String[] args) throws Exception {
    // TODO Auto-generated method stub
    if (args.length < 6) {
      System.err.println("Usage: DoExtractorFromHbase <mapTableName>" +
       "<map_columnFamily> <map_quality> <start> <end> <Output>");
      System.exit(1);
    }
    Configuration conf = HBaseConfiguration.create();
    String mapTableName = args[0];
    String mapColumnFamily = args[1];
    String mapQualify = args[2];
    long started = Long.parseLong(args[3]);
    long ended = Long.parseLong(args[4]);
    String output = args[5];
    logger.info("tableName is:" + mapTableName + ", Output is:" + output);
    conf.set("hbase.zookeeper.quorum", "hg001,hg002,hg003,hg004,hg006");
    conf.set("fs.defaultFS", "hdfs://hg001:8020/");
    // 与hbase/conf/hbase-site.xml中hbase.zookeeper.property.clientPort配置的值相同
    conf.set("hbase.zookeeper.property.clientPort", "2181");
    // set outside parameter
    conf.set("map_columnFamily", mapColumnFamily);
    conf.set("map_quality", mapQualify);
    Job job = Job.getInstance(conf);
    configJob(job, mapTableName, started, ended, output);
    return 0;
  }

  @SuppressWarnings("RegexpSinglelineJava")
  public static void main(String[] args) throws Exception {
    int exitCode = new DoExtractorFromHbase().run(args);
    System.exit(exitCode);
  }
}
