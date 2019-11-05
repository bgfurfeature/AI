package com.jbgfurfeature.hadoop;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.CombineFileSplit;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;

/**
 * This RecordReader implementation extracts individual files from a ZIP
 * file and hands them over to the Mapper. The "key" is the decompressed
 * file name, the "value" is the file contents.
 */
public class RawFileRecordReader
    extends RecordReader<Text, BytesWritable> {
  /**
   * InputStream used to read the ZIP file from the FileSystem
   */
  private FSDataInputStream fsin;

  /**
   * ZIP file parser/decompresser
   */
  private ZipInputStream zip;

  /**
   * Uncompressed file name
   */
  private Text currentKey;

  /**
   * Uncompressed file contents
   */
  private BytesWritable currentValue;

  /**
   * Used to indicate progress
   */
  private boolean isFinished = false;


  public RawFileRecordReader() {

  }

  public RawFileRecordReader(CombineFileSplit split, TaskAttemptContext context, Integer index)
      throws IOException {
    Path path = split.getPath(index);
    FileSystem fs = path.getFileSystem(context.getConfiguration());
    // Open the stream
    fsin = fs.open(path);
    if (path.getName().endsWith(".zip")) {
//            zip = new ZipInputStream(fsin);
      zip = new ZipInputStream(fsin, Charset.forName("GBK"));
    } else {
      currentKey = new Text(path.getName());
    }

  }

  /**
   * Initialise and open the ZIP file from the FileSystem
   */
  @Override
  public void initialize(InputSplit inputSplit, TaskAttemptContext taskAttemptContext)
      throws IOException, InterruptedException {
    if (fsin == null) {
      FileSplit split = (FileSplit) inputSplit;
      Configuration conf = taskAttemptContext.getConfiguration();
      Path path = split.getPath();
      FileSystem fs = path.getFileSystem(conf);

      // Open the stream
      fsin = fs.open(path);
      if (path.getName().endsWith(".zip")) {
//                zip = new ZipInputStream(fsin);
        zip = new ZipInputStream(fsin, Charset.forName("GBK"));
      } else {
        currentKey = new Text(path.getName());
      }
    }
  }

  /**
   * This is where the magic happens, each ZipEntry is decompressed and
   * readied for the Mapper. The contents of each file is held *in memory*
   * in a BytesWritable object.
   *
   * If the ZipFileInputFormat has been set to Lenient (not the default),
   * certain exceptions will be gracefully ignored to prevent a larger job
   * from failing.
   */
  @Override
  @SuppressWarnings("IllegalCatch")
  public boolean nextKeyValue()
      throws IOException, InterruptedException {
    ByteArrayOutputStream bos = null;
    if (isFinished) {
      try {
        zip.close();
      } catch (Exception ignore) {
      }
      try {
        fsin.close();
      } catch (Exception ignore) {
      }
      return false;
    } else if (zip == null) {
      bos = new ByteArrayOutputStream();
      // currentKey

      // Read the file contents
      byte[] temp = new byte[8192];
      while (true) {
        int bytesRead = 0;
        try {
          bytesRead = fsin.read(temp, 0, 8192);
        } catch (EOFException e) {
          if (!RawFileInputFormat.getLenient()) {
            throw e;
          }
          e.printStackTrace();
          return false;
        }
        if (bytesRead > 0) {
          bos.write(temp, 0, bytesRead);
        } else {
          break;
        }
      }
      isFinished = true;
    } else {
      bos = new ByteArrayOutputStream();
      ZipEntry entry = null;
      try {
        entry = zip.getNextEntry();
      } catch (ZipException e) {
        if (!RawFileInputFormat.getLenient()) {
          throw e;
        }
      } catch (IllegalArgumentException iae) {
        isFinished = true;
        return false;
      }

      // Sanity check
      if (entry == null) {
        isFinished = true;
        return false;
      }

      // Filename
      currentKey = new Text(entry.getName());

      // Read the file contents
      byte[] temp = new byte[8192];
      while (true) {
        int bytesRead = 0;
        try {
          bytesRead = zip.read(temp, 0, 8192);
        } catch (EOFException e) {
          if (!RawFileInputFormat.getLenient()) {
            throw e;
          }
          return false;
        }
        if (bytesRead > 0) {
          bos.write(temp, 0, bytesRead);
        } else {
          break;
        }
      }
      zip.closeEntry();
    }

    // Uncompressed contents
    currentValue = new BytesWritable(bos.toByteArray());
    return true;

  }

  /**
   * Rather than calculating progress, we just keep it simple
   */
  @Override
  public float getProgress()
      throws IOException, InterruptedException {
    return isFinished ? 1 : 0;
  }

  /**
   * Returns the current key (name of the zipped file)
   */
  @Override
  public Text getCurrentKey()
      throws IOException, InterruptedException {
    return currentKey;
  }

  /**
   * Returns the current value (contents of the zipped file)
   */
  @Override
  public BytesWritable getCurrentValue()
      throws IOException, InterruptedException {
    return currentValue;
  }

  /**
   * Close quietly, ignoring any exceptions
   */
  @Override
  @SuppressWarnings("IllegalCatch")
  public void close()
      throws IOException {
    try {
      zip.close();
    } catch (Exception ignore) {
    }
    try {
      fsin.close();
    } catch (Exception ignore) {
    }
  }
}
