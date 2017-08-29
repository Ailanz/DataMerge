package db;

import dao.StockDao;
import dao.StockPriceDao;
import external.GlobalUtil;
import external.StockExchange;

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

    static {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(GlobalUtil.jdbcCon);
            statement = connection.createStatement();
            statement.setQueryTimeout(30);  // set timeout to 30 sec.
            createTables();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String args[]) {
//        List<StockPriceDao> prices = getAllStockPrices("ANX.to");
//        List<StockPriceDao> prices = getAllStockPrices();
        System.out.println("lol");
    }

    static void createTables() {
        try {
            statement.execute(StockDao.getTableBuilder().generateQuery());
            statement.execute(StockPriceDao.getTableBuilder().generateQuery());
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Already exists.. moving on");
        }
    }

    public static synchronized void insertStockSymbols(List<String> symbols, String exchange) {
        LocalDate now = LocalDate.now();
        InsertionBuilder builder = InsertionBuilder.aBuilder()
                .withTableBuilder(StockDao.getTableBuilder());
        symbols.stream().forEach(s -> builder.withParams(new StockDao(s, exchange, now).getParams()));
        executeInsert(builder.execute());
    }


    public static ResultSet executeQuery(String query) {
        try {
            return statement.executeQuery(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void executeInsert(String query) {
        try {
            statement.executeUpdate(query);
        } catch (SQLException e) {
            System.err.println(query);
            e.printStackTrace();
        }
    }

}
