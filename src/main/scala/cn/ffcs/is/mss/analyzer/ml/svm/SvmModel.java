package cn.ffcs.is.mss.analyzer.ml.svm;

/**
 * @author hanyu
 * @title SvmModel
 * @date 2020-10-12 09:27
 * @description
 * @update [no][date YYYY-MM-DD][name][description]
 */
public class SvmModel {
    public double[][][] a;
    public double[][] b;

    public SvmModel(double[][][] a, double[][] b) {
        super();
        this.a = a;
        this.b = b;
    }


}
