package com.bgfurfeature.zookeeper.distributed

import java.util.{Collections, Random}

import org.apache.zookeeper.Watcher.Event.EventType
import org.apache.zookeeper.ZooDefs.Ids
import org.apache.zookeeper.{CreateMode, WatchedEvent, Watcher, ZooKeeper}

/**
  * Created by C.J.YOU on 2017/2/16.
  * 分布式共享锁
  */
class DistributedClientLock {

  // 会话超时
  private  final val SESSION_TIMEOUT = 2000
  // zookeeper集群地址
  private val  hosts = "mini1:2181,mini2:2181,mini3:2181"
  private  val groupNode = "locks"
  private  val subNode = "sub"
  private  val haveLock = false

  private var zk: ZooKeeper = null
  // 记录自己创建的子节点路径
  @volatile private  var thisPath = ""

  /**
    * 连接zookeeper
    */
  def connectZookeeper() =  {
    zk = new ZooKeeper(hosts, SESSION_TIMEOUT, new Watcher() {
      override def process(event: WatchedEvent): Unit = {
        try {

          // 判断事件类型，此处只处理子节点变化事件
          if (event.getType() == EventType.NodeChildrenChanged && event.getPath().equals("/" + groupNode)) {
            //获取子节点，并对父节点进行监听
            val childrenNodes = zk.getChildren("/" + groupNode, true)
            val thisNode = thisPath.substring(("/" + groupNode + "/").length())
            // 去比较是否自己是最小id
            Collections.sort(childrenNodes)
            if (childrenNodes.indexOf(thisNode) == 0) {
              //访问共享资源处理业务，并且在处理完成之后删除锁
              doSomething()

              //重新注册一把新的锁
              thisPath = zk.create("/" + groupNode + "/" + subNode, null, Ids.OPEN_ACL_UNSAFE,
                CreateMode.EPHEMERAL_SEQUENTIAL)
            }
          }
        } catch  {
          case e:Exception => println(e.printStackTrace())
        }
      }
    })

    // 1、程序一进来就先注册一把锁到zk上
    thisPath = zk.create("/" + groupNode + "/" + subNode, null, Ids.OPEN_ACL_UNSAFE,
      CreateMode.EPHEMERAL_SEQUENTIAL)

    // wait一小会，便于观察
    Thread.sleep(new Random().nextInt(1000))

    // 从zk的锁父目录下，获取所有子节点，并且注册对父节点的监听
    val childrenNodes = zk.getChildren("/" + groupNode, true)

    //如果争抢资源的程序就只有自己，则可以直接去访问共享资源 
    if (childrenNodes.size() == 1) {
      doSomething()
      thisPath = zk.create("/" + groupNode + "/" + subNode, null, Ids.OPEN_ACL_UNSAFE,
        CreateMode.EPHEMERAL_SEQUENTIAL)
    }
  }

  /**
    * 处理业务逻辑，并且在最后释放锁
    */
  private def doSomething() = {
    try {
      System.out.println("gain lock: " + thisPath)
      Thread.sleep(2000)
      // do something
    } finally {
      System.out.println("finished: " + thisPath)
      // 删除当前客户端创建的节点锁
      zk.delete(this.thisPath, -1)
    }
  }

  def main(args: Array[String]) {
    val dl = new DistributedClientLock()
    dl.connectZookeeper()
    Thread.sleep(Long.MaxValue)
  }

}
