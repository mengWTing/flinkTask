package cn.ffcs.is.mss.analyzer.ml.AHP.sensitivity.geom;

import cn.ffcs.is.mss.analyzer.ml.AHP.util.Point2;

import java.util.List;
/**
 * @author hanyu
 * @ClassName AbstractMethod
 * @date 2022/3/21 11:31
 * @description
 * @update [no][date YYYY-MM-DD][name][description]
 **/
public class Area2 extends Range {
    public Area2(int alternative) {
        super(alternative, 2);
    }

    public List<Point2> getProjectedPoints() {
        return this.getPoints();
    }

    public Point2 projectWeights(double[] values) {
        return new Point2(values[0], values[1]);
    }

    public double[] unprojectWeights(Point2 p) {
        return new double[]{p.getX(), p.getY()};
    }

    public double getMaxArea() {
        return 0.5D;
    }
}

