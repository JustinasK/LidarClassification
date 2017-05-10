/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lidarclassification.classes;

/**
 *
 * @author JustinasK
 */
public class Plane {

    private Point3d vector = new Point3d();
    private double distance = 0;

    public void setVector(Point3d x) {
        vector = x;
    }

    public void setVector(double x, double y, double z) {
        vector.setX(x);
        vector.setY(y);
        vector.setZ(z);
    }

    public Point3d getVector() {
        return vector;
    }

    public void setDistance(double x) {
        distance = x;
    }

    public double getDistance() {
        return distance;
    }

    public Plane(double a, double b, double c, double d) {
        vector.setX(a);
        vector.setY(b);
        vector.setZ(c);
        distance = d;
    }

    public void setX(double x) {
        vector.setX(x);
    }

    public double getX() {
        return vector.getX();
    }

    public void setY(double y) {
        vector.setY(y);
    }

    public double getY() {
        return vector.getY();
    }

    public void setZ(double z) {
        vector.setZ(z);
    }

    public double getZ() {
        return vector.getZ();
    }
}
