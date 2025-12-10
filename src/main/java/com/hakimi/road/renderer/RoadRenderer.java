package com.hakimi.road.renderer;

import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.hakimi.road.level.Level;
import com.hakimi.road.util.GameConfig;

/**
 * 道路渲染器
 * 负责渲染天空、草地和道路
 */
public class RoadRenderer {

    public void render(TextGraphics tg, int width, int height, int distance, Level level) {
        // 地平线
        int horizonY = GameConfig.HORIZON_OFFSET;

        // 绘制天空
        tg.setBackgroundColor(level.getSkyColor());
        for (int x = 0; x < width; x++) {
            tg.putString(x, horizonY, " "); // Cloud or empty sky
        }
        tg.setBackgroundColor(TextColor.ANSI.BLACK);

        for (int y = horizonY + 1; y < height; y++) {
            int roadWidth = GameConfig.getRoadWidthAtRow(height, y);
            int roadLeft = GameConfig.getRoadLeftAtRow(width, height, y);
            int roadRight = Math.min(width - 1, roadLeft + roadWidth);
            int clampedLeft = Math.max(0, roadLeft);
            int clampedRight = Math.max(clampedLeft + 1, Math.min(width - 1, roadRight));

            // 绘制草地（道路两侧）
            tg.setBackgroundColor(level.getGrassColor());
            if (clampedLeft > 0) {
                tg.drawLine(0, y, clampedLeft - 1, y, ' ');
            }
            if (clampedRight < width - 1) {
                tg.drawLine(clampedRight + 1, y, width - 1, y, ' ');
            }
            tg.setBackgroundColor(TextColor.ANSI.BLACK);

            // 绘制道路边界
            tg.setForegroundColor(TextColor.ANSI.WHITE);
            tg.putString(clampedLeft, y, "/");
            tg.putString(clampedRight, y, "\\");

            // 车道分隔线（伪3D透视）
            for (int i = 1; i < GameConfig.ROAD_WIDTH; i++) {
                int prevLane = GameConfig.calculateLaneX(width, height, i - 1, y);
                int nextLane = GameConfig.calculateLaneX(width, height, i, y);
                int laneDivider = (prevLane + nextLane) / 2;
                if ((y + distance) % 4 < 2 && laneDivider > clampedLeft && laneDivider < clampedRight) {
                    tg.putString(laneDivider, y, "|");
                }
            }

            // 地面纹理
            tg.setForegroundColor(level.getRoadColor());
            if ((y + distance) % 6 < 3) {
                for (int fillX = clampedLeft + 1; fillX < clampedRight; fillX += 2) {
                    tg.putString(fillX, y, ".");
                }
            }
            tg.setForegroundColor(TextColor.ANSI.WHITE);
        }
    }
}
