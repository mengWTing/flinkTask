package cn.ffcs.is.mss.analyzer.ml.svm;

/**
 * @author hanyu
 * @title SvmPrediceData
 * @date 2020-10-14 15:08
 * @description
 * @update [no][date YYYY-MM-DD][name][description]
 */
public class SvmPrediceData {
    private double[][] x;
    private int[] y;

    public SvmPrediceData(double[][] x, int[] y) {
        super();
        this.x = x;
        this.y = y;
    }

    public double[][] getX() {
        return x;
    }

    public void setX(double[][] x) {
        this.x = x;
    }

    public int[] getY() {
        return y;
    }

    public void setY(int[] y) {
        this.y = y;
    }
}

