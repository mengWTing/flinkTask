package cn.ffcs.is.mss.analyzer.ml.AHP.sensitivity;

import cn.ffcs.is.mss.analyzer.ml.AHP.core.Criterion;
import cn.ffcs.is.mss.analyzer.ml.AHP.core.DecisionProblem;
import cn.ffcs.is.mss.analyzer.ml.AHP.core.Result;
import cn.ffcs.is.mss.analyzer.ml.AHP.core.ResultList;
import cn.ffcs.is.mss.analyzer.ml.AHP.method.AbstractMethod;
import cn.ffcs.is.mss.analyzer.ml.AHP.sensitivity.geom.Area1;
import cn.ffcs.is.mss.analyzer.ml.AHP.sensitivity.geom.Area2;
import cn.ffcs.is.mss.analyzer.ml.AHP.sensitivity.geom.Area3;
import cn.ffcs.is.mss.analyzer.ml.AHP.sensitivity.geom.Range;
import cn.ffcs.is.mss.analyzer.ml.AHP.util.MathUtil;
import cn.ffcs.is.mss.analyzer.ml.AHP.util.Point2;
import cn.ffcs.is.mss.analyzer.ml.AHP.util.Point3;
import cn.ffcs.is.mss.analyzer.ml.AHP.util.SortedItem;

import java.util.*;
/**
 * @author hanyu
 * @ClassName AbstractMethod
 * @date 2022/3/21 11:31
 * @description
 * @update [no][date YYYY-MM-DD][name][description]
 **/
public class SensitivityDomain {
    private MathematicalSensitivityAnalyzer analyzer;
    private DecisionProblem problem;
    private AbstractMethod method;
    private Range[] topRankedArea;
    private List<RankReversalSegment>[] allReversal;
    private Criterion criterion;
    private int[] child;
    private double[][] data;
    private Point2 currentWeightXY;
    private double[] currentWeights;
    int index = 0;
    private double weightsSum = -1.0D;
    private boolean isCalculated = false;
    private int topAlternative;
    private double opsc;
    private double tsc;
    private transient double[][] cachedDomainInequalities;

    public SensitivityDomain(MathematicalSensitivityAnalyzer sa, Criterion c, int[] child, DecisionProblem problem, AbstractMethod method, double[][] data) {
        this.analyzer = sa;
        this.topRankedArea = new Range[problem.getAlternativeCount()];
        this.criterion = c;
        this.child = child;
        this.problem = problem;
        this.method = method;
        this.data = data;
    }

    private void checkComputeAreas() {
        if (!this.isCalculated) {
            this.cachedDomainInequalities = this.getDomainInequalities(this.data);
            this.topAlternative = this.getTopAlternative();
            int count = this.child.length;
            this.allReversal = new List[this.topRankedArea.length];

            for(int i = 0; i < this.topRankedArea.length; ++i) {
                Object domain;
                if (count == 1) {
                    domain = this.computeArea1D(i);
                } else if (count == 2) {
                    domain = this.computeArea2D(i);
                } else if (count == 3) {
                    domain = this.computeArea3D(i, this.weightsSum);
                } else {
                    domain = this.computeAreaN(i, 1.0D);
                }

                this.topRankedArea[i] = (Range)domain;
                if (domain != null) {
                    this.index = i;
                }
            }

            this.isCalculated = true;
        }
    }

    private int getTopAlternative() {
        ResultList results = this.criterion.getResult(this.method);
        double[] rnk;
        if (results == null) {
            rnk = this.problem.getRanking(this.method);
        } else {
            double[] criterionWeights = ((Result)results.get(0)).getWeights();
            double[] newWeights = new double[criterionWeights.length];
            Arrays.fill(newWeights, -1.0D);
            double sum = 0.0D;

            int i;
            for(i = 0; i < this.child.length; ++i) {
                sum += criterionWeights[this.child[i]];
            }

            if (this.weightsSum == -1.0D) {
                rnk = this.problem.getRanking(this.method);
            } else {
                for(i = 0; i < this.child.length; ++i) {
                    newWeights[this.child[i]] = criterionWeights[this.child[i]] * this.weightsSum / sum;
                }

                rnk = this.analyzer.getRanking(this.criterion, newWeights);
            }
        }

        SortedItem[] item = SortedItem.sort(rnk);
        return item[0].getIndex();
    }

