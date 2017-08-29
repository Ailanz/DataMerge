package external;

import dao.StockDao;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.File;
import java.io.FileNotFoundException;
import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class TSX extends StockExchange{
    public static String getExchange(){
        return "TSX";
    }

    public static List<StockDao> parseFeed(File file){
        List<StockDao> stocks = new LinkedList<>();
        Scanner scan = null;
        try {
            scan = new Scanner(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        scan.nextLine();

        List<String> symbols = new LinkedList<>();
        while (scan.hasNext()) {
            String symbol = scan.nextLine().split("\t")[0] + ".to";
            stocks.add(new StockDao(symbol, TSX.getExchange(), LocalDate.now()));
        }
        return stocks;
    }

}
