package core.strategy;

import algo.ExponentialMovingAverage;
import algo.MovingAverage;
import core.TransactionRecord;
import dao.DayDataDao;
import dao.IndicatorDao;
import dao.StockDao;
import dao.StockPriceDao;
import grabber.DailyIndicatorGrabber;
import grabber.LivePrice;
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
public class DayStrategyBuilder extends StrategyBuilder<DayStrategyBuilder>{

    protected DayStrategyBuilder() {
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

    public static StrategyBuilder<DayStrategyBuilder> aBuilder() {
        return new DayStrategyBuilder();
    }


    public boolean buyCondition(DayDataDao data, DateTime eod) {
//        return true;
        return data.getAdx() > 30 && data.getDate().isBefore(eod);
        // && indicator.getRsi7() < 60;
//       && indicator.getRsi14() < 60 && indicator.getRsi25() < 60;
    }



    public List<TransactionRecord> execute(List<DayDataDao> data) {
        List<TransactionRecord> records = new LinkedList<>();
        try {
            MovingAverage shortMA = getShortMA();
            MovingAverage longMA = getLongMA();
            if (getTimeRange() != null) {
                data = data.stream().filter(s -> getTimeRange().isWithin(s.getDate())).collect(Collectors.toList());
            }

            data = data.stream().sorted((o1, o2) -> o1.getDate().isBefore(o2.getDate()) ? -1 : 1).collect(Collectors.toList());

            double spread = 0.05;
            Boolean isShortOverLong = null;
            int holdingShares = 0;
            double holdingPrice = 0;
            int rep = 0;

            for (DayDataDao sp : data) {
                double price = sp.getClose();
                int numOfSharesToBuy = getNumSharesToBuy(price + spread);
                shortMA.add(price);
                DateTime curDate = sp.getDate();
                DateTime eod = new DateTime(curDate.toString()).withHourOfDay(15).withMinuteOfHour(0);
                longMA.add(price);
                rep++;

                if (getBuyAfterDate() == null || curDate.isAfter(getBuyAfterDate())) {
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

                    if (holdingShares > 0 && price <= holdingPrice * (1 - getMaxLossPercent())) {
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

    protected boolean sellCondition(double boughtPrice, double currentPrice, double spread){
        if(isSellHigher()) {
            return boughtPrice < currentPrice - spread;
        }
        return true;
    }


}
