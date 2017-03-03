package com.usercase.stock

import java.util.regex.{Matcher, Pattern}

import scala.io.Source
import scala.util.control.Breaks

/**
  * Created by C.J.YOU on 2016/12/23.
  */
object StockMatcherRegex {

  /**
    * 搜索和查看的数据的匹配 - 从本地化 的原始数据总获取，其他数据源需
    * 主要字段是否对应等问题，否则无法匹配
    *
    * @param input 源数据
    * @return 匹配后的数据
    */
  def VisitAndSearch(input: Array[String]): String = {

    val visitSearchPatterns = Array[String](
      // 搜索
      "quotes.money.163.com.*word=(.*)&t=", "quotes.money.163.com.*word=(.*)&count",
      "suggest3.sinajs.cn.*key=(((?!&name).)*)", "xueqiu.com.*code=(.*)&size", "xueqiu.com.*q=(.*)",
      "app.stcn.com.*&wd=(.*)", "q.ssajax.cn.*q=(.*)&type", "znsv.baidu.com.*wd=(.*)&ch",
      "so.stockstar.com.*q=(.*)&click", "quote.cfi.cn.*keyword=(.*)&his", "hq.cnstock.com.*q=(.*)&limit",
      "xinpi.cs.com.cn.*q=(.*)", "xinpi.cs.com.cn.*c=(.*)&q", "search.cs.com.cn.*searchword=(.*)",
      "d.10jqka.com.cn.*(\\d{6})/last", "news.10jqka.com.cn.*keyboard_(\\d{6})",
      "10jqka.com.cn.*w=(.*)&tid", "data.10jqka.com.cn.*/(\\d{6})/", "dict.hexin.cn.*pattern=(\\d{6})",
      "dict.hexin.cn.*pattern=(\\S.*)", "q.stock.sohu.com.*keyword=(.*)&(_|c)",
      "api.k.sohu.com.*words=(.*)&p1", "hq.p5w.net.*query=(.*)&i=", "code.jrjimg.cn.*key=(.*)&d=",
      "itougu.jrj.com.cn.*keyword=(.*)&is_stock", "wallstreetcn.com/search\\?q=(.*)",
      "api.markets.wallstreetcn.com.*q=(.*)&limit", "so.hexun.com.*key=(.*)&type",
      "finance.ifeng.com.*code.*(\\d{6})", "finance.ifeng.com.*q=(.*)&cb",
      "suggest.eastmoney.com.*input=(.*?)&", "so.eastmoney.com.*q=(.*)&m",
      "guba.eastmoney.com/search.aspx?t=(.*)", "www.yicai.com.*searchKeyWords=(.*)",
      "www.gw.com.cn.*q=(.*)&s=", "cailianpress.com/search.*keyword=(.*)",
      "api.cailianpress.com.*PutinInfo=(.*)&sign", "news.21cn.com.*keywords=(.*)&view",
      "news.10jqka.com.cn.*text=(.*)&jsoncallback", "smartbox.gtimg.cn.*q=(.*?)&",
      "swww.niuguwang.com/stock/.*q=(.*?)&","info.zq88.cn:9085.*query=(\\d{6})&",
      // 查看
      "quotes.money.163.com/app/stock/\\d(\\d{6})", "m.news.so.com.*q=(\\d{6})",
      "finance.sina.com.cn.*(\\d{6})/nc.shtml", "guba.sina.com.cn.*name=.*(\\d{6})",
      "platform.*symbol=\\w\\w(\\d{6})", "xueqiu.com/S/.*(\\d{6})", "cy.stcn.com/S/.*(\\d{6})",
      "mobile.stcn.com.*secucode=(\\d{6})", "stock.quote.stockstar.com/(\\d{6}).shtml",
      "quote.cfi.cn.*searchcode=(.*)", "hqapi.gxfin.com.*code=(\\d{6}).sh",
      "irm.cnstock.com.*index/(\\d{6})", "app.cnstock.com.*k=(\\d{6})",
      "guba.eastmoney.com.*code=.*(\\d{6})", "stockpage.10jqka.com.cn/(\\d{6})/",
      "0033.*list.*(\\d{6}).*json", "q.stock.sohu.com/cn/(\\d{6})/", "s.m.sohu.com.*/(\\d{6})",
      "data.p5w.net.*code.*(\\d{6})", "hq.p5w.net.*a=.*(\\d{6})", "jrj.com.cn.*(\\d{6}).s?html",
      "mapi.jrj.com.cn.*stockcode=(\\d{6})", "api.buzz.wallstreetcn.com.*(\\d{6})&cid",
      "stockdata.stock.hexun.com/(\\d{6}).shtml", "finance.ifeng.com/app/hq/stock.*(\\d{6})",
      "api.3g.ifeng.com.*k=(\\d{6})", "quote.*(\\d{6}).html", "gw.*stock.*?(\\d{6})",
      "tfile.*(\\d{6})/fs_remind", "api.cailianpress.com.*(\\d{6})&Sing",
      "stock.caijing.com.cn.*(\\d{6}).html", "gu.qq.com.*(\\d{6})\\?", "gubaapi.*code=.*(\\d{6})",
      "mnews.gw.com.cn.*(\\d{6})/(list|gsgg|gsxw|yjbg|trader|ipad2_gg)", "101.226.68.82.*code=.*(\\d{6})",
      "183.238.123.235.*code=(\\d{6})&", "zszx.newone.com.cn.*code=(\\d{6})",
      "compinfo.hsmdb.com.*stock_code=(\\d{6})", "mt.emoney.cn.*barid=(\\d{6})",
      "news.10jqka.com.cn/stock_mlist/(\\d{6})","www.newone.com.cn.*code=(\\d{6})",
      "219.141.183.57.*gpdm=(\\d{6})&","www.hczq.com.*stockCode=(\\d{6})",
      "open.hs.net.*en_prod_code=(\\d{6})","stock.pingan.com.cn.*secucodes=(\\d{6})",
      "58.63.254.170:7710.*code=(\\d{6})&","sjf10.westsecu.com/stock/(\\d{6})/",
      "mds.gf.com.cn.*/(\\d{6})","211.152.53.105.*key=(\\d{6}),"
    )

    val data = input

    if (data.length >= 5) {

      val url = data(3) + data(4)

      var i: Int = 0

      var stockC: String = ""

      var time: String = ""

      for (i <- 1 to visitSearchPatterns.length) {

        val matcher: Matcher = Pattern.compile(visitSearchPatterns(i - 1)).matcher(url)

        if (matcher.find) {

          stockC = matcher.group(1)
          time = data(0) + "\t" + String.valueOf(i)

        }
      }
      if (!"".equals(stockC))
        stockC + "\t" + time
      else
        null

    } else {
      null
    }

  }

