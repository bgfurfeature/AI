import com.bgfurfeature.hbase.HBase;
import com.bgfurfeature.hbase.HBaseUtil;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Table;
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

/*
 * Created by koth on 2017/4/10.
 * http下 载服务（下载hbase中的文件）
 */

public class HBaseResumeServing implements HBase {

  private static Logger logger = LoggerFactory.getLogger(HBaseResumeServing.class);

  public static void main(String[] argv) throws IOException {
    Vertx vtx = Vertx.vertx();
    LocalMap<String, JsonObject> configJson =
        vtx.sharedData().<String, JsonObject>getLocalMap("__GLOBAL_MAP__");
    HttpServer server = vtx.createHttpServer();
    int port = 20199;
    if (argv.length > 0) {
      port = Integer.valueOf(argv[0]);
    }
    HBaseUtil hbase = new HBaseUtil(true);
    final Table htable = hbase.getHTable("resume_file");
    Router router = Router.router(vtx);
    router.route().handler(BodyHandler.create());
    // http://server:port/raw/key-example(hbase-rowkey)
    router.get("/raw/:key").handler(routingContext -> {
      try {

        String keyStr = routingContext.request().getParam("key");
        logger.info("try get resume:" + keyStr);
        Get get = new Get(hbase.getBytes(keyStr));
        Result result = htable.get(get);
        if (result == null || result.isEmpty()) {
          routingContext.response().setStatusCode(404);
          routingContext.response().end("Not Found");
          return;
        }
        byte[] nameBytes = result.getValue(Bytes.toBytes("meta"), Bytes.toBytes("filename"));
        byte[] contentBytes = result.getValue(Bytes.toBytes("data"), Bytes.toBytes("rawcontent"));
        String fileName = new String(nameBytes, Charset.forName("utf8"));
        logger.info("fileName:" + fileName + ",fileName= " + Bytes.toString(nameBytes));
        routingContext.response().putHeader("Content-Disposition", "attachment; filename=" +
            URLEncoder.encode(fileName, "utf-8") + ";");

        routingContext.response().putHeader("Content-Type",
            "application/octet-stream; charset=utf-8");
        routingContext.response().end(Buffer.buffer(contentBytes));
      } catch (Exception ex) {
        ex.printStackTrace();
        routingContext.fail(500);
      }
    });
    server.requestHandler(router::accept);
    server.listen(port);
    logger.info("serving at port:" + port);
  }

}
