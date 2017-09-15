package core.strategy;

import algo.CandleStick;
import algo.CandleStickPattern;
import algo.MovingAverage;
import core.TransactionRecord;
import dao.DayDataDao;
import org.joda.time.DateTime;
import util.TimeRange;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Ailan on 9/12/2017.
 */
public class CandleStickStrategyBuilder extends StrategyBuilder<CandleStickStrategyBuilder>{
    protected  CandleStickStrategyBuilder(){}

    public static CandleStickStrategyBuilder aBuilder() {
        return new CandleStickStrategyBuilder();
    }

    public boolean buyCondition(DayDataDao data, DateTime eod) {
        return data.getDate().isBefore(eod);
    }

    public List<TransactionRecord> execute(List<DayDataDao> data) {
        List<TransactionRecord> records = new LinkedList<>();
        try {
            if (getTimeRange() != null) {
                data = data.stream().filter(s -> getTimeRange().isWithin(s.getDate())).collect(Collectors.toList());
            }

            data = data.stream().sorted((o1, o2) -> o1.getDate().isBefore(o2.getDate()) ? -1 : 1).collect(Collectors.toList());

            double spread = 0.05;
            int holdingShares = 0;
            double holdingPrice = 0;
            CandleStickPattern pattern = new CandleStickPattern();
            for (DayDataDao sp : data) {
                CandleStick stick = new CandleStick(sp.getOpen(), sp.getClose(), sp.getHigh(), sp.getLow(), sp.getDate(), sp.getVolume());
                pattern.addCandleStick(stick);
                if(pattern.isThreeLineStrikes()){
                    System.out.println("YUSH! " + sp.getSymbol());
                }
                double price = sp.getClose();
                int numOfSharesToBuy = getNumSharesToBuy(price + spread);
                DateTime curDate = sp.getDate();
                DateTime eod = new DateTime(curDate.toString()).withHourOfDay(15).withMinuteOfHour(0);
//                CandleStick stick = new CandleStick(sp.getOp)
                if (getBuyAfterDate() == null || curDate.isAfter(getBuyAfterDate())) {

                    if (holdingShares > 0 && curDate.isAfter(eod)) {
                        records.add(TransactionRecord.exit(DateTime.parse(curDate.toString()), sp.getSymbol(), holdingShares, price - spread));
                        holdingShares = 0;
                    }

                    if(holdingShares > 0 && ( pattern.isBearishEngulfing())) {
//                        records.add(TransactionRecord.sell(DateTime.parse(curDate.toString()), sp.getSymbol(), holdingShares, price - spread));
//                        holdingShares = 0;
                    }

                    if (pattern.isThreeLineStrikes()) {
                        if (holdingShares == 0 && buyCondition(sp, eod)) {
                            records.add(TransactionRecord.buy(DateTime.parse(curDate.toString()), sp.getSymbol(), numOfSharesToBuy, price + spread));
                            holdingPrice = price + spread;
                            holdingShares = numOfSharesToBuy;
                        }
                    }

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Skipping: " + data.get(0).getSymbol());
        }
        return records;
    }
}
