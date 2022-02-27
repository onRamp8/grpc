package com.baeldung.grpc.calls;

import com.baeldung.grpc.streaming.Stock;
import com.baeldung.grpc.streaming.StockQuoteProviderGrpc;
import com.baeldung.grpc.streaming.StockQuoteProviderGrpc.StockQuoteProviderBlockingStub;
import com.baeldung.grpc.streaming.StockQuoteProviderGrpc.StockQuoteProviderStub;
import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
        // TODO: 10 - implement client health. testUnaryCall should pass after this is implemented
        return null;
    }

    public int serverSideStreamingListOfStockPrices() {
        logger.info("######START EXAMPLE######: ServerSideStreaming - list of Stock prices from a given stock");
        // TODO: 12 - implement client side of server streaming. testServerSideStream should pass after this is implemented
        return -1;
    }

    public int clientSideStreamingGetStatisticsOfStocks() throws InterruptedException {
        logger.info("######START EXAMPLE######: ClientSideStreaming - getStatisticsOfStocks from a list of stocks");
        // TODO: 13 - server side of client streaming. testClientSideStream should pass after this is implemented
        return -1;
    }

    public int bidirectionalStreamingGetListsStockQuotes() throws InterruptedException{
        logger.info("#######START EXAMPLE#######: BidirectionalStreaming - getListsStockQuotes from list of stocks");
        // TODO: 15 - server side of bi-directional streaming. testBidirectionalStream should pass after this is implemented
        return -1;
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
