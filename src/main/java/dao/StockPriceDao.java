package dao;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class StockPriceDao implements AbstractDao {
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
                         double close, double adjsutedClose, long volume, double dividend, double split) {
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

    @Override
    public Map<String, String> getParams(){
        HashMap<String,String> map = new HashMap<>();
        map.put("SYMBOL", this.symbol);
        map.put("HIGH", String.valueOf(high));
        map.put("LOW", String.valueOf(low));
        map.put("OPEN", String.valueOf(open));
        map.put("CLOSE", String.valueOf(close));
        map.put("VOLUME", String.valueOf(volume));
        map.put("ADJUSTED_CLOSE", String.valueOf(adjsutedClose));
        map.put("DIVIDEND", String.valueOf(dividend));
        map.put("SPLIT", String.valueOf(split));
        map.put("DATE", date.toString());
        return map;
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
