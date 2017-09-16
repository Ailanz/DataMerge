package simulator;

import algo.ExponentialMovingAverage;
import algo.MovingAverage;
import core.Book;
import core.strategy.CandleStickStrategyBuilder;
import core.strategy.DayStrategyBuilder;
import core.TransactionRecord;
import core.strategy.ExitIntervalEnum;
import dao.*;
import db.InsertionBuilder;
import db.SqliteDriver;
import grabber.AlphaVantageEnum;
import grabber.LivePrice;
import org.joda.time.DateTime;
import ui.StockFilter;
import util.TimeRange;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
//ZjksXwDUZ-xqy18TZM0ivt9ky7pZDv_T0

/**
 * Created by Ailan on 9/7/2017.
 */
public class DayTradeSimulator {

    static Map<String, List<DayDataDao>> cachedDayData = new HashMap<>();

    static {
        List<DayDataDao> allData = DayDataDao.getAllDayData();
        getDayDataMap(allData);
    }

    public static void main(String args[]) throws InterruptedException {
        int date = 13 ;
        DateTime minDate = new DateTime(2017, 9, date, 0, 0);
        DateTime maxDate = new DateTime(2017, 9, date+1, 0, 0);
        TimeRange timeRange = new TimeRange(minDate, maxDate);
        ExecutorService pool = Executors.newFixedThreadPool(20);

        List<StockDao> allStocks = StockDao.getAllStocks();
        allStocks = StockFilter.marketCapFilter(allStocks);
        Book book = new Book();
        for (StockDao stock : allStocks) {
            Runnable task = () -> {
                List<DayDataDao> data = getData(stock, new TimeRange(DateTime.now().minusDays(200)), timeRange, false);
                if (data.size() == 0) return;
                MovingAverage s = new ExponentialMovingAverage(data.get(0).getShortMA());
                MovingAverage l = new ExponentialMovingAverage(data.get(0).getLongMA());

                List<TransactionRecord> transactions = CandleStickStrategyBuilder.aBuilder()
//                        .withBuyAfterDate(minDate)
//                        .withBuyAfterDate()
//                        .withTimeRange(timeRange)
//                        .withMovingAverages(s, l)
//                        .withSellHigher(true)
                        .withExitInterval(ExitIntervalEnum.NEVER)
                        .withValueToFulfill(200)
                        .execute(stock);
//                        .execute(data);

                book.addTransaction(transactions);
            };
            pool.execute(task);
        }
        pool.shutdown();
        pool.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
        book.printSummaryTemporal();
    }

    public static List<DayDataDao> getData(StockDao stock, TimeRange dataRange, TimeRange filterRange, boolean forceReinsert) {
        List<DayDataDao> data = cachedDayData.computeIfAbsent(stock.getSymbol(), v -> new LinkedList<>());
        data = data.stream().filter(d -> filterRange.isWithin(d.getDate())).collect(Collectors.toList());
        if (data != null && data.size() > 0 && !forceReinsert) {
            return data;
        }

        constructAndInsertDayData(stock, dataRange, data);
        System.out.println("Processed: " + stock.getSymbol());
        return data;
    }

    private static void constructAndInsertDayData(StockDao stock, TimeRange dataRange, List<DayDataDao> data) {
        List<StockPriceDao> prices = LivePrice.getDaysPrice(stock.getSymbol()).stream()
                .filter(s -> dataRange.isWithin(s.getDate())).collect(Collectors.toList());

        Map<DateTime, IndicatorDao> indicators = DayStrategyBuilder.getIndicatorMap(stock.getSymbol(), AlphaVantageEnum.Interval.FIVE);
        InsertionBuilder builder = InsertionBuilder.aBuilder().withTableBuilder(DayDataDao.getTableBuilder());

        for (StockPriceDao price : prices) {
            MovingAverageDao mv = stock.getMovingAverage();
            double adx = indicators.get(price.getDate()) == null ? -1 : indicators.get(price.getDate()).getAdx();
            DayDataDao day = new DayDataDao(stock.getSymbol(), price.getDate(), price.getOpen(), price.getClose(), price.getHigh(),
                    price.getLow(), price.getVolume(), adx, mv.getShortMA(), mv.getLongMA(), 0);
            data.add(day);
            builder.withParams(day.getParams());
        }
        SqliteDriver.executeInsert(builder.execute());
    }

    public static void getDayDataMap(List<DayDataDao> data) {
        data.forEach(s -> {
            cachedDayData.computeIfAbsent(s.getSymbol(), v -> new LinkedList<>());
            cachedDayData.get(s.getSymbol()).add(s);
        });
    }

}
