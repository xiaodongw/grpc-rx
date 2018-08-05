package io.grpc.rx.stub;

import io.grpc.CallOptions;

public interface GrpcRxOptions {
  CallOptions.Key<Integer> LOW_WATERMARK = CallOptions.Key.createWithDefault("GRPC_RX_LOW_WATERMARK", 4);
  CallOptions.Key<Integer> HIGH_WATERMARK = CallOptions.Key.createWithDefault("GRPC_RX_HIGH_WATERMARK", 32);
}
