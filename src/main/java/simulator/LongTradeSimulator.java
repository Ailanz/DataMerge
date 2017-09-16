package simulator;

import algo.ExponentialMovingAverage;
import algo.MovingAverage;
import core.Book;
import core.TransactionRecord;
import core.strategy.CandleStickStrategyBuilder;
import core.strategy.ExitIntervalEnum;
import dao.DayDataDao;
import dao.StockDao;
import org.joda.time.DateTime;
import ui.StockFilter;
import util.TimeRange;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by Ailan on 9/15/2017.
 */
public class LongTradeSimulator {

    public static void main(String args[]) throws InterruptedException {

        List<StockDao> allStocks =  StockFilter.marketCapFilter(StockDao.getAllStocks());

        Book book = new Book();
        for (StockDao stock : allStocks) {

                List<TransactionRecord> transactions = CandleStickStrategyBuilder.aBuilder()
                        .withExitInterval(ExitIntervalEnum.NEVER)
                        .withValueToFulfill(200)
                        .execute(stock);

                book.addTransaction(transactions);
        }

        book.printSummaryTemporal();
    }
}
