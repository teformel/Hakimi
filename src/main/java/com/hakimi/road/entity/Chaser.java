package com.hakimi.road.entity;

/**
 * 追逐者实体类
 * 表示游戏中追逐玩家的角色
 */
public class Chaser {
    private int y; // 追逐者y坐标
    private int distance; // 与玩家的距离
    
    public Chaser() {
        this.y = 0;
        this.distance = 0;
    }
    
    /**
     * 更新追逐者位置
     */
    public void update(int gameSpeed, int screenHeight) {
        // 追逐者跟随玩家移动，但保持一定距离
        distance += gameSpeed;
    }
    
    /**
     * 计算追逐者在屏幕上的y坐标
     */
    public int calculateY(int screenHeight, int playerY) {
        // 追逐者在玩家上方一定距离
        return playerY - 10;
    }
    
    /**
     * 检查是否追上玩家
     */
    public boolean hasCaughtPlayer(int playerY) {
        return y >= playerY;
    }
    
    // Getters and Setters
    public int getY() {
        return y;
    }
    
    public void setY(int y) {
        this.y = y;
    }
    
    public int getDistance() {
        return distance;
    }
}

