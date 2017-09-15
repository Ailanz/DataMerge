package exchange;

import dao.StockDao;
import org.joda.time.DateTime;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

/**
 * Created by Ailan on 9/13/2017.
 */
public class SP500 implements StockExchange {
    @Override
    public String getExchange() {
        return "S&P500";
    }

    public static StockExchange getInstance() {
        return new SP500();
    }


    @Override
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
            String symbol = scan.nextLine();
            stocks.add(new StockDao(symbol, getExchange(), DateTime.now()));
        }
        return stocks;
    }
}
