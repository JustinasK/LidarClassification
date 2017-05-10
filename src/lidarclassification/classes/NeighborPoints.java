/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lidarclassification.classes;

import java.util.ArrayList;
import lidarclassification.Ops;

/**
 *
 * @author JustinasK
 */
public class NeighborPoints {

    private Point3d center = null;
    private double distance = 0;
    private ArrayList<Point3d> neighbors = new ArrayList<>();
    private ArrayList<Double> weights = new ArrayList<>();

    public NeighborPoints(Point3d n, double m) {
        center = n;
        distance = m;
    }

    public Point3d getCenter() {
        return center;
    }

    public void setCenter(Point3d point) {
        center = point;
    }

    public ArrayList<Point3d> getNeighbors() {
        return neighbors;
    }

    public void setNeighbors(ArrayList<Point3d> list) {
        neighbors = list;
    }

    public ArrayList<Double> getWeights() {
        return weights;
    }

    public void setWeights(ArrayList<Double> list) {
        weights = list;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double n) {
        distance = n;
    }

    public Plane getPlane() {
        double x = 0;
        double y = 0;
        double z = 0;
        if (!neighbors.isEmpty()) {
            for (int i = 0; i < neighbors.size(); i++) {
                Point3d pt = new Point3d();
                pt.setX(neighbors.get(i).getX() - center.getX());
                pt.setY(neighbors.get(i).getY() - center.getY());
                pt.setZ(neighbors.get(i).getZ() - center.getZ());
                if (pt.getX() > 0) {
                    x += pt.getX();
                    y += pt.getY();
                    z += pt.getZ();
                } else {
                    x -= pt.getX();
                    y -= pt.getY();
                    z -= pt.getZ();
                }
            }
            x /= neighbors.size();
            y /= neighbors.size();
            z /= neighbors.size();
        }
        Point3d avg = new Point3d(x + center.getX(), y + center.getY(), z + center.getZ());
        return new Plane(Ops.xCos(center, avg), Ops.yCos(center, avg), Ops.zCos(center, avg), this.getDistance());
    }

    public Point3d getPlane2() {
        double x = 0;
        double y = 0;
        double z = 0;
        int size = this.getNeighbors().size();

        if (size >= 3) {
            for (int i = 0; i < size; i++) {
                Point3d a = this.getNeighbors().get(i);
                x += a.getX();
                y += a.getY();
                z += a.getZ();
            }
            x /= size;
            y /= size;
            z /= size;

            double xx = 0.0;
            double xy = 0.0;
            double xz = 0.0;
            double yy = 0.0;
            double yz = 0.0;
            double zz = 0.0;

            Point3d centroid = new Point3d(x, y, z);

            for (int i = 0; i < size; i++) {
                Point3d a = this.getNeighbors().get(i);
                x = a.getX() - this.getCenter().getX();
                y = a.getY() - this.getCenter().getY();
                z = a.getZ() - this.getCenter().getZ();
                xx += x * x;
                xy += x * y;
                xz += x * z;
                yy += y * y;
                yz += y * z;
                zz += z * z;
            }

            double detX = yy * zz + yz * yz;
            double detY = xx * zz + xz * xz;
            double detZ = yy * xx + xy * xy;
            System.out.println(detX + " " + detY + " " + detZ);

            if (detX > detY && detX > detZ) {
                double a = (xz * yz - xy * zz) / detX;
                double b = (xy * yz - xz * yy) / detX;
                System.out.println(1.0 + " " + a + " " + b);
                return new Point3d(1.0 + this.getCenter().getX(), a + this.getCenter().getY(), b + this.getCenter().getZ());
            } else if (detY > detX && detY > detZ) {
                double a = (yz * xz - xy * zz) / detY;
                double b = (xy * xz - yz * xx) / detY;
                System.out.println(1.0 + " " + a + " " + b);
                return new Point3d(a + this.getCenter().getX(), 1.0 + this.getCenter().getY(), b + this.getCenter().getZ());
            } else {
                double a = (yz * xy - xz * yy) / detZ;
                double b = (xz * xy - yz * xx) / detZ;
                System.out.println(1.0 + " " + a + " " + b);
                return new Point3d(a + this.getCenter().getX(), b + this.getCenter().getY(), 1.0 + this.getCenter().getZ());
            }
        }
        return null;
    }
}
