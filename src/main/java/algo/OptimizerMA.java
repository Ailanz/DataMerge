package algo;

import core.Book;
import core.DayStrategyBuilder;
import core.TransactionRecord;
import dao.IndicatorDao;
import dao.StockDao;
import dao.StockPriceDao;
import grabber.LivePrice;
import org.apache.commons.lang3.tuple.Pair;
import org.joda.time.DateTime;
import ui.StockFilter;
import util.TimeRange;

import java.sql.Time;
import java.util.List;
import java.util.Map;

public class OptimizerMA {

    public static void main(String args[]){
//        List<StockDao> stocks = StockFilter.marketCapFilter(StockDao.getAllStocks());
//        stocks.forEach(s->{
//            try {
//                optimizeEarnings(s.getSymbol());
//            }catch(Exception e) {}
//        });
        optimizeEarnings("AAPL");
    }

    public static Pair<Integer, Integer> optimizeEarnings(String stockSymbol){
        int max = 24;
        int min = 3;
        StockDao stock = StockDao.getStock(stockSymbol);
        List<StockPriceDao> prices = LivePrice.getDaysPrice(stockSymbol);
        Map<DateTime, IndicatorDao> indicators = DayStrategyBuilder.getIndicatorMap(stockSymbol);
        TimeRange timeRange = new TimeRange(DateTime.now().minusMonths(3));

        int maxShort  = min;
        int maxLong = min;
        double maxProfit = -100000000;
        double latestPrice = prices.get(prices.size()-1).getClose();

        for(int i = min; i < max; i++) {
            for(int j = i+1; j < max; j++) {
                Book book = new Book();
                MovingAverage s = new ExponentialMovingAverage(i);
                MovingAverage l = new ExponentialMovingAverage(j);
                List<TransactionRecord> transactions = DayStrategyBuilder.aBuilder()
                        .withMovingAverages(s, l)
                        .withBuyAfterDate(DateTime.now().minusDays(100))
                        .withTimeRange(timeRange)
                        .execute(stock, prices, indicators);
                book.addTransaction(transactions);
                double profit = book.totalPNL(latestPrice);
//                System.out.println(String.format("Short: %s, Long: %s, Profit: %s",
//                        String.valueOf(i), String.valueOf(j), String.valueOf(profit)));
                if(profit > maxProfit) {
                    maxShort = i;
                    maxLong = j;
                    maxProfit = profit;
                }
            }
        }
//        System.out.println("------------------------");
        System.out.println(stockSymbol + " : " + String.format("Short: %s, Long: %s, MAX Profit: %s",
                String.valueOf(maxShort), String.valueOf(maxLong), String.valueOf(maxProfit)));
        return Pair.of(maxShort,maxLong);
    }
}
