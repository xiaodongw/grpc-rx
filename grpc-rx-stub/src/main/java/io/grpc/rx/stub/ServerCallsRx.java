package io.grpc.rx.stub;

import io.grpc.*;
import io.grpc.rx.core.GrpcHelpers;
import io.grpc.rx.core.LogUtils;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Utility functions for adapting {@link ServerCallHandler}s to application service implementation,
 * meant to be used by the generated code.
 */
public final class ServerCallsRx {

  private ServerCallsRx() {
  }

  /**
   * Creates a {@code ServerCallHandler} for a unary call method of the service.
   *
   * @param method an adaptor to the actual method on the service implementation.
   */
  public static <ReqT, RespT> ServerCallHandler<ReqT, RespT> unaryCall(
      final UnaryMethod<ReqT, RespT> method) {
    return new UnaryServerCallHandler<ReqT, RespT>(method);
  }

  /**
   * Creates a {@code ServerCallHandler} for a server streaming method of the service.
   *
   * @param method an adaptor to the actual method on the service implementation.
   */
  public static <ReqT, RespT> ServerCallHandler<ReqT, RespT> serverStreamingCall(
      final ServerStreamingMethod<ReqT, RespT> method) {
    return new ServerStreamingServerCallHandler<ReqT, RespT>(method);
  }

  /**
   * Creates a {@code ServerCallHandler} for a client streaming method of the service.
   *
   * @param method an adaptor to the actual method on the service implementation.
   */
  public static <ReqT, RespT> ServerCallHandler<ReqT, RespT> clientStreamingCall(
      final ClientStreamingMethod<ReqT, RespT> method) {
    return new ClientStreamingServerCallHandler<ReqT, RespT>(method);
  }

  public static <ReqT, RespT> ServerCallHandler<ReqT, RespT> bidiStreamingCall(
      final BidiStreamingMethod<ReqT, RespT> method) {
    return new BidiStreamingServerCallHandler<ReqT, RespT>(method);
  }

  /**
   * A [SingleObserver] which dispatches the message to GRPC as response.
   *
   * @param <RespT>
   */
  private static class ResponseObserver<RespT> implements SingleObserver<RespT> {
    private ServerCall<?, RespT> call;

    public ResponseObserver(ServerCall<?, RespT> call) {
      this.call = call;
    }

    @Override
    public void onSubscribe(Disposable d) {
    }

    @Override
    public void onSuccess(RespT value) {
      call.sendHeaders(new Metadata());
      call.sendMessage(value);
      call.close(Status.OK, new Metadata());
    }

    @Override
    public void onError(Throwable e) {
      try {
        // todo the call may be already closed
        call.close(Status.fromThrowable(e).withDescription(e.getMessage()), new Metadata());
      } catch (Throwable t) {
        // supress error
      }
    }
  }

  /**
   * A [Subscriber] which dispatches messages to GRPC as response.
   *
   * @param <RespT>
   */
  private static class ResponseSubscriber<RespT> implements Subscriber<RespT> {
    private Subscription subscription;
    private ServerCall<?, RespT> call;
    private int lowWaterMark = 8;
    private int highWaterMark = 32;
    private AtomicInteger pendingResps = new AtomicInteger();
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public ResponseSubscriber(ServerCall<?, RespT> call, int lowWaterMark, int highWaterMark) {
      this.call = call;
      this.lowWaterMark = lowWaterMark;
      this.highWaterMark = highWaterMark;
    }

    @Override
    public void onSubscribe(Subscription s) {
      logger.trace("onSubscribe: subscription={}", s);
      subscription = s;

      // todo when is the best time to send headers
      call.sendHeaders(new Metadata());
    }

    @Override
    public void onNext(RespT message) {
      logger.trace("onNext: message={}", LogUtils.objectString(message));
      call.sendMessage(message);
      pendingResps.decrementAndGet();
      askResponses();
    }

    @Override
    public void onError(Throwable t) {
      logger.trace("onError: t={}", t);
      call.close(Status.fromThrowable(t).withDescription(t.getMessage()), new Metadata());
    }

    @Override
    public void onComplete() {
      logger.trace("onComplete");
      call.close(Status.OK, new Metadata());
    }

//		public Subscription getSubscription() {
//			return subscription;
//		}

