package io.grpc.rx;

import io.reactivex.subscribers.TestSubscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

public class AutoTestSubscriber<T> extends TestSubscriber<T> {
  private Logger logger = LoggerFactory.getLogger(this.getClass());

  private AtomicInteger pendingMessages = new AtomicInteger();
  private int lowWatermark = 4;
  private int highWatermark = 8;

  public AutoTestSubscriber(int initialRequest) {
    super(initialRequest);
    pendingMessages.addAndGet(initialRequest);
  }

  @Override
  public void onNext(T t) {
    super.onNext(t);

    int pending = pendingMessages.decrementAndGet();
    logger.trace("pending={}", pending);
    if (pending <= lowWatermark) {
      int gap = highWatermark - pending;
      request(gap);
      pendingMessages.addAndGet(gap);
    }
  }
}
