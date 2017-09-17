package ui;

import core.TransactionRecord;
import core.strategy.CandleStickStrategyBuilder;
import core.strategy.ExitIntervalEnum;
import dao.StockDao;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.SegmentedTimeline;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.CandlestickRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.joda.time.DateTime;
import simulator.LongTradeSimulator;
import util.TimeRange;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Created by Ailan on 9/16/2017.
 */
public class StrategyUI extends JPanel {
    JList list;
    XYPlot mainPlot;

    public StrategyUI() {
        ExecutorService pool = Executors.newFixedThreadPool(30);

        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(10, 10, 10, 10));
        JScrollPane pane = constructStockList();
        add(pane, BorderLayout.WEST);

        DateTime minDate = new DateTime(2016, 9, 6, 0, 0);
        DateTime maxDate = new DateTime(2017, 9, 7, 0, 0);
        TimeRange timeRange = new TimeRange(minDate, maxDate);

        JFreeChart chart = createChart(StockPriceDataSet.getDataSet("AAPL", timeRange), "AAPL", timeRange);
        ChartPanel chartPanel = new ChartPanel(chart);
        add(chartPanel, BorderLayout.CENTER);
//
        list.addListSelectionListener(ev -> {
//            Runnable task = () -> {
                String selected = list.getSelectedValue().toString().trim();
//                System.out.println(selected);
                chartPanel.setChart(createChart(StockPriceDataSet.getDataSet(selected, timeRange), selected, timeRange));
//                add(chartPanel, BorderLayout.CENTER);
//                mainPlot.setDataset(0, StockPriceDataSet.getDataSet(selected, timeRange));
                getParent().repaint();
                getParent().revalidate();
                //                super.repaint();
//                super.revalidate();
//            };
//            pool.execute(task);

        });
//        repaint();
//        revalidate();
    }


    private JFreeChart createChart(final XYDataset dataset, String symbol, TimeRange timeRange) {
        StockDao stock = StockDao.getStock(symbol);
        DateAxis domainAxis = new DateAxis("Date");
        NumberAxis rangeAxis = new NumberAxis("Price");
        CandlestickRenderer renderer = new CandlestickRenderer();

        rangeAxis.setAutoRangeIncludesZero(false);
        domainAxis.setTimeline(SegmentedTimeline.newMondayThroughFridayTimeline());

        mainPlot = new XYPlot(dataset, domainAxis, rangeAxis, renderer);


        JFreeChart chart = new JFreeChart(symbol + " - " + stock.getName(), null, mainPlot, false);
        chart.getXYPlot().setOrientation(PlotOrientation.VERTICAL);
        return chart;
    }

    private JScrollPane constructStockList() {
        DefaultListModel model = new DefaultListModel();
        list = new JList(model);
        JScrollPane pane = new JScrollPane(list);
        list.setSize(300, pane.getHeight());
        stockSymbols().stream().map(s -> "  " + s).forEach(model::addElement);
        System.out.println("Size of Symbol: " + model.size());
        return pane;
    }

    private List<String> stockSymbols(){
        List<String> stocks = LongTradeSimulator.executeStrategy().stream().map(s->s.getSymbol()).collect(Collectors.toList());
        return stocks;
    }

    public static void main(String args[]) {
        setNimbusLnF();
        JFrame frame = new JFrame("Stock Analysis");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(new StrategyUI());
        frame.setSize(1200, 700);
        frame.setResizable(false);
        frame.setVisible(true);
    }

    private static void setNimbusLnF() {
        for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
            if ("Nimbus".equals(info.getName())) {
                try {
                    UIManager.setLookAndFeel(info.getClassName());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }
}
