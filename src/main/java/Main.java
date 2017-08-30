import dao.StockDao;
import db.SqliteDriver;
import external.GlobalUtil;
import external.NASDAQ;
import external.StockExchange;
import external.TSX;
import grabber.DailyPriceGrabber;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class Main {

    public static void main(String[] args) throws IOException, URISyntaxException, SQLException, InterruptedException {
//        File file = new File(GlobalUtil.TSX_FEED);
//        scrapeData("TSX.txt");
        scrapeData("nasdaqlisted.txt", NASDAQ.getInstance());

        System.out.println("Hello World!");
    }

    private static void scrapeData(String textFile, StockExchange exchange) throws InterruptedException {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        File file = new File(classloader.getResource(textFile).getFile());
        List<StockDao> stocks = exchange.parseFeed(file);
        List<String> stockSymbols = stocks.stream().map(s -> s.getSymbol()).collect(Collectors.toList());
        DailyPriceGrabber.populateStockPrices(stockSymbols);
        SqliteDriver.insertStockSymbols(stockSymbols, exchange.getExchange());
    }


}



