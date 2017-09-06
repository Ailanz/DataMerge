package algo;

/**
 * Created by Ailan on 9/4/2017.
 */
public class ExponentialMovingAverage extends MovingAverage {
    private double latestEntry;
    private double weight;

    public ExponentialMovingAverage(int interval) {
        super(interval);
        weight = 2.0 / (interval + 1.0);
    }

    @Override
    public void add(double x) {
        super.add(x);
        latestEntry = x;
    }

    @Override
    public double getAverage() {
        return ((super.total - latestEntry) * (1 - weight) / (super.interval - 1)) + latestEntry * weight;
    }
}
