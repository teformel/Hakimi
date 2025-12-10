package com.hakimi.road.level;

import com.googlecode.lanterna.TextColor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 关卡管理器
 * 根据游戏进度（距离）管理关卡切换
 */
public class LevelManager {
    private static final Logger logger = LogManager.getLogger(LevelManager.class);

    private Level currentLevel;

    // 预定义关卡
    private static final Level LEVEL_FOREST = new Level(
            "森林",
            TextColor.ANSI.CYAN,
            TextColor.ANSI.GREEN,
            TextColor.ANSI.BLACK_BRIGHT,
            Level.ObstacleStyle.FOREST);

    private static final Level LEVEL_DESERT = new Level(
            "沙漠",
            TextColor.ANSI.YELLOW, // 黄色天空
            TextColor.ANSI.YELLOW_BRIGHT, // 沙色草地
            TextColor.ANSI.WHITE, // 亮白色道路 (模拟强光)
            Level.ObstacleStyle.DESERT);

    private static final Level LEVEL_CYBERPUNK = new Level(
            "夜之城",
            TextColor.ANSI.BLACK, // 黑色天空 (夜晚)
            TextColor.ANSI.MAGENTA, // 紫色霓虹草地
            TextColor.ANSI.BLUE, // 蓝色道路
            Level.ObstacleStyle.CYBERPUNK);

    public LevelManager() {
        // 初始关卡
        this.currentLevel = LEVEL_FOREST;
    }

    /**
     * 更新关卡状态
     * 
     * @param distance 当前距离
     */
    public void update(int distance) {
        Level targetLevel = LEVEL_FOREST;

        if (distance >= 2000) {
            targetLevel = LEVEL_CYBERPUNK;
        } else if (distance >= 1000) {
            targetLevel = LEVEL_DESERT;
        }

        if (currentLevel != targetLevel) {
            logger.info("关卡切换: {} -> {}", currentLevel.getName(), targetLevel.getName());
            currentLevel = targetLevel;
        }
    }

    public Level getCurrentLevel() {
        return currentLevel;
    }
}
