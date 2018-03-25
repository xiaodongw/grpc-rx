package io.grpc.rx;

import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import io.grpc.netty.NettyChannelBuilder;
import io.grpc.rx.EchoService.EchoReq;
import io.grpc.rx.EchoService.EchoResp;
import io.reactivex.Flowable;
import io.reactivex.observers.TestObserver;
import io.reactivex.subscribers.TestSubscriber;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

public class NonExistServiceTest {
  private static ManagedChannel channel;
  private final EchoGrpcRx.EchoStub client;

  // InetAddress.getAllByName() blocks, give the test more time to throw exception
  protected int testWaitSeconds = 30;

  public NonExistServiceTest() {
    client = newClient();
  }

  private static EchoGrpcRx.EchoStub newClient() {
    channel = NettyChannelBuilder
        .forAddress("abc", 123)
        .usePlaintext(true)
        .build();

    return EchoGrpcRx.newStub(channel);
  }

  @Test
  public void testUnaryCall() {
    TestObserver<EchoResp> testObserver = new TestObserver<>();
    client.unary(EchoReq.getDefaultInstance())
        .subscribe(testObserver);

    testObserver.awaitDone(testWaitSeconds, TimeUnit.SECONDS);
    testObserver.assertError(StatusRuntimeException.class);
  }

  @Test
  public void testBidiStreamingCall() {
    TestSubscriber<EchoResp> testSubscriber = new AutoTestSubscriber<>(4);
    Flowable<EchoReq> requests = Flowable.range(0, 4).map(i -> EchoReq.getDefaultInstance());
    client.bidiStreaming(requests)
        .subscribe(testSubscriber);

    testSubscriber.awaitDone(testWaitSeconds, TimeUnit.SECONDS);
    testSubscriber.assertError(StatusRuntimeException.class);
  }
}
