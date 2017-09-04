package algo;

import dao.StockPriceDao;
import org.jfree.data.xy.DefaultOHLCDataset;
import org.jfree.data.xy.OHLCDataItem;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Ailan on 9/4/2017.
 */
public class MovingAverage {

    private int size;
    private double total = 0d;
    private int index = 0;
    private double samples[];

    public static void main(String args[]){
        MovingAverage mv = new MovingAverage(4);
        MovingAverage mv2 = new MovingAverage(7);
        double[] a = new double[] {1,2,3,4,5,6,7,8,9,10,11,12};

        for(int i=0; i < 100; i++){
            mv.add(i);
            mv2.add(i);
            System.out.println("Short: " + mv.getAverage() + ", Long: " + mv2.getAverage());
//            System.out.println(mv2.getAverage());
        }
    }

    public MovingAverage(int size) {
        this.size = size;
        samples = new double[size];
        for (int i = 0; i < size; i++) samples[i] = 0d;
    }

    public void add(double x) {
        total -= samples[index];
        samples[index] = x;
        total += x;
        if (++index == size) index = 0; // cheaper than modulus
    }

    public double getAverage() {
        return total / size;
    }

    public static double[] getSimpleAverage(double[] prices){
        MovingAverage ma = new MovingAverage(prices.length);
        double[] ret = new double[prices.length];
        for(int i=0; i< prices.length; i++){
            ma.add(prices[i]);
            ret[i] = ma.getAverage();
        }
        return ret;
    }
}
