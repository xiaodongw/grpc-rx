package io.grpc.rx;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 */
@javax.annotation.Generated(
	value = "by gRPC proto compiler (version 1.0.1)",
	comments = "Source: EchoService.proto")
public class EchoGrpcRx {

	private EchoGrpcRx() {
	}

	public static final String SERVICE_NAME = "io.grpc.reactivex.Echo";

	// Static method descriptors that strictly reflect the proto.
	@io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
	public static final io.grpc.MethodDescriptor<io.grpc.rx.EchoService.EchoReq,
		io.grpc.rx.EchoService.EchoResp> METHOD_UNARY =
		io.grpc.MethodDescriptor.create(
			io.grpc.MethodDescriptor.MethodType.UNARY,
			generateFullMethodName(
				"io.grpc.reactivex.Echo", "unary"),
			io.grpc.protobuf.ProtoUtils.marshaller(io.grpc.rx.EchoService.EchoReq.getDefaultInstance()),
			io.grpc.protobuf.ProtoUtils.marshaller(io.grpc.rx.EchoService.EchoResp.getDefaultInstance()));
	@io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
	public static final io.grpc.MethodDescriptor<io.grpc.rx.EchoService.EchoCountReq,
		io.grpc.rx.EchoService.EchoResp> METHOD_SERVER_STREAMING =
		io.grpc.MethodDescriptor.create(
			io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING,
			generateFullMethodName(
				"io.grpc.reactivex.Echo", "serverStreaming"),
			io.grpc.protobuf.ProtoUtils.marshaller(io.grpc.rx.EchoService.EchoCountReq.getDefaultInstance()),
			io.grpc.protobuf.ProtoUtils.marshaller(io.grpc.rx.EchoService.EchoResp.getDefaultInstance()));
	@io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
	public static final io.grpc.MethodDescriptor<io.grpc.rx.EchoService.EchoReq,
		io.grpc.rx.EchoService.EchoCountResp> METHOD_CLIENT_STREAMING =
		io.grpc.MethodDescriptor.create(
			io.grpc.MethodDescriptor.MethodType.CLIENT_STREAMING,
			generateFullMethodName(
				"io.grpc.reactivex.Echo", "clientStreaming"),
			io.grpc.protobuf.ProtoUtils.marshaller(io.grpc.rx.EchoService.EchoReq.getDefaultInstance()),
			io.grpc.protobuf.ProtoUtils.marshaller(io.grpc.rx.EchoService.EchoCountResp.getDefaultInstance()));
	@io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
	public static final io.grpc.MethodDescriptor<io.grpc.rx.EchoService.EchoReq,
		io.grpc.rx.EchoService.EchoResp> METHOD_BIDI_STREAMING =
		io.grpc.MethodDescriptor.create(
			io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING,
			generateFullMethodName(
				"io.grpc.reactivex.Echo", "bidiStreaming"),
			io.grpc.protobuf.ProtoUtils.marshaller(io.grpc.rx.EchoService.EchoReq.getDefaultInstance()),
			io.grpc.protobuf.ProtoUtils.marshaller(io.grpc.rx.EchoService.EchoResp.getDefaultInstance()));

	/**
	 * Creates a new ReactiveX stub that supports all call types for the service
	 */
	public static EchoStub newStub(io.grpc.Channel channel) {
		return new EchoStub(channel);
	}

	/**
	 */
	public static abstract class EchoImplBase implements io.grpc.BindableService {

		/**
		 */
		public void unary(io.grpc.rx.EchoService.EchoReq request,
						  io.reactivex.SingleObserver<EchoService.EchoResp> responseObserver) {
			io.grpc.rx.stub.ServerCallsRx.unimplementedUnaryCall(METHOD_UNARY, responseObserver);
		}

