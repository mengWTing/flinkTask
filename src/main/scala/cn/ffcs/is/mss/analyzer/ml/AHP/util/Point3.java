package cn.ffcs.is.mss.analyzer.ml.AHP.util;

/**
 * @author hanyu
 * @ClassName AbstractMethod
 * @date 2022/3/21 11:31
 * @description
 * @update [no][date YYYY-MM-DD][name][description]
 **/
public class Point3 extends Point2 {
    private double z;

    public Point3(double x, double y, double z) {
        super(x, y);
        this.z = z;
    }

    public double getZ() {
        return this.z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public String toString() {
        return "[" + this.x + "," + this.y + "," + this.z + "]";
    }

    public void add(double x, double y, double z) {
        this.add(x, y);
        this.z += z;
    }

    public void add(Point3 p) {
        this.add(p.x, p.y, p.z);
    }

    public boolean equals(Object o) {
        if (o instanceof Point3) {
            Point3 p = (Point3)o;
            return this.x == p.x && this.y == p.y && this.z == p.z;
        } else {
            return false;
        }
    }

    public double distance(Point3 p) {
        return Math.sqrt((this.x - p.x) * (this.x - p.x) + (this.y - p.y) * (this.y - p.y) + (this.z - p.z) * (this.z - p.z));
    }
}

