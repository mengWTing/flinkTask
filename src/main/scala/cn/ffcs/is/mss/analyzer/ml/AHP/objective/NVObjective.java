package cn.ffcs.is.mss.analyzer.ml.AHP.objective;

/**
 * @author hanyu
 * @ClassName AbstractMethod
 * @date 2022/3/21 11:31
 * @description
 * @update [no][date YYYY-MM-DD][name][description]
 **/
public class NVObjective extends AbstractObjective {
    private boolean fEq = false;

    public NVObjective() {
    }

    public NVObjective(boolean eq) {
        this.fEq = eq;
    }

    double gauge(double a, double[] w, int i, int j) {
        double b = w[i] / w[j];
        double nv = 0.0D;
        if (a > 1.0D && b < 1.0D) {
            ++nv;
        } else if (a < 1.0D && b > 1.0D) {
            ++nv;
        } else if (this.fEq) {
            if (b == 1.0D && a != 1.0D) {
                nv += 0.5D;
            } else if (a == 1.0D && b != 1.0D) {
                nv += 0.5D;
            }
        }

        return nv;
    }

    double mean(double d, int m) {
        return d / 2.0D;
    }
}

