package dao;

import algo.MovingAverage;
import db.SqliteDriver;
import db.TableBuilder;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MovingAverageDao implements AbstractDao {
    private String symbol;

    private int shortMA;

    private int longMA;
    private double profit;
    private DateTime updated;
    public MovingAverageDao(String symbol, int shortMA, int longMA, double profit, DateTime updated) {
        this.symbol = symbol;
        this.shortMA = shortMA;
        this.longMA = longMA;
        this.profit = profit;
        this.updated = updated;
    }

    static TableBuilder tableBuilder = TableBuilder.aBuilder().withTableName("MovingAverage")
            .withColumn("SYMBOL", TableBuilder.FIELD_TYPE.TEXT)
            .withColumn("SHORT", TableBuilder.FIELD_TYPE.NUMERIC)
            .withColumn("LONG", TableBuilder.FIELD_TYPE.NUMERIC)
            .withColumn("PROFIT", TableBuilder.FIELD_TYPE.NUMERIC)
            .withColumn("UPDATED", TableBuilder.FIELD_TYPE.TEXT)
            .withprimaryKeys("SYMBOL");

    public static synchronized List<MovingAverageDao> getAllMovingAverages() {
        String query = "select * from MovingAverage";
        ResultSet rs = SqliteDriver.executeQuery(query);
        return parseMovingAverage(rs);
    }

    public static synchronized MovingAverageDao getMovingAverage(String symbol) {
        String query = "select * from MovingAverage where SYMBOL = '%s'";
        ResultSet rs = SqliteDriver.executeQuery(String.format(query, symbol));
        List<MovingAverageDao> ret =  parseMovingAverage(rs);
        return ret.size() > 0 ? ret.get(0) : null;
    }

    private static List<MovingAverageDao> parseMovingAverage(ResultSet rs) {
        List<MovingAverageDao> ret = new LinkedList<>();
        try {
            while (rs.next()) {
                MovingAverageDao mv = new MovingAverageDao(rs.getString("SYMBOL"), rs.getInt("SHORT"),
                        rs.getInt("LONG"), rs.getDouble("PROFIT"), DateTime.parse(rs.getString("UPDATED")));
                ret.add(mv);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ret;
    }

    @Override
    public Map<String, String> getParams() {
        HashMap<String, String> map = new HashMap<>();
        map.put("SYMBOL", this.symbol);
        map.put("SHORT", String.valueOf(this.shortMA));
        map.put("LONG", String.valueOf(this.longMA));
        map.put("PROFIT", String.valueOf(this.profit));
        map.put("UPDATED", String.valueOf(this.updated));
        return map;
    }

    public String getSymbol() {
        return symbol;
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

    public static TableBuilder getTableBuilder() {
        return tableBuilder;
    }
}
