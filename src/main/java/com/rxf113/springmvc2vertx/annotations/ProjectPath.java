package com.rxf113.springmvc2vertx.annotations;

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

  String value() default "";

}
