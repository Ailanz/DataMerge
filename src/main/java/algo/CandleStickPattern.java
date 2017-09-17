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
        if(data.size() > window) {
            data.remove(0);
        }
    }

    public boolean isBearishEngulfing(){
        //Bearish
        if(data.size() < 2) {
            return false;
        }
        CandleStick bull = data.get(data.size() - 2);
        CandleStick bear = data.get(data.size() - 1);
        return bear.isBlack() && bull.isWhite() && bull.getOpen() > bear.getClose() && bear.getBodyLength() > bull.getBodyLength() * 1;
        //relax constraint
    }

    public boolean isBullishEngulfing(){
        //Bullish
        if(data.size() < 2) {
            return false;
        }
        CandleStick bear = data.get(data.size() - 2);
        CandleStick bull = data.get(data.size() - 1);
        return bear.isBlack() && bull.isWhite()  && bull.getClose() > bear.getOpen() && bull.getBodyLength() > bear.getBodyLength() * 2;
        //&& bull.getOpen() < bear.getClose()   relax restraint
    }

    public boolean isThreeLineStrikes() {
        //Bullish
        if (data.size() < 4) {
            return false;
        }

        CandleStick blackBar1 = data.get(data.size() - 4);
        CandleStick blackBar2 = data.get(data.size() - 3);
        CandleStick blackBar3 = data.get(data.size() - 2);
        CandleStick longWhiteBar = data.get(data.size() - 1);

        return blackBar1.isBlack() && blackBar2.isBlack() && blackBar3.isBlack()
                && blackBar1.getClose() > blackBar2.getClose() && blackBar2.getClose() > blackBar3.getClose()
                && longWhiteBar.isWhite() && longWhiteBar.getOpen() < blackBar3.getClose()
                && longWhiteBar.getClose() > blackBar1.getOpen();
    }


}
