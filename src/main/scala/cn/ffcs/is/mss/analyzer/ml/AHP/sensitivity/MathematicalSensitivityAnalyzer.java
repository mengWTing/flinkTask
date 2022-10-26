package cn.ffcs.is.mss.analyzer.ml.AHP.sensitivity;

import cn.ffcs.is.mss.analyzer.ml.AHP.core.Criterion;
import cn.ffcs.is.mss.analyzer.ml.AHP.core.DecisionProblem;
import cn.ffcs.is.mss.analyzer.ml.AHP.core.Result;
/**
 * @author hanyu
 * @ClassName AbstractMethod
 * @date 2022/3/21 11:31
 * @description
 * @update [no][date YYYY-MM-DD][name][description]
 **/
public class MathematicalSensitivityAnalyzer extends AbstractSensitivityAnalyzer {
    private static final int NODE_FIRST_LEVEL = 0;
    private static final int NODE_MIDDLE_LEVEL = 1;
    private static final int NODE_BOTTOM_LEVEL = 2;

    public MathematicalSensitivityAnalyzer(DecisionProblem problem) {
        super(problem);
    }

    private double[][] calculateNode(Criterion parent, int[] children, int topAlternative) {
        int count = children.length;
        double[] ranking = this.problem.getRanking(this.method);
        if (ranking != null && count != 0) {
            int type = parent.getParent() == null ? 0 : (parent.hasChildren() ? 1 : 2);
            double[] weights = ((Result)parent.getResult(this.method).get(0)).getWeights();
            double[] lamda0 = new double[count];
            double sum = 0.0D;
            int size;
            if (topAlternative == -1) {
                size = 2 + 2 * count + this.problem.getAlternativeCount() * (this.problem.getAlternativeCount() - 1) / 2;
            } else {
                size = 2 + 2 * count + this.problem.getAlternativeCount() - 1;
            }

            double[][] result = new double[size][count + 2];

            int m;
            for(m = 0; m < count; ++m) {
                sum += weights[children[m]];
                result[2 * m][m] = 1.0D;
                result[2 * m][count] = 1.0D;
                result[2 * m][count + 1] = -weights[children[m]];
                result[2 * m + 1][m] = 1.0D;
                result[2 * m + 1][count] = -1.0D;
                result[2 * m + 1][count + 1] = 1.0D - weights[children[m]];
                result[2 * count][m] = 1.0D;
                result[2 * count + 1][m] = 1.0D;
            }

            result[2 * count][count] = 1.0D;
            result[2 * count][count + 1] = -sum;
            sum = 1.0D - sum;
            result[2 * count + 1][count] = -1.0D;
            result[2 * count + 1][count + 1] = sum;
            m = 2 * count + 2;
            boolean onePass = topAlternative >= 0;

            for(int i = 0; i < this.problem.getAlternativeCount() - 1; ++i) {
                if (onePass) {
                    i = -1;
                }

                for(int j = i + 1; j < this.problem.getAlternativeCount(); ++j) {
                    if (j != topAlternative) {
                        if (!onePass) {
                            topAlternative = i;
                        }

                        double lamda = ranking[topAlternative] - ranking[j];
                        double sum1 = 0.0D;

                        for(int k = 0; k < parent.getChildrenCount(); ++k) {
                            Criterion c = (Criterion)parent.getChildren().get(k);
                            if (this.indexOf(children, k) == -1) {
                                double[] rnk = c.getRanking(this.method);
                                sum1 += (rnk[topAlternative] - rnk[j]) * weights[k];
                            }
                        }

                        if (sum != 0.0D) {
                            sum1 /= sum;
                        } else {
                            sum1 = 0.0D;
                        }

                        double factor;
                        if (type == 0) {
                            factor = 1.0D;
                        } else if (type == 1) {
                            factor = parent.getContribution(this.method);
                        } else {
                            factor = 1.0D;
                        }

                        for(int n = 0; n < count; ++n) {
                            Criterion node = (Criterion)parent.getChildren().get(children[n]);
                            double[] rankingAtCriterion = node.getRanking(this.method);
                            lamda0[n] = factor * (rankingAtCriterion[j] - rankingAtCriterion[topAlternative] + sum1);
                            result[m][n] = lamda0[n];
                        }

                        result[m][count] = -1.0D;
                        result[m][count + 1] = lamda;
                        ++m;
                    }
                }

                if (onePass) {
                    break;
                }
            }

            return result;
        } else {
            return null;
        }
    }

