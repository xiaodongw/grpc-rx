package io.grpc.rx.stub;

import io.grpc.ClientCall;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import io.reactivex.Flowable;
import io.reactivex.SingleObserver;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public final class ClientCallsRx {
	private ClientCallsRx() {
	}

	/**
	 * Executes a unary call with a response {@link SingleObserver}.
	 */
	public static <ReqT, RespT> void rxUnaryCall(
		ClientCall<ReqT, RespT> call,
		ReqT request,
		SingleObserver<RespT> responseObserver) {
		SingleRequestAdapter<ReqT> requestAdapter = new SingleRequestAdapter<>(call, request);
		SingleResponseAdapter<RespT> responseAdapter = new SingleResponseAdapter<>(call, responseObserver);
		ClientCallProxy<ReqT, RespT> proxy = new ClientCallProxy(requestAdapter, responseAdapter);
		startCall(call, proxy);
	}

	/**
	 * Executes a server-streaming call with a response {@link Subscriber}.
	 */
	public static <ReqT, RespT> void rxServerStreamingCall(
		ClientCall<ReqT, RespT> call,
		ReqT request,
		Subscriber<RespT> responseSubscriber) {
		SingleRequestAdapter<ReqT> requestAdapter = new SingleRequestAdapter<ReqT>(call, request);
		StreamResponseAdapter<RespT> responseAdapter = new StreamResponseAdapter<RespT>(call);
		ClientCallProxy<ReqT, RespT> proxy = new ClientCallProxy(requestAdapter, responseAdapter);
		startCall(call, proxy);

		responseAdapter.subscribe(responseSubscriber);
	}

	/**
	 * Executes a client-streaming call returning a {@link StreamObserver} for the request messages.
	 *
	 * @return request stream observer.
	 */
	public static <ReqT, RespT> Subscriber<ReqT> rxClientStreamingCall(
		ClientCall<ReqT, RespT> call,
		SingleObserver<RespT> responseObserver) {
		StreamRequestAdapter<ReqT> requestAdapter = new StreamRequestAdapter<>(call);
		SingleResponseAdapter<RespT> responseAdapter = new SingleResponseAdapter<>(call, responseObserver);
		ClientCallProxy<ReqT, RespT> proxy = new ClientCallProxy(
			requestAdapter,
			responseAdapter);
		startCall(call, proxy);
		return requestAdapter;
	}

	/**
	 * Executes a bidi-streaming call.
	 *
	 * @return request stream observer.
	 */
	public static <ReqT, RespT> Subscriber<ReqT> rxBidiStreamingCall(
		ClientCall<ReqT, RespT> call,
		Subscriber<RespT> responseSubscriber) {
		StreamRequestAdapter<ReqT> requestAdapter = new StreamRequestAdapter<>(call);
		StreamResponseAdapter<RespT> responseAdapter = new StreamResponseAdapter<>(call);
		ClientCallProxy<ReqT, RespT> proxy = new ClientCallProxy(requestAdapter, responseAdapter);
		startCall(call, proxy);

		responseAdapter.subscribe(responseSubscriber);
		return requestAdapter;
	}

	private static <ReqT, RespT> void startCall(ClientCall<ReqT, RespT> call,
												ClientCallProxy<ReqT, RespT> proxy) {
		call.start(proxy, new Metadata());
		proxy.onStart();
	}

	private static abstract class ClientCallAdapter<RespT> extends ClientCall.Listener<RespT> {
		public void onStart() {
		}
	}

	private static class ClientCallProxy<ReqT, RespT> extends ClientCallAdapter<RespT> {
		private ClientCallAdapter<RespT> requestAdapter;
		private ClientCallAdapter<RespT> responseAdapter;

		public ClientCallProxy(ClientCallAdapter<RespT> requestAdapter, ClientCallAdapter<RespT> responseAdapter) {
			this.requestAdapter = requestAdapter;
			this.responseAdapter = responseAdapter;
		}

		@Override
		public void onStart() {
			requestAdapter.onStart();
			responseAdapter.onStart();
		}

		@Override
		public void onHeaders(Metadata headers) {
			//requestAdapter.onHeaders(headers);
			responseAdapter.onHeaders(headers);
		}

		@Override
		public void onMessage(RespT message) {
			//requestAdapter.onMessage(message);
			responseAdapter.onMessage(message);
		}

		@Override
		public void onClose(Status status, Metadata trailers) {
			//requestAdapter.onClose(status, trailers);
			responseAdapter.onClose(status, trailers);
		}

		@Override
		public void onReady() {
			requestAdapter.onReady();
			//responseAdapter.askResponses();
		}
	}

	private static class SingleResponseAdapter<RespT> extends ClientCallAdapter<RespT> {
		private ClientCall<?, RespT> call;
		private SingleObserver<RespT> responseObserver;
		private RespT response;

		public SingleResponseAdapter(ClientCall<?, RespT> call, SingleObserver<RespT> responseObserver) {
			this.call = call;
			this.responseObserver = responseObserver;
		}

		@Override
		public void onStart() {
			call.request(2);
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
	}

	private static class StreamResponseAdapter<RespT> extends ClientCallAdapter<RespT> implements Publisher<RespT> {
		private final ClientCall<?, RespT> call;
		private AtomicReference<Subscriber> subscriber = new AtomicReference<>();

		@Override
		public void subscribe(Subscriber<? super RespT> s) {
			if (!subscriber.compareAndSet(null, s)) {
				throw new IllegalStateException("Already has one subscriber and does not support more than one");
			}

			Subscription subscription = new Subscription() {
				@Override
				public void request(long n) {
					// todo handle conversion overflow
					call.request((int) n);
				}

				@Override
				public void cancel() {
					call.cancel("Canceled by subscriber", null);
				}
			};
			s.onSubscribe(subscription);
		}

		public StreamResponseAdapter(ClientCall<?, RespT> call) {
			this.call = call;
		}

		@Override
		public void onMessage(RespT message) {
			subscriber.get().onNext(message);
		}

		@Override
		public void onClose(Status status, Metadata trailers) {
			if (status.isOk()) {
				subscriber.get().onComplete();

			} else {
				subscriber.get().onError(status.asRuntimeException(trailers));
			}
		}
	}

	private static class SingleRequestAdapter<ReqT> extends ClientCallAdapter<ReqT> {
		private ClientCall<ReqT, ?> call;
		private ReqT request;

		public SingleRequestAdapter(ClientCall<ReqT, ?> call, ReqT request) {
			this.call = call;
			this.request = request;
		}

		@Override
		public void onStart() {
			call.sendMessage(request);
			call.halfClose();
		}
	}

	private static class StreamRequestAdapter<ReqT> extends ClientCallAdapter<ReqT> implements Subscriber<ReqT> {
		private final ClientCall<ReqT, ?> call;

		private int lowWatermark = 4;
		private int highWatermark = 8;

		private Subscription subscription;
		private final AtomicInteger pendingRequest = new AtomicInteger();

		public StreamRequestAdapter(ClientCall<ReqT, ?> call) {
			this.call = call;
		}

		@Override
		public void onReady() {
			pullRequests();
		}

		@Override
		public void onSubscribe(Subscription s) {
			subscription = s;
			pullRequests();
		}

		@Override
		public void onNext(ReqT request) {
			call.sendMessage(request);
			pendingRequest.decrementAndGet();
		}

		@Override
		public void onError(Throwable t) {
			call.cancel("Error from request publisher", t);
		}

		@Override
		public void onComplete() {
			call.halfClose();
		}

		private void pullRequests() {
			int pending = pendingRequest.get();
			if (pending <= lowWatermark) {
				int desired = highWatermark - pending;
				subscription.request(desired);
				pendingRequest.addAndGet(desired);
			}
		}
	}
}
