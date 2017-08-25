package db;

import dao.StockPriceDao;

import java.sql.*;
import java.util.List;

/**
 * Created by Ailan on 8/23/2017.
 */
public class SqliteDriver {
    static Connection connection = null;
    static Statement statement = null;
    static String stockPriceInsertQuery = "insert into stockprice values";

    static String stockPriceTableQuery = "CREATE TABLE `StockPrice` (\n" +
            "\t`SYMBOL`\tTEXT,\n" +
            "\t`HIGH`\tNUMERIC,\n" +
            "\t`LOW`\tNUMERIC,\n" +
            "\t`OPEN`\tNUMERIC,\n" +
            "\t`CLOSE`\tNUMERIC,\n" +
            "\t`VOLUME`\tNUMERIC,\n" +
            "\t`DATE`\tTEXT,\n" +
            "\tPRIMARY KEY(SYMBOL,DATE)\n" +
            ");";

    static String stockTableQuery = "CREATE TABLE `Stock` (\n" +
            "\t`ID`\tINTEGER PRIMARY KEY AUTOINCREMENT,\n" +
            "\t`SYMBOL`\tTEXT\n" +
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

    static void createTables() {
        try {
            statement.execute(stockTableQuery);
            statement.execute(stockPriceTableQuery);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static synchronized void insertStockSymbols(List<String> symbols) {
        String query = "insert into stock (SYMBOL) values ";
        for (String s : symbols) {
            query += String.format("('%s'),", s);
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
            System.out.println("");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
