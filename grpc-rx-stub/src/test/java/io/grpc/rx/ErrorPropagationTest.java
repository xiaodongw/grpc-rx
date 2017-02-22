package io.grpc.rx;

import io.grpc.StatusException;
import io.grpc.StatusRuntimeException;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;
import org.junit.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ErrorPropagationTest extends TestBase {
  @Override
  protected EchoGrpcRx.EchoImplBase newService() {
    return new EchoGrpcRx.EchoImplBase() {
      @Override
      public Single<EchoService.EchoResp> unary(EchoService.EchoReq request) {
        return Single.error(new RuntimeException("mocked unary error"));
      }

      @Override
      public Flowable<EchoService.EchoResp> serverStreaming(EchoService.EchoCountReq request) {
        return Flowable.ambArray(
            Flowable.range(0, 5).map(i -> EchoService.EchoResp.newBuilder().build()),
            Flowable.error(new RuntimeException("mocked server streaming error")));
      }

      @Override
      public Single<EchoService.EchoCountResp> clientStreaming(Flowable<EchoService.EchoReq> requests) {
        AtomicInteger count = new AtomicInteger();
        return requests.map(r -> {
          if (count.incrementAndGet() >=5) {
            throw new RuntimeException("mocked client streaming error");
          }

          return 0;
        }).count().map(c -> EchoService.EchoCountResp.newBuilder().setCount(c.intValue()).build());
      }
    };
  }

  @Test
  public void testUnaryError() {
    EchoGrpcRx.EchoStub stub = EchoGrpcRx.newStub(channel);

    TestObserver<EchoService.EchoResp> testObserver = new TestObserver<>();
    stub.unary(EchoService.EchoReq.getDefaultInstance())
        .subscribe(testObserver);

    testObserver.awaitDone(testWaitSeconds, TimeUnit.SECONDS);
    testObserver.assertError(StatusRuntimeException.class);
    testObserver.assertErrorMessage("UNKNOWN: mocked unary error");
  }

  @Test
  public void testServerStreamingError() {
    EchoGrpcRx.EchoStub stub = EchoGrpcRx.newStub(channel);

    AutoTestSubscriber<EchoService.EchoResp> testSubscriber = new AutoTestSubscriber<EchoService.EchoResp>(4);
    stub.serverStreaming(EchoService.EchoCountReq.getDefaultInstance())
        .subscribe(testSubscriber);

    testSubscriber.awaitDone(testWaitSeconds, TimeUnit.SECONDS);
    testSubscriber.assertError(StatusRuntimeException.class);
    testSubscriber.assertErrorMessage("UNKNOWN: mocked server streaming error");
  }

  @Test
  public void testClientStreamingError() {
    EchoGrpcRx.EchoStub stub = EchoGrpcRx.newStub(channel);

    Flowable<EchoService.EchoReq> requests = Flowable.range(0, 16).map(i -> EchoService.EchoReq.getDefaultInstance());

    TestObserver<EchoService.EchoCountResp> testObserver = new TestObserver<>();
    stub.clientStreaming(requests)
        .subscribe(testObserver);

    testObserver.awaitDone(testWaitSeconds, TimeUnit.SECONDS);
    testObserver.assertError(StatusRuntimeException.class);
    testObserver.assertErrorMessage("CANCELLED: Subscripton cancelled");
  }
}
