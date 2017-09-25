package util;

/**
 * Created by Ailan on 9/2/2017.
 */
public class PriceUnit {
    public static double MILLION = 1000000;
    public static double BILLION = 1000000000;

    public static double toDouble(double num, double unit) {
        return num * unit;
    }

    public static double parseNumber(String number) {
        try {
            String lower = number.toLowerCase();
            if (lower.contains("m")) {
                return Double.valueOf(lower.replace("m", "")) * MILLION;
            }
            if (lower.contains("b")) {
                return Double.valueOf(lower.replace("b", "")) * BILLION;
            }
            return Double.valueOf(lower);
        } catch (Exception e) {
//            System.err.print("cannot parse: " + number);
            return -1;
        }
    }

    public static double round2Decimal(double d){
        return Math.round(d*100.0)/100.0;
    }

}
