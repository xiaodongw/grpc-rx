package io.grpc.rx;

import io.grpc.CallOptions;
import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.NettyServerBuilder;
import io.grpc.rx.stub.GrpcRxOptions;
import io.reactivex.Flowable;
import io.reactivex.subscribers.TestSubscriber;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import static io.grpc.rx.EchoService.*;

public abstract class FlowControlTest extends TestBase {
  private Logger logger = LoggerFactory.getLogger(getClass());
  private String bigStr = new String(new byte[1024 * 8]);

  @Override
  protected EchoGrpcRx.EchoImplBase newService() {
    return new EchoGrpcRx.EchoImplBase() {
      @Override
      public Flowable<EchoResp> bidiStreaming(Flowable<EchoReq> requests) {
        return requests.map(r -> {
          // simulate a slow service
          Thread.sleep(100);

          return EchoResp.newBuilder()
              .setId(r.getId())
              .setValue(r.getValue())
              .build();
        });
      }
    };
  }

  @Test
  public void testFlowControl() {
    CallOptions callOptions = CallOptions.DEFAULT
        .withOption(GrpcRxOptions.LOW_WATERMARK, 2)
        .withOption(GrpcRxOptions.HIGH_WATERMARK, 6);
    EchoGrpcRx.EchoStub stub = EchoGrpcRx.newStub(channel, callOptions);

    Flowable<EchoReq> requests = Flowable.range(0, 64)
        .map(i -> {
          logger.info("Emitting request {}", i);

          return EchoReq.newBuilder()
              .setId(i)
              .setValue(bigStr)
              .build();
        });

    TestSubscriber<EchoResp> testSubscriber = new AutoTestSubscriber<>(4);
    stub.bidiStreaming(requests)
        .subscribe(testSubscriber);

    testSubscriber.awaitDone(1, TimeUnit.MINUTES);
    testSubscriber.assertComplete();
  }

  @Ignore("Flow control test, manually verified by log")
  public static class FlowControlUnitTest extends FlowControlTest {}

  @Ignore("Flow control test, manually verified by log")
  public static class FlowControlIntegrationTest extends FlowControlTest {
    // tiny window so only few outstanding messages is allowed
    private int flowWindow = 32;

    @Override
    protected ManagedChannel newChannel() {
      return NettyChannelBuilder
          .forAddress("localhost", 1234)
          .flowControlWindow(flowWindow)
          .usePlaintext()
          .build();
    }

    @Override
    protected Server newServer() {
      return NettyServerBuilder
          .forPort(1234)
          .flowControlWindow(flowWindow)
          .addService(newService())
          .build();
    }
  }
}
