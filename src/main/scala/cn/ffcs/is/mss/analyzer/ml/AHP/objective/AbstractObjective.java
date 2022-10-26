package cn.ffcs.is.mss.analyzer.ml.AHP.objective;

import cn.ffcs.is.mss.analyzer.ml.AHP.core.PCMatrix;
import cn.ffcs.is.mss.analyzer.ml.AHP.util.MathUtil;
/**
 * @author hanyu
 * @ClassName AbstractMethod
 * @date 2022/3/21 11:31
 * @description
 * @update [no][date YYYY-MM-DD][name][description]
 **/
public abstract class AbstractObjective {
    protected final int DEFAULT_VALUE = 100000;
    protected boolean isIndirect = false;

    public AbstractObjective() {
    }

    public double calculate(PCMatrix matrix, double[] solution) {
        if (solution != null && this.canCalculate(matrix, solution)) {
            return this.isIndirect ? this.indirectValue(matrix, solution) : this.regularValue(matrix, solution);
        } else {
            return 100000.0D;
        }
    }

    protected boolean canCalculate(PCMatrix matrix, double[] solution) {
        return true;
    }

    protected double regularValue(PCMatrix matrix, double[] solution) {
        if (solution == null) {
            return 100000.0D;
        } else {
            double[] vector = MathUtil.normalise(solution);
            int n = matrix.getRows();
            int m = 0;
            double d = 0.0D;

            for(int i = 0; i < n; ++i) {
                for(int j = 0; j < n; ++j) {
                    double a = matrix.get(i, j);
                    if (this.isValid(a, vector, i, j)) {
                        d += this.gauge(a, vector, i, j);
                        ++m;
                    }
                }
            }

            d = this.mean(d, m);
            return d;
        }
    }

    protected double indirectValue(PCMatrix matrix, double[] solution) {
        if (solution == null) {
            return 100000.0D;
        } else {
            double[] vector = MathUtil.normalise(solution);
            int n = matrix.getRows();
            int m = 0;
            double td = 0.0D;

            for(int i = 0; i < n; ++i) {
                for(int j = 0; j < n; ++j) {
                    for(int k = 0; k < n; ++k) {
                        if (j != k && k != i) {
                            double aij = matrix.get(i, k) * matrix.get(k, j);
                            if (aij > 0.0D && matrix.get(i, k) > 0.0D && this.isValid(aij, vector, i, j)) {
                                td += this.gauge(aij, vector, i, j);
                                ++m;
                            }
                        }
                    }
                }
            }

            td = this.mean(td, m);
            return td;
        }
    }

    public double penalty() {
        return 0.0D;
    }

    public int constraints() {
        return 0;
    }

    protected boolean isValid(double a, double[] vector, int i, int j) {
        return a > 0.0D;
    }

    abstract double gauge(double var1, double[] var3, int var4, int var5);

    abstract double mean(double var1, int var3);
}

