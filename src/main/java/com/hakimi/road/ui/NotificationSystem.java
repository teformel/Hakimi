package com.hakimi.road.ui;

import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.hakimi.road.system.Achievement;

import java.util.LinkedList;
import java.util.Queue;

/**
 * 通知系统
 * 负责管理和显示游戏内的通知（如成就解锁）
 */
public class NotificationSystem {
    private static class Notification {
        String title;
        String message;
        String icon;
        long startTime;
        long duration;
        TextColor color;

        public Notification(String title, String message, String icon, long duration, TextColor color) {
            this.title = title;
            this.message = message;
            this.icon = icon;
            this.duration = duration;
            this.color = color;
            this.startTime = System.currentTimeMillis();
        }
    }

    private Queue<Notification> notificationQueue;
    private Notification currentNotification;
    private static final long DEFAULT_DURATION = 4000; // 4秒

    public NotificationSystem() {
        this.notificationQueue = new LinkedList<>();
    }

    /**
     * 显示成就解锁通知
     */
    public void showAchievementUnlock(Achievement achievement) {
        addNotification(
                "成就解锁!",
                achievement.getTitle(),
                achievement.getIcon(),
                DEFAULT_DURATION,
                TextColor.ANSI.YELLOW);
    }

    /**
     * 添加通用通知
     */
    public void addNotification(String title, String message, String icon, long duration, TextColor color) {
        notificationQueue.offer(new Notification(title, message, icon, duration, color));
    }

    /**
     * 更新通知状态
     */
    public void update() {
        if (currentNotification == null && !notificationQueue.isEmpty()) {
            currentNotification = notificationQueue.poll();
            currentNotification.startTime = System.currentTimeMillis();
        }

        if (currentNotification != null) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - currentNotification.startTime > currentNotification.duration) {
                currentNotification = null;
            }
        }
    }

    /**
     * 渲染通知
     */
    public void render(TextGraphics tg, int screenWidth, int screenHeight) {
        if (currentNotification == null) {
            return;
        }

        // 绘制类似Steam的弹窗
        // 位置：右下角
        int width = 30;
        int height = 5;
        int x = screenWidth - width - 2;
        int y = screenHeight - height - 1;

        // 绘制背景框
        tg.setBackgroundColor(TextColor.ANSI.BLUE);
        tg.setForegroundColor(TextColor.ANSI.WHITE);

        // 填充背景
        for (int i = 0; i < height; i++) {
            tg.drawLine(x, y + i, x + width - 1, y + i, ' ');
        }

        // 绘制边框
        tg.drawLine(x, y, x + width - 1, y, '─');
        tg.drawLine(x, y + height - 1, x + width - 1, y + height - 1, '─');
        tg.drawLine(x, y, x, y + height - 1, '│');
        tg.drawLine(x + width - 1, y, x + width - 1, y + height - 1, '│');

        // 角落
        tg.setCharacter(x, y, '┌');
        tg.setCharacter(x + width - 1, y, '┐');
        tg.setCharacter(x, y + height - 1, '└');
        tg.setCharacter(x + width - 1, y + height - 1, '┘');

        // 绘制图标
        tg.putString(x + 2, y + 2, currentNotification.icon);

        // 绘制标题
        tg.setForegroundColor(currentNotification.color);
        tg.putString(x + 6, y + 1, currentNotification.title);

        // 绘制内容
        tg.setForegroundColor(TextColor.ANSI.WHITE);
        tg.putString(x + 6, y + 3, currentNotification.message);
    }
}
