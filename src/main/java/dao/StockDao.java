package dao;

import db.SqliteDriver;
import db.TableBuilder;
import grabber.YahooResult;
import util.GlobalUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class StockDao implements AbstractDao {
    String symbol;
    String name;
    String exchange;
    LocalDate updated;
    double spread;
    double dividendShare;
    double dividendYield;
    double earningsShare;
    double epseEstimateCurrentYear;
    double epseEseEstimateNextYear;
    double marketCap;
    double ebitada;
    double peRatio;

    double yearTargetPrice;

    static TableBuilder stockTableBuilder = TableBuilder.aBuilder().withTableName("Stock")
            .withColumn("SYMBOL", TableBuilder.FIELD_TYPE.TEXT)
            .withColumn("NAME", TableBuilder.FIELD_TYPE.TEXT)
            .withColumn("EXCHANGE", TableBuilder.FIELD_TYPE.TEXT)
            .withColumn("SPREAD", TableBuilder.FIELD_TYPE.NUMERIC)
            .withColumn("DIVIDEND_SHARE", TableBuilder.FIELD_TYPE.NUMERIC)
            .withColumn("DIVIDEND_YIELD", TableBuilder.FIELD_TYPE.NUMERIC)
            .withColumn("EARNINGS_SHARE", TableBuilder.FIELD_TYPE.NUMERIC)
            .withColumn("EPSE_EST_CUR_YEAR", TableBuilder.FIELD_TYPE.NUMERIC)
            .withColumn("EPSE_EST_NEXT_YEAR", TableBuilder.FIELD_TYPE.NUMERIC)
            .withColumn("MARKET_CAP", TableBuilder.FIELD_TYPE.NUMERIC)
            .withColumn("EBITDA", TableBuilder.FIELD_TYPE.NUMERIC)
            .withColumn("PE_RATIO", TableBuilder.FIELD_TYPE.NUMERIC)
            .withColumn("YR_TARGET_PRICE", TableBuilder.FIELD_TYPE.NUMERIC)
            .withColumn("UPDATED", TableBuilder.FIELD_TYPE.TEXT)
            .withprimaryKeys("SYMBOL");

    public StockDao(String symbol, String exchange, LocalDate updated) {
        this.symbol = symbol;
        this.exchange = exchange;
        this.updated = updated;
    }

    public StockDao(String symbol, String name, String exchange, LocalDate updated, double spread, double dividendShare,
                    double dividendYield, double earningsShare, double epseEstimateCurrentYear,
                    double epseEseEstimateNextYear, double marketCap, double ebitada, double peRatio,
                    double yearTargetPrice) {
        this.symbol = symbol;
        this.name = name;
        this.exchange = exchange;
        this.updated = updated;
        this.spread = spread;
        this.dividendShare = dividendShare;
        this.dividendYield = dividendYield;
        this.earningsShare = earningsShare;
        this.epseEstimateCurrentYear = epseEstimateCurrentYear;
        this.epseEseEstimateNextYear = epseEseEstimateNextYear;
        this.marketCap = marketCap;
        this.ebitada = ebitada;
        this.peRatio = peRatio;
        this.yearTargetPrice = yearTargetPrice;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getName() {
        return name;
    }

    public String getExchange() {
        return exchange;
    }

    public LocalDate getUpdated() {
        return updated;
    }

    public static StockDao getStock(String symbol) {
        String query = "select * from stock where symbol = '%s'";
        ResultSet rs = SqliteDriver.executeQuery(String.format(query,symbol));
        return parseStock(rs).get(0);
    }

    public static List<StockDao> getAllStocks() {
        String query = "select * from stock";
        ResultSet rs = SqliteDriver.executeQuery(query);
        return parseStock(rs);
    }

    private static List<StockDao> parseStock(ResultSet rs) {
        List<StockDao> stocks = new LinkedList<>();
        try {
            while (rs.next()) {
                StockDao stock = new StockDao(rs.getString("SYMBOL"), rs.getString("NAME"), rs.getString("EXCHANGE"),
                        LocalDate.parse(rs.getString("UPDATED"), GlobalUtil.DATE_FORMAT), rs.getDouble("SPREAD"),
                        rs.getDouble("DIVIDEND_SHARE"), rs.getDouble("DIVIDEND_YIELD"), rs.getDouble("EARNINGS_SHARE"),
                        rs.getDouble("EPSE_EST_CUR_YEAR"), rs.getDouble("EPSE_EST_NEXT_YEAR"),
                        rs.getDouble("MARKET_CAP"), rs.getDouble("EBITDA"), rs.getDouble("PE_RATIO"),
                        rs.getDouble("YR_TARGET_PRICE"));
                stocks.add(stock);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stocks;
    }

    public void setResult(YahooResult r) {
        if (r == null) {
            return;
        }
        this.name = r.getName();
        this.spread = r.getSpread();
        this.dividendShare = r.getDividendShare();
        this.dividendYield = r.getDividendYield();
        this.earningsShare = r.getEarningsShare();
        this.epseEstimateCurrentYear = r.getEpseEstimateCurrentYear();
        this.epseEseEstimateNextYear = r.getEpseEseEstimateNextYear();
        this.marketCap = r.getMarketCap();
        this.ebitada = r.getEbitada();
        this.peRatio = r.getPeRatio();
        this.yearTargetPrice = r.getYearTargetPrice();
    }

    public static TableBuilder getTableBuilder() {
        return stockTableBuilder;
    }

    @Override
    public Map<String, String> getParams() {
        HashMap<String, String> map = new HashMap<>();
        map.put("SYMBOL", this.symbol);
        map.put("NAME", this.name==null ? null : this.name.replaceAll("'", "''"));
        map.put("EXCHANGE", this.exchange);
        map.put("SPREAD", String.valueOf(this.spread));
        map.put("DIVIDEND_SHARE", String.valueOf(this.dividendShare));
        map.put("DIVIDEND_YIELD", String.valueOf(this.dividendYield));
        map.put("EARNINGS_SHARE", String.valueOf(this.earningsShare));
        map.put("EPSE_EST_CUR_YEAR", String.valueOf(this.epseEstimateCurrentYear));
        map.put("EPSE_EST_NEXT_YEAR", String.valueOf(this.epseEseEstimateNextYear));
        map.put("MARKET_CAP", String.valueOf(this.marketCap));
        map.put("EBITDA", String.valueOf(this.ebitada));
        map.put("PE_RATIO", String.valueOf(this.peRatio));
        map.put("YR_TARGET_PRICE", String.valueOf(this.yearTargetPrice));
        map.put("UPDATED", LocalDate.now().toString());
        return map;
    }

    public List<StockPriceDao> getPrices(){
        return StockPriceDao.getAllStockPrices(this.symbol);
    }

    public double getSpread() {
        return spread;
    }

    public double getDividendShare() {
        return dividendShare;
    }

    public double getDividendYield() {
        return dividendYield;
    }

    public double getEarningsShare() {
        return earningsShare;
    }

    public double getEpseEstimateCurrentYear() {
        return epseEstimateCurrentYear;
    }

    public double getEpseEseEstimateNextYear() {
        return epseEseEstimateNextYear;
    }

    public double getMarketCap() {
        return marketCap;
    }

    public double getEbitada() {
        return ebitada;
    }

    public double getPeRatio() {
        return peRatio;
    }

    public double getYearTargetPrice() {
        return yearTargetPrice;
    }

    public static TableBuilder getStockTableBuilder() {
        return stockTableBuilder;
    }
}
