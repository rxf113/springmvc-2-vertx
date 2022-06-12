package com.rxf113.springmvc2vertx.springmvc.processor.parameter;

import com.rxf113.springmvc2vertx.exception.MissingRequestParameterException;
import io.vertx.ext.web.RoutingContext;
import org.springframework.web.bind.annotation.PathVariable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;

public class PathVariableProcessor implements ParameterAnnotationProcessor {

  @Override
  public Object getRealParameterVal(RoutingContext routingContext, Annotation annotation, Parameter parameter) {

    PathVariable pathVariable = (PathVariable) annotation;
    String paramName = getParamName(pathVariable, parameter);
    String param = routingContext.request().getParam(paramName);
    if (param == null && pathVariable.required()){
      throw new MissingRequestParameterException(paramName, parameter.getType().getSimpleName());
    }
    //如果为空，取方法参数名
    return converter.convertIfNecessary(param, parameter.getType());

  }

  private String getParamName(PathVariable pathVariable, Parameter parameter) {
    String paramName = pathVariable.value().equals("") ? pathVariable.name() : pathVariable.value();
    if (paramName.equals("")) {
      //取参数名
      paramName = parameter.getName();
    }
    return paramName;
  }
}
