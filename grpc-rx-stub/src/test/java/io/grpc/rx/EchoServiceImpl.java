package io.grpc.rx;

import io.grpc.rx.EchoGrpcRx.EchoImplBase;
import io.grpc.rx.core.BidiStreamingProcessor;
import io.grpc.rx.core.ClientStreamingProcessor;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;
import org.reactivestreams.Subscriber;

import java.util.concurrent.Callable;

import static io.grpc.rx.EchoService.*;

public class EchoServiceImpl extends EchoImplBase {
  @Override
  public Single<EchoResp> unary(EchoReq request) {
    return Single.fromCallable(() -> {
      EchoResp resp = EchoResp.newBuilder()
          .setId(request.getId())
          .setValue(request.getValue())
          .build();
      return resp;
    });
  }

  @Override
  public Flowable<EchoResp> serverStreaming(EchoCountReq request) {
    return Flowable.range(0, request.getCount())
        .map(i -> EchoResp.newBuilder()
            .setId(i)
            .setValue(i.toString())
            .build());
  }

  @Override
  public Single<EchoCountResp> clientStreaming(Flowable<EchoReq> requests) {
    // Flowable.count() has BackpressureKind.UNBOUNDED_IN flow control
    return requests.count()
        .map(c -> EchoCountResp.newBuilder()
            .setCount(c.intValue())
            .build());
  }

  @Override
  public Flowable<EchoResp> bidiStreaming(Flowable<EchoReq> requests) {
    return requests.map(r -> EchoResp.newBuilder()
        .setId(r.getId())
        .setValue(r.getValue())
        .build());
  }
}
