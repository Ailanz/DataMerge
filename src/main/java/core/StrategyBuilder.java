package core;

import algo.ExponentialMovingAverage;
import algo.MovingAverage;
import dao.StockDao;
import dao.StockPriceDao;
import org.joda.time.DateTime;
import util.TimeRange;

import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Ailan on 9/4/2017.
 */
public class StrategyBuilder {
    private double maxLossPercent = 0.1;
    private MovingAverage shortMA;
    private MovingAverage longMA;
    private TimeRange timeRange;
    private LocalDate buyAfterDate;

    private StrategyBuilder() {}

    public static void main(String args[]){
        MovingAverage s = new ExponentialMovingAverage(12);
        MovingAverage l = new ExponentialMovingAverage(24);
        List<TransactionRecord> transactions = StrategyBuilder.aBuilder()
                .withTimeRange(new TimeRange(LocalDate.now().minusDays(400), LocalDate.now()))
                .withBuyAfterDate(LocalDate.now().minusDays(200))
                .withMovingAverages(s,l)
                .execute(StockDao.getStock("ABCB"));
        System.out.println(transactions);
    }

    public static StrategyBuilder aBuilder(){
        return new StrategyBuilder();
    }

    public StrategyBuilder withMaxLoss(double maxLoss){
        this.maxLossPercent = maxLoss;
        return this;
    }

    public StrategyBuilder withMovingAverages(MovingAverage s, MovingAverage l){
        this.shortMA = s;
        this.longMA = l;
        return this;
    }

    public StrategyBuilder withTimeRange(TimeRange range){
        this.timeRange = range;
        return this;
    }

    public StrategyBuilder withBuyAfterDate(LocalDate buyAfter){
        this.buyAfterDate = buyAfter;
        return this;
    }

    public List<TransactionRecord> execute(StockDao stock){
        List<TransactionRecord> records = new LinkedList<>();
        List<StockPriceDao> prices = stock.getPrices();
        if(timeRange != null) {
            prices = prices.stream().filter(s->timeRange.isWithin(s.getDate())).collect(Collectors.toList());
        }

        prices = prices.stream().sorted((o1, o2) -> o1.getDate().isBefore(o2.getDate()) ? -1 : 1).collect(Collectors.toList());

        double spread = 0;
        Boolean isShortOverLong = null;
        boolean holdingStock = false;
        double holdingPrice = 0;
        for(StockPriceDao sp : prices){
//            double price = (sp.getHigh() + sp.getLow())/2;
            double price = sp.getClose();
            LocalDate curDate = sp.getDate();
            shortMA.add(price);
            longMA.add(price);

            if(buyAfterDate==null || curDate.isAfter(buyAfterDate)) {
                double shortAvg = shortMA.getAverage();
                double longAvg = longMA.getAverage();

                if (isShortOverLong == null) {
                    isShortOverLong = shortAvg > longAvg;
                }

                if(holdingStock && price <= holdingPrice*(1-maxLossPercent)){
                    records.add(TransactionRecord.exit(DateTime.parse(curDate.toString()), stock.getSymbol(), price - spread));
                    holdingStock = false;
                }

                if(!isShortOverLong && shortAvg > longAvg) {
                    if(!holdingStock) {
                        records.add(TransactionRecord.buy(DateTime.parse(curDate.toString()), stock.getSymbol(), price + spread));
                        holdingPrice = price;
                        holdingStock = true;
                    }
                    isShortOverLong = true;
                }

                if(isShortOverLong && longAvg > shortAvg){
                    if(holdingStock && holdingPrice < price) {
                        records.add(TransactionRecord.sell(DateTime.parse(curDate.toString()), stock.getSymbol(), price - spread));
                        holdingStock = false;
                    }
                    isShortOverLong = false;
                }
            }
        }
        return records;
    }
}