    private double[][] calculateBottomLevel(Criterion parent, int[] children, int topAlternative) {
        int count = children.length;
        double[] ranking = this.problem.getRanking(this.method);
        if (ranking != null && count != 0) {
            double[] weights = ((Result)parent.getResult(this.method).get(0)).getWeights();
            double[] lamda0 = new double[count];
            double sum = 0.0D;
            int size;
            if (topAlternative == -1) {
                size = 2 + 2 * count + this.problem.getAlternativeCount() * (this.problem.getAlternativeCount() - 1) / 2;
            } else {
                size = 2 + 2 * count + this.problem.getAlternativeCount() - 1;
            }

            double[][] result = new double[size][count + 2];

            int m;
            for(m = 0; m < count; ++m) {
                sum += weights[children[m]];
                result[2 * m][m] = 1.0D;
                result[2 * m][count] = 1.0D;
                result[2 * m][count + 1] = -weights[children[m]];
                result[2 * m + 1][m] = 1.0D;
                result[2 * m + 1][count] = -1.0D;
                result[2 * m + 1][count + 1] = 1.0D - weights[children[m]];
                result[2 * count][m] = 1.0D;
                result[2 * count + 1][m] = 1.0D;
            }

            result[2 * count][count] = 1.0D;
            result[2 * count][count + 1] = -sum;
            sum = 1.0D - sum;
            result[2 * count + 1][count] = -1.0D;
            result[2 * count + 1][count + 1] = sum;
            m = 2 * count + 2;
            boolean onePass = topAlternative >= 0;

            for(int i = 0; i < this.problem.getAlternativeCount() - 1; ++i) {
                if (onePass) {
                    i = -1;
                }

                for(int j = i + 1; j < this.problem.getAlternativeCount(); ++j) {
                    if (j != topAlternative) {
                        if (!onePass) {
                            topAlternative = i;
                        }

                        double lamda = ranking[topAlternative] - ranking[j];
                        double parentContribution = parent.getContribution(this.method);
                        boolean hasI = this.indexOf(children, topAlternative) != -1;
                        boolean hasJ = this.indexOf(children, j) != -1;

                        for(int n = 0; n < count; ++n) {
                            if (!hasI && !hasJ) {
                                lamda0[n] = parentContribution * (weights[topAlternative] - weights[j]) / sum;
                            } else {
                                int f;
                                if (hasI && hasJ) {
                                    f = children[n] == topAlternative ? -1 : (children[n] == j ? 1 : 0);
                                    lamda0[n] = parentContribution * (double)f;
                                } else if (!hasI && hasJ) {
                                    f = children[n] != j ? 0 : 1;
                                    lamda0[n] = parentContribution * ((double)f + weights[topAlternative] / sum);
                                } else {
                                    f = children[n] != topAlternative ? 0 : 1;
                                    lamda0[n] = -parentContribution * ((double)f + weights[j] / sum);
                                }
                            }

                            result[m][n] = lamda0[n];
                        }

                        result[m][count] = -1.0D;
                        result[m][count + 1] = lamda;
                        ++m;
                    }
                }

                if (onePass) {
                    break;
                }
            }

            return result;
        } else {
            return null;
        }
    }

    public SensitivityDomain analyze(Criterion parent, int[] children) {
        return this.analyze(parent, children, -1);
    }

    private SensitivityDomain analyze(Criterion parent, int[] children, int alternative) {
        double[][] data;
        if (!parent.hasChildren()) {
            data = this.calculateBottomLevel(parent, children, alternative);
        } else if (parent.getParent() == null) {
            data = this.calculateNode(parent, children, alternative);
        } else {
            data = this.calculateNode(parent, children, alternative);
        }

        if (data == null) {
            return null;
        } else {
            SensitivityDomain solution = new SensitivityDomain(this, parent, children, this.problem, this.method, data);
            return solution;
        }
    }

