package com.rxf113.vertx_demo;

import com.rxf113.vertx_demo.conf.MysqlProcessor;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

/**
 * @author rxf113
 */
public class MainVerticle extends AbstractVerticle {

  MysqlProcessor mysqlProcessor = new MysqlProcessor();

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    //初始化 mysql 数据库
    mysqlProcessor.init(vertx);

    //启动web监听 类似controller
    startWebApp(startPromise);
  }

  private void startWebApp(Promise<Void> startPromise) {
    // Create a router object.
    Router router = Router.router(vertx);

    router.get("/test/one/:id").handler(this::getOne);

    // Create the HTTP server and pass the "accept" method to the request handler.
    vertx.createHttpServer()
      .requestHandler(router)
      .listen(8080,
        //默认后置处理
        http -> {
          if (http.succeeded()) {
            startPromise.complete();
          } else {
            startPromise.fail(http.cause());
          }
        });
  }

  private void getOne(RoutingContext routingContext) {
    final String id = routingContext.request().getParam("id");
    mysqlProcessor.pool.getConnection().compose(conn -> {

      // All operations execute on the same connection
      return conn
        .query("SELECT * FROM users WHERE id='julien'")
        .execute()
        .onComplete(ar -> {
          // Release the connection to the pool
          conn.close();
        });
    }).onComplete(ar -> {
      if (ar.succeeded()) {
        System.out.println("Done");
      } else {
        System.out.println("Something went wrong " + ar.cause().getMessage());
      }
    });
  }
}
