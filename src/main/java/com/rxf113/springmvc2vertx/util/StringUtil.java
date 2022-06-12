package com.rxf113.springmvc2vertx.util;

/**
 * 自定义工具类
 */
public class StringUtil {

  private StringUtil() {
  }

  /**
   * 斜杠优化
   *
   * @param pre
   * @param next
   * @return
   */
  public static String uriSlashOpt(String pre, String next) {
    pre = pre.replace("/", "");
    next = next.replace("/", "");
    return "/" + pre + "/" + next;
  }
}
