# Arkanoid Java

A standalone Java/Swing Arkanoid game rebuilt from an older course-style project. It now includes a start screen, difficulty selection, pause/resume, countdowns, lives, score, power-ups, and infinitely generated levels that continue getting harder.


## Portfolio Value

This project demonstrates Java software-engineering skills beyond basic syntax: object-oriented game-state modeling, Swing/Java2D UI programming, keyboard/mouse input handling, Gradle project structure, and unit-tested gameplay logic.

## Features

- Start screen with Easy, Medium, and Hard difficulty.
- Infinite generated levels with changing layouts, backgrounds, speed, brick count, and brick toughness.
- Difficulty affects lives, paddle width, ball speed, brick toughness, and power-up frequency.
- Clear pause screen with resume instructions.
- Countdown before each level, after losing a life, and after resuming from pause.
- Power-ups: wide paddle, slow ball, multi-ball, and extra life.
- Standard Java Swing/Java2D only; no external game library.

## Requirements

- JDK 17 or newer.
- The Gradle wrapper is pinned to Gradle 9.5.1.

## Run

```bash
./gradlew run
```

For a non-graphical startup check:

```bash
./gradlew run --args="--smoke-test"
```

## Test

```bash
./gradlew test
```

## Controls

- `1` / `E`: select Easy.
- `2` / `M`: select Medium.
- `3` / `H`: select Hard.
- Up/down or left/right arrows on the start screen: switch difficulty.
- Mouse click a difficulty row: select it.
- `Enter`, `Space`, or the Start button: start the selected difficulty.
- Left/right arrows while playing: move paddle.
- `P` or `Esc`: pause/resume.

## Project Structure

```text
src/main/java/com/arkanoid/
  ArkanoidApp.java          Application entry point
  model/                    Pure gameplay state, generated levels, physics, and scoring
  ui/                       Swing window, panel, renderer, and input bindings

src/test/java/com/arkanoid/model/
  Unit tests for gameplay rules and state transitions
```

## Notes

- The game is asset-free by design: all visuals are drawn in Java2D.
- No license is included in this portfolio copy.
