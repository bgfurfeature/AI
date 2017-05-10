package com.jbgfurfeature.hadoop;

import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.CombineFileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.CombineFileRecordReader;
import org.apache.hadoop.mapreduce.lib.input.CombineFileSplit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class MultipleFileInputFormat extends CombineFileInputFormat<Text, BytesWritable> {
  public MultipleFileInputFormat() {
    super();
    setMaxSplitSize(32777216); // 64 MB, default block size on hadoop
  }

  public RecordReader<Text, BytesWritable> createRecordReader(InputSplit split,
                                                              TaskAttemptContext context) throws
      IOException {
    return new CombineFileRecordReader<Text, BytesWritable>((CombineFileSplit) split, context, com.jbgfurfeature.hadoop.RawFileRecordReader.class);
  }

  @Override
  protected List<FileStatus> listStatus(JobContext job) throws IOException {
    List<FileStatus> statuss = super.listStatus(job);
    ArrayList<FileStatus> todos = new ArrayList<>();
    for (FileStatus fst : statuss) {
      String name = fst.getPath().getName();
      if (!name.endsWith(".zip") && !name.endsWith(".doc") && !name.endsWith(".docx") && !name
          .endsWith("pdf")) {
        continue;
      }
      todos.add(fst);
    }
    return todos;
  }

  @Override
  protected boolean isSplitable(JobContext context, Path file) {
    return false;
  }
}
