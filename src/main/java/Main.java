import dao.StockDao;
import db.SqliteDriver;
import external.GlobalUtil;
import external.TSX;
import grabber.DailyPriceGrabber;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class Main {

    public static void main(String[] args) throws IOException, URISyntaxException, SQLException, InterruptedException {
        File file = new File(GlobalUtil.TSX_FEED);
        List<StockDao> stocks = TSX.parseFeed(file);
        List<String> stockSymbols = stocks.stream().map(s -> s.getSymbol()).collect(Collectors.toList());

        DailyPriceGrabber.populateStockPrices(stockSymbols);
        SqliteDriver.insertStockSymbols(stockSymbols, TSX.getExchange());

        System.out.println("Hello World!");
    }


}