    public void setWeightsSum(double sum) {
        if (!(sum < 0.0D) && !(sum > 1.0D)) {
            this.weightsSum = Math.max(sum, 1.0E-4D);
        } else {
            throw new IllegalArgumentException("Invalid argument sum must be 0 <= sum <= 1");
        }
    }

    public double getWeightsSum() {
        return this.weightsSum;
    }

    public void update() {
        this.isCalculated = false;
    }

    public Criterion getCriterion() {
        return this.criterion;
    }

    public int getDimension() {
        return this.child.length;
    }

    public int[] getChildren() {
        return this.child;
    }

    public Point2 getCurrentWeightXY() {
        this.checkComputeAreas();
        return this.currentWeightXY;
    }

    public double[] getCurrentWeights() {
        this.checkComputeAreas();
        return this.currentWeights;
    }

    public List<RankReversalSegment> getAllReversals(int alternative) {
        this.checkComputeAreas();
        return this.allReversal[alternative];
    }

    public List<RankReversalSegment> getAllReversalsXY() {
        this.checkComputeAreas();
        if (this.topRankedArea[this.index] == null) {
            return null;
        } else {
            List<RankReversalSegment> list = new ArrayList();

            for(int alternative = 0; alternative < this.getAlternativeCount(); ++alternative) {
                Iterator var4 = this.allReversal[alternative].iterator();

                while(var4.hasNext()) {
                    RankReversalSegment pts = (RankReversalSegment)var4.next();
                    if (pts.getPoint1() != null && pts.getPoint2() != null) {
                        double[] values1 = new double[this.getDimension()];
                        double[] values2 = new double[this.getDimension()];
                        values1[0] = pts.getPoint1().getX();
                        values2[0] = pts.getPoint2().getX();
                        if (values1.length > 1) {
                            values1[1] = pts.getPoint1().getY();
                            values2[1] = pts.getPoint2().getY();
                            if (values1.length > 2) {
                                values1[2] = ((Point3)pts.getPoint1()).getZ();
                                values2[2] = ((Point3)pts.getPoint2()).getZ();
                            }
                        }

                        Point2 p1 = this.topRankedArea[this.index].projectWeights(values1);
                        Point2 p2 = this.topRankedArea[this.index].projectWeights(values2);
                        if (values1.length == 1) {
                            p2.add(0.0D, 1.0D);
                        }

                        RankReversalSegment segment = new RankReversalSegment(pts.alternative1, pts.alternative2, p1, p2);
                        if (!list.contains(segment)) {
                            list.add(segment);
                        }
                    }
                }
            }

            return list;
        }
    }

    public double getOPSC() {
        this.checkComputeAreas();
        return this.opsc;
    }

    public double getTSC() {
        this.checkComputeAreas();
        return this.tsc;
    }

    public double getSC() {
        return 1.0D / this.getTSC();
    }

    public double getReversalProbability() {
        return 1.0D - this.getTSC();
    }

    public double getWidthXY() {
        this.checkComputeAreas();
        return this.topRankedArea[this.index] != null ? this.topRankedArea[this.index].getMaxWidth() : -1.0D;
    }

    public double getHeightXY() {
        this.checkComputeAreas();
        return this.topRankedArea[this.index] != null ? this.topRankedArea[this.index].getMaxHeight() : -1.0D;
    }

    public double[] getWeights(double x, double y) {
        this.checkComputeAreas();
        return this.topRankedArea[this.index] != null ? this.topRankedArea[this.index].unprojectWeights(new Point2(x, y)) : null;
    }

