package com.hakimi.road;

import com.hakimi.road.system.Achievement;
import com.hakimi.road.system.AchievementManager;
import com.hakimi.road.util.SaveManager;
import java.io.File;
import java.util.List;

public class VerificationTest {
    public static void main(String[] args) {
        System.out.println("Starting Verification Test...");

        // 1. Clean up
        deleteFile("data/achievements.properties");
        deleteFile("data/saves/test_save.save");

        // 2. Unlock an achievement
        System.out.println("Unlocking FIRST_STEP...");
        AchievementManager.getInstance().unlockAchievement(Achievement.FIRST_STEP);

        if (!AchievementManager.getInstance().isUnlocked(Achievement.FIRST_STEP)) {
            System.err.println("FAIL: Achievement not unlocked in manager.");
            System.exit(1);
        }

        // 3. Save Game
        System.out.println("Saving game...");
        SaveManager.GameSaveData saveData = new SaveManager.GameSaveData();
        // Mock data
        saveData.playerLane = 1;
        saveData.obstacles = new java.util.ArrayList<>();
        // Save achievements
        saveData.unlockedAchievements = AchievementManager.getInstance().getUnlockedAchievementIds();

        SaveManager.getInstance().saveGame("test_save", saveData);

        // 4. Simulate Data Loss (Clear global achievements)
        System.out.println("Simulating data loss...");
        deleteFile("data/achievements.properties");
        // Re-initialize manager (hacky via reflection or just create new instance if
        // possible,
        // but singleton is hard to reset. We will just manually clear the set via
        // reflection or assume a restart)
        // Since we can't easily restart JVM, we will just check if the SAVE FILE has
        // the data.

        SaveManager.GameSaveData loadedData = SaveManager.getInstance().loadGame("test_save");
        if (loadedData == null) {
            System.err.println("FAIL: Could not load save file.");
            System.exit(1);
        }

        if (loadedData.unlockedAchievements == null || loadedData.unlockedAchievements.isEmpty()) {
            System.err.println("FAIL: Loaded data has no achievements.");
            System.exit(1);
        }

        if (!loadedData.unlockedAchievements.contains(Achievement.FIRST_STEP.getId())) {
            System.err.println("FAIL: Loaded data missing FIRST_STEP achievement.");
            System.exit(1);
        }

        System.out.println("SUCCESS: Achievement found in save file!");

        // 5. Test Merge logic
        System.out.println("Testing merge logic...");
        // Manually clear unlocked achievements in manager for testing (using
        // reflection)
        try {
            java.lang.reflect.Field field = AchievementManager.class.getDeclaredField("unlockedAchievements");
            field.setAccessible(true);
            ((java.util.Set) field.get(AchievementManager.getInstance())).clear();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (AchievementManager.getInstance().isUnlocked(Achievement.FIRST_STEP)) {
            System.err.println("FAIL: Failed to clear achievements for test.");
        }

        AchievementManager.getInstance().mergeUnlockedAchievements(loadedData.unlockedAchievements);

        if (AchievementManager.getInstance().isUnlocked(Achievement.FIRST_STEP)) {
            System.out.println("SUCCESS: Achievement merged back to manager!");
        } else {
            System.err.println("FAIL: Achievement NOT merged back to manager.");
            System.exit(1);
        }
    }

    private static void deleteFile(String path) {
        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
    }
}
