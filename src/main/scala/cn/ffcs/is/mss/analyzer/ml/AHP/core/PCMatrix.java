package cn.ffcs.is.mss.analyzer.ml.AHP.core;

import Jama.Matrix;
import cn.ffcs.is.mss.analyzer.ml.AHP.property.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

/**
 * @author hanyu
 * @ClassName PCMatrix
 * @date 2022/3/21 11:33
 * @description
 * @update [no][date YYYY-MM-DD][name][description]
 **/

public class PCMatrix extends Matrix {
    private boolean preferenceEquivalence;
    private boolean cache = false;
    private int cycles = 0;
    private ArrayList<int[]> cycle;
    private int[] inconsistentCycle = new int[]{-1, -1, -1};
    private PCMatrix theta;
    private double thetaMax;
    private PCMatrix psi;
    static final Map<String, AbstractMatrixProperty> properties = MatrixPropertyFactory.getInstance().createAllProperties();

    public Iterator<String> getProperties() {
        return properties.keySet().iterator();
    }

    public int getPropertySize() {
        return properties.size();
    }

    public double getProperty(String name) {
        if (this.getRowDimension() <= 1) {
            return 0.0D;
        } else {
            try {
                if (properties.containsKey(name)) {
                    return ((AbstractMatrixProperty)properties.get(name)).getValue(this);
                }
            } catch (Exception var3) {
            }

            return 0.0D;
        }
    }

    public PCMatrix(int n) {
        super(n, n);
        this.setDiagonal(1.0D);
    }

    public PCMatrix(int n, double value) {
        super(n, n, value);
    }

    public PCMatrix(double[][] matrix) {
        super(matrix);
    }

    public PCMatrix(double[][] matrix, int rows, int cols) {
        super(matrix, rows, cols);
    }

    public final void setDiagonal(double value) {
        for(int i = 0; i < this.getRowDimension(); ++i) {
            super.set(i, i, 1.0D);
        }

    }

    private boolean isValid(int i, int j) {
        int n = this.getCols();
        return i >= 0 && j >= 0 && i < n && j < n;
    }

    public boolean invert(int i, int j) {
        if (this.isValid(i, j)) {
            double value = this.get(i, j);
            super.set(i, j, 1.0D / value);
            super.set(j, i, value);
            this.cache = false;
            return true;
        } else {
            return false;
        }
    }

    public PCMatrix getAdjacencyMatrix() {
        int n = this.getRows();
        PCMatrix matrix = new PCMatrix(n);

        for(int i = 0; i < n; ++i) {
            for(int j = 0; j < n; ++j) {
                matrix.set(i, j, (double)(this.get(i, j) > 1.0D ? 1 : 0));
            }
        }

        return matrix;
    }

    public PCMatrix clone() {
        return new PCMatrix(this.getArrayCopy(), this.getRows(), this.getCols());
    }

    public int getRows() {
        return this.getRowDimension();
    }

    public int getCols() {
        return this.getColumnDimension();
    }

