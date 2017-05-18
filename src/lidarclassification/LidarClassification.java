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
import lidarclassification.classes.Neighborhood;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.shape.Sphere;
import javafx.scene.PerspectiveCamera;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.stage.Stage;
import lidarclassification.classes.Plane;
import lidarclassification.classes.Xform;
import javafx.event.EventHandler;
import static javafx.scene.input.KeyCode.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;
import javafx.scene.Node;

/**
 *
 * @author JustinasK
 */
public class LidarClassification extends Application {

    final Group root = new Group();
    final Group axisGroup = new Group();
    final Xform world = new Xform();
    final PerspectiveCamera camera = new PerspectiveCamera(true);
    final Xform cameraXform = new Xform();
    final Xform cameraXform2 = new Xform();
    final Xform cameraXform3 = new Xform();
    final double cameraDistance = 20;
    final Xform pointGroup = new Xform();

    private Timeline timeline;
    boolean timelinePlaying = false;
    double ONE_FRAME = 1.0 / 24.0;
    double DELTA_MULTIPLIER = 200.0;
    double CONTROL_MULTIPLIER = 0.1;
    double SHIFT_MULTIPLIER = 0.1;
    double ALT_MULTIPLIER = 0.5;

    double mousePosX;
    double mousePosY;
    double mouseOldX;
    double mouseOldY;
    double mouseDeltaX;
    double mouseDeltaY;

