package com.hakimi.road.system;

/**
 * 成就定义
 */
public enum Achievement {
    FIRST_STEP("first_step", "第一步", "迈出哈基米生涯的第一步", "★"),
    SPRINTER("sprinter", "短跑健将", "奔跑距离达到 100 米", "⚡"),
    MARATHON("marathon", "马拉松", "奔跑距离达到 1000 米", "🏃"),
    OUCH("ouch", "哎哟！", "第一次撞到障碍物", "💥"),
    SURVIVOR("survivor", "幸存者", "在追逐者出现后存活 30 秒", "🛡️"),
    MASTER("master", "大师", "单局分数超过 5000 分", "👑");

    private final String id;
    private final String title;
    private final String description;
    private final String icon;

    Achievement(String id, String title, String description, String icon) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.icon = icon;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getIcon() {
        return icon;
    }
}
