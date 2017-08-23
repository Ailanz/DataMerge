import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.javafx.binding.StringFormatter;
import com.sun.tools.javac.util.Pair;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

    static String key = "72OFKJ7KN7414UCF";
    static String url = "https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&symbol=%s&outputsize=full&apikey=72OFKJ7KN7414UCF";

    public static void main(String[] args) throws IOException, URISyntaxException {
        Scanner scan = new Scanner(new File("/var/tmp/TSX.txt"));
        scan.nextLine();

        List<String> symbols = new LinkedList<>();
        while (scan.hasNext()) {
            symbols.add(scan.nextLine().split("\t")[0]);
        }

        int countSuc = 0;
        int countFail = 0;
        ExecutorService pool = Executors.newFixedThreadPool(10);


        for (String sym : symbols) {
            Runnable task = () -> { List<StockPrice> prices = getStockPrices(sym + ".to");
                if (prices == null) {
                    System.out.println("Missing: " + sym);
                } else {
                    System.out.println("Processed: " + sym);
                }};
                pool.execute(task);
        }
        System.out.println("Hello World!");
    }

    private static List<StockPrice> getStockPrices(String stockSymbol) {
        URL jsonUrl;
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = null;

        try {
            jsonUrl = new URL(String.format(url, stockSymbol));
            node = mapper.readValue(jsonUrl, JsonNode.class);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        List<JsonNode> list = new ArrayList();
        node.elements().forEachRemaining(list::add);
        List<Pair<LocalDate, JsonNode>> datePrices = new ArrayList();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        if (list.size() == 1) {
            return null;
        }
        list.get(1).fields().forEachRemaining(s -> datePrices.add(Pair.of(LocalDate.parse(s.getKey().split(" ")[0], formatter), s.getValue())));

        datePrices.remove(0);
        List<StockPrice> prices = new LinkedList<>();

        datePrices.forEach(p -> prices.add(new StockPrice(p.fst, p.snd.get("1. open").asDouble(), p.snd.get("2. high").asDouble(),
                p.snd.get("3. low").asDouble(), p.snd.get("4. close").asDouble(), p.snd.get("5. volume").asLong())));

        return prices;
    }
}



