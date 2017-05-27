/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lidarclassification;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import lidarclassification.classes.Attribute;
import lidarclassification.classes.Neighborhood;
import lidarclassification.classes.Plane;
import lidarclassification.classes.TopPeak;

/**
 *
 * @author JustinasK
 */
public class Segmentation {

    public static Neighborhood findNeighborhood(Point3D center, List<Point3D> list, double distance, double accuracy) {
        int iterations = 10;
        Neighborhood neighborhood = findNeighborPoints(center, list, distance);
        Plane plane = findPlane(neighborhood);
        if (plane == null) {
            return null;
        }
        neighborhood.setPlane(plane);
        for (int i = 0; i < iterations; i++) {
            HashMap<Point3D, Double> weights = findWeight(neighborhood, accuracy);
            Plane planeNew = findPlaneWithWeights(weights);
            if (planeNew == null) {
                return null;
            }
            if (Math.abs((planeNew.getX() - plane.getX()) + (planeNew.getY() - plane.getY()) + (planeNew.getZ() - plane.getZ())) > 0.00001) {
                plane = planeNew;
                neighborhood.setPlane(plane);
            } else {
                neighborhood.setPlane(planeNew);
                break;
            }
            /*if (i == iterations) {
                return null;
            }*/
        }

        ArrayList<Point3D> result = new ArrayList<>();
        for (Point3D point : neighborhood.getNeighbors()) {
            if (accuracy * 2 >= dist(point, plane)) {
                result.add(point);
            }
        }
        neighborhood.setNeighbors(result);
        return neighborhood;
    }

    public static Neighborhood findNeighborPoints(Point3D center, List<Point3D> list, double distance) {
        Neighborhood result = new Neighborhood();
        for (Point3D point : list) {
            if (center.distance(point) <= distance) {
                result.getNeighbors().add(point);
            }
        }
        return result;
    }

    public static Plane findPlaneWithWeights(HashMap<Point3D, Double> weights) {
        double x = 0;
        double y = 0;
        double z = 0;
        int size = weights.keySet().size();

        if (size >= 3) {
            for (Point3D point : weights.keySet()) {
                x += point.getX();
                y += point.getY();
                z += point.getZ();
            }
            x /= size;
            y /= size;
            z /= size;

            Point3D centroid = new Point3D(x, y, z);

            double xx = 0.0;
            double xy = 0.0;
            double xz = 0.0;
            double yy = 0.0;
            double yz = 0.0;
            double zz = 0.0;

            for (HashMap.Entry<Point3D, Double> entry : weights.entrySet()) {
                Point3D point = entry.getKey();
                double weight = entry.getValue();
                x = weight * (point.getX() - centroid.getX());
                y = weight * (point.getY() - centroid.getY());
                z = weight * (point.getZ() - centroid.getZ());
                xx += x * x;
                xy += x * y;
                xz += x * z;
                yy += y * y;
                yz += y * z;
                zz += z * z;
            }

            double detX = yy * zz - yz * yz;
            double detY = xx * zz - xz * xz;
            double detZ = xx * yy - xy * xy;
            Point3D dir = null;

            if (detX > 0 || detY > 0 || detZ > 0) {
                if (detX >= detY && detX >= detZ) {
                    double a = (xz * yz - xy * zz) / detX;
                    double b = (xy * yz - xz * yy) / detX;
                    dir = new Point3D(1.0, a, b);
                } else if (detY >= detX && detY >= detZ) {
                    double a = (yz * xz - xy * zz) / detY;
                    double b = (xy * xz - yz * xx) / detY;
                    dir = new Point3D(a, 1.0, b);
                } else {
                    double a = (yz * xy - xz * yy) / detZ;
                    double b = (xz * xy - yz * xx) / detZ;
                    dir = new Point3D(a, b, 1.0);
                }
                return new Plane(
                        dir.normalize().getX(), dir.normalize().getY(), dir.normalize().getZ(),
                        centroid.getX() * dir.normalize().getX() + centroid.getY() * dir.normalize().getY() + centroid.getZ() * dir.normalize().getZ());
            } else {
                return null;
            }
        }
        return null;
    }

