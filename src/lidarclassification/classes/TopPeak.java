/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lidarclassification.classes;

import javafx.geometry.Point2D;

/**
 *
 * @author JustinasK
 */
public class TopPeak {

    private int count = 0;
    private Point2D peak = null;

    public TopPeak(Point2D peak, int count) {
        this.count = count;
        this.peak = peak;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int n) {
        count = n;
    }

    public Point2D getPeak() {
        return peak;
    }

    public void setPeak(Point2D n) {
        peak = n;
    }
}
