package com.arkanoid;

import com.arkanoid.model.Difficulty;
import com.arkanoid.model.GameModel;
import com.arkanoid.ui.GameWindow;

import javax.swing.SwingUtilities;
import java.util.Arrays;

public final class ArkanoidApp {
    private ArkanoidApp() {
    }

    public static void main(String[] args) {
        if (Arrays.asList(args).contains("--smoke-test")) {
            GameModel model = new GameModel();
            model.startNewGame(Difficulty.MEDIUM);
            System.out.printf("Loaded %s. State: %s. Level: %d.%n",
                    model.getCurrentLevel().name(), model.getState(), model.getCurrentLevelNumber());
            return;
        }
        SwingUtilities.invokeLater(() -> new GameWindow(new GameModel()).show());
    }
}
