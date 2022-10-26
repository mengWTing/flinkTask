package cn.ffcs.is.mss.analyzer.ml.AHP.core;


import cn.ffcs.is.mss.analyzer.ml.AHP.method.AbstractMethod;
/**
 * @author hanyu
 * @ClassName ProblemSolution
 * @date 2022/3/21 11:35
 * @description
 * @update [no][date YYYY-MM-DD][name][description]
 **/
public class ProblemSolution extends Result {
    private DecisionProblem problem;
    private AbstractMethod method;

    public ProblemSolution(DecisionProblem problem, AbstractMethod method) {
        this.problem = problem;
        this.method = method;
    }

    public AbstractMethod getMethod() {
        return this.method;
    }

    public DecisionProblem getProblem() {
        return this.problem;
    }
}

