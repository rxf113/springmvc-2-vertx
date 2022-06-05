package com.rxf113.vertx_demo;

import com.rxf113.vertx_demo.annotations.ProjectPath;
import com.rxf113.vertx_demo.springmvc.BaseSpringmvc2RouterProcessorImpl;
import com.rxf113.vertx_demo.springmvc.Springmvc2RouterProcessor;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.ext.web.Router;

/**
 * @author rxf113
 */
@ProjectPath(value = "com.rxf113.vertx_demo")
public class MainVerticle extends AbstractVerticle {

  Springmvc2RouterProcessor springmvc2RouterProcessor = new BaseSpringmvc2RouterProcessorImpl();

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    //启动web监听 类似controller
    startWebApp(startPromise);
  }

  private void startWebApp(Promise<Void> startPromise) {
    //扫描controller 获得router
    ProjectPath annotation = this.getClass().getAnnotation(ProjectPath.class);
    String path = annotation.value();
    Router router = springmvc2RouterProcessor.controller2Router(path, vertx);

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
}
