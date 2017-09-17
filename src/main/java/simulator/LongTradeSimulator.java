package simulator;

import core.Book;
import core.TransactionRecord;
import core.strategy.CandleStickStrategyBuilder;
import core.strategy.ExitIntervalEnum;
import dao.StockDao;
import org.joda.time.DateTime;
import ui.StockFilter;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Ailan on 9/15/2017.
 */
public class LongTradeSimulator {

    public static void main(String args[]) throws InterruptedException {

        executeStrategy();
    }

    public static List<TransactionRecord> executeStrategy() {
        List<StockDao> allStocks = StockFilter.marketCapFilter(StockDao.getAllStocks());
//                .stream().filter(s->s.getSymbol().equals("CNX")).collect(Collectors.toList());
        List<TransactionRecord> allTransactions = new LinkedList<>();
        Book book = new Book();
        for (StockDao stock : allStocks) {

            List<TransactionRecord> transactions = CandleStickStrategyBuilder.aBuilder()
                    .withExitInterval(ExitIntervalEnum.NEVER)
                    .withBuyAfterDate(DateTime.now().minusDays(60))
//                    .withSellLimit(1.03)
//                    .withValueToFulfill(100)
                    .execute(stock);

            book.addTransaction(transactions);
            allTransactions.addAll(transactions);
        }

        book.printSummaryTemporal();
        return allTransactions;
    }
}
