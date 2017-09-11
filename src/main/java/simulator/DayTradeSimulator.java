package simulator;

import algo.ExponentialMovingAverage;
import algo.MovingAverage;
import algo.OptimizerMA;
import core.*;
import dao.*;
import db.InsertionBuilder;
import db.SqliteDriver;
import grabber.*;
import org.apache.commons.lang3.tuple.Pair;
import org.joda.time.DateTime;
import ui.StockFilter;
import util.TimeRange;

import java.sql.Time;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by Ailan on 9/7/2017.
 */
public class DayTradeSimulator {

    static Map<String, List<DayDataDao>> cachedDayData = new HashMap<>();
    static{
        List<DayDataDao> allData = DayDataDao.getAllDayData();
        getDayDataMap(allData);
    }

    public static void main(String args[]) throws InterruptedException {
        DateTime minDate = new DateTime(2017,9,5,0,0);
        DateTime maxDate = new DateTime(2017,9,6,0,0);
        TimeRange timeRange = new TimeRange(minDate, maxDate);
        ExecutorService pool = Executors.newFixedThreadPool(100);

        List<StockDao> allStocks = StockDao.getAllStocks();
        allStocks = StockFilter.marketCapFilter(allStocks);
        Book book = new Book();
        for (StockDao stock : allStocks) {
            Runnable task = () -> {
//                MovingAverageDao mv = stock.getMovingAverage();
                List<DayDataDao> data = getData(stock, new TimeRange(DateTime.now().minusDays(200)), false);
                if(data.size()==0) return;
                MovingAverage s = new ExponentialMovingAverage(data.get(0).getShortMA());
                MovingAverage l = new ExponentialMovingAverage(data.get(0).getLongMA());

                List<TransactionRecord> transactions = DayStrategyBuilder.aBuilder()
                        .withBuyAfterDate(minDate)
                        .withTimeRange(timeRange)
                        .withMovingAverages(s, l)
                        .withValueToFulfill(100)
                        .execute(data);
                Accountant acct = new Accountant();

                book.addTransaction(transactions);
            };
            pool.execute(task);
        }
        pool.shutdown();
        pool.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
        book.printSummary();
    }

    public static synchronized List<DayDataDao> getData(StockDao stock, TimeRange timeRange, boolean forceReinsert){
        List<DayDataDao> data = cachedDayData.get(stock.getSymbol());
        if(data.size() > 0 && !forceReinsert){
            return data;
        }
        List<StockPriceDao> prices = LivePrice.getDaysPrice(stock.getSymbol()).stream().filter(s->timeRange.isWithin(s.getDate())).collect(Collectors.toList());
        Map<DateTime, IndicatorDao> indicators = DayStrategyBuilder.getIndicatorMap(stock.getSymbol());
        InsertionBuilder builder = InsertionBuilder.aBuilder().withTableBuilder(DayDataDao.getTableBuilder());
        for(int i=0; i < prices.size(); i++){
            MovingAverageDao mv = stock.getMovingAverage();
            double adx = indicators.get(prices.get(i).getDate()) == null ? -1 : indicators.get(prices.get(i).getDate()).getAdx();
            DayDataDao day = new DayDataDao(stock.getSymbol(), prices.get(i).getDate(), prices.get(i).getClose(), adx,
                    mv.getShortMA(), mv.getLongMA(), 0);
            data.add(day);
            builder.withParams(day.getParams());
        }
        SqliteDriver.executeInsert(builder.execute());
        System.out.println("Processed: " + stock.getSymbol());
        return data;
    }

    public static void getDayDataMap(List<DayDataDao> data){
        data.forEach(s->{
            cachedDayData.computeIfAbsent(s.getSymbol(), v-> new LinkedList<>());
            cachedDayData.get(s.getSymbol()).add(s);
        });
    }

}
