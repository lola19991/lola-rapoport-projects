package com.arkanoid.model;

import java.awt.Color;
import java.awt.geom.Rectangle2D;

public final class Ball {
    private Vector2 position;
    private Vector2 velocity;
    private final double radius;
    private final Color color;

    public Ball(Vector2 position, Vector2 velocity, double radius, Color color) {
        this.position = position;
        this.velocity = velocity;
        this.radius = radius;
        this.color = color;
    }

    public Ball copyWithVelocity(Vector2 newVelocity) {
        return new Ball(position, newVelocity, radius, color);
    }

    public void move(double deltaSeconds) {
        position = position.add(velocity.scale(deltaSeconds));
    }

    public Rectangle2D.Double getBounds() {
        return new Rectangle2D.Double(position.x() - radius, position.y() - radius,
                radius * 2.0, radius * 2.0);
    }

    public double getSpeed() {
        return velocity.length();
    }

    public void setSpeed(double speed) {
        velocity = velocity.withLength(Math.min(GameConfig.MAX_BALL_SPEED,
                Math.max(GameConfig.MIN_BALL_SPEED, speed)));
    }

    public Vector2 getPosition() {
        return position;
    }

    public void setPosition(Vector2 position) {
        this.position = position;
    }

    public Vector2 getVelocity() {
        return velocity;
    }

    public void setVelocity(Vector2 velocity) {
        this.velocity = velocity;
    }

    public double getRadius() {
        return radius;
    }

    public Color getColor() {
        return color;
    }
}
