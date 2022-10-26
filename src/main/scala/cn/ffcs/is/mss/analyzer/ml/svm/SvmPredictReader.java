package cn.ffcs.is.mss.analyzer.ml.svm;

import java.io.IOException;

/**
 * @author hanyu
 * @title SvmPredictReader
 * @date 2020-10-14 14:34
 * @description Flink流数据转计算矩阵
 * @update [no][date YYYY-MM-DD][name][description]
 */
public class SvmPredictReader {
    public SvmPrediceData getSvmData(String[] strArr, int traitNum) {
        String[] s1;
        double[][] x = new double[1][traitNum];
        int[] y = new int[1];
        y[0] = (int) Double.parseDouble(strArr[0]);
        for (int j = 1; j < strArr.length; j++)
            x[0][j - 1] = Double.parseDouble(strArr[j]);
        return new SvmPrediceData(x, y);


    }

    public SvmPrediceData getSVMDataAll(int index, int size, String[] strs, int traitNum) {
        String[] s1;
        double[][] x = new double[size][traitNum];
        int[] y = new int[size];
        //for (int i = 1; i < size; i++) {
        for (int i = 1; i < size; i++) {

            y[i] = (int) Double.parseDouble(strs[0]);

            for (int j = 1; j < strs.length; j++)
                x[i][j - 1] = Double.parseDouble(strs[j]);

        }
        return new SvmPrediceData(x, y);
    }


}
