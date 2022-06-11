package com.rxf113.vertx_demo.springmvc.processor.parameter;

import io.vertx.ext.web.RoutingContext;
import org.springframework.beans.SimpleTypeConverter;

import java.lang.annotation.Annotation;

/**
 * 参数绑定注解的处理类
 */
public interface ParameterAnnotationProcessor {

  /**
   * 转换不同数据类型，copy from spring
   */
  SimpleTypeConverter converter = new SimpleTypeConverter();

  Object getParameter(RoutingContext routingContext, Annotation annotation, Class<?> parameterTypeCla);
}
