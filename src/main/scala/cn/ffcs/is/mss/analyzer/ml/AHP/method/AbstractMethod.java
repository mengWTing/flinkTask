package cn.ffcs.is.mss.analyzer.ml.AHP.method;

import cn.ffcs.is.mss.analyzer.ml.AHP.core.PCMatrix;
import cn.ffcs.is.mss.analyzer.ml.AHP.core.Result;
import cn.ffcs.is.mss.analyzer.ml.AHP.core.ResultList;


public abstract class AbstractMethod {

    protected PCMatrix matrix;
    public ResultList results;

    public AbstractMethod() {
    }

    public abstract String getName();

    public abstract String getDescription();

    public void cancel() {
    }

    public ResultList calculate(PCMatrix matrix) {
        long t = System.currentTimeMillis();
        this.matrix = matrix.clone();
        if (matrix.getRowDimension() == 1) {
            this.results = this.getSolution2(matrix);
        } else {
            this.results = this.doCalculate(matrix);
        }

        t = System.currentTimeMillis() - t;
        if (this.results != null) {
            this.results.setExecutionTime(t);
        }

        return this.results;
    }

    private ResultList getSolution2(PCMatrix matrix) {
        double[] solution = matrix.getRow(0);
        ResultList result = new ResultList(1);
        result.setMethod(this);
        Result r = new Result();
        r.setWeights(solution);
        r.setTD(0.0D);
        r.setNV(0.0D);
        r.setTD2(0.0D);
        result.add(r);
        return result;
    }

    private ResultList getSolution1() {
        double[] solution = new double[]{1.0D};
        ResultList result = new ResultList(1);
        result.setMethod(this);
        Result r = new Result();
        r.setWeights(solution);
        r.setTD(0.0D);
        r.setNV(0.0D);
        r.setTD2(0.0D);
        result.add(r);
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else {
            return !(obj instanceof AbstractMethod) ? false : this.getName().equals(((AbstractMethod)obj).getName());
        }
    }

    public int hashCode() {
        return this.getName().hashCode();
    }

    protected abstract ResultList doCalculate(PCMatrix var1);

    public String toString() {
        return this.getDescription();
    }
}

