/*
 * @project test-netty
 * @company Fujian Fujitsu Communication Software Co., Ltd.
 * @author chenwei
 * @date 2019-10-17 16:13:14
 * @version v1.0
 * @update [no] [date YYYY-MM-DD] [name] [description]
 */
package cn.ffcs.is.mss.analyzer.ml.tree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author chenwei
 * @date 2019-10-17 16:13:14
 * @title CART
 * @update [no] [date YYYY-MM-DD] [name] [description]
 */
public class CART {

    /**
     * 根据样本和标记进行训练
     */
    public static DecisionTreeNode fit(int[][] samples, String[] targets) {
        return buildDecisionTree(null, samples, targets);
    }


    /**
     * 根据新的数据集进行预测
     */
    public static String[] predict(DecisionTreeNode root, int[][] samples) {
        String[] results = new String[samples.length];
        for (int i = 0; i < samples.length; i++) {
            results[i] = predict(root, samples[i]);
        }
        return results;
    }


    /**
     * 对某个数据进行预测
     */
    public static String predict(DecisionTreeNode root, int[] samples) {

        if (root == null) {
            return null;
        }

        if (root.getTarget() != null) {
            return root.getTarget();
        }

        if (root.getFeatureIndex() != null && root.getFeatureValue() != null) {

            if (samples[root.getFeatureIndex()] <= root.getFeatureValue()) {
                return predict(root.getLeftDecisionTreeNode(), samples);
            } else {
                return predict(root.getRightDecisionTreeNode(), samples);
            }

        }

        return null;
    }


    /**
     * 构建决策树,进行训练
     */
    private static DecisionTreeNode buildDecisionTree(DecisionTreeNode root, int[][] samples,
        String[] targets) {

        //如果样本集合为空，则退出
        if (samplesIsNull(samples)) {
            return null;
        }

        //如果样本数和标签数不同，则退出
        if (inconsistentLength(samples, targets)) {
            return null;
        }

        //如果特征数不相等,则退出
        if (inconsistentFeatureLength(samples)) {
            return null;
        }

        int sampleCount = samples.length;
        int featureCount = samples[0].length;

        //如果剩余的样本都是同一个分类,将剩余样本划分为一个叶子节点
        if (isSameTarget(targets)) {
            DecisionTreeNode decisionTreeNode = new DecisionTreeNode();
            decisionTreeNode.setTarget(targets[0]);
            decisionTreeNode.setParentDecisionTreeNode(root);

            return decisionTreeNode;
        }

        //如果剩余的样本都相同，则无法区分，将剩余样本划分为一个叶子节点，节点类型是其样本最多的类型
        if (isSameSample(samples)) {
            Map<String, Long> map = new HashMap<>();
            for (String target : targets) {
                map.put(target, map.getOrDefault(target, 0L) + 1L);
            }

            Long maxCount = Long.MIN_VALUE;
            String maxTarget = "";
            for (Map.Entry<String, Long> entry : map.entrySet()) {
                if (entry.getValue() > maxCount) {
                    maxCount = entry.getValue();
                    maxTarget = entry.getKey();
                }
            }

            DecisionTreeNode decisionTreeNode = new DecisionTreeNode();
            decisionTreeNode.setTarget(maxTarget);
            decisionTreeNode.setParentDecisionTreeNode(root);
            return decisionTreeNode;
        }

        //计算最大的gani值，对样本进行划分
        double minGain = Double.MAX_VALUE;
        //gani最大时所使用的特征index
        int minFeatureIndex = -1;
        //gani最大时所使用的特征取值(按照<= value对样本进行划分)
        int minFeatureValue = 0;

        //遍历特征集合
        for (int i = 0; i < featureCount; i++) {
            //计算该特征下的特征取值，由于使用<=进行划分，所以不对最大值进行划分
            Set<Integer> set = new TreeSet<Integer>();
            int maxValue = Integer.MIN_VALUE;
            for (int j = 0; j < sampleCount; j++) {
                if (maxValue < samples[j][i]) {
                    maxValue = samples[j][i];
                }
                set.add(samples[j][i]);
            }

            //遍历该特征下的特征取值
            if (set.size() > 1) {
                for (int value : set) {
                    //按照value将样本划分为小于等于value的部分和大于value的部分
                    ArrayList<String> leftArrayList = new ArrayList<>();
                    ArrayList<String> rightArrayList = new ArrayList<>();
                    if (value != maxValue) {
                        for (int j = 0; j < sampleCount; j++) {
                            if (samples[j][i] <= value) {
                                leftArrayList.add(targets[j]);
                            } else {
                                rightArrayList.add(targets[j]);
                            }
                        }

                        //计算左右两部分的gini值
                        double leftGini = computeGini(
                            (String[]) leftArrayList.toArray(new String[0]));
                        double rightGini = computeGini(
                            (String[]) rightArrayList.toArray(new String[0]));

                        //根据左右两部分的gini值，计算gain值。
                        double gain =
                            ((double) leftArrayList.size() / (double) sampleCount) * leftGini +
                                ((double) rightArrayList.size() / (double) sampleCount) * rightGini;

                        //若gain值是最大值，则按照该特征和该特征下的取值对样本进行划分。
                        if (minGain > gain) {
                            minGain = gain;
                            minFeatureIndex = i;
                            minFeatureValue = value;
                        }
                    }
                }
            }


        }

        ArrayList<String> leftTargetArrayList = new ArrayList<>();
        ArrayList<Integer[]> leftSampleArrayList = new ArrayList<>();
        ArrayList<String> rightTargetArrayList = new ArrayList<>();
        ArrayList<Integer[]> rightSampleArrayList = new ArrayList<>();
        for (int i = 0; i < sampleCount; i++) {
            if (samples[i][minFeatureIndex] <= minFeatureValue) {
                leftTargetArrayList.add(targets[i]);
                leftSampleArrayList.add(intArrayToIntegerArray(samples[i]));
            } else {
                rightTargetArrayList.add(targets[i]);
                rightSampleArrayList.add(intArrayToIntegerArray(samples[i]));
            }
        }

        DecisionTreeNode decisionTreeNode = new DecisionTreeNode();
        decisionTreeNode.setParentDecisionTreeNode(root);
        decisionTreeNode.setGain(minGain);
        decisionTreeNode.setFeatureIndex(minFeatureIndex);
        decisionTreeNode.setFeatureValue(minFeatureValue);

        decisionTreeNode.setLeftDecisionTreeNode(buildDecisionTree(decisionTreeNode,
            integerArrayListToIntArray(leftSampleArrayList),
            (String[]) leftTargetArrayList.toArray(new String[0])));

        decisionTreeNode.setRightDecisionTreeNode(buildDecisionTree(decisionTreeNode,
            integerArrayListToIntArray(rightSampleArrayList),
            (String[]) rightTargetArrayList.toArray(new String[0])));

        return decisionTreeNode;
    }


