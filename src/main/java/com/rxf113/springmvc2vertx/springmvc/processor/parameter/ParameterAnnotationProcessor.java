package com.rxf113.springmvc2vertx.springmvc.processor.parameter;

import io.vertx.ext.web.RoutingContext;
import org.springframework.beans.SimpleTypeConverter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;

/**
 * 参数绑定注解的处理类
 */
public interface ParameterAnnotationProcessor {

  /**
   * 转换不同数据类型，copy from spring
   */
  SimpleTypeConverter converter = new SimpleTypeConverter();

  Object getRealParameterVal(RoutingContext routingContext, Annotation annotation, Parameter parameter);
}
