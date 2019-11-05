  package com.bgfurfeature.webmagic.demo

  import us.codecraft.webmagic.monitor.SpiderMonitor
  import us.codecraft.webmagic.pipeline.{ConsolePipeline, FilePipeline}
  import us.codecraft.webmagic.{Page, Site, Spider}
  import us.codecraft.webmagic.processor.PageProcessor

  /**
    * Created by Jerry on 2017/5/4.
    * http://webmagic.io/docs/zh/posts/ch4-basic-page-processor/pageprocessor.html
    */
  class GithubRepoPageProcessor extends PageProcessor {

    // 部分一：抓取网站的相关配置，包括编码、抓取间隔、重试次数、代理等
    private val site = Site.me().setRetryTimes(3).setSleepTime(1000)

    override def process(page: Page): Unit = {
      // 部分二：定义如何抽取页面信息，并保存下来
      page.putField("author", page.getUrl().regex("https://github\\.com/(\\w+)/.*").toString())
      page.putField("name", page.getHtml().xpath("//h1[@class='entry-title public']/strong/a/text()").toString())
      if (page.getResultItems().get("name") == null) {
        //skip this page
        page.setSkip(true)
      }
      page.putField("readme", page.getHtml().xpath("//div[@id='readme']/tidyText()"))

      // 部分三：从页面发现后续的url地址来抓取
      page.addTargetRequests(page.getHtml().links().regex("(https://github\\.com/[\\w\\-]+/[\\w\\-]+)").all())

      // println(page.getResultItems.get("author"))

    }

    override def getSite: Site = site
  }

  object SpiderObject {
    def main(args: Array[String]): Unit = {
      val githubSpider = Spider.create(new GithubRepoPageProcessor())
        //从"https://github.com/code4craft"开始抓
        .addUrl("https://github.com/code4craft")
        // 使用Pipeline保存结果(ConsolePipeline/JsonFilePipeline)
        .addPipeline(new ConsolePipeline)
        .addPipeline(new FilePipeline("/Users/devops/workspace/shell/webmagic/file_name"))
        //开启5个线程抓取
        .thread(5)
        // 添加爬虫的监控(WebMagic的监控使用JMX提供控制，你可以使用任何支持JMX的客户端来进行连接。我们这里以JDK自带的JConsole为例)
      SpiderMonitor.instance().register(githubSpider)
      // 启动爬虫
      githubSpider.start()

    }
  }