    public Point2 getXY(double[] weights) {
        this.checkComputeAreas();
        return this.topRankedArea[this.index] != null ? this.topRankedArea[this.index].projectWeights(weights) : null;
    }

    public int getAlternativeCount() {
        this.checkComputeAreas();
        return this.topRankedArea.length;
    }

    public Range getAlternativeDomain(int alternative) {
        this.checkComputeAreas();
        return this.topRankedArea[alternative];
    }

    public Range getAlternativeDomain(int alternative, double sum) {
        this.checkComputeAreas();
        if (this.child.length == 3) {
            if (!(sum < 0.0D) && !(sum > 1.0D)) {
                return this.computeArea3D(alternative, sum);
            } else {
                throw new IllegalArgumentException("Invalid argument sum must be 0 <= sum <= 1");
            }
        } else {
            return this.getAlternativeDomain(alternative);
        }
    }

    public double[][] getDomainInequalities() {
        this.checkComputeAreas();
        return this.cachedDomainInequalities != null ? this.cachedDomainInequalities : this.getDomainInequalities(this.data);
    }

    public double[][] getRawData() {
        this.checkComputeAreas();
        return this.data;
    }

    private double[][] getAlternativeInequalities(int alternative, double[][] data) {
        int dimension = data[0].length - 2;
        int start = dimension * 2 + 2;
        int pos = start - 1;
        int n = data.length - start;
        n = (int)((1.0D + Math.sqrt((double)(1 + 8 * n))) / 2.0D);
        double[][] inequality = new double[n - 1][dimension + 1];
        int m = 0;

        for(int i = 0; i < n - 1; ++i) {
            for(int j = i + 1; j < n; ++j) {
                ++pos;
                if (i == alternative || j == alternative) {
                    double sign = (double)(alternative == i ? 1 : -1);

                    for(int k = 0; k < dimension; ++k) {
                        inequality[m][k] = sign * data[pos][k];
                    }

                    inequality[m][dimension] = sign * data[pos][dimension + 1];
                    ++m;
                }
            }
        }

        return inequality;
    }

    private double[][] getDomainInequalities(double[][] data) {
        int dimension = data[0].length - 2;
        int end = dimension * 2 + 2;
        double[][] inequality = new double[end][dimension + 1];

        for(int i = 0; i < end; ++i) {
            double sign = (double)(data[i][dimension] > 0.0D ? -1 : 1);

            for(int j = 0; j < dimension; ++j) {
                inequality[i][j] = sign * data[i][j];
            }

            inequality[i][dimension] = sign * data[i][dimension + 1];
        }

        return inequality;
    }

    private boolean evaluate(double[][] inequality, double[] values) {
        for(int m = 0; m < inequality.length; ++m) {
            double sum = 0.0D;

            for(int i = 0; i < inequality[m].length - 1; ++i) {
                sum += inequality[m][i] * values[i];
            }

            sum -= inequality[m][inequality[m].length - 1];
            if (!MathUtil.equals(sum, 0.0D) && sum > 0.0D) {
                return false;
            }
        }

        return true;
    }

