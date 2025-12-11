package com.hakimi.road.entity;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 道具实体类
 * 表示游戏中的可收集物品（小鱼干、特殊能力等）
 */
public class Item extends GameEntity {
    private static final Logger logger = LogManager.getLogger(Item.class);
    private int lane; // 所在车道
                      // y is inherited
    private ItemType type; // 道具类型

    public enum ItemType {
        DRIED_FISH, // 小鱼干
        HAGEN_ABILITY // 哈根能力
    }

    public Item(int lane, int y, ItemType type) {
        super(y);
        this.lane = lane;
        this.type = type;
        logger.debug("道具创建: lane={}, y={}, type={}", lane, y, type);
    }

    // move and isOutOfScreen are inherited

    // Getters
    public int getLane() {
        return lane;
    }

    public ItemType getType() {
        return type;
    }
}
