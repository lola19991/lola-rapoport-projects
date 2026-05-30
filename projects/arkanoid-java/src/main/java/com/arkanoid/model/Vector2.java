package com.arkanoid.model;

public record Vector2(double x, double y) {
    public Vector2 add(Vector2 other) {
        return new Vector2(x + other.x, y + other.y);
    }

    public Vector2 scale(double factor) {
        return new Vector2(x * factor, y * factor);
    }

    public double length() {
        return Math.hypot(x, y);
    }

    public Vector2 normalized() {
        double length = length();
        if (length == 0.0) {
            return new Vector2(0.0, 0.0);
        }
        return new Vector2(x / length, y / length);
    }

    public Vector2 withLength(double length) {
        return normalized().scale(length);
    }

    public Vector2 reflectX() {
        return new Vector2(-x, y);
    }

    public Vector2 reflectY() {
        return new Vector2(x, -y);
    }

    public Vector2 rotateDegrees(double degrees) {
        double radians = Math.toRadians(degrees);
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        return new Vector2(x * cos - y * sin, x * sin + y * cos);
    }

    public static Vector2 fromAngleDegrees(double degrees, double speed) {
        double radians = Math.toRadians(degrees);
        return new Vector2(Math.cos(radians), -Math.sin(radians)).scale(speed);
    }
}
