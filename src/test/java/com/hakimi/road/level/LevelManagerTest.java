package com.hakimi.road.level;

import com.googlecode.lanterna.TextColor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LevelManagerTest {

    private LevelManager levelManager;

    @BeforeEach
    void setUp() {
        levelManager = new LevelManager();
    }

    @Test
    void testInitialLevel() {
        // 初始应该是森林
        Level current = levelManager.getCurrentLevel();
        assertNotNull(current);
        assertEquals("森林", current.getName());
        assertEquals(Level.ObstacleStyle.FOREST, current.getObstacleStyle());
        // 检查颜色
        assertEquals(TextColor.ANSI.CYAN, current.getSkyColor());
    }

    @Test
    void testTransitionToDesert() {
        // 999m 还是森林
        levelManager.update(999);
        assertEquals("森林", levelManager.getCurrentLevel().getName());

        // 1000m 变成沙漠
        levelManager.update(1000);
        Level current = levelManager.getCurrentLevel();
        assertEquals("沙漠", current.getName());
        assertEquals(Level.ObstacleStyle.DESERT, current.getObstacleStyle());
        assertEquals(TextColor.ANSI.YELLOW, current.getSkyColor());
    }

    @Test
    void testTransitionToCyberpunk() {
        // 1999m 还是沙漠
        levelManager.update(1999);
        assertEquals("沙漠", levelManager.getCurrentLevel().getName());

        // 2000m 变成赛博朋克
        levelManager.update(2000);
        Level current = levelManager.getCurrentLevel();
        assertEquals("夜之城", current.getName());
        assertEquals(Level.ObstacleStyle.CYBERPUNK, current.getObstacleStyle());
        assertEquals(TextColor.ANSI.BLACK, current.getSkyColor());
    }

    @Test
    void testBackwardsTransition() {
        // 测试如果距离减少（如果不允许倒退则不应该发生，但作为逻辑检查）
        levelManager.update(2000); // 先去赛博朋克
        assertEquals("夜之城", levelManager.getCurrentLevel().getName());

        levelManager.update(500); // 回到森林
        assertEquals("森林", levelManager.getCurrentLevel().getName());
    }
}
