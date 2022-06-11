package com.rxf113.vertx_demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * controller
 *
 * @author rxf113
 */
@RestController
@RequestMapping(value = "/demo/")
public class DemoController {

//  @GetMapping("test")
//  public void test(RoutingContext routingContext) {
//    //todo 后续简化
//    routingContext.response()
//      .putHeader("content-type", "application/json; charset=utf-8")
//      .end(Json.encodePrettily(new HashMap<String, Object>() {{
//        put("key", 123);
//      }}));
//  }

  @GetMapping("test")
  public Object test(@RequestParam(value = "val") String val) {
    System.out.println(val);
    return "success!" + val;
  }


}
