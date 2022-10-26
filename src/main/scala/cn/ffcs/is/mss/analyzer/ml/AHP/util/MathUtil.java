package cn.ffcs.is.mss.analyzer.ml.AHP.util;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
/**
 * @author hanyu
 * @ClassName AbstractMethod
 * @date 2022/3/21 11:31
 * @description
 * @update [no][date YYYY-MM-DD][name][description]
 **/
public class MathUtil {
    private static final double EPSILON = 1.0E-9D;

    public MathUtil() {
    }

    public static boolean equals(double a, double b) {
        return a == b ? true : Math.abs(a - b) < 1.0E-9D;
    }

    public static boolean equals(double a, double b, double epsilon) {
        return a == b ? true : Math.abs(a - b) < epsilon;
    }

    public static double[] normalise(double[] vector) {
        double[] result = new double[vector.length];
        double sum = absSum(vector);
        if (sum > 0.0D) {
            for(int i = 0; i < vector.length; ++i) {
                result[i] = Math.abs(vector[i]) / sum;
            }
        }

        return result;
    }

    public static double absSum(double[] values) {
        double sum = 0.0D;

        for(int i = 0; i < values.length; ++i) {
            sum += Math.abs(values[i]);
        }

        return sum;
    }

    public static double sum(double[] values) {
        double sum = 0.0D;

        for(int i = 0; i < values.length; ++i) {
            sum += values[i];
        }

        return sum;
    }

    public static int sum(int[] values) {
        int sum = 0;

        for(int i = 0; i < values.length; ++i) {
            sum += values[i];
        }

        return sum;
    }

    public static double mean(double[] values) {
        return sum(values) / (double)values.length;
    }

    public static double mean(int[] values) {
        return (double)sum(values) / (double)values.length;
    }

    public static double multiply(double[] values) {
        double mult = 1.0D;

        for(int i = 0; i < values.length; ++i) {
            mult *= values[i];
        }

        return mult;
    }

    public static int fact(int n) {
        return n <= 1 ? 1 : n * fact(n - 1);
    }

    public static double standardDevS(double[] values, double mean) {
        double stdev = 0.0D;

        for(int i = 0; i < values.length; ++i) {
            stdev += Math.pow(mean - values[i], 2.0D);
        }

        stdev = Math.sqrt(stdev) / (double)(values.length - 1);
        return stdev;
    }

    public static double linearFn(double x1, double y1, double x2, double y2, double x) {
        return (y2 - y1) * (x - x1) / (x2 - x1) + y1;
    }

    public static Point2 lineIntersection(double x11, double y11, double x12, double y12, double x21, double y21, double x22, double y22) {
        double m1 = (y12 - y11) / (x12 - x11);
        double m2 = (y22 - y21) / (x22 - x21);
        if (m1 == m2) {
            return null;
        } else {
            double x = (m2 * x21 - m1 * x11 + y11 - y21) / (m2 - m1);
            if (x >= Math.min(x11, x12) && x >= Math.min(x21, x22) && x <= Math.max(x11, x12) && x <= Math.max(x21, x22)) {
                double y = m1 * (x - x11) + y11;
                return new Point2(x, y);
            } else {
                return null;
            }
        }
    }

    public static Point2 lineIntersection(double a1, double b1, double c1, double a2, double b2, double c2) {
        double det;
        if ((a1 != 0.0D || a2 != 0.0D) && (b1 != 0.0D || b2 != 0.0D) && (det = a1 * b2 - a2 * b1) != 0.0D) {
            double y;
            double x;
            if (a1 != 0.0D) {
                y = (c1 * a2 - c2 * a1) / det;
                x = -(c1 + b1 * y) / a1;
            } else {
                y = -(c2 * a1 - c1 * a2) / det;
                x = -(c2 + b2 * y) / a2;
            }

            return new Point2(x, y);
        } else {
            return null;
        }
    }

    public static void main(String[] a) {
        double[] d1 = planeIntersection(-1.0D, -1.0D, -3.0D, 2.0D, 0.0D);
        double[] d2 = planeIntersection(-1.0D, -1.0D, -3.0D, 2.0D, 1.0D, 1.0D, 1.0D, 0.0D);
        int i;
        if (d1 != null) {
            for(i = 0; i < d1.length; ++i) {
                System.out.println(d1[1]);
            }
        }

        System.out.println();
        if (d2 != null) {
            for(i = 0; i < d2.length; ++i) {
                System.out.println(d2[i]);
            }
        }

    }

