package external;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class KeyDateFilter {
    Map<String, LocalDate> data = new HashMap<>();

    public void add(String symbol, LocalDate date){
        LocalDate curDate = data.get(symbol);
        if(curDate==null || date.isAfter(curDate)) {
            data.put(symbol, date);
        }
    }


    public boolean isAfterOrEmpty(String symbol, LocalDate date){
        LocalDate curDate = data.get(symbol);
        if(curDate==null || date.isAfter(curDate)) {
            return true;
        }
        return false;
    }
}
