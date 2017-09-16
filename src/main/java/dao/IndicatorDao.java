package dao;

import db.InsertionBuilder;
import db.SqliteDriver;
import db.TableBuilder;
import grabber.AlphaVantageEnum;
import grabber.DailyIndicatorGrabber;
import org.joda.time.DateTime;
import ui.MainForm;
import util.KeyDateFilter;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Ailan on 9/5/2017.
 */
public class IndicatorDao implements AbstractDao {
    static TableBuilder tableBuilder = TableBuilder.aBuilder().withTableName("Indicator")
            .withColumn("SYMBOL", TableBuilder.FIELD_TYPE.TEXT)
            .withColumn("DATE", TableBuilder.FIELD_TYPE.TEXT)
            .withColumn("ADX", TableBuilder.FIELD_TYPE.NUMERIC)
            .withColumn("MACD", TableBuilder.FIELD_TYPE.NUMERIC)
            .withColumn("MACD_SIGNAL", TableBuilder.FIELD_TYPE.NUMERIC)
            .withColumn("MACD_HIST", TableBuilder.FIELD_TYPE.NUMERIC)
            .withColumn("RSI7", TableBuilder.FIELD_TYPE.NUMERIC)
            .withColumn("RSI14", TableBuilder.FIELD_TYPE.NUMERIC)
            .withColumn("RSI25", TableBuilder.FIELD_TYPE.NUMERIC)
            .withColumn("CCI", TableBuilder.FIELD_TYPE.NUMERIC)
            .withColumn("AROON_UP", TableBuilder.FIELD_TYPE.NUMERIC)
            .withColumn("AROON_DOWN", TableBuilder.FIELD_TYPE.NUMERIC)
            .withprimaryKeys("SYMBOL", "DATE");

    private String symbol;
    private DateTime date;
    private double adx;
    private double macd;
    private double macdSignal;
    private double macdHist;
    private double rsi7;
    private double rsi14;
    private double rsi25;
    private double cci;
    private double aroonUp;
    private double aroonDown;

    static KeyDateFilter dateFiler;


    public IndicatorDao(String symbol, DateTime date, double adx, double macd, double macdSignal, double macdHist, double rsi7, double rsi14, double rsi25, double cci, double aroonUp, double aroonDown) {
        this.symbol = symbol;
        this.date = date;
        this.adx = adx;
        this.macd = macd;
        this.macdSignal = macdSignal;
        this.macdHist = macdHist;
        this.rsi7 = rsi7;
        this.rsi14 = rsi14;
        this.rsi25 = rsi25;
        this.cci = cci;
        this.aroonUp = aroonUp;
        this.aroonDown = aroonDown;
    }

    public static synchronized void insertIndicator(List<IndicatorDao> indicators) {
        //Remove Everything already added
        if (indicators.size() == 0) {
            System.err.print("Empty Stock Prices!");
        }

        //Lazy load
        if (dateFiler == null) {
            dateFiler = new KeyDateFilter();
            List<IndicatorDao> allPrices = getAllIndicators();
            allPrices.stream().forEach(s -> dateFiler.add(s.getSymbol(), s.getDate()));
        }

        String name = indicators.get(0).getSymbol();
        indicators = indicators.stream()
                .filter(s -> dateFiler.isAfterOrEmpty(s.getSymbol(), s.getDate()))
                .distinct()
                .collect(Collectors.toList());

        if (indicators.size() == 0) {
            System.out.println("IndicatorDao: Already exists, Skipping: " + name);
            return;
        }

        InsertionBuilder builder = InsertionBuilder.aBuilder();
        builder.withTableBuilder(tableBuilder);
        indicators.stream().forEach(s -> builder.withParams(s.getParams()));
        SqliteDriver.executeInsert(builder.execute());
    }

    public static synchronized List<IndicatorDao> getAllIndicators(String symbol) {
        String query = String.format("select * from Indicator where symbol = '%s' order by DATE desc", symbol);
        ResultSet rs = SqliteDriver.executeQuery(query);
        return parseIndicators(rs);
    }

    public static Map<DateTime, IndicatorDao> getIndicatorMap(String symbol){
        List<IndicatorDao> indicators = getAllIndicators(symbol);
        Map<DateTime, IndicatorDao> map = new HashMap<>();
        indicators.forEach(i->map.put(i.getDate(),i));
        return map;
    }

    public static synchronized List<IndicatorDao> getAllIndicators() {
        String query = "select * from Indicator order by DATE desc";
        ResultSet rs = SqliteDriver.executeQuery(query);
        return parseIndicators(rs);
    }

    private static List<IndicatorDao> parseIndicators(ResultSet rs) {
        List<IndicatorDao> indicators = new LinkedList<>();
        try {
            while (rs.next()) {
                IndicatorDao ind = new IndicatorDao(rs.getString("SYMBOL"), DateTime.parse(rs.getString("DATE")),
                        rs.getDouble("ADX"), rs.getDouble("MACD"), rs.getDouble("MACD_SIGNAL"),
                        rs.getDouble("MACD_HIST"), rs.getDouble("RSI7"), rs.getDouble("RSI14"),
                        rs.getDouble("RSI25"), rs.getDouble("CCI"), rs.getDouble("AROON_UP"),
                        rs.getDouble("AROON_DOWN"));
                indicators.add(ind);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return indicators;
    }

    @Override
    public Map<String, String> getParams() {
        HashMap<String, String> map = new HashMap<>();
        map.put("SYMBOL", this.symbol);
        map.put("DATE", this.date.toString());
        map.put("ADX", String.valueOf(this.adx));
        map.put("MACD", String.valueOf(this.macd));
        map.put("MACD_SIGNAL", String.valueOf(this.macdSignal));
        map.put("MACD_HIST", String.valueOf(this.macdHist));
        map.put("RSI7", String.valueOf(this.rsi7));
        map.put("RSI14", String.valueOf(this.rsi14));
        map.put("RSI25", String.valueOf(this.rsi25));
        map.put("CCI", String.valueOf(this.cci));
        map.put("AROON_UP", String.valueOf(this.aroonUp));
        map.put("AROON_DOWN", String.valueOf(this.aroonDown));
        return map;
    }

    public static TableBuilder getTableBuilder() {
        return tableBuilder;
    }

    public double getRsi7() {
        return rsi7;
    }

    public double getRsi14() {
        return rsi14;
    }

    public double getRsi25() {
        return rsi25;
    }

    public String getSymbol() {
        return symbol;
    }

    public DateTime getDate() {
        return date;
    }

    public double getAdx() {
        return adx;
    }

    public double getMacd() {
        return macd;
    }

    public double getMacdSignal() {
        return macdSignal;
    }

    public double getMacdHist() {
        return macdHist;
    }

    public double getCci() {
        return cci;
    }

    public double getAroonUp() {
        return aroonUp;
    }

    public double getAroonDown() {
        return aroonDown;
    }
}
