package com.hakimi.road.util;

/**
 * 游戏配置类
 * 包含所有游戏常量和配置参数
 */
public class GameConfig {
    // 道路配置
    public static final int ROAD_WIDTH = 3; // 3条车道
    public static final int PLAYER_START_LANE = 1; // 中间车道
    public static final int ROAD_MARGIN = 10; // 道路左右边距
    
    /**
     * 根据屏幕宽度计算车道x坐标
     * @param screenWidth 屏幕宽度
     * @return 车道x坐标数组
     */
    public static int[] calculateRoadLanes(int screenWidth) {
        int roadLeft = ROAD_MARGIN;
        int roadRight = screenWidth - ROAD_MARGIN;
        int roadWidth = roadRight - roadLeft;
        int[] lanes = new int[ROAD_WIDTH];
        
        // 计算每条车道的中心位置
        for (int i = 0; i < ROAD_WIDTH; i++) {
            lanes[i] = roadLeft + (roadWidth * (i + 1)) / (ROAD_WIDTH + 1);
        }
        return lanes;
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
    public static final int GAME_LOOP_DELAY_MS = 100; // 游戏循环延迟（毫秒），10fps
    
    // 终端配置
    public static final int TERMINAL_WIDTH = 80; // 终端宽度
    public static final int TERMINAL_HEIGHT = 30; // 终端高度
    
    // 动画配置
    public static final int ANIMATION_FRAME_INTERVAL = 5; // 动画帧间隔
    
    private GameConfig() {
        // 工具类，不允许实例化
    }
}

