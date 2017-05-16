/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lidarclassification;

import lidarclassification.classes.Neighborhood;
import java.util.ArrayList;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import lidarclassification.classes.Plane;

/**
 *
 * @author JustinasK
 */
public class LidarClassification {

    public static Neighborhood findNeighborhood(Point3D center, ArrayList<Point3D> list, double distance, double accuracy) {
        Neighborhood neighborhood = findNeighborPoints(center, list, distance);
        ArrayList weights = new ArrayList<>();
        for (int i = 0; i < neighborhood.getNeighbors().size(); i++) {
            weights.add(1.0);
        }
        Plane plane = findPlane(neighborhood, weights);
        neighborhood.setPlane(plane);
        for (int i = 0; i < 10; i++) {
            weights = findWeight(neighborhood, accuracy);
            Plane planeNew = findPlane(neighborhood, weights);
            if (Math.abs(planeNew.getX() - plane.getX() + planeNew.getY() - plane.getY() + planeNew.getZ() - plane.getZ()) > accuracy * 0.0001) {
                plane = planeNew;
                neighborhood.setPlane(plane);
            } else {
                neighborhood.setPlane(planeNew);
                break;
            }
        }
        ArrayList<Point3D> result = new ArrayList<>();
        for (int i = 0; i < neighborhood.getNeighbors().size(); i++) {
            if ((accuracy * 2) >= dist(neighborhood.getNeighbors().get(i), plane)) {
                result.add(neighborhood.getNeighbors().get(i));
            }
        }
        neighborhood.setNeighbors(result);
        return neighborhood;
    }

