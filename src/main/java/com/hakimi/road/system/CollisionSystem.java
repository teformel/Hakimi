package com.hakimi.road.system;

import com.hakimi.road.entity.Obstacle;
import com.hakimi.road.entity.Player;

import java.util.List;

/**
 * 碰撞检测系统
 * 负责检测玩家与障碍物的碰撞
 */
public class CollisionSystem {
    
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
                    continue;
                } else if (obstacle.getHeight() == Obstacle.ObstacleHeight.HIGH && player.isSliding()) {
                    // 高障碍物可以通过滑铲躲避
                    continue;
                } else if (obstacle.getHeight() == Obstacle.ObstacleHeight.FULL) {
                    // 全高障碍物无法躲避，只能切换车道
                    if (obstacle.getLane() == playerLane) {
                        return true;
                    }
                    continue;
                } else {
                    // 正常状态碰撞
                    return true;
                }
            }
        }
        return false;
    }
}

