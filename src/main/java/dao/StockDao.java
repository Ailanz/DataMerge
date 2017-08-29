package dao;

import db.TableBuilder;
import external.StockExchange;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class StockDao implements AbstractDao{
    String symbol;
    String exchange;
    LocalDate updated;

    static TableBuilder stockTableBuilder = TableBuilder.aBuilder().withTableName("Stock")
            .withColumn("SYMBOL", TableBuilder.FIELD_TYPE.TEXT)
            .withColumn("EXCHANGE", TableBuilder.FIELD_TYPE.TEXT)
            .withColumn("UPDATED", TableBuilder.FIELD_TYPE.TEXT)
            .withprimaryKeys("SYMBOL");

    public StockDao(String symbol, String exchange, LocalDate updated) {
        this.symbol = symbol;
        this.updated = updated;
        this.exchange = exchange;
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

    public static TableBuilder getTableBuilder() {
        return stockTableBuilder;
    }


    @Override
    public Map<String, String> getParams() {
        HashMap<String,String> map = new HashMap<>();
        map.put("SYMBOL", this.symbol);
        map.put("EXCHANGE", this.exchange);
        map.put("UPDATED", LocalDate.now().toString());
        return map;
    }
}
