package cn.ffcs.is.mss.analyzer.ml.AHP.property;

import cn.ffcs.is.mss.analyzer.ml.AHP.core.PCMatrix;
/**
 * @author hanyu
 * @ClassName AbstractMethod
 * @date 2022/3/21 11:31
 * @description
 * @update [no][date YYYY-MM-DD][name][description]
 **/
public class IndirectAnalyzer {
    public IndirectAnalyzer() {
    }

    public static PCMatrix congruence(PCMatrix matrix) {
        int n = matrix.getRowDimension();
        if (n == 0) {
            return new PCMatrix(n);
        } else {
            double[][] ans = new double[n][n];

            for(int i = 0; i < n; ++i) {
                for(int j = 0; j < n; ++j) {
                    double cong = 0.0D;
                    if (i != j) {
                        double Aij = matrix.get(i, j);
                        if (Aij > 0.0D) {
                            double b = Math.log(Aij);

                            for(int k = 0; k < n; ++k) {
                                double Aik = matrix.get(i, k);
                                double Akj = matrix.get(k, j);
                                if (j != k && k != i && Aik > 0.0D && Akj > 0.0D) {
                                    double b2 = Math.log(Aik * Akj);
                                    cong += Math.abs(b - b2);
                                }
                            }
                        }
                    }

                    if (n > 2) {
                        ans[i][j] = cong / (double)(n - 2);
                    }
                }
            }

            return new PCMatrix(ans);
        }
    }

    public static PCMatrix dissonance(PCMatrix matrix) {
        int n = matrix.getRowDimension();
        if (n == 0) {
            return new PCMatrix(n);
        } else {
            double[][] ans = new double[n][n];

            for(int i = 0; i < n; ++i) {
                for(int j = 0; j < n; ++j) {
                    double diss = 0.0D;
                    if (i != j) {
                        double Aij = matrix.get(i, j);
                        if (Aij > 0.0D) {
                            double b = Math.log(Aij);

                            for(int k = 0; k < n; ++k) {
                                double Aik = matrix.get(i, k);
                                double Akj = matrix.get(k, j);
                                if (j != k && k != i && Aik > 0.0D && Akj > 0.0D) {
                                    double b2 = Math.log(Aik * Akj);
                                    diss += b * b2 < 0.0D ? 1.0D : 0.0D;
                                }
                            }
                        }
                    }

                    if (n > 2) {
                        ans[i][j] = diss / (double)(n - 2);
                    }
                }
            }

            return new PCMatrix(ans);
        }
    }
}

