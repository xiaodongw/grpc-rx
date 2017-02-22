package io.grpc.rx;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.reactivex.observers.TestObserver;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

public class UnimplementedMethodTest extends TestBase {
  @Override
  protected EchoGrpcRx.EchoImplBase newService() {
    return new EchoGrpcRx.EchoImplBase() {} ;
  }

  @Test
  public void testUnimplementedMethod() {
    EchoGrpcRx.EchoStub stub = EchoGrpcRx.newStub(channel);
    TestObserver<EchoService.EchoResp> observer = new TestObserver<>();
    EchoService.EchoReq request = EchoService.EchoReq.newBuilder()
        .setId(1)
        .setValue("hello")
        .build();

    stub.unary(request)
        .subscribe(observer);

    observer.awaitDone(testWaitSeconds, TimeUnit.SECONDS);
    observer.assertError(t -> {
      if (!(t instanceof StatusRuntimeException)) return false;
      StatusRuntimeException e = (StatusRuntimeException)t;
      if (e.getStatus().getCode() != Status.UNIMPLEMENTED.getCode()) return false;
      if (!e.getStatus().getDescription().equals("UNIMPLEMENTED: Method io.grpc.rx.Echo/unary is unimplemented")) return false;

      return true;
    });
  }
}
