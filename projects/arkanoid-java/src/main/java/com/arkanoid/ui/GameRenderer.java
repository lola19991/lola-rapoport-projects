package com.arkanoid.ui;

import com.arkanoid.model.Ball;
import com.arkanoid.model.Brick;
import com.arkanoid.model.Difficulty;
import com.arkanoid.model.GameConfig;
import com.arkanoid.model.GameModel;
import com.arkanoid.model.GameState;
import com.arkanoid.model.Paddle;
import com.arkanoid.model.PowerUp;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

public final class GameRenderer {
    private static final Font HUD_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 16);
    private static final Font TITLE_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 62);
    private static final Font OVERLAY_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 54);
    private static final Font SUBTITLE_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 25);
    private static final Font BODY_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 18);
    private static final Font POWER_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 14);

    public void render(Graphics2D graphics, GameModel model) {
        Graphics2D g = (Graphics2D) graphics.create();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            drawBackground(g, model);
            if (model.getState() != GameState.START_SCREEN) {
                drawWalls(g);
                drawHud(g, model);
                drawBricks(g, model);
                drawPowerUps(g, model);
                drawPaddle(g, model.getPaddle());
                drawBalls(g, model);
            }
            drawOverlay(g, model);
        } finally {
            g.dispose();
        }
    }

    private void drawBackground(Graphics2D g, GameModel model) {
        Color top = model.getCurrentLevel().background();
        Color bottom = top.darker().darker();
        g.setPaint(new GradientPaint(0, 0, top, 0, GameConfig.BOARD_HEIGHT, bottom));
        g.fillRect(0, 0, GameConfig.BOARD_WIDTH, GameConfig.BOARD_HEIGHT);
        g.setColor(new Color(255, 255, 255, 17));
        for (int x = -120; x < GameConfig.BOARD_WIDTH; x += 42) {
            g.drawLine(x, 0, x + 180, GameConfig.BOARD_HEIGHT);
        }
        g.setColor(new Color(0, 0, 0, 50));
        g.fillRect(0, 0, GameConfig.BOARD_WIDTH, GameConfig.BOARD_HEIGHT);
    }

    private void drawWalls(Graphics2D g) {
        g.setColor(GameConfig.WALL_COLOR);
        g.fill(new Rectangle2D.Double(0, GameConfig.HUD_HEIGHT,
                GameConfig.BOARD_WIDTH, GameConfig.WALL_THICKNESS));
        g.fill(new Rectangle2D.Double(0, GameConfig.HUD_HEIGHT,
                GameConfig.WALL_THICKNESS, GameConfig.BOARD_HEIGHT - GameConfig.HUD_HEIGHT));
        g.fill(new Rectangle2D.Double(GameConfig.PLAY_RIGHT, GameConfig.HUD_HEIGHT,
                GameConfig.WALL_THICKNESS, GameConfig.BOARD_HEIGHT - GameConfig.HUD_HEIGHT));
        g.setColor(new Color(255, 255, 255, 34));
        g.fill(new Rectangle2D.Double(0, GameConfig.PLAY_TOP - 3,
                GameConfig.BOARD_WIDTH, 3));
    }

    private void drawHud(Graphics2D g, GameModel model) {
        g.setColor(new Color(10, 14, 20, 150));
        g.fillRect(0, 0, GameConfig.BOARD_WIDTH, (int) GameConfig.HUD_HEIGHT);
        g.setFont(HUD_FONT);
        g.setColor(GameConfig.PANEL_TEXT);
        g.drawString("Score " + model.getScore(), 28, 28);
        g.drawString("Lives " + model.getLives(), 170, 28);
        g.drawString(model.getActiveDifficulty().getLabel(), 268, 28);
        String level = "Level " + model.getCurrentLevelNumber() + "  " + model.getCurrentLevel().name();
        int width = g.getFontMetrics().stringWidth(level);
        g.drawString(level, GameConfig.BOARD_WIDTH - width - 28, 28);
    }

    private void drawBricks(Graphics2D g, GameModel model) {
        for (Brick brick : model.getBricks()) {
            Rectangle2D.Double bounds = brick.getBounds();
            Color color = brick.getHitPoints() < brick.getMaxHitPoints()
                    ? brick.getColor().brighter() : brick.getColor();
            g.setColor(color);
            g.fillRoundRect((int) bounds.x, (int) bounds.y,
                    (int) bounds.width, (int) bounds.height, 8, 8);
            g.setStroke(new BasicStroke(1.5f));
            g.setColor(new Color(0, 0, 0, 120));
            g.drawRoundRect((int) bounds.x, (int) bounds.y,
                    (int) bounds.width, (int) bounds.height, 8, 8);
            if (brick.getMaxHitPoints() > 1) {
                g.setColor(new Color(255, 255, 255, 170));
                for (int i = 0; i < brick.getHitPoints(); i++) {
                    g.fillOval((int) (bounds.x + bounds.width - 9 - i * 7),
                            (int) (bounds.y + 6), 4, 4);
                }
            }
        }
    }

    private void drawPaddle(Graphics2D g, Paddle paddle) {
        Rectangle2D.Double bounds = paddle.getBounds();
        g.setColor(new Color(0, 0, 0, 80));
        g.fillRoundRect((int) bounds.x + 3, (int) bounds.y + 5,
                (int) bounds.width, (int) bounds.height, 10, 10);
        g.setColor(GameConfig.PADDLE_COLOR);
        g.fillRoundRect((int) bounds.x, (int) bounds.y,
                (int) bounds.width, (int) bounds.height, 10, 10);
        g.setColor(new Color(255, 255, 255, 130));
        g.drawLine((int) bounds.x + 8, (int) bounds.y + 4,
                (int) (bounds.x + bounds.width - 8), (int) bounds.y + 4);
    }

    private void drawBalls(Graphics2D g, GameModel model) {
        for (Ball ball : model.getBalls()) {
            double radius = ball.getRadius();
            double x = ball.getPosition().x() - radius;
            double y = ball.getPosition().y() - radius;
            g.setColor(new Color(0, 0, 0, 95));
            g.fill(new Ellipse2D.Double(x + 2, y + 3, radius * 2.0, radius * 2.0));
            g.setColor(ball.getColor());
            g.fill(new Ellipse2D.Double(x, y, radius * 2.0, radius * 2.0));
            g.setColor(new Color(255, 255, 255, 160));
            g.fill(new Ellipse2D.Double(x + 3, y + 3, radius * 0.7, radius * 0.7));
        }
    }

    private void drawPowerUps(Graphics2D g, GameModel model) {
        g.setFont(POWER_FONT);
        for (PowerUp powerUp : model.getPowerUps()) {
            Rectangle2D.Double bounds = powerUp.getBounds();
            g.setColor(powerUp.getType().getColor());
            g.fillRoundRect((int) bounds.x, (int) bounds.y,
                    (int) bounds.width, (int) bounds.height, 7, 7);
            g.setColor(new Color(10, 12, 16));
            drawCentered(g, powerUp.getType().getSymbol(), bounds, 0);
        }
    }

    private void drawOverlay(Graphics2D g, GameModel model) {
        GameState state = model.getState();
        if (state == GameState.PLAYING) {
            return;
        }
        if (state == GameState.START_SCREEN) {
            drawStartScreen(g, model);
            return;
        }

        String title = switch (state) {
            case COUNTDOWN -> model.getCountdownText();
            case PAUSED -> "Paused";
            case LEVEL_CLEARED -> "Level Clear";
            case GAME_OVER -> "Game Over";
            case VICTORY -> "You Win";
            default -> "";
        };
        if (title.isEmpty()) {
            return;
        }

        g.setColor(new Color(0, 0, 0, state == GameState.PAUSED ? 190 : 136));
        g.fillRect(0, (int) GameConfig.HUD_HEIGHT,
                GameConfig.BOARD_WIDTH, GameConfig.BOARD_HEIGHT - (int) GameConfig.HUD_HEIGHT);
        g.setFont(OVERLAY_FONT);
        g.setColor(GameConfig.PANEL_TEXT);
        drawCentered(g, title, new Rectangle2D.Double(0, 165, GameConfig.BOARD_WIDTH, 90), 0);

        String subtitle = switch (state) {
            case PAUSED -> "P or Esc to resume";
            case LEVEL_CLEARED -> "Next level is loading";
            case GAME_OVER -> "Space to retry";
            case VICTORY -> "Space to play again";
            default -> "";
        };
        if (!subtitle.isEmpty()) {
            g.setFont(SUBTITLE_FONT);
            drawCentered(g, subtitle, new Rectangle2D.Double(0, 250, GameConfig.BOARD_WIDTH, 48), 0);
        }
    }

    private void drawStartScreen(Graphics2D g, GameModel model) {
        g.setColor(new Color(0, 0, 0, 88));
        g.fill(StartScreenLayout.wholeScreen());
        g.setFont(TITLE_FONT);
        g.setColor(GameConfig.PANEL_TEXT);
        drawCentered(g, "ARKANOID", new Rectangle2D.Double(0, 72, GameConfig.BOARD_WIDTH, 90), 0);

        g.setFont(SUBTITLE_FONT);
        drawCentered(g, "Choose difficulty",
                new Rectangle2D.Double(0, 154, GameConfig.BOARD_WIDTH, 42), 0);

        for (Difficulty difficulty : Difficulty.values()) {
            boolean selected = difficulty == model.getSelectedDifficulty();
            Rectangle2D.Double row = StartScreenLayout.difficultyRow(difficulty);
            g.setColor(selected ? new Color(255, 205, 79, 235) : new Color(255, 255, 255, 42));
            g.fillRoundRect((int) row.x, (int) row.y, (int) row.width, (int) row.height, 10, 10);
            g.setColor(selected ? new Color(25, 27, 31) : GameConfig.PANEL_TEXT);
            g.setFont(BODY_FONT);
            String label = keyLabel(difficulty) + "  " + difficulty.getLabel()
                    + "    Lives " + difficulty.getInitialLives();
            drawCentered(g, label, row, 0);
        }

        Rectangle2D.Double start = StartScreenLayout.startButton();
        g.setColor(new Color(118, 217, 153, 230));
        g.fillRoundRect((int) start.x, (int) start.y, (int) start.width, (int) start.height, 10, 10);
        g.setColor(new Color(15, 24, 20));
        g.setFont(BODY_FONT);
        drawCentered(g, "Start", start, 0);

        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 15));
        g.setColor(new Color(245, 247, 250, 190));
        drawCentered(g, "Use arrows, 1/2/3, E/M/H, mouse, Enter, or Space",
                new Rectangle2D.Double(0, 520, GameConfig.BOARD_WIDTH, 32), 0);
    }

    private String keyLabel(Difficulty difficulty) {
        return switch (difficulty) {
            case EASY -> "1 / E";
            case MEDIUM -> "2 / M";
            case HARD -> "3 / H";
        };
    }

    private void drawCentered(Graphics2D g, String text, Rectangle2D bounds, int yOffset) {
        FontMetrics metrics = g.getFontMetrics();
        int x = (int) (bounds.getX() + (bounds.getWidth() - metrics.stringWidth(text)) / 2.0);
        int y = (int) (bounds.getY() + (bounds.getHeight() - metrics.getHeight()) / 2.0
                + metrics.getAscent() + yOffset);
        g.drawString(text, x, y);
    }
}
