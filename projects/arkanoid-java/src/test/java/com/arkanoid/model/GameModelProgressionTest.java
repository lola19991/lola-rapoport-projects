package com.arkanoid.model;

import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameModelProgressionTest {
    @Test
    void clearingSecondLevelAdvancesToThirdLevel() {
        GameModel model = new GameModel(List.of(emptyLevel("One"), emptyLevel("Two"), emptyLevel("Three")));

        clearCurrentEmptyLevel(model);
        assertEquals(2, model.getCurrentLevelNumber());

        clearCurrentEmptyLevel(model);

        assertEquals(3, model.getCurrentLevelNumber());
        assertEquals(GameState.COUNTDOWN, model.getState());
        assertEquals("Three", model.getCurrentLevel().name());
    }

    @Test
    void infiniteModeKeepsGeneratingAfterManyClears() {
        GameModel model = new GameModel();
        model.startNewGame(Difficulty.EASY);

        for (int i = 0; i < 8; i++) {
            model.tick(GameConfig.COUNTDOWN_SECONDS + 0.1, GameConfig.NO_INPUT);
            forceClearCurrentLevel(model);
            model.tick(GameConfig.LEVEL_CLEAR_SECONDS + 0.1, GameConfig.NO_INPUT);
        }

        assertEquals(9, model.getCurrentLevelNumber());
        assertEquals(GameState.COUNTDOWN, model.getState());
        assertTrue(model.isInfiniteMode());
    }

    @Test
    void losingAllBallsCostsLifeAndStartsCountdown() {
        GameModel model = new GameModel(singleBrickLevel());
        model.tick(GameConfig.COUNTDOWN_SECONDS + 0.1, GameConfig.NO_INPUT);
        Ball ball = model.getBalls().get(0);
        ball.setPosition(new Vector2(400.0, GameConfig.DEATH_Y + ball.getRadius() + 1.0));
        ball.setVelocity(new Vector2(0.0, 300.0));

        model.tick(0.02, GameConfig.NO_INPUT);

        assertEquals(Difficulty.MEDIUM.getInitialLives() - 1, model.getLives());
        assertEquals(GameState.COUNTDOWN, model.getState());
        assertEquals(1, model.getBalls().size());
    }

    @Test
    void gameOverWhenLastLifeIsLost() {
        GameModel model = new GameModel(singleBrickLevel());

        for (int i = 0; i < Difficulty.MEDIUM.getInitialLives(); i++) {
            model.tick(GameConfig.COUNTDOWN_SECONDS + 0.1, GameConfig.NO_INPUT);
            Ball ball = model.getBalls().get(0);
            ball.setPosition(new Vector2(400.0, GameConfig.DEATH_Y + ball.getRadius() + 1.0));
            ball.setVelocity(new Vector2(0.0, 300.0));
            model.tick(0.02, GameConfig.NO_INPUT);
        }

        assertEquals(GameState.GAME_OVER, model.getState());
        model.handleSpace();
        assertEquals(GameState.COUNTDOWN, model.getState());
        assertEquals(Difficulty.MEDIUM.getInitialLives(), model.getLives());
    }

    @Test
    void collectingPowerUpAppliesEffect() {
        Rectangle2D.Double paddle = new Paddle().getBounds();
        BrickSpec powerBrick = new BrickSpec(paddle.getCenterX() - 25.0,
                paddle.y - 52.0, 50.0, 20.0, Color.CYAN, 1, PowerUpType.EXPAND_PADDLE);
        BrickSpec spareBrick = new BrickSpec(100.0, 100.0, 50.0, 20.0, Color.RED, 1, null);
        GameModel model = new GameModel(List.of(new LevelDefinition("Power",
                Color.BLACK, 300.0, 1, List.of(powerBrick, spareBrick))));
        model.tick(GameConfig.COUNTDOWN_SECONDS + 0.1, GameConfig.NO_INPUT);
        Ball ball = model.getBalls().get(0);
        ball.setPosition(new Vector2(powerBrick.x() + powerBrick.width() / 2.0,
                powerBrick.y() + powerBrick.height() + ball.getRadius() + 1.0));
        ball.setVelocity(new Vector2(0.0, -260.0));

        model.tick(0.04, GameConfig.NO_INPUT);
        model.tick(0.6, GameConfig.NO_INPUT);

        assertTrue(model.getPaddle().getWidth() > GameConfig.PADDLE_WIDTH);
    }

    private void clearCurrentEmptyLevel(GameModel model) {
        model.tick(GameConfig.COUNTDOWN_SECONDS + 0.1, GameConfig.NO_INPUT);
        model.tick(0.1, GameConfig.NO_INPUT);
        assertEquals(GameState.LEVEL_CLEARED, model.getState());
        model.tick(GameConfig.LEVEL_CLEAR_SECONDS + 0.1, GameConfig.NO_INPUT);
    }

    private void forceClearCurrentLevel(GameModel model) {
        while (!model.getBricks().isEmpty()) {
            Brick brick = model.getBricks().get(0);
            while (!brick.isDestroyed()) {
                brick.hit();
            }
            model.tick(0.01, GameConfig.NO_INPUT);
        }
    }

    private LevelDefinition emptyLevel(String name) {
        return new LevelDefinition(name, Color.BLACK, 300.0, 1, List.of());
    }

    private List<LevelDefinition> singleBrickLevel() {
        return List.of(new LevelDefinition("Test", Color.BLACK, 300.0, 1,
                List.of(new BrickSpec(360.0, 120.0, 50.0, 20.0, Color.RED, 1, null))));
    }
}
