package cn.ffcs.is.mss.analyzer.ml.AHP.sensitivity.geom;

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
public class Area3 extends Range {
    private double ratioX = Math.sqrt(2.0D);
    private double ratioY = Math.sqrt(1.5D);
    private double sum;

    public Area3(int alternative, double sum) {
        super(alternative, 4);
        this.sum = sum;
        this.calculateRatio();
    }

    public double getSum() {
        return this.sum;
    }

    private void calculateRatio() {
        double s2 = this.sum * this.sum;
        this.ratioX = Math.sqrt(2.0D * s2) / this.sum;
        this.ratioY = Math.sqrt((s2 + 2.0D) / 2.0D) / this.sum;
    }

    public Point2 projectWeights(double[] values) {
        double x = (values[2] + 0.5D * values[1]) * this.ratioX;
        double y = values[1] * this.ratioY;
        return new Point2(x, y);
    }

    public double[] unprojectWeights(Point2 p) {
        double w2 = p.getY() / this.ratioY;
        double w3 = p.getX() / this.ratioX - 0.5D * w2;
        double w1 = this.sum - w2 - w3;
        return new double[]{w1, w2, w3};
    }

    public List<Point2> getProjectedPoints() {
        List<Point2> list = new ArrayList(this.points.size());
        Iterator var3 = this.points.iterator();

        while(var3.hasNext()) {
            Point2 p2 = (Point2)var3.next();
            Point3 p = (Point3)p2;
            Point2 projected = this.projectWeights(new double[]{p.getX(), p.getY(), p.getZ()});
            list.add(projected);
        }

        return list;
    }

    public double getMaxWidth() {
        return this.ratioX * this.sum;
    }

    public double getMaxHeight() {
        return this.ratioY * this.sum;
    }

    public double getMaxArea() {
        return this.getMaxWidth() * this.getMaxHeight() / 2.0D;
    }
}

