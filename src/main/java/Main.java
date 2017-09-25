import dao.StockDao;
import db.SqliteDriver;
import exchange.NASDAQ;
import exchange.SP500;
import exchange.StockExchange;
import grabber.DailyIndicatorGrabber;
import grabber.DailyPriceGrabber;
import ui.StockFilter;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class Main {

    public static void main(String[] args) throws IOException, URISyntaxException, SQLException, InterruptedException {
        List<StockDao> stockDaos = StockFilter.marketCapFilter(StockDao.getAllStocks());
        processStock(NASDAQ.getInstance(), stockDaos);

        //        scrapeData("TSX.txt", TSX.getInstance());
//        scrapeData("SP500.txt", SP500.getInstance());
//        scrapeData("nasdaqlisted.txt", NASDAQ.getInstance());
//        scrapeData("otherlisted.txt", NASDAQ.getInstance());

        System.out.println("Hello World!");
    }

    private static void scrapeData(String textFile, StockExchange exchange) throws InterruptedException {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        File file = new File(classloader.getResource(textFile).getFile());
        List<StockDao> stocks = exchange.parseFeed(file);
        processStock(exchange, stocks);
    }

    private static void processStock(StockExchange exchange, List<StockDao> stocks) throws InterruptedException {
        List<String> stockSymbols = stocks.stream().map(s -> s.getSymbol()).collect(Collectors.toList());
        DailyPriceGrabber.populateStockPrices(stockSymbols);
        SqliteDriver.insertStockSymbols(stockSymbols, exchange.getExchange());
//        DailyIndicatorGrabber.populateIndicators(stockSymbols);
    }


}



