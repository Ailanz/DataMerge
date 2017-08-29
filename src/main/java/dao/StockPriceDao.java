package dao;

import db.InsertionBuilder;
import db.SqliteDriver;
import db.TableBuilder;
import external.GlobalUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
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

    private static TableBuilder stockPriceTableBuilder = TableBuilder.aBuilder().withTableName("StockPrice")
            .withColumn("SYMBOL", TableBuilder.FIELD_TYPE.TEXT)
            .withColumn("HIGH", TableBuilder.FIELD_TYPE.NUMERIC)
            .withColumn("LOW", TableBuilder.FIELD_TYPE.NUMERIC)
            .withColumn("OPEN", TableBuilder.FIELD_TYPE.NUMERIC)
            .withColumn("CLOSE", TableBuilder.FIELD_TYPE.NUMERIC)
            .withColumn("VOLUME", TableBuilder.FIELD_TYPE.NUMERIC)
            .withColumn("ADJUSTED_CLOSE", TableBuilder.FIELD_TYPE.NUMERIC)
            .withColumn("DIVIDEND", TableBuilder.FIELD_TYPE.NUMERIC)
            .withColumn("SPLIT", TableBuilder.FIELD_TYPE.NUMERIC)
            .withColumn("DATE", TableBuilder.FIELD_TYPE.TEXT)
            .withprimaryKeys("SYMBOL", "DATE");

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

    public static TableBuilder getTableBuilder() {
        return stockPriceTableBuilder;
    }

    public static List<StockPriceDao> getAllStockPrices(String symbol) {
        String query = String.format("select * from stockprice where symbol = '%s'", symbol);
        List<StockPriceDao> stockPrices = new LinkedList<>();
        ResultSet rs = SqliteDriver.executeQuery(query);
        return parseStockPrice(rs);
    }

    public static List<StockPriceDao> getAllStockPrices() {
        String query = "select * from stockprice";
        List<StockPriceDao> stockPrices = new LinkedList<>();

        ResultSet rs = SqliteDriver.executeQuery(query);
        return parseStockPrice(rs);
    }

    private static List<StockPriceDao> parseStockPrice(ResultSet rs) {
        List<StockPriceDao> stockPrices = new LinkedList<>();

        try {
            while (rs.next()) {
                StockPriceDao sp = new StockPriceDao(rs.getString("SYMBOL"), LocalDate.parse(rs.getString("DATE"),
                        GlobalUtil.DATE_FORMAT),
                        rs.getDouble("OPEN"), rs.getDouble("HIGH"), rs.getDouble("LOW"),
                        rs.getDouble("CLOSE"), rs.getDouble("ADJUSTED_CLOSE"), rs.getLong("VOLUME"),
                        rs.getDouble("DIVIDEND"), rs.getDouble("SPLIT"));
                stockPrices.add(sp);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stockPrices;
    }

    public static synchronized void insertStockPrice(List<StockPriceDao> stockPrices) {
        InsertionBuilder builder = InsertionBuilder.aBuilder();
        builder.withTableBuilder(stockPriceTableBuilder);
        stockPrices.stream().forEach(s -> builder.withParams(s.getParams()));
        SqliteDriver.executeInsert(builder.execute());
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
