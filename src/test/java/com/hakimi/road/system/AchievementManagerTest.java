package com.hakimi.road.system;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class AchievementManagerTest {

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() throws Exception {
        // Reset singleton instance using reflection
        Field instance = AchievementManager.class.getDeclaredField("instance");
        instance.setAccessible(true);
        instance.set(null, null);

        // Initialize and set temp file
        AchievementManager manager = AchievementManager.getInstance();
        File tempFile = tempDir.resolve("test_achievements.json").toFile();
        // Use package-private setter created for testing
        manager.setAchievementsFile(tempFile.getAbsolutePath());
    }

    @Test
    void testSingleton() {
        AchievementManager instance1 = AchievementManager.getInstance();
        AchievementManager instance2 = AchievementManager.getInstance();
        assertSame(instance1, instance2);
    }

    @Test
    void testUnlockAchievement() {
        AchievementManager manager = AchievementManager.getInstance();
        Achievement achievement = Achievement.FIRST_STEP;

        assertFalse(manager.isUnlocked(achievement));

        manager.unlockAchievement(achievement);

        assertTrue(manager.isUnlocked(achievement));
    }

    @Test
    void testUnlockDuplicate() {
        AchievementManager manager = AchievementManager.getInstance();
        Achievement achievement = Achievement.SPRINTER;

        manager.unlockAchievement(achievement);
        assertTrue(manager.isUnlocked(achievement));

        // Unlock again should not cause error
        manager.unlockAchievement(achievement);
        assertTrue(manager.isUnlocked(achievement));
    }
}
