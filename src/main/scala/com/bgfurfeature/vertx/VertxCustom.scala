package com.bgfurfeature.vertx

import io.vertx.core.Vertx
import io.vertx.core.http.HttpServerRequest
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler

/**
  * Created by Jerry on 2017/4/14.
  */
object VertxCustom {

  def main(args: Array[String]): Unit = {

    var vertx = Vertx.vertx()
    val server = vertx.createHttpServer()
    val router = Router.router(vertx)
    router.route.handler(BodyHandler.create)
    router.get("/war").handler(BodyHandler.create())

  }

}
