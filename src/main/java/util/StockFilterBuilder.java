package util;

import dao.StockDao;
import dao.StockPriceDao;
import exchange.StockExchange;
import org.joda.time.DateTime;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Ailan on 9/3/2017.
 */
public class StockFilterBuilder {

    private double maxMarketCap = Double.MAX_VALUE;
    private double minMarketCap = 0;
    private double averageVolume = 0;
    private StockExchange stockExchange = null;
    private Boolean lowerThanTarget = null;

    private StockFilterBuilder() {
    }

    public static StockFilterBuilder getInstance() {
        return new StockFilterBuilder();
    }

    public StockFilterBuilder withMaxMarketCap(double max) {
        this.maxMarketCap = max;
        return this;
    }

    public StockFilterBuilder withMinMarketCap(double min) {
        this.minMarketCap = min;
        return this;
    }

    public StockFilterBuilder withAverageVolumeOver(double volume) {
        this.averageVolume = volume;
        return this;
    }

    public StockFilterBuilder withStockExchange(StockExchange exchange) {
        this.stockExchange = exchange;
        return this;
    }

    public StockFilterBuilder withLowerThanTargetPrice(boolean isLower) {
        this.lowerThanTarget = isLower;
        return this;
    }

    public List<StockDao> execute(List<StockDao> stocks) {
        return stocks.stream()
                .filter(s -> s.getMarketCap() < maxMarketCap && s.getMarketCap() > minMarketCap)
                .filter(s -> stockExchange == null || s.getExchange().equals(stockExchange.getExchange()))
                .filter(this::filterByTargetPrice)
                .filter(this::filterAverageVolume)
                .filter(s -> s.getLatestPrice().getDate().isAfter(DateTime.now().minusDays(7)))
                .collect(Collectors.toList());

    }

    private boolean filterByTargetPrice(StockDao s){
        if(lowerThanTarget==null) {
            return true;
        }
        if(lowerThanTarget) {
            return s.getYearTargetPrice() - s.getLatestPrice().getClose() > 0;
        }else {
            return s.getYearTargetPrice() - s.getLatestPrice().getClose() < 0;
        }
    }

    private boolean filterAverageVolume(StockDao s) {
        if (averageVolume == 0) {
            return true;
        }
        List<StockPriceDao> prices = s.getPrices();
        return (prices.stream().mapToLong(sa -> sa.getVolume()).sum() / prices.size()) > averageVolume;
    }

}
