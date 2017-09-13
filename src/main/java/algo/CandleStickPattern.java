package algo;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Ailan on 9/12/2017.
 */
public class CandleStickPattern {
    private int window;
    private List<CandleStick> data = new LinkedList<>();

    public CandleStickPattern(){
        this(10);
    }

    public CandleStickPattern(int window) {
        this.window = window;
    }

    public void addCandleStick(CandleStick stick) {
        data.add(stick);
        while (data.size() > window) {
            data.remove(0);
        }
    }

    public boolean isThreeLineStrikes() {
        if (data.size() < 4) {
            return false;
        }

        CandleStick blackBar1 = data.get(data.size() - 4);
        CandleStick blackBar2 = data.get(data.size() - 3);
        CandleStick blackBar3 = data.get(data.size() - 2);
        CandleStick longWhiteBar = data.get(data.size() - 1);

        return !blackBar1.isWhite() && !blackBar2.isWhite() && !blackBar3.isWhite()
                && blackBar1.getClose() > blackBar2.getClose() && blackBar2.getClose() > blackBar3.getClose()
                && longWhiteBar.isWhite() && longWhiteBar.getOpen() < blackBar3.getClose()
                && longWhiteBar.getClose() > blackBar1.getOpen();
    }


}
