package com.hakimi.road.util;

/**
 * 游戏配置类
 * 包含所有游戏常量和配置参数
 */
public class GameConfig {
    // 道路配置
    public static final int ROAD_WIDTH = 3; // 3条车道
    public static final int PLAYER_START_LANE = 1; // 中间车道
    public static final int ROAD_MARGIN = 10; // 道路左右边距（用于菜单等2D界面）
    public static final int ROAD_BOTTOM_WIDTH = 56; // 伪3D道路在屏幕底部的宽度
    public static final int ROAD_TOP_WIDTH = 12; // 伪3D道路在屏幕顶部的宽度
    public static final int HORIZON_OFFSET = 1; // 路面与地平线的间距（从顶部开始，充分利用屏幕空间）

    /**
     * 计算指定行的道路宽度（用于伪3D效果）
     */
    public static int getRoadWidthAtRow(int screenHeight, int row) {
        float depth = Math.min(1f, Math.max(0f, (float) row / (screenHeight - HORIZON_OFFSET)));
        return (int) (ROAD_TOP_WIDTH + (ROAD_BOTTOM_WIDTH - ROAD_TOP_WIDTH) * depth);
    }

    /**
     * 计算指定行的道路左边界
     */
    public static int getRoadLeftAtRow(int screenWidth, int screenHeight, int row) {
        int roadWidth = getRoadWidthAtRow(screenHeight, row);
        int center = screenWidth / 2;
        return center - roadWidth / 2;
    }

    /**
     * 计算指定行指定车道的中心x坐标
     */
    public static int calculateLaneX(int screenWidth, int screenHeight, int laneIndex, int row) {
        int roadWidth = getRoadWidthAtRow(screenHeight, row);
        int roadLeft = getRoadLeftAtRow(screenWidth, screenHeight, row);
        float laneWidth = (float) roadWidth / ROAD_WIDTH;
        float centerOffset = laneWidth * laneIndex + laneWidth / 2f;
        return Math.round(roadLeft + centerOffset);
    }

    // 游戏速度配置
    public static final int OBSTACLE_SPAWN_RATE = 10; // 障碍物生成频率
    public static final int BASE_GAME_SPEED = 1; // 基础游戏速度
    public static final int SPEED_INCREASE_INTERVAL = 50; // 速度增加间隔（分数）

    // 玩家配置
    public static final int PLAYER_HEIGHT = 3; // 玩家高度
    public static final int PLAYER_WIDTH = 9; // 玩家宽度

    // 障碍物配置
    public static final int OBSTACLE_TYPES = 2; // 障碍物类型数量
    public static final int OBSTACLE_HEIGHT = 3; // 障碍物高度

    // 分数配置
    public static final int SCORE_PER_DISTANCE = 10; // 每10单位距离得1分
    public static final int SCORE_PER_OBSTACLE = 5; // 成功躲避障碍物得分

    // 游戏循环配置
    public static final int GAME_LOOP_DELAY_MS = 50; // 游戏循环延迟（毫秒），20fps

    // 动作持续时间
    public static final int JUMP_DURATION = 20; // 跳跃持续20帧 (1秒)
    public static final int SLIDE_DURATION = 20; // 滑铲持续20帧 (1秒)

    // 终端配置
    public static final int TERMINAL_WIDTH = 80; // 终端宽度
    public static final int TERMINAL_HEIGHT = 40; // 终端高度（增加高度以显示更长的跑道）

    // 动画配置
    public static final int ANIMATION_FRAME_INTERVAL = 5; // 动画帧间隔

    private GameConfig() {
        // 工具类，不允许实例化
    }
}