    public SensitivityDomain getMostSensitiveElement() {
        double minOPSC = 1.7976931348623157E308D;
        double minTSC = 1.7976931348623157E308D;
        SensitivityDomain sdMinOPSC = null;
        SensitivityDomain sdMinTSC = null;

        for(int size = 1; size <= 3; ++size) {
            SensitivityDomain[] sd = this.processCriterion(this.problem.getRootNode(), size);

            for(int i = 0; i < sd.length; ++i) {
                if (sd[i] != null) {
                    if (sd[i].getOPSC() < minOPSC) {
                        minOPSC = sd[i].getOPSC();
                        sdMinOPSC = sd[i];
                    }

                    if (sd[i].getTSC() < minTSC) {
                        minTSC = sd[i].getTSC();
                        sdMinTSC = sd[i];
                    }
                }
            }
        }

        if (sdMinOPSC != null && sdMinTSC != null) {
            if (sdMinOPSC.getOPSC() * sdMinOPSC.getTSC() <= sdMinTSC.getOPSC() * sdMinTSC.getTSC()) {
                return sdMinOPSC;
            } else {
                return sdMinTSC;
            }
        } else {
            return sdMinOPSC != null ? sdMinOPSC : sdMinTSC;
        }
    }

    private SensitivityDomain[] processCriterion(Criterion c, int size) {
        int n = c.hasChildren() ? c.getChildrenCount() : this.problem.getAlternativeCount();
        double minOPSC = 1.7976931348623157E308D;
        double minTSC = 1.7976931348623157E308D;
        SensitivityDomain sdMinOPSC = null;
        SensitivityDomain sdMinTSC = null;
        int i;
        if (size != 1 && n != 1) {
            int j;
            if (size == 2 || n == 2) {
                for(i = 0; i < n - 1; ++i) {
                    for(j = i + 1; j < n; ++j) {
                        SensitivityDomain sd = this.analyze(c, new int[]{i, j});
                        if (sd.getOPSC() != 0.0D && sd.getTSC() != 0.0D) {
                            if (sd.getOPSC() < minOPSC) {
                                minOPSC = sd.getOPSC();
                                sdMinOPSC = sd;
                            }

                            if (sd.getTSC() < minTSC) {
                                minTSC = sd.getTSC();
                                sdMinTSC = sd;
                            }
                        }
                    }
                }
            } else if (size == 3) {
                for(i = 0; i < n - 2; ++i) {
                    for(j = i + 1; j < n - 1; ++j) {
                        for(int k = j + 1; k < n; ++k) {
                            SensitivityDomain sd = this.analyze(c, new int[]{i, j, k});
                            if (sd.getOPSC() != 0.0D && sd.getTSC() != 0.0D) {
                                if (sd.getOPSC() < minOPSC) {
                                    minOPSC = sd.getOPSC();
                                    sdMinOPSC = sd;
                                }

                                if (sd.getTSC() < minTSC) {
                                    minTSC = sd.getTSC();
                                    sdMinTSC = sd;
                                }
                            }
                        }
                    }
                }
            }
        } else {
            for(i = 0; i < n; ++i) {
                SensitivityDomain sd = this.analyze(c, new int[]{i});
                if (sd.getOPSC() != 0.0D && sd.getTSC() != 0.0D) {
                    if (sd.getOPSC() < minOPSC) {
                        minOPSC = sd.getOPSC();
                        sdMinOPSC = sd;
                    }

                    if (sd.getTSC() < minTSC) {
                        minTSC = sd.getTSC();
                        sdMinTSC = sd;
                    }
                }
            }
        }

        for(i = 0; i < c.getChildrenCount(); ++i) {
            if (c.getCriterion(i).hasChildren()) {
                SensitivityDomain[] sd = this.processCriterion(c.getCriterion(i), size);
                if (sd[0] != null && sd[0].getOPSC() < minOPSC && sd[0].getOPSC() != 0.0D) {
                    minOPSC = sd[0].getOPSC();
                    sdMinOPSC = sd[0];
                }

                if (sd[1] != null && sd[1].getTSC() < minTSC && sd[1].getTSC() != 0.0D) {
                    minTSC = sd[1].getTSC();
                    sdMinTSC = sd[1];
                }
            }
        }

        return new SensitivityDomain[]{sdMinOPSC, sdMinTSC};
    }

    private int indexOf(int[] array, int value) {
        for(int i = 0; i < array.length; ++i) {
            if (array[i] == value) {
                return i;
            }
        }

        return -1;
    }
}

