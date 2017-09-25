package core.strategy;

import algo.MovingAverage;
import algo.VolumeAverage;
import core.TransactionRecord;
import dao.DayDataDao;
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
 * Created by Ailan on 9/4/2017.
 */
public abstract class StrategyBuilder<E> {
    private double maxLossPercent = 0.1;
    private double sellLimit;
    private MovingAverage shortMA;
    private MovingAverage longMA;
    private TimeRange timeRange;
    private DateTime buyAfterDate;
    private double valueToFulfill;
    private boolean sellHigher;
    private ExitIntervalEnum exitInterval = ExitIntervalEnum.NEVER;
    private AlphaVantageEnum.Interval interval = AlphaVantageEnum.Interval.DAILY;
    public double holdingPrice;
    protected VolumeAverage volumeAverage = new VolumeAverage();

    protected StrategyBuilder() {
    }

//    public static StrategyBuilder aBuilder() {
//        return new StrategyBuilder();
//    }

    public E withMaxLoss(double maxLoss) {
        this.maxLossPercent = maxLoss;
        return (E) this;
    }

    public E withMovingAverages(MovingAverage s, MovingAverage l) {
        this.shortMA = s;
        this.longMA = l;
        return (E) this;
    }

    public E withTimeRange(TimeRange range) {
        this.timeRange = range;
        return (E) this;
    }

    public E withInterval(AlphaVantageEnum.Interval interval) {
        this.interval = interval;
        return (E) this;
    }


    public E withBuyAfterDate(DateTime buyAfter) {
        this.buyAfterDate = buyAfter;
        return (E) this;
    }

    public E withExitInterval(ExitIntervalEnum exitInterval) {
        this.exitInterval = exitInterval;
        return (E) this;
    }

    public E withSellHigher(boolean sellHigher) {
        this.sellHigher = sellHigher;
        return (E) this;
    }

    public E withValueToFulfill(double valueToFulfill) {
        this.valueToFulfill = valueToFulfill;
        return (E) this;
    }

    public E withSellLimit(double sellLimit) {
        this.sellLimit = sellLimit;
        return (E) this;
    }


    protected int getNumSharesToBuy(double price) {
        if (getValueToFulfill() == 0) {
            return 1;
        } else {
            int numShares = new Double(getValueToFulfill() / price).intValue();
            return numShares==0 ? 1 : numShares;
        }
    }

    public boolean buyCondition(DayDataDao data) {
        //true by default
        if (buyAfterDate != null && data.getDate().isBefore(buyAfterDate)) {
            return false;
        }

        DateTime eod = new DateTime(data.getDate().toString()).withHourOfDay(15).withMinuteOfHour(30);
        if (exitInterval == ExitIntervalEnum.DAILY && data.getDate().isAfter(eod)) {
            return false;
        }
        return true;
    }

    public boolean sellCondition(DayDataDao data) {
        //dont sell be default
        DateTime eod = new DateTime(data.getDate().toString()).withHourOfDay(15).withMinuteOfHour(30);
        if(holdingPrice==0) {
            return false;
        }

        if (exitInterval == ExitIntervalEnum.DAILY && data.getDate().isAfter(eod)) {
            return true;
        }

        if (maxLossPercent > 0 && holdingPrice > 0 && data.getClose() <= holdingPrice * (1 - getMaxLossPercent())) {
            return true;
        }

        if(sellHigher && data.getClose() < holdingPrice) {
            return true;
        }

        if(sellLimit !=0 && data.getClose() > holdingPrice * sellLimit) {
            return true;
        }

        return false;
    }

    public List<TransactionRecord> execute(StockDao stock) {
        return execute(stock, StockPriceDao.getAllStockPrices(stock.getSymbol()), IndicatorDao.getIndicatorMap(stock.getSymbol()));
    }

    public List<TransactionRecord> execute(StockDao stock, List<StockPriceDao> prices) {
        return execute(stock, prices, getIndicatorMap(stock.getSymbol(), interval));
    }

    public List<TransactionRecord> execute(StockDao stock, List<StockPriceDao> prices, Map<DateTime, IndicatorDao> indicators) {
        List<DayDataDao> data = new LinkedList<>();
        double lastAdx = -1;
        for (int i = 0; i < prices.size(); i++) {
            double adx = lastAdx;
            IndicatorDao ind = indicators.get(prices.get(i).getDate());
            if (ind != null) {
                adx = ind.getAdx();
            }
            StockPriceDao sp = prices.get(i);
            data.add(new DayDataDao(stock.getSymbol(), sp.getDate(), sp.getOpen(), sp.getClose(), sp.getHigh(), sp.getLow(), sp.getVolume(),
                    adx, shortMA == null ? -1 : getShortMA().getInterval(), longMA==null ? -1 : getLongMA().getInterval(), 0));
        }
        if (getTimeRange() != null) {
            data = data.stream().filter(s -> getTimeRange().isWithin(s.getDate())).collect(Collectors.toList());
        }

        data = data.stream().sorted((o1, o2) -> o1.getDate().isBefore(o2.getDate()) ? -1 : 1).collect(Collectors.toList());
        return execute(data);
    }

    public abstract List<TransactionRecord> execute(List<DayDataDao> data);

    public static Map<DateTime, IndicatorDao> getIndicatorMap(String symbol, AlphaVantageEnum.Interval interval) {
        List<IndicatorDao> indicators = DailyIndicatorGrabber.getIndicators(symbol, interval, 7);
        return toIndicatorMap(indicators);
    }

    public static Map<DateTime, IndicatorDao> toIndicatorMap(List<IndicatorDao> indicators) {
        Map<DateTime, IndicatorDao> map = new HashMap<>();
        indicators.forEach(i -> map.put(i.getDate(), i));
        return map;
    }

    public double getMaxLossPercent() {
        return maxLossPercent;
    }

    public MovingAverage getShortMA() {
        return shortMA;
    }

    public MovingAverage getLongMA() {
        return longMA;
    }

    public TimeRange getTimeRange() {
        return timeRange;
    }

    public DateTime getBuyAfterDate() {
        return buyAfterDate;
    }

    public boolean isSellHigher() {
        return sellHigher;
    }

    public double getValueToFulfill() {
        return valueToFulfill;
    }

    public double getSellLimit() {
        return sellLimit;
    }

    public ExitIntervalEnum getExitInterval() {
        return exitInterval;
    }

    public AlphaVantageEnum.Interval getInterval() {
        return interval;
    }

    public double getHoldingPrice() {
        return holdingPrice;
    }

    public VolumeAverage getVolumeAverage() {
        return volumeAverage;
    }
}
