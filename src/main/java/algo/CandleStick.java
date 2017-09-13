package algo;

import org.joda.time.DateTime;

/**
 * Created by Ailan on 9/12/2017.
 */
public class CandleStick {
    private double open;
    private double close;
    private double high;
    private double low;
    private DateTime date;
    private double volume;

    public CandleStick(double open, double close, double high, double low, DateTime date, double volume) {
        this.open = open;
        this.close = close;
        this.high = high;
        this.low = low;
        this.date = date;
        this.volume = volume;
    }

    public double getOpen() {
        return open;
    }

    public double getClose() {
        return close;
    }

    public double getHigh() {
        return high;
    }

    public double getLow() {
        return low;
    }

    public DateTime getDate() {
        return date;
    }

    public double getVolume() {
        return volume;
    }

    public boolean isWhite(){
        return close > open;
    }
}
