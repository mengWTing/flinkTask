/*
 * @project test-netty
 * @company Fujian Fujitsu Communication Software Co., Ltd.
 * @author chenwei
 * @date 2019-10-21 15:42:03
 * @version v1.0
 * @update [no] [date YYYY-MM-DD] [name] [description]
 */
package cn.ffcs.is.mss.analyzer.ml.utils;

/**
 * @author chenwei
 * @date 2019-10-21 15:42:03
 * @title TrainTestSplit
 * @update [no] [date YYYY-MM-DD] [name] [description]
 */
public class TrainTestSplit {

    public int[][] trainSamples;
    public int[][] testSamples;
    public String[] trainTarget;
    public String[] testTarget;
    public String[] trainSamplesStr;
    public String[] testSamplesStr;

    public TrainTestSplit(int[][] trainSamples, int[][] testSamples,
        String[] trainTarget, String[] testTarget, String[] trainSamplesStr,
        String[] testSamplesStr) {
        this.trainSamples = trainSamples;
        this.testSamples = testSamples;
        this.trainTarget = trainTarget;
        this.testTarget = testTarget;
        this.trainSamplesStr = trainSamplesStr;
        this.testSamplesStr = testSamplesStr;
    }
}
