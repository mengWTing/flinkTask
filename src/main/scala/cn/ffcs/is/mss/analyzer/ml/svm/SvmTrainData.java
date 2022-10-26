package cn.ffcs.is.mss.analyzer.ml.svm;

import java.io.Serializable;
import java.util.*;

/**
 * @author hanyu
 * @title SvmTrainData
 * @date 2020-10-14 15:09
 * @description
 * @update [no][date YYYY-MM-DD][name][description]
 */
public class SvmTrainData implements Serializable {
    private static SvmTrainData instance = null;
    private Map<Integer, Set<Collection<Double>>> featureVectors;

    public Map<Integer, Set<Collection<Double>>> getFeatureVectors() {
        return featureVectors;
    }

    public void setFeatureVectors(Map<Integer, Set<Collection<Double>>> featureVectors) {
        this.featureVectors = featureVectors;
    }

    public SvmTrainData() {
        featureVectors = new HashMap<Integer, Set<Collection<Double>>>();

    }

    public static SvmTrainData getInstance() {
        if (instance == null) {
            instance = new SvmTrainData();
        }
        return instance;
    }

    public void addVector(Collection<Double> vectors, int featureId) {
        if (!featureVectors.containsKey(featureId)) {
            featureVectors.put(featureId, new HashSet<Collection<Double>>());
        }
        Collection<Double> v = new ArrayList<Double>();
        v.addAll(vectors);
        featureVectors.get(featureId).add(v);
    }

    public Set<Collection<Double>> get(int featureeId) {
        return featureVectors.get(featureeId);
    }


}

