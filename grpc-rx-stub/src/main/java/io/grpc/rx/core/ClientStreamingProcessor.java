package io.grpc.rx.core;

import io.reactivex.SingleObserver;
import io.reactivex.SingleSource;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.concurrent.atomic.AtomicLong;

public abstract class ClientStreamingProcessor<REQ, RESP> extends AtomicLong implements Subscriber<REQ>, SingleSource<RESP> {
	private int batchSize;
	private int waterMark;

	private Subscription subscription;
	private SingleObserver<? super RESP> responseObserver;

	public ClientStreamingProcessor(int batchSize, int bufferSize) {
		this.batchSize = batchSize;
		waterMark = bufferSize - batchSize;
	}

	public ClientStreamingProcessor() {
		this(4, 8);
	}

	@Override
	public void onSubscribe(Subscription s) {
		this.subscription = s;

		request();
	}

	@Override
	public void onNext(REQ req) {
		processRequest(req);

		long pending = decrementAndGet();
		if (pending < waterMark) {
			request();
		}
	}

	@Override
	public void onError(Throwable t) {
		responseObserver.onError(t);
	}

	@Override
	public void onComplete() {
		RESP resp = generateResponse();
		responseObserver.onSuccess(resp);
	}

	private void request() {
		subscription.request(batchSize);
		addAndGet(batchSize);
	}

	@Override
	public void subscribe(SingleObserver<? super RESP> observer) {
		responseObserver = observer;
		//responseObserver.onSubscribe();
	}

	protected abstract RESP generateResponse();
	protected abstract void processRequest(REQ req);
}
