package io.grpc.rx;

import static io.grpc.stub.ClientCalls.asyncUnaryCall;
import static io.grpc.stub.ClientCalls.asyncServerStreamingCall;
import static io.grpc.stub.ClientCalls.asyncClientStreamingCall;
import static io.grpc.stub.ClientCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ClientCalls.blockingUnaryCall;
import static io.grpc.stub.ClientCalls.blockingServerStreamingCall;
import static io.grpc.stub.ClientCalls.futureUnaryCall;
import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncServerStreamingCall;
import static io.grpc.stub.ServerCalls.asyncClientStreamingCall;
import static io.grpc.stub.ServerCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.0.1)",
    comments = "Source: EchoService.proto")
public class EchoGrpc {

  private EchoGrpc() {}

  public static final String SERVICE_NAME = "io.grpc.rx.Echo";

  // Static method descriptors that strictly reflect the proto.
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<io.grpc.rx.EchoService.EchoReq,
      io.grpc.rx.EchoService.EchoResp> METHOD_UNARY =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "io.grpc.rx.Echo", "unary"),
          io.grpc.protobuf.ProtoUtils.marshaller(io.grpc.rx.EchoService.EchoReq.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(io.grpc.rx.EchoService.EchoResp.getDefaultInstance()));
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<io.grpc.rx.EchoService.EchoCountReq,
      io.grpc.rx.EchoService.EchoResp> METHOD_SERVER_STREAMING =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING,
          generateFullMethodName(
              "io.grpc.rx.Echo", "serverStreaming"),
          io.grpc.protobuf.ProtoUtils.marshaller(io.grpc.rx.EchoService.EchoCountReq.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(io.grpc.rx.EchoService.EchoResp.getDefaultInstance()));
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<io.grpc.rx.EchoService.EchoReq,
      io.grpc.rx.EchoService.EchoCountResp> METHOD_CLIENT_STREAMING =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.CLIENT_STREAMING,
          generateFullMethodName(
              "io.grpc.rx.Echo", "clientStreaming"),
          io.grpc.protobuf.ProtoUtils.marshaller(io.grpc.rx.EchoService.EchoReq.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(io.grpc.rx.EchoService.EchoCountResp.getDefaultInstance()));
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<io.grpc.rx.EchoService.EchoReq,
      io.grpc.rx.EchoService.EchoResp> METHOD_BIDI_STREAMING =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING,
          generateFullMethodName(
              "io.grpc.rx.Echo", "bidiStreaming"),
          io.grpc.protobuf.ProtoUtils.marshaller(io.grpc.rx.EchoService.EchoReq.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(io.grpc.rx.EchoService.EchoResp.getDefaultInstance()));

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static EchoStub newStub(io.grpc.Channel channel) {
    return new EchoStub(channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static EchoBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    return new EchoBlockingStub(channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary and streaming output calls on the service
   */
  public static EchoFutureStub newFutureStub(
      io.grpc.Channel channel) {
    return new EchoFutureStub(channel);
  }

  /**
   */
  public static abstract class EchoImplBase implements io.grpc.BindableService {

    /**
     */
    public void unary(io.grpc.rx.EchoService.EchoReq request,
        io.grpc.stub.StreamObserver<io.grpc.rx.EchoService.EchoResp> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_UNARY, responseObserver);
    }

    /**
     */
    public void serverStreaming(io.grpc.rx.EchoService.EchoCountReq request,
        io.grpc.stub.StreamObserver<io.grpc.rx.EchoService.EchoResp> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_SERVER_STREAMING, responseObserver);
    }

    /**
     */
    public io.grpc.stub.StreamObserver<io.grpc.rx.EchoService.EchoReq> clientStreaming(
        io.grpc.stub.StreamObserver<io.grpc.rx.EchoService.EchoCountResp> responseObserver) {
      return asyncUnimplementedStreamingCall(METHOD_CLIENT_STREAMING, responseObserver);
    }

    /**
     */
    public io.grpc.stub.StreamObserver<io.grpc.rx.EchoService.EchoReq> bidiStreaming(
        io.grpc.stub.StreamObserver<io.grpc.rx.EchoService.EchoResp> responseObserver) {
      return asyncUnimplementedStreamingCall(METHOD_BIDI_STREAMING, responseObserver);
    }

    @java.lang.Override public io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            METHOD_UNARY,
            asyncUnaryCall(
              new MethodHandlers<
                io.grpc.rx.EchoService.EchoReq,
                io.grpc.rx.EchoService.EchoResp>(
                  this, METHODID_UNARY)))
          .addMethod(
            METHOD_SERVER_STREAMING,
            asyncServerStreamingCall(
              new MethodHandlers<
                io.grpc.rx.EchoService.EchoCountReq,
                io.grpc.rx.EchoService.EchoResp>(
                  this, METHODID_SERVER_STREAMING)))
          .addMethod(
            METHOD_CLIENT_STREAMING,
            asyncClientStreamingCall(
              new MethodHandlers<
                io.grpc.rx.EchoService.EchoReq,
                io.grpc.rx.EchoService.EchoCountResp>(
                  this, METHODID_CLIENT_STREAMING)))
          .addMethod(
            METHOD_BIDI_STREAMING,
            asyncBidiStreamingCall(
              new MethodHandlers<
                io.grpc.rx.EchoService.EchoReq,
                io.grpc.rx.EchoService.EchoResp>(
                  this, METHODID_BIDI_STREAMING)))
          .build();
    }
  }

  /**
   */
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
        io.grpc.stub.StreamObserver<io.grpc.rx.EchoService.EchoResp> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_UNARY, getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void serverStreaming(io.grpc.rx.EchoService.EchoCountReq request,
        io.grpc.stub.StreamObserver<io.grpc.rx.EchoService.EchoResp> responseObserver) {
      asyncServerStreamingCall(
          getChannel().newCall(METHOD_SERVER_STREAMING, getCallOptions()), request, responseObserver);
    }

    /**
     */
    public io.grpc.stub.StreamObserver<io.grpc.rx.EchoService.EchoReq> clientStreaming(
        io.grpc.stub.StreamObserver<io.grpc.rx.EchoService.EchoCountResp> responseObserver) {
      return asyncClientStreamingCall(
          getChannel().newCall(METHOD_CLIENT_STREAMING, getCallOptions()), responseObserver);
    }

    /**
     */
    public io.grpc.stub.StreamObserver<io.grpc.rx.EchoService.EchoReq> bidiStreaming(
        io.grpc.stub.StreamObserver<io.grpc.rx.EchoService.EchoResp> responseObserver) {
      return asyncBidiStreamingCall(
          getChannel().newCall(METHOD_BIDI_STREAMING, getCallOptions()), responseObserver);
    }
  }

  /**
   */
  public static final class EchoBlockingStub extends io.grpc.stub.AbstractStub<EchoBlockingStub> {
    private EchoBlockingStub(io.grpc.Channel channel) {
      super(channel);
    }

    private EchoBlockingStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected EchoBlockingStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new EchoBlockingStub(channel, callOptions);
    }

    /**
     */
    public io.grpc.rx.EchoService.EchoResp unary(io.grpc.rx.EchoService.EchoReq request) {
      return blockingUnaryCall(
          getChannel(), METHOD_UNARY, getCallOptions(), request);
    }

    /**
     */
    public java.util.Iterator<io.grpc.rx.EchoService.EchoResp> serverStreaming(
        io.grpc.rx.EchoService.EchoCountReq request) {
      return blockingServerStreamingCall(
          getChannel(), METHOD_SERVER_STREAMING, getCallOptions(), request);
    }
  }

  /**
   */
  public static final class EchoFutureStub extends io.grpc.stub.AbstractStub<EchoFutureStub> {
    private EchoFutureStub(io.grpc.Channel channel) {
      super(channel);
    }

    private EchoFutureStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected EchoFutureStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new EchoFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.grpc.rx.EchoService.EchoResp> unary(
        io.grpc.rx.EchoService.EchoReq request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_UNARY, getCallOptions()), request);
    }
  }

  private static final int METHODID_UNARY = 0;
  private static final int METHODID_SERVER_STREAMING = 1;
  private static final int METHODID_CLIENT_STREAMING = 2;
  private static final int METHODID_BIDI_STREAMING = 3;

  private static class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final EchoImplBase serviceImpl;
    private final int methodId;

    public MethodHandlers(EchoImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_UNARY:
          serviceImpl.unary((io.grpc.rx.EchoService.EchoReq) request,
              (io.grpc.stub.StreamObserver<io.grpc.rx.EchoService.EchoResp>) responseObserver);
          break;
        case METHODID_SERVER_STREAMING:
          serviceImpl.serverStreaming((io.grpc.rx.EchoService.EchoCountReq) request,
              (io.grpc.stub.StreamObserver<io.grpc.rx.EchoService.EchoResp>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_CLIENT_STREAMING:
          return (io.grpc.stub.StreamObserver<Req>) serviceImpl.clientStreaming(
              (io.grpc.stub.StreamObserver<io.grpc.rx.EchoService.EchoCountResp>) responseObserver);
        case METHODID_BIDI_STREAMING:
          return (io.grpc.stub.StreamObserver<Req>) serviceImpl.bidiStreaming(
              (io.grpc.stub.StreamObserver<io.grpc.rx.EchoService.EchoResp>) responseObserver);
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
