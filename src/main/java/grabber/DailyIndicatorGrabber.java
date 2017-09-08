package grabber;

import dao.IndicatorDao;
import dao.StockDao;
import org.joda.time.DateTime;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by Ailan on 9/5/2017.
 */
public class DailyIndicatorGrabber {


    public static void main(String args[]) throws InterruptedException {
//        IndicatorDao.insertIndicator(getIndicators("MDNA.to"));
        List<String> stocks = StockDao.getAllStocks().stream().map(s -> s.getSymbol()).collect(Collectors.toList());
        ;
        populateIndicators(stocks);
    }

    public static void populateIndicators(List<String> symbols) throws InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(30);

        for (String sym : symbols) {
            Runnable task = () -> {

                List<IndicatorDao> indicators = getIndicators(sym);
                if (indicators == null || indicators.size() == 0) {
                    System.out.println("INDICATOR Missing: " + sym);
                } else {
                    IndicatorDao.insertIndicator(indicators);
                    System.out.println("INDICATOR: Processed: " + sym);
                }
            };
            pool.execute(task);
        }
        pool.shutdown();
        pool.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);

    }

    public static List<IndicatorDao> getIndicators(String symbol) {
        return getIndicators(symbol, AlphaVantageEnum.Interval.DAILY, 7);
    }

    public static List<IndicatorDao> getIndicators(String symbol, AlphaVantageEnum.Interval interval, int timePeriod) {
        AlphaVantageBuilder adxBuilder = AlphaVantageBuilder.aBuilder()
                .withFunction(AlphaVantageEnum.Function.ADX)
                .withInterval(interval)
                .withTimePeriod(timePeriod);

        AlphaVantageBuilder macdBuilder = AlphaVantageBuilder.aBuilder()
                .withFunction(AlphaVantageEnum.Function.MACD)
                .withInterval(interval)
                .withSeriesType(AlphaVantageEnum.SeriesType.CLOSE);

        AlphaVantageBuilder rsiBuilder = AlphaVantageBuilder.aBuilder()
                .withFunction(AlphaVantageEnum.Function.RSI)
                .withInterval(interval)
                .withSeriesType(AlphaVantageEnum.SeriesType.CLOSE);

        AlphaVantageBuilder cciBuilder = AlphaVantageBuilder.aBuilder()
                .withFunction(AlphaVantageEnum.Function.CCI)
                .withInterval(AlphaVantageEnum.Interval.DAILY)
                .withTimePeriod(timePeriod);

        AlphaVantageBuilder aroonBuilder = AlphaVantageBuilder.aBuilder()
                .withFunction(AlphaVantageEnum.Function.AROON)
                .withInterval(interval)
                .withTimePeriod(timePeriod);

        HashMap<DateTime, DataStore> dataGroup = new HashMap<>();

        for (int i = 0; i < 5; i++) {
            try {
                adxBuilder.withSymbol(symbol).execute().forEach(r -> {
                    dataGroup.computeIfAbsent(r.getDate(), k -> new DataStore());
                    dataGroup.get(r.getDate()).setAdx(r.getData().get("ADX").asDouble());
                });
//                macdBuilder.withSymbol(symbol).execute().forEach(r -> {
//                    dataGroup.computeIfAbsent(r.getDate(), k -> new DataStore());
//                    dataGroup.get(r.getDate()).setMacd(r.getData().get("MACD").asDouble());
//                    dataGroup.get(r.getDate()).setMacdHist(r.getData().get("MACD_Hist").asDouble());
//                    dataGroup.get(r.getDate()).setMacdSignal(r.getData().get("MACD_Signal").asDouble());
//                });
//
                rsiBuilder.withSymbol(symbol).withTimePeriod(7).execute().forEach(r -> {
                    dataGroup.computeIfAbsent(r.getDate(), k -> new DataStore());
                    dataGroup.get(r.getDate()).setRsi7(r.getData().get("RSI").asDouble());
                });
//
//                rsiBuilder.withSymbol(symbol).withTimePeriod(14).execute().forEach(r -> {
//                    dataGroup.computeIfAbsent(r.getDate(), k -> new DataStore());
//                    dataGroup.get(r.getDate()).setRsi14(r.getData().get("RSI").asDouble());
//                });
//
//                rsiBuilder.withSymbol(symbol).withTimePeriod(25).execute().forEach(r -> {
//                    dataGroup.computeIfAbsent(r.getDate(), k -> new DataStore());
//                    dataGroup.get(r.getDate()).setRsi25(r.getData().get("RSI").asDouble());
//                });
//
//                cciBuilder.withSymbol(symbol).execute().forEach(r -> {
//                    dataGroup.computeIfAbsent(r.getDate(), k -> new DataStore());
//                    dataGroup.get(r.getDate()).setCci(r.getData().get("CCI").asDouble());
//                });

//                aroonBuilder.withSymbol(symbol).execute().forEach(r -> {
//                    dataGroup.computeIfAbsent(r.getDate(), k -> new DataStore());
//                    dataGroup.get(r.getDate()).setAroonUp(r.getData().get("Aroon Up").asDouble());
//                    dataGroup.get(r.getDate()).setAroonDown(r.getData().get("Aroon Down").asDouble());
//                });
                break;
            } catch (Exception e) {
                System.err.println("Retry...");
            }
        }

        List<IndicatorDao> ret = new LinkedList<>();

        dataGroup.entrySet().stream().sorted((e1, e2) -> e1.getKey().isBefore(e2.getKey()) ? 1 : -1)
                .forEach(e -> ret.add(new IndicatorDao(symbol, e.getKey(), e.getValue().getAdx(),
                        e.getValue().getMacd(), e.getValue().getMacdSignal(), e.getValue().getMacdHist(),
                        e.getValue().getRsi7(), e.getValue().getRsi14(), e.getValue().getRsi25(), e.getValue().getCci(),
                        e.getValue().getAroonUp(), e.getValue().getAroonDown())));

        return ret;
    }
}

