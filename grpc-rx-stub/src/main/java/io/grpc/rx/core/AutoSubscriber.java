package io.grpc.rx.core;

import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Auto pull messages based on watermark settings.
 */
public abstract class AutoSubscriber<T> implements Subscriber<T> {
    private int lowWatermark = 4;
    private int highWatermark = 8;
    private AtomicInteger pending = new AtomicInteger();

    private Subscription subscription;

    public AutoSubscriber(int lowWatermark, int highWatermark) {
        this.lowWatermark = lowWatermark;
        this.highWatermark = highWatermark;
    }

    public AutoSubscriber() {
        this(4, 8);
    }

    @Override
    public void onSubscribe(Subscription s) {
        subscription = s;
        requestMore();
    }

    @Override
    public void onNext(T t) {
        /*processRequest(t, new SingleObserver<Void>() {
            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onSuccess(Void v) {
                pending.decrementAndGet();
                requestMore();
            }

            @Override
            public void onError(Throwable e) {
                cancelSubscription();
            }
        });*/
        processRequest(t);
        pending.decrementAndGet();
        requestMore();
    }

    protected void requestMore() {
        if (subscription == null) return;

        int p = pending.get();
        if (p <= lowWatermark) {
            int gap = highWatermark - p;
            subscription.request(gap);
            pending.addAndGet(gap);
        }
    }

    protected void cancelSubscription() {
        if (subscription == null) return;
        subscription.cancel();
    }

    //protected abstract void processRequest(T req, SingleObserver<Void> resultObserver);
    protected abstract void processRequest(T req);
}
