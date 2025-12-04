package com.hakimi.road.entity;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 追逐者实体类
 * 表示游戏中追逐玩家的角色
 */
public class Chaser {
    private static final Logger logger = LogManager.getLogger(Chaser.class);
    private int y; // 追逐者当前y坐标
    private int offsetFromPlayer = 8; // 与玩家保持的距离
    private int animationTick = 0;

    public Chaser() {
        this.y = 0;
        logger.debug("Chaser创建");
    }

    /**
     * 更新追逐者位置
     * 
     * @param playerY   玩家当前y坐标
     * @param gameSpeed 当前速度
     */
    public void update(int playerY, int gameSpeed) {
        int oldY = this.y;
        int targetY = Math.max(1, playerY - offsetFromPlayer);
        if (y < targetY) {
            y = Math.min(targetY, y + gameSpeed);
        } else {
            y = targetY;
        }
        animationTick++;
        if (oldY != this.y) {
            logger.trace("Chaser更新位置: {} -> {}, 目标y={}", oldY, this.y, targetY);
        }
    }

    public int getY() {
        return y;
    }

    public void reset(int playerY) {
        this.y = Math.max(0, playerY - offsetFromPlayer);
        this.animationTick = 0;
        logger.debug("Chaser重置: y={}", this.y);
    }

    public int getAnimationFrame() {
        return (animationTick / 5) % 2;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setAnimationTick(int tick) {
        this.animationTick = tick;
    }

    public int getAnimationTick() {
        return animationTick;
    }
}
