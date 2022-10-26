package cn.ffcs.is.mss.analyzer.ml.AHP.property;

import cn.ffcs.is.mss.analyzer.ml.AHP.core.PCMatrix;
/**
 * @author hanyu
 * @ClassName AbstractMethod
 * @date 2022/3/21 11:31
 * @description
 * @update [no][date YYYY-MM-DD][name][description]
 **/
public class Dissonance extends AbstractMatrixProperty {
    public static final String PROPERTY_NAME = "Ψ";

    public Dissonance() {
    }

    public double getValue(PCMatrix matrix) {
        matrix = IndirectAnalyzer.dissonance(matrix);
        int n = matrix.getRowDimension();
        double ans = 0.0D;

        for(int i = 0; i < n; ++i) {
            for(int j = 0; j < n; ++j) {
                if (i != j) {
                    ans += matrix.get(i, j);
                }
            }
        }

        return ans / (double)(n * (n - 1));
    }

    public String getName() {
        return "Ψ";
    }
}

