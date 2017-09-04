package util;

import java.time.LocalDate;
import java.time.Year;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalUnit;
import java.util.HashMap;
import java.util.Map;

public class KeyDateFilter {
    Map<String, LocalDate> data = new HashMap<>();
    LocalDate beginningOfTime = LocalDate.now().minusYears(3);

    public void add(String symbol, LocalDate date){
        LocalDate curDate = data.get(symbol);
        if(curDate==null || date.isAfter(curDate)) {
            data.put(symbol, date);
        }
    }

    public boolean isAfterOrEmptyAndInsert(String symbol, LocalDate date){
        boolean ret = isAfterOrEmpty(symbol, date);
        add(symbol, date);
        return ret;
    }


    public boolean isAfterOrEmpty(String symbol, LocalDate date){
        LocalDate curDate = data.get(symbol);
        if(date.isAfter(beginningOfTime) && (curDate==null || date.isAfter(curDate))) {
            return true;
        }
        return false;
    }
}
