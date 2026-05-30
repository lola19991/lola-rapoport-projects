package com.arkanoid.model;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public final class GameModel {
    private final List<LevelDefinition> scriptedLevels;
    private final Paddle paddle = new Paddle();
    private final List<Ball> balls = new ArrayList<>();
    private final List<Brick> bricks = new ArrayList<>();
    private final List<PowerUp> powerUps = new ArrayList<>();

    private GameState state = GameState.START_SCREEN;
    private Difficulty selectedDifficulty = Difficulty.MEDIUM;
    private Difficulty activeDifficulty = Difficulty.MEDIUM;
    private LevelDefinition currentLevel = LevelFactory.createLevel(1, Difficulty.MEDIUM);
    private int currentLevelNumber = 1;
    private int lives = Difficulty.MEDIUM.getInitialLives();
    private int score;
    private int combo;
    private double stateTimer;

    public GameModel() {
        this.scriptedLevels = null;
        paddle.setDifficulty(selectedDifficulty);
    }

    public GameModel(List<LevelDefinition> levels) {
        if (levels == null || levels.isEmpty()) {
            throw new IllegalArgumentException("At least one level is required.");
        }
        this.scriptedLevels = List.copyOf(levels);
        startNewGame(Difficulty.MEDIUM);
    }

    public void selectDifficulty(Difficulty difficulty) {
        if (difficulty == null || !canChangeDifficulty()) {
            return;
        }
        selectedDifficulty = difficulty;
        paddle.setDifficulty(selectedDifficulty);
        lives = selectedDifficulty.getInitialLives();
    }

    public void cycleDifficulty(int direction) {
        if (!canChangeDifficulty()) {
            return;
        }
        Difficulty[] values = Difficulty.values();
        int index = selectedDifficulty.ordinal();
        int next = Math.floorMod(index + (direction < 0 ? -1 : 1), values.length);
        selectDifficulty(values[next]);
    }

    public void startNewGame(Difficulty difficulty) {
        selectedDifficulty = difficulty == null ? selectedDifficulty : difficulty;
        activeDifficulty = selectedDifficulty;
        currentLevelNumber = 1;
        lives = activeDifficulty.getInitialLives();
        score = 0;
        combo = 0;
        paddle.setDifficulty(activeDifficulty);
        loadLevel(currentLevelNumber);
    }

    public void tick(double deltaSeconds, InputState input) {
        if (deltaSeconds <= 0.0) {
            return;
        }
        InputState actualInput = input == null ? GameConfig.NO_INPUT : input;
        double remaining = deltaSeconds;
        while (remaining > 0.0) {
            double dt = Math.min(remaining, 0.05);
            step(dt, actualInput);
            remaining -= dt;
        }
    }

    public void togglePause() {
        if (state == GameState.PLAYING) {
            state = GameState.PAUSED;
        } else if (state == GameState.PAUSED) {
            beginCountdown();
        }
    }

    public void handleSpace() {
        if (canStartFromMenu()) {
            startNewGame(selectedDifficulty);
        }
    }

    public boolean canChangeDifficulty() {
        return state == GameState.START_SCREEN || state == GameState.GAME_OVER || state == GameState.VICTORY;
    }

    public boolean canStartFromMenu() {
        return state == GameState.START_SCREEN || state == GameState.GAME_OVER || state == GameState.VICTORY;
    }

    public String getCountdownText() {
        if (state != GameState.COUNTDOWN) {
            return "";
        }
        if (stateTimer > 2.5) {
            return "3";
        }
        if (stateTimer > 1.5) {
            return "2";
        }
        if (stateTimer > 0.5) {
            return "1";
        }
        return "Go!";
    }

    public LevelDefinition getCurrentLevel() {
        return currentLevel;
    }

    public GameState getState() {
        return state;
    }

    public Difficulty getSelectedDifficulty() {
        return selectedDifficulty;
    }

    public Difficulty getActiveDifficulty() {
        return activeDifficulty;
    }

    public Paddle getPaddle() {
        return paddle;
    }

    public List<Ball> getBalls() {
        return Collections.unmodifiableList(balls);
    }

    public List<Brick> getBricks() {
        return Collections.unmodifiableList(bricks);
    }

    public List<PowerUp> getPowerUps() {
        return Collections.unmodifiableList(powerUps);
    }

    public int getCurrentLevelNumber() {
        return currentLevelNumber;
    }

    public int getLives() {
        return lives;
    }

    public int getScore() {
        return score;
    }

    public int getCombo() {
        return combo;
    }

    public boolean isInfiniteMode() {
        return scriptedLevels == null;
    }

    private void step(double dt, InputState input) {
        switch (state) {
            case COUNTDOWN -> tickCountdown(dt);
            case PLAYING -> tickPlaying(dt, input);
            case LEVEL_CLEARED -> tickLevelClear(dt);
            case START_SCREEN, PAUSED, GAME_OVER, VICTORY -> {
            }
        }
    }

    private void tickCountdown(double deltaSeconds) {
        stateTimer -= deltaSeconds;
        if (stateTimer <= 0.0) {
            state = GameState.PLAYING;
        }
    }

    private void tickLevelClear(double deltaSeconds) {
        stateTimer -= deltaSeconds;
        if (stateTimer > 0.0) {
            return;
        }
        if (scriptedLevels != null && currentLevelNumber >= scriptedLevels.size()) {
            state = GameState.VICTORY;
            return;
        }
        currentLevelNumber++;
        loadLevel(currentLevelNumber);
    }

    private void tickPlaying(double deltaSeconds, InputState input) {
        if (bricks.isEmpty()) {
            completeLevel();
            return;
        }

        paddle.update(deltaSeconds, input);
        updatePowerUps(deltaSeconds);

        List<Ball> lostBalls = new ArrayList<>();
        for (Ball ball : balls) {
            Vector2 previousPosition = ball.getPosition();
            ball.move(deltaSeconds);
            handleWallCollisions(ball);
            handlePaddleCollision(ball, previousPosition);
            handleBrickCollision(ball, previousPosition);
            if (ball.getPosition().y() - ball.getRadius() > GameConfig.DEATH_Y) {
                lostBalls.add(ball);
            }
        }
        balls.removeAll(lostBalls);
        bricks.removeIf(Brick::isDestroyed);

        if (bricks.isEmpty()) {
            completeLevel();
        } else if (balls.isEmpty()) {
            loseLife();
        }
    }

    private void updatePowerUps(double deltaSeconds) {
        Iterator<PowerUp> iterator = powerUps.iterator();
        while (iterator.hasNext()) {
            PowerUp powerUp = iterator.next();
            powerUp.update(deltaSeconds);
            if (powerUp.getBounds().intersects(paddle.getBounds())) {
                applyPowerUp(powerUp.getType());
                score += 25;
                iterator.remove();
            } else if (powerUp.getPosition().y() > GameConfig.BOARD_HEIGHT + GameConfig.POWER_UP_SIZE) {
                iterator.remove();
            }
        }
    }

    private void handleWallCollisions(Ball ball) {
        Vector2 position = ball.getPosition();
        Vector2 velocity = ball.getVelocity();
        double radius = ball.getRadius();

        if (position.x() - radius < GameConfig.PLAY_LEFT) {
            position = new Vector2(GameConfig.PLAY_LEFT + radius, position.y());
            velocity = new Vector2(Math.abs(velocity.x()), velocity.y());
        } else if (position.x() + radius > GameConfig.PLAY_RIGHT) {
            position = new Vector2(GameConfig.PLAY_RIGHT - radius, position.y());
            velocity = new Vector2(-Math.abs(velocity.x()), velocity.y());
        }

        if (position.y() - radius < GameConfig.PLAY_TOP) {
            position = new Vector2(position.x(), GameConfig.PLAY_TOP + radius);
            velocity = new Vector2(velocity.x(), Math.abs(velocity.y()));
        }

        ball.setPosition(position);
        ball.setVelocity(velocity);
    }

    private void handlePaddleCollision(Ball ball, Vector2 previousPosition) {
        Rectangle2D.Double paddleBounds = paddle.getBounds();
        if (ball.getVelocity().y() <= 0.0 || !ball.getBounds().intersects(paddleBounds)) {
            return;
        }
        boolean cameFromAbove = previousPosition.y() + ball.getRadius() <= paddleBounds.y + 4.0;
        if (!cameFromAbove) {
            return;
        }
        ball.setPosition(new Vector2(ball.getPosition().x(), paddleBounds.y - ball.getRadius() - 0.1));
        ball.setVelocity(paddle.bounceVelocityFor(ball.getPosition().x(), ball.getSpeed()));
    }

    private void handleBrickCollision(Ball ball, Vector2 previousPosition) {
        for (Brick brick : bricks) {
            if (brick.isDestroyed() || !circleIntersects(ball, brick.getBounds())) {
                continue;
            }

            reflectFromBrick(ball, previousPosition, brick.getBounds());
            combo++;
            score += 10 + Math.max(0, combo - 1) * 2;
            boolean destroyed = brick.hit();
            if (destroyed) {
                score += brick.getScoreValue();
                if (brick.getPowerUpType() != null) {
                    Rectangle2D.Double bounds = brick.getBounds();
                    powerUps.add(new PowerUp(brick.getPowerUpType(),
                            new Vector2(bounds.getCenterX(), bounds.getCenterY())));
                }
            }
            return;
        }
    }

    private void reflectFromBrick(Ball ball, Vector2 previousPosition, Rectangle2D.Double bounds) {
        Vector2 velocity = ball.getVelocity();
        double radius = ball.getRadius();
        if (previousPosition.y() + radius <= bounds.y) {
            velocity = new Vector2(velocity.x(), -Math.abs(velocity.y()));
        } else if (previousPosition.y() - radius >= bounds.y + bounds.height) {
            velocity = new Vector2(velocity.x(), Math.abs(velocity.y()));
        } else if (previousPosition.x() + radius <= bounds.x) {
            velocity = new Vector2(-Math.abs(velocity.x()), velocity.y());
        } else if (previousPosition.x() - radius >= bounds.x + bounds.width) {
            velocity = new Vector2(Math.abs(velocity.x()), velocity.y());
        } else {
            double leftOverlap = Math.abs(ball.getPosition().x() + radius - bounds.x);
            double rightOverlap = Math.abs(bounds.x + bounds.width - (ball.getPosition().x() - radius));
            double topOverlap = Math.abs(ball.getPosition().y() + radius - bounds.y);
            double bottomOverlap = Math.abs(bounds.y + bounds.height - (ball.getPosition().y() - radius));
            velocity = Math.min(leftOverlap, rightOverlap) < Math.min(topOverlap, bottomOverlap)
                    ? velocity.reflectX() : velocity.reflectY();
        }
        ball.setVelocity(velocity);
    }

    private boolean circleIntersects(Ball ball, Rectangle2D.Double rectangle) {
        Vector2 center = ball.getPosition();
        double closestX = Math.max(rectangle.x, Math.min(center.x(), rectangle.x + rectangle.width));
        double closestY = Math.max(rectangle.y, Math.min(center.y(), rectangle.y + rectangle.height));
        double dx = center.x() - closestX;
        double dy = center.y() - closestY;
        return dx * dx + dy * dy <= ball.getRadius() * ball.getRadius();
    }

    private void applyPowerUp(PowerUpType type) {
        switch (type) {
            case EXPAND_PADDLE -> paddle.expand();
            case SLOW_BALL -> balls.forEach(ball -> ball.setSpeed(ball.getSpeed() * 0.82));
            case MULTI_BALL -> splitBalls();
            case EXTRA_LIFE -> lives++;
        }
    }

    private void splitBalls() {
        List<Ball> additions = new ArrayList<>();
        for (Ball ball : balls) {
            if (balls.size() + additions.size() >= GameConfig.MAX_BALLS) {
                break;
            }
            additions.add(ball.copyWithVelocity(ball.getVelocity().rotateDegrees(18.0)));
        }
        balls.addAll(additions);
    }

    private void completeLevel() {
        score += GameConfig.LEVEL_CLEAR_BONUS * currentLevelNumber;
        combo = 0;
        powerUps.clear();
        balls.clear();
        state = GameState.LEVEL_CLEARED;
        stateTimer = GameConfig.LEVEL_CLEAR_SECONDS;
    }

    private void loseLife() {
        lives--;
        combo = 0;
        powerUps.clear();
        paddle.resetSize();
        paddle.reset();
        if (lives <= 0) {
            state = GameState.GAME_OVER;
        } else {
            spawnBalls(1);
            beginCountdown();
        }
    }

    private void loadLevel(int levelNumber) {
        currentLevel = scriptedLevels == null
                ? LevelFactory.createLevel(levelNumber, activeDifficulty)
                : scriptedLevels.get(levelNumber - 1);
        bricks.clear();
        for (BrickSpec spec : currentLevel.bricks()) {
            bricks.add(new Brick(spec));
        }
        powerUps.clear();
        combo = 0;
        paddle.resetSize();
        paddle.reset();
        spawnBalls(currentLevel.initialBalls());
        beginCountdown();
    }

    private void spawnBalls(int count) {
        balls.clear();
        double speed = currentLevel.ballSpeed();
        double centerX = paddle.getCenterX();
        double y = paddle.getY() - GameConfig.BALL_RADIUS - 2.0;
        double[] angles = {90.0, 106.0, 74.0, 118.0, 62.0, 82.0};
        for (int i = 0; i < Math.min(count, angles.length); i++) {
            balls.add(new Ball(new Vector2(centerX, y),
                    Vector2.fromAngleDegrees(angles[i], speed),
                    GameConfig.BALL_RADIUS, GameConfig.BALL_COLOR));
        }
    }

    private void beginCountdown() {
        state = GameState.COUNTDOWN;
        stateTimer = GameConfig.COUNTDOWN_SECONDS;
    }
}
