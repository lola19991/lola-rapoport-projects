package com.arkanoid.model;

import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class GameModelStateTest {
    @Test
    void difficultyCanBeSelectedCycledAndStarted() {
        GameModel model = new GameModel();

        assertEquals(GameState.START_SCREEN, model.getState());
        model.selectDifficulty(Difficulty.HARD);
        assertEquals(Difficulty.HARD, model.getSelectedDifficulty());

        model.cycleDifficulty(1);
        assertEquals(Difficulty.EASY, model.getSelectedDifficulty());

        model.handleSpace();
        assertEquals(GameState.COUNTDOWN, model.getState());
        assertEquals(Difficulty.EASY, model.getActiveDifficulty());
        assertEquals(Difficulty.EASY.getInitialLives(), model.getLives());
    }

    @Test
    void difficultyCannotChangeDuringActiveGame() {
        GameModel model = new GameModel();

        model.selectDifficulty(Difficulty.HARD);
        model.handleSpace();
        model.selectDifficulty(Difficulty.EASY);
        model.cycleDifficulty(1);

        assertEquals(Difficulty.HARD, model.getSelectedDifficulty());
        assertEquals(Difficulty.HARD, model.getActiveDifficulty());
        assertFalse(model.canChangeDifficulty());
    }

    @Test
    void countdownMovesIntoPlay() {
        GameModel model = new GameModel(singleBrickLevel());

        assertEquals(GameState.COUNTDOWN, model.getState());
        assertEquals("3", model.getCountdownText());

        model.tick(GameConfig.COUNTDOWN_SECONDS + 0.1, GameConfig.NO_INPUT);

        assertEquals(GameState.PLAYING, model.getState());
        assertEquals("", model.getCountdownText());
    }

    @Test
    void pauseFreezesGameplayAndResumeUsesCountdown() {
        GameModel model = new GameModel(singleBrickLevel());
        model.tick(GameConfig.COUNTDOWN_SECONDS + 0.1, GameConfig.NO_INPUT);
        Ball ball = model.getBalls().get(0);
        Vector2 beforePause = ball.getPosition();

        model.togglePause();
        model.tick(1.0, GameConfig.NO_INPUT);

        assertEquals(GameState.PAUSED, model.getState());
        assertEquals(beforePause, ball.getPosition());

        model.togglePause();

        assertEquals(GameState.COUNTDOWN, model.getState());
        assertNotEquals("", model.getCountdownText());
    }

    @Test
    void scriptedFinalLevelCanStillEndInVictory() {
        GameModel model = new GameModel(List.of(new LevelDefinition("Empty",
                Color.BLACK, 300.0, 1, List.of())));

        model.tick(GameConfig.COUNTDOWN_SECONDS + 0.1, GameConfig.NO_INPUT);
        model.tick(0.1, GameConfig.NO_INPUT);
        assertEquals(GameState.LEVEL_CLEARED, model.getState());

        model.tick(GameConfig.LEVEL_CLEAR_SECONDS + 0.1, GameConfig.NO_INPUT);

        assertEquals(GameState.VICTORY, model.getState());
    }

    private List<LevelDefinition> singleBrickLevel() {
        return List.of(new LevelDefinition("Test", Color.BLACK, 300.0, 1,
                List.of(new BrickSpec(360.0, 120.0, 50.0, 20.0, Color.RED, 1, null))));
    }
}
