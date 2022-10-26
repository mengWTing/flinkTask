package cn.ffcs.is.mss.analyzer.ml.AHP.objective;

/**
 * @author hanyu
 * @ClassName AbstractMethod
 * @date 2022/3/21 11:31
 * @description
 * @update [no][date YYYY-MM-DD][name][description]
 **/
public class TDObjective extends AbstractObjective {
    public TDObjective() {
    }

    public TDObjective(boolean indirect) {
        this.isIndirect = indirect;
    }

    double gauge(double a, double[] w, int i, int j) {
        double td = a - w[i] / w[j];
        return td * td;
    }

    double mean(double d, int m) {
        return d;
    }
}