  /**
    * 搜索数据的匹配
    *
    * @param url 源数据
    * @return 匹配后的数据
    */
  def visitAndSearch(url: String): String = {

    val visitSearchPatterns = Array[String](
      // 查看
      "finance.sina.com.cn.*sz(\\d{6})/nc.shtml",
      "quote.eastmoney.com/sz(\\d{6}).html",
      "stock.jrj.com.cn.*share\\,(.*).shtml",
      "markets.wallstreetcn.com/stock/sz(.*)",
      "gu.qq.com.*sz(\\d{6})\\?",
      "cy.stcn.com/S/.*(SZ|sz)(\\d{6})",
      "finance.ifeng.com/app/hq/stock.*sz(\\d{6})",
      "stockdata.stock.hexun.com/(\\d{6}).shtml",

      "guba.sina.com.cn.*name=.*(\\d{6})","quotes.money.163.com/app/stock/\\d(\\d{6})","m.news.so.com.*q=(\\d{6})",
      "platform.*symbol=\\w\\w(\\d{6})", "xueqiu.com/S/.*(\\d{6})",
      "mobile.stcn.com.*secucode=(\\d{6})", "stock.quote.stockstar.com/(\\d{6}).shtml",
      "quote.cfi.cn.*searchcode=(.*)", "hqapi.gxfin.com.*code=(\\d{6}).sh",
      "irm.cnstock.com.*index/(\\d{6})", "app.cnstock.com.*k=(\\d{6})",
      "guba.eastmoney.com.*code=.*(\\d{6})", "stockpage.10jqka.com.cn/(\\d{6})/",
      "0033.*list.*(\\d{6}).*json", "q.stock.sohu.com/cn/(\\d{6})/", "s.m.sohu.com.*/(\\d{6})",
      "data.p5w.net.*code.*(\\d{6})", "hq.p5w.net.*a=.*(\\d{6})", "jrj.com.cn.*(\\d{6}).s?html",
      "mapi.jrj.com.cn.*stockcode=(\\d{6})", "api.buzz.wallstreetcn.com.*(\\d{6})&cid",
      "api.3g.ifeng.com.*k=(\\d{6})", "quote.*1(\\d{6}).html", "gw.*stock.*?(\\d{6})",
      "tfile.*(\\d{6})/fs_remind", "api.cailianpress.com.*(\\d{6})&Sing",
      "stock.caijing.com.cn.*(\\d{6}).html", "gubaapi.*code=.*(\\d{6})",
      "mnews.gw.com.cn.*(\\d{6})/(list|gsgg|gsxw|yjbg|trader|ipad2_gg)", "101.226.68.82.*code=.*(\\d{6})",
      "183.238.123.235.*code=(\\d{6})&", "zszx.newone.com.cn.*code=(\\d{6})",
      "compinfo.hsmdb.com.*stock_code=(\\d{6})", "mt.emoney.cn.*barid=(\\d{6})",
      "news.10jqka.com.cn/stock_mlist/(\\d{6})","www.newone.com.cn.*code=(\\d{6})",
      "219.141.183.57.*gpdm=(\\d{6})&","www.hczq.com.*stockCode=(\\d{6})",
      "open.hs.net.*en_prod_code=(\\d{6})","stock.pingan.com.cn.*secucodes=(\\d{6})",
      "58.63.254.170:7710.*code=(\\d{6})&","sjf10.westsecu.com/stock/(\\d{6})/",
      "mds.gf.com.cn.*/(\\d{6})","211.152.53.105.*key=(\\d{6}),"
    )

    var result = ""

    if(url != null ) {

      for (i <- 1 to visitSearchPatterns.length) {

        val break = new Breaks

        break.breakable {
          val matcher: Matcher = Pattern.compile(visitSearchPatterns(i - 1)).matcher(url)

          if (matcher.find) {

            val stock = matcher.group(1)
            result = url + "\t" + stock + "\t" + String.valueOf(i)  + "\t" + visitSearchPatterns(i - 1 )
            break.break()

          }
        }

      }
    }

    result

  }


  def main(args: Array[String]) {


    Source.fromFile("C:\\Users\\Administrator.HTBJUOLFUL4H7CK\\Desktop\\zsgp").getLines().map(visitAndSearch).foreach(println)


  }

}
