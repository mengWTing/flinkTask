package cn.ffcs.is.mss.analyzer.ml.AHP.method;


import Jama.EigenvalueDecomposition;
import Jama.Matrix;
import cn.ffcs.is.mss.analyzer.ml.AHP.core.PCMatrix;
import cn.ffcs.is.mss.analyzer.ml.AHP.core.Result;
import cn.ffcs.is.mss.analyzer.ml.AHP.core.ResultList;
import cn.ffcs.is.mss.analyzer.ml.AHP.objective.NVObjective;
import cn.ffcs.is.mss.analyzer.ml.AHP.objective.TDObjective;
import cn.ffcs.is.mss.analyzer.ml.AHP.util.MathUtil;
/**
 * @author hanyu
 * @ClassName AbstractMethod
 * @date 2022/3/21 11:31
 * @description
 * @update [no][date YYYY-MM-DD][name][description]
 **/
public class EigenvectorMethod extends AbstractMethod {
    private static final String _NAME = "EV";
    private static final String _DESC = "Eigenvector";

    public EigenvectorMethod() {
    }

    protected ResultList doCalculate(PCMatrix matrix) {
        if (matrix == null) {
            return null;
        } else {
            boolean isIncomplete = matrix.containsLowerThan(0.0D);
            if (isIncomplete) {
                return new ResultList(0);
            } else {
                EigenvalueDecomposition ev = matrix.eig();
                Matrix evd = ev.getD();
                Matrix vec = ev.getV();
                int n = matrix.getCols();
                int lemdaRow = 0;
                double lemdaMax = evd.get(lemdaRow, lemdaRow);
                for(int i = 0; i < n; ++i) {
                    double d = evd.get(i, i);
                    if (d > lemdaMax) {
                        lemdaMax = d;
                        lemdaRow = i;
                    }
                }
                double[] solution = new double[vec.getRowDimension()];

                for(int i = 0; i < n; ++i) {
                    solution[i] = vec.get(i, lemdaRow);
                }

                solution = MathUtil.normalise(solution);
                ResultList resultList = new ResultList(1);
                resultList.setMethod(this);
                Result r = new Result();
                r.setWeights(solution);
                r.setTD((new TDObjective()).calculate(matrix, solution));
                r.setNV((new NVObjective()).calculate(matrix, solution));
                r.setTD2((new TDObjective(true)).calculate(matrix, solution));
                resultList.add(r);
                return resultList;
            }
        }
    }

    public String getName() {
        return "EV";
    }

    public String getDescription() {
        return "Eigenvector";
    }
}

