package com.rxf113.springmvc2vertx.springmvc.processor.parameter;

import io.vertx.ext.web.RoutingContext;
import org.springframework.web.bind.annotation.RequestParam;

import java.lang.annotation.Annotation;

/**
 * RequestParam 注解的处理类
 */
public class RequestParamProcessor implements ParameterAnnotationProcessor {


  @Override
  public Object getParameter(RoutingContext routingContext, Annotation annotation, Class<?> parameterTypeCla) {

    RequestParam requestParam = (RequestParam) annotation;
    String paramKey = requestParam.value().equals("") ? requestParam.name() : requestParam.value();
    String defaultValue = requestParam.defaultValue();
    String param = routingContext.request().getParam(paramKey, defaultValue);
    return converter.convertIfNecessary(param, parameterTypeCla);

  }
}