    public static Plane findPlane(ArrayList<Point3D> pointList) {
        double x = 0;
        double y = 0;
        double z = 0;
        int size = pointList.size();

        if (size >= 3) {
            for (Point3D point : pointList) {
                x += point.getX();
                y += point.getY();
                z += point.getZ();
            }
            x /= size;
            y /= size;
            z /= size;

            Point3D centroid = new Point3D(x, y, z);

            double xx = 0.0;
            double xy = 0.0;
            double xz = 0.0;
            double yy = 0.0;
            double yz = 0.0;
            double zz = 0.0;

            for (Point3D point : pointList) {
                x = point.getX() - centroid.getX();
                y = point.getY() - centroid.getY();
                z = point.getZ() - centroid.getZ();
                xx += x * x;
                xy += x * y;
                xz += x * z;
                yy += y * y;
                yz += y * z;
                zz += z * z;
            }

            double detX = yy * zz - yz * yz;
            double detY = xx * zz - xz * xz;
            double detZ = xx * yy - xy * xy;
            Point3D dir = null;

            if (detX > 0 || detY > 0 || detZ > 0) {
                if (detX >= detY && detX >= detZ) {
                    double a = (xz * yz - xy * zz) / detX;
                    double b = (xy * yz - xz * yy) / detX;
                    dir = new Point3D(1.0, a, b);
                } else if (detY >= detX && detY >= detZ) {
                    double a = (yz * xz - xy * zz) / detY;
                    double b = (xy * xz - yz * xx) / detY;
                    dir = new Point3D(a, 1.0, b);
                } else {
                    double a = (yz * xy - xz * yy) / detZ;
                    double b = (xz * xy - yz * xx) / detZ;
                    dir = new Point3D(a, b, 1.0);
                }
                return new Plane(
                        dir.normalize().getX(), dir.normalize().getY(), dir.normalize().getZ(),
                        centroid.getX() * dir.normalize().getX() + centroid.getY() * dir.normalize().getY() + centroid.getZ() * dir.normalize().getZ());
            } else {
                return null;
            }
        }
        return null;
    }

    public static Plane findPlane(Neighborhood pointList) {
        return findPlane(pointList.getNeighbors());
    }

    public static HashMap<Point3D, Double> findWeight(Neighborhood list, double accuracy) {
        HashMap<Point3D, Double> results = new HashMap<>();
        list.getNeighbors().forEach((point) -> {
            double dist = dist(point, list.getPlane());
            double weight = (dist <= accuracy) ? 1 : (accuracy / dist);
            results.put(point, weight);
        });
        return results;
    }

    /*public static HashMap<Point3D, Point2D> findAttributes(Point3D origin1, Point3D origin2, ArrayList<Neighborhood> neighborhoodList) {
        HashMap<Point3D, Point2D> attributeList = new HashMap<>();
        neighborhoodList.forEach((neighborhood) -> {
            Plane plane = neighborhood.getPlane();
            double x = dist(origin1, plane);
            double y = dist(origin2, plane);
            Point2D attribute = new Point2D(x, y);
            neighborhood.getNeighbors().forEach((point) -> {
                attributeList.put(point, attribute);
            });
        });
        return attributeList;
    }*/
    public static ArrayList<Attribute> findAttributes(Point3D origin1, Point3D origin2, ArrayList<Neighborhood> neighborhoodList) {
        ArrayList<Attribute> attributeList = new ArrayList<>();
        neighborhoodList.forEach((neighborhood) -> {
            Plane plane = neighborhood.getPlane();
            double x = dist(origin1, plane);
            double y = dist(origin2, plane);
            Point2D attribute = new Point2D(x, y);
            neighborhood.getNeighbors().forEach((point) -> {
                attributeList.add(new Attribute(point, attribute));
            });
        });
        return attributeList;
    }

