package cn.ffcs.is.mss.analyzer.ml.AHP.core;

import cn.ffcs.is.mss.analyzer.ml.AHP.method.AbstractMethod;
import cn.ffcs.is.mss.analyzer.ml.AHP.sensitivity.MathematicalSensitivityAnalyzer;
import cn.ffcs.is.mss.analyzer.ml.AHP.sensitivity.SensitivityDomain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * @author hanyu
 * @ClassName Solver
 * @date 2022/3/21 11:47
 * @description
 * @update [no][date YYYY-MM-DD][name][description]
 **/

public class Solver {
    private List<AbstractMethod> methods = new ArrayList();
    private List<ExecutionListener> listeners;
    private int current;
    private int total;
    private DecisionProblem problem;
    private boolean cancel = false;
    private AbstractMethod currentMethod;
    private boolean singleSolution = false;

    public Solver() {
    }

    public void addMethod(AbstractMethod method) {
        this.methods.add(method);
    }

    public void removeMethod(int index) {
        if (index >= 0 && index <= this.getMethodCount()) {
            this.methods.remove(index);
        }

    }

    public int getMethodCount() {
        return this.methods.size();
    }

    public List<AbstractMethod> getMethods() {
        return Collections.unmodifiableList(this.methods);
    }

    public void cancel() {
        this.cancel = true;
        if (this.currentMethod != null) {
            this.currentMethod.cancel();
        }

    }

    public List<ProblemSolution> solve(DecisionProblem problem) {
        List<ProblemSolution> solutions = new ArrayList(this.getMethodCount());
        this.problem = problem;
        this.current = 0;
        this.total = this.getMethodCount() * problem.getNodeCount();
        this.currentMethod = null;

        AbstractMethod method;
        Iterator var4;
        try {
            var4 = this.methods.iterator();

            while(var4.hasNext()) {
                method = (AbstractMethod)var4.next();
                this.solveForCriteria(method, problem.getRootNode());
            }
        } catch (InterruptedException var11) {
            problem.getRootNode().clearAllResults();
            return null;
        }

        ProblemSolution s;
        for(var4 = this.methods.iterator(); var4.hasNext(); solutions.add(s)) {
            method = (AbstractMethod)var4.next();
            s = new ProblemSolution(problem, method);
            double[] weights = problem.getRanking(method);
            double[] scores = ((Result)problem.getRootNode().getResult(method).get(0)).getScroes();
            s.setScroes(scores);
            if (weights != null) {
                s.setWeights(weights);
                String[] objs = ((Result)problem.getRootNode().getResult(method).get(0)).getObjectives();

                for(int i = 0; i < objs.length; ++i) {
                    s.setObjectiveValue(objs[i], problem.getRootNode().getObjectiveValue(method, objs[i]));
                }
            }
        }

        if (this.singleSolution) {
            try {
                this.selectSingleSolution(problem.getRootNode());
            } catch (InterruptedException var10) {
                problem.getRootNode().clearAllResults();
                return null;
            }
        }

        return solutions;
    }

    private void solveForCriteria(AbstractMethod method, Criterion node) throws InterruptedException {
        if (this.cancel) {
            throw new InterruptedException("Canceled");
        } else {
            this.currentMethod = method;
            if (node != null && node.getPCMatrix() != null && node.getPCMatrix().getRows() != 0) {
                if ((!node.hasChildren() || node.getPCMatrix().getRows() == node.getChildrenCount()) && (node.hasChildren() || node.getPCMatrix().getCols() == this.problem.getAlternativeCount())) {
                    new ResultList();
                    ResultList result = method.calculate(node.getPCMatrix());
                    if (this.cancel) {
                        throw new InterruptedException("Canceled");
                    } else {
                        node.setResult(method, result);
                        ++this.current;
                        this.notifyListeners(method, node);
                        Iterator var5 = node.getChildren().iterator();

                        while(var5.hasNext()) {
                            Criterion child = (Criterion)var5.next();
                            this.solveForCriteria(method, child);
                        }

                    }
                } else {
                    throw new IllegalArgumentException("PCMatrix size not equals to children size");
                }
            } else {
                throw new IllegalArgumentException("Node and PCMatrix cannot be null");
            }
        }
    }

    public synchronized void addExecutionListener(ExecutionListener listener) {
        if (listener != null) {
            if (this.listeners == null) {
                this.listeners = new ArrayList();
            }

            this.listeners.add(listener);
        }
    }

    private void notifyListeners(AbstractMethod m, Criterion c) {
        if (this.listeners != null) {
            Iterator var4 = this.listeners.iterator();

            while(var4.hasNext()) {
                ExecutionListener l = (ExecutionListener)var4.next();
                l.methodExecuted(m, c, this.current, this.total);
            }

        }
    }

    public void setSingleSolution(boolean singleSolution) {
        this.singleSolution = singleSolution;
    }

    public boolean isSingleSolution() {
        return this.singleSolution;
    }

    private void selectSingleSolution(Criterion node) throws InterruptedException {
        if (this.cancel) {
            throw new InterruptedException("Canceled");
        } else {
            Iterator var3 = this.methods.iterator();

            while(var3.hasNext()) {
                AbstractMethod method = (AbstractMethod)var3.next();
                ResultList rl = node.getResult(method);
                if (rl.size() > 1) {
                    this.setBestSolution(rl);
                }
            }

            var3 = node.getChildren().iterator();

            while(var3.hasNext()) {
                Criterion child = (Criterion)var3.next();
                this.selectSingleSolution(child);
            }

        }
    }

    private Result setBestSolution(ResultList rl) {
        MathematicalSensitivityAnalyzer sa = new MathematicalSensitivityAnalyzer(this.problem);
        sa.setMethod(rl.getMethod());
        List results = (List)rl.clone();
        double max = 0.0D;
        Result best = null;

        for(int i = 0; i < results.size(); ++i) {
            rl.clear();
            rl.add((Result)results.get(i));
            SensitivityDomain sd = sa.getMostSensitiveElement();
            if (sd.getOPSC() > max) {
                max = sd.getOPSC();
                best = (Result)results.get(i);
            }
        }

        rl.clear();
        rl.add(best);
        return best;
    }
}

