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
public class Neighborhood {

    private Point3d center = null;
    private ArrayList<Point3d> neighbors = new ArrayList<>();
    private Plane plane = null;
    
    public Neighborhood(Point3d n) {
        center = n;
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
    
    public Plane getPlane() {
        return plane;
    }

    public void setPlane(Plane n) {
        plane = n;
    }
}
