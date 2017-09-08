package simulator;

import algo.ExponentialMovingAverage;
import algo.MovingAverage;
import core.*;
import dao.StockDao;
import dao.StockPriceDao;
import grabber.AlphaVantageBuilder;
import grabber.AlphaVantageEnum;
import grabber.DailyPriceGrabber;
import grabber.ResultData;
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

    static AlphaVantageBuilder builder = AlphaVantageBuilder.aBuilder()
            .withFunction(AlphaVantageEnum.Function.TIME_SERIES_INTRADAY)
            .withOutputSize(AlphaVantageEnum.OutputSize.COMPACT)
            .withInterval(AlphaVantageEnum.Interval.FIVE);

    public static void main(String args[]) throws InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(100);

        List<StockDao> allStocks = StockDao.getAllStocks();
        allStocks = StockFilter.marketCapFilter(allStocks);
        Book book = new Book();
        for (StockDao stock : allStocks) {
            Runnable task = () -> {
                MovingAverage s = new ExponentialMovingAverage(5);
                MovingAverage l = new ExponentialMovingAverage(8);
                List<TransactionRecord> transactions = DayStrategyBuilder.aBuilder()
                        .withBuyAfterDate(DateTime.now().minusDays(1))
                        .withMovingAverages(s, l)
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
