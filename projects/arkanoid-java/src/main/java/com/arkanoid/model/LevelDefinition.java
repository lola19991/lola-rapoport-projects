package com.arkanoid.model;

import java.awt.Color;
import java.util.List;

public record LevelDefinition(
        String name,
        Color background,
        double ballSpeed,
        int initialBalls,
        List<BrickSpec> bricks
) {
    public LevelDefinition {
        bricks = List.copyOf(bricks);
        if (initialBalls < 1) {
            initialBalls = 1;
        }
    }
}
