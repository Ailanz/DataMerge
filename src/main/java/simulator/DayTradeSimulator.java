package simulator;

import algo.ExponentialMovingAverage;
import algo.MovingAverage;
import algo.OptimizerMA;
import core.*;
import dao.StockDao;
import dao.StockPriceDao;
import grabber.AlphaVantageBuilder;
import grabber.AlphaVantageEnum;
import grabber.DailyPriceGrabber;
import grabber.ResultData;
import org.apache.commons.lang3.tuple.Pair;
import org.joda.time.DateTime;
import ui.StockFilter;
import util.TimeRange;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by Ailan on 9/7/2017.
 */
public class DayTradeSimulator {


    public static void main(String args[]) throws InterruptedException {
        DateTime minDate = new DateTime(2017,9,6,0,0);
        DateTime maxDate = new DateTime(2017,9,7,0,0);
        ExecutorService pool = Executors.newFixedThreadPool(100);

        List<StockDao> allStocks = StockDao.getAllStocks();
        allStocks = StockFilter.marketCapFilter(allStocks);
        Book book = new Book();
        for (StockDao stock : allStocks) {
            Runnable task = () -> {
                Pair<Integer,Integer> lengths = OptimizerMA.optimizeEarnings(stock.getSymbol());
                MovingAverage s = new ExponentialMovingAverage(lengths.getLeft());
                MovingAverage l = new ExponentialMovingAverage(lengths.getRight());
                List<TransactionRecord> transactions = DayStrategyBuilder.aBuilder()
                        .withBuyAfterDate(minDate)
                        .withTimeRange(new TimeRange(minDate, maxDate))
                        .withMovingAverages(s, l)
                        .withValueToFulfill(100)
                        .execute(stock);
                Accountant acct = new Accountant();

                book.addTransaction(transactions);
            };
            pool.execute(task);
        }
        pool.shutdown();
        pool.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
        book.printSummary();
    }

}
