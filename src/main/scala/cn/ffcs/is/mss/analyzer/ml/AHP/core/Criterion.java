package cn.ffcs.is.mss.analyzer.ml.AHP.core;

import cn.ffcs.is.mss.analyzer.ml.AHP.method.AbstractMethod;

import java.util.*;

/**
 * @author hanyu
 * @ClassName Criterion
 * @date 2022/3/21 11:29
 * @description
 * @update [no][date YYYY-MM-DD][name][description]
 **/

public class Criterion {

    private String name;
    private Criterion parent;
    private List<Criterion> children;
    private PCMatrix matrix;
    private Map<AbstractMethod, ResultList> results;

    public Criterion(String name) {
        this.name = name;
        this.children = new ArrayList();
        this.results = new LinkedHashMap();
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Criterion getParent() {
        return this.parent;
    }

    public void setParent(Criterion parent) {
        this.parent = parent;
    }

    public Criterion addCriterion(Criterion c) {
        if (!this.children.contains(c)) {
            this.children.add(c);
            c.setParent(this);
            this.clearResults();
            return c;
        } else {
            return null;
        }
    }

    public Criterion addCriterion(String name) {
        Criterion c = new Criterion(name);
        return this.addCriterion(c);
    }

    public List<Criterion> getChildren() {
        return this.children;
    }

    public Criterion getCriterion(int index) {
        return (Criterion) this.children.get(index);
    }

    public int getChildrenCount() {
        return this.children.size();
    }

    public boolean hasChildren() {
        return this.getChildrenCount() > 0;
    }

    public void removeCriterion(int index) {
        ((Criterion) this.children.get(index)).setParent((Criterion) null);
        this.children.remove(index);
        this.clearResults();
    }

    public void removeCriterion(Criterion c) {
        this.removeCriterion(this.indexOf(c));
    }

    public void removeAllCriteria() {
        Iterator var2 = this.children.iterator();

        while (var2.hasNext()) {
            Criterion c = (Criterion) var2.next();
            c.setParent((Criterion) null);
        }

        this.children.clear();
    }

    public int indexOf(Criterion c) {
        return this.children.indexOf(c);
    }

    public void setPCMatrix(PCMatrix matrix) {
        if ((matrix.getRows() != matrix.getCols() || matrix.getRows() != this.children.size()) && !this.children.isEmpty()) {
            throw new IllegalArgumentException("Matrix size must match sub-criteria number");
        } else {
            this.matrix = matrix.clone();
        }
    }

    public PCMatrix getPCMatrix() {
        return this.matrix;
    }

    public void setResult(AbstractMethod method, ResultList results) {
        this.results.put(method, results);
    }

    public ResultList getResult(AbstractMethod method) {
        if (this.results.isEmpty()) {
            return null;
        } else {
            return method == null ? (ResultList) this.results.values().iterator().next() : (ResultList) this.results.get(method);
        }
    }

    public Map<AbstractMethod, ResultList> getResults() {
        return this.results;
    }

    public void clearResults() {
        this.results.clear();
    }

    public void clearAllResults() {
        this.clearResults();
        Iterator var2 = this.getChildren().iterator();

        while (var2.hasNext()) {
            Criterion child = (Criterion) var2.next();
            child.clearAllResults();
        }

    }

    public double[] getRanking() {
        return this.getRanking((AbstractMethod) null);
    }

    public Map<AbstractMethod, double[]> getRankings() {
        Map rankings = new LinkedHashMap();
        Iterator var3 = this.results.keySet().iterator();

        while (var3.hasNext()) {
            AbstractMethod m = (AbstractMethod) var3.next();

            try {
                double[] rank = this.getRanking(m);
                if (rank != null) {
                    rankings.put(m, rank);
                }
            } catch (Exception var5) {
            }
        }

        return rankings;
    }

    public double[] getRanking(AbstractMethod method) {
        ResultList r;
        if (this.getChildrenCount() == 0) {
            r = this.getResult(method);
            return r != null && r.size() != 0 ? ((Result) r.get(0)).getWeights() : null;
        } else {
            r = this.getResult(method);
            if (r != null && r.size() != 0) {
                double[] weights = ((Result) r.get(0)).getWeights();
                double[] scores = null;
                double[] s = new double[this.getChildrenCount()];

                for (int j = 0; j < this.getChildren().size(); ++j) {
                    Criterion child = (Criterion) this.getChildren().get(j);
                    double[] childRank = child.getRanking(method);
                    if (childRank == null) {
                        return null;
                    }

                    s[j] = childRank[0];
                    if (scores == null) {
                        scores = new double[childRank.length];
                    }

                    for (int i = 0; i < childRank.length; ++i) {
                        scores[i] += childRank[i] * weights[j];
                    }
                }

                ((Result) this.getResult(method).get(0)).setScroes(s);
                return scores;
            } else {
                return null;
            }
        }
    }

    public ArrayList<double[]> getDecisionMatrix(AbstractMethod method) {
        ArrayList<double[]> scores = new ArrayList();
        ResultList r = this.getResult(method);
        if (r != null && r.size() != 0) {
            for (int j = 0; j < this.getChildren().size(); ++j) {
                Criterion child = (Criterion) this.getChildren().get(j);
                double[] s_ij = child.getRanking(method);
                scores.add(s_ij);
            }

            return scores;
        } else {
            return null;
        }
    }

    public double getObjectiveValue(AbstractMethod method, String objective) {
        if (this.getChildrenCount() == 0) {
            ResultList r = this.getResult(method);
            return ((Result) r.get(0)).getObjectiveValue(objective);
        } else {
            double td = 0.0D;
            List<Criterion> children = this.getChildren();

            for (int j = 0; j < children.size(); ++j) {
                Criterion child = (Criterion) children.get(j);
                double childTD = child.getObjectiveValue(method, objective);
                ResultList r = this.getResult(method);
                double[] weights = ((Result) r.get(0)).getWeights();
                td += childTD * weights[j];
            }

            return td;
        }
    }

    public double getContribution() {
        return this.getContribution((AbstractMethod) null, (Criterion) null);
    }

    public double getContribution(AbstractMethod method) {
        return this.getContribution(method, (Criterion) null);
    }

    public double getContribution(AbstractMethod method, Criterion c) {
        Criterion parentCriterion = this.getParent();
        if (this != c && parentCriterion != null) {
            ResultList rl = parentCriterion.getResult(method);
            if (rl != null && rl.size() != 0) {
                double[] weight = ((Result) rl.get(0)).getWeights();
                double w = weight[parentCriterion.indexOf(this)];
                return w * parentCriterion.getContribution(method, c);
            } else {
                return 0.0D;
            }
        } else {
            return 1.0D;
        }
    }
}

