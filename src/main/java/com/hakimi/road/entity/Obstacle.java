package com.hakimi.road.entity;

import com.hakimi.road.util.GameConfig;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 障碍物实体类
 * 表示游戏中的障碍物
 */
public class Obstacle {
    private static final Logger logger = LogManager.getLogger(Obstacle.class);
    private int lane; // 所在车道
    private int y; // y坐标
    private int type; // 障碍物类型 (0=石头, 1=栅栏)
    private ObstacleHeight height; // 障碍物高度类型

    public enum ObstacleHeight {
        LOW, // 低障碍物（需要跳跃）
        HIGH, // 高障碍物（需要滑铲）
        FULL // 全高障碍物（需要切换车道）
    }

    public Obstacle(int lane, int y, int type) {
        this.lane = lane;
        this.y = y;
        this.type = type;
        // 根据类型设置高度
        this.height = type == 0 ? ObstacleHeight.LOW : ObstacleHeight.HIGH;
        logger.debug("障碍物创建: lane={}, y={}, type={}, height={}", lane, y, type, height);
    }

    /**
     * 移动障碍物
     */
    public void move(int speed) {
        this.y += speed;
    }

    /**
     * 检查障碍物是否超出屏幕
     */
    public boolean isOutOfScreen(int screenHeight) {
        return y > screenHeight;
    }

    /**
     * 检查是否与玩家碰撞
     */
    public boolean checkCollision(int playerLane, int playerY, int playerHeight, int screenHeight) {
        if (this.lane != playerLane) {
            return false; // 不在同一车道
        }

        int obstacleBottom = y + GameConfig.OBSTACLE_HEIGHT;
        int playerBottom = screenHeight - 1;
        int playerTop = playerY;

        // 检查是否有重叠
        boolean collision = obstacleBottom >= playerTop && y <= playerBottom;
        if (collision) {
            logger.trace("检测到碰撞: 障碍物(lane={}, y={}), 玩家(lane={}, y={})",
                    this.lane, this.y, playerLane, playerY);
        }
        return collision;
    }

    // Getters and Setters
    public int getLane() {
        return lane;
    }

    public void setLane(int lane) {
        this.lane = lane;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public ObstacleHeight getHeight() {
        return height;
    }
}
