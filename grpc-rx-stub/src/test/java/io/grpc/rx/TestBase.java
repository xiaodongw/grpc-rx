package io.grpc.rx;

import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import org.junit.After;
import org.junit.Before;

public abstract class TestBase {
  String uniqueServerName;

  protected ManagedChannel channel;
  private Server server;

  protected int testWaitSeconds = 10;

  protected ManagedChannel newChannel() {
    return InProcessChannelBuilder
        .forName(uniqueServerName)
        .usePlaintext()
        .build();
  }

  protected Server newServer() {
    return InProcessServerBuilder.forName(uniqueServerName)
        .addService(newService())
        .directExecutor()
        .build();
  }

  protected abstract EchoGrpcRx.EchoImplBase newService();

  public TestBase() {
    uniqueServerName = "in-process server for " + getClass();
  }

  @Before
  public void setUp() throws Exception {
    server = newServer();
    server.start();

    channel = newChannel();
  }

  @After
  public void tearDown() {
    channel.shutdownNow();
    server.shutdownNow();
  }
}
