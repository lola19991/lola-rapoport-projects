package com.arkanoid.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaddleTest {
    @Test
    void clampsInsidePlayArea() {
        Paddle paddle = new Paddle();

        paddle.setX(-500.0);
        assertEquals(GameConfig.PLAY_LEFT, paddle.getX(), 0.0001);

        paddle.setX(5_000.0);
        assertEquals(GameConfig.PLAY_RIGHT - paddle.getWidth(), paddle.getX(), 0.0001);
    }

    @Test
    void difficultyChangesBaseWidth() {
        Paddle paddle = new Paddle();

        paddle.setDifficulty(Difficulty.EASY);
        assertTrue(paddle.getWidth() > GameConfig.PADDLE_WIDTH);

        paddle.setDifficulty(Difficulty.HARD);
        assertTrue(paddle.getWidth() < GameConfig.PADDLE_WIDTH);
    }

    @Test
    void returnsDifferentBounceAnglesAcrossPaddle() {
        Paddle paddle = new Paddle();
        double speed = 300.0;

        Vector2 leftBounce = paddle.bounceVelocityFor(paddle.getX(), speed);
        Vector2 centerBounce = paddle.bounceVelocityFor(paddle.getCenterX(), speed);
        Vector2 rightBounce = paddle.bounceVelocityFor(paddle.getX() + paddle.getWidth(), speed);

        assertTrue(leftBounce.x() < 0.0);
        assertEquals(0.0, centerBounce.x(), 0.0001);
        assertTrue(rightBounce.x() > 0.0);
        assertEquals(speed, centerBounce.length(), 0.0001);
    }
}
