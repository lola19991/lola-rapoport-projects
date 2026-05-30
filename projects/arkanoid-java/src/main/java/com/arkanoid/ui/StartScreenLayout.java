package com.arkanoid.ui;

import com.arkanoid.model.Difficulty;
import com.arkanoid.model.GameConfig;

import java.awt.geom.Rectangle2D;

final class StartScreenLayout {
    private static final double ROW_X = 190.0;
    private static final double ROW_Y = 245.0;
    private static final double ROW_WIDTH = 420.0;
    private static final double ROW_HEIGHT = 54.0;
    private static final double ROW_GAP = 14.0;
    private static final Rectangle2D.Double START_BUTTON =
            new Rectangle2D.Double(300.0, 458.0, 200.0, 46.0);

    private StartScreenLayout() {
    }

    static Rectangle2D.Double difficultyRow(Difficulty difficulty) {
        int index = difficulty.ordinal();
        return new Rectangle2D.Double(ROW_X, ROW_Y + index * (ROW_HEIGHT + ROW_GAP),
                ROW_WIDTH, ROW_HEIGHT);
    }

    static Rectangle2D.Double startButton() {
        return START_BUTTON;
    }

    static Difficulty difficultyAt(double x, double y) {
        for (Difficulty difficulty : Difficulty.values()) {
            if (difficultyRow(difficulty).contains(x, y)) {
                return difficulty;
            }
        }
        return null;
    }

    static boolean isStartButton(double x, double y) {
        return START_BUTTON.contains(x, y);
    }

    static Rectangle2D.Double wholeScreen() {
        return new Rectangle2D.Double(0, 0, GameConfig.BOARD_WIDTH, GameConfig.BOARD_HEIGHT);
    }
}
