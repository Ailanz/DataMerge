package dao;

import java.time.LocalDate;

public class StockPriceDao {
    String symbol;
    LocalDate date;
    double high;
    double low;
    double open;
    double close;
    long volume;

    public StockPriceDao(String symbol, LocalDate date, double open, double high, double low, double close, long volume) {
        this.symbol = symbol;
        this.date = date;
        this.high = high;
        this.low = low;
        this.open = open;
        this.close = close;
        this.volume = volume;
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

}
