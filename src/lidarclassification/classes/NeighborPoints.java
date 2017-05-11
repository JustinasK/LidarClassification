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
        double w = 0;
        int size = this.getNeighbors().size();

        if (size >= 3) {
            for (int i = 0; i < size; i++) {
                Point3d a = this.getNeighbors().get(i);
                x += a.getX() /** weights.get(i)*/;
                y += a.getY() /** weights.get(i)*/;
                z += a.getZ() /** weights.get(i)*/;
                w += this.getWeights().get(i);
            }
            x /= w;
            y /= w;
            z /= w;
            
            Point3d centroid = new Point3d(x, y, z);
            
            x = 0;
            y = 0;
            z = 0;  
            
            for (int i = 0; i < size; i++) {
                Point3d a = this.getNeighbors().get(i);
                x += (a.getX() - centroid.getX()) * weights.get(i) + centroid.getX();
                y += (a.getY() - centroid.getX()) * weights.get(i) + centroid.getY();
                z += (a.getZ() - centroid.getX()) * weights.get(i) + centroid.getZ();
                w += this.getWeights().get(i);
            }
            x /= w;
            y /= w;
            z /= w;

            

            double xx = 0.0 ;
            double xy = 0.0;
            double xz = 0.0;
            double yy = 0.0;
            double yz = 0.0;
            double zz = 0.0;

            for (int i = 0; i < size; i++) {
                Point3d a = this.getNeighbors().get(i);
                x = a.getX() - centroid.getX();
                y = a.getY() - centroid.getY();
                z = a.getZ() - centroid.getZ();
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

            if (detX >= detY && detX >= detZ) {
                double a = (xz * yz - xy * zz) / detX;
                double b = (xy * yz - xz * yy) / detX;
                Point3d dir = new Point3d(1.0, a, b);
                return new Plane(Ops.xCos(dir), Ops.yCos(dir), Ops.zCos(dir), 0 * Ops.xCos(dir) + 0 * Ops.yCos(dir) + 0 * Ops.zCos(dir));
            } else if (detY >= detX && detY >= detZ) {
                double a = (yz * xz - xy * zz) / detY;
                double b = (xy * xz - yz * xx) / detY;
                Point3d dir = new Point3d(a, 1.0, b);
                return new Plane(Ops.xCos(dir), Ops.yCos(dir), Ops.zCos(dir), 0 * Ops.xCos(dir) + 0 * Ops.yCos(dir) + 0 * Ops.zCos(dir));
            } else {
                double a = (yz * xy - xz * yy) / detZ;
                double b = (xz * xy - yz * xx) / detZ;
                Point3d dir = new Point3d(a, b, 1.0);
                return new Plane(Ops.xCos(dir), Ops.yCos(dir), Ops.zCos(dir), 0 * Ops.xCos(dir) + 0 * Ops.yCos(dir) + 0 * Ops.zCos(dir));
            }
        }
        return null;
    }
}
