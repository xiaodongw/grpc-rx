package io.grpc.rx;

import io.reactivex.Flowable;
import io.reactivex.observers.TestObserver;
import io.reactivex.subscribers.TestSubscriber;
import org.junit.Test;
import org.reactivestreams.Subscriber;

import java.util.concurrent.TimeUnit;

public abstract class EchoTestBase {
	protected EchoGrpcRx.EchoStub client;
	private int testWaitSeconds = 10;

	@Test
	public void unary() {
		EchoService.EchoReq req = EchoService.EchoReq.newBuilder().setValue("Hello").build();
		TestObserver<EchoService.EchoResp> responseObserver = new TestObserver<EchoService.EchoResp>();

		client.unary(req, responseObserver);

		responseObserver.awaitDone(testWaitSeconds, TimeUnit.SECONDS);
		responseObserver.assertComplete();
	}

	@Test
	public void serverStreaming() {
		EchoService.EchoCountReq req = EchoService.EchoCountReq.newBuilder().setValue("Hello").setCount(5).build();
		TestSubscriber<EchoService.EchoResp> responseSubscriber = new TestSubscriber<>(5);

		client.serverStreaming(req, responseSubscriber);

		responseSubscriber.awaitDone(testWaitSeconds, TimeUnit.SECONDS);
		// responseSubscriber.assertResult();
		responseSubscriber.assertComplete();
	}

	@Test
	public void clientStreaming() {
		TestObserver<EchoService.EchoCountResp> responseObserver = new TestObserver<EchoService.EchoCountResp>();

		EchoService.EchoReq[] requests = new EchoService.EchoReq[3];
		for (int i = 0; i < requests.length; i++) {
			requests[i] = EchoService.EchoReq.newBuilder().setValue(String.format("%d", i)).build();
		}
		Flowable<EchoService.EchoReq> requestFlowable = Flowable.fromArray(requests);
		Subscriber<EchoService.EchoReq> requestSubscriber = client.clientStreaming(responseObserver);

		// start to send requests to grpc subscriber
		requestFlowable.subscribe(requestSubscriber);

		responseObserver.awaitDone(testWaitSeconds, TimeUnit.SECONDS);
		responseObserver.assertComplete();
	}

	@Test
	public void bidiStreaming() {
		EchoService.EchoReq[] requests = new EchoService.EchoReq[5];
		for (int i = 0; i < requests.length; i++) {
			requests[i] = EchoService.EchoReq.newBuilder().setValue(String.format("%d", i)).build();
		}
		Flowable<EchoService.EchoReq> requestFlowable = Flowable.fromArray(requests);

		TestSubscriber<EchoService.EchoResp> responseSubscriber = new TestSubscriber(5);

		Subscriber<EchoService.EchoReq> requestSubscriber = client.bidiStreaming(responseSubscriber);

		// start to send requests to grpc subscriber
		requestFlowable.subscribe(requestSubscriber);

		responseSubscriber.awaitDone(testWaitSeconds, TimeUnit.SECONDS);
		responseSubscriber.assertComplete();
	}
}
