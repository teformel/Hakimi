package com.hakimi.road.entity;

import com.hakimi.road.util.GameConfig;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 玩家实体类
 * 表示游戏中的玩家角色（哈基米）
 */
public class Player {
    private static final Logger logger = LogManager.getLogger(Player.class);
    private int lane; // 当前所在车道(0,1,2)
    private int y; // 玩家y坐标（从底部向上）
    private PlayerState state; // 玩家状态
    private int stateTimer; // 状态计时器
    private int verticalOffset; // 垂直偏移量（用于跳跃，正值表示向上）
    private int driedFishCount; // 小鱼干数量
    private boolean hasHagenAbility; // 是否拥有哈根能力
    private int health; // 当前血量
    private int maxHealth; // 最大血量
    private int invincibilityTimer; // 无敌时间计时器

    public enum PlayerState {
        NORMAL, // 正常状态
        JUMPING, // 跳跃状态
        SLIDING // 滑铲状态
    }

    public Player() {
        this.lane = GameConfig.PLAYER_START_LANE;
        this.state = PlayerState.NORMAL;
        this.stateTimer = 0;
        this.verticalOffset = 0;
        this.driedFishCount = 0;
        this.hasHagenAbility = false;
        this.maxHealth = 3;
        this.health = this.maxHealth;
        this.invincibilityTimer = 0;
        logger.debug("Player创建: 初始车道={}", lane);
    }

    /**
     * 移动到指定车道
     */
    public void moveToLane(int newLane) {
        if (newLane >= 0 && newLane < GameConfig.ROAD_WIDTH) {
            int oldLane = this.lane;
            this.lane = newLane;
            logger.debug("玩家切换车道: {} -> {}", oldLane, newLane);
        }
    }

    /**
     * 开始跳跃
     */
    public void jump() {
        if (state == PlayerState.NORMAL) {
            state = PlayerState.JUMPING;
            stateTimer = GameConfig.JUMP_DURATION; // 跳跃持续20帧
            verticalOffset = 0; // 重置垂直偏移
            logger.debug("玩家跳跃");
        }
    }

    /**
     * 开始滑铲
     */
    public void slide() {
        if (state == PlayerState.NORMAL) {
            state = PlayerState.SLIDING;
            state = PlayerState.SLIDING;
            stateTimer = GameConfig.SLIDE_DURATION; // 滑铲持续20帧
            logger.debug("玩家滑铲");
        }
    }

    /**
     * 更新玩家状态
     */
    public void update() {
        if (stateTimer > 0) {
            stateTimer--;
            // 更新垂直偏移量（用于跳跃）
            if (state == PlayerState.JUMPING) {
                int jumpProgress = GameConfig.JUMP_DURATION - stateTimer;
                // 抛物线公式：h = maxHeight * 4 * (t/T) * (1 - t/T)
                // 使用更平滑的抛物线，最大跳跃高度增加到8行，让跳跃效果更明显
                float normalizedProgress = jumpProgress / (float) GameConfig.JUMP_DURATION; // 0.0到1.0
                verticalOffset = (int) (8.0f * 4.0f * normalizedProgress * (1.0f - normalizedProgress)); // 最大跳跃8行
            } else {
                verticalOffset = 0;
            }
            if (stateTimer == 0) {
                logger.trace("玩家状态恢复: {} -> NORMAL", state);
                state = PlayerState.NORMAL;
                verticalOffset = 0;
            }
        }

        // 更新无敌时间
        if (invincibilityTimer > 0) {
            invincibilityTimer--;
        }
    }

    /**
     * 计算玩家在屏幕上的y坐标（伪3D透视）
     * 在伪3D中，Y坐标越小越靠近地平线（越远），Y坐标越大越靠近屏幕底部（越近）
     * 跳跃时保持在同一深度（baseY不变），垂直偏移在渲染时应用
     */
    public int calculateY(int screenHeight) {
        // 基础位置在屏幕底部附近，保持在同一深度
        int baseY = screenHeight - GameConfig.PLAYER_HEIGHT - 1;

        if (state == PlayerState.SLIDING) {
            // 滑铲时：稍微向下（更靠近屏幕底部）
            return Math.min(baseY + 1, screenHeight - 2);
        }

        // 正常和跳跃状态都返回baseY，垂直偏移在渲染时应用
        return baseY;
    }

    /**
     * 获取跳跃进度（0.0到1.0），用于计算透视缩放
     */
    public float getJumpProgress() {
        if (state == PlayerState.JUMPING) {
            return (GameConfig.JUMP_DURATION - stateTimer) / (float) GameConfig.JUMP_DURATION;
        }
        return 0.0f;
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

    public PlayerState getState() {
        return state;
    }

    public boolean isJumping() {
        return state == PlayerState.JUMPING;
    }

    public boolean isSliding() {
        return state == PlayerState.SLIDING;
    }

    public boolean isNormal() {
        return state == PlayerState.NORMAL;
    }

    public void setState(PlayerState newState) {
        this.state = newState;
    }

    public void setStateTimer(int timer) {
        this.stateTimer = timer;
    }

    public int getStateTimer() {
        return stateTimer;
    }

    /**
     * 获取垂直偏移量（用于渲染）
     */
    public int getVerticalOffset() {
        return verticalOffset;
    }

    public void setStateFromString(String stateStr) {
        try {
            this.state = PlayerState.valueOf(stateStr);
        } catch (IllegalArgumentException e) {
            this.state = PlayerState.NORMAL;
        }
    }

    // Dried Fish Methods
    public int getDriedFishCount() {
        return driedFishCount;
    }

    public void addDriedFish(int amount) {
        this.driedFishCount += amount;
    }

    public void setDriedFishCount(int count) {
        this.driedFishCount = count;
    }

    // Hagen Ability Methods
    public boolean hasHagenAbility() {
        return hasHagenAbility;
    }

    public void setHagenAbility(boolean hasAbility) {
        this.hasHagenAbility = hasAbility;
    }

    public void consumeHagen() {
        if (this.hasHagenAbility) {
            this.hasHagenAbility = false;
            logger.info("玩家使用了哈根能力！");
        }
    }

    // Health Methods
    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public void damage() {
        if (invincibilityTimer == 0 && health > 0) {
            health--;
            invincibilityTimer = GameConfig.FPS * 2; // 2秒无敌时间 (假设FPS为30-60，这里给一个大致值，后续调整)
            logger.info("玩家受伤: 剩余血量={}", health);
        }
    }

    public boolean isInvincible() {
        return invincibilityTimer > 0;
    }

    public int getInvincibilityTimer() {
        return invincibilityTimer;
    }

    public void setInvincibilityTimer(int timer) {
        this.invincibilityTimer = timer;
    }
}
