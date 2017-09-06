package grabber;

public class YahooResult {
    String symbol;
    String name;
    double spread;
    double dividendShare;
    double dividendYield;
    double earningsShare;
    double epseEstimateCurrentYear;
    double epseEseEstimateNextYear;
    double marketCap;
    double ebitada;
    double peRatio;
    double yearTargetPrice;

    public YahooResult(String symbol, String name, double spread, double dividendShare, double dividendYield, double earningsShare,
                       double epseEstimateCurrentYear, double epseEseEstimateNextYear, double marketCap, double ebitada,
                       double peRatio, double yearTargetPrice) {
        this.symbol = symbol;
        this.name = name;
        this.spread = spread;
        this.dividendShare = dividendShare;
        this.dividendYield = dividendYield;
        this.earningsShare = earningsShare;
        this.epseEstimateCurrentYear = epseEstimateCurrentYear;
        this.epseEseEstimateNextYear = epseEseEstimateNextYear;
        this.marketCap = marketCap;
        this.ebitada = ebitada;
        this.peRatio = peRatio;
        this.yearTargetPrice = yearTargetPrice;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getName() {
        return name;
    }

    public double getSpread() {
        if (spread < 0) {
            return 0;
        }
        return spread;
    }

    public double getDividendShare() {
        return dividendShare;
    }

    public double getDividendYield() {
        return dividendYield;
    }

    public double getEarningsShare() {
        return earningsShare;
    }

    public double getEpseEstimateCurrentYear() {
        return epseEstimateCurrentYear;
    }

    public double getEpseEseEstimateNextYear() {
        return epseEseEstimateNextYear;
    }

    public double getMarketCap() {
        return marketCap;
    }

    public double getEbitada() {
        return ebitada;
    }

    public double getPeRatio() {
        return peRatio;
    }

    public double getYearTargetPrice() {
        return yearTargetPrice;
    }

}
