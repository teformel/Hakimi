package com.hakimi.road.entity;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 场景实体类
 * 表示路边的装饰物（如树木）
 */
public class Scenery extends GameEntity {
    private static final Logger logger = LogManager.getLogger(Scenery.class);

    public enum SceneryType {
        TREE
    }

    private int side; // -1: 左侧, 1: 右侧
                      // y is inherited
    private SceneryType type;

    public Scenery(int side, int y, SceneryType type) {
        super(y);
        this.side = side;
        this.type = type;
        logger.debug("场景创建: side={}, y={}, type={}", side, y, type);
    }

    // move and isOutOfScreen are inherited

    public int getSide() {
        return side;
    }

    public SceneryType getType() {
        return type;
    }
}