    public Area1 computeArea1D(int alternative) {
        double[][] inequality = this.getAlternativeInequalities(alternative, this.data);
        double[][] domain;
        if (this.cachedDomainInequalities != null) {
            domain = this.cachedDomainInequalities;
        } else {
            domain = this.getDomainInequalities(this.data);
        }

        Area1 area = new Area1(alternative);
        area.setInequalities(inequality);
        double x0 = domain[0][1];
        double[] x = new double[1];
        HashMap<Integer, RankReversalSegment> map = new HashMap();

        for(int i = 0; i < inequality.length; ++i) {
            if (!MathUtil.equals(inequality[i][0], 0.0D)) {
                x[0] = inequality[i][1] / inequality[i][0];
                boolean valid = this.evaluate(domain, x);
                if (valid) {
                    Point2 px = new Point2(x[0] + x0, 0.0D);
                    valid = this.evaluate(inequality, x);
                    if (valid) {
                        area.add(px);
                    }

                    if (alternative != i && i != this.topAlternative && alternative != this.topAlternative) {
                        this.addPointToSegment(map, alternative, i, px);
                        this.addPointToSegment(map, alternative, i, new Point2(px.getX(), 0.0D));
                    }
                }
            }
        }

        double[][] borderPoints = new double[][]{{-domain[0][1]}, {domain[1][1]}};
        double[][] var13 = borderPoints;
        int var12 = borderPoints.length;

        for(int var17 = 0; var17 < var12; ++var17) {
            double[] xp = var13[var17];
            boolean valid = this.evaluate(inequality, xp);
            if (valid) {
                valid = this.evaluate(domain, xp);
                if (valid) {
                    area.add(new Point2(xp[0] + x0, 0.0D));
                }
            }
        }

        if (area.getPoints().size() < 2) {
            area.getPoints().clear();
        }

        this.currentWeights = new double[]{x0};
        this.currentWeightXY = new Point2(x0, 0.5D);
        this.weightsSum = 1.0D;
        if (alternative == this.topAlternative) {
            this.opsc = this.calculateOpsc(area);
            this.tsc = area.getArea() / area.getMaxArea();
        }

        this.allReversal[alternative] = new ArrayList();
        this.allReversal[alternative].addAll(map.values());
        return area;
    }

    private double calculateOpsc(Range area) {
        double min = 1.7976931348623157E308D;
        List<Point2> points = area.getPointsXY();

        for(int i = 0; i < points.size(); ++i) {
            int j = (i + 1) % points.size();
            Point2 p1 = (Point2)points.get(i);
            Point2 p2 = (Point2)points.get(j);
            double[] w1 = area.unprojectWeights(p1);
            double[] w2 = area.unprojectWeights(p2);
            double s1 = 0.0D;
            double s2 = 0.0D;
            boolean ignore = false;

            for(int k = 0; k < w1.length; ++k) {
                s1 += w1[k];
                s2 += w2[k];
                if (MathUtil.equals(w1[k], 0.0D) && MathUtil.equals(w2[k], 0.0D)) {
                    ignore = true;
                    break;
                }
            }

            if (!ignore && this.getDimension() < 3) {
                ignore = MathUtil.equals(s1, 1.0D) && MathUtil.equals(s2, 1.0D);
                if (area.getDimension() == 1) {
                    ignore = ignore || MathUtil.equals(s1, 1.0D) && MathUtil.equals(s2, 0.0D) || MathUtil.equals(s1, 0.0D) && MathUtil.equals(s2, 1.0D);
                }
            }

            if (!ignore) {
                min = Math.min(min, MathUtil.distance(this.currentWeightXY, p1, p2));
            }
        }

        return min == 1.7976931348623157E308D ? 0.0D : min;
    }

