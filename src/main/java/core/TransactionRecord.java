package core;

import org.joda.time.DateTime;

/**
 * Created by Ailan on 9/4/2017.
 */
public class TransactionRecord {
    private Type type;

    private String symbol;

    private DateTime date;

    private double price;

    private int numOfShare;

    private TransactionRecord(Type type, String symbol, DateTime date, int numOfShare, double price) {
        this.symbol = symbol;
        this.type = type;
        this.date = date;
        this.numOfShare = numOfShare;
        this.price = price;
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

    public Type getType() {
        return type;
    }

    public DateTime getDate() {
        return date;
    }

    public int getNumOfShare() {
        return numOfShare;
    }

    public double getPrice() {
        return price;
    }
    public enum Type {
        BUY,
        SELL,
        EXIT;
    }
}