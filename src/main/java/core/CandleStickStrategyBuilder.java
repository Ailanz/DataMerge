package core;

import algo.CandleStick;
import algo.CandleStickPattern;
import algo.MovingAverage;
import dao.DayDataDao;
import org.joda.time.DateTime;
import util.TimeRange;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Ailan on 9/12/2017.
 */
public class CandleStickStrategyBuilder extends DayStrategyBuilder{
    protected  CandleStickStrategyBuilder(){}

    public static CandleStickStrategyBuilder aBuilder() {
        return new CandleStickStrategyBuilder();
    }


    @Override
    public CandleStickStrategyBuilder withMaxLoss(double maxLoss) {
        super.withMaxLoss(maxLoss);
        return this;
    }

    @Override
    public CandleStickStrategyBuilder withMovingAverages(MovingAverage s, MovingAverage l) {
        super.withMovingAverages(s, l);
        return this;

    }

    @Override
    public CandleStickStrategyBuilder withTimeRange(TimeRange range) {
        super.withTimeRange(range);
        return this;

    }

    @Override
    public CandleStickStrategyBuilder withBuyAfterDate(DateTime buyAfter) {
        super.withBuyAfterDate(buyAfter);
        return this;
    }

    @Override
    public CandleStickStrategyBuilder withValueToFulfill(double valueToFulfill) {
        super.withValueToFulfill(valueToFulfill);
        return this;
    }

    @Override
    public CandleStickStrategyBuilder withSellHigher(boolean sellHigher) {
        super.withSellHigher(sellHigher);
        return this;

    }

    @Override
    public boolean buyCondition(DayDataDao data, DateTime eod) {
        return super.buyCondition(data, eod);
    }

    @Override
    public List<TransactionRecord> execute(List<DayDataDao> data) {
        List<TransactionRecord> records = new LinkedList<>();
        try {
            if (super.timeRange != null) {
                data = data.stream().filter(s -> timeRange.isWithin(s.getDate())).collect(Collectors.toList());
            }

            data = data.stream().sorted((o1, o2) -> o1.getDate().isBefore(o2.getDate()) ? -1 : 1).collect(Collectors.toList());

            double spread = 0.05;
            Boolean isShortOverLong = null;
            int holdingShares = 0;
            double holdingPrice = 0;
            CandleStickPattern pattern = new CandleStickPattern();
            for (DayDataDao sp : data) {
                CandleStick stick = new CandleStick(sp.getOpen(), sp.getClose(), sp.getHigh(), sp.getLow(), sp.getDate(), sp.getVolume());
                pattern.addCandleStick(stick);
                if(pattern.isThreeLineStrikes()){
                    System.out.println("YUSH!");
                }
                double price = sp.getClose();
                int numOfSharesToBuy = getNumSharesToBuy(price + spread);
                DateTime curDate = sp.getDate();
                DateTime eod = new DateTime(curDate.toString()).withHourOfDay(15).withMinuteOfHour(0);
//                CandleStick stick = new CandleStick(sp.getOp)
                if (buyAfterDate == null || curDate.isAfter(buyAfterDate)) {


                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Skipping: " + data.get(0).getSymbol());
        }
        return records;
    }
}
