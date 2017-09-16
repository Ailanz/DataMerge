package algo;

import core.Book;
import core.strategy.DayStrategyBuilder;
import core.TransactionRecord;
import dao.IndicatorDao;
import dao.MovingAverageDao;
import dao.StockDao;
import dao.StockPriceDao;
import db.InsertionBuilder;
import db.SqliteDriver;
import grabber.AlphaVantageEnum;
import grabber.LivePrice;
import org.apache.commons.lang3.tuple.Pair;
import org.joda.time.DateTime;
import util.TimeRange;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class OptimizerMA {

    public static void main(String args[]) throws InterruptedException {
//        List<StockDao> stocks = StockFilter.marketCapFilter(StockDao.getAllStocks());
//        List<StockDao> stocks = StockDao.getAllStocks();
//        List<String> existingSymbols = MovingAverageDao.getAllMovingAverages().stream()
//                .map(MovingAverageDao::getSymbol).collect(Collectors.toList());
//        stocks.forEach(s->{
//            try {
//                optimizeEarnings(s.getSymbol());
//            }catch(Exception e) {}
//        });
        insertToDB();
//        optimizeEarnings("AAPL");
    }

    public static Pair<Integer, Integer> optimizeEarnings(String stockSymbol) {
        int max = 24;
        int min = 3;
        StockDao stock = StockDao.getStock(stockSymbol);
        List<StockPriceDao> prices = LivePrice.getDaysPrice(stockSymbol);
        Map<DateTime, IndicatorDao> indicators = IndicatorDao.getIndicatorMap(stockSymbol);
        TimeRange timeRange = new TimeRange(DateTime.now().minusMonths(3));

        int maxShort = min;
        int maxLong = min;
        double maxProfit = -100000000;
        double latestPrice = prices.get(prices.size() - 1).getClose();

        for (int i = min; i < max; i++) {
            for (int j = i + 1; j < max; j++) {
                Book book = new Book();
                MovingAverage s = new ExponentialMovingAverage(i);
                MovingAverage l = new ExponentialMovingAverage(j);
                List<TransactionRecord> transactions = DayStrategyBuilder.aBuilder()
                        .withMovingAverages(s, l)
                        .withBuyAfterDate(DateTime.now().minusDays(30))
                        .withTimeRange(timeRange)
                        .execute(stock, prices, indicators);
                book.addTransaction(transactions);
                double profit = book.totalPNL(latestPrice);
//                System.out.println(String.format("Short: %s, Long: %s, Profit: %s",
//                        String.valueOf(i), String.valueOf(j), String.valueOf(profit)));
                if (profit > maxProfit) {
                    maxShort = i;
                    maxLong = j;
                    maxProfit = profit;
                }
            }
        }
//        System.out.println("------------------------");
        System.out.println(stockSymbol + " : " + String.format("Short: %s, Long: %s, MAX Profit: %s",
                String.valueOf(maxShort), String.valueOf(maxLong), String.valueOf(maxProfit)));

        return Pair.of(maxShort, maxLong);
    }

    public static void insertToDB() throws InterruptedException {
        int max = 24;
        int min = 3;
        ExecutorService pool = Executors.newFixedThreadPool(30);
//        List<String> existingSymbols = MovingAverageDao.getAllMovingAverages().stream()
//                .map(MovingAverageDao::getSymbol).collect(Collectors.toList());
        List<StockDao> stocks = StockDao.getAllStocks();
                //.stream().filter(s -> !existingSymbols.contains(s.getSymbol())).collect(Collectors.toList());
        stocks.forEach(st -> {
            Runnable task = () -> {
                List<StockPriceDao> prices = LivePrice.getDaysPrice(st.getSymbol());
                Map<DateTime, IndicatorDao> indicators = DayStrategyBuilder.getIndicatorMap(st.getSymbol(), AlphaVantageEnum.Interval.FIVE);
                TimeRange timeRange = new TimeRange(DateTime.now().minusMonths(3));

                int maxShort = min;
                int maxLong = min;
                double maxProfit = -100000000;
                double latestPrice = prices.get(prices.size() - 1).getClose();

                for (int i = min; i < max; i++) {
                    for (int j = i + 1; j < max; j++) {
                        Book book = new Book();
                        MovingAverage s = new ExponentialMovingAverage(i);
                        MovingAverage l = new ExponentialMovingAverage(j);
                        List<TransactionRecord> transactions = DayStrategyBuilder.aBuilder()
                                .withMovingAverages(s, l)
                                .withBuyAfterDate(DateTime.now().minusDays(30))
                                .withTimeRange(timeRange)
                                .execute(st, prices, indicators);
                        book.addTransaction(transactions);
                        double profit = book.totalPNL(latestPrice);
                        if (profit > maxProfit) {
                            maxShort = i;
                            maxLong = j;
                            maxProfit = profit;
                        }
                    }
                }
                MovingAverageDao mvd = new MovingAverageDao(st.getSymbol(), maxShort, maxLong, maxProfit, DateTime.now());
                SqliteDriver.executeInsert(InsertionBuilder.aBuilder().withTableBuilder(mvd.getTableBuilder()).withParams(mvd.getParams()).execute());
//        System.out.println("------------------------");
                System.out.println(st.getSymbol() + " : " + String.format("Short: %s, Long: %s, MAX Profit: %s",
                        String.valueOf(maxShort), String.valueOf(maxLong), String.valueOf(maxProfit)));
            };
            pool.execute(task);
        });
        pool.shutdown();
        pool.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
    }
}
