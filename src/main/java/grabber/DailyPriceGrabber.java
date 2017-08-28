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

import static grabber.AlphaVantageApi.getResult;

public class DailyPriceGrabber {

    public static List<StockPriceDao> getStockPrices(String stockSymbol) {
        List<ResultData> data = getResult(AlphaVantageApi.TIME_SERIES_DAILY, stockSymbol);

        if(data == null){
            return null;
        }

        List<StockPriceDao> ret = new LinkedList<>();
        for(ResultData r : data) {
            ret.add(new StockPriceDao(stockSymbol, r.getDate(), r.getData().get("1. open").asDouble(), r.getData().get("2. high").asDouble(),
                    r.getData().get("3. low").asDouble(), r.getData().get("4. close").asDouble(), r.getData().get("5. volume").asLong()));
        }

        return ret;
    }
}
