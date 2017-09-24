package ui;

import dao.StockDao;
import exchange.NASDAQ;
import exchange.SP500;
import util.PriceUnit;
import util.StockFilterBuilder;

import java.util.List;

/**
 * Created by Ailan on 9/3/2017.
 */
public class StockFilter {
    public static List<StockDao> marketCapFilter(List<StockDao> stocks) {
        StockFilterBuilder builder = StockFilterBuilder.getInstance()
                .withMinMarketCap(PriceUnit.toDouble(100, PriceUnit.MILLION))
                .withMinSharePrice(2)
//                .withStockExchange(SP500.getInstance())
//                .withMaxSharePrice(30)
//                .withPositiveMA(true)
//                .withLowerThanTargetPrice(false)
                .withAverageVolumeOver(5000);
        return builder.execute(stocks);
    }
}
