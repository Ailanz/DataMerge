package exchange;

import dao.StockDao;

import java.io.File;
import java.io.FileNotFoundException;
import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

/**
 * Created by Ailan on 8/29/2017.
 */
public class NASDAQ implements StockExchange {

    public static void main(String args[]){
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        File file = new File(classloader.getResource("nasdaqlisted.txt").getFile());
        List<StockDao> stocks = getInstance().parseFeed(file);
        for(StockDao s : stocks) {
            System.out.println(s.getSymbol());
        }
    }

    public String getExchange(){
        return "NASDAQ";
    }

    public static StockExchange getInstance(){
        return new NASDAQ();
    }

    public List<StockDao> parseFeed(File file){
        List<StockDao> stocks = new LinkedList<>();
        Scanner scan = null;
        try {
            scan = new Scanner(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        scan.nextLine();

        while (scan.hasNext()) {
            String symbol = scan.nextLine().split("[|]")[0];
            if(scan.hasNext()) {
                stocks.add(new StockDao(symbol, getExchange(), LocalDate.now()));
            }
        }
        return stocks;
    }
}
