package com.rxf113.springmvc2vertx.springmvc.processor.mvc2router;

import com.rxf113.springmvc2vertx.exception.Springmvc2RouterException;
import com.rxf113.springmvc2vertx.springmvc.processor.parameter.ParameterAnnotationProcessor;
import com.rxf113.springmvc2vertx.springmvc.processor.parameter.PathVariableProcessor;
import com.rxf113.springmvc2vertx.springmvc.processor.parameter.RequestParamProcessor;
import com.rxf113.springmvc2vertx.util.StringUtil;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URL;
import java.util.*;

import static com.rxf113.springmvc2vertx.util.ConstantUtil.PACKAGE_SEPARATOR;
import static com.rxf113.springmvc2vertx.util.ConstantUtil.PATH_SEPARATOR;

/**
 * 基础实现
 *
 * @author rxf113
 */
public class BaseSpringmvc2RouterProcessorImpl implements Springmvc2RouterProcessor {

  @Override
  public Router convertClasses2Router(List<Class<?>> classes, Vertx vertx) throws InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchFieldException {
    Router router = Router.router(vertx);
    for (Class<?> aClass : classes) {
      Object obj;
      try {
        obj = aClass.getDeclaredConstructors()[0].newInstance();
      } catch (IllegalArgumentException e) {
        throw new Springmvc2RouterException("class: " + aClass.getName() + " empty parameter constructor is required", e);
      }
      //默认RequestMapping 只有一个值
      String requestMappingVal = aClass.getAnnotation(RequestMapping.class).value()[0];

      Method[] declaredMethods = aClass.getDeclaredMethods();
      for (Method method : declaredMethods) {
        Optional<Annotation> webBindAnnotation = getWebBindAnnotation(method);
        if (!webBindAnnotation.isPresent()) {
          continue;
        }

        Annotation annotation = webBindAnnotation.get();

        //获取mapping注解上的 value (路径)
        //暂时只考虑单个参数
        String methodMappingVal;
        if (annotation.annotationType().equals(GetMapping.class)) {
          methodMappingVal = method.getAnnotation(GetMapping.class).value()[0];
        } else if (annotation.annotationType().equals(PostMapping.class)) {
          methodMappingVal = method.getAnnotation(PostMapping.class).value()[0];
        } else if (annotation.annotationType().equals(PutMapping.class)) {
          methodMappingVal = method.getAnnotation(PutMapping.class).value()[0];
        } else if (annotation.annotationType().equals(DeleteMapping.class)) {
          methodMappingVal = method.getAnnotation(DeleteMapping.class).value()[0];
        } else {
          continue;
        }

        //斜杠优化
        String methodUri = StringUtil.uriSlashOpt(requestMappingVal, methodMappingVal);
        //占位符替换为vertx那种 :xxx
        methodUri = methodUri.replaceAll("\\{(.*?)}", ":$1");

        //具体的请求方式 暂时只考虑单个
        Route route;
        if (annotation.annotationType().equals(GetMapping.class)) {
          route = router.get(methodUri);
        } else if (annotation.annotationType().equals(PostMapping.class)) {
          route = router.post(methodUri);
        } else if (annotation.annotationType().equals(PutMapping.class)) {
          route = router.put(methodUri);
        } else if (annotation.annotationType().equals(DeleteMapping.class)) {
          route = router.delete(methodUri);
        } else {
          continue;
        }

        //具体的参数绑定，获取到实际参数 绑定到controller的方法上
        route.handler(routingContext -> {

          int parameterCount = method.getParameterCount();

          //存储 method 方法的实际的参数
          Object[] realParams = new Object[parameterCount];

          Parameter[] parameters = method.getParameters();

          Annotation[][] parameterAnnotations = method.getParameterAnnotations();

          for (int i = 0; i < parameterCount; i++) {
            Annotation[] annotations = parameterAnnotations[i];

            Annotation parameterBindAnnotation = null;
            if (annotations.length != 0) {
              //判断注解是否包括 RequestParam PathVariable RequestBody ,  默认是RequestParam的方式
              parameterBindAnnotation = getParameterBindAnnotations(annotations);
            }
            //获取ParameterBindAnnotationProcessor 如 RequestParamProcessor(RequestParam注解对应的处理类)
            ParameterAnnotationProcessor parameterAnnotationProcessor = getParameterBindAnnotationProcessor(parameterBindAnnotation);
            //获取实际的参数
            Object res = parameterAnnotationProcessor.getRealParameterVal(routingContext, parameterBindAnnotation, parameters[i]);
            realParams[i] = res;
          }

          //调用controller method 拿到返回值返回
          try {
            Object colRes = method.invoke(obj, realParams);
            routingContext.response()
              .setStatusCode(200)
              .putHeader("content-type", "application/json; charset=utf-8")
              .end(Json.encodePrettily(colRes));
          } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
          }
        });
      }
    }
    return router;
  }


  @Override
  public String getProjectPath(String path) {
    return path;
  }

  @Override
  public Set<Resource> getUrlResourcesByPath(String path) throws IOException {
    Set<Resource> urlResources = new HashSet<>(16);
    path = path.replace(PACKAGE_SEPARATOR, PATH_SEPARATOR);
    Enumeration<URL> resourceUrls = ClassLoader.getSystemResources(path);
    Objects.requireNonNull(resourceUrls, "非空校验失败");
    while (resourceUrls.hasMoreElements()) {
      URL url = resourceUrls.nextElement();
      urlResources.add(new UrlResource(url));
    }

    return urlResources;
  }

  @Override
  public List<Resource> getFileResourcesByUrlResources(Set<Resource> urlResources) throws IOException {
    List<Resource> fileResources = new ArrayList<>();
    for (Resource resource : urlResources) {
      File file = resource.getFile();
      getAllFileUnderFile(file, fileResources);
    }
    return fileResources;
  }

  private static void getAllFileUnderFile(File file, List<Resource> resourceList) {
    File[] files = file.listFiles();
    assert files != null;
    for (File cuFile : files) {
      if (cuFile.isFile()) {
        resourceList.add(new FileSystemResource(cuFile));
      } else {
        getAllFileUnderFile(cuFile, resourceList);
      }
    }
  }


  @Override
  public List<Class<?>> filterMatchControllers(List<Resource> fileResources) throws ClassNotFoundException, IOException {
    List<Class<?>> controllerClasses = new ArrayList<>(fileResources.size());
    for (Resource resource : fileResources) {
      MetadataReader metadataReader;
      metadataReader = METADATA_READER_FACTORY.getMetadataReader(resource);
      AnnotationMetadata annotationMetadata = metadataReader.getAnnotationMetadata();
      if (annotationMetadata.isAnnotated(Controller.class.getName()) && annotationMetadata.isAnnotated(RequestMapping.class.getName())) {
        String className = annotationMetadata.getClassName();
        Class<?> aClass = ClassLoader.getSystemClassLoader().loadClass(className);
        controllerClasses.add(aClass);
      }
    }
    return controllerClasses;
  }

  /**
   * 获取 RequestMapping GetMapping PostMapping PutMapping DeleteMapping
   *
   * @param method method
   * @return Annotation
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  private Optional<Annotation> getWebBindAnnotation(Method method) {
    //todo RequestMapping.class
    Class[] mappingAnnotations = new Class[]{GetMapping.class, PostMapping.class, PutMapping.class, DeleteMapping.class};
    Annotation annotation;
    for (Class aClass : mappingAnnotations) {
      annotation = method.getAnnotation(aClass);
      if (annotation != null) {
        return Optional.of(annotation);
      }
    }
    return Optional.empty();
  }

  Map<Class<? extends Annotation>, ParameterAnnotationProcessor> annotation2ProcessorMap = new HashMap<>(4, 1);

  {
    RequestParamProcessor requestParamProcessor = new RequestParamProcessor();
    PathVariableProcessor pathVariableProcessor = new PathVariableProcessor();
    // RequestParam注解
    annotation2ProcessorMap.put(null, requestParamProcessor);
    annotation2ProcessorMap.put(RequestParam.class, requestParamProcessor);

    // PathVariable注解
    annotation2ProcessorMap.put(PathVariable.class, pathVariableProcessor);
  }

  private ParameterAnnotationProcessor getParameterBindAnnotationProcessor(Annotation parameterBindAnnotation) {
    Class<? extends Annotation> aClass = parameterBindAnnotation == null ? null : parameterBindAnnotation.annotationType();
    return annotation2ProcessorMap.get(aClass);
  }

  @SuppressWarnings("rawtypes")
  Class[] mappingAnnotations = new Class[]{RequestParam.class, PathVariable.class};

  @SuppressWarnings("rawtypes")
  private Annotation getParameterBindAnnotations(Annotation[] annotations) {
    for (Annotation annotation : annotations) {
      for (Class aClass : mappingAnnotations) {
        if (annotation.annotationType().equals(aClass)) {
          return annotation;
        }
      }
    }
    return null;
  }

}
