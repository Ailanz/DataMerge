package ui;

import dao.StockDao;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.SegmentedTimeline;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.CandlestickRenderer;
import org.jfree.data.xy.DefaultHighLowDataset;
import org.jfree.data.xy.XYDataset;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.util.List;

/**
 * Created by Ailan on 9/3/2017.
 */
public class MainForm extends JPanel {
    JList list;

    public MainForm() {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(10, 10, 10, 10));
        JScrollPane pane = constructStockList();
        add(pane, BorderLayout.WEST);
        JFreeChart chart = createChart(StockPriceDataSet.getDataSet("AAPL"), "AAPL");
        ChartPanel chartPanel = new ChartPanel(chart);
        add(chartPanel, BorderLayout.CENTER);

        list.addListSelectionListener(ev -> {
            String selected = list.getSelectedValue().toString().trim();
            chartPanel.setChart(createChart(StockPriceDataSet.getDataSet(selected),selected));
            repaint();
            revalidate();
        });


    }

    private JFreeChart createChart(final XYDataset dataset, String symbol) {
        StockDao stock = StockDao.getStock(symbol);
        DateAxis domainAxis = new DateAxis("Date");
        NumberAxis rangeAxis = new NumberAxis("Price");
        CandlestickRenderer renderer = new CandlestickRenderer();

//        renderer.setSeriesPaint(0, Color.BLACK);
//        renderer.setDrawVolume(false);
        rangeAxis.setAutoRangeIncludesZero(false);
        domainAxis.setTimeline(SegmentedTimeline.newMondayThroughFridayTimeline());

        XYPlot mainPlot = new XYPlot(dataset, domainAxis, rangeAxis, renderer);
        JFreeChart chart = new JFreeChart(symbol + " - " + stock.getName(), null, mainPlot, false);
        chart.getXYPlot().setOrientation(PlotOrientation.VERTICAL);
        return chart;
    }

    private JScrollPane constructStockList() {
        DefaultListModel model = new DefaultListModel();
        list = new JList(model);
        JScrollPane pane = new JScrollPane(list);
        list.setSize(300, pane.getHeight());
        List<StockDao> stocks = StockDao.getAllStocks();
        stocks = StockFilter.marketCapFilter(stocks);
        stocks.stream().map(s -> "  " + s.getSymbol()).forEach(model::addElement);
        System.out.println("Size of Symbol: " + stocks.size());
        return pane;
    }

    public static void main(String args[]) {
        setNimbusLnF();
        JFrame frame = new JFrame("Stock Analysis");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(new MainForm());
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
