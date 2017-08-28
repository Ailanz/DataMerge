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

public class AlphaVantageApi {
    public static String TIME_SERIES_DAILY = "TIME_SERIES_DAILY";
    static String url = "https://www.alphavantage.co/query?function=%s&symbol=%s&outputsize=full&apikey=72OFKJ7KN7414UCF";
    static ObjectMapper mapper = new ObjectMapper();

    public static List<ResultData> getResult(String function, String symbol) {
        String targetUrl = String.format(url, function, symbol);
        JsonNode node = getJsonNode(targetUrl);
        if (node == null) {
            return null;
        }

        List<JsonNode> list = new ArrayList();
        node.elements().forEachRemaining(list::add);
        List<Pair<LocalDate, JsonNode>> dates = new ArrayList();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        if (list.size() == 1) {
            return null;
        }

        try {
            list.get(1).fields().forEachRemaining(s -> dates.add(Pair.of(LocalDate.parse(s.getKey().split(" ")[0], formatter), s.getValue())));
        } catch (Exception e) {
            System.err.println("Error: " + url);
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
