/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lidarclassification;

import java.awt.Graphics;
import lidarclassification.classes.Point3d;
import lidarclassification.classes.Neighborhood;
import java.util.ArrayList;
import lidarclassification.classes.Plane;

/**
 *
 * @author JustinasK
 */
public class LidarClassification {

    public static ArrayList<Point3d> findNeighborhood(Point3d center, ArrayList<Point3d> list, double distance, double accuracy) {
        Neighborhood neighborhood = findNeighborPoints(center, list, distance);
        ArrayList weights = new ArrayList<>();
        for (int i=0; i<neighborhood.getNeighbors().size(); i++){
            weights.add(1.0);
        }
        Plane plane = findPlane(neighborhood, weights);
        neighborhood.setPlane(plane);
        for (int i = 0; i < 10; i++) {
            findWeight(weights, neighborhood, accuracy);
            Plane planeNew = findPlane(neighborhood, weights);
            if (Math.abs(planeNew.getX() - plane.getX() + planeNew.getY() - plane.getY() + planeNew.getZ() - plane.getZ()) > accuracy * 0.0001) {
                plane = planeNew;   
                neighborhood.setPlane(plane); 
            } else {
                neighborhood.setPlane(planeNew); 
                break;
            }
        }
        ArrayList<Point3d> result = new ArrayList<>();
        for (int i = 0; i < neighborhood.getNeighbors().size(); i++) {
            if ((accuracy * 2) >= dist(neighborhood.getNeighbors().get(i), plane)) {
                result.add(neighborhood.getNeighbors().get(i));
            }
        }
        return result;
    }

    public static Neighborhood findNeighborPoints(Point3d center, ArrayList<Point3d> list, double distance) {
        Neighborhood result = new Neighborhood(center);
        for (int i = 0; i < list.size(); i++) {
            Point3d point = list.get(i);
            if (Ops.distance(center, point) <= distance) {
                result.getNeighbors().add(point);
            }
        }
        return result;
    }

    public static Plane findPlane(Neighborhood hood, ArrayList<Double> weights) {
        double x = 0;
        double y = 0;
        double z = 0;
        double w = 0;
        int size = hood.getNeighbors().size();

        if (size >= 3) {
            for (int i = 0; i < size; i++) {
                Point3d a = hood.getNeighbors().get(i);
                x += a.getX() - hood.getCenter().getX();
                y += a.getY() - hood.getCenter().getY();
                z += a.getZ() - hood.getCenter().getZ();
            }
            x /= size;
            y /= size;
            z /= size;

            Point3d centroid = new Point3d(x, y, z);

            x = 0;
            y = 0;
            z = 0;

            for (int i = 0; i < size; i++) {
                Point3d a = hood.getNeighbors().get(i);
                x += (a.getX() - centroid.getX()) * weights.get(i) + centroid.getX();
                y += (a.getY() - centroid.getX()) * weights.get(i) + centroid.getY();
                z += (a.getZ() - centroid.getX()) * weights.get(i) + centroid.getZ();
                w += weights.get(i);
            }
            x /= w;
            y /= w;
            z /= w;

            centroid = new Point3d(x, y, z);

            double xx = 0.0;
            double xy = 0.0;
            double xz = 0.0;
            double yy = 0.0;
            double yz = 0.0;
            double zz = 0.0;

            for (int i = 0; i < size; i++) {
                Point3d a = hood.getNeighbors().get(i);
                x = a.getX() - hood.getCenter().getX() - centroid.getX();
                y = a.getY() - hood.getCenter().getY() - centroid.getY();
                z = a.getZ() - hood.getCenter().getZ() - centroid.getZ();
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
                return new Plane(
                        Ops.xCos(dir), Ops.yCos(dir), Ops.zCos(dir),
                        hood.getCenter().getX() * Ops.xCos(dir) + hood.getCenter().getY() * Ops.yCos(dir) + hood.getCenter().getZ() * Ops.zCos(dir));
            } else if (detY >= detX && detY >= detZ) {
                double a = (yz * xz - xy * zz) / detY;
                double b = (xy * xz - yz * xx) / detY;
                Point3d dir = new Point3d(a, 1.0, b);
                return new Plane(
                        Ops.xCos(dir), Ops.yCos(dir), Ops.zCos(dir),
                        hood.getCenter().getX() * Ops.xCos(dir) + hood.getCenter().getY() * Ops.yCos(dir) + hood.getCenter().getZ() * Ops.zCos(dir));
            } else {
                double a = (yz * xy - xz * yy) / detZ;
                double b = (xz * xy - yz * xx) / detZ;
                Point3d dir = new Point3d(a, b, 1.0);
                return new Plane(
                        Ops.xCos(dir), Ops.yCos(dir), Ops.zCos(dir),
                        hood.getCenter().getX() * Ops.xCos(dir) + hood.getCenter().getY() * Ops.yCos(dir) + hood.getCenter().getZ() * Ops.zCos(dir));
            }
        }
        return null;
    }

