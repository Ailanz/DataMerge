package dao;

import db.TableBuilder;
import grabber.YahooResult;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class StockDao implements AbstractDao{
    String symbol;
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

    public String getSymbol() {
        return symbol;
    }

    public String getExchange() {
        return exchange;
    }

    public LocalDate getUpdated() {
        return updated;
    }

    public void setResult(YahooResult r){
        if(r==null){
            return;
        }
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
        HashMap<String,String> map = new HashMap<>();
        map.put("SYMBOL", this.symbol);
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
}
