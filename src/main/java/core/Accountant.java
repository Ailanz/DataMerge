package core;

import algo.ExponentialMovingAverage;
import algo.MovingAverage;
import dao.StockDao;
import org.joda.time.DateTime;
import ui.StockFilter;
import util.TimeRange;

import java.util.List;
import java.util.Stack;

/**
 * Created by Ailan on 9/4/2017.
 */
public class Accountant {
    //THE accountant
    public Accountant() {

    }

    public static void main(String args[]) {
        List<StockDao> allStocks = StockDao.getAllStocks();
        allStocks = StockFilter.marketCapFilter(allStocks);
        Book book = new Book();
        for (StockDao stock : allStocks) {
            MovingAverage s = new ExponentialMovingAverage(12);
            MovingAverage l = new ExponentialMovingAverage(24);
            List<TransactionRecord> transactions = StrategyBuilder.aBuilder()
                    .withTimeRange(new TimeRange(DateTime.now().minusDays(400), DateTime.now()))
                    .withBuyAfterDate(DateTime.now().minusDays(100))
                    .withMovingAverages(s, l)
                    .execute(StockDao.getStock(stock.getSymbol()));
            Accountant acct = new Accountant();
            book.addTransaction(transactions);
        }
        book.printSummary();
//        System.out.println("Total: " + sum / count + " pos : " + pos + " , neg : " + neg);
    }
    
}
