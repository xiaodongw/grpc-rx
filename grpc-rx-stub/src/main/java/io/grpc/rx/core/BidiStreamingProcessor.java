package io.grpc.rx.core;

import org.reactivestreams.Processor;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

public abstract class BidiStreamingProcessor<REQ, RESP> implements Processor<REQ, RESP>, Subscription {
	private Subscription requestSubscription;
	private Subscriber<? super RESP> responseSubscriber;

	protected abstract RESP process(REQ req);

	@Override
	public void subscribe(Subscriber<? super RESP> s) {
		responseSubscriber = s;
		responseSubscriber.onSubscribe(this);
	}

	@Override
	public void onSubscribe(Subscription s) {
		requestSubscription = s;
	}

	@Override
	public void onNext(REQ req) {
		RESP resp = process(req);
		responseSubscriber.onNext(resp);
	}

	@Override
	public void onError(Throwable t) {
		responseSubscriber.onError(t);
	}

	@Override
	public void onComplete() {
		responseSubscriber.onComplete();
	}

	@Override
	public void request(long n) {
		requestSubscription.request(n);
	}

	@Override
	public void cancel() {
		requestSubscription.cancel();
	}
}
