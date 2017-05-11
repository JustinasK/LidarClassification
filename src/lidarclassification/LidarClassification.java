/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lidarclassification;

import java.awt.Graphics;
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
            System.out.println(plane.getVector().print() + " " + plane.getDistance());
            if (Math.abs(planeNew.getX() - plane.getX() + planeNew.getY() - plane.getY() + planeNew.getZ() - plane.getZ()) > accuracy*0.0001) {
                plane = planeNew;
            } else {
                break;
            }
        }
        ArrayList<Point3d> result = new ArrayList<>();
        for (int i = 0; i < neighborhood.getNeighbors().size(); i++) {
            //System.out.print(neighborhood.getNeighbors().get(i).print() + " (" + dist(neighborhood.getNeighbors().get(i), plane, center) + ") / ");
            if (accuracy >= dist(neighborhood.getNeighbors().get(i), plane, center)) {
                //System.out.println("+");
                result.add(neighborhood.getNeighbors().get(i));
            }
        }
        //System.out.println();
        return result;
    }

    public static NeighborPoints getNeighborPoints(Point3d center, ArrayList<Point3d> list, double distance) {
        NeighborPoints result = new NeighborPoints(center, distance);
        for (int i = 0; i < list.size(); i++) {
            Point3d point = list.get(i);
            if (Ops.distance2(center, point) <= distance) { 
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
            
            double dist = dist(point2, plane, center);
            double weight = (dist <= accuracy) ? 1 : (accuracy / dist);
            list.getWeights().set(i, weight);
        }
    }

    private static double dist(Point3d point, Plane plane, Point3d center) {
        double dist = (Math.abs((point.getX() * plane.getX()) + (point.getY() * plane.getY()) + (point.getZ() * plane.getZ()) - plane.getDistance()))
                / (Math.sqrt(Math.pow(plane.getX(), 2) + Math.pow(plane.getY(), 2) + Math.pow(plane.getZ(), 2)));
        return dist;
    }

    public static void main(String[] args) {
        ArrayList<Point3d> pointList = new ArrayList<>();
        pointList.add(new Point3d(1, 1, 0));
        pointList.add(new Point3d(2, 2, 0));        
        pointList.add(new Point3d(2, 2, 3));
        pointList.add(new Point3d(3, 3, 0));
        pointList.add(new Point3d(2, 2.01, 3));


        System.out.println(Ops.distance(pointList.get(0), pointList.get(1)));
        System.out.println(Ops.distance2(pointList.get(0), pointList.get(2)));

        for (int i = 0; i < pointList.size(); i++) {
            ArrayList<Point3d> list = findNeighborhood(pointList.get(i), pointList, 4, 0.01);
            System.out.print(pointList.get(i).print() + " -> ");
            for (int j = 0; j < list.size(); j++) {                
                System.out.print(list.get(j).print() + " / ");
            }
            System.out.println();
        }
    }
}
