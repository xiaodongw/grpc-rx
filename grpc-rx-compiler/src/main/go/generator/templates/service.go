package templates

const (
	Service = `
{{- with $s := .}}
package {{$s.JavaPackage}};

import static io.grpc.MethodDescriptor.generateFullMethodName;
import io.grpc.rx.stub.ClientCallsRx;
import io.grpc.rx.stub.ServerCallsRx;

{{- /**
 * <pre>
 * Test service that supports all call types.
 * </pre>
 */}}
@javax.annotation.Generated(
  value = "by gRPC proto compiler (version 0.5.0)",
  comments = "Source: {{.ProtoFile}}")
public class {{$s.Name}}GrpcRx {

  private {{$s.Name}}GrpcRx() {}

  public static final String SERVICE_NAME = "{{$s.ProtoName}}";

  // Static method descriptors that strictly reflect the proto.
  {{- range $i, $m := .Methods}}
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<{{$m.InputType}},
    {{$m.OutputType}}> {{$m.FieldName}} =
    io.grpc.MethodDescriptor.<{{$m.InputType}}, {{$m.OutputType}}>newBuilder()
      .setType(io.grpc.MethodDescriptor.MethodType.{{$m.GrpcMethodType}})
      .setFullMethodName(generateFullMethodName("{{$s.ProtoName}}", "{{$m.Name}}"))
      .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller({{$m.InputType}}.getDefaultInstance()))
      .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller({{$m.OutputType}}.getDefaultInstance()))
      .build();
  {{- end}}

  /**
   * Creates a new RX stub
   */
  public static {{.Name}}Stub newStub(io.grpc.Channel channel) {
    return new {{.Name}}Stub(channel);
  }

  /**
   * Creates a new RX stub with call options
   */
  public static {{.Name}}Stub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
    return new {{.Name}}Stub(channel, callOptions);
  }

  {{/**
   * <pre>
   * Test service that supports all call types.
   * </pre>
   */}}
  public static abstract class {{.Name}}ImplBase implements io.grpc.BindableService {

    {{- range $i, $m := .Methods}}
    {{- /**
     * <pre>
     * One requestMore followed by one response.
     * The server returns the client payload as-is.
     * </pre>
     */ -}}
    public {{$m.FullOutputType}} {{$m.JavaName}}({{$m.FullInputType}} request) {
      return ServerCallsRx.{{$m.UnimplementedCall}}({{$m.FieldName}});
    }
    {{- end}}

    @java.lang.Override public io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
        {{- range $i, $m := .Methods}}
        .addMethod(
          {{$m.FieldName}},
          ServerCallsRx.{{$m.Call}}(
            new MethodHandlers<
              {{$m.InputType}},
              {{$m.OutputType}}>(
              this, {{$m.IdName}})))
        {{- end}}
        .build();
    }
  }

  {{/**
   * <pre>
   * Test service that supports all call types.
   * </pre>
   */}}
  public static class {{$s.Name}}Stub extends io.grpc.stub.AbstractStub<{{$s.Name}}Stub> {
    private {{$s.Name}}Stub(io.grpc.Channel channel) {
      super(channel);
    }

    private {{$s.Name}}Stub(io.grpc.Channel channel,
                            io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected {{$s.Name}}Stub build(io.grpc.Channel channel,
                                    io.grpc.CallOptions callOptions) {
      return new {{$s.Name}}Stub(channel, callOptions);
    }

    {{- range $i, $m := .Methods}}
    {{- /**
     * <pre>
     * One requestMore followed by one response.
     * The server returns the client payload as-is.
     * </pre>
     */ -}}
    public {{$m.FullOutputType}} {{$m.JavaName}}({{$m.FullInputType}} request) {
      return ClientCallsRx.{{$m.Call}}(
        getChannel().newCall({{$m.FieldName}}, getCallOptions()), {{$m.CallParams}});
    }
    {{- end}}
  }

  {{range $i, $m := .Methods}}
  private static final int {{$m.IdName}} = {{$m.Id}};
  {{- end}}

  private static class MethodHandlers<Req, Resp> implements
    io.grpc.rx.stub.ServerCallsRx.UnaryMethod<Req, Resp>,
    io.grpc.rx.stub.ServerCallsRx.ServerStreamingMethod<Req, Resp>,
    io.grpc.rx.stub.ServerCallsRx.ClientStreamingMethod<Req, Resp>,
    io.grpc.rx.stub.ServerCallsRx.BidiStreamingMethod<Req, Resp> {
    private final {{$s.Name}}ImplBase serviceImpl;
    private final int methodId;

    public MethodHandlers({{.Name}}ImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.reactivex.Single<Resp> unaryInvoke(Req request) {
      switch (methodId) {
        {{- range $i, $m := .Methods}}
        {{- if eq $m.MethodType 0}}
        case {{$m.IdName}}:
          return (io.reactivex.Single<Resp>) serviceImpl.{{$m.JavaName}}(({{$m.FullInputType}}) request);
        {{- end}}
        {{- end}}
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.reactivex.Flowable<Resp> serverStreamingInvoke(Req request) {
      switch (methodId) {
        {{- range $i, $m := .Methods}}
        {{- if eq $m.MethodType 1}}
        case {{$m.IdName}}:
          return (io.reactivex.Flowable<Resp>) serviceImpl.{{$m.JavaName}}(({{$m.FullInputType}}) request);
        {{- end}}
        {{- end}}
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.reactivex.Single<Resp> clientStreamingInvoke(io.reactivex.Flowable<Req> requests) {
      switch (methodId) {
        {{- range $i, $m := .Methods}}
        {{- if eq $m.MethodType 2}}
        case {{$m.IdName}}:
          return (io.reactivex.Single<Resp>) serviceImpl.{{$m.JavaName}}(({{$m.FullInputType}}) requests);
        {{- end}}
        {{- end}}
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.reactivex.Flowable<Resp> bidiStreamingInvoke(io.reactivex.Flowable<Req> requests) {
      switch (methodId) {
        {{- range $i, $m := .Methods}}
        {{- if eq $m.MethodType 3}}
        case {{$m.IdName}}:
          return (io.reactivex.Flowable<Resp>) serviceImpl.{{$m.JavaName}}(({{$m.FullInputType}}) requests);
        {{- end}}
        {{- end}}
        default:
          throw new AssertionError();
      }
    }

  }

  private static final class {{$s.Name}}DescriptorSupplier implements io.grpc.protobuf.ProtoFileDescriptorSupplier {
    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return {{$s.JavaPackage}}.{{$s.OuterClassName}}.getDescriptor();
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized ({{$s.Name}}GrpcRx.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
            .setSchemaDescriptor(new {{$s.Name}}DescriptorSupplier())
            {{- range $i, $m := .Methods}}
            .addMethod({{$m.FieldName}})
            {{- end}}
            .build();
        }
      }
    }
    return result;
  }
}
{{- end}}
`
)
