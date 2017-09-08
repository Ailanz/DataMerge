package ui;

import dao.StockDao;
import exchange.NASDAQ;
import util.PriceUnit;
import util.StockFilterBuilder;

import java.util.List;

/**
 * Created by Ailan on 9/3/2017.
 */
public class StockFilter {
    public static List<StockDao> marketCapFilter(List<StockDao> stocks) {
        StockFilterBuilder builder = StockFilterBuilder.getInstance()
                .withMinMarketCap(PriceUnit.toDouble(1000, PriceUnit.MILLION))
                .withStockExchange(NASDAQ.getInstance())
                .withMaxSharePrice(20)
//                .withLowerThanTargetPrice(false)
                .withAverageVolumeOver(50000);
        return builder.execute(stocks);
    }
}
