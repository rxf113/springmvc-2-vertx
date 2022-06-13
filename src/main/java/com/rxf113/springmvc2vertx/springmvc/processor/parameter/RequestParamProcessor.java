package com.rxf113.springmvc2vertx.springmvc.processor.parameter;

import com.rxf113.springmvc2vertx.exception.MissingRequestParameterException;
import io.vertx.ext.web.RoutingContext;
import org.springframework.web.bind.annotation.RequestParam;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;

/**
 * RequestParam 注解的参数获取
 */
public class RequestParamProcessor implements ParameterAnnotationProcessor {


  @Override
  public Object getRealParameterVal(RoutingContext routingContext, Annotation annotation, Parameter parameter) {
    RequestParam requestParam = (RequestParam) annotation;
    String paramName = getParamName(requestParam, parameter);
    String param = routingContext.request().getParam(paramName);
    if (param == null) {
      if (requestParam.required()) {
        throw new MissingRequestParameterException(paramName, parameter.getType().getSimpleName());
      }
      param = requestParam.defaultValue();
    }
    return converter.convertIfNecessary(param, parameter.getType());
  }

  private String getParamName(RequestParam requestParam, Parameter parameter) {
    String paramName = requestParam.value().equals("") ? requestParam.name() : requestParam.value();
    if (paramName.equals("")) {
      //取参数名
      paramName = parameter.getName();
    }
    return paramName;
  }
}
