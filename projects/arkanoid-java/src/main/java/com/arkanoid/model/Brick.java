package com.arkanoid.model;

import java.awt.Color;
import java.awt.geom.Rectangle2D;

public final class Brick {
    private final Rectangle2D.Double bounds;
    private final Color color;
    private final int maxHitPoints;
    private final PowerUpType powerUpType;
    private int hitPoints;

    public Brick(BrickSpec spec) {
        this.bounds = new Rectangle2D.Double(spec.x(), spec.y(), spec.width(), spec.height());
        this.color = spec.color();
        this.maxHitPoints = Math.max(1, spec.hitPoints());
        this.hitPoints = maxHitPoints;
        this.powerUpType = spec.powerUpType();
    }

    public boolean hit() {
        if (hitPoints > 0) {
            hitPoints--;
        }
        return isDestroyed();
    }

    public boolean isDestroyed() {
        return hitPoints <= 0;
    }

    public int getScoreValue() {
        return 40 * maxHitPoints;
    }

    public Rectangle2D.Double getBounds() {
        return bounds;
    }

    public Color getColor() {
        return color;
    }

    public int getHitPoints() {
        return hitPoints;
    }

    public int getMaxHitPoints() {
        return maxHitPoints;
    }

    public PowerUpType getPowerUpType() {
        return powerUpType;
    }
}
