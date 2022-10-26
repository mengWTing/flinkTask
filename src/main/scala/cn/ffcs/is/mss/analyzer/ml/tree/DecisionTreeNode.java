/*
 * @project test-netty
 * @company Fujian Fujitsu Communication Software Co., Ltd.
 * @author chenwei
 * @date 2019-10-17 16:16:26
 * @version v1.0
 * @update [no] [date YYYY-MM-DD] [name] [description]
 */
package cn.ffcs.is.mss.analyzer.ml.tree;

import java.io.Serializable;

/**
 * @author chenwei
 * @date 2019-10-17 16:16:26
 * @title DecisionTreeNode
 * @update [no] [date YYYY-MM-DD] [name] [description]
 */
public class DecisionTreeNode implements Serializable {

    /**
     * 父节点
     */
    private DecisionTreeNode parentDecisionTreeNode;
    /**
     * 左子树
     */
    private DecisionTreeNode leftDecisionTreeNode;
    /**
     * 右子树
     */
    private DecisionTreeNode rightDecisionTreeNode;
    /**
     * 特征索引
     */
    private Integer featureIndex;
    /**
     * 特征划分的值
     */
    private Integer featureValue;
    /**
     * gini系数
     */
    private Double gain;
    /**
     * 类别
     */
    private String target;

    public DecisionTreeNode(){

    }

    public DecisionTreeNode getParentDecisionTreeNode() {
        return parentDecisionTreeNode;
    }

    public void setParentDecisionTreeNode(
        DecisionTreeNode parentDecisionTreeNode) {
        this.parentDecisionTreeNode = parentDecisionTreeNode;
    }

    public DecisionTreeNode getLeftDecisionTreeNode() {
        return leftDecisionTreeNode;
    }

    public void setLeftDecisionTreeNode(
        DecisionTreeNode leftDecisionTreeNode) {
        this.leftDecisionTreeNode = leftDecisionTreeNode;
    }

    public DecisionTreeNode getRightDecisionTreeNode() {
        return rightDecisionTreeNode;
    }

    public void setRightDecisionTreeNode(
        DecisionTreeNode rightDecisionTreeNode) {
        this.rightDecisionTreeNode = rightDecisionTreeNode;
    }

    public Integer getFeatureIndex() {
        return featureIndex;
    }

    public void setFeatureIndex(Integer featureIndex) {
        this.featureIndex = featureIndex;
    }

    public Integer getFeatureValue() {
        return featureValue;
    }

    public void setFeatureValue(Integer featureValue) {
        this.featureValue = featureValue;
    }

    public Double getGain() {
        return gain;
    }

    public void setGain(Double gain) {
        this.gain = gain;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    @Override
    public String toString() {

        return "featureIndex:" + this.getFeatureIndex() + "|" +
            "featureValue:" + this.getFeatureValue() + "|" +
            "gain:" + this.getGain() + "|" +
            "target:" + this.getTarget() + "|";
    }
}
