package com.bgfurfeature.util

/**
  * Created by devops on 2017/4/6.
  */
class BloomFilter (bitsLength: Int) {

    import java.util

    private  final val DEFAULT_SIZE = 2 << bitsLength // 24bit

    private  final val seeds = Array(7, 11, 12, 31, 37, 61) // 6bit hash function

    private  val  bits = new util.BitSet(DEFAULT_SIZE)

    private  val  func = new Array[SimpleHash](seeds.length)

    // 初始化随机种子
    for(i <- seeds.indices) {
      func(i) = new SimpleHash(DEFAULT_SIZE,seeds(i))
    }

    // 添加没有的数据
    def add(value:String)= {

      // if(!contain(value)) {
        for(i <- func.indices){
          bits.set(func(i).hash(value),true)
      //   }
      }
    }

    // 判断是否包含value（每个hash函数映射的位都一样的时候才判断为存在）
    def contain(value:String) : Boolean = {
      if(value == null)
        return false

      var flag = true
      for(i <- func.indices){
        flag = flag && bits.get(func(i).hash(value))
      }
      flag
    }
  }
  // 取值的简单哈希函数
  class SimpleHash(cap:Int,seed:Int) {

    def hash(value:String): Int = {

      var result = 0
      val length = value.length
      for(i <- 0 until length) {
        result = seed * result + value.charAt(i)
      }
      (cap - 1 ) & result
    }
  }

  object BloomFilter {

    private  var bf : BloomFilter = _

    def apply(length: Int): BloomFilter = {

      if(bf == null) bf = new BloomFilter(length)

      bf

    }

    def getInstance = bf


}
