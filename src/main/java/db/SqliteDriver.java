package db;

import algo.MovingAverage;
import dao.IndicatorDao;
import dao.MovingAverageDao;
import dao.StockDao;
import dao.StockPriceDao;
import grabber.YahooFinanceBuilder;
import grabber.YahooResult;
import org.joda.time.DateTime;

import java.sql.*;
import java.util.List;
import java.util.Map;


/**
 * Created by Ailan on 8/23/2017.
 */
public class SqliteDriver {
    static Connection connection = null;
    static Statement statement = null;

    static {
        try {
            ClassLoader classloader = Thread.currentThread().getContextClassLoader();
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + classloader.getResource("").getFile() + "stock.sqlite");
            statement = connection.createStatement();
            statement.setQueryTimeout(30);  // set timeout to 30 sec.
            createTables();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String args[]) throws SQLException {
//        List<StockPriceDao> prices = getAllStockPrices("ANX.to");
//        List<StockPriceDao> prices = getAllStockPrices();
//        statement.execute("CREATE INDEX test_index ON stockprice (symbol, date);");
        statement.execute(MovingAverageDao.getTableBuilder().generateQuery());
    }

    static void createTables() {
        try {
            statement.execute(StockDao.getTableBuilder().generateQuery());
            statement.execute(StockPriceDao.getTableBuilder().generateQuery());
            statement.execute(IndicatorDao.getTableBuilder().generateQuery());
            statement.execute(MovingAverageDao.getTableBuilder().generateQuery());
        } catch (SQLException e) {
//            e.printStackTrace();
            System.out.println("Already exists.. moving on");
        }
    }

    public static synchronized void insertStockSymbols(List<String> symbols, String exchange) {
        DateTime now = DateTime.now();
        InsertionBuilder builder = InsertionBuilder.aBuilder()
                .withTableBuilder(StockDao.getTableBuilder());

        Map<String, YahooResult> results = YahooFinanceBuilder.getInstance().withSymbols(symbols).withBatch(100).execute();

        symbols.stream().forEach(s -> {
            StockDao sd = new StockDao(s, exchange, now);
            sd.setResult(results.get(s));
            builder.withParams(sd.getParams());
        });
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