    public Area2 computeArea2D(int alternative) {
        double[][] inequality = this.getAlternativeInequalities(alternative, this.data);
        double[][] domain;
        if (this.cachedDomainInequalities != null) {
            domain = this.cachedDomainInequalities;
        } else {
            domain = this.getDomainInequalities(this.data);
        }

        Area2 area = new Area2(alternative);
        area.setInequalities(inequality);
        int m = domain[0].length - 1;
        double x0 = -domain[0][m];
        double y0 = -domain[2][m];

        int i;
        for(i = 0; i < inequality.length - 1; ++i) {
            for(i = i + 1; i < inequality.length; ++i) {
                Point2 xy = MathUtil.lineIntersection(inequality[i][0], inequality[i][1], -inequality[i][2], inequality[i][0], inequality[i][1], -inequality[i][2]);
                if (xy != null) {
                    double[] coords = new double[]{xy.getX(), xy.getY()};
                    boolean valid = this.evaluate(inequality, coords);
                    if (valid) {
                        valid = this.evaluate(domain, coords);
                        if (valid) {
                            xy.add(-x0, -y0);
                            area.add(xy);
                        }
                    }
                }
            }
        }

        HashMap<Integer, RankReversalSegment> map = new HashMap();

        double[] xy;
        for(i = 0; i < inequality.length; ++i) {
            for(int j = 0; j < domain.length; ++j) {
                Point2 xy1 = MathUtil.lineIntersection(inequality[i][0], inequality[i][1], -inequality[i][2], domain[j][0], domain[j][1], -domain[j][2]);
                if (xy1 != null) {
                    xy = new double[]{xy1.getX(), xy1.getY()};
                    boolean valid = this.evaluate(domain, xy);
                    if (valid) {
                        valid = this.evaluate(inequality, xy);
                        xy1.add(-x0, -y0);
                        if (valid) {
                            area.add(xy1);
                        }

                        if (alternative != i && i != this.topAlternative && alternative != this.topAlternative) {
                            this.addPointToSegment(map, alternative, i, xy1);
                        }
                    }
                }
            }
        }

        double f = domain[5][m];
        double[][] borderPoints = new double[][]{{x0, y0}, {x0, f - x0}, {f - y0, y0}};
        double[][] var17 = borderPoints;
        int var16 = borderPoints.length;

        for(int var25 = 0; var25 < var16; ++var25) {
            xy = var17[var25];
            boolean valid = this.evaluate(inequality, xy);
            if (valid) {
                valid = this.evaluate(domain, xy);
                if (valid) {
                    area.add(new Point2(xy[0] - x0, xy[1] - y0));
                }
            }
        }

        if (area.getPoints().size() < 3) {
            area.getPoints().clear();
        }

        this.currentWeights = new double[]{-x0, -y0};
        this.currentWeightXY = new Point2(-x0, -y0);
        this.weightsSum = 1.0D;
        if (alternative == this.topAlternative) {
            this.opsc = this.calculateOpsc(area);
            this.tsc = area.getArea() / area.getMaxArea();
        }

        this.allReversal[alternative] = new ArrayList();
        this.allReversal[alternative].addAll(map.values());
        return area;
    }

