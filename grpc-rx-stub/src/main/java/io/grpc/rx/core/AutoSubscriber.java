package io.grpc.rx.core;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Auto pull messages based on watermark settings.
 */
public abstract class AutoSubscriber<T> implements Subscriber<T> {
  static private Logger logger = LoggerFactory.getLogger(AutoSubscriber.class);

  private int lowWatermark = 4;
  private int highWatermark = 32;
  private AtomicInteger pending = new AtomicInteger();

  private Subscription subscription;

  public AutoSubscriber(int lowWatermark, int highWatermark) {
    this.lowWatermark = lowWatermark;
    this.highWatermark = highWatermark;
  }

  public AutoSubscriber() {
    this(4, 32);
  }

  @Override
  public void onSubscribe(Subscription s) {
    subscription = s;
  }

  @Override
  public void onNext(T message) {
    if (logger.isTraceEnabled()) {
      logger.trace("onNext message={}", LogUtils.objectString(message));
    }
    processRequest(message);
    pending.decrementAndGet();

    requestMore();
  }

  protected void requestMore() {
    if (subscription == null) return;

    int p = pending.get();
    if (p <= lowWatermark) {
      int gap = highWatermark - p;

      if (logger.isTraceEnabled()) {
        logger.trace("subscription.request gap={}", gap);
      }
      pending.addAndGet(gap);
      subscription.request(gap);
    }
  }

  protected void cancelSubscription() {
    if (subscription == null) return;
    subscription.cancel();
  }

  //protected abstract void processRequest(T req, SingleObserver<Void> resultObserver);
  protected abstract void processRequest(T req);
}