    public static void findWeight(ArrayList weights, Neighborhood list, double accuracy) {
        Point3d center = list.getCenter();
        for (int i = 0; i < list.getNeighbors().size(); i++) {
            Point3d point = list.getNeighbors().get(i);
            Point3d point2 = new Point3d(point.getX() - center.getX(), point.getY() - center.getY(), point.getZ() - center.getZ());

            double dist = dist(point2, list.getPlane());
            double weight = (dist <= accuracy) ? 1 : (accuracy / dist);
            weights.set(i, weight);
        }
    }

    private static double dist(Point3d point, Plane plane) {
        double dist = (Math.abs((point.getX() * plane.getX()) + (point.getY() * plane.getY()) + (point.getZ() * plane.getZ()) - plane.getDistance()))
                / (Math.sqrt(Math.pow(plane.getX(), 2) + Math.pow(plane.getY(), 2) + Math.pow(plane.getZ(), 2)));
        return dist;
    }

    private static Point3d getMin3dPoint(ArrayList<Point3d> list) {
        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double minZ = Double.MAX_VALUE;

        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getX() < minX) {
                minX = list.get(i).getX();
            }
            if (list.get(i).getY() < minY) {
                minY = list.get(i).getY();
            }
            if (list.get(i).getZ() < minZ) {
                minZ = list.get(i).getZ();
            }
        }
        return new Point3d(minX, minY, minZ);
    }

    private static Point3d getMax3dPoint(ArrayList<Point3d> list) {
        double maxX = Double.MIN_VALUE;
        double maxY = Double.MIN_VALUE;
        double maxZ = Double.MIN_VALUE;

        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getX() > maxX) {
                maxX = list.get(i).getX();
            }
            if (list.get(i).getY() > maxY) {
                maxY = list.get(i).getY();
            }
            if (list.get(i).getZ() > maxZ) {
                maxZ = list.get(i).getZ();
            }
        }
        return new Point3d(maxX, maxY, maxZ);
    }

    public static void main(String[] args) {
        ArrayList<Point3d> pointList = new ArrayList<>();
        pointList.add(new Point3d(1, 1, 0));
        pointList.add(new Point3d(2, 2, 0));
        pointList.add(new Point3d(2, 2, 3));
        pointList.add(new Point3d(3, 3, 0));
        pointList.add(new Point3d(2, 2.5, 3));

        System.out.println(Ops.distance(pointList.get(0), pointList.get(2)));

        for (int i = 0; i < pointList.size(); i++) {
            ArrayList<Point3d> list = findNeighborhood(pointList.get(i), pointList, 4, 0.01);
            System.out.print(pointList.get(i).print() + " -> ");
            for (int j = 0; j < list.size(); j++) {
                System.out.print(list.get(j).print() + " / ");
            }
            System.out.println();
        }

        Point3d max = getMax3dPoint(pointList);
        Point3d min = getMin3dPoint(pointList);
        Point3d origin1 = new Point3d(
                min.getX() + (max.getX() - min.getX()) / 3,
                min.getY() + (max.getY() - min.getY()) / 3,
                min.getZ() + (max.getZ() - min.getZ()) / 3);
        Point3d origin2 = new Point3d(
                min.getX() + 2 * (max.getX() - min.getX()) / 3,
                min.getY() + 2 * (max.getY() - min.getY()) / 3,
                min.getZ() + 2 * (max.getZ() - min.getZ()) / 3);

        System.out.println(min.print());
        System.out.println(max.print());
        System.out.println(origin1.print());
        System.out.println(origin2.print());
    }
}
