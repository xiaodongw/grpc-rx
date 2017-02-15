package io.grpc.rx.core;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A publisher receives message from GRPC, then publishes them to the subscriber
 */
public abstract class GrpcPublisher<T> implements Publisher<T> {
  private Logger logger = LoggerFactory.getLogger(this.getClass());
  private Subscriber subscriber = null;

  @Override
  public void subscribe(Subscriber<? super T> s) {
    if (subscriber != null) {
      throw new IllegalStateException("Already has one subscriber and does not support more than one");
    }

    subscriber = s;
    Subscription subscription = new Subscription() {
      @Override
      public void request(long n) {
        logger.trace("subscription.requestMore: n={}", n);
        // todo handle conversion overflow
        requestMore(n);
      }

      @Override
      public void cancel() {
        logger.trace("subscription.cancel");
        cancelSubscription("Canceled by subscriber", null);
      }
    };

    subscriber.onSubscribe(subscription);
  }

  public void message(T msg) {
    subscriber.onNext(msg);
  }

  public void complete() {
    subscriber.onComplete();
  }

  public void error(Throwable t) {
    subscriber.onError(t);
  }

  protected abstract void requestMore(long n);

  protected abstract void cancelSubscription(String message, Throwable cause);
}
