package io.grpc.rx;

import io.grpc.rx.EchoGrpcRx.EchoImplBase;
import io.reactivex.SingleObserver;
import org.reactivestreams.Subscriber;

import static io.grpc.rx.EchoService.*;

public class EchoProxyImpl extends EchoImplBase {
    private EchoGrpcRx.EchoStub stub;

    public EchoProxyImpl(EchoGrpcRx.EchoStub stub) {
        this.stub = stub;
    }

    @Override
    public void unary(EchoReq request, SingleObserver<EchoResp> responseObserver) {
        stub.unary(request, responseObserver);
    }

    @Override
    public Subscriber<EchoReq> clientStreaming(SingleObserver<EchoCountResp> responseObserver) {
        return stub.clientStreaming(responseObserver);
    }

    @Override
    public void serverStreaming(EchoCountReq request, Subscriber<EchoResp> responseSubscriber) {
        stub.serverStreaming(request, responseSubscriber);
    }

    @Override
    public Subscriber<EchoReq> bidiStreaming(Subscriber<EchoResp> responseSubscriber) {
        return stub.bidiStreaming(responseSubscriber);
    }
}
