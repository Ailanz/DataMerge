package exchange;

import dao.StockDao;

import java.io.File;
import java.util.List;

public interface StockExchange {
    public String getExchange();

    public List<StockDao> parseFeed(File file);
}

