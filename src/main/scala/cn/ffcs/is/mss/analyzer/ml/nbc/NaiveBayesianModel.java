package cn.ffcs.is.mss.analyzer.ml.nbc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.math3.distribution.NormalDistribution;

/**
 * @Auther chenwei
 * @Description
 * @Date: Created in 2017/11/17 18:01
 * @Modified By
 */
public class NaiveBayesianModel {

    public Map<Integer, NormalDistribution[]> normalDistributionMap;

    /**
     * 根据样本获取类别
     */
    public int getClassified(double[] sample) {

        Map<Integer, double[]> probabilityMap = new HashMap<>();

        //计算样本在不同类别下的每个特征中分布的概率
        for (Integer classified : normalDistributionMap.keySet()) {
            double[] probabilities = probabilityMap.getOrDefault(classified, new double[sample.length]);
            for (int j = 0; j < sample.length; j++) {
                probabilities[j] = MaximumLikelihoodEstimate
                    .getProbability(normalDistributionMap.get(classified)[j], sample[j]);
            }

            probabilityMap.put(classified, probabilities);
        }

        int index = -1;
        double maxProbability = Double.MIN_VALUE;
        //计算出现的概率。并且概率最大的即为类型
        for (Map.Entry<Integer, double[]> entry : probabilityMap.entrySet()){
            double probability = 1.0;
            double[] probabilities = entry.getValue();
            for (int i = 0; i < probabilities.length; i++) {
                probability *= probabilities[i];
            }
            if (maxProbability < probability) {
                maxProbability = probability;
                index = entry.getKey();
            }
        }

        return index;
    }


    /**
     * 根据样本进行训练
     */
    public void train(double[][] samples, int[] classifieds) {

        Map<Integer, ArrayList<double[]>> sampleMap = new HashMap<>(5);
        for (int i = 0; i < samples.length; i++) {
            ArrayList<double[]> arrayList = sampleMap
                .getOrDefault(classifieds[i], new ArrayList<>());
            arrayList.add(samples[i]);
            sampleMap.put(classifieds[i], arrayList);
        }

        normalDistributionMap = new HashMap<>(sampleMap.size());

        for (Map.Entry<Integer, ArrayList<double[]>> entry : sampleMap.entrySet()) {

            int featureNumber = entry.getValue().get(0).length;
            NormalDistribution[] normalDistributions = new NormalDistribution[featureNumber];
            for (int i = 0; i < featureNumber; i++) {

                double[] features = new double[featureNumber];

                for (int j = 0; j < entry.getValue().size(); j++) {
                    features[j] = entry.getValue().get(j)[i];

                }

                double mean = MaximumLikelihoodEstimate.getMean(features);
                double sd = MaximumLikelihoodEstimate.getSd(features, mean);
                normalDistributions[i] = MaximumLikelihoodEstimate.getNormalDistribution(mean, sd);
            }

            normalDistributionMap.put(entry.getKey(), normalDistributions);


        }
    }

}
