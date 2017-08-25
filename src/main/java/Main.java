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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {

    public static void main(String[] args) throws IOException, URISyntaxException, SQLException, InterruptedException {
        Scanner scan = new Scanner(new File("/var/tmp/TSX.txt"));
//        Scanner scan = new Scanner(new File("D:\\data\\TSX.txt"));
        scan.nextLine();

        List<String> symbols = new LinkedList<>();
        int count = 0;
        while (scan.hasNext()) {
            count++;
            symbols.add(scan.nextLine().split("\t")[0] + ".to");
        }

        SqliteDriver.insertStockSymbols(symbols);
        System.out.println(count);

        ExecutorService pool = Executors.newFixedThreadPool(10);


        for (String sym : symbols) {
            Runnable task = () -> {
                List<StockPriceDao> prices = DailyPriceGrabber.getStockPrices(sym);
                if (prices == null) {
                    System.out.println("Missing: " + sym);
                } else {
                    SqliteDriver.insertStockPrice(prices);
                    System.out.println("Processed: " + sym);
                }};
                pool.execute(task);
        }
//        pool.awaitTermination(1, TimeUnit.DAYS);
        pool.shutdown();
        System.out.println("Hello World!");
    }


}



