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
        double sum = 0;
        List<StockDao> allStocks = StockDao.getAllStocks();
        allStocks = StockFilter.marketCapFilter(allStocks);

        int count = 0;
        for (StockDao stock : allStocks) {
            MovingAverage s = new ExponentialMovingAverage(12);
            MovingAverage l = new ExponentialMovingAverage(24);
            List<TransactionRecord> transactions = StrategyBuilder.aBuilder()
                    .withTimeRange(new TimeRange(DateTime.now().minusDays(400), DateTime.now()))
                    .withBuyAfterDate(DateTime.now().minusDays(100))
                    .withMovingAverages(s, l)
                    .execute(StockDao.getStock(stock.getSymbol()));
            Accountant acct = new Accountant();
            double earnings = acct.calculatePLPercentage(transactions) - 1;

            if (!Double.isNaN(earnings)) {
                count++;
                sum += earnings;
                System.out.println(stock.getSymbol() + " : " + earnings);
            }
        }
        System.out.println("Total: " + sum / count);
    }

    private double calculatePLPercentage(List<TransactionRecord> records) {
        double sum = 0;
        Stack<TransactionRecord> stack = new Stack<>();
        int sells = 0;
        for (TransactionRecord record : records) {
            if (record.getType() == TransactionRecord.Type.BUY) {
                stack.push(record);
//                sum -= record.getPrice();
            }

            if (record.getType() == TransactionRecord.Type.SELL || record.getType() == TransactionRecord.Type.EXIT) {
                sum += record.getPrice() / stack.pop().getPrice();
                sells++;
            }
        }

        while (!stack.isEmpty()) {
            sum += StockDao.getStock(records.get(0).getSymbol()).getLatestPrice().getClose() / stack.pop().getPrice();
            sells++;
        }
        return sum / sells;
    }

    private double calculatePL(List<TransactionRecord> records) {
        double sum = 0;
        Stack<TransactionRecord> stack = new Stack<>();
        for (TransactionRecord record : records) {
            if (record.getType() == TransactionRecord.Type.BUY) {
                stack.push(record);
                sum -= record.getPrice();
            }

            if (record.getType() == TransactionRecord.Type.SELL || record.getType() == TransactionRecord.Type.EXIT) {
                stack.pop();
                sum += record.getPrice();
            }
        }

        while (!stack.isEmpty()) {
            sum += stack.pop().getPrice();
        }
        return sum;
    }
}
