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
}
