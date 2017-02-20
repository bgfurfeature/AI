package com.bgfurfeature.zookeeper

import org.apache.zookeeper.ZooDefs.Ids
import org.apache.zookeeper.{CreateMode, WatchedEvent, Watcher, ZooKeeper}

/**
  * Created by C.J.YOU on 2017/2/16.
  */
class SimpleZKClient {

  val connectString="host1:2181,host2:2181,..."
  val sessionTimeOut = 2000
  var zkClient: ZooKeeper = null

  // zkClient init
  def  init() = {
     zkClient = new ZooKeeper(connectString, sessionTimeOut, new Watcher {
      override def process(watchedEvent: WatchedEvent): Unit = {
        // 收到事件通知后的回调函数（应该是我们自己的事件处理逻辑）
        println(watchedEvent.getType() + "---" + watchedEvent.getPath())

        zkClient.getChildren("/", true)
      }
    })
    zkClient
  }

  case class Znode(nodeName:String, nodeData: String, createMode: CreateMode)
  // 创建数据节点到zk中
  def testCreate(znode: Znode) = {

    val nodeCreated = zkClient.create(znode.nodeName, znode.nodeData.getBytes, Ids.OPEN_ACL_UNSAFE, znode.createMode)
    //上传的数据可以是任何类型，但都要转成byte[]
  }

  // 判断znode是否存在
  def testExist(znode: Znode): Unit = {
    val stat = zkClient.exists(znode.nodeName, false)
    println(if(stat == null) "notExist")
  }

  // 获取子节点
  def getChildren(znode: Znode) = {
    val childrens = zkClient.getChildren(znode.nodeName, true)
    val length = childrens.size()
    for(children <- 0 until length) {
      println(childrens.get(children))
    }
  }

  // 获取node 数据
  def getData(znode: Znode) = {

    val data = zkClient.getData(znode.nodeName, false, null)
    println(data)

  }

  // 删除znode
  def deleteZnode() = {
    //参数2：指定要删除的版本，-1表示删除所有版本
    zkClient.delete("/eclipse", -1)
  }

  // 修改node数据
  def setData(znode: Znode) = {
    zkClient.setData(znode.nodeName, znode.nodeData.getBytes, -1)
    val data = zkClient.getData(znode.nodeName, false, null)
    println(new String(data))
  }

  def main(args: Array[String]) {

  }

}
