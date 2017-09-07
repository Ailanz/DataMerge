package grabber;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.tuple.Pair;
import org.joda.time.DateTime;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class AlphaVantageBuilder extends UrlHelper {

    static String baseUrl = "https://www.alphavantage.co/query?";
    static String apikey = "apikey=72OFKJ7KN7414UCF";

    private List<Pair<String, String>> params = new LinkedList<>();

    static {
        System.setProperty("sun.net.client.defaultConnectTimeout", "30000");
        System.setProperty("sun.net.client.defaultReadTimeout", "30000");
    }

    public static void main(String[] args) {
        AlphaVantageBuilder builder = aBuilder()
                .withSymbol("ANX.to")
                .withFunction(AlphaVantageEnum.Function.ADX)
                .withInterval(AlphaVantageEnum.Interval.DAILY)
                .withTimePeriod(60);
        List<ResultData> result = builder.execute();
        System.out.println(result.size());
    }

    private AlphaVantageBuilder() {
    }

    public static AlphaVantageBuilder aBuilder() {
        return new AlphaVantageBuilder();
    }

    public AlphaVantageBuilder withFunction(AlphaVantageEnum.Function function) {
        params.add(Pair.of("function", function.name()));
        return this;
    }

    public AlphaVantageBuilder withSymbol(String symbol) {
        params.add(Pair.of("symbol", symbol));
        return this;
    }

    public AlphaVantageBuilder withOutputSize(AlphaVantageEnum.OutputSize size) {
        params.add(Pair.of("outputsize", size.name().toLowerCase()));
        return this;
    }

    public AlphaVantageBuilder withInterval(AlphaVantageEnum.Interval interval) {
        params.add(Pair.of("interval", interval.name().toLowerCase()));
        return this;
    }

    public AlphaVantageBuilder withSeriesType(AlphaVantageEnum.SeriesType seriesType) {
        params.add(Pair.of("series_type", seriesType.name().toLowerCase()));
        return this;
    }

    public AlphaVantageBuilder withTimePeriod(int period) {
        params.add(Pair.of("time_period", String.valueOf(period)));
        return this;
    }

    public List<ResultData> execute() {
        StringBuilder sb = new StringBuilder();
        sb.append(baseUrl);
        params.forEach(s -> sb.append(s.getKey() + "=" + s.getValue() + "&"));
        sb.append(apikey);
        return getResult(sb.toString(), 3);
    }

    private static List<ResultData> getResult(String targetUrl, int retry) {
        JsonNode node = getJsonNode(targetUrl);
        if (node == null) {
            return null;
        }

        List<JsonNode> list = new ArrayList();
        node.elements().forEachRemaining(list::add);
        List<Pair<DateTime, JsonNode>> dates = new ArrayList();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        if (list.size() == 1) {
            if (retry > 0) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return getResult(targetUrl, retry - 1);
            } else {
                System.out.println("Cannot parse results: " + targetUrl);
                return null;
            }
        }

        try {
            list.get(1).fields().forEachRemaining(s -> dates.add(Pair.of(DateTime.parse(s.getKey().split(" ")[0]), s.getValue())));
        } catch (Exception e) {
            System.err.println("Error: " + targetUrl);
        }

        if(dates.size()==0) {
            return getResult(targetUrl, retry - 1);
        }
        dates.remove(0);   //exclude current incomplete data
        List<ResultData> ret = new LinkedList<>();
        for (Pair<DateTime, JsonNode> p : dates) {
            HashMap<String, JsonNode> data = new HashMap<>();
            p.getRight().fields().forEachRemaining(s -> data.put(s.getKey(), s.getValue()));
            ret.add(new ResultData(p.getLeft(), data));
        }

        return ret;
    }

}

class ResultData {
    private DateTime date;
    private HashMap<String, JsonNode> data;

    public ResultData(DateTime date, HashMap<String, JsonNode> data) {
        this.date = date;
        this.data = data;
    }

    public DateTime getDate() {
        return date;
    }

    public HashMap<String, JsonNode> getData() {
        return data;
    }
}
