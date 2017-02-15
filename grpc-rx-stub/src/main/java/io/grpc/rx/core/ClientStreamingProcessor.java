package io.grpc.rx.core;

import io.reactivex.SingleObserver;
import io.reactivex.SingleSource;

public abstract class ClientStreamingProcessor<REQ, RESP> extends AutoSubscriber<REQ> implements SingleSource<RESP> {
  private SingleObserver<? super RESP> responseObserver;

  public ClientStreamingProcessor(int lowWatermark, int highWatermark) {
    super(lowWatermark, highWatermark);
  }

  public ClientStreamingProcessor() {
    super();
  }

  @Override
  public void onError(Throwable t) {
    cancelSubscription();
    responseObserver.onError(t);
  }

  @Override
  public void onComplete() {
    RESP resp = generateResponse();
    responseObserver.onSuccess(resp);
  }

  @Override
  public void subscribe(SingleObserver<? super RESP> observer) {
    responseObserver = observer;
  }

  protected abstract RESP generateResponse();
}
