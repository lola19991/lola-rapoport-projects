package com.arkanoid.model;

import java.awt.Color;

public final class GameConfig {
    public static final int BOARD_WIDTH = 800;
    public static final int BOARD_HEIGHT = 600;
    public static final int FRAME_RATE = 60;
    public static final double HUD_HEIGHT = 44.0;
    public static final double WALL_THICKNESS = 24.0;
    public static final double PLAY_LEFT = WALL_THICKNESS;
    public static final double PLAY_RIGHT = BOARD_WIDTH - WALL_THICKNESS;
    public static final double PLAY_TOP = HUD_HEIGHT + WALL_THICKNESS;
    public static final double DEATH_Y = BOARD_HEIGHT + 48.0;
    public static final double PADDLE_Y = BOARD_HEIGHT - 50.0;
    public static final double PADDLE_HEIGHT = 14.0;
    public static final double PADDLE_WIDTH = 118.0;
    public static final double PADDLE_EXPAND_AMOUNT = 50.0;
    public static final double PADDLE_SPEED = 520.0;
    public static final double MAX_PADDLE_BOUNCE_DEGREES = 65.0;
    public static final double BALL_RADIUS = 7.0;
    public static final double MIN_BALL_SPEED = 210.0;
    public static final double MAX_BALL_SPEED = 585.0;
    public static final int MAX_BALLS = 6;
    public static final double BRICK_WIDTH = 52.0;
    public static final double BRICK_HEIGHT = 22.0;
    public static final double BRICK_GAP = 5.0;
    public static final double BRICK_TOP = 90.0;
    public static final double POWER_UP_SIZE = 22.0;
    public static final double POWER_UP_SPEED = 135.0;
    public static final int LEVEL_CLEAR_BONUS = 750;
    public static final double COUNTDOWN_SECONDS = 3.5;
    public static final double LEVEL_CLEAR_SECONDS = 1.15;
    public static final Color PANEL_TEXT = new Color(245, 247, 250);
    public static final Color WALL_COLOR = new Color(39, 45, 56);
    public static final Color PADDLE_COLOR = new Color(255, 205, 79);
    public static final Color BALL_COLOR = new Color(247, 248, 250);
    public static final InputState NO_INPUT = new InputState();

    private GameConfig() {
    }
}
