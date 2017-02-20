package com.bgfurfeature.zookeeper.distributed

import org.apache.zookeeper.ZooDefs.Ids
import org.apache.zookeeper.{CreateMode, WatchedEvent, Watcher, ZooKeeper}

/**
  * Created by C.J.YOU on 2017/2/16.
  */
class DistributedServer {

  private final val  connectString = "mini1:2181,mini2:2181,mini3:2181"
  private final val  sessionTimeout = 2000
  private final val parentNode = "/servers"
  private  var zk: ZooKeeper = null
  /**
    * 创建到zk的客户端连接
    */
  def getConnect() = {

    zk = new ZooKeeper(connectString, sessionTimeout, new Watcher(){
      override def process(event: WatchedEvent): Unit = {
        // 收到事件通知后的回调函数（应该是我们自己的事件处理逻辑）
        System.out.println(event.getType() + "---" + event.getPath())
        try {
          zk.getChildren("/", true)
        } catch  {
          case e:Exception =>
        }
      }
    })

  }

  /**
    * 向zk集群注册服务器信息
    *
    * @param hostname
    *
    */
  def registerServer(hostname: String)= {

    // 临时server节点， 当出现服务下线断开连接时，对应的节点会自动删除掉
    val create = zk.create(parentNode + "/server", hostname.getBytes(),
      Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL)
    System.out.println(hostname + "is online.." + create)

  }

  /**
    * 业务功能
    */
  def handleBussiness(hostname: String) = {
    System.out.println(hostname + "start working.....")
    Thread.sleep(Long.MaxValue)
  }

  def main(args: Array[String]) {

    val servers = args
    // 获取zk连接
    val server = new DistributedServer()
    server.getConnect()
    // 利用zk连接注册服务器信息
    for (host <- servers) {
      server.registerServer(host)
    }

    // 启动业务功能
    for (host <- servers) {
      server.handleBussiness(hostname = host)
    }

  }

}
