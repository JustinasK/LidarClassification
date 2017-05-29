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

    //Finds the cylindrical neighborhood from a list of points
    public static Neighborhood findNeighborhood(Point3D center, List<Point3D> list, double distance, double accuracy) {
        int iterations = 10;
        
        //Finds the points belonging in a spherical neighborhood
        Neighborhood neighborhood = findNeighborPoints(center, list, distance);
        
        //Finds the initial plane
        Plane plane = findPlane(neighborhood);
        if (plane == null) {
            return null;
        }     
        neighborhood.setPlane(plane);
        
        //Fits the plane depending on points
        for (int i = 0; i < iterations; i++) {
            //Finds weights
            HashMap<Point3D, Double> weights = findWeight(neighborhood, accuracy);
            //Fits the plane according to weighs
            Plane planeNew = findPlaneWithWeights(weights);
            if (planeNew == null) {
                return null;
            }
            //Check if plane convergence is happening. The loop ends if it does.
            if (Math.abs((planeNew.getX() - plane.getX()) + (planeNew.getY() - plane.getY()) + (planeNew.getZ() - plane.getZ())) > 0.00001) {
                plane = planeNew;
                neighborhood.setPlane(plane);
            } else {
                neighborhood.setPlane(planeNew);
                break;
            }
            //Return null neighborhood is max iteration number is reached
            /*if (i == iterations) {
                return null;
            }*/
        }

        //Find the points close to the plane
        ArrayList<Point3D> result = new ArrayList<>();
        for (Point3D point : neighborhood.getNeighbors()) {
            if (accuracy * 2 >= dist(point, plane)) {
                result.add(point);
            }
        }
        neighborhood.setNeighbors(result);
        return neighborhood;
    }

    //Finds an initial neighborhood
    private static Neighborhood findNeighborPoints(Point3D center, List<Point3D> list, double distance) {
        Neighborhood result = new Neighborhood();
        //Finds all the points within a distance to a center point
        for (Point3D point : list) {
            if (center.distance(point) <= distance) {
                result.getNeighbors().add(point);
            }
        }
        return result;
    }

    //Calculates a plane with weights
    public static Plane findPlaneWithWeights(HashMap<Point3D, Double> weights) {
        double x = 0;
        double y = 0;
        double z = 0;
        int size = weights.keySet().size();

        //Plane must have be at least three points
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

    //Calculates a plane without weights
    public static Plane findPlane(ArrayList<Point3D> pointList) {
        double x = 0;
        double y = 0;
        double z = 0;
        int size = pointList.size();

        //Plane must have be at least three points
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

    //Finds weights of the points, based on the distance to the neighborhood plane
    private static HashMap<Point3D, Double> findWeight(Neighborhood list, double accuracy) {
        HashMap<Point3D, Double> results = new HashMap<>();
        list.getNeighbors().forEach((point) -> {
            double dist = dist(point, list.getPlane());
            double weight = (dist <= accuracy) ? 1 : (accuracy / dist);
            results.put(point, weight);
        });
        return results;
    }

    //Finds point attributes
    public static ArrayList<Attribute> findAttributes(Point3D origin1, Point3D origin2, ArrayList<Neighborhood> neighborhoodList) {
        ArrayList<Attribute> attributeList = new ArrayList<>();
        //Iterates though each neighborhood
        neighborhoodList.forEach((neighborhood) -> {
            Plane plane = neighborhood.getPlane();
            //Find the neighborhood distance to origin points
            double x = dist(origin1, plane);
            double y = dist(origin2, plane);
            //Set it as the attribute
            Point2D attribute = new Point2D(x, y);
            //Assign the attribute to all points in the neighborhood
            neighborhood.getNeighbors().forEach((point) -> {
                attributeList.add(new Attribute(point, attribute));
            });
        });
        return attributeList;
    }

    //Add all the points to accumulator array
    public static HashMap<Point2D, ArrayList<Point3D>> setAccumulatorArray(ArrayList<Attribute> attributeList, double acc, double maxDist) {
        HashMap<Point2D, ArrayList<Point3D>> accumulatorArray = new HashMap<>();

        //Iterate though all known point-attribute combos
        attributeList.forEach((entry) -> {
            //Get the attribute of the point
            double pointX = entry.getAttribute().getX();
            double pointY = entry.getAttribute().getY();
            //Find a plane in the accumulator array
            for (double i = 0; i <= Math.ceil(pointX + 1.0); i += acc * 2) {
                if (pointX >= i && pointX < i + acc * 2) {
                    for (double j = 0; j < Math.ceil(pointY + 1.0); j+= acc * 2) {
                        if (pointY >= j && pointY < j + acc * 2) {
                            //Define a cell
                            Point2D currentPoint = new Point2D(i, j);
                            //If the cell does not exist, create one. Then add the point to the cell.
                            if (!accumulatorArray.containsKey(currentPoint)) {
                                accumulatorArray.put(currentPoint, new ArrayList<>());                                
                            }
                            accumulatorArray.get(currentPoint).add(entry.getPoint());
                        }
                    }
                }
            }
        });
        //Remove duplicate points in accumulator array if a cell them
        for (HashMap.Entry<Point2D, ArrayList<Point3D>> entry : accumulatorArray.entrySet()) {
            removeDuplicates(entry.getValue());
        }
        return accumulatorArray;
    }

    //Find a top peak in the accumulator array
    public static TopPeak findTopPeak(HashMap<Point2D, ArrayList<Point3D>> accumulatorArray) {
        int count = 0;
        Point2D peak = null;
        //Iterate though all the peaks in accumulator array
        for (HashMap.Entry<Point2D, ArrayList<Point3D>> entry : accumulatorArray.entrySet()) {
            //If the peak size is the known largest, save its size and key
            if (entry.getValue().size() > count) {
                count = entry.getValue().size();
                peak = entry.getKey();
            }
        }
        return new TopPeak(peak, count);
    }

    //Check the coplanarity of a list of points
    public static boolean coPlanarityCheck(ArrayList<Point3D> pointList, double accuracy) {
        Plane plane = findPlane(pointList);
        double rms = 0;

        //Find the mean root average of all the point distances the plane 
        rms = pointList.stream().map((point) -> Math.pow(dist(point, plane), 2)).reduce(rms, (accumulator, _item) -> accumulator + _item);
        rms /= pointList.size();
        rms /= Math.sqrt(rms);

        //Return true if the mean root average is lower or equal to the accuracy
        return rms <= accuracy;
    }

    //Find the distance of a point to the plane
    private static double dist(Point3D point, Plane plane) {
        double dist = (Math.abs((point.getX() * plane.getX()) + (point.getY() * plane.getY()) + (point.getZ() * plane.getZ()) - plane.getDistance()))
                / (Math.sqrt(Math.pow(plane.getX(), 2) + Math.pow(plane.getY(), 2) + Math.pow(plane.getZ(), 2)));
        return dist;
    }

    //Find the maximum value in the coordinate space
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

    //Find the minimum value in the coordinate space
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

    //Return a list of all the 3d points in a file
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

    //Find all the 3D point segments from a date file
    public static HashMap<ArrayList<Point3D>, Plane> findPoints(double distance, double accuracy, double proximity, int threshold, String path) throws FileNotFoundException, IOException {

        //Read points from a file
        ArrayList<Point3D> pointList = readPointsFromFile(path);
        //Create a list for the neighborhoods
        ArrayList<Neighborhood> neighborhoodList = new ArrayList<>();

        //Iterate though all the points in the list and define neighborhoods for all of them
        for (int i = 0; i < pointList.size(); i++) {
            Neighborhood neighborhood = findNeighborhood(pointList.get(i), pointList, distance, accuracy);
            //If a neighborhood is created, add it to the list
            if (neighborhood != null) {
                neighborhoodList.add(neighborhood);
            }
            System.out.println(i + ") " + pointList.get(i).toString() + " -> " + neighborhood);
        }

        //Define two origin points
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
        //Find attributes of all the points in the neighborhood list
        ArrayList<Attribute> attributeList = findAttributes(origin1, origin2, neighborhoodList);

        /*for (HashMap.Entry<Point3D, Point2D> entry : attributeList.entrySet()) {
            System.out.println(entry.getKey().toString() + " -> " + entry.getValue().toString());
        }*/
        
        //Create the accumulator array
        HashMap<Point2D, ArrayList<Point3D>> accumulatorArray = setAccumulatorArray(attributeList, accuracy, 2 * min.distance(max) / 3);
        
        /*accumulatorArray.forEach((k, v) -> {
            System.out.println("key: " + k.toString() + " value:" + v.toString());
        });*/

        //Create a map for all the segements and planes they are located
        HashMap<ArrayList<Point3D>, Plane> recorded = new HashMap<>();
        int peakCount = Integer.MAX_VALUE;
        //Iterate while max peak value in the accumulator array is larger than the threshold
        while (peakCount > threshold) {
            //Find the top peak
            TopPeak peak = findTopPeak(accumulatorArray);
            peakCount = peak.getCount();
            System.out.println(peakCount);
            //Check if peak size is larger than the threshold
            if (peakCount > threshold) {
                //Check the coplanarity
                if (coPlanarityCheck(accumulatorArray.get(peak.getPeak()), accuracy)) {
                    //If there's no coplanarity, find the cluster of the top peak
                    ArrayList<Point3D> cluster = findCluster(accumulatorArray, peak.getPeak(), accumulatorArray.get(peak.getPeak()), accuracy, proximity);
                    //Put the cluster in the segment map
                    recorded.put(cluster, findPlane(cluster));
                } else {
                    //If there's coplanarity, find a new peak within the old peak
                    moveOrigins(origin1, origin2, peak, accuracy, proximity, distance, min, max, threshold, accumulatorArray, recorded);
                }
            }
        }
        return recorded;
    }

    //Find a cluster within a peak with coplanarity
    public static void moveOrigins(Point3D origin1, Point3D origin2, TopPeak peak, double accuracy, double proximity, double distance, Point3D min, Point3D max, int threshold, HashMap<Point2D, ArrayList<Point3D>> accumulatorArray, HashMap<ArrayList<Point3D>, Plane> recorded) {
        System.out.print(">");
        Random r = new Random();
        double range = 5;
        int iterations = 5;
        HashMap<Point2D, ArrayList<Point3D>> accumulatorArrayNew = accumulatorArray;
        TopPeak peakNew = peak;

        //Iterate to find a new peak without coplanarity
        for (int i = 0; i < iterations; i++) {
            
            //Define new neighborhoods
            ArrayList<Neighborhood> neighborhoodList = new ArrayList<>();
            for (Point3D point : accumulatorArrayNew.get(peakNew.getPeak())) {
                Neighborhood neighborhood = findNeighborhood(point, accumulatorArrayNew.get(peakNew.getPeak()), distance, accuracy);
                if (neighborhood != null) {
                    neighborhoodList.add(neighborhood);
                }
            }

            //Find new origin points
            Point3D originNew1 = origin1.add(r.nextDouble() * range, r.nextDouble() * range, r.nextDouble() * range);
            Point3D originNew2 = origin2.add(r.nextDouble() * range, r.nextDouble() * range, r.nextDouble() * range);

            //Define the new attributes
            ArrayList<Attribute> attributeListNew = findAttributes(originNew1, originNew2, neighborhoodList);
            //Create the accumulator array
            accumulatorArrayNew = setAccumulatorArray(attributeListNew, accuracy, 2 * min.distance(max) / 3);
            //Find the new top peak
            peakNew = findTopPeak(accumulatorArrayNew);

            //Check the coplanarity
            if (coPlanarityCheck(accumulatorArrayNew.get(peakNew.getPeak()), accuracy)) {
                int peakCountNew = peak.getCount();
                //Check if peak size is larger than the threshold
                if (peakCountNew > threshold) {
                    //Find the cluster of the points within the old accumulator array
                    ArrayList<Point3D> cluster = findCluster(accumulatorArray, peak.getPeak(), accumulatorArrayNew.get(peakNew.getPeak()), accuracy, proximity);
                    //Put the cluster in the segment map
                    recorded.put(cluster, findPlane(cluster));
                }
                break;
            }
            //If max iteration count has been reached, remove the points within the last found peak
            if (i == iterations - 1) {
                accumulatorArray.get(peak.getPeak()).removeAll(accumulatorArrayNew.get(peakNew.getPeak()));
            }
        }
    }

    //Find a cluster within a peak without coplanarity
    public static ArrayList<Point3D> findCluster(HashMap<Point2D, ArrayList<Point3D>> accumulatorArray, Point2D peakKey, ArrayList<Point3D> peakValue, double accuracy, double proximity) {
        int k = 0;
        ArrayList<Point3D> cluster = new ArrayList<>();

        ArrayList<Point3D> toAdd = new ArrayList<>();
        toAdd.add(Point3D.ZERO);

        //Find all the points within the top peak
        ArrayList<Point3D> peakPoints = new ArrayList<>();
        for (Point3D neighborhood : peakValue) {
            peakPoints.add(neighborhood);
        }
        //Remove the duplicate points in the peak
        removeDuplicates(peakPoints);
        //Remove the points in the top peak of the accumulator array
        accumulatorArray.get(peakKey).removeAll(peakPoints);

        //Iterate until there are no new points to be added
        while (toAdd.size() > 0) {
            System.out.print("/");
            k++;            
            ArrayList<Point3D> neighboringPoints = new ArrayList<>();
            toAdd.clear();
            //Find the points in the neighboring cells
            for (double i = peakKey.getX() - (accuracy * 2 * k); i <= peakKey.getX() + (accuracy * 2 * k); i = i + accuracy * 2) {
                for (double j = peakKey.getY() - (accuracy * 2 * k); j <= peakKey.getY() + (accuracy * 2 * k); j = j + accuracy * 2) {
                    Point2D cell = new Point2D(i, j);
                    if (accumulatorArray.containsKey(cell)) {
                        neighboringPoints.addAll(accumulatorArray.get(cell));
                    }
                }
            }
            //Remove duplicate points in the neighboring cells
            removeDuplicates(neighboringPoints);
            ArrayList<Point3D> eligible = new ArrayList<>();

            //Find the points that pass the proximity check
            for (Point3D point : neighboringPoints) {
                for (Point3D peakPoint : peakPoints) {
                    if (point.distance(peakPoint) < proximity * 2) {
                        eligible.add(point);
                        break;
                    }
                }
            }

            //Find the points that also pass the coplanarity check
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

            //Remove the peak points and points to be added to the cluster from all other cells in accumulator array
            for (ArrayList<Point3D> points : accumulatorArray.values()) {
                points.removeAll(toAdd);
                points.removeAll(peakPoints);
            }
            //Add points to the cluster
            cluster.addAll(toAdd);
        }
        System.out.println("");
        //Add the peak points to the cluster
        cluster.addAll(peakPoints);
        //Remove duplicate points from the cluster
        removeDuplicates(cluster);
        return cluster;
    }

    //Removes duplicate points from the cluster
    public static void removeDuplicates(ArrayList<Point3D> neighboringPoints) {
        HashSet temp = new HashSet<>();
        temp.addAll(neighboringPoints);
        neighboringPoints.clear();
        neighboringPoints.addAll(temp);
    }
}
