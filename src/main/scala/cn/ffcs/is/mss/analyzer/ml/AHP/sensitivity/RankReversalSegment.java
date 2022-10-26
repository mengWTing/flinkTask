package cn.ffcs.is.mss.analyzer.ml.AHP.sensitivity;

import cn.ffcs.is.mss.analyzer.ml.AHP.util.Point2;
/**
 * @author hanyu
 * @ClassName AbstractMethod
 * @date 2022/3/21 11:31
 * @description
 * @update [no][date YYYY-MM-DD][name][description]
 **/
public class RankReversalSegment {
    int alternative1;
    int alternative2;
    Point2 p1;
    Point2 p2;

    protected RankReversalSegment(int alternative1, int alternative2) {
        this.alternative1 = alternative1;
        this.alternative2 = alternative2;
    }

    protected RankReversalSegment(int alternative1, int alternative2, Point2 p1, Point2 p2) {
        this.alternative1 = alternative1;
        this.alternative2 = alternative2;
        this.p1 = p1;
        this.p2 = p2;
    }

    protected void setPoint1(Point2 p) {
        this.p1 = p;
    }

    protected void setPoint2(Point2 p) {
        this.p2 = p;
    }

    public Point2 getPoint1() {
        return this.p1;
    }

    public Point2 getPoint2() {
        return this.p2;
    }

    public int getAlternative1() {
        return this.alternative1;
    }

    public int getAlternative2() {
        return this.alternative2;
    }

    public boolean equals(Object o) {
        if (!(o instanceof RankReversalSegment)) {
            return false;
        } else {
            RankReversalSegment s = (RankReversalSegment)o;
            return this.p1.equals(s.p1) && this.p2.equals(s.p2);
        }
    }
}

