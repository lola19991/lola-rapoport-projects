package com.arkanoid.model;

import java.awt.geom.Rectangle2D;

public final class Paddle {
    private final double y;
    private final double height;
    private final double boardLeft;
    private final double boardRight;
    private double x;
    private double width;
    private double baseWidth = GameConfig.PADDLE_WIDTH;

    public Paddle() {
        this(GameConfig.PADDLE_Y, GameConfig.PADDLE_HEIGHT,
                GameConfig.PLAY_LEFT, GameConfig.PLAY_RIGHT);
    }

    public Paddle(double y, double height, double boardLeft, double boardRight) {
        this.y = y;
        this.height = height;
        this.boardLeft = boardLeft;
        this.boardRight = boardRight;
        resetSize();
        reset();
    }

    public void setDifficulty(Difficulty difficulty) {
        baseWidth = GameConfig.PADDLE_WIDTH * difficulty.getPaddleWidthMultiplier();
        resetSize();
        reset();
    }

    public void update(double deltaSeconds, InputState input) {
        double direction = 0.0;
        if (input.isLeftPressed()) {
            direction -= 1.0;
        }
        if (input.isRightPressed()) {
            direction += 1.0;
        }
        setX(x + direction * GameConfig.PADDLE_SPEED * deltaSeconds);
    }

    public void reset() {
        x = boardLeft + (boardRight - boardLeft - width) / 2.0;
    }

    public void resetSize() {
        width = baseWidth;
        setX(x);
    }

    public void expand() {
        double center = getCenterX();
        width = Math.min(boardRight - boardLeft, baseWidth + GameConfig.PADDLE_EXPAND_AMOUNT);
        setX(center - width / 2.0);
    }

    public Vector2 bounceVelocityFor(double ballCenterX, double speed) {
        double hitRatio = (ballCenterX - getCenterX()) / (width / 2.0);
        hitRatio = Math.max(-1.0, Math.min(1.0, hitRatio));
        double angle = Math.toRadians(hitRatio * GameConfig.MAX_PADDLE_BOUNCE_DEGREES);
        return new Vector2(Math.sin(angle), -Math.cos(angle)).withLength(speed);
    }

    public Rectangle2D.Double getBounds() {
        return new Rectangle2D.Double(x, y, width, height);
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = Math.max(boardLeft, Math.min(boardRight - width, x));
    }

    public double getY() {
        return y;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public double getCenterX() {
        return x + width / 2.0;
    }
}
