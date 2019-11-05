package com.usercase.resume.input

import java.util

import org.apache.hadoop.fs.Path
import org.apache.hadoop.io.{BytesWritable, Text}
import org.apache.hadoop.mapreduce.{InputSplit, JobContext, RecordReader, TaskAttemptContext}
import org.apache.hadoop.mapreduce.lib.input.{CombineFileRecordReader, CombineFileSplit, FileInputFormat}

/**
  * Created by devops on 2017/3/28.
  *
  */
class RawFileInputFormat  extends  FileInputFormat[Text, BytesWritable] {

  override def createRecordReader(inputSplit: InputSplit, taskAttemptContext: TaskAttemptContext):
  RecordReader[Text, BytesWritable] = {

    new RawFileRecordReader()
  }

  // 每份简历文件不可被分片
  override def isSplitable(context: JobContext, filename: Path): Boolean = {

    false

  }

}



