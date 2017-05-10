/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lidarclassification;

import lidarclassification.classes.Point3d;

/**
 *
 * @author JustinasK
 */
public class Ops {

    public static double distance(Point3d a) {
        return Math.sqrt(Math.pow(a.getX(), 2) + Math.pow(a.getY(), 2) + Math.pow(a.getZ(), 2));
    }

    public static double distance(Point3d a, Point3d b) {
        return Math.sqrt(Math.pow(a.getX() - b.getX(), 2) + Math.pow(a.getY() - b.getY(), 2) + Math.pow(a.getZ() - b.getZ(), 2));
    }

    public static double distance2(Point3d a) {
        return a.getX() * xCos(a) + a.getY() * yCos(a) + a.getZ() * zCos(a);
    }

    public static double distance2(Point3d a, Point3d b) {
        return (b.getX() - a.getX()) * xCos(a, b) + (b.getY() - a.getY()) * yCos(a, b) + (b.getZ() - a.getZ()) * zCos(a, b);
    }

    public static double xCos(Point3d a) {
        return a.getX() / Math.sqrt(Math.pow(a.getX(), 2) + Math.pow(a.getY(), 2) + Math.pow(a.getZ(), 2));
    }

    public static double xCos(Point3d a, Point3d b) {
        double deltaX = b.getX() - a.getX();
        double deltaY = b.getY() - a.getY();
        double deltaZ = b.getZ() - a.getZ();
        return deltaX / Math.sqrt(Math.pow(deltaX, 2) + Math.pow(deltaY, 2) + Math.pow(deltaZ, 2));
    }

    public static double yCos(Point3d a) {
        return a.getY() / Math.sqrt(Math.pow(a.getX(), 2) + Math.pow(a.getY(), 2) + Math.pow(a.getZ(), 2));
    }

    public static double yCos(Point3d a, Point3d b) {
        double deltaX = b.getX() - a.getX();
        double deltaY = b.getY() - a.getY();
        double deltaZ = b.getZ() - a.getZ();
        return deltaY / Math.sqrt(Math.pow(deltaX, 2) + Math.pow(deltaY, 2) + Math.pow(deltaZ, 2));
    }

    public static double zCos(Point3d a) {
        return a.getZ() / Math.sqrt(Math.pow(a.getX(), 2) + Math.pow(a.getY(), 2) + Math.pow(a.getZ(), 2));
    }

    public static double zCos(Point3d a, Point3d b) {
        double deltaX = b.getX() - a.getX();
        double deltaY = b.getY() - a.getY();
        double deltaZ = b.getZ() - a.getZ();
        return deltaZ / Math.sqrt(Math.pow(deltaX, 2) + Math.pow(deltaY, 2) + Math.pow(deltaZ, 2));
    }
}
