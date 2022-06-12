package com.rxf113.springmvc2vertx.springmvc.processor.mvc2router;

import com.rxf113.springmvc2vertx.exception.Springmvc2RouterException;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import org.springframework.core.io.Resource;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Set;

/**
 * springmvc方式定义的Controller转换为vertx router
 *
 * @author rxf113
 */
public interface Springmvc2RouterProcessor {

  CachingMetadataReaderFactory METADATA_READER_FACTORY = new CachingMetadataReaderFactory();

  /**
   * Controller转换为vertx router
   *
   * @param path 项目基础包路径
   * @return Router
   */
  default Router controller2Router(String path, Vertx vertx) {
    try {
      path = getProjectPath(path);
      Set<Resource> urlResources = getUrlResourcesByPath(path);
      List<Resource> fileResources = getFileResourcesByUrlResources(urlResources);
      List<Class<?>> classes = filterMatchControllers(fileResources);
      return convertClasses2Router(classes, vertx);
    } catch (Exception e) {
      throw new Springmvc2RouterException("springmvc2Router exception", e);
    }

  }

  /**
   * 转换controller classes为 router
   *
   * @param classes controllers
   * @param vertx   vertx
   * @return Router
   */
  Router convertClasses2Router(List<Class<?>> classes, Vertx vertx) throws InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchFieldException;

  /**
   * 1 获取项目基础包路径拓展
   *
   * @param path 项目基础包路径
   * @return String
   */
  String getProjectPath(String path);

  /**
   * 2 根据基础包获取UrlResources
   *
   * @param path 项目基础包路径
   * @return Set<Resource>
   */
  Set<Resource> getUrlResourcesByPath(String path) throws IOException;

  /**
   * 3 根据UrlResources获取路径下所有类、注解、接口等Resources
   *
   * @param urlResources 路径Resources
   * @return List<Resource>
   */
  List<Resource> getFileResourcesByUrlResources(Set<Resource> urlResources) throws IOException;

  /**
   * 4 过滤出符合规则的Controllers
   *
   * @param fileResources 类 接口等文件Resources
   * @return List<Class < ?>>
   */
  List<Class<?>> filterMatchControllers(List<Resource> fileResources) throws ClassNotFoundException, IOException;

  //5 将Controller转为routers

}
