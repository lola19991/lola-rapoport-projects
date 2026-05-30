package com.arkanoid.ui;

import com.arkanoid.model.Difficulty;
import com.arkanoid.model.GameConfig;
import com.arkanoid.model.GameModel;
import com.arkanoid.model.GameState;
import com.arkanoid.model.InputState;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.Timer;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public final class GamePanel extends JPanel implements ActionListener {
    private final GameModel model;
    private final GameRenderer renderer = new GameRenderer();
    private final InputState input = new InputState();
    private final Timer timer;
    private long lastFrameNanos;

    public GamePanel(GameModel model) {
        this.model = model;
        this.timer = new Timer(1000 / GameConfig.FRAME_RATE, this);
        setFocusable(true);
        setDoubleBuffered(true);
        installKeyBindings();
        installMouseBindings();
    }

    public void start() {
        lastFrameNanos = System.nanoTime();
        timer.start();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        renderer.render((Graphics2D) graphics, model);
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        long now = System.nanoTime();
        double deltaSeconds = (now - lastFrameNanos) / 1_000_000_000.0;
        lastFrameNanos = now;
        model.tick(deltaSeconds, input);
        repaint();
    }

    private void installKeyBindings() {
        bindPressed(KeyEvent.VK_LEFT, "leftPressed", () -> {
            if (model.canChangeDifficulty()) {
                model.cycleDifficulty(-1);
            } else {
                input.setLeftPressed(true);
            }
        });
        bindReleased(KeyEvent.VK_LEFT, "leftReleased", () -> input.setLeftPressed(false));
        bindPressed(KeyEvent.VK_RIGHT, "rightPressed", () -> {
            if (model.canChangeDifficulty()) {
                model.cycleDifficulty(1);
            } else {
                input.setRightPressed(true);
            }
        });
        bindReleased(KeyEvent.VK_RIGHT, "rightReleased", () -> input.setRightPressed(false));
        bindPressed(KeyEvent.VK_UP, "difficultyUp", () -> model.cycleDifficulty(-1));
        bindPressed(KeyEvent.VK_DOWN, "difficultyDown", () -> model.cycleDifficulty(1));
        bindPressed(KeyEvent.VK_P, "pauseP", model::togglePause);
        bindPressed(KeyEvent.VK_ESCAPE, "pauseEscape", model::togglePause);
        bindPressed(KeyEvent.VK_SPACE, "space", model::handleSpace);
        bindPressed(KeyEvent.VK_ENTER, "enter", model::handleSpace);

        bindPressed(KeyEvent.VK_1, "easy1", () -> model.selectDifficulty(Difficulty.EASY));
        bindPressed(KeyEvent.VK_NUMPAD1, "easyNumpad1", () -> model.selectDifficulty(Difficulty.EASY));
        bindPressed(KeyEvent.VK_E, "easyE", () -> model.selectDifficulty(Difficulty.EASY));
        bindPressed(KeyEvent.VK_2, "medium2", () -> model.selectDifficulty(Difficulty.MEDIUM));
        bindPressed(KeyEvent.VK_NUMPAD2, "mediumNumpad2", () -> model.selectDifficulty(Difficulty.MEDIUM));
        bindPressed(KeyEvent.VK_M, "mediumM", () -> model.selectDifficulty(Difficulty.MEDIUM));
        bindPressed(KeyEvent.VK_3, "hard3", () -> model.selectDifficulty(Difficulty.HARD));
        bindPressed(KeyEvent.VK_NUMPAD3, "hardNumpad3", () -> model.selectDifficulty(Difficulty.HARD));
        bindPressed(KeyEvent.VK_H, "hardH", () -> model.selectDifficulty(Difficulty.HARD));
    }

    private void installMouseBindings() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent event) {
                requestFocusInWindow();
                if (model.getState() != GameState.START_SCREEN
                        && model.getState() != GameState.GAME_OVER
                        && model.getState() != GameState.VICTORY) {
                    return;
                }
                Difficulty clicked = StartScreenLayout.difficultyAt(event.getX(), event.getY());
                if (clicked != null) {
                    model.selectDifficulty(clicked);
                    repaint();
                    return;
                }
                if (StartScreenLayout.isStartButton(event.getX(), event.getY())) {
                    model.handleSpace();
                    repaint();
                }
            }
        });
    }

    private void bindPressed(int keyCode, String name, Runnable action) {
        bind(KeyStroke.getKeyStroke(keyCode, 0, false), name, action);
    }

    private void bindReleased(int keyCode, String name, Runnable action) {
        bind(KeyStroke.getKeyStroke(keyCode, 0, true), name, action);
    }

    private void bind(KeyStroke keyStroke, String name, Runnable action) {
        InputMap inputMap = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = getActionMap();
        inputMap.put(keyStroke, name);
        actionMap.put(name, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                action.run();
                repaint();
            }
        });
    }
}
