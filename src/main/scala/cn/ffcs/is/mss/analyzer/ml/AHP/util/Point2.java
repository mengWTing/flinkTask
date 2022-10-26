package cn.ffcs.is.mss.analyzer.ml.AHP.util;

/**
 * @author hanyu
 * @ClassName AbstractMethod
 * @date 2022/3/21 11:31
 * @description
 * @update [no][date YYYY-MM-DD][name][description]
 **/
public class Point2 {
    protected double x;
    protected double y;

    public Point2(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return this.x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return this.y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public String toString() {
        return "[" + this.x + "," + this.y + "]";
    }

    public void add(double x, double y) {
        this.x += x;
        this.y += y;
    }

    public void add(Point2 p) {
        this.add(p.x, p.y);
    }

    public boolean equals(Object o) {
        if (o instanceof Point2) {
            Point2 p = (Point2)o;
            return this.x == p.x && this.y == p.y;
        } else {
            return false;
        }
    }

    public double distance(Point2 p) {
        return Math.sqrt((this.x - p.x) * (this.x - p.x) + (this.y - p.y) * (this.y - p.y));
    }
}

