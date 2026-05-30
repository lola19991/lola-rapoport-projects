package com.arkanoid.model;

import java.awt.Color;

public record BrickSpec(
        double x,
        double y,
        double width,
        double height,
        Color color,
        int hitPoints,
        PowerUpType powerUpType
) {
}
