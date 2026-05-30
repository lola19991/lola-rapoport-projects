package com.arkanoid.model;

import org.junit.jupiter.api.Test;

import java.awt.Color;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BrickTest {
    @Test
    void multiHitBrickRequiresAllHits() {
        Brick brick = new Brick(new BrickSpec(10.0, 10.0, 20.0, 10.0,
                Color.GRAY, 2, null));

        assertFalse(brick.hit());
        assertTrue(brick.hit());
        assertTrue(brick.isDestroyed());
    }
}
