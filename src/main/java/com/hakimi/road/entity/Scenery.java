package com.hakimi.road.entity;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 场景实体类
 * 表示路边的装饰物（如树木）
 */
public class Scenery {
    private static final Logger logger = LogManager.getLogger(Scenery.class);

    public enum SceneryType {
        TREE
    }

    private int side; // -1: 左侧, 1: 右侧
    private int y;
    private SceneryType type;

    public Scenery(int side, int y, SceneryType type) {
        this.side = side;
        this.y = y;
        this.type = type;
        logger.debug("场景创建: side={}, y={}, type={}", side, y, type);
    }

    /**
     * 移动场景
     */
    public void move(int speed) {
        this.y += speed;
    }

    /**
     * 检查是否超出屏幕
     */
    public boolean isOutOfScreen(int screenHeight) {
        return y > screenHeight;
    }

    public int getSide() {
        return side;
    }

    public int getY() {
        return y;
    }

    public SceneryType getType() {
        return type;
    }
}
