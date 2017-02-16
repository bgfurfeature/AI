package com.bgfurfeature.zookeeper.distributed

import java.util

import org.apache.zookeeper.{WatchedEvent, Watcher, ZooKeeper}

/**
  * Created by C.J.YOU on 2017/2/16.
  */
class DistributedClient {

  private final val  connectString = "mini1:2181,mini2:2181,mini3:2181"
  private final val  sessionTimeout = 2000
  private final val parentNode = "/servers"
  // 注意:加volatile的意义何在？
  @volatile private  var serverList = new util.ArrayList[String]()
  private  var zk: ZooKeeper = null

  /**
    * 创建到zk的客户端连接
    */
  def getConnect() : Unit= {

    zk = new ZooKeeper(connectString, sessionTimeout, new Watcher() {
      override def process(event: WatchedEvent): Unit = {
        // 收到事件通知后的回调函数（应该是我们自己的事件处理逻辑）
        //重新更新服务器列表，并且注册了监听
        try {
          getServerList()
        } catch  {
          case e:Exception =>
        }
      }
    })
  }

  /**
    * 获取服务器信息列表
    */
  def getServerList() =  {

    // 获取服务器子节点信息，并且对父节点进行监听
    val children = zk.getChildren(parentNode, true)

    // 先创建一个局部的list来存服务器信息
    val servers = new util.ArrayList[String]()
    for (child <- children) {
      // child只是子节点的节点名
      val data = zk.getData(parentNode + "/" + child, false, null)
      servers.add(new String(data))
    }
    // 把servers赋值给成员变量serverList，已提供给各业务线程使用
    serverList = servers

    //打印服务器列表
    System.out.println(serverList)

  }

  /**
    * 业务功能
    */
  def handleBussiness() = {
    System.out.println("client start working.....")
    Thread.sleep(Long.MaxValue)
  }


  def main(args: Array[String]) {
    // 获取zk连接
    val client = new DistributedClient()
    client.getConnect()
    // 获取servers的子节点信息（并监听），从中获取服务器信息列表
    client.getServerList()
    // 业务线程启动
    client.handleBussiness()
  }

}
