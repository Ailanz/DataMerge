package dao;

import db.InsertionBuilder;
import db.SqliteDriver;
import db.TableBuilder;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by Ailan on 9/10/2017.
 */
public class DayDataDao implements AbstractDao {
    static TableBuilder tableBuilder = TableBuilder.aBuilder().withTableName("DayData")
            .withColumn("SYMBOL", TableBuilder.FIELD_TYPE.TEXT)
            .withColumn("DATE", TableBuilder.FIELD_TYPE.TEXT)
            .withColumn("PRICE", TableBuilder.FIELD_TYPE.NUMERIC)
            .withColumn("ADX", TableBuilder.FIELD_TYPE.NUMERIC)
            .withColumn("SHORT_MA", TableBuilder.FIELD_TYPE.NUMERIC)
            .withColumn("LONG_MA", TableBuilder.FIELD_TYPE.NUMERIC)
            .withColumn("PROFIT", TableBuilder.FIELD_TYPE.NUMERIC)
            .withprimaryKeys("SYMBOL", "DATE");

    public DayDataDao(String symbol, DateTime date, double price, double adx, int shortMA, int longMA, double profit) {
        this.symbol = symbol;
        this.date = date;
        this.price = price;
        this.adx = adx;
        this.shortMA = shortMA;
        this.longMA = longMA;
        this.profit = profit;
    }

    private String symbol;
    private DateTime date;
    private double price;
    private double adx;
    private int shortMA;
    private int longMA;
    private double profit;

    public static TableBuilder getTableBuilder() {
        return tableBuilder;
    }

    public String getSymbol() {
        return symbol;
    }

    public DateTime getDate() {
        return date;
    }

    public double getPrice() {
        return price;
    }

    public double getAdx() {
        return adx;
    }

    public int getShortMA() {
        return shortMA;
    }

    public int getLongMA() {
        return longMA;
    }

    public double getProfit() {
        return profit;
    }

    private static List<DayDataDao> parseDayData(ResultSet rs) {
        List<DayDataDao> dayData = new LinkedList<>();
        try {
            while (rs.next()) {
                DayDataDao ind = new DayDataDao(rs.getString("SYMBOL"), DateTime.parse(rs.getString("DATE")),
                        rs.getDouble("PRICE"), rs.getDouble("ADX"), rs.getInt("SHORT_MA"),
                        rs.getInt("LONG_MA"), rs.getDouble("PROFIT"));
                dayData.add(ind);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dayData;
    }

    public static synchronized List<DayDataDao> getAllDayData() {
        String query = "select * from DayData";
        ResultSet rs = SqliteDriver.executeQuery(query);
        return parseDayData(rs);
    }

    public static synchronized List<DayDataDao> getDayData(String symbol) {
        String query = "select * from DayData where SYMBOL = '%s'";
        ResultSet rs = SqliteDriver.executeQuery(String.format(query, symbol));
        return parseDayData(rs);
    }

    public void insertDayData() {
        SqliteDriver.executeInsert(InsertionBuilder.aBuilder().withTableBuilder(getTableBuilder()).withParams(getParams()).execute());
    }

        @Override
    public Map<String, String> getParams() {
        HashMap<String, String> map = new HashMap<>();
        map.put("SYMBOL", this.symbol);
        map.put("DATE", this.date.toString());
        map.put("PRICE", String.valueOf(this.price));
        map.put("ADX", String.valueOf(this.adx));
        map.put("SHORT_MA", String.valueOf(this.shortMA));
        map.put("LONG_MA", String.valueOf(this.longMA));
        map.put("PROFIT", String.valueOf(this.profit));
        return map;
    }
}
