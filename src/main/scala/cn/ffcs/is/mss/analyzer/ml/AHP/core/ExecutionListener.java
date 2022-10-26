package cn.ffcs.is.mss.analyzer.ml.AHP.core;
import cn.ffcs.is.mss.analyzer.ml.AHP.method.AbstractMethod;

public interface ExecutionListener {

    void methodExecuted(AbstractMethod var1, Criterion var2, int var3, int var4);
}
