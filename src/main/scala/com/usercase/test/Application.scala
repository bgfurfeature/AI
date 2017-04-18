package com.usercase.test

import io.vertx.core.json.JsonObject

/**
  * Created by Jerry on 2017/4/18.
  */
object Application {

  def main(args: Array[String]): Unit = {

    val jsonString = "{\"org\":{\"name\":\" 一头猫(北京)科技有限公司 \"},\"jobTitle\":\"开发经理\"," +
      "\"jobgrade\":3," +
      "\"startedAt\":\"2015-03-01T00:00:00.000Z\",\"endedAt\":\"2016-09-01T00:00:00.000Z\"," +
      "\"salaryDetail\":{\"salaryType\":1,\"salary\":25.0},\"salary\":\"25000\",\"leaderJobTitle\":\"\",\"description\":\"工作描述: 创业公司,负责技术>开发,开发经理.公司主要做艺术品电商,打造与社群为主的电商,致力于解决艺术品变现难和信任问题.负责整体开发和设计.\",\"functions\":[{\"code\":429,\"title\":\"技术\"}],\"industryDict\":{\"code\":18,\"name\":\"互联网/软件/服务\"}}"

    // java 对象都是引用，直接修改会改变原值
    val json = new JsonObject(jsonString)
    val org = json.getJsonObject("org", null)
    val name = org.getString("name", "")
    org.put("name", "")
    println(json)

  }

}