    public void askResponses() {
      if (subscription == null) return;

      int p = pendingResps.get();
      if (p < lowWaterMark) {
        int want = highWaterMark - p;
        subscription.request(want);
        pendingResps.addAndGet(want);
      }
    }
  }

  /**
   * Listener for single request, it raises error if more than one request arrives
   *
   * @param <ReqT>
   */
  private static abstract class SingleRequestListener<ReqT> extends ServerCall.Listener<ReqT> {
    private ServerCall<ReqT, ?> call;
    private ReqT request;

    public SingleRequestListener(ServerCall<ReqT, ?> call) {
      this.call = call;

      call.request(2);
    }

    @Override
    public void onMessage(ReqT message) {
      request = message;
    }

    @Override
    public void onHalfClose() {
      if (request != null) {
        invoke(request);

        if (call.isReady()) {
          // Since we are calling invoke in halfClose we have missed the askResponses
          // event from the transport so recover it here.
          onReady();
        }
      } else {
        call.close(Status.INTERNAL.withDescription("Half-closed without a requestMore"), new Metadata());
      }
    }

    @Override
    public void onCancel() {
      //responseObserver.onError();
    }

    @Override
    public void onComplete() {
      //call.close(Status.OK, new Metadata());
    }

    @Override
    public void onReady() {
      //super.askResponses();
    }

    protected abstract void invoke(ReqT request);
  }

  /**
   * Streaming request listener, dispatches the request messages from GRPC as a [Publisher].
   *
   * @param <ReqT>
   */
  private static class RequestPublisher<ReqT> extends ServerCall.Listener<ReqT> implements Publisher<ReqT> {
    private ServerCall<ReqT, ?> call;
    private Subscriber<? super ReqT> subscriber;
    private Subscription requestSubscription;
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public RequestPublisher(final ServerCall<ReqT, ?> call) {
      this.call = call;
    }

    @Override
    public void onMessage(ReqT message) {
      logger.trace("onMessage: message={}", LogUtils.objectString(message));
      subscriber.onNext(message);
    }

    @Override
    public void onHalfClose() {
      logger.trace("onHalfClose");
      subscriber.onComplete();
    }

    @Override
    public void onCancel() {
      logger.trace("onCancel");
      subscriber.onError(new CancellationException("cancelled from grpc"));
    }

    @Override
    public void onComplete() {

    }

    @Override
    public void onReady() {

    }

    @Override
    public void subscribe(Subscriber<? super ReqT> s) {
      this.subscriber = s;

      requestSubscription = new Subscription() {
        @Override
        public void request(long n) {
          int fixed = GrpcHelpers.fixRequestNum(n);
          logger.trace("subscription.request, n={}", fixed);
          call.request(fixed);
        }

        @Override
        public void cancel() {
          logger.trace("subscription.cancel");

          // todo cannot tell cancelled by excepting or initiated
          call.close(Status.CANCELLED.withDescription("Subscripton cancelled"), new Metadata());
        }
      };
      subscriber.onSubscribe(requestSubscription);
    }
  }

  /**
   * Unary call handler, combines SingleRequestListener & ResponseObserver
   *
   * @param <ReqT>
   * @param <RespT>
   */
  public static class UnaryServerCallHandler<ReqT, RespT> implements ServerCallHandler<ReqT, RespT> {
    private UnaryMethod<ReqT, RespT> method;

    public UnaryServerCallHandler(UnaryMethod<ReqT, RespT> method) {
      this.method = method;
    }

    @Override
    public ServerCall.Listener<ReqT> startCall(ServerCall<ReqT, RespT> call, Metadata headers) {
      final SingleObserver<RespT> responseObserver = new ResponseObserver<RespT>(call);

      return new SingleRequestListener<ReqT>(call) {
        @Override
        protected void invoke(ReqT request) {
          method.unaryInvoke(request)
              .subscribe(responseObserver);
        }
      };
    }
  }

  /**
   * Server streaming call handler, combines SingleRequestListener & ResponseSubscriber
   *
   * @param <ReqT>
   * @param <RespT>
   */
  public static class ServerStreamingServerCallHandler<ReqT, RespT> implements ServerCallHandler<ReqT, RespT> {
    private ServerStreamingMethod<ReqT, RespT> method;

    public ServerStreamingServerCallHandler(ServerStreamingMethod<ReqT, RespT> method) {
      this.method = method;
    }

