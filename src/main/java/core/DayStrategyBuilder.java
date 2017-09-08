package core;

import algo.ExponentialMovingAverage;
import algo.MovingAverage;
import dao.IndicatorDao;
import dao.StockDao;
import dao.StockPriceDao;
import grabber.AlphaVantageBuilder;
import grabber.AlphaVantageEnum;
import grabber.DailyIndicatorGrabber;
import grabber.ResultData;
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
public class DayStrategyBuilder{
    private double maxLossPercent = 0.1;
    private MovingAverage shortMA;
    private MovingAverage longMA;
    private TimeRange timeRange;
    private DateTime buyAfterDate;

    private DayStrategyBuilder() {
    }

    public static void main(String args[]) {
        MovingAverage s = new ExponentialMovingAverage(8);
        MovingAverage l = new ExponentialMovingAverage(13);
        List<TransactionRecord> transactions = DayStrategyBuilder.aBuilder()
                .withBuyAfterDate(DateTime.now().minusDays(1))
                .withMovingAverages(s, l)
                .execute(StockDao.getStock("AAPL"));
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

    public boolean buyCondition(IndicatorDao indicator){
        if(indicator==null) {
            return false;
        }
        return indicator.getAdx() > 30 && indicator.getRsi7() < 60;
//       && indicator.getRsi14() < 60 && indicator.getRsi25() < 60;
    }

    public List<TransactionRecord> execute(StockDao stock) {
        List<TransactionRecord> records = new LinkedList<>();
        try {
            List<StockPriceDao> prices = getDaysPrice(stock.getSymbol());
            if (timeRange != null) {
                prices = prices.stream().filter(s -> timeRange.isWithin(s.getDate())).collect(Collectors.toList());
            }

            prices = prices.stream().sorted((o1, o2) -> o1.getDate().isBefore(o2.getDate()) ? -1 : 1).collect(Collectors.toList());

            double spread = 0.05;
            Boolean isShortOverLong = null;
            boolean holdingStock = false;
            double holdingPrice = 0;
            Map<DateTime, IndicatorDao> indicators = getIndicatorMap(stock.getSymbol());
            for (StockPriceDao sp : prices) {
                double price = sp.getClose();
                DateTime curDate = sp.getDate();
                shortMA.add(price);
                longMA.add(price);
                IndicatorDao indicator = indicators.get(sp.getDate());

                if (buyAfterDate == null || curDate.isAfter(buyAfterDate)) {
                    double shortAvg = shortMA.getAverage();
                    double longAvg = longMA.getAverage();

                    if (isShortOverLong == null) {
                        isShortOverLong = shortAvg > longAvg;
                    }

                    if (holdingStock && price <= holdingPrice * (1 - maxLossPercent)) {
                        records.add(TransactionRecord.exit(DateTime.parse(curDate.toString()), stock.getSymbol(), price - spread));
                        holdingStock = false;
                    }

                    if (!isShortOverLong && shortAvg > longAvg) {
                        if (!holdingStock && buyCondition(indicator)) {
                            records.add(TransactionRecord.buy(DateTime.parse(curDate.toString()), stock.getSymbol(), price + spread));
                            holdingPrice = price + spread;
                            holdingStock = true;
                        }
                        isShortOverLong = true;
                    }

                    if (isShortOverLong && longAvg > shortAvg) {
                        if (holdingStock && holdingPrice < price-spread) {
                            records.add(TransactionRecord.sell(DateTime.parse(curDate.toString()), stock.getSymbol(), price - spread));
                            holdingStock = false;
                        }
                        isShortOverLong = false;
                    }
                }
            }
        }catch(Exception e) {
            e.printStackTrace();
            System.err.println("Skipping: " + stock.getSymbol());
        }
        return records;
    }

    public static Map<DateTime, IndicatorDao> getIndicatorMap(String symbol){
        List<IndicatorDao> indicators = DailyIndicatorGrabber.getIndicators(symbol, AlphaVantageEnum.Interval.FIVE, 7);
        Map<DateTime, IndicatorDao> map = new HashMap<>();
        indicators.forEach(i->map.put(i.getDate(),i));
        return map;
    }

    public List<StockPriceDao> getDaysPrice(String symbol) {
        AlphaVantageBuilder builder = AlphaVantageBuilder.aBuilder()
                .withFunction(AlphaVantageEnum.Function.TIME_SERIES_INTRADAY)
                .withOutputSize(AlphaVantageEnum.OutputSize.COMPACT)
                .withInterval(AlphaVantageEnum.Interval.FIVE);
        List<ResultData> results = builder.withSymbol(symbol).execute();
        return parseResultData(symbol, results).stream().sorted((o1, o2) -> o1.getDate().isBefore(o2.getDate()) ? -1 : 1).collect(Collectors.toList());
    }

    private static List<StockPriceDao> parseResultData(String stockSymbol, List<ResultData> data) {
        List<StockPriceDao> ret = new LinkedList<>();
        for (ResultData r : data) {
            ret.add(new StockPriceDao(stockSymbol, r.getDate(), r.getData().get("2. high").asDouble(), r.getData().get("3. low").asDouble(), r.getData().get("1. open").asDouble()
                    , r.getData().get("4. close").asDouble(), r.getData().get("4. close").asDouble(),
                    r.getData().get("5. volume").asLong()));
        }
        return ret;
    }
}
