package com.arkanoid.model;

import java.awt.geom.Rectangle2D;

public final class PowerUp {
    private final PowerUpType type;
    private Vector2 position;

    public PowerUp(PowerUpType type, Vector2 position) {
        this.type = type;
        this.position = position;
    }

    public void update(double deltaSeconds) {
        position = position.add(new Vector2(0.0, GameConfig.POWER_UP_SPEED * deltaSeconds));
    }

    public Rectangle2D.Double getBounds() {
        double half = GameConfig.POWER_UP_SIZE / 2.0;
        return new Rectangle2D.Double(position.x() - half, position.y() - half,
                GameConfig.POWER_UP_SIZE, GameConfig.POWER_UP_SIZE);
    }

    public PowerUpType getType() {
        return type;
    }

    public Vector2 getPosition() {
        return position;
    }
}
