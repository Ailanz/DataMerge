package core.strategy;

import algo.CandleStick;
import algo.CandleStickPattern;
import algo.MovingAverage;
import core.TransactionRecord;
import dao.DayDataDao;
import org.joda.time.DateTime;
import ui.StockPriceDataSet;
import util.TimeRange;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * Created by Ailan on 9/12/2017.
 */
public class CandleStickStrategyBuilder extends StrategyBuilder<CandleStickStrategyBuilder>{
    protected  CandleStickStrategyBuilder(){}

    public static CandleStickStrategyBuilder aBuilder() {
        return new CandleStickStrategyBuilder();
    }

    @Override
    public boolean buyCondition(DayDataDao data) {
        return super.buyCondition(data) && volumeAverage.isAboveAverag();
    }

    @Override
    public boolean sellCondition(DayDataDao data) {
        return super.sellCondition(data);
    }



    @Override
    public List<TransactionRecord> execute(List<DayDataDao> data) {
        List<TransactionRecord> records = new LinkedList<>();
        try {
            double spread = 0.05;
            CandleStickPattern pattern = new CandleStickPattern();
            for (DayDataDao sp : data) {
                volumeAverage.add(sp.getVolume());
                CandleStick stick = new CandleStick(sp.getOpen(), sp.getClose(), sp.getHigh(), sp.getLow(), sp.getDate(), sp.getVolume());
                pattern.addCandleStick(stick);
                double price = sp.getClose();
                int numOfSharesToBuy = getNumSharesToBuy(price + spread);
                DateTime curDate = sp.getDate();
                DateTime eod = new DateTime(curDate.toString()).withHourOfDay(15).withMinuteOfHour(0);
//                CandleStick stick = new CandleStick(sp.getOp)
                if (getBuyAfterDate() == null || curDate.isAfter(getBuyAfterDate())) {

                    if(holdingPrice > 0 && ( pattern.isBearishEngulfing()) || sellCondition(sp)) {
                        records.add(TransactionRecord.sell(DateTime.parse(curDate.toString()), sp.getSymbol(), numOfSharesToBuy, price - spread));
                        holdingPrice = 0;
                        continue;
                    }

                    if (pattern.isBullishEngulfing() && buyCondition(sp)) {
                        if (holdingPrice == 0 ) {
                            records.add(TransactionRecord.buy(DateTime.parse(curDate.toString()), sp.getSymbol(), numOfSharesToBuy, price + spread));
                            holdingPrice = price + spread;
                            continue;
                        }
                    }

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Skipping: " + data.get(0).getSymbol());
        }

        if(!records.isEmpty()) {
            List<DayDataDao> buyAfterFilter = data.stream().filter(d->d.getDate().isAfter(getBuyAfterDate())).collect(Collectors.toList());
            StockPriceDataSet.saveChart(StockPriceDataSet.convertToXYDataSet(buyAfterFilter), data.get(0).getSymbol());
        }
        return records;
    }
}
