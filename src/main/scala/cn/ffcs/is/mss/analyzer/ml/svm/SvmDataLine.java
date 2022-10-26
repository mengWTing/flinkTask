package cn.ffcs.is.mss.analyzer.ml.svm;

/**
 * @author hanyu
 * @title SvmDataLine
 * @date 2020-10-12 09:56
 * @description
 * @update [no][date YYYY-MM-DD][name][description]
 */

import java.util.ArrayList;
import java.util.Collection;

public class SvmDataLine {
    Collection<Double> x;
    public int y;

    public SvmDataLine(ArrayList<Double> x, int y) {
        super();
        this.x = x;
        this.y = y;

    }

}
