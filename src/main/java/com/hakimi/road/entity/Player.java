package com.hakimi.road.entity;

import com.hakimi.road.util.GameConfig;

/**
 * 玩家实体类
 * 表示游戏中的玩家角色（哈基米）
 */
public class Player {
    private int lane; // 当前所在车道(0,1,2)
    private int y; // 玩家y坐标（从底部向上）
    private PlayerState state; // 玩家状态
    private int stateTimer; // 状态计时器
    
    public enum PlayerState {
        NORMAL,    // 正常状态
        JUMPING,   // 跳跃状态
        SLIDING    // 滑铲状态
    }
    
    public Player() {
        this.lane = GameConfig.PLAYER_START_LANE;
        this.state = PlayerState.NORMAL;
        this.stateTimer = 0;
    }
    
    /**
     * 移动到指定车道
     */
    public void moveToLane(int newLane) {
        if (newLane >= 0 && newLane < GameConfig.ROAD_WIDTH) {
            this.lane = newLane;
        }
    }
    
    /**
     * 开始跳跃
     */
    public void jump() {
        if (state == PlayerState.NORMAL) {
            state = PlayerState.JUMPING;
            stateTimer = 10; // 跳跃持续10帧
        }
    }
    
    /**
     * 开始滑铲
     */
    public void slide() {
        if (state == PlayerState.NORMAL) {
            state = PlayerState.SLIDING;
            stateTimer = 10; // 滑铲持续10帧
        }
    }
    
    /**
     * 更新玩家状态
     */
    public void update() {
        if (stateTimer > 0) {
            stateTimer--;
            if (stateTimer == 0) {
                state = PlayerState.NORMAL;
            }
        }
    }
    
    /**
     * 计算玩家在屏幕上的y坐标
     */
    public int calculateY(int screenHeight) {
        int baseY = screenHeight - GameConfig.PLAYER_HEIGHT - 1;
        if (state == PlayerState.JUMPING) {
            // 跳跃时向上偏移
            int jumpOffset = Math.min(stateTimer, 5) * 2;
            return baseY - jumpOffset;
        } else if (state == PlayerState.SLIDING) {
            // 滑铲时向下偏移
            return baseY + 1;
        }
        return baseY;
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
}

