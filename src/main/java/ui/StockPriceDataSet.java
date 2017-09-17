package ui;

import algo.ExponentialMovingAverage;
import algo.MovingAverage;
import dao.DayDataDao;
import dao.StockDao;
import dao.StockPriceDao;
import grabber.LivePrice;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.SegmentedTimeline;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.CandlestickRenderer;
import org.jfree.data.xy.AbstractXYDataset;
import org.jfree.data.xy.DefaultOHLCDataset;
import org.jfree.data.xy.OHLCDataItem;
import org.jfree.data.xy.XYDataset;
import org.joda.time.DateTime;
import util.TimeRange;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
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
        List<StockPriceDao> sp = StockPriceDao.getAllStockPrices(symbol);
//        List<StockPriceDao> sp = LivePrice.getDaysPrice(symbol);

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

    public static AbstractXYDataset convertToXYDataSet(List<DayDataDao> data){
        List<OHLCDataItem> dataItems = new ArrayList<>();
        if(data==null || data.size() ==0) {
            return null;
        }

        String stockSymbol = data.get(0).getSymbol();
        data.stream().forEach(s -> dataItems.add(
                new OHLCDataItem(s.getDate().toDate(),
                        s.getOpen(), s.getHigh(), s.getLow(), s.getClose(), s.getVolume())));

        OHLCDataItem[] dataItem = dataItems.toArray(new OHLCDataItem[dataItems.size()]);
        return new DefaultOHLCDataset(stockSymbol, dataItem);
    }

    public static JFreeChart createChart(final XYDataset dataset, String symbol) {
        StockDao stock = StockDao.getStock(symbol);
        DateAxis domainAxis = new DateAxis("Date");
        NumberAxis rangeAxis = new NumberAxis("Price");
        CandlestickRenderer renderer = new CandlestickRenderer();

        rangeAxis.setAutoRangeIncludesZero(false);
        domainAxis.setTimeline(SegmentedTimeline.newMondayThroughFridayTimeline());

        XYPlot mainPlot = new XYPlot(dataset, domainAxis, rangeAxis, renderer);


        JFreeChart chart = new JFreeChart(symbol + " - " + stock.getName(), null, mainPlot, false);
        chart.getXYPlot().setOrientation(PlotOrientation.VERTICAL);
        return chart;
    }

    public static void saveChart(XYDataset dataset, String symbol){
        JFreeChart chart = createChart(dataset, symbol);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ChartUtilities.writeScaledChartAsPNG(baos, chart, 800, 500, 4, 3);
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] bi=baos.toByteArray();
        InputStream in = new ByteArrayInputStream(bi);
        BufferedImage image = null;
        try {
            image = ImageIO.read(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        File outputfile = new File("C:\\Users\\Ailan\\Desktop\\github\\data\\" + symbol + ".png");
        try {
            ImageIO.write(image, "png", outputfile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
