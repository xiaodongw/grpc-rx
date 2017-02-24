package io.grpc.rx.core;

/**
 * A subscriber takes messages and sends them to GRPC.
 * The interface is general so it can be used by ClientCall and ServerCall.
 */
public abstract class GrpcSubscriber<T> extends AutoSubscriber<T> {
  public GrpcSubscriber(int lowWatermark, int highWatermark) {
    super(lowWatermark, highWatermark);
  }

  public GrpcSubscriber() {
  }

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

  // Call this in ClientCall.Listener.onReady / ServerCall.Listener.onReady
  public void ready() {
    requestMore();
  }

  // To be implemented by GRPC, delegate to ClientCall.isReady() / ServerCall.isReady()
  protected abstract boolean isReady();

  // To be implemented by GRPC, delegate to ClientCall.sendMessage() / ServerCall.sendMessage()
  protected abstract void sendMessage(T req);

  // To be implemented by GRPC, delegate to ClientCall.cancel() / ServerCall.cancel()
  protected abstract void error(Throwable t);

  // To be implemented by GRPC
  // For ClientCall requests, map to ClientCall.halfClose()
  // For ServerCall responses, map to ServerCall.close()
  protected abstract void complete();
}
