package com.rxf113.vertx_demo;

import com.rxf113.vertx_demo.annotations.ProjectPath;
import com.rxf113.vertx_demo.conf.MysqlProcessor;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.sqlclient.Row;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;

/**
 * @author rxf113
 */
@ProjectPath(basePackage = "com.rxf113.vertx_demo")
public class MainVerticle extends AbstractVerticle {

  private static final char PACKAGE_SEPARATOR = '.';

  private static final char PATH_SEPARATOR = '/';

  MysqlProcessor mysqlProcessor = new MysqlProcessor();

  @Override
  public void start(Promise<Void> startPromise) throws Exception {

    //初始化 mysql 数据库
    //mysqlProcessor.init(vertx);

    //启动web监听 类似controller
    startWebApp(startPromise);
  }

  private void startWebApp(Promise<Void> startPromise) {
    // Create a router object.
    Router router = Router.router(vertx);
    //todo 扫描controller 获得router

    scanControllerAndInitRouter();
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

  private void scanControllerAndInitRouter() {
    ProjectPath annotation = this.getClass().getAnnotation(ProjectPath.class);
    String basePackage = annotation.basePackage();
    basePackage = basePackage.replace(PACKAGE_SEPARATOR,PATH_SEPARATOR);
    Enumeration<URL> resourceUrls = null;
    try {
      resourceUrls = ClassLoader.getSystemResources(basePackage);

    } catch (IOException e) {
      e.printStackTrace();
    }
    List<String> filePaths = new ArrayList<>();
    Objects.requireNonNull(resourceUrls, "非空校验失败");
    while (resourceUrls.hasMoreElements()) {
      URL url = resourceUrls.nextElement();
      filePaths.add(url.getPath());
    }

    List<String> controllerClss = new ArrayList<>();
    for (String filePath : filePaths) {
      getControllers(new File(filePath), controllerClss);
    }

    //todo 加载 controllerClss 获取注解 添加到 router

  }

  private static void getControllers(File file, List<String> list) {
    File[] files = file.listFiles();
    assert files != null;
    for (File file1 : files) {
      if (file1.isFile()) {
        //判断是不是Controller结尾
        if (file1.getPath().endsWith("Controller.class")) {
          list.add(file1.getPath());
        }
      } else {
         getControllers(file1, list);
      }
    }
  }

  private void getOne(RoutingContext routingContext) {
    final String id = routingContext.request().getParam("id");
    mysqlProcessor.pool.getConnection().compose(conn -> {

      // All operations execute on the same connection
      return conn
        .query("SELECT * FROM user WHERE id='50'")
        .execute()
        .onComplete(ar -> {
          // Release the connection to the pool
          conn.close();
        });
    }).onComplete(ar -> {
      if (ar.succeeded()) {
        for (Row row : ar.result()) {
          System.out.println(row);
        }
        System.out.println("Done");
      } else {
        System.out.println("Something went wrong " + ar.cause().getMessage());
      }
    });
  }
}
