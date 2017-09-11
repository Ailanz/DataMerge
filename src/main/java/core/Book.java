package core;

import dao.StockDao;

import java.util.*;

/**
 * Created by Ailan on 9/6/2017.
 */
public class Book {
    Map<String, List<TransactionRecord>> masterRecord = new HashMap<>();

    public void addTransaction(TransactionRecord record) {
        masterRecord.computeIfAbsent(record.getSymbol(), s -> new LinkedList<>());
        masterRecord.get(record.getSymbol()).add(record);
    }

    public synchronized void addTransaction(List<TransactionRecord> records) {
        records.forEach(r -> addTransaction(r));
    }

    public double totalPNL(double latestPrice) {
        double totalRealized = 0;
        double totalUnrealized = 0;
        for (Map.Entry<String, List<TransactionRecord>> s : masterRecord.entrySet()) {
            List<TransactionRecord> records = s.getValue();
            Stack<TransactionRecord> stack = new Stack<>();
            double realized = 0;
            double unrealized = 0;
            for (TransactionRecord record : records) {
                if (record.getType() == TransactionRecord.Type.BUY) {
                    stack.push(record);
                }

                if (record.getType() == TransactionRecord.Type.SELL ) {
                    realized += record.getNumOfShare() * (record.getPrice() - stack.pop().getPrice());
                }

                if (record.getType() == TransactionRecord.Type.EXIT){
                    realized += record.getNumOfShare() * (record.getPrice() - stack.pop().getPrice());
                }
            }
            while (!stack.isEmpty()) {
                TransactionRecord r = stack.pop();
                unrealized += r.getNumOfShare() * (latestPrice - r.getPrice());
            }
            totalRealized += realized;
            totalUnrealized += unrealized;
        }
        //STOCK GET LATEST PRICE IS WRONG, NEED REAL TIME API TO GET IT RIGHT!
      return totalRealized + totalUnrealized;
    }

    public void printSummary() {
        double totalRealized = 0;
        double totalUnrealized = 0;
        double totalBuys = 0;
        double totalSells = 0;
        double totalExits = 0;
        double totalPurchase = 0;
        for (Map.Entry<String, List<TransactionRecord>> s : masterRecord.entrySet()) {
            double leftOverCash = 0;
            List<TransactionRecord> records = s.getValue();
            Stack<TransactionRecord> stack = new Stack<>();
            double realized = 0;
            double unrealized = 0;
            for (TransactionRecord record : records) {
                if (record.getType() == TransactionRecord.Type.BUY) {
                    double price = record.getNumOfShare() * record.getPrice();
                    if(leftOverCash > price) {
                        leftOverCash -= price;
                    }else {
                        totalPurchase += price;
                    }
                    stack.push(record);
                    totalBuys++;
                }

                if (record.getType() == TransactionRecord.Type.SELL ) {
                    double price = record.getNumOfShare() * (record.getPrice() - stack.pop().getPrice());
                    realized += price;
                    leftOverCash += price;
                    totalSells++;
                }

                if (record.getType() == TransactionRecord.Type.EXIT){
                    double price = record.getNumOfShare() * (record.getPrice() - stack.pop().getPrice());
                    realized += price;
                    leftOverCash += price;
                    totalExits++;
                }
            }
            while (!stack.isEmpty()) {
                TransactionRecord r = stack.pop();
                unrealized += r.getNumOfShare() * (StockDao.getStock(r.getSymbol()).getLatestPrice().getClose() - r.getPrice());
                totalExits++;
            }
            totalRealized += realized;
            totalUnrealized += unrealized;
            System.out.println(String.format("Symbol: %s Realized: %s, Unrealized: %s", s.getKey(), String.valueOf(realized), String.valueOf(unrealized)));
        }
        System.out.println("--------------------------------------------------");
        System.out.println("Total Spent: " + totalPurchase);
        System.out.println(String.format("Total Buys: %s, Sells: %s, Exits: %s", String.valueOf(totalBuys),String.valueOf(totalSells),String.valueOf(totalExits)));
        System.out.println(String.format("Total Realized: %s, Unrealized: %s", String.valueOf(totalRealized), String.valueOf(totalUnrealized)));
        System.out.println(String.format("Return: %s",String.valueOf(1 + (totalRealized + totalUnrealized)/totalPurchase)));

    }

    private double calculatePLPercentage(List<TransactionRecord> records) {
        double sum = 0;
        Stack<TransactionRecord> stack = new Stack<>();
        int sells = 0;
        for (TransactionRecord record : records) {
            if (record.getType() == TransactionRecord.Type.BUY) {
                stack.push(record);
//                sum -= record.getPrice();
            }

            if (record.getType() == TransactionRecord.Type.SELL || record.getType() == TransactionRecord.Type.EXIT) {
                sum += record.getPrice() / stack.pop().getPrice();
                sells++;
            }
        }

        while (!stack.isEmpty()) {
            sum += StockDao.getStock(records.get(0).getSymbol()).getLatestPrice().getClose() / stack.pop().getPrice();
            sells++;
        }
        return sum / sells;
    }

}
