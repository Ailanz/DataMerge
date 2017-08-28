package dao;

import java.time.LocalDate;

public class StockPriceDao {
    String symbol;
    LocalDate date;
    double high;
    double low;
    double open;
    double close;
    double adjsutedClose;
    double dividend;
    double split;
    long volume;

    public StockPriceDao(String symbol, LocalDate date, double high, double low, double open,
                         double close, double adjsutedClose,  long volume, double dividend, double split) {
        this.symbol = symbol;
        this.date = date;
        this.high = high;
        this.low = low;
        this.open = open;
        this.close = close;
        this.adjsutedClose = adjsutedClose;
        this.volume = volume;
        this.dividend = dividend;
        this.split = split;
    }

    public String getSymbol() {
        return symbol;
    }

    public LocalDate getDate() {
        return date;
    }

    public double getHigh() {
        return high;
    }

    public long getVolume() {
        return volume;
    }

    public double getLow() {
        return low;
    }

    public double getOpen() {
        return open;
    }

    public double getClose() {
        return close;
    }

    public double getAdjsutedClose() {
        return adjsutedClose;
    }

    public double getDividend() {
        return dividend;
    }

    public double getSplit() {
        return split;
    }

}
