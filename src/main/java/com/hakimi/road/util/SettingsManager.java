package com.hakimi.road.util;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 设置管理类
 * 负责保存和加载游戏设置
 */
public class SettingsManager {
    private static final Logger logger = LogManager.getLogger(SettingsManager.class);
    private static final String SETTINGS_DIR = "data";
    private static final String SETTINGS_FILE = "settings.properties";
    private static SettingsManager instance;

    private Properties settings;
    private Path settingsPath;

    // 默认设置
    private static final int DEFAULT_BASE_GAME_SPEED = 1;
    private static final int DEFAULT_OBSTACLE_SPAWN_RATE = 10;
    private static final int DEFAULT_SPEED_INCREASE_INTERVAL = 50;
    private static final int DEFAULT_GAME_LOOP_DELAY_MS = 100;

    private SettingsManager() {
        settings = new Properties();
        // 确保数据目录存在
        try {
            Path dataDir = Paths.get(SETTINGS_DIR);
            if (!Files.exists(dataDir)) {
                Files.createDirectories(dataDir);
                logger.info("创建设置目录: {}", SETTINGS_DIR);
            }
            settingsPath = dataDir.resolve(SETTINGS_FILE);
            loadSettings();
            logger.debug("SettingsManager初始化完成");
        } catch (IOException e) {
            logger.error("无法创建设置目录: {}", SETTINGS_DIR, e);
            settingsPath = Paths.get(SETTINGS_FILE); // 回退到当前目录
        }
    }

    public static SettingsManager getInstance() {
        if (instance == null) {
            instance = new SettingsManager();
        }
        return instance;
    }

    /**
     * 加载设置
     */
    public void loadSettings() {
        try {
            if (Files.exists(settingsPath)) {
                try (InputStream input = Files.newInputStream(settingsPath)) {
                    settings.load(input);
                }
                logger.info("设置加载成功: {}", settingsPath);
            } else {
                // 使用默认设置
                logger.warn("设置文件不存在，使用默认设置");
                setDefaults();
            }
        } catch (IOException e) {
            logger.error("加载设置失败，使用默认设置", e);
            setDefaults();
        }
    }

    /**
     * 保存设置
     */
    public void saveSettings() {
        try {
            try (OutputStream output = Files.newOutputStream(settingsPath)) {
                settings.store(output, "游戏设置");
            }
            logger.info("设置保存成功");
        } catch (IOException e) {
            logger.error("保存设置失败", e);
        }
    }

    /**
     * 设置默认值
     */
    private void setDefaults() {
        settings.setProperty("baseGameSpeed", String.valueOf(DEFAULT_BASE_GAME_SPEED));
        settings.setProperty("obstacleSpawnRate", String.valueOf(DEFAULT_OBSTACLE_SPAWN_RATE));
        settings.setProperty("speedIncreaseInterval", String.valueOf(DEFAULT_SPEED_INCREASE_INTERVAL));
        settings.setProperty("gameLoopDelayMs", String.valueOf(DEFAULT_GAME_LOOP_DELAY_MS));
        logger.debug("应用默认设置");
    }

    // Getters and Setters
    public int getBaseGameSpeed() {
        return Integer.parseInt(settings.getProperty("baseGameSpeed", String.valueOf(DEFAULT_BASE_GAME_SPEED)));
    }

    public void setBaseGameSpeed(int speed) {
        settings.setProperty("baseGameSpeed", String.valueOf(speed));
        logger.debug("更新基础游戏速度: {}", speed);
    }

    public int getObstacleSpawnRate() {
        return Integer.parseInt(settings.getProperty("obstacleSpawnRate", String.valueOf(DEFAULT_OBSTACLE_SPAWN_RATE)));
    }

    public void setObstacleSpawnRate(int rate) {
        settings.setProperty("obstacleSpawnRate", String.valueOf(rate));
        logger.debug("更新障碍物生成频率: {}", rate);
    }

    public int getSpeedIncreaseInterval() {
        return Integer.parseInt(
                settings.getProperty("speedIncreaseInterval", String.valueOf(DEFAULT_SPEED_INCREASE_INTERVAL)));
    }

    public void setSpeedIncreaseInterval(int interval) {
        settings.setProperty("speedIncreaseInterval", String.valueOf(interval));
        logger.debug("更新速度增加间隔: {}", interval);
    }

    public int getGameLoopDelayMs() {
        return Integer.parseInt(settings.getProperty("gameLoopDelayMs", String.valueOf(DEFAULT_GAME_LOOP_DELAY_MS)));
    }

    public void setGameLoopDelayMs(int delay) {
        settings.setProperty("gameLoopDelayMs", String.valueOf(delay));
        logger.debug("更新游戏循环延迟: {}ms", delay);
    }

    /**
     * 重置为默认设置
     */
    public void resetToDefaults() {
        logger.info("重置设置为默认值");
        setDefaults();
        saveSettings();
    }
}
