package com.hakimi.road.system;

import com.hakimi.road.ui.NotificationSystem;

import java.util.HashSet;
import java.util.Set;

/**
 * 成就管理器
 * 负责成就的解锁逻辑和状态管理
 */
public class AchievementManager {
    private static AchievementManager instance;
    private Set<String> unlockedAchievements;
    private NotificationSystem notificationSystem;

    private AchievementManager() {
        unlockedAchievements = new HashSet<>();
        loadAchievements();
    }

    public static AchievementManager getInstance() {
        if (instance == null) {
            instance = new AchievementManager();
        }
        return instance;
    }

    public void setNotificationSystem(NotificationSystem notificationSystem) {
        this.notificationSystem = notificationSystem;
    }

    /**
     * 尝试解锁成就
     */
    public void unlockAchievement(Achievement achievement) {
        if (!unlockedAchievements.contains(achievement.getId())) {
            unlockedAchievements.add(achievement.getId());
            if (notificationSystem != null) {
                notificationSystem.showAchievementUnlock(achievement);
            }
            saveAchievements();
        }
    }

    /**
     * 检查成就是否已解锁
     */
    public boolean isUnlocked(Achievement achievement) {
        return unlockedAchievements.contains(achievement.getId());
    }

    /**
     * 保存成就状态
     */
    private void saveAchievements() {
        try {
            java.util.Properties props = new java.util.Properties();
            for (String id : unlockedAchievements) {
                props.setProperty(id, "true");
            }

            java.nio.file.Path path = java.nio.file.Paths.get("data/achievements.properties");
            if (!java.nio.file.Files.exists(path.getParent())) {
                java.nio.file.Files.createDirectories(path.getParent());
            }

            try (java.io.OutputStream out = java.nio.file.Files.newOutputStream(path)) {
                props.store(out, "Unlocked Achievements");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 加载成就状态
     */
    public void loadAchievements() {
        try {
            java.nio.file.Path path = java.nio.file.Paths.get("data/achievements.properties");
            if (java.nio.file.Files.exists(path)) {
                java.util.Properties props = new java.util.Properties();
                try (java.io.InputStream in = java.nio.file.Files.newInputStream(path)) {
                    props.load(in);
                    unlockedAchievements.addAll(props.stringPropertyNames());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
