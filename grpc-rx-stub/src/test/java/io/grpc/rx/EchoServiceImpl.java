package io.grpc.rx;

import io.grpc.rx.EchoGrpcRx.EchoImplBase;
import io.grpc.rx.core.BidiStreamingProcessor;
import io.grpc.rx.core.ClientStreamingProcessor;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import org.reactivestreams.Subscriber;

import static io.grpc.rx.EchoService.*;

public class EchoServiceImpl extends EchoImplBase {
	@Override
	public void unary(EchoReq request, SingleObserver<EchoResp> responseObserver) {
		Single.fromCallable(() -> {
			EchoResp resp = EchoResp.newBuilder().setValue("Server: " + request.getValue()).build();
			return resp;
		}).subscribe(responseObserver);
	}

	@Override
	public Subscriber<EchoReq> clientStreaming(SingleObserver<EchoCountResp> responseObserver) {
		ClientStreamingProcessor<EchoReq, EchoCountResp> processor = new ClientStreamingProcessor<EchoReq, EchoCountResp>() {
			private int count;
			@Override
			protected EchoCountResp generateResponse() {
				return EchoCountResp.newBuilder().setValue("Result").setCount(count).build();
			}

			@Override
			protected void processRequest(EchoReq echoReq) {
				count++;
			}
		};
		processor.subscribe(responseObserver);

		return processor;
	}

	@Override
	public void serverStreaming(EchoCountReq request, Subscriber<EchoResp> responseSubscriber) {
		Flowable.range(0, request.getCount())
			.map(i -> EchoResp.newBuilder().setValue(i.toString()).build())
			.subscribe(responseSubscriber);
	}

	@Override
	public Subscriber<EchoReq> bidiStreaming(Subscriber<EchoService.EchoResp> responseSubscriber) {
		BidiStreamingProcessor<EchoReq, EchoResp> processor = new BidiStreamingProcessor<EchoReq, EchoResp>() {
			@Override
			protected EchoResp process(EchoReq echoReq) {
				return EchoResp.newBuilder().setValue(echoReq.getValue()).build();
			}
		};

		processor.subscribe(responseSubscriber);
		return processor;
	}
}
