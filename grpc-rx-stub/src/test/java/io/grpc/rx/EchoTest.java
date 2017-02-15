package io.grpc.rx;

import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import org.junit.After;
import org.junit.Before;

public class EchoTest extends EchoTestBase {
  private static final String UNIQUE_SERVER_NAME = "in-process server for " + EchoTest.class;
  private Server server;
  private ManagedChannel channel;

  private EchoGrpcRx.EchoStub newClient() {
    channel = InProcessChannelBuilder
        .forName(UNIQUE_SERVER_NAME)
        .usePlaintext(true)
        .build();

    return EchoGrpcRx.newStub(channel);
  }

  public EchoTest() {
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
}
