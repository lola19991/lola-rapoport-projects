package com.arkanoid.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Vector2Test {
    @Test
    void createsVelocityFromArcadeAngle() {
        Vector2 velocity = Vector2.fromAngleDegrees(90.0, 10.0);

        assertEquals(0.0, velocity.x(), 0.0001);
        assertEquals(-10.0, velocity.y(), 0.0001);
    }

    @Test
    void keepsDirectionWhenChangingLength() {
        Vector2 vector = new Vector2(3.0, 4.0).withLength(10.0);

        assertEquals(6.0, vector.x(), 0.0001);
        assertEquals(8.0, vector.y(), 0.0001);
    }
}
