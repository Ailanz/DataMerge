package util;

import dao.StockDao;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Ailan on 9/3/2017.
 */
public class StockFilterBuilder {

    private double maxMarketCap = Double.MAX_VALUE;
    private double minMarketCap = 0;

    private StockFilterBuilder() {}

    public static StockFilterBuilder getInstance(){
        return new StockFilterBuilder();
    }

    public StockFilterBuilder withMaxMarketCap(double max){
        this.maxMarketCap = max;
        return this;
    }

    public StockFilterBuilder withMinMarketCap(double min){
        this.minMarketCap = min;
        return this;
    }

    public List<StockDao> execute(List<StockDao> stocks){
        return stocks.stream().filter(s->s.getMarketCap() < maxMarketCap && s.getMarketCap() > minMarketCap).collect(Collectors.toList());
    }

}
