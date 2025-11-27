package com.hakimi.road.util;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * 设置管理类
 * 负责保存和加载游戏设置
 */
public class SettingsManager {
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
            }
            settingsPath = dataDir.resolve(SETTINGS_FILE);
            loadSettings();
        } catch (IOException e) {
            System.err.println("无法创建设置目录: " + e.getMessage());
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
            } else {
                // 使用默认设置
                setDefaults();
            }
        } catch (IOException e) {
            System.err.println("加载设置失败: " + e.getMessage());
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
        } catch (IOException e) {
            System.err.println("保存设置失败: " + e.getMessage());
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
    }
    
    // Getters and Setters
    public int getBaseGameSpeed() {
        return Integer.parseInt(settings.getProperty("baseGameSpeed", String.valueOf(DEFAULT_BASE_GAME_SPEED)));
    }
    
    public void setBaseGameSpeed(int speed) {
        settings.setProperty("baseGameSpeed", String.valueOf(speed));
    }
    
    public int getObstacleSpawnRate() {
        return Integer.parseInt(settings.getProperty("obstacleSpawnRate", String.valueOf(DEFAULT_OBSTACLE_SPAWN_RATE)));
    }
    
    public void setObstacleSpawnRate(int rate) {
        settings.setProperty("obstacleSpawnRate", String.valueOf(rate));
    }
    
    public int getSpeedIncreaseInterval() {
        return Integer.parseInt(settings.getProperty("speedIncreaseInterval", String.valueOf(DEFAULT_SPEED_INCREASE_INTERVAL)));
    }
    
    public void setSpeedIncreaseInterval(int interval) {
        settings.setProperty("speedIncreaseInterval", String.valueOf(interval));
    }
    
    public int getGameLoopDelayMs() {
        return Integer.parseInt(settings.getProperty("gameLoopDelayMs", String.valueOf(DEFAULT_GAME_LOOP_DELAY_MS)));
    }
    
    public void setGameLoopDelayMs(int delay) {
        settings.setProperty("gameLoopDelayMs", String.valueOf(delay));
    }
    
    /**
     * 重置为默认设置
     */
    public void resetToDefaults() {
        setDefaults();
        saveSettings();
    }
}