    public Area3 computeArea3D(int alternative, double sum) {
        double[][] inequality = this.getAlternativeInequalities(alternative, this.data);
        double[][] domain;
        if (this.cachedDomainInequalities != null) {
            domain = this.cachedDomainInequalities;
        } else {
            domain = this.getDomainInequalities(this.data);
        }

        int m = domain[0].length - 1;
        double x0 = -domain[0][m];
        double y0 = -domain[2][m];
        double z0 = -domain[4][m];
        if (sum == -1.0D) {
            sum = -(x0 + y0 + z0);
            this.weightsSum = sum;
        }

        double plane2 = sum - domain[6][m];
        double[][] lineVector = new double[inequality.length][];
        double[] NULL_VECTOR = new double[0];
        Area3 area = new Area3(alternative, sum);
        area.setInequalities(inequality);

        int i;
        for(i = 0; i < inequality.length - 1; ++i) {
            for(i = i + 1; i < inequality.length; ++i) {
                if (lineVector[i] == null) {
                    lineVector[i] = MathUtil.planeIntersection(inequality[i][0], inequality[i][1], inequality[i][2], inequality[i][3], 1.0D, 1.0D, 1.0D, plane2);
                    if (lineVector[i] == null) {
                        lineVector[i] = NULL_VECTOR;
                    }
                }

                if (lineVector[i] == null) {
                    lineVector[i] = MathUtil.planeIntersection(inequality[i][0], inequality[i][1], inequality[i][2], inequality[i][3], 1.0D, 1.0D, 1.0D, plane2);
                    if (lineVector[i] == null) {
                        lineVector[i] = NULL_VECTOR;
                    }
                }

                if (lineVector[i] != NULL_VECTOR && lineVector[i] != NULL_VECTOR) {
                    Point3 xyz = MathUtil.lineIntersection(lineVector[i], lineVector[i]);
                    if (xyz != null) {
                        double[] coords = new double[]{xyz.getX(), xyz.getY(), xyz.getZ()};
                        boolean valid = this.evaluate(inequality, coords);
                        if (valid) {
                            valid = this.evaluate(domain, coords);
                            if (valid) {
                                xyz.add(-x0, -y0, -z0);
                                area.add(xyz);
                            }
                        }
                    }
                }
            }
        }

        HashMap<Integer, RankReversalSegment> map = new HashMap();

        double[] coords;
        for(i = 0; i < inequality.length; ++i) {
            for(int j = 0; j < domain.length - 2; ++j) {
                if (lineVector[i] != NULL_VECTOR) {
                    Point3 xyz = MathUtil.linePlaneIntersection(lineVector[i], domain[j][0], domain[j][1], domain[j][2], domain[j][3]);
                    if (xyz != null) {
                        coords = new double[]{xyz.getX(), xyz.getY(), xyz.getZ()};
                        boolean valid = this.evaluate(domain, coords);
                        if (valid) {
                            valid = this.evaluate(inequality, coords);
                            xyz.add(-x0, -y0, -z0);
                            if (valid) {
                                area.add(xyz);
                            }

                            if (alternative != i && i != this.topAlternative && alternative != this.topAlternative) {
                                this.addPointToSegment(map, alternative, i, xyz);
                            }
                        }
                    }
                }
            }
        }

        double[][] borderPoints = new double[][]{{x0, y0, plane2 - x0 - y0}, {x0, plane2 - x0 - z0, z0}, {plane2 - y0 - z0, y0, z0}};
        double[][] var25 = borderPoints;
        int var24 = borderPoints.length;

        for(int var33 = 0; var33 < var24; ++var33) {
            coords = var25[var33];
            boolean valid = this.evaluate(inequality, coords);
            if (valid) {
                valid = this.evaluate(domain, coords);
                if (valid) {
                    area.add(new Point3(coords[0] - x0, coords[1] - y0, coords[2] - z0));
                }
            }
        }

        if (area.getPoints().size() < 3) {
            area.getPoints().clear();
        }

        double adjustment = -sum / (x0 + y0 + z0);
        this.currentWeights = new double[]{-x0 * adjustment, -y0 * adjustment, -z0 * adjustment};
        this.currentWeightXY = area.projectWeights(this.currentWeights);
        if (alternative == this.topAlternative) {
            this.opsc = this.calculateOpsc(area);
            this.tsc = area.getArea() / area.getMaxArea();
        }

        this.allReversal[alternative] = new ArrayList();
        this.allReversal[alternative].addAll(map.values());
        return area;
    }

    public Area3 computeAreaN(int alternative, double sum) {
        double[][] inequality = this.getAlternativeInequalities(alternative, this.data);
        Area3 area = new Area3(alternative, sum);
        area.setInequalities(inequality);
        this.allReversal[alternative] = new ArrayList(0);
        this.currentWeights = null;
        this.currentWeightXY = null;
        return area;
    }

    private void addPointToSegment(HashMap<Integer, RankReversalSegment> map, int alternative, int segment, Point2 p) {
        RankReversalSegment s = (RankReversalSegment)map.get(segment);
        if (s == null) {
            s = new RankReversalSegment(alternative, segment);
            s.setPoint1(p);
            map.put(segment, s);
        } else {
            s.setPoint2(p);
        }

    }

    public MathematicalSensitivityAnalyzer getAnalyzer() {
        return this.analyzer;
    }

    public DecisionProblem getProblem() {
        return this.problem;
    }

    public AbstractMethod getMethod() {
        return this.method;
    }
}
