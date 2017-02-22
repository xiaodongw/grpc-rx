package io.grpc.rx.stub;

import io.grpc.ClientCall;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.rx.core.DelegateClientCallListener;
import io.grpc.rx.core.GrpcPublisher;
import io.grpc.rx.core.GrpcSubscriber;
import io.grpc.stub.StreamObserver;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.SingleSource;
import io.reactivex.disposables.Disposables;
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
  public static <ReqT, RespT> Single<RespT> unaryCall(
      final ClientCall<ReqT, RespT> call,
      final ReqT request) {
    final SingleRequestSender<ReqT> requestSender = new SingleRequestSender<ReqT>(call, request);
    SingleResponseReceiver<RespT> responseReceiver = new SingleResponseReceiver<RespT>(call) {
      @Override
      public void startCall() {
        requestSender.startCall();
        super.startCall();
      }
    };

    call.start(responseReceiver, new Metadata());

    return Single.wrap(responseReceiver.singleSource());
  }

  /**
   * Executes a server-streaming call with a response {@link Subscriber}.
   */
  public static <ReqT, RespT> Flowable<RespT> serverStreamingCall(
      final ClientCall<ReqT, RespT> call,
      ReqT request) {
    final SingleRequestSender<ReqT> requestSender = new SingleRequestSender<ReqT>(call, request);
    StreamingResponseReceiver<RespT> responseReceiver = new StreamingResponseReceiver<RespT>(call) {
      @Override
      public void startCall() {
        requestSender.startCall();
        super.startCall();
      }
    };

    call.start(responseReceiver, new Metadata());

    return Flowable.fromPublisher(responseReceiver.publisher());
  }

  /**
   * Executes a client-streaming call returning a {@link StreamObserver} for the requestMore messages.
   *
   * @return requestMore stream observer.
   */
  public static <ReqT, RespT> Single<RespT> clientStreamingCall(
      ClientCall<ReqT, RespT> call,
      Flowable<ReqT> requests) {
    final StreamRequestSender<ReqT> requestSender = new StreamRequestSender<ReqT>(call);
    SingleResponseReceiver<RespT> responseReceiver = new SingleResponseReceiver<RespT>(call) {
      @Override
      public void startCall() {
        requestSender.startCall();
        super.startCall();
      }
    };

    ClientCall.Listener<RespT> delegate = new DelegateClientCallListener<RespT>(requestSender, responseReceiver);
    call.start(delegate, new Metadata());

    requests.subscribe(requestSender.subscriber());

    return Single.wrap(responseReceiver.singleSource());
  }

  /**
   * Executes a bidi-streaming call.
   *
   * @return requestMore stream observer.
   */
  public static <ReqT, RespT> Flowable<RespT> bidiStreamingCall(
      ClientCall<ReqT, RespT> call,
      Flowable<ReqT> requests) {
    final StreamRequestSender<ReqT> requestSender = new StreamRequestSender<ReqT>(call);
    StreamingResponseReceiver<RespT> responseReceiver = new StreamingResponseReceiver<RespT>(call) {
      @Override
      public void startCall() {
        requestSender.startCall();
        super.startCall();
      }
    };

    ClientCall.Listener<RespT> delegate = new DelegateClientCallListener<RespT>(requestSender, responseReceiver);
    call.start(delegate, new Metadata());

    requests.subscribe(requestSender.subscriber());

    return Flowable.fromPublisher(responseReceiver.publisher());
  }

  /**
   * Interface for starting the call.
   * The response Single / Flowable will trigger starting call on subscribe.
   */
  private interface StartCall {
    void startCall();
  }

  private static class SingleRequestSender<ReqT> implements StartCall {
    private ClientCall<ReqT, ?> call;
    private ReqT request;

    public SingleRequestSender(ClientCall<ReqT, ?> call, ReqT request) {
      this.call = call;
      this.request = request;
    }

    public void startCall() {
      call.sendMessage(request);
      call.halfClose();
    }
  }

  private static class SingleResponseReceiver<RespT> extends ClientCall.Listener<RespT> implements StartCall {
    protected ClientCall<?, RespT> call;
    private SingleObserver<? super RespT> responseObserver;
    private RespT response;
    private SingleSource<RespT> source;

    public SingleSource<RespT> singleSource() {
      return source;
    }

    public SingleResponseReceiver(ClientCall<?, RespT> call) {
      this.call = call;

      this.source = new SingleSource<RespT>() {
        @Override
        public void subscribe(SingleObserver<? super RespT> observer) {
          responseObserver = observer;

          // todo which disposable should be used here
          observer.onSubscribe(Disposables.disposed());

          // start call until response gets subscribed
          startCall();
        }
      };
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

    public void startCall() {
      call.request(2);
    }
  }

  private static class StreamRequestSender<ReqT> extends ClientCall.Listener<ReqT> implements StartCall {
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

    public Subscriber<ReqT> subscriber() {
      return grpcSubscriber;
    }

    @Override
    public void onReady() {
      logger.trace("onReady");
      grpcSubscriber.ready();
    }

    @Override
    public void startCall() {
      grpcSubscriber.requestMore();
    }
  }

  private static class StreamingResponseReceiver<RespT> extends ClientCall.Listener<RespT> implements StartCall {
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

      @Override
      public void subscribe(Subscriber<? super RespT> s) {
        super.subscribe(s);

        startCall();
      }
    };

    public StreamingResponseReceiver(ClientCall<?, RespT> call) {
      this.call = call;
    }

    public Publisher<RespT> publisher() {
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

    public void startCall() {
    }
  }
}
