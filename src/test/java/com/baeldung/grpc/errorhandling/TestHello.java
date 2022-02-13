package com.baeldung.grpc.errorhandling;

import com.baeldung.grpc.HelloResponse;
import com.baeldung.grpc.client.GrpcClient;
import com.baeldung.grpc.server.GrpcServer;
import org.junit.jupiter.api.Test;
import java.util.List;

import static java.lang.Thread.sleep;
import static org.junit.jupiter.api.Assertions.*;

public class TestHello {

  @Test
  public void testServerClientCommunication() throws Exception {
    // given
    ClientThread client = new ClientThread(1);
    Thread serverThread = new Thread(new ServerThread(), "server-thread");
    Thread clientThread = new Thread(client, "client-thread");
    // and
    serverThread.start();
    sleep(4000);

    // when
    clientThread.start();
    sleep(4000);

    // then
    assertNotNull(client.receivedMsgs);
    assertEquals(1, client.receivedMsgs.size());
    assertTrue(client.receivedMsgs.get(0).getGreeting().contains("Hello"));
  }

  private static class ServerThread implements Runnable {

    @Override
    public void run() {
      try {
        GrpcServer.main(null);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }

  private static class ClientThread implements Runnable {
    private int numMsgs;
    public List<HelloResponse> receivedMsgs;

    public ClientThread(int numMsgs) {
      this.numMsgs = numMsgs;
    }

    @Override
    public void run() {
      try {
        this.receivedMsgs = new GrpcClient().run(numMsgs);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }
}
