package core;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.joda.time.DateTime;

import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Ailan on 9/24/2017.
 */
public class Notification {
    //Only contain data to be actioned next day
    private static List<Message> notifications = new LinkedList<>();
    private Notification() {}

    public static void watch(String symbol, double price, String message, DateTime date){
        if(isEligibleDate(date)) {
            notifications.add(new Message(Message.Type.WATCH, symbol, price, message, date));
        }
    }

    public static void buy(String symbol, double price, String message, DateTime date){
        if(isEligibleDate(date)) {
            notifications.add(new Message(Message.Type.BUY, symbol, price, message, date));
        }
    }

    public static void sell(String symbol, double price, String message, DateTime date){
        if(isEligibleDate(date)) {
            notifications.add(new Message(Message.Type.SELL, symbol, price, message, date));
        }
    }

    public static void summary(){
        System.out.println("=======NOTIFICATIONS=======");
        notifications.forEach(n->
            System.out.println(n.getMessageType().name() + ": " + n.getSymbol() + " Price: " + n.getPrice()
                    + " Message: " + n.getMessage() + " | " + n.getDate())
        );
    }

    public static void main(String args[]){
        isEligibleDate(new DateTime());
    }

    private static boolean isEligibleDate(DateTime date){
        Calendar currentDate = Calendar.getInstance();

        int dayOfWeek = currentDate.get(Calendar.DAY_OF_WEEK);
        if(dayOfWeek==1) {
            //Sunday
            currentDate.add(Calendar.DATE, -2);
        }

        if(dayOfWeek==7) {
            //Saturday
            currentDate.add(Calendar.DATE, -1);
        }
        currentDate.add(Calendar.DATE,-1);
        currentDate.set(Calendar.HOUR_OF_DAY,23);
        return date.toDate().after(currentDate.getTime());
    }
}

@Data
@AllArgsConstructor
class Message {
    enum Type {
        WATCH,
        BUY,
        SELL
    }

    private Type messageType;
    private String symbol;
    private double price;
    private String message;
    private DateTime date;
}
