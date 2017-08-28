package grabber;

public class AlphaVantageEnum {
    public enum Function {
        TIME_SERIES_DAILY,
        TIME_SERIES_DAILY_ADJUSTED,
        ADX
    }

    public enum OutputSize {
        FULL,
        COMPACT
    }

    public enum Interval {
        DAILY
    }
}
