package com.arkanoid.model;

public enum Difficulty {
    EASY("Easy", 5, 0.86, 1.15, 1.25, 0.78),
    MEDIUM("Medium", 3, 1.0, 1.0, 1.0, 1.0),
    HARD("Hard", 2, 1.18, 0.88, 0.7, 1.35);

    private final String label;
    private final int initialLives;
    private final double speedMultiplier;
    private final double paddleWidthMultiplier;
    private final double powerUpMultiplier;
    private final double toughnessMultiplier;

    Difficulty(String label, int initialLives, double speedMultiplier,
               double paddleWidthMultiplier, double powerUpMultiplier,
               double toughnessMultiplier) {
        this.label = label;
        this.initialLives = initialLives;
        this.speedMultiplier = speedMultiplier;
        this.paddleWidthMultiplier = paddleWidthMultiplier;
        this.powerUpMultiplier = powerUpMultiplier;
        this.toughnessMultiplier = toughnessMultiplier;
    }

    public String getLabel() {
        return label;
    }

    public int getInitialLives() {
        return initialLives;
    }

    public double getSpeedMultiplier() {
        return speedMultiplier;
    }

    public double getPaddleWidthMultiplier() {
        return paddleWidthMultiplier;
    }

    public double getPowerUpMultiplier() {
        return powerUpMultiplier;
    }

    public double getToughnessMultiplier() {
        return toughnessMultiplier;
    }
}
