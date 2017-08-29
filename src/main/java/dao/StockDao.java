package dao;

import external.StockExchange;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class StockDao implements AbstractDao{
    String symbol;
    StockExchange exchange;
    LocalDate updated;

    public StockDao(String symbol, StockExchange exchange, LocalDate updated) {
        this.symbol = symbol;
        this.updated = updated;
        this.exchange = exchange;
    }

    public String getSymbol() {
        return symbol;
    }

    public StockExchange getExchange() {
        return exchange;
    }

    public LocalDate getUpdated() {
        return updated;
    }


    @Override
    public Map<String, String> getParams() {
        HashMap<String,String> map = new HashMap<>();
        map.put("SYMBOL", this.symbol);
        map.put("EXCHANGE", exchange.name());
        map.put("UPDATED", LocalDate.now().toString());
        return map;
    }
}
