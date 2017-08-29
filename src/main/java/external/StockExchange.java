package external;

import dao.StockDao;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.File;
import java.util.List;

public class StockExchange {
    static String getExchange(){
        throw new NotImplementedException();
    }

    static List<StockDao> parseFeed(File file){
        throw new NotImplementedException();
    }
}

