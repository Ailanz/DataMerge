package core;

import algo.ExponentialMovingAverage;
import algo.MovingAverage;
import dao.DayDataDao;
import dao.IndicatorDao;
import dao.StockDao;
import dao.StockPriceDao;
import grabber.AlphaVantageEnum;
import grabber.DailyIndicatorGrabber;
import grabber.LivePrice;
import jdk.management.resource.internal.inst.DatagramDispatcherRMHooks;
import org.joda.time.DateTime;
import util.TimeRange;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static grabber.AlphaVantageEnum.*;

/**
 * Created by Ailan on 9/7/2017.
 * OMG SO MUCH DUPLICATED CODE FROM STRATEGY BUILDER
 */
public class DayStrategyBuilder {
    private double maxLossPercent = 0.1;
    private MovingAverage shortMA;
    private MovingAverage longMA;
    private TimeRange timeRange;
    private DateTime buyAfterDate;
    private double valueToFulfill;
    private boolean sellLHigher = false;

    private DayStrategyBuilder() {
    }

    public static void main(String args[]) {
        DateTime minDate = new DateTime(2017, 9, 5, 0, 0);
        DateTime maxDate = new DateTime(2017, 9, 6, 0, 0);
        TimeRange timeRange = new TimeRange(minDate, maxDate);
        MovingAverage s = new ExponentialMovingAverage(5);
        MovingAverage l = new ExponentialMovingAverage(13);
        List<TransactionRecord> transactions = DayStrategyBuilder.aBuilder()
                .withBuyAfterDate(minDate)
                .withMovingAverages(s, l)
                .withTimeRange(timeRange)
                .execute(StockDao.getStock("HIW"));
        System.out.println(transactions);
    }

    public static DayStrategyBuilder aBuilder() {
        return new DayStrategyBuilder();
    }

    public DayStrategyBuilder withMaxLoss(double maxLoss) {
        this.maxLossPercent = maxLoss;
        return this;
    }

    public DayStrategyBuilder withMovingAverages(MovingAverage s, MovingAverage l) {
        this.shortMA = s;
        this.longMA = l;
        return this;
    }

    public DayStrategyBuilder withTimeRange(TimeRange range) {
        this.timeRange = range;
        return this;
    }

    public DayStrategyBuilder withBuyAfterDate(DateTime buyAfter) {
        this.buyAfterDate = buyAfter;
        return this;
    }

    public DayStrategyBuilder withValueToFulfill(double valueToFulfill) {
        this.valueToFulfill = valueToFulfill;
        return this;
    }

    public DayStrategyBuilder withSellHigher(boolean sellHigher) {
        this.sellLHigher = sellHigher;
        return this;
    }

    public boolean buyCondition(DayDataDao data, DateTime eod) {
//        return true;
        return data.getAdx() > 30 && data.getDate().isBefore(eod);
        // && indicator.getRsi7() < 60;
//       && indicator.getRsi14() < 60 && indicator.getRsi25() < 60;
    }

    public List<TransactionRecord> execute(StockDao stock) {
        return execute(stock, LivePrice.getDaysPrice(stock.getSymbol()), getIndicatorMap(stock.getSymbol()));
    }

    public List<TransactionRecord> execute(StockDao stock, List<StockPriceDao> prices, Map<DateTime, IndicatorDao> indicators) {
        List<DayDataDao> data = new LinkedList<>();
        double lastAdx = -1;
        for(int i=0; i < prices.size(); i++){
            double adx = lastAdx;
            IndicatorDao ind = indicators.get(prices.get(i).getDate());
            if(ind!=null) {
                adx =ind.getAdx();
            }
            data.add(new DayDataDao(stock.getSymbol(), prices.get(i).getDate(), prices.get(i).getClose(), adx,
                    this.shortMA.getInterval(), this.longMA.getInterval(), 0));
        }
        return execute(data);
    }

    public List<TransactionRecord> execute(List<DayDataDao> data) {
        List<TransactionRecord> records = new LinkedList<>();
        try {
            if (timeRange != null) {
                data = data.stream().filter(s -> timeRange.isWithin(s.getDate())).collect(Collectors.toList());
            }

            data = data.stream().sorted((o1, o2) -> o1.getDate().isBefore(o2.getDate()) ? -1 : 1).collect(Collectors.toList());

            double spread = 0.05;
            Boolean isShortOverLong = null;
            int holdingShares = 0;
            double holdingPrice = 0;
            int rep = 0;

            for (DayDataDao sp : data) {
                double price = sp.getPrice();
                int numOfSharesToBuy = getNumSharesToBuy(price + spread);
                shortMA.add(price);
                DateTime curDate = sp.getDate();
                DateTime eod = new DateTime(curDate.toString()).withHourOfDay(15).withMinuteOfHour(0);
                longMA.add(price);
                rep++;

                if (buyAfterDate == null || curDate.isAfter(buyAfterDate)) {
                    double shortAvg = shortMA.getAverage();
                    double longAvg = longMA.getAverage();

                    if (isShortOverLong == null) {
                        isShortOverLong = shortAvg > longAvg;
                    }

                    //liquid EOD
                    if (holdingShares > 0 && curDate.isAfter(eod)) {
                        records.add(TransactionRecord.exit(DateTime.parse(curDate.toString()), sp.getSymbol(), holdingShares, price - spread));
                        holdingShares = 0;
                    }

                    if (holdingShares > 0 && price <= holdingPrice * (1 - maxLossPercent)) {
                        records.add(TransactionRecord.exit(DateTime.parse(curDate.toString()), sp.getSymbol(), holdingShares, price - spread));
                        holdingShares = 0;
                    }

                    if (!isShortOverLong && shortAvg > longAvg && rep > longMA.getInterval()) {
                        if (holdingShares == 0 && buyCondition(sp, eod)) {
                            records.add(TransactionRecord.buy(DateTime.parse(curDate.toString()), sp.getSymbol(), numOfSharesToBuy, price + spread));
                            holdingPrice = price + spread;
                            holdingShares = numOfSharesToBuy;
                        }
                        isShortOverLong = true;
                    }

                    if (isShortOverLong && longAvg > shortAvg) {
                        if (holdingShares > 0 && sellCondition(holdingPrice, price, spread)) {
                            records.add(TransactionRecord.sell(DateTime.parse(curDate.toString()), sp.getSymbol(), holdingShares, price - spread));
                            holdingShares = 0;
                        }
                        isShortOverLong = false;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Skipping: " + data.get(0).getSymbol());
        }
        return records;
    }

    private boolean sellCondition(double boughtPrice, double currentPrice, double spread){
        if(sellLHigher) {
            return boughtPrice < currentPrice - spread;
        }
        return true;
    }

    private int getNumSharesToBuy(double price) {
        if (valueToFulfill == 0) {
            return 1;
        } else {
            return new Double(valueToFulfill / price).intValue();
        }
    }

    public static Map<DateTime, IndicatorDao> getIndicatorMap(String symbol) {
        List<IndicatorDao> indicators = DailyIndicatorGrabber.getIndicators(symbol, Interval.FIVE, 7);
        Map<DateTime, IndicatorDao> map = new HashMap<>();
        indicators.forEach(i -> map.put(i.getDate(), i));
        return map;
    }


}
