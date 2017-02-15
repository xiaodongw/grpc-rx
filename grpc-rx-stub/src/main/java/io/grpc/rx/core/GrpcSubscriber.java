package io.grpc.rx.core;

/**
 * A subscriber sends messages to GRPC
 */
public abstract class GrpcSubscriber<T> extends AutoSubscriber<T> {

  @Override
  protected void processRequest(T req) {
    sendMessage(req);
  }

  @Override
  protected void requestMore() {
    if (isReady()) {
      super.requestMore();
    }
  }

  @Override
  public void onError(Throwable t) {
    error(t);
  }

  @Override
  public void onComplete() {
    complete();
  }

  public void ready() {
    requestMore();
  }

  // to be implemented by GRPC
  protected abstract boolean isReady();

  protected abstract void sendMessage(T req);

  protected abstract void error(Throwable t);

  protected abstract void complete();
}
