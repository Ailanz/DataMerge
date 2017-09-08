package core;

import algo.ExponentialMovingAverage;
import algo.MovingAverage;
import dao.IndicatorDao;
import dao.StockDao;
import dao.StockPriceDao;
import grabber.AlphaVantageEnum;
import grabber.DailyIndicatorGrabber;
import grabber.LivePrice;
import org.joda.time.DateTime;
import util.TimeRange;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    private DayStrategyBuilder() {
    }

    public static void main(String args[]) {
        DateTime minDate = new DateTime(2017, 9, 6, 0, 0);
        DateTime maxDate = new DateTime(2017, 9, 7, 0, 0);
        TimeRange timeRange = new TimeRange(minDate, maxDate);
        MovingAverage s = new ExponentialMovingAverage(5);
        MovingAverage l = new ExponentialMovingAverage(13);
        List<TransactionRecord> transactions = DayStrategyBuilder.aBuilder()
                .withBuyAfterDate(minDate)
                .withMovingAverages(s, l)
                .withTimeRange(timeRange)
                .execute(StockDao.getStock("NWSA"));
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

    public boolean buyCondition(IndicatorDao indicator) {
        if (indicator == null) {
            return false;
        }
//        return true;
        return indicator.getAdx() > 30;
        // && indicator.getRsi7() < 60;
//       && indicator.getRsi14() < 60 && indicator.getRsi25() < 60;
    }

    public List<TransactionRecord> execute(StockDao stock) {
        return execute(stock, LivePrice.getDaysPrice(stock.getSymbol()), getIndicatorMap(stock.getSymbol()));
    }

    public List<TransactionRecord> execute(StockDao stock, List<StockPriceDao> prices, Map<DateTime, IndicatorDao> indicators) {
        List<TransactionRecord> records = new LinkedList<>();
        try {
            if (timeRange != null) {
                prices = prices.stream().filter(s -> timeRange.isWithin(s.getDate())).collect(Collectors.toList());
            }

            prices = prices.stream().sorted((o1, o2) -> o1.getDate().isBefore(o2.getDate()) ? -1 : 1).collect(Collectors.toList());

            double spread = 0.05;
            Boolean isShortOverLong = null;
            int holdingShares = 0;
            double holdingPrice = 0;
            int rep = 0;

            for (StockPriceDao sp : prices) {
                double price = sp.getClose();
                int numOfSharesToBuy = getNumSharesToBuy(price + spread);
                DateTime curDate = sp.getDate();
                shortMA.add(price);
                longMA.add(price);
                rep++;
                IndicatorDao indicator = indicators.get(sp.getDate());

                if (buyAfterDate == null || curDate.isAfter(buyAfterDate)) {
                    double shortAvg = shortMA.getAverage();
                    double longAvg = longMA.getAverage();

                    if (isShortOverLong == null) {
                        isShortOverLong = shortAvg > longAvg;
                    }

                    if (holdingShares > 0 && price <= holdingPrice * (1 - maxLossPercent)) {
                        records.add(TransactionRecord.exit(DateTime.parse(curDate.toString()), stock.getSymbol(), holdingShares, price - spread));
                        holdingShares = 0;
                    }

                    if (!isShortOverLong && shortAvg > longAvg && rep > longMA.getInterval()) {
                        if (holdingShares == 0 && buyCondition(indicator)) {
                            records.add(TransactionRecord.buy(DateTime.parse(curDate.toString()),
                                    stock.getSymbol(), numOfSharesToBuy, price + spread));
                            holdingPrice = price + spread;
                            holdingShares = numOfSharesToBuy;
                        }
                        isShortOverLong = true;
                    }

                    if (isShortOverLong && longAvg > shortAvg) {
                        if (holdingShares > 0) {
                            records.add(TransactionRecord.sell(DateTime.parse(curDate.toString()), stock.getSymbol(), holdingShares, price - spread));
                            holdingShares = 0;
                        }
                        isShortOverLong = false;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Skipping: " + stock.getSymbol());
        }
        return records;
    }

    private int getNumSharesToBuy(double price) {
        if (valueToFulfill == 0) {
            return 1;
        } else {
            return new Double(valueToFulfill / price).intValue();
        }
    }

    public static Map<DateTime, IndicatorDao> getIndicatorMap(String symbol) {
        List<IndicatorDao> indicators = DailyIndicatorGrabber.getIndicators(symbol, AlphaVantageEnum.Interval.FIVE, 7);
        Map<DateTime, IndicatorDao> map = new HashMap<>();
        indicators.forEach(i -> map.put(i.getDate(), i));
        return map;
    }


}
