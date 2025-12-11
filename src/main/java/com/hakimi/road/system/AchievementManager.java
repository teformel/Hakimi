package com.hakimi.road.system;

import com.hakimi.road.ui.NotificationSystem;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;

/**
 * 成就管理器
 * 负责成就的解锁逻辑和状态管理
 */
public class AchievementManager {
    private static final Logger logger = LogManager.getLogger(AchievementManager.class);
    private static AchievementManager instance;
    private Set<String> unlockedAchievements;
    private NotificationSystem notificationSystem;
    private final ObjectMapper objectMapper;
    private String achievementsFile = "data/achievements.json";

    private AchievementManager() {
        unlockedAchievements = new HashSet<>();
        objectMapper = new ObjectMapper();
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
            logger.info("Achievement unlocked: {}", achievement.getId());
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
            File file = new File(achievementsFile);
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            objectMapper.writeValue(file, unlockedAchievements);
            logger.debug("Achievements saved to {}", achievementsFile);
        } catch (IOException e) {
            logger.error("Failed to save achievements", e);
        }
    }

    /**
     * 加载成就状态
     */
    public void loadAchievements() {
        unlockedAchievements = new HashSet<>(); // Reset state
        try {
            File file = new File(achievementsFile);
            if (file.exists()) {
                unlockedAchievements = objectMapper.readValue(file, new TypeReference<Set<String>>() {
                });
                logger.info("Loaded {} achievements", unlockedAchievements.size());
            }
        } catch (IOException e) {
            logger.error("Failed to load achievements", e);
            // Initialize empty if load fails
            unlockedAchievements = new HashSet<>();
        }
    }

    /**
     * 获取所有已解锁成就的ID列表
     */
    public List<String> getUnlockedAchievementIds() {
        return new ArrayList<>(unlockedAchievements);
    }

    /**
     * 合并成就列表（用于读档）
     */
    public void mergeUnlockedAchievements(List<String> achievementIds) {
        if (achievementIds == null)
            return;

        boolean changed = false;
        for (String id : achievementIds) {
            if (!unlockedAchievements.contains(id)) {
                unlockedAchievements.add(id);
                changed = true;
            }
        }

        if (changed) {
            saveAchievements();
        }
    }

    /**
     * Set the achievements file path (For Testing Only)
     */
    void setAchievementsFile(String filePath) {
        this.achievementsFile = filePath;
        loadAchievements(); // Reload from new file
    }
}