		/**
		 */
		public void serverStreaming(io.grpc.rx.EchoService.EchoCountReq request,
									org.reactivestreams.Subscriber<io.grpc.rx.EchoService.EchoResp> responseSubscriber) {
			io.grpc.rx.stub.ServerCallsRx.unimplementedServerStreamingCall(METHOD_SERVER_STREAMING, responseSubscriber);
		}

		/**
		 */
		public org.reactivestreams.Subscriber<io.grpc.rx.EchoService.EchoReq> clientStreaming(
			io.reactivex.SingleObserver<io.grpc.rx.EchoService.EchoCountResp> responseObserver) {
			return io.grpc.rx.stub.ServerCallsRx.unimplementedClientStreamingCall(METHOD_CLIENT_STREAMING, responseObserver);
		}

		/**
		 */
		public org.reactivestreams.Subscriber<io.grpc.rx.EchoService.EchoReq> bidiStreaming(
			org.reactivestreams.Subscriber<io.grpc.rx.EchoService.EchoResp> responseSubscriber) {
			return io.grpc.rx.stub.ServerCallsRx.unimplementedBidiStreamingCall(METHOD_BIDI_STREAMING, responseSubscriber);
		}

		@java.lang.Override
		public io.grpc.ServerServiceDefinition bindService() {
			return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
				.addMethod(
					METHOD_UNARY,
					io.grpc.rx.stub.ServerCallsRx.unaryCall(
						new MethodHandlers<
							io.grpc.rx.EchoService.EchoReq,
							io.grpc.rx.EchoService.EchoResp>(
							this, METHODID_UNARY)))
				.addMethod(
					METHOD_SERVER_STREAMING,
					io.grpc.rx.stub.ServerCallsRx.serverStreamingCall(
						new MethodHandlers<
							io.grpc.rx.EchoService.EchoCountReq,
							io.grpc.rx.EchoService.EchoResp>(
							this, METHODID_SERVER_STREAMING)))
				.addMethod(
					METHOD_CLIENT_STREAMING,
					io.grpc.rx.stub.ServerCallsRx.clientStreamingCall(
						new MethodHandlers<
							io.grpc.rx.EchoService.EchoReq,
							io.grpc.rx.EchoService.EchoCountResp>(
							this, METHODID_CLIENT_STREAMING)))
				.addMethod(
					METHOD_BIDI_STREAMING,
					io.grpc.rx.stub.ServerCallsRx.bidiStreamingCall(
						new MethodHandlers<
							io.grpc.rx.EchoService.EchoReq,
							io.grpc.rx.EchoService.EchoResp>(
							this, METHODID_BIDI_STREAMING)))
				.build();
		}
	}

	public static final class EchoStub extends io.grpc.stub.AbstractStub<EchoStub> {
		private EchoStub(io.grpc.Channel channel) {
			super(channel);
		}

		private EchoStub(io.grpc.Channel channel,
						 io.grpc.CallOptions callOptions) {
			super(channel, callOptions);
		}

		@java.lang.Override
		protected EchoStub build(io.grpc.Channel channel,
								 io.grpc.CallOptions callOptions) {
			return new EchoStub(channel, callOptions);
		}

		/**
		 */
		public void unary(io.grpc.rx.EchoService.EchoReq request,
						  io.reactivex.SingleObserver<io.grpc.rx.EchoService.EchoResp> responseObserver) {
			io.grpc.rx.stub.ClientCallsRx.rxUnaryCall(
				getChannel().newCall(METHOD_UNARY, getCallOptions()), request, responseObserver);
		}

		/**
		 */
		public void serverStreaming(io.grpc.rx.EchoService.EchoCountReq request,
									org.reactivestreams.Subscriber<EchoService.EchoResp> responseSubscriber) {
			io.grpc.rx.stub.ClientCallsRx.rxServerStreamingCall(
				getChannel().newCall(METHOD_SERVER_STREAMING, getCallOptions()), request, responseSubscriber);
		}

