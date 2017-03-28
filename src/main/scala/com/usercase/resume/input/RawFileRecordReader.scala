package com.usercase.resume.input

import java.io.ByteArrayOutputStream
import java.nio.charset.Charset
import java.util.zip.{ZipEntry, ZipInputStream}

import org.apache.hadoop.fs.FSDataInputStream
import org.apache.hadoop.io.{BytesWritable, Text}
import org.apache.hadoop.mapreduce.lib.input.FileSplit
import org.apache.hadoop.mapreduce.{InputSplit, RecordReader, TaskAttemptContext}

import scala.util.control.Breaks

/**
  * Created by devops on 2017/3/28.
  */
class RawFileRecordReader extends RecordReader[Text, BytesWritable] {

  /**
    * InputStream used to read the ZIP file from the FileSystem
    */
  private var fsin: FSDataInputStream = null

  /**
    * ZIP file parser/decompresser
    */
  private var zip : ZipInputStream = null

  /**
    * Uncompressed file name
    */
  private var currentKey: Text = null

  /**
    * Uncompressed file contents
    */
  private var currentValue: BytesWritable = null

  /**
    * Used to indicate progress
    */
  private var isFinished = false


  override def getCurrentKey: Text = {
    currentKey
  }

  override def getProgress: Float = {

    return if(isFinished) 1f else 0f
  }

  override def nextKeyValue(): Boolean = {

    var bos: ByteArrayOutputStream  = null

    if(isFinished) {

      close()

      return false

    } else if(zip == null) { // doc,docx, txt

      bos = new ByteArrayOutputStream()

      val tmp = new Array[Byte](8192)

      val break = new Breaks

      break.breakable {

        while(true) {

          var bytesRead = 0

          try {
            bytesRead = fsin.read(tmp, 0, 8192)
          } catch {
            case e: Exception =>
              return false
          }

          if(bytesRead > 0) {
            bos.write(tmp,0,bytesRead)
          } else  break.break()
        }
      }

      isFinished = true

      return  true

    } else { // ZIP

      bos = new ByteArrayOutputStream()

      var entry: ZipEntry = null

       try {
         entry = zip.getNextEntry
       } catch {
         case e: Exception =>
           return false
       }

      currentKey = new Text(entry.getName)

      val tmp = new Array[Byte](8192)

      val break = new Breaks

      break.breakable {

        while(true) {

          var bytesRead = 0

          try {
            bytesRead = zip.read(tmp, 0, 8192)
          } catch {
            case e: Exception =>
              return false
          }

          if(bytesRead > 0) {
            bos.write(tmp,0,bytesRead)
          } else  break.break()
        }
      }

      zip.closeEntry()

      currentValue = new BytesWritable(bos.toByteArray())
      isFinished = true

      return true

    }

  }

  override def getCurrentValue: BytesWritable = {
    currentValue
  }

  override def initialize(inputSplit: InputSplit, taskAttemptContext: TaskAttemptContext): Unit = {

    if (fsin == null) {

      val fileSplit = inputSplit.asInstanceOf[FileSplit]
      val conf = taskAttemptContext.getConfiguration
      val path = fileSplit.getPath
      val fs = path.getFileSystem(conf)

      fsin = fs.open(path)

      if (path.getName().endsWith(".zip")) {
        zip = new ZipInputStream(fsin, Charset.forName("GBK"))
      } else {
        currentKey = new Text(path.getName())
      }

    }

  }

  override def close(): Unit = {

    try {
      zip.close()
    } catch  {
      case  e: Exception =>

    }

    try {
      fsin.close()
    } catch  {
      case  e: Exception =>

    }

  }
}
