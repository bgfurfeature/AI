package com.bgfurfeature.hbase

import com.bgfurfeature.log.CLogger
import org.apache.hadoop.conf.Configuration
import org.apache.spark.SparkContext
import org.apache.hadoop.hbase.{HBaseConfiguration, HColumnDescriptor, HTableDescriptor, TableName}
import org.apache.hadoop.hbase.client.{Result, _}
import org.apache.hadoop.hbase.io.ImmutableBytesWritable
import org.apache.hadoop.hbase.mapreduce.TableInputFormat
import org.apache.hadoop.hbase.protobuf.ProtobufUtil
import org.apache.hadoop.hbase.protobuf.generated.ClientProtos
import org.apache.hadoop.hbase.util.Base64
import org.apache.spark.rdd.RDD


/**
  * Created by devops on 2017/4/6.
  */
class HBaseUtil(isConfig: Boolean) extends HBase with CLogger {

  // 自定义访问hbase的接口（属于HBASE 的一些属性）

  private val hBaseConfiguration: Configuration = getConfiguration()

  @transient private lazy val connection = createConnection


  /**
    * 配置HBase的参数信心
    * @param property 参数
    * @return Configuration
    */
  def setConfiguration(property: Map[String, String]) = {

    // hBaseConfiguration = HBaseConfiguration.create

    property.foreach{ case(key, value) =>

      hBaseConfiguration.set(key, value)

    }

    hBaseConfiguration

  }

  def configuration = hBaseConfiguration

  private  def getConfiguration() = {

    val hBaseConfiguration = HBaseConfiguration.create()

    if(isConfig) {
      warn("hbase setDefaultConfiguration....")
      setDefaultConfiguration(hBaseConfiguration)
    } else  {

      println("use config")

      hBaseConfiguration
    }
  }

  def setDefaultConfiguration(hBaseConfiguration: Configuration) = {

    // hBaseConfiguration = HBaseConfiguration.create

    // 本地测试 需配置的选项， 在集群上通过对应配置文件路径自动获得
    hBaseConfiguration.set("fs.defaultFS", "hdfs://hg001:8020"); // nameservices的路径
    // hBaseConfiguration.set("dfs.nameservices", "ns1");  //
    // hBaseConfiguration.set("dfs.ha.namenodes.ns1", "nn1,nn2"); //namenode的路径
    // hBaseConfiguration.set("dfs.namenode.rpc-address.ns1.nn1", "server3:9000"); // namenode 通信地址
    // hBaseConfiguration.set("dfs.namenode.rpc-address.ns1.nn2", "server4:9000"); // namenode 通信地址
    // 设置namenode自动切换的实现类
    hBaseConfiguration.set("dfs.client.failover.proxy.provider.ns1", "org.apache.hadoop.hdfs.server.namenode.ha.ConfiguredFailoverProxyProvider")
    hBaseConfiguration.set("hbase.rootdir", "hdfs://hg001:8020/hbase")
    hBaseConfiguration.set("hbase.zookeeper.quorum", "hg001")
    hBaseConfiguration.set("hbase.zookeeper.property.clientPort", "2181")

    hBaseConfiguration

  }

  /**
    * 获取HBase的连接器
    * @return connection
    */
  def createConnection: Connection = {

    warn("create connection")

    val connection = ConnectionFactory.createConnection(getConfiguration())

    sys.addShutdownHook {
      connection.close ()
    }
    connection
  }

  def shutDown() ={
    connection.close()

  }

  /**
    * 创建hbase表
    * @param tableName 表名
    * @param columnFamilies 列族的声明
    *
    */
  def createHBaseTable(tableName: String, columnFamilies:List[String]): Table = {

    // connection.getAdmin.createTable(new HTableDescriptor(tableName).addFamily(new HColumnDescriptor(columnFamily).setMaxVersions(3)))

    val admin = connection.getAdmin

    val table = TableName.valueOf(tableName)

    val htd = new HTableDescriptor(table)

    for (family <- columnFamilies) {

      val hcd = new HColumnDescriptor(family)
      htd.addFamily(hcd.setMaxVersions(3))

    }

    if(!admin.tableExists(table)) {
      admin.createTable(htd)
    }

    admin.close()

    val htable = getHTable(tableName)

    htable

  }


  /**
    * 判断 row key 是否存在
    * @param row rowKey
    * @param  table table
    * @return Boolean
    */
  def existRowKey(row:String, table:Table): Boolean = {

    val get = new Get(getBytes(row))
    val result = table.get(get)

    if (result.isEmpty) {
      warn("hbase table don't have this data,execute insert")
      return false
    }

    true

  }

  def getHTable(tableName: String) = {
    connection.getTable(TableName.valueOf(tableName))
  }

  def  getDataFormHbase(rowKey:String, table: Table, colmnFamily:String, qualifier: String):
  String = {

    val result = table.get(new Get(getBytes(rowKey)));

    result.getValue(getBytes(colmnFamily), getBytes(qualifier)).toString

  }

  /**
    * 通过rdd 形式批量获取hbase数据
    * @param sc sparkContext
    * @param property 获取数据的属性：tableName, family, qualifiers , ...(TableInputFormat.SCAN，....)
    * @param scan  scan 定义
    * @return  RDD[Result]
    */
  def getHBaseDataThroughNewAPI(sc: SparkContext, property: Map[String, String],
                                scan: Scan) = {

    val proto:ClientProtos.Scan = ProtobufUtil.toScan(scan)
    val scanToString = Base64.encodeBytes(proto.toByteArray)

    if(hBaseConfiguration == null)
      throw new Exception(s"please init configuration first")

    // hbase-server pkg
    hBaseConfiguration.set(TableInputFormat.SCAN, scanToString)

    setConfiguration(property)

    val hbaseRDD = sc.newAPIHadoopRDD(hBaseConfiguration, classOf[TableInputFormat], classOf[ImmutableBytesWritable], classOf[Result]).map(_._2 )

    hbaseRDD

  }

  /**
    * 批量写数据入 Hbase中
    * @param table
    * @param rowKey
    * @param data
    */
  def putToHbase(table:Table, rowKey: String, data: RDD[DataFormatForTable]) = {

    data.foreachPartition { iterator => {

      val putList = iterator.map { dataForHbase =>
        val put = new Put(getBytes(rowKey))
        put.addColumn(getBytes(dataForHbase.columnFamily), getBytes(dataForHbase.qualifier), getBytes(dataForHbase.content))

      }

      val arrayList = toJavaList(putList.toList)

      table.put(arrayList)

    }}

    table.close()
  }

}
