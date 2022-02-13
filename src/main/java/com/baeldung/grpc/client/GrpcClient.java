package com.baeldung.grpc.client;

import com.baeldung.grpc.HelloRequest;
import com.baeldung.grpc.HelloResponse;
import com.baeldung.grpc.HelloServiceGrpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.util.ArrayList;
import java.util.List;

public class GrpcClient {
    public static void main(String[] args) {
        new GrpcClient().run(5);
    }


    public List<HelloResponse> run(int numMsgs) {
        // connect
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 8080)
            .usePlaintext()
            .build();

        HelloServiceGrpc.HelloServiceBlockingStub stub
            = HelloServiceGrpc.newBlockingStub(channel);

        // send messages
        List<HelloResponse> msgs = new ArrayList<>(numMsgs);
        for (int i=0; i<numMsgs; i++) {
            HelloResponse helloResponse = stub.hello(HelloRequest.newBuilder()
                .setFirstName("Baeldung" + i)
                .setLastName("gRPC")
                .build());

            System.out.println("Response received from server:\n" + helloResponse);

            msgs.add(helloResponse);
        }

        channel.shutdown();
        return msgs;
    }
}
