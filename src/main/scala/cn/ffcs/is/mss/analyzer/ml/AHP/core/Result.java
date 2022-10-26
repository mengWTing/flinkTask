package cn.ffcs.is.mss.analyzer.ml.AHP.core;


import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;
/**
 * @author hanyu
 * @ClassName Result
 * @date 2022/3/21 11:35
 * @description
 * @update [no][date YYYY-MM-DD][name][description]
 **/
public class Result {
    private double[] scroes;
    private double[] weights;
    private final Map<String, Double> _OBJECTIVES = new HashMap();

    public Result() {
    }

    public double[] getScroes() {
        return this.scroes;
    }

    public void setScroes(double[] s) {
        this.scroes = new double[s.length];
        System.arraycopy(s, 0, this.scroes, 0, s.length);
    }

    public void setWeights(double[] w) {
        this.weights = new double[w.length];
        System.arraycopy(w, 0, this.weights, 0, w.length);
    }

    public double[] getWeights() {
        return this.weights;
    }

    public String[] getObjectives() {
        String[] result = new String[0];
        return (String[])this._OBJECTIVES.keySet().toArray(result);
    }

    public double getObjectiveValue(String objective) {
        return this._OBJECTIVES.containsKey(objective) ? (Double)this._OBJECTIVES.get(objective) : 0.0D;
    }

    public void setObjectiveValue(String objective, double value) {
        this._OBJECTIVES.put(objective, value);
    }

    public double getTD() {
        return (Double)this._OBJECTIVES.get("TD");
    }

    public void setTD(double td) {
        this.setObjectiveValue("TD", td);
    }

    public double getTD2() {
        return (Double)this._OBJECTIVES.get("TD2");
    }

    public void setTD2(double td2) {
        this.setObjectiveValue("TD2", td2);
    }

    public double getNV() {
        return (Double)this._OBJECTIVES.get("NV");
    }

    public void setNV(double nv) {
        this.setObjectiveValue("NV", nv);
    }

    public Result normalize() {
        Result res = new Result();
        res.setWeights(normalize(this.weights));
        return res;
    }

    public static Result normalize(Result w) {
        Result res = new Result();
        res.setWeights(normalize(w.weights));
        return res;
    }

    public static double[] normalize(double[] w) {
        double[] ans = new double[w.length];
        double sum = 0.0D;

        int i;
        for(i = 0; i < w.length; ++i) {
            ans[i] = Math.abs(w[i]);
            sum += ans[i];
        }

        if (sum > 0.0D) {
            for(i = 0; i < ans.length; ++i) {
                ans[i] /= sum;
            }
        }

        return ans;
    }

    public String asCSV() {
        return asCSV(this.getWeights());
    }

    public static String asCSV(double[] data) {
        if (data == null) {
            return "";
        } else {
            String str = data.length + ",1,";
            NumberFormat formatter = new DecimalFormat("#.000000000");

            for(int j = 0; j < data.length; ++j) {
                str = str + formatter.format(data[j]) + ",";
            }

            str = str + "EOR";
            return str;
        }
    }
}

