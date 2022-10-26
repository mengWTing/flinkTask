package cn.ffcs.is.mss.analyzer.ml.AHP.sensitivity;


import cn.ffcs.is.mss.analyzer.ml.AHP.core.Criterion;
import cn.ffcs.is.mss.analyzer.ml.AHP.core.DecisionProblem;
import cn.ffcs.is.mss.analyzer.ml.AHP.core.Result;
import cn.ffcs.is.mss.analyzer.ml.AHP.method.AbstractMethod;

import java.util.Arrays;
/**
 * @author hanyu
 * @ClassName AbstractMethod
 * @date 2022/3/21 11:31
 * @description
 * @update [no][date YYYY-MM-DD][name][description]
 **/
public abstract class AbstractSensitivityAnalyzer {
    protected DecisionProblem problem;
    protected AbstractMethod method;

    public AbstractSensitivityAnalyzer(DecisionProblem problem) {
        this.problem = problem;
    }

    public DecisionProblem getProblem() {
        return this.problem;
    }

    public double[] getRanking(double[] newWeights) {
        return this.problem.getRanking(this.method);
    }

    public synchronized double[] getRanking(Criterion c, double[] newWeights) {
        if (c.getResult(this.method) == null) {
            return null;
        } else {
            Result result = (Result)c.getResult(this.method).get(0);
            double[] weigths = result.getWeights();
            newWeights = this.proportionalWeights(weigths, newWeights);
            result.setWeights(newWeights);
            double[] ranking = this.problem.getRanking(this.method);
            result.setWeights(weigths);
            return ranking;
        }
    }

    public double[] getRanking(Criterion c, int child, double weight) {
        double[] newWeights = new double[c.hasChildren() ? c.getChildrenCount() : this.problem.getAlternativeCount()];
        Arrays.fill(newWeights, -1.0D);
        newWeights[child] = weight;
        return this.getRanking(c, newWeights);
    }

    public double[] getRanking(Criterion c, double weight) {
        Criterion p = c.getParent();
        return this.getRanking(p, p.indexOf(c), weight);
    }

    public AbstractMethod getMethod() {
        return this.method;
    }

    public void setMethod(AbstractMethod method) {
        if (this.problem != null && !this.problem.getRootNode().getResults().containsKey(method)) {
            throw new IllegalArgumentException("Method " + method + " not found in problem results");
        } else {
            this.method = method;
        }
    }

    protected double[] proportionalWeights(double[] weights, int index, double weight) {
        double[] newWeights = new double[weights.length];
        Arrays.fill(newWeights, -1.0D);
        newWeights[index] = weight;
        return this.proportionalWeights(weights, newWeights);
    }

    public double[] proportionalWeights(double[] weights, double[] newWeights) {
        double[] w = new double[weights.length];
        double sum = 0.0D;
        double sum1 = 0.0D;
        double sum2 = 0.0D;

        int i;
        for(i = 0; i < newWeights.length; ++i) {
            if (newWeights[i] != -1.0D) {
                sum += newWeights[i];
                sum1 += newWeights[i] - weights[i];
            } else {
                sum2 += weights[i];
            }
        }

        if (sum == 1.0D) {
            for(i = 0; i < w.length; ++i) {
                w[i] = newWeights[i] == -1.0D ? 0.0D : newWeights[i];
            }
        } else {
            for(i = 0; i < w.length; ++i) {
                if (newWeights[i] == -1.0D) {
                    w[i] = weights[i] - sum1 * weights[i] / sum2;
                } else {
                    w[i] = newWeights[i];
                }
            }
        }

        return w;
    }
}