    private void handleMouse(Scene scene, final Node root) {
        scene.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent me) {
                mousePosX = me.getSceneX();
                mousePosY = me.getSceneY();
                mouseOldX = me.getSceneX();
                mouseOldY = me.getSceneY();
            }
        });
        scene.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent me) {
                mouseOldX = mousePosX;
                mouseOldY = mousePosY;
                mousePosX = me.getSceneX();
                mousePosY = me.getSceneY();
                mouseDeltaX = (mousePosX - mouseOldX);
                mouseDeltaY = (mousePosY - mouseOldY);

                double modifier = 1.0;
                double modifierFactor = 0.1;

                if (me.isControlDown()) {
                    modifier = 0.1;
                }
                if (me.isShiftDown()) {
                    modifier = 10.0;
                }
                if (me.isPrimaryButtonDown()) {
                    cameraXform.ry.setAngle(cameraXform.ry.getAngle() - mouseDeltaX * modifierFactor * modifier * 2.0);  // +
                    cameraXform.rx.setAngle(cameraXform.rx.getAngle() + mouseDeltaY * modifierFactor * modifier * 2.0);  // -
                } else if (me.isSecondaryButtonDown()) {
                    double z = camera.getTranslateZ();
                    double newZ = z + mouseDeltaX * modifierFactor * modifier;
                    camera.setTranslateZ(newZ);
                } else if (me.isMiddleButtonDown()) {
                    cameraXform2.t.setX(cameraXform2.t.getX() + mouseDeltaX * modifierFactor * modifier * 0.3);  // -
                    cameraXform2.t.setY(cameraXform2.t.getY() + mouseDeltaY * modifierFactor * modifier * 0.3);  // -
                }
            }
        });
    }

    private void handleKeyboard(Scene scene, final Node root) {
        final boolean moveCamera = true;
        scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                Duration currentTime;
                switch (event.getCode()) {
                    case Z:
                        if (event.isShiftDown()) {
                            cameraXform.ry.setAngle(0.0);
                            cameraXform.rx.setAngle(0.0);
                            camera.setTranslateZ(-300.0);
                        }
                        cameraXform2.t.setX(0.0);
                        cameraXform2.t.setY(0.0);
                        break;
                    case X:
                        if (event.isControlDown()) {
                            if (axisGroup.isVisible()) {
                                System.out.println("setVisible(false)");
                                axisGroup.setVisible(false);
                            } else {
                                System.out.println("setVisible(true)");
                                axisGroup.setVisible(true);
                            }
                        }
                        break;
                    case S:
                        if (event.isControlDown()) {
                            if (pointGroup.isVisible()) {
                                pointGroup.setVisible(false);
                            } else {
                                pointGroup.setVisible(true);
                            }
                        }
                        break;
                    case SPACE:
                        if (timelinePlaying) {
                            timeline.pause();
                            timelinePlaying = false;
                        } else {
                            timeline.play();
                            timelinePlaying = true;
                        }
                        break;
                    case UP:
                        if (event.isControlDown() && event.isShiftDown()) {
                            cameraXform2.t.setY(cameraXform2.t.getY() - 10.0 * CONTROL_MULTIPLIER);
                        } else if (event.isAltDown() && event.isShiftDown()) {
                            cameraXform.rx.setAngle(cameraXform.rx.getAngle() - 10.0 * ALT_MULTIPLIER);
                        } else if (event.isControlDown()) {
                            cameraXform2.t.setY(cameraXform2.t.getY() - 1.0 * CONTROL_MULTIPLIER);
                        } else if (event.isAltDown()) {
                            cameraXform.rx.setAngle(cameraXform.rx.getAngle() - 2.0 * ALT_MULTIPLIER);
                        } else if (event.isShiftDown()) {
                            double z = camera.getTranslateZ();
                            double newZ = z + 5.0 * SHIFT_MULTIPLIER;
                            camera.setTranslateZ(newZ);
                        }
                        break;
                    case DOWN:
                        if (event.isControlDown() && event.isShiftDown()) {
                            cameraXform2.t.setY(cameraXform2.t.getY() + 10.0 * CONTROL_MULTIPLIER);
                        } else if (event.isAltDown() && event.isShiftDown()) {
                            cameraXform.rx.setAngle(cameraXform.rx.getAngle() + 10.0 * ALT_MULTIPLIER);
                        } else if (event.isControlDown()) {
                            cameraXform2.t.setY(cameraXform2.t.getY() + 1.0 * CONTROL_MULTIPLIER);
                        } else if (event.isAltDown()) {
                            cameraXform.rx.setAngle(cameraXform.rx.getAngle() + 2.0 * ALT_MULTIPLIER);
                        } else if (event.isShiftDown()) {
                            double z = camera.getTranslateZ();
                            double newZ = z - 5.0 * SHIFT_MULTIPLIER;
                            camera.setTranslateZ(newZ);
                        }
                        break;
                    case RIGHT:
                        if (event.isControlDown() && event.isShiftDown()) {
                            cameraXform2.t.setX(cameraXform2.t.getX() + 10.0 * CONTROL_MULTIPLIER);
                        } else if (event.isAltDown() && event.isShiftDown()) {
                            cameraXform.ry.setAngle(cameraXform.ry.getAngle() - 10.0 * ALT_MULTIPLIER);
                        } else if (event.isControlDown()) {
                            cameraXform2.t.setX(cameraXform2.t.getX() + 1.0 * CONTROL_MULTIPLIER);
                        } else if (event.isAltDown()) {
                            cameraXform.ry.setAngle(cameraXform.ry.getAngle() - 2.0 * ALT_MULTIPLIER);
                        }
                        break;
                    case LEFT:
                        if (event.isControlDown() && event.isShiftDown()) {
                            cameraXform2.t.setX(cameraXform2.t.getX() - 10.0 * CONTROL_MULTIPLIER);
                        } else if (event.isAltDown() && event.isShiftDown()) {
                            cameraXform.ry.setAngle(cameraXform.ry.getAngle() + 10.0 * ALT_MULTIPLIER);  // -
                        } else if (event.isControlDown()) {
                            cameraXform2.t.setX(cameraXform2.t.getX() - 1.0 * CONTROL_MULTIPLIER);
                        } else if (event.isAltDown()) {
                            cameraXform.ry.setAngle(cameraXform.ry.getAngle() + 2.0 * ALT_MULTIPLIER);  // -
                        }
                        break;
                }
            }
        });
    }

    public Neighborhood findNeighborhood(Point3D center, ArrayList<Point3D> list, double distance, double accuracy) {
        Neighborhood neighborhood = findNeighborPoints(center, list, distance);
        if (neighborhood.getNeighbors().size() < 3) {
            return null;
        }
        ArrayList weights = new ArrayList<>();
        for (int i = 0; i < neighborhood.getNeighbors().size(); i++) {
            weights.add(1.0);
        }
        Plane plane = findPlaneWithWeights(neighborhood, weights);
        neighborhood.setPlane(plane);
        for (int i = 0; i < 10; i++) {
            weights = findWeight(neighborhood, accuracy);
            Plane planeNew = findPlaneWithWeights(neighborhood, weights);
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

    public Neighborhood findNeighborPoints(Point3D center, ArrayList<Point3D> list, double distance) {
        Neighborhood result = new Neighborhood(center);
        for (int i = 0; i < list.size(); i++) {
            Point3D point = list.get(i);
            if (Ops.distance(center, point) <= distance) {
                result.getNeighbors().add(point);
            }
        }
        return result;
    }

    public Plane findPlaneWithWeights(Neighborhood hood, ArrayList<Double> weights) {
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

    public Plane findPlane(ArrayList<Point3D> pointList) {
        double x = 0;
        double y = 0;
        double z = 0;
        double w = 0;
        int size = pointList.size();

        if (size >= 3) {
            for (int i = 0; i < size; i++) {
                Point3D a = pointList.get(i);
                x += a.getX();
                y += a.getY();
                z += a.getZ();
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

            for (int i = 0; i < size; i++) {
                Point3D a = pointList.get(i);
                x = a.getX() - centroid.getX();
                y = a.getY() - centroid.getY();
                z = a.getZ() - centroid.getZ();
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
                        centroid.getX() * Ops.xCos(dir) + centroid.getY() * Ops.yCos(dir) + centroid.getZ() * Ops.zCos(dir));
            } else if (detY >= detX && detY >= detZ) {
                double a = (yz * xz - xy * zz) / detY;
                double b = (xy * xz - yz * xx) / detY;
                Point3D dir = new Point3D(a, 1.0, b);
                return new Plane(
                        Ops.xCos(dir), Ops.yCos(dir), Ops.zCos(dir),
                        centroid.getX() * Ops.xCos(dir) + centroid.getY() * Ops.yCos(dir) + centroid.getZ() * Ops.zCos(dir));
            } else {
                double a = (yz * xy - xz * yy) / detZ;
                double b = (xz * xy - yz * xx) / detZ;
                Point3D dir = new Point3D(a, b, 1.0);
                return new Plane(
                        Ops.xCos(dir), Ops.yCos(dir), Ops.zCos(dir),
                        centroid.getX() * Ops.xCos(dir) + centroid.getY() * Ops.yCos(dir) + centroid.getZ() * Ops.zCos(dir));
            }
        }
        return null;
    }

    public ArrayList<Double> findWeight(Neighborhood list, double accuracy) {
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

    public ArrayList<Point2D> findAttributes(Point3D origin1, Point3D origin2, ArrayList<Neighborhood> neighborhoodList) {
        ArrayList<Point2D> attributeList = new ArrayList<>();
        for (int i = 0; i < neighborhoodList.size(); i++) {
            Plane plane = neighborhoodList.get(i).getPlane();
            double x = dist(origin1, plane);
            double y = dist(origin2, plane);
            attributeList.add(new Point2D(x, y));
        }
        return attributeList;
    }

    /*public HashMap<Point2D, ArrayList<Neighborhood>> setAccumulatorArray(ArrayList<Point2D> attributeList, ArrayList<Neighborhood> neighborhoodList, double acc, double maxDist) {

        HashMap<Point2D, ArrayList<Neighborhood>> accumulatorArray = new HashMap<>();

        for (int k = 0; k < attributeList.size(); k++) {
            double pointX = attributeList.get(k).getX();
            double pointY = attributeList.get(k).getY();
            for (double i = Math.floor(pointX); i <= Math.ceil(pointX); i = i + acc) {
                if (pointX >= i && pointX < i + acc) {
                    for (double j = Math.floor(pointY); j < Math.ceil(pointY); j = j + acc) {
                        if (pointY >= j && pointY < j + acc) {
                            Point2D currentPoint = new Point2D(i, j);
                            if (!accumulatorArray.containsKey(currentPoint)) {
                                accumulatorArray.put(currentPoint, new ArrayList<>());
                                accumulatorArray.get(currentPoint).add(neighborhoodList.get(k));
                            } else {
                                accumulatorArray.get(currentPoint).add(neighborhoodList.get(k));
                            }
                        }
                    }
                }
            }
        }
        return accumulatorArray;
    }*/
    public HashMap<Point2D, ArrayList<Point3D>> setAccumulatorArray(ArrayList<Point2D> attributeList, ArrayList<Neighborhood> neighborhoodList, double acc, double maxDist) {

        HashMap<Point2D, ArrayList<Point3D>> accumulatorArray = new HashMap<>();

        for (int k = 0; k < attributeList.size(); k++) {
            double pointX = attributeList.get(k).getX();
            double pointY = attributeList.get(k).getY();
            for (double i = Math.floor(pointX); i <= Math.ceil(pointX); i = i + acc) {
                if (pointX >= i && pointX < i + acc) {
                    for (double j = Math.floor(pointY); j < Math.ceil(pointY); j = j + acc) {
                        if (pointY >= j && pointY < j + acc) {
                            Point2D currentPoint = new Point2D(i, j);
                            if (!accumulatorArray.containsKey(currentPoint)) {
                                accumulatorArray.put(currentPoint, new ArrayList<>());
                                accumulatorArray.get(currentPoint).addAll(neighborhoodList.get(k).getNeighbors());
                            } else {
                                accumulatorArray.get(currentPoint).addAll(neighborhoodList.get(k).getNeighbors());
                            }
                        }
                    }
                }
            }
        }
        return accumulatorArray;
    }

    /*public HashMap.Entry<Point2D, ArrayList<Neighborhood>> findTopPeak(HashMap<Point2D, ArrayList<Neighborhood>> accumulatorArray) {
        int count = 0;
        HashMap.Entry<Point2D, ArrayList<Neighborhood>> topPeak = null;
        for (HashMap.Entry<Point2D, ArrayList<Neighborhood>> entry : accumulatorArray.entrySet()) {
            if (entry.getValue().size() > count) {
                count = entry.getValue().size();
                topPeak = entry;
            }
        }
        return topPeak;
    }*/
    public HashMap.Entry<Point2D, ArrayList<Point3D>> findTopPeak(HashMap<Point2D, ArrayList<Point3D>> accumulatorArray) {
        int count = 0;
        HashMap.Entry<Point2D, ArrayList<Point3D>> topPeak = null;
        for (HashMap.Entry<Point2D, ArrayList<Point3D>> entry : accumulatorArray.entrySet()) {
            if (entry.getValue().size() > count) {
                count = entry.getValue().size();
                topPeak = entry;
            }
        }
        return topPeak;
    }

    /*public boolean coPlanarityCheck(ArrayList<Neighborhood> neighborhoodList, double accuracy) {
        Neighborhood neighborhood = new Neighborhood();
        for (int i = 0; i < neighborhoodList.size(); i++) {
            neighborhood.getNeighbors().addAll(neighborhoodList.get(i).getNeighbors());
        }
        Plane plane = findPlane(neighborhood);
        double rms = 0;

        for (int i = 0; i < neighborhood.getNeighbors().size(); i++) {
            rms += Math.pow(dist(neighborhood.getNeighbors().get(i), plane), 2);
        }
        rms /= neighborhood.getNeighbors().size();
        rms /= Math.sqrt(rms);
        System.out.println(rms);

        return rms <= accuracy;
    }*/
    public boolean coPlanarityCheck(ArrayList<Point3D> pointList, double accuracy) {
        Neighborhood neighborhood = new Neighborhood();
        for (int i = 0; i < pointList.size(); i++) {
            neighborhood.getNeighbors().addAll(pointList);
        }
        Plane plane = findPlane(neighborhood.getNeighbors());
        double rms = 0;

        for (int i = 0; i < neighborhood.getNeighbors().size(); i++) {
            rms += Math.pow(dist(neighborhood.getNeighbors().get(i), plane), 2);
        }
        rms /= neighborhood.getNeighbors().size();
        rms /= Math.sqrt(rms);
        System.out.println(rms);

        return rms <= accuracy;
    }

    private double dist(Point3D point, Plane plane) {
        double dist = (Math.abs((point.getX() * plane.getX()) + (point.getY() * plane.getY()) + (point.getZ() * plane.getZ()) - plane.getDistance()))
                / (Math.sqrt(Math.pow(plane.getX(), 2) + Math.pow(plane.getY(), 2) + Math.pow(plane.getZ(), 2)));
        return dist;
    }

    private Point3D getMin3DPoint(ArrayList<Point3D> list) {
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

    private Point3D getMax3DPoint(ArrayList<Point3D> list) {
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

    private ArrayList<Point3D> readPointsFromFile(String path) throws FileNotFoundException, IOException {
        File file = new File(path);
        BufferedReader fr = new BufferedReader(new FileReader(file));
        ArrayList<Point3D> array = new ArrayList<>();
        String text = null;
        while ((text = fr.readLine()) != null) {
            String[] nums = text.split(" ");
            double x = Double.parseDouble(nums[0]);
            double y = Double.parseDouble(nums[1]);
            double z = Double.parseDouble(nums[2]);
            array.add(new Point3D(x, y, z));
        }
        return array;
    }

    public ArrayList<Point3D> findPoints(String path) throws FileNotFoundException, IOException {
        double distance = 0.5;
        double accuracy = 0.001;
        int threshold = 3;

        ArrayList<Point3D> pointList = readPointsFromFile(path);
        ArrayList<Neighborhood> neighborhoodList = new ArrayList<>();

        for (int i = 0; i < pointList.size(); i++) {
            Neighborhood neighborhood = findNeighborhood(pointList.get(i), pointList, distance, accuracy);
            if (neighborhood != null) {
                neighborhoodList.add(neighborhood);
            }
            System.out.println(i + ") " + pointList.get(i).toString() + " -> ");
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
        HashMap<Point2D, ArrayList<Point3D>> accumulatorArray = setAccumulatorArray(attributeList, neighborhoodList, accuracy, 2 * Ops.distance(min, max) / 3);
        accumulatorArray.forEach((k, v) -> {
            HashSet<Point3D> set = new HashSet<>();
            set.addAll(v);
            v.clear();
            v.addAll(set);
            System.out.println("key: " + k.toString() + " value:" + v.toString());
        });

        ArrayList<Point3D> recorded = new ArrayList<>();
        int peakCount = Integer.MAX_VALUE;
        while (peakCount > threshold) {
            HashMap.Entry<Point2D, ArrayList<Point3D>> peak = findTopPeak(accumulatorArray);
            System.out.println(peak.toString());

            
            peakCount = peak.getValue().size();

            if (peakCount > threshold) {
                if (coPlanarityCheck(peak.getValue(), accuracy)) {
                    clustering(accumulatorArray, peak, accuracy, recorded);
                } else{
                    accumulatorArray.remove(peak.getKey());
                }
            }
        }
        return recorded;
    }

    public void clustering(HashMap<Point2D, ArrayList<Point3D>> accumulatorArray, Map.Entry<Point2D, ArrayList<Point3D>> peak, double accuracy, ArrayList<Point3D> recorded) {
        int k = 0;
        int neighborCount = 0;
        
        do {
            k++;
            neighborCount = 0;
            ArrayList<Point3D> neighboringPoints = new ArrayList<>();
            ArrayList<Point3D> eligible = new ArrayList<>();
            for (HashMap.Entry<Point2D, ArrayList<Point3D>> entry : accumulatorArray.entrySet()) {
                if (entry.getKey().getX() >= peak.getKey().getX() - accuracy * k
                        && entry.getKey().getX() >= peak.getKey().getX() - accuracy * k
                        && entry.getKey().getX() <= peak.getKey().getX() + accuracy * k
                        && entry.getKey().getY() <= peak.getKey().getY() + accuracy * k
                        && entry.getKey() != peak.getKey()) {
                    neighboringPoints.addAll(entry.getValue());
                    neighborCount++;
                    System.out.println(entry.getKey().toString() + " -> " + entry.getValue().toString());
                }
            }
            for (int i = 0; i < neighboringPoints.size(); i++) {
                double dist = Double.MAX_VALUE;
                int j = 0;
                while (dist > accuracy * 2 && j < peak.getValue().size()) {
                    if ((dist = Ops.distance(neighboringPoints.get(i), peak.getValue().get(j))) < accuracy * 2) {
                        eligible.add(neighboringPoints.get(i));
                    }
                    j++;
                }
            }
            System.out.println(eligible.toString());
            Plane peakPlane = findPlane(peak.getValue());
            for (int i = 0; i < eligible.size(); i++) {
                if (dist(eligible.get(i), peakPlane) < accuracy * 2 && !peak.getValue().contains(eligible.get(i))) {
                    peak.getValue().add(eligible.get(i));
                }
            }
        } while (k < 10);
        System.out.println(peak.getValue().size());
        recorded.addAll(peak.getValue());
        Set<Point3D> set = new HashSet<>();
        set.addAll(recorded);
        recorded.clear();
        recorded.addAll(set);
        accumulatorArray.remove(peak.getKey());
    }

    public static void main(String[] args) {
        Application.launch(args);
    }

    private void buildScene() {
        System.out.println("buildScene");
        root.getChildren().add(world);
    }

    private void buildCamera() {
        root.getChildren().add(cameraXform);
        cameraXform.getChildren().add(cameraXform2);
        cameraXform2.getChildren().add(cameraXform3);
        cameraXform3.getChildren().add(camera);
        cameraXform3.setRotateZ(180.0);

        camera.setNearClip(0.1);
        camera.setFarClip(10000.0);
        camera.setTranslateZ(-cameraDistance);
        cameraXform.setTranslateX(-17.5);
        cameraXform.setTranslateY(-20);
        cameraXform.rz.setAngle(0);
        cameraXform.ry.setAngle(0);
        cameraXform.rx.setAngle(115);
    }

    private void buildAxes() {
        System.out.println("buildAxes()");
        final PhongMaterial redMaterial = new PhongMaterial();
        redMaterial.setDiffuseColor(Color.DARKRED);
        redMaterial.setSpecularColor(Color.RED);

        final PhongMaterial greenMaterial = new PhongMaterial();
        greenMaterial.setDiffuseColor(Color.DARKGREEN);
        greenMaterial.setSpecularColor(Color.GREEN);

        final PhongMaterial blueMaterial = new PhongMaterial();
        blueMaterial.setDiffuseColor(Color.DARKBLUE);
        blueMaterial.setSpecularColor(Color.BLUE);

        final Box xAxis = new Box(240.0, 0.01, 0.01);
        final Box yAxis = new Box(0.01, 240.0, 0.01);
        final Box zAxis = new Box(0.01, 0.01, 240.0);

        xAxis.setMaterial(redMaterial);
        yAxis.setMaterial(greenMaterial);
        zAxis.setMaterial(blueMaterial);

        axisGroup.getChildren().addAll(xAxis, yAxis, zAxis);
        world.getChildren().addAll(axisGroup);
    }

    private void buildPoints() throws FileNotFoundException, IOException {
        PhongMaterial black = new PhongMaterial();
        black.setDiffuseColor(Color.BLACK);
        black.setSpecularColor(Color.BLACK);
        ArrayList<Point3D> pointList = findPoints("C:\\Users\\user\\Documents\\NetBeansProjects\\LidarClassification\\src\\lidarclassification\\resources\\test2-1.txt");

        for (int i = 0; i < pointList.size(); i++) {
            Sphere sphere = new Sphere(0.01);
            sphere.setMaterial(black);
            sphere.setTranslateX(pointList.get(i).getX());
            sphere.setTranslateY(pointList.get(i).getY());
            sphere.setTranslateZ(pointList.get(i).getZ());
            world.getChildren().add(sphere);
        }
    }

    @Override
    public void start(Stage primaryStage) throws FileNotFoundException, IOException {
        System.out.println("start");
        buildScene();
        buildCamera();
        buildAxes();
        buildPoints();

        Scene scene = new Scene(root, 1024, 768, true);
        scene.setFill(Color.WHITE);
        handleKeyboard(scene, world);
        handleMouse(scene, world);

        primaryStage.setTitle("Molecule Sample Application");
        primaryStage.setScene(scene);
        primaryStage.show();
        scene.setCamera(camera);
    }
}