    /*public static HashMap<Point2D, ArrayList<Point3D>> setAccumulatorArray(HashMap<Point3D, Point2D> attributeList, double acc, double maxDist) {
        HashMap<Point2D, ArrayList<Point3D>> accumulatorArray = new HashMap<>();

        attributeList.entrySet().forEach((entry) -> {
            double pointX = entry.getValue().getX();
            double pointY = entry.getValue().getY();
            for (double i = Math.floor(pointX - 1.0); i <= Math.ceil(pointX + 1.0); i = i + (acc * 2)) {
                if (pointX >= i && pointX < i + acc * 2) {
                    for (double j = Math.floor(pointY - 1.0); j < Math.ceil(pointY + 1.0); j = j + (acc * 2)) {
                        if (pointY >= j && pointY < j + acc * 2) {
                            Point2D currentPoint = new Point2D(i, j);
                            if (!accumulatorArray.containsKey(currentPoint)) {
                                accumulatorArray.put(currentPoint, new ArrayList<>());
                                accumulatorArray.get(currentPoint).add(entry.getKey());
                            } else {
                                accumulatorArray.get(currentPoint).add(entry.getKey());
                            }
                        }
                    }
                }
            }
        });
        return accumulatorArray;
    }*/
    public static HashMap<Point2D, ArrayList<Point3D>> setAccumulatorArray(ArrayList<Attribute> attributeList, double acc, double maxDist) {
        HashMap<Point2D, ArrayList<Point3D>> accumulatorArray = new HashMap<>();

        attributeList.forEach((entry) -> {
            double pointX = entry.getAttribute().getX();
            double pointY = entry.getAttribute().getY();
            for (double i = 0; i <= Math.ceil(pointX + 1.0); i += acc * 2) {
                if (pointX >= i && pointX < i + acc * 2) {
                    for (double j = 0; j < Math.ceil(pointY + 1.0); j+= acc * 2) {
                        if (pointY >= j && pointY < j + acc * 2) {
                            Point2D currentPoint = new Point2D(i, j);
                            if (!accumulatorArray.containsKey(currentPoint)) {
                                accumulatorArray.put(currentPoint, new ArrayList<>());
                                accumulatorArray.get(currentPoint).add(entry.getPoint());
                            } else {
                                accumulatorArray.get(currentPoint).add(entry.getPoint());
                            }
                        }
                    }
                }
            }
        });
        for (HashMap.Entry<Point2D, ArrayList<Point3D>> entry : accumulatorArray.entrySet()) {
            removeDuplicates(entry.getValue());
        }
        return accumulatorArray;
    }

    public static TopPeak findTopPeak(HashMap<Point2D, ArrayList<Point3D>> accumulatorArray) {
        int count = 0;
        Point2D peak = null;
        for (HashMap.Entry<Point2D, ArrayList<Point3D>> entry : accumulatorArray.entrySet()) {
            ArrayList<Point3D> points = new ArrayList<>();
            entry.getValue().forEach((point) -> {
                points.add(point);
            });
            //removeDuplicates(points);
            if (points.size() > count) {
                count = points.size();
                peak = entry.getKey();
            }
        }
        return new TopPeak(peak, count);
    }

    public static boolean coPlanarityCheck(ArrayList<Point3D> pointList, double accuracy) {
        Plane plane = findPlane(pointList);
        double rms = 0;

        rms = pointList.stream().map((point) -> Math.pow(dist(point, plane), 2)).reduce(rms, (accumulator, _item) -> accumulator + _item);
        rms /= pointList.size();
        rms /= Math.sqrt(rms);

        return rms <= accuracy;
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

        for (Point3D point : list) {
            if (point.getX() < minX) {
                minX = point.getX();
            }
            if (point.getY() < minY) {
                minY = point.getY();
            }
            if (point.getZ() < minZ) {
                minZ = point.getZ();
            }
        }
        return new Point3D(minX, minY, minZ);
    }

    private static Point3D getMax3DPoint(ArrayList<Point3D> list) {
        double maxX = Double.MIN_VALUE;
        double maxY = Double.MIN_VALUE;
        double maxZ = Double.MIN_VALUE;

        for (Point3D point : list) {
            if (point.getX() > maxX) {
                maxX = point.getX();
            }
            if (point.getY() > maxY) {
                maxY = point.getY();
            }
            if (point.getZ() > maxZ) {
                maxZ = point.getZ();
            }
        }
        return new Point3D(maxX, maxY, maxZ);
    }

