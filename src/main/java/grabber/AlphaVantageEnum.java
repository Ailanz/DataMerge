package grabber;

public class AlphaVantageEnum {
    public enum Function {
        TIME_SERIES_DAILY,
        TIME_SERIES_DAILY_ADJUSTED,
        TIME_SERIES_INTRADAY,
        ADX,
        MACD,
        RSI,
        CCI,
        AROON
    }

    public enum OutputSize {
        FULL,
        COMPACT
    }

    public enum Interval {
        DAILY,
        ONE,
        FIVE,
        FIFTEEN,
    }

    public enum SeriesType {
        CLOSE,
        OPEN,
        HIGH,
        LOW
    }
}
