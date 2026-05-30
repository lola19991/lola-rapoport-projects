package com.arkanoid.ui;

import com.arkanoid.model.GameConfig;
import com.arkanoid.model.GameModel;

import javax.swing.JFrame;
import java.awt.Dimension;

public final class GameWindow {
    private final GameModel model;

    public GameWindow(GameModel model) {
        this.model = model;
    }

    public void show() {
        JFrame frame = new JFrame("Arkanoid");
        GamePanel panel = new GamePanel(model);
        panel.setPreferredSize(new Dimension(GameConfig.BOARD_WIDTH, GameConfig.BOARD_HEIGHT));
        frame.setContentPane(panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        panel.requestFocusInWindow();
        panel.start();
    }
}
