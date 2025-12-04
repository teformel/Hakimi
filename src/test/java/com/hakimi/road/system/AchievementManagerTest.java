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

        // Mock file path if possible, or just let it use default relative path for now
        // Ideally we should inject the file path or use a temporary file, but for this
        // existing code
        // we might need to rely on integration style testing or modify the class to
        // accept a path.
        // For now, we will test the logic in memory.
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