    /**
     * 计算数据集的gini系数
     * gini(T) = 1 - ∑p²
     */
    private static double computeGini(String[] targets) {
        Map<String, Long> targetDataCount = new HashMap<>(10);

        //计算不同类型样本的个数
        for (String target : targets) {
            targetDataCount.put(target, targetDataCount.getOrDefault(target, 0L) + 1L);
        }

        //获取样本个数
        long sum = targets.length;

        //计算gini系数
        double gini = 0.0;
        for (long count : targetDataCount.values()) {
            gini += Math.pow((double) count / (double) sum, 2);

        }
        gini = 1 - gini;

        return gini;
    }

    /**
     * 判断样本集合是否为空
     */
    private static boolean samplesIsNull(int[][] samples) {

        //判断样本空间为空，或者样本空间大小为0;
        return (samples == null || samples.length == 0);
    }

    /**
     * 判断样本数和标签数是否不一致
     */
    private static boolean inconsistentLength(int[][] samples, String[] targets) {
        //判断样本个数和标签个数是否不一致
        return samples.length != targets.length;
    }

    /**
     * 判断特征是否为空或者特征数为0
     */
    private static boolean featureIsNull(int[] sample) {

        //判断特征是否为空或者特征数为0
        return sample == null || sample.length == 0;
    }

    /**
     * 判断特征数是否不一致
     */
    private static boolean inconsistentFeatureLength(int[][] samples) {
        for (int i = 0; i < samples.length; i++) {
            if (featureIsNull(samples[i])) {
                return true;
            }

            if (samples[i].length != samples[0].length) {
                return true;
            }
        }

        return false;
    }

    /**
     * 判断特征是否为空
     */
    private static boolean targetIsNull(String target) {
        return target == null;
    }

    /**
     * 判断是否是同一个分类
     */
    private static boolean isSameTarget(String[] targets) {

        //判断剩余的样本是否都为同一个分类
        String firstTarget = targets[0];
        //todo
        //如果标签为空,则返回
        if (firstTarget == null) {
            return true;
        }
        boolean allTargetAreSame = true;
        for (int i = 1; i < targets.length; i++) {

            //todo
            //如果标签为空,则返回
            if (targets[i] == null) {
                return true;
            }
            if (!firstTarget.equals(targets[i])) {
                allTargetAreSame = false;
                break;
            }
        }

        return allTargetAreSame;

    }

    /**
     * 判断样本是否相同
     */
    private static boolean isSameSample(int[][] samples) {
        boolean allSamplesAreSame = true;
        for (int i = 0; i < samples.length; i++) {
            if (!Arrays.equals(samples[i], samples[0])) {
                allSamplesAreSame = false;
                break;
            }
        }
        return allSamplesAreSame;

    }

    /**
     * Integer数组转换为int数组
     */
    private static int[] integerArrayToIntArray(Integer[] arrays) {
        if (arrays == null || arrays.length == 0) {
            return new int[0];
        }

        int[] integerArray = new int[arrays.length];

        for (int i = 0; i < arrays.length; i++) {
            integerArray[i] = arrays[i];
        }

        return integerArray;
    }

    /**
     * int数组转换为Integer数组
     */
    private static Integer[] intArrayToIntegerArray(int[] arrays) {
        if (arrays == null || arrays.length == 0) {
            return new Integer[0];
        }

        Integer[] integerArray = new Integer[arrays.length];

        for (int i = 0; i < arrays.length; i++) {
            integerArray[i] = arrays[i];
        }

        return integerArray;
    }

    private static int[][] integerArrayListToIntArray(ArrayList<Integer[]> arrayList) {
        int[][] intArray = new int[arrayList.size()][];

        for (int i = 0; i < intArray.length; i++) {

            intArray[i] = integerArrayToIntArray(arrayList.get(i));
        }
        return intArray;
    }

}
