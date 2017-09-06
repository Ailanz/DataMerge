package util;

import org.joda.time.DateTime;

/**
 * Created by Ailan on 9/4/2017.
 */
public class TimeRange {
    DateTime min;
    DateTime max;

    public TimeRange(DateTime min, DateTime max) {
        this.min = min;
        this.max = max;
    }

    public TimeRange(DateTime min) {
        this.min = min;
        this.max = DateTime.now().plusYears(999);
    }

    public boolean isWithin(DateTime date) {
        return date.isAfter(min) && date.isBefore(max);
    }
}
