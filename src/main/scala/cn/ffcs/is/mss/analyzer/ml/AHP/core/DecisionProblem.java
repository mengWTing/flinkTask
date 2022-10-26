package cn.ffcs.is.mss.analyzer.ml.AHP.core;

import cn.ffcs.is.mss.analyzer.ml.AHP.method.AbstractMethod;

import java.util.*;

/**
 * @author hanyu
 * @ClassName DecisionProblem
 * @date 2022/3/21 11:31
 * @description
 * @update [no][date YYYY-MM-DD][name][description]
 **/

public class DecisionProblem {

    private String goal;
    private List<String> alternatives;
    private Criterion rootNode;
    private String title;
    private String descripttion;

    public DecisionProblem() {
        this("Goal");
    }

    public DecisionProblem(String goal) {
        this.goal = goal;
        this.rootNode = new Criterion(goal);
        this.alternatives = new ArrayList();
    }

    public DecisionProblem(String goal, String title) {
        this(goal);
        this.title = title;
    }

    public String getGoal() {
        return this.goal;
    }

    public void setGoal(String goal) {
        this.goal = goal;
        this.rootNode.setName(goal);
    }

    public void addAlternative(String alternative) {
        this.alternatives.add(alternative);
    }

    public void setAlternatives(String[] alternatives) {
        this.alternatives.clear();
        this.alternatives.addAll(Arrays.asList((String[]) Arrays.copyOf(alternatives, alternatives.length)));
    }

    public List<String> getAlternatives() {
        return Collections.unmodifiableList(this.alternatives);
    }

    public int getAlternativeCount() {
        return this.alternatives.size();
    }

    public String getAlternative(int index) {
        return (String) this.alternatives.get(index);
    }

    public void setAlternativeAt(String name, int index) {
        this.alternatives.set(index, name);
    }

    public void removeAlternative(String alternative) {
        this.alternatives.remove(alternative);
    }

    public void removeAlternative(int index) {
        this.alternatives.remove(index);
    }

    public void removeAllAlternatives() {
        this.alternatives.clear();
    }

    public Criterion addCriteria(String name) {
        return this.rootNode.addCriterion(name);
    }

    public Criterion getRootNode() {
        return this.rootNode;
    }

    public void setRootNode(Criterion root) {
        this.rootNode = root;
    }

    public void setPCMatrix(PCMatrix matrix) {
        if (matrix.getRows() == matrix.getCols() && matrix.getRows() == this.rootNode.getChildrenCount()) {
            this.rootNode.setPCMatrix(matrix);
        } else {
            throw new IllegalArgumentException("Matrix size must match sub-criteria number");
        }
    }

    public double[] getRanking(AbstractMethod method) {
        return this.rootNode.getRanking(method);
    }

    public double getTD(AbstractMethod method) {
        return this.rootNode.getObjectiveValue(method, "TD");
    }

    public double getTD2(AbstractMethod method) {
        return this.rootNode.getObjectiveValue(method, "TD2");
    }

    public double getNV(AbstractMethod method) {
        return this.rootNode.getObjectiveValue(method, "NV");
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescripttion() {
        return this.descripttion;
    }

    public void setDescripttion(String descripttion) {
        this.descripttion = descripttion;
    }

    public int getNodeCount() {
        return this.getNodeCount(this.rootNode);
    }

    private int getNodeCount(Criterion node) {
        int count = 1;
        List<Criterion> children = node.getChildren();

        Criterion child;
        for (Iterator var5 = children.iterator(); var5.hasNext(); count += this.getNodeCount(child)) {
            child = (Criterion) var5.next();
        }

        return count;
    }
}

