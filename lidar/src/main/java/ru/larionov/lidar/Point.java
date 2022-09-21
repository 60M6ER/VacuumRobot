package ru.larionov.lidar;

public class Point {
    private int distance;
    private double angle;

    public Point(int distance, double angle) {
        this.distance = distance;
        this.angle = angle;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public double getAngle() {
        return angle;
    }

    public void setAngle(double angle) {
        this.angle = angle;
    }

    public int getIntAngle() {
        return (int) angle;
    }
}
