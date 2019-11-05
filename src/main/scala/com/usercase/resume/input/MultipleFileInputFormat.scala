package com.usercase.resume.input

import org.apache.hadoop.fs.Path
import org.apache.hadoop.io.{BytesWritable, Text}
import org.apache.hadoop.mapreduce.{InputSplit, JobContext, RecordReader, TaskAttemptContext}
import org.apache.hadoop.mapreduce.lib.input.{CombineFileInputFormat, CombineFileRecordReader, CombineFileSplit}

/**
  * Created by devops on 2017/3/30.
  * 场景描述：
  * MapReduce程序会将输入的文件进行分片(Split)，每个分片对应一个map任务，而默认一个文件至少有一个分片，
  * 一个分片也只属于一个文件。这样大量的小文件会导致大量的map任务，导致资源过度消耗，且效率低下。
  * Hadoop自身包含了CombineFileInputFormat，其功能是将多个小文件合并如一个分片，由一个map任务处理，
  * 这样就减少了不必要的map数量
  * CombineFileInputFormat 适合处理海量小文件，CombineInputFormat处理少量，较大的文件没有优势，相反，
  * 如果没有合理的设置maxSplitSize，minSizeNode，minSizeRack，
  * 则可能会导致一个map任务需要大量访问非本地的Block造成网络开销，反而比正常的非合并方式更慢。
  * 针对大量远小于块大小的小文件处理，CombineInputFormat的使用还是很有优势
  */
class MultipleFileInputFormat  extends  CombineFileInputFormat[Text, BytesWritable] {

  override def createRecordReader(inputSplit: InputSplit, taskAttemptContext: TaskAttemptContext)
  : RecordReader[Text, BytesWritable] = {

    new CombineFileRecordReader[Text, BytesWritable](inputSplit.asInstanceOf[CombineFileSplit],
      taskAttemptContext, classOf[RawFileRecordReader])
  }

  override def setMaxSplitSize(maxSplitSize: Long): Unit = {

    // 如果maxSplitSize，minSizeNode，minSizeRack三个都没有设置，那是所有输入整合成

    setMaxSplitSize(134217728) // 128M block size in hadoop 2+


  }

  override def isSplitable(context: JobContext, file: Path): Boolean = {

    false
  }
}
