package cn.ffcs.is.mss.analyzer.ml.AHP.property;

import cn.ffcs.is.mss.analyzer.ml.AHP.core.PCMatrix;
/**
 * @author hanyu
 * @ClassName AbstractMethod
 * @date 2022/3/21 11:31
 * @description
 * @update [no][date YYYY-MM-DD][name][description]
 **/
public abstract class AbstractMatrixProperty {
    public AbstractMatrixProperty() {
    }

    public abstract double getValue(PCMatrix var1);

    public abstract String getName();
}
