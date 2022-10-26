package cn.ffcs.is.mss.analyzer.ml.AHP.property;


import cn.ffcs.is.mss.analyzer.ml.AHP.core.PCMatrix;

import java.util.ArrayList;
import java.util.Arrays;
/**
 * @author hanyu
 * @ClassName AbstractMethod
 * @date 2022/3/21 11:31
 * @description
 * @update [no][date YYYY-MM-DD][name][description]
 **/
public class TournamentAnalyzer {
    public TournamentAnalyzer() {
    }

    public static ArrayList<int[]> findLoops(PCMatrix matrix, boolean preferenceEquivalence) {
        return preferenceEquivalence ? findLoopsJensen(matrix) : findLoopsKendall(matrix);
    }

    public static ArrayList<int[]> findLoopsJensen(PCMatrix matrix) {
        int n = matrix.getRowDimension();
        ArrayList<int[]> vLoops = new ArrayList();

        for(int i = 0; i < n; ++i) {
            for(int j = 0; j < n; ++j) {
                if (i != j) {
                    for(int k = 0; k < n; ++k) {
                        if (j != k && k != i) {
                            int[] vLoop = new int[]{i, j, k};
                            Arrays.sort(vLoop);
                            boolean bFlag = true;

                            for(int m = 0; m < vLoops.size(); ++m) {
                                int[] vMem = (int[])vLoops.get(m);
                                if (Arrays.equals(vMem, vLoop)) {
                                    bFlag = false;
                                }
                            }

                            double Rij = Math.log(matrix.get(i, j));
                            double Rik = Math.log(matrix.get(i, k));
                            double Rjk = Math.log(matrix.get(j, k));
                            if (Rij * Rik <= 0.0D && Rik * Rjk < 0.0D && bFlag) {
                                vLoops.add(vLoop);
                            }

                            if (Rij == 0.0D && Rik == 0.0D && Rjk != 0.0D && bFlag) {
                                vLoops.add(vLoop);
                            }
                        }
                    }
                }
            }
        }

        return vLoops;
    }

    public static ArrayList<int[]> findLoopsKendall(PCMatrix matrix) {
        int N = matrix.getRowDimension();
        ArrayList<int[]> vLoops = new ArrayList();

        for(int i = 0; i < N; ++i) {
            for(int j = 0; j < N; ++j) {
                if (i != j) {
                    double a_ij = matrix.get(i, j);

                    for(int k = 0; k < N; ++k) {
                        if (j != k && k != i) {
                            double a_ik = matrix.get(i, k);
                            double a_kj = matrix.get(k, j);
                            if (a_ik > 0.0D && a_kj > 0.0D && a_ij > 0.0D && a_ik > 1.0D && a_kj > 1.0D && a_ij < 1.0D) {
                                int[] vLoop = new int[]{i, j, k};
                                Arrays.sort(vLoop);
                                boolean bFlag = true;

                                for(int m = 0; m < vLoops.size(); ++m) {
                                    int[] vMem = (int[])vLoops.get(m);
                                    if (Arrays.equals(vMem, vLoop)) {
                                        bFlag = false;
                                    }
                                }

                                if (bFlag) {
                                    vLoops.add(vLoop);
                                }
                            }
                        }
                    }
                }
            }
        }

        return vLoops;
    }
}

