package io.grpc.rx.stub;

import io.grpc.*;
import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Utility functions for adapting {@link ServerCallHandler}s to application service implementation,
 * meant to be used by the generated code.
 */
public final class ServerCallsRx {

	private ServerCallsRx() {
	}

	/**
	 * Creates a {@code ServerCallHandler} for a unary call method of the service.
	 *
	 * @param method an adaptor to the actual method on the service implementation.
	 */
	public static <ReqT, RespT> ServerCallHandler<ReqT, RespT> unaryCall(
		final UnaryMethod<ReqT, RespT> method) {
		return new UnaryServerCallHandler<ReqT, RespT>(method);
	}

	/**
	 * Creates a {@code ServerCallHandler} for a server streaming method of the service.
	 *
	 * @param method an adaptor to the actual method on the service implementation.
	 */
	public static <ReqT, RespT> ServerCallHandler<ReqT, RespT> serverStreamingCall(
		final ServerStreamingMethod<ReqT, RespT> method) {
		return new ServerStreamingServerCallHandler<ReqT, RespT>(method);
	}

	/**
	 * Creates a {@code ServerCallHandler} for a client streaming method of the service.
	 *
	 * @param method an adaptor to the actual method on the service implementation.
	 */
	public static <ReqT, RespT> ServerCallHandler<ReqT, RespT> clientStreamingCall(
		final ClientStreamingMethod<ReqT, RespT> method) {
		return new ClientStreamingServerCallHandler<ReqT, RespT>(method);
	}

	public static <ReqT, RespT> ServerCallHandler<ReqT, RespT> bidiStreamingCall(
		final BidiStreamingMethod<ReqT, RespT> method) {
		return new BidiStreamingServerCallHandler<ReqT, RespT>(method);
	}

	private static class ResponseObserver<RespT> implements SingleObserver<RespT> {
		private ServerCall<?, RespT> call;

		public ResponseObserver(ServerCall<?, RespT> call) {
			this.call = call;
		}

		@Override
		public void onSubscribe(Disposable d) {
		}

		@Override
		public void onSuccess(RespT value) {
			call.sendHeaders(new Metadata());
			call.sendMessage(value);
			call.close(Status.OK, new Metadata());
		}

		@Override
		public void onError(Throwable e) {
			call.close(Status.fromThrowable(e), new Metadata());
		}
	}

	private static class ResponseSubscriber<RespT> implements Subscriber<RespT> {
		private Subscription subscription;
		private ServerCall<?, RespT> call;
		private int lowWaterMark = 4;
		private int highWaterMark = 8;
		private AtomicInteger pendingResps = new AtomicInteger();

		public ResponseSubscriber(ServerCall<?, RespT> call) {
			this.call = call;
		}

		@Override
		public void onSubscribe(Subscription s) {
			subscription = s;

			// todo when is the best time to send headers
			call.sendHeaders(new Metadata());
		}

		@Override
		public void onNext(RespT message) {
			call.sendMessage(message);
			pendingResps.decrementAndGet();
		}

		@Override
		public void onError(Throwable t) {
			call.close(Status.fromThrowable(t), new Metadata());
		}

		@Override
		public void onComplete() {
			call.close(Status.OK, new Metadata());
		}

//		public Subscription getSubscription() {
//			return subscription;
//		}

		public void askResponses() {
			int p = pendingResps.get();
			if (p < lowWaterMark) {
				int want = highWaterMark - p;
				subscription.request(want);
				pendingResps.addAndGet(want);
			}
		}
	}

	private static abstract class SingleRequestListener<ReqT> extends ServerCall.Listener<ReqT> {
		private ServerCall<ReqT, ?> call;
		private ReqT request;

		public SingleRequestListener(ServerCall<ReqT, ?> call) {
			this.call = call;

			call.request(2);
		}

		@Override
		public void onMessage(ReqT message) {
			request = message;
		}

		@Override
		public void onHalfClose() {
			if (request != null) {
				invoke(request);

				if (call.isReady()) {
					// Since we are calling invoke in halfClose we have missed the askResponses
					// event from the transport so recover it here.
					onReady();
				}
			} else {
				call.close(Status.INTERNAL.withDescription("Half-closed without a request"), new Metadata());
			}
		}

		@Override
		public void onCancel() {
			//responseObserver.onError();
		}

		@Override
		public void onComplete() {
			//call.close(Status.OK, new Metadata());
		}

		@Override
		public void onReady() {
			//super.askResponses();
		}

		protected abstract void invoke(ReqT request);
	}

	private static class StreamRequestListener<ReqT> extends ServerCall.Listener<ReqT> {
		private ServerCall<ReqT, ?> call;
		private Subscriber<ReqT> requestSubscriber;
		private Subscription requestSubscription;

		public StreamRequestListener(ServerCall<ReqT, ?> call, Subscriber<ReqT> requestSubscriber) {
			this.call = call;
			this.requestSubscriber = requestSubscriber;

			requestSubscription = new Subscription() {
				@Override
				public void request(long n) {
					call.request((int) n);
				}

				@Override
				public void cancel() {
					call.close(Status.CANCELLED, new Metadata());
				}
			};
			requestSubscriber.onSubscribe(requestSubscription);
		}


		@Override
		public void onMessage(ReqT message) {
			requestSubscriber.onNext(message);
		}

		@Override
		public void onHalfClose() {
			requestSubscriber.onComplete();
		}

		@Override
		public void onCancel() {
			requestSubscriber.onError(new CancellationException("cancelled from grpc"));
		}

		@Override
		public void onComplete() {

		}

		@Override
		public void onReady() {

		}
	}

