package io.grpc.rx;

import io.reactivex.Flowable;
import io.reactivex.observers.TestObserver;
import io.reactivex.subscribers.TestSubscriber;
import org.junit.Test;
import org.reactivestreams.Subscriber;
import static io.grpc.rx.EchoService.*;

import java.util.concurrent.TimeUnit;

public abstract class EchoTestBase {
    protected EchoGrpcRx.EchoStub client;
    private int testWaitSeconds = 10;
    private int streamNum = 128;

    @Test
    public void unary() {
        EchoService.EchoReq req = EchoReq.newBuilder().setId(1).setValue("Hello").build();
        TestObserver<EchoService.EchoResp> responseObserver = new TestObserver<EchoService.EchoResp>();

        client.unary(req, responseObserver);

        responseObserver.awaitDone(testWaitSeconds, TimeUnit.SECONDS);
        responseObserver.assertComplete();
    }

    @Test
    public void serverStreaming() {
        EchoService.EchoCountReq req = EchoService.EchoCountReq.newBuilder().setCount(streamNum).build();
        TestSubscriber<EchoService.EchoResp> responseSubscriber = new AutoTestSubscriber<>(4);

        client.serverStreaming(req, responseSubscriber);

        responseSubscriber.awaitDone(testWaitSeconds, TimeUnit.SECONDS);
        responseSubscriber.assertValueCount(streamNum);
        responseSubscriber.assertComplete();
    }

    @Test
    public void clientStreaming() {
        TestObserver<EchoService.EchoCountResp> responseObserver = new TestObserver<EchoService.EchoCountResp>();

        EchoService.EchoReq[] requests = new EchoService.EchoReq[streamNum];
        for (int i = 0; i < requests.length; i++) {
            requests[i] = EchoService.EchoReq.newBuilder().setId(i).setValue(String.format("%d", i)).build();
        }
        Flowable<EchoService.EchoReq> requestFlowable = Flowable.fromArray(requests);
        Subscriber<EchoService.EchoReq> requestSubscriber = client.clientStreaming(responseObserver);

        // start to send requests to grpc subscriber
        requestFlowable.subscribe(requestSubscriber);

        responseObserver.awaitDone(testWaitSeconds, TimeUnit.SECONDS);
        responseObserver.assertValue(EchoCountResp.newBuilder().setCount(streamNum).build());
        responseObserver.assertComplete();
    }

    @Test
    public void bidiStreaming() {
        EchoService.EchoReq[] requests = new EchoService.EchoReq[streamNum];
        for (int i = 0; i < requests.length; i++) {
            requests[i] = EchoService.EchoReq.newBuilder().setId(i).setValue(String.format("%d", i)).build();
        }
        Flowable<EchoService.EchoReq> requestFlowable = Flowable.fromArray(requests);

        TestSubscriber<EchoService.EchoResp> responseSubscriber = new AutoTestSubscriber(4);

        Subscriber<EchoService.EchoReq> requestSubscriber = client.bidiStreaming(responseSubscriber);

        // start to send requests to grpc subscriber
        requestFlowable.subscribe(requestSubscriber);

        responseSubscriber.awaitDone(testWaitSeconds, TimeUnit.SECONDS);
        responseSubscriber.assertValueCount(streamNum);
        responseSubscriber.assertComplete();
    }
}
