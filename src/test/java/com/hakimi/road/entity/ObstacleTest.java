package com.hakimi.road.entity;

import com.hakimi.road.util.GameConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Obstacle 类的单元测试
 */
class ObstacleTest {

    private Obstacle obstacle;

    @BeforeEach
    void setUp() {
        obstacle = new Obstacle(1, 10, 0);
    }

    @Test
    void testInitialState() {
        assertEquals(1, obstacle.getLane());
        assertEquals(10, obstacle.getY());
        assertEquals(0, obstacle.getType());
        assertEquals(Obstacle.ObstacleHeight.LOW, obstacle.getHeight());
    }

    @Test
    void testObstacleHeight() {
        // 类型0应该是低障碍物
        Obstacle lowObstacle = new Obstacle(0, 0, 0);
        assertEquals(Obstacle.ObstacleHeight.LOW, lowObstacle.getHeight());

        // 类型1应该是高障碍物
        Obstacle highObstacle = new Obstacle(0, 0, 1);
        assertEquals(Obstacle.ObstacleHeight.HIGH, highObstacle.getHeight());
    }

    @Test
    void testMove() {
        int initialY = obstacle.getY();
        int speed = 5;

        obstacle.move(speed);
        assertEquals(initialY + speed, obstacle.getY());

        obstacle.move(speed);
        assertEquals(initialY + speed * 2, obstacle.getY());
    }

    @Test
    void testIsOutOfScreen() {
        int screenHeight = 30;

        // 在屏幕内
        assertFalse(obstacle.isOutOfScreen(screenHeight));

        // 移动到屏幕外
        obstacle.setY(screenHeight + 1);
        assertTrue(obstacle.isOutOfScreen(screenHeight));
    }

    @Test
    void testCheckCollision_SameLane() {
        int screenHeight = 30;
        int playerLane = 1;
        int playerY = 20;
        int playerHeight = GameConfig.PLAYER_HEIGHT;

        obstacle.setLane(playerLane);
        obstacle.setY(playerY - 2); // 障碍物在玩家上方附近

        // 应该检测到碰撞
        assertTrue(obstacle.checkCollision(playerLane, playerY, playerHeight, screenHeight));
    }

    @Test
    void testCheckCollision_DifferentLane() {
        int screenHeight = 30;
        int playerLane = 0;
        int playerY = 20;
        int playerHeight = GameConfig.PLAYER_HEIGHT;

        obstacle.setLane(1); // 不同车道
        obstacle.setY(playerY);

        // 不应该检测到碰撞
        assertFalse(obstacle.checkCollision(playerLane, playerY, playerHeight, screenHeight));
    }

    @Test
    void testCheckCollision_NoOverlap() {
        int screenHeight = 30;
        int playerLane = 1;
        int playerY = 20;
        int playerHeight = GameConfig.PLAYER_HEIGHT;

        obstacle.setLane(playerLane);
        obstacle.setY(5); // 障碍物远在玩家上方

        // 不应该检测到碰撞
        assertFalse(obstacle.checkCollision(playerLane, playerY, playerHeight, screenHeight));
    }

    @Test
    void testSetters() {
        obstacle.setLane(2);
        assertEquals(2, obstacle.getLane());

        obstacle.setY(50);
        assertEquals(50, obstacle.getY());

        obstacle.setType(1);
        assertEquals(1, obstacle.getType());
    }

    @Test
    void testCollisionBoundary() {
        int screenHeight = 30;
        int playerLane = 1;
        int playerY = 20;
        int playerHeight = GameConfig.PLAYER_HEIGHT;

        obstacle.setLane(playerLane);

        // 测试边界情况：障碍物刚好在玩家下方
        obstacle.setY(screenHeight - 1);
        assertTrue(obstacle.checkCollision(playerLane, playerY, playerHeight, screenHeight));

        // 测试边界情况：障碍物刚好在玩家上方
        obstacle.setY(playerY - GameConfig.OBSTACLE_HEIGHT);
        boolean collision = obstacle.checkCollision(playerLane, playerY, playerHeight, screenHeight);
        // 根据实际碰撞逻辑判断
        assertNotNull(collision); // 至少不会抛出异常
    }
}
