package exchange;

import dao.StockDao;
import org.joda.time.DateTime;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class TSX implements StockExchange {

    public static StockExchange getInstance() {
        return new TSX();
    }

    public String getExchange() {
        return "TSX";
    }

    public List<StockDao> parseFeed(File file) {
        List<StockDao> stocks = new LinkedList<>();
        Scanner scan = null;
        try {
            scan = new Scanner(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        scan.nextLine();

        while (scan.hasNext()) {
            String symbol = scan.nextLine().split("\t")[0] + ".to";
            stocks.add(new StockDao(symbol, getExchange(), DateTime.now()));
        }
        return stocks;
    }

}
