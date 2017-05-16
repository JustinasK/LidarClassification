/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lidarclassification;

import javafx.geometry.Point3D;

/**
 *
 * @author JustinasK
 */
public class Ops {

    public static double distance(Point3D a) {
        if (a.getX() == 0.0 && a.getY() == 0.0 && a.getZ() == 0.0) {
            return 0.0;
        } else {
            return a.getX() * xCos(a) + a.getY() * yCos(a) + a.getZ() * zCos(a);
        }
    }

    public static double distance(Point3D a, Point3D b) {
        if (a.equals(b)) {
            return 0.0;
        } else {
            return (b.getX() - a.getX()) * xCos(a, b) + (b.getY() - a.getY()) * yCos(a, b) + (b.getZ() - a.getZ()) * zCos(a, b);
        }
    }

    public static double xCos(Point3D a) {
        return a.getX() / Math.sqrt(Math.pow(a.getX(), 2) + Math.pow(a.getY(), 2) + Math.pow(a.getZ(), 2));
    }

    public static double xCos(Point3D a, Point3D b) {
        double deltaX = b.getX() - a.getX();
        double deltaY = b.getY() - a.getY();
        double deltaZ = b.getZ() - a.getZ();
        return deltaX / Math.sqrt(Math.pow(deltaX, 2) + Math.pow(deltaY, 2) + Math.pow(deltaZ, 2));
    }

    public static double yCos(Point3D a) {
        return a.getY() / Math.sqrt(Math.pow(a.getX(), 2) + Math.pow(a.getY(), 2) + Math.pow(a.getZ(), 2));
    }

    public static double yCos(Point3D a, Point3D b) {
        double deltaX = b.getX() - a.getX();
        double deltaY = b.getY() - a.getY();
        double deltaZ = b.getZ() - a.getZ();
        return deltaY / Math.sqrt(Math.pow(deltaX, 2) + Math.pow(deltaY, 2) + Math.pow(deltaZ, 2));
    }

    public static double zCos(Point3D a) {
        return a.getZ() / Math.sqrt(Math.pow(a.getX(), 2) + Math.pow(a.getY(), 2) + Math.pow(a.getZ(), 2));
    }

    public static double zCos(Point3D a, Point3D b) {
        double deltaX = b.getX() - a.getX();
        double deltaY = b.getY() - a.getY();
        double deltaZ = b.getZ() - a.getZ();
        return deltaZ / Math.sqrt(Math.pow(deltaX, 2) + Math.pow(deltaY, 2) + Math.pow(deltaZ, 2));
    }
}
