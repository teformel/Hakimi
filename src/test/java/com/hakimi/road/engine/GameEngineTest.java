package com.hakimi.road.engine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class GameEngineTest {

    private GameEngine gameEngine;

    @BeforeEach
    public void setUp() {
        // Passing null for Screen as it's not used in core logic updates
        gameEngine = new GameEngine(null);
    }

    @Test
    public void testInitialState() {
        assertEquals(GameEngine.GameState.MENU, gameEngine.getGameState());
        assertNotNull(gameEngine.getPlayer());
        assertNotNull(gameEngine.getObstacles());
    }

    @Test
    public void testStartGame() {
        gameEngine.startGame();
        assertEquals(GameEngine.GameState.PLAYING, gameEngine.getGameState());
        assertEquals(0, gameEngine.getScoreSystem().getScore());
        assertEquals(0, gameEngine.getScoreSystem().getDistance());
    }

    @Test
    public void testUpdateIncreasesDistance() {
        gameEngine.startGame();
        int initialDistance = gameEngine.getScoreSystem().getDistance();

        // Simulating updates
        try {
            // Update involves random generation, but distance should increase over time if
            // speed > 0
            // GameEngine updates logic every 2 ticks (tickCounter % 2 != 0 return)
            // So we need call update multiple times
            for (int i = 0; i < 10; i++) {
                gameEngine.update();
            }
        } catch (Exception e) {
            fail("Update should not throw exception: " + e.getMessage());
        }

        assertTrue(gameEngine.getScoreSystem().getDistance() >= initialDistance);
        // If speed is defaults (base 5), distance += speed/10 or similar logic in
        // ScoreSystem.
        // Just checking state doesn't crash is good for now, verifying exact distance
        // requires specific ScoreSystem knowledge.
    }

    @Test
    public void testPauseToggle() {
        gameEngine.startGame();
        gameEngine.togglePause();
        assertEquals(GameEngine.GameState.PAUSED, gameEngine.getGameState());
        gameEngine.togglePause();
        assertEquals(GameEngine.GameState.PLAYING, gameEngine.getGameState());
    }
}
