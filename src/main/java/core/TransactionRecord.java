package core;

import lombok.Data;
import org.joda.time.DateTime;
import util.PriceUnit;

/**
 * Created by Ailan on 9/4/2017.
 */

@Data
public class TransactionRecord {
    private Type type;
    private String symbol;
    private DateTime date;
    private double price;
    private int numOfShare;
    private double maxPrice;

    private TransactionRecord(Type type, String symbol, DateTime date, int numOfShare, double price) {
        this.symbol = symbol;
        this.type = type;
        this.date = date;
        this.numOfShare = numOfShare;
        this.price = PriceUnit.round2Decimal(price);
        this.maxPrice = -1;
    }

    public static TransactionRecord buy(DateTime date, String symbol, int numOfShare, double price) {
        return new TransactionRecord(Type.BUY, symbol, date, numOfShare, price);
    }

    public static TransactionRecord sell(DateTime date, String symbol, int numOfShare, double price) {
        return new TransactionRecord(Type.SELL, symbol, date, numOfShare, price);
    }

    public String getSymbol() {
        return symbol;
    }

    public static TransactionRecord exit(DateTime date, String symbol, int numOfShare, double price) {
        return new TransactionRecord(Type.EXIT, symbol, date, numOfShare, price);
    }

    public void processPotentialEarning(double d){
        if(d > maxPrice && this.type==Type.BUY) {
            maxPrice = PriceUnit.round2Decimal(d);
        }
    }

    public double getPrice() {
        return PriceUnit.round2Decimal(price);
    }
    public enum Type {
        BUY,
        SELL,
        EXIT;
    }
}