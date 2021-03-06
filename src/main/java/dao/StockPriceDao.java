package dao;

import db.InsertionBuilder;
import db.SqliteDriver;
import db.TableBuilder;
import org.joda.time.DateTime;
import util.KeyDateFilter;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StockPriceDao implements AbstractDao {
    String symbol;
    DateTime date;
    double high;
    double low;
    double open;
    double close;
    double adjsutedClose;
    long volume;

    static KeyDateFilter dateFiler;

    private static TableBuilder stockPriceTableBuilder = TableBuilder.aBuilder().withTableName("StockPrice")
            .withColumn("SYMBOL", TableBuilder.FIELD_TYPE.TEXT)
            .withColumn("HIGH", TableBuilder.FIELD_TYPE.NUMERIC)
            .withColumn("LOW", TableBuilder.FIELD_TYPE.NUMERIC)
            .withColumn("OPEN", TableBuilder.FIELD_TYPE.NUMERIC)
            .withColumn("CLOSE", TableBuilder.FIELD_TYPE.NUMERIC)
            .withColumn("VOLUME", TableBuilder.FIELD_TYPE.NUMERIC)
            .withColumn("ADJUSTED_CLOSE", TableBuilder.FIELD_TYPE.NUMERIC)
            .withColumn("DATE", TableBuilder.FIELD_TYPE.TEXT)
            .withprimaryKeys("SYMBOL", "DATE");

    public StockPriceDao(String symbol, DateTime date, double high, double low, double open,
                         double close, double adjsutedClose, long volume) {
        this.symbol = symbol;
        this.date = date;
        this.high = high;
        this.low = low;
        this.open = open;
        this.close = close;
        this.adjsutedClose = adjsutedClose;
        this.volume = volume;
    }

    @Override
    public Map<String, String> getParams() {
        HashMap<String, String> map = new HashMap<>();
        map.put("SYMBOL", this.symbol);
        map.put("HIGH", String.valueOf(high));
        map.put("LOW", String.valueOf(low));
        map.put("OPEN", String.valueOf(open));
        map.put("CLOSE", String.valueOf(close));
        map.put("VOLUME", String.valueOf(volume));
        map.put("ADJUSTED_CLOSE", String.valueOf(adjsutedClose));
        map.put("DATE", date.toString());
        return map;
    }

    public static TableBuilder getTableBuilder() {
        return stockPriceTableBuilder;
    }

    public static synchronized List<StockPriceDao> getAllStockPrices(String symbol) {
        String query = String.format("select * from stockprice where symbol = '%s' order by DATE desc", symbol);
        List<StockPriceDao> stockPrices = new LinkedList<>();
        ResultSet rs = SqliteDriver.executeQuery(query);
        return parseStockPrice(rs);
    }

    public static synchronized List<StockPriceDao> getAllStockPrices() {
        String query = "select * from stockprice order by DATE desc";
        ResultSet rs = SqliteDriver.executeQuery(query);
        return parseStockPrice(rs);
    }

    private static List<StockPriceDao> parseStockPrice(ResultSet rs) {
        List<StockPriceDao> stockPrices = new LinkedList<>();
        try {
            while (rs.next()) {
                StockPriceDao sp = new StockPriceDao(rs.getString("SYMBOL"), DateTime.parse(rs.getString("DATE")),
                        rs.getDouble("HIGH"), rs.getDouble("LOW"), rs.getDouble("OPEN"),
                        rs.getDouble("CLOSE"), rs.getDouble("ADJUSTED_CLOSE"), rs.getLong("VOLUME"));
                stockPrices.add(sp);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stockPrices;
    }

    public static synchronized void insertStockPrice(List<StockPriceDao> stockPrices) {
        //Remove Everything already added
        if (stockPrices.size() == 0) {
            System.err.print("Empty Stock Prices!");
        }

        //Lazy load
        if (dateFiler == null) {
            dateFiler = new KeyDateFilter();
            List<StockPriceDao> allPrices = getAllStockPrices();
            allPrices.stream().forEach(s -> dateFiler.add(s.getSymbol(), s.getDate()));
        }

        String name = stockPrices.get(0).getSymbol();
        stockPrices = stockPrices.stream()
                .filter(s -> dateFiler.isAfterOrEmpty(s.getSymbol(), s.getDate()))
                .distinct()
                .collect(Collectors.toList());

        if (stockPrices.size() == 0) {
            System.out.println("Already esists, Skipping: " + name);
            return;
        }

        InsertionBuilder builder = InsertionBuilder.aBuilder();
        builder.withTableBuilder(stockPriceTableBuilder);
        stockPrices.stream().forEach(s -> builder.withParams(s.getParams()));
        SqliteDriver.executeInsert(builder.execute());
    }

    public String getSymbol() {
        return symbol;
    }

    public DateTime getDate() {
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


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StockPriceDao)) return false;

        StockPriceDao that = (StockPriceDao) o;

        if (!symbol.equals(that.symbol)) return false;
        return date.equals(that.date);
    }

    @Override
    public int hashCode() {
        int result = symbol.hashCode();
        result = 31 * result + date.hashCode();
        return result;
    }
}
