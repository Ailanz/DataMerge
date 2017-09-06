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

    private TransactionRecord(Type type, String symbol, DateTime date, double price) {
        this.symbol = symbol;
        this.type = type;
        this.date = date;
        this.price = price;
    }

    public static TransactionRecord buy(DateTime date, String symbol, double price) {
        return new TransactionRecord(Type.BUY, symbol, date, price);
    }

    public static TransactionRecord sell(DateTime date, String symbol, double price) {
        return new TransactionRecord(Type.SELL, symbol, date, price);
    }

    public String getSymbol() {
        return symbol;
    }

    public static TransactionRecord exit(DateTime date, String symbol, double price) {
        return new TransactionRecord(Type.EXIT, symbol, date, price);
    }

    public Type getType() {
        return type;
    }

    public DateTime getDate() {
        return date;
    }

    public double getPrice() {
        return price;
    }

    public enum Type {
        BUY,
        SELL,
        EXIT
    }
}