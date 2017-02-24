package io.grpc.rx;

import io.grpc.BindableService;
import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.NettyServerBuilder;

public interface IntegrationTestHelper {
  static ManagedChannel newChannel() {
    return NettyChannelBuilder
        .forAddress("localhost", 1234)
        .usePlaintext(true)
        .build();
  }

  static Server newServer(BindableService service) {
    return NettyServerBuilder
        .forPort(1234)
        .addService(service)
        .build();
  }
}
