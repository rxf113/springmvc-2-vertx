package com.rxf113.springmvc2vertx;

import com.rxf113.springmvc2vertx.annotations.ProjectPath;
import com.rxf113.springmvc2vertx.springmvc.processor.mvc2router.BaseSpringmvc2RouterProcessorImpl;
import com.rxf113.springmvc2vertx.springmvc.processor.mvc2router.Springmvc2RouterProcessor;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.ext.web.Router;

/**
 * @author rxf113
 */
@ProjectPath(value = "com.rxf113.springmvc2vertx")
public class MainVerticle extends AbstractVerticle {

  Springmvc2RouterProcessor springmvc2RouterProcessor = new BaseSpringmvc2RouterProcessorImpl();

  @Override
  public void start(Promise<Void> startPromise) {
    //启动web监听 类似controller
    startWebApp(startPromise);
  }

  private void startWebApp(Promise<Void> startPromise) {
    //扫描controller 获得router
    ProjectPath annotation = this.getClass().getAnnotation(ProjectPath.class);
    String path = annotation.value();

    //将springmvc方式的controller 转为vertx router
    Router router = springmvc2RouterProcessor.controller2Router(path, vertx);

    //创建启动服务器
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
