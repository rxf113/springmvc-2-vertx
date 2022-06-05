package com.rxf113.vertx_demo.springmvc;

import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

import static com.rxf113.vertx_demo.util.ConstantUtil.PACKAGE_SEPARATOR;
import static com.rxf113.vertx_demo.util.ConstantUtil.PATH_SEPARATOR;

/**
 * 基础实现
 *
 * @author rxf113
 */
public class BaseSpringmvc2RouterProcessorImpl implements Springmvc2RouterProcessor {

  @Override
  public Router convertClasses2Router(List<Class<?>> classes, Vertx vertx) throws InvocationTargetException, InstantiationException, IllegalAccessException {
    Router router = Router.router(vertx);
    for (Class<?> aClass : classes) {
      //todo 暂时方便测试
      Object o = aClass.getDeclaredConstructors()[0].newInstance();
      String requestMappingVal = aClass.getAnnotation(RequestMapping.class).value()[0];
      Method[] declaredMethods = aClass.getDeclaredMethods();
      for (Method declaredMethod : declaredMethods) {
        String value = declaredMethod.getAnnotation(GetMapping.class).value()[0];
        router.get(requestMappingVal + value).handler(routingContext -> {
          try {
            declaredMethod.invoke(o, routingContext);
          } catch (IllegalAccessException e) {
            e.printStackTrace();
          } catch (InvocationTargetException e) {
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
      if (annotationMetadata.isAnnotated(Controller.class.getName())
        && annotationMetadata.isAnnotated(RequestMapping.class.getName())) {
        String className = annotationMetadata.getClassName();
        Class<?> aClass = ClassLoader.getSystemClassLoader().loadClass(className);
        controllerClasses.add(aClass);
      }
    }
    return controllerClasses;
  }
}
