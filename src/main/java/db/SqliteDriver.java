package db;

import dao.StockPriceDao;
import external.StockExchange;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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

//
//    static String stockPriceTableQuery = "CREATE TABLE `StockPrice` (" +
//            "'SYMBOL' TEXT," +
//            "'HIGH' NUMERIC," +
//            "'LOW' NUMERIC," +
//            "'OPEN' NUMERIC," +
//            "'CLOSE' NUMERIC," +
//            "'VOLUME' NUMERIC," +
//            "'DATE' TEXT," +
//            "PRIMARY KEY(SYMBOL,DATE)" +
//            ");";
//
//    static String stockTableQuery = "CREATE TABLE `Stock` (" +
//            "'SYMBOL' TEXT," +
//            "'UPDATED' TEXT," +
//            " PRIMARY KEY(SYMBOL)" +
//            ");";

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

    public static void main(String args[]) {
//        List<StockPriceDao> prices = getAllStockPrices("ANX.to");
//        List<StockPriceDao> prices = getAllStockPrices();
        System.out.println("lol");
    }

    static void createTables() {
        try {
            TableBuilder stockPriceTableBuilder = TableBuilder.aBuilder().withTableName("StockPrice")
                    .withColumn("SYMBOL", TableBuilder.FIELD_TYPE.TEXT)
                    .withColumn("HIGH", TableBuilder.FIELD_TYPE.NUMERIC)
                    .withColumn("LOW", TableBuilder.FIELD_TYPE.NUMERIC)
                    .withColumn("OPEN", TableBuilder.FIELD_TYPE.NUMERIC)
                    .withColumn("CLOSE", TableBuilder.FIELD_TYPE.NUMERIC)
                    .withColumn("VOLUME", TableBuilder.FIELD_TYPE.NUMERIC)
                    .withColumn("DATE", TableBuilder.FIELD_TYPE.TEXT)
                    .withprimaryKeys("SYMBOL", "DATE");

            TableBuilder stockTableBuilder = TableBuilder.aBuilder().withTableName("Stock")
                    .withColumn("SYMBOL", TableBuilder.FIELD_TYPE.TEXT)
                    .withColumn("EXCHANGE", TableBuilder.FIELD_TYPE.TEXT)
                    .withColumn("UPDATED", TableBuilder.FIELD_TYPE.TEXT)
                    .withprimaryKeys("SYMBOL");

            statement.execute(stockTableBuilder.generateQuery());
            statement.execute(stockPriceTableBuilder.generateQuery());
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Already exists.. moving on");
        }
    }

    public static synchronized void insertStockSymbols(List<String> symbols, StockExchange exchange) {
        String query = "insert into stock (SYMBOL, EXCHANGE, UPDATED) values ";
        for (String s : symbols) {
            query += String.format("('%s','%s', date('%s')),", s, exchange.name(), LocalDate.now());
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

    public static List<StockPriceDao> getAllStockPrices(String symbol) {
        String query = String.format("select * from stockprice where symbol = '%s'", symbol);
        List<StockPriceDao> stockPrices = new LinkedList<>();

        try {
            ResultSet rs = statement.executeQuery(query);
            return parseStockPrice(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<StockPriceDao> getAllStockPrices() {
        String query = "select * from stockprice";
        List<StockPriceDao> stockPrices = new LinkedList<>();

        try {
            ResultSet rs = statement.executeQuery(query);
            return parseStockPrice(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static List<StockPriceDao> parseStockPrice(ResultSet rs) {
        List<StockPriceDao> stockPrices = new LinkedList<>();

        try {
            while (rs.next()) {
                StockPriceDao sp = new StockPriceDao(rs.getString("SYMBOL"), LocalDate.parse(rs.getString("DATE"), dateFormat),
                        rs.getDouble("OPEN"), rs.getDouble("HIGH"), rs.getDouble("LOW"),
                        rs.getDouble("CLOSE"), rs.getLong("VOLUME"));
                stockPrices.add(sp);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stockPrices;
    }
}
