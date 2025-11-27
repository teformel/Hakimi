package com.hakimi.road.util;


import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * 存档管理类
 * 负责保存和加载游戏存档
 */
public class SaveManager {
    private static final String SAVE_DIR = "data/saves";
    private static SaveManager instance;
    
    private SaveManager() {
        // 确保存档目录存在
        try {
            Path saveDir = Paths.get(SAVE_DIR);
            if (!Files.exists(saveDir)) {
                Files.createDirectories(saveDir);
            }
        } catch (IOException e) {
            System.err.println("无法创建存档目录: " + e.getMessage());
        }
    }
    
    public static SaveManager getInstance() {
        if (instance == null) {
            instance = new SaveManager();
        }
        return instance;
    }
    
    /**
     * 保存游戏状态
     */
    public boolean saveGame(String saveName, GameSaveData saveData) {
        try {
            Path saveFile = Paths.get(SAVE_DIR, saveName + ".save");
            Properties props = new Properties();
            
            // 保存玩家数据
            props.setProperty("player.lane", String.valueOf(saveData.playerLane));
            props.setProperty("player.y", String.valueOf(saveData.playerY));
            props.setProperty("player.state", saveData.playerState);
            props.setProperty("player.stateTimer", String.valueOf(saveData.playerStateTimer));
            
            // 保存追逐者数据
            props.setProperty("chaser.y", String.valueOf(saveData.chaserY));
            props.setProperty("chaser.animationTick", String.valueOf(saveData.chaserAnimationTick));
            
            // 保存游戏状态
            props.setProperty("game.speed", String.valueOf(saveData.gameSpeed));
            props.setProperty("game.hitCount", String.valueOf(saveData.hitCount));
            props.setProperty("game.chaserVisibleTimer", String.valueOf(saveData.chaserVisibleTimer));
            props.setProperty("game.chaserAwakened", String.valueOf(saveData.chaserAwakened));
            props.setProperty("game.caughtByChaser", String.valueOf(saveData.caughtByChaser));
            
            // 保存分数系统
            props.setProperty("score.score", String.valueOf(saveData.score));
            props.setProperty("score.distance", String.valueOf(saveData.distance));
            props.setProperty("score.combo", String.valueOf(saveData.combo));
            
            // 保存障碍物
            props.setProperty("obstacles.count", String.valueOf(saveData.obstacles.size()));
            for (int i = 0; i < saveData.obstacles.size(); i++) {
                ObstacleData obs = saveData.obstacles.get(i);
                props.setProperty("obstacle." + i + ".lane", String.valueOf(obs.lane));
                props.setProperty("obstacle." + i + ".y", String.valueOf(obs.y));
                props.setProperty("obstacle." + i + ".type", String.valueOf(obs.type));
            }
            
            // 保存时间戳
            props.setProperty("save.timestamp", String.valueOf(System.currentTimeMillis()));
            
            try (OutputStream output = Files.newOutputStream(saveFile)) {
                props.store(output, "游戏存档 - " + saveName);
            }
            return true;
        } catch (IOException e) {
            System.err.println("保存游戏失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 加载游戏状态
     */
    public GameSaveData loadGame(String saveName) {
        try {
            Path saveFile = Paths.get(SAVE_DIR, saveName + ".save");
            if (!Files.exists(saveFile)) {
                return null;
            }
            
            Properties props = new Properties();
            try (InputStream input = Files.newInputStream(saveFile)) {
                props.load(input);
            }
            
            GameSaveData saveData = new GameSaveData();
            
            // 加载玩家数据
            saveData.playerLane = Integer.parseInt(props.getProperty("player.lane", "1"));
            saveData.playerY = Integer.parseInt(props.getProperty("player.y", "0"));
            saveData.playerState = props.getProperty("player.state", "NORMAL");
            saveData.playerStateTimer = Integer.parseInt(props.getProperty("player.stateTimer", "0"));
            
            // 加载追逐者数据
            saveData.chaserY = Integer.parseInt(props.getProperty("chaser.y", "0"));
            saveData.chaserAnimationTick = Integer.parseInt(props.getProperty("chaser.animationTick", "0"));
            
            // 加载游戏状态
            saveData.gameSpeed = Integer.parseInt(props.getProperty("game.speed", "1"));
            saveData.hitCount = Integer.parseInt(props.getProperty("game.hitCount", "0"));
            saveData.chaserVisibleTimer = Integer.parseInt(props.getProperty("game.chaserVisibleTimer", "0"));
            saveData.chaserAwakened = Boolean.parseBoolean(props.getProperty("game.chaserAwakened", "false"));
            saveData.caughtByChaser = Boolean.parseBoolean(props.getProperty("game.caughtByChaser", "false"));
            
            // 加载分数系统
            saveData.score = Integer.parseInt(props.getProperty("score.score", "0"));
            saveData.distance = Integer.parseInt(props.getProperty("score.distance", "0"));
            saveData.combo = Integer.parseInt(props.getProperty("score.combo", "0"));
            
            // 加载障碍物
            int obstacleCount = Integer.parseInt(props.getProperty("obstacles.count", "0"));
            saveData.obstacles = new ArrayList<>();
            for (int i = 0; i < obstacleCount; i++) {
                ObstacleData obs = new ObstacleData();
                obs.lane = Integer.parseInt(props.getProperty("obstacle." + i + ".lane", "0"));
                obs.y = Integer.parseInt(props.getProperty("obstacle." + i + ".y", "0"));
                obs.type = Integer.parseInt(props.getProperty("obstacle." + i + ".type", "0"));
                saveData.obstacles.add(obs);
            }
            
            saveData.timestamp = Long.parseLong(props.getProperty("save.timestamp", "0"));
            
            return saveData;
        } catch (IOException | NumberFormatException e) {
            System.err.println("加载游戏失败: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 获取所有存档列表
     */
    public List<String> getSaveList() {
        List<String> saves = new ArrayList<>();
        try {
            Path saveDir = Paths.get(SAVE_DIR);
            if (Files.exists(saveDir)) {
                Files.list(saveDir)
                    .filter(path -> path.toString().endsWith(".save"))
                    .forEach(path -> {
                        String fileName = path.getFileName().toString();
                        saves.add(fileName.substring(0, fileName.length() - 5)); // 移除 .save 扩展名
                    });
            }
        } catch (IOException e) {
            System.err.println("获取存档列表失败: " + e.getMessage());
        }
        return saves;
    }
    
    /**
     * 删除存档
     */
    public boolean deleteSave(String saveName) {
        try {
            Path saveFile = Paths.get(SAVE_DIR, saveName + ".save");
            if (Files.exists(saveFile)) {
                Files.delete(saveFile);
                return true;
            }
            return false;
        } catch (IOException e) {
            System.err.println("删除存档失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 获取存档时间戳
     */
    public long getSaveTimestamp(String saveName) {
        try {
            Path saveFile = Paths.get(SAVE_DIR, saveName + ".save");
            if (!Files.exists(saveFile)) {
                return 0;
            }
            
            Properties props = new Properties();
            try (InputStream input = Files.newInputStream(saveFile)) {
                props.load(input);
            }
            return Long.parseLong(props.getProperty("save.timestamp", "0"));
        } catch (IOException | NumberFormatException e) {
            return 0;
        }
    }
    
    /**
     * 游戏存档数据类
     */
    public static class GameSaveData {
        public int playerLane;
        public int playerY;
        public String playerState;
        public int playerStateTimer;
        
        public int chaserY;
        public int chaserAnimationTick;
        
        public int gameSpeed;
        public int hitCount;
        public int chaserVisibleTimer;
        public boolean chaserAwakened;
        public boolean caughtByChaser;
        
        public int score;
        public int distance;
        public int combo;
        
        public List<ObstacleData> obstacles;
        public long timestamp;
    }
    
    /**
     * 障碍物数据类
     */
    public static class ObstacleData {
        public int lane;
        public int y;
        public int type;
    }
}

