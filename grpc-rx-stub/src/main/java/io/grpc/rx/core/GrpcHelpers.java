package io.grpc.rx.core;

public class GrpcHelpers {
  private GrpcHelpers() {}

  static public int fixRequestNum(long n) {
    if(n >= Integer.MAX_VALUE) {
      return Integer.MAX_VALUE;
    } else {
      return (int)n;
    }
  }
}
