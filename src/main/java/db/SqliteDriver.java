package db;

import dao.StockPriceDao;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Ailan on 8/23/2017.
 */
public class SqliteDriver {
    static Connection connection = null;
    static Statement statement = null;
    static String stockPriceInsertQuery = "insert into stockprice values";
    static DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");


    static String stockPriceTableQuery = "CREATE TABLE `StockPrice` (" +
            "'SYMBOL' TEXT," +
            "'HIGH' NUMERIC," +
            "'LOW' NUMERIC," +
            "'OPEN' NUMERIC," +
            "'CLOSE' NUMERIC," +
            "'VOLUME' NUMERIC," +
            "'DATE' TEXT," +
            "PRIMARY KEY(SYMBOL,DATE)" +
            ");";

    static String stockTableQuery = "CREATE TABLE `Stock` (" +
            "'SYMBOL' TEXT," +
            "'UPDATED' TEXT," +
            "PRIMARY KEY(`SYMBOL`)" +
            ");";

    static {
        try {
            Class.forName("org.sqlite.JDBC");
//            connection = DriverManager.getConnection("jdbc:sqlite:D:\\data\\stock.sqlite");
            connection = DriverManager.getConnection("jdbc:sqlite:/var/tmp/stock.sqlite");
            statement = connection.createStatement();
            statement.setQueryTimeout(30);  // set timeout to 30 sec.
            createTables();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String args[]){
        List<StockPriceDao> prices = getAllStockPrices("ANX.to");
        System.out.println("lol");
    }

    static void createTables() {
        try {
            statement.execute(stockTableQuery);
            statement.execute(stockPriceTableQuery);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static synchronized void insertStockSymbols(List<String> symbols) {
        String query = "insert into stock (SYMBOL, UPDATED) values ";
        for (String s : symbols) {
            query += String.format("('%s',date('%s')),", s, LocalDate.now());
        }
        executeInsert(query);

        System.out.println("");
    }


    public static synchronized void insertStockPrice(List<StockPriceDao> stockPrices) {
        String query = stockPriceInsertQuery;
        String stockValues = "('%s', %s, %s, %s, %s, %s, date('%s')),";

        for (StockPriceDao stockPrice : stockPrices) {
            query += String.format(stockValues, stockPrice.getSymbol(), stockPrice.getHigh(), stockPrice.getLow(), stockPrice.getOpen(),
                    stockPrice.getClose(), stockPrice.getVolume(), stockPrice.getDate().toString());
        }
        executeInsert(query);
    }

    private static void executeInsert(String query) {
        try {
            statement.executeUpdate(query.substring(0, query.length() - 1));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<StockPriceDao> getAllStockPrices(String symbol){
        String query = String.format("select * from stockprice where symbol = '%s'", symbol);
        List<StockPriceDao> stockPrices = new LinkedList<>();

        try {
            ResultSet rs = statement.executeQuery(query);
            while(rs.next()){
                StockPriceDao sp = new StockPriceDao(rs.getString("SYMBOL"), LocalDate.parse(rs.getString("DATE"), dateFormat),
                        rs.getDouble("OPEN"), rs.getDouble("HIGH"), rs.getDouble("LOW"),
                        rs.getDouble("CLOSE"), rs.getLong("VOLUME"));
                stockPrices.add(sp);
            }
            return stockPrices;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

}
