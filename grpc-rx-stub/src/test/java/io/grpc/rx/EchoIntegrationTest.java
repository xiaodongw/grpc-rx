package io.grpc.rx;

import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.NettyServerBuilder;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.io.IOException;

public class EchoIntegrationTest extends EchoTestBase {
    private static Server server;
    private static ManagedChannel channel;

    public EchoIntegrationTest() {
        client = newClient();
    }

    private static EchoGrpcRx.EchoStub newClient() {
        channel = NettyChannelBuilder
                .forAddress("localhost", 8080)
                .usePlaintext(true)
                .build();

        return EchoGrpcRx.newStub(channel);
    }

    private static void startServer() throws IOException {
        server = NettyServerBuilder
                .forPort(8080)
                .addService(new EchoServiceImpl())
                .build();

        server.start();
    }

    private static void stopServer() throws InterruptedException {
        server.shutdown();
        server.awaitTermination();
    }

    @BeforeClass
    public static void beforeClass() throws IOException {
        startServer();
    }

    @AfterClass
    public static void afterClass() throws InterruptedException {
        stopServer();
    }
}
