package grabber;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dao.StockPriceDao;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class DailyPriceGrabber {

    public static List<StockPriceDao> getStockPrices(String stockSymbol) {
        return AlphaVantageApi.getStockPrices(stockSymbol);
    }
}
