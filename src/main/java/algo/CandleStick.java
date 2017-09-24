package algo;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.joda.time.DateTime;

/**
 * Created by Ailan on 9/12/2017.
 */
@Data
@AllArgsConstructor
public class CandleStick {
    private double open;
    private double close;
    private double high;
    private double low;
    private DateTime date;
    private double volume;

    public boolean isWhite(){
        return close > open;
    }

    public double getBodyLength(){
        return  Math.abs(open - close);
    }

    public boolean isBlack(){
        return open > close;
    }

    public boolean isDoji(){
        return getBodyLength() < Math.min(open * 0.02, 0.05);
    }
}
