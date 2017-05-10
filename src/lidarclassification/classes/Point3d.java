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
public class Point3d {

    private double x = 0;
    private double y = 0;
    private double z = 0;

    public Point3d() {
    }

    public Point3d(double a, double b, double c) {
        x = a;
        y = b;
        z = c;
    }

    public double getX() {
        return x;
    }

    public void setX(double r) {
        x = r;
    }

    public double getY() {
        return y;
    }

    public void setY(double r) {
        y = r;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double r) {
        z = r;
    }

    public String print() {
        return Double.toString(x) + " " + Double.toString(y) + " " + Double.toString(z);
    }

    public boolean equals(Point3d a) {
        if (x == a.getX() && y == a.getY() && z == a.getZ()) {
            return true;
        } else {
            return false;
        }
    }
}
