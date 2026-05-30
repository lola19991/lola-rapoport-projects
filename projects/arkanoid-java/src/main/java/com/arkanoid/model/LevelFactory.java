package com.arkanoid.model;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public final class LevelFactory {
    private static final Color[] BRICK_COLORS = {
            new Color(232, 92, 92),
            new Color(245, 190, 72),
            new Color(80, 190, 126),
            new Color(74, 205, 210),
            new Color(64, 149, 245),
            new Color(155, 112, 236)
    };

    private static final Color[] BACKGROUNDS = {
            new Color(28, 36, 48),
            new Color(22, 50, 56),
            new Color(42, 32, 52),
            new Color(43, 43, 37),
            new Color(34, 31, 52),
            new Color(45, 35, 44),
            new Color(24, 44, 64)
    };

    private LevelFactory() {
    }

    public static LevelDefinition createLevel(int levelNumber, Difficulty difficulty) {
        int level = Math.max(1, levelNumber);
        Difficulty selected = difficulty == null ? Difficulty.MEDIUM : difficulty;
        double speed = Math.min(GameConfig.MAX_BALL_SPEED,
                (265.0 + level * 13.0) * selected.getSpeedMultiplier());
        int initialBalls = Math.min(GameConfig.MAX_BALLS, 1 + level / 9);
        int pattern = Math.floorMod(level - 1, 5);
        List<BrickSpec> bricks = switch (pattern) {
            case 0 -> wall(level, selected);
            case 1 -> staircase(level, selected);
            case 2 -> gaps(level, selected);
            case 3 -> fortress(level, selected);
            default -> diamond(level, selected);
        };
        if (pattern == 2) {
            initialBalls = Math.min(GameConfig.MAX_BALLS, initialBalls + 1);
        }
        String name = "Level " + level + " - " + patternName(pattern);
        return new LevelDefinition(name, BACKGROUNDS[(level - 1) % BACKGROUNDS.length],
                speed, initialBalls, bricks);
    }

    private static List<BrickSpec> wall(int level, Difficulty difficulty) {
        List<BrickSpec> bricks = new ArrayList<>();
        int rows = Math.min(9, 4 + level / 2);
        int columns = Math.min(13, 9 + level / 4);
        double startX = centeredStart(columns);
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                bricks.add(spec(startX, row, col, color(row, col),
                        hitPoints(level, row, col, false, difficulty),
                        powerUp(level, row, col, difficulty)));
            }
        }
        return bricks;
    }

    private static List<BrickSpec> staircase(int level, Difficulty difficulty) {
        List<BrickSpec> bricks = new ArrayList<>();
        int rows = Math.min(10, 5 + level / 3);
        for (int row = 0; row < rows; row++) {
            int columns = Math.min(13, 5 + row + level / 6);
            double startX = Math.max(42.0, centeredStart(columns) + row * 8.0);
            for (int col = 0; col < columns; col++) {
                bricks.add(spec(startX, row, col, color(row, col + level),
                        hitPoints(level, row, col, false, difficulty),
                        powerUp(level, row, col, difficulty)));
            }
        }
        return bricks;
    }

    private static List<BrickSpec> gaps(int level, Difficulty difficulty) {
        List<BrickSpec> bricks = new ArrayList<>();
        int rows = Math.min(9, 5 + level / 3);
        int columns = 12;
        double startX = centeredStart(columns);
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                if ((row + col + level) % 4 == 0) {
                    continue;
                }
                bricks.add(spec(startX, row, col, color(row * 2, col),
                        hitPoints(level, row, col, false, difficulty),
                        powerUp(level, row, col, difficulty)));
            }
        }
        return bricks;
    }

    private static List<BrickSpec> fortress(int level, Difficulty difficulty) {
        List<BrickSpec> bricks = new ArrayList<>();
        int rows = Math.min(9, 6 + level / 4);
        int columns = 12;
        double startX = centeredStart(columns);
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                boolean border = row == 0 || row == rows - 1 || col == 0 || col == columns - 1;
                Color color = border ? new Color(132, 144, 161) : color(row, col);
                bricks.add(spec(startX, row, col, color,
                        hitPoints(level, row, col, border, difficulty),
                        powerUp(level, row, col, difficulty)));
            }
        }
        return bricks;
    }

    private static List<BrickSpec> diamond(int level, Difficulty difficulty) {
        List<BrickSpec> bricks = new ArrayList<>();
        int rows = Math.min(11, 7 + level / 5);
        int columns = 13;
        double startX = centeredStart(columns);
        int middle = columns / 2;
        for (int row = 0; row < rows; row++) {
            int rowMiddle = rows / 2;
            int radius = Math.max(2, middle - Math.abs(row - rowMiddle));
            for (int col = 0; col < columns; col++) {
                if (Math.abs(col - middle) > radius) {
                    continue;
                }
                bricks.add(spec(startX, row, col, color(row + level, col * 2),
                        hitPoints(level, row, col, false, difficulty),
                        powerUp(level, row, col, difficulty)));
            }
        }
        return bricks;
    }

    private static BrickSpec spec(double startX, int row, int col, Color color,
                                  int hitPoints, PowerUpType powerUpType) {
        double x = startX + col * (GameConfig.BRICK_WIDTH + GameConfig.BRICK_GAP);
        double y = GameConfig.BRICK_TOP + row * (GameConfig.BRICK_HEIGHT + GameConfig.BRICK_GAP);
        return new BrickSpec(x, y, GameConfig.BRICK_WIDTH, GameConfig.BRICK_HEIGHT,
                color, hitPoints, powerUpType);
    }

    private static int hitPoints(int level, int row, int col, boolean fortressBorder,
                                 Difficulty difficulty) {
        double pressure = (level + row * 0.55 + col * 0.2) * difficulty.getToughnessMultiplier();
        int hits = 1 + (pressure >= 8.5 ? 1 : 0) + (pressure >= 18.0 ? 1 : 0);
        if (fortressBorder) {
            hits++;
        }
        return Math.min(4, hits);
    }

    private static PowerUpType powerUp(int level, int row, int col, Difficulty difficulty) {
        int cadence = Math.max(4, (int) Math.round(7.0 / difficulty.getPowerUpMultiplier()));
        if (Math.floorMod(level * 13 + row * 11 + col * 7, cadence) != 0) {
            return null;
        }
        PowerUpType[] values = PowerUpType.values();
        return values[Math.floorMod(level + row + col, values.length)];
    }

    private static double centeredStart(int columns) {
        double width = columns * GameConfig.BRICK_WIDTH + (columns - 1) * GameConfig.BRICK_GAP;
        return (GameConfig.BOARD_WIDTH - width) / 2.0;
    }

    private static Color color(int row, int col) {
        return BRICK_COLORS[Math.floorMod(row + col, BRICK_COLORS.length)];
    }

    private static String patternName(int pattern) {
        return switch (pattern) {
            case 0 -> "Rising Wall";
            case 1 -> "Color Staircase";
            case 2 -> "Twin Trouble";
            case 3 -> "Fortress";
            default -> "Prism Core";
        };
    }
}
