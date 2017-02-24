package io.grpc.rx;

import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;
import io.reactivex.subscribers.TestSubscriber;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static io.grpc.rx.EchoService.*;

public abstract class EchoTest extends TestBase {
  static private int testWaitSeconds = 10;
  static private int streamNum = 128;

  @Override
  protected EchoGrpcRx.EchoImplBase newService() {
    return new EchoGrpcRx.EchoImplBase() {
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
    };
  }

  @Test
  public void testUnary() {
    EchoGrpcRx.EchoStub stub = EchoGrpcRx.newStub(channel);

    EchoReq req = EchoReq.newBuilder()
        .setId(1)
        .setValue("Hello")
        .build();
    TestObserver<EchoResp> responseObserver = new TestObserver<EchoService.EchoResp>();

    stub.unary(req)
        .subscribe(responseObserver);

    responseObserver.awaitDone(testWaitSeconds, TimeUnit.SECONDS);
    responseObserver.assertComplete();
  }

  @Test
  public void testServerStreaming() {
    EchoGrpcRx.EchoStub stub = EchoGrpcRx.newStub(channel);

    EchoCountReq req = EchoCountReq.newBuilder()
        .setCount(streamNum)
        .build();
    TestSubscriber<EchoResp> responseSubscriber = new AutoTestSubscriber<EchoResp>(4);

    stub.serverStreaming(req)
        .subscribe(responseSubscriber);

    responseSubscriber.awaitDone(testWaitSeconds, TimeUnit.SECONDS);
    responseSubscriber.assertValueCount(streamNum);
    responseSubscriber.assertComplete();
  }

  @Test
  public void testClientStreaming() {
    EchoGrpcRx.EchoStub stub = EchoGrpcRx.newStub(channel);

    TestObserver<EchoCountResp> responseObserver = new TestObserver<EchoService.EchoCountResp>();

    Flowable<EchoReq> requests = Flowable.range(0, streamNum)
        .map(i -> EchoService.EchoReq.newBuilder()
            .setId(i)
            .setValue(String.format("%d", i))
            .build());

    stub.clientStreaming(requests)
        .subscribe(responseObserver);

    responseObserver.awaitDone(testWaitSeconds, TimeUnit.SECONDS);
    responseObserver.assertValue(EchoCountResp.newBuilder().setCount(streamNum).build());
    responseObserver.assertComplete();
  }

  @Test
  public void testBidiStreaming() {
    EchoGrpcRx.EchoStub stub = EchoGrpcRx.newStub(channel);

    Flowable<EchoReq> requests = Flowable.range(0, streamNum)
        .map(i -> EchoService.EchoReq.newBuilder()
            .setId(i)
            .setValue(String.format("%d", i))
            .build());

    TestSubscriber<EchoService.EchoResp> responseSubscriber = new AutoTestSubscriber(4);

    stub.bidiStreaming(requests)
        .subscribe(responseSubscriber);

    responseSubscriber.awaitDone(testWaitSeconds, TimeUnit.SECONDS);
    responseSubscriber.assertValueCount(streamNum);
    responseSubscriber.assertComplete();
  }

  public static class EchoUnitTest extends EchoTest {}

  public static class EchoIntegrationTest extends EchoTest {
    @Override
    protected ManagedChannel newChannel() {
      return IntegrationTestHelper.newChannel();
    }

    @Override
    protected Server newServer() {
      return IntegrationTestHelper.newServer(newService());
    }
  }
}
