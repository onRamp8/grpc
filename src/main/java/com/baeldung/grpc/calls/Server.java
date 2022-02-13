package com.baeldung.grpc.calls;

import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import com.baeldung.grpc.streaming.HealthStatus;
import com.baeldung.grpc.streaming.Stock;
import com.baeldung.grpc.streaming.StockQuote;
import com.baeldung.grpc.streaming.StockQuoteProviderGrpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

public class Server {

    public static final String HEALTH_STATUS_ALIVE = "Alive";
    public static final int NUM_RESPONSES_PER_REQUEST = 5;
    private static final Logger logger = LoggerFactory.getLogger(Server.class.getName());
    private final int port;
    private final io.grpc.Server server;

    public Server(int port) {
        this.port = port;
        server = ServerBuilder.forPort(port)
            .addService(new StockService())
            .build();
    }

    public void start() throws IOException {
        logger.info("Starting gRPC server");
        server.start();
        logger.info("Server started, listening on " + port);
        Runtime.getRuntime()
            .addShutdownHook(new Thread() {
                @Override
                public void run() {
                    System.err.println("shutting down server");
                    try {
                        Server.this.stop();
                    } catch (InterruptedException e) {
                        e.printStackTrace(System.err);
                    }
                    System.err.println("server shutted down");
                }
            });
    }

    public void stop() throws InterruptedException {
        if (server != null) {
            server.shutdown()
                .awaitTermination(30, TimeUnit.SECONDS);
        }
    }

    public static void main(String[] args) throws Exception {
        Server server = new Server(8980);
        server.start();
        if (server.server != null) {
            server.server.awaitTermination();
        }
    }

    private static class StockService extends StockQuoteProviderGrpc.StockQuoteProviderImplBase {

        StockService() {
        }

        @Override
        public void unaryCallForServerHealth(HealthStatus request,
                                             StreamObserver<HealthStatus> responseObserver) {
            logger.info("health request received");

            HealthStatus response = HealthStatus.newBuilder()
                .setStatus(HEALTH_STATUS_ALIVE)
                .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
            logger.debug("health request completed. Response sent");
        }

        @Override
        public void serverSideStreamingGetListStockQuotes(Stock request, StreamObserver<StockQuote> responseObserver) {
            logger.info("list of quotes request received for stock: {}", request.getTickerSymbol());

            for (int i = 1; i <= NUM_RESPONSES_PER_REQUEST; i++) {

                StockQuote stockQuote = StockQuote.newBuilder()
                    .setPrice(fetchStockPriceBid(request))
                    .setOfferNumber(i)
                    .setDescription("Price for stock:" + request.getTickerSymbol())
                    .build();
                responseObserver.onNext(stockQuote);
            }
            responseObserver.onCompleted();
            logger.debug("completed work for list of quotes request");
        }

        @Override
        public StreamObserver<Stock> clientSideStreamingGetStatisticsOfStocks(final StreamObserver<StockQuote> responseObserver) {
            logger.info("statistics request received");

            return new StreamObserver<Stock>() {
                int count;
                double price = 0.0;
                StringBuffer sb = new StringBuffer();

                @Override
                public void onNext(Stock stock) {
                    count++;
                    price = +fetchStockPriceBid(stock);
                    sb.append(":")
                        .append(stock.getTickerSymbol());
                }

                @Override
                public void onCompleted() {
                    responseObserver.onNext(StockQuote.newBuilder()
                        .setPrice(price / count)
                        .setDescription("Statistics-" + sb.toString())
                        .build());
                    responseObserver.onCompleted();
                    logger.debug("completed work for statistics request");
                }

                @Override
                public void onError(Throwable t) {
                    logger.warn("error:{}", t.getMessage());
                }
            };
        }

        @Override
        public StreamObserver<Stock> bidirectionalStreamingGetListsStockQuotes(final StreamObserver<StockQuote> responseObserver) {
            logger.info("bi directional request received");

            return new StreamObserver<Stock>() {
                @Override
                public void onNext(Stock request) {

                    for (int i = 1; i <= NUM_RESPONSES_PER_REQUEST; i++) {

                        StockQuote stockQuote = StockQuote.newBuilder()
                            .setPrice(fetchStockPriceBid(request))
                            .setOfferNumber(i)
                            .setDescription("Price for stock:" + request.getTickerSymbol())
                            .build();
                        responseObserver.onNext(stockQuote);
                    }
                }

                @Override
                public void onCompleted() {
                    responseObserver.onCompleted();
                    logger.debug("completed work for bi-directional request");
                }

                @Override
                public void onError(Throwable t) {
                    logger.warn("error:{}", t.getMessage());
                }
            };
        }
    }

    private static double fetchStockPriceBid(Stock stock) {

        return stock.getTickerSymbol()
            .length()
            + ThreadLocalRandom.current()
                .nextDouble(-0.1d, 0.1d);
    }
}