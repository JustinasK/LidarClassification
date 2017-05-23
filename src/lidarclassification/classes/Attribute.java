/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lidarclassification.classes;

import javafx.geometry.Point2D;
import javafx.geometry.Point3D;

/**
 *
 * @author JustinasK
 */
public class Attribute {

    private Point3D point;
    private Point2D attribute;

    public Attribute(Point3D x, Point2D y) {
        point = x;
        attribute = y;
    }

    public Point3D getPoint() {
        return point;
    }

    public void getPoint(Point3D x) {
        point = x;
    }

    public Point2D getAttribute() {
        return attribute;
    }

    public void getAttribute(Point2D x) {
        attribute = x;
    }

}
