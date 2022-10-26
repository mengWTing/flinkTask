package cn.ffcs.is.mss.analyzer.ml.AHP.property;

import Jama.EigenvalueDecomposition;
import Jama.Matrix;
import cn.ffcs.is.mss.analyzer.ml.AHP.core.PCMatrix;
/**
 * @author hanyu
 * @ClassName AbstractMethod
 * @date 2022/3/21 11:31
 * @description
 * @update [no][date YYYY-MM-DD][name][description]
 **/
public class ConsistencyRatio extends AbstractMatrixProperty {
    public static final String PROPERTY_NAME = "CR";
    static final double[] RI = new double[]{
            0.0D, 0.0D, 1.0D, 0.58D,
            0.9D, 1.12D, 1.24D, 1.32D,
            1.41D, 1.45D, 1.51D, 1.56D,
            1.59D, 1.6D
    };

    public ConsistencyRatio() {
    }

    public double getValue(PCMatrix matrix) {
        if (matrix.containsLowerThan(0.0D)) {
            return 0.0D;
        } else {
            double lemdaMax = this.findPerronRoot(matrix);
            int n = matrix.getRowDimension();
            double CI = Math.abs((lemdaMax - (double)n) / (double)(n - 1));
            double CR = CI;
            if (n >= 2 && n <= RI.length) {
                CR = CI / RI[n];
            }

            return CR;
        }
    }

    public String getName() {
        return "CR";
    }

    private double findPerronRoot(PCMatrix matrix) {
        EigenvalueDecomposition ev = matrix.eig();
        Matrix evd = ev.getD();
        int n = matrix.getRowDimension();
        int lemdaRow = 0;
        double lemdaMax = evd.get(lemdaRow, lemdaRow);

        for(int i = 0; i < n; ++i) {
            double d = evd.get(i, i);
            if (d > lemdaMax) {
                lemdaMax = d;
            }
        }

        return lemdaMax;
    }
}

