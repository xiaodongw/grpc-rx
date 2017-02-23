package io.grpc.rx;

import io.grpc.rx.EchoGrpcRx.EchoImplBase;
import io.reactivex.Flowable;
import io.reactivex.Single;

import static io.grpc.rx.EchoService.*;

public class EchoProxyImpl extends EchoImplBase {
  private EchoGrpcRx.EchoStub stub;

  public EchoProxyImpl(EchoGrpcRx.EchoStub stub) {
    this.stub = stub;
  }

  @Override
  public Single<EchoResp> unary(EchoReq request) {
    return stub.unary(request);
  }

  @Override
  public Flowable<EchoResp> serverStreaming(EchoCountReq request) {
    return stub.serverStreaming(request);
  }

  @Override
  public Single<EchoCountResp> clientStreaming(Flowable<EchoReq> requests) {
    return stub.clientStreaming(requests);
  }

  @Override
  public Flowable<EchoResp> bidiStreaming(Flowable<EchoReq> requests) {
    return stub.bidiStreaming(requests);
  }
}
