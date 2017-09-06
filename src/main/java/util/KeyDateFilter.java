package util;

import org.joda.time.DateTime;

import java.util.HashMap;
import java.util.Map;

public class KeyDateFilter {
    Map<String, DateTime> data = new HashMap<>();
    DateTime beginningOfTime = DateTime.now().minusYears(3);

    public void add(String symbol, DateTime date) {
        DateTime curDate = data.get(symbol);
        if (curDate == null || date.isAfter(curDate)) {
            data.put(symbol, date);
        }
    }

    public boolean isAfterOrEmptyAndInsert(String symbol, DateTime date) {
        boolean ret = isAfterOrEmpty(symbol, date);
        add(symbol, date);
        return ret;
    }


    public boolean isAfterOrEmpty(String symbol, DateTime date) {
        DateTime curDate = data.get(symbol);
        if (date.isAfter(beginningOfTime) && (curDate == null || date.isAfter(curDate))) {
            return true;
        }
        return false;
    }
}
