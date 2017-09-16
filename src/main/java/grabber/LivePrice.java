package grabber;

import dao.StockPriceDao;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class LivePrice {
    public static List<StockPriceDao> getDaysPrice(String symbol) {
        return getDaysPrice(symbol, AlphaVantageEnum.Interval.FIVE);
    }

    public static List<StockPriceDao> getDaysPrice(String symbol, AlphaVantageEnum.Interval interval) {
        AlphaVantageBuilder builder = AlphaVantageBuilder.aBuilder()
                .withFunction(AlphaVantageEnum.Function.TIME_SERIES_INTRADAY)
                .withOutputSize(AlphaVantageEnum.OutputSize.FULL)
                .withInterval(interval);
        List<ResultData> results = builder.withSymbol(symbol).execute();

        List<StockPriceDao> prices = parseResultData(symbol, results).stream()
                .sorted((o1, o2) -> o1.getDate().isBefore(o2.getDate()) ? -1 : 1).collect(Collectors.toList());


        return prices;
    }

    private static List<StockPriceDao> parseResultData(String stockSymbol, List<ResultData> data) {
        List<StockPriceDao> ret = new LinkedList<>();
        for (ResultData r : data) {
            ret.add(new StockPriceDao(stockSymbol, r.getDate(), r.getData().get("2. high").asDouble(), r.getData().get("3. low").asDouble(), r.getData().get("1. open").asDouble()
                    , r.getData().get("4. close").asDouble(), r.getData().get("4. close").asDouble(),
                    r.getData().get("5. volume").asLong()));
        }
        return ret;
    }
}
