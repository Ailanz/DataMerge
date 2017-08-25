package grabber;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dao.StockPriceDao;
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

    public static  List<ResultData> getResult(String function, String symbol){
        JsonNode node = getJsonNode(String.format(url, function, symbol));
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

        list.get(1).fields().forEachRemaining(s -> dates.add(Pair.of(LocalDate.parse(s.getKey().split(" ")[0], formatter), s.getValue())));

        dates.remove(0);   //exclude current incomplete data
        List<StockPriceDao> prices = new LinkedList<>();
        List<ResultData> ret = new LinkedList<>();
        for(Pair<LocalDate, JsonNode>  p : dates) {
            HashMap<String, JsonNode> data = new HashMap<>();
            p.getRight().fields().forEachRemaining(s -> data.put(s.getKey(), s.getValue()));
            ret.add(new ResultData(p.getLeft(), data));
        }

        return ret;
    }

    public static List<StockPriceDao> getStockPrices(String stockSymbol) {
        List<ResultData> data = getResult(TIME_SERIES_DAILY, stockSymbol);

        if(data == null){
            return null;
        }

        List<StockPriceDao> ret = new LinkedList<>();
        for(ResultData r : data) {
            ret.add(new StockPriceDao(stockSymbol, r.getDate(), r.getData().get("1. open").asDouble(), r.getData().get("2. high").asDouble(),
                    r.getData().get("3. low").asDouble(), r.getData().get("4. close").asDouble(), r.getData().get("5. volume").asLong()));
        }

        return ret;
    }

    private static JsonNode getJsonNode(String url) {
        URL jsonUrl = null;
        JsonNode node;

        try {
            jsonUrl = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < 3; i++) {  //retry = 3
            try {
                return mapper.readValue(jsonUrl, JsonNode.class);
            } catch (IOException e) {
                e.printStackTrace();
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

    public ResultData(LocalDate date,  HashMap<String, JsonNode> data) {
        this.date = date;
        this.data = data;
    }

    public LocalDate getDate() {
        return date;
    }

    public  HashMap<String, JsonNode> getData() {
        return data;
    }

}
