package grabber;

import dao.StockPriceDao;
import util.GlobalUtil;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class DailyPriceGrabber {

    public static List<StockPriceDao> getStockPrices(String stockSymbol) {

        AlphaVantageBuilder builder = AlphaVantageBuilder.aBuilder()
                .withFunction(AlphaVantageEnum.Function.TIME_SERIES_DAILY_ADJUSTED)
                .withOutputSize(AlphaVantageEnum.OutputSize.FULL)
                .withSymbol(stockSymbol);

        List<ResultData> data = builder.execute();
        if (data == null) {
            return null;
        }

        List<StockPriceDao> ret = new LinkedList<>();
        for (ResultData r : data) {
            ret.add(new StockPriceDao(stockSymbol, r.getDate(), r.getData().get("2. high").asDouble(), r.getData().get("3. low").asDouble(), r.getData().get("1. open").asDouble()
                    , r.getData().get("4. close").asDouble(), r.getData().get("5. adjusted close").asDouble(),
                    r.getData().get("6. volume").asLong()));
        }

        return ret;
    }

    //Populate Stock Prices and Remove ununsed Stock symbols
    public static void populateStockPrices(List<String> symbols) throws InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(20);

        List<String> invalidSymbols = new CopyOnWriteArrayList<>();
        for (String sym : symbols) {
            Runnable task = () -> {
                final long startTime = System.currentTimeMillis();

                List<StockPriceDao> prices = DailyPriceGrabber.getStockPrices(sym);
                if (prices == null || prices.size() == 0) {
                    invalidSymbols.add(sym);
                    System.out.println("Missing: " + sym);
                } else {
                    StockPriceDao.insertStockPrice(prices);
                    System.out.println("Processed: " + sym + " - " + prices.size() + " : "
                            + (System.currentTimeMillis() - startTime) + " : " + GlobalUtil.getMemoryConsumption());
                }
            };
            pool.execute(task);
        }
        pool.shutdown();
        pool.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);

        invalidSymbols.stream().forEach(s -> symbols.remove(s));
    }
}
