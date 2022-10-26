package cn.ffcs.is.mss.analyzer.ml.AHP.core;

import cn.ffcs.is.mss.analyzer.ml.AHP.method.AbstractMethod;

import java.util.ArrayList;

/**
 * @author hanyu
 * @ClassName ResultList
 * @date 2022/3/21 11:36
 * @description
 * @update [no][date YYYY-MM-DD][name][description]
 **/

public class ResultList extends ArrayList<Result> {
    private long executionTime;
    private AbstractMethod method;

    public ResultList() {
    }

    public ResultList(int size) {
        super(size);
    }

    public long getExecutionTime() {
        return this.executionTime;
    }

    public void setExecutionTime(long executionTime) {
        this.executionTime = executionTime;
    }

    public AbstractMethod getMethod() {
        return this.method;
    }

    public void setMethod(AbstractMethod method) {
        this.method = method;
    }
}

