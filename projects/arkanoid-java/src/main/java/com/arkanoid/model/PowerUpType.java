package com.arkanoid.model;

import java.awt.Color;

public enum PowerUpType {
    EXPAND_PADDLE("W", new Color(88, 196, 255), "Wide Paddle"),
    SLOW_BALL("S", new Color(118, 217, 153), "Slow Ball"),
    MULTI_BALL("M", new Color(255, 139, 94), "Multi Ball"),
    EXTRA_LIFE("+", new Color(255, 102, 154), "Extra Life");

    private final String symbol;
    private final Color color;
    private final String label;

    PowerUpType(String symbol, Color color, String label) {
        this.symbol = symbol;
        this.color = color;
        this.label = label;
    }

    public String getSymbol() {
        return symbol;
    }

    public Color getColor() {
        return color;
    }

    public String getLabel() {
        return label;
    }
}