    public static Point3 lineIntersection(double[] vector1, double[] vector2) {
        double x10 = vector1[0];
        double y10 = vector1[1];
        double z10 = vector1[2];
        double i1 = vector1[3];
        double j1 = vector1[4];
        double k1 = vector1[5];
        double x20 = vector2[0];
        double y20 = vector2[1];
        double z20 = vector2[2];
        double i2 = vector2[3];
        double j2 = vector2[4];
        double k2 = vector2[5];
        double det = i1 * j2 - j1 * i2;
        if (det == 0.0D) {
            return null;
        } else {
            double t2 = (i1 * (y10 - y20) + j1 * (x20 - x10)) / det;
            double x = x20 + i2 * t2;
            double y = y20 + j2 * t2;
            double z = z20 + k2 * t2;
            return new Point3(x, y, z);
        }
    }

    public static Point3 linePlaneIntersection(double[] vector, double a, double b, double c, double d) {
        double x = vector[0];
        double y = vector[1];
        double z = vector[2];
        double i = vector[3];
        double j = vector[4];
        double k = vector[5];
        double det = a * i + b * j + c * k;
        if (det == 0.0D) {
            return null;
        } else {
            double t = -(a * x + b * y + c * z - d) / det;
            return new Point3(x + i * t, y + j * t, z + k * t);
        }
    }

    public static double[] planeIntersection(double a1, double b1, double c1, double d1, double plane2) {
        if (c1 - b1 != 0.0D && c1 != 0.0D) {
            double i = b1 - c1;
            double j = -(a1 - c1);
            double k = a1 - b1;
            double x0 = 0.5D;
            double y0 = (plane2 * c1 - d1 + x0 * (a1 - c1)) / (c1 - b1);
            double z0 = (d1 - a1 * x0 - b1 * y0) / c1;
            return new double[]{x0, y0, z0, i, j, k};
        } else {
            return null;
        }
    }

    public static double[] planeIntersection(double a1, double b1, double c1, double d1, double a2, double b2, double c2, double d2) {
        Point2 p = lineIntersection(a1, b1, -d1, a2, b2, -d2);
        double x0;
        double y0;
        double z0;
        if (p == null) {
            p = lineIntersection(a1, c1, -d1, a2, c2, -d2);
            if (p == null) {
                p = lineIntersection(b1, c1, -d1, b2, c2, -d2);
                if (p == null) {
                    return null;
                }

                x0 = 0.0D;
                y0 = p.getX();
                z0 = p.getY();
            } else {
                x0 = p.getX();
                y0 = 0.0D;
                z0 = p.getY();
            }
        } else {
            x0 = p.getX();
            y0 = p.getY();
            z0 = 0.0D;
        }

        double i = b1 * c2 - b2 * c1;
        double j = -(a1 * c2 - a2 * c1);
        double k = a1 * b2 - a2 * b1;
        return new double[]{x0, y0, z0, i, j, k};
    }

    public static double distance(Point2 p, Point2 p1, Point2 p2) {
        if (p1.equals(p2)) {
            return p.distance(p1);
        } else {
            double d2 = p1.distance(p2);
            d2 *= d2;
            double t = ((p.getX() - p1.getX()) * (p2.getX() - p1.getX()) + (p.getY() - p1.getY()) * (p2.getY() - p1.getY())) / d2;
            if (t < 0.0D) {
                return p.distance(p1);
            } else if (t > 1.0D) {
                return p.distance(p2);
            } else {
                Point2 projection = new Point2(p1.getX(), p1.getY());
                projection.add(t * (p2.getX() - p1.getX()), t * (p2.getY() - p1.getY()));
                return p.distance(projection);
            }
        }
    }

    public static void sortPoints(List<Point2> points, Point2 reference, boolean clockwise) {
        PointAngleComparator comp = new PointAngleComparator(reference, clockwise);
        Collections.sort(points, comp);
    }

    private static class PointAngleComparator implements Comparator<Point2> {
        Point2 center;
        int s;

        public PointAngleComparator(Point2 center, boolean clockwise) {
            this.center = center;
            this.s = clockwise ? 1 : -1;
        }

        public int compare(Point2 o1, Point2 o2) {
            double atan1 = Math.atan2(o1.getY() - this.center.getY(), o1.getX() - this.center.getX());
            double atan2 = Math.atan2(o2.getY() - this.center.getY(), o2.getX() - this.center.getX());
            if (atan1 == atan2) {
                return 0;
            } else {
                return atan1 < atan2 ? this.s : -this.s;
            }
        }
    }
}

