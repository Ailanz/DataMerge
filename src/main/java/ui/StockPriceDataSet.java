package ui;

import algo.MovingAverage;
import dao.StockPriceDao;
import org.jfree.data.xy.*;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Ailan on 9/3/2017.
 */
public class StockPriceDataSet {

    protected static OHLCDataItem[] getData(String symbol) {
        List<OHLCDataItem> dataItems = new ArrayList<>();
        List<StockPriceDao> sp = StockPriceDao.getAllStockPrices(symbol);

        sp.stream().limit(100).forEach(s->dataItems.add(
                new OHLCDataItem(Date.from(s.getDate().atStartOfDay(ZoneId.systemDefault()).toInstant()),
                        s.getOpen(), s.getHigh(), s.getLow(), s.getClose(), s.getVolume())));

        OHLCDataItem[] data = dataItems.toArray(new OHLCDataItem[dataItems.size()]);
        return data;
    }

    protected static AbstractXYDataset getDataSet(String stockSymbol) {
        DefaultOHLCDataset result = null;
        OHLCDataItem[] data;
        data = getData(stockSymbol);
        result = new DefaultOHLCDataset(stockSymbol, data);

        return result;
    }

    public static AbstractXYDataset simpleMovingAverage(String symbol, int interval){
        MovingAverage mv = new MovingAverage(interval);
        List<OHLCDataItem> dataItems = new ArrayList<>();
        List<StockPriceDao> sp = StockPriceDao.getAllStockPrices(symbol);
        sp.stream().limit(100 + interval).sorted((o1, o2) -> o1.getDate().isBefore(o2.getDate()) ? -1 : 1).forEach(s->{
            mv.add(s.getClose());
            dataItems.add(new OHLCDataItem(Date.from(s.getDate().atStartOfDay(ZoneId.systemDefault()).toInstant()),
                            mv.getAverage(), mv.getAverage(), mv.getAverage(), mv.getAverage(), mv.getAverage()));
        });

        for(int i=0; i < interval; i++){
            dataItems.remove(0);
        }
        return new DefaultOHLCDataset(symbol, dataItems.toArray(new OHLCDataItem[dataItems.size()]));
    }
}
