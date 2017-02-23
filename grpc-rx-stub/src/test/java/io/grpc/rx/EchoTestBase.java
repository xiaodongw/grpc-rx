package io.grpc.rx;

import io.reactivex.Flowable;
import io.reactivex.observers.TestObserver;
import io.reactivex.subscribers.TestSubscriber;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static io.grpc.rx.EchoService.*;

public abstract class EchoTestBase {
  protected EchoGrpcRx.EchoStub client;
  private int testWaitSeconds = 10;
  private int streamNum = 128;

  @Test
  public void unary() {
    EchoService.EchoReq req = EchoReq.newBuilder().setId(1).setValue("Hello").build();
    TestObserver<EchoService.EchoResp> responseObserver = new TestObserver<EchoService.EchoResp>();

    client.unary(req)
        .subscribe(responseObserver);

    responseObserver.awaitDone(testWaitSeconds, TimeUnit.SECONDS);
    responseObserver.assertComplete();
  }

  @Test
  public void serverStreaming() {
    EchoService.EchoCountReq req = EchoService.EchoCountReq.newBuilder().setCount(streamNum).build();
    TestSubscriber<EchoService.EchoResp> responseSubscriber = new AutoTestSubscriber<EchoResp>(4);

    client.serverStreaming(req)
        .subscribe(responseSubscriber);

    responseSubscriber.awaitDone(testWaitSeconds, TimeUnit.SECONDS);
    responseSubscriber.assertValueCount(streamNum);
    responseSubscriber.assertComplete();
  }

  @Test
  public void clientStreaming() {
    TestObserver<EchoService.EchoCountResp> responseObserver = new TestObserver<EchoService.EchoCountResp>();

    Flowable<EchoReq> requests = Flowable.range(0, streamNum)
        .map(i -> EchoService.EchoReq.newBuilder()
            .setId(i)
            .setValue(String.format("%d", i))
            .build());

    client.clientStreaming(requests)
        .subscribe(responseObserver);

    responseObserver.awaitDone(testWaitSeconds, TimeUnit.SECONDS);
    responseObserver.assertValue(EchoCountResp.newBuilder().setCount(streamNum).build());
    responseObserver.assertComplete();
  }

  @Test
  public void bidiStreaming() {
    Flowable<EchoReq> requests = Flowable.range(0, streamNum)
        .map(i -> EchoService.EchoReq.newBuilder()
            .setId(i)
            .setValue(String.format("%d", i))
            .build());

    TestSubscriber<EchoService.EchoResp> responseSubscriber = new AutoTestSubscriber(4);

    client.bidiStreaming(requests)
        .subscribe(responseSubscriber);

    responseSubscriber.awaitDone(testWaitSeconds, TimeUnit.SECONDS);
    responseSubscriber.assertValueCount(streamNum);
    responseSubscriber.assertComplete();
  }
}
