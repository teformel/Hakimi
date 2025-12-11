package com.hakimi.road.entity;

/**
 * 游戏实体基类
 * 所有可移动的游戏对象都应继承此类
 */
public abstract class GameEntity {
    protected int y;

    public GameEntity(int y) {
        this.y = y;
    }

    /**
     * 移动实体
     * 
     * @param speed 移动速度
     */
    public void move(int speed) {
        this.y += speed;
    }

    /**
     * 检查实体是否超出屏幕范围
     * 
     * @param screenHeight 屏幕高度
     * @return 如果超出返回true
     */
    public boolean isOutOfScreen(int screenHeight) {
        return y > screenHeight;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }
}
