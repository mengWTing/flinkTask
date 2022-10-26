package cn.ffcs.is.mss.analyzer.ml.AHP.sensitivity.geom;


import cn.ffcs.is.mss.analyzer.ml.AHP.util.Point2;

import java.util.ArrayList;
import java.util.List;
/**
 * @author hanyu
 * @ClassName AbstractMethod
 * @date 2022/3/21 11:31
 * @description
 * @update [no][date YYYY-MM-DD][name][description]
 **/
public class Area1 extends Range {
    public Area1(int alternative) {
        super(alternative, 1);
    }

    public List<Point2> getProjectedPoints() {
        List<Point2> list = new ArrayList(this.points.size() + 2);
        Point2 p = (Point2)this.points.get(0);
        list.add(p);
        list.add(new Point2(p.getX(), 1.0D));
        p = (Point2)this.points.get(1);
        list.add(new Point2(p.getX(), 1.0D));
        list.add(p);
        return list;
    }

    public Point2 projectWeights(double[] values) {
        return new Point2(values[0], 0.0D);
    }

    public double[] unprojectWeights(Point2 p) {
        return new double[]{p.getX()};
    }

    public double getMaxArea() {
        return 1.0D;
    }
}