    public boolean contains(double value) {
        for(int i = 0; i < this.getRows(); ++i) {
            for(int j = 0; j < this.getCols(); ++j) {
                if (this.get(i, j) == value) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean containsLowerThan(double value) {
        for(int i = 0; i < this.getRows(); ++i) {
            for(int j = 0; j < this.getCols(); ++j) {
                if (this.get(i, j) <= value) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean containsGreaterThan(double value) {
        for(int i = 0; i < this.getRows(); ++i) {
            for(int j = 0; j < this.getCols(); ++j) {
                if (this.get(i, j) >= value) {
                    return true;
                }
            }
        }

        return false;
    }

    public double getMax() {
        double max = 0.0D;

        for(int i = 0; i < this.getRows(); ++i) {
            for(int j = 0; j < this.getCols(); ++j) {
                if (this.get(i, j) > max) {
                    max = this.get(i, j);
                }
            }
        }

        return max;
    }

    public double[] getRow(int row) {
        double[] vector = new double[this.getCols()];

        for(int i = 0; i < vector.length; ++i) {
            vector[i] = this.get(row, i);
        }

        return vector;
    }

    public void setRow(int row, double[] values) {
        if (values != null) {
            for(int i = 0; i < values.length && i < this.getRows(); ++i) {
                super.set(row, i, values[i]);
            }

            this.cache = false;
        }
    }

    public double[] getColumn(int col) {
        double[] vector = new double[this.getRows()];

        for(int i = 0; i < vector.length; ++i) {
            vector[i] = this.get(i, col);
        }

        return vector;
    }

    public void setColumn(int col, double[] values) {
        if (values != null) {
            for(int i = 0; i < values.length && i < this.getCols(); ++i) {
                super.set(i, col, values[i]);
            }

            this.cache = false;
        }
    }

    public void set(int i, int j, double d) {
        super.set(i, j, d);
        super.set(j, i, d != 0.0D ? 1.0D / d : 0.0D);
        this.cache = false;
    }

    private void updateCache() {
        if (!this.cache) {
            this.cycles = (int)this.getProperty("L");
            this.cycle = TournamentAnalyzer.findLoops(this, this.preferenceEquivalence);
            this.inconsistentCycle = new int[]{-1, -1, -1};
            ConsistencyMeasure.cm(this, this.inconsistentCycle);
            this.theta = IndirectAnalyzer.congruence(this);
            this.thetaMax = this.theta.getMax();
            this.psi = IndirectAnalyzer.dissonance(this);
            this.cache = true;
        }

    }

    public double[] getIndirectRatios(int i, int j) {
        if (i == j) {
            return null;
        } else {
            double[] ratios = new double[this.getRowDimension() - 2];
            int r = 0;

            for(int k = 0; k < this.getRowDimension(); ++k) {
                if (k != i && k != j) {
                    double aik = this.get(i, k);
                    double akj = this.get(k, j);
                    if (aik > 0.0D && akj > 0.0D) {
                        ratios[r++] = aik * akj;
                    }
                }
            }

            return ratios;
        }
    }

    public boolean isInconsistent(int i, int j) {
        this.updateCache();
        int n = 0;

        for(int c = 0; c != this.inconsistentCycle.length; ++c) {
            if (i == this.inconsistentCycle[c] || j == this.inconsistentCycle[c]) {
                ++n;
            }
        }

        if (n == 2) {
            return true;
        } else {
            return false;
        }
    }

    public int[] getCycle(int index) {
        this.updateCache();
        return (int[])this.cycle.get(index);
    }

    public int[] getInconsistentCycle() {
        this.updateCache();
        return this.inconsistentCycle[0] != -1 ? this.inconsistentCycle : null;
    }

    public int getCycleSize() {
        this.updateCache();
        return this.cycles;
    }

    public double getCongruence(int i, int j) {
        this.updateCache();
        return this.theta != null ? this.theta.get(i, j) : 0.0D;
    }

    public double getMaxCongruence() {
        this.updateCache();
        return this.thetaMax;
    }

    public double getDissonance(int i, int j) {
        this.updateCache();
        return this.psi != null ? this.psi.get(i, j) : 0.0D;
    }

    public boolean isPreferenceEquivalence() {
        return this.preferenceEquivalence;
    }

    public void setPreferenceEquivalence(boolean preferenceEquivalence) {
        if (this.preferenceEquivalence != preferenceEquivalence) {
            this.cache = false;
        }

        if (properties.containsKey("L")) {
            ((Loops)properties.get("L")).setPreferenceEquivalence(preferenceEquivalence);
        }

        this.preferenceEquivalence = preferenceEquivalence;
    }

    public void setTransposeElements() {
        int N = this.getColumnDimension();

        for(int i = 0; i < N; ++i) {
            for(int j = 0; j < N; ++j) {
                double Aij = this.get(i, j);
                double Aji = this.get(j, i);
                if (Aij <= 0.0D && Aji > 0.0D) {
                    this.set(i, j, 1.0D / Aji);
                }
            }
        }

    }

    public boolean isReciprocal() {
        int m = this.getRowDimension();
        int n = this.getColumnDimension();
        if (m != n) {
            return false;
        } else {
            for(int i = 0; i < m; ++i) {
                if (this.get(i, i) != 1.0D) {
                    return false;
                }
            }

            return true;
        }
    }
}

