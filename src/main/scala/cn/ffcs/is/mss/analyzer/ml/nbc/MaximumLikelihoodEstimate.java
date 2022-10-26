package cn.ffcs.is.mss.analyzer.ml.nbc;

import org.apache.commons.math3.distribution.NormalDistribution;

/**
 * @Auther chenwei
 * @Description
 * @Date: Created in 2017/11/17 15:37
 * @Modified By
 */
public class MaximumLikelihoodEstimate {


    /**
     * 构建正态分布
     * @param mean
     * @param sd
     * @return
     */
    public static NormalDistribution getNormalDistribution(double mean, double sd) {

        return new NormalDistribution(mean, sd);

    }


    public static double getProbability(NormalDistribution normalDistribution, double count) {

        double x1 = (1 / Math.sqrt(2 * Math.PI * normalDistribution.getStandardDeviation()));
        double x2 = (Math.exp(
            (-1 * Math.pow((count - normalDistribution.getMean()), 2) / (2 * normalDistribution
                .getStandardDeviation()))));

        return x1 * x2;
    }


    /**
     * @Auther chenwei
     * @Description 获取平均数
     * @Date: Created in 2017/11/17 15:51
     */
    public static double getMean(double[] X) {

        double sum = 0;

        for (int i = 0; i < X.length; i++) {

            sum = sum + X[i];

        }

        return sum / X.length;
    }

    /**
     * 获取标准差
     */
    public static double getSd(double[] X, double mean) {

        double sum = 0;

        for (int i = 0; i < X.length; i++) {

            sum = sum + Math.pow(2, (X[i] - mean));

        }

        double sd = sum / X.length;

        return Math.sqrt(sd);

    }

}
