package ui;

import dao.StockPriceDao;
import org.jfree.data.xy.AbstractXYDataset;
import org.jfree.data.xy.DefaultHighLowDataset;
import org.jfree.data.xy.DefaultOHLCDataset;
import org.jfree.data.xy.OHLCDataItem;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Ailan on 9/3/2017.
 */
public class StockPriceDataSet {

    public static DefaultHighLowDataset getData(String symbol){
        Comparable<String> key = symbol;
        List<StockPriceDao> stockPrices = StockPriceDao.getAllStockPrices(symbol);
        Date date = new Date();
        Date[] dates = stockPrices.stream().map(s -> Date.from(s.getDate().atStartOfDay(ZoneId.systemDefault()).toInstant()))
                .filter(d->d.after(new Date(date.getYear(), date.getMonth()-5, date.getDay())))
                .toArray(size -> new Date[size]);

        double[] high = stockPrices.stream().mapToDouble(s->s.getHigh()).toArray();
        double[] low = stockPrices.stream().mapToDouble(s->s.getLow()).toArray();
        double[] open = stockPrices.stream().mapToDouble(s->s.getOpen()).toArray();
        double[] close = stockPrices.stream().mapToDouble(s->s.getClose()).toArray();
        double[] volume = stockPrices.stream().mapToDouble(s->s.getVolume()).toArray();
        return new DefaultHighLowDataset(key, dates, high, low, open, close, volume);
    }

    protected static OHLCDataItem[] getData2(String symbol) {
        List<OHLCDataItem> dataItems = new ArrayList<>();
        List<StockPriceDao> sp = StockPriceDao.getAllStockPrices(symbol);

        sp.stream().limit(100).forEach(s->dataItems.add(
                new OHLCDataItem(Date.from(s.getDate().atStartOfDay(ZoneId.systemDefault()).toInstant()),
                        s.getOpen(), s.getHigh(), s.getLow(), s.getClose(), s.getVolume())));

        OHLCDataItem[] data = dataItems.toArray(new OHLCDataItem[dataItems.size()]);

        return data;
    }

    protected static AbstractXYDataset getDataSet(String stockSymbol) {
        //This is the dataset we are going to create
        DefaultOHLCDataset result = null;
        //This is the data needed for the dataset
        OHLCDataItem[] data;

        //This is where we go get the data, replace with your own data source
        data = getData2(stockSymbol);

        //Create a dataset, an Open, High, Low, Close dataset
        result = new DefaultOHLCDataset(stockSymbol, data);

        return result;
    }
}
