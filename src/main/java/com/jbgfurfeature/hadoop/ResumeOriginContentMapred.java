package com.bgfurfeature.mr;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Counter;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;

import java.io.IOException;

import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

/**
 * Created by Jerry on 2017/4/12.
 */
public class ResumeOriginContentMapred extends Configured implements Tool {
	private static Logger logger = LoggerFactory.getLogger(ResumeOriginContentMapred.class);
	public static class Map extends Mapper<LongWritable, Text, Text, Text> {
		private Counter skipperCounter;
		private HTable htable;
		private Counter noFiledCounter;
		private Counter successCounter;

		@Override
		protected void setup(Context context) throws IOException, InterruptedException {
			super.setup(context);
			skipperCounter = context.getCounter("runner", "skipperCounter");
			htable = HTableUtil.getHTable("resume_file");
			noFiledCounter = context.getCounter("runner", "noFiledCounter");
			successCounter = context.getCounter("runner", "successCounter");
		}

		@Override
		protected void cleanup(Context context) throws IOException, InterruptedException {
			super.cleanup(context);
			htable.close();
		}

		@Override
		public void run(Context context) throws IOException, InterruptedException {
			setup(context);
			try {
				while (context.nextKeyValue()) {
					String info = context.getCurrentValue().toString().trim();
					// 提取特征处理
					String[] strings = info.split("\t");
					if (strings.length == 2) {
						String rowKey = strings[0];
						JsonObject infoObject = new JsonObject(strings[1]);
						String originResumeContent = infoObject.getString("originResumeContent", "");
						if (!"".equals(originResumeContent)) {
							String res = putToHbase(HTableUtil.gB(originResumeContent), rowKey);
							if (res != null) {
								successCounter.increment(1);
								logger.info("rowKey is :" + rowKey);
								context.write(new Text(rowKey), new Text(originResumeContent));
							}
						} else {
							noFiledCounter.increment(1);
						}
					} else {
						skipperCounter.increment(1);
					}
				}
			} finally {
				cleanup(context);
			}
		}

		private String putToHbase(byte[] originResumeContent, String md5) throws IOException {
			Put put = new Put(HTableUtil.gB(md5));
			put.addColumn(HTableUtil.gB("data"), HTableUtil.gB("originResumeContent"), originResumeContent);
			htable.put(put);
			return "http://resume.hgcluster:20177/raw/" + md5;
		}
	}

	public void configJob(Job job, String input, String output) throws Exception {
		job.setJarByClass(ResumeOriginContentMapred.class);
		job.setJobName("ResumeOriginContentMapred-youcj");
		job.setMapperClass(ResumeOriginContentMapred.Map.class);
		Path outPath = new Path(output);
		FileSystem fs = FileSystem.get(job.getConfiguration());
		fs.delete(outPath, true);
		FileInputFormat.setInputPaths(job, input);
		FileOutputFormat.setOutputPath(job, outPath);
		job.setInputFormatClass(TextInputFormat.class);
		job.setOutputFormatClass(TextOutputFormat.class);
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(Text.class);
		job.setNumReduceTasks(0);
	}

	@SuppressWarnings("RegexpSinglelineJava")
	public int run(String[] args) throws Exception {
		if (args.length < 2) {
			System.err
					.println("Usage: ResumeOriginContentMapred <Input> <Output>");
			System.exit(1);
		}
		Configuration conf = new Configuration();
		conf.setBoolean("mapreduce.input.fileinputformat.input.dir.recursive", true);
		Job job = Job.getInstance(conf);
		configJob(job, args[0], args[1]);
		job.waitForCompletion(true);
		return 0;
	}

	@SuppressWarnings("RegexpSinglelineJava")
	public static void main(String[] args) throws Exception {
		int exitCode = new ResumeOriginContentMapred().run(args);
		System.exit(exitCode);
	}
}
