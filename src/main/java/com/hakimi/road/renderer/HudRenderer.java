package com.hakimi.road.renderer;

import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.hakimi.road.entity.Player;
import com.hakimi.road.ui.NotificationSystem;
import com.hakimi.road.util.GameConfig;

/**
 * 界面渲染器 (Head-Up Display)
 * 负责渲染分数、生命值、通知等UI元素
 */
public class HudRenderer {
    private int lastScore = -1;
    private String scoreStr = "";
    private int lastDistance = -1;
    private String distanceStr = "";
    private int lastGameSpeed = -1;
    private String gameSpeedStr = "";
    private int lastDriedFish = -1;
    private String driedFishStr = "";

    public void renderHud(TextGraphics tg, int width, int height, Player player, int score, int distance,
            int gameSpeed) {
        // 绘制HUD（放在屏幕右侧，不占用跑道空间）
        int hudX = width - 20;

        if (score != lastScore) {
            scoreStr = "分数: " + score;
            lastScore = score;
        }
        tg.putString(hudX, 1, scoreStr);

        if (distance != lastDistance) {
            distanceStr = "距离: " + distance;
            lastDistance = distance;
        }
        tg.putString(hudX, 2, distanceStr);

        if (gameSpeed != lastGameSpeed) {
            gameSpeedStr = "速度: " + gameSpeed;
            lastGameSpeed = gameSpeed;
        }
        tg.putString(hudX, 3, gameSpeedStr);

        int currentDriedFish = player.getDriedFishCount();
        if (currentDriedFish != lastDriedFish) {
            driedFishStr = "小鱼干: " + currentDriedFish;
            lastDriedFish = currentDriedFish;
        }
        tg.putString(hudX, 4, driedFishStr);

        // 绘制血量
        tg.putString(hudX, 5, "血量: ");
        tg.setForegroundColor(TextColor.ANSI.RED);
        StringBuilder hearts = new StringBuilder();
        for (int i = 0; i < player.getMaxHealth(); i++) {
            if (i < player.getHealth()) {
                hearts.append("❤");
            } else {
                hearts.append("♡");
            }
        }
        tg.putString(hudX + 6, 5, hearts.toString());
        tg.setForegroundColor(TextColor.ANSI.WHITE);

        if (player.hasHagenAbility()) {
            tg.setForegroundColor(TextColor.ANSI.YELLOW);
            tg.putString(hudX, 7, "★ 哈根之力 ★");
            tg.setForegroundColor(TextColor.ANSI.WHITE);
        }

        // 绘制车道指示器（放在底部）
        for (int i = 0; i < GameConfig.ROAD_WIDTH; i++) {
            String indicator = (i == player.getLane()) ? "[★]" : "[ ]";
            int laneX = GameConfig.calculateLaneX(width, height, i, height - 1);
            tg.putString(laneX - 1, height - 1, indicator);
        }
    }

    public void renderNotifications(TextGraphics tg, int width, int height, NotificationSystem notificationSystem) {
        if (notificationSystem != null) {
            notificationSystem.render(tg, width, height);
            renderScreenFlash(tg, width, height, notificationSystem);
        }
    }

    private void renderScreenFlash(TextGraphics tg, int width, int height, NotificationSystem notificationSystem) {
        if (notificationSystem.getScreenFlashTimer() > 0) {
            tg.setBackgroundColor(notificationSystem.getScreenFlashColor());
            // 绘制边框
            tg.drawLine(0, 0, width - 1, 0, ' ');
            tg.drawLine(0, height - 1, width - 1, height - 1, ' ');
            tg.drawLine(0, 0, 0, height - 1, ' ');
            tg.drawLine(width - 1, 0, width - 1, height - 1, ' ');

            // 简单闪烁：绘制四周边框 (内圈)
            tg.drawLine(1, 1, width - 2, 1, ' ');
            tg.drawLine(1, height - 2, width - 2, height - 2, ' ');
            tg.drawLine(1, 1, 1, height - 2, ' ');
            tg.drawLine(width - 2, 1, width - 2, height - 2, ' ');
        }
    }
}