		/**
		 */
		public org.reactivestreams.Subscriber<io.grpc.rx.EchoService.EchoReq> clientStreaming(
			io.reactivex.SingleObserver<io.grpc.rx.EchoService.EchoCountResp> responseObserver) {
			return io.grpc.rx.stub.ClientCallsRx.rxClientStreamingCall(
				getChannel().newCall(METHOD_CLIENT_STREAMING, getCallOptions()), responseObserver);
		}

		/**
		 */
		public org.reactivestreams.Subscriber<io.grpc.rx.EchoService.EchoReq> bidiStreaming(
			org.reactivestreams.Subscriber<EchoService.EchoResp> responseSubscriber) {
			return io.grpc.rx.stub.ClientCallsRx.rxBidiStreamingCall(
				getChannel().newCall(METHOD_BIDI_STREAMING, getCallOptions()), responseSubscriber);
		}
	}

	private static final int METHODID_UNARY = 0;
	private static final int METHODID_SERVER_STREAMING = 1;
	private static final int METHODID_CLIENT_STREAMING = 2;
	private static final int METHODID_BIDI_STREAMING = 3;

	private static class MethodHandlers<Req, Resp> implements
		io.grpc.rx.stub.ServerCallsRx.UnaryMethod<Req, Resp>,
		io.grpc.rx.stub.ServerCallsRx.ServerStreamingMethod<Req, Resp>,
		io.grpc.rx.stub.ServerCallsRx.ClientStreamingMethod<Req, Resp>,
		io.grpc.rx.stub.ServerCallsRx.BidiStreamingMethod<Req, Resp> {
		private final EchoImplBase serviceImpl;
		private final int methodId;

		public MethodHandlers(EchoImplBase serviceImpl, int methodId) {
			this.serviceImpl = serviceImpl;
			this.methodId = methodId;
		}

		@java.lang.Override
		@java.lang.SuppressWarnings("unchecked")
		public void invoke(Req request, io.reactivex.SingleObserver<Resp> responseObserver) {
			switch (methodId) {
				case METHODID_UNARY:
					serviceImpl.unary((io.grpc.rx.EchoService.EchoReq) request,
						(io.reactivex.SingleObserver<io.grpc.rx.EchoService.EchoResp>) responseObserver);
					break;
				default:
					throw new AssertionError();
			}
		}

		@Override
		public void invoke(Req request, org.reactivestreams.Subscriber<Resp> resposneSubscriber) {
			switch (methodId) {
				case METHODID_SERVER_STREAMING:
					serviceImpl.serverStreaming((io.grpc.rx.EchoService.EchoCountReq) request,
						(org.reactivestreams.Subscriber<io.grpc.rx.EchoService.EchoResp>) resposneSubscriber);
					break;
				default:
					throw new AssertionError();
			}
		}

		@Override
		public org.reactivestreams.Subscriber<Req> invoke(io.reactivex.SingleObserver<Resp> responseObserver) {
			switch (methodId) {
				case METHODID_CLIENT_STREAMING:
					return (org.reactivestreams.Subscriber<Req>) serviceImpl.clientStreaming(
						(io.reactivex.SingleObserver<io.grpc.rx.EchoService.EchoCountResp>) responseObserver);
				default:
					throw new AssertionError();
			}
		}

		@Override
		public org.reactivestreams.Subscriber<Req> invoke(
			org.reactivestreams.Subscriber<Resp> resposneSubscriber) {
			switch (methodId) {
				case METHODID_BIDI_STREAMING:
					return (org.reactivestreams.Subscriber<Req>) serviceImpl.bidiStreaming(
						(org.reactivestreams.Subscriber<io.grpc.rx.EchoService.EchoResp>) resposneSubscriber);
				default:
					throw new AssertionError();
			}
		}
	}

	public static io.grpc.ServiceDescriptor getServiceDescriptor() {
		return new io.grpc.ServiceDescriptor(SERVICE_NAME,
			METHOD_UNARY,
			METHOD_SERVER_STREAMING,
			METHOD_CLIENT_STREAMING,
			METHOD_BIDI_STREAMING);
	}

}