class DataStore {
    private double adx = -1;
    private double macd = -1;
    private double macdSignal = -1;
    private double macdHist = -1;
    private double rsi14 = -1;
    private double rsi25 = -1;
    private double cci = -1;
    private double aroonUp = -1;
    private double aroonDown = -1;
    private double rsi7 = -1;

    public DataStore() {
    }

    public DataStore(double adx, double macd, double macdSignal, double macdHist, double rsi7, double rsi14, double rsi25, double cci, double aroonUp, double aroonDown) {
        this.adx = adx;
        this.macd = macd;
        this.macdSignal = macdSignal;
        this.macdHist = macdHist;
        this.rsi7 = rsi7;
        this.rsi14 = rsi14;
        this.rsi25 = rsi25;
        this.cci = cci;
        this.aroonUp = aroonUp;
        this.aroonDown = aroonDown;
    }

    public double getAdx() {
        return adx;
    }

    public void setAdx(double adx) {
        this.adx = adx;
    }

    public double getMacd() {
        return macd;
    }

    public void setMacd(double macd) {
        this.macd = macd;
    }

    public double getMacdSignal() {
        return macdSignal;
    }

    public void setMacdSignal(double macdSignal) {
        this.macdSignal = macdSignal;
    }

    public double getMacdHist() {
        return macdHist;
    }

    public void setMacdHist(double macdHist) {
        this.macdHist = macdHist;
    }

    public double getRsi7() {
        return rsi7;
    }

    public void setRsi7(double rsi7) {
        this.rsi7 = rsi7;
    }

    public double getRsi14() {
        return rsi14;
    }

    public void setRsi14(double rsi14) {
        this.rsi14 = rsi14;
    }

    public double getRsi25() {
        return rsi25;
    }

    public void setRsi25(double rsi25) {
        this.rsi25 = rsi25;
    }

    public double getCci() {
        return cci;
    }

    public void setCci(double cci) {
        this.cci = cci;
    }

    public double getAroonUp() {
        return aroonUp;
    }

    public void setAroonUp(double aroonUp) {
        this.aroonUp = aroonUp;
    }

    public double getAroonDown() {
        return aroonDown;
    }

    public void setAroonDown(double aroonDown) {
        this.aroonDown = aroonDown;
    }
}
