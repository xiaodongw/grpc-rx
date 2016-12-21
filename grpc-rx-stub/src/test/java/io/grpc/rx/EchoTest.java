package io.grpc.rx;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.reactivex.Flowable;
import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.TestObserver;
import io.reactivex.subscribers.TestSubscriber;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static io.grpc.rx.EchoService.*;

public class EchoTest {
	private static final String UNIQUE_SERVER_NAME = "in-process server for " + EchoTest.class;
	private ExecutorService executor;
	private ListeningExecutorService executorService;
	private EchoGrpcRx.EchoStub client;
	private Server server;
	private ManagedChannel channel;
	private int testWaitSeconds = 10;

	private class EchoRespSubscriber implements Subscriber<EchoResp> {
		private Subscription subscription;
		private AtomicInteger pendingResponses = new AtomicInteger();

		@Override
		public void onSubscribe(Subscription s) {
			subscription = s;
			subscription.request(2);
		}

		@Override
		public void onNext(final EchoResp resp) {
			System.out.println(resp.toString());

			ListenableFuture<Void> future = executorService.submit(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					System.out.println("Processing " + resp.toString());
					Thread.sleep(500);  // simulate a slow process
					return null;
				}
			});
			future.addListener(new Runnable() {
				@Override
				public void run() {
					// mark the response process done
					int pending = pendingResponses.decrementAndGet();
					if (pending < 4) {
						subscription.request(2);
					}
				}
			}, executor);
		}

		@Override
		public void onError(Throwable e) {
			System.out.println(e.toString());
		}

		@Override
		public void onComplete() {
			System.out.println("complete");
		}
	}

	private class ResponseObserver<T> implements SingleObserver<T> {
		@Override
		public void onSubscribe(Disposable d) {
		}

		@Override
		public void onSuccess(T value) {
			System.out.println(value.toString());
		}

		@Override
		public void onError(Throwable e) {
			System.out.println(e.toString());
		}
	}

	private EchoGrpcRx.EchoStub newClient() {
		channel = InProcessChannelBuilder
			.forName(UNIQUE_SERVER_NAME)
			.usePlaintext(true)
			.build();

		return EchoGrpcRx.newStub(channel);
	}

	public EchoTest() {
		executor = Executors.newFixedThreadPool(4);
		executorService = MoreExecutors.listeningDecorator(executor);
		client = newClient();
		server = InProcessServerBuilder.forName(UNIQUE_SERVER_NAME).addService(new EchoServiceImpl()).directExecutor().build();
	}

	@Before
	  public void setUp() throws Exception {
		server.start();
	  }

	@After
	public void tearDown() {
		channel.shutdownNow();
		server.shutdownNow();
	}

	@Test
	public void unary() {
		EchoReq req = EchoReq.newBuilder().setValue("Hello").build();
		TestObserver<EchoResp> responseObserver = new TestObserver<EchoResp>();

		client.unary(req, responseObserver);

		responseObserver.awaitDone(testWaitSeconds, TimeUnit.SECONDS);
		responseObserver.assertComplete();
	}

	@Test
	public void serverStreaming() {
		EchoCountReq req = EchoCountReq.newBuilder().setValue("Hello").setCount(5).build();
		TestSubscriber<EchoResp> responseSubscriber = new TestSubscriber<>(5);

		client.serverStreaming(req, responseSubscriber);

		responseSubscriber.awaitDone(testWaitSeconds, TimeUnit.SECONDS);
		// responseSubscriber.assertResult();
		responseSubscriber.assertComplete();
	}

	@Test
	public void clientStreaming() {
		TestObserver<EchoCountResp> responseObserver = new TestObserver<EchoCountResp>();

		EchoReq[] requests = new EchoReq[3];
		for(int i = 0; i < requests.length; i++) {
			requests[i] = EchoReq.newBuilder().setValue(String.format("%d", i)).build();
		}
		Flowable<EchoReq> requestFlowable = Flowable.fromArray(requests);
		Subscriber<EchoReq> requestSubscriber = client.clientStreaming(responseObserver);

		// start to send requests to grpc subscriber
		requestFlowable.subscribe(requestSubscriber);

		responseObserver.awaitDone(testWaitSeconds, TimeUnit.SECONDS);
		responseObserver.assertComplete();
	}

	@Test
	public void bidiStreaming() {
		EchoReq[] requests = new EchoReq[5];
		for (int i = 0; i < requests.length; i++) {
			requests[i] = EchoReq.newBuilder().setValue(String.format("%d", i)).build();
		}
		Flowable<EchoReq> requestFlowable = Flowable.fromArray(requests);

		TestSubscriber<EchoResp> responseSubscriber = new TestSubscriber(5);

		Subscriber<EchoReq> requestSubscriber = client.bidiStreaming(responseSubscriber);

		// start to send requests to grpc subscriber
		requestFlowable.subscribe(requestSubscriber);

		responseSubscriber.awaitDone(testWaitSeconds, TimeUnit.SECONDS);
		responseSubscriber.assertComplete();
	}
}
