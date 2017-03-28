package com.usercase.resume.input

import java.util

import org.apache.hadoop.io.{BytesWritable, Text}
import org.apache.hadoop.mapreduce.{InputSplit, RecordReader, TaskAttemptContext}
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat

/**
  * Created by devops on 2017/3/28.
  */
class RawFileInputFormat  extends  FileInputFormat[Text, BytesWritable] {

  override def createRecordReader(inputSplit: InputSplit, taskAttemptContext: TaskAttemptContext):
  RecordReader[Text, BytesWritable] = {

    new RawFileRecordReader()

  }

}



