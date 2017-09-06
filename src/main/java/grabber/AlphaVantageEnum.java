package grabber;

public class AlphaVantageEnum {
    public enum Function {
        TIME_SERIES_DAILY,
        TIME_SERIES_DAILY_ADJUSTED,
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
        DAILY
    }

    public enum SeriesType {
        CLOSE,
        OPEN,
        HIGH,
        LOW
    }
}
