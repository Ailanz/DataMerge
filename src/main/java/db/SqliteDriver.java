package db;

import dao.StockPrice;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * Created by Ailan on 8/23/2017.
 */
public class SqliteDriver {
    static Connection connection = null;
    static Statement statement = null;
    static String stockPriceInsertQuery = "insert into stockprice values";
    static String stockValues = "('%s', %s, %s, %s, %s, %s, date('%s')),";
    static {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:D:\\data\\stock");
            statement = connection.createStatement();
            statement.setQueryTimeout(30);  // set timeout to 30 sec.
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static synchronized void insertStockPrice(List<StockPrice> stockPrices){
        String query = stockPriceInsertQuery;
        for(StockPrice stockPrice : stockPrices) {
                query += String.format(stockValues, stockPrice.getSymbol(), stockPrice.getHigh(), stockPrice.getLow(), stockPrice.getOpen(),
                        stockPrice.getClose(), stockPrice.getVolume(), stockPrice.getDate().toString());
        }
        try {
            statement.executeUpdate(query.substring(0, query.length()-1));
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }


}
