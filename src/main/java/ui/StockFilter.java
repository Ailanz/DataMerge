package ui;

import dao.StockDao;
import util.PriceUnit;
import util.StockFilterBuilder;

import java.util.List;

/**
 * Created by Ailan on 9/3/2017.
 */
public class StockFilter {
    public static List<StockDao> marketCapFilter(List<StockDao> stocks) {
        StockFilterBuilder builder = StockFilterBuilder.getInstance().withMinMarketCap(PriceUnit.toDouble(100,PriceUnit.MILLION));
        return builder.execute(stocks);
    }
}
