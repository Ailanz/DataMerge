package core.strategy;

import algo.CandleStick;
import algo.CandleStickPattern;
import algo.MovingAverage;
import core.Notification;
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
        return super.buyCondition(data);
//        && volumeAverage.isAboveAverag();
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
                double price = sp.getClose();
                preprossData(records, spread, pattern, sp, price);
                int numOfSharesToBuy = getNumSharesToBuy(price + spread);
                if (getBuyAfterDate() == null || sp.getDate().isAfter(getBuyAfterDate())) {
                    if(pattern.isBullishAbandonedBaby()){
                        Notification.buy(sp.getSymbol(), price, "Bullish Abandoned Baby", sp.getDate());
                    }
                    if(pattern.isPreBullishAbandonedBaby()){
                        Notification.watch(sp.getSymbol(), price, "PRE Bullish Abandoned Baby", sp.getDate());
                    }

                    if(holdingPrice > 0 && (  pattern.isBearishAbandonedBaby() || sellCondition(sp))) {
                        records.add(TransactionRecord.sell(DateTime.parse(sp.getDate().toString()), sp.getSymbol(), numOfSharesToBuy, price - spread));
                        holdingPrice = 0;
                        continue;
                    }

                    if (pattern.isBullishAbandonedBaby() && buyCondition(sp)) {
                        if (holdingPrice == 0 ) {
                            records.add(TransactionRecord.buy(DateTime.parse(sp.getDate().toString()), sp.getSymbol(), numOfSharesToBuy, price + spread));
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

    private void preprossData(List<TransactionRecord> records, double spread, CandleStickPattern pattern, DayDataDao sp, double price) {
        volumeAverage.add(sp.getVolume());
        pattern.addCandleStick(new CandleStick(sp.getOpen(), sp.getClose(), sp.getHigh(), sp.getLow(), sp.getDate(), sp.getVolume()));
        records.forEach(r->r.processPotentialEarning(price - spread));
    }
}
