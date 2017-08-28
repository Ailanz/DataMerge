package grabber;

import dao.StockPriceDao;
import db.SqliteDriver;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class DailyPriceGrabber {

    public static List<StockPriceDao> getStockPrices(String stockSymbol) {

        AlphaVantageBuilder builder = AlphaVantageBuilder.aBuilder()
                .withFunction(AlphaVantageEnum.Function.TIME_SERIES_DAILY)
                .withOutputSize(AlphaVantageEnum.OutputSize.FULL)
                .withSymbol(stockSymbol);

        List<ResultData> data = builder.execute();
        if (data == null) {
            return null;
        }

        List<StockPriceDao> ret = new LinkedList<>();
        for (ResultData r : data) {
            ret.add(new StockPriceDao(stockSymbol, r.getDate(), r.getData().get("1. open").asDouble(), r.getData().get("2. high").asDouble(),
                    r.getData().get("3. low").asDouble(), r.getData().get("4. close").asDouble(), r.getData().get("5. volume").asLong()));
        }

        return ret;
    }

    //Populate Stock Prices and Remove ununsed Stock symbols
    public static void populateStockPrices(List<String> symbols) throws InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(5);

        List<String> invalidSymbols = new CopyOnWriteArrayList<>();
        for (String sym : symbols) {
            Runnable task = () -> {
                List<StockPriceDao> prices = DailyPriceGrabber.getStockPrices(sym);
                if (prices == null) {
                    invalidSymbols.add(sym);
                    System.out.println("Missing: " + sym);
                } else {
                    SqliteDriver.insertStockPrice(prices);
                    System.out.println("Processed: " + sym);
                }
            };
            pool.execute(task);
        }
        pool.shutdown();
        pool.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);

        invalidSymbols.stream().forEach(s -> symbols.remove(s));
    }
}
