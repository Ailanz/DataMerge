package grabber;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringUtils;
import util.PriceUnit;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;

/**
 * Created by Ailan on 9/2/2017.
 */
public class YahooFinanceBuilder extends UrlHelper {
    private static String url = "https://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20yahoo.finance.quotes%20where%20symbol%20in%20(%22%s%22)&format=json&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys";

    private List<String> symbols = new LinkedList<>();
    private int batch = 20;

    public static void main(String args[]) {
//        execute(new LinkedList(new String[]{"aapl", "ebay"}));
    }

    private YahooFinanceBuilder() {}

    public static YahooFinanceBuilder getInstance(){
        return new YahooFinanceBuilder();
    }

    public YahooFinanceBuilder withSymbols(List<String> symbols) {
        this.symbols.addAll(symbols);
        return this;
    }

    public YahooFinanceBuilder withBatch(int batch) {
        this.batch = batch;
        return this;
    }

    public Map<String, YahooResult> execute() {
        ExecutorService pool = Executors.newFixedThreadPool(2);

        Map<String, YahooResult> results = new HashMap<>();
        List<List<String>> chunks = chunkTheList(symbols);

        //Thread it!
        chunks.forEach( symList -> {
            Runnable task = getWork(results, symList);
            pool.execute(task);
        });
        pool.shutdown();
        try {
            pool.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return results;
    }

    private Runnable getWork(Map<String, YahooResult> results, List<String> symList) {
        return () -> {
                    String symbolList = StringUtils.join(symList, ',').replace('[', ' ').replace(']', ' ');
                    JsonNode node = getJsonNode(url.replace("%s", symbolList), 100);
                    node = node.get("query").get("results").get("quote");
                    if (symList.size() > 1) {
                        node.forEach(s -> {
                            YahooResult r = getYahooResult(s);
                            results.put(r.getSymbol(), r);
                        });
                    } else {
                        YahooResult r = getYahooResult(node);
                        results.put(r.getSymbol(), r);
                    }
                };
    }

    private List<List<String>> chunkTheList(List<String> source){

        List<List<String>> groups = range(0, source.size())
                .boxed()
                .collect(groupingBy(index -> index / batch))
                .values()
                .stream()
                .map(indices -> indices
                        .stream()
                        .map(source::get)
                        .collect(toList()))
                .collect(toList());
        return groups;
    }

    private static YahooResult getYahooResult(JsonNode s) {
        String symbol = s.get("symbol").asText();
        String name = s.get("Name").asText();
        double ask = PriceUnit.parseNumber(s.get("Ask").asText());
        double bid = PriceUnit.parseNumber(s.get("Bid").asText());
        double dividendShare = PriceUnit.parseNumber(s.get("DividendShare").asText());
        double dividendYield = PriceUnit.parseNumber(s.get("DividendYield").asText());
        double earningsShare = PriceUnit.parseNumber(s.get("EarningsShare").asText());
        double epseEstimateCurrentYear = PriceUnit.parseNumber(s.get("EPSEstimateCurrentYear").asText());
        double epseEseEstimateNextYear = PriceUnit.parseNumber(s.get("EPSEstimateNextYear").asText());
        double marketCap = PriceUnit.parseNumber(s.get("MarketCapitalization").asText());
        double ebitada = PriceUnit.parseNumber(s.get("EBITDA").asText());
        double peRatio = PriceUnit.parseNumber(s.get("PERatio").asText());
        double yearTargetPrice = PriceUnit.parseNumber(s.get("OneyrTargetPrice").asText());
        double spread = (ask == -1 || bid == -1) ? -1 : Math.round((ask - bid) * 100.0) / 100.0;
        return new YahooResult(symbol, name, spread, dividendShare, dividendYield, earningsShare,
                epseEstimateCurrentYear, epseEseEstimateNextYear, marketCap, ebitada, peRatio, yearTargetPrice);
    }


}
