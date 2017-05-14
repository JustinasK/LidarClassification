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
public class Point {

    private double x = 0;
    private double y = 0;

    public Point() {
    }

    public Point(double a, double b) {
        x = a;
        y = b;
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
    
    public String print() {
        return Double.toString(x) + " " + Double.toString(y);
    }

    public boolean equals(Point3d a) {
        if (this.getX() == a.getX() && this.getY() == a.getY()) {
            return true;
        } else {
            return false;
        }
    }
}
