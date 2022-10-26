package cn.ffcs.is.mss.analyzer.ml.AHP.property;

import cn.ffcs.is.mss.analyzer.ml.AHP.core.PCMatrix;
/**
 * @author hanyu
 * @ClassName AbstractMethod
 * @date 2022/3/21 11:31
 * @description
 * @update [no][date YYYY-MM-DD][name][description]
 **/
public class Loops extends AbstractMatrixProperty {
    public static final String PROPERTY_NAME = "L";
    private boolean preferenceEquivalence = false;

    public Loops() {
    }

    public double getValue(PCMatrix matrix) {
        return (double)TournamentAnalyzer.findLoops(matrix, this.preferenceEquivalence).size();
    }

    public String getName() {
        return "L";
    }

    public void setPreferenceEquivalence(boolean value) {
        this.preferenceEquivalence = value;
    }

    public boolean isPreferenceEquivalence() {
        return this.preferenceEquivalence;
    }
}

