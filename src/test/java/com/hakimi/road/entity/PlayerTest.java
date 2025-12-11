package com.hakimi.road.entity;

import com.hakimi.road.util.GameConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Player 类的单元测试
 */
class PlayerTest {

    private Player player;

    @BeforeEach
    void setUp() {
        player = new Player();
    }

    @Test
    void testInitialState() {
        assertEquals(GameConfig.PLAYER_START_LANE, player.getLane());
        assertEquals(Player.PlayerState.NORMAL, player.getState());
        assertEquals(0, player.getStateTimer());
        assertEquals(0, player.getVerticalOffset());
        assertTrue(player.isNormal());
        assertFalse(player.isJumping());
        assertFalse(player.isSliding());
    }

    @Test
    void testMoveToLane() {
        // 测试有效移动
        player.moveToLane(0);
        assertEquals(0, player.getLane());

        player.moveToLane(2);
        assertEquals(2, player.getLane());

        // 测试边界情况
        player.moveToLane(-1); // 无效，应保持原位
        assertEquals(2, player.getLane());

        player.moveToLane(GameConfig.ROAD_WIDTH); // 超出范围
        assertEquals(2, player.getLane());
    }

    @Test
    void testJump() {
        // 正常状态下可以跳跃
        player.jump();
        assertTrue(player.isJumping());
        assertEquals(GameConfig.JUMP_DURATION, player.getStateTimer());

        // 跳跃状态下不能再次跳跃
        player.jump();
        assertEquals(GameConfig.JUMP_DURATION, player.getStateTimer()); // 计时器不应重置
    }

    @Test
    void testSlide() {
        // 正常状态下可以滑铲
        player.slide();
        assertTrue(player.isSliding());
        assertEquals(GameConfig.SLIDE_DURATION, player.getStateTimer());

        // 滑铲状态下不能再次滑铲
        player.slide();
        assertEquals(GameConfig.SLIDE_DURATION, player.getStateTimer());
    }

    @Test
    void testUpdate() {
        // 测试跳跃状态更新
        player.jump();
        int initialTimer = player.getStateTimer();

        player.update();
        assertEquals(initialTimer - 1, player.getStateTimer());
        assertTrue(player.getVerticalOffset() > 0); // 跳跃时应有垂直偏移

        // 模拟跳跃完成
        for (int i = 0; i < GameConfig.JUMP_DURATION; i++) {
            player.update();
        }
        assertTrue(player.isNormal());
        assertEquals(0, player.getVerticalOffset());
    }

    @Test
    void testJumpVerticalOffset() {
        player.jump();

        // 跳跃过程中应该有垂直偏移
        player.update();
        int offset1 = player.getVerticalOffset();
        assertTrue(offset1 >= 0); // 在第一帧可能还是0或者很小，取决于公式

        // 跳跃中期偏移应该更大 (10帧后)
        for (int i = 0; i < 9; i++) {
            player.update();
        }
        int offset2 = player.getVerticalOffset();
        assertTrue(offset2 > offset1);

        // 跳跃结束时偏移应该回到0
        for (int i = 0; i < 10; i++) {
            player.update();
        }
        assertEquals(0, player.getVerticalOffset());
    }

    @Test
    void testCalculateY() {
        int screenHeight = 30;
        int baseY = player.calculateY(screenHeight);

        // 正常状态
        assertEquals(screenHeight - GameConfig.PLAYER_HEIGHT - 1, baseY);

        // 滑铲状态应该稍微向下
        player.slide();
        int slidingY = player.calculateY(screenHeight);
        assertTrue(slidingY >= baseY);

        // 跳跃状态的baseY应该保持不变（垂直偏移在渲染时应用）
        player = new Player();
        player.jump();
        int jumpingY = player.calculateY(screenHeight);
        assertEquals(baseY, jumpingY);
    }

    @Test
    void testGetJumpProgress() {
        // 正常状态下进度为0
        assertEquals(0.0f, player.getJumpProgress());

        // 跳跃状态下进度应该在0到1之间
        player.jump();
        float progress1 = player.getJumpProgress();
        assertEquals(0.0f, progress1, 0.01f);

        player.update();
        float progress2 = player.getJumpProgress();
        assertTrue(progress2 > progress1);
        assertTrue(progress2 <= 1.0f);
    }

    @Test
    void testSetStateFromString() {
        player.setStateFromString("JUMPING");
        assertEquals(Player.PlayerState.JUMPING, player.getState());

        player.setStateFromString("SLIDING");
        assertEquals(Player.PlayerState.SLIDING, player.getState());

        player.setStateFromString("NORMAL");
        assertEquals(Player.PlayerState.NORMAL, player.getState());

        // 无效字符串应该设置为NORMAL
        player.setStateFromString("INVALID");
        assertEquals(Player.PlayerState.NORMAL, player.getState());
    }

    @Test
    void testStateTransitions() {
        // 从正常到跳跃
        player.jump();
        assertTrue(player.isJumping());

        // 等待跳跃完成
        for (int i = 0; i < GameConfig.JUMP_DURATION; i++) {
            player.update();
        }
        assertTrue(player.isNormal());

        // 从正常到滑铲
        player.slide();
        assertTrue(player.isSliding());

        // 等待滑铲完成
        for (int i = 0; i < GameConfig.SLIDE_DURATION; i++) {
            player.update();
        }
        assertTrue(player.isNormal());
    }
}
