package io.grpc.rx.stub;

import io.grpc.ClientCall;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.rx.core.GrpcPublisher;
import io.grpc.rx.core.GrpcSubscriber;
import io.grpc.stub.StreamObserver;
import io.reactivex.SingleObserver;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ClientCallsRx {
  private ClientCallsRx() {
  }

  /**
   * Executes a unary call with a response {@link SingleObserver}.
   */
  public static <ReqT, RespT> void rxUnaryCall(
      ClientCall<ReqT, RespT> call,
      ReqT request,
      SingleObserver<RespT> responseObserver) {
    SingleRequestSender<ReqT> requestSender = new SingleRequestSender<ReqT>(call, request);
    SingleResponseReceiver<RespT> responseReceiver = new SingleResponseReceiver<RespT>(call, responseObserver);
    ClientCallProxy<ReqT, RespT> proxy = new ClientCallProxy(requestSender, responseReceiver);
    startCall(call, proxy);
  }

  /**
   * Executes a server-streaming call with a response {@link Subscriber}.
   */
  public static <ReqT, RespT> void rxServerStreamingCall(
      ClientCall<ReqT, RespT> call,
      ReqT request,
      Subscriber<RespT> responseSubscriber) {
    SingleRequestSender<ReqT> requestSender = new SingleRequestSender<ReqT>(call, request);
    StreamResponseReceiver<RespT> responseReceiver = new StreamResponseReceiver<RespT>(call);
    ClientCallProxy<ReqT, RespT> proxy = new ClientCallProxy(requestSender, responseReceiver);
    startCall(call, proxy);

    responseReceiver.getPublisher().subscribe(responseSubscriber);
  }

  /**
   * Executes a client-streaming call returning a {@link StreamObserver} for the requestMore messages.
   *
   * @return requestMore stream observer.
   */
  public static <ReqT, RespT> Subscriber<ReqT> rxClientStreamingCall(
      ClientCall<ReqT, RespT> call,
      SingleObserver<RespT> responseObserver) {
    StreamRequestSender<ReqT> requestSender = new StreamRequestSender<ReqT>(call);
    SingleResponseReceiver<RespT> responseReceiver = new SingleResponseReceiver<RespT>(call, responseObserver);
    ClientCallProxy<ReqT, RespT> proxy = new ClientCallProxy(
        requestSender,
        responseReceiver);
    startCall(call, proxy);
    return requestSender.getSubscriber();
  }

  /**
   * Executes a bidi-streaming call.
   *
   * @return requestMore stream observer.
   */
  public static <ReqT, RespT> Subscriber<ReqT> rxBidiStreamingCall(
      ClientCall<ReqT, RespT> call,
      Subscriber<RespT> responseSubscriber) {
    StreamRequestSender<ReqT> requestSender = new StreamRequestSender<ReqT>(call);
    StreamResponseReceiver<RespT> responseReceiver = new StreamResponseReceiver<RespT>(call);
    ClientCallProxy<ReqT, RespT> proxy = new ClientCallProxy(requestSender, responseReceiver);
    startCall(call, proxy);

    responseReceiver.getPublisher().subscribe(responseSubscriber);
    return requestSender.getSubscriber();
  }

  private static <ReqT, RespT> void startCall(ClientCall<ReqT, RespT> call,
                                              ClientCallProxy<ReqT, RespT> proxy) {
    call.start(proxy, new Metadata());
    proxy.start();
  }

  private static abstract class ClientCallListener<RespT> extends ClientCall.Listener<RespT> {
    public abstract void start();
  }

  private static class ClientCallProxy<ReqT, RespT> extends ClientCallListener<RespT> {
    private ClientCallListener<RespT> requestAdapter;
    private ClientCallListener<RespT> responseAdapter;

    public ClientCallProxy(ClientCallListener<RespT> requestAdapter, ClientCallListener<RespT> responseAdapter) {
      this.requestAdapter = requestAdapter;
      this.responseAdapter = responseAdapter;
    }

    @Override
    public void start() {
      requestAdapter.start();
      responseAdapter.start();
    }

    @Override
    public void onHeaders(Metadata headers) {
      //requestAdapter.onHeaders(headers);
      responseAdapter.onHeaders(headers);
    }

    @Override
    public void onMessage(RespT message) {
      //requestAdapter.onMessage(message);
      responseAdapter.onMessage(message);
    }

    @Override
    public void onClose(Status status, Metadata trailers) {
      //requestAdapter.onClose(status, trailers);
      responseAdapter.onClose(status, trailers);
    }

    @Override
    public void onReady() {
      requestAdapter.onReady();
      //responseAdapter.askResponses();
    }
  }

  private static class SingleRequestSender<ReqT> extends ClientCallListener<ReqT> {
    private ClientCall<ReqT, ?> call;
    private ReqT request;

    public SingleRequestSender(ClientCall<ReqT, ?> call, ReqT request) {
      this.call = call;
      this.request = request;
    }

    @Override
    public void start() {
      call.sendMessage(request);
      call.halfClose();
    }
  }

  private static class SingleResponseReceiver<RespT> extends ClientCallListener<RespT> {
    private ClientCall<?, RespT> call;
    private SingleObserver<RespT> responseObserver;
    private RespT response;

    public SingleResponseReceiver(ClientCall<?, RespT> call, SingleObserver<RespT> responseObserver) {
      this.call = call;
      this.responseObserver = responseObserver;
    }

    @Override
    public void start() {
      call.request(2);
    }

    public void onMessage(RespT value) {
      if (this.response != null) {
        throw Status.INTERNAL.withDescription("More than one value received for unary call")
            .asRuntimeException();
      }
      this.response = value;
    }

    public void onClose(Status status, Metadata trailers) {
      if (status.isOk()) {
        if (response == null) {
          // No value received so mark the future as an error
          responseObserver.onError(
              Status.INTERNAL.withDescription("No value received for unary call")
                  .asRuntimeException(trailers));
        } else {
          responseObserver.onSuccess(response);
        }
      } else {
        responseObserver.onError(status.asRuntimeException(trailers));
      }
    }
  }

  private static class StreamRequestSender<ReqT> extends ClientCallListener<ReqT> {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private final ClientCall<ReqT, ?> call;

    private GrpcSubscriber<ReqT> grpcSubscriber = new GrpcSubscriber<ReqT>() {
      @Override
      protected boolean isReady() {
        return call.isReady();
      }

      @Override
      protected void sendMessage(ReqT req) {
        call.sendMessage(req);
      }

      @Override
      protected void error(Throwable t) {
        call.cancel("Upstream error", t);
      }

      @Override
      protected void complete() {
        call.halfClose();
      }
    };

    public StreamRequestSender(ClientCall<ReqT, ?> call) {
      this.call = call;
    }

    public Subscriber<ReqT> getSubscriber() {
      return grpcSubscriber;
    }

    @Override
    public void onReady() {
      logger.trace("onReady");
      grpcSubscriber.ready();
    }

    @Override
    public void start() {
      grpcSubscriber.ready();
    }
  }

  private static class StreamResponseReceiver<RespT> extends ClientCallListener<RespT> {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private final ClientCall<?, RespT> call;


    private GrpcPublisher<RespT> grpcPublisher = new GrpcPublisher<RespT>() {
      @Override
      protected void requestMore(long n) {
        call.request((int) n);
      }

      @Override
      protected void cancelSubscription(String message, Throwable cause) {
        call.cancel(message, cause);
      }
    };

    public StreamResponseReceiver(ClientCall<?, RespT> call) {
      this.call = call;
    }

    public Publisher<RespT> getPublisher() {
      return grpcPublisher;
    }


    @Override
    public void onMessage(RespT message) {
      logger.trace("onMessage: message={}", message);
      grpcPublisher.message(message);
    }

    @Override
    public void onClose(Status status, Metadata trailers) {
      logger.trace("onClose");
      if (status.isOk()) {
        grpcPublisher.complete();

      } else {
        grpcPublisher.error(status.asRuntimeException(trailers));
      }
    }

    @Override
    public void start() {

    }
  }
}
