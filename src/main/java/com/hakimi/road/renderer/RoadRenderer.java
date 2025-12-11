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

    public void render(TextGraphics tg, int width, int height, int distance, Level level, double curvature) {
        // 地平线
        int horizonY = GameConfig.HORIZON_OFFSET;

        // 绘制天空
        tg.setBackgroundColor(level.getSkyColor());
        for (int x = 0; x < width; x++) {
            tg.putString(x, horizonY, " "); // Cloud or empty sky
        }
        tg.setBackgroundColor(TextColor.ANSI.BLACK);

        for (int y = horizonY + 1; y < height; y++) {
            // 计算弯曲偏移量
            int curveOffset = GameConfig.calculateCurvatureOffset(height, y, curvature);

            int roadWidth = GameConfig.getRoadWidthAtRow(height, y);
            int baseRoadLeft = GameConfig.getRoadLeftAtRow(width, height, y);

            // 应用弯曲偏移
            int roadLeft = baseRoadLeft + curveOffset;

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
                // 车道线也需要应用同样的偏移
                int prevLaneBase = GameConfig.calculateLaneX(width, height, i - 1, y);
                int nextLaneBase = GameConfig.calculateLaneX(width, height, i, y);
                int laneDividerBase = (prevLaneBase + nextLaneBase) / 2;

                // 这里我们要稍微hack一下，因为GameConfig计算的是基于屏幕中心的
                // 我们直接把偏移量加到计算出的x坐标上即可，因为我们平移了整个道路
                // 但是wait, baseRoadLeft也是调用的GameConfig...
                // GameConfig.calculateLaneX 使用 getRoadLeftAtRow
                // 我们已经计算了 roadLeft = baseRoadLeft + curveOffset
                // 所以车道线位置应该是 laneDividerBase - baseRoadLeft + roadLeft = laneDividerBase +
                // curveOffset

                int laneDivider = laneDividerBase + curveOffset;

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