    public static ArrayList<Point3D> readPointsFromFile(String path) throws FileNotFoundException, IOException {
        File file = new File(path);
        BufferedReader fr = new BufferedReader(new FileReader(file));
        ArrayList<Point3D> array = new ArrayList<>();
        String text = "";
        while ((text = fr.readLine()) != null) {
            String[] nums = text.split(" ");
            double x = Double.parseDouble(nums[0]);
            double y = Double.parseDouble(nums[1]);
            double z = Double.parseDouble(nums[2]);
            array.add(new Point3D(x, y, z));
        }
        return array;
    }

    public static HashMap<ArrayList<Point3D>, Plane> findPoints(double distance, double accuracy, double proximity, int threshold, String path) throws FileNotFoundException, IOException {

        ArrayList<Point3D> pointList = readPointsFromFile(path);
        ArrayList<Neighborhood> neighborhoodList = new ArrayList<>();

        for (int i = 0; i < pointList.size(); i++) {
            Neighborhood neighborhood = findNeighborhood(pointList.get(i), pointList, distance, accuracy);
            if (neighborhood != null) {
                neighborhoodList.add(neighborhood);
            }
            System.out.println(i + ") " + pointList.get(i).toString() + " -> " + neighborhood);
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
        ArrayList<Attribute> attributeList = findAttributes(origin1, origin2, neighborhoodList);

        /*for (HashMap.Entry<Point3D, Point2D> entry : attributeList.entrySet()) {
            System.out.println(entry.getKey().toString() + " -> " + entry.getValue().toString());
        }*/
        HashMap<Point2D, ArrayList<Point3D>> accumulatorArray = setAccumulatorArray(attributeList, accuracy, 2 * min.distance(max) / 3);
        /*accumulatorArray.forEach((k, v) -> {
            System.out.println("key: " + k.toString() + " value:" + v.toString());
        });*/

        HashMap<ArrayList<Point3D>, Plane> recorded = new HashMap<>();
        int peakCount = Integer.MAX_VALUE;
        while (peakCount > threshold) {
            TopPeak peak = findTopPeak(accumulatorArray);
            peakCount = peak.getCount();
            System.out.println(peakCount);
            if (peakCount > threshold) {
                if (coPlanarityCheck(accumulatorArray.get(peak.getPeak()), accuracy)) {
                    ArrayList<Point3D> cluster = findCluster(accumulatorArray, peak.getPeak(), accumulatorArray.get(peak.getPeak()), accuracy, proximity);
                    recorded.put(cluster, findPlane(cluster));
                    /*accumulatorArray.remove(peak.getKey());*/
                } else {
                    moveOrigins(origin1, origin2, peak, accuracy, proximity, distance, min, max, threshold, accumulatorArray, recorded);
                    /*accumulatorArray.remove(peak.getKey());*/
                }
            }
        }
        return recorded;
    }

    public static void moveOrigins(Point3D origin1, Point3D origin2, TopPeak peak, double accuracy, double proximity, double distance, Point3D min, Point3D max, int threshold, HashMap<Point2D, ArrayList<Point3D>> accumulatorArray, HashMap<ArrayList<Point3D>, Plane> recorded) {
        System.out.print(">");
        Random r = new Random();
        double range = 5;
        int iterations = 5;
        HashMap<Point2D, ArrayList<Point3D>> accumulatorArrayNew = accumulatorArray;
        TopPeak peakNew = peak;

        for (int i = 0; i < iterations; i++) {
            ArrayList<Neighborhood> neighborhoodList = new ArrayList<>();
            for (Point3D point : accumulatorArrayNew.get(peakNew.getPeak())) {
                Neighborhood neighborhood = findNeighborhood(point, accumulatorArrayNew.get(peakNew.getPeak()), distance, accuracy);
                if (neighborhood != null) {
                    neighborhoodList.add(neighborhood);
                }
            }

            Point3D originNew1 = origin1.add(r.nextDouble() * range, r.nextDouble() * range, r.nextDouble() * range);
            Point3D originNew2 = origin2.add(r.nextDouble() * range, r.nextDouble() * range, r.nextDouble() * range);

            ArrayList<Attribute> attributeListNew = findAttributes(originNew1, originNew2, neighborhoodList);
            accumulatorArrayNew = setAccumulatorArray(attributeListNew, accuracy, 2 * min.distance(max) / 3);
            peakNew = findTopPeak(accumulatorArrayNew);

            if (coPlanarityCheck(accumulatorArrayNew.get(peakNew.getPeak()), accuracy)) {
                int peakCountNew = peak.getCount();
                if (peakCountNew > threshold) {
                    ArrayList<Point3D> cluster = findCluster(accumulatorArray, peak.getPeak(), accumulatorArrayNew.get(peakNew.getPeak()), accuracy, proximity);
                    recorded.put(cluster, findPlane(cluster));
                }
                break;
            }
            if (i == iterations - 1) {
                accumulatorArray.get(peak.getPeak()).removeAll(accumulatorArrayNew.get(peakNew.getPeak()));
            }
        }
    }

    public static ArrayList<Point3D> findCluster(HashMap<Point2D, ArrayList<Point3D>> accumulatorArray, Point2D peakKey, ArrayList<Point3D> peakValue, double accuracy, double proximity) {
        int k = 0;
        ArrayList<Point3D> cluster = new ArrayList<>();

        ArrayList<Point3D> toAdd = new ArrayList<>();
        toAdd.add(Point3D.ZERO);

        ArrayList<Point3D> peakPoints = new ArrayList<>();
        for (Point3D neighborhood : peakValue) {
            peakPoints.add(neighborhood);
        }
        removeDuplicates(peakPoints);
        accumulatorArray.get(peakKey).removeAll(peakPoints);

        while (toAdd.size() > 0) {
            System.out.print("/");
            k++;
            ArrayList<Point3D> neighboringPoints = new ArrayList<>();
            toAdd.clear();
            for (double i = peakKey.getX() - (accuracy * 2 * k); i <= peakKey.getX() + (accuracy * 2 * k); i = i + accuracy * 2) {
                for (double j = peakKey.getY() - (accuracy * 2 * k); j <= peakKey.getY() + (accuracy * 2 * k); j = j + accuracy * 2) {
                    Point2D cell = new Point2D(i, j);
                    if (accumulatorArray.containsKey(cell)) {
                        neighboringPoints.addAll(accumulatorArray.get(cell));
                    }
                }
            }

            removeDuplicates(neighboringPoints);
            ArrayList<Point3D> eligible = new ArrayList<>();

            for (Point3D point : neighboringPoints) {
                for (Point3D peakPoint : peakPoints) {
                    if (point.distance(peakPoint) < proximity * 2) {
                        eligible.add(point);
                        break;
                    }
                }
            }

            Plane peakPlane = findPlane(peakPoints);
            for (Point3D point : eligible) {
                if ((dist(point, peakPlane) < accuracy * 2.0) /*&& !peakPoints.contains(point)*/) {
                    toAdd.add(point);
                }
            }

           /* for (double i = peakKey.getX() - (accuracy * 2 * k); i <= peakKey.getX() + (accuracy * k); i = i + accuracy * 2) {
                for (double j = peakKey.getY() - (accuracy * 2 * k); j <= peakKey.getY() + (accuracy * k); j = j + accuracy * 2) {
                    Point2D cell = new Point2D(i, j);
                    if (accumulatorArray.containsKey(cell)) {
                        accumulatorArray.get(cell).removeAll(toAdd);
                        accumulatorArray.get(cell).removeAll(peakPoints);
                    }
                }
            }*/

            for (ArrayList<Point3D> points : accumulatorArray.values()) {
                points.removeAll(toAdd);
                points.removeAll(peakPoints);
            }
            cluster.addAll(toAdd);
        }
        System.out.println("");
        cluster.addAll(peakPoints);
        removeDuplicates(cluster);
        return cluster;
    }

    public static void removeDuplicates(ArrayList<Point3D> neighboringPoints) {
        HashSet temp = new HashSet<>();
        temp.addAll(neighboringPoints);
        neighboringPoints.clear();
        neighboringPoints.addAll(temp);
    }
}
