package util;

import java.time.format.DateTimeFormatter;

public class GlobalUtil {
    public static DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static String jdbcCon = "jdbc:sqlite:/var/tmp/stock.sqlite";
//    public static String jdbcCon = "jdbc:sqlite:D:\\data\\stock.sqlite";

    public static String TSX_FEED = "/var/tmp/TSX.txt";
//    public static String TSX_FEED = "D:\\data\\TSX.txt";

    public static double getMemoryConsumption(){
        long totalmem = java.lang.Runtime.getRuntime().totalMemory();
        long freemem  = java.lang.Runtime.getRuntime().freeMemory();
        return Math.round((totalmem - freemem) / 1000000.0);
    }
}
