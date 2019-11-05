package com.bgfurfeature.mr.util;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;

public class HTableUtil {
  private static HTable table;
  private static Configuration conf;

  static {
    // 与hbase/conf/hbase-site.xml中hbase.zookeeper.quorum配置的值相同
    conf = HBaseConfiguration.create();
    conf.set("hbase.zookeeper.quorum", "hg001,hg002,hg003,hg004,hg006");
    conf.set("fs.default.name", "hg001:9000");
    // 与hbase/conf/hbase-site.xml中hbase.zookeeper.property.clientPort配置的值相同
    conf.set("hbase.zookeeper.property.clientPort", "2181");

  }
  public static Configuration getConf() {
    return conf;
  }

  public static HTable getHTable(String tablename) throws IOException {
    if (table == null) {
      table = new HTable(conf, tablename);
    }
    return table;
  }

  public static byte[] gB(String name) {
    return Bytes.toBytes(name);
  }

}
