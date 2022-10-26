package cn.ffcs.is.mss.analyzer.ml.AHP.sensitivity.geom;

import cn.ffcs.is.mss.analyzer.ml.AHP.util.MathUtil;
import cn.ffcs.is.mss.analyzer.ml.AHP.util.Point2;
import cn.ffcs.is.mss.analyzer.ml.AHP.util.Point3;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
/**
 * @author hanyu
 * @ClassName AbstractMethod
 * @date 2022/3/21 11:31
 * @description
 * @update [no][date YYYY-MM-DD][name][description]
 **/
public abstract class Range {
    protected int alternative;
    protected int dimension;
    protected List<Point2> points;
    protected List<Point2> pointsXY;
    protected double[][] inequalities;
    private boolean invalid = true;

    public Range(int alternative, int dimension) {
        this.alternative = alternative;
        this.dimension = dimension;
        this.points = new ArrayList();
    }

    public int getAlternative() {
        return this.alternative;
    }

    public int getDimension() {
        return this.dimension;
    }

    public void setInequalities(double[][] inequalities) {
        this.inequalities = inequalities;
    }

    public double[][] getInequalities() {
        return this.inequalities;
    }

    public boolean isEmpty() {
        return this.points.isEmpty();
    }

    public void add(Point2 p) {
        if (!this.points.contains(p)) {
            this.points.add(p);
            this.invalid = true;
        }

    }

    public List<Point2> getPoints() {
        return this.points;
    }

    public List<Point2> getPointsXY() {
        if (this.invalid) {
            this.pointsXY = this.getProjectedPoints();
            MathUtil.sortPoints(this.pointsXY, this.getCenter(this.pointsXY), true);
            this.invalid = false;
        }

        return this.pointsXY;
    }

    public abstract Point2 projectWeights(double[] var1);

    public abstract double[] unprojectWeights(Point2 var1);

    public double getMaxWidth() {
        return 1.0D;
    }

    public double getMaxHeight() {
        return 1.0D;
    }

    protected abstract List<Point2> getProjectedPoints();

    private Point3 getCenter(List<Point2> list) {
        if (list.isEmpty()) {
            return null;
        } else {
            Point3 center = new Point3(0.0D, 0.0D, 0.0D);
            Iterator var4 = list.iterator();

            while(var4.hasNext()) {
                Point2 p = (Point2)var4.next();
                center.add(p);
            }

            center.setX(center.getX() / (double)list.size());
            center.setY(center.getY() / (double)list.size());
            center.setZ(center.getZ() / (double)list.size());
            return center;
        }
    }

    public abstract double getMaxArea();

    public double getArea() {
        double area = 0.0D;
        List<Point2> areaPoints = this.getPointsXY();

        for(int i = 0; i < areaPoints.size(); ++i) {
            int j = (i + 1) % areaPoints.size();
            Point2 p1 = (Point2)areaPoints.get(i);
            Point2 p2 = (Point2)areaPoints.get(j);
            area += p1.getX() * p2.getY() - p1.getY() * p2.getX();
        }

        return Math.abs(area) / 2.0D;
    }
}

