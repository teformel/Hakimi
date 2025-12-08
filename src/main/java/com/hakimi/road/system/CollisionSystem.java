package com.hakimi.road.system;

import com.hakimi.road.entity.Obstacle;
import com.hakimi.road.entity.Player;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 碰撞检测系统
 * 负责检测玩家与障碍物的碰撞
 */
public class CollisionSystem {
    private static final Logger logger = LogManager.getLogger(CollisionSystem.class);

    /**
     * 获取碰撞的障碍物
     * 
     * @return 碰撞的障碍物，如果没有碰撞则返回null
     */
    public Obstacle getCollidedObstacle(Player player, List<Obstacle> obstacles, int screenHeight) {
        int playerLane = player.getLane();
        int playerY = player.calculateY(screenHeight);
        int playerHeight = com.hakimi.road.util.GameConfig.PLAYER_HEIGHT;

        for (Obstacle obstacle : obstacles) {
            if (obstacle.checkCollision(playerLane, playerY, playerHeight, screenHeight)) {
                // 检查玩家状态是否可以躲避
                if (obstacle.getHeight() == Obstacle.ObstacleHeight.LOW && player.isJumping()) {
                    continue;
                } else if (obstacle.getHeight() == Obstacle.ObstacleHeight.HIGH && player.isSliding()) {
                    continue;
                } else if (obstacle.getHeight() == Obstacle.ObstacleHeight.FULL) {
                    if (obstacle.getLane() == playerLane) {
                        return obstacle;
                    }
                    continue;
                } else {
                    return obstacle;
                }
            }
        }
        return null;
    }

    /**
     * 检查玩家是否与障碍物碰撞
     */
    public boolean checkCollision(Player player, List<Obstacle> obstacles, int screenHeight) {

        int playerLane = player.getLane();
        int playerY = player.calculateY(screenHeight);
        int playerHeight = com.hakimi.road.util.GameConfig.PLAYER_HEIGHT;

        for (Obstacle obstacle : obstacles) {
            if (obstacle.checkCollision(playerLane, playerY, playerHeight, screenHeight)) {
                // 检查玩家状态是否可以躲避
                if (obstacle.getHeight() == Obstacle.ObstacleHeight.LOW && player.isJumping()) {
                    // 低障碍物可以通过跳跃躲避
                    logger.debug("跳跃躲避低障碍物成功");
                    continue;
                } else if (obstacle.getHeight() == Obstacle.ObstacleHeight.HIGH && player.isSliding()) {
                    // 高障碍物可以通过滑铲躲避
                    logger.debug("滑铲躲避高障碍物成功");
                    continue;
                } else if (obstacle.getHeight() == Obstacle.ObstacleHeight.FULL) {
                    // 全高障碍物无法躲避，只能切换车道
                    if (obstacle.getLane() == playerLane) {
                        logger.info("碰撞全高障碍物: 车道={}, 障碍物y={}", playerLane, obstacle.getY());
                        return true;
                    }
                    continue;
                } else {
                    // 正常状态碰撞
                    logger.info("碰撞障碍物: 车道={}, 玩家状态={}, 障碍物高度={}",
                            playerLane, player.getState(), obstacle.getHeight());
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 检查玩家是否与道具碰撞
     * 
     * @return 碰撞的道具，如果没有碰撞则返回null
     */
    public com.hakimi.road.entity.Item checkItemCollision(Player player, List<com.hakimi.road.entity.Item> items,
            int screenHeight) {
        int playerLane = player.getLane();
        int playerY = player.calculateY(screenHeight);

        // 简单的碰撞判定：同车道且y轴距离够近
        for (com.hakimi.road.entity.Item item : items) {
            if (item.getLane() == playerLane) {
                // 道具通常较小，只要中心点接近即可
                int dist = Math.abs(item.getY() - playerY);
                if (dist <= 1) {
                    logger.debug("检测到道具拾取: type={}", item.getType());
                    return item;
                }
            }
        }
        return null;
    }
}