	public static class UnaryServerCallHandler<ReqT, RespT> implements ServerCallHandler<ReqT, RespT> {
		private UnaryMethod<ReqT, RespT> method;

		public UnaryServerCallHandler(UnaryMethod<ReqT, RespT> method) {
			this.method = method;
		}

		@Override
		public ServerCall.Listener<ReqT> startCall(ServerCall<ReqT, RespT> call, Metadata headers) {
			SingleObserver<RespT>responseObserver = new ResponseObserver<RespT>(call);

			return new SingleRequestListener<ReqT>(call) {
				@Override
				protected void invoke(ReqT request) {
					method.invoke(request, responseObserver);
				}
			};
		}
	}

	public static class ServerStreamingServerCallHandler<ReqT, RespT> implements ServerCallHandler<ReqT, RespT> {
		private ServerStreamingMethod<ReqT, RespT> method;

		public ServerStreamingServerCallHandler(ServerStreamingMethod<ReqT, RespT> method) {
			this.method = method;
		}

		@Override
		public ServerCall.Listener<ReqT> startCall(ServerCall<ReqT, RespT> call, Metadata headers) {
			ResponseSubscriber<RespT> responseSubscriber = new ResponseSubscriber<RespT>(call);

			return new SingleRequestListener<ReqT>(call) {
				@Override
				protected void invoke(ReqT request) {
					method.invoke(request, responseSubscriber);
				}

				@Override
				public void onReady() {
					responseSubscriber.askResponses();
				}
			};
		}
	}

	public static class ClientStreamingServerCallHandler<ReqT, RespT> implements ServerCallHandler<ReqT, RespT> {
		private ClientStreamingMethod<ReqT, RespT> method;

		public ClientStreamingServerCallHandler(ClientStreamingMethod<ReqT, RespT> method) {
			this.method = method;
		}

		@Override
		public ServerCall.Listener<ReqT> startCall(ServerCall<ReqT, RespT> call, Metadata headers) {
			SingleObserver<RespT> responseObserver = new ResponseObserver<RespT>(call);
			Subscriber<ReqT> requestSubscriber = method.invoke(responseObserver);
			return new StreamRequestListener<ReqT>(call, requestSubscriber);
		}
	}

	public static class BidiStreamingServerCallHandler<ReqT, RespT> implements ServerCallHandler<ReqT, RespT> {
		private BidiStreamingMethod<ReqT, RespT> method;

		public BidiStreamingServerCallHandler(BidiStreamingMethod<ReqT, RespT> method) {
			this.method = method;
		}

		@Override
		public ServerCall.Listener<ReqT> startCall(ServerCall<ReqT, RespT> call, Metadata headers) {
			ResponseSubscriber<RespT> responseSubscriber = new ResponseSubscriber<RespT>(call);
			Subscriber<ReqT>requestSubscriber = method.invoke(responseSubscriber);
			return new StreamRequestListener<ReqT>(call, requestSubscriber) {
				@Override
				public void onReady() {
					responseSubscriber.askResponses();
				}
			};
		}
	}

	/**
	 * Adaptor to a unary call method.
	 */
	public static interface UnaryMethod<ReqT, RespT> {
		void invoke(ReqT request, SingleObserver<RespT> responseObserver);
	}

	/**
	 * Adaptor to a server streaming method.
	 */
	public static interface ServerStreamingMethod<ReqT, RespT> {
		void invoke(ReqT request, Subscriber<RespT> resposneSubscriber);
	}

	/**
	 * Adaptor to a client streaming method.
	 */
	public static interface ClientStreamingMethod<ReqT, RespT> {
		Subscriber<ReqT> invoke(SingleObserver<RespT> responseObserver);
	}

	/**
	 * Adaptor to a bi-directional streaming method.
	 */
	public static interface BidiStreamingMethod<ReqT, RespT> {
		Subscriber<ReqT> invoke(Subscriber<RespT> resposneSubscriber);
	}

	public static <T> void unimplementedUnaryCall(
		MethodDescriptor<?, ?> methodDescriptor,
		SingleObserver<T> responseObserver) {
		checkNotNull(methodDescriptor);
		checkNotNull(responseObserver);
		responseObserver.onError(Status.UNIMPLEMENTED
			.withDescription(String.format("Method %s is unimplemented",
				methodDescriptor.getFullMethodName()))
			.asException());
	}

	public static <T> void unimplementedServerStreamingCall(
		MethodDescriptor<?, ?> methodDescriptor,
		Subscriber<T> responseSubscriber) {
		checkNotNull(methodDescriptor);
		checkNotNull(responseSubscriber);
		responseSubscriber.onError(Status.UNIMPLEMENTED
			.withDescription(String.format("Method %s is unimplemented",
				methodDescriptor.getFullMethodName()))
			.asException());
	}

	public static <REQ, RESP> Subscriber<REQ> unimplementedClientStreamingCall(
		MethodDescriptor<?, ?> methodDescriptor,
		SingleObserver<RESP> responseObserver) {
		unimplementedUnaryCall(methodDescriptor, responseObserver);
		return new NoopSubscriber<REQ>();
	}

	public static <REQ, RESP> Subscriber<REQ> unimplementedBidiStreamingCall(
		MethodDescriptor<?, ?> methodDescriptor,
		Subscriber<RESP> responseSubscriber) {
		unimplementedServerStreamingCall(methodDescriptor, responseSubscriber);
		return new NoopSubscriber<REQ>();
	}

	static class NoopSubscriber<V> implements Subscriber<V> {
		@Override
		public void onSubscribe(Subscription s) {
		}

		@Override
		public void onNext(V v) {
		}

		@Override
		public void onError(Throwable t) {
		}

		@Override
		public void onComplete() {
		}
	}
}
