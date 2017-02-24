package io.grpc.rx.core;

public class LogUtils {
  private LogUtils() {}

  static public String objectString(Object obj) {
    return obj.getClass().getName() + "@" + Integer.toHexString(obj.hashCode());
  }
}
