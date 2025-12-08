package com.hakimi.road.system;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 分数系统
 * 负责管理游戏分数和距离
 */
public class ScoreSystem {
    private static final Logger logger = LogManager.getLogger(ScoreSystem.class);
    private int score;
    private int distance;
    private int combo; // 连击数

    public ScoreSystem() {
        this.score = 0;
        this.distance = 0;
        this.combo = 0;
        logger.debug("ScoreSystem初始化");
    }

    /**
     * 更新距离和分数
     */
    public void update(int gameSpeed) {
        distance += gameSpeed;
        score = distance / com.hakimi.road.util.GameConfig.SCORE_PER_DISTANCE;
    }

    /**
     * 成功躲避障碍物，增加分数和连击
     */
    public void obstacleAvoided() {
        combo++;
        int oldScore = score;
        score += com.hakimi.road.util.GameConfig.SCORE_PER_OBSTACLE;
        if (combo > 5) {
            score += combo / 5;
        }
        logger.debug("躲避障碍物: 分数 {} -> {}, 连击={}", oldScore, score, combo);
    }

    /**
     * 增加指定分数
     */
    public void addScore(int amount) {
        this.score += amount;
    }

    /**
     * 重置连击
     */
    public void resetCombo() {
        if (combo > 0) {
            logger.debug("重置连击: {}", combo);
        }
        combo = 0;
    }

    /**
     * 重置所有数据
     */
    public void reset() {
        logger.info("重置分数系统: 最终分数={}, 距离={}", score, distance);
        score = 0;
        distance = 0;
        combo = 0;
    }

    // Getters
    public int getScore() {
        return score;
    }

    public int getDistance() {
        return distance;
    }

    public int getCombo() {
        return combo;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public void setCombo(int combo) {
        this.combo = combo;
    }
}
