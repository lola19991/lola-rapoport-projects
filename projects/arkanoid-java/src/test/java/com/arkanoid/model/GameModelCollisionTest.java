package com.arkanoid.model;

import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameModelCollisionTest {
    @Test
    void ballReflectsFromLeftWall() {
        GameModel model = new GameModel(singleBrickLevel());
        model.tick(GameConfig.COUNTDOWN_SECONDS + 0.1, GameConfig.NO_INPUT);
        Ball ball = model.getBalls().get(0);
        ball.setPosition(new Vector2(GameConfig.PLAY_LEFT + ball.getRadius() - 1.0, 240.0));
        ball.setVelocity(new Vector2(-200.0, 0.0));

        model.tick(0.02, GameConfig.NO_INPUT);

        assertTrue(ball.getVelocity().x() > 0.0);
    }

    @Test
    void paddleReflectsBallUpward() {
        GameModel model = new GameModel(singleBrickLevel());
        model.tick(GameConfig.COUNTDOWN_SECONDS + 0.1, GameConfig.NO_INPUT);
        Ball ball = model.getBalls().get(0);
        Rectangle2D.Double paddle = model.getPaddle().getBounds();
        ball.setPosition(new Vector2(paddle.getCenterX(), paddle.y - ball.getRadius() - 1.0));
        ball.setVelocity(new Vector2(0.0, 260.0));

        model.tick(0.02, GameConfig.NO_INPUT);

        assertTrue(ball.getVelocity().y() < 0.0);
    }

    @Test
    void brickHitAddsScoreAndClearsLevel() {
        BrickSpec brick = new BrickSpec(360.0, 150.0, 60.0, 24.0, Color.RED, 1, null);
        GameModel model = new GameModel(List.of(new LevelDefinition("Score",
                Color.BLACK, 300.0, 1, List.of(brick))));
        model.tick(GameConfig.COUNTDOWN_SECONDS + 0.1, GameConfig.NO_INPUT);
        Ball ball = model.getBalls().get(0);
        ball.setPosition(new Vector2(390.0, 150.0 + 24.0 + ball.getRadius() + 1.0));
        ball.setVelocity(new Vector2(0.0, -300.0));

        model.tick(0.03, GameConfig.NO_INPUT);

        assertTrue(model.getScore() > 0);
        assertEquals(GameState.LEVEL_CLEARED, model.getState());
    }

    private List<LevelDefinition> singleBrickLevel() {
        return List.of(new LevelDefinition("Test", Color.BLACK, 300.0, 1,
                List.of(new BrickSpec(360.0, 120.0, 50.0, 20.0, Color.RED, 1, null))));
    }
}
