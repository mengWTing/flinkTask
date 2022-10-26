package cn.ffcs.is.mss.analyzer.ml.AHP.property;

import Jama.Matrix;
import cn.ffcs.is.mss.analyzer.ml.AHP.core.PCMatrix;
/**
 * @author hanyu
 * @ClassName AbstractMethod
 * @date 2022/3/21 11:31
 * @description
 * @update [no][date YYYY-MM-DD][name][description]
 **/
public class ConsistencyMeasure extends AbstractMatrixProperty {
    public static final String PROPERTY_NAME = "CM";

    public ConsistencyMeasure() {
    }

    public double getValue(PCMatrix matrix) {
        return cm(matrix, (int[])null);
    }

    public String getName() {
        return "CM";
    }

    public static double cm(PCMatrix matrix, int[] blamed) {
        int n = matrix.getRowDimension();
        if (n == 0) {
            return 0.0D;
        } else {
            PCMatrix matOut = new PCMatrix(Matrix.identity(n, n).getArray());
            double CM = 0.0D;

            for(int i = 0; i < n; ++i) {
                matOut.set(i, i, 0.0D);

                for(int j = i + 1; j < n; ++j) {
                    for(int k = 0; k < n; ++k) {
                        if (i != k && j != k) {
                            double a = matrix.get(i, j);
                            double c = matrix.get(j, k);
                            double b = matrix.get(i, k);
                            if (a > 0.0D && b > 0.0D && c > 0.0D) {
                                double cm_a = 1.0D / a * Math.abs(a - b / c);
                                double cm_b = 1.0D / b * Math.abs(b - a * c);
                                double cm_c = 1.0D / c * Math.abs(c - b / a);
                                double cm = cm_a;
                                if (cm_a > cm_b) {
                                    cm = cm_b;
                                }

                                if (cm > cm_c) {
                                    cm = cm_c;
                                }

                                if (cm > CM) {
                                    CM = cm;
                                    if (blamed != null) {
                                        blamed[0] = i;
                                        blamed[1] = j;
                                        blamed[2] = k;
                                    }
                                }
                            }
                        }
                    }
                }
            }

            return CM;
        }
    }
}

