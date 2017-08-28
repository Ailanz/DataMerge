import dao.StockPriceDao;
import db.SqliteDriver;
import grabber.DailyPriceGrabber;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {

    public static void main(String[] args) throws IOException, URISyntaxException, SQLException, InterruptedException {
//        Scanner scan = new Scanner(new File("/var/tmp/TSX.txt"));
        Scanner scan = new Scanner(new File("D:\\data\\TSX.txt"));
        scan.nextLine();

        List<String> symbols = new LinkedList<>();
        int count = 0;
        while (scan.hasNext()) {
            count++;
            symbols.add(scan.nextLine().split("\t")[0] + ".to");
        }

        System.out.println(count);

        populateStockPrices(symbols);
        SqliteDriver.insertStockSymbols(symbols);

        System.out.println("Hello World!");
    }

    private static void populateStockPrices(List<String> symbols) throws InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(5);

        List<String> invalidSymbols = new CopyOnWriteArrayList<>();
        for (String sym : symbols) {
            Runnable task = () -> {
                List<StockPriceDao> prices = DailyPriceGrabber.getStockPrices(sym);
                if (prices == null) {
                    invalidSymbols.add(sym);
                    System.out.println("Missing: " + sym);
                } else {
                    SqliteDriver.insertStockPrice(prices);
                    System.out.println("Processed: " + sym);
                }};
                pool.execute(task);
        }
        pool.shutdown();
        pool.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);

        invalidSymbols.stream().forEach(s -> symbols.remove(s));
    }


}



