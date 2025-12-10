package com.hakimi.road.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class SaveManagerTest {

    private static final String TEST_SAVE_NAME = "test_save";

    @BeforeEach
    void setUp() throws IOException {
        // Ensure clean state
        SaveManager.getInstance().deleteSave(TEST_SAVE_NAME);
    }

    @AfterEach
    void tearDown() {
        SaveManager.getInstance().deleteSave(TEST_SAVE_NAME);
    }

    @Test
    void testSaveAndLoadWithObstacles() {
        SaveManager saveManager = SaveManager.getInstance();
        SaveManager.GameSaveData saveData = new SaveManager.GameSaveData();

        // Setup test data
        saveData.playerLane = 1;
        saveData.playerY = 10;
        saveData.playerState = "NORMAL";
        saveData.driedFishCount = 5;
        saveData.hasHagenAbility = true;

        // Use ArrayList instead of generic List to match implementation if needed,
        // though List is fine
        saveData.obstacles = new ArrayList<>();
        SaveManager.ObstacleData obs1 = new SaveManager.ObstacleData();
        obs1.lane = 0;
        obs1.y = 20;
        obs1.type = 0;
        saveData.obstacles.add(obs1);

        SaveManager.ObstacleData obs2 = new SaveManager.ObstacleData();
        obs2.lane = 2;
        obs2.y = 30;
        obs2.type = 1;
        saveData.obstacles.add(obs2);

        // Initialize other required fields to avoid NPEs if any (though logic seems
        // robust)
        saveData.items = new ArrayList<>();
        saveData.unlockedAchievements = new ArrayList<>();

        // Save
        boolean saveResult = saveManager.saveGame(TEST_SAVE_NAME, saveData);
        assertTrue(saveResult, "Save should succeed");

        // Load
        SaveManager.GameSaveData loadedData = saveManager.loadGame(TEST_SAVE_NAME);
        assertNotNull(loadedData, "Loaded data should not be null");

        // Verify Obstacles
        assertNotNull(loadedData.obstacles, "Loaded obstacles should not be null");
        assertEquals(2, loadedData.obstacles.size(), "Should have exactly 2 obstacles");

        // Verify Content (optional, but good for completeness)
        assertEquals(0, loadedData.obstacles.get(0).lane);
        assertEquals(20, loadedData.obstacles.get(0).y);

        assertEquals(2, loadedData.obstacles.get(1).lane);
        assertEquals(30, loadedData.obstacles.get(1).y);
    }
}
