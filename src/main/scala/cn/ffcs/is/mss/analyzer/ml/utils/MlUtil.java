/*
 * @project test-netty
 * @company Fujian Fujitsu Communication Software Co., Ltd.
 * @author chenwei
 * @date 2019-10-21 15:40:25
 * @version v1.0
 * @update [no] [date YYYY-MM-DD] [name] [description]
 */
package cn.ffcs.is.mss.analyzer.ml.utils;


/**
 * @author chenwei
 * @date 2019-10-21 15:40:25
 * @title MlUtil
 * @update [no] [date YYYY-MM-DD] [name] [description]
 */
public class MlUtil {
    /**
     * 按照testSize对数据进行划分
     * @param samples
     * @param targets
     * @param testSize
     * @return
     */
    public static TrainTestSplit trainTestSplit(int[][] samples, String[] targets, double testSize, String[] samplesStr) {

        int samplesCount = samples.length;
        int testSamplesCount = (int) (testSize * samplesCount);
        int trainSamplesCount = samplesCount - testSamplesCount;

        int[][] trainSamples = new int[trainSamplesCount][];
        int[][] testSamples = new int[testSamplesCount][];
        String[] trainTarget = new String[trainSamplesCount];
        String[] testTarget = new String[testSamplesCount];

        String[] trainSamplesStr = new String[trainSamplesCount];
        String[] testSamplesStr = new String[testSamplesCount];

        int testIndex = 0;
        int trainIndex = 0;

        for (int i = 0; i < samples.length; i++) {
            if (Math.random() <= testSize && testIndex < testSamplesCount) {
                testSamples[testIndex] = samples[i];
                testTarget[testIndex] = targets[i];
                testSamplesStr[testIndex] = samplesStr[i];
                testIndex++;
            } else {
                if (trainIndex < trainSamplesCount) {
                    trainSamples[trainIndex] = samples[i];
                    trainTarget[trainIndex] = targets[i];
                    trainSamplesStr[trainIndex] = samplesStr[i];
                    trainIndex++;
                }else{
                    testSamples[testIndex] = samples[i];
                    testTarget[testIndex] = targets[i];
                    testSamplesStr[testIndex] = samplesStr[i];
                    testIndex++;
                }
            }
        }

        return new TrainTestSplit(trainSamples, testSamples, trainTarget, testTarget, trainSamplesStr, testSamplesStr);

    }


}
