package com.rxf113.vertx_demo.annotations;

import java.lang.annotation.*;

/**
 * 指定项目根路径
 *
 * @author rxf113
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface ProjectPath {

  String basePackage() default "";

}
