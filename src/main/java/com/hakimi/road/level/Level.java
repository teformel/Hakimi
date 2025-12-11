package com.hakimi.road.level;

import com.googlecode.lanterna.TextColor;

/**
 * 关卡数据类
 * 包含关卡的主题颜色和风格配置
 */
public class Level {
    private String name;
    private TextColor skyColor;
    private TextColor grassColor;
    private TextColor roadColor;
    private ObstacleStyle obstacleStyle;

    public enum ObstacleStyle {
        FOREST, // 森林 (岩石/普通)
        DESERT, // 沙漠 (仙人掌/秃鹫)
        CYBERPUNK // 赛博朋克 (路障/无人机)
    }

    public Level(String name, TextColor skyColor, TextColor grassColor, TextColor roadColor,
            ObstacleStyle obstacleStyle) {
        this.name = name;
        this.skyColor = skyColor;
        this.grassColor = grassColor;
        this.roadColor = roadColor;
        this.obstacleStyle = obstacleStyle;
    }

    public String getName() {
        return name;
    }

    public TextColor getSkyColor() {
        return skyColor;
    }

    public TextColor getGrassColor() {
        return grassColor;
    }

    public TextColor getRoadColor() {
        return roadColor;
    }

    public ObstacleStyle getObstacleStyle() {
        return obstacleStyle;
    }
}
