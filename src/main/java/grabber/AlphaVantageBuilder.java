package grabber;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class AlphaVantageBuilder {

    static String baseUrl = "https://www.alphavantage.co/query?";
    static String apikey = "apikey=72OFKJ7KN7414UCF";

    static ObjectMapper mapper = new ObjectMapper();

    private List<Pair<String, String>> params = new LinkedList<>();

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
        params.add(Pair.of("outputsize", size.name()));
        return this;
    }

    public AlphaVantageBuilder withInterval(AlphaVantageEnum.Interval interval) {
        params.add(Pair.of("interval", interval.name().toLowerCase()));
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
        return getResult(sb.toString());
    }

    private static List<ResultData> getResult(String targetUrl) {
        JsonNode node = getJsonNode(targetUrl);
        if (node == null) {
            return null;
        }

        List<JsonNode> list = new ArrayList();
        node.elements().forEachRemaining(list::add);
        List<Pair<LocalDate, JsonNode>> dates = new ArrayList();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        if (list.size() == 1) {
            System.out.println("Cannot parse results: " + targetUrl);
            return null;
        }

        try {
            list.get(1).fields().forEachRemaining(s -> dates.add(Pair.of(LocalDate.parse(s.getKey().split(" ")[0], formatter), s.getValue())));
        } catch (Exception e) {
            System.err.println("Error: " + targetUrl);
        }

        dates.remove(0);   //exclude current incomplete data
        List<ResultData> ret = new LinkedList<>();
        for (Pair<LocalDate, JsonNode> p : dates) {
            HashMap<String, JsonNode> data = new HashMap<>();
            p.getRight().fields().forEachRemaining(s -> data.put(s.getKey(), s.getValue()));
            ret.add(new ResultData(p.getLeft(), data));
        }

        return ret;
    }

    private static JsonNode getJsonNode(String url) {
        URL jsonUrl = null;
        try {
            jsonUrl = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < 3; i++) {  //retry = 3
            try {
                JsonNode node = mapper.readValue(jsonUrl, JsonNode.class);
                if (node.size() != 0) {
                    return node;
                }
            } catch (IOException e) {
                System.err.println("Retrying... " + jsonUrl.toString());
            }
        }

        System.err.println("Giving Up... " + jsonUrl.toString());

        return null;
    }

}

class ResultData {
    private LocalDate date;
    private HashMap<String, JsonNode> data;

    public ResultData(LocalDate date, HashMap<String, JsonNode> data) {
        this.date = date;
        this.data = data;
    }

    public LocalDate getDate() {
        return date;
    }

    public HashMap<String, JsonNode> getData() {
        return data;
    }
}
