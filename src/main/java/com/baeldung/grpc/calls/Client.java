package com.baeldung.grpc.calls;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.baeldung.grpc.streaming.HealthStatus;
import com.baeldung.grpc.streaming.Stock;
import com.baeldung.grpc.streaming.StockQuote;
import com.baeldung.grpc.streaming.StockQuoteProviderGrpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baeldung.grpc.streaming.StockQuoteProviderGrpc.StockQuoteProviderBlockingStub;
import com.baeldung.grpc.streaming.StockQuoteProviderGrpc.StockQuoteProviderStub;

import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;

public class Client {
    private static final Logger logger = LoggerFactory.getLogger(Client.class.getName());

    private final StockQuoteProviderBlockingStub blockingStub;
    private final StockQuoteProviderStub nonBlockingStub;
    private List<Stock> stocks = new ArrayList<>();

    public Client(Channel channel) {
        
        blockingStub = StockQuoteProviderGrpc.newBlockingStub(channel);
        nonBlockingStub = StockQuoteProviderGrpc.newStub(channel);
        initializeStocks();
    }

    public String serverHealth() {
        logger.info("######START EXAMPLE######: Unary Call - list of Stock prices from a given stock");
        HealthStatus status = HealthStatus.getDefaultInstance();
        HealthStatus response = blockingStub.unaryCallForServerHealth(status);

        logger.info("RESPONSE - Health: {}", response.getStatus());
        return response.getStatus();
    }

    public int serverSideStreamingListOfStockPrices() {
        List<StockQuote> received = new ArrayList<>(stocks.size());

        logger.info("######START EXAMPLE######: ServerSideStreaming - list of Stock prices from a given stock");
        Stock request = Stock.newBuilder()
            .setTickerSymbol("AU")
            .setCompanyName("Austich")
            .setDescription("server streaming example")
            .build();
        Iterator<StockQuote> stockQuotes;
        try {

            logger.info("REQUEST - ticker symbol {}", request.getTickerSymbol());
            stockQuotes = blockingStub.serverSideStreamingGetListStockQuotes(request);

            for (int i = 1; stockQuotes.hasNext(); i++) {
                StockQuote stockQuote = stockQuotes.next();
                logger.info("RESPONSE - Price #" + i + ": {}", stockQuote.getPrice());
                received.add(stockQuote);
            }
        } catch (StatusRuntimeException e) {
            logger.info("RPC failed: {}", e.getStatus());
        }

        return received.size();
    }

    public int clientSideStreamingGetStatisticsOfStocks() throws InterruptedException {
        List<StockQuote> received = new ArrayList<>(stocks.size());

        logger.info("######START EXAMPLE######: ClientSideStreaming - getStatisticsOfStocks from a list of stocks");
        final CountDownLatch finishLatch = new CountDownLatch(1);
        StreamObserver<StockQuote> responseObserver = new StreamObserver<StockQuote>() {
            @Override
            public void onNext(StockQuote summary) {
                logger.info("RESPONSE, got stock statistics - Average Price: {}, description: {}",
                    summary.getPrice(), summary.getDescription());
                received.add(summary);
            }

            @Override
            public void onCompleted() {
                logger.info("Finished clientSideStreamingGetStatisticsOfStocks");
                finishLatch.countDown();
            }

            @Override
            public void onError(Throwable t) {
                logger.warn("Stock Statistics Failed: {}", Status.fromThrowable(t));
                 finishLatch.countDown();
            }
        };

        StreamObserver<Stock> requestObserver = nonBlockingStub.clientSideStreamingGetStatisticsOfStocks(responseObserver);
        try {

            for (Stock stock : stocks) {
                logger.info("REQUEST: {}, {}", stock.getTickerSymbol(), stock.getCompanyName());
                requestObserver.onNext(stock);
                if (finishLatch.getCount() == 0) {
                    return received.size();
                }
            }
        } catch (RuntimeException e) {
            requestObserver.onError(e);
            throw e;
        }
        requestObserver.onCompleted();
        if (!finishLatch.await(1, TimeUnit.MINUTES)) {
            logger.warn("clientSideStreamingGetStatisticsOfStocks can not finish within 1 minutes");
        }
        return received.size();
    }

    public int bidirectionalStreamingGetListsStockQuotes() throws InterruptedException{
        List<StockQuote> received = new ArrayList<>(stocks.size());

        logger.info("#######START EXAMPLE#######: BidirectionalStreaming - getListsStockQuotes from list of stocks");
        final CountDownLatch finishLatch = new CountDownLatch(1);
        StreamObserver<StockQuote> responseObserver = new StreamObserver<StockQuote>() {
            @Override
            public void onNext(StockQuote stockQuote) {
                logger.info("RESPONSE price#{} : {}, description:{}", stockQuote.getOfferNumber(), stockQuote.getPrice(), stockQuote.getDescription());
                received.add(stockQuote);
            }

            @Override
            public void onCompleted() {
                logger.info("Finished bidirectionalStreamingGetListsStockQuotes");
                finishLatch.countDown();
            }

            @Override
            public void onError(Throwable t) {
                logger.warn("bidirectionalStreamingGetListsStockQuotes Failed: {0}", Status.fromThrowable(t));
                finishLatch.countDown();
            }
        };
        StreamObserver<Stock> requestObserver = nonBlockingStub.bidirectionalStreamingGetListsStockQuotes(responseObserver);
        try {            
            for (Stock stock : stocks) {
                logger.info("REQUEST: {}, {}", stock.getTickerSymbol(), stock.getCompanyName());
                requestObserver.onNext(stock);
                Thread.sleep(200);
                if (finishLatch.getCount() == 0) {
                    return received.size();
                }
            }
        } catch (RuntimeException e) {
            requestObserver.onError(e);
            throw e;
        }
        requestObserver.onCompleted();

        if (!finishLatch.await(1, TimeUnit.MINUTES)) {
            logger.warn("bidirectionalStreamingGetListsStockQuotes can not finish within 1 minute");
        }
        return received.size();
    }

     public static void main(String[] args) throws InterruptedException {
        // run bi-directional from command line
        String target = "localhost:8980";
        if (args.length > 0) {
            target = args[0];
        }

        ManagedChannel channel = ManagedChannelBuilder.forTarget(target)
            .usePlaintext()
            .build();
        try {
            Client client = new Client(channel);
            client.bidirectionalStreamingGetListsStockQuotes();

        } finally {
            channel.shutdownNow()
                .awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    public int getNumInitializedStocks() {
        return stocks.size();
    }

    private void initializeStocks() {
        
            this.stocks.addAll(Arrays.asList(
                Stock.newBuilder().setTickerSymbol("AU").setCompanyName("Auburn Corp").setDescription("Aptitude Intel").build()
             , Stock.newBuilder().setTickerSymbol("BAS").setCompanyName("Bassel Corp").setDescription("Business Intel").build()
            , Stock.newBuilder().setTickerSymbol("COR").setCompanyName("Corvine Corp").setDescription("Corporate Intel").build()
            , Stock.newBuilder().setTickerSymbol("DIA").setCompanyName("Dialogic Corp").setDescription("Development Intel").build()
            , Stock.newBuilder().setTickerSymbol("EUS").setCompanyName("Euskaltel Corp").setDescription("English Intel").build()));
    }
}
