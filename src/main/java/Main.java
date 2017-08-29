import db.SqliteDriver;
import external.StockExchange;
import grabber.DailyPriceGrabber;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

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

        DailyPriceGrabber.populateStockPrices(symbols);
        SqliteDriver.insertStockSymbols(symbols, StockExchange.TSX);

        System.out.println("Hello World!");
    }


}



