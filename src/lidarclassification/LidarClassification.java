/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lidarclassification;

import lidarclassification.classes.Point3d;
import lidarclassification.classes.NeighborPoints;
import java.util.ArrayList;
import lidarclassification.classes.Plane;

/**
 *
 * @author JustinasK
 */
public class LidarClassification {

    public static ArrayList<Point3d> findNeighborhood(Point3d center, ArrayList<Point3d> list, double radius, double accuracy) {
        NeighborPoints neighborhood = getNeighborPoints(center, list, radius);
        Plane plane = neighborhood.getPlane();
        for (int i = 0; i < 10; i++) {
            getWeight(plane, neighborhood, accuracy);
            Plane planeNew = neighborhood.getPlane();
            if (Math.abs(planeNew.getX() - plane.getX() + planeNew.getY() - plane.getY() + planeNew.getZ() - plane.getZ()) > 0.00001) {
                plane = planeNew;
            } else {
                break;
            }
        }
        ArrayList<Point3d> result = new ArrayList<>();
        for (int i = 0; i < neighborhood.getNeighbors().size(); i++) {
            if (neighborhood.getWeights().get(i) == 1) {
                result.add(neighborhood.getNeighbors().get(i));
            }
        }
        result.add(0, center);
        return result;
    }

    public static NeighborPoints getNeighborPoints(Point3d center, ArrayList<Point3d> list, double distance) {
        NeighborPoints result = new NeighborPoints(center, distance);
        for (int i = 0; i < list.size(); i++) {
            Point3d point = list.get(i);
            if (Ops.distance2(center, point) <= distance && !point.equals(center)) {
                result.getNeighbors().add(point);
                result.getWeights().add(1.0);
            }
        }
        return result;
    }

    

    public static void getWeight(Plane plane, NeighborPoints list, double accuracy) {
        Point3d center = list.getCenter();
        for (int i = 0; i < list.getNeighbors().size(); i++) {
            Point3d point = list.getNeighbors().get(i);
            Point3d point2 = new Point3d(point.getX() - center.getX(), point.getY() - center.getY(), point.getZ() - center.getZ());
            if (point2.getX() < 0){
                point2.setX(0-point2.getX());
                point2.setY(0-point2.getY());
                point2.setZ(0-point2.getZ());
            }           
            
            double dist = (Math.abs((point2.getX() * plane.getX()) + (point2.getY() * plane.getY()) + (point2.getZ() * plane.getZ()) - Ops.distance2(point2))) / 
                    (Math.sqrt(Math.pow(plane.getX(), 2) + Math.pow(plane.getY(), 2) + Math.pow(plane.getZ(), 2)));
            
            double weight = (dist <= accuracy) ? 1 : (accuracy / dist);
            list.getWeights().set(i, weight);
        }
    }

    public static void main(String[] args) {
        ArrayList<Point3d> pointList = new ArrayList<>();
        pointList.add(new Point3d(0, 0, 0));
        pointList.add(new Point3d(-1, -1, 0));
        pointList.add(new Point3d(1, 1, 0));
        pointList.add(new Point3d(2, 2, 0));
        pointList.add(new Point3d(3, 3, 0));
        
        System.out.println(Ops.distance(pointList.get(1), pointList.get(2)));
        System.out.println(Ops.distance2(pointList.get(2)));
        
        for (int i = 0; i < pointList.size(); i++) {
            ArrayList<Point3d> list = findNeighborhood(pointList.get(i), pointList, 2, 0.1);
            for (int j = 0; j < list.size(); j++) {
                System.out.print(list.get(j).print() + " / ");
            }
            System.out.println();
        }
    }
}
