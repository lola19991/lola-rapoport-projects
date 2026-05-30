package com.arkanoid.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LevelFactoryTest {
    @Test
    void generatedLevelsGrowHarderForever() {
        LevelDefinition first = LevelFactory.createLevel(1, Difficulty.MEDIUM);
        LevelDefinition tenth = LevelFactory.createLevel(10, Difficulty.MEDIUM);
        LevelDefinition twentieth = LevelFactory.createLevel(20, Difficulty.MEDIUM);

        assertTrue(tenth.ballSpeed() > first.ballSpeed());
        assertTrue(twentieth.ballSpeed() >= tenth.ballSpeed());
        assertTrue(tenth.bricks().size() >= first.bricks().size());
        assertNotEquals(first.background(), tenth.background());
    }

    @Test
    void hardDifficultyStartsFasterAndTougherThanEasy() {
        LevelDefinition easy = LevelFactory.createLevel(6, Difficulty.EASY);
        LevelDefinition hard = LevelFactory.createLevel(6, Difficulty.HARD);

        assertTrue(hard.ballSpeed() > easy.ballSpeed());
        int easyHits = easy.bricks().stream().mapToInt(BrickSpec::hitPoints).sum();
        int hardHits = hard.bricks().stream().mapToInt(BrickSpec::hitPoints).sum();
        assertTrue(hardHits > easyHits);
    }
}
