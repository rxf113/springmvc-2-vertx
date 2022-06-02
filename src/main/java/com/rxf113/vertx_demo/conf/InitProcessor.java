package com.rxf113.vertx_demo.conf;

import io.vertx.core.Vertx;

/**
 * 初始化配置等
 *
 * @author rxf113
 */
public interface InitProcessor {
  boolean init(Vertx vertx);
}

