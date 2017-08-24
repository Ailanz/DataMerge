import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dao.StockPrice;
import db.SqliteDriver;
import grabber.DailyPriceGrabber;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

    public static void main(String[] args) throws IOException, URISyntaxException, SQLException {
        Scanner scan = new Scanner(new File("/var/tmp/TSX.txt"));
//        Scanner scan = new Scanner(new File("D:\\data\\TSX.txt"));
        scan.nextLine();

        List<String> symbols = new LinkedList<>();
        int count = 0;
        while (scan.hasNext()) {
            count++;
            symbols.add(scan.nextLine().split("\t")[0]);
        }
        System.out.println(count);

        ExecutorService pool = Executors.newFixedThreadPool(10);


        for (String sym : symbols) {
            Runnable task = () -> {
                List<StockPrice> prices = DailyPriceGrabber.getStockPrices(sym + ".to");
                if (prices == null) {
                    System.out.println("Missing: " + sym);
                } else {
                    SqliteDriver.insertStockPrice(prices);
                    System.out.println("Processed: " + sym);
                }};
                pool.execute(task);
        }
        System.out.println("Hello World!");
    }


}



