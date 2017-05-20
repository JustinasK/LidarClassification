/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lidarclassification.classes;

import java.util.ArrayList;
import javafx.geometry.Point3D;

/**
 *
 * @author JustinasK
 */
public class Neighborhood {

    private ArrayList<Point3D> neighbors = new ArrayList<>();
    private Plane plane = null;
    
    
    public Neighborhood() {
    }

    public ArrayList<Point3D> getNeighbors() {
        return neighbors;
    }

    public void setNeighbors(ArrayList<Point3D> list) {
        neighbors = list;
    }
    
    public Plane getPlane() {
        return plane;
    }

    public void setPlane(Plane n) {
        plane = n;
    }
}
