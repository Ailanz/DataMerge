package core.strategy;

import algo.MovingAverage;
import org.joda.time.DateTime;
import util.TimeRange;

/**
 * Created by Ailan on 9/4/2017.
 */
public class StrategyBuilder<E> {
    private double maxLossPercent = 0.1;
    private MovingAverage shortMA;
    private MovingAverage longMA;
    private TimeRange timeRange;
    private DateTime buyAfterDate;

    private double valueToFulfill;

    private boolean sellHigher;
    protected StrategyBuilder() {
    }

    public static StrategyBuilder aBuilder() {
        return new StrategyBuilder();
    }

    public E withMaxLoss(double maxLoss) {
        this.maxLossPercent = maxLoss;
        return (E)this;
    }

    public E withMovingAverages(MovingAverage s, MovingAverage l) {
        this.shortMA = s;
        this.longMA = l;
        return (E)this;
    }

    public E withTimeRange(TimeRange range) {
        this.timeRange = range;
        return (E)this;
    }

    public E withBuyAfterDate(DateTime buyAfter) {
        this.buyAfterDate = buyAfter;
        return (E)this;
    }

    public E withBuyAfterDate(boolean sellHigher) {
        this.sellHigher = sellHigher;
        return (E)this;
    }

    public E withValueToFulfill(double valueToFulfill) {
        this.valueToFulfill = valueToFulfill;
        return (E)this;
    }


    protected int getNumSharesToBuy(double price) {
        if (getValueToFulfill() == 0) {
            return 1;
        } else {
            return new Double(getValueToFulfill() / price).intValue();
        }
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
}
