/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lidarclassification.classes;

import javafx.geometry.Point3D;

/**
 *
 * @author JustinasK
 */
public class Plane {

    private Point3D vector = null;
    private double distance = 0;

    public void setVector(Point3D x) {
        vector = x;
    }

    public void setVector(double x, double y, double z) {
        vector = new Point3D(x, y, z);
    }

    public Point3D getVector() {
        return vector;
    }

    public void setDistance(double x) {
        distance = x;
    }

    public double getDistance() {
        return distance;
    }

    public Plane(double a, double b, double c, double d) {
        vector = new Point3D(a, b, c);
        distance = d;
    }

    public double getX() {
        return vector.getX();
    }

    public double getY() {
        return vector.getY();
    }

    public double getZ() {
        return vector.getZ();
    }
}
