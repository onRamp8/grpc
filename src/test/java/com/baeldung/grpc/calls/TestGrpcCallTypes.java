package com.baeldung.grpc.calls;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static java.lang.Thread.sleep;
import static org.junit.jupiter.api.Assertions.assertEquals;

/*
 *  Test the 4 types of gRPC calls
 *  1. Unary
 *  2. Server side stream
 *  3. Client side stream
 *  4. Bi-directional stream
 */
public class TestGrpcCallTypes {
  private static final int PORT = 8088;
  private static final String SERVER_URL = "localhost:" + PORT;
  private static final ManagedChannel CHANNEL = ManagedChannelBuilder.forTarget(SERVER_URL)
      .usePlaintext()
      .build();

  private static Server server = new Server(PORT);
  private static Client client;

  @BeforeAll
  public static void setup() throws Exception {
    server.start();
    sleep(TimeUnit.SECONDS.toMillis(1));

    client = new Client(CHANNEL);
  }

  @AfterAll
  public static void teardown() throws Exception {
    CHANNEL.shutdownNow()
        .awaitTermination(5, TimeUnit.SECONDS);
  }

  @Test
  public void testUnaryCall() {
    assertEquals(Server.HEALTH_STATUS_ALIVE,
        client.serverHealth());
  }

  @Test
  public void testServerSideStream()  {
    int numReceived = 0;
    numReceived = client.serverSideStreamingListOfStockPrices();
    assertEquals(5, numReceived);
  }

  @Test
  public void testClientSideStream() throws Exception {
    int numReceived = 0;
      numReceived = client.clientSideStreamingGetStatisticsOfStocks();
      assertEquals(1, numReceived);
  }

  @Test
  public void testBidirectionalStream() throws Exception {
    int numReceived = 0;
      int numExpectedBiDirectional = client.getNumInitializedStocks() * Server.NUM_RESPONSES_PER_REQUEST;
      numReceived = client.bidirectionalStreamingGetListsStockQuotes();
      assertEquals(numExpectedBiDirectional, numReceived);
  }
}
