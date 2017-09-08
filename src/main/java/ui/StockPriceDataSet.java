package ui;

import algo.ExponentialMovingAverage;
import algo.MovingAverage;
import dao.StockPriceDao;
import grabber.LivePrice;
import org.jfree.data.xy.AbstractXYDataset;
import org.jfree.data.xy.DefaultOHLCDataset;
import org.jfree.data.xy.OHLCDataItem;
import org.joda.time.DateTime;
import util.TimeRange;

import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Ailan on 9/3/2017.
 */
public class StockPriceDataSet {

    protected static OHLCDataItem[] getData(String symbol, TimeRange timeRange) {
        List<OHLCDataItem> dataItems = new ArrayList<>();
//        List<StockPriceDao> sp = StockPriceDao.getAllStockPrices(symbol);
        List<StockPriceDao> sp = LivePrice.getDaysPrice(symbol);


        sp = sp.stream().filter(s -> timeRange.isWithin(s.getDate())).collect(Collectors.toList());
        sp.stream().forEach(s -> dataItems.add(
                new OHLCDataItem(s.getDate().toDate(),
                        s.getOpen(), s.getHigh(), s.getLow(), s.getClose(), s.getVolume())));

        OHLCDataItem[] data = dataItems.toArray(new OHLCDataItem[dataItems.size()]);
        return data;
    }

    protected static AbstractXYDataset getDataSet(String stockSymbol, TimeRange timeRange) {
        DefaultOHLCDataset result = null;
        OHLCDataItem[] data;
        data = getData(stockSymbol, timeRange);
        result = new DefaultOHLCDataset(stockSymbol, data);

        return result;
    }

    public static AbstractXYDataset simpleMovingAverage(String symbol, int interval, TimeRange timeRange) {
        MovingAverage mv = new ExponentialMovingAverage(interval);
        List<OHLCDataItem> dataItems = new ArrayList<>();
//        List<StockPriceDao> sp = StockPriceDao.getAllStockPrices(symbol);
        List<StockPriceDao> sp = LivePrice.getDaysPrice(symbol);

        sp.stream().filter(s -> timeRange.isWithin(s.getDate())).forEach(s -> {
            mv.add(s.getClose());
            dataItems.add(new OHLCDataItem(s.getDate().toDate(),
                    mv.getAverage(), mv.getAverage(), mv.getAverage(), mv.getAverage(), mv.getAverage()));
        });

        for (int i = 0; i < interval; i++) {
            dataItems.remove(0);
        }
        return new DefaultOHLCDataset(symbol, dataItems.toArray(new OHLCDataItem[dataItems.size()]));
    }
}
