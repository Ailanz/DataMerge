package external;

import dao.StockDao;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.File;
import java.util.List;

public interface StockExchange {
    public String getExchange();

    public List<StockDao> parseFeed(File file);
}

