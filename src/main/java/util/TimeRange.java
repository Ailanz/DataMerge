package util;

import java.time.LocalDate;

/**
 * Created by Ailan on 9/4/2017.
 */
public class TimeRange {
    LocalDate min;
    LocalDate max;

    public TimeRange(LocalDate min, LocalDate max){
        this.min = min;
        this.max = max;
    }

    public TimeRange(LocalDate min) {
        this.min = min;
        this.max = LocalDate.MAX;
    }

    public boolean isWithin(LocalDate date){
        return date.isAfter(min) && date.isBefore(max);
    }
}
