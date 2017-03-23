package com.bgfurfeature.hdfs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Iterator;
import java.util.Map.Entry;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
/**
 * 
 * 客户端去操作hdfs时，是有一个用户身份的
 * 默认情况下，hdfs客户端api会从jvm中获取一个参数来作为自己的用户身份：-DHADOOP_USER_NAME=hadoop
 * 
 * 也可以在构造客户端fs对象时，通过参数传递进去
 * @author
 *
 */
public class HdfsClientDemo {
	FileSystem fs = null;
	Configuration conf = null;
	@Before
	public void init() throws Exception{
		
		conf = new Configuration();
		conf.set("fs.defaultFS", "hdfs://master:9000");
		
		//拿到一个文件系统操作的客户端实例对象
		/*fs = FileSystem.get(conf);*/
		//可以直接传入 uri和用户身份
		fs = FileSystem.get(new URI("hdfs://master:9000"),conf,"hadoop"); //最后一个参数为用户名
	}

	@Test
	public void testDownLoadFileToLocal() throws IllegalArgumentException, IOException{

		//先获取一个文件的输入流----针对hdfs上的
		FSDataInputStream in = fs.open(new Path("/jdk-7u65-linux-i586.tar.gz"));

		//再构造一个文件的输出流----针对本地的
		FileOutputStream out = new FileOutputStream(new File("c:/jdk.tar.gz"));

		//再将输入流中数据传输到输出流
		IOUtils.copyBytes(in, out, 4096);


	}

	@Test
	public void testUploadByStream() throws Exception{

		//hdfs文件的输出流
		FSDataOutputStream fsout = fs.create(new Path("/aaa.txt"));

		//本地文件的输入流
		FileInputStream fsin = new FileInputStream("c:/111.txt");

		IOUtils.copyBytes(fsin, fsout,4096);


	}


	/**
	 * hdfs支持随机定位进行文件读取，而且可以方便地读取指定长度
	 * 用于上层分布式运算框架并发处理数据
	 * @throws IllegalArgumentException
	 * @throws IOException
	 */
	@Test
	public void testRandomAccess() throws IllegalArgumentException, IOException {

		FSDataInputStream in = fs.open(new Path("/iloveyou.txt"));//先获取一个文件的输入流----针对hdfs上的
		in.seek(22);	//可以将流的起始偏移量进行自定义
		FileOutputStream out = new FileOutputStream(new File("c:/iloveyou.line.2.txt"));//再构造一个文件的输出流----针对本地的
		IOUtils.copyBytes(in,out,19L,true);	//apache的工具类， 3.缓冲大小，4.是否关闭流

	}



	/**
	 * 读取指定的block
	 * @throws IOException
	 * @throws IllegalArgumentException
	 */
	@Test
	public void testCat() throws IllegalArgumentException, IOException {

		FSDataInputStream in = fs.open(new Path("/weblog/input/access.log.10"));
		//拿到文件信息
		FileStatus[] listStatus = fs.listStatus(new Path("/weblog/input/access.log.10"));
		//获取这个文件的所有block的信息
		BlockLocation[] fileBlockLocations = fs.getFileBlockLocations(listStatus[0], 0L, listStatus[0].getLen());


		//第一个block的长度
		long length = fileBlockLocations[0].getLength();
		//第一个block的起始偏移量
		long offset = fileBlockLocations[0].getOffset();

		System.out.println(length);
		System.out.println(offset);

		//获取第一个block写入输出流
//		IOUtils.copyBytes(in, System.out, (int)length);
		byte[] b = new byte[4096];

		FileOutputStream os = new FileOutputStream(new File("d:/block0"));
		while(in.read(offset, b, 0, 4096)!=-1){
			os.write(b);
			offset += 4096;
			if(offset>length) return;
		};

		os.flush();
		os.close();
		in.close();


	}

	@Test
	public void testUpload() throws Exception {
		
		Thread.sleep(2000);
		fs.copyFromLocalFile(new Path("G:/access.log"), new Path("/access.log.copy"));
		fs.close();
	}
	
	
	@Test
	public void testDownload() throws Exception {

		// IOUtils.copyBytes(); IOUtil类实现数据传输的封装
		fs.copyToLocalFile(new Path("/access.log.copy"), new Path("d:/"));
		fs.close();
	}
	
	@Test
	public void testConf(){
		Iterator<Entry<String, String>> iterator = conf.iterator();
		while (iterator.hasNext()) {
			Entry<String, String> entry = iterator.next();
			System.out.println(entry.getValue() + "--" + entry.getValue());//conf加载的内容
		}
	}
	
	/**
	 * 创建目录
	 */
	@Test
	public void makdirTest() throws Exception {
		boolean mkdirs = fs.mkdirs(new Path("/aaa/bbb"));
		System.out.println(mkdirs);
	}
	
	/**
	 * 删除
	 */
	@Test
	public void deleteTest() throws Exception{
		boolean delete = fs.delete(new Path("/aaa"), true);//true， 递归删除
		System.out.println(delete);
	}
	
	@Test
	public void listTest() throws Exception{
		
		FileStatus[] listStatus = fs.listStatus(new Path("/"));
		for (FileStatus fileStatus : listStatus) {
			System.err.println(fileStatus.getPath() +" ================= " + fileStatus.toString());
		}
		//会递归找到所有的文件
		RemoteIterator<LocatedFileStatus> listFiles = fs.listFiles(new Path("/"), true);
		while(listFiles.hasNext()){
			LocatedFileStatus next = listFiles.next();
			String name = next.getPath().getName();
			Path path = next.getPath();
			System.out.println(name + "---" + path.toString());
		}
	}
	
	public static void main(String[] args) throws Exception {
		Configuration conf = new Configuration();
		conf.set("fs.defaultFS", "hdfs://master:9000");
		//拿到一个文件系统操作的客户端实例对象
		FileSystem fs = FileSystem.get(conf);
		
		fs.copyFromLocalFile(new Path("G:/access.log"), new Path("/access.log.copy"));
		fs.close();
	}
	

}