    @Override
    public ServerCall.Listener<ReqT> startCall(ServerCall<ReqT, RespT> call, Metadata headers) {
      // todo implement server side watermark settings per method
      final ResponseSubscriber<RespT> responseSubscriber = new ResponseSubscriber<RespT>(call, 4, 32);

      return new SingleRequestListener<ReqT>(call) {
        @Override
        protected void invoke(ReqT request) {
          method.serverStreamingInvoke(request)
              .subscribe(responseSubscriber);
        }

        @Override
        public void onReady() {
          responseSubscriber.askResponses();
        }
      };
    }
  }

  /**
   * Client streaming handler, combines StreamRequestListener & ResponseObserver
   *
   * @param <ReqT>
   * @param <RespT>
   */
  public static class ClientStreamingServerCallHandler<ReqT, RespT> implements ServerCallHandler<ReqT, RespT> {
    private ClientStreamingMethod<ReqT, RespT> method;

    public ClientStreamingServerCallHandler(ClientStreamingMethod<ReqT, RespT> method) {
      this.method = method;
    }

    @Override
    public ServerCall.Listener<ReqT> startCall(ServerCall<ReqT, RespT> call, Metadata headers) {
      SingleObserver<RespT> responseObserver = new ResponseObserver<RespT>(call);
      RequestPublisher<ReqT> requestPublisher = new RequestPublisher<ReqT>(call);

      method.clientStreamingInvoke(Flowable.fromPublisher(requestPublisher))
          .subscribe(responseObserver);

      return requestPublisher;
    }
  }

  /**
   * Bidi streaming handler, combines StreamRequestListener & ResponseSubscriber
   *
   * @param <ReqT>
   * @param <RespT>
   */
  public static class BidiStreamingServerCallHandler<ReqT, RespT> implements ServerCallHandler<ReqT, RespT> {
    private BidiStreamingMethod<ReqT, RespT> method;

    public BidiStreamingServerCallHandler(BidiStreamingMethod<ReqT, RespT> method) {
      this.method = method;
    }

    @Override
    public ServerCall.Listener<ReqT> startCall(ServerCall<ReqT, RespT> call, Metadata headers) {
      // todo implement server side watermark settings per method
      final ResponseSubscriber<RespT> responseSubscriber = new ResponseSubscriber<RespT>(call, 4, 32);
      RequestPublisher<ReqT> requestPublisher = new RequestPublisher<ReqT>(call) {
        @Override
        public void onReady() {
          // trigger the flow, which starts asking for response, which then triggers asking for requests
          responseSubscriber.askResponses();
        }
      };

      method.bidiStreamingInvoke(Flowable.fromPublisher(requestPublisher))
          .subscribe(responseSubscriber);

      return requestPublisher;
    }
  }

  /**
   * Adaptor to a unary call method.
   */
  public interface UnaryMethod<ReqT, RespT> {
    Single<RespT> unaryInvoke(ReqT request);
  }

  /**
   * Adaptor to a server streaming method.
   */
  public interface ServerStreamingMethod<ReqT, RespT> {
    Flowable<RespT> serverStreamingInvoke(ReqT request);
  }

  /**
   * Adaptor to a client streaming method.
   */
  public interface ClientStreamingMethod<ReqT, RespT> {
    Single<RespT> clientStreamingInvoke(Flowable<ReqT> requests);
  }

  /**
   * Adaptor to a bi-directional streaming method.
   */
  public interface BidiStreamingMethod<ReqT, RespT> {
    Flowable<RespT> bidiStreamingInvoke(Flowable<ReqT> requests);
  }

  public static <T> Single<T> unimplementedUnaryCall(
      MethodDescriptor<?, ?> methodDescriptor) {
    checkNotNull(methodDescriptor);
    return Single.error(Status.UNIMPLEMENTED
        .withDescription(String.format("Method %s is unimplemented", methodDescriptor.getFullMethodName()))
        .asRuntimeException());
  }

  public static <T> Flowable<T> unimplementedStreamingCall(MethodDescriptor<?, ?> methodDescriptor) {
    checkNotNull(methodDescriptor);
    return Flowable.error(Status.UNIMPLEMENTED
        .withDescription(String.format("Method %s is unimplemented", methodDescriptor.getFullMethodName()))
        .asRuntimeException());
  }
}
