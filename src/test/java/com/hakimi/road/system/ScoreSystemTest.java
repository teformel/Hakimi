package com.hakimi.road.system;

import com.hakimi.road.util.GameConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ScoreSystem 类的单元测试
 */
class ScoreSystemTest {

    private ScoreSystem scoreSystem;

    @BeforeEach
    void setUp() {
        scoreSystem = new ScoreSystem();
    }

    @Test
    void testInitialState() {
        assertEquals(0, scoreSystem.getScore());
        assertEquals(0, scoreSystem.getDistance());
        assertEquals(0, scoreSystem.getCombo());
    }

    @Test
    void testUpdate() {
        int gameSpeed = 5;
        scoreSystem.update(gameSpeed);

        assertEquals(gameSpeed, scoreSystem.getDistance());
        assertEquals(gameSpeed / GameConfig.SCORE_PER_DISTANCE, scoreSystem.getScore());

        // 再次更新
        scoreSystem.update(gameSpeed);
        assertEquals(gameSpeed * 2, scoreSystem.getDistance());
    }

    @Test
    void testObstacleAvoided() {
        int initialScore = scoreSystem.getScore();
        int initialCombo = scoreSystem.getCombo();

        scoreSystem.obstacleAvoided();

        assertEquals(initialScore + GameConfig.SCORE_PER_OBSTACLE, scoreSystem.getScore());
        assertEquals(initialCombo + 1, scoreSystem.getCombo());
    }

    @Test
    void testComboBonus() {
        // 建立连击
        for (int i = 0; i < 6; i++) {
            scoreSystem.obstacleAvoided();
        }

        int scoreBeforeBonus = scoreSystem.getScore();
        int combo = scoreSystem.getCombo();

        // 第7次躲避应该有连击奖励
        scoreSystem.obstacleAvoided();
        int expectedBonus = (combo + 1) / 5; // 新的combo是7，7/5=1
        int expectedScore = scoreBeforeBonus + GameConfig.SCORE_PER_OBSTACLE + expectedBonus;

        assertEquals(expectedScore, scoreSystem.getScore());
        assertEquals(7, scoreSystem.getCombo());
    }

    @Test
    void testResetCombo() {
        // 建立连击
        for (int i = 0; i < 5; i++) {
            scoreSystem.obstacleAvoided();
        }
        assertEquals(5, scoreSystem.getCombo());

        // 重置连击
        scoreSystem.resetCombo();
        assertEquals(0, scoreSystem.getCombo());
    }

    @Test
    void testReset() {
        // 设置一些值
        scoreSystem.update(100);
        scoreSystem.obstacleAvoided();
        scoreSystem.obstacleAvoided();

        assertTrue(scoreSystem.getScore() > 0);
        assertTrue(scoreSystem.getDistance() > 0);
        assertTrue(scoreSystem.getCombo() > 0);

        // 重置
        scoreSystem.reset();

        assertEquals(0, scoreSystem.getScore());
        assertEquals(0, scoreSystem.getDistance());
        assertEquals(0, scoreSystem.getCombo());
    }

    @Test
    void testSetters() {
        scoreSystem.setScore(1000);
        assertEquals(1000, scoreSystem.getScore());

        scoreSystem.setDistance(500);
        assertEquals(500, scoreSystem.getDistance());

        scoreSystem.setCombo(10);
        assertEquals(10, scoreSystem.getCombo());
    }

    @Test
    void testScoreCalculation() {
        // 测试分数计算公式
        int distance = 100;
        scoreSystem.setDistance(distance);
        scoreSystem.update(0); // 更新以触发分数计算

        int expectedScore = distance / GameConfig.SCORE_PER_DISTANCE;
        assertEquals(expectedScore, scoreSystem.getScore());
    }

    @Test
    void testComboPersistence() {
        // 测试连击在多次躲避后的累积
        for (int i = 1; i <= 10; i++) {
            scoreSystem.obstacleAvoided();
            assertEquals(i, scoreSystem.getCombo());
        }
    }
}
