package com.hakimi.road.system;

import com.hakimi.road.entity.Obstacle;
import com.hakimi.road.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CollisionSystem 类的单元测试
 */
class CollisionSystemTest {

    private CollisionSystem collisionSystem;
    private Player player;
    private List<Obstacle> obstacles;
    private static final int SCREEN_HEIGHT = 30;

    @BeforeEach
    void setUp() {
        collisionSystem = new CollisionSystem();
        player = new Player();
        obstacles = new ArrayList<>();
    }

    @Test
    void testNoCollision_EmptyObstacles() {
        // 没有障碍物时不应该有碰撞
        assertFalse(collisionSystem.checkCollision(player, obstacles, SCREEN_HEIGHT));
    }

    @Test
    void testNoCollision_DifferentLane() {
        player.setLane(0);
        Obstacle obstacle = new Obstacle(1, player.calculateY(SCREEN_HEIGHT), 0);
        obstacles.add(obstacle);

        // 不同车道不应该碰撞
        assertFalse(collisionSystem.checkCollision(player, obstacles, SCREEN_HEIGHT));
    }

    @Test
    void testCollision_SameLane() {
        player.setLane(1);
        int playerY = player.calculateY(SCREEN_HEIGHT);
        Obstacle obstacle = new Obstacle(1, playerY - 2, 0);
        obstacles.add(obstacle);

        // 同一车道且位置重叠应该碰撞
        assertTrue(collisionSystem.checkCollision(player, obstacles, SCREEN_HEIGHT));
    }

    @Test
    void testAvoidLowObstacle_ByJumping() {
        player.setLane(1);
        player.jump(); // 跳跃

        int playerY = player.calculateY(SCREEN_HEIGHT);
        Obstacle lowObstacle = new Obstacle(1, playerY - 2, 0); // 低障碍物
        obstacles.add(lowObstacle);

        // 跳跃应该能躲避低障碍物
        assertFalse(collisionSystem.checkCollision(player, obstacles, SCREEN_HEIGHT));
    }

    @Test
    void testAvoidHighObstacle_BySliding() {
        player.setLane(1);
        player.slide(); // 滑铲

        int playerY = player.calculateY(SCREEN_HEIGHT);
        Obstacle highObstacle = new Obstacle(1, playerY - 2, 1); // 高障碍物
        obstacles.add(highObstacle);

        // 滑铲应该能躲避高障碍物
        assertFalse(collisionSystem.checkCollision(player, obstacles, SCREEN_HEIGHT));
    }

    @Test
    void testCannotAvoidLowObstacle_BySliding() {
        player.setLane(1);
        player.slide(); // 滑铲

        int playerY = player.calculateY(SCREEN_HEIGHT);
        Obstacle lowObstacle = new Obstacle(1, playerY - 2, 0); // 低障碍物
        obstacles.add(lowObstacle);

        // 滑铲不能躲避低障碍物
        assertTrue(collisionSystem.checkCollision(player, obstacles, SCREEN_HEIGHT));
    }

    @Test
    void testCannotAvoidHighObstacle_ByJumping() {
        player.setLane(1);
        player.jump(); // 跳跃

        int playerY = player.calculateY(SCREEN_HEIGHT);
        Obstacle highObstacle = new Obstacle(1, playerY - 2, 1); // 高障碍物
        obstacles.add(highObstacle);

        // 跳跃不能躲避高障碍物
        assertTrue(collisionSystem.checkCollision(player, obstacles, SCREEN_HEIGHT));
    }

    @Test
    void testMultipleObstacles() {
        player.setLane(1);
        int playerY = player.calculateY(SCREEN_HEIGHT);

        // 添加多个障碍物
        obstacles.add(new Obstacle(0, playerY, 0)); // 不同车道
        obstacles.add(new Obstacle(2, playerY, 0)); // 不同车道
        obstacles.add(new Obstacle(1, 5, 0)); // 同车道但远离

        // 不应该碰撞
        assertFalse(collisionSystem.checkCollision(player, obstacles, SCREEN_HEIGHT));

        // 添加一个会碰撞的障碍物
        obstacles.add(new Obstacle(1, playerY - 2, 0));

        // 应该检测到碰撞
        assertTrue(collisionSystem.checkCollision(player, obstacles, SCREEN_HEIGHT));
    }

    @Test
    void testNormalState_CannotAvoidAnyObstacle() {
        player.setLane(1);
        int playerY = player.calculateY(SCREEN_HEIGHT);

        // 正常状态下，无论是低障碍物还是高障碍物都会碰撞
        Obstacle lowObstacle = new Obstacle(1, playerY - 2, 0);
        obstacles.add(lowObstacle);
        assertTrue(collisionSystem.checkCollision(player, obstacles, SCREEN_HEIGHT));

        obstacles.clear();
        Obstacle highObstacle = new Obstacle(1, playerY - 2, 1);
        obstacles.add(highObstacle);
        assertTrue(collisionSystem.checkCollision(player, obstacles, SCREEN_HEIGHT));
    }

    @Test
    void testCollisionWithMultipleObstaclesInDifferentLanes() {
        player.setLane(1);
        int playerY = player.calculateY(SCREEN_HEIGHT);

        // 在玩家车道前后添加障碍物
        obstacles.add(new Obstacle(0, playerY, 0));
        obstacles.add(new Obstacle(2, playerY, 0));

        // 不应该碰撞
        assertFalse(collisionSystem.checkCollision(player, obstacles, SCREEN_HEIGHT));
    }
}