    public static Neighborhood findNeighborPoints(Point3D center, ArrayList<Point3D> list, double distance) {
        Neighborhood result = new Neighborhood(center);
        for (int i = 0; i < list.size(); i++) {
            Point3D point = list.get(i);
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
                Point3D a = hood.getNeighbors().get(i);
                x += a.getX() - hood.getCenter().getX();
                y += a.getY() - hood.getCenter().getY();
                z += a.getZ() - hood.getCenter().getZ();
            }
            x /= size;
            y /= size;
            z /= size;

            Point3D centroid = new Point3D(x, y, z);

            x = 0;
            y = 0;
            z = 0;

            for (int i = 0; i < size; i++) {
                Point3D a = hood.getNeighbors().get(i);
                x += (a.getX() - centroid.getX()) * weights.get(i) + centroid.getX();
                y += (a.getY() - centroid.getX()) * weights.get(i) + centroid.getY();
                z += (a.getZ() - centroid.getX()) * weights.get(i) + centroid.getZ();
                w += weights.get(i);
            }
            x /= w;
            y /= w;
            z /= w;

            centroid = new Point3D(x, y, z);

            double xx = 0.0;
            double xy = 0.0;
            double xz = 0.0;
            double yy = 0.0;
            double yz = 0.0;
            double zz = 0.0;

            for (int i = 0; i < size; i++) {
                Point3D a = hood.getNeighbors().get(i);
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
                Point3D dir = new Point3D(1.0, a, b);
                return new Plane(
                        Ops.xCos(dir), Ops.yCos(dir), Ops.zCos(dir),
                        hood.getCenter().getX() * Ops.xCos(dir) + hood.getCenter().getY() * Ops.yCos(dir) + hood.getCenter().getZ() * Ops.zCos(dir));
            } else if (detY >= detX && detY >= detZ) {
                double a = (yz * xz - xy * zz) / detY;
                double b = (xy * xz - yz * xx) / detY;
                Point3D dir = new Point3D(a, 1.0, b);
                return new Plane(
                        Ops.xCos(dir), Ops.yCos(dir), Ops.zCos(dir),
                        hood.getCenter().getX() * Ops.xCos(dir) + hood.getCenter().getY() * Ops.yCos(dir) + hood.getCenter().getZ() * Ops.zCos(dir));
            } else {
                double a = (yz * xy - xz * yy) / detZ;
                double b = (xz * xy - yz * xx) / detZ;
                Point3D dir = new Point3D(a, b, 1.0);
                return new Plane(
                        Ops.xCos(dir), Ops.yCos(dir), Ops.zCos(dir),
                        hood.getCenter().getX() * Ops.xCos(dir) + hood.getCenter().getY() * Ops.yCos(dir) + hood.getCenter().getZ() * Ops.zCos(dir));
            }
        }
        return null;
    }

    public static ArrayList<Double> findWeight(Neighborhood list, double accuracy) {
        ArrayList<Double> results = new ArrayList<>();
        Point3D center = list.getCenter();
        for (int i = 0; i < list.getNeighbors().size(); i++) {
            Point3D point = list.getNeighbors().get(i).subtract(center);
            double dist = dist(point, list.getPlane());
            double weight = (dist <= accuracy) ? 1 : (accuracy / dist);
            results.add(weight);
        }
        return results;
    }

    public static ArrayList<Point2D> findAttributes(Point3D origin1, Point3D origin2, ArrayList<Neighborhood> neighborhoodList) {
        ArrayList<Point2D> attributeList = new ArrayList<>();
        for (int i = 0; i < neighborhoodList.size(); i++) {
            Plane plane = neighborhoodList.get(i).getPlane();
            double x = dist(origin1, plane);
            double y = dist(origin2, plane);
            attributeList.add(new Point2D(x, y));
        }
        return attributeList;
    }

    public static Point2D findPeak(ArrayList<Point2D> pointList, double acc) {
        double peakX = 0;
        double peakY = 0;
        for (int i = 0; i < pointList.get(0).getX(); i += acc) {
            for (int j = 0; j < pointList.get(0).getY(); j += acc) {
            }
        }
        return new Point2D(peakX, peakY);
    }

    private static double dist(Point3D point, Plane plane) {
        double dist = (Math.abs((point.getX() * plane.getX()) + (point.getY() * plane.getY()) + (point.getZ() * plane.getZ()) - plane.getDistance()))
                / (Math.sqrt(Math.pow(plane.getX(), 2) + Math.pow(plane.getY(), 2) + Math.pow(plane.getZ(), 2)));
        return dist;
    }

    private static Point3D getMin3DPoint(ArrayList<Point3D> list) {
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
        return new Point3D(minX, minY, minZ);
    }

    private static Point3D getMax3DPoint(ArrayList<Point3D> list) {
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
        return new Point3D(maxX, maxY, maxZ);
    }

    public static void main(String[] args) {
        double distance = 4.0;
        double accuracy = 0.01;

        ArrayList<Point3D> pointList = new ArrayList<>();
        pointList.add(new Point3D(1, 1, 0));
        pointList.add(new Point3D(2, 2, 0));
        pointList.add(new Point3D(2, 2, 3));
        pointList.add(new Point3D(3, 3, 0));
        pointList.add(new Point3D(2, 2.5, 3));
        pointList.add(new Point3D(1, 3, 1.5));

        ArrayList<Neighborhood> neighborhoodList = new ArrayList<>();

        for (int i = 0; i < pointList.size(); i++) {
            Neighborhood neighborhood = findNeighborhood(pointList.get(i), pointList, distance, accuracy);
            neighborhoodList.add(neighborhood);
            /*System.out.print(pointList.get(i).print() + " -> ");
            for (int j = 0; j < neighborhood.getNeighbors().size(); j++) {
                System.out.print(neighborhood.getNeighbors().get(j).print() + " / ");
            }
            System.out.println();*/
        }

        Point3D max = getMax3DPoint(pointList);
        Point3D min = getMin3DPoint(pointList);
        Point3D origin1 = new Point3D(
                min.getX() + (max.getX() - min.getX()) / 3,
                min.getY() + (max.getY() - min.getY()) / 3,
                min.getZ() + (max.getZ() - min.getZ()) / 3);
        Point3D origin2 = new Point3D(
                min.getX() + 2 * (max.getX() - min.getX()) / 3,
                min.getY() + 2 * (max.getY() - min.getY()) / 3,
                min.getZ() + 2 * (max.getZ() - min.getZ()) / 3);
        ArrayList<Point2D> attributeList = findAttributes(origin1, origin2, neighborhoodList);

        for (int i = 0; i < attributeList.size(); i++) {
            System.out.println(neighborhoodList.get(i).getPlane().getVector().toString() + " " + neighborhoodList.get(i).getPlane().getDistance() + " -> " + attributeList.get(i).toString());
        }
    }
}
