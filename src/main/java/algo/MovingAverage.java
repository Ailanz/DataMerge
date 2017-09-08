package algo;

/**
 * Created by Ailan on 9/4/2017.
 */
public class MovingAverage {

    protected int interval;

    protected double total = 0d;
    private int index = 0;
    private double samples[];
    public static void main(String args[]) {
        MovingAverage mv = new MovingAverage(4);
        MovingAverage mv2 = new MovingAverage(7);
        double[] a = new double[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};

        for (int i = 0; i < 100; i++) {
            mv.add(i);
            mv2.add(i);
            System.out.println("Short: " + mv.getAverage() + ", Long: " + mv2.getAverage());
//            System.out.println(mv2.getAverage());
        }
    }

    public MovingAverage(int interval) {
        this.interval = interval;
        samples = new double[interval];
        for (int i = 0; i < interval; i++) samples[i] = 0d;
    }

    public void add(double x) {
        total -= samples[index];
        samples[index] = x;
        total += x;
        if (++index == interval) index = 0; // cheaper than modulus
    }

    public double getAverage() {
        return total / interval;
    }

    public static double[] getSimpleAverage(double[] prices) {
        MovingAverage ma = new MovingAverage(prices.length);
        double[] ret = new double[prices.length];
        for (int i = 0; i < prices.length; i++) {
            ma.add(prices[i]);
            ret[i] = ma.getAverage();
        }
        return ret;
    }

    public int getInterval() {
        return interval;
    }

    public double getTotal() {
        return total;
    }

    public int getIndex() {
        return index;
    }

    public double[] getSamples() {
        return samples;
    }
}
