package com.bgfurfeature.vertx;

import com.inmind.idlg.common.ConfigUtils;
import com.inmind.idlg.hadoop.utils.HTableUtil;

import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.Charset;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

/**
 * Created by koth on 2017/4/10.
 */
public class HBaseResumeServing {
  private static Logger logger = LoggerFactory.getLogger(HBaseResumeServing.class);

  private static HTable htable = null;

  private static final Vertx VERTX = Vertx.vertx();

  private static final String TABLE_NAME = "resume_file";

  private static Map<String, String> rowKeyMap = new HashMap<>();
  private static Map<String, Integer> sourceMap = new HashMap<>();

  private void init() {
    try {
      htable = HTableUtil.getHTable(TABLE_NAME);
      // set source ref to HBase row key prefix
      rowKeyMap.put("yahui", "000");
      rowKeyMap.put("ifc", "102");
      sourceMap.put("yahui", 101);
      sourceMap.put("ifc", 102);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  private JsonObject formatResponse(int status, String msg, JsonObject data) {
    JsonObject resp = new JsonObject();
    resp.put("status", status);
    resp.put("msg", msg);
    resp.put("data", data);
    return resp;
  }

  private void getRawResume(Router router) {
    router.get("/raw/:key").handler(routingContext -> {
      try {
        String keyStr = routingContext.request().getParam("key");
        logger.info("try get resume:" + keyStr);
        Get get = new Get(HTableUtil.gB(keyStr));
        Result result = htable.get(get);
        if (result == null || result.isEmpty()) {
          routingContext.response().setStatusCode(404);
          routingContext.response().end("Not Found");
          return;
        }
        byte[] nameBytes = result.getValue(HTableUtil.gB("meta"), HTableUtil.gB("filename"));
        byte[] contentBytes = result.getValue(HTableUtil.gB("data"), HTableUtil.gB("rawcontent"));
        String fileName = new String(nameBytes, Charset.forName("utf8"));
        logger.info("fileName:" + fileName + ",fileName=" + Bytes.toString(nameBytes));
        routingContext.response().putHeader("Content-Disposition", "attachment; filename=" +
            URLEncoder.encode(fileName, "utf-8") + ";");
        routingContext.response().putHeader("X-Filename", fileName);
        routingContext.response().putHeader("Content-Type",
            "application/octet-stream; charset=utf-8");
        routingContext.response().end(Buffer.buffer(contentBytes));
      } catch (Exception ex) {
        ex.printStackTrace();
        routingContext.fail(500);
      }
    });
  }

  private void getTextResume(Router router) {
    router.get("/text/:key").handler(routingContext -> {
      try {
        String keyStr = routingContext.request().getParam("key");
        logger.info("try get text resume:" + keyStr);
        Get get = new Get(HTableUtil.gB(keyStr));
        Result result = htable.get(get);
        if (result == null || result.isEmpty()) {
          routingContext.response().setStatusCode(404);
          routingContext.response().end("Not Found");
          return;
        }
        byte[] nameBytes = result.getValue(HTableUtil.gB("meta"), HTableUtil.gB("filename"));
        byte[] contentBytes =
            result.getValue(HTableUtil.gB("data"), HTableUtil.gB("originResumeContent"));
        String fileName = new String(nameBytes, Charset.forName("utf8"));
        logger.info("fileName:" + fileName + ",fileName=" + Bytes.toString(nameBytes));
        routingContext.response().putHeader("X-Filename", fileName);
        routingContext.response().putHeader("Content-Type",
            "application/octet-stream; charset=utf-8");
        routingContext.response().end(Buffer.buffer(contentBytes));
      } catch (Exception ex) {
        ex.printStackTrace();
        routingContext.fail(500);
      }
    });
  }

  private void getJSONResume(Router router) {
    router.get("/json/:key").handler(routingContext -> {
      try {
        HttpServerRequest request = routingContext.request();
        HttpServerResponse response = routingContext.response();
        response.putHeader("Content-Type", "application/json; charset=utf-8");
        String key = request.getParam("key");
        logger.info(String.format("try get json resume:%s", key));
        Get get = new Get(HTableUtil.gB(key));
        Result result = htable.get(get);
        if (result == null || result.isEmpty()) {
          response.setStatusCode(404).end(this.formatResponse(404, "Not found", null).toString());
          return;
        }
        byte[] jsonBytes = result.getValue(HTableUtil.gB("data"), HTableUtil.gB("json"));
        JsonObject data = new JsonObject(new String(jsonBytes, Charset.forName("utf8")));
        response.setStatusCode(200).end(this.formatResponse(200, "success", data).toString());
      } catch (Exception ex) {
        ex.printStackTrace();
        routingContext.fail(500);
      }
    });
  }

  /**
   * receive post resume json save to HBase
   * TODO: add json structure valid
   */
  private void postResume(Router router) {
    router.post("/resume").handler(routingContext -> {
      try {
        HttpServerResponse response = routingContext.response();
        response.putHeader("Content-Type", "application/json; charset=utf-8");
        JsonObject body = routingContext.getBodyAsJson();

        Integer id = body.getInteger("id");
        String chineseName = body.getString("chineseName");
        String mobile = body.getString("mobile");
        String source = body.getString("source");
        String birthday = body.getString("birthday");
        JsonArray education = body.getJsonArray("educationExperiences");
        JsonArray work = body.getJsonArray("workExperiences");

        String prefix = rowKeyMap.get(source);
        // check post data
        boolean breakFlag = false;
        String breakMsg = "";
        if (null == prefix) {
          breakFlag = true;
          breakMsg = "source not match";
        } else if (null == id || id.toString().length() > 29) {
          breakFlag = true;
          breakMsg = "id can not be empty / reach max length";
        } else if (StringUtils.isBlank(chineseName)) {
          breakFlag = true;
          breakMsg = "chineseName can not be empty";
        } else if (StringUtils.isBlank(mobile)) {
          breakFlag = true;
          breakMsg = "该用户未填写联系方式";
        } else if (StringUtils.isBlank(birthday)) {
//          breakFlag = true;
//          breakMsg = "birthday can not be empty";
        } else if (null == education || education.size() == 0) {
//          breakFlag = true;
//          breakMsg = "education can not be empty";
        } else if (null == work || work.size() == 0) {
//          breakFlag = true;
//          breakMsg = "work can not be empty";
        }
        if (breakFlag) {
          response.setStatusCode(400).end(this.formatResponse(400, breakMsg, null).toString());
          return;
        }
        // save to HBase
        String family = "data";
        // format row key
        String key = String.format("%s%029d", prefix, id);
        Map<String, Map<String, byte[]>> resMap = new HashMap<>();
        Map<String, byte[]> fieldMap = new HashMap<>();
        body.put("source", sourceMap.get(source));
        fieldMap.put("json", HTableUtil.gB(body.toString()));
        resMap.put(key, fieldMap);
        if (HTableUtil.addBatch(TABLE_NAME, family, resMap, false)) {
          JsonObject data = new JsonObject();
          data.put("id", key);
          response.setStatusCode(201).end(this.formatResponse(201, "success", data).toString());
        } else {
          response.setStatusCode(500).end(this.formatResponse(500, "failure", null).toString());
        }
        logger.info(String.format("save to HBase key:%s", key));
      } catch (Exception ex) {
        ex.printStackTrace();
        routingContext.fail(500);
      }
    });
  }

  private void index(Router router) {
    router.get("/").handler(routingContext -> {
      try {
        HttpServerResponse response = routingContext.response();
        response.end("Hi, this is data server");
      } catch (Exception ex) {
        ex.printStackTrace();
        routingContext.fail(500);
      }
    });
  }

  public static void main(String[] argv) throws IOException {
    HBaseResumeServing serving = new HBaseResumeServing();
    // init context
    serving.init();
    LocalMap<String, JsonObject> configJson =
        VERTX.sharedData().<String, JsonObject>getLocalMap(ConfigUtils.GLOBAL_MAP_KEY);
    HttpServer server = VERTX.createHttpServer();
    int port = 20199;
    if (argv.length > 0) {
      port = Integer.valueOf(argv[0]);
    }
    Router router = Router.router(VERTX);
    router.route().handler(BodyHandler.create());

    // index
    serving.index(router);
    // GET raw resume
    // URI:/raw/:key
    serving.getRawResume(router);
    // GET text resume
    // URI:/text/:key
    serving.getTextResume(router);
    // GET json resume
    // URI:/json/:key
    serving.getJSONResume(router);
    // POST json resume to HBase
    // URI:/resume
    serving.postResume(router);

    // start server
    server.requestHandler(router::accept);
    server.listen(port);
    logger.info("serving at port:" + port);
  }

}
