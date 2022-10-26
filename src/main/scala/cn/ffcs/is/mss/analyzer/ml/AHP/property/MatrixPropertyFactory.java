package cn.ffcs.is.mss.analyzer.ml.AHP.property;


import java.util.LinkedHashMap;
import java.util.Map;
/**
 * @author hanyu
 * @ClassName AbstractMethod
 * @date 2022/3/21 11:31
 * @description
 * @update [no][date YYYY-MM-DD][name][description]
 **/
public class MatrixPropertyFactory {
    private static final MatrixPropertyFactory instance = new MatrixPropertyFactory();

    private MatrixPropertyFactory() {
    }

    public static MatrixPropertyFactory getInstance() {
        return instance;
    }

    public Map<String, AbstractMatrixProperty> createAllProperties() {
        Map<String, AbstractMatrixProperty> map = new LinkedHashMap();
        map.put("Ψ", new Dissonance());
        map.put("Θ", new Congruence());
        map.put("L", new Loops());
        map.put("CR", new ConsistencyRatio());
        map.put("CM", new ConsistencyMeasure());
        return map;
    }
}

